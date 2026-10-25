/**
 * KASIR KEDAI POS KOPI - WEB POS & INVENTARIS ENGINE
 * Berjalan langsung di browser (Client-Side) dan kompatibel 100% dengan GitHub Pages.
 */

// ================= DATA AWAL DEFAULT (SINKRON DENGAN APLIKASI JAVA) =================
const DEFAULT_PRODUCTS = [
    { kode: "M01", kategori: "Makanan/Minuman", nama: "Kopi Susu Pos Aren", harga: 18000, stok: 25, info: "Exp: 2026-11-15" },
    { kode: "M02", kategori: "Makanan/Minuman", nama: "Espresso Double Shot", harga: 21000, stok: 30, info: "Exp: 2026-11-20" },
    { kode: "M03", kategori: "Makanan/Minuman", nama: "Croissant Cokelat Keju", harga: 35000, stok: 15, info: "Exp: 2026-11-10" },
    { kode: "M04", kategori: "Makanan/Minuman", nama: "Roti Toast Srikaya", harga: 45000, stok: 12, info: "Exp: 2026-11-12" },
    { kode: "E01", kategori: "Elektronik/Gadget", nama: "Electric Milk Frother", harga: 125000, stok: 10, info: "Garansi: 12 Bulan" },
    { kode: "E02", kategori: "Elektronik/Gadget", nama: "Digital Coffee Scale", harga: 450000, stok: 8, info: "Garansi: 12 Bulan" },
    { kode: "E03", kategori: "Elektronik/Gadget", nama: "Mesin Espresso Portable", harga: 650000, stok: 6, info: "Garansi: 24 Bulan" },
    { kode: "E04", kategori: "Elektronik/Gadget", nama: "Manual Coffee Grinder", harga: 85000, stok: 20, info: "Garansi: 36 Bulan" }
];

// ================= STATE SISTEM =================
let products = JSON.parse(localStorage.getItem("pos_products")) || DEFAULT_PRODUCTS;
let cart = [];
let historyList = JSON.parse(localStorage.getItem("pos_history")) || [];
let currentCategory = "all";
let notaSequence = parseInt(localStorage.getItem("pos_nota_seq") || "1", 10);

function saveProducts() {
    localStorage.setItem("pos_products", JSON.stringify(products));
}

function saveHistory() {
    localStorage.setItem("pos_history", JSON.stringify(historyList));
}

function formatRupiah(num) {
    return "Rp " + Math.round(num).toLocaleString("id-ID");
}

function getTodayString() {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    return `${y}${m}${d}`;
}

function getFormattedDateTime(date = new Date()) {
    const d = String(date.getDate()).padStart(2, '0');
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const y = date.getFullYear();
    const h = String(date.getHours()).padStart(2, '0');
    const min = String(date.getMinutes()).padStart(2, '0');
    const s = String(date.getSeconds()).padStart(2, '0');
    return `${d}/${m}/${y} ${h}:${min}:${s}`;
}

function generateNotaNumber() {
    return `TRX-${getTodayString()}-${String(notaSequence).padStart(3, '0')}`;
}

// ================= LOGIKA DISKON POLIMORFIS (REPLIKASI OOP) =================
/**
 * 1. Makanan/Minuman: Pembelian grosir (qty >= 5) dapat diskon 10%
 * 2. Elektronik/Alat: Total belanja item >= 500.000 dapat diskon 5%
 */
function calculateItemDiscount(product, qty) {
    const subtotal = product.harga * qty;
    if (product.kategori === "Makanan/Minuman") {
        if (qty >= 5) {
            return subtotal * 0.10;
        }
    } else if (product.kategori === "Elektronik/Gadget") {
        if (subtotal >= 500000) {
            return subtotal * 0.05;
        }
    }
    return 0;
}

// ================= RENDER KATALOG POS =================
function renderCatalog() {
    const grid = document.getElementById("product-grid");
    const keyword = document.getElementById("pos-search").value.toLowerCase().trim();

    grid.innerHTML = "";

    const filtered = products.filter(p => {
        const matchCat = (currentCategory === "all") || (p.kategori === currentCategory);
        const matchKey = p.nama.toLowerCase().includes(keyword) || p.kode.toLowerCase().includes(keyword);
        return matchCat && matchKey;
    });

    if (filtered.length === 0) {
        grid.innerHTML = `<div class="empty-state" style="grid-column: 1/-1;">
            <p>Tidak ada produk ditemukan.</p>
            <span>Coba ubah kata kunci atau kategori filter.</span>
        </div>`;
        return;
    }

    filtered.forEach(p => {
        const isFood = p.kategori === "Makanan/Minuman";
        const badgeClass = isFood ? "badge-food" : "badge-tool";
        const badgeLabel = isFood ? "Makanan/Kopi" : "Alat Kopi";
        const outOfStock = p.stok <= 0;

        const card = document.createElement("div");
        card.className = "product-card";
        card.innerHTML = `
            <div class="card-top">
                <span class="card-category-badge ${badgeClass}">${badgeLabel}</span>
                <h3 class="card-title">${p.nama}</h3>
                <span class="card-info">${p.info}</span>
            </div>
            <div class="card-bottom-wrap">
                <div class="card-bottom">
                    <span class="card-price">${formatRupiah(p.harga)}</span>
                    <span class="card-stock" style="color: ${p.stok <= 5 ? '#B91C1C' : '#57534E'}">Stok: ${p.stok}</span>
                </div>
                <button class="btn-add-cart" data-kode="${p.kode}" ${outOfStock ? "disabled" : ""}>
                    ${outOfStock ? "Habis" : "+ Tambah Pesanan"}
                </button>
            </div>
        `;

        card.querySelector(".btn-add-cart").addEventListener("click", () => addToCart(p.kode));
        grid.appendChild(card);
    });
}

// ================= MANAJEMEN KERANJANG =================
function addToCart(kode) {
    const product = products.find(p => p.kode === kode);
    if (!product) return;

    const existing = cart.find(c => c.product.kode === kode);
    const currentQtyInCart = existing ? existing.qty : 0;

    if (currentQtyInCart + 1 > product.stok) {
        alert(`Stok tidak mencukupi! Stok ${product.nama} hanya tersisa ${product.stok}`);
        return;
    }

    if (existing) {
        existing.qty += 1;
    } else {
        cart.push({ product: product, qty: 1 });
    }

    renderCart();
}

function updateCartQty(kode, delta) {
    const item = cart.find(c => c.product.kode === kode);
    if (!item) return;

    if (delta > 0) {
        if (item.qty + 1 > item.product.stok) {
            alert(`Stok tidak mencukupi! Stok tersisa: ${item.product.stok}`);
            return;
        }
        item.qty += 1;
    } else {
        item.qty -= 1;
        if (item.qty <= 0) {
            cart = cart.filter(c => c.product.kode !== kode);
        }
    }
    renderCart();
}

function removeCartItem(kode) {
    cart = cart.filter(c => c.product.kode !== kode);
    renderCart();
}

function clearCart() {
    if (cart.length === 0) return;
    if (confirm("Kosongkan keranjang pesanan saat ini?")) {
        cart = [];
        document.getElementById("input-bayar").value = "";
        renderCart();
    }
}

function renderCart() {
    const container = document.getElementById("cart-items");
    const subtotalEl = document.getElementById("cart-subtotal");
    const discountEl = document.getElementById("cart-discount");
    const grandTotalEl = document.getElementById("cart-grand-total");

    if (cart.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <p>Keranjang masih kosong.</p>
                <span>Pilih item di katalog untuk menambahkan pesanan.</span>
            </div>
        `;
        subtotalEl.textContent = "Rp 0";
        discountEl.textContent = "-Rp 0";
        grandTotalEl.textContent = "Rp 0";
        calculateChange();
        return;
    }

    container.innerHTML = "";
    let totalSubtotal = 0;
    let totalDiscount = 0;

    cart.forEach(item => {
        const itemSubtotal = item.product.harga * item.qty;
        const itemDiscount = calculateItemDiscount(item.product, item.qty);
        totalSubtotal += itemSubtotal;
        totalDiscount += itemDiscount;

        const row = document.createElement("div");
        row.className = "cart-item-row";
        row.innerHTML = `
            <div class="cart-item-info">
                <div class="cart-item-title">${item.product.nama}</div>
                <div class="cart-item-sub">${formatRupiah(item.product.harga)} x ${item.qty} = ${formatRupiah(itemSubtotal)}</div>
                ${itemDiscount > 0 ? `<div class="cart-item-discount">* Diskon Polimorfis: -${formatRupiah(itemDiscount)}</div>` : ''}
            </div>
            <div class="cart-item-controls">
                <button class="qty-btn" data-action="minus" data-kode="${item.product.kode}">-</button>
                <span class="qty-val">${item.qty}</span>
                <button class="qty-btn" data-action="plus" data-kode="${item.product.kode}">+</button>
                <button class="btn-remove-item" data-kode="${item.product.kode}" title="Hapus">✕</button>
            </div>
        `;

        row.querySelector('[data-action="minus"]').addEventListener("click", () => updateCartQty(item.product.kode, -1));
        row.querySelector('[data-action="plus"]').addEventListener("click", () => updateCartQty(item.product.kode, 1));
        row.querySelector('.btn-remove-item').addEventListener("click", () => removeCartItem(item.product.kode));

        container.appendChild(row);
    });

    const grandTotal = totalSubtotal - totalDiscount;
    subtotalEl.textContent = formatRupiah(totalSubtotal);
    discountEl.textContent = "-" + formatRupiah(totalDiscount);
    grandTotalEl.textContent = formatRupiah(grandTotal);

    calculateChange();
}

function calculateChange() {
    const grandTotalEl = document.getElementById("cart-grand-total");
    const grandTotal = parseInt(grandTotalEl.textContent.replace(/[^0-9]/g, ""), 10) || 0;
    const inputVal = parseInt(document.getElementById("input-bayar").value, 10) || 0;
    const changeEl = document.getElementById("cart-change");

    if (inputVal === 0 && grandTotal === 0) {
        changeEl.textContent = "Rp 0";
        changeEl.classList.remove("negative");
        return;
    }

    const kembalian = inputVal - grandTotal;
    if (kembalian >= 0) {
        changeEl.textContent = formatRupiah(kembalian);
        changeEl.classList.remove("negative");
    } else {
        changeEl.textContent = "-" + formatRupiah(Math.abs(kembalian));
        changeEl.classList.add("negative");
    }
}

// ================= PROSES CHECKOUT & STRUK =================
function processCheckout() {
    if (cart.length === 0) {
        alert("Keranjang pesanan masih kosong!");
        return;
    }

    const grandTotalEl = document.getElementById("cart-grand-total");
    const grandTotal = parseInt(grandTotalEl.textContent.replace(/[^0-9]/g, ""), 10) || 0;
    const inputBayar = parseInt(document.getElementById("input-bayar").value, 10) || 0;

    if (inputBayar < grandTotal) {
        alert(`Uang pembayaran kurang! Total belanja: ${formatRupiah(grandTotal)}, baru dibayar: ${formatRupiah(inputBayar)}`);
        document.getElementById("input-bayar").focus();
        return;
    }

    const kembalian = inputBayar - grandTotal;
    const currentNota = generateNotaNumber();
    const waktuFormatted = getFormattedDateTime();

    // Kurangi stok riil
    cart.forEach(item => {
        const prod = products.find(p => p.kode === item.product.kode);
        if (prod) {
            prod.stok = Math.max(0, prod.stok - item.qty);
        }
    });
    saveProducts();

    // Buat data riwayat transaksi
    const transactionRecord = {
        nota: currentNota,
        waktu: waktuFormatted,
        itemCount: cart.reduce((acc, c) => acc + c.qty, 0),
        subtotal: cart.reduce((acc, c) => acc + (c.product.harga * c.qty), 0),
        diskon: cart.reduce((acc, c) => acc + calculateItemDiscount(c.product, c.qty), 0),
        grandTotal: grandTotal,
        bayar: inputBayar,
        kembalian: kembalian,
        items: [...cart]
    };

    historyList.unshift(transactionRecord);
    saveHistory();

    // Siapkan teks nota
    const receiptText = generateReceiptText(transactionRecord);
    showReceiptModal(receiptText);

    // Naikan nomor nota
    notaSequence++;
    localStorage.setItem("pos_nota_seq", notaSequence.toString());
    document.getElementById("active-nota").textContent = generateNotaNumber();

    // Reset keranjang
    cart = [];
    document.getElementById("input-bayar").value = "";
    renderCart();
    renderCatalog();
    renderCrudTable();
    renderHistoryTable();
}

function generateReceiptText(t) {
    let str = "";
    str += "=========================================\n";
    str += "           KASIR KEDAI POS KOPI          \n";
    str += "         Spesialis Kopi & Santai         \n";
    str += "=========================================\n";
    str += `No. Nota  : ${t.nota}\n`;
    str += `Waktu     : ${t.waktu}\n`;
    str += "-----------------------------------------\n";
    str += "Item                Qty    Harga Subtotal\n";
    str += "-----------------------------------------\n";

    t.items.forEach(c => {
        let name = c.product.nama;
        if (name.length > 18) name = name.substring(0, 15) + "...";
        const padName = name.padEnd(18, ' ');
        const padQty = String(c.qty).padStart(4, ' ');
        const padPrice = String(c.product.harga).padStart(8, ' ');
        const padSub = String(c.product.harga * c.qty).padStart(7, ' ');
        str += `${padName} ${padQty} ${padPrice} ${padSub}\n`;

        const disc = calculateItemDiscount(c.product, c.qty);
        if (disc > 0) {
            str += `  * Diskon (${c.product.kategori}): -${formatRupiah(disc)}\n`;
        }
    });

    str += "-----------------------------------------\n";
    str += `Total Belanja : ${formatRupiah(t.subtotal).padStart(18, ' ')}\n`;
    str += `Total Diskon  : ${formatRupiah(t.diskon).padStart(18, ' ')}\n`;
    str += `Grand Total   : ${formatRupiah(t.grandTotal).padStart(18, ' ')}\n`;
    str += `Tunai/Bayar   : ${formatRupiah(t.bayar).padStart(18, ' ')}\n`;
    str += `Kembalian     : ${formatRupiah(t.kembalian).padStart(18, ' ')}\n`;
    str += "=========================================\n";
    str += "       Terima Kasih Atas Kunjungan Anda   \n";
    str += "      Barang yang dibeli tidak dapat     \n";
    str += "              dikembalikan               \n";
    str += "=========================================\n";
    return str;
}

function showReceiptModal(receiptText) {
    document.getElementById("receipt-text").textContent = receiptText;
    document.getElementById("receipt-modal").classList.add("show");
}

function closeReceiptModal() {
    document.getElementById("receipt-modal").classList.remove("show");
}

// ================= CRUD PRODUK =================
function renderCrudTable() {
    const tbody = document.getElementById("crud-table-body");
    const keyword = document.getElementById("crud-search").value.toLowerCase().trim();

    tbody.innerHTML = "";

    const filtered = products.filter(p => {
        return p.nama.toLowerCase().includes(keyword) || p.kode.toLowerCase().includes(keyword) || p.kategori.toLowerCase().includes(keyword);
    });

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding: 20px; color: #78716C;">Tidak ada produk.</td></tr>`;
        return;
    }

    filtered.forEach(p => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td><strong>${p.kode}</strong></td>
            <td>${p.kategori}</td>
            <td>${p.nama}</td>
            <td class="text-right">${formatRupiah(p.harga)}</td>
            <td class="text-right" style="font-weight: 700; color: ${p.stok <= 5 ? '#B91C1C' : '#1C1917'}">${p.stok}</td>
            <td>${p.info}</td>
            <td class="text-center">
                <div class="action-btn-row">
                    <button class="btn-tbl-edit" data-kode="${p.kode}">Edit</button>
                    <button class="btn-tbl-del" data-kode="${p.kode}">Hapus</button>
                </div>
            </td>
        `;

        tr.querySelector('.btn-tbl-edit').addEventListener("click", () => editProductForm(p.kode));
        tr.querySelector('.btn-tbl-del').addEventListener("click", () => deleteProduct(p.kode));
        tbody.appendChild(tr);
    });
}

function editProductForm(kode) {
    const p = products.find(prod => prod.kode === kode);
    if (!p) return;

    document.getElementById("form-mode").value = "edit";
    document.getElementById("form-title").textContent = `Edit Produk [${p.kode}]`;
    document.getElementById("btn-save-product").textContent = "✓ Simpan Perubahan";

    document.getElementById("p-kode").value = p.kode;
    document.getElementById("p-kode").readOnly = true;
    document.getElementById("p-kategori").value = p.kategori;
    document.getElementById("p-nama").value = p.nama;
    document.getElementById("p-harga").value = p.harga;
    document.getElementById("p-stok").value = p.stok;
    document.getElementById("p-info").value = p.info.replace("Exp: ", "").replace("Garansi: ", "");

    updateInfoKhususLabel();
    document.getElementById("p-nama").focus();
}

function resetProductForm() {
    document.getElementById("form-mode").value = "create";
    document.getElementById("form-title").textContent = "Tambah Produk Baru";
    document.getElementById("btn-save-product").textContent = "+ Tambah Produk";

    document.getElementById("product-form").reset();
    document.getElementById("p-kode").readOnly = false;
    updateInfoKhususLabel();
}

function deleteProduct(kode) {
    const p = products.find(prod => prod.kode === kode);
    if (!p) return;

    if (confirm(`Yakin ingin menghapus produk [${p.kode}] - ${p.nama}?`)) {
        products = products.filter(prod => prod.kode !== kode);
        saveProducts();
        renderCatalog();
        renderCrudTable();
    }
}

function updateInfoKhususLabel() {
    const cat = document.getElementById("p-kategori").value;
    const lbl = document.getElementById("lbl-info-khusus");
    const input = document.getElementById("p-info");

    if (cat === "Makanan/Minuman") {
        lbl.textContent = "Tanggal Kadaluarsa (Exp)";
        input.placeholder = "2026-12-31";
    } else {
        lbl.textContent = "Masa Garansi";
        input.placeholder = "12 Bulan";
    }
}

// ================= RIWAYAT TRANSAKSI =================
function renderHistoryTable() {
    const tbody = document.getElementById("history-table-body");
    const countEl = document.getElementById("history-count");

    countEl.textContent = `Total Transaksi: ${historyList.length}`;
    tbody.innerHTML = "";

    if (historyList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding: 24px; color: #78716C;">Belum ada riwayat transaksi.</td></tr>`;
        return;
    }

    historyList.forEach((t, idx) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td><strong>${t.nota}</strong></td>
            <td>${t.waktu}</td>
            <td>${t.itemCount} Item</td>
            <td class="text-right">${formatRupiah(t.subtotal)}</td>
            <td class="text-right" style="color: #047857; font-weight: 700;">-${formatRupiah(t.diskon)}</td>
            <td class="text-right" style="font-weight: 800;">${formatRupiah(t.grandTotal)}</td>
            <td class="text-center">
                <button class="btn-tbl-edit" data-idx="${idx}">Buka Struk</button>
            </td>
        `;

        tr.querySelector('.btn-tbl-edit').addEventListener("click", () => {
            const receipt = generateReceiptText(t);
            showReceiptModal(receipt);
        });

        tbody.appendChild(tr);
    });
}

// ================= INISIALISASI & EVENT LISTENERS =================
document.addEventListener("DOMContentLoaded", () => {
    // Jam Berjalan Realtime
    setInterval(() => {
        const now = new Date();
        const timeStr = now.toTimeString().split(' ')[0];
        document.getElementById("current-clock").textContent = timeStr;
    }, 1000);

    // Set Nota ID Awal
    document.getElementById("active-nota").textContent = generateNotaNumber();

    // Navigasi Tab
    const tabBtns = document.querySelectorAll(".nav-tab");
    tabBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            tabBtns.forEach(b => b.classList.remove("active"));
            document.querySelectorAll(".tab-content").forEach(tc => tc.classList.remove("active"));

            btn.classList.add("active");
            const tabId = "tab-" + btn.dataset.tab;
            document.getElementById(tabId).classList.add("active");

            if (btn.dataset.tab === "pos") renderCatalog();
            if (btn.dataset.tab === "crud") renderCrudTable();
            if (btn.dataset.tab === "history") renderHistoryTable();
        });
    });

    // Filter Kategori Katalog
    const pills = document.querySelectorAll(".filter-pill");
    pills.forEach(pill => {
        pill.addEventListener("click", () => {
            pills.forEach(p => p.classList.remove("active"));
            pill.classList.add("active");
            currentCategory = pill.dataset.cat;
            renderCatalog();
        });
    });

    // Pencarian Katalog
    document.getElementById("pos-search").addEventListener("input", renderCatalog);

    // Pencarian Tabel CRUD
    document.getElementById("crud-search").addEventListener("input", renderCrudTable);

    // Kosongkan Keranjang
    document.getElementById("btn-clear-cart").addEventListener("click", clearCart);

    // Input Bayar & Hitung Kembalian
    document.getElementById("input-bayar").addEventListener("input", calculateChange);

    // Tombol Uang Pas
    document.getElementById("btn-exact-cash").addEventListener("click", () => {
        const grandTotal = parseInt(document.getElementById("cart-grand-total").textContent.replace(/[^0-9]/g, ""), 10) || 0;
        document.getElementById("input-bayar").value = grandTotal;
        calculateChange();
    });

    // Tombol Cepat Pecahan
    document.querySelectorAll(".btn-quick-cash[data-cash]").forEach(btn => {
        btn.addEventListener("click", () => {
            document.getElementById("input-bayar").value = btn.dataset.cash;
            calculateChange();
        });
    });

    // Tombol Bayar / Checkout
    document.getElementById("btn-process-checkout").addEventListener("click", processCheckout);

    // Form CRUD Kategori Change
    document.getElementById("p-kategori").addEventListener("change", updateInfoKhususLabel);

    // Form CRUD Submit
    document.getElementById("product-form").addEventListener("submit", (e) => {
        e.preventDefault();
        const mode = document.getElementById("form-mode").value;
        const kode = document.getElementById("p-kode").value.trim().toUpperCase();
        const kategori = document.getElementById("p-kategori").value;
        const nama = document.getElementById("p-nama").value.trim();
        const harga = parseFloat(document.getElementById("p-harga").value);
        const stok = parseInt(document.getElementById("p-stok").value, 10);
        let rawInfo = document.getElementById("p-info").value.trim();
        const info = (kategori === "Makanan/Minuman") ? `Exp: ${rawInfo}` : `Garansi: ${rawInfo}`;

        if (mode === "create") {
            if (products.some(p => p.kode.toUpperCase() === kode)) {
                alert(`Kode produk [${kode}] sudah digunakan! Silakan gunakan kode lain.`);
                return;
            }
            products.push({ kode, kategori, nama, harga, stok, info });
            alert("Produk berhasil ditambahkan!");
        } else {
            const p = products.find(prod => prod.kode === kode);
            if (p) {
                p.kategori = kategori;
                p.nama = nama;
                p.harga = harga;
                p.stok = stok;
                p.info = info;
                alert("Data produk berhasil diperbarui!");
            }
        }

        saveProducts();
        resetProductForm();
        renderCrudTable();
        renderCatalog();
    });

    document.getElementById("btn-reset-form").addEventListener("click", resetProductForm);

    // Modal Struk Actions
    document.getElementById("btn-close-modal").addEventListener("click", closeReceiptModal);
    document.getElementById("btn-done-receipt").addEventListener("click", closeReceiptModal);
    document.getElementById("btn-print-receipt").addEventListener("click", () => {
        window.print();
    });

    // Render Perdana
    renderCatalog();
    renderCart();
    renderCrudTable();
    renderHistoryTable();
});

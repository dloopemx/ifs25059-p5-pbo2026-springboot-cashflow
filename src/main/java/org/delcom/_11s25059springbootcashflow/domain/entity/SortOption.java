package org.delcom._11s25059springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi.
 * Setiap opsi membawa comparator-nya sendiri sehingga logika pengurutan
 * terpusat di domain, bukan tersebar di view/use case.
 * ID dipakai sebagai pembanding kedua agar urutan stabil.
 */
public enum SortOption {
    /** Jumlah terkecil lebih dulu. */
    AMOUNT_ASC(Comparator.comparingLong(Transaction::getAmount)),

    /** Jumlah terbesar lebih dulu. */
    AMOUNT_DESC(Comparator.comparingLong(Transaction::getAmount).reversed()),

    /** Pemasukan ditampilkan lebih dulu. */
    INCOME_FIRST(Comparator.comparing(Transaction::isIncome).reversed()),

    /** Pengeluaran ditampilkan lebih dulu. */
    EXPENSE_FIRST(Comparator.comparing(Transaction::isExpense).reversed()),

    /** Transaksi terbaru lebih dulu. */
    NEWEST(Comparator.comparing(Transaction::getCreatedAt).reversed()),

    /** Transaksi terlama lebih dulu. */
    OLDEST(Comparator.comparing(Transaction::getCreatedAt));

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator.thenComparingInt(Transaction::getId);
    }

    /** Mengembalikan comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}

package org.delcom.ifs25059springbootcashflow.domain.entity;

import java.util.List;

/**
 * Value object ringkasan keuangan: jumlah transaksi, total pemasukan, total pengeluaran.
 * Saldo = total pemasukan - total pengeluaran.
 */
public record Balance(int transactionCount, long totalIncome, long totalExpense) {

    /** Ringkasan kosong (belum ada transaksi). */
    public static final Balance EMPTY = new Balance(0, 0, 0);

    /** Menghitung ringkasan dari daftar transaksi. */
    public static Balance of(List<Transaction> transactions) {
        long income = 0;
        long expense = 0;
        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
            }
        }
        return new Balance(transactions.size(), income, expense);
    }

    /** Saldo akhir. */
    public long balance() {
        return totalIncome - totalExpense;
    }
}

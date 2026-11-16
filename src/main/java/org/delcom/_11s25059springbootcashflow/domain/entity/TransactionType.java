package org.delcom._11s25059springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * Pemasukan menambah saldo, pengeluaran mengurangi saldo.
 */
public enum TransactionType {
    /** Pemasukan: menambah saldo. */
    INCOME(1),

    /** Pengeluaran: mengurangi saldo. */
    EXPENSE(-1);

    /** Pengali tanda terhadap saldo (+1 / -1). */
    private final int sign;

    TransactionType(int sign) {
        this.sign = sign;
    }

    /** Mengembalikan jumlah bertanda sesuai pengaruhnya terhadap saldo. */
    public long applySign(long amount) {
        return sign * amount;
    }
}

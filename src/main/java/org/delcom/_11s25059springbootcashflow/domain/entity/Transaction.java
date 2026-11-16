package org.delcom._11s25059springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entity inti yang merepresentasikan satu transaksi keuangan milik seorang pengguna.
 * Berada di layer domain — bebas dari urusan tampilan (format uang) maupun penyimpanan.
 */
public class Transaction {
    /** ID unik transaksi. */
    private final int id;

    /** Pemilik transaksi. */
    private final int userId;

    /** Keterangan transaksi. */
    private String description;

    /** Jumlah uang (selalu positif, satuan rupiah). */
    private long amount;

    /** Jenis transaksi: pemasukan atau pengeluaran. */
    private TransactionType type;

    /** Path relatif gambar bukti transaksi (opsional). */
    private String proofPath;

    /** Waktu transaksi dicatat. */
    private final LocalDateTime createdAt;

    /** Membuat transaksi baru tanpa bukti dengan waktu sekarang. */
    public Transaction(int id, int userId, String description, long amount, TransactionType type) {
        this(id, userId, description, amount, type, null, LocalDateTime.now());
    }

    /** Membuat transaksi lengkap (dipakai saat memuat dari penyimpanan). */
    public Transaction(
            int id,
            int userId,
            String description,
            long amount,
            TransactionType type,
            String proofPath,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    /** Pengaruh transaksi terhadap saldo: positif untuk pemasukan, negatif untuk pengeluaran. */
    public long signedAmount() {
        return type.applySign(amount);
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeAmount(long amount) {
        this.amount = amount;
    }

    public void changeType(TransactionType type) {
        this.type = type;
    }

    public void changeProofPath(String proofPath) {
        this.proofPath = proofPath;
    }
}

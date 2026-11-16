package org.delcom._11s25059springbootcashflow.usecase;

import org.delcom._11s25059springbootcashflow.domain.entity.Balance;
import org.delcom._11s25059springbootcashflow.domain.entity.SortOption;
import org.delcom._11s25059springbootcashflow.domain.entity.Transaction;
import org.delcom._11s25059springbootcashflow.domain.entity.TransactionType;
import org.delcom._11s25059springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom._11s25059springbootcashflow.domain.exception.ValidationException;
import org.delcom._11s25059springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;

/**
 * Use case pencatatan keuangan (pemasukan / pengeluaran) per pengguna.
 * Tidak melakukan I/O UI — hanya memproses data dan melempar checked exception
 * domain jika input tidak valid, data tidak ditemukan, atau bukan milik pengguna.
 */
public class FinanceUseCase {
    private static final String ENTITY_NAME = "Transaksi";

    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi milik satu pengguna. */
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Mengambil semua transaksi di sistem. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Mencatat pemasukan baru. */
    public Transaction addIncome(int userId, String description, long amount) throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.INCOME);
    }

    /** Mencatat pengeluaran baru. */
    public Transaction addExpense(int userId, String description, long amount) throws ValidationException {
        return addTransaction(userId, description, amount, TransactionType.EXPENSE);
    }

    /** Mencatat transaksi baru setelah validasi keterangan, jumlah, dan tipe. */
    public Transaction addTransaction(int userId, String description, long amount, TransactionType type)
            throws ValidationException {
        String safeDescription = requireDescription(description);
        requirePositiveAmount(amount);
        requireType(type);
        return transactionRepository.save(userId, safeDescription, amount, type);
    }

    /**
     * Mengubah keterangan, jumlah, tipe, dan/atau bukti transaksi milik pengguna.
     * Parameter {@code null} berarti field tersebut tidak diubah
     * (kecuali {@code clearProof=true} untuk menghapus bukti).
     */
    public Transaction updateTransactionForUser(
            int userId,
            int id,
            String description,
            Long amount,
            TransactionType type,
            String proofPath,
            boolean clearProof
    ) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        if (description != null) {
            transaction.changeDescription(requireDescription(description));
        }
        if (amount != null) {
            requirePositiveAmount(amount);
            transaction.changeAmount(amount);
        }
        if (type != null) {
            transaction.changeType(type);
        }
        if (clearProof) {
            transaction.changeProofPath(null);
        } else if (proofPath != null) {
            transaction.changeProofPath(proofPath);
        }
        transactionRepository.update(transaction);
        return transaction;
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!transactionRepository.deleteById(id)) {
            throw new EntityNotFoundException(ENTITY_NAME, id);
        }
    }

    /** Menghapus transaksi milik pengguna tertentu. */
    public void removeTransactionForUser(int userId, int id) throws EntityNotFoundException, ValidationException {
        Transaction transaction = requireOwnedTransaction(userId, id);
        removeTransaction(transaction.getId());
    }

    /** Mencari transaksi pengguna yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getTransactionsByUser(userId);
        }
        String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return transactionRepository.findByUserId(userId).stream()
                .filter(tx -> tx.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan transaksi pengguna sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return sort(transactionRepository.findByUserId(userId), option);
    }

    /** Mengurutkan daftar transaksi apa pun sesuai {@link SortOption}. */
    public List<Transaction> sort(List<Transaction> transactions, SortOption option) {
        return transactions.stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Ringkasan saldo pengguna: jumlah transaksi, total pemasukan, total pengeluaran, saldo. */
    public Balance getBalance(int userId) {
        return Balance.of(transactionRepository.findByUserId(userId));
    }

    /**
     * Mengubah teks jumlah dari input pengguna menjadi angka.
     * Pemisah ribuan (titik, koma, spasi, garis bawah) diabaikan, mis. "1.000.000" atau "1,000,000".
     */
    public long parseAmount(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        String digits = raw.trim().replaceAll("[.,_\\s]", "");
        if (!digits.matches("-?\\d+")) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        long amount;
        try {
            amount = Long.parseLong(digits);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah terlalu besar");
        }
        requirePositiveAmount(amount);
        return amount;
    }

    /** Mengubah teks ID dari input pengguna menjadi angka positif. */
    public int parseId(String raw) throws ValidationException {
        if (raw == null || !raw.trim().matches("\\d{1,9}")) {
            throw new ValidationException("ID tidak valid");
        }
        int id = Integer.parseInt(raw.trim());
        if (id <= 0) {
            throw new ValidationException("ID tidak valid");
        }
        return id;
    }

    private Transaction requireOwnedTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NAME, id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi tidak milik pengguna ini");
        }
        return transaction;
    }

    private static String requireDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private static void requirePositiveAmount(long amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
    }

    private static void requireType(TransactionType type) throws ValidationException {
        if (type == null) {
            throw new ValidationException("Tipe transaksi wajib dipilih");
        }
    }
}

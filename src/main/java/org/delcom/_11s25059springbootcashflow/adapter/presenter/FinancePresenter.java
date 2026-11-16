package org.delcom._11s25059springbootcashflow.adapter.presenter;

import org.delcom._11s25059springbootcashflow.domain.entity.Balance;
import org.delcom._11s25059springbootcashflow.domain.entity.SortOption;
import org.delcom._11s25059springbootcashflow.domain.entity.Transaction;
import org.delcom._11s25059springbootcashflow.domain.entity.TransactionType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data keuangan menjadi teks siap tampil.
 * Format uang (Rp 1,000,000.00), label tipe, dan pesan notifikasi
 * bukan tanggung jawab entity maupun use case.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Memformat jumlah uang: {@code Rp 1,000,000.00}; nilai negatif: {@code -Rp 25,000.00}. */
    public String formatMoney(long amount) {
        // DecimalFormat tidak thread-safe, jadi dibuat per pemanggilan.
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        String formatted = "Rp " + format.format(Math.abs(amount));
        return amount < 0 ? "-" + formatted : formatted;
    }

    /** Label tipe transaksi dalam Bahasa Indonesia. */
    public String typeLabel(TransactionType type) {
        return switch (type) {
            case INCOME -> "Pemasukan";
            case EXPENSE -> "Pengeluaran";
        };
    }

    /** Label opsi pengurutan untuk ditampilkan di pilihan urutan. */
    public String sortOptionLabel(SortOption option) {
        return switch (option) {
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
            case NEWEST -> "Terbaru";
            case OLDEST -> "Terlama";
        };
    }

    /** Jumlah transaksi dengan tanda: {@code + Rp ...} untuk pemasukan, {@code - Rp ...} untuk pengeluaran. */
    public String formatSignedAmount(Transaction transaction) {
        String sign = transaction.isIncome() ? "+ " : "- ";
        return sign + formatMoney(transaction.getAmount());
    }

    /** Memformat satu transaksi: {@code [id] keterangan | Rp 1,000,000.00 | [Pemasukan]}. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                typeLabel(transaction.getType())
        );
    }

    /** Memformat waktu transaksi dicatat. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Baris saldo: {@code Saldo: Rp 4,975,000.00}. */
    public String formatBalance(Balance balance) {
        return "Saldo: " + formatMoney(balance.balance());
    }

    /** Ringkasan lengkap multi-baris. */
    public String formatSummary(Balance balance) {
        return String.join("\n",
                "Jumlah Transaksi: " + balance.transactionCount(),
                "Total Pemasukan: " + formatMoney(balance.totalIncome()),
                "Total Pengeluaran: " + formatMoney(balance.totalExpense()),
                formatBalance(balance)
        );
    }

    /** Memformat daftar transaksi menjadi teks multi-baris diikuti saldo. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            return builder.append('\n').append(emptyMessage).toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        return builder.append('\n').append(body)
                .append('\n').append(formatBalance(Balance.of(transactions)))
                .toString();
    }

    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", showEmpty());
    }

    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, searchTitle(keyword), showNotFound());
    }

    public String showSortedTransactions(List<Transaction> transactions, SortOption option) {
        return formatList(transactions, "Daftar Transaksi (" + sortOptionLabel(option) + "):", showEmpty());
    }

    public String searchTitle(String keyword) {
        return "Hasil Pencarian: \"" + keyword + "\"";
    }

    public String showEmpty() {
        return "- Data transaksi belum tersedia!";
    }

    public String showNotFound() {
        return "- Transaksi tidak ditemukan!";
    }

    public String showAddSuccess(Transaction transaction) {
        return "Berhasil mencatat " + typeLabel(transaction.getType()).toLowerCase(Locale.ROOT)
                + ": " + format(transaction);
    }

    public String showUpdateSuccess(Transaction transaction) {
        return "Berhasil mengubah transaksi: " + format(transaction);
    }

    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}

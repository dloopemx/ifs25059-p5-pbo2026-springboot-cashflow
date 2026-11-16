package org.delcom._11s25059springbootcashflow.adapter.presenter;

import org.delcom._11s25059springbootcashflow.domain.entity.Balance;
import org.delcom._11s25059springbootcashflow.domain.entity.SortOption;
import org.delcom._11s25059springbootcashflow.domain.entity.Transaction;
import org.delcom._11s25059springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final LocalDateTime time = LocalDateTime.of(2026, 3, 4, 9, 5);
    private final Transaction income =
            new Transaction(1, 1, "Gaji", 5_000_000, TransactionType.INCOME, null, time);
    private final Transaction expense =
            new Transaction(2, 1, "Makan", 25_000, TransactionType.EXPENSE, null, time);

    @Test
    void formatsMoneyWithThousandSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("Rp 999.00", presenter.formatMoney(999));
        assertEquals("-Rp 25,000.00", presenter.formatMoney(-25_000));
    }

    @Test
    void formatsTransactionLineAndLabels() {
        assertEquals("[1] Gaji | Rp 5,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense));
        assertEquals("+ Rp 5,000,000.00", presenter.formatSignedAmount(income));
        assertEquals("- Rp 25,000.00", presenter.formatSignedAmount(expense));
        assertEquals("04/03/2026 09:05", presenter.formatCreatedAt(income));
    }

    @Test
    void labelsEverySortOption() {
        assertEquals("Jumlah terkecil", presenter.sortOptionLabel(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.sortOptionLabel(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.sortOptionLabel(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.sortOptionLabel(SortOption.EXPENSE_FIRST));
        assertEquals("Terbaru", presenter.sortOptionLabel(SortOption.NEWEST));
        assertEquals("Terlama", presenter.sortOptionLabel(SortOption.OLDEST));
    }

    @Test
    void formatsBalanceAndSummary() {
        Balance balance = new Balance(3, 6_000_000, 1_025_000);
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(balance));
        assertEquals("""
                Jumlah Transaksi: 3
                Total Pemasukan: Rp 6,000,000.00
                Total Pengeluaran: Rp 1,025,000.00
                Saldo: Rp 4,975,000.00""", presenter.formatSummary(balance));
    }

    @Test
    void formatsListsWithBalanceOrEmptyMessage() {
        assertEquals("""
                Daftar Transaksi:
                [1] Gaji | Rp 5,000,000.00 | [Pemasukan]
                [2] Makan | Rp 25,000.00 | [Pengeluaran]
                Saldo: Rp 4,975,000.00""", presenter.showTransactions(List.of(income, expense)));
        assertEquals("Daftar Transaksi:\n- Data transaksi belum tersedia!", presenter.showTransactions(List.of()));
        assertEquals("Hasil Pencarian: \"xyz\"\n- Transaksi tidak ditemukan!",
                presenter.showSearchResults(List.of(), "xyz"));
        assertEquals("Daftar Transaksi (Jumlah terbesar):\n[2] Makan | Rp 25,000.00 | [Pengeluaran]\nSaldo: -Rp 25,000.00",
                presenter.showSortedTransactions(List.of(expense), SortOption.AMOUNT_DESC));
    }

    @Test
    void formatsMessages() {
        assertEquals("Berhasil mencatat pemasukan: [1] Gaji | Rp 5,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income));
        assertEquals("Berhasil mencatat pengeluaran: [2] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showAddSuccess(expense));
        assertEquals("Berhasil mengubah transaksi: [2] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showUpdateSuccess(expense));
        assertEquals("[!] Gagal mengubah transaksi dengan ID: 5.", presenter.showUpdateFailed(5));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 5.", presenter.showRemoveFailed(5));
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] Keterangan kosong", presenter.showValidationError("Keterangan kosong"));
        assertEquals("Pemasukan", presenter.typeLabel(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.typeLabel(TransactionType.EXPENSE));
    }
}

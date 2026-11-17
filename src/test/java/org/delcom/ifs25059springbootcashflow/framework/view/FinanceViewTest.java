package org.delcom.ifs25059springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.delcom.ifs25059springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25059springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25059springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25059springbootcashflow.domain.entity.Role;
import org.delcom.ifs25059springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25059springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25059springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25059springbootcashflow.domain.entity.User;
import org.delcom.ifs25059springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25059springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25059springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class FinanceViewTest {

    private static final int USER_ID = 1;

    @TempDir
    Path tempDir;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = spy(new FinanceUseCase(new TransactionRepository()));
        MockVaadin.setup();
        AuthSession.login(new User(USER_ID, "andi", "hash", "Andi", Role.USER, null));
        UI.getCurrent().add(new FinanceView(useCase, new FinancePresenter(), new FileStorage(tempDir.toString())));
    }

    @AfterEach
    void tearDown() {
        AuthSession.logout();
        MockVaadin.tearDown();
    }

    @Test
    void addIncomeExpenseSearchSortEditAndDelete() {
        assertTrue(status().contains("belum tersedia"));
        assertEquals("Rp 0.00", balanceText());

        addTransaction("Tambah Pemasukan", "Gaji", "5.000.000");
        addTransaction("Tambah Pengeluaran", "Makan siang", "25000");
        assertEquals(2, useCase.getTransactionsByUser(USER_ID).size());
        assertEquals("Rp 4,975,000.00", balanceText());

        TextField search = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(search, "MAKAN");
        clickButton("Cari");
        assertEquals(1, grid().getListDataView().getItemCount());
        assertTrue(status().startsWith("Hasil Pencarian: \"MAKAN\""));

        _setValue(search, "tidakada");
        clickButton("Cari");
        assertTrue(status().contains("Transaksi tidak ditemukan"));

        clickButton("Muat Ulang");
        assertEquals(2, grid().getListDataView().getItemCount());

        @SuppressWarnings("unchecked")
        Select<SortOption> sort = _get(Select.class);
        _setValue(sort, SortOption.EXPENSE_FIRST);
        assertEquals("Makan siang", grid().getListDataView().getItems().toList().get(0).getDescription());
        assertTrue(status().contains("Pengeluaran dulu"));

        grid().sort(List.of(new GridSortOrder<>(grid().getColumnByKey("amount"), SortDirection.DESCENDING)));
        grid().sort(List.of(new GridSortOrder<>(grid().getColumnByKey("type"), SortDirection.ASCENDING)));
        grid().sort(List.of(new GridSortOrder<>(grid().getColumnByKey("createdAt"), SortDirection.ASCENDING)));
        grid().sort(List.of(new GridSortOrder<>(grid().getColumnByKey("description"), SortDirection.ASCENDING)));

        int row = rowOf("Makan siang");
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid(), row, "actions");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Ubah")));
        Dialog edit = _get(Dialog.class);
        _setValue(_get(edit, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), "30000");
        @SuppressWarnings("unchecked")
        RadioButtonGroup<TransactionType> type = _get(edit, RadioButtonGroup.class);
        _setValue(type, TransactionType.INCOME);
        clickDialogButton(edit, "Simpan");
        assertEquals("Rp 5,030,000.00", balanceText());

        row = rowOf("Makan siang");
        actions = (HorizontalLayout) _getCellComponent(grid(), row, "actions");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        clickDialogButton(_get(Dialog.class), "Hapus");
        assertEquals(1, useCase.getTransactionsByUser(USER_ID).size());
        assertEquals("Rp 5,000,000.00", balanceText());
    }

    @Test
    void showsValidationErrorsAndKeepsDialogOpen() {
        clickButton("Tambah Pemasukan");
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Keterangan")), " ");
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), "100");
        clickDialogButton(dialog, "Simpan");
        assertTrue(dialog.isOpened());

        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Keterangan")), "Gaji");
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), "abc");
        clickDialogButton(dialog, "Simpan");
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), "-5");
        clickDialogButton(dialog, "Simpan");
        assertTrue(useCase.getTransactionsByUser(USER_ID).isEmpty());
        clickDialogButton(dialog, "Batal");
        assertTrue(_find(Dialog.class).isEmpty());
    }

    @Test
    void handlesNotFoundOnDelete() throws Exception {
        Transaction tx = useCase.addExpense(USER_ID, "Temp", 10);
        clickButton("Muat Ulang");
        doThrow(new EntityNotFoundException("Transaksi", tx.getId()))
                .when(useCase).removeTransactionForUser(anyInt(), anyInt());
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid(), 0, "actions");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
        clickDialogButton(_get(Dialog.class), "Hapus");
        assertEquals(1, useCase.getTransactionsByUser(USER_ID).size());
    }

    private void addTransaction(String button, String description, String amount) {
        clickButton(button);
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Keterangan")), description);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), amount);
        clickDialogButton(dialog, "Simpan");
    }

    @SuppressWarnings("unchecked")
    private Grid<Transaction> grid() {
        return _get(Grid.class);
    }

    private int rowOf(String description) {
        List<Transaction> items = grid().getListDataView().getItems().toList();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getDescription().equals(description)) {
                return i;
            }
        }
        throw new AssertionError("tidak ada: " + description);
    }

    private String status() {
        return _get(Paragraph.class).getText();
    }

    private String balanceText() {
        return _get(SummaryCards.class).balanceText();
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }
}

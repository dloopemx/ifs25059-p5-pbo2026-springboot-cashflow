package org.delcom._11s25059springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom._11s25059springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom._11s25059springbootcashflow.adapter.storage.FileStorage;
import org.delcom._11s25059springbootcashflow.domain.entity.SortOption;
import org.delcom._11s25059springbootcashflow.domain.entity.Transaction;
import org.delcom._11s25059springbootcashflow.domain.entity.TransactionType;
import org.delcom._11s25059springbootcashflow.domain.entity.User;
import org.delcom._11s25059springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom._11s25059springbootcashflow.domain.exception.ValidationException;
import org.delcom._11s25059springbootcashflow.framework.auth.AuthSession;
import org.delcom._11s25059springbootcashflow.usecase.FinanceUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dashboard keuangan pribadi pengguna yang sedang login:
 * kartu saldo, toolbar (cari, urut, muat ulang, tambah pemasukan/pengeluaran), dan grid transaksi.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span();
    private final SummaryCards summaryCards;

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter, FileStorage fileStorage) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.summaryCards = new SummaryCards(presenter);

        setSizeFull();
        getElement().setAttribute("role", "main");
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        H1 title = new H1("Keuangan Saya");
        title.getStyle().set("margin", "0");
        statusLabel.getStyle().set("margin", "0").set("color", "#4B5563");

        configureGrid();
        Div card = PageCard.wrap(title, summaryCards, buildToolbar(), statusLabel, grid);
        add(card);
        expand(card);
        refreshAll();
    }

    private User requireUser() {
        return AuthSession.currentUser().orElseThrow();
    }

    // ------------------------------------------------------------------ grid

    private void configureGrid() {
        grid.addComponentColumn(this::createProofPreview)
                .setHeader("Bukti")
                .setKey("proof")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(tx -> TypeBadge.amount(tx, presenter))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));

        grid.addComponentColumn(tx -> TypeBadge.of(tx, presenter))
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
        GridStyles.apply(grid);
    }

    private Component createProofPreview(Transaction transaction) {
        if (transaction.getProofPath() == null || transaction.getProofPath().isBlank()) {
            return new Span("-");
        }
        return ImagePreviewDialog.clickableThumbnail(
                "/uploads/" + transaction.getProofPath(),
                "Bukti transaksi #" + transaction.getId(),
                "48px"
        );
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button editButton = new Button("Ubah", VaadinIcon.EDIT.create(), event -> openEditDialog(transaction));
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        editButton.getStyle().set("cursor", "pointer");

        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getStyle().set("cursor", "pointer");

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    // --------------------------------------------------------------- toolbar

    private HorizontalLayout buildToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addKeyUpListener(Key.ENTER, event -> applyFilters());
        searchField.setMinWidth("200px");

        sortSelect.setPlaceholder("Urutkan...");
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(option -> option == null ? "Urutan default (ID)" : presenter.sortOptionLabel(option));
        sortSelect.setEmptySelectionAllowed(true);
        sortSelect.setEmptySelectionCaption("Urutan default (ID)");
        sortSelect.addValueChangeListener(event -> applyFilters());
        sortSelect.setWidth("190px");

        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> applyFilters());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS_CIRCLE.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS_CIRCLE.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        for (Button button : List.of(searchButton, refreshButton, incomeButton, expenseButton)) {
            button.getStyle().set("cursor", "pointer");
        }

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortSelect, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    // --------------------------------------------------------------- dialogs

    private void openAddDialog(TransactionType type) {
        String label = presenter.typeLabel(type);
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        TextField amountField = amountField();

        AtomicReference<String> pendingName = new AtomicReference<>();
        AtomicReference<byte[]> pendingData = new AtomicReference<>();
        Upload upload = imageUpload(pendingName, pendingData);
        Span uploadHint = new Span("Bukti transaksi (opsional)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                long amount = financeUseCase.parseAmount(amountField.getValue());
                Transaction created = financeUseCase.addTransaction(
                        user.getId(), descriptionField.getValue(), amount, type);
                String proofPath = storeProof(pendingName.get(), pendingData.get());
                if (proofPath != null) {
                    created = financeUseCase.updateTransactionForUser(
                            user.getId(), created.getId(), null, null, null, proofPath, false);
                }
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException | EntityNotFoundException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(descriptionField, amountField, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);
        ModalDialogs.build(dialog, "Tambah " + label, label, ModalDialogs.Tone.PRIMARY, loadingBar, body, cancelButton, saveButton);
        dialog.open();
        descriptionField.focus();
    }

    private void openEditDialog(Transaction transaction) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setValue(transaction.getDescription());
        descriptionField.setWidthFull();

        TextField amountField = amountField();
        amountField.setValue(String.valueOf(transaction.getAmount()));

        RadioButtonGroup<TransactionType> typeGroup = new RadioButtonGroup<>("Tipe");
        typeGroup.setItems(TransactionType.values());
        typeGroup.setItemLabelGenerator(presenter::typeLabel);
        typeGroup.setValue(transaction.getType());

        AtomicReference<String> pendingName = new AtomicReference<>();
        AtomicReference<byte[]> pendingData = new AtomicReference<>();
        Upload upload = imageUpload(pendingName, pendingData);
        Span uploadHint = new Span("Ganti bukti (opsional)");

        Checkbox clearProof = new Checkbox("Hapus bukti saat ini");
        boolean hasProof = transaction.getProofPath() != null && !transaction.getProofPath().isBlank();
        clearProof.setVisible(hasProof);

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                long amount = financeUseCase.parseAmount(amountField.getValue());
                String proofPath = storeProof(pendingName.get(), pendingData.get());
                Transaction updated = financeUseCase.updateTransactionForUser(
                        user.getId(),
                        transaction.getId(),
                        descriptionField.getValue(),
                        amount,
                        typeGroup.getValue(),
                        proofPath,
                        proofPath == null && clearProof.getValue()
                );
                showSuccess(presenter.showUpdateSuccess(updated));
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showUpdateFailed(ex.getId()));
            } catch (ValidationException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(descriptionField, amountField, typeGroup, uploadHint, upload, clearProof);
        body.setPadding(false);
        body.setSpacing(true);
        ModalDialogs.build(dialog, "Ubah Transaksi", "ID " + transaction.getId(), ModalDialogs.Tone.PRIMARY,
                loadingBar, body, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph("Yakin ingin menghapus transaksi berikut?");
        message.getStyle().set("margin", "0 0 0.5rem 0");
        Paragraph detail = new Paragraph(presenter.format(transaction));
        detail.getStyle()
                .set("margin", "0")
                .set("font-family", "var(--lumo-font-family)")
                .set("font-weight", "600")
                .set("background", "#F9FAFB")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "8px")
                .set("padding", "0.6rem 0.8rem");
        Div content = new Div(message, detail);

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setVisible(false);

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            confirmButton.setEnabled(false);
            deleteTransaction(transaction);
            dialog.close();
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        ModalDialogs.build(dialog, "Hapus Transaksi", "ID " + transaction.getId(), ModalDialogs.Tone.DANGER,
                loadingBar, content, cancelButton, confirmButton);
        dialog.open();
    }

    // --------------------------------------------------------------- actions

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransactionForUser(requireUser().getId(), transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void applyFilters() {
        int userId = requireUser().getId();
        String keyword = searchField.getValue();
        boolean searching = keyword != null && !keyword.isBlank();

        List<Transaction> items = financeUseCase.searchTransactions(userId, keyword);
        SortOption option = sortSelect.getValue();
        if (option != null) {
            items = financeUseCase.sort(items, option);
        }

        emptyState.setText(searching ? presenter.showNotFound() : presenter.showEmpty());
        String title = searching ? presenter.searchTitle(keyword.trim()) : "Daftar Transaksi";
        if (option != null) {
            title += " — diurutkan: " + presenter.sortOptionLabel(option);
        }
        refreshGrid(items, title);
    }

    private void refreshAll() {
        searchField.clear();
        sortSelect.clear();
        applyFilters();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        summaryCards.update(financeUseCase.getBalance(requireUser().getId()));
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title + " (" + transactions.size() + " transaksi)");
        }
    }

    // --------------------------------------------------------------- helpers

    private TextField amountField() {
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000 atau 1.000.000");
        amountField.setPrefixComponent(new Span("Rp"));
        amountField.setWidthFull();
        return amountField;
    }

    private String storeProof(String filename, byte[] data) throws IOException {
        if (filename == null || filename.isBlank() || data == null) {
            return null;
        }
        return fileStorage.store("proofs", filename, new ByteArrayInputStream(data));
    }

    private Upload imageUpload(AtomicReference<String> pendingName, AtomicReference<byte[]> pendingData) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingName.set(metadata.fileName());
            pendingData.set(data);
        }));
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/webp");
        upload.setAcceptedFileExtensions(".png", ".jpg", ".jpeg", ".webp");
        upload.setMaxFiles(1);
        upload.setWidthFull();
        return upload;
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3500, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4500, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

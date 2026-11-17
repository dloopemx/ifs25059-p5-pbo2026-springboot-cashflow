package org.delcom._11s25059springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom._11s25059springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom._11s25059springbootcashflow.domain.entity.Transaction;
import org.delcom._11s25059springbootcashflow.domain.entity.User;
import org.delcom._11s25059springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom._11s25059springbootcashflow.framework.auth.AuthSession;
import org.delcom._11s25059springbootcashflow.usecase.FinanceUseCase;
import org.delcom._11s25059springbootcashflow.usecase.UserUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * (ADMIN) Peninjauan transaksi milik pengguna lain — hanya baca, tanpa tombol ubah/hapus.
 */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna")
public class UserTransactionsView extends VerticalLayout
        implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final FinanceUseCase financeUseCase;
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final H1 title = new H1("Transaksi Pengguna");
    private final Paragraph subtitle = new Paragraph("Mode hanya baca (read-only).");
    private final Paragraph empty = new Paragraph("Pengguna ini belum memiliki transaksi.");
    private final SummaryCards summaryCards;

    public UserTransactionsView(UserUseCase userUseCase, FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.userUseCase = userUseCase;
        this.financeUseCase = financeUseCase;
        this.summaryCards = new SummaryCards(presenter);

        setSizeFull();
        getElement().setAttribute("role", "main");
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        title.getStyle().set("margin", "0");
        subtitle.getStyle().set("margin", "0").set("color", "#4B5563");
        empty.getStyle().set("margin", "0").set("color", "#4B5563");

        Button back = new Button("Kembali ke Daftar Pengguna", VaadinIcon.ARROW_LEFT.create(),
                event -> UI.getCurrent().navigate(UsersView.class));
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("cursor", "pointer").set("align-self", "flex-start");

        grid.addComponentColumn(tx -> {
            if (tx.getProofPath() == null || tx.getProofPath().isBlank()) {
                return new Span("-");
            }
            return ImagePreviewDialog.clickableThumbnail(
                    "/uploads/" + tx.getProofPath(), "Bukti transaksi #" + tx.getId(), "48px");
        }).setHeader("Bukti").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0)
                .setSortable(true).setComparator(Transaction::getId);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));
        grid.addComponentColumn(tx -> TypeBadge.amount(tx, presenter)).setHeader("Jumlah").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparingLong(Transaction::getAmount));
        grid.addComponentColumn(tx -> TypeBadge.of(tx, presenter)).setHeader("Tipe").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getType));
        grid.addColumn(presenter::formatCreatedAt).setHeader("Waktu").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getCreatedAt));
        grid.setSizeFull();
        GridStyles.apply(grid);

        Div card = PageCard.wrap(back, title, subtitle, summaryCards, empty, grid);
        add(card);
        expand(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser().orElse(null);
        if (current == null || !current.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer userId) {
        User current = AuthSession.currentUser().orElse(null);
        if (current == null || !current.isAdmin()) {
            return;
        }
        if (userId == null) {
            event.forwardTo(UsersView.class);
            return;
        }
        try {
            User owner = userUseCase.getUser(userId);
            title.setText("Transaksi milik " + owner.getFullName() + " (@" + owner.getUsername() + ")");
            List<Transaction> transactions = financeUseCase.getTransactionsByUser(userId);
            grid.setItems(transactions);
            summaryCards.update(financeUseCase.getBalance(userId));
            empty.setVisible(transactions.isEmpty());
            grid.setVisible(!transactions.isEmpty());
        } catch (EntityNotFoundException ex) {
            event.forwardTo(UsersView.class);
        }
    }
}

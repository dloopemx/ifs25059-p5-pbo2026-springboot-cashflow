package org.delcom.ifs25059springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.delcom.ifs25059springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25059springbootcashflow.domain.entity.Balance;

/**
 * Kartu ringkasan keuangan: jumlah transaksi, total pemasukan, total pengeluaran, saldo.
 * Dipakai di halaman keuangan pribadi dan halaman admin (read-only).
 */
final class SummaryCards extends HorizontalLayout {

    private final FinancePresenter presenter;
    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Span balanceValue = new Span();
    private final Div balanceCard;

    SummaryCards(FinancePresenter presenter) {
        this.presenter = presenter;
        balanceCard = card("Saldo", balanceValue, "#EDE9FE", "#7C3AED", "#5B21B6");
        add(
                card("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                card("Total Pemasukan", totalIncome, "#DCFCE7", "#16A34A", "#166534"),
                card("Total Pengeluaran", totalExpense, "#FEE2E2", "#DC2626", "#991B1B"),
                balanceCard
        );
        setWidthFull();
        setSpacing(true);
        getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        update(Balance.EMPTY);
    }

    void update(Balance balance) {
        totalCount.setText(String.valueOf(balance.transactionCount()));
        totalIncome.setText(presenter.formatMoney(balance.totalIncome()));
        totalExpense.setText(presenter.formatMoney(balance.totalExpense()));
        balanceValue.setText(presenter.formatMoney(balance.balance()));
        balanceValue.getStyle().set("color", balance.balance() < 0 ? "#B91C1C" : "#5B21B6");
    }

    String balanceText() {
        return balanceValue.getText();
    }

    private static Div card(String label, Span value, String background, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "600")
                .set("color", valueColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.35rem")
                .set("font-weight", "700")
                .set("line-height", "1.2")
                .set("white-space", "nowrap")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1 1 200px")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }
}

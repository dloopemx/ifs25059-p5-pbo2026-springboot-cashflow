package org.delcom.ifs25059springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Span;
import org.delcom.ifs25059springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25059springbootcashflow.domain.entity.Transaction;

/**
 * Badge berwarna untuk tipe transaksi dan teks jumlah berwarna.
 */
final class TypeBadge {

    private TypeBadge() {
    }

    static Span of(Transaction transaction, FinancePresenter presenter) {
        Span badge = new Span(presenter.typeLabel(transaction.getType()));
        boolean income = transaction.isIncome();
        badge.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.15rem 0.6rem")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("background", income ? "#DCFCE7" : "#FEE2E2")
                .set("color", income ? "#166534" : "#991B1B");
        return badge;
    }

    static Span amount(Transaction transaction, FinancePresenter presenter) {
        Span amount = new Span(presenter.formatSignedAmount(transaction));
        amount.getStyle()
                .set("font-weight", "600")
                .set("white-space", "nowrap")
                .set("color", transaction.isIncome() ? "#15803D" : "#B91C1C");
        return amount;
    }
}

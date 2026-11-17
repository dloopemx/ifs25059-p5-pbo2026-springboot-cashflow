package org.delcom.ifs25059springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Halaman sambutan publik. Ditampilkan di "/" untuk pengunjung yang belum login
 * (via reroute dari {@link MainLayout}, URL tetap "/"), dan juga tersedia di "/welcome".
 */
@Route("welcome")
@PageTitle("Cashflow - Catatan Keuangan")
public class LandingView extends VerticalLayout {

    public LandingView() {
        H1 title = new H1("Catat keuangan pribadi dengan mudah");
        title.getStyle().set("margin", "0 0 0.75rem 0").set("color", "#111827");

        Paragraph intro = new Paragraph(
                "Cashflow membantu kamu mencatat pemasukan dan pengeluaran, menyimpan bukti transaksi, "
                        + "dan melihat saldo secara langsung.");
        intro.getStyle().set("font-size", "1.1rem").set("color", "#374151").set("margin", "0 0 1.5rem 0");

        Div actions = new Div(
                PublicPage.buttonLink("Masuk", "/login", true),
                PublicPage.buttonLink("Daftar Akun", "/register", false)
        );
        actions.getStyle().set("display", "flex").set("gap", "0.75rem").set("flex-wrap", "wrap");

        H2 featuresTitle = new H2("Fitur");
        featuresTitle.getStyle().set("margin", "2.5rem 0 1rem 0").set("color", "#111827");

        Div features = new Div(
                feature("Pemasukan & Pengeluaran", "Catat setiap transaksi beserta keterangan dan jumlahnya."),
                feature("Ringkasan Saldo", "Total pemasukan, total pengeluaran, dan saldo selalu terbaru."),
                feature("Bukti Transaksi", "Unggah foto struk atau bukti transfer dan perbesar kapan saja.")
        );
        features.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(220px, 1fr))")
                .set("gap", "1rem");

        PublicPage.build(this, title, intro, actions, featuresTitle, features);
    }

    private static Div feature(String heading, String text) {
        Paragraph name = new Paragraph(heading);
        name.getStyle().set("font-weight", "700").set("margin", "0 0 0.4rem 0").set("color", "#1E3A8A");
        Paragraph description = new Paragraph(text);
        description.getStyle().set("margin", "0").set("color", "#374151");
        Div card = new Div(name, description);
        card.getStyle()
                .set("background", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem");
        return card;
    }
}

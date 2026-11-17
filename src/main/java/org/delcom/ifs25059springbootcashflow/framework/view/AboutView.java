package org.delcom.ifs25059springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Halaman publik "Tentang" aplikasi.
 */
@Route("about")
@PageTitle("Tentang - Cashflow")
public class AboutView extends VerticalLayout {

    public AboutView() {
        H1 title = new H1("Tentang Cashflow");
        title.getStyle().set("margin", "0 0 0.75rem 0").set("color", "#111827");

        Paragraph intro = new Paragraph(
                "Cashflow adalah aplikasi web pencatat transaksi keuangan pribadi. Setiap transaksi bertipe "
                        + "pemasukan atau pengeluaran, milik satu pengguna, dan dapat dilengkapi bukti gambar.");
        intro.getStyle().set("color", "#374151");

        H2 techTitle = new H2("Teknologi");
        techTitle.getStyle().set("color", "#111827");
        UnorderedList tech = new UnorderedList(
                new ListItem("Spring Boot dan Vaadin"),
                new ListItem("JDBC dengan database PostgreSQL"),
                new ListItem("Clean Architecture: domain, use case, adapter, framework")
        );
        tech.getStyle().set("color", "#374151");

        Paragraph cta = new Paragraph();
        cta.add(PublicPage.navLink("Masuk untuk mulai mencatat", "/login"));

        PublicPage.build(this, title, intro, techTitle, tech, cta);
    }
}

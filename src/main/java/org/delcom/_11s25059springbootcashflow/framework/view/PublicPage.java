package org.delcom._11s25059springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Kerangka halaman publik (tanpa login): header dengan navigasi, konten utama, dan footer.
 */
final class PublicPage {

    private PublicPage() {
    }

    static void build(VerticalLayout root, Component... content) {
        root.setSizeFull();
        root.setPadding(false);
        root.setSpacing(false);
        root.getStyle().set("background", "#F3F4F6").set("min-height", "100vh");

        Image logo = new Image("icons/logo-64.png", "Logo Cashflow");
        logo.setWidth("32px");
        logo.setHeight("32px");
        logo.getStyle().set("border-radius", "8px");
        Span brand = new Span("Cashflow");
        brand.getStyle().set("font-size", "1.2rem").set("font-weight", "700").set("color", "#111827");
        Anchor home = new Anchor("/", logo, brand);
        home.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "0.6rem")
                .set("text-decoration", "none");

        Nav nav = new Nav(
                navLink("Beranda", "/"),
                navLink("Tentang", "/about"),
                navLink("Masuk", "/login"),
                navLink("Daftar", "/register")
        );
        nav.getElement().setAttribute("aria-label", "Navigasi utama");
        nav.getStyle().set("display", "flex").set("gap", "1rem").set("flex-wrap", "wrap");

        Header header = new Header(home, nav);
        header.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("flex-wrap", "wrap")
                .set("gap", "0.75rem")
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("padding", "1rem 1.5rem")
                .set("background", "#FFFFFF")
                .set("border-bottom", "1px solid #E5E7EB");

        Main main = new Main(content);
        main.getStyle()
                .set("width", "100%")
                .set("max-width", "960px")
                .set("margin", "0 auto")
                .set("padding", "2rem 1.25rem")
                .set("box-sizing", "border-box")
                .set("flex", "1");

        Span copyright = new Span("Cashflow — Praktikum PBO 2026");
        copyright.getStyle().set("color", "#374151").set("font-size", "0.875rem");
        Footer footer = new Footer(copyright);
        footer.getStyle()
                .set("width", "100%")
                .set("box-sizing", "border-box")
                .set("text-align", "center")
                .set("padding", "1rem")
                .set("border-top", "1px solid #E5E7EB")
                .set("background", "#FFFFFF");

        root.add(header, main, footer);
    }

    static Anchor navLink(String label, String href) {
        Anchor link = new Anchor(href, label);
        link.getStyle()
                .set("color", "#1D4ED8")
                .set("font-weight", "600")
                .set("text-decoration", "none")
                .set("cursor", "pointer");
        return link;
    }

    static Anchor buttonLink(String label, String href, boolean primary) {
        Anchor link = new Anchor(href, label);
        link.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.7rem 1.4rem")
                .set("border-radius", "10px")
                .set("font-weight", "600")
                .set("text-decoration", "none")
                .set("cursor", "pointer")
                .set("background", primary ? "#1D4ED8" : "#FFFFFF")
                .set("color", primary ? "#FFFFFF" : "#1D4ED8")
                .set("border", "2px solid #1D4ED8");
        return link;
    }
}

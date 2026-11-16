package org.delcom._11s25059springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Pembangun kerangka dialog modal yang seragam:
 * header berwarna primer (#2563EB) + badge, ProgressBar, body, dan footer abu-abu (#F3F4F6)
 * dengan tombol di sisi kanan.
 */
final class ModalDialogs {

    enum Tone {
        PRIMARY("#2563EB"),
        DANGER("#DC2626");

        private final String color;

        Tone(String color) {
            this.color = color;
        }
    }

    private ModalDialogs() {
    }

    static void build(
            Dialog dialog,
            String title,
            String badgeText,
            Tone tone,
            ProgressBar loadingBar,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("520px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.addClassName("cashflow-form-dialog");

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        if (badgeText != null && !badgeText.isBlank()) {
            Span badge = new Span(badgeText);
            badge.getStyle()
                    .set("background", "rgba(255, 255, 255, 0.22)")
                    .set("color", "#FFFFFF")
                    .set("border", "1px solid rgba(255, 255, 255, 0.45)")
                    .set("border-radius", "999px")
                    .set("padding", "0.1rem 0.6rem")
                    .set("font-size", "0.75rem")
                    .set("font-weight", "600");
            header.add(badge);
        }
        header.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("gap", "0.75rem")
                .set("background", tone.color)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        loadingBar.setIndeterminate(true);
        loadingBar.getStyle().set("margin", "0").set("height", "4px");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        for (Button button : footerButtons) {
            button.getStyle().set("cursor", "pointer");
        }
        footer.add(footerButtons);

        Div shell = new Div(header, loadingBar, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
        removeOverlayPadding(dialog);
    }

    private static void removeOverlayPadding(Dialog dialog) {
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$ && this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) {
                          return;
                        }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) {
                          content.style.padding = '0';
                        }
                        const overlayPart = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (overlayPart) {
                          overlayPart.style.padding = '0';
                          overlayPart.style.overflow = 'hidden';
                        }
                        """);
            }
        });
    }
}

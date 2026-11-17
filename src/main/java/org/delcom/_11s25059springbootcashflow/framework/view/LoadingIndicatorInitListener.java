package org.delcom._11s25059springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import org.jsoup.nodes.DataNode;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi indikator loading global untuk tindakan yang memerlukan waktu tunggu.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    private static final String CONNECTION_INDICATOR_A11Y = """
            (function () {
              function apply() {
                var el = document.querySelector('vaadin-connection-indicator');
                if (!el) { return false; }
                el.setAttribute('role', 'region');
                el.setAttribute('aria-label', 'Status koneksi');
                return true;
              }
              if (!apply()) {
                new MutationObserver(function (m, o) { if (apply()) { o.disconnect(); } })
                    .observe(document.documentElement, { childList: true, subtree: true });
              }
            })();
            """;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        // Atribut bahasa dokumen (aksesibilitas / Lighthouse).
        event.addIndexHtmlRequestListener(response -> {
            response.getDocument().selectFirst("html").attr("lang", "id");
            // Indikator koneksi bawaan Vaadin diberi label region agar tidak berada di luar landmark.
            response.getDocument().body().appendElement("script").appendChild(new DataNode(CONNECTION_INDICATOR_A11Y));
        });
        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration conf = uiEvent.getUI().getLoadingIndicatorConfiguration();
            conf.setApplyDefaultTheme(true);
            conf.setFirstDelay(150);
            conf.setSecondDelay(600);
            conf.setThirdDelay(1500);
        });
    }
}


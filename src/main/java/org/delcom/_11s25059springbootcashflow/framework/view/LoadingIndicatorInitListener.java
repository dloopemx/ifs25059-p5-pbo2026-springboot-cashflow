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

    private static final String INITIAL_SHELL = """
            <main id="app-initial-shell" style="position:absolute;width:1px;height:1px;padding:0;margin:-1px;\
            overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0">\
            <h1>Cashflow - Catatan Keuangan</h1><p>Memuat aplikasi...</p></main>""";

    private static final String INITIAL_SHELL_CLEANUP = """
            (function () {
              function done() {
                var shell = document.getElementById('app-initial-shell');
                if (!shell) { return true; }
                var rendered = document.querySelector('main:not(#app-initial-shell), [role="main"]');
                if (rendered) { shell.remove(); return true; }
                return false;
              }
              if (!done()) {
                new MutationObserver(function (m, o) { if (done()) { o.disconnect(); } })
                    .observe(document.body, { childList: true, subtree: true, attributes: true, attributeFilter: ['role'] });
              }
            })();
            """;

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
            // Kerangka awal (main + h1) agar halaman sudah valid sebelum Vaadin selesai merender;
            // dihapus otomatis setelah konten view muncul.
            response.getDocument().body().prepend(INITIAL_SHELL);
            response.getDocument().body().appendElement("script").appendChild(new DataNode(INITIAL_SHELL_CLEANUP));
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


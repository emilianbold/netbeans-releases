package org.netbeans.core.browser;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.browser.api.WebBrowser;
import org.openide.awt.HtmlBrowser;

/**
 * HTML browser implementation which uses embedded native browser component.
 *
 * @author S. Aubrecht
 */
class HtmlBrowserImpl extends HtmlBrowser.Impl {

    private WebBrowser browser;
    private final Object LOCK = new Object();

    public HtmlBrowserImpl() {
        super();
    }

    @Override
    public Component getComponent() {
        return getBrowser().getComponent();
    }

    private WebBrowser getBrowser() {
        synchronized( LOCK ) {
            if( null == browser ) {
                browser = ApiAccessor.DEFAULT.createWebBrowser();
            }
            return browser;
        }
    }

    @Override
    public void reloadDocument() {
        getBrowser().reloadDocument();
    }

    @Override
    public void stopLoading() {
        getBrowser().stopLoading();
    }

    @Override
    public void setURL(URL url) {
        getBrowser().setURL(url.toString());
    }

    @Override
    public URL getURL() {
        String strUrl = getBrowser().getURL();
        if( null == strUrl )
            return null;
        try {
            return new URL(strUrl);
        } catch( MalformedURLException ex ) {
            Logger.getLogger(BrowserFactory.class.getName()).log(Level.FINE, null, ex);
        }
        return null;
    }

    @Override
    public String getStatusMessage() {
        return getBrowser().getStatusMessage();
    }

    @Override
    public String getTitle() {
        return getBrowser().getTitle();
    }

    @Override
    public boolean isForward() {
        return getBrowser().isForward();
    }

    @Override
    public void forward() {
        getBrowser().forward();
    }

    @Override
    public boolean isBackward() {
        return getBrowser().isBackward();
    }

    @Override
    public void backward() {
        getBrowser().backward();
    }

    @Override
    public boolean isHistory() {
        return getBrowser().isHistory();
    }

    @Override
    public void showHistory() {
        getBrowser().showHistory();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getBrowser().addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getBrowser().removePropertyChangeListener(l);
    }

    @Override
    public void dispose() {
        synchronized( LOCK ) {
            if( null != browser ) {
                browser.dispose();
            }
            browser = null;
        }
    }
}

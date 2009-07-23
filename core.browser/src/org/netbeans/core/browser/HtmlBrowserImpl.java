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

    private final WebBrowser browser;

    public HtmlBrowserImpl() {
        super();
        browser = ApiAccessor.DEFAULT.createWebBrowser();
    }

    @Override
    public Component getComponent() {
        return browser.getComponent();
    }

    @Override
    public void reloadDocument() {
        browser.reloadDocument();
    }

    @Override
    public void stopLoading() {
        browser.stopLoading();
    }

    @Override
    public void setURL(URL url) {
        browser.setURL(url.toString());
    }

    @Override
    public URL getURL() {
        String strUrl = browser.getURL();
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
        return browser.getStatusMessage();
    }

    @Override
    public String getTitle() {
        return browser.getTitle();
    }

    @Override
    public boolean isForward() {
        return browser.isForward();
    }

    @Override
    public void forward() {
        browser.forward();
    }

    @Override
    public boolean isBackward() {
        return browser.isBackward();
    }

    @Override
    public void backward() {
        browser.backward();
    }

    @Override
    public boolean isHistory() {
        return browser.isHistory();
    }

    @Override
    public void showHistory() {
        browser.showHistory();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        browser.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        browser.removePropertyChangeListener(l);
    }

    @Override
    public void dispose() {
        browser.dispose();
    }

//    private void initialize() {
//        synchronized( this ) {
//            if( initialized ) {
//                return;
//            }
//            initialized = true;
//            final XULManager xm = XULManager.getDefault();
//            if( !xm.isFinished() ) {
//                addLabel("Initializing...");
//                xm.addChangeListener(new ChangeListener() {
//
//                    public void stateChanged(ChangeEvent e) {
//                        xm.removeChangeListener(this);
//                        mozilla = new BrowserPanel(propSupport);
//                        wrapper.removeAll();
//                        wrapper.add(mozilla, BorderLayout.CENTER);
//                        if( null != urlToLoad ) {
//                            setURL(urlToLoad);
//                        }
//                    }
//                });
//                return;
//            }
//            if( xm.isXULRunnerAvailable() ) {
//                mozilla = new BrowserPanel(propSupport);
//                wrapper.add(mozilla, BorderLayout.CENTER);
//            } else {
//                addLabel("<XULRunner initialization failed, see log file for more details.>");
//            }
//        }
//    }
//
//    private void addLabel(String text) {
//        JLabel lbl = new JLabel(text);
//        lbl.setHorizontalAlignment(JLabel.CENTER);
//        wrapper.removeAll();
//        wrapper.add(lbl, BorderLayout.CENTER);
//    }
//
//    private void showCookies() {
//        nsICookieManager cm = XPCOMUtils.getService("@browser.org/cookiemanager;1", nsICookieManager.class);
//        if( null != cm ) {
//            nsISimpleEnumerator enumerator = cm.getEnumerator();
//            while( enumerator.hasMoreElements() ) {
//                nsISupports obj = enumerator.getNext();
//                nsICookie cookie = XPCOMUtils.qi(obj, nsICookie.class);
//                if( null != cookie ) {
//                    System.out.println("name: " + cookie.getName());
//                    System.out.println("value: " + cookie.getValue());
//                    System.out.println("host: " + cookie.getHost());
//                    System.out.println("path: " + cookie.getPath());
//                    System.out.println();
//                }
//            }
//            cm.remove("kenai.com", "SSO", "/", false);
//            cm.remove("kenai.com", "auth_token", "/", false);
//        }
//    }
}

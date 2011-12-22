/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.browser.webview;

import java.awt.AWTEvent;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javax.swing.SwingUtilities;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.core.browser.api.WebBrowserEvent;
import org.netbeans.core.browser.api.WebBrowserListener;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Browser component implementation.
 *
 * @author S. Aubrecht
 */
class WebBrowserImpl extends WebBrowser implements BrowserCallback {
    
    private JFXPanel container;
    private String urlToLoad;
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private final List<WebBrowserListener> browserListners = new ArrayList<WebBrowserListener>(10);
    private final Object LOCK = new Object();
    private PropertyChangeListener tcListener;
    
    private WebView browser;


    private static final Logger LOG = Logger.getLogger(WebBrowserImpl.class.getName());

    private static final String CA_DOMAIN = "domain"; //NOI18N
    private static final String CA_PATH = "path"; //NOI18N
    private static final String CA_NAME = "name"; //NOI18N
    private static final String CA_VALUE = "value"; //NOI18N
    private static final String CA_IS_SECURE = "isSecure"; //NOI18N
    private static final String CA_IS_HTTP_ONLY = "isHttpOnly"; //NOI18N
    private static final String CA_IS_SESSION = "isSession"; //NOI18N
    private static final String CA_EXPIRY = "expiry"; //NOI18N
    
    private String status;

    public WebBrowserImpl() {
    }

    @Override
    public Component getComponent() {
        synchronized( LOCK ) {
            if( null == container ) {
                container = new JFXPanel();
                javafx.application.Platform.runLater( new Runnable() {

                    @Override
                    public void run() {
                        createBrowser();
                    }
                });
            }
        }
        return container;
    }

    @Override
    public void reloadDocument() {
        if( !isInitialized() )
            return;
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                browser.getEngine().reload();
            }
        });
    }

    @Override
    public void stopLoading() {
        if( !isInitialized() )
            return;
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                browser.getEngine().getLoadWorker().cancel();
            }
        });
    }

    @Override
    public void setURL(final String url) {
        if( !isInitialized() ) {
            urlToLoad = url;
            return;
        }
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                browser.getEngine().load( url );
            }
        });
    }

    @Override
    public String getURL() {
        if( !isInitialized() )
            return null;
        String url = browser.getEngine().getLocation();
        if( null == url )
            url = urlToLoad;
        return url;
    }

    @Override
    public String getStatusMessage() {
        if( !isInitialized() )
            return null;
        return status;
    }

    @Override
    public String getTitle() {
        if( !isInitialized() )
            return null;
        return browser.getEngine().getTitle();
    }

    @Override
    public boolean isForward() {
        if( !isInitialized() )
            return false;
        return false;
    }

    @Override
    public void forward() {
        if( !isInitialized() )
            return;
//        browser.navigateForward();
    }

    @Override
    public boolean isBackward() {
        if( !isInitialized() )
            return false;
        return false; //browser.isBackNavigationEnabled();
    }

    @Override
    public void backward() {
        if( !isInitialized() )
            return;
//        browser.navigateBack();
    }

    @Override
    public boolean isHistory() {
        return false; //TODO implement this
    }

    @Override
    public void showHistory() {
        //TODO implement this
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propSupport.removePropertyChangeListener(l);
    }

    @Override
    public void setContent(final String content) {
        if( !isInitialized() ) {
            return;
        }
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                browser.getEngine().loadContent( content );
            }
        });
    }

    @Override
    public Document getDocument() {
        if( !isInitialized() )
            return null;
        return browser.getEngine().getDocument();
    }

    @Override
    public void dispose() {
        synchronized( LOCK ) {
            if( isInitialized() ) {
                container.removeAll();
//                browser.getEngine().isposeNativePeer();
                browser = null;
            }
            browserListners.clear();
            container = null;
        }
        if( null != tcListener ) {
            TopComponent.getRegistry().removePropertyChangeListener( tcListener );
        }
    }

    @Override
    public void addWebBrowserListener(WebBrowserListener l) {
        synchronized( browserListners ) {
            browserListners.add(l);
        }
    }

    @Override
    public void removeWebBrowserListener(WebBrowserListener l) {
        synchronized( browserListners ) {
            browserListners.remove(l);
        }
    }

    @Override
    public Map<String, String> getCookie(String domain, String name, String path) {
        if( isInitialized() ) {
//            nsICookieManager cm = XPCOMUtils.getService(SERVICE_COOKIE_MANAGER, nsICookieManager.class);
//            if( null != cm ) {
//                nsISimpleEnumerator enumerator = cm.getEnumerator();
//                nsICookie theCookie = null;
//                while( enumerator.hasMoreElements() ) {
//                    nsISupports obj = enumerator.getNext();
//                    nsICookie cookie = XPCOMUtils.qi(obj, nsICookie.class);
//                    if( null == cookie )
//                        continue;
//                    LOG.log(Level.FINER, "Cookie: domain={0}, name={1}, path={2}, value={3}", //NOI18N
//                        new Object[] { cookie.getHost(), cookie.getName(), cookie.getPath(), cookie.getValue() });
//
//                    if( (null == domain || domain.equals(cookie.getHost()))
//                            && (null == name || name.equals(cookie.getName()))
//                            && (null == path || path.equals(cookie.getPath()))) {
//                        theCookie = cookie;
//                        break;
//                    }
//                }
//                if( null == theCookie ) {
//                    LOG.log(Level.FINE, "Cookie not found, domain={0}, name={1}, path={2}", //NOI18N
//                            new Object[] {domain, name, path} );
//                } else {
//                    return cookie2map( theCookie );
//                }
//            } else {
//                LOG.info("CookieManager interface not found."); //NOI18N
//            }
        }
        return new HashMap<String, String>(0);

    }

    @Override
    public void deleteCookie(String domain, String name, String path) {
        if( !isInitialized() )
            return;
//        nsICookieManager cm = XPCOMUtils.getService(SERVICE_COOKIE_MANAGER, nsICookieManager.class);
//        if( null != cm ) {
//            cm.remove(domain, name, path, false);
//            LOG.log(Level.FINE, "Cookie removed, domain={0}, name={1}, path={2}",  //NOI18N
//                    new Object[] {domain, name, path} );
//        } else {
//            LOG.info("CookieManager interface not found."); //NOI18N
//        }
    }

    @Override
    public void addCookie(Map<String, String> cookie) {
        if( !isInitialized() )
            return;
//        nsICookieManager2 cm = XPCOMUtils.getService(SERVICE_COOKIE_MANAGER, nsICookieManager2.class);
//        if( null != cm ) {
//            String aDomain = cookie.get(CA_DOMAIN);
//            String aPath = cookie.get(CA_PATH);
//            String aName = cookie.get(CA_NAME);
//            String aValue = cookie.get(CA_VALUE);
//            boolean aIsSecure = Boolean.valueOf(cookie.get(CA_IS_SECURE));
//            boolean aIsHttpOnly = Boolean.valueOf(cookie.get(CA_IS_HTTP_ONLY));
//            boolean aIsSession = Boolean.valueOf(cookie.get(CA_IS_SESSION));
//            long aExpiry = 0; //TODO use default expiration interval
//            String expiry = cookie.get(CA_EXPIRY);
//            if( null != expiry ) {
//                aExpiry = Long.valueOf(expiry).longValue();
//            }
//            cm.add(aDomain, aPath, aName, aValue, aIsSecure, aIsHttpOnly, aIsSession, aExpiry);
//        } else {
//            LOG.info("CookieManager2 interface not found."); //NOI18N
//        }
    }

    @Override
    public void executeJavaScript(final String script) {
        if( !isInitialized() )
            return;
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                browser.getEngine().executeScript( script);
            }
        });
    }

    private boolean isInitialized() {
        synchronized( LOCK ) {
            return null != browser;
        }
    }

    public boolean fireBrowserEvent(int type, String url) {
        WebBrowserEventImpl event = new WebBrowserEventImpl(type, this, url);
        urlToLoad = url;
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
        return event.isCancelled();
    }

    public void fireBrowserEvent(int type, AWTEvent e, Node n) {
        WebBrowserEvent event = new WebBrowserEventImpl(type, this, e, n);
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
    }


//    private static Map<String, String> cookie2map(nsICookie cookie) {
//        Map<String, String> res = new HashMap<String, String>(10);
//        res.put(CA_PATH, cookie.getPath());
//        res.put(CA_DOMAIN, cookie.getHost());
////        res.put(CA_EXPIRY, String.valueOf(cookie.getExpiry()));
////        res.put(CA_IS_HTTP_ONLY, String.valueOf(cookie.getIsHttpOnly()));
//        res.put(CA_IS_SECURE, String.valueOf(cookie.getIsSecure()));
////        res.put(CA_IS_SESSION, String.valueOf(cookie.getIsSession()));
//        res.put(CA_NAME, cookie.getName());
//        res.put(CA_VALUE, cookie.getValue());
//        return res;
//    }

    private void createBrowser() {
        browser = new WebView();
        browser.setMinSize(100, 100);
        final WebEngine eng = browser.getEngine();
        eng.setOnStatusChanged( new EventHandler<WebEvent<String>> () {
            @Override
            public void handle( WebEvent<String> e ) {
                final String oldStatus = status;
                status = e.getData();
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_STATUS_MESSAGE, oldStatus, status );
                    }
                } );
            }
        });

        eng.locationProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_URL, oldValue, newValue );
                    }
                } );
            }
        });
        eng.titleProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        propSupport.firePropertyChange( WebBrowser.PROP_TITLE, oldValue, newValue );
                    }
                } );
            }
        });
        container.setScene( new Scene( browser) );
    }
}

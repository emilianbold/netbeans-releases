/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.core.browser.api;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mozilla.browser.MozillaRuntimeException;
import org.mozilla.browser.XPCOMUtils;
import org.mozilla.interfaces.nsICookie2;
import org.mozilla.interfaces.nsICookieManager;
import org.mozilla.interfaces.nsICookieManager2;
import org.mozilla.interfaces.nsISimpleEnumerator;
import org.mozilla.interfaces.nsISupports;
import org.netbeans.core.browser.BrowserCallback;
import org.netbeans.core.browser.BrowserManager;
import org.netbeans.core.browser.BrowserPanel;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Browser component implementation.
 *
 * @author S. Aubrecht
 */
class WebBrowserImpl extends WebBrowser implements BrowserCallback {
    private static final String SERVICE_COOKIE_MANAGER = "@mozilla.org/cookiemanager;1"; //NOI18N

    private JPanel container;
    private BrowserPanel browser;
    private String urlToLoad;
    private String contentToLoad;
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private final List<WebBrowserListener> browserListners = new ArrayList<WebBrowserListener>(10);
    private final Object LOCK = new Object();
    private PropertyChangeListener tcListener;

    private boolean disposed = false;

    private static final Logger LOG = Logger.getLogger(WebBrowserImpl.class.getName());

    private static final String CA_DOMAIN = "domain"; //NOI18N
    private static final String CA_PATH = "path"; //NOI18N
    private static final String CA_NAME = "name"; //NOI18N
    private static final String CA_VALUE = "value"; //NOI18N
    private static final String CA_IS_SECURE = "isSecure"; //NOI18N
    private static final String CA_IS_HTTP_ONLY = "isHttpOnly"; //NOI18N
    private static final String CA_IS_SESSION = "isSession"; //NOI18N
    private static final String CA_EXPIRY = "expiry"; //NOI18N

    public WebBrowserImpl() {
    }

    @Override
    public Component getComponent() {
        synchronized( LOCK ) {
            if( null == container ) {
                final BrowserManager bm = BrowserManager.getDefault();
                container = new BrowserContainer();
                if( bm.isNativeModuleAvailable() ) {
                    //create browser
                    createBrowser();
                } else {
                    //create a prompt for the user download native binaries
                    container.add( bm.createDownloadNativeModulePanel(), BorderLayout.CENTER );
                    BrowserManager.getDefault().addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            bm.removeChangeListener(this);
                            if( bm.isEnabled() ) {
                                createBrowser();
                            } else {
                                createDisabledLabel();
                            }
                        }
                    });
                }
                if( Utilities.isMac() ) {
                    tcListener = createTopComponentListener();
                    TopComponent.getRegistry().addPropertyChangeListener( tcListener );
                }
            }
        }
        return container;
    }

    private void createBrowser() {
        synchronized( LOCK ) {
            container.removeAll();
            browser = new BrowserPanel(propSupport, this);
            if( null != contentToLoad ) {
                browser.loadHTML(contentToLoad);
            } else if( null != urlToLoad ) {
                browser.load(urlToLoad);
            }
            container.add(browser, BorderLayout.CENTER);
            container.revalidate();
            container.invalidate();
            container.repaint();
            parentWindow = new WeakReference<Window>( SwingUtilities.getWindowAncestor(container) );
        }
    }

    private void createDisabledLabel() {
        synchronized( LOCK ) {
            container.removeAll();
            JLabel lbl = new JLabel(NbBundle.getMessage(WebBrowserImpl.class, "LBL_EmbeddedBrowserDisabled")); //NOI18N
            lbl.setVerticalAlignment(JLabel.CENTER);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setEnabled(false);
            container.add(lbl, BorderLayout.CENTER);
            container.revalidate();
            container.invalidate();
            container.repaint();
        }
    }

    @Override
    public void reloadDocument() {
        if( !isInitialized() )
            return;
        browser.reload();
    }

    @Override
    public void stopLoading() {
        if( !isInitialized() )
            return;
        browser.stop();
    }

    @Override
    public void setURL(String url) {
        if( !isInitialized() ) {
            urlToLoad = url;
            return;
        }
        try {
            browser.load(url);
        } catch( MozillaRuntimeException ex ) {
            Logger.getLogger(WebBrowserImpl.class.getName()).log(Level.FINE, null, ex);
        }
    }

    @Override
    public String getURL() {
        if( !isInitialized() )
            return null;
        try {
            return browser.getUrl();
        } catch( MozillaRuntimeException ex ) {
            Logger.getLogger(WebBrowserImpl.class.getName()).log(Level.FINE, null, ex);
        }
        return null;
    }

    @Override
    public String getStatusMessage() {
        if( !isInitialized() )
            return null;
        return browser.getStatusText();
    }

    @Override
    public String getTitle() {
        if( !isInitialized() )
            return null;
        return browser.getTitleText();
    }

    @Override
    public boolean isForward() {
        if( !isInitialized() )
            return false;
        return browser.isForwardEnabled();
    }

    @Override
    public void forward() {
        if( !isInitialized() )
            return;
        browser.goForward();
    }

    @Override
    public boolean isBackward() {
        if( !isInitialized() )
            return false;
        return browser.isBackwardEnabled();
    }

    @Override
    public void backward() {
        if( !isInitialized() )
            return;
        browser.goBack();
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
    public void setContent(String content) {
        if( !isInitialized() ) {
            contentToLoad = content;
            return;
        }
        browser.loadHTML(content);
    }

    @Override
    public Document getDocument() {
        if( !isInitialized() )
            return null;
        return browser.getDocument();
    }

    @Override
    public void dispose() {
        synchronized( LOCK ) {
            disposed = true;
            if( isInitialized() ) {
                browser.dispose();
                container.removeAll();
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
            nsICookieManager cm = XPCOMUtils.getService(SERVICE_COOKIE_MANAGER, nsICookieManager.class);
            if( null != cm ) {
                nsISimpleEnumerator enumerator = cm.getEnumerator();
                nsICookie2 theCookie = null;
                while( enumerator.hasMoreElements() ) {
                    nsISupports obj = enumerator.getNext();
                    nsICookie2 cookie = XPCOMUtils.qi(obj, nsICookie2.class);
                    if( null == cookie )
                        continue;
                    LOG.log(Level.FINER, "Cookie: domain={0}, name={1}, path={2}, value={3}", //NOI18N
                        new Object[] { cookie.getHost(), cookie.getName(), cookie.getPath(), cookie.getValue() });

                    if( (null == domain || domain.equals(cookie.getHost()))
                            && (null == name || name.equals(cookie.getName()))
                            && (null == path || path.equals(cookie.getPath()))) {
                        theCookie = cookie;
                        break;
                    }
                }
                if( null == theCookie ) {
                    LOG.log(Level.FINE, "Cookie not found, domain={0}, name={1}, path={2}", //NOI18N
                            new Object[] {domain, name, path} );
                } else {
                    return cookie2map( theCookie );
                }
            } else {
                LOG.info("CookieManager interface not found."); //NOI18N
            }
        }
        return new HashMap<String, String>(0);

    }

    @Override
    public void deleteCookie(String domain, String name, String path) {
        if( !isInitialized() )
            return;
        nsICookieManager cm = XPCOMUtils.getService(SERVICE_COOKIE_MANAGER, nsICookieManager.class);
        if( null != cm ) {
            cm.remove(domain, name, path, false);
            LOG.log(Level.FINE, "Cookie removed, domain={0}, name={1}, path={2}",  //NOI18N
                    new Object[] {domain, name, path} );
        } else {
            LOG.info("CookieManager interface not found."); //NOI18N
        }
    }

    @Override
    public void addCookie(Map<String, String> cookie) {
        if( !isInitialized() )
            return;
        nsICookieManager2 cm = XPCOMUtils.getService(SERVICE_COOKIE_MANAGER, nsICookieManager2.class);
        if( null != cm ) {
            String aDomain = cookie.get(CA_DOMAIN);
            String aPath = cookie.get(CA_PATH);
            String aName = cookie.get(CA_NAME);
            String aValue = cookie.get(CA_VALUE);
            boolean aIsSecure = Boolean.valueOf(cookie.get(CA_IS_SECURE));
            boolean aIsHttpOnly = Boolean.valueOf(cookie.get(CA_IS_HTTP_ONLY));
            boolean aIsSession = Boolean.valueOf(cookie.get(CA_IS_SESSION));
            long aExpiry = 0; //TODO use default expiration interval
            String expiry = cookie.get(CA_EXPIRY);
            if( null != expiry ) {
                aExpiry = Long.valueOf(expiry).longValue();
            }
            cm.add(aDomain, aPath, aName, aValue, aIsSecure, aIsHttpOnly, aIsSession, aExpiry);
        } else {
            LOG.info("CookieManager2 interface not found."); //NOI18N
        }
    }

    @Override
    public void executeJavaScript(String script) {
        if( !isInitialized() )
            return;
        browser.jsexec(script);
    }

    private boolean isInitialized() {
        synchronized( LOCK ) {
            return null != browser;
        }
    }

    public boolean fireBrowserEvent(int type, String url) {
        WebBrowserEvent event = WebBrowserEvent.create(type, this, url);
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
        return event.isCancelled();
    }

    public void fireBrowserEvent(int type, AWTEvent e, Node n) {
        WebBrowserEvent event = WebBrowserEvent.create(type, this, e, n);
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
    }

    private void forceLayout() {
        Window w = SwingUtilities.getWindowAncestor(browser);
        if( w instanceof JFrame ) {
            JFrame frame = (JFrame) w;
            frame.getRootPane().doLayout();
            frame.getRootPane().invalidate();
            frame.getRootPane().revalidate();
            frame.getRootPane().repaint();
        }
    }

    private PropertyChangeListener createTopComponentListener() {
        return new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if( TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName() )
                    && browser != null && browser.isShowing() ) {
                    browser.requestRepaint();
                }
            }
        };
    }

    private static Map<String, String> cookie2map(nsICookie2 cookie) {
        Map<String, String> res = new HashMap<String, String>(10);
        res.put(CA_PATH, cookie.getPath());
        res.put(CA_DOMAIN, cookie.getHost());
        res.put(CA_EXPIRY, String.valueOf(cookie.getExpiry()));
        res.put(CA_IS_HTTP_ONLY, String.valueOf(cookie.getIsHttpOnly()));
        res.put(CA_IS_SECURE, String.valueOf(cookie.getIsSecure()));
        res.put(CA_IS_SESSION, String.valueOf(cookie.getIsSession()));
        res.put(CA_NAME, cookie.getName());
        res.put(CA_VALUE, cookie.getValue());
        return res;
    }

    private WeakReference<Window> parentWindow;

    private class BrowserContainer extends JPanel {


        public BrowserContainer() {
            super( new BorderLayout() );
            setMinimumSize(new Dimension(10, 10));
        }

        @Override
        public void addNotify() {
            super.addNotify();
            if( isInitialized() ) {
                Window w = SwingUtilities.getWindowAncestor(this);
                if( w != getParentWindow() ) {
                    browser.reparent();
                    if( null != urlToLoad ) {
                        browser.load(urlToLoad);
                    } else {
                        browser.loadHTML("<html></html>"); //NOI18N
                    }
                }
                parentWindow = new WeakReference<Window>(w);
                browser.setVisible(true);
            }
        }

        private Window getParentWindow() {
            return null == parentWindow ? null : parentWindow.get();
        }

        @Override
        public void removeNotify() {
            if( disposed ) {
                super.removeNotify();
            } else {
                urlToLoad = getURL();
                if( isInitialized() ) {
                    browser.setVisible(false);
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if( null == w || !w.isVisible() ) {
                        //something is happening with our window, probably 
                        //switching fullscreen mode
                        parentWindow = null;
                        browser.removeNotify();
                    }
                }
            }
        }

        @Override
        public boolean requestFocusInWindow() {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    if( null != browser ) {
                        browser.requestFocusInBrowser();
                        //for some reason the native browser has invalid
                        //position/size when activated in a topcomponent
                        if( Utilities.isWindows() ) {
                            forceLayout();
                        }
                    }
                }
            });
            return super.requestFocusInWindow();
        }
    }
}

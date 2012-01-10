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

package org.netbeans.core.browser.djnsswt;

import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JPanel;
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
class WebBrowserImpl extends WebBrowser implements BrowserCallback, chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener {
    
    private JPanel container;
    private JWebBrowser browser;
    private String urlToLoad;
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private final List<WebBrowserListener> browserListners = new ArrayList<WebBrowserListener>(10);
    private final Object LOCK = new Object();
    private PropertyChangeListener tcListener;


    private static final Logger LOG = Logger.getLogger(WebBrowserImpl.class.getName());

    private static final String CA_DOMAIN = "domain"; //NOI18N
    private static final String CA_PATH = "path"; //NOI18N
    private static final String CA_NAME = "name"; //NOI18N
    private static final String CA_VALUE = "value"; //NOI18N
    private static final String CA_IS_SECURE = "isSecure"; //NOI18N
    private static final String CA_IS_HTTP_ONLY = "isHttpOnly"; //NOI18N
    private static final String CA_IS_SESSION = "isSession"; //NOI18N
    private static final String CA_EXPIRY = "expiry"; //NOI18N

    private final BrowserType type;
    
    public WebBrowserImpl( BrowserType type ) {
        this.type = type;
    }

    @Override
    public Component getComponent() {
        synchronized( LOCK ) {
            if( null == container ) {
                NativeInterface.open();
                container = new JPanel( new BorderLayout() ) {
                    public void addNotify() {
                        super.addNotify();
                    }
                };
                browser = createBrowser();
                container.add( browser, BorderLayout.CENTER );
            }
        }
        return container;
    }

    @Override
    public void reloadDocument() {
        if( !isInitialized() )
            return;
        browser.reloadPage();
    }

    @Override
    public void stopLoading() {
        if( !isInitialized() )
            return;
        browser.stopLoading();
    }

    @Override
    public void setURL(String url) {
        if( !isInitialized() ) {
            urlToLoad = url;
            return;
        }
        browser.navigate( url);
    }

    @Override
    public String getURL() {
        if( !isInitialized() )
            return null;
        String url = browser.getResourceLocation();
        if( null == url )
            url = urlToLoad;
        return url;
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
        return browser.getPageTitle();
    }

    @Override
    public boolean isForward() {
        if( !isInitialized() )
            return false;
        return browser.isForwardNavigationEnabled();
    }

    @Override
    public void forward() {
        if( !isInitialized() )
            return;
        browser.navigateForward();
    }

    @Override
    public boolean isBackward() {
        if( !isInitialized() )
            return false;
        return browser.isBackNavigationEnabled();
    }

    @Override
    public void backward() {
        if( !isInitialized() )
            return;
        browser.navigateBack();
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
            return;
        }
        browser.setHTMLContent( content );
    }

    @Override
    public Document getDocument() {
        if( !isInitialized() )
            return null;
        return null;//browser.getWebBrowserWindow().Document();
    }

    @Override
    public void dispose() {
        synchronized( LOCK ) {
            if( isInitialized() ) {
                container.removeAll();
                browser.disposeNativePeer();
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
    public Object executeJavaScript(String script) {
        if( !isInitialized() )
            return null;
        return browser.executeJavascriptWithResult(script);
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

    @Override
    public void windowWillOpen( WebBrowserWindowWillOpenEvent wbwwoe ) {
        browser.getWebBrowserWindow().setBarsVisible( false );
    }

    @Override
    public void windowOpening( WebBrowserWindowOpeningEvent wbwoe ) {
    }

    @Override
    public void windowClosing( chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent wbe ) {
    }

    @Override
    public void locationChanging( WebBrowserNavigationEvent wbne ) {
    }

    @Override
    public void locationChanged( WebBrowserNavigationEvent wbne ) {
        boolean isBack = isBackward();
        boolean isForward = isForward();
        propSupport.firePropertyChange(HtmlBrowserImpl.PROP_BACKWARD, !isBack, isBack);
        propSupport.firePropertyChange(HtmlBrowserImpl.PROP_FORWARD, !isForward, isForward);
        propSupport.firePropertyChange(HtmlBrowserImpl.PROP_URL, null, wbne.getNewResourceLocation());
    }

    @Override
    public void locationChangeCanceled( WebBrowserNavigationEvent wbne ) {
    }

    @Override
    public void loadingProgressChanged( chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent wbe ) {
    }

    @Override
    public void titleChanged( chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent wbe ) {
        propSupport.firePropertyChange(HtmlBrowserImpl.PROP_TITLE, null, browser.getPageTitle());
    }

    @Override
    public void statusChanged( chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent wbe ) {
        propSupport.firePropertyChange(HtmlBrowserImpl.PROP_STATUS_MESSAGE, null, browser.getStatusText());
    }

    @Override
    public void commandReceived( WebBrowserCommandEvent wbce ) {
    }
    
    private JWebBrowser createBrowser() {
        List<NSOption> options = new ArrayList<NSOption>( 4 );
        options.add( JWebBrowser.destroyOnFinalization() );
        options.add( JWebBrowser.proxyComponentHierarchy() );
        switch( type ) {
            case Mozilla:
                options.add( JWebBrowser.useXULRunnerRuntime() );
                break;
            case Webkit:
                options.add( JWebBrowser.useWebkitRuntime() );
                break;
        }
        JWebBrowser res = new JWebBrowser( options.toArray( new NSOption[options.size()] ) );  
        res.addWebBrowserListener( this );
        res.setBarsVisible( false );
        return res;
    }
}

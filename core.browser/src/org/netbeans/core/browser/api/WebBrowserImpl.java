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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mozilla.browser.MozillaRuntimeException;
import org.netbeans.core.browser.BrowserCallback;
import org.netbeans.core.browser.BrowserManager;
import org.netbeans.core.browser.BrowserPanel;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Browser component implementation.
 *
 * @author S. Aubrecht
 */
class WebBrowserImpl extends WebBrowser implements BrowserCallback {

    private JPanel container;
    private BrowserPanel browser;
    private String urlToLoad;
    private String contentToLoad;
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private final List<WebBrowserListener> browserListners = new ArrayList<WebBrowserListener>(10);
    private final Object LOCK = new Object();

    public WebBrowserImpl() {
    }

    @Override
    public Component getComponent() {
        synchronized( LOCK ) {
            if( null == container ) {
                final BrowserManager bm = BrowserManager.getDefault();
                container = new JPanel(new BorderLayout()) {

                    @Override
                    public boolean requestFocusInWindow() {
                        if( null != browser )
                            return browser.requestFocusInWindow();
                        return super.requestFocusInWindow();
                    }

                };
                container.setMinimumSize(new Dimension(10, 10));
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
        browser.goForward();
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
            if( isInitialized() ) {
                browser.dispose();
                container.removeAll();
                browser = null;
            }
            browserListners.clear();
            container = null;
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
    public Map<String, List<String>> getCookie(String domain, String name, String path) {
        if( !isInitialized() ) {
            return new HashMap<String, List<String>>(0);
        }

        //TODO implement
        return new HashMap<String, List<String>>(0);

    }

    @Override
    public void deleteCookie(String domain, String name, String path) {
        if( !isInitialized() )
            return;
        //TODO implement
    }

    @Override
    public void addCookie(Map<String, String> cookie) {
        if( !isInitialized() )
            return;
        //TODO implement
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
}

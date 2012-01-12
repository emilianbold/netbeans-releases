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

package org.netbeans.core.browser.webview.ext;

import java.awt.AWTEvent;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
import org.netbeans.core.browser.webview.BrowserCallback;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Browser component implementation.
 *
 * @author S. Aubrecht, Jan Stola
 */
public class WebBrowserImpl extends WebBrowser implements BrowserCallback {
    
    private JFXPanel container;
    private String urlToLoad;
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private final List<WebBrowserListener> browserListners = new ArrayList<WebBrowserListener>();
    private final Object LOCK = new Object();
    private WebView browser;
    private String status;

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
        _setURL( url );
    }
    
    private void _setURL( final String url ) {
        javafx.application.Platform.runLater( new Runnable() {
            @Override
            public void run() {
                String fullUrl = url;
                if (!(url.startsWith( "http://") || url.startsWith( "https://") || url.startsWith("file:/"))) { // NOI18N
                    fullUrl = "http://" + url; // NOI18N
                }
                browser.getEngine().load( fullUrl );
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
        return true;
    }

    @Override
    public void forward() {
        if (isInitialized()) {
            javafx.application.Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    browser.getEngine().executeScript("window.history.forward()"); // NOI18N
                }
            });
        }
    }

    @Override
    public boolean isBackward() {
        return true;
    }

    @Override
    public void backward() {
        if (isInitialized()) {
            javafx.application.Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    browser.getEngine().executeScript("window.history.back()"); // NOI18N
                }
            });
        }
    }

    @Override
    public boolean isHistory() {
        return false;
    }

    @Override
    public void showHistory() {
        throw new UnsupportedOperationException();
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
    public Map<String, String> getCookie(String domain, String name, String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteCookie(String domain, String name, String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCookie(Map<String, String> cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object executeJavaScript(final String script) {
        if( !isInitialized() )
            return null;
        if (javafx.application.Platform.isFxApplicationThread()) {
            return browser.getEngine().executeScript(script);
        } else {
            final Object[] result = new Object[1];
            final CountDownLatch latch = new CountDownLatch(1);
            javafx.application.Platform.runLater( new Runnable() {
                @Override
                public void run() {
                    try {
                        result[0] = browser.getEngine().executeScript(script);
                    } finally {
                        latch.countDown();
                    }
                }
            });
            try {
                latch.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return result[0];
        }
    }

    private boolean isInitialized() {
        synchronized( LOCK ) {
            return null != browser;
        }
    }

    @Override
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

    @Override
    public void fireBrowserEvent(int type, AWTEvent e, Node n) {
        WebBrowserEvent event = new WebBrowserEventImpl(type, this, e, n);
        synchronized( browserListners ) {
            for( WebBrowserListener l : browserListners ) {
                l.onDispatchEvent(event);
            }
        }
    }

    private void createBrowser() {
        WebView view = new WebView();
        view.setMinSize(100, 100);
        final WebEngine eng = view.getEngine();
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
        container.setScene( new Scene( view ) );
        
        browser = view;
        
        if( null != urlToLoad ) {
            _setURL( urlToLoad );
            urlToLoad = null;
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core;

import java.awt.Component;
import java.awt.Desktop;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.core.ui.SwingBrowser;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.awt.HtmlBrowser.Impl;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Implementation of URL displayer, which shows documents in the configured web browser.
 */
@ServiceProvider(service=URLDisplayer.class)
public final class NbURLDisplayer extends URLDisplayer {

    private NbBrowser htmlViewer;

    public void showURL(final URL u) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                if (htmlViewer == null) {
                    htmlViewer = new NbBrowser();
                }
                htmlViewer.showUrl(u);
            }
        });
    }

    @Override
    public void showURLExternal(final URL u) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                if (htmlViewer == null) {
                    htmlViewer = new NbBrowser();
                }
                htmlViewer.showUrlExternal(u);
            }
        });
    }

    /**
     * Able to reuse HtmlBrowserComponent.
     */
    private static class NbBrowser {

        private HtmlBrowserComponent brComp;
        private HtmlBrowserComponent externalBrowser;
        private PreferenceChangeListener idePCL;
        private static Lookup.Result factoryResult;

        static {
            factoryResult = Lookup.getDefault().lookupResult(Factory.class);
            factoryResult.allItems();
            factoryResult.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    ((NbURLDisplayer) URLDisplayer.getDefault()).htmlViewer = null;
                }
            });
        }

        public NbBrowser() {
            Factory browser = IDESettings.getWWWBrowser();
            if (browser == null) {
                // Fallback.
                browser = new SwingBrowser();
            }
            // try if an internal browser is set and possibly try to reuse an
            // existing component
            if (browser.createHtmlBrowserImpl().getComponent() != null) {
                brComp = findOpenedBrowserComponent();
            }
            if (brComp == null) {
                brComp = new HtmlBrowserComponent(browser, true, true);
                brComp.putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N
            }
            browser = IDESettings.getExternalWWWBrowser();
            if (browser == null) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    browser = new DesktopBrowser(desktop);
                } else {
                    //external browser is not available, fallback to swingbrowser
                    browser = new SwingBrowser();
                }
            }
            externalBrowser = new HtmlBrowserComponent(browser, true, true);
            setListener();
        }

        /**
         * Tries to find already opened <code>HtmlBrowserComponent</code>. In
         * the case of success returns the instance, null otherwise.
         */
        private HtmlBrowserComponent findOpenedBrowserComponent() {
            for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
                if (tc instanceof HtmlBrowserComponent) {
                    return (HtmlBrowserComponent) tc;
                }
            }
            return null;
        }

        /** Show URL in browser
         * @param url URL to be shown
         */
        private void showUrl(URL url) {
            brComp.setURLAndOpen(url);
        }

        /**
         * Show URL in an external browser.
         * @param url URL to show
         */
        private void showUrlExternal(URL url) {
            externalBrowser.setURLAndOpen(url);
        }

        /**
         *  Sets listener that invalidates this as main IDE's browser if user changes the settings
         */
        private void setListener() {
            if (idePCL != null) {
                return;
            }
            try {
                // listen on preffered browser change
                idePCL = new PreferenceChangeListener() {
                    public void preferenceChange(PreferenceChangeEvent evt) {
                        if (IDESettings.PROP_WWWBROWSER.equals(evt.getKey())) {
                            ((NbURLDisplayer) URLDisplayer.getDefault()).htmlViewer = null;
                            if (idePCL != null) {
                                IDESettings.getPreferences().removePreferenceChangeListener(idePCL);
                                idePCL = null;
                                brComp = null;
                            }
                        }
                    }
                };
                IDESettings.getPreferences().addPreferenceChangeListener(idePCL);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final class DesktopBrowser implements Factory {
        private final Desktop desktop;
        public DesktopBrowser(Desktop desktop) {
            this.desktop = desktop;
        }
        public @Override Impl createHtmlBrowserImpl() {
            return new Impl() {
                private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
                private URL url;
                public @Override void setURL(URL url) {
                    this.url = url;
                    try {
                        desktop.browse(url.toURI());
                    } catch (Exception x) {
                        Logger.getLogger(NbURLDisplayer.class.getName()).log(Level.INFO, "showing: " + url, x);
                    }
                }
                public @Override URL getURL() {
                    return url;
                }
                public @Override void reloadDocument() {
                    setURL(url);
                }
                public @Override void addPropertyChangeListener(PropertyChangeListener l) {
                    pcs.addPropertyChangeListener(l);
                }
                public @Override void removePropertyChangeListener(PropertyChangeListener l) {
                    pcs.removePropertyChangeListener(l);
                }
                public @Override Component getComponent() {return null;}
                public @Override void stopLoading() {}
                public @Override String getStatusMessage() {return "";}
                public @Override String getTitle() {return "";}
                public @Override boolean isForward() {return false;}
                public @Override void forward() {}
                public @Override boolean isBackward() {return false;}
                public @Override void backward() {}
                public @Override boolean isHistory() {return false;}
                public @Override void showHistory() {}
            };
        }
    }

}

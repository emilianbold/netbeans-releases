/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.browser.api;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.NbBundle;

/**
 * Support for web browsers.
 * @since 1.9
 */
public final class WebBrowserSupport {

    private static final Logger LOGGER = Logger.getLogger(WebBrowserSupport.class.getName());

    private static final String DEFAULT = "default"; // NOI18N
    private static final String INTEGRATED = ".INTEGRATED"; // NOI18N


    private WebBrowserSupport() {
    }

    /**
     * Create model for component with browsers, possibly with the
     * {@link BrowserComboBoxModel#getSelectedBrowserId() selected browser identifier}.
     * <p>
     * If the browser identifier is {@code null} (likely not set yet?), then the first (external,
     * if possible) browser with NetBeans integration is selected.
     * @param selectedBrowserId browser identifier, can be {@code null} if e.g. not set yet
     * @return model for component with browsers
     * @see #createBrowserRenderer()
     */
    public static BrowserComboBoxModel createBrowserModel(@NullAllowed String selectedBrowserId) {
        List<BrowserWrapper> browsers = new ArrayList<BrowserWrapper>();
        int chrome = 200;
        int chromium = 300;
        int others = 400;
        BrowserWrapper nbChrome = null;
        BrowserWrapper nbChromium = null;
        BrowserWrapper nbInternal = null;
        browsers.add(new BrowserWrapper(null, 1, true));
        for (WebBrowser browser : WebBrowsers.getInstance().getAll(false)) {
            if (browser.getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW) {
                nbInternal = new BrowserWrapper(browser, 100, false);
                browsers.add(nbInternal);
            } else if (browser.getBrowserFamily() == BrowserFamilyId.CHROME || browser.getId().endsWith("ChromeBrowser")) { // NOI18N
                BrowserWrapper wrapper = new BrowserWrapper(browser, chrome++, false);
                if (nbChrome == null) {
                    nbChrome = wrapper;
                }
                browsers.add(wrapper);
                browsers.add(new BrowserWrapper(browser, chrome++, true));
            } else if (browser.getBrowserFamily() == BrowserFamilyId.CHROMIUM || browser.getId().endsWith("ChromiumBrowser")) { // NOI18N
                BrowserWrapper wrapper = new BrowserWrapper(browser, chromium++, false);
                if (nbChromium == null) {
                    nbChromium = wrapper;
                }
                browsers.add(wrapper);
                browsers.add(new BrowserWrapper(browser, chromium++, true));
            } else {
                browsers.add(new BrowserWrapper(browser, others++, true));
            }
        }
        Collections.sort(browsers, new Comparator<BrowserWrapper>() {
            @Override
            public int compare(BrowserWrapper o1, BrowserWrapper o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        if (selectedBrowserId == null) {
            if (nbChrome != null) {
                selectedBrowserId = nbChrome.getId();
            } else if (nbChromium != null) {
                selectedBrowserId = nbChromium.getId();
            } else if (nbInternal != null) {
                selectedBrowserId = nbInternal.getId();
            }
        }
        BrowserComboBoxModel model = new BrowserComboBoxModel(browsers);
        for (int i = 0; i < model.getSize(); i++) {
            BrowserWrapper browserWrapper = (BrowserWrapper) model.getElementAt(i);
            assert browserWrapper != null;
            if ((selectedBrowserId == null
                    && !browserWrapper.isDisableIntegration())
                    || browserWrapper.getId().equals(selectedBrowserId)) {
                model.setSelectedItem(browserWrapper);
                break;
            }
        }
        return model;
    }

    /**
     * Create renderer for component with browsers.
     * @return renderer for component with browsers
     * @see #createBrowserModel(String)
     */
    public static ListCellRenderer createBrowserRenderer() {
        return new BrowserRenderer();
    }

    /**
     * Check whether the given {@link BrowserComboBoxModel#getSelectedBrowserId() browser identifier} represents browser with NetBeans integration.
     * <p>
     * If the browser identifier is {@code null} (likely not set yet?), then the first (external,
     * if possible) browser with NetBeans integration is used.
     * @param browserId browser identifier, can be {@code null} if e.g. not set yet
     * @return {@code true} if the given browser identifier represents browser with NetBeans integration
     */
    public static boolean isIntegratedBrowser(@NullAllowed String browserId) {
        if (browserId != null
                && browserId.endsWith(INTEGRATED)) {
            return true;
        }
        ComboBoxModel model = createBrowserModel(browserId);
        return !((BrowserWrapper) model.getSelectedItem()).isDisableIntegration();
    }

    /**
     * Get browser for the given {@link BrowserComboBoxModel#getSelectedBrowserId() browser identifier}. Returns {@code null}
     * for the default IDE browser (set in IDE Options).
     * If the browser identifier is {@code null} (likely not set yet?), then the first (external,
     * if possible) browser with NetBeans integration is returned.
     * @param browserId browser identifier, can be {@code null} if e.g. not set yet
     * @return browser for the given browser identifier or {@code null} for the default IDE browser
     */
    @CheckForNull
    public static WebBrowser getBrowser(@NullAllowed String browserId) {
        if (DEFAULT.equals(browserId)) {
            return null;
        }
        ComboBoxModel model = createBrowserModel(browserId);
        return ((BrowserWrapper) model.getSelectedItem()).getBrowser();
    }

    //~ Inner classes

    /**
     * Model for component with browsers.
     */
    public static final class BrowserComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private static final long serialVersionUID = -45857643232L;

        private final List<BrowserWrapper> browsers = new CopyOnWriteArrayList<BrowserWrapper>();

        private volatile BrowserWrapper selectedBrowser = null;


        BrowserComboBoxModel(List<BrowserWrapper> browsers) {
            assert browsers != null;
            assert !browsers.isEmpty();
            this.browsers.addAll(browsers);
            selectedBrowser = browsers.get(0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getSize() {
            return browsers.size();
        }

        /**
         * {@inheritDoc}
         */
        @CheckForNull
        @Override
        public Object getElementAt(int index) {
            try {
                return browsers.get(index);
            } catch (IndexOutOfBoundsException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSelectedItem(Object browser) {
            selectedBrowser = (BrowserWrapper) browser;
            fireContentsChanged(this, -1, -1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getSelectedItem() {
            assert selectedBrowser != null;
            return selectedBrowser;
        }

        /**
         * Get selected browser or {@code null} if the IDE default browser selected.
         * @return selected browser or {@code null} if the IDE default browser selected
         */
        @CheckForNull
        public WebBrowser getSelectedBrowser() {
            assert selectedBrowser != null;
            return selectedBrowser.getBrowser();
        }

        /**
         * Get selected browser identifier.
         * @return selected browser identifier
         */
        public String getSelectedBrowserId() {
            assert selectedBrowser != null;
            return selectedBrowser.getId();
        }

    }

    /**
     * Renderer for component with browsers.
     */
    private static final class BrowserRenderer implements ListCellRenderer {

        // @GuardedBy("EDT")
        private static final ListCellRenderer ORIGINAL_RENDERER = new JComboBox().getRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert EventQueue.isDispatchThread();
            if (value instanceof BrowserWrapper) {
                value = ((BrowserWrapper) value).getDesc();
            }
            return ORIGINAL_RENDERER.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }

    /**
     * Wrapper class for {@link WebBrowser}.
     * <p>
     * This class is thread-safe.
     */
    private static final class BrowserWrapper {

        private final WebBrowser browser;
        private final int order;
        private final boolean disableIntegration;


        public BrowserWrapper(WebBrowser browser, int order, boolean disableIntegration) {
            this.browser = browser;
            this.order = order;
            this.disableIntegration = disableIntegration;
        }

        @NbBundle.Messages({
            "WebBrowserSupport.browser.ideDefault=IDE's default browser",
            "# {0} - web browser",
            "WebBrowserSupport.browser.integrated={0} with NetBeans Integration"
        })
        public String getDesc() {
            if (browser == null) {
                return Bundle.WebBrowserSupport_browser_ideDefault();
            }
            if (disableIntegration
                    || browser.getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW) {
                return browser.getName();
            }
            return Bundle.WebBrowserSupport_browser_integrated(browser.getName());
        }

        public boolean isDisableIntegration() {
            return disableIntegration;
        }

        public WebBrowser getBrowser() {
            return browser;
        }

        public String getId() {
            if (browser == null) {
                return DEFAULT;
            }
            return browser.getId() + (disableIntegration ? "" : INTEGRATED); // NOI18N
        }

        int getOrder() {
            return order;
        }

    }

}

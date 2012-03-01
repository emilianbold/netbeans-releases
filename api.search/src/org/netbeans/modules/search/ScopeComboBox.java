/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author jhavlin
 */
public class ScopeComboBox extends org.netbeans.api.search.ui.ScopeComboBox {

    SearchScopeChangeListener searchScopeChangeListener;
    Lookup.Result lookupResult;
    private SearchScopeDefinition selectedSearchScope;
    private ManualSelectionListener manualSelectionListener;
    private String manuallySelectedId = null;
    SearchScopeList scopeList = new SearchScopeList();

    public ScopeComboBox(String prefferedId) {
        super();
        manualSelectionListener = new ManualSelectionListener();
        searchScopeChangeListener = new SearchScopeChangeListener();
        scopeList.addChangeListener(
                WeakListeners.change(searchScopeChangeListener,
                scopeList));
        setEditable(false);
        init(prefferedId);
    }

    /**
     * Add row with selection of scope to the form panel.
     */
    protected final void init(String prefferedId) {

        updateScopeItems(prefferedId);
        this.addActionListener(manualSelectionListener);
    }

    private void updateScopeItems(String prefferedId) {

        this.removeAllItems();
        selectedSearchScope = null;

        for (SearchScopeDefinition ss: scopeList.getSeachScopeDefinitions()) {
            if (ss.isApplicable()) { // add only enabled search scopes
                ScopeItem si = new ScopeItem(ss);
                this.addItem(si);
                if (selectedSearchScope == null) {
                    if (ss.getTypeId().equals(prefferedId)) {
                        selectedSearchScope = ss;
                        this.setSelectedItem(si);
                    }
                }
            }
        }
        if (selectedSearchScope == null) {
            ScopeItem si = (ScopeItem) this.getItemAt(0);
            selectedSearchScope = si.getSearchScope();
            this.setSelectedIndex(0);
        }
    }

    /**
     *
     * @return Currently selected search scope, or null if no search scope is
     * available.
     */
    public SearchScopeDefinition getSelectedSearchScope() {
        return selectedSearchScope;
    }

    @Override
    public SearchInfo getSearchScopeInfo() {
        SearchScopeDefinition ss = getSelectedSearchScope();
        if (ss == null) {
            return null;
        } else {
            SearchInfo ssi = ss.getSearchInfo();
            return ssi;
        }
    }

    /**
     * Wrapper of scope to be used as JComboBox item.
     */
    private final class ScopeItem {

        private static final String START = "(";                       // NOI18N
        private static final String END = ")";                         // NOI18N
        private static final String SP = " ";                          // NOI18N
        private static final String ELLIPSIS = "...";                  // NOI18N
        private static final int MAX_EXTRA_INFO_LEN = 20;
        private SearchScopeDefinition searchScope;

        public ScopeItem(SearchScopeDefinition searchScope) {
            this.searchScope = searchScope;
        }

        public SearchScopeDefinition getSearchScope() {
            return this.searchScope;
        }

        private boolean isAdditionaInfoAvailable() {
            return searchScope.getAdditionalInfo() != null
                    && searchScope.getAdditionalInfo().length() > 0;
        }

        private String getTextForLabel(String text) {
            String extraInfo = searchScope.getAdditionalInfo();
            String extraText = extraInfo;
            if (extraInfo.length() > MAX_EXTRA_INFO_LEN) {
                extraText = extraInfo.substring(0, MAX_EXTRA_INFO_LEN)
                        + ELLIPSIS;
                if (extraText.length() >= extraInfo.length()) {
                    extraText = extraInfo;
                }
            }
            return getFullText(text, extraText);
        }

        private String getFullText(String text, String extraText) {
            return text + SP + START + SP + extraText + SP + END;
        }

        @Override
        public String toString() {
            if (isAdditionaInfoAvailable()) {
                return getTextForLabel(clr(searchScope.getDisplayName()));
            } else {
                return clr(searchScope.getDisplayName());
            }
        }

        /**
         * Clear some legacy special characters from scope names.
         *
         * Some providers can still include ampresands that were used for
         * mnemonics in previous versions, but now are ignored.
         */
        private String clr(String s) {
            return s.replaceAll("\\&", "");                             //NOI18N
        }
    }

    private class SearchScopeChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (manuallySelectedId == null && selectedSearchScope != null) {
                manuallySelectedId = selectedSearchScope.getTypeId();
            }
            removeActionListener(manualSelectionListener);
            updateScopeItems(manuallySelectedId);
            addActionListener(manualSelectionListener);
            Dialog d = (Dialog) SwingUtilities.getAncestorOfClass(
                    Dialog.class, ScopeComboBox.this);
            if (d != null) {
                d.repaint();
            }
        }
    }

    private class ManualSelectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ScopeItem item = (ScopeItem) getSelectedItem();
            if (item != null) {
                selectedSearchScope = item.getSearchScope();
                manuallySelectedId = selectedSearchScope.getTypeId();
            } else {
                selectedSearchScope = null;
            }
        }
    }

    /**
     * Clear this component - unregister registered listeners.
     */
    @Override
    public void clean() {
        lookupResult = null;
        scopeList.removeChangeListener(
                searchScopeChangeListener);
        scopeList.clean();
    }
}

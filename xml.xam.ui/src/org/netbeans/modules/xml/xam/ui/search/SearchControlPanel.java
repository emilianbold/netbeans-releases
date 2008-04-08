/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.xam.ui.search;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.category.SearchComponent;
import org.netbeans.modules.xml.xam.ui.highlight.Highlight;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightGroup;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightManager;
import org.openide.util.NbBundle;

/**
 * Presents the search component for XAM-based editors.
 *
 * @author  Nathan Fiedler
 */
public abstract class SearchControlPanel extends JPanel
        implements ActionListener, SearchComponent, SearchListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The search input field. */
    private SearchFieldPanel searchField;
    /** If not null, the results of the most recent search. */
    private List<Object> searchResults;
    /** Offset into searchResults which is currently shown. */
    private int searchResultIndex;
    private Action findAcion;
    
    /**
     * Creates new form SearchControlPanel.
     */
    public SearchControlPanel() {
        initComponents();
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);
        closeButton.addActionListener(this);
        nextButton.addActionListener(this);
        prevButton.addActionListener(this);
        resetButton.addActionListener(this);
        searchField = new SearchFieldPanel();
        searchField.addSearchListener(this);
        fieldPanel.add(searchField, BorderLayout.CENTER);
        
        //IZ 91545
        findAcion = new FindAction();
        nextButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "findNext"); //NOI18N
        nextButton.getActionMap().put("findNext", findAcion); //NOI18N
        prevButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK), "findPrevious"); //NOI18N
        prevButton.getActionMap().put("findPrevious", findAcion);
    }
    
    class FindAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            performAction(e);
        }        
    }
    
    public void actionPerformed(ActionEvent event) {
        performAction(event);
    }

    private void performAction(ActionEvent event) {
        Object src = event.getSource();
        if (src == closeButton) {
            dismissSearch();
            hideComponent();
        } else if (src == resetButton) {
            dismissSearch();
            searchField.prepareForInput(true);
        } else if (src == nextButton) {
            if(!isSameQuery()) {
                dismissSearch();
                searchField.newSearch();
            }
            searchResultIndex++;
            if (searchResultIndex >= searchResults.size()) {
                searchResultIndex = 0;
                beep();
            }
            if(searchResults.size() > 0)
                showSearchResult(searchResults.get(searchResultIndex));
        } else if (src == prevButton) {
            if(!isSameQuery()) {
                dismissSearch();
                searchField.newSearch();
            }
            searchResultIndex--;          
            if (searchResultIndex < 0) {
                searchResultIndex = searchResults.size() - 1;
                beep();
            }
            if(searchResults.size() > 0 )
                showSearchResult(searchResults.get(searchResultIndex));
            searchField.requestFocus();
        }
    }
    
    private boolean isSameQuery() {
        String text = searchField.currentSearchString();
        String lastText = searchField.lastSearchString();
        if(text == null) text = "";
        if(lastText == null) lastText = "";
        if(!searchResults.isEmpty() && !text.equals(lastText)) {
            return false;
        }
        
        return true;
    }
    
    private void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Hide the results of the previous search, if any.
     */
    private void dismissSearch() {
        hideResults();
        // Clear the cached search results.
        searchResults = null;
        // No results, no navigation either.
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);
        resultsLabel.setText("   ");
    }

    protected void hideResults() {
        HighlightManager hm = HighlightManager.getDefault();
        List<HighlightGroup> groups = hm.getHighlightGroups(HighlightGroup.SEARCH);
        if (groups != null) {
            for (HighlightGroup group : groups) {
                hm.removeHighlightGroup(group);
            }
        }
    }

    public java.awt.Component getComponent() {
        return this;
    }

    public void hideComponent() {
        setVisible(false);
    }

    public void showComponent() {
        setVisible(true);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            searchField.prepareForInput(false);
        }
        revalidate();
        repaint();
    }

    public void searchCommenced(SearchEvent event) {
        dismissSearch();
    }

    public void searchDismissed(SearchEvent event) {
        dismissSearch();
        hideComponent();
    }

    public void searchFailed(SearchEvent event) {
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);
        SearchException exc = event.getException();
        String msg = null;
        if (exc != null) {
            msg = exc.getMessage();
            if (msg == null || msg.length() == 0) {
                msg = exc.toString();
            }
        } else {
            msg = NbBundle.getMessage(SearchControlPanel.class,
                    "LBL_SearchControlPanel_Failed");
        }
        resultsLabel.setText(msg);
    }

    public void searchFinished(SearchEvent event) {
        searchResults = event.getResults();
        if (searchResults.isEmpty()) {
            nextButton.setEnabled(false);
            prevButton.setEnabled(false);
            resultsLabel.setText(NbBundle.getMessage(SearchControlPanel.class,
                    "LBL_SearchControlPanel_NoResults"));
        } else {
            nextButton.setEnabled(true);
            prevButton.setEnabled(true);
            // Generate Highlight instances for each matching result, and its
            // parents, grand parents, and so on, up to the root component.
            HighlightManager hm = HighlightManager.getDefault();
            HighlightGroup group = new HighlightGroup(HighlightGroup.SEARCH);
            Iterator<Object> iter = searchResults.iterator();

            while (iter.hasNext()) {
                Object object = iter.next();

                if ( !(object instanceof Component)) {
                  continue;
                }
                Component comp = (Component) object;
                SearchHighlight h = new SearchHighlight(comp, Highlight.SEARCH_RESULT);
                group.addHighlight(h);
                Component parent = comp.getParent();
                
                while (parent != null) {
                    h = new SearchHighlight(parent, Highlight.SEARCH_RESULT_PARENT);
                    group.addHighlight(h);
                    parent = parent.getParent();
                }
            }
            hm.addHighlightGroup(group);
            // Show the first matching result.
            searchResultIndex = 0;
            showSearchResult(searchResults.get(searchResultIndex));
            int count = searchResults.size();
            if (count == 1) {
                resultsLabel.setText(NbBundle.getMessage(SearchControlPanel.class,
                        "LBL_SearchControlPanel_OneResult"));
            } else {
                resultsLabel.setText(NbBundle.getMessage(SearchControlPanel.class,
                        "LBL_SearchControlPanel_MultipleResults", count));
            }
        }
    }

    /**
     * Set the search providers to be made available in the search field.
     *
     * @param  providers  the available search providers (may be null).
     */
    public void setProviders(Collection providers) {
        if (providers != null && providers.size() > 0) {
            searchField.setProviders(providers);
            searchField.setEnabled(true);
        } else {
            searchField.setEnabled(false);
        }
    }

    /**
     * Make the given search result visible in the editor.
     *
     * @param  result  search result to show.
     */
    protected abstract void showSearchResult(Object result);

    /**
     * Concrete implementation of a Highlight.
     */
    private static class SearchHighlight extends Highlight {

        public SearchHighlight(Component comp, String searchResults) {
            super(comp, searchResults);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        closeButton = new javax.swing.JButton();
        fieldLabel = new javax.swing.JLabel();
        fieldPanel = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        resultsLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/xam/ui/search/search_close.png")));
        closeButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("HINT_SearchControlPanel_Close"));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        closeButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/xam/ui/search/search_close_light.png")));

        fieldLabel.setLabelFor(fieldPanel);
        fieldLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("LBL_SearchControlPanel_Find"));

        fieldPanel.setLayout(new java.awt.BorderLayout());

        nextButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("KEY_SearchControlPanel_FindNext").charAt(0));
        nextButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("LBL_SearchControlPanel_FindNext"));
        nextButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("HINT_SearchControlPanel_FindNext"));

        prevButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("KEY_SearchControlPanel_FindPrevious").charAt(0));
        prevButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("LBL_SearchControlPanel_FindPrevious"));
        prevButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("HINT_SearchControlPanel_FindPrevious"));

        resetButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("KEY_SearchControlPanel_Reset").charAt(0));
        resetButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("LBL_SearchControlPanel_Reset"));
        resetButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("TIP_SearchControlPanel_Reset"));

        resultsLabel.setText("   ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(closeButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fieldLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fieldPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(nextButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(prevButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resetButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resultsLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(closeButton)
                .add(fieldLabel)
                .add(nextButton)
                .add(prevButton)
                .add(resetButton)
                .add(resultsLabel))
            .add(fieldPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel fieldLabel;
    private javax.swing.JPanel fieldPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel resultsLabel;
    // End of variables declaration//GEN-END:variables
}

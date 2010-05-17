/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Interface for searching an XML document.
 *
 * @author  Nathan Fiedler
 */
public class SearchFieldPanel extends JPanel
        implements ActionListener, DocumentListener, FocusListener,
        KeyListener, MouseListener, PopupMenuListener {
    /** Color used to indicate no match. */
    private static final Color MATCH_FAILED_COLOR = new Color(255, 102, 102);
    /** silence compiler warnings */
    static final long serialVersionUID = 1L;
    /** A mapping of action command name to SearchProvider instances. */
    private Map<String, SearchProvider> buttonProviderMap;
    /** If true, the text field is expecting to get input from the user,
     * otherwise it is displaying the search type label. */
    private boolean expectingInput;
    /** Starts the search from the selected component. */
    private JCheckBoxMenuItem fromSelectedMenuItem;
    /** Indicates if search phrase is a regular expression. */
    private JCheckBoxMenuItem regexMenuItem;
    /** List of search listeners. */
    private EventListenerList searchListeners = new EventListenerList();
    /** Last search string. */
    private String lastSearchString;
    
    /**
     * Creates new form SearchFieldPanel.
     */
    public SearchFieldPanel() {
        initComponents();
        typesButton.addActionListener(this);
        searchTextField.addKeyListener(this);
        searchTextField.addActionListener(this);
        searchTextField.addFocusListener(this);
        searchTextField.addMouseListener(this);
        searchTextField.getDocument().addDocumentListener(this);
        typesPopupMenu.addPopupMenuListener(this);
        buttonProviderMap = new HashMap<String, SearchProvider>();
    }
    
    String lastSearchString() {
        return lastSearchString;
    }

    String currentSearchString() {
        return searchTextField.getText();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == typesButton) {
            typesPopupMenu.show(typesButton, 0, typesButton.getHeight());
        } else if (src == searchTextField) {
            newSearch();
        } else if (src instanceof JMenuItem) {
            if (!expectingInput) {
                // One of the serach type menu items was selected, need to
                // update the search text field appropriately.
                indicateSearchType();
            } else {
                // Need to set the text field tooltip appropriately.
                ButtonModel bm = typeButtonGroup.getSelection();
                SearchProvider sp = buttonProviderMap.get(bm.getActionCommand());
                searchTextField.setToolTipText(sp.getInputDescription());
            }
        }
    }
    
    void newSearch() {
        fireSearchEvent(SearchEvent.Type.COMMENCED);
        // Perform the search.
        String text = searchTextField.getText();
        if (text.length() > 0) {
            ButtonModel model = typeButtonGroup.getSelection();
            SearchProvider provider = buttonProviderMap.get(
                    model.getActionCommand());
            boolean selected = fromSelectedMenuItem.isSelected();
            boolean regex = regexMenuItem.isSelected();
            Query query = new Query(text, selected, regex);
            try {
                List<Object> results = provider.search(query);
                if (results.isEmpty()) {
                    indicateFailure();
                } else {
                    clearFailure(false);
                }
                lastSearchString = text;
                fireSearchComplete(results);
            } catch (SearchException se) {
                fireSearchFailed(se);
            }
        } else {
            // Notify listeners that the search was empty.
            List<Object> empty = Collections.emptyList();
            fireSearchComplete(empty);
        }        
    }

    /**
     * Add the given search listener to this search interface. The listener
     * will be notified when the user performs a search.
     *
     * @param  l  search listener.
     */
    public void addSearchListener(SearchListener l) {
        searchListeners.add(SearchListener.class, l);
    }

    public void changedUpdate(DocumentEvent e) {
    }

    /**
     * Reset the text field colors to their usual defaults.
     *
     * @param  clear  true to erase contents of text field, false to leave as-is.
     */
    private void clearFailure(boolean clear) {
        Color color = UIManager.getDefaults().getColor("TextField.background");
        setBackground(color);
        searchTextField.setBackground(color);
        color = UIManager.getDefaults().getColor("TextField.foreground");
        searchTextField.setForeground(color);
        if (clear) {
            searchTextField.setText("");
        }
    }

    /**
     * Notify the search listeners that the search is complete.
     *
     * @return  results  list of search results.
     */
    protected void fireSearchComplete(List<Object> results) {
        fireSearchEvent(new SearchEvent(this, SearchEvent.Type.FINISHED, results));
    }

    /**
     * Notify the search listeners of a particular search event type.
     *
     * @param  type  search event type.
     */
    private void fireSearchEvent(SearchEvent.Type type) {
        fireSearchEvent(new SearchEvent(this, type));
    }

    /**
     * Notify the search listeners that the search failed.
     *
     * @return  error  the search exception that occurred.
     */
    protected void fireSearchFailed(SearchException error) {
        fireSearchEvent(new SearchEvent(this, SearchEvent.Type.FAILED, error));
    }

    /**
     * Notify the search listeners of a particular search event type.
     */
    private void fireSearchEvent(SearchEvent event) {
        Object[] listeners = searchListeners.getListenerList();
        SearchEvent.Type type = event.getType();
        for (int ii = listeners.length - 2; ii >= 0; ii -= 2) {
            if (listeners[ii] == SearchListener.class) {
                type.fireEvent(event, (SearchListener) listeners[ii + 1]);
            }
        }
    }

    public void focusGained(FocusEvent e) {
        // Ignore focus changes due to switching between apps.
        if (e.getSource() == searchTextField && !expectingInput) {
            // Field is showing search type name, so clear it for user to
            // enter input, otherwise it was user input, so leave it as-is.
            clearFailure(true);
            expectingInput = true;
        }
    }

    public void focusLost(FocusEvent e) {
        if (e.getSource() == searchTextField &&
                searchTextField.getText().length() == 0) {
            clearFailure(false);
            indicateSearchType();
        }
    }

    /**
     * Indicate that the user's query did not match anything.
     */
    private void indicateFailure() {
        setBackground(MATCH_FAILED_COLOR);
        searchTextField.setBackground(MATCH_FAILED_COLOR);
        searchTextField.setForeground(Color.white);
    }

    /**
     * Displays the search provider name in the text field in grey color.
     */
    private void indicateSearchType() {
        // We are no longer expecting input from the user.
        expectingInput = false;
        ButtonModel bm = typeButtonGroup.getSelection();
        if (bm == null) {
            // No button is selected, most likely because there are no
            // providers. Just have to wait for them to become available.
            return;
        }
        SearchProvider provider = buttonProviderMap.get(bm.getActionCommand());
        String name = provider.getDisplayName();
        Color color = UIManager.getDefaults().getColor("textInactiveText");
        searchTextField.setForeground(color);
        searchTextField.setText(name);
        searchTextField.setToolTipText(provider.getInputDescription());
    }

    public void insertUpdate(DocumentEvent e) {
        // To ignore the "insert" performed by showing the search type,
        // check if expectingInput is true or not. This was used to make
        // the close button enable, but now the close button has moved.
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (!expectingInput) {
                clearFailure(true);
                indicateSearchType();
            }
            fireSearchEvent(SearchEvent.Type.DISMISSED);
            // Send the focus away from the text field.
            typesButton.requestFocusInWindow();
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        // This handles the case where the user performs a quick-copy
        // (on X Windows, select some text, then middle-click in a text field)
        // and the text field is about to receive some text.
        if (!expectingInput) {
            clearFailure(true);
            expectingInput = true;
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if (!expectingInput) {
            clearFailure(true);
        }
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    /**
     * Make the search field ready for input from the user.
     *
     * @param  clear  if true, clear the input text field.
     */
    public void prepareForInput(boolean clear) {
        clearFailure(false);
        searchTextField.requestFocusInWindow();
        if (clear) {
            searchTextField.setText("");
        } else {
            searchTextField.selectAll();
        }
    }

    /**
     * Rmove the given search listener from this search interface.
     *
     * @param  l  search listener.
     */
    public void removeSearchListener(SearchListener l) {
        searchListeners.remove(SearchListener.class, l);
    }

    public void removeUpdate(DocumentEvent e) {
        if (e.getDocument().getLength() == 0) {
            // Remove the error indicator colors.
            clearFailure(false);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        typesButton.setEnabled(enabled);
        searchTextField.setEnabled(enabled);
        if (!expectingInput) {
            clearFailure(true);
            indicateSearchType();
        }
    }

    /**
     * Set the collection of available SearchProvider implementations.
     *
     * @param  providers  collection of search providers, must be non-empty.
     */
    public void setProviders(Collection providers) {
        if (providers.size() == 0) {
            throw new IllegalArgumentException("providers must be non-empty");
        }
        buttonProviderMap.clear();
        typesPopupMenu.removeAll();
        Iterator iter = providers.iterator();
        while (iter.hasNext()) {
            SearchProvider provider = (SearchProvider) iter.next();
            String name = provider.getDisplayName();
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
            item.addActionListener((ActionListener) WeakListeners.create(
                    ActionListener.class, this, item));
            item.setActionCommand(name);
            item.setToolTipText(provider.getShortDescription());
            buttonProviderMap.put(name, provider);
            typeButtonGroup.add(item);
            typesPopupMenu.add(item);
        }
        // Set the first item selected by default.
        AbstractButton button = (AbstractButton) typesPopupMenu.getComponent(0);
        button.setSelected(true);
        if (!expectingInput) {
            clearFailure(true);
            indicateSearchType();
        }

        typesPopupMenu.add(new JSeparator());

        if (fromSelectedMenuItem == null) {
            fromSelectedMenuItem = new JCheckBoxMenuItem();
            fromSelectedMenuItem.addActionListener((ActionListener) WeakListeners.create(
                    ActionListener.class, this, fromSelectedMenuItem));
            fromSelectedMenuItem.setText(NbBundle.getMessage(SearchFieldPanel.class,
                    "LBL_SearchField_StartFromSelected"));
        }
        typesPopupMenu.add(fromSelectedMenuItem);

        if (regexMenuItem == null) {
            regexMenuItem = new JCheckBoxMenuItem();
            regexMenuItem.addActionListener((ActionListener) WeakListeners.create(
                    ActionListener.class, this, regexMenuItem));
            regexMenuItem.setText(NbBundle.getMessage(SearchFieldPanel.class,
                    "LBL_SearchField_RegularExpression"));
        }
        typesPopupMenu.add(regexMenuItem);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        typesPopupMenu = new javax.swing.JPopupMenu();
        typeButtonGroup = new javax.swing.ButtonGroup();
        typesButton = new javax.swing.JButton();
        searchTextField = new javax.swing.JTextField();

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        typesButton.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
        typesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/xam/ui/search/search_types.png")));
        typesButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/search/Bundle").getString("HINT_SearchFieldPanel_Types"));
        typesButton.setBorderPainted(false);
        typesButton.setContentAreaFilled(false);
        typesButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/xam/ui/search/search_types_disabled.png")));
        typesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        searchTextField.setColumns(15);
        searchTextField.setBorder(null);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(typesButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchTextField))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(typesButton)
            .add(searchTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField searchTextField;
    private javax.swing.ButtonGroup typeButtonGroup;
    private javax.swing.JButton typesButton;
    private javax.swing.JPopupMenu typesPopupMenu;
    // End of variables declaration//GEN-END:variables
}

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
package org.openide.explorer.view;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Position.Bias;

/**
 * Quick search infrastructure
 * 
 * @author Martin Entlicher
 */
class QuickSearch {
    
    private static final String ICON_FIND = "org/openide/explorer/view/find.png";
    private static final String ICON_FIND_WITH_MENU = "org/openide/explorer/view/findMenu.png";
    
    private final JComponent component;
    private final Object constraints;
    private boolean enabled = true;
    private final List<QuickSearchListener> listeners = new LinkedList<QuickSearchListener>();
    private SearchTextField searchTextField;
    private KeyAdapter quickSearchKeyAdapter;
    private JPanel searchPanel;
    private JMenu popupMenu;
    
    private QuickSearch(JComponent component, Object constraints) {
        this.component = component;
        this.constraints = constraints;
        setUpSearch();
    }
    
    public static QuickSearch attach(JComponent component, Object constraints) {
        Object qso = component.getClientProperty(QuickSearch.class.getName());
        if (qso instanceof QuickSearch) {
            return (QuickSearch) qso;
        } else {
            QuickSearch qs = new QuickSearch(component, constraints);
            component.putClientProperty(QuickSearch.class.getName(), qs);
            return qs;
        }
    }
    
    public void detach() {
        setEnabled(false);
        component.putClientProperty(QuickSearch.class.getName(), null);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return ;
        }
        this.enabled = enabled;
        if (enabled) {
            component.addKeyListener(quickSearchKeyAdapter);
        } else {
            component.removeKeyListener(quickSearchKeyAdapter);
        }
    }
    
    public void addQuickSearchListener(QuickSearchListener qsl) {
        synchronized (listeners) {
            listeners.add(qsl);
        }
    }
    
    public void removeQuickSearchListener(QuickSearchListener qsl) {
        synchronized (listeners) {
            listeners.remove(qsl);
        }
    }
    
    public void setPopupMenu(JMenu popupMenu) {
        this.popupMenu = popupMenu;
    }
    
    public void processKeyEvent(KeyEvent ke) {
        switch(ke.getID()) {
            case KeyEvent.KEY_PRESSED:
                quickSearchKeyAdapter.keyPressed(ke);
                break;
            case KeyEvent.KEY_RELEASED:
                quickSearchKeyAdapter.keyReleased(ke);
                break;
            case KeyEvent.KEY_TYPED:
                quickSearchKeyAdapter.keyTyped(ke);
                break;
        }
    }
    
    private QuickSearchListener[] getQuickSearchListeners() {
        QuickSearchListener[] qsls;
        synchronized (listeners) {
            qsls = listeners.toArray(new QuickSearchListener[] {});
        }
        return qsls;
    }
    
    private void fireQuickSearchUpdate(String searchText) {
        for (QuickSearchListener qsl : getQuickSearchListeners()) {
            qsl.quickSearchUpdate(searchText);
        }
    }
    
    private void fireShowNextSelection(Bias bias) {
        for (QuickSearchListener qsl : getQuickSearchListeners()) {
            qsl.showNextSelection(bias);
        }
    }
    
    private String findMaxPrefix(String prefix) {
        for (QuickSearchListener qsl : getQuickSearchListeners()) {
            prefix = qsl.findMaxPrefix(prefix);
        }
        return prefix;
    }
    
    private void fireQuickSearchConfirmed() {
        for (QuickSearchListener qsl : getQuickSearchListeners()) {
            qsl.quickSearchConfirmed();
        }
    }
    
    private void fireQuickSearchCanceled() {
        for (QuickSearchListener qsl : getQuickSearchListeners()) {
            qsl.quickSearchCanceled();
        }
    }
    
    private void setUpSearch() {
        searchTextField = new SearchTextField();
        // create new key listeners
        quickSearchKeyAdapter = (
            new KeyAdapter() {
            @Override
                public void keyTyped(KeyEvent e) {
                    int modifiers = e.getModifiers();
                    int keyCode = e.getKeyCode();
                    char c = e.getKeyChar();

                    //#43617 - don't eat + and -
                    //#98634 - and all its duplicates dont't react to space
                    if ((c == '+') || (c == '-') || (c==' ')) return; // NOI18N

                    if (((modifiers > 0) && (modifiers != KeyEvent.SHIFT_MASK)) || e.isActionKey()) {
                        return;
                    }

                    if (Character.isISOControl(c) ||
                            (keyCode == KeyEvent.VK_SHIFT) ||
                            (keyCode == KeyEvent.VK_ESCAPE)) return;

                    final KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
                    searchTextField.setText(String.valueOf(stroke.getKeyChar()));

                    displaySearchField();
                    e.consume();
                }
            }
        );
        if(isEnabled()){
            component.addKeyListener(quickSearchKeyAdapter);
        }
        // Create a the "multi-event" listener for the text field. Instead of
        // adding separate instances of each needed listener, we're using a
        // class which implements them all. This approach is used in order 
        // to avoid the creation of 4 instances which takes some time
        SearchFieldListener searchFieldListener = new SearchFieldListener();
        searchTextField.addKeyListener(searchFieldListener);
        searchTextField.addFocusListener(searchFieldListener);
        searchTextField.getDocument().addDocumentListener(searchFieldListener);
        
    }
    
    private void displaySearchField() {
        if (searchPanel != null || !isEnabled()) {
            return;
        }
        /*
        TreeView previousSearchField = lastSearchField.get();
        if (previousSearchField != null && previousSearchField != this) {
            previousSearchField.removeSearchField();
        }
        */
        //JViewport vp = getViewport();
        //originalScrollMode = vp.getScrollMode();
        //vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        searchTextField.setOriginalFocusOwner();
        searchTextField.setFont(component.getFont());
        searchPanel = new SearchPanel();
        //JLabel lbl = new JLabel(NbBundle.getMessage(TreeView.class, "LBL_QUICKSEARCH")); //NOI18N
        final JLabel lbl;
        if (popupMenu != null) {
            lbl = new JLabel(org.openide.util.ImageUtilities.loadImageIcon(ICON_FIND_WITH_MENU, false));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e != null && !SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    JPopupMenu pm = popupMenu.getPopupMenu();
                    pm.show(lbl, 0, lbl.getHeight() - 1);
                }
            });
        } else {
            lbl = new JLabel(org.openide.util.ImageUtilities.loadImageIcon(ICON_FIND, false));
        }
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.add(lbl);
        searchPanel.add(searchTextField);
        lbl.setLabelFor(searchTextField);
        searchTextField.setColumns(10);
        searchTextField.setMaximumSize(searchTextField.getPreferredSize());
        searchTextField.putClientProperty("JTextField.variant", "search"); //NOI18N
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //JToggleButton matchCaseButton = new JToggleButton("aA");
        //matchCaseButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        //searchPanel.add(matchCaseButton);
        if (component instanceof JScrollPane) {
        //    ((JScrollPane) component).getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        }
        if (constraints == null) {
            component.add(searchPanel);
        } else {
            component.add(searchPanel, constraints);
        }
        component.invalidate();
        component.revalidate();
        component.repaint();
        searchTextField.requestFocus();
    }
    
    private void removeSearchField() {
        if (searchPanel == null) {
            return;
        }
        component.remove(searchPanel);
        searchPanel = null;
        //getViewport().setScrollMode(originalScrollMode);
        component.invalidate();
        component.revalidate();
        component.repaint();
    }
    
    public static String findMaxCommonSubstring(String str1, String str2, boolean ignoreCase) {
        int n1 = str1.length();
        int n2 = str2.length();
        int i = 0;
        if (ignoreCase) {
            for ( ; i < n1 && i < n2; i++) {
                char c1 = Character.toUpperCase(str1.charAt(i));
                char c2 = Character.toUpperCase(str2.charAt(i));
                if (c1 != c2) {
                    break;
                }
            }
        } else {
            for ( ; i < n1 && i < n2; i++) {
                char c1 = str1.charAt(i);
                char c2 = str2.charAt(i);
                if (c1 != c2) {
                    break;
                }
            }
        }
        return str1.substring(0, i);
    }

    public static interface QuickSearchListener {
        
        void quickSearchUpdate(String searchText);
        
        void showNextSelection(Bias bias);
        
        String findMaxPrefix(String prefix);
        
        void quickSearchConfirmed();
        
        void quickSearchCanceled();

    }
    
    private static class SearchPanel extends JPanel {
        
        public SearchPanel() {
            if (ViewUtil.isAquaLaF) {
                setBorder(BorderFactory.createEmptyBorder(9,6,8,2));
            } else {
                setBorder(BorderFactory.createEmptyBorder(2,6,2,2));
            }
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (ViewUtil.isAquaLaF && g instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, UIManager.getColor("NbExplorerView.quicksearch.background.top"),
                        0, getHeight(), UIManager.getColor("NbExplorerView.quicksearch.background.bottom")));//NOI18N
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(UIManager.getColor("NbExplorerView.quicksearch.border")); //NOI18N
                g2d.drawLine(0, 0, getWidth(), 0);
            } else {
                super.paintComponent(g);
            }
        }
    }

    /** searchTextField manages focus because it handles VK_ESCAPE key */
    private class SearchTextField extends JTextField {
        
        private WeakReference<Component> originalFocusOwner = new WeakReference<Component>(null);
        
        public SearchTextField() {
        }
        
        void setOriginalFocusOwner() {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner != null && component.isAncestorOf(focusOwner)) {
                originalFocusOwner = new WeakReference<Component>(focusOwner);
            } else {
                originalFocusOwner = new WeakReference<Component>(component);
            }
        }
        
        void requestOriginalFocusOwner() {
            SwingUtilities.invokeLater(
                new Runnable() {
                    //additional bugfix - do focus change later or removing
                    //the component while it's focused will cause focus to
                    //get transferred to the next component in the
                    //parent focusTraversalPolicy *after* our request
                    //focus completes, so focus goes into a black hole - Tim
                    @Override
                    public void run() {
                        Component fo = originalFocusOwner.get();
                        if (fo != null) {
                            fo.requestFocusInWindow();
                        }
                    }
                }
            );
        }
        
        @Override
        public boolean isManagingFocus() {
            return true;
        }

        @Override
        public void processKeyEvent(KeyEvent ke) {
            //override the default handling so that
            //the parent will never receive the escape key and
            //close a modal dialog
            if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                removeSearchField();
                ke.consume();
                // bugfix #32909, reqest focus when search field is removed
                requestOriginalFocusOwner();
                fireQuickSearchCanceled();
            } else {
                super.processKeyEvent(ke);
            }
        }
    };
    
    private class SearchFieldListener extends KeyAdapter implements DocumentListener, FocusListener {
        
        private boolean ignoreEvents;

        SearchFieldListener() {
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (ignoreEvents) return;
            searchForNode();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (ignoreEvents) return;
            searchForNode();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (ignoreEvents) return;
            searchForNode();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            if (keyCode == KeyEvent.VK_ESCAPE) {
                removeSearchField();
                searchTextField.requestOriginalFocusOwner();
                fireQuickSearchCanceled();
                e.consume();
            } else if (keyCode == KeyEvent.VK_UP || (keyCode == KeyEvent.VK_F3 && e.isShiftDown())) {
                fireShowNextSelection(Bias.Backward);
                // Stop processing the event here. Otherwise it's dispatched
                // to the tree too (which scrolls)
                e.consume();
            } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_F3) {
                fireShowNextSelection(Bias.Forward);
                // Stop processing the event here. Otherwise it's dispatched
                // to the tree too (which scrolls)
                e.consume();
            } else if (keyCode == KeyEvent.VK_TAB) {
                String maxPrefix = findMaxPrefix(searchTextField.getText());
                ignoreEvents = true;
                try {
                    searchTextField.setText(maxPrefix);
                } finally {
                    ignoreEvents = false;
                }

                e.consume();
            } else if (keyCode == KeyEvent.VK_ENTER) {
                removeSearchField();
                fireQuickSearchConfirmed();

                component.requestFocusInWindow();
                e.consume();
            }
        }

        /** Searches for a node in the tree. */
        private void searchForNode() {
            String text = searchTextField.getText();
            fireQuickSearchUpdate(text);
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (e.getSource() == searchTextField) {
                // make sure nothing is selected
                int n = searchTextField.getText().length();
                searchTextField.select(n, n);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (e.isTemporary()) return ;
            Component oppositeComponent = e.getOppositeComponent();
            if (e.getSource() != searchTextField) {
                ((Component) e.getSource()).removeFocusListener(this);
            }
            if (oppositeComponent instanceof JMenuItem || oppositeComponent instanceof JPopupMenu) {
                oppositeComponent.addFocusListener(this);
                return ;
            }
            if (oppositeComponent == searchTextField) {
                return ;
            }
            removeSearchField();
            fireQuickSearchConfirmed();
        }
    }

}

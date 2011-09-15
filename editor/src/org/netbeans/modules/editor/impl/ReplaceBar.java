/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.editor.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.editor.lib2.search.EditorFindSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class ReplaceBar extends JPanel {

    private static ReplaceBar replacebarInstance = null;
    private static final boolean CLOSE_ON_ENTER = Boolean.getBoolean("org.netbeans.modules.editor.search.closeOnEnter"); // NOI18N
    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    private static final int defaultIncremantalSearchComboWidth = 200;
    private static final int maxIncremantalSearchComboWidth = 350;
    private SearchBar searchBar;
    private final FocusAdapter focusAdapterForComponent;
    private final JComboBox replaceComboBox;
    private final JTextField replaceTextField;
    private boolean hadFocusOnReplaceTextField = false;
    private final JButton replaceButton;
    private final JButton replaceAllButton;
    private final JPanel padding;
    private final JLabel replaceLabel;
    private final JCheckBox preserveCaseCheckBox;
    private ActionListener actionListenerForPreserveCase;
    private final JCheckBox backwardsCheckBox;
    private final FocusTraversalPolicy searchBarFocusTraversalPolicy;
    private ArrayList<JComponent> focusList = new ArrayList<JComponent>();
    private final JButton expandButton;
    private final JPopupMenu expandPopup;
    /**
     * contains everything that is in Search bar and is possible to move to
     * expand popup
     */
    private final List<Component> inBar = new ArrayList<Component>();
    /**
     * components moved to popup
     */
    private final LinkedList<Component> inPopup = new LinkedList<Component>();
    /**
     * defines index of all components in Search bar
     */
    private final List<Component> barOrder = new ArrayList<Component>();
    private boolean isPopupGoingToShow = false;
    private boolean isPopupShown = false;

    public static ReplaceBar getInstance(SearchBar searchBar) {
        if (replacebarInstance == null) {
            replacebarInstance = new ReplaceBar(searchBar);
        }
        if (replacebarInstance.getSearchBar() != searchBar) {
            replacebarInstance.setSearchBar(searchBar);
        }
        return replacebarInstance;
    }

    private ReplaceBar(SearchBar searchBar) {
        setSearchBar(searchBar);
        focusAdapterForComponent = createFocusAdapterForComponent();
        addEscapeKeystrokeFocusBackTo(this);
        searchBar.addKeystrokeFindActionTo(this);
        searchBar.addKeystrokeReplaceActionTo(this);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setFocusCycleRoot(true);
        Color bgColor = getBackground();
        setBackground(new Color(Math.max(0, bgColor.getRed() - 20),
                Math.max(0, bgColor.getGreen() - 20),
                Math.max(0, bgColor.getBlue() - 20)));
        setForeground(UIManager.getColor("textText")); //NOI18N

        // padding at the end of the toolbar
        add(Box.createHorizontalStrut(8)); //spacer in the beginnning of the toolbar

        replaceComboBox = createReplaceComboBox();
        replaceLabel = new JLabel();
        Mnemonics.setLocalizedText(replaceLabel, NbBundle.getMessage(ReplaceBar.class, "CTL_Replace")); // NOI18N
        replaceLabel.setLabelFor(replaceComboBox);
        add(replaceLabel);
        replaceTextField = (JTextField) replaceComboBox.getEditor().getEditorComponent();
        replaceTextField.setToolTipText(NbBundle.getMessage(ReplaceBar.class, "TOOLTIP_ReplaceText")); // NOI18N
        // flatten the action map for the text field to allow removal
        ActionMap origActionMap = replaceTextField.getActionMap();
        ActionMap newActionMap = new ActionMap();
        for (Object key : origActionMap.allKeys()) {
            newActionMap.put(key, origActionMap.get(key));
        }
        replaceTextField.setActionMap(newActionMap);
        replaceTextField.getActionMap().remove("toggle-componentOrientation"); // NOI18N
        replaceTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK), "none"); // NOI18N
        replaceTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                hadFocusOnReplaceTextField = true;
                getSearchBar().lostFocusOnTextField();
            }
        });
        addEnterKeystrokeReplaceTo(replaceTextField);
        addShiftEnterReplaceAllTo(replaceTextField);
        add(replaceComboBox);

        final JToolBar.Separator leftSeparator = new JToolBar.Separator();
        leftSeparator.setOrientation(SwingConstants.VERTICAL);
        add(leftSeparator);

        replaceButton = new JButton();
        Mnemonics.setLocalizedText(replaceButton, NbBundle.getMessage(ReplaceBar.class, "CTL_ReplaceNext"));
        replaceButton.setMargin(BUTTON_INSETS);
        replaceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replace();
            }
        });
        add(replaceButton);

        replaceAllButton = new JButton();
        Mnemonics.setLocalizedText(replaceAllButton, NbBundle.getMessage(ReplaceBar.class, "CTL_ReplaceAll"));
        replaceAllButton.setMargin(BUTTON_INSETS);
        replaceAllButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replaceAll();
            }
        });
        add(replaceAllButton);
        
        changeButtonsSizeAsSearchBarButtons();

        final JToolBar.Separator rightSeparator = new JToolBar.Separator();
        rightSeparator.setOrientation(SwingConstants.VERTICAL);
        add(rightSeparator);

        backwardsCheckBox = searchBar.createCheckBox("CTL_BackwardsReplace", EditorFindSupport.FIND_BACKWARD_SEARCH); // NOI18N
        add(backwardsCheckBox);
        preserveCaseCheckBox = searchBar.createCheckBox("CTL_PreserveCase", EditorFindSupport.FIND_PRESERVE_CASE); // NOI18N
        add(preserveCaseCheckBox);

        backwardsCheckBox.setSelected(searchBar.getFindSupportValue(EditorFindSupport.FIND_BACKWARD_SEARCH));
        preserveCaseCheckBox.setSelected(searchBar.getFindSupportValue(EditorFindSupport.FIND_PRESERVE_CASE));
        preserveCaseCheckBox.setEnabled(!searchBar.getRegExp() && !searchBar.getFindSupportValue(EditorFindSupport.FIND_MATCH_CASE));

        expandButton = createExpandButton();
        expandPopup = createExpandPopup(expandButton);
        add(expandButton);
        
        // padding at the end of the toolbar
        padding = new JPanel();
        padding.setOpaque(false);
        add(padding);

        searchBarFocusTraversalPolicy = createSearchBarFocusTraversalPolicy();

        setVisible(false);
        createFocusList();
        makeBarExpandable();
    }

    private SearchBar getSearchBar() {
        return searchBar;
    }

    private void setSearchBar(SearchBar searchBar) {
        this.searchBar = searchBar;
        searchBar.addActualComponentListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                getSearchBar().getActualTextComponent().addFocusListener(focusAdapterForComponent);
            }
        });
        createFocusList();

    }
    
    private void makeBarExpandable() {
        inBar.add(backwardsCheckBox);
        inBar.add(preserveCaseCheckBox);
        barOrder.addAll(Arrays.asList(this.getComponents()));
        remove(expandButton);
    }

    private void showExpandedMenu() {
        if (!inPopup.isEmpty() && !expandPopup.isVisible()) {
            isPopupGoingToShow = true;
            Insets ins = expandPopup.getInsets();
            // compute popup height since JPopupMenu.getHeight does not work
            expandPopup.show(expandButton, 0, -(backwardsCheckBox.getHeight() * inPopup.size() + ins.top + ins.bottom));
        }
    }

    private void hideExpandedMenu() {
        if (expandPopup.isVisible()) {
            expandPopup.setVisible(false);
            replaceTextField.requestFocusInWindow();
        }
    }

    private JButton createExpandButton() throws MissingResourceException {
        JButton expButton = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/editor/resources/find_expand.png", false)); // NOI18N
        expButton.setMnemonic(NbBundle.getMessage(SearchBar.class, "CTL_ExpandButton_Mnemonic").charAt(0)); // NOI18N
        expButton.setMargin(BUTTON_INSETS);
        expButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = !isPopupShown;
                isPopupShown = state;
                if (state) {
                    showExpandedMenu();
                } else {
                    hideExpandedMenu();
                }
            }
        });
        return expButton;
    }

    private JPopupMenu createExpandPopup(final JButton expButton) {
        JPopupMenu expPopup = new JPopupMenu();
        expPopup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // check if it was canceled by click on expand button
                if (expButton.getMousePosition() == null) {
                    expButton.setContentAreaFilled(false);
                    expButton.setBorderPainted(false);
                    isPopupShown = false;
                }
            }
        });
        return expPopup;
    }
    
    @Override
    public Dimension getPreferredSize() {
        computeLayout();
        return super.getPreferredSize();
    }

    private void computeLayout() {
        int parentWidth = this.getParent().getWidth();
        int totalWidth = 0;
        for (Component c : this.getComponents()) {
            if (c != padding) {
                totalWidth += c.getPreferredSize().width;
            }
        }

        boolean change = false;
        if (totalWidth <= parentWidth) { // enough space try to clear expand popup
            while (!inPopup.isEmpty()) {
                Component c = inPopup.getFirst();
                totalWidth += c.getPreferredSize().width;

                if (totalWidth > parentWidth) {
                    break;
                }
                inPopup.removeFirst();
                inBar.add(c);
                expandPopup.remove(c);
                add(c, barOrder.indexOf(c));
                change = true;
            }
        } else { // lack of space
            while (totalWidth > parentWidth && !inBar.isEmpty()) {
                Component c = inBar.remove(inBar.size() - 1);
                inPopup.addFirst(c);
                remove(c);
                expandPopup.add(c, 0);
                totalWidth -= c.getPreferredSize().width;
                change = true;
            }
        }

        if (change) {
            if (inPopup.isEmpty()) {
                remove(expandButton);
            } else if (getComponentIndex(expandButton) < 0) {
                add(expandButton, getComponentIndex(padding));
            }
            this.revalidate();
            expandPopup.invalidate();
            expandPopup.validate();
        }
    }

    private int getComponentIndex(Component c) {
        Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (c == comps[i]) {
                return i;
            }
        }
        return -1;
    }

    private void changeButtonsSizeAsSearchBarButtons() {
        int replaceBarButtonsSize = replaceButton.getPreferredSize().width + replaceAllButton.getPreferredSize().width;
        int searchBarButtonsSize = searchBar.getFindNextButton().getPreferredSize().width + searchBar.getFindPreviousButton().getPreferredSize().width;
        int diffButtonsSize = (searchBarButtonsSize - replaceBarButtonsSize) / 2;
        int diffEven = diffButtonsSize % 2 == 0 ? 0 : 1;
        if (diffButtonsSize > 0 ) {
            replaceButton.setPreferredSize(new Dimension(replaceButton.getPreferredSize().width + diffButtonsSize + diffEven, replaceButton.getPreferredSize().height));
            replaceAllButton.setPreferredSize(new Dimension(replaceAllButton.getPreferredSize().width + diffButtonsSize, replaceAllButton.getPreferredSize().height));
        } else {
            searchBar.getFindNextButton().setPreferredSize(new Dimension(searchBar.getFindNextButton().getPreferredSize().width - diffButtonsSize + diffEven, searchBar.getFindNextButton().getPreferredSize().height));
            searchBar.getFindPreviousButton().setPreferredSize(new Dimension(searchBar.getFindPreviousButton().getPreferredSize().width - diffButtonsSize, searchBar.getFindPreviousButton().getPreferredSize().height));
        }
    }
    
    private void addEnterKeystrokeReplaceTo(JTextField replaceTextField) {
        replaceTextField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                "replace-next"); // NOI18N
        replaceTextField.getActionMap().put("replace-next", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replace();
                if (CLOSE_ON_ENTER) {
                    looseFocus();
                }
            }
        });
    }
    
    private void addShiftEnterReplaceAllTo(JTextField textField) {
        textField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK, true),
                "replace-all"); // NOI18N
        textField.getActionMap().put("replace-all", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                replaceAll();
                if (CLOSE_ON_ENTER) {
                    looseFocus();
                }
            }
        });
    }

    private void createFocusList() {
        focusList.clear();
        focusList.add(searchBar.getIncSearchTextField());
        focusList.add(replaceTextField);
        focusList.add(replaceButton);
        focusList.add(replaceAllButton);
        focusList.add(searchBar.getFindPreviousButton());
        focusList.add(searchBar.getFindNextButton());
    }

    private ArrayList<JComponent> getFocusList() {
        return focusList;
    }

    private FocusTraversalPolicy createSearchBarFocusTraversalPolicy() {
        return new FocusTraversalPolicy() {

            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                int indexOf = getFocusList().indexOf(aComponent);
                if (indexOf == -1) {
                    return null;
                } else if (indexOf == getFocusList().size() - 1) {
                    return getFocusList().get(0).isEnabled() ? getFocusList().get(0) : getComponentAfter(aContainer, getFocusList().get(0));
                } else {
                    return getFocusList().get(indexOf + 1).isEnabled() ? getFocusList().get(indexOf + 1) : getComponentAfter(aContainer, getFocusList().get(indexOf + 1));
                }
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                int indexOf = getFocusList().indexOf(aComponent);
                if (indexOf == -1) {
                    return null;
                } else if (indexOf == 0) {
                    return getFocusList().get(getFocusList().size() - 1).isEnabled() ? getFocusList().get(getFocusList().size() - 1) : getComponentBefore(aContainer, getFocusList().get(getFocusList().size() - 1));
                } else {
                    return getFocusList().get(indexOf - 1).isEnabled() ? getFocusList().get(indexOf - 1) : getComponentBefore(aContainer, getFocusList().get(indexOf - 1));
                }
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return getFocusList().get(0);
            }

            @Override
            public Component getLastComponent(Container aContainer) {
                return getFocusList().get(getFocusList().size() - 1);
            }

            @Override
            public Component getDefaultComponent(Container aContainer) {
                return getFocusList().get(0);
            }
        };
    }

    private FocusAdapter createFocusAdapterForComponent() {
        return new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                hadFocusOnReplaceTextField = false;
            }
        };
    }

    private static JComboBox createReplaceComboBox() {
        JComboBox incSearchComboBox = new JComboBox() {

            public @Override
            Dimension getMinimumSize() {
                return getPreferredSize();
            }

            public @Override
            Dimension getMaximumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                int width;
                int editsize = this.getEditor().getEditorComponent().getPreferredSize().width + 10;
                if (editsize > defaultIncremantalSearchComboWidth && editsize < maxIncremantalSearchComboWidth) {
                    width = editsize;
                } else if (editsize >= maxIncremantalSearchComboWidth) {
                    width = maxIncremantalSearchComboWidth;
                } else {
                    width = defaultIncremantalSearchComboWidth;
                }
                return new Dimension(width,
                        super.getPreferredSize().height);
            }
        };

        incSearchComboBox.setEditable(true);
        return incSearchComboBox;
    }

    private void addEscapeKeystrokeFocusBackTo(JPanel jpanel) {
        jpanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                "loose-focus"); // NOI18N
        jpanel.getActionMap().put("loose-focus", new AbstractAction() {// NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                looseFocus();
            }
        });
    }

    void updateReplaceComboBoxHistory(String incrementalSearchText) {
        // Add the text to the top of the list
        for (int i = replaceComboBox.getItemCount() - 1; i >= 0; i--) {
            String item = (String) replaceComboBox.getItemAt(i);
            if (item.equals(incrementalSearchText)) {
                replaceComboBox.removeItemAt(i);
            }
        }
        ((MutableComboBoxModel) replaceComboBox.getModel()).insertElementAt(incrementalSearchText, 0);
        replaceComboBox.setSelectedIndex(0);
    }
    
    private ActionListener getActionListenerForPreserveCase() {
        if (actionListenerForPreserveCase == null) {
            return new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    preserveCaseCheckBox.setEnabled(!searchBar.getRegexpCheckBox().isSelected() && !searchBar.getMatchCaseCheckBox().isSelected());

                }
            };
        } else {
            return actionListenerForPreserveCase;
        }
    }

    private ActionListener closeButtonListener;
    private ActionListener getCloseButtonListener() {
        if (closeButtonListener == null)
            closeButtonListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                looseFocus();
            }
        };
        return closeButtonListener;
    }
    
    private void unchangeSearchBarToBeOnlySearchBar() throws MissingResourceException {
        searchBar.getCloseButton().removeActionListener(getCloseButtonListener());
        Mnemonics.setLocalizedText(searchBar.getFindLabel(), NbBundle.getMessage(SearchBar.class, "CTL_Find")); // NOI18N
        Dimension oldDimensionForFindLabel = searchBar.getFindLabel().getUI().getMinimumSize(searchBar.getFindLabel());
        searchBar.getFindLabel().setMinimumSize(oldDimensionForFindLabel);
        searchBar.getFindLabel().setPreferredSize(oldDimensionForFindLabel);
        searchBar.addEscapeKeystrokeFocusBackTo(searchBar);
        if (searchBar.getActualTextComponent() != null) {
            searchBar.getActualTextComponent().removeFocusListener(focusAdapterForComponent);
        }
        searchBar.getRegexpCheckBox().removeActionListener(getActionListenerForPreserveCase());
        searchBar.getMatchCaseCheckBox().removeActionListener(getActionListenerForPreserveCase());
        searchBar.setFocusTraversalPolicy(null);
        searchBar.looseFocus();
    }

    private void changeSearchBarToBePartOfReplaceBar() throws MissingResourceException {
        searchBar.getCloseButton().addActionListener(getCloseButtonListener());
        Mnemonics.setLocalizedText(searchBar.getFindLabel(), NbBundle.getMessage(SearchBar.class, "CTL_Replace_Find")); // NOI18N
        Dimension newDimensionForFindLabel = new Dimension(replaceLabel.getPreferredSize().width, searchBar.getFindLabel().getPreferredSize().height);
        searchBar.getFindLabel().setMinimumSize(newDimensionForFindLabel);
        searchBar.getFindLabel().setPreferredSize(newDimensionForFindLabel);
        this.addEscapeKeystrokeFocusBackTo(searchBar);
        if (searchBar.getActualTextComponent() != null) {
            searchBar.getActualTextComponent().addFocusListener(focusAdapterForComponent);
        }
        searchBar.getRegexpCheckBox().addActionListener(getActionListenerForPreserveCase());
        searchBar.getMatchCaseCheckBox().addActionListener(getActionListenerForPreserveCase());
        searchBar.setFocusTraversalPolicy(searchBarFocusTraversalPolicy);
        setFocusTraversalPolicy(searchBarFocusTraversalPolicy);
        searchBar.computeLayout();
    }

   
    void looseFocus() {
        hadFocusOnReplaceTextField = false;
        if (!isVisible()) {
            return;
        }
        if (isPopupGoingToShow) {
            isPopupGoingToShow = false;
            return;
        }

        unchangeSearchBarToBeOnlySearchBar();
                                setVisible(false);
    }

    void gainFocus() {
        if (!isVisible()) {
            changeSearchBarToBePartOfReplaceBar();
            setVisible(true);
        }
        searchBar.gainFocus();
        
        if (searchBar.getIncSearchTextField().getText().isEmpty()) {
            searchBar.getIncSearchTextField().requestFocusInWindow();
        } else {
            replaceTextField.requestFocusInWindow();
        }
    }

    boolean hadFocusOnTextField() {
        return hadFocusOnReplaceTextField;
    }

    void lostFocusOnTextField() {
        hadFocusOnReplaceTextField = false;
    }

    private void replace() {
        replace(false);
    }

    private void replaceAll() {
        replace(true);
    }

    private void replace(boolean replaceAll) {
        searchBar.updateIncSearchComboBoxHistory(searchBar.getIncSearchTextField().getText());
        this.updateReplaceComboBoxHistory(replaceTextField.getText());

        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        Map<String, Object> findProps = new HashMap<String, Object>();
        findProps.putAll(searchBar.getFindProps());
        findProps.put(EditorFindSupport.FIND_REPLACE_WITH, replaceTextField.getText());
        findProps.put(EditorFindSupport.FIND_BACKWARD_SEARCH, backwardsCheckBox.isSelected());
        findProps.put(EditorFindSupport.FIND_PRESERVE_CASE, preserveCaseCheckBox.isSelected() && preserveCaseCheckBox.isEnabled());
        findSupport.putFindProperties(findProps);
        try {
            if (replaceAll) {
                findSupport.replaceAll(findProps);
            } else {
                findSupport.replace(findProps, false);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.editor.impl;

import java.awt.event.FocusEvent;
import org.netbeans.editor.*;
import javax.swing.Action;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;


/**
 * This is an implementation of a Firefox(TM) style Incremental Search Side Bar.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public final class SearchBar extends JToolBar {

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    
    /** Shared mouse listener used for setting the border painting property
     * of the toolbar buttons and for invoking the popup menu.
     */
    private static final MouseListener sharedMouseListener
        = new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Object src = evt.getSource();
                
                if (src instanceof JButton) { // ignore JMenuItem and JToggleButton
                    AbstractButton button = (AbstractButton)evt.getSource();
                    if (button.isEnabled()) {
                        button.setContentAreaFilled(true);
                        button.setBorderPainted(true);
                    }
                }
            }
            
            public void mouseExited(MouseEvent evt) {
                Object src = evt.getSource();
                if (src instanceof JButton) { // ignore JMenuItem and JToggleButton
                    AbstractButton button = (AbstractButton)evt.getSource();
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                }
            }
            
            protected void showPopup(MouseEvent evt) {
            }
        };

    private JTextComponent component;
    private JButton closeButton;
    private JLabel findLabel;
    private JTextField incrementalSearchTextField;
    private JButton findNextButton;
    private JButton findPreviousButton;
    private JCheckBox matchCaseCheckBox;
    private Map findProps;
        
    public SearchBar(JTextComponent component) {
        this.component = component;

        setFloatable(false);
        
        //setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        addMouseListener(sharedMouseListener);

        Keymap keymap = component.getKeymap();

        if (keymap instanceof MultiKeymap) {
            MultiKeymap multiKeymap = (MultiKeymap) keymap;

            Action[] actions = component.getActions();
            for(Action action:actions) {
                // Discover the keyStrokes for incremental-search-forward
                if (action.getValue(Action.NAME).equals(IncrementalSearchForwardAction.NAME)) {
                    Action incrementalSearchForwardAction = action;
                    KeyStroke[] keyStrokes = multiKeymap.getKeyStrokesForAction(incrementalSearchForwardAction);
                    if (keyStrokes != null) {
                        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                            .put(keyStrokes[0], IncrementalSearchForwardAction.NAME);
                        getActionMap().put(IncrementalSearchForwardAction.NAME,
                            new AbstractAction() {
                                public void actionPerformed(ActionEvent e) {
                                    findNext();
                                }
                            });
                    }
                // Discover the keyStrokes for incremental-search-backward
                } else if (action.getValue(Action.NAME).equals(IncrementalSearchBackwardAction.NAME)) {
                    Action incrementalSearchBackwardAction = action;
                    KeyStroke[] keyStrokes = multiKeymap.getKeyStrokesForAction(incrementalSearchBackwardAction);
                    if (keyStrokes != null) {
                        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                            .put(keyStrokes[0], IncrementalSearchBackwardAction.NAME);
                        getActionMap().put(IncrementalSearchBackwardAction.NAME,
                            new AbstractAction() {
                                public void actionPerformed(ActionEvent e) {
                                    findPrevious();
                                }
                            });
                    }
                }
            }
        }

        // ESCAPE to put focus back in the editor
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
            "loose-focus"); // NOI18N
        getActionMap().put("loose-focus", // NOI18N
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    looseFocus();
                }
            });

        closeButton = new JButton(new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/editor/resources/find_close.png"))); // NOI18N
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                looseFocus();
            }
        });
        closeButton.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_CloseIncrementalSearchSidebar")); // NOI18N
        processButton(closeButton);
        
        findLabel = new JLabel(NbBundle.getMessage(SearchBar.class, "CTL_Find")); // NOI18N

        // configure incremental search text field
        incrementalSearchTextField = new JTextField(10) {
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        incrementalSearchTextField.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_IncrementalSearchText")); // NOI18N

        // listen on text change
        incrementalSearchTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }

            public void insertUpdate(DocumentEvent e) {
                // text changed - attempt incremental search
                incrementalSearch();
            }

            public void removeUpdate(DocumentEvent e) {
                // text changed - attempt incremental search
                incrementalSearch();
            }
        });

        incrementalSearchTextField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                looseFocus();
            }
        });
        
        // ENTER to find next
        incrementalSearchTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findNext();
            }
        });

        // Shift+ENTER to find previous
        incrementalSearchTextField.getInputMap()
                                  .put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK, true),
            "incremental-find-previous"); // NOI18N
        incrementalSearchTextField.getActionMap().put("incremental-find-previous", // NOI18N
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    findPrevious();
                }});

        // configure find next button
        
        // configure find next button
        findNextButton = new JButton(
            NbBundle.getMessage(SearchBar.class, "CTL_FindNext"), // NOI18N
            new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/editor/resources/find_next.png"))); // NOI18N
        findNextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    findNext();
                }});
        processButton(findNextButton);

        // configure find previous button
        
        // configure find previous button
        findPreviousButton = new JButton(
            NbBundle.getMessage(SearchBar.class, "CTL_FindPrevious"), // NOI18N
            new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/editor/resources/find_previous.png"))); // NOI18N
        findPreviousButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    findPrevious();
                }
            });
        processButton(findPreviousButton);

        // configure match case check box
        matchCaseCheckBox = new JCheckBox(NbBundle.getMessage(SearchBar.class, "CTL_MatchCase"), false); // NOI18N
        matchCaseCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Put focus back in the incremental search textField
                incrementalSearchTextField.requestFocusInWindow();
            }
        });
        processButton(matchCaseCheckBox);

        // configure find properties
        findProps = new HashMap();
        findProps.put(SettingsNames.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
        findProps.put(SettingsNames.FIND_WHOLE_WORDS, Boolean.FALSE);
        findProps.put(SettingsNames.FIND_WRAP_SEARCH, Boolean.TRUE);

        add(closeButton);
        addSeparator();
        add(findLabel);
        add(incrementalSearchTextField);
        addSeparator();
        add(findPreviousButton);
        add(findNextButton);
        add(matchCaseCheckBox);

        // padding at the end of the toolbar
        addSeparator(getMaximumSize());
        
        // initially not visible
        setVisible(false);
    }

    public String getUIClassID() {
        //For GTK and Aqua look and feels, we provide a custom toolbar UI -
        //but we cannot override this globally or it will cause problems for
        //the form editor & other things
        if (UIManager.get("Nb.Toolbar.ui") != null) { //NOI18N
            return "Nb.Toolbar.ui"; //NOI18N
        } else {
            return super.getUIClassID();
        }
    }
    
    public String getName() {
        //Required for Aqua L&F toolbar UI
        return "editorSearchBar"; // NOI18N
    }
    
    private void gainFocus() {
        setVisible(true);
        incrementalSearchTextField.requestFocusInWindow();

        if (incrementalSearchTextField.getText().length() > 0) {
            // preselect the text in incremental search text field
            incrementalSearchTextField.selectAll();
        }
    }

    private void looseFocus() {
        setVisible(false);

        if (component.isEnabled()) {
            component.requestFocusInWindow();
        }
    }

    private void incrementalSearch() {
        String incrementalSearchText = incrementalSearchTextField.getText();

        // configure find properties
        FindSupport findSupport = FindSupport.getFindSupport();

        findProps.put(SettingsNames.FIND_WHAT, incrementalSearchText);
        findProps.put(SettingsNames.FIND_MATCH_CASE,
            matchCaseCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        findProps.put(SettingsNames.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        findProps.put(SettingsNames.FIND_INC_SEARCH, Boolean.TRUE);

        // search starting at current caret position
        int caretPosition = component.getCaretPosition();

        if (findSupport.incSearch(findProps, caretPosition)) {
            // text found - reset incremental search text field's foreground
            incrementalSearchTextField.setForeground(null);
        } else {
            // text not found - indicate error in incremental search
            // text field with red foreground
            incrementalSearchTextField.setForeground(Color.red);
        }
    }

    private void findNext() {
        find(true);
    }

    private void findPrevious() {
        find(false);
    }

    private void find(boolean next) {
        String incrementalSearchText = incrementalSearchTextField.getText();

        // configure find properties
        FindSupport findSupport = FindSupport.getFindSupport();

        findProps.put(SettingsNames.FIND_WHAT, incrementalSearchText);
        findProps.put(SettingsNames.FIND_MATCH_CASE,
            matchCaseCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        findProps.put(SettingsNames.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        findProps.put(SettingsNames.FIND_INC_SEARCH, Boolean.FALSE);

        if (findSupport.find(findProps, !next)) {
            // text found - reset incremental search text field's foreground
            incrementalSearchTextField.setForeground(null);
        } else {
            // text not found - indicate error in incremental search text field with red foreground
            incrementalSearchTextField.setForeground(Color.red);
        }
    }

    private void processButton(AbstractButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setMargin(BUTTON_INSETS);
        if (button instanceof JButton) {
            button.addMouseListener(sharedMouseListener);
        }
        button.setFocusable(false);
    }
    
    /**
     * Factory for creating the incremental search sidebar
     */
    public static final class Factory implements SideBarFactory {
        public JComponent createSideBar(JTextComponent target) {
            return new SearchBar(target);
        }
    }

    public static class IncrementalSearchForwardAction extends BaseAction {
        
        public static final String NAME = "incremental-search-forward"; // NOI18N
    
        static final long serialVersionUID = -1; // TODO
        
        public IncrementalSearchForwardAction() {
            super(NAME, CLEAR_STATUS_TEXT);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JComponent comp = Utilities.getEditorUI(target).getExtComponent();
                if (comp != null) {
                    SearchBar issb = findComponent(comp,SearchBar.class, 2);
                    issb.gainFocus();
                }
            }
        }
    }
    
    public static class IncrementalSearchBackwardAction extends BaseAction {
        
        public static final String NAME = "incremental-search-backward"; // NOI18N

        static final long serialVersionUID = -1; // TODO
        
        public IncrementalSearchBackwardAction() {
            super(NAME, CLEAR_STATUS_TEXT);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JComponent comp = Utilities.getEditorUI(target).getExtComponent();
                if (comp != null) {
                    SearchBar issb = findComponent(comp,SearchBar.class, 2);
                    issb.gainFocus();
                }
            }
        }
    }

    private static <T> T findComponent(Container container, Class<T> componentClass, int depth) {
        if (depth > 0) {
            for(Component c : container.getComponents()) {
                if (componentClass.isAssignableFrom(c.getClass())) {
                    @SuppressWarnings("unchecked")
                    T target = (T) c;
                    return target;
                } else if (c instanceof Container) {
                    T target = findComponent((Container) c, componentClass, depth - 1);
                    if (target != null) {
                        return target;
                    }
                }
            }
        }
        return null;
    }
}

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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.*;
import javax.swing.Action;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.Mnemonics;
import org.openide.util.Utilities;


/**
 * This is an implementation of a Firefox(TM) style Incremental Search Side Bar.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public final class SearchBar extends JToolBar {
    
    private static final Logger LOG = Logger.getLogger(SearchBar.class.getName());

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    private static final Color NOT_FOUND = new Color(220, 90, 90, 255);
    private static final Color INVALID_REGEXP = Color.red;
    
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
    private JComboBox incrementalSearchComboBox;
    private JTextField incrementalSearchTextField;
    private JButton findNextButton;
    private JButton findPreviousButton;
    private JCheckBox matchCaseCheckBox;
    private JCheckBox wholeWordsCheckBox;
    private JCheckBox regexpCheckBox;
    private JCheckBox highlightCheckBox;
    private Map<Object, Object> findProps;
        
    @SuppressWarnings("unchecked")
    public SearchBar(JTextComponent component) {
        this.component = component;

        setFloatable(false);
        Color bgColor = getBackground();
        bgColor = new Color( Math.max( 0, bgColor.getRed() - 20 ),
                             Math.max( 0, bgColor.getGreen() - 20 ),
                             Math.max( 0, bgColor.getBlue() - 20 ) );        
        setBackground(bgColor);
        
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
                        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                        for(KeyStroke ks : keyStrokes) {
                            LOG.fine("found IncrementalSearchForwardAction, " + ks); //NOI18N
                            inputMap.put(ks, IncrementalSearchForwardAction.NAME);
                        }
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
                        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                        for(KeyStroke ks : keyStrokes) {
                            LOG.fine("found IncrementalSearchBackwardAction, " + ks); //NOI18N
                            inputMap.put(ks, IncrementalSearchBackwardAction.NAME);
                        }
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

        closeButton = new JButton(new ImageIcon(Utilities.loadImage("org/netbeans/modules/editor/resources/find_close.png"))); // NOI18N
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                looseFocus();
            }
        });
        closeButton.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_CloseIncrementalSearchSidebar")); // NOI18N
        processButton(closeButton);
        
        findLabel = new JLabel(); 
        Mnemonics.setLocalizedText( findLabel, NbBundle.getMessage(SearchBar.class, "CTL_Find")); // NOI18N
        
        // configure incremental search text field
        incrementalSearchComboBox = new JComboBox()
        {
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        incrementalSearchComboBox.setEditable(true);
        incrementalSearchTextField = (JTextField) incrementalSearchComboBox.getEditor().getEditorComponent();
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
        findNextButton = new JButton(
            new ImageIcon(Utilities.loadImage("org/netbeans/modules/editor/resources/find_next.png"))); // NOI18N
        Mnemonics.setLocalizedText( findNextButton, NbBundle.getMessage(SearchBar.class, "CTL_FindNext")); // NOI18N
        findNextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    findNext();
                }});
        processButton(findNextButton);

        // configure find previous button
        findPreviousButton = new JButton(
            new ImageIcon(Utilities.loadImage("org/netbeans/modules/editor/resources/find_previous.png"))); // NOI18N
        Mnemonics.setLocalizedText(findPreviousButton, NbBundle.getMessage(SearchBar.class, "CTL_FindPrevious")); // NOI18N
        findPreviousButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    findPrevious();
                }
            });
        processButton(findPreviousButton);

        // configure match case check box
        matchCaseCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(matchCaseCheckBox, NbBundle.getMessage(SearchBar.class, "CTL_MatchCase")); // NOI18N
        matchCaseCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Put focus back in the incremental search textField
                incrementalSearch();
                incrementalSearchTextField.requestFocusInWindow();
            }
        });
        processButton(matchCaseCheckBox);

        wholeWordsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(wholeWordsCheckBox, NbBundle.getMessage(SearchBar.class, "CTL_WholeWords")); // NOI18N
        wholeWordsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Put focus back in the incremental search textField
                incrementalSearch();
                incrementalSearchTextField.requestFocusInWindow();
            }
        });
        processButton(wholeWordsCheckBox);
        
        regexpCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(regexpCheckBox, NbBundle.getMessage(SearchBar.class, "CTL_Regexp")); // NOI18N
        regexpCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Switch other checkbozes on/off
                matchCaseCheckBox.setEnabled(!regexpCheckBox.isSelected());
                wholeWordsCheckBox.setEnabled(!regexpCheckBox.isSelected());
                // Put focus back in the incremental search textField
                incrementalSearch();
                incrementalSearchTextField.requestFocusInWindow();
            }
        });
        processButton(regexpCheckBox);
        
        highlightCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(highlightCheckBox, NbBundle.getMessage(SearchBar.class, "CTL_Highlight")); // NOI18N
        highlightCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Put focus back in the incremental search textField
                incrementalSearch();
                incrementalSearchTextField.requestFocusInWindow();
            }
        });
        highlightCheckBox.setSelected(true);
        processButton(highlightCheckBox);
        

        // configure find properties
        findProps = new HashMap<Object, Object>();
        findProps.put(SettingsNames.FIND_HIGHLIGHT_SEARCH, true);
        findProps.put(SettingsNames.FIND_WHOLE_WORDS, false);
        findProps.put(SettingsNames.FIND_WRAP_SEARCH, true);
        findProps.put(SettingsNames.FIND_BLOCK_SEARCH, false);
        findProps.put(SettingsNames.FIND_BLOCK_SEARCH_START, null);
        findProps.put(SettingsNames.FIND_BLOCK_SEARCH_END, null);
        // XXX take from preferences
        findProps.put(SettingsNames.FIND_MATCH_CASE, false);
        findProps.put(SettingsNames.FIND_REG_EXP, false);
        findProps.put(SettingsNames.FIND_HIGHLIGHT_SEARCH, true);
        
        // padding at the end of the toolbar
        JPanel spacer = new JPanel();
        spacer.setSize(4, 4);
        spacer.setMaximumSize(new Dimension(4,4));
        spacer.setOpaque(false);
        add(spacer);
        
        add(findLabel);
        add(incrementalSearchComboBox);
        addSeparator();
        add(findPreviousButton);
        add(findNextButton);
        addSeparator();
        add(matchCaseCheckBox);
        add(wholeWordsCheckBox);
        add(regexpCheckBox);
        add(highlightCheckBox);
        // padding at the end of the toolbar
        JPanel padding = new JPanel();
        padding.setOpaque(false);
        add(padding);
        add(closeButton);
        
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
        initBlockSearch();
        incrementalSearchTextField.requestFocusInWindow();

        if (incrementalSearchTextField.getText().length() > 0) {
            // preselect the text in incremental search text field
            incrementalSearchTextField.selectAll();
            findPreviousButton.setEnabled(true);
            findNextButton.setEnabled(true);
        }
        else {
            findPreviousButton.setEnabled(false);
            findNextButton.setEnabled(false);
        }
    }

    private void looseFocus() {
        FindSupport.getFindSupport().setBlockSearchHighlight(0, 0);
        FindSupport.getFindSupport().incSearchReset();
        setVisible(false);

        if (component.isEnabled()) {
            component.requestFocusInWindow();
        }
    }

    private void incrementalSearch() {
        String incrementalSearchText = incrementalSearchTextField.getText();
        boolean empty = incrementalSearchText.length() <= 0;
        
        // Enable/disable the pre/next buttons
        findPreviousButton.setEnabled(!empty);
        findNextButton.setEnabled(!empty);
        
        // configure find properties
        FindSupport findSupport = FindSupport.getFindSupport();

        findProps.put(SettingsNames.FIND_WHAT, incrementalSearchText);
        findProps.put(SettingsNames.FIND_MATCH_CASE, matchCaseCheckBox.isSelected());
        findProps.put(SettingsNames.FIND_WHOLE_WORDS, wholeWordsCheckBox.isSelected());
        findProps.put(SettingsNames.FIND_REG_EXP, regexpCheckBox.isSelected());
        findProps.put(SettingsNames.FIND_HIGHLIGHT_SEARCH, !empty && highlightCheckBox.isSelected());
        
        findProps.put(SettingsNames.FIND_BACKWARD_SEARCH, false);        
        findProps.put(SettingsNames.FIND_INC_SEARCH, true);
        
        findSupport.putFindProperties(findProps);
        
        // search starting at current caret position
        int caretPosition = component.getCaretPosition();

        if (regexpCheckBox.isSelected()) {
            Pattern pattern;
            try {
                pattern = Pattern.compile(incrementalSearchText);
            } catch (PatternSyntaxException e) {
                pattern = null;
            }
            if (pattern != null) {
                // valid regexp
                incrementalSearchTextField.setBackground(null);
                incrementalSearchTextField.setForeground(Color.BLACK);
            } else {
                // invalid regexp
                incrementalSearchTextField.setBackground(null);
                incrementalSearchTextField.setForeground(INVALID_REGEXP);
            }
        } else {
            if (findSupport.incSearch(findProps, caretPosition) || empty) {
                // text found - reset incremental search text field's foreground
                incrementalSearchTextField.setBackground(null);
                incrementalSearchTextField.setForeground(Color.BLACK);
            } else {
                // text not found - indicate error in incremental search
                // text field with red foreground
                incrementalSearchTextField.setBackground(NOT_FOUND);
                incrementalSearchTextField.setForeground(Color.WHITE);
                Toolkit.getDefaultToolkit().beep();
            }
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
        boolean empty = incrementalSearchText.length() <= 0;
        
        // Add the text to the top of the list
        for(int i = incrementalSearchComboBox.getItemCount() - 1; i >= 0; i--) {
            String item = (String) incrementalSearchComboBox.getItemAt(i);
            if (item.equals(incrementalSearchText)) {
                incrementalSearchComboBox.removeItemAt(i);
            }
        }
        ((MutableComboBoxModel) incrementalSearchComboBox.getModel()).insertElementAt(incrementalSearchText, 0);
        incrementalSearchComboBox.setSelectedIndex(0);
        
        // configure find properties
        FindSupport findSupport = FindSupport.getFindSupport();

        findProps.put(SettingsNames.FIND_WHAT, incrementalSearchText);
        findProps.put(SettingsNames.FIND_MATCH_CASE, matchCaseCheckBox.isSelected());
        findProps.put(SettingsNames.FIND_WHOLE_WORDS, wholeWordsCheckBox.isSelected());
        findProps.put(SettingsNames.FIND_REG_EXP, regexpCheckBox.isSelected());
        findProps.put(SettingsNames.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        findProps.put(SettingsNames.FIND_INC_SEARCH, Boolean.TRUE);
        findProps.put(SettingsNames.FIND_HIGHLIGHT_SEARCH, !empty && highlightCheckBox.isSelected());

        findSupport.putFindProperties(findProps);
        
        if (findSupport.find(findProps, !next) || empty) {
            // text found - reset incremental search text field's foreground
            incrementalSearchTextField.setBackground(null);
            incrementalSearchTextField.setForeground(Color.BLACK);
        } else {
            // text not found - indicate error in incremental search text field with red foreground
            incrementalSearchTextField.setBackground(NOT_FOUND);
            incrementalSearchTextField.setForeground(Color.WHITE);
            Toolkit.getDefaultToolkit().beep();
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
    
    @SuppressWarnings("unchecked")
    private void initBlockSearch() {
        JTextComponent c = EditorRegistry.lastFocusedComponent();
        String selText = null;
        int startSelection = 0;
        int endSelection = 0;
        boolean blockSearchVisible = false;

        if (c != null) {
            startSelection = c.getSelectionStart();
            endSelection = c.getSelectionEnd();

            Document doc = c.getDocument();
            if (doc instanceof BaseDocument){
                BaseDocument bdoc = (BaseDocument) doc;
                try{
                    int startLine = org.netbeans.editor.Utilities.getLineOffset(bdoc, startSelection);
                    int endLine = org.netbeans.editor.Utilities.getLineOffset(bdoc, endSelection);
                    if (endLine > startLine) {
                        blockSearchVisible = true;
                    }
                } catch (BadLocationException ble){
                }
            }

            // caretPosition = bwdSearch.isSelected() ? c.getSelectionEnd() : c.getSelectionStart();

            if (!blockSearchVisible){
                selText = c.getSelectedText();
                if (selText != null && selText.length() > 0) {
                    int n = selText.indexOf( '\n' );
                    if (n >= 0 ) selText = selText.substring(0, n);
                    incrementalSearchTextField.setText(selText);
                    // findWhat.getEditor().setItem(selText);
                    // changeFindWhat(true);
                } else {
                    String findWhat = (String) FindSupport.getFindSupport().getFindProperty(SettingsNames.FIND_WHAT);
                    if (findWhat != null && findWhat.length() > 0) {
                        incrementalSearchTextField.setText(findWhat);
                    }
                }
            }

            int blockSearchStartPos = blockSearchVisible ? startSelection : 0;
            int blockSearchEndPos = blockSearchVisible ? endSelection : 0;

            try{                
                findProps.put(SettingsNames.FIND_BLOCK_SEARCH, blockSearchVisible);
                findProps.put(SettingsNames.FIND_BLOCK_SEARCH_START, blockSearchStartPos);
                int be = getBlockEndOffset();
                if (be < 0){
                    findProps.put(SettingsNames.FIND_BLOCK_SEARCH_END, doc.createPosition(blockSearchEndPos));
                }else{
                    blockSearchEndPos = be;
                }
                FindSupport.getFindSupport().setBlockSearchHighlight(blockSearchStartPos, blockSearchEndPos);
            } catch(BadLocationException ble){
                findProps.put(SettingsNames.FIND_BLOCK_SEARCH, Boolean.FALSE);
                findProps.put(SettingsNames.FIND_BLOCK_SEARCH_START, null);
            }
            
            FindSupport.getFindSupport().putFindProperties(findProps);
        }
    }
    
    private int getBlockEndOffset(){
        Position pos = (Position) FindSupport.getFindSupport().getFindProperties().get(SettingsNames.FIND_BLOCK_SEARCH_END);
        return (pos != null) ? pos.getOffset() : -1;
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
    
        static final long serialVersionUID = -1;
        
        public IncrementalSearchForwardAction() {
            super(NAME, CLEAR_STATUS_TEXT);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IncrementalSearchForwardAction.class, NAME));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui != null) {
                    JComponent comp = eui.getExtComponent();
                    if (comp != null) {
                        SearchBar issb = findComponent(comp,SearchBar.class, 5);
                        if (issb != null) {
                            issb.gainFocus();
                        }
                    }
                }
            }
        }
    }
    
    public static class IncrementalSearchBackwardAction extends BaseAction {
        
        public static final String NAME = "incremental-search-backward"; // NOI18N

        static final long serialVersionUID = -1;
        
        public IncrementalSearchBackwardAction() {
            super(NAME, CLEAR_STATUS_TEXT);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IncrementalSearchBackwardAction.class, NAME));
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui != null) {
                    JComponent comp = eui.getExtComponent();
                    if (comp != null) {
                        SearchBar issb = findComponent(comp,SearchBar.class, 5);
                        if (issb != null) {
                            issb.gainFocus();
                        }
                    }
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

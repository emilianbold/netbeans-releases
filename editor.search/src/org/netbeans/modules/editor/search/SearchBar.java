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
package org.netbeans.modules.editor.search;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.*;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeymap;
import org.netbeans.modules.editor.lib2.search.EditorFindSupport;
import org.netbeans.modules.editor.search.SearchPropertiesSupport.SearchProperties;
import org.openide.awt.CloseButtonFactory;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * This is an implementation of a Firefox(TM) style Incremental Search Side Bar.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public final class SearchBar extends JPanel implements PropertyChangeListener {
    private static SearchBar searchbarInstance = null;
    private static final Logger LOG = Logger.getLogger(SearchBar.class.getName());
    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    private static final Color NOT_FOUND = Color.RED.darker();
    private static final Color INVALID_REGEXP = Color.red;
    // Delay times for incremental search [ms]
    private static final int SEARCH_DELAY_TIME_LONG = 300; // < 3 chars
    private static final int SEARCH_DELAY_TIME_SHORT = 20; // >= 3 chars
    private static final Color DEFAULT_FG_COLOR = UIManager.getColor("textText"); //NOI18N
    private WeakReference<JTextComponent> actualTextComponent;
    private List<PropertyChangeListener> actualComponentListeners = new LinkedList<PropertyChangeListener>();
    private FocusAdapter focusAdapterForComponent;
    private KeyListener keyListenerForComponent;
    private PropertyChangeListener propertyChangeListenerForComponent;
    private final JLabel findLabel;
    private final JComboBox incSearchComboBox;
    private final JTextComponent incSearchTextField;
    private final DocumentListener incSearchTextFieldListener;
    private boolean hadFocusOnIncSearchTextField = false;
    private final JButton findNextButton;
    private final JButton findPreviousButton;
    private final JCheckBox matchCaseCheckBox;
    private final JCheckBox wholeWordsCheckBox;
    private final JCheckBox regexpCheckBox;
    private final JCheckBox highlightCheckBox;
    private final JCheckBox wrapAroundCheckBox;
    private final JButton closeButton;
    private final SearchExpandMenu expandMenu;
    private SearchProperties searchProps = SearchPropertiesSupport.getSearchProperties();
    private boolean popupMenuWasCanceled = false;
    private Rectangle actualViewPort;
    private boolean highlightCanceled = false;
    private boolean whenOpenedWasNotVisible = false;

    public static SearchBar getInstance() {
        if (searchbarInstance == null) {
            searchbarInstance = new SearchBar();
        }
        return searchbarInstance;
    }

    /*
     * default getInstance
     */
    public static SearchBar getInstance(JTextComponent component) {
        SearchBar searchbarIns = getInstance();
        if (searchbarIns.getActualTextComponent() != component) {
            searchbarIns.setActualTextComponent(component);
        }
        return searchbarIns;
    }

    @SuppressWarnings("unchecked")
    private SearchBar() {
        addEscapeKeystrokeFocusBackTo(this);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setFocusCycleRoot(true);
        Color bgColor = getBackground();
        bgColor = new Color(Math.max(0, bgColor.getRed() - 20),
                Math.max(0, bgColor.getGreen() - 20),
                Math.max(0, bgColor.getBlue() - 20));
        setBackground(bgColor);
        setForeground(DEFAULT_FG_COLOR); //NOI18N

        add(Box.createHorizontalStrut(8)); //spacer in the beginnning of the toolbar

        SearchComboBox scb = new SearchComboBox();
        incSearchComboBox = scb;
        scb.getEditor().getEditorComponent().setBackground(bgColor);
        incSearchComboBox.setFocusable(false);
        incSearchComboBox.addPopupMenuListener(new SearchPopupMenuListener());
        incSearchTextField = scb.getEditorPane();
        //todo fix no effect
        incSearchTextField.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_IncrementalSearchText")); //NOI18N
        incSearchTextFieldListener = createIncSearchTextFieldListener(incSearchTextField);
        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
        addEnterKeystrokeFindNextTo(incSearchTextField);
        addShiftEnterKeystrokeFindPreviousTo(incSearchTextField);
        if (getCurrentKeyMapProfile().startsWith("Emacs")) { // NOI18N
            emacsProfileFix(incSearchTextField);
        }
        incSearchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (ReplaceBar.getInstance(SearchBar.getInstance()).isVisible()) {
                    ReplaceBar.getInstance(SearchBar.getInstance()).getReplaceTextField().select(0, 0);
                }
                hadFocusOnIncSearchTextField = true;
                incSearchTextField.selectAll();
            }
        });

        findLabel = new JLabel();
        Mnemonics.setLocalizedText(findLabel, NbBundle.getMessage(SearchBar.class, "CTL_Find")); // NOI18N
        findLabel.setLabelFor(incSearchTextField);
        add(findLabel);
        add(incSearchComboBox);

        final JToolBar.Separator leftSeparator = new JToolBar.Separator();
        leftSeparator.setOrientation(SwingConstants.VERTICAL);
        add(leftSeparator);

        findPreviousButton = createFindButton("org/netbeans/modules/editor/resources/find_previous.png", "CTL_FindPrevious"); // NOI18N
        findPreviousButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findPrevious();
            }
        });
        add(findPreviousButton);
        findNextButton = createFindButton("org/netbeans/modules/editor/resources/find_next.png", "CTL_FindNext"); // NOI18N
        findNextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findNext();
            }
        });
        add(findNextButton);

        final JToolBar.Separator rightSeparator = new JToolBar.Separator();
        rightSeparator.setOrientation(SwingConstants.VERTICAL);
        add(rightSeparator);

        matchCaseCheckBox = createCheckBox("CTL_MatchCase", EditorFindSupport.FIND_MATCH_CASE); // NOI18N
        add(matchCaseCheckBox);
        wholeWordsCheckBox = createCheckBox("CTL_WholeWords", EditorFindSupport.FIND_WHOLE_WORDS); // NOI18N
        add(wholeWordsCheckBox);
        regexpCheckBox = createRegExpCheckBox("CTL_Regexp", EditorFindSupport.FIND_REG_EXP); // NOI18N
        add(regexpCheckBox);
        highlightCheckBox = createCheckBox("CTL_Highlight", EditorFindSupport.FIND_HIGHLIGHT_SEARCH); // NOI18N
        add(highlightCheckBox);
        EditorFindSupport.getInstance().addPropertyChangeListener(WeakListeners.propertyChange(this, EditorFindSupport.getInstance()));
        wrapAroundCheckBox = createCheckBox("CTL_WrapAround", EditorFindSupport.FIND_WRAP_SEARCH); // NOI18N
        add(wrapAroundCheckBox);
        selectCheckBoxes();

        expandMenu = new SearchExpandMenu(matchCaseCheckBox.getHeight());
        JButton expButton = expandMenu.getExpandButton();
        expButton.setMnemonic(NbBundle.getMessage(SearchBar.class, "CTL_ExpandButton_Mnemonic").charAt(0)); // NOI18N
        expButton.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_ExpandButton")); // NOI18N
        add(expButton);

        // padding at the end of the toolbar
        add(expandMenu.getPadding());

        closeButton = createCloseButton();
        add(closeButton);

        makeBarExpandable(expandMenu);
        setVisible(false);
        usageLogging();
    }

    private static void usageLogging() {
        Logger logger = Logger.getLogger("org.netbeans.ui.metrics.editor"); // NOI18N
        LogRecord rec = new LogRecord(Level.INFO, "USG_SEARCH_TYPE"); // NOI18N
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        rec.setParameters(new Object[] {prefs.get(SimpleValueNames.EDITOR_SEARCH_TYPE, "default")}); // NOI18N
        rec.setLoggerName(logger.getName());
        logger.log(rec);
    }

    private void makeBarExpandable(SearchExpandMenu expMenu) {
        expMenu.addToInbar(matchCaseCheckBox);
        expMenu.addToInbar(wholeWordsCheckBox);
        expMenu.addToInbar(regexpCheckBox);
        expMenu.addToInbar(highlightCheckBox);
        expMenu.addToInbar(wrapAroundCheckBox);
        expMenu.addAllToBarOrder(Arrays.asList(this.getComponents()));
        remove(getExpandButton());
        getExpandButton().setVisible(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || evt.getPropertyName() == null || evt.getPropertyName().equals(EditorFindSupport.FIND_HIGHLIGHT_SEARCH)) {
            Boolean value = (Boolean) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
            highlightCheckBox.setSelected(value == null ? false : value.booleanValue());
        }
    }

    void updateIncSearchComboBoxHistory(String incrementalSearchText) {
        EditorFindSupport.getInstance().addToHistory(new EditorFindSupport.SPW(incrementalSearchText,
                wholeWordsCheckBox.isSelected(), matchCaseCheckBox.isSelected(), regexpCheckBox.isSelected()));
        incSearchTextField.getDocument().removeDocumentListener(incSearchTextFieldListener);
        // Add the text to the top of the list
        for (int i = incSearchComboBox.getItemCount() - 1; i >= 0; i--) {
            String item = (String) incSearchComboBox.getItemAt(i);
            if (item.equals(incrementalSearchText)) {
                incSearchComboBox.removeItemAt(i);
            }
        }
        ((MutableComboBoxModel) incSearchComboBox.getModel()).insertElementAt(incrementalSearchText, 0);
        incSearchComboBox.setSelectedIndex(0);
        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
    }

    private KeyListener createKeyListenerForComponent() {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    looseFocus();
                    ReplaceBar replaceBarInstance = ReplaceBar.getInstance(SearchBar.this);
                    if (replaceBarInstance.isVisible()) {
                        replaceBarInstance.looseFocus();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        };
    }

    private FocusAdapter createFocusAdapterForComponent() {
        return new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (e.getOppositeComponent() instanceof JRootPane) {
                    // Hack for linux where invoking Find from main menu caused focus gained on editor
                    // even when openning quick search
                    return;
                }
                hadFocusOnIncSearchTextField = false;
                if (isClosingSearchType() && !ReplaceBar.getInstance(SearchBar.getInstance()).isVisible()) {
                    looseFocus();
                }
            }
        };
    }

    private PropertyChangeListener createPropertyChangeListenerForComponent() {
        PropertyChangeListener pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt == null || !"keymap".equals(evt.getPropertyName())) { // NOI18N
                    return;
                }
                JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
                if (lastFocusedComponent == null) {
                    return;
                }
                Keymap keymap = lastFocusedComponent.getKeymap();

                if (keymap instanceof MultiKeymap) {
                    MultiKeymap multiKeymap = (MultiKeymap) keymap;

                    Action[] actions = lastFocusedComponent.getActions();
                    for (Action action : actions) { // Discover the keyStrokes for incremental-search-forward
                        String actionName = (String) action.getValue(Action.NAME);
                        if (actionName == null) {
                            LOG.log(Level.WARNING, "SearchBar: Null Action.NAME property of action: {0}\n", action); //NOI18N
                        } else if (actionName.equals(SearchNbEditorKit.INCREMENTAL_SEARCH_FORWARD) || actionName.equals(BaseKit.findNextAction)) {
                            keystrokeForSearchAction(multiKeymap, action,
                                    new AbstractAction() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            findNext();
                                        }
                                    });
                        } else if (actionName.equals(SearchNbEditorKit.INCREMENTAL_SEARCH_BACKWARD) || actionName.equals(BaseKit.findPreviousAction)) {
                            keystrokeForSearchAction(multiKeymap, action,
                                    new AbstractAction() {

                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            findPrevious();
                                        }
                                    });
                        }
                    }
                }
            }

            private void keystrokeForSearchAction(MultiKeymap multiKeymap, Action searchAction, AbstractAction newSearchAction) {
                KeyStroke[] keyStrokes = multiKeymap.getKeyStrokesForAction(searchAction);
                if (keyStrokes != null) {
                    InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                    for (KeyStroke ks : keyStrokes) {
                        LOG.log(Level.FINE, "found {1} search action, {0}", new Object[]{ks, searchAction.getValue(Action.NAME)}); //NOI18N
                        inputMap.put(ks, (String) searchAction.getValue(Action.NAME));
                    }
                    getActionMap().put((String) searchAction.getValue(Action.NAME), newSearchAction);
                }
            }
        };
        pcl.propertyChange(new PropertyChangeEvent(this, "keymap", null, null)); //NOI18N
        return pcl;
    }

    private void addShiftEnterKeystrokeFindPreviousTo(JTextComponent incSearchTextField) {
        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK, true),
                "incremental-find-previous"); // NOI18N
        incSearchTextField.getActionMap().put("incremental-find-previous", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findPrevious();
            }
        });
    }

    private void addEnterKeystrokeFindNextTo(JTextComponent incSearchTextField) {
        incSearchTextField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                "incremental-find-next"); // NOI18N
        incSearchTextField.getActionMap().put("incremental-find-next", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findNext();
                if (isClosingSearchType() && !ReplaceBar.getInstance(SearchBar.getInstance()).isVisible()) {
                    looseFocus();
                }
            }
        });
    }


    private DocumentListener createIncSearchTextFieldListener(final JTextComponent incSearchTextField) {
        final Timer searchDelayTimer = new Timer(SEARCH_DELAY_TIME_LONG, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                incrementalSearch();
            }
        });
        searchDelayTimer.setRepeats(false);

        // listen on text change
        return new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                // text changed - attempt incremental search
                if (incSearchTextField.getText().length() > 3) {
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_SHORT);
                }
                searchDelayTimer.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // text changed - attempt incremental search
                if (incSearchTextField.getText().length() <= 3) {
                    searchDelayTimer.setInitialDelay(SEARCH_DELAY_TIME_LONG);
                }
                searchDelayTimer.restart();
            }
        };
    }

    private JButton createCloseButton() {
        JButton button = CloseButtonFactory.createBigCloseButton();
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                looseFocus();
            }
        });
        button.setToolTipText(NbBundle.getMessage(SearchBar.class, "TOOLTIP_CloseIncrementalSearchSidebar")); // NOI18N
        return button;
    }

    private JCheckBox createRegExpCheckBox(String resName, final String findConstant) {
        final JCheckBox regExpCheckBox = new JCheckBox() {

            @Override
            public void setSelected(boolean b) {
                super.setSelected(b);
                wholeWordsCheckBox.setEnabled(!regexpCheckBox.isSelected());
            }

        };
        regExpCheckBox.setOpaque(false);
        Mnemonics.setLocalizedText(regExpCheckBox, NbBundle.getMessage(SearchBar.class, resName));
        regExpCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switchFindSupportValue(findConstant);
                wholeWordsCheckBox.setEnabled(!regexpCheckBox.isSelected());
                incrementalSearch();
            }
        });
        regExpCheckBox.setMargin(BUTTON_INSETS);
        regExpCheckBox.setFocusable(false);
        return regExpCheckBox;
    }
    
    JCheckBox createCheckBox(String resName, final String findConstant) {
        final JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        Mnemonics.setLocalizedText(checkBox, NbBundle.getMessage(SearchBar.class, resName));
        checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                switchFindSupportValue(findConstant);
                incrementalSearch();
            }
        });
        checkBox.setMargin(BUTTON_INSETS);
        checkBox.setFocusable(false);
        return checkBox;
    }

    private void selectCheckBoxes() {
        wholeWordsCheckBox.setSelected(getFindSupportValue(EditorFindSupport.FIND_WHOLE_WORDS));
        wholeWordsCheckBox.setEnabled(!getRegExp());
        matchCaseCheckBox.setSelected(getFindSupportValue(EditorFindSupport.FIND_MATCH_CASE));
        regexpCheckBox.setSelected(getRegExp());
        highlightCheckBox.setSelected(getFindSupportValue(EditorFindSupport.FIND_HIGHLIGHT_SEARCH));
        wrapAroundCheckBox.setSelected(getFindSupportValue(EditorFindSupport.FIND_WRAP_SEARCH));
    }

    private JButton createFindButton(final String imageIcon,final String resName) {
        JButton button = new JButton(
                ImageUtilities.loadImageIcon(imageIcon, false));
        Mnemonics.setLocalizedText(button, NbBundle.getMessage(SearchBar.class, resName));
        button.setMargin(BUTTON_INSETS);
        return button;
    }


    // Treat Emacs profile specially in order to fix #191895
    private void emacsProfileFix(final JTextComponent incSearchTextField) {
        class JumpOutOfSearchAction extends AbstractAction {

            private String actionName;

            public JumpOutOfSearchAction(String n) {
                actionName = n;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                looseFocus();
                if (getActualTextComponent() != null) {
                    ActionEvent ev = new ActionEvent(getActualTextComponent(), e.getID(), e.getActionCommand(), e.getModifiers());
                    Action action = getActualTextComponent().getActionMap().get(actionName);
                    action.actionPerformed(ev);
                }
            }
        }
        String actionName = "caret-begin-line"; // NOI18N
        Action a1 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put(actionName, a1);
        actionName = "caret-end-line"; // NOI18N
        Action a2 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put(actionName, a2);

        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, InputEvent.CTRL_MASK, false), "caret-up-alt"); // NOI18N
        actionName = "caret-up"; // NOI18N
        Action a3 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put("caret-up-alt", a3); // NOI18N

        incSearchTextField.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, InputEvent.CTRL_MASK, false), "caret-down-alt"); // NOI18N
        actionName = "caret-down"; // NOI18N
        Action a4 = new JumpOutOfSearchAction(actionName);
        incSearchTextField.getActionMap().put("caret-down-alt", a4); // NOI18N
    }
    // From org.netbeans.modules.editor.settings.storage.EditorSettingsImpl
    private static final String DEFAULT_PROFILE = "NetBeans"; //NOI18N
    private static final String FATTR_CURRENT_KEYMAP_PROFILE = "currentKeymap";      // NOI18N
    private static final String KEYMAPS_FOLDER = "Keymaps"; // NOI18N

    /*
     * This method is verbatim copy from class
     * org.netbeans.modules.editor.settings.storage.EditorSettingsImpl bacause
     * we don't want to introduce the dependency between this module and Editor
     * Setting Storage module.
     */
    private String getCurrentKeyMapProfile() {
        String currentKeyMapProfile = null;
        FileObject fo = FileUtil.getConfigFile(KEYMAPS_FOLDER);
        if (fo != null) {
            Object o = fo.getAttribute(FATTR_CURRENT_KEYMAP_PROFILE);
            if (o instanceof String) {
                currentKeyMapProfile = (String) o;
            }
        }
        if (currentKeyMapProfile == null) {
            currentKeyMapProfile = DEFAULT_PROFILE;
        }
        return currentKeyMapProfile;
    }

    @Override
    public Dimension getPreferredSize() {
        expandMenu.computeLayout(this);
        return super.getPreferredSize();
    }

    @Override
    public String getName() {
        //Required for Aqua L&F toolbar UI
        return "editorSearchBar"; // NOI18N
    }

    void addEscapeKeystrokeFocusBackTo(JComponent component) {
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
                "loose-focus"); // NOI18N
        component.getActionMap().put("loose-focus", // NOI18N
                new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!popupMenuWasCanceled) {
                    looseFocus();
                    if (isClosingSearchType()) {
                        getActualTextComponent().scrollRectToVisible(actualViewPort);
                    }
                } else {
                    popupMenuWasCanceled = false;
                }
            }
        });
    }

    private static boolean isClosingSearchType() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.get(SimpleValueNames.EDITOR_SEARCH_TYPE, "default").equals("closing"); // NOI18N
    }

    public void gainFocus() {
        String lastSearch = "";
        if (!isClosingSearchType()) {
            lastSearch = incSearchTextField.getText();
        }
        incSearchTextField.getDocument().removeDocumentListener(incSearchTextFieldListener);
        SearchComboBoxEditor.changeToOneLineEditorPane((JEditorPane) incSearchTextField);
        addEnterKeystrokeFindNextTo(incSearchTextField);

        MutableComboBoxModel comboBoxModelIncSearch = ((MutableComboBoxModel) incSearchComboBox.getModel());
        for (int i = comboBoxModelIncSearch.getSize() - 1; i >= 0; i--) {
            comboBoxModelIncSearch.removeElementAt(i);
        }
        for (EditorFindSupport.SPW spw : EditorFindSupport.getInstance().getHistory()) {
            comboBoxModelIncSearch.addElement(spw.getSearchExpression());
        }
        if (!isClosingSearchType()) {
            incSearchTextField.setText(lastSearch);
        }
        if (!isVisible() && isClosingSearchType()) {
            whenOpenedWasNotVisible = true;
        }
        if (whenOpenedWasNotVisible) {
            incSearchTextField.setText("");
            whenOpenedWasNotVisible = false;
        }
        hadFocusOnIncSearchTextField = true;
        setVisible(true);
        initBlockSearch();
        EditorFindSupport.getInstance().setFocusedTextComponent(getActualTextComponent());

        incSearchTextField.requestFocusInWindow();

        boolean empty = incSearchTextField.getText().isEmpty();
        if (!empty) { // preselect the text in incremental search text field
            incSearchTextField.selectAll();
        }
        findPreviousButton.setEnabled(!empty);
        findNextButton.setEnabled(!empty);
        ReplaceBar.getInstance(this).getReplaceButton().setEnabled(!empty);
        ReplaceBar.getInstance(this).getReplaceAllButton().setEnabled(!empty);

        actualViewPort = getActualTextComponent().getVisibleRect();
        if (!isClosingSearchType() && highlightCanceled) {
            searchProps.setProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.TRUE);
            highlightCanceled = false;
        }
        incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
    }

    public void looseFocus() {
        hadFocusOnIncSearchTextField = false;
        if (!isVisible()) {
            return;
        }
        EditorFindSupport.getInstance().setBlockSearchHighlight(0, 0);
        EditorFindSupport.getInstance().incSearchReset();
        EditorFindSupport.getInstance().setFocusedTextComponent(null);
        if (getActualTextComponent() != null) {
            org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), "");
            getActualTextComponent().requestFocusInWindow();
        }
        setVisible(false);
        if (!isClosingSearchType() && getFindSupportValue(EditorFindSupport.FIND_HIGHLIGHT_SEARCH)) {
            searchProps.setProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, Boolean.FALSE);
            highlightCanceled = true;
        }
    }

    private void incrementalSearch() {
        if (getActualTextComponent() == null) {
            return;
        }
        String incrementalSearchText = incSearchTextField.getText();
        boolean empty = incrementalSearchText.length() <= 0;

        // Enable/disable the pre/next buttons
        findPreviousButton.setEnabled(!empty);
        findNextButton.setEnabled(!empty);
        ReplaceBar.getInstance(this).getReplaceButton().setEnabled(!empty);
        ReplaceBar.getInstance(this).getReplaceAllButton().setEnabled(!empty);

        // configure find properties
        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        findSupport.putFindProperties(getSearchProperties());

        // search starting at current caret position
        int caretPosition = getActualTextComponent().getSelectionStart();
        if (isClosingSearchType()) {
            caretPosition = getActualTextComponent().getCaretPosition();
        }
        if (regexpCheckBox.isSelected()) {
            Pattern pattern;
            String patternErrorMsg = null;
            try {
                pattern = Pattern.compile(incrementalSearchText);
            } catch (PatternSyntaxException e) {
                pattern = null;
                patternErrorMsg = e.getDescription();
            }
            if (pattern != null) {
                // valid regexp
                incSearchTextField.setForeground(DEFAULT_FG_COLOR); //NOI18N
                org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), "", StatusDisplayer.IMPORTANCE_INCREMENTAL_FIND);
            } else {
                // invalid regexp
                incSearchTextField.setForeground(INVALID_REGEXP);
                org.netbeans.editor.Utilities.setStatusBoldText(getActualTextComponent(), NbBundle.getMessage(
                        SearchBar.class, "incremental-search-invalid-regexp", patternErrorMsg)); //NOI18N
            }
        } else {
            if (findSupport.incSearch(searchProps.getProperties(), caretPosition) || empty) {
                // text found - reset incremental search text field's foreground
                incSearchTextField.setForeground(DEFAULT_FG_COLOR); //NOI18N
                org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), "", StatusDisplayer.IMPORTANCE_INCREMENTAL_FIND);
                changeHighlightCheckboxName(getCountFindMatches(findSupport));
            } else {
                // text not found - indicate error in incremental search
                // text field with red foreground
                incSearchTextField.setForeground(NOT_FOUND);
                org.netbeans.editor.Utilities.setStatusText(getActualTextComponent(), NbBundle.getMessage(
                        SearchBar.class, "incremental-search-not-found", incrementalSearchText),
                        StatusDisplayer.IMPORTANCE_INCREMENTAL_FIND); //NOI18N
                Toolkit.getDefaultToolkit().beep();
                changeHighlightCheckboxName(0);
            }
        }
    }

    void findNext() {
        find(true);
    }

    void findPrevious() {
        find(false);
    }


    private int getCountFindMatches(EditorFindSupport findSupport) {
            int num = 0;
            try {
                int[] blocks = findSupport.getBlocks(new int [] {-1, -1}, getActualTextComponent().getDocument(), 0, getActualTextComponent().getDocument().getLength());
                for (int i : blocks) {
                    if (i > 0) {
                        num++;
                    }
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return num == 0 ? 0 : (num + 1) / 2;
    }

    private void changeHighlightCheckboxName(int num) {
        if (num == 0) {
            Mnemonics.setLocalizedText(highlightCheckBox, NbBundle.getMessage(SearchBar.class, "CTL_Highlight"));
        } else if (num == 1) {
            Mnemonics.setLocalizedText(highlightCheckBox, NbBundle.getMessage(SearchBar.class, "CTL_Highlight_1_results"));
        } else {
            Mnemonics.setLocalizedText(highlightCheckBox, NbBundle.getMessage(SearchBar.class, "CTL_Highlight_n_results", num));
        }
    }

    private void find(boolean next) {
        String incrementalSearchText = incSearchTextField.getText();
        boolean empty = incrementalSearchText.length() <= 0;
        updateIncSearchComboBoxHistory(incrementalSearchText);

        // configure find properties
        EditorFindSupport findSupport = EditorFindSupport.getInstance();
        Map<String, Object> actualfindProps = getSearchProperties();
        findSupport.putFindProperties(actualfindProps);

        if (findSupport.find(actualfindProps, !next) || empty) {
            // text found - reset incremental search text field's foreground
            incSearchTextField.setForeground(DEFAULT_FG_COLOR); //NOI18N
            changeHighlightCheckboxName(getCountFindMatches(findSupport));
        } else {
            // text not found - indicate error in incremental search text field with red foreground
            incSearchTextField.setForeground(NOT_FOUND);
            changeHighlightCheckboxName(0);
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @SuppressWarnings("unchecked")
    void initBlockSearch() {
        JTextComponent c = getActualTextComponent();
        String selText;
        int startSelection;
        int endSelection;
        boolean blockSearchVisible = false;

        if (c != null) {
            startSelection = c.getSelectionStart();
            endSelection = c.getSelectionEnd();

            Document doc = c.getDocument();
            if (doc instanceof BaseDocument) {
                BaseDocument bdoc = (BaseDocument) doc;
                try {
                    int startLine = org.netbeans.editor.Utilities.getLineOffset(bdoc, startSelection);
                    int endLine = org.netbeans.editor.Utilities.getLineOffset(bdoc, endSelection);
                    if (endLine > startLine) {
                        blockSearchVisible = true;
                    }
                } catch (BadLocationException ble) {
                }
            }

            // caretPosition = bwdSearch.isSelected() ? c.getSelectionEnd() : c.getSelectionStart();

            if (!blockSearchVisible) {
                selText = c.getSelectedText();
                if (selText != null && selText.length() > 0) {
                    int n = selText.indexOf('\n');
                    if (n >= 0) {
                        selText = selText.substring(0, n);
                    }
                    incSearchTextField.setText(selText);
                } else {
                    if (isClosingSearchType()) {
                        String findWhat = (String) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_WHAT);
                        if (findWhat != null && findWhat.length() > 0) {
                            incSearchTextField.getDocument().removeDocumentListener(incSearchTextFieldListener);
                            incSearchTextField.setText(findWhat);
                            incSearchTextField.getDocument().addDocumentListener(incSearchTextFieldListener);
                        }
                    }

                }
            }

            int blockSearchStartOffset = blockSearchVisible ? startSelection : 0;
            int blockSearchEndOffset = blockSearchVisible ? endSelection : 0;

            try {
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH, blockSearchVisible);
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_START, doc.createPosition(blockSearchStartOffset));
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_END, doc.createPosition(blockSearchEndOffset));
                EditorFindSupport.getInstance().setBlockSearchHighlight(blockSearchStartOffset, blockSearchEndOffset);
            } catch (BadLocationException ble) {
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH, Boolean.FALSE);
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_START, null);
                searchProps.setProperty(EditorFindSupport.FIND_BLOCK_SEARCH_END, null);
            }

            EditorFindSupport.getInstance().putFindProperties(searchProps.getProperties());
        }
    }

    boolean getFindSupportValue(String findConstant) {
        Boolean b = (Boolean) searchProps.getProperty(findConstant);
        return b != null ? b.booleanValue() : false;
    }

    private void switchFindSupportValue(String findConstant) {
        searchProps.setProperty(findConstant, !getFindSupportValue(findConstant));
    }

    boolean getRegExp() {
        return getFindSupportValue(EditorFindSupport.FIND_REG_EXP);
    }

    boolean hadFocusOnTextField() {
        return hadFocusOnIncSearchTextField;
    }

    void lostFocusOnTextField() {
        hadFocusOnIncSearchTextField = false;
    }

    void setActualTextComponent(JTextComponent component) {
        if (getActualTextComponent() != null) {
            getActualTextComponent().removeFocusListener(focusAdapterForComponent);
            getActualTextComponent().removePropertyChangeListener(propertyChangeListenerForComponent);
            getActualTextComponent().removeKeyListener(keyListenerForComponent);
        }
        if (focusAdapterForComponent == null) {
            focusAdapterForComponent = createFocusAdapterForComponent();
        }
        if (propertyChangeListenerForComponent == null) {
            propertyChangeListenerForComponent = createPropertyChangeListenerForComponent();
        }
        if (keyListenerForComponent == null) {
            keyListenerForComponent = createKeyListenerForComponent();
        }
        component.addFocusListener(focusAdapterForComponent);
        component.addPropertyChangeListener(propertyChangeListenerForComponent);
        component.addKeyListener(keyListenerForComponent);
        for (PropertyChangeListener pcl : actualComponentListeners) {
            pcl.propertyChange(new PropertyChangeEvent(this, "actualTextComponent", getActualTextComponent(), component)); //NOI18N
        }
        actualTextComponent = new WeakReference<JTextComponent>(component);
        EditorFindSupport.getInstance().setFocusedTextComponent(getActualTextComponent());
    }

    void addActualComponentListener(PropertyChangeListener propertyChangeListener) {
        actualComponentListeners.add(propertyChangeListener);
    }

    public JTextComponent getActualTextComponent() {
        return actualTextComponent != null ? actualTextComponent.get() : null;
    }

    JTextComponent getIncSearchTextField() {
        return incSearchTextField;
    }

    JButton getCloseButton() {
        return closeButton;
    }

    JLabel getFindLabel() {
        return findLabel;
    }

    JButton getFindNextButton() {
        return findNextButton;
    }

    JButton getFindPreviousButton() {
        return findPreviousButton;
    }

    public Map<String, Object> getSearchProperties() {
        searchProps.setProperty(EditorFindSupport.FIND_WHAT, incSearchTextField.getText());
        searchProps.setProperty(EditorFindSupport.FIND_MATCH_CASE, matchCaseCheckBox.isSelected());
        searchProps.setProperty(EditorFindSupport.FIND_WHOLE_WORDS, wholeWordsCheckBox.isSelected());
        searchProps.setProperty(EditorFindSupport.FIND_REG_EXP, regexpCheckBox.isSelected());
        searchProps.setProperty(EditorFindSupport.FIND_BACKWARD_SEARCH, Boolean.FALSE);
        searchProps.setProperty(EditorFindSupport.FIND_INC_SEARCH, Boolean.TRUE);
        searchProps.setProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, highlightCheckBox.isSelected());
        searchProps.setProperty(EditorFindSupport.FIND_WRAP_SEARCH, wrapAroundCheckBox.isSelected());
        return searchProps.getProperties();
    }

    public void setSearchProperties(SearchProperties searchProperties) {
        searchProps = searchProperties;
        selectCheckBoxes();
    }

    JCheckBox getMatchCaseCheckBox() {
        return matchCaseCheckBox;
    }

    JCheckBox getRegexpCheckBox() {
        return regexpCheckBox;
    }


    JComponent getExpandButton() {
        return expandMenu.getExpandButton();
    }

    public boolean isPopupMenuWasCanceled() {
        return popupMenuWasCanceled;
    }

    public void setPopupMenuWasCanceled(boolean popupMenuWasCanceled) {
        this.popupMenuWasCanceled = popupMenuWasCanceled;
    }

    public JComboBox getIncSearchComboBox() {
        return incSearchComboBox;
    }

    private class SearchPopupMenuListener implements PopupMenuListener {
        private boolean canceled = false;

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (!canceled) {
                Object selectedItem = getIncSearchComboBox().getModel().getSelectedItem();
                if (selectedItem instanceof String) {
                    String findWhat = (String) selectedItem;
                    for (EditorFindSupport.SPW spw : EditorFindSupport.getInstance().getHistory()) {
                        if (findWhat.equals(spw.getSearchExpression())) {
                            SearchBar.getInstance().getRegexpCheckBox().setSelected(spw.isRegExp());
                            break;
                        }
                    }
                }
            } else {
                canceled = false;
            }
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            canceled = true;
            SearchBar.getInstance().setPopupMenuWasCanceled(true);
        }
    };
}

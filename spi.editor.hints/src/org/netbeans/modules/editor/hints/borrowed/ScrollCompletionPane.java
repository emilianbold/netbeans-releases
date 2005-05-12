/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.hints.borrowed;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.EditorKit;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.hints.spi.Hint;

/**
* Pane displaying the completion view and accompanying components
* like label for title etc.
*
* @author Miloslav Metelka, Martin Roskanin, Dusan Balek
* @version 1.00
*/

public class ScrollCompletionPane extends JScrollPane implements SettingsChangeListener {
    
    private static final String COMPLETION_UP = "completion-up"; //NOI18N
    private static final String COMPLETION_DOWN = "completion-down"; //NOI18N
    private static final String COMPLETION_PGUP = "completion-pgup"; //NOI18N
    private static final String COMPLETION_PGDN = "completion-pgdn"; //NOI18N
    private static final String COMPLETION_BEGIN = "completion-begin"; //NOI18N
    private static final String COMPLETION_END = "completion-end"; //NOI18N

    private static final int ACTION_COMPLETION_UP = 1;
    private static final int ACTION_COMPLETION_DOWN = 2;
    private static final int ACTION_COMPLETION_PGUP = 3;
    private static final int ACTION_COMPLETION_PGDN = 4;
    private static final int ACTION_COMPLETION_BEGIN = 5;
    private static final int ACTION_COMPLETION_END = 6;

    private JTextComponent component;

    private ListCompletionView view;
    private JLabel topLabel;

    private Dimension minSize;
    private Dimension maxSize;
    private Dimension scrollBarSize;

    public ScrollCompletionPane(JTextComponent component, List result, String title, ListSelectionListener listener) {
        this.component = component;
        
        // Compute size of the scrollbars
        Dimension smallSize = super.getPreferredSize();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        scrollBarSize = super.getPreferredSize();
        scrollBarSize.width -= smallSize.width;
        scrollBarSize.height -= smallSize.height;
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);

        Settings.addSettingsChangeListener(this);
        settingsChange(null); // initialize sizes

        // Add the completion view
        view = new ListCompletionView();
//        view.addListSelectionListener(listener);
        view.setResult(result);
        resetViewSize();
        setViewportView(view);

        setTitle(title);
        installKeybindings();
        setFocusable (false);
        view.setFocusable (false);
    }
    
    public ListCompletionView getView() {
        return view;
    }

    public void reset(List result, String title) {
        view.setResult(result);
        resetViewSize();
        setTitle(title);
    }

    public Hint getSelectedCompletionItem() {
        Object ret = view.getSelectedValue();
        return ret instanceof Hint ? (Hint) ret : null;
    }

    public void settingsChange(SettingsChangeEvent evt) {
        Class kitClass = Utilities.getKitClass(component);
        if (kitClass != null) {
            minSize = (Dimension)SettingsUtil.getValue(kitClass,
                      ExtSettingsNames.COMPLETION_PANE_MIN_SIZE,
                      ExtSettingsDefaults.defaultCompletionPaneMinSize);
            setMinimumSize(minSize);

            maxSize = (Dimension)SettingsUtil.getValue(kitClass,
                      ExtSettingsNames.COMPLETION_PANE_MAX_SIZE,
                      ExtSettingsDefaults.defaultCompletionPaneMaxSize);
            setMaximumSize(maxSize);
        }
    }

    public Dimension getPreferredSize() {
        Dimension ps = super.getPreferredSize();

        /* Add size of the vertical scrollbar by default. This could be improved
        * to be done only if the height exceeds the bounds. */
        int width = ps.width + scrollBarSize.width;
        boolean displayHorizontalScrollbar = width > maxSize.width;
        width = Math.max(Math.max(width, minSize.width),
                            getTitleComponentPreferredSize().width);
        width = Math.min(width, maxSize.width);

        int height = displayHorizontalScrollbar ? ps.height + scrollBarSize.height : ps.height;
        height = Math.min(height, maxSize.height);
        height = Math.max(height, minSize.height);
        return new Dimension(width, height);
    }

    private void resetViewSize() {
        Dimension viewSize = view.getPreferredSize();
        if (viewSize.width > maxSize.width - scrollBarSize.width) {
            viewSize.width = maxSize.width - scrollBarSize.width;
            view.setPreferredSize(viewSize);
        }
    }
    
    private void setTitle(String title) {
        if (title == null) {
            if (topLabel != null) {
                setColumnHeader(null);
                topLabel = null;
            }
        } else {
            if (topLabel != null) {
                topLabel.setText(title);
            } else {
                topLabel = new JLabel(title);
                topLabel.setForeground(Color.blue);
                topLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
                setColumnHeaderView(topLabel);
            }
        }
    }

    private Dimension getTitleComponentPreferredSize() {
        return topLabel != null ? topLabel.getPreferredSize() : new Dimension();
    }

    /** Attempt to find the editor keystroke for the given editor action. */
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey) {
        // This method is implemented due to the issue
        // #25715 - Attempt to search keymap for the keybinding that logically corresponds to the action
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        if (component != null) {
            TextUI ui = component.getUI();
            Keymap km = component.getKeymap();
            if (ui != null && km != null) {
                EditorKit kit = ui.getEditorKit(component);
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit)kit).getActionByName(editorActionName);
                    if (a != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            ret = keys;
                        }
                    }
                }
            }
        }
        return ret;
    }

    private void registerKeybinding(int action, String actionName, KeyStroke stroke, String editorActionName){
        KeyStroke[] keys = findEditorKeys(editorActionName, stroke);
        for (int i = 0; i < keys.length; i++) {
            getInputMap().put(keys[i], actionName);
        }
        getActionMap().put(actionName, new CompletionPaneAction(action));
    }

    private void installKeybindings() {
        // Register up key
        registerKeybinding(ACTION_COMPLETION_UP, COMPLETION_UP,
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
        BaseKit.upAction
        );

        // Register down key
        registerKeybinding(ACTION_COMPLETION_DOWN, COMPLETION_DOWN,
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
        BaseKit.downAction
        );

        // Register PgDn key
        registerKeybinding(ACTION_COMPLETION_PGDN, COMPLETION_PGDN,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
        BaseKit.pageDownAction
        );

        // Register PgUp key
        registerKeybinding(ACTION_COMPLETION_PGUP, COMPLETION_PGUP,
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
        BaseKit.pageUpAction
        );

        // Register home key
        registerKeybinding(ACTION_COMPLETION_BEGIN, COMPLETION_BEGIN,
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
        BaseKit.beginLineAction
        );

        // Register end key
        registerKeybinding(ACTION_COMPLETION_END, COMPLETION_END,
        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
        BaseKit.endLineAction
        );
    }

    private class CompletionPaneAction extends AbstractAction {
        private int action;

        private CompletionPaneAction(int action) {
            this.action = action;
        }

        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            switch (action) {
                case ACTION_COMPLETION_UP:
                    view.up();
                    break;
                case ACTION_COMPLETION_DOWN:
                    view.down();
                    break;
                case ACTION_COMPLETION_PGUP:
                    view.pageUp();
                    break;
                case ACTION_COMPLETION_PGDN:
                        view.pageDown();
                    break;
                case ACTION_COMPLETION_BEGIN:
                        view.begin();
                    break;
                case ACTION_COMPLETION_END:
                        view.end();
                    break;
            }
        }
    }
}

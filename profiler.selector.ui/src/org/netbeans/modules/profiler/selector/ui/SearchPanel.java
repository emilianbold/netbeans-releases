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
package org.netbeans.modules.profiler.selector.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;
import javax.swing.*;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * A visual component providing search input to an associated component (eg. <code>JTree</code>)
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "DSC_FindNext=Find Next ({0})",
    "DSC_FindPrevious=Find Previous ({0})",
    "DSC_Close=Close ({0})"
})
abstract public class SearchPanel extends JPanel {
    final private static String SEARCHPANEL_PERMANENT = "searchPanel.shownDefault"; // NOI18N
    final private static String SEARCH_NEXT_NAME = "search.next"; // NOI18N
    final private static String SEARCH_PREV_NAME = "search.prev"; // NOI18N
    
    final private JTextField searchField = new JTextField(""); // NOI18N
    final private int findKeyEvent = KeyEvent.VK_F3;
    final private int closeKeyEvent = KeyEvent.VK_ESCAPE;
    
    private boolean permanent;
    
    private Action nextAction = new AbstractAction("", Icons.getIcon(GeneralIcons.FIND_NEXT)) { // NOI18N
        {
            KeyStroke ks = KeyStroke.getKeyStroke(findKeyEvent, 0);
            putValue(AbstractAction.ACCELERATOR_KEY, ks);
            putValue(AbstractAction.SHORT_DESCRIPTION, Bundle.DSC_FindNext(getAcceleratorDesc(ks)));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            performNext();
        }
    };
    private Action prevAction = new AbstractAction("", Icons.getIcon(GeneralIcons.FIND_PREVIOUS)) { // NOI18N
        {
            KeyStroke ks = KeyStroke.getKeyStroke(findKeyEvent, KeyEvent.SHIFT_DOWN_MASK);
            putValue(AbstractAction.ACCELERATOR_KEY, ks);
            putValue(AbstractAction.SHORT_DESCRIPTION, Bundle.DSC_FindPrevious(getAcceleratorDesc(ks)));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            performPrevious();
        }
    };
    private Action closeAction = new AbstractAction("", Icons.getIcon(GeneralIcons.STOP)) { // NOI18N
        {
            KeyStroke ks = KeyStroke.getKeyStroke(closeKeyEvent, 0);
            putValue(AbstractAction.ACCELERATOR_KEY, ks);
            putValue(AbstractAction.SHORT_DESCRIPTION, Bundle.DSC_Close(getAcceleratorDesc(ks)));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            SearchPanel.this.setVisible(false);
            onClose();
        }
    };
    
    private JButton prev = new JButton(prevAction);
    private JButton next = new JButton(nextAction);
    private JButton close = new JButton(closeAction);
    
    @NbBundle.Messages({
        "LBL_Find=Find:",
        "INFO_TypeToSearch=You are about to close the search panel.\nWhen you need it again simply type a few letters in the selector tree."
    })
    SearchPanel(@NonNull JComponent buddy) {
        this.permanent = NbPreferences.forModule(SearchPanel.class).getBoolean(SEARCHPANEL_PERMANENT, true);;
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(new JLabel(Bundle.LBL_Find()));
        add(searchField);
        
        searchField.setOpaque(false);
        prev.setBorderPainted(false);
        next.setBorderPainted(false);
        close.setBorderPainted(false);
        
        add(prev);
        add(next);
        add(close);
        
        buddy.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown() || e.isMetaDown() || e.isActionKey()) return;
                
                if (!Character.isUnicodeIdentifierStart(e.getKeyChar())) return; // NOI18N

                if (e.getKeyCode() != KeyEvent.VK_ENTER && e.getKeyCode() != KeyEvent.VK_SPACE && e.getKeyCode() != closeKeyEvent) {
                    if (!isVisible()) {
                        open();
                    } else {
                        searchField.requestFocus();
                    }
                    setSearchText(String.valueOf(e.getKeyChar()));
                    e.consume();
                } else if (isVisible() && e.getKeyCode() == closeKeyEvent) {
                    onCancel();
                    setVisible(false);
                    e.consume();
                }
            }
        });
        
        buddy.getInputMap().put((KeyStroke)nextAction.getValue(Action.ACCELERATOR_KEY), SEARCH_NEXT_NAME);
        buddy.getInputMap().put((KeyStroke)prevAction.getValue(Action.ACCELERATOR_KEY), SEARCH_PREV_NAME);

        buddy.getActionMap().put(SEARCH_NEXT_NAME, nextAction);
        buddy.getActionMap().put(SEARCH_PREV_NAME, prevAction);
        
        searchField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (isVisible()) {
                    int code = e.getKeyCode();
                    if (code == KeyEvent.VK_ENTER || code == closeKeyEvent) {
                        e.consume();
                        
                        if (code == KeyEvent.VK_ENTER) {
                            performFind();
                        } else {
                            onCancel();
                        }
                        if (!SearchPanel.this.permanent || code == closeKeyEvent) {
                            setVisible(false);
                            onClose();
                        }
                    }
                }
            }
        });
        
        setVisible(permanent);
    }
    
    void reset() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                searchField.setText("");
                setVisible(permanent);
            }
        });
        
    }
    
    void open() {
        setVisible(true);
        searchField.requestFocus();
        onOpen();
    }
    
    void setSearchText(String text) {
        searchField.setText(text);
    }
    
    String getSearchText() {
        return searchField.getText();
    }
    
    void setPermanent(boolean permanent) {
        this.permanent = permanent;
        
        NbPreferences.forModule(SearchPanel.class).putBoolean(SEARCHPANEL_PERMANENT, permanent);
    }
    
    KeyStroke getNextShortcut() {
        return (KeyStroke)nextAction.getValue(Action.ACCELERATOR_KEY);
    }
    
    KeyStroke getPrevShortcut() {
        return (KeyStroke)prevAction.getValue(Action.ACCELERATOR_KEY);
    }
    
    /**
     * Called when a user enters the search pattern and presses enter
     */
    protected abstract void performFind();
    /**
     * Move to the next occurrence of the search pattern
     */
    protected abstract void performNext();
    /**
     * Move to the previous occurrence of the search pattern
     */
    protected abstract void performPrevious();
    /**
     * Called when a user cancels the search by eg. hitting Escape
     */
    protected void onCancel() {};
    /**
     * Called when the panel is being explicitly closed
     */
    protected void onClose(){};
    /**
     * Called when the panel is being open
     */
    protected void onOpen() {};
    
    private String getAcceleratorDesc(KeyStroke ks) {
        String emacs = Utilities.keyToString(ks);
        StringTokenizer st = new StringTokenizer(emacs, "-"); // NOI18N
        if (st.countTokens() == 1) {
            return st.nextToken();
        }
        String mods = st.nextToken();
        String key = st.nextToken();
        
        StringBuilder sb = new StringBuilder();
        for(char c : mods.toCharArray()) {
            switch (c) {
                case 'S': {
                    sb.append("Shift+"); // NOI18N
                    break;
                }
                case 'A':
                case 'O': {
                    sb.append("Alt+"); // NOI18N
                    break;
                }
                case 'C': 
                case 'D': {
                    sb.append("Ctrl+"); // NOI18N
                    break;
                }
                case 'M': {
                    sb.append("Meta+"); // NOI18N
                    break;
                }
            }
        }
        sb.append(key);
        
        return sb.toString();
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.search.impl.output;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Position;
import javax.swing.tree.TreePath;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.10.18
 */
final class Wrapper extends JPanel {

    Wrapper(JTree tree) {
        myTree = tree;
        myTree.addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent event) {
                int modifiers = event.getModifiers();
                char c = event.getKeyChar();

                if (c == KeyEvent.VK_ENTER) {
                    return;
                }
                if (c == KeyEvent.VK_ESCAPE) {
                    return;
                }
                if (c == KeyEvent.VK_DELETE) {
                    return;
                }
                if (isCtrl(modifiers)) {
                    return;
                }
                if (isAlt(modifiers)) {
                    return;
                }
                event.consume();
                myTextField.setText(String.valueOf(c));
                showPanel();
            }
        });
        JLabel label = new JLabel(" " + i18n(Wrapper.class, "LBL_Quick_Search")); // NOI18N
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(label);

        myTextField = new JTextField() {

            public void processKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hidePanel();
                    event.consume();

                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            myTree.requestFocus();
                        }
                    });
                } else {
                    super.processKeyEvent(event);
                }
            }
        };
        setWidth(myTextField, TEXT_WIDTH);
        setVisible(false);

        MultiListener listener = new MultiListener();
        myTextField.addKeyListener(listener);
        myTextField.addFocusListener(listener);
        myTextField.getDocument().addDocumentListener(listener);

        add(myTextField);
        label.setLabelFor(myTextField);

        setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
    }

    private void showPanel() {
        setVisible(true);
        myTextField.requestFocus();
    }

    private void hidePanel() {
        setVisible(false);
    }

    private List<TreePath> doSearch(String prefix) {
        List<TreePath> results = new ArrayList<TreePath>();
        int[] rows = myTree.getSelectionRows();
        int size = myTree.getRowCount();
        int startIndex;

        if (rows == null || rows.length == 0) {
            startIndex = 0;
        } else {
            startIndex = rows[0];
        }

        if (size == 0) {
            return results;
        }
        while (true) {
            startIndex = startIndex % size;
            TreePath path = myTree.getNextMatch(prefix, startIndex, Position.Bias.Forward);

            if (path == null || results.contains(path)) {
                break;
            }
            startIndex = myTree.getRowForPath(path);
            results.add(path);
            startIndex++;
        }
        return results;
    }

    // ----------------------------------------------------------------------------------------
    private class MultiListener extends KeyAdapter implements DocumentListener, FocusListener {

        public MultiListener() {
            myResults = new ArrayList<TreePath>();
        }

        public void changedUpdate(DocumentEvent e) {
            searchForNode();
        }

        public void insertUpdate(DocumentEvent e) {
            searchForNode();
        }

        public void removeUpdate(DocumentEvent e) {
            searchForNode();
        }

        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            if (keyCode == KeyEvent.VK_UP) {
                myIndex--;
                showResult();
            } else if (keyCode == KeyEvent.VK_DOWN) {
                myIndex++;
                showResult();
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                hidePanel();
                myTree.requestFocus();
            }
        }

        private void searchForNode() {
            myIndex = 0;
            myResults.clear();
            String text = myTextField.getText().toUpperCase();

            if (text.length() > 0) {
                myResults = doSearch(text);
                showResult();
            }
        }

        private void showResult() {
            int size = myResults.size();

            if (size > 0) {
                if (myIndex < 0) {
                    myIndex = size - 1;
                } else if (myIndex >= size) {
                    myIndex = 0;
                }
                TreePath path = myResults.get(myIndex);
                myTree.setSelectionPath(path);
                myTree.scrollPathToVisible(path);
            } else {
                myTree.clearSelection();
            }
        }

        public void focusGained(FocusEvent event) {
        }

        public void focusLost(FocusEvent event) {
            hidePanel();
        }
    
        private int myIndex;
        private List<TreePath> myResults;
    }

    private JTree myTree;
    private JTextField myTextField;
    private static final int TEXT_WIDTH = 60;
}

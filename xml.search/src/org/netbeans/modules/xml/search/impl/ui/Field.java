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
package org.netbeans.modules.xml.search.impl.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.22
 */
final class Field {

    Field(String items) {
        myItems = new ArrayList<String>();
        createUIComponent();
        setItems(items);
    }

    public String getText() {
        return (String) myComboBox.getEditor().getItem();
    }

    public void requestFocus() {
        myComboBox.getEditor().getEditorComponent().requestFocus();
    }

    public String save() {
        addItem((String) myComboBox.getEditor().getItem()); // save last input item
        StringBuilder items = new StringBuilder();

        for (int i = 0; i < myItems.size(); i++) {
            items.append((i == 0 ? "" : DELIM) + myItems.get(i)); // NOI18N

            if (i == MAX_SIZE) {
                break;
            }
        }
        return items.toString();
    }

    public void addActionListener(ActionListener listener) {
        myActionListener = listener;
    }

    public JComponent getUIComponent() {
        return myComboBox;
    }

    private void addItem(String item) {
        if (item == null) {
            select();
            return;
        }
        myComboBox.removeAllItems();

        if (myItems.contains(item)) {
            myItems.remove(item);
        }
        myItems.add(0, item);

        for (int i = 0; i < myItems.size(); i++) {
            if (i == MAX_SIZE) {
                break;
            }
            myComboBox.addItem(myItems.get(i));
        }
        myComboBox.setSelectedItem(item);
        select();
    }

    private void setItems(String items) {
        if (items == null) {
            return;
        }
        if (items.startsWith(DELIM)) {
            if (!myItems.contains("")) { // NOI18N
                myItems.add(""); // NOI18N
            }
        }
        StringTokenizer stk = new StringTokenizer(items, DELIM);

        while (stk.hasMoreTokens()) {
            String item = stk.nextToken();

            if (!myItems.contains(item)) {
                myItems.add(item);
            }
        }
        myComboBox.removeAllItems();

        for (int i = 0; i < myItems.size(); i++) {
            myComboBox.addItem(myItems.get(i));

            if (i == MAX_SIZE) {
                break;
            }
        }
        if (myItems.size() > 0) {
            myComboBox.setSelectedItem(myItems.get(0));
        }
        select();
    }

    private void createUIComponent() {
        myComboBox = new JComboBox();
        myComboBox.setEditable(true);
        myComboBox.getEditor().getEditorComponent().addKeyListener(
            new KeyAdapter() {

                public void keyReleased(KeyEvent event) {
                    if (myActionListener != null) {
                        myActionListener.actionPerformed(new ActionEvent(myComboBox.getEditor().getItem(), 0, null));
                    }
                }

                public void keyPressed(KeyEvent key) {
                    if (key.getKeyCode() == key.VK_ENTER && !myComboBox.isPopupVisible()) {
                        JRootPane rootPane = SwingUtilities.getRootPane(myComboBox);

                        if (rootPane != null) {
                            rootPane.dispatchEvent(key);
                        }
                    }
                }
            }
        );
    }

    private void select() {
        myComboBox.getEditor().selectAll();
    }

    private JComboBox myComboBox;
    private List<String> myItems;
    private ActionListener myActionListener;
    private static final int MAX_SIZE = 15;
    private static final String DELIM = "\u007f"; // NOI18N
}

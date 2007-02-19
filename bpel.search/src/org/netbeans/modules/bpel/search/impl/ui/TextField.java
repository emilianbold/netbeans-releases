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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.search.impl.ui;

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

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.22
 */
public final class TextField {

  /**{@inheritDoc}*/
  public TextField(String items) {
    myItems = new ArrayList<String>();
    createUIComponent();
    setItems(items);
  }

  /**{@inheritDoc}*/
  public String getText() {
    return (String) myComboBox.getEditor().getItem();
  }

  /**{@inheritDoc}*/
  public void requestFocus() {
    myComboBox.getEditor().getEditorComponent().requestFocus();
  }

  /**{@inheritDoc}*/
  public String save() {
    addItem((String) myComboBox.getEditor().getItem()); // save last input item
    StringBuffer items = new StringBuffer();

    for (int i=0; i < myItems.size(); i++) {
      items.append((i==0 ? "" : DELIM) + myItems.get(i)); // NOI18N
      
      if (i == MAX_SIZE) {
        break;
      }
    }
    return items.toString();
  }

  /**{@inheritDoc}*/
  public void addActionListener(ActionListener listener) {
    myActionListener = listener;
  }

  /**{@inheritDoc}*/
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

    for (int i=0; i < myItems.size(); i++) {
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
      if ( !myItems.contains("")) { // NOI18N
        myItems.add(""); // NOI18N
      }
    }
    StringTokenizer stk = new StringTokenizer(items, DELIM);

    while (stk.hasMoreTokens()) {
      String item = stk.nextToken();

      if ( !myItems.contains(item)) {
        myItems.add(item);
      }
    }
    myComboBox.removeAllItems();

    for (int i=0; i < myItems.size(); i++) {
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
            myActionListener.actionPerformed(
              new ActionEvent(myComboBox.getEditor().getItem(), 0, null));
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

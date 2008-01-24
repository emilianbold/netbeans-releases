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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.10.18
 */
final class Navigation extends JPanel {
  
  Navigation(JTree tree, JScrollPane scrollPane, JComponent component) {
    myWrapper = new Wrapper(tree);
    myScrollPane = scrollPane;
    myComponent = component;

    add(myWrapper);
    add(myComponent);
  }

  @Override
  public boolean isOptimizedDrawingEnabled()
  {
    return false;
  }

  @Override
  public void doLayout()
  {
    Dimension size = myWrapper.getPreferredSize();
    int x = getWidth() - myScrollPane.getVerticalScrollBar().getPreferredSize().width -
      size.width - INSET;
    myWrapper.setBounds(x, INSET, size.width, size.height);
    myComponent.setBounds(0, 0, getWidth(), getHeight());
  }
  
  private JPanel myWrapper;
  private JComponent myComponent;
  private JScrollPane myScrollPane;
  private static final int INSET = 4;
}

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

package org.netbeans.modules.form;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Tran Duc Trung
 */

class ComponentLayer extends JPanel
{
    ComponentLayer() {
        setBackground(Color.white);
        setFont(new Font("Dialog", Font.PLAIN, 12)); // NOI18N
        setLayout(new BorderLayout());
    }
    
//      public Insets getInsets() {
//          Insets insets = super.getInsets();
//          insets.top += 20;
//          insets.left += 20;
//          insets.bottom += 20;
//          insets.right += 20;
//          return insets;
//        }
}

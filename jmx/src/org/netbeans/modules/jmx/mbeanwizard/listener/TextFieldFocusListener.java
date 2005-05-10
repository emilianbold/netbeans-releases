/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.listener;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author an156382
 */
public class TextFieldFocusListener implements FocusListener {     
        public void focusGained(FocusEvent e) {
            
            Object source = e.getSource();
            Component opposite = e.getOppositeComponent();
            
            if (!e.isTemporary() &&
                    source instanceof JTextField &&
                    opposite instanceof JComponent ) {
                
                ((JTextField)source).selectAll();
            }
        }
        
        public void focusLost(FocusEvent e) {
            
        }
    }

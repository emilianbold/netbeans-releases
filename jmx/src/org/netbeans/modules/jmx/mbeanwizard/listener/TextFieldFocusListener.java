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
 * Class handling the focus listeners on text fields
 *
 */
public class TextFieldFocusListener implements FocusListener {
    
        /**
         * Method handling what to do when the text field gets the focus
         * @param e FocusEvent
         */
        public void focusGained(FocusEvent e) {
            
            Object source = e.getSource();
            Component opposite = e.getOppositeComponent();
            
            if (!e.isTemporary() &&
                    source instanceof JTextField &&
                    opposite instanceof JComponent ) {
                
                ((JTextField)source).selectAll();
            }
        }
        
        /**
         * Method handling what to do when the text field looses the focus
         * @param e FocusEvent
         */
        public void focusLost(FocusEvent e) {
            
        }
    }

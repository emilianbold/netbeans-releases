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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
            //System.out.println("host or port lost focus");
        }
    }

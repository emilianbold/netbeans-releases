/*
 * TextFieldFocusListener.java
 *
 * Created on April 6, 2005, 11:38 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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

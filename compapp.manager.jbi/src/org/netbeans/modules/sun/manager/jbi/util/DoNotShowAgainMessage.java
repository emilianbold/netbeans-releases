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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.sun.manager.jbi.util;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Notification message with do-not-show-again option.
 * 
 * @author jqian
 */
public class DoNotShowAgainMessage extends NotifyDescriptor.Message {

    private JCheckBox checkbox;
    private String checkboxText;

    /**
     * Create an informational report about the results of a command.
     *
     * @param message the message object
     * @see NotifyDescriptor#NotifyDescriptor
     */
    public DoNotShowAgainMessage(Object message) {
        this(message, INFORMATION_MESSAGE); 
    }
    
    /**
     * Create an informational report about the results of a command.
     *
     * @param message the message object
     * @see NotifyDescriptor#NotifyDescriptor
     */
    public DoNotShowAgainMessage(Object message, int messageType) {
        this(message, messageType, null);                
    }

    /**
     * Create a report about the results of a command.
     *
     * @param message   the message object
     * @param messageType   the type of message to be displayed
     * @param checkboxText  text for the do-not-show-again checkbox, 
     *                      null for the default message
     * @see NotifyDescriptor#NotifyDescriptor
     */
    public DoNotShowAgainMessage(Object message, int messageType, 
            String checkboxText) {
        super(message, messageType);  
        
        this.checkboxText = checkboxText;
        setMessage(createDesign());
    }

    /**
     * Get the do-not-show-again state.
     * @return the state whether the user wants to see this message again.
     */
    public boolean getDoNotShowAgain() {
        return checkbox.isSelected();
    }

    private Component createDesign() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(11, 0, 1, 11));
        
        Object msg = getMessage();
        if (msg instanceof Component) {
            panel.add((Component)msg, BorderLayout.NORTH);
        } else {
            JLabel label = new JLabel(super.getMessage().toString());
            panel.add(label, BorderLayout.NORTH);
        }
        
        if (checkboxText == null) {
            checkboxText = 
                NbBundle.getMessage(DoNotShowAgainMessage.class, 
                "LBL_DO_NOT_SHOW_MESSAGE_AGAIN"); // NOI18N 
        }
        checkbox = new JCheckBox(checkboxText);
        
        panel.add(checkbox, BorderLayout.SOUTH);        
        panel.requestFocus();
        
        return panel;
    }
}

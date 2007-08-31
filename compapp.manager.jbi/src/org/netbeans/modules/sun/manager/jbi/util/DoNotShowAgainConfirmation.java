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
public class DoNotShowAgainConfirmation extends NotifyDescriptor.Confirmation {

    private JCheckBox checkbox;
    private String checkboxText; 
    
   /**
    * Create a yes/no/cancel question with default title.
    *
    * @param message the message object
    * @see NotifyDescriptor#NotifyDescriptor
    */
    public DoNotShowAgainConfirmation(Object message) {
        this(message, YES_NO_CANCEL_OPTION);
    }

    /**
    * Create a yes/no/cancel question.
    *
    * @param message the message object
    * @param title the dialog title
    * @see NotifyDescriptor#NotifyDescriptor
    */
    public DoNotShowAgainConfirmation(Object message, String title) {
        this(message, title, YES_NO_CANCEL_OPTION);
    }

    /**
    * Create a question with default title.
    *
    * @param message the message object
    * @param optionType the type of options to display to the user
    * @see NotifyDescriptor#NotifyDescriptor
    */
    public DoNotShowAgainConfirmation(Object message, int optionType) {
        this(message, optionType, QUESTION_MESSAGE);
    }

    /**
    * Create a question.
    *
    * @param message the message object
    * @param title the dialog title
    * @param optionType the type of options to display to the user
    * @see NotifyDescriptor#NotifyDescriptor
    */
    public DoNotShowAgainConfirmation(Object message, String title, int optionType) {
        this(message, title, optionType, QUESTION_MESSAGE);
    }

    /**
    * Create a confirmation with default title.
    *
    * @param message the message object
    * @param optionType the type of options to display to the user
    * @param messageType the type of message to use
    * @see NotifyDescriptor#NotifyDescriptor
    */
    public DoNotShowAgainConfirmation(Object message, int optionType, int messageType) {
        super(message, optionType, messageType);
        
        setMessage(createDesign());
    }

    /**
    * Create a confirmation.
    *
    * @param message the message object
    * @param title the dialog title
    * @param optionType the type of options to display to the user
    * @param messageType the type of message to use
    * @see NotifyDescriptor#NotifyDescriptor
    */
    public DoNotShowAgainConfirmation(Object message, String title, int optionType, int messageType) {
        super(message, title, optionType, messageType);
        
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
                NbBundle.getMessage(DoNotShowAgainConfirmation.class, 
                "LBL_DO_NOT_ASK_FOR_CONFIRMATION_AGAIN"); // NOI18N 
        }
        
        checkbox = new JCheckBox(checkboxText);
        
        panel.add(checkbox, BorderLayout.SOUTH);        
        panel.requestFocus();
        
        return panel;
    }
}

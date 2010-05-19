/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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

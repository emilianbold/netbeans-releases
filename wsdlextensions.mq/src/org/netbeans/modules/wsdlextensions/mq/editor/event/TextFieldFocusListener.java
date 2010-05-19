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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor.event;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 * FocusListener implementation for text fields. When a field gains focus, this
 * listener will select the contents of the entire field.  When a field loses
 * focus, this listener executes the registered callback, if one is provided.
 *
 * @author Noel.Ang@sun.com
 */
public class TextFieldFocusListener implements FocusListener {
    private final JTextField subject;
    private final PostFocusCallback callback;

    public TextFieldFocusListener(JTextField subject,
                                  PostFocusCallback validator) {
        if (subject == null) {
            throw new NullPointerException("subject"); // NOI18N
        }
        this.subject = subject;
        this.callback = validator;
    }

    /** Invoked when a component gains the keyboard focus. */
    public void focusGained(FocusEvent e) {
        // Highlights the entire content of the field,
        // and puts the input caret at the end of the content.
        if (!e.isTemporary() && e.getComponent().equals(subject)) {
            int contentLength = subject.getDocument().getLength();
            subject.setCaretPosition(contentLength);
            subject.setSelectionStart(0);
            subject.setSelectionEnd(contentLength);
        }
    }

    /** Invoked when a component loses the keyboard focus. */
    public void focusLost(FocusEvent e) {
        // Focus leaving the subject component.
        // Validate the data contained in it.
        if (!e.isTemporary()) {
            if (callback != null && e.getComponent().equals(subject)) {
                callback.invoke(subject);
            }
        }
    }


    /** Callback interface. */
    public interface PostFocusCallback {
        void invoke(Component subject);
    }
}

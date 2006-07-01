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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide;

import javax.swing.JComponent;


/** The exception informs about fail in wizard panel validation and provides
 * a localized description what's wrong. Also can return JComponent which should
 * be focused to correct wrong values.
 *
 * @author  Jiri Rechtacek
 * @since 4.28
 */
final public class WizardValidationException extends Exception {
    private String localizedMessage;
    private JComponent source;

    /** Creates a new instance of WizardValidationException */
    private WizardValidationException() {
    }

    /**
     * Creates a new exception instance.
     * @param source component which should have focus to correct wrong values
     * @param message the detail message
     * @param localizedMessage description notifies an user what value must be corrected
     */
    public WizardValidationException(JComponent source, String message, String localizedMessage) {
        super(message);
        this.source = source;
        this.localizedMessage = localizedMessage;
    }

    /**
     *
     * @return JComponent for request focus to correct wrong values
     * or null if there is no useful component to focus it
     */
    public JComponent getSource() {
        return source;
    }

    /**
     *
     * @return description will notifies an user what value must be corrected
     */
    public String getLocalizedMessage() {
        return (localizedMessage != null) ? localizedMessage : this.getMessage();
    }
}

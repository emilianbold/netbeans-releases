/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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

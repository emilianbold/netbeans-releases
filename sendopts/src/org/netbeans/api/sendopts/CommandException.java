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

package org.netbeans.api.sendopts;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/** Signals that something is wrong when processing the command line arguments.
 *
 * @author Jaroslav Tulach
 */
public final class CommandException extends Exception {
    private final int exitCode;
    private final String locMsg;
    
    /** Simple constructor for the CommandException to indicate that a 
     * processing error occurred. The provided <code>exitCode</code> represents
     * the value to be usually send to as a return value to {@link System#exit}.
     * 
     * @param exitCode the value, should be different than zero
     */
    public CommandException(int exitCode) {
        this("Error code: " + exitCode, exitCode, null); // NOI18N
    }

    /** Creates new exception with a localised message assigned to it.
     * @param exitCode exit code to report from the exception
     * @param locMsg localised message
     */
    public CommandException(int exitCode, String locMsg) {
        this("Error code: " + exitCode, exitCode, locMsg); // NOI18N
    }
    
    
    /** Creates a new instance of CommandException */
    CommandException(String msg, int exitCode, String locMsg) {
        super(msg);
        this.exitCode = exitCode;
        this.locMsg = locMsg;
    }
    /** Creates a new instance of CommandException */
    CommandException(String msg, int exitCode) {
        this(msg, exitCode, null);
    }

    /** Returns an exit code for this exception.
     * @return integer exit code, zero if exited correctly
     */
    public int getExitCode() {
        return exitCode;
    }

    /** Localized message describing the problem that is usually printed
     * to the user.
     */
    public String getLocalizedMessage() {
        if (locMsg != null) {
            return locMsg;
        }
        if (getCause() != null) {
            return getCause().getLocalizedMessage();
        }
        return super.getLocalizedMessage();
    }
}

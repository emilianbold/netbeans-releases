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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.autoupdate;

/**
 *
 * @author Radek Matous
 */
public final class OperationException extends Exception {    
    private static ERROR_TYPE error;
    public static enum ERROR_TYPE {
        PROXY,
        INSTALLER,
        INSTALL,
        ENABLE,
        UNINSTALL,
    }       
 
    /** not public contructor */
    public OperationException (ERROR_TYPE error) {
        super (/*e.g.message from ERR*/);
        this.error = error;
    }
    
    /** not public contructor */
    public OperationException (ERROR_TYPE error, Exception x) {
        super (x);
        this.error = error;
    }
    
    /** not public contructor */
    public OperationException (ERROR_TYPE error, String message) {
        super (message);
        this.error = error;
    }
    
    public ERROR_TYPE getErrorType () {return error;}
    
}

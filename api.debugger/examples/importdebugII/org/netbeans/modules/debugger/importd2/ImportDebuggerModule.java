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

package org.netbeans.modules.debugger.importd2;

import org.netbeans.modules.debugger.*;
import org.openide.modules.ModuleInstall;

/**
* Module installation class for ImportDebugger Module
*
* @author Jan Jancura
*/
public class ImportDebuggerModule extends ModuleInstall {

    static final long serialVersionUID = -2272025566936120988L;

    private static ImportDebuggerImpl idi;

    /** Module installed for the first time. */
    public void installed () {
        restored ();
    }

    /** Module installed again. */
    public void restored () {
        try {
            Register.registerDebuggerImpl (
                idi = new ImportDebuggerImpl ()
            );
        } catch (Exception e) {
        }
    }

    /** Module was uninstalled. */
    public void uninstalled () {
        try {
            Register.unregisterDebuggerImpl (
                idi
            );
            idi = null;
        } catch (RuntimeException e) {
        }
    }
    
    /**
     * Return type of debugger which should be used to debug this DebuggerInfo.
     *
     * @return type of debugger which should be used to debug this DebuggerInfo
     */
    public static DebuggerImpl getDebuggerImpl () {
        return idi;
    }
}

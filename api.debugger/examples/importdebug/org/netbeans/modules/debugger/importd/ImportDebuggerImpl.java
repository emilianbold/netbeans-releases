/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.importd;

import org.openide.debugger.Debugger;

import org.netbeans.modules.debugger.*;


/**
 * This class represents JPDA Debugger Implementation.
 *
 * @author Jan Jancura
 */
public class ImportDebuggerImpl extends DebuggerImpl {
    
    
    /**
     * Returns displayable name of JPDA debugger.
     *
     * @return displayable name of JPDA debugger
     */
    public  String getDisplayName () {
        return ImportDebugger.getLocString ("CTL_Import_Debugger");
    }

    /**
     * Returns a new instance of Debugger.
     */
    public Debugger createDebugger () {
        return new ImportDebugger ();
    }
}


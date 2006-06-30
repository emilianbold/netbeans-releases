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

package org.netbeans.modules.debugger.importd;

import org.openide.TopManager;
import org.openide.execution.ExecInfo;
import org.openide.debugger.DebuggerType;
import org.openide.debugger.DebuggerException;
import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

import org.netbeans.modules.debugger.AbstractDebuggerType;


/**
* Default debugger type for Import debugger.
*/
public class ImportDebuggerType extends AbstractDebuggerType {

    static final long serialVersionUID = 5234304898551299437L;

    /* Gets the display name for this debugger type. */
    public String displayName () {
        return ImportDebugger.getLocString ("CTL_Import_Debugger_Type");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ImportDebuggerType.class);
    }

    /* Starts the debugger. */
    public void startDebugger (ExecInfo info, boolean stopOnMain) 
    throws DebuggerException {
        TopManager.getDefault ().getDebugger ().startDebugger (
            new ImportDebuggerInfo (
                info.getClassName (),
                info.getArguments (),
                stopOnMain ? info.getClassName () : null
            )
        );
        return;
    }
    
    /**
     * Should return <code>true</code> if this DebuggerType supports debugging
     * of given {@link org.openide.loaders.DataObject}.
     *
     * @param obj DataObject to test
     * @return <code>true</code> if this DebuggerType supports debugging
     * of given {@link org.openide.loaders.DataObject}
     */
    public boolean supportsDebuggingOf (DataObject obj) {
        return obj.getPrimaryFile ().getMIMEType ().equals ("text/x-java");
    }
    
    /**
     * Starts debugging for a dataobject. Debugging should stop on given line.
     * This method is called from RunToCursorAction.
     *
     * @param obj object to run
     * @param stopOnLine should the debugging stop on given line or go to
     * first breakpoint (if stopOnLine == <code>null</code>)
     * @exception DebuggerException if debugger is not installed or cannot
     * be started
     */
    public void startDebugger (DataObject obj, Line stopOnline) throws DebuggerException {
        startDebugger (obj, false);
    }
    
}

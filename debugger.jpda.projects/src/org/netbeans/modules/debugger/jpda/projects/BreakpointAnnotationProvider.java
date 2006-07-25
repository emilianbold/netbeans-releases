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

package org.netbeans.modules.debugger.jpda.projects;

import org.netbeans.api.debugger.DebuggerManager;
import org.openide.filesystems.FileObject;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Lookup;


/**
 * This class is called when some file in editor is openend. It changes if
 * some LineBreakpoints with annotations should be readed.
 *
 * @author Jan Jancura
 */
public class BreakpointAnnotationProvider implements AnnotationProvider {


    public void annotate (Line.Set set, Lookup lookup) {
        FileObject fo = (FileObject) lookup.lookup (FileObject.class);
        if (fo == null) return;
        // This loads, as a side-effect, the breakpoints and displays them
        // in Editor.
        DebuggerManager.getDebuggerManager ().getBreakpoints ();
    }
    
}

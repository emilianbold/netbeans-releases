/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelimpl;

import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {
    
    public interface Startupable {
	public void startup();
	public void shutdown();
	public void unload();
    }
    
    public void restored() {
        // By default, do nothing.
        // Put your startup code here.
	if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("=== Installer.restored");
	CsmModel model = CsmModelAccessor.getModel();
	if( model instanceof Startupable ) {
	    ((Startupable) model).startup();
	}
	super.restored();
    }

    public void close() {
        super.close();
	if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("=== Installer.close");
	CsmModel model = CsmModelAccessor.getModel();
	if( model instanceof Startupable ) {
	    ((Startupable) model).shutdown();
	}
    }
   
    public void uninstalled() {
	if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("=== Installer.uninstalled");
	CsmModel model = CsmModelAccessor.getModel();
	if( model instanceof Startupable ) {
	    ((Startupable) model).unload();
	}
	super.uninstalled();
    }
}

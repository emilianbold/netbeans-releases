/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl;

import javax.swing.SwingUtilities;
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
    
    @Override
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

    @Override
    public void close() {
        super.close();
	if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("=== Installer.close");
	final CsmModel model = CsmModelAccessor.getModel();
	if( model instanceof Startupable ) {
            runTask(new Runnable() {
                public void run() {
                    ((Startupable) model).shutdown();
                }
            }, "close ModelImpl"); // NOI18N
	}
    }
   
    @Override
    public void uninstalled() {
	if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("=== Installer.uninstalled");
	final CsmModel model = CsmModelAccessor.getModel();
	if( model instanceof Startupable ) {
	    runTask(new Runnable() {
                public void run() {
                    ((Startupable) model).unload();
                }
            }, "uninstall ModelImpl"); // NOI18N
	}
	super.uninstalled();
    }
    
    private void runTask(Runnable task, String name) {
        if (SwingUtilities.isEventDispatchThread()) {
            new Thread(task, name).run();
        } else {
            task.run();
        }        
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.cnd.otool.debugger.api;

import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.debugger.Breakpoint;

/**
 *
 * @author Nikolay Koldunov
 */
abstract public class OtoolNativeBreakpoint extends Breakpoint implements java.io.Serializable {

    private volatile boolean enabled = true;

    private final AtomicInteger id = new AtomicInteger();
    private OtoolNativeDebugger debugger;
    private Context context;
    private boolean isEnabled;


    protected OtoolNativeBreakpoint () {

    }


    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


   public final void unbind() {
//	assert isMidlevel();

	// We don't null the context ... we become a ghost
//	for (OtoolNativeBreakpoint c : g) {
//	    c.setDebugger(null);
//	    c.setHandler(null);
//	    c.update();
//	}
	this.setDebugger(null);
	//this.update();
    }

    public void bindTo(OtoolNativeDebugger debugger) {
	//assert isSubBreakpoint() || isMidlevel();

//	assert getDebugger() == null || getDebugger() == debugger :
//	       "NativeBreakpoint.bindTo(): already have a debugger"; // NOI18N
	final String executable = debugger.getNDI().getTarget();
	final String hostname = debugger.getExecEnv().getHost();
	setContext(new Context(executable, hostname));
	setDebugger(debugger);

//	// OLD getParent().setEnabled(getParent().recalculateIsEnabled());
//	updateAndParent();
    }
    public final void setContext(Context newContext) {
	context = newContext;

//	if (isMidlevel()) {
//	    for (NativeBreakpoint c : getChildren())
//		c.context.set(newContext);
//	}
    }

    private void setDebugger(OtoolNativeDebugger debugger) {
	// See comments in cleanup()
	//assert !isToplevel() : "Cannot setDebugger() on toplevel bpt";
	this.debugger = debugger;
    }    
    
    public final void setId(int newId) {
	id.set(newId);
    }
    
    public final int getId() {
        return id.get();
    }
    
    public final void setEnabled (boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

        
}

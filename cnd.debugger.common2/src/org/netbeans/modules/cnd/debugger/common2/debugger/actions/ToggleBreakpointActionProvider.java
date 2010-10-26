/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import org.openide.text.Line;

import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.RoutingToken;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.Handler;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointBag;
import org.netbeans.spi.debugger.ActionsProvider;


/*
 * ToggleBreakpointActionProvider
 *
 * Modelled on
 *	org.netbeans.modules.debugger.jpda.ui.actions.ToggleBreakpointAction
 * was: ToggleBreakpointSupport (or ToggleLinePerformer?)
 */
@ActionsProvider.Registration
public class ToggleBreakpointActionProvider extends NativeActionsProvider {
    
    public ToggleBreakpointActionProvider() {
	super(null);
	setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
    }

    public ToggleBreakpointActionProvider(ContextProvider ctxProvider) {
	super(ctxProvider);
	setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
    }

    /* LATER
    public String getName() {
	// SHOULD use getText() (aka getString())
	return Catalog.get("ToggleBreakpointAction");
    }
    */

    /* interface ActionsProvider */
    public Set getActions() {
	return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    private boolean checkTarget() {
	if (! DebuggerManager.isPerTargetBpts())
	    return true;	// we always accept them for global bpts

	NativeDebugger debugger = getDebugger();

	if (debugger == null || !debugger.state().isLoaded) {
	    DebuggerManager.errorLoadBeforeBpt();
	    return false;
	} else {
	    return true;
	}

    }

    /* interface ActionsProvider */
    public void doAction(Object action) {
	NativeDebugger debugger = getDebugger();
	
	Line l = EditorBridge.getCurrentLine();

	// 6502318
	if (l == null)
	    return;

	String fileName = EditorBridge.filenameFor(l);
	int lineNo = l.getLineNumber();

	if (ignoreJava && fileName.indexOf(".java") > 0) { // NOI18N
	    // Ignore toggles in .java files because if the jpda debugger
	    // is on we'll get two breakpoints.
	    return;
	}

	if (!checkTarget())
	    return;

	BreakpointBag bb = DebuggerManager.get().breakpointBag();

	NativeBreakpoint bpt =
	    bb.locateBreakpointAt(fileName, lineNo, debugger);

	int routingToken = RoutingToken.BREAKPOINTS.getUniqueRoutingTokenInt();

	if (bpt != null) {
	    // toggle off
	    bpt.dispose();
	} else {
	    // toggle on
	    bpt = NativeBreakpoint.newLineBreakpoint(fileName, lineNo);
	    if (bpt != null)
		Handler.postNewHandler(debugger, bpt, routingToken);
	}
    }

    /* interface NativeActionsProvider */
    public void update(State state) {
	// always enabled
    }

    //
    // Ignore java is jpda debugger is loaded.
    //
    // We ...
    // - At this classes load time check for jpdas presense.
    // - Setup a Lookup.Result to learn of module loading and unloading
    //   and check for jpdas presense on eache vent.
    // - If the module is loaded listen to it's enable property getting 
    //   enabled or disabled.

    private static boolean ignoreJava = false;

    private static final String jpdaModuleName =
	"org.netbeans.modules.debugger.jpda";		// NOI18N

    /**
     * See if the jpda debugger is loaded or not and set 'ignoreJava'.
     */
    private static void checkForJpdaDebugger() {
	if (Log.Action.jpdaWatcher)
	    System.out.printf("checkForJpdaDebugger #######################\n"); // NOI18N

	Collection<? extends ModuleInfo> moduleInfos =
	    Lookup.getDefault().lookupAll(ModuleInfo.class);

	ignoreJava = false;
	String jdbx = System.getProperty("spro.jdbx");

	for (ModuleInfo moduleInfo : moduleInfos) {
	    if (moduleInfo.getCodeNameBase().equals(jpdaModuleName)) {
		if (moduleInfo.isEnabled() &&  jdbx != null && !jdbx.equals("on")) { // NOI18N
		    ignoreJava = true;
		}

		// Arrange to get notified when the module is enabled
		// or disabled.

		moduleInfo.addPropertyChangeListener(new PropertyChangeListener() {
		    public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(ModuleInfo.PROP_ENABLED )) {
			    if ( ((boolean) (Boolean) evt.getNewValue()) == true ) {
				ignoreJava = true;
			    } else {
				ignoreJava = false;
			    }
			    if (Log.Action.jpdaWatcher)
				System.out.printf("\tignoreJava -> %s\n", ignoreJava); // NOI18N
			}
		    }
		} );
	    }
	}

	if (Log.Action.jpdaWatcher)
	    System.out.printf("\tignoreJava = %s\n", ignoreJava); // NOI18N
    }

    static {
	// Initial check

	checkForJpdaDebugger();

	// Arrange to get notified when modules get loaded or unloaded

	final Lookup.Result<ModuleInfo> result =
	    Lookup.getDefault().lookupResult(ModuleInfo.class);
	result.addLookupListener(new LookupListener() {
	    public void resultChanged(LookupEvent event) {
		checkForJpdaDebugger();
	    }
	});
    }
}

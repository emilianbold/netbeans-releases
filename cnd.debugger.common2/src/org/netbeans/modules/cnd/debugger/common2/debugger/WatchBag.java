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

/*
 * "watchBag.java"
 */

package org.netbeans.modules.cnd.debugger.common2.debugger;

import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;
import java.util.ArrayList;

public class WatchBag {
    private ArrayList<NativeWatch> watches = new ArrayList<NativeWatch>();

    private boolean dirty;

    private boolean initialized;
    private boolean isRestoring;

    private static DebuggerManager manager() {
	return DebuggerManager.get();
    }

    public WatchBag() {
    }

    // 6600130
    public boolean isRestoring() {
	return isRestoring;
    }

    private void initialize() {

	// Fix for 6589755
	// We need to call (debuggercores) getWatches() which will
	// trigger the loading of watches. (DebuggerManager.initWatches()).

	if (initialized)
	    return;
	if (Log.Watch.pathway)
	    System.out.printf("WatchBag.initialize() initializing\n"); // NOI18N

	// 6600130
	isRestoring = true;
	manager().getWatches();
	isRestoring = false;
	initialized = true;
    }

    ModelChangeDelegator watchUpdater() {
	return manager().watchUpdater();
    }

    public NativeWatch[] getWatches() {
	initialize();
	NativeWatch[] wa = new NativeWatch[watches.size()];
	return watches.toArray(wa);
    }

    public WatchVariable[] watchesFor(NativeDebugger debugger) {
	initialize();
	ArrayList<WatchVariable> ws = new ArrayList<WatchVariable>();
	for (NativeWatch w : watches) {
	    WatchVariable dw = w.findByDebugger(debugger);
	    if (dw != null) {
                ws.add(dw);
            }
	}
	return ws.toArray(new WatchVariable[ws.size()]);
    }

    public void postDeleteAllWatches() {

	// Use an array because
	// 	for (WatchVariable w : subWatches)
	// will run into ConcurrentModificationException's
	
	NativeDebugger debugger = DebuggerManager.get().currentNativeDebugger();

	for (NativeWatch w : getWatches()) {
	    WatchVariable dw = w.findByDebugger(debugger);
	    if (dw != null) {
		w.postDelete(false);
            }
	}
	    
    }

    /**
     * Called back when we restore a bag from XML.
     * At that point there may be no debugger or updater.
     * All such restored watches get re-add'ed later on so only need to put
     * them on the list.
     */
    public void restore(NativeWatch newWatch) {
	assert !watches.contains(newWatch) :
	       "WB.restore(): watch added redundantly"; // NOI18N
	// LATER newWatch.restored();
	watches.add(newWatch);
	// OLD manager().addWatch(newWatch);
	newWatch.setUpdater(watchUpdater());
    }

    public void add(NativeWatch newWatch) {
	assert !watches.contains(newWatch) :
	       "WB.add(): watch added redundantly"; // NOI18N
	watches.add(newWatch);
	// OLD manager().addWatch(newWatch);
	newWatch.setUpdater(watchUpdater());
	watchUpdater().treeChanged();      // causes a pull

	dirty = true;
    }

    public void remove(NativeWatch oldWatch) {
	if (oldWatch == null)
	    return;

	oldWatch.cleanup();
	boolean removed = watches.remove(oldWatch);
	assert removed :
	       "WB.remove(): watch to be removed not in bag"; // NOI18N
	assert !watches.contains(oldWatch) :
	       "WB.remove(): watch still there after removal"; // NOI18N
	// OLD manager().removeWatch(oldWatch);
	watchUpdater().treeChanged();      // causes a pull

	dirty = true;
    }

    private static final String moduleFolderName = "DbxGui";	// NOI18N
    private static final String folderName = "DbxDebugWatches";	// NOI18N
    private static final String filename = "Watches";		// NOI18N

    private static final UserdirFile userdirFile =
	new UserdirFile(moduleFolderName, folderName, filename);

    public void restore() {
	/*
	No-op since we're depending on debuggercore Watches

	if (!Log.Watch.wall) {
	    if (Log.Watch.xml)
		System.out.printf("WatchBag.restore() skipping\n");
	    return;
	}

	if (Log.Watch.xml)
	    System.out.printf("WatchBag.restore()\n");

	WatchXMLReader xr = new WatchXMLReader(userdirFile, this);
	try {
	    xr.read();
	} catch (Exception x) {
	    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, x);
	}
	if (Log.Watch.xml)
	    System.out.printf("WatchBag.restore() DONE\n");
	watchUpdater().treeChanged();      // causes a pull
	*/
    }

    public void save() {
	/*
	No-op since we're depending on debuggercore Watches

	if (!Log.Watch.wall) {
	    if (Log.Watch.xml)
		System.out.printf("WatchBag.save() skipping\n");
	    return;
	}

	if (Log.Watch.xml)
	    System.out.printf("WatchBag.save()\n");

	if (!isDirty()) {
	    if (Log.Watch.xml)
		System.out.printf("\tnot dirty --- nothing to save\n");
	    return;
	} else {
	    if (Log.Watch.xml)
		System.out.printf("\tdirty --- proceeding\n");
	}

	WatchXMLWriter xw = new WatchXMLWriter(userdirFile, this);
	try {
	    xw.write();
	    clearDirty();
	} catch (Exception e) {
	    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
	}
	if (Log.Watch.xml)
	    System.out.printf("WatchBag.save() DONE\n");
	*/
    }

    public boolean isDirty() {
	if (dirty)
	    return true;
	/* LATER
	for (Watch w : watches)
	    if (w.isDirty())
		return true;
	*/
	return false;
    }

    public void clearDirty() {
	dirty = false;
	/* LATER
	for (Watch w : watches)
	    w.clearDirty();
	*/
    }

}

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

package org.netbeans.modules.cnd.otool.debugger.api;

import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.spi.debugger.ContextProvider;



/**
 * Our prallel object to debuggercore Sessions.
 *
 * Stores information to be presented in sessions view which is accessed
 in SessionFilter.

 Registered in
	META-INF/debugger/
	org.netbeans.modules.cnd.debugger.common2.debugger.OtoolNativeSession
 This one is unusual in that it registers itself in itself.
 */

public final class OtoolNativeSession {

    private ContextProvider ctx;
    private Session coreSession;
    private OtoolNativeDebugger debugger;

    private static int nextSerialNumber = 0;
    private int serialNumber = 0;


    public OtoolNativeSession(ContextProvider ctx) {
	this.ctx = ctx;
	coreSession = ctx.lookupFirst(null, Session.class);
        debugger = ctx.lookupFirst(null, OtoolNativeDebugger.class);

	serialNumber = nextSerialNumber++;

	// the following relies crucially on
	// DebuggerManager.registerSessionModel() having been called
	// already.
	//setUpdater(OtoolNativeDebuggerManager.get().sessionUpdater());
    } 

    // point back to debugger
    public void setDebugger(OtoolNativeDebugger nd) {
	debugger = nd;
    }

    public OtoolNativeDebugger getDebugger() {
        return debugger;
    }
    
    /**
     * Get [debugger]core session corresponding to us.
     */
    public Session coreSession() {
	return coreSession;
    } 

    /**
     * Map a debuggercore Session to one of ours
     * @param coreSession
     * @return 
     */

    public static OtoolNativeSession map(Session coreSession) {
	return coreSession.lookupFirst(null, OtoolNativeSession.class);
    }


//    public void update() {
//	// OLD updater.treeNodeChanged(coreSession);
//	updater.modelChanged(new ModelEvent.NodeChanged(this, coreSession));
//    } 


    /**
     * Switch to this session
     */

    public void makeCurrent() {
	OtoolNativeDebuggerManager.get().setCurrentSession(coreSession);
    }

    public void kill() {
	coreSession.kill();
    }


    /**
     * Is this session target name unique between all sessions?
     */

    private boolean isUnique() {
	OtoolNativeSession sessions[] = OtoolNativeDebuggerManager.get().getSessions();
	String thisName = getShortName();
	boolean programUnique = true;
	for (int sx = 0; sx < sessions.length; sx++) {
	    OtoolNativeSession thatSession = sessions[sx];
	    if (thatSession != this) {
		if (sameString(thisName, thatSession.getShortName())) {
		    programUnique = false;
		    break;
		}
	    }
	}
	return programUnique;
    }

    /**
     * Same as String.equals, but allows arguments to be null
     */
    private static boolean sameString(String a, String b) {
	if (a == null) {
	    return (b == null);
	} else if (b == null) {
	    return false;
	} else {
	    return a.equals(b);
	}
    }    
    //
    // Attributes
    //

    /**
     * Return a uniquified short session name, suitable for the Name column.
     */

    // "interface" Session
    public String getName() {
	String name = getShortName();
	if (!isUnique())
	    name += " # " + serialNumber; // NOI18N
	return name;
    }

    @Override
    public String toString() {
	String s = "";				// NOI18N
	if (target != null)
	    s += target;
	else
	    s += "-";				// NOI18N

	if (corefile != null) {
	    s += " " + corefile + " ";		// NOI18N
	}
	if (pid != -1) {
	    s += " (" + pid + ")";		// NOI18N
	}
	return s;
    } 

    private EngineType engine = null;
    public final void setSessionEngine(EngineType s) {
	engine = s;
    }

    public final EngineType getSessionEngine() {
	return engine;
    }


    // get core file location if this session is debugging core file
    public String getSessionCore() {
	OtoolNativeDebuggerInfo ddi = debugger.getNDI();

	if (ddi != null) {
            return ddi.getCorefile();
        } else {
            return "";
        }

    }

    public String getSessionLocation() {
	return getTarget();
    /*
	NativeDebuggerInfo ddi = debugger.getNDI();

	if (ddi != null) 
	   return ddi.getTarget();
	else 
	   return "";
   */
    }

    public String getSessionArgs() {
	OtoolNativeDebuggerInfo ddi = debugger.getNDI();

	if (ddi != null) 
	   return ddi.getArgsFlat();
	else 
	   return "";
    }
    
   

    private long pid = -1;
    public long getPid() {
	return pid;
    }
    public void setPid(long pid) {
	this.pid = pid;
	//update();
    } 

    private String target;
    public String getTarget() {
	if (target == null)
	    return "-"; // NOI18N
	else 
	    return target;
    } 
    public void setTarget(String target) {
	this.target = target;

	shortName = null;	// reset cache

	// force a re-rendering of the _whole_ tree becauase unique names
	// might get affected by this.
//	if (updater != null) {
//	    // OLD updater.treeChanged();
//	    updater.modelChanged(new ModelEvent.TreeChanged(this));
//	}
    } 

    private String shortName = null;
    public String getShortName() {
	if (target == null) {
            return "";
        } else {
	    if (shortName == null) {
                shortName = CndPathUtilities.getBaseName(target);
            }
	    return shortName;
	}
    }

    private String corefile;
    public String getCorefile() {
	return corefile;
    }
    public void setCorefile(String corefile) {
	this.corefile = corefile;
    }

    /* LATER
    private String currentLanguage;
    public String getCurrentLanguage() {
	return "unknown";
    } 

    public void setCurrentLanguage() {
	this.currentLanguage = currentLanguage;
    }
    */

    public void setSessionHost(String h) {
	hostName = h;
    }


    private String hostName;
    public String getSessionHost() {
	if (hostName == null || hostName.trim().isEmpty()) {
            return "localhost"; // NOI18N
        } else {
            return hostName;
        }
    }
}

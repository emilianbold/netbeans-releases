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
 * File         : RoundTripSource.java
 * Version      : 1.0
 * Description  : Base class for classes that source round trip events.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.ElementInfo;
import org.netbeans.modules.uml.integration.ide.events.EventFilter;
import org.netbeans.modules.uml.integration.ide.events.EventManager;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;

/**
 * Base class for classes that source round trip events.
 */
abstract public class RoundTripSource {
    private UMLSupport umlsupport = UMLSupport.getUMLSupport();
    private EventFilter filter = 
        EventManager.getEventManager().getEventFilter();

    /**
     * If <code>true</code>, we switch the default project to the newly changed
     * project before firing model-source events.
     */
    private static boolean projectSwitchingEnabled = false;

    protected void queue(Runnable r) {
        umlsupport.getRoundtripQueue().queueRunnable(r);
    }

    protected boolean isValidEventType(int type) {
        return filter.isValidEventType(type);
    }

    protected boolean isValidEvent(ElementInfo el, ClassInfo parent) {
        return filter.isValid(el, parent);
    }

    protected boolean isValidEvent(INamedElement el) {
        return !eventsBlocked && filter.isValid(el);
    }

    protected void scheduleForNavigation(IElement el) {
        try {
            EventFrameworkSink sink = UMLSupport.getUMLSupport()
                                    .getSinkManager()
                                    .getEventFrameworkSink();
            if (sink != null)
                sink.navigateLater(el);
        } catch (Exception ignored) { }
    }
    
    /**
     * Sets the default Describe project to the project that owns the Describe
     * model element specified by the given ElementInfo, and calls the IDE
     * manager to activate the appropriate IDE project. The IDE manager may
     * choose to veto the roundtrip event here.
     * 
     * @param elementInfo The ElementInfo that we're firing an event for.
     */
    protected void setDefaultProject(ElementInfo elementInfo) {
        if (projectSwitchingEnabled && elementInfo != null) {
            UMLSupport.setDefaultProject(elementInfo.getProject());
            if (!UMLSupport.getUMLSupport().getIDEManager()
                    .activateIDEProject(UMLSupport.getDefaultProject()))
                throw new RoundtripVetoException("");
        }
    }
    
    protected void setDefaultProject(MethodInfo elementInfo) {
        if (projectSwitchingEnabled && elementInfo != null) {
            UMLSupport.setDefaultProject(elementInfo.getProject());
            if (!UMLSupport.getUMLSupport().getIDEManager()
                    .activateIDEProject(UMLSupport.getDefaultProject()))
                throw new RoundtripVetoException("");
        }
    }
	
    /**
     * Sets whether switching of the default project during roundtrip is 
     * enabled. If enabled, before each model-source event, the default project
     * will be set to the project where changes to the model have occurred.
     * 
     * @param enabled <code>true</code> to turn on default project switching.
     *                Default project switching is disabled by default.
     */
    public static void setProjectSwitchingEnabled(boolean enabled) {
        projectSwitchingEnabled = enabled;
    }

    public static abstract class RoundtripThread implements Runnable {
        public void run() {
            try {
                EventManager.setRoundTripActive(true);
                work();
            } finally {
                EventManager.setRoundTripActive(false);
            }
        }

        abstract public void work();
    }
    
    public static class RoundtripVetoException extends RuntimeException {
        public RoundtripVetoException() {
        }
        
        public RoundtripVetoException(String message) {
            super(message);
        }
    }

    public static void setEventsBlocked(boolean blocked) {
        eventsBlocked = blocked;
    }

    private static boolean eventsBlocked = false;
}

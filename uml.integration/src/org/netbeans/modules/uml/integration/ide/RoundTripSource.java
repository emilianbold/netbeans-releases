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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
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
 * File         : EventFrameworkSink.java
 * Version      : 1.2
 * Description  : Listener for special context events - like batch context etc.
 * Authors      : Sumitabh
 */
package org.netbeans.modules.uml.integration.ide;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 *  Listener for special context events - like batch context etc.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-09-16  Sumitabh    Created.
 *
 * @author  Sumitabh
 * @version 1.2
 */
public class EventFrameworkSink extends SourceNavigable
                                implements IEventFrameworkEventsSink {

    public void onPreEventContextPushed(IEventContext iEventContext, IResultCell iResultCell) {
    }
    
    public void onEventContextPushed(IEventContext iEventContext, IResultCell iResultCell) {
        navigate       = true;
        navigateTarget = null;
        //ClassInfo.eraseRefClasses();
    }
    
    public void onPreEventContextPopped(IEventContext iEventContext, IResultCell iResultCell) {
    }
    
    public void onEventContextPopped(IEventContext iEventContext, IResultCell iResultCell) {
        if (navigate && navigateTarget != null) {
            final IElement target = navigateTarget;
            Runnable r = new Runnable() {
                public void run() {
                    Log.out("Firing navigate event!");
                    fireNavigateEvent(target);
                }
            };
            UMLSupport.getUMLSupport().getRoundtripQueue().queueRunnable(r);
            navigate       = false;
            navigateTarget = null;
        }
        ClassInfo.eraseRefClasses();
    }
    
    public void onEventDispatchCancelled(ETList<Object> listeners, Object cancellor, IResultCell iResultCell) {
        ClassInfo.eraseRefClasses();
    }

    public void navigateLater(IElement target) {
        if (navigateTarget != null && target != null) {
            navigate = false;
            return ;
        }
        navigateTarget = target;
    }

    private IElement navigateTarget = null;
    private boolean  navigate       = false;
}
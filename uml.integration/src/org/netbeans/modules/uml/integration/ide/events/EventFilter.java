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
 * File         : EventFilter.java
 * Version      : 1.0
 * Description  : Filters model-source roundtrip events to eliminate events
 *                caused by the source-model roundtrip.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide.events;

import java.util.HashSet;
import java.util.Hashtable;

import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 *  Filters model-source roundtrip events to eliminate events caused by the
 * source-model roundtrip.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *  1   2002-07-15  Darshan     Fixed filter ids using $ as a name separator
 *                              while the check uses . as the separator.
 */
public class EventFilter {
    private Hashtable blockedEvents = new Hashtable();
    private HashSet   blockedEventTypes = new HashSet();

    public boolean isValid(ElementInfo element, ClassInfo parent) {
        String eventId = getEventId(element, parent);
        return (eventId != null && !isBlocked(eventId));
    }

    public boolean isValid(INamedElement named) {
        String eventId = getEventId(named);
        return (eventId != null && !isBlocked(eventId));
    }

    public boolean isValidEventType(int type) {
        return !blockedEventTypes.contains(new Integer(type)) &&
                     type != ChangeUtils.RDT_SOURCE_DIR_CHANGED;
    }

    protected String getEventId(ElementInfo element, ClassInfo parent) {
        if (element == null)
            return null;

        try {
            StringBuffer buf = new StringBuffer();
            buf = buf.append(element.getName());
            if(parent != null)
              buf.append("%").append(parent.getId());
            return buf.toString();
        } catch (NullPointerException e) {
            Log.stackTrace(e);
        }
        return null;
    }

    public String getEventId(INamedElement element) {
        if (element == null)
            return null;

        IElement parent = element.getOwner();
        try {
            String id = element.getName() + "%";
            if (parent != null && parent instanceof INamedElement)
                id += parent.getXMIID();
            return id;
        } catch (Exception ex) {
            Log.stackTrace(ex);
        }
        return null;
    }

    public void blockEvents(ElementInfo element, ClassInfo parent) {
        String eventId = getEventId(element, parent);
        if (eventId != null) {
            Log.out("Blocking events for id " + eventId);
            blockedEvents.put(eventId, eventId);

            // If the element name has been changed, also block events for the
            // new name.
            String elementName = element.getName(),
                   newName     = element.getNewName();

            if (newName != null && !elementName.equals(newName)) {
                element.setName(newName);
                eventId = getEventId(element, parent);
                if (eventId != null)
                    blockedEvents.put(eventId, eventId);
                element.setName(elementName);
            }
        }
    }

    public void blockEventType(int eventType) {
        blockedEventTypes.add(new Integer(eventType));
    }

    public void unblockEventType(int eventType) {
        blockedEventTypes.remove(new Integer(eventType));
    }

    public void unblockEvents(ElementInfo element, ClassInfo parent) {
        String eventId = getEventId(element, parent);
        if (eventId != null) {
            Log.out("Unblocking events for id " + eventId);
            blockedEvents.remove(eventId);

            // If the element name has been changed, also block events for the
            // new name.
            String elementName = element.getName(),
                   newName     = element.getNewName();

            if (newName != null && !elementName.equals(newName)) {
                element.setName(newName);
                eventId = getEventId(element, parent);
                if (eventId != null)
                    blockedEvents.remove(eventId);
                element.setName(elementName);
            }
        }
    }

    public void unblockAll() {
        blockedEvents.clear();
    }

    protected String getElementType(ElementInfo element) {
        return element.getCode();
    }

    protected String getElementType(INamedElement nel) {
        if (nel instanceof IAttribute)
            return "F";
        else if (nel instanceof IOperation)
            return "M";
        else
            return "C";
    }

    protected boolean isBlocked(String eventId) {
        boolean blocked = (eventId == null? true : blockedEvents.containsKey(eventId));
        //Log.out("Event (" + eventId + ") is " + (blocked? "blocked" : "valid"));
        return blocked;
    }
}

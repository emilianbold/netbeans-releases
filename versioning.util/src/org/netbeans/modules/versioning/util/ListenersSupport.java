/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.versioning.util;

import java.util.*;

/**
 * Support for firing versioning events.
 *
 * @author Maros Sandor
 */
public class ListenersSupport {

    private final Object    source;
    private HashSet         listeners = new HashSet(1);

    public ListenersSupport(Object source) {
        this.source = source;
    }

    public synchronized void addListener(VersioningListener listener) {
        HashSet copy = (HashSet) listeners.clone();
        copy.add(listener);
        listeners = copy;
    }

    public synchronized void removeListener(VersioningListener listener) {
        HashSet copy = (HashSet) listeners.clone();
        copy.remove(listener);
        listeners = copy;
    }
    
    public void fireVersioningEvent(Object eventID) {
        fireVersioningEvent(new VersioningEvent(source, eventID, null));
    }    

    public void fireVersioningEvent(Object eventID, Object param) {
        fireVersioningEvent(new VersioningEvent(source, eventID, new Object [] { param }));
    }    
    
    public void fireVersioningEvent(Object eventID, Object [] params) {
        fireVersioningEvent(new VersioningEvent(source, eventID, params));
    }    
    
    private void fireVersioningEvent(VersioningEvent event) {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            VersioningListener listener = (VersioningListener) i.next();
            listener.versioningEvent(event);
        }
    }
}

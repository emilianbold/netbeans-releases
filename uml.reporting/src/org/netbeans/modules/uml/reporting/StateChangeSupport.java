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

package org.netbeans.modules.uml.reporting;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.*;

/**
 *
 * @author whd
 */
public class StateChangeSupport {


    Vector changeListeners = new Vector();
    Object owner = null;
    public StateChangeSupport(Object owner) {
        this.owner = owner;
    }

    public void addListener(ChangeListener listener) {
        changeListeners.add(listener);
    }
    public void removeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    
    public void fireStateChanged() {
        ChangeEvent event=new ChangeEvent(this);
        Iterator i = changeListeners.iterator();
        while (i.hasNext()) {
            
            ((ChangeListener)i.next()).stateChanged(event);
            
        }
    }
    
    
    
}

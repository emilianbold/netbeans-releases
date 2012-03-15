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
package org.netbeans.modules.web.javascript.debugger;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.ModelEvent.TreeChanged;


/**
 * @author ads
 *
 */
public abstract class ViewModelSupport {
    
    protected ViewModelSupport() {
        myListeners = new CopyOnWriteArrayList<ModelListener>();
    }

    public void addModelListener(ModelListener l) {
        myListeners.add(l);
    }

    public void removeModelListener(ModelListener l) {
        myListeners.remove(l);
    }

    protected void refresh() {
        fireChangeEvent(new TreeChanged(this));
    }
    
    protected void fireChangeEvent(ModelEvent modelEvent) {
        for ( ModelListener listener : myListeners ) {
            listener.modelChanged(modelEvent);
        }
    }
    
    protected void fireChangeEvents(ModelEvent[] events) {
        for( ModelEvent event : events ){
            fireChangeEvent( event );
        }
    }
    
    protected void fireChangeEvents(Collection<ModelEvent> events) {
        for( ModelEvent event : events ){
            fireChangeEvent( event );
        }
    }
    
    
    private CopyOnWriteArrayList<ModelListener> myListeners;
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.ui.tnv.impl;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.soa.ui.tnv.api.ObservableChangeTracker;
import org.netbeans.modules.soa.ui.tnv.api.ObservableChangeTracker.UpdateListener;

/**
 *
 * @author nk160297
 */
public class ComponentChangeTracker implements ObservableChangeTracker {

    private List<UpdateListener> listeners = new ArrayList<UpdateListener>();
    
    public ComponentChangeTracker(JScrollPane scrollPain) {
        scrollPain.getViewport().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fireUpdateMessage();
            }
        });
        
//        myComp.addComponentListener(new ComponentListener() {
//            public void componentHidden(ComponentEvent e) {
//                fireUpdateMessage();
//            }
//            public void componentMoved(ComponentEvent e) {
//                fireUpdateMessage();
//            }
//            public void componentResized(ComponentEvent e) {
//                fireUpdateMessage();
//            }
//            public void componentShown(ComponentEvent e) {
//                fireUpdateMessage();
//            }
//        });
    }

    public void dispose() {
        listeners.clear();
    }
    
    public void addUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }
    
    public void removeUpdateListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    private void fireUpdateMessage() {
        for (UpdateListener listener : listeners) {
            listener.observableChanged();
        }
    }
    
}

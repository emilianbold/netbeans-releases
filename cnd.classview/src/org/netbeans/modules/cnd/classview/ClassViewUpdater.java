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

package org.netbeans.modules.cnd.classview;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.model.*;

import org.openide.nodes.Node;

/**
 * Deals with class view model updates
 * @author vk155633
 */
public class ClassViewUpdater implements Runnable {
    
    private static class BlockingQueue {
        
        private LinkedList data = new LinkedList();
        
        private Object lock = new Object();
        
        public CsmChangeEvent get() throws InterruptedException {
            synchronized( lock ) {
                while( data.isEmpty() ) {
                    lock.wait();
                }
                return (CsmChangeEvent) data.removeFirst();
            }
        }
        
        public void add(CsmChangeEvent event) {
            synchronized( lock ) {
                data.add(event);
                lock.notify();
            }
        }
    }
    
    private ClassViewModel model;
    private BlockingQueue queue;
    
    public ClassViewUpdater(ClassViewModel model) {
        this.model = model;
        queue = new BlockingQueue();
    }
    
    public void run() {
        try {
            while( true ) {
                CsmChangeEvent e = queue.get();
                model.update(e);
            }
        }
        catch( InterruptedException e ) {
            return;
        }
    }
    
    public void scheduleUpdate(CsmChangeEvent e) {
        //model.update(e);
        queue.add(e);
    }
    
//    public void update(CsmChangeEvent e) {
//	update(model.getRoot(), e);
//    }
//    
//    private void update(Node node, CsmChangeEvent e) {
//	if( node instanceof BaseNode ) {
//	    ((BaseNode) node).update(e);
//	}
//        else if( node instanceof ProjectNode ) {
//            if( e.getChangedProjects().contains(((ProjectNode) node).getProject()) ) {
//                ((ProjectNode) node).update(e);
//            }
//        }
//	else {
//	    for( Enumeration children = node.getChildren().nodes(); children.hasMoreElements(); ) {
//		update((Node) children.nextElement(), e);
//	    }
//	}
//    }
    
}

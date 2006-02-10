/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.viewmodel.TreeModelNode;
import org.netbeans.modules.viewmodel.TreeModelRoot;
import org.netbeans.modules.viewmodel.TreeTable;
import org.netbeans.spi.viewmodel.*;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;



/**
 * Tests the JPDABreakpointEvent.resume() functionality.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class YardaTest  extends NbTestCase {


    public YardaTest (String s) {
        super (s);
    }

    public void testSubsequentRequest () throws Exception {
        doColeasingSimulation (0);
    }
    public void testColeasingOfRequests () throws Exception {
        doColeasingSimulation (1);
    }
    
    private void doColeasingSimulation (int type) throws Exception {
        ArrayList l = new ArrayList ();
        CompoundModel1 cm1 = new CompoundModel1 ();
        l.add (cm1);
        TreeTable tt = (TreeTable) Models.createView 
            (Models.createCompoundModel (l));
        Node n = tt.getExplorerManager ().
            getRootContext ();
        synchronized (cm1) {
            n.getChildren ().getNodes ();
            cm1.wait ();
            assertEquals ("Model caled", 1, cm1.count);
            cm1.fire ();
            n.getChildren ().getNodes ();
            
            if (type == 1) {
                cm1.fire ();
                n.getChildren ().getNodes ();
            }
            
            cm1.notifyAll ();
        }
        TreeModelNode.getRequestProcessor ().post (new Runnable () {
            public void run () {}
        }).waitFinished ();
        //System.err.println("Child = "+n.getChildren().getNodes()[0]);
        // TODO: Broken, there's a Please wait... node!
        assertEquals ("Computation has finished in RP", 3, n.getChildren ().getNodes ().length);
        assertEquals ("Model caled", 2, cm1.count);
    }
    
    public final class CompoundModel1 extends BasicTest.CompoundModel {
        
        public int count = 0; 


        // init ....................................................................

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        public synchronized int getChildrenCount (Object node) throws UnknownTypeException {
            count++;
            notify ();
            /*
            try {
                wait (2000); // We must not wait here, otherwise we get a "Please wait..." node
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
             */
            return super.getChildrenCount (node);
        }
    }
}

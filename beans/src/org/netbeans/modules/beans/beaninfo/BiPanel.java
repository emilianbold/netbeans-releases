/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.awt.BorderLayout;
import java.beans.BeanInfo;
import java.io.ObjectInput;
import java.io.IOException;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;


import org.openide.windows.TopComponent;
import org.openide.explorer.view.BeanTreeView;

import org.openide.awt.SplittedPanel;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;


/**
* Search doc action.
*
* @author   Petr Hrebejk
*/
public class BiPanel extends ExplorerPanel  {

    private SplittedPanel sp;
    private static ExplorerManager em;
    private PropertySheetView psv;
    private BeanTreeView btv;

    static final long serialVersionUID =4088175782441275332L;

    BiPanel( ) {
        Node waitNode = new BiNode.Wait();

        createContent( waitNode );
    }

    private void createContent ( Node biNode ) {

        SplittedPanel sp = new SplittedPanel ();
        btv = new BeanTreeView ();
        em = getExplorerManager ();
        PropertySheetView psv = new PropertySheetView ();

        try {
            psv.setSortingMode (PropertySheetView.UNSORTED);
        }
        catch (java.beans.PropertyVetoException e) {
        }

        //sp.add (new org.openide.explorer.view.ListView (), SplittedPanel.ADD_LEFT);
        sp.add (btv, SplittedPanel.ADD_LEFT);
        sp.add (psv, SplittedPanel.ADD_RIGHT);

        em.setRootContext ( biNode );
        em.setExploredContext( biNode );

        btv.setDefaultActionAllowed( true );

        setLayout (new BorderLayout());
        add (BorderLayout.CENTER, sp);

    }

    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension sup = super.getPreferredSize ();
        return new java.awt.Dimension ( Math.max (sup.width, 450), Math.max (sup.height, 300 ));
    }

    void expandAll() {
        btv.expandAll();
    }

    static Node[] getSelectedNodes() {
        return em.getSelectedNodes();
    }

    void setContext( Node node ) {
        em.setRootContext ( node );
        em.setExploredContext( node );
    }

}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/18/99  Petr Hrebejk    BeanInfo analyse moved 
 *       to separate thread
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         7/26/99  Petr Hrebejk    
 * $
 */

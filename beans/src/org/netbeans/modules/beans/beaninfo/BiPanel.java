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

package org.netbeans.modules.beans.beaninfo;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;


/**
* Search doc action.
*
* @author   Petr Hrebejk
*/
public final class BiPanel extends TopComponent implements ExplorerManager.Provider {
    public static final String BEANINFO_HELP = "beans.beaninfo.nodes"; // NOI18N

    private static ExplorerManager em;
    private BeanTreeView btv;

    static final long serialVersionUID =4088175782441275332L;

    public BiPanel( ) {
        Node waitNode = new BiNode.Wait();

        createContent( waitNode );
    }

    private void createContent ( Node biNode ) {

        btv = new BeanTreeView ();
        em = new ExplorerManager();
        
        PropertySheetView psv = new PropertySheetView ();

        try {
            psv.setSortingMode (PropertySheetView.UNSORTED);
        }
        catch (java.beans.PropertyVetoException e) {
        }

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, btv, psv);

        em.setRootContext ( biNode );
        em.setExploredContext( biNode );

        btv.setDefaultActionAllowed( true );

        setLayout (new BorderLayout());
        add (BorderLayout.CENTER, sp);
        
        initAccessibility();
    }

    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension sup = super.getPreferredSize ();
        return new java.awt.Dimension ( Math.max (sup.width, 450), Math.max (sup.height, 300 ));
    }

    protected void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(em, true);
    }

    protected void componentDeactivated() {
        ExplorerUtils.activateActions(em, false);
        super.componentDeactivated();
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

    public HelpCtx getHelpCtx(){
        return new HelpCtx(BiPanel.BEANINFO_HELP);
    }
    
    private void initAccessibility() {
        btv.getAccessibleContext().setAccessibleName((NbBundle.getBundle("org.netbeans.modules.beans.beaninfo.Bundle")).getString("ACSN_BeanInfoLeftTreeView"));
        btv.getAccessibleContext().setAccessibleDescription((NbBundle.getBundle("org.netbeans.modules.beans.beaninfo.Bundle")).getString("ACSD_BeanInfoLeftTreeView"));
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }
}

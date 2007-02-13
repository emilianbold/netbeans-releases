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

package org.netbeans.modules.xml.nbprefuse;


import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import prefuse.controls.ControlAdapter;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *  In the prefuse NodeItem is stored a UIHelper org.openide.nodes.Node
 *  The actions from Node.getActions() are used to populate a NodeItem
 *  popup menu.  
 *
 *
 * @author Jeri Lockhart
 */
public final class PopupMouseControl extends ControlAdapter {
        
        private VisualItem vItem;

        public void itemReleased(VisualItem gi, MouseEvent e) {

            super.itemReleased(gi, e);
            vItem = gi;
            maybeShowPopup(e);
        }

        public void itemPressed(VisualItem gi, MouseEvent e) {

            super.itemPressed(gi, e);
            vItem = gi;
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (vItem instanceof NodeItem){
                    if ((vItem.canGetBoolean(AnalysisConstants.IS_PRIMITIVE) &&
                            vItem.getBoolean(AnalysisConstants.IS_PRIMITIVE) == false)
                            &&
                            (hasActions())
                            ){
                        
                        createPopup().show(e.getComponent(),
                                   e.getX(), e.getY());
                        
                    }
                }
            }
        }
        
        private boolean hasActions(){
        /*    org.openide.nodes.Node displayNode = null;
            if (vItem.canGet(AnalysisConstants.OPENIDE_NODE, 
                    org.openide.nodes.Node.class)) {
                displayNode = (org.openide.nodes.Node)
                    vItem.get(AnalysisConstants.OPENIDE_NODE);
                if (displayNode == null){
                    return false;
                }
                Action[] actions = displayNode.getActions(false);
                return (actions != null && actions.length > 0);
            }*/
            return false;
        }
        
	private JPopupMenu createPopup() {
	    JPopupMenu menu = null;
	    /*if (vItem.canGet(AnalysisConstants.OPENIDE_NODE, 
                    org.openide.nodes.Node.class)) {
		org.openide.nodes.Node displayNode = null;
                displayNode = (org.openide.nodes.Node)
                    vItem.get(AnalysisConstants.OPENIDE_NODE);
		if (displayNode != null) {
		    menu = displayNode.getContextMenu();
		}
	    }*/
	    return menu == null ? new JPopupMenu() : menu;
	}

    }

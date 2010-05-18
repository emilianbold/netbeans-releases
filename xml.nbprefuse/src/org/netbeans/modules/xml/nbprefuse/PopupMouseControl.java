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

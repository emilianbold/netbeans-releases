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


/*
 * Created on Mar 2, 2004
 *
 */
package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.LabelManager;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IMenuKind;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSorter;
import org.netbeans.modules.uml.ui.support.contextmenusupport.SorterHelper;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraph;
import com.tomsawyer.graph.TSNode;

/**
 * @author jingmingm
 *
 */
public class DrawingAreaContextMenuSorter extends SorterHelper implements IProductContextMenuSorter
{
	public void sort(IMenuManager pContextMenu)
	{
		Object pItemClickedOn = pContextMenu.getContextObject();
//		CComPtr < IDispatch > pItemClickedOn;
//
//	  _VH(pContextMenu->get_ItemClickedOn( &pItemClickedOn ) );
//
//	  CComQIPtr < TSCOM::TSGraph > pProductGraph(pItemClickedOn);
//	  CComQIPtr < TSCOM::TSNode >  pProductNode(pItemClickedOn);
//	  CComQIPtr < TSCOM::TSEdge >  pProductEdge(pItemClickedOn);
//	  CComQIPtr < TSCOM::TSLabel > pProductLabel(pItemClickedOn);
		
		m_TopSortItems.clear();
		m_BottomSortItems.clear();
		
		if (pItemClickedOn instanceof TSGraph)
		{
			// Here's how the context menu should look (No Elements Selected)
			// Edit > (pull right) with (MBK_POPUP_COPY) as a child
			// Layout > (pull right) with (MBK_POPUP_LAYOUT_CIRCULAR) as a child
			// Background > (pull right) with (MBK_POPUP_COLOR) as a child
			// ----------------------------
			// MBK_ZOOM_IN
			// MBK_ZOOM_OUT
			// MBK_ZOOM_CUSTOM_ZOOM
			// ----------------------------
			// MBK_SYNCH_ELEMENT_WITH_DATA
			// MBK_DIAGRAM_OPTIONS
			// MBK_GRAPHPROPERTIES
			
			// Top Sort Items
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_POPUP_COPY", new Integer(IMenuKind.MK_PULLRIGHT)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_LAYOUT_POPUP_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_POPUP_COLOR", new Integer(IMenuKind.MK_PULLRIGHT)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ZOOM_IN", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ZOOM_OUT", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ZOOM_CUSTOM_ZOOM", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ADVANCEDDOCUMENTATION", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_SYNCH_ELEMENT_WITH_DATA", new Integer(IMenuKind.MK_BUTTON)));
			
			m_BottomSortItems.add(new ETPairT<String, Integer>("MBK_DIAGRAM_OPTIONS", new Integer(IMenuKind.MK_BUTTON)));
//			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_BottomSortItems.add(new ETPairT<String, Integer>(
					"org.netbeans.modules.uml.integration.ide.actions.SelectInModel", 
					new Integer(IMenuKind.MK_BUTTON)));
			
			m_BottomSortItems.add(new ETPairT<String, Integer>("MBK_GRAPHPROPERTIES", new Integer(IMenuKind.MK_BUTTON)));
		}
		
		else if (pItemClickedOn instanceof TSEdge)
		{
			// Here's how the context menu should look (Edge is selected)
			// Operations > (pull right)
			// Edit > (pull right) with (MBK_POPUP_COPY) as a child
			// Find > (pull right) with (MBK_FIND_FROM_NODE) as a child
			// ----------------------------
			// Labels > (pull right)
			// Multiplicity > (pull right) with (MBK_SET_MULTIPLICITY_0_1) as a child
			// ----------------------------
			// Transform > (pull right)
			// ----------------------------
			// MBK_ADVANCEDDOCUMENTATION
			// ----------------------------
			// MBK_SYNCH_ELEMENT_WITH_DATA
			// MBK_EDGEPROPERTIES
			
			
			
			// Top Sort Items
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_SQD_OPERATIONS_PULLRIGHT"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_POPUP_COPY", new Integer(IMenuKind.MK_PULLRIGHT)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_EDGETRANSFORM_POPUP_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_GENERATE_PULLRIGHT"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_FIND_FROM_NODE", new Integer(IMenuKind.MK_PULLRIGHT)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_LABELS_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_SET_MULTIPLICITY_0_1", new Integer(IMenuKind.MK_PULLRIGHT)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ADVANCEDDOCUMENTATION", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_SYNCH_ELEMENT_WITH_DATA", new Integer(IMenuKind.MK_BUTTON)));
			
			m_BottomSortItems.add(new ETPairT<String, Integer>(
					"org.netbeans.modules.uml.integration.ide.actions.SelectInModel", 
					new Integer(IMenuKind.MK_BUTTON)));
			// The Properties action is now just a generic NB Properties action
			// and can't be identified by the "MBK" id
			// m_BottomSortItems.add(new ETPairT<String, Integer>(
			//	"MBK_EDGEPROPERTIES", new Integer(IMenuKind.MK_BUTTON)));
			m_BottomSortItems.add(
				new ETPairT<String, Integer>(
					"org.openide.actions.PropertiesAction", // NOI18N
					new Integer(IMenuKind.MK_BUTTON)));
		}
		
		else if (pItemClickedOn instanceof TSNode)
		{
			// Here's how the context menu should look (Node is selected)
			// MBK_INSERT_ATTRIBUTE
			// MBK_INSERT_OPERATION
			// MBK_DELETE_ATTRIBUTE
			// MBK_DELETE_OPERATION
			// MBK_INSERT_EXTENSIONPOINT
			// MBK_DELETE_EXTENSIONPOINT
			// MBK_INSERT_ENUMERATIONLITERAL
			// MBK_DELETE_ENUMERATIONLITERAL
			// Events and Transitions (pull right)
			// ----------------------------
			// Edit > (pull right) with (MBK_POPUP_COPY) as a child
			// Transform > (pull right) with (MBK_TRANSFORM_TO_ACTOR) as a child
			// ----------------------------
			// &Show (pull right)
			// &Hide (pull right)
			// ----------------------------
			// MBK_ADVANCEDDOCUMENTATION
			// MBK_RESET_EDGES
			// MBK_RESIZE_ELEMENT_TO_CONTEXT
			// MBK_SYNCH_ELEMENT_WITH_DATA
			// ----------------------------
			// MBK_POPUP_FIND_IN_PROJECTTREE
			// MBK_NODEPROPERTIES
			
			// Top Sort Items
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_INSERT_ATTRIBUTE", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_INSERT_ERATTRIBUTE", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_INSERT_ERATTRIBUTE_PK", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_INSERT_OPERATION", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DELETE_ATTRIBUTE", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DELETE_ERATTRIBUTE", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DELETE_OPERATION", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_INSERT_EXTENSIONPOINT", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DELETE_EXTENSIONPOINT", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_INSERT_ENUMERATIONLITERAL", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DELETE_ENUMERATIONLITERAL", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_POPUP_STATE_EVENTS"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_POPUP_COPY", new Integer(IMenuKind.MK_PULLRIGHT)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_TRANSFORM_TO_ACTOR", new Integer(IMenuKind.MK_PULLRIGHT)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_GENERATE_PULLRIGHT"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_COMPARTMENT_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_ELEMENT_TYPE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_FONT", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_RESET_EDGES", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_RESIZE_ELEMENT_TO_CONTEXT", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DIMENSIONAL_RESIZE_ELEMENT", new Integer(IMenuKind.MK_BUTTON)));
			
                        // horizontal alignment
                        m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ALIGN_LEFT", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ALIGN_HCENTER", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ALIGN_RIGHT", new Integer(IMenuKind.MK_BUTTON)));

                        // vertical alignment
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ALIGN_TOP", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ALIGN_VCENTER", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ALIGN_BOTTOM", new Integer(IMenuKind.MK_BUTTON)));

// disabled - feature to be added with Meteora
                        // horizontal distribution
//                        m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISTRIBUTE_LEFT_EDGE", new Integer(IMenuKind.MK_BUTTON)));
//                        m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISTRIBUTE_HCENTER", new Integer(IMenuKind.MK_BUTTON)));
//                        m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISTRIBUTE_RIGHT_EDGE", new Integer(IMenuKind.MK_BUTTON)));
                        // vertical distribution
//                        m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISTRIBUTE_TOP_EDGE", new Integer(IMenuKind.MK_BUTTON)));
//                        m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISTRIBUTE_VCENTER", new Integer(IMenuKind.MK_BUTTON)));
//                        m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISTRIBUTE_BOTTOM_EDGE", new Integer(IMenuKind.MK_BUTTON)));
                        
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_SYNCH_ELEMENT_WITH_DATA", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_POPUP_HIDING_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_POPUP_SHOWING_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_STATE_EVENT_TRANSITIONS_COMPARTMENT_SHOW", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_STATE_EVENT_TRANSITIONS_COMPARTMENT_HIDE", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_GRAPHS_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_LABELS_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_POPUPMENU_CUSTOMIZE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_PORTS_TITLE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("IDS_PORT_LOCATIONS"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISTRIBUTE_PORT_INTERFACES", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ADVANCEDDOCUMENTATION", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DESIGN_PATTERN_APPLY", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_ASSOCIATEDLG_ASSOCIATEWITH", new Integer(IMenuKind.MK_BUTTON)));
//			m_TopSortItems.add(new ETPairT<String, Integer>("MBK_CLASSIFIER_DEPENDENCY", new Integer(IMenuKind.MK_BUTTON)));
			m_TopSortItems.add(new ETPairT<String, Integer>(LabelManager.loadString("MBK_GENERATE_CODE"), new Integer(IMenuKind.MK_PULLRIGHTTITLE)));
			m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
			
//			m_BottomSortItems.add(new ETPairT<String, Integer>("MBK_POPUP_FIND_IN_PROJECTTREE", new Integer(IMenuKind.MK_BUTTON)));
			m_BottomSortItems.add(new ETPairT<String, Integer>(
					"org.netbeans.modules.uml.integration.ide.actions.SelectInModel", 
					new Integer(IMenuKind.MK_BUTTON)));
			// cvc - CR 6276919
			// The Properties action is now just a generic NB Properties action
			// and can't be identified by the "MBK" id
			// String name = new org.openide.actions.PropertiesAction().getName();
			// m_BottomSortItems.add(new ETPairT<String, Integer>(
                        //       name, new Integer(IMenuKind.MK_BUTTON)));
			// m_BottomSortItems.add(new ETPairT<String, Integer>(
			//	"MBK_NODEPROPERTIES", new Integer(IMenuKind.MK_BUTTON)));
			m_BottomSortItems.add(
				new ETPairT<String, Integer>(
					"org.openide.actions.PropertiesAction", // NOI18N
					new Integer(IMenuKind.MK_BUTTON)));
		}
		
		// Do the actual sort of the toplevel menu buttons
		super.sortMenu(pContextMenu);
		
		// Now go into the labels pullright and make sure the reset edges button is on the bottom
		m_TopSortItems.clear();
		m_BottomSortItems.clear();
		m_BottomSortItems.add(new ETPairT<String, Integer>("MBK_RESET_LABELS", new Integer(IMenuKind.MK_BUTTON)));
		
		// Now sort the labels pullright
		super.sortMenu(pContextMenu, LabelManager.loadString("IDS_LABELS_TITLE"));
		
//	  // Now go into the show pullright and sort those guys
//	  m_TopSortItems.clear();
//	  m_BottomSortItems.clear();
//	  m_TopSortItems.add(new ETPairT<String, Integer>("MBK_PGK_NAME_IN_TAB", new Integer(IMenuKind.MK_BUTTON)));
//	  m_TopSortItems.add(new ETPairT<String, Integer>("MBK_PGK_NAME_IN_NOTINTAB", new Integer(IMenuKind.MK_BUTTON)));
//	  m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISPLAY_AS_ICON", new Integer(IMenuKind.MK_BUTTON)));
//	  m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISPLAY_AS_CLASS", new Integer(IMenuKind.MK_BUTTON)));
//	  m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
//
//	  // Now sort the show pullright
//	  super.sortMenu(pContextMenu, LabelManager.loadString("IDS_LABELS_TITLE"));
		
		// Now go into the show pullright and sort those guys
		m_TopSortItems.clear();
		m_BottomSortItems.clear();
		m_TopSortItems.add(new ETPairT<String, Integer>("MBK_PGK_NAME_IN_TAB", new Integer(IMenuKind.MK_BUTTON)));
		m_TopSortItems.add(new ETPairT<String, Integer>("MBK_PGK_NAME_IN_NOTINTAB", new Integer(IMenuKind.MK_BUTTON)));
		m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISPLAY_AS_ICON", new Integer(IMenuKind.MK_BUTTON)));
		m_TopSortItems.add(new ETPairT<String, Integer>("MBK_DISPLAY_AS_CLASS", new Integer(IMenuKind.MK_BUTTON)));
		m_TopSortItems.add(new ETPairT<String, Integer>("", new Integer(IMenuKind.MK_SEPARATOR)));
		
		// Now sort the show pullright
		super.sortMenu(pContextMenu, LabelManager.loadString("IDS_POPUP_SHOWING_TITLE"));
	}
}

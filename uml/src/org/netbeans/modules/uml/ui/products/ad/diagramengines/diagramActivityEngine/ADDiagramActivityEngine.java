/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.ui.products.ad.diagramengines.diagramActivityEngine;

import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.SimpleAction;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.ADCoreEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IDrawingAreaAcceleratorKind;

import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSGraphTailor;
import com.tomsawyer.editor.TSEGraph;
//import com.tomsawyer.layout.property.*;
//import com.tomsawyer.layout.glt.property.*;

/**
 * 
 * @author Trey Spiva
 * The ADActivityDiagEngineImpl provides behavioral support for the GET when it is supposed to
 * act like an Activity diagram *

 */
public class ADDiagramActivityEngine extends ADCoreEngine implements IADActivityDiagramEngine {
	public ADDiagramActivityEngine() {
		super();
	}

//	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass) {
//		boolean bFlag = isParentDiagramReadOnly() ? false : true;
//		if (bFlag) {
//			if (id.equals("MBK_ACD_ADD_VERT_PARTITION")) {
//				bFlag = false;
//			} else if (id.equals("MBK_ACD_ADD_HORZ_PARTITION")) {
//				bFlag = true;
//			} else {
//				bFlag = super.setSensitivityAndCheck(id, pClass);
//			}
//		}
//
//		return bFlag;
//	}

	public boolean onHandleButton(ActionEvent e, String id) {
		if (id.equals("MBK_ACD_ADD_VERT_PARTITION")) {
		} else if (id.equals("MBK_ACD_ADD_HORZ_PARTITION")) {
			//createDragManagerTool();
		} else {
			return super.onHandleButton(e, id);
		}
		return false;
	}

	/**
	* Allows the diagram engines the ability to change the element being dropped
	* For the sequence diagram, convert the element:
	* class, and actor get converted to a lifeline, which references the class, or actor
	* combined fragment must have at least one interaction operand
	*
	* @param pElementBeingDropped[in] Element to be converted, if necessary
	* @param pChangedElement[out] The new element that should be treated as if it was dropped
	* @param bCancelThisElement[out] Set to true to cancel this event
	*/
	public ETPairT<Boolean,IElement> processOnDropElement(IElement pElementBeingDropped) {
		if (pElementBeingDropped == null)
			return new ETPairT<Boolean,IElement>(new Boolean(false),null);

		boolean bCancelThisElement = false;
		IElement pChangedElement = pElementBeingDropped;
		try {
			//*pChangedElement = 0;

			IInteraction cpInteraction = pElementBeingDropped instanceof IInteraction ? (IInteraction) pElementBeingDropped : null;
			IActivity cpActivity = pElementBeingDropped instanceof IActivity ? (IActivity) pElementBeingDropped : null;

			if (cpInteraction != null) {
				bCancelThisElement = true;

				// Fix W1762:  Make sure the interaction is not the same as this diagram's parent interaction
				INamespace cpNamespace = this.getDrawingArea().getNamespace();
				IInteraction cpDiagramsInteraction = cpNamespace instanceof IInteraction ? (IInteraction) cpNamespace : null;
				if (cpDiagramsInteraction != cpInteraction && cpNamespace != null) {
					// Create the IInteractionOccurrence, and attach the interaction
					TypedFactoryRetriever < IInteractionOccurrence > factory = new TypedFactoryRetriever < IInteractionOccurrence > ();
					IInteractionOccurrence cpInteractionOccurrence = factory.createType("InteractionOccurrence");
					if (cpInteractionOccurrence != null) {
						cpInteractionOccurrence.setInteraction(cpInteraction);

						// Use the diagram's namespace for the interaction occurrence
						cpInteractionOccurrence.setNamespace(cpNamespace);

						// Set the rcpElement to be the interaction occurrence so,
						// the rest of the attach stuff works
						pChangedElement = cpInteractionOccurrence;
						bCancelThisElement = false;
					}
				}
			} else if (cpActivity != null) {
				bCancelThisElement = true;

				// Fix W1762:  Make sure the interaction is not the same as this diagram's parent interaction
				INamespace cpNamespace = this.getDrawingArea().getNamespace();
				IActivity pActivity = cpNamespace instanceof IActivity ? (IActivity) cpNamespace : null;
				if (pActivity != cpActivity && cpNamespace != null) {
					// Create the IInteractionOccurrence, and attach the interaction
					TypedFactoryRetriever < IInteractionOccurrence > factory = new TypedFactoryRetriever < IInteractionOccurrence > ();
					IInteractionOccurrence cpInteractionOccurrence = factory.createType("InteractionOccurrence");
					if (cpInteractionOccurrence != null) {
						cpInteractionOccurrence.setBehavior(cpActivity);

						// Use the diagram's namespace for the interaction occurrence
						cpInteractionOccurrence.setNamespace(cpNamespace);

						// Set the rcpElement to be the interaction occurrence so,
						// the rest of the attach stuff works
						pChangedElement = cpInteractionOccurrence;
						bCancelThisElement = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ETPairT<Boolean,IElement>(new Boolean(bCancelThisElement),pChangedElement);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#registerAccelerators()
	 */
	public void registerAccelerators()
	{
		ETList<String> accelsToRegister = new ETArrayList<String>();

		// Add the normal accelerators, minus the layout stuff
		addNormalAccelerators(accelsToRegister, true);

		// Add the nodes and edges specific to the activity diagram
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_INVOCATION);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_DECISION);
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_ACTIVITYEDGE  );
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_CREATE_ABSTRACTION  );
		
		// Toggle orthogonality
		accelsToRegister.add(IDrawingAreaAcceleratorKind.DAVK_TOGGLE_ORTHOGONALITY );

		registerAcceleratorsByType(accelsToRegister);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#convertDiagramsToElements(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[], org.netbeans.modules.uml.core.support.umlsupport.IStrings)
	 */
	public void convertDiagramsToElements(IElement[] pMEs, IStrings pDiagramLocations) {
		// TODO Auto-generated method stub
		super.convertDiagramsToElements(pMEs, pDiagramLocations);
	}

	/**
	* Called after a new diagram is initialized.  The activity diagram creates a new
	* IActivity and then places the diagram under that IElement.
	*/
	public void initializeNewDiagram() {
		try {
			if (this.getDrawingArea() != null) {
				ISimpleAction pSimpleAction = new SimpleAction();

				if (pSimpleAction != null) {
					pSimpleAction.setKind(DiagramAreaEnumerations.SAK_DELAYED_INITIALIZATION);

					this.getDrawingArea().postDelayedAction(pSimpleAction);
				}
				this.setupLayoutSettings(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
	 */
	public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem) {
		// TODO Auto-generated method stub
		super.onContextMenuHandleSelection(pContextMenu, pMenuItem);
	}

	/**
	 *
	 * Uses the presentation reference to determine the activity associated with this diagram
	 *
	 * @param pActivity[out] The parent IActivity of the diagram, if it's null then an activity is 
	 * created and the diagram is reparented.
	 *
	 * @return HRESULT
	 */
	protected IActivity getActivityDiagramActivity() {
		IActivity pActivityForActivityDiagram = null;
		try {
			if (this.getDrawingArea() != null) {
				INamespace pNamespace = null;
				IDiagram pDiagram = this.getDiagram();
				String sName = null;

				// See if the namespace is already and activity.  If it is then don't create one.
				if (pDiagram != null) {
					pNamespace = pDiagram.getNamespace();
					sName = pDiagram.getName();
				}

				IActivity pNamespaceActivity = pNamespace instanceof IActivity ? (IActivity) pNamespace : null;
				if (pNamespaceActivity != null) {
					pActivityForActivityDiagram = pNamespaceActivity;
				}

				// If we did not find an interaction,
				// create an interaction, and create the associated presentation reference
				if (pDiagram != null && pActivityForActivityDiagram == null) {
					TypedFactoryRetriever < IActivity > factory = new TypedFactoryRetriever < IActivity > ();

					// Create the activity for the activity diagram.
					pActivityForActivityDiagram = factory.createType("Activity");

					if (pActivityForActivityDiagram != null) {
						// Give the activity the same name as the diagram
						pActivityForActivityDiagram.setName(sName);

						// Associate the activity with the diagram's current namespace
						pActivityForActivityDiagram.setNamespace(pNamespace);

						// Move the activity diagram under the activity
						// CComQIPtr< INamespace > pNamespaceActivity( pActivityForActivityDiagram );
						getDrawingArea().setNamespace(pActivityForActivityDiagram);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pActivityForActivityDiagram;
	}

	/**
	* Allows engine to handle delayed actions
	*
	* @param pAction [in] The action that we delayed and should be handled now
	* @param bHandled [out,retval] Set this to true if this engine has handled the action.
	*/
	public boolean handleDelayedAction(IDelayedAction pAction) {
		boolean bHandled = false;
		try {
			ISimpleAction pSimpleAction = pAction instanceof ISimpleAction ? (ISimpleAction) pAction : null;
			if (this.getDrawingArea() != null && pSimpleAction != null) {
				if (pSimpleAction.getKind() == DiagramAreaEnumerations.SAK_DELAYED_INITIALIZATION) {
					getActivityDiagramActivity();
					bHandled = true;
					getDrawingArea().setIsDirty(true);
				}
			} else if (pAction instanceof ISwapEdgeEndsAction) {
				ISwapEdgeEndsAction swapEnds = (ISwapEdgeEndsAction) pAction;
				swapEnds.execute();
				bHandled = true;
			}

			if (bHandled == false) {
				bHandled = super.handleDelayedAction(pAction);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bHandled;
	}

   /**
    * Called after a new diagram is initialized to setup our default layout settings
    */
   public void setupLayoutSettings(boolean bNewDiagram)
   {
      if (bNewDiagram)
      {
         IDrawingAreaControl control = getDrawingArea();
         if (control != null)
         {
				control.setLayoutStyleSilently(ILayoutKind.LK_HIERARCHICAL_LAYOUT);  
                                /*
				TSBooleanLayoutProperty property = new TSBooleanLayoutProperty(TSTailorProperties.HIERARCHICAL_ORTHOGONAL_ROUTING);
				property.setCurrentValue(true);
            
				control.getGraphWindow().getGraph().setTailorProperty(property);
                                 */
         }      
      }
   }
}

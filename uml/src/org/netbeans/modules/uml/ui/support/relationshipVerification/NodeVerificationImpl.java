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


//	 Author:: josephg
//	   Date:: Oct 16, 2003 1:34:59 PM
//	Modtime:: 1/31/2004 12:16:07 PM 1:34:59 PM

package org.netbeans.modules.uml.ui.support.relationshipVerification;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Iterator;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IFinalState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IPseudostateKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.support.applicationmanager.DrawingFactory;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.drawing.geometry.TSPoint;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;

/**
 * 
 * @author Trey Spiva
 */
public class NodeVerificationImpl implements INodeVerification {

	public ETPairT < IElement, IPresentationElement > createAndVerify(IDiagram pDiagram,
			IETNode pCreatedNode, INamespace pNamespace)
	{
		return createAndVerify(pDiagram, pCreatedNode, pNamespace, null,null);
	}
 

	/**
	 * Creates the appropriate metatype for this node.  
	 * 
	 * @param pDiagram The current diagram
	 * @param pCreatedNode The node that just got created
	 * @param pNamespace The namespace the new node should be in
	 * @param metaTypeString The metatype string of the new element
	 * @param sInitializationString The initialization string of the node that was just created.
	 * @return A pair that contains that conitains the IElement and a 
	 *         presentation reference relationship is created between the 
	 *         referencing presentation element and the PresentationElement to
	 *         be created.  If no referencing presentation element exist then
	 *         <code>null</code> will be returned.
	 */
	public ETPairT < IElement, IPresentationElement > createAndVerify(IDiagram pDiagram,
			IETNode pCreatedNode, INamespace pNamespace, String metaTypeString, String sInitializationString) {
		
		IElement modelElement = null;
		IPresentationElement presentation = null;
		IETGraphObjectUI objUI = pCreatedNode != null ? pCreatedNode.getETUI() : null;
		if (objUI != null)
		{
			String elementKind = metaTypeString == null ? getElementKind(objUI) : metaTypeString;

			ITSGraphObject graphObj = objUI.getTSObject();
			if (graphObj != null && graphObj instanceof TSENode && metaTypeString != null && metaTypeString.equals("Port"))
			{
				// Handle creating a port.  In this case we need to find the nearest component draw engine
				// and put the port on the edge of the component.
				TSENode pNode = (TSENode)graphObj;
				TSConstPoint centerPoint = pNode.getCenter();
				if (centerPoint != null)
				{
					double nearestX = centerPoint.getX();
					double nearestY = centerPoint.getY();
					
					IDrawingAreaControl pControl = null;
					if (pDiagram != null && pDiagram instanceof IUIDiagram)
					{
						pControl = ((IUIDiagram)pDiagram).getDrawingArea();
						
						ETTripleT<IPresentationElement, Double, Double> nearestVal = getNearestNode(pControl, nearestX, nearestY, "ComponentDrawEngine");
						if(nearestVal != null) {
							IPresentationElement pNearestComponent = nearestVal.getParamOne();
							
							IElement pElement = TypeConversions.getElement(pNearestComponent);
							if (pElement != null && pElement instanceof INamespace)
							{
								pNamespace = (INamespace)pElement;
								presentation = pNearestComponent;
							}
						}
					}
				}
			}

			modelElement = objUI.getModelElement();
			if (modelElement == null)
			{
				modelElement = createElement(pCreatedNode.getText(), elementKind, pNamespace);
				objUI.setModelElement(modelElement);
			}
			else
			{
				//currently the way things are coded, we get into this call twice, first from 
				//ADCreateState and then from postAddObject in ADDrawingAreaControl
				//when we get called here from postAddObject, the namespace might not be right, so fix it.
				IElement owner = modelElement.getOwner();
				if (!owner.isSame(pNamespace))
				{
					elementKind = modelElement.getElementType();
					modelElement.delete();
					modelElement = createElement(pCreatedNode.getText(), elementKind, pNamespace);
					objUI.setModelElement(modelElement);
				}
			}
			
			if (modelElement != null)
			{
				postCreate(modelElement, sInitializationString);
			}
			
//			presentation = pCreatedNode.getPresentationElement();
//			if (presentation == null)
//			{
//				presentation = DrawingFactory.createPresentationObj(objUI.getDrawEngine());
//				pCreatedNode.setPresentationElement(presentation); 
//			} 			
			
		}
		return new ETPairT(modelElement, presentation);
	}

	final static int LOCATION_SLOP = 20;

	/**
	 * Returns the nearest node of this type and the x,y location of the nearest side
	 *
	 * @param pDiagram [in] The current diagram
	 * @param x [in,out] The current x location of the node, use this as a return variable to change the location
	 * @param y [in,out] The current y location of the node, use this as a return variable to change the location
	 * @param sDrawEngineID [in] The type of draw engine to search for
	 * @param pFoundPE [out,retval] The found PE that matches the criteria of an sDrawEngineID within LOCATION_SLOP.
	 */
	private ETTripleT<IPresentationElement,Double,Double> getNearestNode(IDrawingAreaControl pControl, double x, double y, String sDrawEngineID)
	{
		if(pControl == null || sDrawEngineID == null )
			return null;
			
		double left = x-LOCATION_SLOP/2;
		double top = y+LOCATION_SLOP/2;
			
		IETRect etRect = new ETRect(new Point((int)left,(int)top),new Dimension(LOCATION_SLOP,LOCATION_SLOP));
			
		ETList<IPresentationElement> presentationElements = pControl.getAllNodesViaRect(etRect,true);
		
		ETList<IPresentationElement> componentPEs = new ETArrayList<IPresentationElement>();
		
		if(presentationElements != null) {
			Iterator<IPresentationElement> iterator = presentationElements.iterator();
			
			while(iterator.hasNext()) {
				IPresentationElement presentationElement = iterator.next();
				
				if(presentationElement != null) {
					IDrawEngine drawEngine = TypeConversions.getDrawEngine(presentationElement);
					if(drawEngine != null) {
						String sID = drawEngine.getDrawEngineID();
						
						if(sID.compareTo(sDrawEngineID) == 0) {
							componentPEs.add(presentationElement);
						}
					}
				}
			}
		}
		
		TSPoint inputTSPoint = new TSPoint(x,y);
		double nMinDistance = 999.0;
		TSPoint nearestPoint = null;
		IPresentationElement nearestComponent = null;		
		
		Iterator<IPresentationElement> iterator = componentPEs.iterator();
		while(iterator.hasNext()) {
			IPresentationElement presentationElement = iterator.next();
			
			if(presentationElement != null) {
				TSENode tseNode = TypeConversions.getOwnerNode(presentationElement);
				
				if(tseNode != null) {
					TSConstRect thisRect = tseNode.getBounds();
					
					TSPoint tempPoint = new TSPoint(inputTSPoint);
					if(RectConversions.moveToNearestPoint(thisRect,tempPoint)) {
						double nTempDistance = nMinDistance;
						double thisDistance = inputTSPoint.distance(tempPoint);
						if(thisDistance < nTempDistance) {
							nearestPoint = tempPoint;
							nMinDistance = thisDistance;
							nearestComponent = presentationElement;
						}
					}
					else {
						nearestPoint = tempPoint;
						nMinDistance = 0;
						nearestComponent = presentationElement;
					}
				}
			}
		}
		if(nearestComponent == null)
			return null;
			
		return new ETTripleT<IPresentationElement,Double,Double>(nearestComponent,
			new Double(nearestPoint.getX()), new Double(nearestPoint.getY()));
	}

	/**
	 * Called right after the model element is created so that the model element could have its properties modified
	 *
	 * @param pModelElement [in] The model element that was just created
	 * @param sInitializationString [in] The init string that was used to create the model element.
	 */
	private void postCreate(IElement pModelElement, String sInitStr)
        {
           if (sInitStr == null)
           {
              return;
           }
           
           int pos = sInitStr.indexOf(' ');
           if(pos >= 0)
           {
              sInitStr = sInitStr.substring(pos);
              sInitStr = sInitStr.trim();
           }
           
           IPseudoState pPseudoState = null;
           IState pState = null;
           IClassifier pClassifier = null;
           IPartFacade pPartFacade = null;
           IFinalState pFinalState = null;
           IPort pPort = null;
           ILifeline pLifeline = null;
           
           if (pModelElement instanceof IPseudoState)
           {
              pPseudoState = (IPseudoState)pModelElement;
           }
           if (pModelElement instanceof IState)
           {
              pState = (IState)pModelElement;
           }
           if (pModelElement instanceof IClassifier)
           {
              pClassifier = (IClassifier)pModelElement;
           }
           if (pModelElement instanceof IPartFacade)
           {
              pPartFacade = (IPartFacade)pModelElement;
           }
           if (pModelElement instanceof IFinalState)
           {
              pFinalState = (IFinalState)pModelElement;
           }
           if (pModelElement instanceof IPort)
           {
              pPort = (IPort)pModelElement;
           }
           if (pModelElement instanceof ILifeline)
           {
              pLifeline = (ILifeline)pModelElement;
           }
           
           if (pPseudoState != null)
           {
              if (sInitStr.equals("PseudoState Choice"))
              {
                 pPseudoState.setKind(IPseudostateKind.PK_CHOICE);
              }
              else if (sInitStr.equals("PseudoState EntryPoint"))
              {
                 pPseudoState.setKind(IPseudostateKind.PK_ENTRYPOINT);
                 pPseudoState.setName("activeEntry");
              }
              else if (sInitStr.equals("PseudoState DeepHistory"))
              {
                 pPseudoState.setKind(IPseudostateKind.PK_DEEPHISTORY);
              }
              else if (sInitStr.equals("PseudoState ShallowHistory"))
              {
                 pPseudoState.setKind(IPseudostateKind.PK_SHALLOWHISTORY);
              }
              else if (sInitStr.equals("PseudoState Initial"))
              {
                 pPseudoState.setKind(IPseudostateKind.PK_INITIAL);
              }
              else if (sInitStr.equals("PseudoState Junction"))
              {
                 pPseudoState.setKind(IPseudostateKind.PK_JUNCTION);
              }
              else if (sInitStr.equals("PseudoState Join") || sInitStr.equals("PseudoState Join Horizontal"))
              {
                 pPseudoState.setKind(IPseudostateKind.PK_JOIN);
              }
           }
           else if (pPartFacade != null)
           {
              if (pPartFacade instanceof IParameterableElement)
              {
                 IParameterableElement pParamEle = (IParameterableElement)pPartFacade;
                 if (sInitStr.equals("PartFacade Interface"))
                 {
                    pParamEle.setTypeConstraint("Interface");
                    if (pPartFacade instanceof IClassifier)
                    {
                       ((IClassifier)pPartFacade).setIsAbstract(true);
                    }
                 }
                 else if (sInitStr.equals("PartFacade Class"))
                 {
                    pParamEle.setTypeConstraint("Class");
                 }
                 else if (sInitStr.equals("PartFacade UseCase"))
                 {
                    pParamEle.setTypeConstraint("UseCase");
                 }
                 else if (sInitStr.equals("PartFacade Actor"))
                 {
                    pParamEle.setTypeConstraint("Actor");
                 }
              }
           }
           else if (pFinalState != null)
           {
              if (sInitStr.equals("FinalState Aborted"))
              {
                 pFinalState.setName("aborted");
              }
           }
           else if (pState != null)
           {
              if (sInitStr.equals("CompositeState"))
              {
                 pState.setIsComposite(true);
              }
              else if (sInitStr.equals("SimpleState"))
              {
                 pState.setIsSimple(true);
              }
              else if (sInitStr.equals("SubmachineState"))
              {
                 pState.setIsSubmachineState(true);
              }
           }
           else if (pClassifier != null)
           {
              if (sInitStr.equals("TemplateClass"))
              {
                 IElement pEle = DrawingFactory.retrieveModelElement("ParameterableElement");
                 if (pEle != null && pEle instanceof IParameterableElement)
                 {
                    pClassifier.addTemplateParameter((IParameterableElement)pEle);
                 }
              }
              else if (sInitStr.equals("UtilityClass"))
              {
                 pClassifier.applyStereotype2("utility");
              }
           }
           else if (pPort != null)
           {
              // The namespace of the port is assumed to be the featuring classifier
              INamespace pSpace = pPort.getNamespace();
              
              if (pSpace != null && pSpace instanceof IComponent)
              {
                 IComponent pComponent = (IComponent)pSpace;
                 pPort.setFeaturingClassifier(pComponent);
                 pComponent.addExternalInterface(pPort);
              }
           }
           // Fixed 82207, 82208, 78848
           // Set an attribute of this lifeline to indicate that it is an actor lifeline.
           // This attribute can later be used to determine the presentation of the element
           // on collaboration and sequence diagram
           else if (pLifeline != null)
           {
              if (sInitStr.equals("Actor"))
              {
                 pLifeline.setIsActorLifeline(true);
              }
           }
        }

	/**
	 * Verifies that this node is valid at this point.  Fired by the diagram 
	 * add node tool.  If the node requires a node to node relationship the
	 * actual location will be different then the location passed into the method. 
	 * 
	 * @param pDiagram The diagram to verify the location.
	 * @param location The location to verify.
	 * @return <code>true</code> if the location if valid, <code>false</code>
	 *         otherwise.
	 */
	public boolean verifyCreationLocation(IDiagram pDiagram, IETPoint location) {
		return true;
	}

	/**
	 * During the creation process this is fired when the node is dragged around.
	 * Fired by the diagram add node tool.
	 */
	public boolean verifyDragDuringCreation(IDiagram pDiagram, IETNode pCreatedNode, IETPoint location) {
		return true;
	}

	protected IDrawingAreaControl getDrawingArea(IETGraphObject graphObj) {
		if (graphObj != null) {
			IETGraphObjectUI ui = graphObj.getETUI();
			return ui != null ? ui.getDrawingArea() : null;
		}
		return null;
	}

	protected IElement createElement(String name, String eleKind, INamespace namespace) {
		if (name == null || name.length() == 0) {
			IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
			name = prefMan.getTranslatedPreferenceValue("NewProject", "DefaultElementName");
			//PreferenceAccessor accessor = PreferenceAccessor.instance();
			//name = accessor.getDefaultElementName();
		}
		
		IElement retEle = null;
		String elementType = eleKind;
		if (elementType == null || elementType.length() == 0) {
			elementType = "Class";
		}

		if (namespace == null) {
			//get the project namespace
			IApplication pApp = ProductHelper.getApplication();
			if (pApp != null && pApp.getNumOpenedProjects() > 0) {
					namespace = pApp.getProjects().get(0);
			}
		}

		//FactoryRetriever ret = FactoryRetriever.instance();
		//Object obj = ret.createType(elementType, null);
		retEle = DrawingFactory.retrieveModelElement(elementType);
		if (retEle != null) {
			// Name the element and add it to the correct namespace
			if (retEle instanceof INamedElement) {
				INamedElement nEle = (INamedElement) retEle;

				// Set the name on the package
				if (namespace != null) {
					if(namespace.addOwnedElement(nEle) == false)
               {
                  retEle = null;
               }
				}
				//nEle.setName(name);
			}
			else
			{
				if(namespace.addElement(retEle) == null)
            {
               retEle = null;
            }
			}
		}

		return retEle;
	}

	protected String getElementKind(IETGraphObjectUI objUI) {
		String retName = "";
		if (objUI != null) {
			IDrawEngine engine = objUI.getDrawEngine();
			
			String initString = engine != null ? engine.getElementType() : null;
			if (initString != null && initString.length() > 0) {
				retName = initString;
			} else {
				retName = "Class";
			}
		}
		return retName;
	}

}

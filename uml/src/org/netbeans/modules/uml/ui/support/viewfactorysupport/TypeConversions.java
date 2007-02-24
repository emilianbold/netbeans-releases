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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSEdgeLabel;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.util.TSObject;

/**
 * 
 * @author Trey Spiva
 */
public class TypeConversions {
   // //////////////////////////////////////////////////////////////////
   //
   // Getting the IETxxx objects
   //
   ////////////////////////////////////////////////////////////////////
   
	/** Returns the IETNode for the input TSNode */
	public static IETNode getETNode(TSObject pTSNode) {
		return pTSNode instanceof IETNode ? (IETNode) pTSNode : null;
	}

	/** Returns the IETEdge for the input TSEdge */
	public static IETEdge getETEdge(TSObject pTSEdge) {
		return pTSEdge instanceof IETEdge ? (IETEdge) pTSEdge : null;
	}

   /** Returns the IETNode for the input presentation element */
	public static IETNode getETNode(IPresentationElement pPresElement) {
		IETGraphObject obj = getETGraphObject(pPresElement);
		return obj instanceof IETNode ? (IETNode) obj : null;
	}

	 public static IETLabel getETLabel(IPresentationElement pPresElement) {
		 IETGraphObject obj = getETGraphObject(pPresElement);
		 return obj instanceof IETLabel ? (IETLabel) obj : null;
	 }

	public static IETLabel getETLabel(TSObject pTSLabel) {
      return pTSLabel instanceof IETLabel ? (IETLabel) pTSLabel : null;
   }
   
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // Getting the IETGraphObject
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	
	/*
	 * Returns the IETGraphObject for this TSNode
	 */
	public static IETGraphObject getETGraphObject(TSNode pTSNode) {
		return getETNode(pTSNode);
	}

	/*
	 * Returns the IETGraphObject for this TSEdge
	 */
	public static IETGraphObject getETGraphObject(TSEdge pTSEdge) {
		return getETEdge(pTSEdge);
	}

	/*
	 * Returns the IETGraphObject for this TSObject
	 */
	public static IETGraphObject getETGraphObject(TSObject pObject) {
		return pObject instanceof IETGraphObject ? (IETGraphObject) pObject : null;
	}

	/*
	 *  Returns the IETGraphObject for this IPresentationElement
	 */
	public static IETGraphObject getETGraphObject(IPresentationElement pPresElement) {
		if (pPresElement instanceof IGraphPresentation) {
			return ((IGraphPresentation) pPresElement).getETGraphObject();
		} else
			return null;
	}

	/*
	 * Returns the IETGraphObject for this TSObjectView
	 */
	public static IETGraphObject getETGraphObject(TSEObjectUI pView) {
		return pView instanceof IETGraphObjectUI ? (IETGraphObject)((IETGraphObjectUI)pView).getTSObject() : null;
	}

	/*
	 * Returns the IETGraphObject for this IDrawEngine
	 */
	public static IETGraphObject getETGraphObject(IDrawEngine pDrawEngine) {
		return pDrawEngine != null ? getETGraphObject(pDrawEngine.getPresentation()) : null;
	}

	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the IETElement : IETGraphObject
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	/*
	 * Returns the IETElement for this TSNode
	 */
	public static IGraphPresentation getETElement(TSObject pObject) {
		IETGraphObject graphObj = getETGraphObject(pObject);
		return graphObj != null ? getETElement(graphObj.getPresentationElement()) : null;
	}

	/*
	 * Returns the IGraphPresentation for this IPresentationElement
	 */
	public static IGraphPresentation getETElement(IPresentationElement pPresElement) {
		return pPresElement instanceof IGraphPresentation ? (IGraphPresentation) pPresElement : null;
	}
	
	//
	//   /// Returns the IGraphPresentation for this TSObjectView
	//   public static IGraphPresentation getETElement(TSEObjectUI pView)
	//   {
	//      return null;
	//   }
	//
	
	   /// Returns the IGraphPresentation for this IDrawEngine
//	   public static IGraphPresentation getETElement(IDrawEngine pDrawEngine) {
//	   	return pDrawEngine.getPresentation() != null? getETElement(pDrawEngine.getPresentation()) : null;
//	   }
	   
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the IETLabel : IETGraphObject
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	//   /// Returns the IETLabel for the input TSNodeLabel
	//   public static IETLabel getETLabel(TSNodeLabel pTSNodeLabel)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the IETLabel for the input TSEdgeLabel
	//   public static IETLabel getETLabel(TSEdgeLabel pTSEdgeLabel)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the IETLabel for the input TSEdgeLabel
	//   public static IETLabel getETLabel(TSLabel pTSLabel)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the IETLabel for this TSGraphObject
	//   public static IETLabel getETLabel(TSObject pObject)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the IETLabel for this TSNodeLabel
	//   public static IETLabel getETLabel(TSENodeLabel pNodeLabel)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the IETLabel for this TSEdgeLabel
	//   public static IETLabel getETLabel(TSEEdgeLabel pEdgeLabel)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the IETLabel for this IPresentationElement
	//   public static IETLabel getETLabel(IPresentationElement pPresElement)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the IETLabel for this TSEObjectUI
	//   public static IETLabel getETLabel(TSEObjectUI pView)
	//   {
	//      return null;
	//   }
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the IPresentationElement
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	
	/*
	 * Returns the IPresentationElement for this IETGraphObject
	 */
	public static IPresentationElement getPresentationElement(IETGraphObject pETGraphObject) {
		return pETGraphObject != null ? pETGraphObject.getPresentationElement() : null;
	}

	
	/*
	 * Returns the IPresentationElement for this TSGraphObject
	 */
	public static IPresentationElement getPresentationElement(TSObject pTSObject) {
		return getPresentationElement(getETGraphObject(pTSObject));
	}
	
	
	/*
	 * Returns the IPresentationElement for this IDrawEngine
	 */
	public static IPresentationElement getPresentationElement(IDrawEngine pDrawEngine) {
		return getPresentationElement(getETGraphObject(pDrawEngine));
	}
	
	/*
    * Returns the IPresentationElement for this ICompartment
	 */
	public static IPresentationElement getPresentationElement( ICompartment compartment )
	{
      return getPresentationElement(getETGraphObject( compartment.getEngine() ));
	}
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the INodePresentation
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	//   /// Returns the INodePresentation for this TSNode
	//   public static INodePresentation getNodePresentation(TSNode pTSNode)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the INodePresentation for this TSENode
	//   public static INodePresentation getNodePresentation(TSENode pTSENode)
	//   {
	//      return null;
	//   }
	//
	// 
	
	
	/* 
	 *  Returns the INodePresentation for this IETGraphObject
	 */
	public static INodePresentation getNodePresentation(IETGraphObject graphObject) {
		return getNodePresentation(getPresentationElement(graphObject));
	}
	
	/*
	 * Returns the INodePresentation for this IGraphPresentation
	 */
	public static INodePresentation getNodePresentation(IPresentationElement pETElement) {
		return pETElement instanceof INodePresentation ? (INodePresentation) pETElement : null;
	}

	/*
	 *  Returns the INodePresentation for this TSObject
	 */
	public static INodePresentation getNodePresentation(TSObject pObject)
   {
		return getNodePresentation(getPresentationElement(pObject));
	}

	/*
	 * Returns the INodePresentation for this IDrawEngine
	 */
	public static INodePresentation getNodePresentation(IDrawEngine pDrawEngine)
   {
		return getNodePresentation(getPresentationElement(pDrawEngine));
	}

	/*
    * Returns the INodePresentation for this ICompartment
	 */
	public static INodePresentation getNodePresentation( ICompartment compartment )
	{
      return getNodePresentation(getPresentationElement( compartment ));
	}

	////////////////////////////////////////////////////////////////////
	//
	// getting the IEdgePresentation
	//
	////////////////////////////////////////////////////////////////////

	public static IEdgePresentation getEdgePresentation(IETGraphObject etGraphObject) {
		IPresentationElement presentationElement = getPresentationElement(etGraphObject);
		if (presentationElement instanceof IEdgePresentation)
			return (IEdgePresentation) presentationElement;
			
		return null;
	}

	/*
	 * Returns the IEdgePresentation for this TSEdge
	 */
	public static IEdgePresentation getEdgePresentation(TSEdge pTSEdge) {
		return pTSEdge instanceof IETEdge ? getEdgePresentation((IETEdge) pTSEdge) : null;
	}

	public static IEdgePresentation getEdgePresentation(IETEdge edge) {
		if (edge != null) {
			IPresentationElement pres = edge.getPresentationElement();
			return pres instanceof IEdgePresentation ? (IEdgePresentation) pres : null;
		} else
			return null;
	}

	/*
	 *  Returns the IEdgePresentation for this TSObject
	 */
	public static IEdgePresentation getEdgePresentation(TSObject pObject) {
		return getEdgePresentation(getETEdge(pObject));
	}

	/*
	 * Returns the IEdgePresentation for this IGraphPresentation
	 */
	public static IEdgePresentation getEdgePresentation(IGraphPresentation pETElement) {
		return pETElement instanceof IEdgePresentation ? (IEdgePresentation) pETElement : null;
	}

	/*
	 * Returns the IEdgePresentation for this IDrawEngine
	 */
	public static IEdgePresentation getEdgePresentation(IDrawEngine pDrawEngine) {
		if (pDrawEngine != null) {
			IGraphPresentation gPres = pDrawEngine.getPresentation();
			if (gPres instanceof IEdgePresentation) {
				return (IEdgePresentation) gPres;
			}
		}

		return null;
	}

   ////////////////////////////////////////////////////////////////////
   //
   // getting the ILabelPresentation
   //
   ////////////////////////////////////////////////////////////////////

   /** Returns the ILabelPresentation for this pTSENodeLabel */
	public static ILabelPresentation getLabelPresentation(TSENodeLabel pTSENodeLabel) {
		return pTSENodeLabel instanceof IETLabel ? getLabelPresentation((IETLabel) pTSENodeLabel) : null;
   }

   /** Returns the ILabelPresentation for this pTSEEdgeLabel */
	public static ILabelPresentation getLabelPresentation(TSEEdgeLabel pTSEEdgeLabel) {
		return pTSEEdgeLabel instanceof IETLabel ? getLabelPresentation((IETLabel) pTSEEdgeLabel) : null;
   }

   /** Returns the ILabelPresentation for the input IETLabel */
	public static ILabelPresentation getLabelPresentation(IETLabel pETLabel) {
		if (pETLabel != null) {
         IPresentationElement pres = pETLabel.getPresentationElement();
			if (pres instanceof IEdgePresentation) {
				return (ILabelPresentation) pres;
         }
		}
      
      return null;
   }
   
	/** Returns the IEdgePresentation for this TSObject */
	public static ILabelPresentation getLabelPresentation(TSObject pObject) {
       return getLabelPresentation(getETLabel(pObject));
    }

    /** Returns the IEdgePresentation for this IGraphPresentation */
	public static ILabelPresentation getLabelPresentation(IGraphPresentation pETElement) {
		return pETElement instanceof ILabelPresentation ? (ILabelPresentation) pETElement : null;
    }

    /** Returns the IEdgePresentation for this IDrawEngine */
	public static ILabelPresentation getLabelPresentation(IDrawEngine pDrawEngine) {
		if (pDrawEngine != null) {
          IGraphPresentation gPres = pDrawEngine.getPresentation();
			if (gPres instanceof ILabelPresentation) {
             return (ILabelPresentation) gPres;
          }
       }
       return null;
    }

	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the IElement
	//   //
	//   ////////////////////////////////////////////////////////////////////

	/*
	 * Returns the IElement for this IPresentationElement
	 */
	public static IElement getElement(IPresentationElement pPresElement) {
		return pPresElement != null ? pPresElement.getFirstSubject() : null;
	}

   /// Returns the IElement for this TSObject
   public static IElement getElement(TSObject pTSObject)
   {
      IElement element = null;
      
      if (pTSObject instanceof TSNode)
      {
         element = getElement( (TSNode)pTSObject );
      }
      else if (pTSObject instanceof TSEdge)
      {
         element = getElement( (TSEdge)pTSObject );
      }
      else
      {
         // We've probably got a label
         IPresentationElement pe = TypeConversions.getPresentationElement( pTSObject );
         if ( pe != null )
         {
            element = pe.getFirstSubject();
         }
      }
      
      return element;
   }
	
	/*
	 * Returns the IElement for this TSNode
	 */
	public static IElement getElement(TSNode pObject) {
		return getElement(getETGraphObject(pObject));

	}

	/*
	 * Returns the IElement for this TSEdge
	 */
	public static IElement getElement(TSEdge pObject) {
		return getElement(getETGraphObject(pObject));
	}

	/*
	 * Returns the IElement for this IETGraphObject
	 */
	public static IElement getElement(IETGraphObject obj) {
		return obj != null ? getElement(obj.getPresentationElement()) : null;
	}

	/*
	 * Returns the IElement for this IDrawEngine
	 */
	public static IElement getElement(IDrawEngine pDrawEngine) {
		return getElement(getETGraphObject(pDrawEngine));
	}

	/*
	 * Returns the IElement for this TSGraphObjectpGraphObject
	 */
	public static IElement getElement(TSGraphObject pGraphObject) {
		return getElement(getETGraphObject(pGraphObject));
	}

	/**
	 * Returns the IElement for this ICompartment.  It does NOT get the element
	 * for the draw engine - that's potentially a different model element and you should
	 * use a different GetElement routine for that one.
	 */
	public static IElement getElement(ICompartment pCompartment) {
		return pCompartment != null ? pCompartment.getModelElement() : null;
	}
	
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the various TS objects
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	
	public static TSENode getOwnerNode(IETGraphObject etGraphObject) {
		return etGraphObject != null && etGraphObject.isNode() ? (TSENode) etGraphObject : null;
	}

	/*
	 *  Returns the owner node for the engine (note it may return 0 if it's not a node)
	 */
	public static TSENode getOwnerNode(IDrawEngine engine) {
		return engine != null ? getOwnerNode(getPresentationElement(engine)) : null;
	}

	/*
	 * Returns the owner node for the presentation element (note it may return 0 if it's an edge)
	 */
	public static TSENode getOwnerNode(IPresentationElement presElement) {
		if (presElement instanceof INodePresentation) {
			INodePresentation nodePresentation = (INodePresentation) presElement;

			TSNode cpNode = nodePresentation.getTSNode();
			
			if (cpNode instanceof TSENode)
				return (TSENode) cpNode;
		}
		return null;
	}

	//
	//   /// Returns the owner node for the ET element (note it may return 0 if it's not a node)
	//   public static TSENode getOwnerNode(IGraphPresentation pETElement)
	//   {
	//      return null;
	//   }

	/*
	 * Returns the owner node for the graph object (note it may return 0 if it's not a node)
	 */
	public static TSENode getOwnerNode(TSGraphObject graphObject) {
		return graphObject instanceof TSNode ? (TSENode) graphObject : null;
	}

	public static TSEEdge getOwnerEdge(IETGraphObject etGraphObject) {
		return etGraphObject != null && etGraphObject.isEdge() ? (TSEEdge) etGraphObject : null;
	}

   /// Returns the owner edge for the presentation element (note it may return 0 if it's a node)
   public static TSEEdge getOwnerEdge(IPresentationElement pPresElement, boolean findLabelsIsEdge )
   {
      TSEEdge foundEdge = null;
      
      if(pPresElement != null) {
      	IEdgePresentation edgePE = null;
      	if(pPresElement instanceof IEdgePresentation)
      		edgePE = (IEdgePresentation)pPresElement;
      		
      	if(edgePE != null) {
      		foundEdge = edgePE.getTSEdge();
      	}
      	else if(findLabelsIsEdge)
      	{
      		if(pPresElement instanceof ILabelPresentation)
      		{
				ILabelPresentation labelPE = (ILabelPresentation)pPresElement;
      			TSLabel label = labelPE.getTSLabel();
      			TSEdgeLabel edgeLabel = null;
      			if(label instanceof TSEdgeLabel) {
      				edgeLabel = (TSEdgeLabel)label;
      			}
      			
      			if(edgeLabel != null) {
      				TSGraphObject tsGraphObject = edgeLabel.getOwner();
      				
      				if(tsGraphObject instanceof TSEEdge)
      					foundEdge = (TSEEdge)tsGraphObject;
      			}
      		}
      	}
      }
      return foundEdge;
   }
	//
	//   /// Returns the owner edge for the presentation element (note it may return 0 if it's a node)
	//   public static TSEdge getOwnerEdge(IPresentationElement pPresElement)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the owner edge for the ET element (note it may return 0 if it's not an edge)
	//   public static TSEEdge getOwnerEdge(IGraphPresentation pETElement)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the owner edge for the graph object (note it may return 0 if it's node an edge)
	//   public static TSEEdge getOwnerEdge(TSGraphObject pGraphObject)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the owner label for the product label (note it may return 0 if it's not a label)
	//   public static TSLabel pLabel(IETLabel pETLabel)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the owner label for the product label (note it may return 0 if it's not a label)
	//   public static TSEEdgeLabel pEdgeLabel(IETLabel pETLabel)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the owner label for the IPresentationElement (note it may return 0 if it's not a label)
	//   public static TSEEdgeLabel pEdgeLabel(IPresentationElement pPresElement)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the Edge label for this graph object, NULL if not an edge label
	//   public static TSEEdgeLabel pEdgeLabel(TSGraphObject pGraphObject)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the owner label for the product label (note it may return 0 if it's not a label)
	//   public static TSENodeLabel pNodeLabel(IETLabel pETLabel)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the owner label for the IPresentationElement (note it may return 0 if it's not a label)
	//   public static TSENodeLabel pNodeLabel(IPresentationElement pPresElement)
	//   {
	//      return null;
	//   }
	//
	//   /// Returns the Node label for this graph object, NULL if not an node label
	//   public static TSENodeLabel pNodeLabel(TSGraphObject pGraphObject)
	//   {
	//      return null;
	//   }
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the IClassifier
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	
	/*
	 * Returns the IClassifier for this drawengine's owner node
	 */
	public static IClassifier getClassifier(IDrawEngine pEngine) {
		if (pEngine != null && pEngine instanceof INodeDrawEngine) {
			return ((INodeDrawEngine) pEngine).getParentClassifier();
	   		}
	      	return null;
	   }
	
	//
	//   /// Returns the IClassifier for this element
	//   public static IClassifier getClassifier(IGraphPresentation pETElement)
	//   {
	//      return null;
	//   }
	//
	
	/*
	 * Returns the IClassifier for this presentation element
	 */
	public static IClassifier getClassifier(IPresentationElement pPE) {
	      return getClassifier(getDrawEngine(pPE));
	}
	
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the IDrawEngine
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	
	/*
	 * Returns the DrawEngine for this ET graph object
	 */
	public static IDrawEngine getDrawEngine(IETGraphObject pETGraphObject) {
		return pETGraphObject != null ? pETGraphObject.getEngine() : null;
	}

	public static IDrawEngine getDrawEngine(TSNode node) {
	   	IETGraphObject obj = getETGraphObject(node);
		return obj != null ? obj.getEngine() : null;
	}

	public static IDrawEngine getDrawEngine(TSEdge node) {
		 IETGraphObject obj = getETGraphObject(node);
		return obj != null ? obj.getEngine() : null;
	}

	/*
	 * Returns the DrawEngine for this presentation element
	 */
	public static IDrawEngine getDrawEngine(IPresentationElement pElement) {
		if (pElement instanceof IGraphPresentation) {
			return ((IGraphPresentation) pElement).getDrawEngine();
		}
		return null;
	}

	public static IDrawEngine getDrawEngine(TSConnector connector) {
      IDrawEngine retObj = null;

		if (connector != null) {
         TSGraphObject gObject = connector.getOwner();
         IETGraphObject obj = getETGraphObject(gObject);
			if (obj != null) {
            retObj = getDrawEngine(obj);
          }
      }

      return retObj;
   }
   
	/** Returns the DrawEngine for this TSObject */
	public static IDrawEngine getDrawEngine(TSObject pObject) {
      IDrawEngine retVal = null;

      IETGraphObject obj = getETGraphObject(pObject);
		if (obj != null) {
         retVal = getDrawEngine(obj);
      }

      return retVal;
   }
   
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the ICompartments
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	//   /// Returns the Compartments for this TSObject
	//   public static ICompartments getCompartments(TSObject pObject)
	//   {
	//      return null;
	//   }
	//
	//   /// Finds the compartment from the node attached to the connector
	//   public static ICompartments getCompartmentFromConnector(TSDConnector pConnector, VARIANT_BOOL bUseOnlyYAxis = false)
	//   {
	//      return null;
	//   }
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the TSEDrawInfo
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	//   /// Returns the TSEDrawInfo for this IDrawInfo
	//   public static TSEDrawInfo getTSEDrawInfo(IDrawInfo pInfo)
	//   {
	//      return null;
	//   }
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the TSObject
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	/** Returns the TSObject for this IGraphPresentation */
	public static TSObject getTSObject(IGraphPresentation pETElement) {
		return pETElement != null ? (TSObject) pETElement.getETGraphObject() : null;
	}

	/** Returns the TSObject for this IPresentationElement */
	public static TSObject getTSObject(IPresentationElement pPresentationElement) {
		return pPresentationElement instanceof IGraphPresentation ? getTSObject((IGraphPresentation) pPresentationElement) : null;
	}
	
	/** Returns the TSObject for the input IDrawEngine */
	public static TSObject getTSObject(IDrawEngine pDrawEngine) {
		return (TSObject) getETGraphObject(pDrawEngine);
	}
	
	/** Returns the TSObject for the input ICompartment */
	public static TSObject getTSObject(ICompartment pCompartment) {
		IDrawEngine engine = pCompartment != null ? pCompartment.getEngine() : null;
		return getTSObject(engine);
   }
	
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the IDiagram
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	
	/*
	 * Returns the IDiagram for this IETGraphObject
	 */
	public static IDiagram getDiagram(IETGraphObject pETGraphObject) {
		return pETGraphObject != null ? pETGraphObject.getDiagram() : null;
	}

	/*
	 * Returns the IDiagram for this IDrawEngine
	 */
	public static IDiagram getDiagram(IDrawEngine pDrawEngine) {
		return getDiagram(getETGraphObject(pDrawEngine));
	}

	/*
	 * Returns the IDiagram for the graph object
	 */
	public static IDiagram getDiagram(TSGraphObject pGraphObject) {
		return getDiagram(getETGraphObject(pGraphObject));
	}

	/*
	 * Returns the IDiagram for the input presentation element
	 */
	public static IDiagram getDiagram(IPresentationElement pPresentationElement) {
		return getDiagram(getETGraphObject(pPresentationElement));
	}

	/*
	 * Get the IETRect rectangle from the IProductGraphPresentation, in the TS logical coordinates
	 */
	public static IETRect getLogicalBoundingRect(IProductGraphPresentation pPGP) {
		return pPGP != null ? pPGP.getBoundingRect() : null;
	}
	
	/*
	 * Get the bounding rectangle from the IPresentationElement, in the TS logical coordinates
	 */
	public static IETRect getLogicalBoundingRect(IPresentationElement pPE) {
	   return getLogicalBoundingRect(pPE, false);
	}
   
	public static IETRect getLogicalBoundingRect(IPresentationElement pPE, boolean bIncludeLabels) {
		return getLogicalBoundingRect(getDrawEngine(pPE), bIncludeLabels);
	}

   /**
    * Get the bounding rectangle that contains all the input IPresentationElements, in the TS logical coordinates
    */
   public static IETRect getLogicalBoundingRect( ETList< IPresentationElement > pes, boolean bIncludeLabels )
   {
      if( null == pes ) throw new IllegalArgumentException();

      IETRect rectEncompassing = new ETRect(0,0,0,0);
      
      for (Iterator iter = pes.iterator(); iter.hasNext();)
      {         
         IPresentationElement presentationElement = (IPresentationElement)iter.next();
//         TSENode node = (TSENode)getTSObject(presentationElement);
//         IETRect rectEngine = new ETRectEx(node.getLocalBounds());
         IETRect rectEngine = TypeConversions.getLogicalBoundingRect( presentationElement, bIncludeLabels );
         rectEncompassing.unionWith( rectEngine );
      }

		if (!rectEncompassing.isZero())
		{
			// "that contains all", we need to add one to the union.
			rectEncompassing.inflate(1, 1);
		}
      return rectEncompassing;
   }

	/// Get the bounding rectangle from the IDrawEngine, in the TS logical coordinates
	public static IETRect getLogicalBoundingRect(IDrawEngine pDrawEngine, boolean bIncludeLabels) {
		return  pDrawEngine != null ? pDrawEngine.getLogicalBoundingRect(bIncludeLabels) : null;
	}
   
	public static IETRect getLogicalBoundingRect(IDrawEngine pDrawEngine) {
	   return getLogicalBoundingRect(pDrawEngine, false);
	}
//
	/// Get the bounding rectangle from the ICompartment, in the TS logical coordinates
	public static IETRect getLogicalBoundingRect(ICompartment pCompartment) {
		return pCompartment != null ? pCompartment.getLogicalBoundingRect() : null;
	}

	/// Returns the IRelationshipDiscovery for this IDiagram
	public static ICoreRelationshipDiscovery getRelationshipDiscovery(IDiagram pDiagram) {
		return pDiagram != null ? pDiagram.getRelationshipDiscovery(): null;
	}

	/*
	 * Returns the IDiagramEngine for this IDiagram
	 */
	public static IDiagramEngine getDiagramEngine(IDiagram pDiagram) {
		if (pDiagram instanceof IUIDiagram) {
			IDrawingAreaControl control = ((IUIDiagram) pDiagram).getDrawingArea();
			if (control != null) {
				return control.getDiagramEngine();
			 }
		 }
		return null;
	}
	
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the IDiagramEngine
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	//   /// Returns the IDiagramEngine for this IDiagram
	//   public static IDiagramEngine getDiagramEngine(IDiagram pDiagram)
	//   {
	//      return null;
	//   }
	//
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // getting the ICoreRelationshipDiscovery
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	//   /// Returns the IRelationshipDiscovery for this IDiagram
	//   public static ICoreRelationshipDiscovery getRelationshipDiscovery(IDiagram pDiagram)
	//   {
	//      return null;
	//   }
	//
	   ////////////////////////////////////////////////////////////////////
	   //
	   // getting the INodePresenation that is the graphical container
	   //
	   ////////////////////////////////////////////////////////////////////
	
	/*
	 * Returns the INodePresenation that is the graphical container for this IPresentationElement
	 */
	public static INodePresentation getGraphicalContainer(IPresentationElement pe) {
		return getGraphicalContainer(getDrawEngine(pe));
	   }
	
	/*
	 * Returns the INodePresenation that is the graphical container for this IDrawEngine
	 */
	public static INodePresentation getGraphicalContainer(IDrawEngine engine) {
		return engine != null ? getNodePresentation(engine.getGraphicalContainer()) : null;
         }
         
	/*
	 * Returns the INodePresenation that is the graphical container for this TSGraphObject
	 */
	public static INodePresentation getGraphicalContainer(TSObject pTSObject) {
		return getGraphicalContainer(getDrawEngine(pTSObject));
	   }
	
   ////////////////////////////////////////////////////////////////////
   //
   // TS Helper funtions
   //
   ////////////////////////////////////////////////////////////////////

   /// TS helper function for comparing TSObjects
   public static boolean areSameTSObjects( TSObject obj1, TSObject obj2 )
   {
      if( (null == obj1) ||
          (null == obj2) ||
          !(obj1 instanceof TSGraphObject) ||
          !(obj2 instanceof TSGraphObject) )
      {
         return false;
      }
      
      return ((TSGraphObject)obj1).getID() == ((TSGraphObject)obj2).getID();
   }
      
	//   ////////////////////////////////////////////////////////////////////
	//   //
	//   // Bounding rectangle functions
	//   //
	//   ////////////////////////////////////////////////////////////////////
	//
	//   /// get the bounding rectangle from the IDrawInfo, in the drawing DC's coordinates
	//   public static Rectangle getBoundingRect(IDrawInfo pInfo)
	//   {
	//      return null;
	//   }
	//
	//   /// get the bounding rectangle from the IDrawEngine, in the draw engine's scaled coordinates
	//   public static Rectangle getBoundingRect(IDrawEngine pDrawEngine)
	//   {
	//      return null;
	//   }
	//
	//   /// get the bounding rectangle from the ICompartment, in the parent draw engine's scaled coordinates
	//   public static Rectangle getBoundingRect(ICompartment pCompartment)
	//   {
	//      return null;
	//   }
	//
	//   /// get the bounding rectangle from the IProductGraphPresentation, in the TS logical coordinates
	//   public static Rectangle getLogicalBoundingRect(IProductGraphPresentation pPGP)
	//   {
	//      return null;
	//   }
	//
	//   /// get the bounding rectangle from the IPresentationElement, in the TS logical coordinates
	//   public static Rectangle getLogicalBoundingRect(IPresentationElement pPE, bool bIncludeLabels = false)
	//   {
	//      return null;
	//   }
	//
	//   /// get the bounding rectangle that contains all the input IPresentationElements, in the TS logical coordinates
	//   public static Rectangle getLogicalBoundingRect(IPresentationElements pPEs, bool bIncludeLabels = false)
	//   {
	//      return null;
	//   }
	//
	//   /// get the bounding rectangle from the IDrawEngine, in the TS logical coordinates
	//   public static Rectangle getLogicalBoundingRect(IDrawEngine pDrawEngine, bool bIncludeLabels = false)
	//   {
	//      return null;
	//   }
	//
	//   /// get the bounding rectangle from the ICompartment, in the TS logical coordinates
	//   public static Rectangle getLogicalBoundingRect(ICompartment pCompartment)
	//   {
	//      return null;
	//   }
	//
	//   /// Combine the TS Logical rectangles, updating the first input
	//   public static void UnionTSLogicalRects(Rectangle & rrectCombined, const Rectangle & rectNew)
	//   {
	//      return null;
	//   }
   
	/**
	 * Function for accessing a specific compartment type from the input 
	 * connector
	 * 
	 * @param connector The reference connector.
	 * @param desiredType The class type of the connector.
	 * @return The compartment.
	 */
	public static ICompartment getCompartment(TSConnector connector,Class desiredType)
   {
		ICompartment retVal = null;
	
		if (connector != null)
		{
         IDrawEngine engine = getDrawEngine(connector);
         if (engine != null)
         {
            IETPoint logicalCenter = new ETPointEx(connector.getCenter());
            IETPoint deviceCenter = engine.getDrawingArea().logicalToDevicePoint(logicalCenter);

            ETList < ICompartment > compartments = engine.getCompartments();
            for (Iterator iter = compartments.iterator(); iter.hasNext();)
            {
               ICompartment element = (ICompartment)iter.next();
               if (desiredType.isAssignableFrom(element.getClass()))
               {
                  if (element.isPointInCompartment(deviceCenter))
                  {
                     retVal = element;
                     break;
                  }
               }
            }
         }
		}
	
		return retVal;
	}
   
   /**
    * Function for accessing a specific compartment type from the input 
    * draw engine
    * 
    * @param connector The reference draw engine.
    * @param desiredType The class type of the connector.
    * @return The compartment.
    */
	public static ICompartment getCompartment(IPresentationElement element, Class desiredType) {
		return getCompartment(getDrawEngine(element), desiredType);
   }

   /**
    * Function for accessing a specific compartment type from the input 
    * draw engine
    * 
    * @param connector The reference draw engine.
    * @param desiredType The class type of the connector.
    * @return The compartment.
    */
	public static ICompartment getCompartment(TSGraphObject element, Class desiredType) {
		return getCompartment(getDrawEngine(getETGraphObject(element)), desiredType);
   }
      
   /**
    * Function for accessing a specific compartment type from the input 
    * draw engine
    * 
    * @param connector The reference draw engine.
    * @param desiredType The class type of the connector.
    * @return The compartment.
    */
	public static ICompartment getCompartment(IDrawEngine engine, Class desiredType) {
		if (engine != null) {
         List compartments = engine.getCompartments();
			for (Iterator iter = compartments.iterator(); iter.hasNext();) {
				ICompartment element = (ICompartment) iter.next();
				if (desiredType.isAssignableFrom(element.getClass()) == true) {
					return element;
            }
         }
      }

		return null;
   }
}

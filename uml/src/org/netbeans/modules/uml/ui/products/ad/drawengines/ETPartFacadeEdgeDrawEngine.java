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
 * Created on Dec 16, 2003
 *
 */
package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author jingmingm
 *
 */
public class ETPartFacadeEdgeDrawEngine extends ETEdgeDrawEngine
{
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("PartFacade");
		}
		return type;
	}

	public void doDraw(IDrawInfo drawInfo)
	{
		super.doDraw(drawInfo);

	}
	
	protected int getLineKind() 
	{
		return DrawEngineLineKindEnum.DELK_DASH;	 
	}
	
	protected int getEndArrowKind()
	{
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
	{
		boolean bFlag = handleStandardLabelSensitivityAndCheck(id, pClass);
		if (!bFlag)
		{
			bFlag = super.setSensitivityAndCheck(id, pClass);
		}
		
		return bFlag;
	}
	
	public boolean onHandleButton(ActionEvent e, String id)
	{
		boolean handled = handleStandardLabelSelection(e, id);
		if (!handled)
		{
			handled = super.onHandleButton(e, id);
		}
		return handled;
	}

	public void onContextMenu(IMenuManager manager)
	{
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_ALL, manager);
		
		super.onContextMenu(manager);
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive
	 *
	 * @param sID A unique identifier for this draw engine.  Used when persisting to the etlp file.
	 */
	public String getDrawEngineID() 
	{
		return "PartFacadeEdgeDrawEngine";
	}

	/**
	 * Is this draw engine valid for the element it is representing?
	 *
	 * @param bIsValid[in] true if this draw engine can correctly represent the attached model element.
	 */
	public boolean isDrawEngineValidForModelElement()
	{
		boolean valid = false;
		String metaType = getMetaTypeOfElement();
		if (metaType.equals("PartFacade"))
		{
			valid = true;
		}
		return valid;
	}

	/**
	 * Returns the metatype of the label manager we should use
	 *
	 * @param return The metatype in essentialconfig.etc that defines the label manager
	 */
// J373-Binding role to design pattern gives link (binding) a label (uses name for role) in class diagram
//	public String getManagerMetaType(int nManagerKind)
//	{
//		return nManagerKind == MK_LABELMANAGER ? "SimpleStereotypeAndNameLabelManager" : "";
//	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("partfacadeedgecolor", Color.BLACK);
		super.initResources();
	}
	
	/**
	 * When a presentation element is selected and VK_DELETE is selected, the user is
	 * asked if the data model should be affected as well.  For part facades we need to
	 * find the collaboration and remove the part facade from the role
	 */
	public void affectModelElementDeletion()
	{
		ETPairT<ICollaboration, IPartFacade> elements = getRelationshipElements();
		ICollaboration pCollaboration = (ICollaboration)elements.getParamOne();
		IPartFacade pPartFacade = (IPartFacade)elements.getParamTwo();

		IConnectableElement pConnectableElement = (IConnectableElement)pPartFacade;
		IParameterableElement pParameterableElement = (IParameterableElement)pPartFacade;

		pCollaboration.removeTemplateParameter(pParameterableElement);
		pCollaboration.removeRole(pConnectableElement);

		// Now reparent the partfacade back to the namespace of the diagram
		IDrawingAreaControl pControl = getDrawingArea();
		if(pControl != null)
		{
			INamespace pNamespace = pControl.getNamespaceForCreatedElements();
			if (pNamespace != null)
			{
				pNamespace.addOwnedElement(pPartFacade);
			}
		}
	}
	
	/**
	 * Returns the ICollaboration and IPartFacade represented by this links relationship
	 *
	 * @param pCollaboration [out] The collaboration attached to this relationship
	 * @param pPartFacade [out] The partfacade attached to this relationship
	 */
	protected ETPairT<ICollaboration, IPartFacade> getRelationshipElements()
	{
		ETPairT<ICollaboration, IPartFacade> retVal = new ETPairT<ICollaboration, IPartFacade>();
		ICollaboration pCollaboration = null;
		IPartFacade pPartFacade = null;
      
		IEdgePresentation pThisEdgePresentation = getEdgePresentationElement();
		if (pThisEdgePresentation != null)
		{
			// Get the ends of the link and break the namespace relationship
			ETPairT<IElement, IElement> elements = pThisEdgePresentation.getEdgeFromAndToElement(false);
			IElement pSourceModelElement = (IElement)elements.getParamOne();
			IElement pTargetModelElement = (IElement)elements.getParamTwo();

			if (pSourceModelElement instanceof ICollaboration)
			{
				pCollaboration = (ICollaboration)pSourceModelElement;
				pPartFacade = (IPartFacade)pTargetModelElement;
			}
			else
			{
				pCollaboration = (ICollaboration)pTargetModelElement;
				pPartFacade = (IPartFacade)pSourceModelElement;
			}
		}
		
		retVal.setParamOne(pCollaboration);
		retVal.setParamTwo(pPartFacade);
		return retVal;
	}
}




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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.event.ActionEvent;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

/*
 *
 * @author KevinM
 *
 */
public class ETCommentEdgeDrawEngine extends ETEdgeDrawEngine {
	
	public ETCommentEdgeDrawEngine()
	{
		super();
	}
	
	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("Comment Edge");
		}
		return type;
	}

	public void doDraw(IDrawInfo drawInfo) {
		super.doDraw(drawInfo);
	}

	public void onContextMenu(IMenuManager manager) {
		// Add the stereotype label pullright
		addStandardLabelsToPullright(StandardLabelKind.SLK_STEREOTYPE, manager);
		super.onContextMenu(manager);
	}

	public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass) {
		boolean retVal = handleStandardLabelSensitivityAndCheck(id, pClass);
		if (!retVal) {
			super.setSensitivityAndCheck(id, pClass);
		}
		return retVal;
	}

	public boolean onHandleButton(ActionEvent e, String id) {
		boolean handled = handleStandardLabelSelection(e, id);
		if (!handled) {
			handled = super.onHandleButton(e, id);
		}
		return handled;
	}

	public String getDrawEngineID() {
		return "CommentEdgeDrawEngine";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getEndArrowKind()
	 */
	protected int getEndArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine#getStartArrowKind()
	 */
	protected int getStartArrowKind() {
		return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
	}

	protected int getLineKind() {
		return DrawEngineLineKindEnum.DELK_DASH;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources()
	{
		this.setLineColor("commentedgecolor", Color.BLACK);
		super.initResources();
	}
        
       
        // Fixed issue 104692.  Permanently deleting a comment link shoulkd not
        // deleting the comment that goes with it.
        /**
         * When a presentation element is selected and VK_DELETE is selected, the user is
         * asked if the data model should be affected as well.  For Comment link, we need to
         * find the annotated element of this link and remove it from the annotated 
         * list of the comment element.  The Comment element stays intact.
         */
        public void affectModelElementDeletion()
        {
            IComment commentEle = null;
            IElement otherEndElem = null;
            IEdgePresentation pThisEdgePresentation = getEdgePresentationElement();
            
            if (pThisEdgePresentation != null)
            {
                 //Get the ends of the link and break the namespace relationship
                ETPairT<IElement, IElement> elements = 
                        pThisEdgePresentation.getEdgeFromAndToElement(false);
                IElement sourceModelElement = elements.getParamOne();
                IElement targetModelElement = elements.getParamTwo();
                
                if (sourceModelElement instanceof IComment) 
                {
                    commentEle = (IComment) sourceModelElement;
                    otherEndElem = targetModelElement;
                }
                else if (targetModelElement instanceof IComment)
                {
                    commentEle = (IComment) targetModelElement;
                    otherEndElem = sourceModelElement;
                }

                if ( otherEndElem != null && otherEndElem instanceof INamedElement)
                {
                    boolean isAnnotated = commentEle.getIsAnnotatedElement(
                            (INamedElement)otherEndElem );
                    if (isAnnotated) 
                    {
                        commentEle.removeAnnotatedElement((INamedElement)otherEndElem);
                    }
                }
            }
        }
}

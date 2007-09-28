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

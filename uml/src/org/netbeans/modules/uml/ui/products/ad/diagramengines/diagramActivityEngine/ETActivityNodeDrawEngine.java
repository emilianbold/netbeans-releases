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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ContainmentTypeEnum;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETContainerDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import com.tomsawyer.editor.TSEColor;

/**
 * @author KevinM
 * The ETActivityNodeDrawEngine provides drawing support for an TSGraphObject.
 * There is a one to one relationship between an TSGraphObject and an ETActivityNodeDrawEngine
 *
 */
public class ETActivityNodeDrawEngine extends ETContainerDrawEngine implements IActivityNodeDrawEngine {

	/*
	 * Default Constructor.
	 */
	public ETActivityNodeDrawEngine() {
		super();
		setContainmentType(ContainmentTypeEnum.CT_GRAPHICAL);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void drawContents(IDrawInfo pDrawInfo) {
		if (pDrawInfo == null)
			return;

		TSEColor textColor = this.getTextColor();

		// draw our frame

		// Draw a dashed, rounded rectangle around the entire node
		//
		//      /--------\   
		//     /          \    
		//    |            |  
		//    |            | 
		//    |    Name    | 
		//    |            | 
		//     \           / 
		//      \--------/
		//

		//CRect boundingRect = CTypeConversions::GetBoundingRect( pInfo );

		GDISupport.drawDashedRoundRect(pDrawInfo.getTSEGraphics(), getDeviceBoundingRectangle(pDrawInfo), getZoomLevel(pDrawInfo), this.getBorderBoundsColor(), this.getBkColor());

		// Draw each compartment
		//handleNameListCompartmentDraw(pInfo,getDeviceBoundingRect(),MIN_NAME_SIZE_X,MIN_NAME_SIZE_Y));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void initCompartments(IPresentationElement pElement) {
		super.initCompartments(pElement);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources() {
		// TODO Auto-generated method stub
		super.initResources();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementHasChanged(INotificationTargets pTargets) {
		// TODO Auto-generated method stub
		return super.modelElementHasChanged(pTargets);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
	 */
	public void sizeToContents() {
		// TODO Auto-generated method stub
		super.sizeToContents();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementDeleted(INotificationTargets pTargets) {
		// TODO Auto-generated method stub
		return super.modelElementDeleted(pTargets);
	}

	public String getDrawEngineID() {
		return "ActivityNodeDrawEngine";
	}

	/**
	 * Used in ResizeToFitCompartment.  Returns the resize behavior
	 * PSK_RESIZE_ASNEEDED     :  Always resize to fit. May grow or shrink.
	 * PSK_RESIZE_EXPANDONLY   :  Grows only if necessary, never shrinks.
	 * PSK_RESIZE_UNLESSMANUAL :  Grows only if the user has not manually resized. Never shrinks.
	 * PSK_RESIZE_NEVER        :  Never resize.
	 *
	 * @param sBehavior [out,retval] The behavior when resize to fit compartment is called.
	 */
	public String getResizeBehavior()
	{
		return "PSK_RESIZE_EXPANDONLY";
	}
	
}
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



package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;

/**
 * @author sumitabhk
 *
 */
public interface IADSequenceDiagEngine extends IADCoreEngine
{
	/** Sequence Diagram specific call from the CDrawingAreaButtonHandler */ 
	public void setShowAllReturnMessages(boolean bShowAllReturnMessages);
	
	/** Sequence Diagram specific call from the CDrawingAreaButtonHandler */ 
	public boolean isShowAllReturnMessages();

	/** Indicates that the auto-message numbers should be displayed. */ 
	public void setShowMessageNumbers(boolean bShowMessageNumbers);
	
	/** Indicates that the auto-message numbers should be displayed. */ 
	public boolean isShowMessageNumbers();

	/** Indicates wether the interaction boundary is already being shown, or not */ 
	public boolean isInteractionBoundaryShowing();

	/** Process the Show Interaction Boundary request */ 
	public void showInteractionBoundary(boolean bIsShowing);

	/** Retrieve the interaction operand that contains the edge and the 
    * compartment that contians the edge.
    */
	public ETPairT < IInteractionOperand, ICompartment > getEdgesInteractionOperand( IETEdge edge );

	/** Find 1st element below the logicial vertical location on this diagram */
	public IElement findFirstElementBelow(String sMetaType, int lY);

	/** Find 1st element above the logicial vertical location on this diagram */
	public IElement findFirstElementAbove(String sMetaType, int lY);

	/** Find 1st draw engine above the logicial vertical location on this diagram */
	public IDrawEngine findFirstDrawEngineAbove(String  sMetaType, int lY);

	/** Find 1st draw engine below the logicial vertical location on this diagram */
	public IDrawEngine findFirstDrawEngineBelow(String sMetaType, int lY);
}



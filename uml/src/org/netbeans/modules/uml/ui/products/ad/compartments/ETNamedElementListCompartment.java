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


//	 $Date$
package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class ETNamedElementListCompartment extends ETListCompartment implements IADNamedElementListCompartment {

	public ETNamedElementListCompartment() {
		super();
	}

	public ETNamedElementListCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
	}

	/**
	 * Initialize this compartment, make it visible, then edit
	 *
	 * @param pParentDrawEngine [in] The parent drawengine for this compartment
	 * @param pElement [in] The IElement belonging to the compartment pCompartment
	 * @param pCompartment [in] The newly created compartment
	 * @param bRedrawNow [in] true to redraw the engine now
	 */
	public void finishAddCompartment(IDrawEngine pParentDrawEngine, IElement pElement, 
									 ICompartment pCompartment, boolean bRedrawNow)
	{
		// attach transition to the compartment
		pCompartment.addModelElement(pElement, -1);
		
		// pumping message allows RT to fire
		pumpMessages();
		
		// force re-paint after insertion
		if (bRedrawNow)
		{
			// clear all selected compartments
			pParentDrawEngine.selectAllCompartments(false);
			
			// select the new compartment
			pCompartment.setSelected(true);
			
			// make sure it's visible and anchored
			ensureVisible(pCompartment, true);
			pParentDrawEngine.setAnchoredCompartment(pCompartment);
			
			// this causes the message pump to run, otherwise the following call to the edit control will
			// load over dead air
			redrawNow();
		}
		
		// edit the compartment
		if (pCompartment instanceof IADNameCompartment)
		{
			pCompartment.editCompartment(true, 0, 0, 0);
		}
		
		// notify drawing it need updating
		setIsDirty();
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADNamedElementListCompartment";
	}
        
    protected DuplicateElementRenameDescriptor showDuplicateElementRenameDialog(
            INamespace targetNS, INamedElement sourceElement)
    {
        DuplicateElementRenamePanel derPanel = new DuplicateElementRenamePanel(
            targetNS, sourceElement, sourceElement.getName());
        
        DuplicateElementRenameDescriptor derDesc = 
            new DuplicateElementRenameDescriptor(
                derPanel, // inner pane
                NbBundle.getMessage(ETNamedElementListCompartment.class, 
                    "LBL_DuplicateElementRenamePanel_Title"), // NOI18N
                true, // modal flag
                NotifyDescriptor.OK_CANCEL_OPTION, // button option type
                NotifyDescriptor.OK_OPTION, // default button
                DialogDescriptor.DEFAULT_ALIGN, // button alignment
                null, // new HelpCtx(""), // NOI18N // TODO: alert Docs
                derPanel); // button action listener
  
//        derPanel.getAccessibleContext().setAccessibleName(NbBundle
//            .getMessage(ETNamedElementListCompartment.class, 
//            "ACSN_DuplicateElementRenameDialog")); // NOI18N
//        
//        derPanel.getAccessibleContext().setAccessibleDescription(NbBundle
//            .getMessage(ETNamedElementListCompartment.class, 
//            "ACSD_DuplicateElementRenameDialog")); // NOI18N
        
        derPanel.requestFocus();
        DialogDisplayer.getDefault().notify(derDesc);
        
        return derDesc;
    }
}

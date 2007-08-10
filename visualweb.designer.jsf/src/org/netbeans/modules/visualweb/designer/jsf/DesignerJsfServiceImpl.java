/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.designer.jsf;

import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.designer.jsf.ui.NotAvailableMultiViewElement;
import org.netbeans.modules.visualweb.spi.designer.jsf.DesignerJsfService;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;

/**
 * Implementation of <code>DesignerJsfService</code>.
 *
 * @author Peter Zavadsky
 */
public class DesignerJsfServiceImpl implements DesignerJsfService {

    private static final DesignerJsfService INSTANCE = new DesignerJsfServiceImpl();

    /** Creates a new instance of DesignerJsfServiceImpl */
    private DesignerJsfServiceImpl() {
    }

    public static DesignerJsfService getDefault() {
        return INSTANCE;
    }

    public MultiViewElement createDesignerMultiViewElement(DataObject jsfJspDataObject) {
//        Designer designer = JsfForm.createDesigner(jsfJspDataObject);
        JsfForm jsfForm = JsfForm.getJsfForm(jsfJspDataObject);
        if (jsfForm == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("Can not create JsfForm for JSF data Object, jsfJspDataObject=" + jsfJspDataObject)); // NOI18N
            return new NotAvailableMultiViewElement();
        }
        
        // XXX #112235 There could be created designer representing external form (e.g. fragment),
        // which doesn't have associated multiview element yet, the one needs to be used.
        Designer designerWithoutMultiView = findDesignerWithoutMultiViewElement(jsfForm);
//        Designer designer = JsfForm.createDesigner(jsfForm);
        Designer designer = designerWithoutMultiView == null ? jsfForm.createDesigner() : designerWithoutMultiView;
        if (designer == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("Can not create Designer for JsfForm, jsfForm=" + jsfForm)); // NOI18N
            return new NotAvailableMultiViewElement();
        }
//        return designer == null ? new NotAvailableMultiViewElement() : new JsfMultiViewElement(jsfForm, designer);
        return JsfForm.createMultiViewElement(jsfForm, designer, jsfJspDataObject);
    }


    /** Find designer without multi view element. */
    private static Designer findDesignerWithoutMultiViewElement(JsfForm jsfForm) {
        Designer[] designers = JsfForm.findDesigners(jsfForm);
        for (Designer candidate : designers) {
            if (candidate != null && JsfForm.findJsfMultiViewElementForDesigner(candidate) == null) {
                return candidate;
            }
        }
        return null;
    }
}

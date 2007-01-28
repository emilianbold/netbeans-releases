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


import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.insync.CustomizerDisplayer;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * XXX Moved from designer.
 *
 *  Listener for beans modified in the Customizer2
 * @todo Make this into a DesignerService instead! Actually move the whole customizer listener
 * into the designer.
 *
 * @author Tor Norbye
 */
class BeanModifyListener implements CustomizerDisplayer.BatchListener {
    /** XXX ??? */
    private MarkupDesignBean filterBean = null;

    public BeanModifyListener() {
    }

    public void beanModifyBegin(MarkupDesignBean bean, FacesModel model) {
        // Discover the right document:
//        WebForm webform = WebForm.get(model);
        JsfForm jsfForm = findWebForm(model);

        if (jsfForm == null) {
            return;
        }

        filterBean = bean;
//        webform.getDomSynchronizer().setUpdatesSuspended(filterBean, true);
        jsfForm.setUpdatesSuspended(filterBean, true);
    }

    public void beanModifyEnd(MarkupDesignBean bean, FacesModel model) {
        // Discover the right document:
//        final WebForm webform = WebForm.get(model);
        JsfForm jsfForm = findWebForm(model);

        if (jsfForm == null) {
            return;
        }

//        webform.getDomSynchronizer().setUpdatesSuspended(filterBean, false);
        jsfForm.setUpdatesSuspended(filterBean, false);
    }

    private static JsfForm findWebForm(FacesModel facesModel) {
        if (facesModel == null) {
            return null;
        }

        FileObject fo = facesModel.getMarkupFile();
        DataObject dobj = null;

        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }

        return JsfForm.getJsfForm(dobj);
    }
}

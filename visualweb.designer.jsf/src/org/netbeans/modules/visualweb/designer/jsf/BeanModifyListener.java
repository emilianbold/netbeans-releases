/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

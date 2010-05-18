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
/*
 * VWPContentItem.java
 *
 * Created on April 13, 2007, 2:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.navigation;

import com.sun.rave.designtime.DesignBean;
import java.awt.Image;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node.Cookie;

/**
 *
 * @author joelle
 */
public class VWPContentItem extends PageContentItem {

    DesignBean designBean;

//    /** Creates a new instance of VWPContentItem */
//    public VWPContentItem( VWPContentModel model, DesignBean bean, String name, String fromString, Image icon, boolean isOutcome )  {
//        super(name, fromString, icon, isOutcome);
//        this.designBean = bean;
//        this.model = model;
//    }
    public VWPContentItem(VWPContentModel model, DesignBean bean, String name, String fromOutcome, Image icon) {
        super(name, fromOutcome, icon);
        assert bean != null;
        assert model != null;

        this.designBean = bean;
        setModel( model );
    }


    @Override
    public void setFromAction(String fromAction) {
//        model.setCaseAction(this, fromOutcome, false);
        super.setFromAction(fromAction);
    }

    @Override
    public void setFromOutcome(String fromOutcome) {
        if (fromOutcome == null) {
            getModel().deleteCaseOutcome(this);
        } else {
            getModel().setCaseOutcome(this, fromOutcome, false);
        }
        super.setFromOutcome(fromOutcome);
    }



    public DesignBean getDesignBean() {
        return designBean;
    }

    private Action[] actions;

    @Override
    public Action[] getActions() {
        if (actions == null) {
            actions = getModel().getActionsFactory().getVWPContentItemActions(this);
        }
        return actions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Cookie> T getCookie(Class<T> type) {
        final PageContentItem item = this;
        if (type.equals(OpenCookie.class)) {
            return (T) new OpenCookie() {
                public void open() {
                    getModel().openPageHandler(item);
                }
            };
        } 
        return super.getCookie(type);
    }

    private WeakReference<VWPContentModel> refVWPContentModel;
    private VWPContentModel getModel() {
        VWPContentModel model = null;
        if( refVWPContentModel != null ) {
            model = refVWPContentModel.get();
        }
        return model;
    }

    private void setModel(VWPContentModel model) {
        this.refVWPContentModel = new WeakReference<VWPContentModel>( model );
    }
}

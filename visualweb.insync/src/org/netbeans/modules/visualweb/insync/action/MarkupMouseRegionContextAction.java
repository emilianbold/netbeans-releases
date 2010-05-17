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

package org.netbeans.modules.visualweb.insync.action;


import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;


/**
 * Action encapsulating <code>MarkupMouseRegion</code> <code>DisplayAction</code>s.
 *
 * @author Peter Zavadsky
 */
public class MarkupMouseRegionContextAction extends AbstractDisplayActionAction {

    /** Creates a new instance of MarkupMouseRegionAction */
    public MarkupMouseRegionContextAction() {
    }

    protected DisplayAction[] getDisplayActions(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return new DisplayAction[0];
        }

        DesignBean designBean = designBeans[0];
        if (designBean instanceof MarkupDesignBean) {
            DesignContext designContext = designBean.getDesignContext();
            // XXX This casting is error-prone, missing api.
            FacesModel facesModel = ((LiveUnit)designContext).getModel();
//            RaveElement raveElement = (RaveElement)((MarkupDesignBean)designBean).getElement();
//            MarkupMouseRegion markupMouseRegion = raveElement.getMarkupMouseRegion();
            Element element = ((MarkupDesignBean)designBean).getElement();
            MarkupMouseRegion markupMouseRegion = FacesPageUnit.getMarkupMouseRegionForElement(element);
            if (markupMouseRegion != null) {
                DisplayAction[] displayActions = markupMouseRegion.getContextItems();
                if (displayActions == null) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new NullPointerException("Incorrect implementation of MarkupMouseRegion," + // NOI18N
                            "it returns null instead of an empty arrray from getContextItems method, markupMouseRegion=" + // NOI18N
                            markupMouseRegion));
                    return new DisplayAction[0];
                }

                return displayActions;
            }
        }

        return new DisplayAction[0];
    }

    protected String getDefaultDisplayName() {
        return NbBundle.getMessage(MarkupMouseRegionContextAction.class, "LBL_MarkupMouseRegionContextActionName");
    }

}

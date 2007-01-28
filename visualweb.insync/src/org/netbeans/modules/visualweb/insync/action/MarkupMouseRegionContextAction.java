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

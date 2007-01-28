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
import org.netbeans.modules.visualweb.insync.live.FacesDesignBean;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import com.sun.rave.propertyeditors.binding.PropertyBindingHelper;
import org.openide.util.NbBundle;


/**
 * Action encapsulating <code>DesignBean</code>'s <code>DesignInfo</code>
 * <code>DisplayAction</code>s.
 *
 * @author Peter Zavadsky
 */
public class PropertyBindingAction extends AbstractDisplayActionAction {

    /** Creates a new instance of DesignBeanAction. */
    public PropertyBindingAction() {
    }

    protected DisplayAction[] getDisplayActions(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return new DisplayAction[0];
        }

        DesignBean designBean = designBeans[0];

        // XXX
        if (designBean instanceof FacesDesignBean) {
            DesignContext designContext = designBean.getDesignContext();
            // XXX This casting is error-prone, missing api.
            FacesModel facesModel = ((LiveUnit)designContext).getModel();
            // XXX Side effect init?
            ((MarkupDesignBean)designBean).getElement();
            return new DisplayAction[] {PropertyBindingHelper.getContextItem(designBean)};
        }

        return new DisplayAction[0];
    }

    protected String getDefaultDisplayName() {
        return NbBundle.getMessage(PropertyBindingAction.class, "LBL_PropertyBindingActionName");
    }

}

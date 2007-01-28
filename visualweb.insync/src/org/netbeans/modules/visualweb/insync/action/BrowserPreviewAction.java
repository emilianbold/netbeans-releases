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
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import org.openide.util.NbBundle;

/**
 * Action showing the page corresponding to specified bean in the browser.
 *
 * @author Peter Zavadsky
 */
public class BrowserPreviewAction  extends AbstractDesignBeanAction {

    /** Creates a new instance of BrowserPreviewAction */
    public BrowserPreviewAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        return NbBundle.getMessage(BrowserPreviewAction.class, "LBL_BrowserPreviewAction");
    }

    protected String getIconBase(DesignBean[] designBeans) {
        return "org/netbeans/modules/visualweb/insync/action/browserPreview.png"; // NOI18N
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return false;
        }

        DesignBean designBean = designBeans[0];
        if (designBean == null) {
            return false;
        }

        DesignContext designContext = designBean.getDesignContext();
        if (designContext == null) {
            return false;
        }

        return Util.isPageRootContainerDesignBean(designContext.getRootContainer());
    }

    protected void performAction(DesignBean[] designBeans) {
        DesignContext context = designBeans[0].getDesignContext();
        FacesModel facesModel = ((LiveUnit)context).getModel();

        new BrowserPreview(facesModel).preview();
    }

}

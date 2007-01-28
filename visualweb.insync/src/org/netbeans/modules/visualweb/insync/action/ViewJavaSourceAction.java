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
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * Action showing the java source.
 *
 * @author Peter Zavadsky
 */
public class ViewJavaSourceAction  extends AbstractDesignBeanAction {

    /** Creates a new instance of ViewJavaSourceAction */
    public ViewJavaSourceAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        return NbBundle.getMessage(ViewJavaSourceAction.class, "LBL_ViewJavaSourceAction");
    }

    protected String getIconBase(DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        return getJavaFileOpenCookie(designBeans) != null;
    }

    protected void performAction(DesignBean[] designBeans) {
        OpenCookie openCookie = getJavaFileOpenCookie(designBeans);
        if (openCookie == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Open cookie on java file is null for designBeans=" + designBeans)); // NOI18N
        } else {
            openCookie.open();
        }
    }


    private static OpenCookie getJavaFileOpenCookie(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return null;
        }
        DesignContext context = designBeans[0].getDesignContext();
        // XXX Casting is error-prone.
        FacesModel facesModel = ((LiveUnit)context).getModel();
        FileObject javaFile = facesModel.getJavaFile();

        if (javaFile == null) {
            return null;
        }

        try {
            DataObject javaDataObject = DataObject.find(javaFile);
            return (OpenCookie)javaDataObject.getCookie(OpenCookie.class);
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return null;
    }
}

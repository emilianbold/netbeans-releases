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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core;

import java.io.IOException;
import org.netbeans.modules.web.core.jsploader.JspLoader;
import org.netbeans.modules.web.core.jsploader.JspDataObject;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;

/**
* EditQueryStringAction.
*
* @author   Petr Jiricka
*/
public class EditQueryStringAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8487176709444303658L;

    /** Actually performs the SwitchOn action.
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return;
        }
        DataObject dObj = (DataObject)(activatedNodes[0]).getCookie(DataObject.class);
        QueryStringCookie qsc = (QueryStringCookie)activatedNodes[0].getCookie(QueryStringCookie.class);

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                                             NbBundle.getBundle(EditQueryStringAction.class).getString("CTL_QueryStringLabel"),
                                             NbBundle.getBundle(EditQueryStringAction.class).getString("CTL_QueryStringTitle"));

        dlg.setInputText(WebExecSupport.getQueryString(dObj.getPrimaryFile()));

        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            try {
                // WebExecSupport.setQueryString(dObj.getPrimaryFile(), dlg.getInputText());
                qsc.setQueryString (dlg.getInputText());
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    /**
    * Returns MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    protected boolean enable (Node[] activatedNodes){
        if (activatedNodes.length == 0) {
            return false;
        }
        for (int i = 0; i < activatedNodes.length; i++){
            DataObject dObj = (DataObject)(activatedNodes[i]).getCookie(DataObject.class);
            QueryStringCookie qsc = (QueryStringCookie)activatedNodes[i].getCookie(QueryStringCookie.class);

            if (qsc == null || dObj == null)
                return false;
            
            if (dObj instanceof JspDataObject){
                String ext = dObj.getPrimaryFile().getExt();
                if (ext.equals(JspLoader.TAGF_FILE_EXTENSION) 
                    || ext.equals(JspLoader.TAGX_FILE_EXTENSION)
                    || ext.equals(JspLoader.TAG_FILE_EXTENSION))
                        return false;
            }
        }
        return true;
    }
    /**
    * Returns QueryStringCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   // ExecCookie.class, DataObject.class
                   QueryStringCookie.class 
               };
    }

    /** @return the action's icon */
    public String getName() {
        return NbBundle.getBundle (EditQueryStringAction.class).getString ("LBL_EditQueryString");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (EditQueryStringAction.class);
    }
}


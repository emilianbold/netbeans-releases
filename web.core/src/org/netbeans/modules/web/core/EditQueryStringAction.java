/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core;

import java.util.ResourceBundle;
import java.io.IOException;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.execution.Executor;
import org.openide.cookies.ExecCookie;
import org.openide.nodes.Node;
import org.openide.loaders.ExecSupport;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

import org.netbeans.modules.j2ee.impl.ServerExecSupport;

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
        DataObject dObj = (DataObject)(activatedNodes[0]).getCookie(DataObject.class);

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                                             NbBundle.getBundle(EditQueryStringAction.class).getString("CTL_QueryStringLabel"),
                                             NbBundle.getBundle(EditQueryStringAction.class).getString("CTL_QueryStringTitle"));

        dlg.setInputText(WebExecSupport.getQueryString(dObj.getPrimaryFile()));

        if (NotifyDescriptor.OK_OPTION.equals(TopManager.getDefault().notify(dlg))) {
            try {
                WebExecSupport.setQueryString(dObj.getPrimaryFile(), dlg.getInputText());
                // PENDING - am I able to fire the change so the node's propertysheet chnges its value ?
            }
            catch (IOException e) {
                TopManager.getDefault().notifyException(e);
            }
        }
    }

    /**
    * Returns MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Adds test of executor for JavaDataObjects */
    protected boolean enable (Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            Node.Cookie c = (activatedNodes[0]).getCookie(QueryStringCookie.class);
            if (c != null)
                return true;
            DataObject dObj = (DataObject)(activatedNodes[0]).getCookie(DataObject.class);
            if (dObj instanceof MultiDataObject) {
                Executor exec = ExecSupport.getExecutor(((MultiDataObject)dObj).getPrimaryEntry());
                if (exec == null) {
                    ServerExecSupport wes = (ServerExecSupport)dObj.getCookie(ServerExecSupport.class);
                    if (wes != null)
                        exec = wes.defaultExecutor();
                }
                return ((exec != null) && (exec instanceof QueryStringCookie));
            }
            else
                return false;
        }
        else
            return false;
    }

    /**
    * Returns ThreadCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   ExecCookie.class, DataObject.class
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

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/web/core/resources/EditQueryString.gif"; // NOI18N
    }
}


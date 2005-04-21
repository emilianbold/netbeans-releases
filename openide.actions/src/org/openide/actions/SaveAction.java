/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.actions;

import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

import java.io.IOException;


/** Save a single object.
* @see SaveCookie
*
* @author   Jan Jancura, Petr Hamernik, Ian Formanek, Dafe Simonek
*/
public class SaveAction extends CookieAction {
    private static Class dataObject;
    private static java.lang.reflect.Method getNodeDelegate;

    public SaveAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[] { SaveCookie.class };
    }

    protected void performAction(final Node[] activatedNodes) {
        SaveCookie sc = (SaveCookie) activatedNodes[0].getCookie(SaveCookie.class);
        assert sc != null : "SaveCookie found for " + activatedNodes[0];

        try {
            sc.save();
            StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(SaveAction.class, "MSG_saved", getSaveMessage(activatedNodes[0]))
            );
        } catch (IOException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, NbBundle.getMessage(SaveAction.class, "EXC_notsaved", getSaveMessage(activatedNodes[0])));
            err.notify(e);
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    /**
     * Extract a suitable after-save message. Will call
     * <code>Node.getValue(&quot;saveName&quot;)</code> to allow the node to
     * supply an appropriate name.  If null, it will simply return the
     * node's display name.
     *
     * @param node that is being saved.
     * @return name that should be printed to the user.
     */
    private String getSaveMessage(Node n) {
        if (dataObject == null) {
            // read the class
            ClassLoader l = (ClassLoader) org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);

            if (l == null) {
                l = getClass().getClassLoader();
            }

            try {
                dataObject = Class.forName("org.openide.loaders.DataObject", true, l); // NOI18N
                getNodeDelegate = dataObject.getMethod("getNodeDelegate", new Class[0]); // NOI18N
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }

        if (getNodeDelegate != null) {
            // try to search for a name on the class
            Object obj = n.getCookie(dataObject);

            if (obj != null) {
                try {
                    n = (Node) getNodeDelegate.invoke(obj, new Object[0]);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }

        return n.getDisplayName();
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(SaveAction.class, "Save");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(SaveAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/save.png"; // NOI18N
    }
}

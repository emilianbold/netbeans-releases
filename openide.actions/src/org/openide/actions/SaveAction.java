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

package org.openide.actions;

import java.io.IOException;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

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
        assert sc != null : "SaveCookie must be present on " + activatedNodes[0] + ". " +
                "See http://www.netbeans.org/issues/show_bug.cgi?id=68285 for details on overriding " + activatedNodes[0].getClass().getName() + ".getCookie correctly.";
        
        // avoid NPE if disabled assertions
        if (sc == null) return ;

        try {
            sc.save();
            StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(SaveAction.class, "MSG_saved", getSaveMessage(activatedNodes[0]))
            );
        } catch (IOException e) {
            Exceptions.attachLocalizedMessage(e,
                                              NbBundle.getMessage(SaveAction.class,
                                                                  "EXC_notsaved",
                                                                  getSaveMessage(activatedNodes[0])));
            Exceptions.printStackTrace(e);
        }
    }

    protected boolean asynchronous() {
        return false;
    }

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
                Exceptions.printStackTrace(ex);
            }
        }

        if (getNodeDelegate != null) {
            // try to search for a name on the class
            Object obj = n.getCookie(dataObject);

            if (obj != null) {
                try {
                    n = (Node) getNodeDelegate.invoke(obj, new Object[0]);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
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

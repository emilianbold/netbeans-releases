/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;
import org.openide.util.actions.CookieAction;

/** Save a single object.
* @see SaveCookie
*
* @author   Jan Jancura, Petr Hamernik, Ian Formanek, Dafe Simonek
*/
public class SaveAction extends CookieAction {
    private static Class<? extends Node.Cookie> dataObject;
    private static java.lang.reflect.Method getNodeDelegate;

    public SaveAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[] { SaveCookie.class };
    }

    protected void performAction(final Node[] activatedNodes) {
        SaveCookie sc = activatedNodes[0].getCookie(SaveCookie.class);
        assert sc != null : "SaveCookie must be present on " + activatedNodes[0] + ". " +
                "See http://www.netbeans.org/issues/show_bug.cgi?id=68285 for details on overriding " + activatedNodes[0].getClass().getName() + ".getCookie correctly.";
        
        // avoid NPE if disabled assertions
        if (sc == null) return ;

        UserQuestionException userEx = null;
        for (;;) {
            try {
                if (userEx == null) {
                    sc.save();
                } else {
                    userEx.confirmed();
                }
                StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(SaveAction.class, "MSG_saved", getSaveMessage(activatedNodes[0]))
                );
            } catch (UserQuestionException ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(ex.getLocalizedMessage(),
                        NotifyDescriptor.YES_NO_OPTION);
                Object res = DialogDisplayer.getDefault().notify(nd);

                if (NotifyDescriptor.OK_OPTION.equals(res)) {
                    userEx = ex;
                    continue;
                }
            } catch (IOException e) {
                Exceptions.attachLocalizedMessage(e,
                                                  NbBundle.getMessage(SaveAction.class,
                                                                      "EXC_notsaved",
                                                                      getSaveMessage(activatedNodes[0]),
                                                                      e.getLocalizedMessage ()));
                Logger.getLogger (getClass ().getName ()).log (Level.SEVERE, null, e);
            }
            break;
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private String getSaveMessage(Node n) {
        if (dataObject == null) {
            // read the class
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

            if (l == null) {
                l = getClass().getClassLoader();
            }

            try {
                dataObject = Class.forName("org.openide.loaders.DataObject", true, l).asSubclass(Node.Cookie.class); // NOI18N
                getNodeDelegate = dataObject.getMethod("getNodeDelegate"); // NOI18N
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

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/save.png"; // NOI18N
    }
}

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

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.Random;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.ManagerNode;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
import org.openide.util.RequestProcessor;

/** Action that can always be invoked and work procedurally.
 * This action will display the URL for the given admin server node in the runtime explorer
 * @author  ludo
 */
public class ShowAdminToolAction extends CookieAction {
    
    protected Class[] cookieClasses() {
        return new Class[] {/* SourceCookie.class */};
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
        // return MODE_ALL;
    }
    
    protected void performAction(Node[] nodes) {
        if(nodes != null && nodes.length == 1) {
            final ManagerNode node = nodes[0].getLookup().lookup(ManagerNode.class);
            if(node != null) {
                // Move this action off AWT thread just in case getDeploymentManager() blocks.
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            if(node.getDeploymentManager().isRunning()) {
                                String url = node.getAdminURL() + "/?" + genRandomString() +
                                        "=" + genRandomString();
                                URLDisplayer.getDefault().showURL(new URL(url));
                            } else {
                                Util.showInformation(NbBundle.getMessage(ShowAdminToolAction.class,
                                        "MESS_START_INSTANCE"));
                            }
                        } catch(MissingResourceException ex) {
                            // this should not happen... If it does, we should find out.
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        } catch(MalformedURLException ex) {
                            // this should not happen... If it does, we should find out.
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        } catch(Exception ex){
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                });
            }
        }
    }
    
    
    private String genRandomString() {
        Random r = new Random();
        return "rs"+r.nextInt();
    }
    
    
    public String getName() {
        return NbBundle.getMessage(ShowAdminToolAction.class, "LBL_ShowAdminGUIAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/j2ee/sun/ide/resources/AddInstanceActionIcon.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return null; // HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RefreshAction.class);
    }
    
    protected boolean enable(Node[] nodes) {
        return (nodes != null) && (nodes.length == 1); // true;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    
}

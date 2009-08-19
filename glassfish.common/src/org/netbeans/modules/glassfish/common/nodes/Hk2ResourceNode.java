/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common.nodes;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;
import org.netbeans.modules.glassfish.common.PartialCompletionException;
import org.netbeans.modules.glassfish.common.CommandRunner;
import org.netbeans.modules.glassfish.common.nodes.actions.EditDetailsCookie;
import org.netbeans.modules.glassfish.common.nodes.actions.UnregisterResourceCookie;
import org.netbeans.modules.glassfish.common.ui.BasePanel;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.ResourceDecorator;
import org.netbeans.modules.glassfish.spi.ResourceDesc;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Williams
 */
public class Hk2ResourceNode extends Hk2ItemNode {

    public Hk2ResourceNode(final Lookup lookup, final ResourceDesc resource, 
            final ResourceDecorator decorator, final Class customizer) {
        super(Children.LEAF, lookup, resource.getName(), decorator);
        setDisplayName(resource.getName());
        setShortDescription("<html>name: " + resource.getName() + "</html>");

        if(decorator.canUnregister()) {
            getCookieSet().add(new UnregisterResourceCookie() {

                private volatile WeakReference<Future<OperationState>> status;

                public Future<OperationState> unregister() {
                    Future<OperationState> result = null;
                    GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
                    if(commonModule != null) {
                        CommandRunner mgr = new CommandRunner(commonModule.getInstanceProperties());
                        result = mgr.unregister(resource.getName(), resource.getCommandSuffix(),
                                decorator.getCmdPropertyName(), decorator.isCascadeDelete());
                        status = new WeakReference<Future<OperationState>>(result);
                    }
                    return result;
                }

                public boolean isRunning() {
                    WeakReference<Future<OperationState>> localref = status;
                    if(localref == null) {
                        return false;
                    }
                    Future<OperationState> cmd = localref.get();
                    if(cmd == null || cmd.isDone()) {
                        return false;
                    }
                    return true;
                }

            });
        }

        if (decorator.canEditDetails()) {
            GlassfishModule m = lookup.lookup(GlassfishModule.class);
            if (null != m) {
                String rootDir = m.getInstanceProperties().get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
                if (ServerUtilities.isTP2(rootDir)) {
                    // don't add the edit details cookie
                } else {
                    // add the editor cookie
                    getCookieSet().add(new EditDetailsCookie() {

                        public void openCustomizer() {
                            final BasePanel retVal = getBasePanel();
                            RequestProcessor.getDefault().post(new Runnable() {

                                // fetch the data for the BasePanel
                                public void run() {
                                    GlassfishModule commonSupport = lookup.lookup(GlassfishModule.class);
                                    if (commonSupport != null) {
                                        //try {
                                        java.util.Map<String, String> ip = commonSupport.getInstanceProperties();
                                        CommandRunner mgr = new CommandRunner(ip);
                                        if (!GlassfishModule.JDBC_RESOURCE.equals(resource.getCommandSuffix())) {
                                            retVal.initializeData(getDisplayName(), mgr.getResourceData(getDisplayName()));
                                        } else {
                                            // jdbc resources need to get connection pool data also, so we cannot
                                            // filter by the name of the resource here.
                                            retVal.initializeData(getDisplayName(), mgr.getResourceData(null));
                                        }
                                    //}
                                    }
                                }
                            });
                            DialogDescriptor dd = new DialogDescriptor(retVal,
                                    NbBundle.getMessage(this.getClass(), "TITLE_RESOURCE_EDIT", getDisplayName()),
                                    false,
                                    new ActionListener() {

                                        public void actionPerformed(ActionEvent event) {
                                            if (event.getSource().equals(NotifyDescriptor.OK_OPTION)) {
                                                // write the data back to the server
                                                GlassfishModule commonSupport = lookup.lookup(GlassfishModule.class);
                                                if (commonSupport != null) {
                                                    //try {
                                                    java.util.Map<String, String> ip = commonSupport.getInstanceProperties();
                                                    CommandRunner mgr = new CommandRunner(ip);
                                                    //retVal.initializeData(getDisplayName(), mgr.getResourceData(getDisplayName()));
                                                    try {
                                                        mgr.putResourceData(retVal.getData());
                                                    } catch (PartialCompletionException pce) {
                                                        Exceptions.printStackTrace(pce);
                                                    }
                                                //}
                                                }
                                            }
                                        }
                                    });
                            Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                            d.setVisible(true);
                        }

                        private BasePanel getBasePanel() {
                            BasePanel temp;
                            try {
                                temp = (BasePanel) customizer.getConstructor().newInstance();
                            } catch (InstantiationException ex) {
                                temp = new BasePanel.Error();
                                Exceptions.printStackTrace(ex);
                            } catch (IllegalAccessException ex) {
                                temp = new BasePanel.Error();
                                Exceptions.printStackTrace(ex);
                            } catch (IllegalArgumentException ex) {
                                temp = new BasePanel.Error();
                                Exceptions.printStackTrace(ex);
                            } catch (InvocationTargetException ex) {
                                temp = new BasePanel.Error();
                                Exceptions.printStackTrace(ex);
                            } catch (NoSuchMethodException ex) {
                                temp = new BasePanel.Error();
                                Exceptions.printStackTrace(ex);
                            } catch (SecurityException ex) {
                                temp = new BasePanel.Error();
                                Exceptions.printStackTrace(ex);
                            }
                            return temp;
                        }
                    });
                }

            }

        }
    }
}

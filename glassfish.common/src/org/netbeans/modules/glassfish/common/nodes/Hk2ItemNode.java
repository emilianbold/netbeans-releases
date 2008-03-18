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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.glassfish.common.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.netbeans.modules.glassfish.common.CommandRunner;
import org.netbeans.modules.glassfish.common.nodes.actions.DeployDirectoryCookie;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesCookie;
import org.netbeans.spi.glassfish.AppDesc;
import org.netbeans.spi.glassfish.Decorator;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;


/**
 * Extensible node.
 * 
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2ItemNode extends AbstractNode {
    
    private Decorator decorator;
    
    private static final String HTTP_HEADER = "http://";
    
    private static final int TIMEOUT = 30000;
    
    private Hk2ItemNode(Children children, final Lookup lookup, final Decorator decorator) {
        super(children);
        this.decorator = decorator;
        
        // NEW COOKIE CODE
        if(decorator.isRefreshable()) {
            getCookieSet().add(new RefreshModulesCookie() {
                public void refresh() {
                    Children children = getChildren();
                    if(children instanceof Refreshable) {
                        ((Refreshable) children).updateKeys();
                    }
                }
            });
        }
        
        if(decorator.canDeployTo()) {
            getCookieSet().add(new DeployDirectoryCookie() {
                public void deployDirectory() {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle(NbBundle.getMessage(Hk2ItemNode.class, "LBL_ChooseButton")); // NOI18N
                    chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setMultiSelectionEnabled(false);
                    
                    int returnValue = chooser.showDialog(WindowManager.getDefault().getMainWindow(),
                            NbBundle.getMessage(Hk2ItemNode.class, "LBL_ChooseButton")); // NOI18N
                    if(returnValue != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    
                    final File dir = new File(chooser.getSelectedFile().getAbsolutePath());
                    
                    GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
                    if(commonModule != null) {
                        CommandRunner mgr = new CommandRunner(commonModule.getInstanceProperties());
                        mgr.deploy(dir);
                    }
                }
            }); 
        }
        
        // OLD COOKIE CODE
//        if(decorator.equals(J2EE_APPLICATION_FOLDER) ||
//                decorator.equals(REFRESHABLE_FOLDER)) {
//            getCookieSet().add(new RefreshModulesCookie() {
//                public void refresh() {
//                    Children children = getChildren();
//                    if(children instanceof Refreshable)
//                        ((Refreshable)children).updateKeys();
//                }
//            });
//            getCookieSet().add(new DeployDirectoryCookie() {
//                private boolean isRunning = false;
//                public void deployDirectory() {
//                    
//                    JFileChooser chooser = new JFileChooser();
//                    chooser.setDialogTitle(NbBundle.getMessage(Hk2ItemNode.class, "LBL_ChooseButton")); //NOI18N
//                    chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
//                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//                    //chooser.setApproveButtonMnemonic("Choose_Button_Mnemonic".charAt(0)); //NOI18N
//                    chooser.setMultiSelectionEnabled(false);
//                    
//                    //chooser.setAcceptAllFileFilterUsed(false);
//                    //chooser.setApproveButtonToolTipText(NbBundle.getMessage(Hk2ItemNode.class, "LBL_ChooserName")); //NOI18N
//                    //chooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(Hk2ItemNode.class, "LBL_ChooserName")); //NOI18N
//                    //chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Hk2ItemNode.class, "LBL_ChooserName")); //NOI18N
//                    
//                    int returnValue = chooser.showDialog(new JFrame(), NbBundle.getMessage(Hk2ItemNode.class, "LBL_ChooseButton")); //NOI18N
//                    
//                    if(returnValue != JFileChooser.APPROVE_OPTION){
//                        return;
//                        
//                    }
//                    
//                    final File dir=new File(chooser.getSelectedFile().getAbsolutePath());
//                    final Hk2DeploymentManager dm =
//                            (Hk2DeploymentManager)lookup.lookup(Hk2DeploymentManager.class);
//                    final String message=NbBundle.getMessage(Hk2ItemNode.class,
//                            "LBL_DeployProgress", chooser.getSelectedFile().getAbsolutePath());
//                    final ProgressHandle handle = ProgressHandleFactory.createHandle(message);
//                    
//                    Runnable r = new Runnable() {
//                        public void run() {
//                            isRunning = true;
//                            
//                            // Save the current time so that we can deduct that the deploy
//                            // failed due to timeout
//                            long start = System.currentTimeMillis();
//                            FastDeploy g= new FastDeploy(dm);
//                            ProgressObject o =g.initialDeploy(null, dir,dir.getName()) ;
//                            handle.progress(o.getDeploymentStatus().getMessage());
//                            
//                            
//                            while(!(o.getDeploymentStatus().isCompleted()||o.getDeploymentStatus().isFailed()) && System.currentTimeMillis() - start < TIMEOUT) {
//                                //                                System.out.println("o.getDeploymentStatus()"+o.getDeploymentStatus());
//                                handle.progress(o.getDeploymentStatus().getMessage());
//                                try {
//                                    Thread.sleep(500);
//                                } catch(InterruptedException ex) {
//                                    // Nothing to do
//                                }
//                            }
//                            handle.progress(o.getDeploymentStatus().getMessage());
//                            handle.finish();
//                            
//                            NotifyDescriptor d = new NotifyDescriptor.Message(o.getDeploymentStatus().getMessage(), NotifyDescriptor.INFORMATION_MESSAGE);
//                            d.setTitle(message);
//                            DialogDisplayer.getDefault().notify(d);
//                            isRunning = false;
//                        }
//                    };
//                    
//                    handle.start();
//                    RequestProcessor.getDefault().post(r);
//                }
//            });        } else if(type.equals(ItemType.J2EE_APPLICATION)) {
//                getCookieSet().add(new OpenURLActionCookie() {
//                    public String getWebURL() {
//                        if(module == null || lookup == null)
//                            return null;
//                        
//                        try {
//                            Hk2DeploymentManager dm = (Hk2DeploymentManager)lookup.lookup(Hk2DeploymentManager.class);
//                            String app =  module.getModuleID();
//                            
//                            
//                            
//                            InstanceProperties ip = dm.getInstanceProperties();
//                            
//                            String host = ip.getProperty(Hk2PluginProperties.PROPERTY_HOST);
//                            String httpPort = ip.getProperty(InstanceProperties.HTTP_PORT_NUMBER);
//                            if(app == null || host == null || httpPort == null)
//                                return null;
//                            
//                            return HTTP_HEADER + host + ":" + httpPort + "/"+app+"/";
//                        } catch (Throwable t) {
//                            return null;
//                        }
//                    }
//                });
//                getCookieSet().add(new UndeployModuleCookie() {
//                    private boolean isRunning = false;
//                    
//                    public Task undeploy() {
//                        final Hk2DeploymentManager dm =
//                                (Hk2DeploymentManager)lookup.lookup(Hk2DeploymentManager.class);
//                        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Hk2ItemNode.class,
//                                "LBL_UndeployProgress", ((Hk2TargetModuleID)module).getModuleID()));
//                        
//                        Runnable r = new Runnable() {
//                            public void run() {
//                                isRunning = true;
//                                
//                                // Save the current time so that we can deduct that the undeploy
//                                // failed due to timeout
//                                long start = System.currentTimeMillis();
//                                
//                                ProgressObject o = dm.undeploy(new TargetModuleID[] {module});
//                                
//                                while(!o.getDeploymentStatus().isCompleted() && System.currentTimeMillis() - start < TIMEOUT) {
//                                    //                                System.out.println("o.getDeploymentStatus()"+o.getDeploymentStatus());
//                                    try {
//                                        Thread.sleep(500);
//                                    } catch(InterruptedException ex) {
//                                        // Nothing to do
//                                    }
//                                }
//                                handle.progress(o.getDeploymentStatus().getMessage());
//                                handle.finish();
//                                isRunning = false;
//                            }
//                        };
//                        
//                        handle.start();
//                        return RequestProcessor.getDefault().post(r);
//                    }
//                    
//                    public synchronized boolean isRunning() {
//                        return isRunning;
//                    }
//                });
//            } else if (decorator.equals(JDBC_NATIVE_DATASOURCES) ||
//                    decorator.equals(JDBC_MANAGED_DATASOURCES) ||
//                    decorator.equals(CONNECTION_POOLS)) {
//                getCookieSet().add(new UndeployModuleCookie() {
//                    private boolean isRunning = false;
//                    
//                    public Task undeploy() {
//                        final Hk2DeploymentManager dm =(Hk2DeploymentManager) lookup.lookup(Hk2DeploymentManager.class);
//                        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Hk2ItemNode.class,
//                                "LBL_UndeployProgress", getDisplayName()));
//                        
//                        Runnable r = new Runnable() {
//                            public void run() {
//                                isRunning = true;
//                                
//                                //////      ludo                      Hk2DatasourceManager dsManager = new Hk2DatasourceManager(dm);
//                                //////
//                                //////                            // Undeploying
//                                //////                            if(type.equals(ItemType.JDBC_NATIVE_DATASOURCES)) {
//                                //////                                dsManager.undeployNativeDataSource(getDisplayName());
//                                //////                            } else if (type.equals(ItemType.JDBC_MANAGED_DATASOURCES)) {
//                                //////                                dsManager.undeployManagedDataSource(getDisplayName());
//                                //////                            } else if (type.equals(ItemType.CONNECTION_POOLS)) {
//                                //////                                dsManager.undeployConnectionPool(getDisplayName());
//                                //////                            }
//                                //////
//                                handle.finish();
//                                isRunning = false;
//                            }
//                        };
//                        
//                        handle.start();
//                        return RequestProcessor.getDefault().post(r);
//                    }
//                    
//                    public synchronized boolean isRunning() {
//                        return isRunning;
//                    }
//                });
//            }
    }
    
    public Hk2ItemNode(Lookup lookup, AppDesc app, Decorator decorator) {
        this(Children.LEAF, lookup, decorator);
        setDisplayName(app.getName());
        setShortDescription("<html>name: " + app.getName() + "<br>path: " + app.getPath() + "</html>");
    }
    
    public Hk2ItemNode(Lookup lookup, Children children, String name, Decorator type) {
        this(children, lookup, type);
        setDisplayName(name);
    }
    
    @Override
    public Image getIcon(int type) {
        Image image = null;
        Image badge = decorator.getIconBadge();
        if(badge != null) {
            image = badgeFolder(badge, false);
        } else {
            image = decorator.getIcon(type);
        }
        return image != null ? image : getIconDelegate().getIcon(type);
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        Image image = null;
        Image badge = decorator.getIconBadge();
        if(badge != null) {
            image = badgeFolder(badge, true);
        } else {
            image = decorator.getOpenedIcon(type);
        }
        return image != null ? image : getIconDelegate().getOpenedIcon(type);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();

        if(decorator.isRefreshable()) {
            actions.add(SystemAction.get(RefreshModulesAction.class));
        }
        
        if(decorator.canDeployTo()) {
//            actions.add(SystemAction.get(DeployDirectoryAction.class));
        }
        
        if(decorator.canUndeploy()) {
//            actions.add(SystemAction.get(UndeployModuleAction.class));
        }
    
        if(decorator.canShowBrowser()) {
//            actions.add(SystemAction.get(OpenURLAction.class));
        }
        
        return actions.toArray(new Action[actions.size()]);
    }
    
    /* Creates and returns the instance of the node
     * representing the status 'WAIT' of the node.
     * It is used when it spent more time to create elements hierarchy.
     * @return the wait node.
     */
    public static Node createWaitNode() {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setName(NbBundle.getMessage(Hk2ItemNode.class, "LBL_WaitNode_DisplayName")); //NOI18N
        node.setIconBaseWithExtension("org/openide/src/resources/wait.gif"); // NOI18N
        return node;
    }

    /**
     * Applies a badge to an open or closed folder icon.
     * 
     * @param badge badge image for folder
     * @param opened use open or closed folder
     * @return an image of the badged folder
     */
    public static Image badgeFolder(Image badge, boolean opened) {
        Node folderNode = getIconDelegate();
        Image folder = opened ? folderNode.getOpenedIcon(BeanInfo.ICON_COLOR_16x16) : 
                folderNode.getIcon(BeanInfo.ICON_COLOR_16x16);
        return Utilities.mergeImages(folder, badge, 7, 7);
    }
    
    /**
     * Retrieves the IDE's standard folder node, so we can access the default
     * open/closed folder icons.
     * 
     * @return standard folder node
     */
    private static Node getIconDelegate() {
        return DataFolder.findFolder(Repository.getDefault().
                getDefaultFileSystem().getRoot()).getNodeDelegate();
    }
    
    public static Decorator J2EE_APPLICATION_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public boolean canDeployTo() { return true; }
    };
    
    public static Decorator J2EE_APPLICATION = new Decorator() { 
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canShowBrowser() { return true; }
    };
    
    public static Decorator REFRESHABLE_FOLDER = new Decorator() { 
        @Override public boolean isRefreshable() { return true; }
        @Override public boolean canDeployTo() { return true; }
    };
    
}
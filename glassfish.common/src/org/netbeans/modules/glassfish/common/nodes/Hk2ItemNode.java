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
import org.netbeans.spi.glassfish.GlassfishModule;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;


/**
 * Extensible node.
 * 
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2ItemNode extends AbstractNode {
    
    private ItemType type;
    
    private static final String HTTP_HEADER = "http://";
    
    private static final int TIMEOUT = 30000;
    
    private Hk2ItemNode(Children children, final Lookup lookup, final ItemType type) {
        super(children);
        this.type = type;
        
        // NEW COOKIE CODE
        if(type.isRefreshable()) {
            getCookieSet().add(new RefreshModulesCookie() {
                public void refresh() {
                    Children children = getChildren();
                    if(children instanceof Refreshable) {
                        ((Refreshable) children).updateKeys();
                    }
                }
            });
        }
        
        if(type.canDeployTo()) {
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
        if(type.equals(ItemType.J2EE_APPLICATION_FOLDER) ||
                type.equals(ItemType.REFRESHABLE_FOLDER)) {
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
            } else if (type.equals(ItemType.JDBC_NATIVE_DATASOURCES) ||
            type.equals(ItemType.JDBC_MANAGED_DATASOURCES) ||
            type.equals(ItemType.CONNECTION_POOLS)) {
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
            }
    }
    
    public Hk2ItemNode(Lookup lookup, AppDesc app, ItemType type) {
        this(Children.LEAF, lookup, type);
        setDisplayName(app.getName());
        setShortDescription("<html>name: " + app.getName() + "<br>path: " + app.getPath() + "</html>");
    }
    
    public Hk2ItemNode(Lookup lookup, Children children, String name, ItemType type) {
        this(children, lookup, type);
        setDisplayName(name);
    }
    
    /** 
     * !PW FIXME Need logic to delegate icon display to decorator in node or
     * instance lookup.  Then JavaEE module can provide icons for EE nodes and
     * Ruby module provide icon for Ruby nodes independent of each other.
     *
     * @param type
     * @return
     */
    @Override
    public Image getIcon(int type) {
//        if(this.type.equals(ItemType.J2EE_APPLICATION_FOLDER)) {
//            return UISupport.getIcon(ServerIcon.EAR_FOLDER);
//        } else if(this.type.equals(ItemType.J2EE_APPLICATION) ||
//                this.type.equals(ItemType.J2EE_APPLICATION_SYSTEM)) {
//            return UISupport.getIcon(ServerIcon.EAR_ARCHIVE);
//        } else if(this.type.equals(ItemType.JDBC_MANAGED_DATASOURCES) ||
//                this.type.equals(ItemType.JDBC_NATIVE_DATASOURCES)) {
//            return Utilities.loadImage(JDBC_RESOURCE_ICON);
//        } else if(this.type.equals(ItemType.CONNECTION_POOLS)) {
//            return Utilities.loadImage(CONNECTION_POOL_ICON);
//        } else {
            return getIconDelegate().getIcon(type);
//        }
    }
    
    /** 
     * !PW FIXME Need logic to delegate icon display to decorator in node or
     * instance lookup.  Then JavaEE module can provide icons for EE nodes and
     * Ruby module provide icon for Ruby nodes independent of each other.
     *
     * @param type
     * @return
     */
    @Override
    public Image getOpenedIcon(int type) {
//        if(this.type.equals(ItemType.J2EE_APPLICATION_FOLDER)) {
//            return UISupport.getIcon(ServerIcon.EAR_OPENED_FOLDER);
//        } else if(this.type.equals(ItemType.J2EE_APPLICATION) ||
//                this.type.equals(ItemType.J2EE_APPLICATION_SYSTEM) ||
//                this.type.equals(ItemType.JDBC_MANAGED_DATASOURCES) ||
//                this.type.equals(ItemType.JDBC_NATIVE_DATASOURCES) ||
//                this.type.equals(ItemType.CONNECTION_POOLS)) {
//            return getIcon(type);
//        } else {
            return getIconDelegate().getOpenedIcon(type);
//        }
    }
    
    private Node getIconDelegate() {
        return DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();

        if(type.isRefreshable()) {
            actions.add(SystemAction.get(RefreshModulesAction.class));
        }
        
        if(type.canDeployTo()) {
//            actions.add(SystemAction.get(DeployDirectoryAction.class));
        }
        
        if(type.canUndeploy()) {
//            actions.add(SystemAction.get(UndeployModuleAction.class));
        }
    
        if(type.canShowBrowser()) {
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
     * Enumeration of node types.  Allows single location of logic for determining
     * node action availability.
     */
    public static enum ItemType {
        
        J2EE_APPLICATION_FOLDER { 
            @Override public boolean isRefreshable() { return true; }
            @Override public boolean canDeployTo() { return true; }
        },
        J2EE_APPLICATION { 
            @Override public boolean canUndeploy() { return true; }
            @Override public boolean canShowBrowser() { return true; }
        },
        REFRESHABLE_FOLDER { 
            @Override public boolean isRefreshable() { return true; }
            @Override public boolean canDeployTo() { return true; }
        },
        JDBC_MANAGED_DATASOURCES { 
            @Override public boolean canUndeploy() { return true; }
        },
        JDBC_NATIVE_DATASOURCES {
            @Override public boolean canUndeploy() { return true; }
        },
        CONNECTION_POOLS {
            @Override public boolean canUndeploy() { return true; }
        };
        
        public boolean isRefreshable() { return false; }
        public boolean canDeployTo() { return false; }
        public boolean canUndeploy() { return false; }
        public boolean canShowBrowser() { return false; }

    }
 
}
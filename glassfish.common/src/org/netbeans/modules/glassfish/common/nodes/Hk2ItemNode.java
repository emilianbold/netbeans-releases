/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.netbeans.modules.glassfish.common.CommandRunner;
import org.netbeans.modules.glassfish.common.nodes.actions.DeployDirectoryCookie;
import org.netbeans.modules.glassfish.common.nodes.actions.EditDetailsAction;
import org.netbeans.modules.glassfish.common.nodes.actions.OpenURLAction;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.glassfish.common.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.glassfish.common.nodes.actions.UndeployModuleCookie;
import org.netbeans.modules.glassfish.common.nodes.actions.UnregisterResourceAction;
import org.netbeans.modules.glassfish.spi.Decorator;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.ResourceDecorator;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
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
    
    protected final Decorator decorator;
    
    protected Hk2ItemNode(Children children, final Lookup lookup, final String name, final Decorator decorator) {
        super(children);
        this.decorator = decorator;
        
        if(decorator.isRefreshable()) {
            getCookieSet().add(new RefreshModulesCookie() {
                public void refresh() {
                    refresh(null, null);
                }

                public void refresh(String expected, String unexpected) {
                    Children children = getChildren();
                    if(children instanceof Refreshable) {
                        ((Refreshable) children).updateKeys();
                        boolean foundExpected = expected == null ? true : false;
                        boolean foundUnexpected = false;
                        for (Node node : children.getNodes()) {
                            if (!foundExpected && node.getDisplayName().equals(expected))
                                foundExpected = true;
                            if (!foundUnexpected && node.getDisplayName().equals(unexpected))
                                foundUnexpected = true;
                        }
                        if (!foundExpected) {
                            Logger.getLogger("glassfish").log(Level.WARNING, null, new IllegalStateException("did not find a child node, named "+expected));
                        }
                        if (foundUnexpected) {
                            Logger.getLogger("glassfish").log(Level.WARNING, null, new IllegalStateException("found unexpected child node, named "+unexpected));
                        }
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
                        CommandRunner mgr = new CommandRunner(true, commonModule.getCommandFactory(), commonModule.getInstanceProperties());
                        mgr.deploy(dir);
                    }
                }
            }); 
        }
        
        if(decorator.canUndeploy()) {
            getCookieSet().add(new UndeployModuleCookie() {
                
                private volatile WeakReference<Future<OperationState>> status;

                public Future<OperationState> undeploy() {
                    Future<OperationState> result = null;
                    GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
                    if(commonModule != null) {
                        CommandRunner mgr = new CommandRunner(true, commonModule.getCommandFactory(), commonModule.getInstanceProperties());
                        result = mgr.undeploy(name);
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

//        if(decorator.canUnregister()) {
//            getCookieSet().add(new UnregisterResourceCookie() {
//
//                private volatile WeakReference<Future<OperationState>> status;
//
//                public Future<OperationState> unregister() {
//                    Future<OperationState> result = null;
//                    GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
//                    if(commonModule != null) {
//                        CommandRunner mgr = new CommandRunner(commonModule.getInstanceProperties());
//                        result = mgr.unregister(name, suffix);
//                        status = new WeakReference<Future<OperationState>>(result);
//                    }
//                    return result;
//                }
//
//                public boolean isRunning() {
//                    WeakReference<Future<OperationState>> localref = status;
//                    if(localref == null) {
//                        return false;
//                    }
//                    Future<OperationState> cmd = localref.get();
//                    if(cmd == null || cmd.isDone()) {
//                        return false;
//                    }
//                    return true;
//                }
//
//            });
//        }
    }
    
//    public Hk2ItemNode(final Lookup lookup, final AppDesc app, Decorator decorator) {
//        this(Children.LEAF, lookup, app.getName(), decorator);
//        setDisplayName(app.getName());
//        setShortDescription("<html>name: " + app.getName() + "<br>path: " + app.getPath() + "</html>");
//
//        // !PW FIXME should method of retrieving context root be controlled by decorator?
//        if(decorator.canShowBrowser()) {
//            getCookieSet().add(new OpenURLActionCookie() {
//                public String getWebURL() {
//                    String result = null;
//                    GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
//                    if(commonModule != null) {
//                        Map<String, String> ip = commonModule.getInstanceProperties();
//                        String host = ip.get(GlassfishModule.HOSTNAME_ATTR);
//                        String httpPort = ip.get(GlassfishModule.HTTPPORT_ATTR);
//                        result = HTTP_HEADER + host + ":" + httpPort + "/" + app.getContextRoot() + "/";
//                    }
//                    return result;
//                }
//            });
//        }
//    }
//
//    public Hk2ItemNode(Lookup lookup, ResourceDesc resource, Decorator decorator) {
//        this(Children.LEAF, lookup, resource.getName(), decorator);
//        setDisplayName(resource.getName());
//        setShortDescription("<html>name: " + resource.getName() + "</html>");
//    }
    
    public Hk2ItemNode(Lookup lookup, Children children, String name, Decorator type) {
        this(children, lookup, name, type);
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
            actions.add(SystemAction.get(UndeployModuleAction.class));
        }
    
        if(decorator.canUnregister()) {
            actions.add(SystemAction.get(UnregisterResourceAction.class));
        }

        if(decorator.canShowBrowser()) {
            actions.add(SystemAction.get(OpenURLAction.class));
        }
        if (decorator.canEditDetails()) {
            actions.add(SystemAction.get(EditDetailsAction.class));
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
        return ImageUtilities.mergeImages(folder, badge, 7, 7);
    }
    
    /**
     * Retrieves the IDE's standard folder node, so we can access the default
     * open/closed folder icons.
     * 
     * @return standard folder node
     */
    private static Node getIconDelegate() {
        return DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
    }
    
    private static final String RESOURCES_ICON = 
            "org/netbeans/modules/glassfish/common/resources/resources.gif"; // NOI18N
        
    public static final Decorator J2EE_APPLICATION_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public boolean canDeployTo() { return true; }
    };
    
    public static final Decorator RESOURCES_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(RESOURCES_ICON); }
        @Override public Image getOpenedIcon(int type) { return getIcon(type); }
    };
    
    public static final Decorator J2EE_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canShowBrowser() { return true; }
    };
    
    public static final Decorator REFRESHABLE_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public boolean canDeployTo() { return true; }
    };

    public static final Decorator JDBC_MANAGED_DATASOURCES = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(RESOURCES_ICON); }
        @Override public String getCmdPropertyName() { return "jdbc_resource_name"; }
    };

    public static final Decorator CONNECTION_POOLS = new ResourceDecorator() {
        @Override public boolean canUnregister() { return true; }
        @Override public Image getIcon(int type) { return ImageUtilities.loadImage(RESOURCES_ICON); }
        @Override public String getCmdPropertyName() { return "jdbc_connection_pool_id"; }
        @Override public boolean isCascadeDelete() { return true; }
    };
    
}

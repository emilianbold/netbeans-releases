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
import org.glassfish.tools.ide.admin.*;
import org.netbeans.modules.glassfish.common.CommonServerSupport;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.Util;
import org.netbeans.modules.glassfish.common.nodes.actions.*;
import org.netbeans.modules.glassfish.spi.Decorator;
import org.netbeans.modules.glassfish.spi.ResourceDecorator;
import org.openide.actions.CopyAction;
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
 * <p/>
 * @author Ludovic Champenois, Peter Williams, Tomas Kraus
 */
public class Hk2ItemNode extends AbstractNode {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Common node cookie.
     */
    private static class Cookie {

        /** Task status. */
        volatile WeakReference<Future<ResultString>> status;

        /** GlassFish server instance. */
        final GlassfishInstance instance;

        /** Resource name. */
        final String name;

        /**
         * Creates an instance of cookie.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name   Name of resource to be enabled.
         */
        Cookie(final Lookup lookup, final String name) {
            this.instance = getGlassFishInstance(lookup);
            this.name = name;
        }

        /**
         * Returns <code>true</code> if this task is still running.
         * <p/>
         * @return Value of <code>true</code> if this task is still running
         *         or <code>false</code> otherwise.
         */
        public boolean isRunning() {
            WeakReference<Future<ResultString>> localref = status;
            if (localref == null) {
                return false;
            }
            Future<ResultString> future = localref.get();
            return future != null && !future.isDone();
        }
    }

    /**
     * Enable node cookie.
     */
    private static class Enable
            extends Cookie implements EnableModulesCookie {

        /**
         * Creates an instance of cookie for enabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name   Name of resource to be enabled.
         */
        Enable(final Lookup lookup, final String name) {
            super(lookup, name);
        }

        /**
         * Enable module on GlassFish server.
         * <p/>
         * @return Result of enable task execution.
         */
        @Override
        public Future<ResultString> enableModule() {
            if (instance != null) {
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandEnable(name, Util.computeTarget(
                        instance.getProperties())), null);
                status = new WeakReference<Future<ResultString>>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Disable node cookie.
     */
    private static class Disable
            extends Cookie implements DisableModulesCookie {

        /**
         * Creates an instance of cookie for disabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name   Name of resource to be enabled.
         */
        Disable(final Lookup lookup, final String name) {
            super(lookup, name);
        }

        /**
         * Disable module on GlassFish server.
         * <p/>
         * @return Result of disable task execution.
         */
        @Override
        public Future<ResultString> disableModule() {
            if (instance != null) {
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandDisable(name, Util.computeTarget(
                        instance.getProperties())), null);
                status = new WeakReference<Future<ResultString>>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Undeploy node cookie.
     */
    private static class Undeploy
            extends Cookie implements UndeployModuleCookie {

        /**
         * Creates an instance of cookie for disabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name   Name of resource to be enabled.
         */
        Undeploy(final Lookup lookup, final String name) {
            super(lookup, name);
        }

        /**
         * Undeploy module on GlassFish server.
         * <p/>
         * @return Result of undeploy task execution.
         */
        @Override
        public Future<ResultString> undeploy() {
            if (instance != null) {
                Future<ResultString> future = ServerAdmin.<ResultString>exec(
                        instance, new CommandUndeploy(name, Util.computeTarget(
                        instance.getProperties())), null);
                status = new WeakReference<Future<ResultString>>(future);
                return future;
            } else {
                return null;
            }
        }
    }

    /**
     * Deploy node cookie.
     */
    private static class Deploy
            extends Cookie implements DeployDirectoryCookie {

        /**
         * Creates an instance of cookie for disabling module.
         * <p/>
         * @param lookup Lookup containing {@see CommonServerSupport}.
         * @param name   Name of resource to be enabled.
         */
        Deploy(final Lookup lookup) {
            super(lookup, null);
        }

        /**
         * Deploy module from directory on GlassFish server.
         * <p/>
         * @return Result of undeploy task execution.
         */
        @Override
        public Future<ResultString> deployDirectory() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(NbBundle.getMessage(Hk2ItemNode.class,
                    "LBL_ChooseButton")); // NOI18N
            chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(false);

            int returnValue = chooser.showDialog(WindowManager.getDefault()
                    .getMainWindow(), NbBundle.getMessage(
                    Hk2ItemNode.class, "LBL_ChooseButton"));
            if (instance != null
                    || returnValue != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            final File dir
                    = new File(chooser.getSelectedFile().getAbsolutePath());

            Future<ResultString> future = ServerAdmin.<ResultString>exec(
                    instance, new CommandDeploy(dir.getParentFile().getName(),
                    Util.computeTarget(instance.getProperties()),
                    dir, null, null, null), null);
            status = new WeakReference<Future<ResultString>>(future);
            return future;
        }
    }

    /**
     * Refresh node cookie.
     */
    private static class Refresh implements RefreshModulesCookie {

        /** Child nodes to be refreshed. */
        private final Children children;

        /**
         * Creates an instance of cookie for refreshing nodes.
         * <p/>
         * @param children Child nodes to be refreshed.
         */
        Refresh(Children children) {
            this.children = children;
        }

        /**
         * Refresh child nodes.
         */
        @Override
        public void refresh() {
            refresh(null, null);
        }

        /**
         * Refresh child nodes.
         * <p/>
         * @param expected   Expected node display name.
         * @param unexpected Unexpected node display name.
         */
        @Override
        public void refresh(String expected, String unexpected) {
            if (children instanceof Refreshable) {
                ((Refreshable) children).updateKeys();
                boolean foundExpected = expected == null ? true : false;
                boolean foundUnexpected = false;
                for (Node node : children.getNodes()) {
                    if (!foundExpected
                            && node.getDisplayName().equals(expected)) {
                        foundExpected = true;
                    }
                    if (!foundUnexpected
                            && node.getDisplayName().equals(unexpected)) {
                        foundUnexpected = true;
                    }
                }
                if (!foundExpected) {
                    Logger.getLogger("glassfish").log(Level.WARNING, null,
                            new IllegalStateException(
                            "did not find a child node, named " + expected));
                }
                if (foundUnexpected) {
                    Logger.getLogger("glassfish").log(Level.WARNING, null,
                            new IllegalStateException(
                            "found unexpected child node, named "
                            + unexpected));
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Resources icon. */
    private static final String RESOURCES_ICON = 
            "org/netbeans/modules/glassfish/common/resources/resources.gif";

    /** Web services icon. */
    private static final String WS_ICON =
            "org/netbeans/modules/glassfish/common/resources/webservice.png";

    /** Java EE applications folder. */
    public static final Decorator J2EE_APPLICATION_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() {
            return true;
        }
        @Override public boolean canDeployTo() {
            return true;
        }
    };

    /** Resources folder. */
    public static final Decorator RESOURCES_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() {
            return true;
        }
        @Override public Image getIcon(int type) {
            return ImageUtilities.loadImage(RESOURCES_ICON);
        }
        @Override public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    };

    /** Web services folder. */
    public static final Decorator WS_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() {
            return true;
        }
    };

    /** Web service. */
    public static final Decorator WS_ENDPOINT = new Decorator() {
        @Override public boolean canTest() {
            return true;
        }
        @Override public boolean canCopy() {
            return true;
        }
        @Override public Image getIcon(int type) {
            return ImageUtilities.loadImage(WS_ICON);
        }
        @Override public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    };

    /** Java EE application. */
    public static final Decorator J2EE_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() {
            return true;
        }
        @Override public boolean canEnable() {
            return true;
        }
        @Override public boolean canDisable() {
            return true;
        }
        @Override public boolean canShowBrowser() {
            return true;
        }
    };

    /** Refreshable folder. */
    public static final Decorator REFRESHABLE_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() {
            return true;
        }
        @Override public boolean canDeployTo() {
            return true;
        }
    };

    /** JDBC managed data sources. */
    public static final Decorator JDBC_MANAGED_DATASOURCES
            = new ResourceDecorator() {
        @Override public boolean canUnregister() {
            return true;
        }
        @Override public Image getIcon(int type) {
            return ImageUtilities.loadImage(RESOURCES_ICON);
        }
        @Override public String getCmdPropertyName() {
            return "jdbc_resource_name";
        }
    };

    /** Connection pools. */
    public static final Decorator CONNECTION_POOLS = new ResourceDecorator() {
        @Override public boolean canUnregister() {
            return true;
        }
        @Override public Image getIcon(int type) {
            return ImageUtilities.loadImage(RESOURCES_ICON);
        }
        @Override public String getCmdPropertyName() {
            return "jdbc_connection_pool_id";
        }
        @Override public boolean isCascadeDelete() {
            return true;
        }
    };

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve GlassFish instance from {@see Lookup} object.
     * <p/>
     * @param lookup Lookup containing {@see CommonServerSupport}.
     * @return GlassFish instance retrieved from lookup object.
     */
    private static GlassfishInstance getGlassFishInstance(final Lookup lookup) {
        CommonServerSupport commonModule = lookup.lookup(
                CommonServerSupport.class);
        return commonModule != null ? commonModule.getInstance() : null;
    }

    /** Node decorator. */
    protected final Decorator decorator;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of extensible node without setting node display name.
     * <p/>
     * @param children  Child nodes.
     * @param lookup    Lookup containing {@see CommonServerSupport}.
     * @param name      Node name.
     * @param decorator Node decorator.
     */
    protected Hk2ItemNode(Children children, final Lookup lookup,
            final String name, final Decorator decorator) {
        super(children);
        this.decorator = decorator;
        
        if(decorator.isRefreshable()) {
            getCookieSet().add(new Refresh(children));
        }
        if(decorator.canDeployTo()) {
            getCookieSet().add(new Deploy(lookup)); 
        }
        if(decorator.canUndeploy()) {
            getCookieSet().add(new Undeploy(lookup, name));
        }
        if(decorator.canEnable()) {
            getCookieSet().add(new Enable(lookup, name));
        }
        if(decorator.canDisable()) {
            getCookieSet().add(new Disable(lookup, name));
        }
    }
        
    /**
     * Creates an instance of extensible node and sets node display name.
     * <p/>
     * @param children  Child nodes.
     * @param lookup    Lookup containing {@see CommonServerSupport}.
     * @param name      Node name.
     * @param decorator Node decorator.
     */
    public Hk2ItemNode(final Lookup lookup,
            Children children, final String name, final Decorator decorator) {
        this(children, lookup, name, decorator);
        setDisplayName(name);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Find an icon for this node (in the closed state).
     * <p/>
     * @param type Constant from {@link java.beans.BeanInfo}.
     * @return Icon to use to represent this node.
     */
    @Override
    public Image getIcon(int type) {
        Image image;
        Image badge = decorator.getIconBadge();
        if(badge != null) {
            if (null == decorator.getIcon(type)) {
                image = badgeFolder(badge, false);
            } else {
                image = badgeIcon(badge,decorator.getIcon(type));
            }
        } else {
            image = decorator.getIcon(type);
        }
        return image != null ? image : getIconDelegate().getIcon(type);
    }
    
    /**
     * Find an icon for this node (in the open state).
     * <p/>
     * This icon is used when the node may have children and is expanded.
     * <p/>
     * @param type Constant from {@link java.beans.BeanInfo}.
     * @return Icon to use to represent this node when open.
     */
    @Override
    public Image getOpenedIcon(int type) {
        Image image;
        Image badge = decorator.getIconBadge();
        if(badge != null) {
            image = badgeFolder(badge, true);
        } else {
            image = decorator.getOpenedIcon(type);
        }
        return image != null ? image : getIconDelegate().getOpenedIcon(type);
    }
    
    /** Get the set of actions that are associated with this node.
     * <p/>
     * This set is used to construct the context menu for the node.
     * By default this method delegates to the deprecated getActions
     * or getContextActions method depending on the value of supplied argument.
     * It is supposed to be overridden by subclasses accordingly.
     * <p/>
     * @param context Whether to find actions for context meaning or for
     *                the node itself.
     * @return {@see List} of actions (you may include nulls for separators).
     */
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
        if(decorator.canEnable()) {
            actions.add(SystemAction.get(EnableModulesAction.class));
        }
        if(decorator.canDisable()) {
            actions.add(SystemAction.get(DisableModulesAction.class));
        }
        if(decorator.canUnregister()) {
            actions.add(SystemAction.get(UnregisterResourceAction.class));
        }
        if(decorator.canShowBrowser()) {
            actions.add(SystemAction.get(OpenURLAction.class));
        }
        if(decorator.canTest()) {
            actions.add(SystemAction.get(OpenTestURLAction.class));
        }
        if(decorator.canCopy()) {
            actions.add(SystemAction.get(CopyAction.class));
        }
        if (decorator.canEditDetails()) {
            actions.add(SystemAction.get(EditDetailsAction.class));
        }
        return actions.toArray(new Action[actions.size()]);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates and returns the instance of the node representing the status
     * 'WAIT' of the node.
     * <p/>
     * It is used when it spent more time to create elements hierarchy.
     * <p/>
     * @return The wait node.
     */
    public static Node createWaitNode() {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setName(NbBundle.getMessage(Hk2ItemNode.class,
                "LBL_WaitNode_DisplayName"));
        node.setIconBaseWithExtension("org/openide/src/resources/wait.gif");
        return node;
    }

    /**
     * Applies a badge to an open or closed folder icon.
     * <p/>
     * @param badge  Badge image for folder.
     * @param opened Use open or closed folder.
     * @return An image of the badged folder.
     */
    public static Image badgeFolder(Image badge, boolean opened) {
        Node folderNode = getIconDelegate();
        Image folder = opened
                ? folderNode.getOpenedIcon(BeanInfo.ICON_COLOR_16x16)
                : folderNode.getIcon(BeanInfo.ICON_COLOR_16x16);
        return ImageUtilities.mergeImages(folder, badge, 7, 7);
    }
    
    /**
     * Applies a badge to an icon.
     * <p/>
     * @param badge Badge image for folder.
     * @param icon  Tthe image to be badged.
     * @return An image of the badged folder.
     */
    public static Image badgeIcon(Image badge, Image icon) {
        return ImageUtilities.mergeImages(icon, badge, 7, 7);
    }

    /**
     * Retrieves the IDE's standard folder node, so we can access the default
     * open/closed folder icons.
     * <p/>
     * @return Standard folder node.
     */
    private static Node getIconDelegate() {
        return DataFolder.findFolder(
                FileUtil.getConfigRoot()).getNodeDelegate();
    }

}

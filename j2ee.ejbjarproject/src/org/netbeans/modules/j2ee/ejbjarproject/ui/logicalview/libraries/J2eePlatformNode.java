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

package org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.libraries;


import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * J2eePlatformNode represents the J2EE platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * @see J2eePlatform
 * @author Andrei Badea
 */
class J2eePlatformNode extends AbstractNode implements PropertyChangeListener, InstanceListener {

    private static final String ARCHIVE_ICON = "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/jar.gif"; //NOI18N
    private static final String DEFAULT_ICON = "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/j2eeServer.gif"; //NOI18N
    private static final String BROKEN_PROJECT_BADGE = "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/brokenProjectBadge.gif"; //NOI18N
    
    private static final Icon icon = new ImageIcon(Utilities.loadImage(ARCHIVE_ICON));
    
    private static final Image brokenIcon = Utilities.mergeImages(
            Utilities.loadImage(DEFAULT_ICON),
            Utilities.loadImage(BROKEN_PROJECT_BADGE), 
            8, 0);

    private final PropertyEvaluator evaluator;
    private final String platformPropName;
    private J2eePlatform platformCache;
    
    private final PropertyChangeListener platformListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (J2eePlatform.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                fireNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
                fireDisplayNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
            }
            if (J2eePlatform.PROP_CLASSPATH.equals(evt.getPropertyName())) {
                postAddNotify();
            }
        }
    };

    private J2eePlatformNode(Project project, PropertyEvaluator evaluator, String platformPropName) {
        super(new PlatformContentChildren());
        this.evaluator = evaluator;
        this.platformPropName = platformPropName;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        
        J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        moduleProvider.addInstanceListener(
                (InstanceListener)WeakListeners.create(InstanceListener.class, this, moduleProvider));
    }
    
    public static J2eePlatformNode create(Project project, PropertyEvaluator evaluator, String platformPropName) {
        return new J2eePlatformNode(project, evaluator, platformPropName);
    }

    public String getName () {
        return this.getDisplayName();
    }
    
    public String getDisplayName() {
        return "";
    }
    
    public String getHtmlDisplayName() {
        if (getPlatform() != null)
            return getPlatform().getDisplayName();
        else 
            return NbBundle.getMessage(J2eePlatformNode.class, "LBL_J2eeServerMissing");
    }
    
    public Image getIcon(int type) {
        Image result = null;
        if (getPlatform() != null) {
            result = getPlatform().getIcon();
        }
        return result != null ? result : brokenIcon;
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    public boolean canCopy() {
        return false;
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[0];
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //The caller holds ProjectManager.mutex() read lock
        
        if (platformPropName.equals(evt.getPropertyName())) {
            refresh();
        }
    }
    
    private void refresh() {
        if (platformCache != null)
            platformCache.removePropertyChangeListener(platformListener);

        platformCache = null;

        this.fireNameChange(null, null);
        this.fireDisplayNameChange(null, null);
        this.fireIconChange();
        
        // The caller may hold ProjectManager.mutex() read lock (i.e., the propertyChange() method)
        postAddNotify();
    }
    
    public void instanceAdded(String serverInstanceID) {
        refresh();
    }
    
    public void instanceRemoved(String serverInstanceID) {
        refresh();
    }
    
    public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
    }

    private void postAddNotify() {
        LibrariesNode.rp.post (new Runnable () {
            public void run () {
                ((PlatformContentChildren)getChildren()).addNotify ();
            }
        });
    }

    private J2eePlatform getPlatform () {
        if (platformCache == null) {
            String j2eePlatformInstanceId = this.evaluator.getProperty(this.platformPropName);
            if (j2eePlatformInstanceId != null) {
                platformCache = Deployment.getDefault().getJ2eePlatform(j2eePlatformInstanceId);
            }
            if (platformCache != null) {
                platformCache.addPropertyChangeListener(platformListener);
                // the platform has likely changed, so force the node to display the new platform's icon
                this.fireIconChange();
            }
        }
        return platformCache;
    }

    private static class PlatformContentChildren extends Children.Keys {

        PlatformContentChildren () {
        }

        protected void addNotify() {
            this.setKeys (this.getKeys());
        }

        protected void removeNotify() {
            this.setKeys(Collections.EMPTY_SET);
        }

        protected Node[] createNodes(Object key) {
            SourceGroup sg = (SourceGroup) key;
            return new Node[] {ActionFilterNode.create(PackageView.createPackageView(sg), null, null, null, null, null, null)};
        }

        private List getKeys () {
            List result;
            
            J2eePlatform j2eePlatform = ((J2eePlatformNode)this.getNode()).getPlatform();
            if (j2eePlatform != null) {
                File[] classpathEntries = j2eePlatform.getClasspathEntries();
                result = new ArrayList(classpathEntries.length);
                for (int i = 0; i < classpathEntries.length; i++) {
                    FileObject file = FileUtil.toFileObject(classpathEntries[i]);
                    if (file != null) {
                        FileObject archiveFile = FileUtil.getArchiveRoot(file);
                        if (archiveFile != null) {
                            result.add(new LibrariesSourceGroup(archiveFile, file.getNameExt(), icon, icon));
                        }
                    }
                }
            } else {
                result = Collections.EMPTY_LIST;
            }
            
            return result;
        }
    }
}

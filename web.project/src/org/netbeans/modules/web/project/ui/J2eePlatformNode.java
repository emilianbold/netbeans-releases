/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui;


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
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * J2eePlatformNode represents the J2EE platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * @see J2eePlatform
 * @author Andrei Badea
 */
class J2eePlatformNode extends AbstractNode implements PropertyChangeListener {

    private static final String ARCHIVE_ICON = "org/netbeans/modules/web/project/ui/resources/jar.gif"; //NOI18N
    
    private static final Icon icon = new ImageIcon(Utilities.loadImage(ARCHIVE_ICON));

    private final PropertyEvaluator evaluator;
    private final String platformPropName;
    private J2eePlatform platformCache;
    
    private final PropertyChangeListener displayNameListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            fireNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
            fireDisplayNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
        }
    };

    private J2eePlatformNode(PropertyEvaluator evaluator, String platformPropName) {
        super(new PlatformContentChildren());
        this.evaluator = evaluator;
        this.platformPropName = platformPropName;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }
    
    public static J2eePlatformNode create(PropertyEvaluator evaluator, String platformPropName) {
        return new J2eePlatformNode(evaluator, platformPropName);
    }

    public String getName () {
        return this.getDisplayName();
    }

    public String getDisplayName () {
        return getPlatform().getDisplayName();
    }
    
    public Image getIcon(int type) {
        Image result = getPlatform().getIcon();
        if (result == null)
            result = super.getIcon(type);
        
        return result;
    }
    
    public Image getOpenedIcon(int type) {
        Image result = getPlatform().getIcon();
        if (result == null)
            result = super.getOpenedIcon(type);
        
        return result;
    }

    public boolean canCopy() {
        return false;
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[0];
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (platformPropName.equals(evt.getPropertyName())) {
            platformCache.removePropertyChangeListener(displayNameListener);
            platformCache = null;
            this.fireNameChange(null, null);
            this.fireDisplayNameChange(null, null);
            //The caller holds ProjectManager.mutex() read lock
            LibrariesNode.rp.post (new Runnable () {
                public void run () {
                    ((PlatformContentChildren)getChildren()).addNotify ();
                }
            });
        }
        if (J2eePlatform.PROP_CLASSPATH.equals(evt.getPropertyName())) {
            LibrariesNode.rp.post (new Runnable () {
                public void run () {
                    ((PlatformContentChildren)getChildren()).addNotify ();
                }
            });
        }
    }

    private J2eePlatform getPlatform () {
        if (platformCache == null) {
            String j2eePlatformInstanceId = this.evaluator.getProperty(this.platformPropName);
            if (j2eePlatformInstanceId != null) {
                platformCache = Deployment.getDefault().getJ2eePlatform(j2eePlatformInstanceId);
            }
            if (platformCache == null) {
                platformCache = Deployment.getDefault().getJ2eePlatform(Deployment.getDefault().getDefaultServerInstanceID());
                
            }
            if (platformCache != null) {
                platformCache.addPropertyChangeListener(displayNameListener);
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
            return new Node[] {ActionFilterNode.create(PackageView.createPackageView(sg), null, null, null, null, null)};
        }

        private List getKeys () {
            J2eePlatform j2eePlatform = ((J2eePlatformNode)this.getNode()).getPlatform();
            File[] classpathEntries = j2eePlatform.getClasspathEntries();
            List result = new ArrayList(classpathEntries.length);
            for (int i = 0; i < classpathEntries.length; i++) {
                FileObject file = FileUtil.toFileObject(classpathEntries[i]);
                if (file != null) {
                    FileObject archiveFile = FileUtil.getArchiveRoot(file);
                    if (archiveFile != null) {
                        result.add(new LibrariesSourceGroup(archiveFile, file.getNameExt(), icon, icon));
                    }
                }
            }
            
            return result;
        }
    }
}

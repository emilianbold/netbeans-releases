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

package org.netbeans.modules.java.j2seproject.ui;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.ErrorManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;



/**
 * PlatformNode represents Java platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * It displays the content of boot classpath.
 * @see JavaPlatform
 * @author Tomas Zezula
 */
class PlatformNode extends AbstractNode implements PropertyChangeListener {

    private static final String PLATFORM_ICON = "org/netbeans/modules/java/j2seproject/ui/resources/platform";    //NOI18N

    private final PropertyEvaluator evaluator;
    private final String platformPropName;
    private JavaPlatform platformCache;

    PlatformNode (PropertyEvaluator evaluator, String platformPropName) {
        super (new PlatformContentChildren ());
        this.evaluator = evaluator;
        this.platformPropName = platformPropName;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this,evaluator));
        setIconBase(PLATFORM_ICON);
    }

    public String getName () {
        return this.getDisplayName();
    }

    public String getDisplayName () {
        return getPlatform().getDisplayName();
    }

    public boolean canCopy() {
        return false;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (platformPropName.equals (evt.getPropertyName())) {
            platformCache = null;
            this.fireNameChange(null,null);
            this.fireDisplayNameChange(null,null);
            //The caller holds ProjectManager.mutex() read lock
            LibrariesNode.rp.post (new Runnable () {
                public void run () {
                    ((PlatformContentChildren)getChildren()).addNotify ();
                }
            });
        }
    }

    private JavaPlatform getPlatform () {
        if (platformCache == null) {
            String platformSystemName = this.evaluator.getProperty(this.platformPropName);
            if (platformSystemName != null) {
                JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
                for (int i=0; i<platforms.length; i++) {
                    if (platformSystemName.equals(platforms[i].getProperties().get("platform.ant.name"))) { //NOI18N
                        platformCache = platforms[i];
                        break;
                    }
                }
            }
            if (platformCache == null) {
                platformCache = JavaPlatformManager.getDefault().getDefaultPlatform();
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
            return new Node[] {ActionFilterNode.create(PackageView.createPackageView(sg), null,null,null)};
        }

        private List getKeys () {
            JavaPlatform platform = ((PlatformNode)this.getNode()).getPlatform();
            //Todo: Should listen on returned classpath, but now the bootstrap libraries are read only
            FileObject[] roots = platform.getBootstrapLibraries().getRoots();
            List result = new ArrayList (roots.length);
            for (int i=0; i<roots.length; i++) {
                try {
                    FileObject file = "jar".equals(roots[i].getURL().getProtocol()) ?   //NOI18N
                            FileUtil.getArchiveFile (roots[i]) : roots[i];
                    if (file.isValid()) {
                        result.add (new LibrariesSourceGroup(roots[i],file.getNameExt()));
                    }
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            return result;
        }
    }

}



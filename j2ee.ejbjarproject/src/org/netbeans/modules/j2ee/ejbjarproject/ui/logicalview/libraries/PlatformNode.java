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

package org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.libraries;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.ErrorManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;



/**
 * PlatformNode represents Java platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * It displays the content of boot classpath.
 * @see JavaPlatform
 * @author Tomas Zezula
 */
class PlatformNode extends AbstractNode implements ChangeListener {

    private static final String PLATFORM_ICON = "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/platform";    //NOI18N
    private static final String ARCHIVE_ICON = "org/netbeans/modules/j2ee/ejbjarproject/ui/resources/jar.gif"; //NOI18N

    private final PlatformProvider pp;

    private PlatformNode (PlatformProvider pp) {
        super (new PlatformContentChildren (), Lookups.singleton (new JavadocProvider(pp)));        
        this.pp = pp;
        this.pp.addChangeListener(this);
        setIconBase(PLATFORM_ICON);
    }

    public String getName () {
        return this.getDisplayName();
    }

    public String getDisplayName () {
        return pp.getPlatform().getDisplayName();
    }

    public boolean canCopy() {
        return false;
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get (ShowJavadocAction.class)
        };
    }

    public void stateChanged(ChangeEvent e) {
        this.fireNameChange(null,null);
        this.fireDisplayNameChange(null,null);
        //The caller holds ProjectManager.mutex() read lock
        LibrariesNode.rp.post (new Runnable () {
            public void run () {
                ((PlatformContentChildren)getChildren()).addNotify ();
            }
        });
    }
    
    
    /**
     * Creates new PlatformNode
     * @param eval the PropertyEvaluator used for obtaining the active platform name
     * and listening on the active platform change
     * @param platformPropName the name of ant property holding the platform name
     *
     */
    static PlatformNode create (PropertyEvaluator eval, String platformPropName) {
        PlatformProvider pp = new PlatformProvider (eval, platformPropName);
        return new PlatformNode (pp);
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
            JavaPlatform platform = ((PlatformNode)this.getNode()).pp.getPlatform();
            //Todo: Should listen on returned classpath, but now the bootstrap libraries are read only
            FileObject[] roots = platform.getBootstrapLibraries().getRoots();
            List result = new ArrayList (roots.length);
            for (int i=0; i<roots.length; i++) {
                try {
                    FileObject file;
                    Icon icon;
                    Icon openedIcon;
                    if ("jar".equals(roots[i].getURL().getProtocol())) { //NOI18N
                        file = FileUtil.getArchiveFile (roots[i]);
                        icon = openedIcon = new ImageIcon (Utilities.loadImage(ARCHIVE_ICON));
                    }
                    else {
                        file = roots[i];
                        icon = null;
                        openedIcon = null;
                    }
                    
                    if (file.isValid()) {
                        result.add (new LibrariesSourceGroup(roots[i],file.getNameExt(),icon, openedIcon));
                    }
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            return result;
        }
    }
    
    private static class PlatformProvider implements PropertyChangeListener {
        
        private final PropertyEvaluator evaluator;
        private final String platformPropName;
        private JavaPlatform platformCache;
        private List/*<ChangeListener>*/ listeners;
        
        public PlatformProvider (PropertyEvaluator evaluator, String platformPropName) {
            this.evaluator = evaluator;
            this.platformPropName = platformPropName;
            this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this,evaluator));
        }
        
        public JavaPlatform getPlatform () {
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
        
        public synchronized void addChangeListener (ChangeListener l) {
            if (this.listeners == null) {
                this.listeners = new ArrayList ();
            }
            this.listeners.add (l);
        }
        
        public synchronized void removeChangeListener (ChangeListener l) {
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(l);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (platformPropName.equals (evt.getPropertyName())) {
                platformCache = null;                
                this.fireChange ();
            }
        }
        
        private void fireChange () {
            ChangeListener[] _listeners;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                _listeners = (ChangeListener[]) this.listeners.toArray(new ChangeListener[listeners.size()]);
            }
            ChangeEvent event = new ChangeEvent (this);
            for (int i=0; i< _listeners.length; i++) {
                _listeners[i].stateChanged(event);
            }
        }
        
    }
    
    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {
        
        PlatformProvider platformProvider;
        
        private JavadocProvider (PlatformProvider platformProvider) {
            this.platformProvider = platformProvider;
        }
        
        public boolean hasJavadoc() {
            JavaPlatform platform = platformProvider.getPlatform();            
            URL[] javadocRoots = getJavadocRoots(platform);
            return javadocRoots.length > 0;
        }

        public void showJavadoc() {
            JavaPlatform platform = platformProvider.getPlatform();            
            URL[] javadocRoots = getJavadocRoots(platform);
            URL pageURL = ShowJavadocAction.findJavadoc("/overview-summary.html",javadocRoots);
            if (pageURL == null) {
                pageURL = ShowJavadocAction.findJavadoc("/index.html",javadocRoots);
            }
            ShowJavadocAction.showJavaDoc(pageURL, platform.getDisplayName());
        }
        
        
        private static URL[]  getJavadocRoots (JavaPlatform platform) {
            Set result = new HashSet ();
            List/*<ClassPath.Entry>*/ l = platform.getBootstrapLibraries().entries();            
            for (Iterator it = l.iterator(); it.hasNext();) {
                ClassPath.Entry e = (ClassPath.Entry) it.next ();                
                result.addAll(Arrays.asList(JavadocForBinaryQuery.findJavadoc (e.getURL()).getRoots()));
            }
            return (URL[]) result.toArray (new URL[result.size()]);
        }
        
        
    }

}





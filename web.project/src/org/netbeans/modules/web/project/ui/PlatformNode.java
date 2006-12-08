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

package org.netbeans.modules.web.project.ui;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.net.URL;
import java.text.MessageFormat;
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
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
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
import org.openide.xml.XMLUtil;



/**
 * PlatformNode represents Java platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * It displays the content of boot classpath.
 * @see JavaPlatform
 * @author Tomas Zezula
 */
class PlatformNode extends AbstractNode implements ChangeListener {

    private static final String PLATFORM_ICON = "org/netbeans/modules/web/project/ui/resources/platform";    //NOI18N
    private static final String ARCHIVE_ICON = "org/netbeans/modules/web/project/ui/resources/jar.gif"; //NOI18N

    private final PlatformProvider pp;

    private PlatformNode(PlatformProvider pp) {
        super (new PlatformContentChildren (), Lookups.singleton (new JavadocProvider(pp)));        
        this.pp = pp;
        this.pp.addChangeListener(this);
        setIconBase(PLATFORM_ICON);
    }

    public String getName () {
        return this.getDisplayName();
    }

    public String getDisplayName () {
        JavaPlatform plat = pp.getPlatform();
        String name;
        if (plat != null) {
            name = plat.getDisplayName();
        }
        else {
            String platformId = pp.getPlatformId ();
            if (platformId == null) {
                name = NbBundle.getMessage(PlatformNode.class,"TXT_BrokenPlatform");
            }
            else {
                name = MessageFormat.format(NbBundle.getMessage(PlatformNode.class,"FMT_BrokenPlatform"), new Object[] {platformId});
            }
        }
        return name;
    }
    
    public String getHtmlDisplayName () {
        if (pp.getPlatform() == null) {
            String displayName = this.getDisplayName();
            try {
                displayName = XMLUtil.toElementContent(displayName);
            } catch (CharConversionException ex) {
                // OK, no annotation in this case
                return null;
            }
            return "<font color=\"#A40000\">" + displayName + "</font>"; //NOI18N
        }
        else {
            return null;
        }                                
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
            return new Node[] {ActionFilterNode.create(PackageView.createPackageView(sg), null,null,null,null,null,null)};
        }

        private List getKeys () {            
            JavaPlatform platform = ((PlatformNode)this.getNode()).pp.getPlatform();
            if (platform == null) {
                return Collections.EMPTY_LIST;
            }
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
        
        public String getPlatformId () {
            return this.evaluator.getProperty(this.platformPropName);
        }
        
        public JavaPlatform getPlatform () {
            if (platformCache == null) {
                String platformSystemName = getPlatformId();
                if (platformSystemName == null) {
                    platformCache = JavaPlatformManager.getDefault().getDefaultPlatform();
                }
                else {
                    JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
                    for (int i=0; i<platforms.length; i++) {
                        if (platformSystemName.equals(platforms[i].getProperties().get("platform.ant.name"))) { //NOI18N
                            if (platforms[i].getInstallFolders().size()>0) {
                                platformCache = platforms[i];
                            }
                            break;
                        }
                    }
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
            if (platform == null) {
                return false;
            }
            URL[] javadocRoots = getJavadocRoots(platform);
            return javadocRoots.length > 0;
        }

        public void showJavadoc() {
            JavaPlatform platform = platformProvider.getPlatform();            
            if (platform != null) {                            
                URL[] javadocRoots = getJavadocRoots(platform);
                URL pageURL = ShowJavadocAction.findJavadoc("/overview-summary.html",javadocRoots);
                if (pageURL == null) {
                    pageURL = ShowJavadocAction.findJavadoc("/index.html",javadocRoots);
                }
                ShowJavadocAction.showJavaDoc(pageURL, platform.getDisplayName());
            }
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



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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.api.common.project.ui;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
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

    private static final String PLATFORM_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/platform.gif";    //NOI18N
    private static final String ARCHIVE_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/jar.gif"; //NOI18N

    private final PlatformProvider pp;

    private PlatformNode(PlatformProvider pp, ClassPathSupport cs) {
        super (new PlatformContentChildren (cs), Lookups.singleton (new JavadocProvider(pp)));        
        this.pp = pp;
        this.pp.addChangeListener(this);
        setIconBaseWithExtension(PLATFORM_ICON);
    }

    @Override
    public String getName () {
        return this.getDisplayName();
    }

    @Override
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
    
    @Override
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

    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
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
    public static PlatformNode create (PropertyEvaluator eval, String platformPropName, ClassPathSupport cs) {
        PlatformProvider pp = new PlatformProvider (eval, platformPropName);
        return new PlatformNode (pp, cs);
    }

    private static class PlatformContentChildren extends Children.Keys<SourceGroup> {

        PlatformContentChildren (ClassPathSupport cs) {
        }

        @Override
        protected void addNotify() {
            this.setKeys (this.getKeys());
        }

        @Override
        protected void removeNotify() {
            this.setKeys(Collections.<SourceGroup>emptySet());
        }

        protected Node[] createNodes(SourceGroup sg) {
            return new Node[] {ActionFilterNode.create(PackageView.createPackageView(sg), null,null,null,null,null,null)};
        }

        private List<SourceGroup> getKeys () {            
            JavaPlatform platform = ((PlatformNode)this.getNode()).pp.getPlatform();
            if (platform == null) {
                return Collections.<SourceGroup>emptyList();
            }
            //Todo: Should listen on returned classpath, but now the bootstrap libraries are read only
            FileObject[] roots = platform.getBootstrapLibraries().getRoots();
            List<SourceGroup> result = new ArrayList<SourceGroup>(roots.length);
            for (int i = 0; i < roots.length; i++) {
                try {
                    FileObject file;
                    Icon icon;
                    Icon openedIcon;
                    if ("jar".equals(roots[i].getURL().getProtocol())) { //NOI18N
                        file = FileUtil.getArchiveFile (roots[i]);
                        icon = openedIcon = new ImageIcon (ImageUtilities.loadImage(ARCHIVE_ICON));
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
                    Exceptions.printStackTrace(e);
                }
            }
            return result;
        }
    }
    
    private static class PlatformProvider implements PropertyChangeListener {
        
        private final PropertyEvaluator evaluator;
        private final String platformPropName;
        private JavaPlatform platformCache;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        
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
                platformCache = CommonProjectUtils.getActivePlatform (getPlatformId());
            }
            return platformCache;
        }
        
        public void addChangeListener (ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        
        public void removeChangeListener (ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (platformPropName.equals (evt.getPropertyName())) {
                platformCache = null;                
                this.changeSupport.fireChange ();
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
                URL pageURL = ShowJavadocAction.findJavadoc("overview-summary.html",javadocRoots);
                if (pageURL == null) {
                    pageURL = ShowJavadocAction.findJavadoc("index.html",javadocRoots);
                }
                ShowJavadocAction.showJavaDoc(pageURL, platform.getDisplayName());
            }
        }
        
        
        private static URL[]  getJavadocRoots (JavaPlatform platform) {
            Set<URL> result = new HashSet<URL>();
            List<ClassPath.Entry> l = platform.getBootstrapLibraries().entries();            
            for (ClassPath.Entry e : l) {
                result.addAll(Arrays.asList(JavadocForBinaryQuery.findJavadoc (e.getURL()).getRoots()));
            }
            return result.toArray (new URL[result.size()]);
        }
        
        
    }

}



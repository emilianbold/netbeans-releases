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
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
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
        super (new PlatformContentChildren (cs), new ProxyLookup(new Lookup[]{
            Lookups.fixed(new PlatformEditable(pp), new JavadocProvider(pp),  new PathFinder()),
                    new PlatformFolderLookup(new InstanceContent(), pp)
            }));
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
        final Object[] platHolder = pp.getPlatform();
        if (platHolder == null) {
            return NbBundle.getMessage(PlatformNode.class, "TXT_UnknownPlatform");
        }
        String name;
        if (platHolder[1] != null) {
            name = ((JavaPlatform)platHolder[1]).getDisplayName();
        } else {
            String platformId = (String) platHolder[0];
            if (platformId == null) {
                name = NbBundle.getMessage(PlatformNode.class,"TXT_BrokenPlatform");
            } else {
                name = MessageFormat.format(NbBundle.getMessage(PlatformNode.class,"FMT_BrokenPlatform"), new Object[] {platformId});
            }
        }
        return name;
    }
    
    @Override
    public String getHtmlDisplayName () {
        final Object[] platHolder = pp.getPlatform();
        if (platHolder == null) {
            return null;
        }
        if (platHolder[1] == null) {
            String displayName = this.getDisplayName();
            try {
                displayName = XMLUtil.toElementContent(displayName);
            } catch (CharConversionException ex) {
                // OK, no annotation in this case
                return null;
            }
            return "<font color=\"#A40000\">" + displayName + "</font>"; //NOI18N
        } else {
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
            SystemAction.get (ShowJavadocAction.class),
            SystemAction.get (EditRootAction.class),
        };
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        this.fireNameChange(null,null);
        this.fireDisplayNameChange(null,null);
        //The caller holds ProjectManager.mutex() read lock
        LibrariesNode.rp.post (new Runnable () {
            @Override
            public void run () {
                ((PlatformContentChildren)getChildren()).addNotify ();
            }
        });
    }

    @Override
    public String getShortDescription() {
        final Object[] platHolder = pp.getPlatform();
        if (platHolder != null && platHolder[1] != null && !((JavaPlatform)platHolder[1]).getInstallFolders().isEmpty()) {
            final FileObject installFolder = ((JavaPlatform)platHolder[1]).getInstallFolders().iterator().next();
            return FileUtil.getFileDisplayName(installFolder);
        } else {
            return super.getShortDescription();
        }
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

        @Override
        protected Node[] createNodes(SourceGroup sg) {
            return new Node[] {ActionFilterNode.forPackage(PackageView.createPackageView(sg))};
        }

        private List<SourceGroup> getKeys () {
            Object[] platHolder = ((PlatformNode)this.getNode()).pp.getPlatform();;
            if (platHolder == null || platHolder[1] == null) {
                return Collections.<SourceGroup>emptyList();
            }
            //Todo: Should listen on returned classpath, but now the bootstrap libraries are read only
            FileObject[] roots = ((JavaPlatform)platHolder[1]).getBootstrapLibraries().getRoots();
            List<SourceGroup> result = new ArrayList<SourceGroup>(roots.length);
            for (int i = 0; i < roots.length; i++) {
                    FileObject file;
                    Icon icon;
                    Icon openedIcon;
                    if ("jar".equals(roots[i].toURL().getProtocol())) { //NOI18N
                        file = FileUtil.getArchiveFile (roots[i]);
                        icon = openedIcon = ImageUtilities.loadImageIcon(ARCHIVE_ICON, false);
                    }
                    else {
                        file = roots[i];
                        icon = null;
                        openedIcon = null;
                    }
                    
                    if (file.isValid()) {
                        result.add (new LibrariesSourceGroup(roots[i],file.getNameExt(),icon, openedIcon));
                    }
            }
            return result;
        }
    }

    private static class PlatformEditable implements EditRootAction.Editable {

        private final PlatformProvider pp;

        private PlatformEditable(final @NonNull PlatformProvider pp) {
            Parameters.notNull("pp", pp);   //NOI18N
            this.pp = pp;
        }

        @Override
        public boolean canEdit() {
            final Object[] platHolder = pp.getPlatform();
            return platHolder != null && platHolder[1] != null;
        }

        @Override
        public void edit() {
            final Object[] platHolder = pp.getPlatform();
            if (platHolder != null && platHolder[1] != null) {
                PlatformsCustomizer.showCustomizer((JavaPlatform)platHolder[1]);
            }
        }
    }

    private static class PlatformProvider implements PropertyChangeListener {

        private static final Object[] BUSY = new Object[0];
        private static final RequestProcessor RP = new RequestProcessor(PlatformProvider.class);
        
        private final PropertyEvaluator evaluator;
        private final String platformPropName;
        private final AtomicReference<Object[]> platformCache = new AtomicReference<Object[]>();
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        
        public PlatformProvider (PropertyEvaluator evaluator, String platformPropName) {
            this.evaluator = evaluator;
            this.platformPropName = platformPropName;
            this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this,evaluator));
        }
                
        @CheckForNull   //Todo: Replace by Pair
        public Object[] getPlatform () {
            if (platformCache.compareAndSet(null, BUSY)) {
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        final String platformId = evaluator.getProperty(platformPropName);
                        final JavaPlatform platform = CommonProjectUtils.getActivePlatform (platformId);
                        platformCache.set(new Object[]{platformId, platform});
                        changeSupport.fireChange ();
                    }
                });
            }
            Object[] res = platformCache.get();
            return res == BUSY ? null : res;
        }
        
        public void addChangeListener (ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        
        public void removeChangeListener (ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (platformPropName.equals (evt.getPropertyName())) {
                platformCache.set(null);
                getPlatform();
            }
        }
        
    }
    
    private static class JavadocProvider implements ShowJavadocAction.JavadocProvider {
        
        private final PlatformProvider platformProvider;
        
        private JavadocProvider (PlatformProvider platformProvider) {
            this.platformProvider = platformProvider;
        }
        
        @Override
        public boolean hasJavadoc() {
            Object[] platHolder = platformProvider.getPlatform();
            if (platHolder == null || platHolder[1] == null) {
                return false;
            }
            URL[] javadocRoots = getJavadocRoots((JavaPlatform)platHolder[1]);
            return javadocRoots.length > 0;
        }

        @Override
        public void showJavadoc() {
            final Object[] platHolder = platformProvider.getPlatform();
            if (platHolder != null && platHolder[1] != null) {
                final JavaPlatform platform = (JavaPlatform) platHolder[1];
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

    private static class PlatformFolderLookup extends AbstractLookup {

        private final InstanceContent content;
        private final PlatformProvider platformProvider;

        PlatformFolderLookup(final InstanceContent content, final PlatformProvider platformProvider) {
            super(content);
            this.content = content;
            this.platformProvider = platformProvider;
        }

        @Override
        protected void beforeLookup(Template<?> template) {
            super.beforeLookup(template);
            if (template.getType() == FileObject.class) {
                final Collection<DataObject> toAdd = new ArrayList<DataObject>(1);
                final Object[] platHolder = platformProvider.getPlatform();
                if (platHolder != null && platHolder[1] != null) {
                    final Collection<? extends FileObject> folders = ((JavaPlatform)platHolder[1]).getInstallFolders();
                    if (!folders.isEmpty()) {
                        final FileObject fo = folders.iterator().next();
                        if (fo.isValid() && fo.isFolder()) {
                            try {
                                toAdd.add(DataFolder.find(fo));
                            } catch (DataObjectNotFoundException ex) {
                                //pass - clears content
                            }
                        }
                    }
                }
                content.set(toAdd, null);
            }
        }

    }

    private static final class PathFinder implements org.netbeans.spi.project.ui.PathFinder {

        PathFinder() {
        }

        @Override
        public Node findPath(Node root, Object target) {
            Node result = null;
            for (Node node : root.getChildren().getNodes(true)) {
                final org.netbeans.spi.project.ui.PathFinder pf =
                    node.getLookup().lookup(org.netbeans.spi.project.ui.PathFinder.class);
                if (pf == null) {
                    continue;
                }
                result = pf.findPath(node, target);
                if (result != null) {
                    break;
                }
            }
            return result;
        }

    }

}



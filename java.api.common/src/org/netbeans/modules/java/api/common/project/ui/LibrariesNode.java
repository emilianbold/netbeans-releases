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

package org.netbeans.modules.java.api.common.project.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ui.customizer.AntArtifactItem;
import org.netbeans.modules.java.api.common.project.ui.customizer.EditMediator;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * LibrariesNode displays the content of classpath and optionaly Java platform.
 * @author Tomas Zezula
 * @since org.netbeans.modules.java.api.common/1 1.5
*/
public final class LibrariesNode extends AbstractNode {

    private static final Image ICON_BADGE = ImageUtilities.loadImage("org/netbeans/modules/java/api/common/project/ui/resources/libraries-badge.png");    //NOI18N    
    public static final RequestProcessor rp = new RequestProcessor ();
    private static Icon folderIconCache;
    private static Icon openedFolderIconCache;

    private final String displayName;
    private final Action[] librariesNodeActions;


    /**
     * Creates new LibrariesNode named displayName displaying classPathProperty classpath
     * and optionaly Java platform.
     * @param displayName the display name of the node
     * @param eval {@link PropertyEvaluator} used for listening
     * @param helper {@link UpdateHelper} used for reading and updating project's metadata
     * @param refHelper {@link ReferenceHelper} used for destroying unused references
     * @param classPathProperty the ant property name of classpath which should be visualized
     * @param classPathIgnoreRef the array of ant property names which should not be displayed, may be
     * an empty array but not null
     * @param platformProperty the ant name property holding the Web platform system name or null
     * if the platform should not be displayed
     * @param librariesNodeActions actions which should be available on the created node.
     */
    public LibrariesNode (String displayName, Project project, PropertyEvaluator eval, UpdateHelper helper, ReferenceHelper refHelper,
                   String classPathProperty, String[] classPathIgnoreRef, String platformProperty,
                   Action[] librariesNodeActions, String webModuleElementName, ClassPathSupport cs,
                   Callback extraKeys) {
        super (new LibrariesChildren (project, eval, helper, refHelper, classPathProperty,
                    classPathIgnoreRef, platformProperty,
                    webModuleElementName, cs, extraKeys),
                Lookups.fixed(project, new PathFinder()));
        this.displayName = displayName;
        this.librariesNodeActions = librariesNodeActions;
    }

    @Override
    public String getDisplayName () {
        return this.displayName; 
    }

    @Override
    public String getName () {
        return this.getDisplayName();
    }    

    @Override
    public Image getIcon( int type ) {        
        return computeIcon( false, type );
    }
        
    @Override
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }

    @Override
    public Action[] getActions(boolean context) {        
        return this.librariesNodeActions;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    //Static Action Factory Methods
    public static Action createAddProjectAction (Project p, SourceRoots sources) {        
        return new AddProjectAction(p, sources);
    }

    public static Action createAddLibraryAction (ReferenceHelper helper, 
            SourceRoots sources, LibraryChooser.Filter filter) {        
        return new AddLibraryAction(helper, sources, 
                filter != null ? filter : EditMediator.createLibraryFilter());
    }

    public static Action createAddFolderAction (AntProjectHelper p, SourceRoots sources) {        
        return new AddFolderAction(p, sources);
    }
    
    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
            openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        }
        else {
            return folderIconCache;
        }
    }

    private Image computeIcon( boolean opened, int type ) {        
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = ImageUtilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }

    //Static inner classes
    private static class LibrariesChildren extends Children.Keys<Key> implements PropertyChangeListener {

        
        /**
         * Constant represneting a prefix of library reference generated by {@link org.netbeans.modules.java.j2seplatform.libraries.J2SELibraryTypeProvider}
         */
        private static final String LIBRARY_PREFIX = "${libs."; // NOI18N
        
        /**
         * Constant representing a prefix of artifact reference generated by {@link ReferenceHelper}
         */
        private static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N
        /**
         * Constant representing a prefix of file reference generated by {@link ReferenceHelper}
         */
        private static final String FILE_REF_PREFIX = "${file.reference."; //NOI18N
        /**
         * Constant representing a prefix of ant property reference
         */
        private static final String REF_PREFIX = "${"; //NOI18N
        
        private static final String LIBRARIES_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/libraries.gif"; //NOI18N
        private static final String ARCHIVE_ICON = "org/netbeans/modules/java/api/common/project/ui/resources/jar.gif";//NOI18N        

        private final PropertyEvaluator eval;
        private final UpdateHelper helper;
        private final ReferenceHelper refHelper;
        private final String classPathProperty;
        private final String platformProperty;
        private final Set<String> classPathIgnoreRef;
        private final String webModuleElementName;
        private final ClassPathSupport cs;
        
        private Callback extraKeys;
        private Project project;
        
        //XXX: Workaround: classpath is used only to listen on non existent files.
        // This should be removed when there will be API for it
        // See issue: http://www.netbeans.org/issues/show_bug.cgi?id=33162
        private RootsListener fsListener;


        LibrariesChildren (Project project, PropertyEvaluator eval, UpdateHelper helper, ReferenceHelper refHelper,
                           String classPathProperty, String[] classPathIgnoreRef, String platformProperty, 
                           String webModuleElementName, ClassPathSupport cs, Callback extraKeys) {
            this.eval = eval;
            this.helper = helper;
            this.refHelper = refHelper;
            this.classPathProperty = classPathProperty;
            this.classPathIgnoreRef = new HashSet<String>(Arrays.asList(classPathIgnoreRef));
            this.platformProperty = platformProperty;
            this.webModuleElementName = webModuleElementName;
            this.cs = cs;
            this.extraKeys = extraKeys;
            this.project = project;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            final boolean propRoots = RootsListener.PROP_ROOTS.equals(propName);
            if (classPathProperty.equals(propName) || propRoots || LibraryManager.PROP_LIBRARIES.equals(propName)) {
                synchronized (this) {
                    if (fsListener!=null) {
                        fsListener.removePropertyChangeListener (this);
                        fsListener = null;
                    }
                }
                rp.post (new Runnable () {
                    @Override
                    public void run () {
                        setKeys(getKeys());
                        if (propRoots) {
                            LogicalViewProvider2 lvp = project.getLookup().lookup(LogicalViewProvider2.class);
                            if (lvp != null) {
                                lvp.testBroken();
                            }
                        }
                    }
                });                
            }
        }

        @Override
        protected void addNotify() {
            this.eval.addPropertyChangeListener (this);
            if (refHelper.getProjectLibraryManager() != null) {
                refHelper.getProjectLibraryManager().addPropertyChangeListener(this);
            } else {
                LibraryManager.getDefault().addPropertyChangeListener(this);
            }
            this.setKeys(getKeys ());
        }

        @Override
        protected void removeNotify() {
            this.eval.removePropertyChangeListener(this);
            if (refHelper.getProjectLibraryManager() != null) {
                refHelper.getProjectLibraryManager().removePropertyChangeListener(this);
            } else {
                LibraryManager.getDefault().removePropertyChangeListener(this);
            }
            synchronized (this) {
                if (fsListener!=null) {
                    fsListener.removePropertyChangeListener (this);
                    fsListener = null;
                }
            }
            this.setKeys(Collections.<Key>emptySet());
        }

        @Override
        protected Node[] createNodes(Key key) {
            Node[] result = null;
            switch (key.getType()) {
                case Key.TYPE_PLATFORM:
                    result = new Node[] {PlatformNode.create(eval, platformProperty, cs)};
                    break;
                case Key.TYPE_PROJECT:
                    result = new Node[] {new ProjectNode(key.getProject(), key.getArtifactLocation(), helper, key.getClassPathId(),
                        key.getEntryId(), webModuleElementName, cs, refHelper)};
                    break;
                case Key.TYPE_LIBRARY:
                {
                    final Node afn = ActionFilterNode.forLibrary(
                        PackageView.createPackageView(key.getSourceGroup()),
                        helper,
                        key.getClassPathId(),
                        key.getEntryId(),
                        webModuleElementName,
                        cs,
                        refHelper);
                    result = afn == null ? new Node[0] : new Node[] {afn};
                    break;
                }
                case Key.TYPE_FILE_REFERENCE:
                {
                    final Node afn = ActionFilterNode.forArchive(
                        PackageView.createPackageView(key.getSourceGroup()),
                        helper,
                        eval,
                        key.getClassPathId(),
                        key.getEntryId(),
                        webModuleElementName,
                        cs,
                        refHelper);
                    result = afn == null ? new Node[0] : new Node[] {afn};
                    break;
                }
                case Key.TYPE_FILE:
                {
                    final Node afn = ActionFilterNode.forRoot(
                        PackageView.createPackageView(key.getSourceGroup()),
                        helper,
                        key.getClassPathId(),
                        key.getEntryId(),
                        webModuleElementName,
                        cs,
                        refHelper);
                    result = afn == null ? new Node[0] : new Node[] {afn};
                    break;
                }
                case Key.TYPE_OTHER:
                    result = extraKeys.createNodes(key);
                    break;
            }
            if (result == null) {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            return result;
        }
        
        private List<Key> getKeys () {
            EditableProperties projectSharedProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            EditableProperties projectPrivateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            EditableProperties privateProps = PropertyUtils.getGlobalProperties();
            List<URL> rootsList = new ArrayList<URL>();
            List<Key> result = getKeys (projectSharedProps, projectPrivateProps, privateProps, classPathProperty, rootsList);
            if (platformProperty!=null) {
                result.add (Key.platform());
            }
            final RootsListener rootsListener = new RootsListener(rootsList);
            rootsListener.addPropertyChangeListener(this);
            synchronized (this) {
                fsListener = rootsListener;
            }
            if (extraKeys != null) {
                result.addAll(extraKeys.getExtraKeys());
            }
            return result;
        }

        private List<Key> getKeys (EditableProperties projectSharedProps, EditableProperties projectPrivateProps,
                              EditableProperties privateProps, String currentClassPath, List<URL> rootsList) {
            List<Key> result = new ArrayList<Key>();
            String raw = projectSharedProps.getProperty (currentClassPath);
            if (raw == null) {
                raw = projectPrivateProps.getProperty(currentClassPath);
            }
            if (raw == null) {
                raw = privateProps.getProperty(currentClassPath);
            }
            if (raw == null) {
                return result;
            }
            List<String> pe = new ArrayList<String>(Arrays.asList(PropertyUtils.tokenizePath( raw )));
            while (pe.size()>0){
                String prop = pe.remove(0);
                String propName = CommonProjectUtils.getAntPropertyName (prop);
                if (classPathIgnoreRef.contains(propName)) {
                    continue;
                }
                else if (prop.startsWith( LIBRARY_PREFIX )) {
                    //Library reference
                    String eval = prop.substring( LIBRARY_PREFIX.length(), prop.lastIndexOf('.') ); //NOI18N
                    Library lib = refHelper.findLibrary(eval);
                    if (lib != null) {
                        Icon libIcon = ImageUtilities.loadImageIcon(LIBRARIES_ICON, false);
                        for (URL rootUrl : lib.getContent("classpath")) {
                            rootsList.add (rootUrl);
                            FileObject root = URLMapper.findFileObject (rootUrl);
                            if (root != null && root.isFolder()) {
                                String displayName;
                                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                                    FileObject file = FileUtil.getArchiveFile (root);
                                    displayName = file.getNameExt();
                                } else {
                                    File file = FileUtil.toFile (root);
                                    if (file != null) {
                                        displayName = file.getAbsolutePath();
                                    } else {
                                        displayName = root.getNameExt();
                                    }
                                }
                                displayName = MessageFormat.format (
                                    NbBundle.getMessage (LibrariesNode.class,"TXT_LibraryPartFormat"),
                                    new Object[] {lib.getDisplayName(), displayName});
                                SourceGroup sg = new LibrariesSourceGroup (root, displayName, libIcon, libIcon);
                                result.add (Key.library(sg,currentClassPath, propName));
                            }
                        }
                    }
                    //Todo: May try to resolve even broken library
                }
                else if (prop.startsWith(ANT_ARTIFACT_PREFIX)) {
                    //Project reference
                    Object[] ref = refHelper.findArtifactAndLocation(prop);
                    if (ref[0] != null && ref[1] != null) {
                        AntArtifact artifact = (AntArtifact)ref[0];
                        URI uri = (URI)ref[1];
                        result.add(Key.project(artifact, uri, currentClassPath, propName));
                    }
                }
                else if (prop.startsWith(FILE_REF_PREFIX)) {
                    //File reference
                    String evaluatedRef = eval.getProperty(propName);
                    if (evaluatedRef != null) {
                        File file = helper.getAntProjectHelper().resolveFile(evaluatedRef);
                        SourceGroup sg = createFileSourceGroup(file,rootsList);
                        if (sg !=null) {
                            result.add (Key.fileReference(sg,currentClassPath, propName));
                        }
                    }
                }
                else if (prop.startsWith(REF_PREFIX)) {
                    //Path reference
                    result.addAll(getKeys(projectSharedProps, projectPrivateProps, privateProps,propName, rootsList));
                }
                else {
                    //file
                    File file = helper.getAntProjectHelper().resolveFile(prop);
                    SourceGroup sg = createFileSourceGroup(file,rootsList);
                    if (sg !=null) {
                        result.add (Key.file(sg,currentClassPath, propName));
                    }
                }
            }
            return result;
        }

        private static SourceGroup createFileSourceGroup (File file, List<URL> rootsList) {
            Icon icon;
            Icon openedIcon;
            String displayName;
            final URL url = FileUtil.urlForArchiveOrDir(file);
            if (url == null) {
                return null;
            }
            else if ("jar".equals(url.getProtocol())) {  //NOI18N
                icon = openedIcon = ImageUtilities.loadImageIcon(ARCHIVE_ICON, false);
                displayName = file.getName();
            }
            else {                                
                icon = getFolderIcon (false);
                openedIcon = getFolderIcon (true);
                displayName = file.getAbsolutePath();
            }
            rootsList.add (url);
            FileObject root = URLMapper.findFileObject (url);
            if (root != null) {
                return new LibrariesSourceGroup (root,displayName,icon,openedIcon);
            }
            return null;
        }        
    }

    //XXX: Leaking of implementation, should be pkg private
    //the reason why it's public is wrongly designed Callback interface
    public static final class Key {
        static final int TYPE_PLATFORM = 0;         //platform
        static final int TYPE_LIBRARY = 1;          //library
        static final int TYPE_FILE_REFERENCE = 2;   //file added by ReferenceHelper ${reference.
        static final int TYPE_PROJECT = 3;          //project
        static final int TYPE_OTHER = 4;            //extension provided by Callback
        static final int TYPE_FILE = 5;             //direct file not added by ReferenceHelper

        private int type;
        private String classPathId;
        private String entryId;
        private SourceGroup sg;
        private AntArtifact antArtifact;
        private URI uri;
        private String anID;
                

        private static Key platform() {
            return new Key();
        }
        
        private static Key project(AntArtifact a, URI uri, String classPathId, String entryId) {
            return new Key(a, uri, classPathId, entryId);
        }
        
        private static Key library(SourceGroup sg, String classPathId, String entryId) {
            return new Key(TYPE_LIBRARY, sg, classPathId, entryId);
        }
        
        private static Key fileReference(SourceGroup sg, String classPathId, String entryId) {
            return new Key(TYPE_FILE_REFERENCE, sg, classPathId, entryId);
        }

        private static Key file(SourceGroup sg, String classPathId, String entryId) {
            return new Key(TYPE_FILE, sg, classPathId, entryId);
        }

        public Key (String anID) {
            this.type = TYPE_OTHER;
            this.anID = anID;

        }

        private Key () {
            type = TYPE_PLATFORM;
        }

        private Key (int type, SourceGroup sg, String classPathId, String entryId) {
            assert type == TYPE_LIBRARY || type == TYPE_FILE_REFERENCE || type == TYPE_FILE;
            this.type = type;
            this.sg = sg;
            this.classPathId = classPathId;
            this.entryId = entryId;
        }

        private Key (AntArtifact a, URI uri, String classPathId, String entryId) {
            this.type = TYPE_PROJECT;
            this.antArtifact = a;
            this.uri = uri;
            this.classPathId = classPathId;
            this.entryId = entryId;
        }

        public int getType () {
            return this.type;
        }

        public String getClassPathId () {
            return this.classPathId;
        }

        public String getEntryId () {
            return this.entryId;
        }

        public SourceGroup getSourceGroup () {
            return this.sg;
        }

        public AntArtifact getProject() {
            return this.antArtifact;
        }
        
        public URI getArtifactLocation() {
            return this.uri;
        }

        public String getID() {
            return anID;
        }

        
        @Override
        public int hashCode() {
            int hashCode = this.type<<16;
            switch (this.type) {
                case TYPE_LIBRARY:
                case TYPE_FILE_REFERENCE:
                case TYPE_FILE:
                    hashCode ^= this.sg == null ? 0 : this.sg.hashCode();
                    break;
                case TYPE_PROJECT:
                    hashCode ^= this.antArtifact == null ? 0 : this.antArtifact.hashCode();
                    break;
                case TYPE_OTHER:
                    hashCode ^= anID.hashCode();
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (other.type != type) {
                return false;
            }
            switch (type) {
                case TYPE_LIBRARY:
                case TYPE_FILE_REFERENCE:
                case TYPE_FILE:
                    return (this.sg == null ? other.sg == null : this.sg.equals(other.sg)) &&
                        (this.classPathId == null ? other.classPathId == null : this.classPathId.equals (other.classPathId)) &&
                        (this.entryId == null ? other.entryId == null : this.entryId.equals (other.entryId));
                case TYPE_PROJECT:
                    return (this.antArtifact == null ? other.antArtifact == null : this.antArtifact.equals(other.antArtifact)) &&
                        (this.classPathId == null ? other.classPathId == null : this.classPathId.equals (other.classPathId)) &&
                        (this.entryId == null ? other.entryId == null : this.entryId.equals (other.entryId));
                case TYPE_PLATFORM:
                    return true;
                case TYPE_OTHER:
                    return anID.equals(other.anID);
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private static class AddProjectAction extends AbstractAction {

        private final Project project;
        private final SourceRoots sources;

        public AddProjectAction (Project project, SourceRoots sources) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddProject_Action" ) );
            this.project = project;
            this.sources = sources;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AntArtifactItem ai[] = AntArtifactItem.showAntArtifactItemChooser(
                    new String[] {JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER},
                    project, null);
                if ( ai != null ) {
                    addArtifacts( ai );
                }
        }

        @Override
        public boolean isEnabled() {
            return this.sources.getRoots().length > 0;
        }

        private void addArtifacts (AntArtifactItem[] artifactItems) {
            final FileObject[] roots = this.sources.getRoots();
            if (roots.length == 0) {
                return;
            }
            final FileObject projectSourcesArtifact = roots[0];
            AntArtifact[] artifacts = new AntArtifact[artifactItems.length];
            URI[] artifactURIs = new URI[artifactItems.length];
            for (int i = 0; i < artifactItems.length; i++) {
                artifacts[i] = artifactItems[i].getArtifact();
                artifactURIs[i] = artifactItems[i].getArtifactURI();
            }
            try {
                ProjectClassPathModifier.addAntArtifacts(artifacts, artifactURIs,
                        projectSourcesArtifact, ClassPath.COMPILE);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    private static class AddLibraryAction extends AbstractAction {

        private final LibraryChooser.Filter filter;
        private final SourceRoots sourceRoots;
        private ReferenceHelper refHelper;

        public AddLibraryAction(ReferenceHelper refHelper, SourceRoots sourceRoots, LibraryChooser.Filter filter) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddLibrary_Action" ) );
            this.refHelper = refHelper;
            this.sourceRoots = sourceRoots;
            this.filter = filter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Set<Library> added = LibraryChooser.showDialog(
                    refHelper.getProjectLibraryManager(), filter,
                    refHelper.getLibraryChooserImportHandler());
            if (added != null) {
                addLibraries(added.toArray(new Library[added.size()]));
            }
        }

        @Override
        public boolean isEnabled() {
            return this.sourceRoots.getRoots().length > 0;
        }

        private void addLibraries (Library[] libraries) {
            final FileObject[] roots = this.sourceRoots.getRoots();
            if (roots.length == 0) {
                return;
            }
            final FileObject projectSourcesArtifact = roots[0];
            try {
                ProjectClassPathModifier.addLibraries(libraries,
                        projectSourcesArtifact, ClassPath.COMPILE);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
    }

    private static class AddFolderAction extends AbstractAction {

        private final AntProjectHelper helper;
        private final SourceRoots sources;

        public AddFolderAction (AntProjectHelper helper, SourceRoots sources) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddFolder_Action" ) );
            this.helper = helper;
            this.sources = sources;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            org.netbeans.api.project.ant.FileChooser chooser;
            if (helper.isSharableProject()) {
                chooser = new org.netbeans.api.project.ant.FileChooser(helper, true);
            } else {
                chooser = new org.netbeans.api.project.ant.FileChooser(FileUtil.toFile(helper.getProjectDirectory()), null);
            }
            chooser.enableVariableBasedSelection(true);
            chooser.setFileHidingEnabled(false);
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
            chooser.setMultiSelectionEnabled( true );
            chooser.setDialogTitle( NbBundle.getMessage( LibrariesNode.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
            //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
            chooser.setAcceptAllFileFilterUsed( false );
            FileFilter fileFilter = new SimpleFileFilter (
                    NbBundle.getMessage( LibrariesNode.class, "LBL_ZipJarFolderFilter" )); // NOI18N
            chooser.setFileFilter(fileFilter);
            File curDir = EditMediator.getLastUsedClassPathFolder();
            chooser.setCurrentDirectory (curDir);
            int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() );
            if ( option == JFileChooser.APPROVE_OPTION ) {
                String filePaths[];
                try {
                    filePaths = chooser.getSelectedPaths();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return;
                }
                addJarOrFolder ( filePaths, chooser.getSelectedPathVariables(), fileFilter, FileUtil.toFile(helper.getProjectDirectory()));
                curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                EditMediator.setLastUsedClassPathFolder(curDir);
            }
        }

        @Override
        public boolean isEnabled() {
            return this.sources.getRoots().length > 0;
        }

        private void addJarOrFolder (String[] filePaths, final String[] pathBasedVariables, FileFilter fileFilter, File base) {
            final FileObject[] roots = this.sources.getRoots();
            if (roots.length == 0) {
                return;
            }
            try {
                final FileObject projectSourcesArtifact = roots[0];
                final List<URI> toAdd = new ArrayList<URI>(filePaths.length);            
                for (int i=0; i<filePaths.length;i++) {
                    //Check if the file is acceted by the FileFilter,
                    //user may enter the name of non displayed file into JFileChooser
                    File fl = PropertyUtils.resolveFile(base, filePaths[i]);
                    FileObject fo = FileUtil.toFileObject(fl);
                    assert fo != null || !fl.canRead(): fl;
                    if (fo != null && fileFilter.accept(fl)) {
                        URI u;
                        boolean isArchiveFile = FileUtil.isArchiveFile(fo);
                        if (pathBasedVariables == null) {
                            u = LibrariesSupport.convertFilePathToURI(filePaths[i]);
                        } else {
                            try {
                                String path = pathBasedVariables[i];
                                // append slash before creating relative URI:
                                if (!isArchiveFile && !path.endsWith("/")) { // NOI18N
                                    path += "/"; // NOI18N
                                }
                                // create relative URI
                                u = new URI(null, null, path, null);
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                                u = LibrariesSupport.convertFilePathToURI(filePaths[i]);
                            }
                        }
                        if (isArchiveFile) {
                            try {
                                new JarFile (fl);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog (
                                    WindowManager.getDefault ().getMainWindow (),
                                    NbBundle.getMessage (LibrariesNode.class, "LBL_Corrupted_JAR", fl),
                                    NbBundle.getMessage (LibrariesNode.class, "LBL_Corrupted_JAR_title"),
                                    JOptionPane.WARNING_MESSAGE
                                );
                                continue;
                            }
                            u = LibrariesSupport.getArchiveRoot(u);
                        } else if (!u.toString().endsWith("/")) { // NOI18N
                            try {
                                u = new URI(u.toString() + "/"); // NOI18N
                            } catch (URISyntaxException ex) {
                                throw new AssertionError(ex);
                            }
                        }
                        assert u != null;
                        toAdd.add(u);
                    }                    
                }
                final Project prj = FileOwnerQuery.getOwner(helper.getProjectDirectory());
                final ClassPathModifier modifierImpl = prj.getLookup().lookup(ClassPathModifier.class);
                if (modifierImpl == null) {
                    throw new IllegalStateException(
                        String.format("Project: %s (located in: %s) does not provide ClassPathModifier in Lookup.",   //NOI18N
                        prj,
                        FileUtil.getFileDisplayName(prj.getProjectDirectory())));
                } else {
                    modifierImpl.addRoots(toAdd.toArray(new URI[toAdd.size()]),
                        findSourceGroup(projectSourcesArtifact, modifierImpl),
                        ClassPath.COMPILE,
                        ClassPathModifier.ADD_NO_HEURISTICS);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

    }
    
    private static SourceGroup findSourceGroup(FileObject fo, ClassPathModifier modifierImpl) {
        SourceGroup[]sgs = modifierImpl.getExtensibleSourceGroups();
        for (SourceGroup sg : sgs) {
            if ((fo == sg.getRootFolder() || FileUtil.isParentOf(sg.getRootFolder(),fo)) && sg.contains(fo)) {
                return sg;
            }
        }
        throw new AssertionError("Cannot find source group for '"+fo+"' in "+Arrays.asList(sgs)); // NOI18N
    }

    private static class SimpleFileFilter extends FileFilter {

        private String description;

        private final Set<String> extensions;


        public SimpleFileFilter (String description) {
            this.description = description;
            this.extensions = new HashSet<String>();
            this.extensions.addAll(FileUtil.getMIMETypeExtensions("application/x-java-archive"));    //NOI18N
        }

        @Override
        public boolean accept(final File f) {
            if (f.isDirectory()) {
                return true;
            }
            try {
                //Can use FileUtil.getMIMEType(fo, withinMIMETypes), but this should be even faster no FO is created
                if (!this.extensions.isEmpty()) {
                    final String fileName = f.getName();
                    int index = fileName.lastIndexOf('.');  //NOI18N
                    if (index > 0 && index < fileName.length()-1) {
                        return extensions.contains(fileName.substring(index+1));
                    }
                }
                else {
                    //No MimeResolver fallback
                    return FileUtil.isArchiveFile(Utilities.toURI(f).toURL());
                }
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);                
            }
            return false;
        }

        @Override
        public String getDescription() {
            return this.description;
        }
    }

    /**
     * Optional extension point to enhance LibrariesNode with additional nodes,
     * for example J2EE project type may add J2EE platform node.
     */
    public static interface Callback {

        /** Enhance LibrariesNode with additional <code>Key</code>s.*/
        List<Key> getExtraKeys();

        /** Creates nodes for extra key. */
        Node[] createNodes(Key key);
    }

    private static final class PathFinder implements org.netbeans.spi.project.ui.PathFinder {
        

        PathFinder() {
        }

        @Override
        public Node findPath(Node root, Object target) {
            Node result = null;
            for (Node  node : root.getChildren().getNodes(true)) {
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

    private static class RootsListener implements FileChangeListener  {

        static final String PROP_ROOTS = "roots";   //NOI18N

        private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        private final Collection<File> listensOn;
        private final AtomicInteger state = new AtomicInteger();

        RootsListener(List<? extends URL> roots) {
            listensOn = new HashSet<File>();
            for (URL root : roots) {
                try {
                    final URL archiveURL = FileUtil.getArchiveFile(root);
                    if (archiveURL != null) {
                        root = archiveURL;
                    }
                    listensOn.add(Utilities.toFile(root.toURI()));
                } catch (IllegalArgumentException e) {
                    //Ignore - not a local file
                } catch (URISyntaxException e) {
                    //Ignore - not a local file
                }
            }
        }


        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            if (!state.compareAndSet(0, 1)) {
                throw new IllegalStateException("Already in state: " + state.get());    //NOI18N
            }
            support.addPropertyChangeListener(listener);
            for (File f : listensOn) {
                FileUtil.addFileChangeListener(this, f);
            }
        }

        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            if (!state.compareAndSet(1, 2)) {
                throw new IllegalStateException("Already in state: " + state.get());    //NOI18N
            }
            support.removePropertyChangeListener(listener);
            for (File f : listensOn) {
                FileUtil.removeFileChangeListener(this, f);
            }
        }        

        @Override
        public void fileFolderCreated(FileEvent fe) {
            fire();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            fire();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fire();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fire();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fire();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        private void fire() {
            support.firePropertyChange(PROP_ROOTS, null, null);
        }

    }

    

}

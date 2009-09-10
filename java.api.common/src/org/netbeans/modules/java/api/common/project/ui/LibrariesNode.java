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
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
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
import org.openide.util.Exceptions;
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
                Lookups.singleton(project));
        this.displayName = displayName;
        this.librariesNodeActions = librariesNodeActions;
    }

    public String getDisplayName () {
        return this.displayName; 
    }

    public String getName () {
        return this.getDisplayName();
    }    

    public Image getIcon( int type ) {        
        return computeIcon( false, type );
    }
        
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }

    public Action[] getActions(boolean context) {        
        return this.librariesNodeActions;
    }

    public boolean canCopy() {
        return false;
    }

    //Static Action Factory Methods
    public static Action createAddProjectAction (Project p, SourceRoots sources) {
        if (sources.getRoots().length == 0) {
            return null;
        }
        return new AddProjectAction(p, sources.getRoots()[0]);
    }

    public static Action createAddLibraryAction (ReferenceHelper helper, 
            SourceRoots sources, LibraryChooser.Filter filter) {
        if (sources.getRoots().length == 0) {
            return null;
        }
        return new AddLibraryAction(helper, sources.getRoots()[0], 
                filter != null ? filter : EditMediator.createLibraryFilter());
    }

    public static Action createAddFolderAction (AntProjectHelper p, SourceRoots sources) {
        if (sources.getRoots().length == 0) {
            return null;
        }
        return new AddFolderAction(p, sources.getRoots()[0]);
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
        private ClassPath fsListener;


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

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            final boolean propRoots = ClassPath.PROP_ROOTS.equals(propName);
            if (classPathProperty.equals(propName) || propRoots || LibraryManager.PROP_LIBRARIES.equals(propName)) {
                synchronized (this) {
                    if (fsListener!=null) {
                        fsListener.removePropertyChangeListener (this);
                    }
                }
                rp.post (new Runnable () {
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

        protected void addNotify() {
            this.eval.addPropertyChangeListener (this);
            if (refHelper.getProjectLibraryManager() != null) {
                refHelper.getProjectLibraryManager().addPropertyChangeListener(this);
            } else {
                LibraryManager.getDefault().addPropertyChangeListener(this);
            }
            this.setKeys(getKeys ());
        }

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
                    result = new Node[] {ActionFilterNode.create(PackageView.createPackageView(key.getSourceGroup()),
                        helper, key.getClassPathId(), key.getEntryId(), webModuleElementName, cs, refHelper)};
                    break;
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
                result.add (new Key());
            }
            //XXX: Workaround: Remove this when there will be API for listening on nonexistent files
            // See issue: http://www.netbeans.org/issues/show_bug.cgi?id=33162
            ClassPath cp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath (rootsList.toArray(new URL[rootsList.size()]));
            cp.addPropertyChangeListener (this);
            cp.getRoots();
            synchronized (this) {
                fsListener = cp;
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
                        List/*<URL>*/ roots = lib.getContent("classpath");  //NOI18N
                        Icon libIcon = ImageUtilities.loadImageIcon(LIBRARIES_ICON, false);
                        for (Iterator it = roots.iterator(); it.hasNext();) {
                            URL rootUrl = (URL) it.next();
                            rootsList.add (rootUrl);
                            FileObject root = URLMapper.findFileObject (rootUrl);
                            if (root != null) {
                                String displayName;
                                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                                    FileObject file = FileUtil.getArchiveFile (root);
                                    displayName = file.getNameExt();
                                }
                                else {
                                    File file = FileUtil.toFile (root);
                                    if (file != null) {
                                        displayName = file.getAbsolutePath();
                                    }
                                    else {
                                        displayName = root.getNameExt();
                                    }
                                }
                                displayName = MessageFormat.format (
                                    NbBundle.getMessage (LibrariesNode.class,"TXT_LibraryPartFormat"),
                                    new Object[] {lib.getDisplayName(), displayName});
                                SourceGroup sg = new LibrariesSourceGroup (root, displayName, libIcon, libIcon);
                                result.add (new Key(sg,currentClassPath, propName));
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
                        result.add(new Key(artifact, uri, currentClassPath, propName));                 
                    }
                }
                else if (prop.startsWith(FILE_REF_PREFIX)) {
                    //File reference
                    String evaluatedRef = eval.getProperty(propName);
                    if (evaluatedRef != null) {
                        File file = helper.getAntProjectHelper().resolveFile(evaluatedRef);
                        SourceGroup sg = createFileSourceGroup(file,rootsList);
                        if (sg !=null) {
                            result.add (new Key(sg,currentClassPath, propName));
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
                        result.add ( new Key(sg,currentClassPath, propName));
                    }
                }
            }
            return result;
        }

        private static SourceGroup createFileSourceGroup (File file, List<URL> rootsList) {
            Icon icon;
            Icon openedIcon;
            String displayName;
            try {
                URL url = file.toURI().toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot(url);
                    icon = openedIcon = ImageUtilities.loadImageIcon(ARCHIVE_ICON, false);
                    displayName = file.getName();
                }
                else {
                    String sURL = url.toExternalForm();
                    if (!sURL.endsWith("/")) {  //NOI18N
                        url = new URL (sURL+"/");   //NOI18N
                    }
                    icon = getFolderIcon (false);
                    openedIcon = getFolderIcon (true);
                    displayName = file.getAbsolutePath();
                }
                rootsList.add (url);
                FileObject root = URLMapper.findFileObject (url);
                if (root != null) {
                    return new LibrariesSourceGroup (root,displayName,icon,openedIcon);
                }
            } catch (MalformedURLException e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }        
    }

    public static final class Key {
        static final int TYPE_PLATFORM = 0;
        static final int TYPE_LIBRARY = 1;
        static final int TYPE_PROJECT = 2;
        static final int TYPE_OTHER = 3;

        private int type;
        private String classPathId;
        private String entryId;
        private SourceGroup sg;
        private AntArtifact antArtifact;
        private URI uri;
        private String anID;
        
        Key () {
            type = TYPE_PLATFORM;
        }

        public Key (String anID) {
            this.type = TYPE_OTHER;
            this.anID = anID;

        }

        Key (SourceGroup sg, String classPathId, String entryId) {
            this.type = TYPE_LIBRARY;
            this.sg = sg;
            this.classPathId = classPathId;
            this.entryId = entryId;
        }

        Key (AntArtifact a, URI uri, String classPathId, String entryId) {
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

        
        public int hashCode() {
            int hashCode = this.type<<16;
            switch (this.type) {
                case TYPE_LIBRARY:
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
        private final FileObject projectSourcesArtifact;

        public AddProjectAction (Project project, FileObject projectSourcesArtifact) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddProject_Action" ) );
            this.project = project;
            this.projectSourcesArtifact = projectSourcesArtifact;
        }

        public void actionPerformed(ActionEvent e) {
            AntArtifactItem ai[] = AntArtifactItem.showAntArtifactItemChooser(
                    new String[] {JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER},
                    project, null);
                if ( ai != null ) {
                    addArtifacts( ai );
                }
        }

        private void addArtifacts (AntArtifactItem[] artifactItems) {
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
        private final FileObject projectSourcesArtifact;
        private ReferenceHelper refHelper;

        public AddLibraryAction(ReferenceHelper refHelper, FileObject projectSourcesArtifact, LibraryChooser.Filter filter) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddLibrary_Action" ) );
            this.refHelper = refHelper;
            this.projectSourcesArtifact = projectSourcesArtifact;
            this.filter = filter;
        }

        public void actionPerformed(ActionEvent e) {
            Set<Library> added = LibraryChooser.showDialog(
                    refHelper.getProjectLibraryManager(), filter,
                    refHelper.getLibraryChooserImportHandler());
            if (added != null) {
                addLibraries(added.toArray(new Library[added.size()]));
            }
        }

        private void addLibraries (Library[] libraries) {
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
        private final FileObject projectSourcesArtifact;

        public AddFolderAction (AntProjectHelper helper, FileObject projectSourcesArtifact) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddFolder_Action" ) );
            this.helper = helper;
            this.projectSourcesArtifact = projectSourcesArtifact;
        }

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

        private void addJarOrFolder (String[] filePaths, final String[] pathBasedVariables, FileFilter fileFilter, File base) {
            for (int i=0; i<filePaths.length;i++) {
                try {
                    //Check if the file is acceted by the FileFilter,
                    //user may enter the name of non displayed file into JFileChooser
                    File fl = PropertyUtils.resolveFile(base, filePaths[i]);
                    FileObject fo = FileUtil.toFileObject(fl);
                    assert fo != null : fl;
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
                        Project prj = FileOwnerQuery.getOwner(helper.getProjectDirectory());
                        ClassPathModifier modifierImpl = prj.getLookup().lookup(ClassPathModifier.class);
                        assert modifierImpl != null : prj.getProjectDirectory();
                        modifierImpl.addRoots(new URI[]{u}, findSourceGroup(projectSourcesArtifact, modifierImpl), ClassPath.COMPILE, ClassPathModifier.ADD_NO_HEURISTICS);
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
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
                    return FileUtil.isArchiveFile(f.toURI().toURL());
                }
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);                
            }
            return false;
        }

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

}

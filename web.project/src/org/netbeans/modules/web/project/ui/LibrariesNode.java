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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.web.project.ui.customizer.AntArtifactChooser.ArtifactItem;
import org.netbeans.modules.web.project.ui.customizer.WebClassPathUi;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.web.project.UpdateHelper;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.classpath.WebProjectClassPathExtender;
import org.netbeans.modules.web.project.ui.customizer.AntArtifactChooser;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.web.project.ui.customizer.LibrariesChooser;
import org.openide.util.lookup.Lookups;

/**
 * LibrariesNode displays the content of classpath and optionaly Java platform.
 * @author Tomas Zezula
 */
final class LibrariesNode extends AbstractNode {

    private static final Image ICON_BADGE = Utilities.loadImage("org/netbeans/modules/web/project/ui/resources/libraries-badge.png");    //NOI18N
    static final RequestProcessor rp = new RequestProcessor ();
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
    LibrariesNode (String displayName, Project project, PropertyEvaluator eval, UpdateHelper helper, ReferenceHelper refHelper,
                   String classPathProperty, String[] classPathIgnoreRef, String platformProperty, String j2eePlatformProperty,
                   Action[] librariesNodeActions, String webModuleElementName) {
        super (new LibrariesChildren (eval, helper, refHelper, classPathProperty, classPathIgnoreRef, platformProperty, j2eePlatformProperty, webModuleElementName), Lookups.singleton(project));
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
    public static Action createAddProjectAction (Project p, String classPathId, String webModuleElementName) {
        return new AddProjectAction (p, classPathId, webModuleElementName);
    }

    public static Action createAddLibraryAction (Project p, String classPathId, String webModuleElementName) {
        return new AddLibraryAction (p, classPathId, webModuleElementName);
    }

    public static Action createAddFolderAction (Project p, String classPathId, String webModuleElementName) {
        return new AddFolderAction (p, classPathId, webModuleElementName);
    }
    
    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
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
        image = Utilities.mergeImages(image, ICON_BADGE, 7, 7 );
        return image;        
    }

    //Static inner classes
    private static class LibrariesChildren extends Children.Keys implements PropertyChangeListener {

        
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
        
        private static final String LIBRARIES_ICON = "org/netbeans/modules/web/project/ui/resources/libraries.gif"; //NOI18N
        private static final String ARCHIVE_ICON = "org/netbeans/modules/web/project/ui/resources/jar.gif";//NOI18N        

        private final PropertyEvaluator eval;
        private final UpdateHelper helper;
        private final ReferenceHelper refHelper;
        private final String classPathProperty;
        private final String platformProperty;
        private final String j2eePlatformProperty;
        private final Set classPathIgnoreRef;
        private final String webModuleElementName;

        //XXX: Workaround: classpath is used only to listen on non existent files.
        // This should be removed when there will be API for it
        // See issue: http://www.netbeans.org/issues/show_bug.cgi?id=33162
        private ClassPath fsListener;


        LibrariesChildren (PropertyEvaluator eval, UpdateHelper helper, ReferenceHelper refHelper,
                           String classPathProperty, String[] classPathIgnoreRef, String platformProperty, String j2eePlatformProperty, String webModuleElementName) {
            this.eval = eval;
            this.helper = helper;
            this.refHelper = refHelper;
            this.classPathProperty = classPathProperty;
            this.classPathIgnoreRef = new HashSet(Arrays.asList(classPathIgnoreRef));
            this.platformProperty = platformProperty;
            this.j2eePlatformProperty = j2eePlatformProperty;
            this.webModuleElementName = webModuleElementName;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (classPathProperty.equals(propName) || ClassPath.PROP_ROOTS.equals(propName)) {
                synchronized (this) {
                    if (fsListener!=null) {
                        fsListener.removePropertyChangeListener (this);
                    }
                }
                rp.post (new Runnable () {
                    public void run () {
                        setKeys(getKeys());
                    }
                });
            }
        }

        protected void addNotify() {
            this.eval.addPropertyChangeListener (this);
            this.setKeys(getKeys ());
        }

        protected void removeNotify() {
            this.eval.removePropertyChangeListener(this);
            synchronized (this) {
                if (fsListener!=null) {
                    fsListener.removePropertyChangeListener (this);
                    fsListener = null;
                }
            }
            this.setKeys(Collections.EMPTY_SET);
        }

        protected Node[] createNodes(Object obj) {
            Node[] result = null;
            if (obj instanceof Key) {
                Key key = (Key) obj;
                switch (key.getType()) {
                    case Key.TYPE_PLATFORM:
                        result = new Node[] {PlatformNode.create(eval, platformProperty)};
                        break;
                    case Key.TYPE_J2EE_PLATFORM:
                        Project p = FileOwnerQuery.getOwner(helper.getAntProjectHelper().getProjectDirectory());
                        result = new Node[] {J2eePlatformNode.create(p, eval, j2eePlatformProperty)};
                        break;
                    case Key.TYPE_PROJECT:
                        result = new Node[] {new ProjectNode(key.getProject(), key.getArtifactLocation(), helper, eval, refHelper, key.getClassPathId(),
                            key.getEntryId(), webModuleElementName)};
                        break;
                    case Key.TYPE_LIBRARY:
                        result = new Node[] {ActionFilterNode.create(PackageView.createPackageView(key.getSourceGroup()),
                            helper, eval, refHelper, key.getClassPathId(), key.getEntryId(), webModuleElementName)};
                        break;
                }
            }
            if (result == null) {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            return result;
        }
        
        private List getKeys () {
            EditableProperties projectSharedProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            EditableProperties projectPrivateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            EditableProperties privateProps = PropertyUtils.getGlobalProperties();
            List/*<URL>*/ rootsList = new ArrayList ();
            List result = getKeys (projectSharedProps, projectPrivateProps, privateProps, classPathProperty, rootsList);
            if (platformProperty!=null) {
                result.add (new Key());
            }
            if (j2eePlatformProperty != null) {
                result.add(new Key(true));
            }
            //XXX: Workaround: Remove this when there will be API for listening on nonexistent files
            // See issue: http://www.netbeans.org/issues/show_bug.cgi?id=33162
            ClassPath cp = ClassPathSupport.createClassPath ((URL[])rootsList.toArray(new URL[rootsList.size()]));
            cp.addPropertyChangeListener (this);
            cp.getRoots();
            synchronized (this) {
                fsListener = cp;
            }
            return result;
        }

        private List getKeys (EditableProperties projectSharedProps, EditableProperties projectPrivateProps,
                              EditableProperties privateProps, String currentClassPath, List/*<URL>*/ rootsList) {
            List result = new ArrayList ();
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
            List pe = new ArrayList(Arrays.asList(PropertyUtils.tokenizePath( raw )));
            while (pe.size()>0){
                String prop = (String) pe.remove(0);
                String propName = WebProjectProperties.getAntPropertyName (prop);
                if (classPathIgnoreRef.contains(propName)) {
                    continue;
                }
                else if (prop.startsWith( LIBRARY_PREFIX )) {
                    //Library reference
                    String eval = prop.substring( LIBRARY_PREFIX.length(), prop.lastIndexOf('.') ); //NOI18N
                    Library lib = LibraryManager.getDefault().getLibrary (eval);
                    if (lib != null) {
                        List/*<URL>*/ roots = lib.getContent("classpath");  //NOI18N
                        Icon libIcon = new ImageIcon (Utilities.loadImage(LIBRARIES_ICON));
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
                    File file = helper.getAntProjectHelper().resolveFile(evaluatedRef);
                    SourceGroup sg = createFileSourceGroup(file,rootsList);
                    if (sg !=null) {
                        result.add (new Key(sg,currentClassPath, propName));
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

        private static SourceGroup createFileSourceGroup (File file, List/*<URL>*/ rootsList) {
            Icon icon;
            Icon openedIcon;
            String displayName;
            try {
                URL url = file.toURI().toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot(url);
                    icon = openedIcon = new ImageIcon (Utilities.loadImage(ARCHIVE_ICON));
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
                ErrorManager.getDefault().notify(e);
            }
            return null;
        }        
    }

    private static class Key {
        static final int TYPE_PLATFORM = 0;
        static final int TYPE_LIBRARY = 1;
        static final int TYPE_PROJECT = 2;
        static final int TYPE_J2EE_PLATFORM = 3;

        private int type;
        private String classPathId;
        private String entryId;
        private SourceGroup sg;
        private AntArtifact antArtifact;
        private URI uri;
        
        Key () {
            this(false);
        }

        Key (boolean j2ee) {
            this.type = j2ee ? TYPE_J2EE_PLATFORM : TYPE_PLATFORM;
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
        
        public int hashCode() {
            int hashCode = this.type<<16;
            switch (this.type) {
                case TYPE_LIBRARY:
                    hashCode ^= this.sg == null ? 0 : this.sg.hashCode();
                    break;
                case TYPE_PROJECT:
                    hashCode ^= this.antArtifact == null ? 0 : this.antArtifact.hashCode();
                    break;
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
                case TYPE_J2EE_PLATFORM:
                    return true;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private static class AddProjectAction extends AbstractAction {

        private final Project project;
        private final String classPathId;
        private final String webModuleElementName;

        public AddProjectAction (Project project, String classPathId, String webModuleElementName) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddProject_Action" ) );
            this.project = project;
            this.classPathId = classPathId;
            this.webModuleElementName = webModuleElementName;
        }

        public void actionPerformed(ActionEvent e) {
            ArtifactItem artifacts[] = AntArtifactChooser.showDialog(JavaProjectConstants.ARTIFACT_TYPE_JAR, project, null);
                if ( artifacts != null ) {
                    addArtifacts( artifacts );
                }
        }

        private void addArtifacts (ArtifactItem[] artifactItems) {
            WebProjectClassPathExtender cpExtender = (WebProjectClassPathExtender) project.getLookup().lookup(WebProjectClassPathExtender.class);
            if (cpExtender != null) {
                try {
                    cpExtender.addAntArtifacts(classPathId, artifactItems, webModuleElementName);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            else {
                ErrorManager.getDefault().log ("WebProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
            }
        }
    }

    private static class AddLibraryAction extends AbstractAction {

        private final Project project;
        private final String classPathId;
        private final String webModuleElementName;

        public AddLibraryAction (Project project, String classPathId, String webModuleElementName) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddLibrary_Action" ) );
            this.project = project;
            this.classPathId = classPathId;
            this.webModuleElementName = webModuleElementName;
        }

        public void actionPerformed(ActionEvent e) {
            Object[] options = new Object[] {
                new JButton (NbBundle.getMessage (LibrariesNode.class,"LBL_AddLibrary")),
                DialogDescriptor.CANCEL_OPTION
            };
            ((JButton)options[0]).setEnabled(false);
            ((JButton)options[0]).getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (WebClassPathUi.class,"AD_AddLibrary"));
                        
            WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
            String j2eeVersion = wm.getJ2eePlatformVersion();
            LibrariesChooser panel = new LibrariesChooser ((JButton)options[0], j2eeVersion);
            DialogDescriptor desc = new DialogDescriptor(panel,NbBundle.getMessage( LibrariesNode.class, "LBL_CustomizeCompile_Classpath_AddLibrary" ),
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,null,null);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);
            if (desc.getValue() == options[0]) {
                addLibraries (panel.getSelectedLibraries());
            }
            dlg.dispose();
        }

        private void addLibraries (Library[] libraries) {
            WebProjectClassPathExtender cpExtender = (WebProjectClassPathExtender) project.getLookup().lookup(WebProjectClassPathExtender.class);
            if (cpExtender != null) {
                try {
                    cpExtender.addLibraries(classPathId, libraries, webModuleElementName);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            else {
                ErrorManager.getDefault().log ("WebProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
            }
        }
        
    }

    private static class AddFolderAction extends AbstractAction {

        private final Project project;
        private final String classPathId;
        private final String webModuleElementName;

        public AddFolderAction (Project project, String classPathId, String webModuleElementName) {
            super( NbBundle.getMessage( LibrariesNode.class, "LBL_AddFolder_Action" ) );
            this.project = project;
            this.classPathId = classPathId;
            this.webModuleElementName = webModuleElementName;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
            chooser.setMultiSelectionEnabled( true );
            chooser.setDialogTitle( NbBundle.getMessage( LibrariesNode.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
            //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
            chooser.setAcceptAllFileFilterUsed( false );
            FileFilter fileFilter = new SimpleFileFilter (
                    NbBundle.getMessage( WebClassPathUi.class, "LBL_ZipJarFolderFilter" )); // NOI18N
            chooser.setFileFilter(fileFilter);
            File curDir = FoldersListSettings.getDefault().getLastUsedClassPathFolder();
            chooser.setCurrentDirectory (curDir);
            int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() );
            if ( option == JFileChooser.APPROVE_OPTION ) {
                File files[] = chooser.getSelectedFiles();
                addJarFiles( files, fileFilter );
                curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                FoldersListSettings.getDefault().setLastUsedClassPathFolder(curDir);
            }
        }

        private void addJarFiles (File[] files, FileFilter fileFilter) {
            WebProjectClassPathExtender cpExtender = (WebProjectClassPathExtender) project.getLookup().lookup(WebProjectClassPathExtender.class);
            if (cpExtender != null) {
                List fileObjects = new LinkedList();
                for (int i = 0; i < files.length; i++) {
                    if (fileFilter.accept(files[i])) {
                        FileObject fo = FileUtil.toFileObject (files[i]);
                        assert fo != null : files[i];
                        fileObjects.add(fo);
                    }
                }
                try {
                    FileObject[] fileObjectArray = new FileObject[fileObjects.size()];
                    fileObjects.toArray(fileObjectArray);
                    cpExtender.addArchiveFiles(classPathId, fileObjectArray, webModuleElementName);                    
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            else {
                ErrorManager.getDefault().log ("WebProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
            }
        }

    }

    private static class SimpleFileFilter extends FileFilter {

        private String description;


        public SimpleFileFilter (String description) {
            this.description = description;
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;            
            try {
                return FileUtil.isArchiveFile(f.toURI().toURL());
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
                return false;
            }
        }

        public String getDescription() {
            return this.description;
        }
    }
}

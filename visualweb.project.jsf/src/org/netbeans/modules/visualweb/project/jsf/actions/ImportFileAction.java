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


package org.netbeans.modules.visualweb.project.jsf.actions;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;


/**
 * Action appearing in sub menu.. for importing file (java, jsp sources, images... etc.).
 *
 * @author  Peter Zavadsky
 */
public class ImportFileAction implements ProjectActionPerformer {

    /** Extension for .jsp files (including the dot) */
    private static final String JSP_EXT = ".jsp"; //NOI18N
    /** Extension for .jpg files (including the dot) */
    private static final String JPG_EXT = ".jpg"; //NOI18N
    /** Extension for .jpe files (including the dot) */
    private static final String JPE_EXT = ".jpe"; //NOI18N
    /** Extension for .gif files (including the dot) */
    private static final String GIF_EXT = ".gif"; //NOI18N
    /** Extension for .png files (including the dot) */
    private static final String PNG_EXT = ".png"; //NOI18N
    /** Extension for .jpeg files (including the dot) */
    private static final String JPEG_EXT = ".jpeg"; //NOI18N
    /** Extension for .css files (including the dot) */
    private static final String CSS_EXT = ".css"; //NOI18N
    /** Extension for .html files (including the dot) */
    private static final String HTML_EXT = ".html"; //NOI18N
    /** Extension for .java files (including the dot) */
    private static final String XML_EXT = ".xml"; //NOI18N
    /** Extension for .java files (including the dot) */
    private static final String JAVA_EXT = ".java"; //NOI18N
    /** Extension for .jar files (including the dot) */
    private static final String JAR_EXT = ".jar"; //NOI18N
    /** Extension for .properties files (including the dot) */
    private static final String PROPERTIES_EXT = ".properties"; //NOI18N

//    private final static ProjectStateListener projectListener;
//
//    private static Project currentProject;

    /** stores the last current directory of the file chooser */
    private static File currDir;

//    static {
//        projectListener = new ProjectStateAdapter() {
//            public void projectCreated(ProjectStateEvent evt) {}
//            public void projectOpened(ProjectStateEvent evt) {
//                currentProject = evt.getProject();
////                javax.swing.SwingUtilities.invokeLater(new Runnable() {
////                    public void run() {
////                        ImportFileAction.this.setEnabled(true);
////                    }
////                });
//            }
//            public void projectRenamed(ProjectRenameEvent evt) {}
//            public void projectClosing(ProjectStateEvent evt) {
//                currentProject = null;
////                javax.swing.SwingUtilities.invokeLater(new Runnable() {
////                    public void run() {
////                        ImportFileAction.this.setEnabled(false);
////                    }
////                });
//            }
//            public void projectClosed(ProjectStateEvent evt) {
//            }
//        };
//
//        Portfolio.addProjectStateListener(projectListener);
//
//        // PENDING
//        Portfolio portfolio = ProjectManager.getDefault().getPortfolio();
//        if(portfolio != null) {
//            Project[] projects = portfolio.getProjects();
//            if(projects != null && projects.length > 0) {
//                currentProject = projects[0];
//            }
//        }
//    }

    static final int TYPE_JSP        = 0;
    static final int TYPE_IMAGE      = 1;
    static final int TYPE_STYLESHEET = 2;
    static final int TYPE_JAVA       = 3;
    static final int TYPE_OTHER      = 4;

    private final int type;

    /** Creates a new instance of ImportFileAction */
    private ImportFileAction(int type) {
        this.type = type;
    }
    
    static Action createAction(int type) {
//        setEnabled(currentProject != null);
        String displayName;
        if(type == TYPE_JSP) {
            displayName =  NbBundle.getMessage(ImportFileAction.class, "LBL_ImportJspFileAction");
        } else if(type == TYPE_IMAGE) {
            displayName = NbBundle.getMessage(ImportFileAction.class, "LBL_ImportImageFileAction");
        } else if(type == TYPE_STYLESHEET) {
            displayName = NbBundle.getMessage(ImportFileAction.class, "LBL_ImportStylesheetFileAction");
        } else if(type == TYPE_JAVA) {
            displayName = NbBundle.getMessage(ImportFileAction.class, "LBL_ImportJavaFileAction");
        } else if(type == TYPE_OTHER) {
            displayName = NbBundle.getMessage(ImportFileAction.class, "LBL_ImportOtherFileAction");
        } else {
            throw new IllegalArgumentException("Unknown type=" + type);
        }
        
        return MainProjectSensitiveActions.mainProjectSensitiveAction(new ImportFileAction(type), displayName, null);
    }


//    public void actionPerformed(ActionEvent evt) {
//        // TODO
////        System.err.println("Performing File.. action in " + Thread.currentThread());
//        JFileChooser chooser = prepareFileChooser();
//        File file = chooseFile(chooser);
//        if(file == null) {
//            return;
//        }
////        Project proj = currentProject;
//        if(proj == null) {
//            return;
//        }
////        System.err.println("chosen file=" + file);
//        importFile(proj, file);
////        for (int i = 0; i < files.length; i++) {
////            OpenFile.openFile(files[i], -1, null);
////        }
//        currDir = chooser.getCurrentDirectory();
//    }
    

    //////////////////////////////////////////////////////
    // Implements ProjectActionPerformer
    public boolean enable(Project project) {
        return project != null;
    }
    
    public void perform(Project project) {
        // TODO
//        System.err.println("Performing File.. action in " + Thread.currentThread());
        JFileChooser chooser = null;
        File file = null;
        while(true) {
            chooser = prepareFileChooser();
            file = chooseFile(chooser);
            if(file == null) {
                return;
            }
            if(file.exists()) {
                break;
            }
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(ImportFileAction.class, "TXT_FileDoesntExist", file.getName())));
        }
        
        importFile(project, file);
//        for (int i = 0; i < files.length; i++) {
//            OpenFile.openFile(files[i], -1, null);
//        }
        currDir = chooser.getCurrentDirectory();
    }
    //
    /////////////////////////////////////////////////////////

    private JFileChooser prepareFileChooser() {
        JFileChooser chooser = JsfProjectUtils.getJFileChooser(currDir/*resolveInitialDirectory()*/);
//        HelpCtx.setHelpIDString(chooser, getHelpCtx().getHelpID());
        chooser.setDialogTitle(NbBundle.getMessage(ImportFileAction.class, "LBL_DialogTitle"));

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        /* initialize file filters */
        FileFilter currentFilter = chooser.getFileFilter();
        if(type == TYPE_JSP) {
            FileFilter filter = new Filter(new String[] {JSP_EXT},
                NbBundle.getMessage(ImportFileAction.class, "TXT_JspFilter"));
            chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(filter);
        } else if(type == TYPE_IMAGE) {
            FileFilter filter = new Filter(new String[] {JPG_EXT, GIF_EXT, PNG_EXT, JPEG_EXT, JPE_EXT},
                NbBundle.getMessage(ImportFileAction.class, "TXT_ImageFilter"));
            chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(filter);
        } else if(type == TYPE_STYLESHEET) {
            FileFilter filter = new Filter(new String[] {CSS_EXT},
                NbBundle.getMessage(ImportFileAction.class, "TXT_CssFilter"));
            chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(filter);
        } else if(type == TYPE_JAVA) {
            FileFilter filter = new Filter(
                new String[] {JAVA_EXT}, NbBundle.getMessage(ImportFileAction.class, "TXT_JavaFilter"));
            chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(filter);
        } else {
            FileFilter filter = new Filter(
                new String[] {JAR_EXT}, NbBundle.getMessage(ImportFileAction.class, "TXT_JarFilter"));
            chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(currentFilter);
        }

        return chooser;
    }

//    /**
//     * Resolves directory to be set as a current directory for the file chooser.
//     * If the file chooser has already been displayed since the beginning
//     * of the current NetBeans session, the last used current directory
//     * is returned. Otherwise, the root of the first visible valid non-JAR
//     * filesystem, having a non-empty system name, is returned.
//     * <p>
//     * <em>Warning:</em> The returned directory may not exist&nbsp;-
//     * <code>JFileChooser</code> should handle such situations.
//     *
//     * @return  directory to be used as a current directory,
//     *          or <code>null</code> if the resolution failed
//     */
//    private File resolveInitialDirectory() {
//        if (currDir != null) {
//            return currDir;
//        }
//        try {
//            Enumeration enu = Repository.getDefault().getFileSystems();
//            while (enu.hasMoreElements()) {
//                FileSystem fs = (FileSystem) enu.nextElement();
//                if (fs != null && fs.isValid() && fs.isHidden() == false
//                        && fs instanceof JarFileSystem == false
//                        && fs.getSystemName() != null) {
//                    return new File(fs.getSystemName());
//                }
//            }
//        } catch (Exception e) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//        }
//
//        return null;
//    }

    public static File chooseFile(JFileChooser chooser) {
        File file;
        do {
            int selectedOption = chooser.showDialog(
                org.openide.windows.WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(ImportFileAction.class, "LBL_ImportButton"));

            if (selectedOption != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            // In jdk.1.2 is in fact not supported multi selection -> bug.
            // Try to get the first file and open.
            file = chooser.getSelectedFile();

            if(file != null) {
                return file;
            } else {
                // Selected file doesn't exist.
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ImportFileAction.class, "MSG_noFileSelected"),
                    NotifyDescriptor.WARNING_MESSAGE));
            }
        } while (file == null);

        return file;
    }


    /** File chooser filter that filters files by their names' suffixes. */
    private static class Filter extends FileFilter {

        /** suffixes accepted by this filter */
        private String[] extensions;

        /** localized description of this filter */
        private String description;


        /**
         * Creates a new filter that accepts files having specified suffixes.
         * The filter is case-insensitive.
         * <p>
         * The filter does not use file <em>extensions</em> but it just
         * tests whether the file name ends with the specified string.
         * So it is recommended to pass a file name extension including the
         * preceding dot rather than just the extension.
         *
         * @param  extensions  list of accepted suffixes
         * @param  description  name of the filter
         */
        public Filter(String[] extensions, String description) {

            this.extensions = new String[extensions.length];
            for (int i = 0; i < extensions.length; i++) {
                this.extensions[i] = extensions[i].toLowerCase();
            }
            this.description = description;
        }


        /**
         * @return  <code>true</code> if the file's name ends with one of the
         *          strings specified by the constructor or if the file
         *          is a directory, <code>false</code> otherwise
         */
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            for (int i = 0; i < extensions.length; i++) {
                if (file.getName().toLowerCase().endsWith(extensions[i])) {
                    return true;
                }
            }

            return false;
        }

        /** */
        public String getDescription() {
            return description;
        }
    } // End of Filter class.


    /** */
    public static void importFile(final Project project, File file) {
        FileObject fileObject = FileUtil.toFileObject(file);
        if(FileOwnerQuery.getOwner(fileObject) == project) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                NbBundle.getMessage(ImportFileAction.class,
                    "MSG_FileIsInProject",
                    file.getName(),
                    ProjectUtils.getInformation(project).getDisplayName()
                ));
            DialogDisplayer.getDefault().notify(nd);
            return;
        }


        // Based on the type of the file, copy it to the target directory.
        String fileName = file.getName().toLowerCase();
        FileObject targetFolder;
        if(fileName.endsWith(JSP_EXT)
        || fileName.endsWith(HTML_EXT)
        || fileName.endsWith(XML_EXT)) {
            targetFolder = JsfProjectUtils.getDocumentRoot(project);
        } else if(fileName.endsWith(JAVA_EXT)) {
            FileObject beanRoot = JsfProjectUtils.getPageBeanRoot(project);
            // XXX
            String pkg = findJavaPackage(file);
            if(pkg != null && pkg.length() > 0) {
                String path = pkg.replace('.','/'); // NOI18N
                try {
                    targetFolder = FileUtil.createFolder(beanRoot, path);
                } catch(IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    targetFolder = null;
                }
            } else {
                targetFolder = beanRoot;
            }
        } else if(fileName.endsWith(PROPERTIES_EXT)) {
            targetFolder = JsfProjectUtils.getPageBeanRoot(project);
        } else if(fileName.endsWith(CSS_EXT)
        || fileName.endsWith(JPG_EXT)
        || fileName.endsWith(GIF_EXT)
        || fileName.endsWith(PNG_EXT)
        || fileName.endsWith(JPEG_EXT)
        || fileName.endsWith(JPE_EXT)) {
            try {
                targetFolder = JsfProjectUtils.getResourcesDirectory(project);
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                targetFolder = JsfProjectUtils.getDocumentRoot(project);
            }
        } else if(fileName.endsWith(JAR_EXT)) {
            targetFolder = JsfProjectUtils.getPageBeanRoot(project);
        } else {
            targetFolder = JsfProjectUtils.getPageBeanRoot(project);
        }

//        System.err.println("\ntargetFolder=" + targetFolder);
        if(targetFolder != null) {
            FileObject newFileObject = copyFileToFolder(targetFolder, file);
            if(newFileObject != null) {
                JsfProjectUtils.selectResourceInWindow(newFileObject);
//                java.util.Set s = org.netbeans.modules.visualweb.project.ProjectFileSystem.getInstance().getFileSystems();
//                for(java.util.Iterator it = s.iterator(); it.hasNext(); ) {
//                    FileSystem fs = (FileSystem)it.next();
//                    fs.refresh(false);
//                }
//                expandNodeForPath(newFileObject.getPath());
            }
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new NullPointerException("Cannot find import folder for file " + file.getName())); // NOI18N
        }
    }

//    private static FileObject getProjectFolder(Project proj, String path) {
//        // Create the folder path if needed..
//        File f = new File(proj.getAbsolutePath()
//            + File.separator + path.replace('/', File.separatorChar)); // NOI18N
//        if(!f.exists()) {
//            f.mkdirs();
//        }
//
//        FileSystem fs = getProjectFileSystem(proj);
//        return fs == null ? null : fs.findResource(path);
//    }
//
//    private static FileSystem getProjectFileSystem(Project proj) {
//        File root = new File(proj.getAbsolutePath());
//        LocalFileSystem lfs = new LocalFileSystem();
//        try {
////            System.err.println("root fs=" + root);
//            lfs.setRootDirectory(root);
//        } catch(java.beans.PropertyVetoException pve) { // PropertyVeto??
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, pve);
//            return null;
//        } catch(java.io.IOException ioe) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
//            return null;
//        }
//        return lfs;
//    }

    private static FileObject copyFileToFolder(FileObject folder, File file) {
        try {
            DataFolder dataFolder = DataFolder.findFolder(folder);
            DataObject dataSource = DataObject.find(getFileObjectFromFile(file));
            return dataSource.copy(dataFolder).getPrimaryFile();
        } catch(java.io.IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            return null;
        }
    }

    private static FileObject getFileObjectFromFile(File file) {
        File parent = file.getParentFile();
        LocalFileSystem lfs = new LocalFileSystem();
        try {
            lfs.setRootDirectory(parent);
        } catch(java.beans.PropertyVetoException pve) { // ??
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, pve);
            return null;
        } catch(java.io.IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            return null;
        }
        return lfs.findResource(file.getName());
    }

//    private static void expandNodeForPath(final String path) {
//        org.netbeans.modules.visualweb.project.ProjectExplorer pe = org.netbeans.modules.visualweb.project.ProjectExplorer.getInstance();
//        pe.requestActive();
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                findNode(org.netbeans.modules.visualweb.project.ProjectExplorer.getInstance()
//                    .getExplorerManager().getRootContext(), path);
//            }
//        });
//    }
//
//    private static void findNode(org.openide.nodes.Node node, final String path) {
//        DataObject dob = (DataObject)node.getCookie(DataObject.class);
//        if(dob == null) {
//            // Must be DataObject
////            System.err.println("1");
//            return;
//        }
//
//        String p = dob.getPrimaryFile().getPath();
//        if(p != null && p.length() > 0 && path.endsWith(p)) {
//            // TODO Expand the node for the newly created file.. and select it.
//            expandNode(node);
//
//            return;
//        }
//
//        if(node.getCookie(DataFolder.class) == null) {
//            // Must be a folder.
////            System.err.println("2");
//            return;
//        }
//
//        // Force to get the children.
//        Node[] children = node.getChildren().getNodes(true);
//        for(int i = 0; i < children.length; i++) {
//            final Node ch = children[i];
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    findNode(ch, path);
//                }
//            });
//        }
//    }
//
//    private static void expandNode(Node node) {
//        org.netbeans.modules.visualweb.project.ProjectExplorer.getInstance().expandNode(node);
//
//        // Select node
//        selectNode(node);
//    }
//
//    private static void selectNode(Node node) {
//        try {
//            org.netbeans.modules.visualweb.project.ProjectExplorer.getInstance().getExplorerManager()
//                .setSelectedNodes(new Node[] {node});
//        } catch(java.beans.PropertyVetoException pve) { // ??
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, pve);
//        }
//    }
//
//    private static void expandAllNodes(javax.swing.JTree tree,
//    Object node, javax.swing.tree.TreePath tPath) {
//        System.err.println("\nexpanding all nodes for->" + node);
//        System.err.println("getClass->" + node.getClass());
//        tree.expandPath(tPath);
//
//        final org.openide.nodes.Node n = org.openide.explorer.view.Visualizer.findNode(node);
//        System.err.println("real..node=" + n);
//
//        if(n != null) {
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    expandFolderNode(n);
//                }
//            });
//        }
//
////        javax.swing.tree.TreeModel model = tree.getModel();
////
////        // XXX Reload model.
////        System.err.println("model=" + model);
////        if(model instanceof javax.swing.tree.DefaultTreeModel) {
////            System.err.println("reloading model");
////            ((javax.swing.tree.DefaultTreeModel)model).reload();
////        }
////
////        int childCount = model.getChildCount(node);
////        System.err.println("child count = " + childCount);
////        for(int i = 0; i < childCount; i++) {
////            Object ch = model.getChild(node, i);
////            System.err.println("at " + i + " is " + ch);
////            if(ch != null) {
////                java.util.List pathList = new java.util.ArrayList(java.util.Arrays.asList(tPath.getPath()));
////                pathList.add(ch);
////                javax.swing.tree.TreePath chPath = new javax.swing.tree.TreePath(pathList.toArray());
////
////                expandAllNodes(tree, ch, chPath);
////            }
////        }
//    }

//    private static void expandFolderNode(org.openide.nodes.Node n) {
//        if(n.getCookie(org.openide.loaders.DataFolder.class) == null) {
//            // It is not a folder.
//            System.err.println("it is not a folder=" + n);
//            return;
//        }
//
//        System.err.println("children false=" + java.util.Arrays.asList(n.getChildren().getNodes(false)));
//        org.openide.nodes.Node[] nodes = n.getChildren().getNodes(true);
//        System.err.println("children nodes=" + java.util.Arrays.asList(nodes));
//        for(int i = 0; i < nodes.length; i++) {
//            final org.openide.nodes.Node ch = nodes[i];
//            System.err.println("expanding->"+ch);
//           org.netbeans.modules.visualweb.project.ProjectExplorer.getInstance().getTreeView().expandNode(ch);
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    expandFolderNode(ch);
//                }
//            });
//        }
//    }

// XXX Copied from utilities/openfile/../DefaultOpenFileImpl
    /** Name of package keyword. */
    private static final String PACKAGE = "package"; // NOI18N
    /**
     * Find java package in side .java file.
     *
     * @return package or null if not found
     */
    private static String findJavaPackage(File file) {
        String pkg = ""; // NOI18N
        boolean packageKnown = false;

        // Try to find the package name and then infer a directory to mount.
        BufferedReader rd = null;

        try {
            int pckgPos; // found package position

            rd = new BufferedReader(new SourceReader(new FileInputStream(file)));

            // Check for unicode byte watermarks.
            rd.mark(2);
            char[] cbuf = new char[2];
            rd.read(cbuf, 0, 2);

            if (cbuf[0] == 255 && cbuf[1] == 254) {
                rd.close();
                rd = new BufferedReader(new SourceReader(new FileInputStream(file), "Unicode")); // NOI18N
            } else {
                rd.reset();
            }

            while (!packageKnown) {
                String line = rd.readLine();
                if (line == null) {
                    packageKnown = true; // i.e. valid termination of search, default pkg
                    //break;
                    return pkg;
                }

                pckgPos = line.indexOf(PACKAGE);
                if (pckgPos == -1) {
                    continue;
                }
                StringTokenizer tok = new StringTokenizer(line, " \t;"); // NOI18N
                boolean gotPackage = false;
                while (tok.hasMoreTokens()) {
                    String theTok = tok.nextToken ();
                    if (gotPackage) {
                        // Hopefully the package name, but first a sanity check...
                        StringTokenizer ptok = new StringTokenizer(theTok, "."); // NOI18N
                        boolean ok = ptok.hasMoreTokens();
                        while (ptok.hasMoreTokens()) {
                            String component = ptok.nextToken();
                            if (component.length() == 0) {
                                ok = false;
                                break;
                            }
                            if (!Character.isJavaIdentifierStart(component.charAt(0))) {
                                ok = false;
                                break;
                            }
                            for (int pos = 1; pos < component.length(); pos++) {
                                if (!Character.isJavaIdentifierPart(component.charAt(pos))) {
                                    ok = false;
                                    break;
                                }
                            }
                        }
                        if (ok) {
                            pkg = theTok;
                            packageKnown = true;
                            //break;
                            return pkg;
                        } else {
                            // Keep on looking for valid package statement.
                            gotPackage = false;
                            continue;
                        }
                    } else if (theTok.equals (PACKAGE)) {
                        gotPackage = true;
                    } else if (theTok.equals ("{")) { // NOI18N
                        // Most likely we can stop if hit opening brace of class def.
                        // Usually people leave spaces around it.
                        packageKnown = true; // valid end of search, default pkg
                        // break;
                        return pkg;
                    }
                }
            }
        } catch (IOException e1) {
            ErrorManager.getDefault().notify(e1);
        } finally {
            try {
                if (rd != null) {
                    rd.close();
                }
            } catch (IOException e2) {
                ErrorManager.getDefault().notify(e2);
            }
        }

        return null;
    }

    /**
     * Filtered reader for Java sources - it simply excludes
     * comments and some useless whitespaces from the original stream.
     */
    public static class SourceReader extends InputStreamReader {
        private int preRead = -1;
        private boolean inString = false;
        private boolean backslashLast = false;
        private boolean separatorLast = false;
        static private final char separators[] = {'.'}; // dot is enough here...
        static private final char whitespaces[] = {' ', '\t', '\r', '\n'};

        public SourceReader(InputStream in) {
            super(in);
        }

        public SourceReader(InputStream in, String encoding)
        throws UnsupportedEncodingException {
            super(in, encoding);
        }

        /** Reads chars from input reader and filters them. */
        public int read(char[] data, int pos, int len) throws IOException {
            int numRead = 0;
            int c;
            char[] onechar = new char[1];

            while (numRead < len) {
                if (preRead != -1) {
                    c = preRead;
                    preRead = -1;
                } else {
                    c = super.read(onechar, 0, 1);
                    if (c == -1) {   // end of stream reached
                        return (numRead > 0) ? numRead : -1;
                    }
                    c = onechar[0];
                }

                if (c == '/' && !inString) { // a comment could start here
                    preRead = super.read(onechar, 0, 1);
                    if (preRead == 1) {
                        preRead = onechar[0];
                    }
                    if (preRead != '*' && preRead != '/') { // it's not a comment
                        data[pos++] = (char) c;
                        numRead++;
                        if (preRead == -1) {   // end of stream reached
                            return numRead;
                        }
                    } else { // we have run into the comment - skip it
                        if (preRead == '*') { // comment started with /*
                            preRead = -1;
                            do {
                                c = moveToChar('*');
                                if (c == 0) {
                                    c = super.read(onechar, 0, 1);
                                    if (c == 1) {
                                        c = onechar[0];
                                    }
                                    if (c == '*') {
                                        preRead = c;
                                    }
                                }
                            } while (c != '/' && c != -1);
                        } else { // comment started with //
                            preRead = -1;
                            c = moveToChar('\n');
                            if (c == 0) {
                                preRead = '\n';
                            }
                        }
                        if (c == -1) {   // end of stream reached
                            return -1;
                        }
                    }
                } else { // normal valid character
                    if (!inString) { // not inside a string " ... "
                        if (isWhitespace(c)) { // reduce some whitespaces
                            while (true) {
                                preRead = super.read(onechar, 0, 1);
                                if (preRead == -1) {   // end of stream reached
                                    return (numRead > 0) ? numRead : -1;
                                }
                                preRead = onechar[0];

                                if (isSeparator(preRead)) {
                                    c = preRead;
                                    preRead = -1;
                                    break;
                                } else if (!isWhitespace(preRead)) {
                                    if (separatorLast) {
                                        c = preRead;
                                        preRead = -1;
                                    }
                                    break;
                                }
                            }
                        }

                        if (c == '\"' || c == '\'') {
                            inString = true;
                            separatorLast = false;
                        } else {
                            separatorLast = isSeparator(c);
                        }
                    } else { // we are just in a string
                        if (c == '\"' || c == '\'') {
                            if (!backslashLast) {
                                inString = false;
                            } else {
                                backslashLast = false;
                            }
                        } else {
                            backslashLast = (c == '\\');
                        }
                    }

                    data[pos++] = (char) c;
                    numRead++;
                }
            }
            return numRead;
        }

        private int moveToChar(int c) throws IOException {
            int cc;
            char[] onechar = new char[1];

            if (preRead != -1) {
                cc = preRead;
                preRead = -1;
            } else {
                cc = super.read(onechar, 0, 1);
                if (cc == 1) {
                    cc = onechar[0];
                }
            }

            while (cc != -1 && cc != c) {
                cc = super.read(onechar, 0, 1);
                if (cc == 1) {
                    cc = onechar[0];
                }
            }

            return (cc == -1) ? -1 : 0;
        }

        static private boolean isSeparator(int c) {
            for (int i=0; i < separators.length; i++) {
                if (c == separators[i]) {
                    return true;
                }
            }
            return false;
        }

        static private boolean isWhitespace(int c) {
            for (int i=0; i < whitespaces.length; i++) {
                if (c == whitespaces[i]) {
                    return true;
                }
            }
            return false;
        }
    } // End of class SourceReader.
// End of copy.
}

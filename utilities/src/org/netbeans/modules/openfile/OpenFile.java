/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.openfile;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.StyledDocument;

import org.openide.actions.FileSystemAction;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.text.NbDocument;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.RepositoryNodeFactory;
import org.openide.nodes.NodeOperation;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Opens files when requested. Main functionality.
 *
 * @author Jaroslav Tulach, Jesse Glick
 * @author Marian Petras
 */
public class OpenFile extends Object {

    /** extenstion for .java files (including the dot) */
    static final String JAVA_EXT = ".JAVA";                             //NOI18N
    /** extension for .txt files (including the dot) */
    static final String TXT_EXT = ".TXT";                               //NOI18N
    /** extension for .zip files (including the dot) */
    private static final String ZIP_EXT = ".ZIP";                       //NOI18N
    /** extension for .jar files (including the dot) */
    private static final String JAR_EXT = ".JAR";                       //NOI18N
    /** Name of package keyword. */
    private static final String PACKAGE = "package";                    //NOI18N

    /** For debug purposes. */
    private static final ErrorManager em
            = ErrorManager.getDefault().getInstance(
                    "org.netbeans.modules.openfile");                   //NOI18N

    
    /**
     * Open the file either by calling {@link OpenCookie} ({@link ViewCookie}),
     * or by showing it in the Explorer.
     * Uses {@link #find} to figure out what the right file object is.
     *
     * @param fileName file name to open
     */
    public static void open(final String fileName) {
        em.log("OpenFile.open: " + fileName);

        final File f = new File(fileName);
        RequestProcessor.getDefault().post(
                new Runnable() {
                    public void run() {
                        open(f, false, null, -1, -1);
                    }
                },
                10000); //!!! Waiting for IDE initialization
    }

    
    /**
     * Open the file either by calling {@link OpenCookie} ({@link ViewCookie}),
     * or by showing it in the Explorer.
     * Uses {@link #find} to figure out what the right file object is.
     *
     * @param file file to open
     * @param wait whether to wait until requested to return a status
     * @param address address to send reply to, valid only if wait set
     * @param port port to send reply to, valid only if wait set
     * @param line line number to try to open to (starting at zero),
     *             or <code>-1</code> to ignore
     */
    static void open(File file,
                     final boolean wait,
                     InetAddress address,
                     int port,
                     int line) {
        /*
         * Try to canonicalize the file:
         */
        try {
            file = file.getCanonicalFile();
        } catch (IOException exc) {
            // ignore it -- use original File instance
            em.log(exc.getMessage());
        }

        /*
         * If the file doesn't exist or if it is not a plain file, display
         * an error message (in a separate thread) and exit:
         */
        em.log("    file: " + file);
        em.log("    file.exists: " + file.exists());
        em.log("    file.isFile: " + file.isFile());
        if ((file.exists() == false) || (file.isFile() == false)) {
            final String fileName = file.toString();
            new Thread (new Runnable() {
                    public void run() {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                SettingsBeanInfo.getString("MSG_fileNotFound",  //NOI18N
                                                           fileName)));
                    }
                }).start();
            return;
        }

        FileObject fileObject = find(file);

        if (fileObject == null) {
            return;
        }
        try {
            DataObject dataObject = DataObject.find(fileObject);
            
            final EditorCookie editorCookie = (line != -1)
                    ? (EditorCookie) dataObject.getCookie(EditorCookie.class)
                    : null;
            final OpenCookie openCookie = (OpenCookie) dataObject.getCookie(OpenCookie.class);
            final ViewCookie viewCookie = (ViewCookie) dataObject.getCookie(ViewCookie.class);
            
            if (openCookie != null || viewCookie != null || editorCookie != null) {
                StatusDisplayer.getDefault().setStatusText(
                        SettingsBeanInfo.getString(wait ? "MSG_openingAndWaiting"   //NOI18N
                                                        : "MSG_opening",            //NOI18N
                                                   file.toString()));
                
                if (editorCookie != null) {
                    editorCookie.open();
                    StyledDocument doc = editorCookie.openDocument();
                    JEditorPane[] panes = editorCookie.getOpenedPanes();
                    
                    if (panes.length > 0) {
                        panes[0].setCaretPosition(NbDocument.findLineOffset(doc, line));
                    } else {
                        StatusDisplayer.getDefault().setStatusText(
                                SettingsBeanInfo.getString("MSG_couldNotOpenAt"));  //NOI18N
                    }
                } else if (openCookie != null) {
                    openCookie.open();
                } else {
                    viewCookie.view();
                }
                
                StatusDisplayer.getDefault().setStatusText(""); // NOI18N
                
                if (wait) {
                    // Could look for a SaveCookie just to see, but need not.
                    Server.waitFor(dataObject, address, port);
                }
            } else {
                try {
                    // If it's zip/jar file dont do additional things.
                    if (fileObject.getFileSystem() instanceof JarFileSystem) {
                        return;
                    }
                } catch (FileStateInvalidException fse) {
                    org.openide.ErrorManager.getDefault().notify(
                            org.openide.ErrorManager.INFORMATIONAL, fse);
                }
                
                Node node = dataObject.getNodeDelegate();
                
                if (fileObject.isRoot()) {
                    // Try to get the node used in the usual Repository, which
                    // has a non-blank display name and is thus nicer.
                    FileSystem fs = fileObject.getFileSystem();
                    Node reponode = RepositoryNodeFactory.getDefault()
                                    .repository(DataFilter.ALL);
                    Children repokids = reponode.getChildren();
                    Enumeration fsenum = repokids.nodes();
                    
                    while (fsenum.hasMoreElements()) {
                        Node fsnode = (Node) fsenum.nextElement();
                        DataFolder df = (DataFolder) fsnode.getCookie(DataFolder.class);
                        if (df != null && df.getPrimaryFile().getFileSystem().equals(fs)) {
                            node = fsnode;
                            break;
                        }
                    }
                }

                // PENDING Opening in new explorer window was submitted as bug (#8809).
                // Here we check if the data object is default data one, 
                // and try to change it to text one. 
                // 1) We get default data loader,
                // 2) Compare if oyr data object is of deafult data object type,
                // 3) Get its default action
                // 4) If the default action is not FileSystemAction we assume text module
                // is avilable and the default action is Convert to text.
                // 5) Perform the action, find changed data object and open it.
                Enumeration loaders = ((DataLoaderPool) Lookup.getDefault().lookup(DataLoaderPool.class))
                                      .allLoaders();
                DataLoader DDOLoader = null;
                // get last data loader from enumeration which have to be default data loader
                for (; loaders.hasMoreElements(); ) {
                    DDOLoader = (DataLoader)loaders.nextElement();
                }
                boolean opened = false;
                
                if (DDOLoader != null
                        && dataObject.getClass().getName().equals(
                                DDOLoader.getRepresentationClass().getName())) {
                    // Is default data object.
                    Action defaultAction = node.getPreferredAction();
                    
                    if (defaultAction != null
                            && !(defaultAction instanceof FileSystemAction)) {
                        // Now we suppose Convert To Text Action is available.
                        defaultAction.actionPerformed(new ActionEvent(node, 0, null)); 

                        fileObject.refresh();
                        try {
                            DataObject newDataObject = DataObject.find(fileObject);
                            OpenCookie newOpenCookie = (OpenCookie) newDataObject.getCookie(OpenCookie.class);

                            if (newOpenCookie != null) {
                                newOpenCookie.open();
                                opened = true;
                            }
                        } catch (DataObjectNotFoundException dnfe) {
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, dnfe);
                        }
                    }
                }

                if (!opened) {
                    // As last resort, explore the node.
                    NodeOperation.getDefault().explore(node);
                }
                if (wait) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(SettingsBeanInfo.getString(
                                    "MSG_cannotOpenWillClose",          //NOI18N
                                    file)));
                }
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    /**
     * Handles .zip and .jar files. Finds of the jar file system
     * is already mounted, if not mounts it.
     *
     * @return root <code>FileObject</code> of jar file system
     *         or null in case of error
     */ 
    private static FileObject handleZipJar(File file) {
        JarFileSystem jarFileSystem = new JarFileSystem ();
        try {
            jarFileSystem.setJarFile(file);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            
            return null;
        } catch (PropertyVetoException pve) {
            ErrorManager.getDefault().notify(pve);
            
            return null;
        }
        
        Repository repository = Repository.getDefault();
        
        FileSystem existing = repository.findFileSystem(jarFileSystem.getSystemName());
        
        if (existing == null) {
            // have commented out (issue #23223)
//             if (TopManager.getDefault().notify(new NotifyDescriptor.Confirmation (SettingsBeanInfo.getString ("MSG_mountArchiveConfirm", file),
//                 SettingsBeanInfo.getString ("LBL_mountArchiveConfirm"))).equals (NotifyDescriptor.OK_OPTION)) {
                repository.addFileSystem(jarFileSystem);
                existing = jarFileSystem;
//             } else {
//                 return null;
//             }
        }
        
        // The root folder will be displayed in the Explorer:
        return existing.getRoot();
    }

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
     * Searches repository for a <code>FileObject</code> best matching
     * to the specified <code>File</code>.
     * <p>
     * If there is just one <code>FileObject</code> found, it is returned.
     * If there are more <code>FileObject</code>s found (at most one
     * in each filesystem), then one of the following algorithms is used
     * for finding the best matching one:</p>
     * <ul>
     *     <li>if the <code>FileObject</code> is a Java file, we try to find
     *         a file whose path relative to the filesystem (it resides in)
     *         matches the package name declared in the source code</li>
     *     <li>for non-Java files, we choose the file having the shortest
     *         name (relative to the filesystem it resides in)</li>
     * </ul>
     *
     * @param  f  file to find a <code>FileObject</code> for
     * @return  matching <code>FileObject</code>,
     *          or <code>null</code> if none is found
     */
    private static FileObject findInRepository(File f) {
        FileObject[] candidates = FileUtil.fromFile(f);

        /* no FileObject found: */
        if (candidates.length == 0) {
            return null;
        }

        /* one FileObject found: */
        if (candidates.length == 1) {
            return candidates[0];
        }

        /* more FileObject's found - select the best one: */
        if (f.toString().toUpperCase().endsWith(JAVA_EXT)) {
            String wantedPkg = findJavaPackage(f);
            if (wantedPkg == null) {
                return candidates[0];
            }
            for (int i = 0; i < candidates.length; i++) {
                String pkg = candidates[i].isRoot()
                             ? ""                                       //NOI18N
                             : candidates[i].getParent().getPackageName('.');
                if (pkg.equals(wantedPkg)) {
                    return candidates[i];
                }
            }
            return candidates[0];
        } else {
            FileObject best = candidates[0];
            int bestNameLen = best.getPackageName('.').length();
            for (int i = 1; i < candidates.length; i++) {
                int nameLen = candidates[i].getPackageName('.').length();
                if (nameLen < bestNameLen) {
                    best = candidates[i];
                    bestNameLen = nameLen;
                }
            }
            return best;
        }
    }
    
    /**
     * Try to find the file object corresponding to a given file on disk.
     * Can produce a folder, mount directories, etc. as needed.
     *
     * @param f the file on local disk 
     * @return file object or <code>null</code> if not found
     */
    private static synchronized FileObject find(File f) {
        String fileName = f.toString();
        String fileNameUpper = fileName.toUpperCase();
        
        FileObject fObject;

        /* Handle ZIP/JAR files by mounting and displaying: */
        if (fileNameUpper.endsWith(ZIP_EXT) || fileNameUpper.endsWith(JAR_EXT)) {
            return handleZipJar(f);
        }
        
        /* Next see if it is present in an existing file systems: */
        if ((fObject = findInRepository(f)) != null) {
            return fObject;
        }

        /* Handle Java files: */
        if (fileNameUpper.endsWith(JAVA_EXT)) {
            return handleJavaFile(f);
        }
        
        File dirToMount;
        if ((dirToMount = f.getParentFile()) == null) {
            return null;
        }
        LocalFileSystem fs;
        if ((fs = mountDirectory(dirToMount)) == null) {
            return null;
        }
        return fs.findResource(f.getName());
    }

    /** */
    private static FileObject handleJavaFile(File f) {

        /*
         * Not found. For Java files, it is reasonable to mount
         * the package root.
         */
        // packageKnown will only be true if
        // a .java is used and its package declaration
        // indicates a real directory
        boolean packageKnown = false;
        String pkg = findJavaPackage(f);
        String pkgtouse = null;
        if (pkg == null) {
            packageKnown = false;
        } else {
            packageKnown = true;
    
            /*
             * Now try to go through the package name piece by piece
             * and get the right parent directory.
             */
            File dir = f.getParentFile();
            while (!pkg.equals("") && dir != null) {                //NOI18N
                int lastDot = pkg.lastIndexOf('.');
                String pkgName = (lastDot == -1)
                                 ? pkg
                                 : pkg.substring(lastDot + 1);
                String dirName = dir.getName();
                if (dirName.equals(pkgName)
                        && (dir = dir.getParentFile()) != null) {
                    // Worked so far.
                    pkgtouse = pkgtouse == null ? pkgName
                                                : pkgName + '.' + pkgtouse;
                } else {
                    // No dice.
                    packageKnown = false;
                    break;
                }
                pkg = (lastDot == -1)
                      ? ""                                          //NOI18N
                      : pkg.substring(0, lastDot);
            }
        }

        int pkgLevel;
        if (packageKnown) {
            pkgLevel = 0;
            if (pkgtouse != null) {
                int pos = -1;
                do {
                    pos = pkgtouse.indexOf('.', pos + 1);
                    pkgLevel++;
                } while (pos != -1);
            }
        } else {
            pkgLevel = -1;
        }

        /*
         * Ask what to mount (if anything). Prompt appropriately with
         * the possible mount points, as well as the recommended one
         * if there is one (i.e. for valid *.java).
         */
        File[] dirToMountContainer = new File[1];
        String[] mountPackageContainer = new String[1];
        askForMountPoint(f,
                         pkgLevel,
                         dirToMountContainer,
                         mountPackageContainer);
        File dirToMount = dirToMountContainer[0];
        String mountPackage = mountPackageContainer[0];

        if (dirToMount == null) {
            return null;
        }
        LocalFileSystem fs;
        if ((fs = mountDirectory(dirToMount)) == null) {
            return null;
        }
        
        return fs.findResource(
                mountPackage.equals("")                                 //NOI18N
                ? f.getName()
                : mountPackage.replace('.', '/') + '/' + f.getName());
    }

    /**
     * Mounts a local directory to the repository.
     *
     * @param  dirToMount  directory to mount
     * @return  filesystem representing the mounted directory;
     *          or <code>null</code> if mounting failed
     */
    private static LocalFileSystem mountDirectory(File dirToMount) {
        LocalFileSystem fs = new LocalFileSystem();
        try {
            fs.setRootDirectory(dirToMount);
        } catch (PropertyVetoException e3) {
            ErrorManager.getDefault().notify(e3);
            return null;
        } catch (IOException e4) {
            ErrorManager.getDefault().notify(e4);
            return null;
        }
        
        Repository repo = Repository.getDefault();
        if (repo.findFileSystem(fs.getSystemName()) != null) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            NbBundle.getMessage(OpenFile.class,
                                                "MSG_wasAlreadyMounted",//NOI18N
                                                fs.getSystemName())));
            return null;
        }
        repo.addFileSystem(fs);
        return fs;
    }

    /**
     * Ask what dir to mount to access a given file.
     * First may display a dialog asking whether the user wishes to select the default,
     * or edit the package selection.
     *
     * @param f the file which should be accessible
     * @param pkgLevel the suggested depth of the package;
     *                 0 = default, 1 = single component, 2 = foo.bar, etc.;
     *                 -1 if no suggested package
     * @param dirToMount 0th elt will contain the directory to mount
     *                   (null to cancel the mount)
     * @param mountPackage 0th elt will contain the name of the package
     *                     (possibly empty, not null) the file will be in
     */
    private static void askForMountPoint(File f,
                                         int pkgLevel,
                                         final File[] dirToMount,
                                         final String[] mountPackage) {
        final java.util.List /*Vector*/ dirs = new Vector(); // list of mountable dir names; Vector<File>
        final java.util.List /*Vector*/ pkgs = new Vector(); // list of resulting package names; Vector<String>
        String pkg = ""; // NOI18N
        for (File dir = f.getParentFile(); dir != null; dir = dir.getParentFile()) {
            dirs.add(dir);
            pkgs.add(pkg);
            if (!pkg.equals("")) { // NOI18N
                pkg = "." + pkg; // NOI18N
            }
            pkg = dir.getName() + pkg;
        }

        // If no guess, always show full dialog.
        if (pkgLevel != -1) {
            String guessed = (String) pkgs.get(pkgLevel);

            // have commented out (issue #23223)
//             Object yesOption = new JButton (SettingsBeanInfo.getString ("LBL_quickMountYes"));
//             ((JButton)yesOption).getAccessibleContext().setAccessibleDescription(SettingsBeanInfo.getString ("ACS_LBL_quickMountYes"));
//             Object noOption = new JButton (SettingsBeanInfo.getString ("LBL_quickMountNo"));
//             ((JButton)noOption).setMnemonic((SettingsBeanInfo.getString ("LBL_quickMountNo_Mnem")).charAt(0));  
//             ((JButton)noOption).getAccessibleContext().setAccessibleDescription(SettingsBeanInfo.getString ("ACS_LBL_quickMountNo"));
//             Object cancelOption = new JButton(SettingsBeanInfo.getString("LBL_cancelButton"));
//             ((JButton)cancelOption).getAccessibleContext().setAccessibleDescription(SettingsBeanInfo.getString ("ACS_LBL_cancelButton")); 
//             Object result = TopManager.getDefault ().notify (new NotifyDescriptor
//                             ("".equals (guessed) ? // NOI18N
//                              SettingsBeanInfo.getString ("MSG_quickMountDefault", f.getName ()) :
//                              SettingsBeanInfo.getString ("MSG_quickMount", f.getName (), guessed), // message
//                              SettingsBeanInfo.getString ("LBL_quickMountTitle"), // title
//                              NotifyDescriptor.YES_NO_OPTION, // optionType
//                              NotifyDescriptor.QUESTION_MESSAGE, // messageType
//                              new Object[] { yesOption, noOption, cancelOption }, // options
//                              yesOption // initialValue
//                             ));
//             if (result.equals (yesOption)) {
                dirToMount[0] = (File) dirs.get(pkgLevel);
                mountPackage[0] = guessed;
                return;
//             } else if (! result.equals (noOption)) {
//                 // Dialog closed--just stop everything.
//                 return;
//             }
        }

        final PackagePanel panel = new PackagePanel(f, pkgLevel, dirs, pkgs);

        final Dialog[] dialog = new Dialog[1];
        
        final JButton okButton = panel.getOKButton();
        JButton cancelButton = panel.getCancelButton();
        final JList list = panel.getList();
        
        dialog[0] = DialogDisplayer.getDefault().createDialog(
            new DialogDescriptor(
              panel,                   // object
              SettingsBeanInfo.getString("LBL_wizTitle"), // title
              true,                    // modal
              new Object[] {okButton, cancelButton}, // options
              okButton,                // initial
              DialogDescriptor.DEFAULT_ALIGN, // align
              new HelpCtx(OpenFile.class.getName() + ".dialog"), // help // NOI18N
              new ActionListener() { // listener
                  public void actionPerformed(ActionEvent evt) {
                      if (evt.getSource() == okButton) {
                          int idx = list.getSelectedIndex();
                          if (idx != -1) {
                              dirToMount[0] = (File) dirs.get(idx);
                              mountPackage[0] = (String) pkgs.get(idx);
                          } else {
                              System.err.println ("Should not have accepted OK button");
                          }
                      }
                      dialog[0].dispose();
                  }
              }));
        dialog[0].show();
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

}

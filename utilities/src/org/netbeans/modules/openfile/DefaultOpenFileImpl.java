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
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.openfile.cli.Callback;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.RepositoryNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * Opens files when requested. Main functionality.
 *
 * @author Jaroslav Tulach, Jesse Glick
 * @author  Marian Petras
 */
public class DefaultOpenFileImpl implements OpenFileImpl {
    
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

    /** Creates a new instance of OpenFileImpl */
    public DefaultOpenFileImpl() {
    }
    
    /**
     * Sets the specified text into the status line.
     *
     * @param  text  text to be displayed
     */
    protected final void setStatusLine(String text) {
        StatusDisplayer.getDefault().setStatusText(text);
    }
    
    /** Clears the status line. */
    protected final void clearStatusLine() {
        setStatusLine("");                                              //NOI18N
    }
    
    /**
     * Prints a text into the status line that a file is being opened
     * and (optionally) that the Open File Server is waiting
     * for it to be closed.
     *
     * @param  fileName  name of the file
     * @param  waiting  <code>true</code> if the server will wait for the file,
     *                  <code>false</code> if not
     */
    protected void setStatusLineOpening(String fileName, boolean waiting) {
        setStatusLine(NbBundle.getMessage(
                OpenFileImpl.class,
                waiting ? "MSG_openingAndWaiting"                       //NOI18N
                        : "MSG_opening",                                //NOI18N
                fileName));
    }
    
    /**
     * Displays a dialog that the file cannot be open.
     * This method is to be used in cases that the file was open via
     * the Open File Server. The message also informs that
     * the launcher will be notified as if the file
     * was closed immediately.
     *
     * @param  fileName  name of file that could not be opened
     */
    protected void notifyCannotOpen(String fileName) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(OpenFileImpl.class,
                                    "MSG_cannotOpenWillClose",          //NOI18N
                                    fileName)));
    }
    
    /**
     * Returns an explorer node for the specified <code>FileObject</code>.
     *
     * @param  fileObject  <code>FileObject</code> to return a node for
     * @param  dataObject  <code>DataObject</code> representing
     *                     the <code>FileObject</code>
     * @return  node representing the specified <code>FileObject</code>
     */
    private final Node getNodeFor(FileObject fileObject,
                                         DataObject dataObject) {
        Node node = dataObject.getNodeDelegate();
        if (fileObject.isRoot()) {

            /*
             * Root folders have an empty path, hence an empty display name.
             * Since root folders are roots of filesystems (which have non-empty
             * display names) we will try to grab the node representing
             * this FileObject's filesystem and use its display name instead.
             */
            try {
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
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
        return node;
    }
    
    /**
     * Activates the specified cookie, thus opening a file.
     * The file is specified by the cookie, because the cookie was obtained
     * from it. The cookie must be one of <code>EditorCookie</code>
     * <code>OpenCookie</code>, <code>ViewCookie</code>.
     *
     * @param  cookie  cookie to activate
     * @param  cookieClass  type of the cookie - specifies action to activate
     * @param  line  used only by <code>EditorCookie</code>s&nbsp;-
     *               specifies initial line to open the file at
     * @return  <code>true</code> if the cookie was successfully activated,
     *          <code>false</code> if some error occured
     * @exception  java.lang.IllegalArgumentException
     *             if <code>cookieClass</code> is not any of
     *             <code>EditorCookie</code>, <code>OpenCookie</code>,
     *             <code>ViewCookie</code>
     * @exception  java.lang.ClassCastException
     *             if the <code>cookie</code> is not an instance
     *             of the specified cookie class
     */
    protected boolean openByCookie(Node.Cookie cookie,
                                   Class cookieClass,
                                   int line) {
        if (cookieClass == EditorCookie.class) {
            EditorCookie editorCookie = (EditorCookie) cookie;
            editorCookie.open();
            StyledDocument doc;
            try {
                doc = editorCookie.openDocument();
            } catch (IOException ex) {
                String msg = NbBundle.getMessage(
                        OpenFileImpl.class,
                        "MSG_cannotOpenWillClose");                     //NOI18N
                ErrorManager.getDefault().notify(
                        ErrorManager.EXCEPTION,
                        ErrorManager.getDefault().annotate(ex, msg));
                clearStatusLine();
                return false;
            }
            JEditorPane[] panes = editorCookie.getOpenedPanes();

            if (panes != null) {
                //assert panes.length > 0;
                panes[0].setCaretPosition(NbDocument.findLineOffset(doc, line));
            } else {
                setStatusLine(NbBundle.getMessage(
                        OpenFileImpl.class,
                        "MSG_couldNotOpenAt"));                         //NOI18N
            }
        } else if (cookieClass == OpenCookie.class) {
            ((OpenCookie) cookie).open();
        } else if (cookieClass == ViewCookie.class) {
            ((ViewCookie) cookie).view();
        } else {
            throw new IllegalArgumentException();
        }
        return true;
    }
    
    /**
     * Tries to open the specified file, using one of <code>EditorCookie</code>,
     * <code>OpenCookie</code>, <code>ViewCookie</code> (in the same order).
     * If the client of the open file server wants, waits until the file is
     * closed and notifies the client.
     *
     * @param  dataObject  <code>DataObject</code> representing the file
     * @param  line  if <code>EditorCookie</code> is used,
     *               specifies initial line to open the file at
     * @return  <code>true</code> if the file was successfully open,
     *          <code>false</code> otherwise
     */
    private final boolean openByCookie(DataObject dataObject,
                                       int line) {
        Node.Cookie cookie;
        Class cookieClass;
        if ((line != -1 && (cookie = dataObject.getCookie(cookieClass = EditorCookie.class)) != null)
                || (cookie = dataObject.getCookie(cookieClass = OpenCookie.class)) != null
                || (cookie = dataObject.getCookie(cookieClass = ViewCookie.class)) != null) {
            return openByCookie(cookie, cookieClass, line);
        }
        return false;
    }
    
    /**
     * Tries to open the specified file using the default action
     * of a node representing the file. If it fails, simply
     * {@linkplain org.openide.nodes.NodeOperation#explore(Node) explores}
     * the node.
     *
     * @param  fileObject  <code>FileObject</code> representing the file to open
     * @param  dataObject  <code>DataObject</code> representing the file
     */
    private final void openByNode(FileObject fileObject,
                                  DataObject dataObject) {
        Node node = getNodeFor(fileObject, dataObject);

        // PENDING Opening in new explorer window was submitted as bug (#8809).
        // Here we check if the data object is default data one, 
        // and try to change it to text one. 
        // 1) We get default data loader,
        // 2) Compare if oyr data object is of deafult data object type,
        // 3) Get its default action
        // 4) If the default action is not FileSystemAction we assume text module
        // is avilable and the default action is Convert to text.
        // 5) Perform the action, find changed data object and open it.
        boolean opened = false;
        DataLoader defaultLoader;
        if ((defaultLoader = getDefaultLoader()) != null
                && dataObject.getClass().getName().equals(
                        defaultLoader.getRepresentationClassName())) {
            // Is default data object.
            Action defaultAction = node.getPreferredAction();
            if (defaultAction != null
                    && !(defaultAction instanceof FileSystemAction)) {
                // Now we suppose Convert To Text Action is available.
                defaultAction.actionPerformed(new ActionEvent(node, 0, null)); 
                fileObject.refresh();
                try {
                    DataObject newDataObject = DataObject.find(fileObject);
                    Node.Cookie newOpenCookie
                            = newDataObject.getCookie(OpenCookie.class);

                    if (newOpenCookie != null) {
                        ((OpenCookie) newOpenCookie).open();
                        opened = true;
                    }
                } catch (DataObjectNotFoundException dnfe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                                     dnfe);
                }
            }
        }

        if (!opened) {
            // As last resort, explore the node.
            NodeOperation.getDefault().explore(node);
        }
    }
    
    /**
     * Gets the default system <code>DataLoader</code>.
     *
     * @return  default <code>DataLoader</code>
     */
    private final DataLoader getDefaultLoader() {
        DataLoader defaultLoader = null;
        DataLoaderPool loaderPool
            = (DataLoaderPool) Lookup.getDefault().lookup(DataLoaderPool.class);
        
        /* default loader is the last loader in the enumeration of loaders: */
        for (Enumeration loaders = loaderPool.allLoaders();
             loaders.hasMoreElements();
             defaultLoader = (DataLoader) loaders.nextElement());
        return defaultLoader;
    }
    
    /**
     * Opens the <codeFileObject</code> either by calling {@link EditorCookie}
     * (or {@link OpenCookie} or {@link ViewCookie}),
     * or by showing it in the Explorer.
     */
    public boolean open(final FileObject fileObject, int line, Callback.Waiter waiter) {

        String fileName = fileObject.getNameExt();
                  
        /* Find a DataObject for the FileObject: */
        final DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        
        /* Try to grab an editor/open/view cookie and open the object: */
        setStatusLineOpening(fileName, waiter != null);
        boolean success = openByCookie(dataObject, line);
        clearStatusLine();
        if (success) {
            return true;
        }

        /* Quit if the FileObject is within a JarFileSystem: */
        try {
            if (fileObject.getFileSystem() instanceof JarFileSystem) {
                return false;
            }
        } catch (FileStateInvalidException fse) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fse);
            return false;
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // This needs to run in EQ generally.
                openByNode(fileObject, dataObject);
            }
        });
        // XXX if waiter != null, call waiter.done() when the document is closed
        return true;
    }
    
    /**
     * Handles .zip and .jar files. Finds of the jar file system
     * is already mounted, if not mounts it.
     *
     * @return root <code>FileObject</code> of jar file system
     *         or null in case of error
     */ 
    private FileObject handleZipJar(File file) {
        JarFileSystem jarFileSystem = new JarFileSystem();
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
    private String findJavaPackage(File file) {
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
     *         a file whose path (relative to the filesystem it resides in)
     *         matches the package name declared in the source code</li>
     *     <li>for non-Java files, we choose the file having the shortest
     *         name (relative to the filesystem it resides in)</li>
     * </ul>
     *
     * @param  f  file to find a <code>FileObject</code> for
     * @return  matching <code>FileObject</code>,
     *          or <code>null</code> if none is found
     */
    private FileObject findInRepository(File f) {
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
     * Tries to find a <code>FileObject</code> for the specified
     * <code>File</code>. If the repository does not contain any matching
     * <code>FileObject</code>, a directory the specified <code>File</code>
     * pertains to is mounted (as a {@link LocalFileSystem}) and a matching
     * <code>FileObject</code> is created. If there are more matching
     * <code>FileObject</code>s in the repository, one is chosen, using
     * a special algorithm for particular file types (see below).
     * <p>
     * If the specified file has extension <tt>.java</tt>, a special algorithm
     * is used for computation of a mount point (if a filesystem is to be
     * mounted) or for selection among several existing
     * <code>FileObject</code>s.
     * <p>
     * If the specified file has extension <tt>.jar</tt> or <tt>.zip</tt>,
     * a JAR filesystem having the specified file as a root is returned.
     * If it did not exist, it is created (mounted) first.
     *
     * @param  f  existing file
     * @return  <code>FileObject</code> matching the <code>File</code>,
     *          or <code>null</code> if not found
     */
    public synchronized FileObject findFileObject(File f) {
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
    
    /**
     * Allows to do additional operations when a <code>FileObject</code>
     * for the file (to be open) is found. It also allows to interrupt
     * the process of opening the file.
     * <p>
     * The default implementation does nothing and allows to continue
     * with any file (always returns <code>true</code>).
     *
     * @param  fileObject  <code>FileObject</code> found
     *                     by {@link #findFileObject}
     * @return  <code>true</code> if it is OK to continue with the
     *          <code>FileObject</code>, <code>false</code> if not
     */
    protected boolean prepareFileObject(FileObject fileObject) {
        return true;
    }

    /** */
    private FileObject handleJavaFile(File f) {

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
            while (!pkg.equals("") && dir != null) {                    //NOI18N
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
                      ? ""                                              //NOI18N
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
    private LocalFileSystem mountDirectory(File dirToMount) {
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
                            NbBundle.getMessage(OpenFileImpl.class,
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
    private void askForMountPoint(File f,
                                         int pkgLevel,
                                         final File[] dirToMount,
                                         final String[] mountPackage) {
        final List /*Vector*/ dirs = new Vector(); // list of mountable dir names; Vector<File>
        final List /*Vector*/ pkgs = new Vector(); // list of resulting package names; Vector<String>
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
              NbBundle.getMessage(DefaultOpenFileImpl.class, "LBL_wizTitle"), // title
              true,                    // modal
              new Object[] {okButton, cancelButton}, // options
              okButton,                // initial
              DialogDescriptor.DEFAULT_ALIGN, // align
              new HelpCtx(OpenFileImpl.class.getName() + ".dialog"), // help // NOI18N
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

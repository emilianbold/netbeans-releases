/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openfile;

import java.beans.*;
import java.io.*;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.StyledDocument;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Opens files when requested. Main functionality.
*
* @author Jaroslav Tulach, Jesse Glick
*/
class OpenFile extends Object {

    static final String PACKAGE = "package";
    
    /** Open the file either by calling {@link OpenCookie} ({@link ViewCookie}), or by
    * showing it in the Explorer.
    * Uses {@link #find} to figure out what the right file object is.
    * @param f file on local disk
    * @param wait whether to wait until requested to return a status
    * @param addr address to send reply to, if waiting
    * @param port port to send reply to, if waiting
    * @param line line number to try to open to (starting at zero), or <code>-1</code> to ignore
    */
    static void open (File f, final boolean wait, InetAddress addr, int port, int line) {
        FileObject fo = find (f);

        if (fo != null) {
            try {
                DataObject obj = DataObject.find (fo);
                final EditorCookie edit = line != -1 ? (EditorCookie) obj.getCookie (EditorCookie.class) : null;
                final OpenCookie open = (OpenCookie) obj.getCookie (OpenCookie.class);
                final ViewCookie view = (ViewCookie) obj.getCookie (ViewCookie.class);
                if (open != null || view != null || edit != null) {
                    TopManager.getDefault ().setStatusText (SettingsBeanInfo.getString (wait ? "MSG_openingAndWaiting" : "MSG_opening", f.toString ()));
                    if (edit != null) {
                        edit.open ();
                        StyledDocument doc = edit.openDocument ();
                        JEditorPane[] panes = edit.getOpenedPanes ();
                        if (panes.length > 0)
                            panes[0].setCaretPosition (NbDocument.findLineOffset (doc, line));
                        else
                            TopManager.getDefault ().setStatusText (SettingsBeanInfo.getString ("MSG_couldNotOpenAt"));
                    } else if (open != null) {
                        open.open ();
                    } else {
                        view.view ();
                    }
                    TopManager.getDefault ().setStatusText (""); // NOI18N
                    if (wait) {
                        // Could look for a SaveCookie just to see, but need not.
                        Server.waitFor (obj, addr, port);
                    }
                } else {
                    Node n = obj.getNodeDelegate ();
                    if (fo.isRoot ()) {
                        // Try to get the node used in the usual Repository, which
                        // has a non-blank display name and is thus nicer.
                        FileSystem fs = fo.getFileSystem ();
                        Node reponode = TopManager.getDefault ().getPlaces ().nodes ().repository ();
                        Children repokids = reponode.getChildren ();
                        Enumeration fsenum = repokids.nodes ();
                        while (fsenum.hasMoreElements ()) {
                            Node fsnode = (Node) fsenum.nextElement ();
                            DataFolder df = (DataFolder) fsnode.getCookie (DataFolder.class);
                            if (df != null && df.getPrimaryFile ().getFileSystem ().equals (fs)) {
                                n = fsnode;
                                break;
                            }
                        }
                    }
                    TopManager.getDefault ().getNodeOperation ().explore (n);
                    if (wait)
                        TopManager.getDefault ().notify (new NotifyDescriptor.Message (SettingsBeanInfo.getString ("MSG_cannotOpenWillClose", f)));
                }
            } catch (IOException ioe) {
                TopManager.getDefault ().notifyException (ioe);
            }
        }
    }

    /** Try to find the file object corresponding to a given file on disk.
    * Can produce a folder, mount directories, etc. as needed.
    * @param f the file on local disk
    * @return file object or <code>null</code> if not found
    */
    private static synchronized FileObject find (File f) {
        String fileName = f.toString ();
        String fileNameUpper = fileName.toUpperCase ();
        // Handle ZIP/JAR files by mounting and displaying.
        if (fileNameUpper.endsWith (".ZIP") || fileNameUpper.endsWith (".JAR")) { // NOI18N
            JarFileSystem jfs = new JarFileSystem ();
            try {
                jfs.setJarFile (f);
            } catch (IOException e5) {
                TopManager.getDefault ().notifyException (e5);
                return null;
            } catch (PropertyVetoException e6) {
                TopManager.getDefault ().notifyException (e6);
                return null;
            }
            Repository repo2 = TopManager.getDefault ().getRepository ();
            FileSystem exist = repo2.findFileSystem (jfs.getSystemName ());
            if (exist == null) {
                if (TopManager.getDefault ().notify (new NotifyDescriptor.Confirmation (SettingsBeanInfo.getString ("MSG_mountArchiveConfirm", f),
                                                     SettingsBeanInfo.getString ("LBL_mountArchiveConfirm")))
                        .equals (NotifyDescriptor.OK_OPTION)) {
                    repo2.addFileSystem (jfs);
                    exist = jfs;
                } else {
                    return null;
                }
            }
            // The root folder will be displayed in the Explorer:
            return exist.getRoot ();
        }
        // Next see if it is present in an existing LocalFileSystem.
        // enumeration of file systems
        Enumeration en = TopManager.getDefault ().getRepository ().getFileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            if (fs instanceof LocalFileSystem) {
                LocalFileSystem lfs = (LocalFileSystem)fs;
                File root = lfs.getRootDirectory ();
                String rootName = root.toString ().toUpperCase ();
                if (fileNameUpper.startsWith (rootName)) {
                    // the filesystem can contain the file
                    String resource = fileName.substring (rootName.length ()).replace (File.separatorChar, '/');
                    if (resource.startsWith ("/")) // NOI18N
                        resource = resource.substring (1);
                    else if (resource.length () > 0)
                        continue;           // e.g. root = /tmp/foo but file = /tmp/foobar
                    FileObject fo = fs.findResource (resource);
                    if (fo != null) {
                        return fo;
                    } else {
                        // Most likely, file was just created and is not yet in folder cache. Refresh each segment.
                        FileObject currentPoint = fs.getRoot ();
                        StringTokenizer resourceTok = new StringTokenizer (resource, "/"); // NOI18N
                        String currentResource = ""; // NOI18N
                        while (resourceTok.hasMoreTokens ()) {
                            if (currentPoint == null || currentPoint.isData ()) {
                                TopManager.getDefault ().notify (new NotifyDescriptor.Message
                                                                 (MessageFormat.format (NbBundle.getBundle (OpenFile.class).getString("MSG_no_file_in_root_nondir_comp"),
                                                                                        new Object[] { fileName, root })));
                                return null;
                            } else {
                                currentPoint.refresh ();
                                if (currentResource.length () > 0) currentResource += '/';
                                currentResource += resourceTok.nextToken ();
                                currentPoint = fs.findResource (currentResource);
                            }
                        }
                        if (currentPoint != null && currentPoint.isData ()) {
                            return currentPoint;
                        } else {
                            TopManager.getDefault ().notify (new NotifyDescriptor.Message
                                                             (MessageFormat.format (NbBundle.getBundle (OpenFile.class).getString ("MSG_no_file_in_root"),
                                                                                    new Object[] { fileName, root })));
                            return null;
                        }
                    }
                }
            }
        }
        // Not found. For Java files, it is reasonable to mount the package root.
        String pkg = null;
        // packageKnown will only be true if
        // a .java is used and its package decl
        // indicates a real dir
        boolean packageKnown = false;
        if (fileNameUpper.endsWith (".JAVA")) { // NOI18N
            // Try to find the package name and then infer a directory to mount.
            BufferedReader rd = null;
            try {
                int pckgPos; // found package position

                rd = new BufferedReader(new InputStreamReader(new SourceInputStream(new FileInputStream(f))));
                while (!packageKnown) {
                    String line = rd.readLine ();
                    if (line == null) {
                        packageKnown = true; // i.e. valid termination of search, default pkg
                        break;
                    }

                    t(line); // test what line has SourceInputStream produced

                    pckgPos = line.indexOf(PACKAGE);
                    if (pckgPos == -1) continue;

                    StringTokenizer tok = new StringTokenizer (line, " \t;"); // NOI18N
                    boolean gotPackage = false;
                    while (tok.hasMoreTokens ()) {
                        String theTok = tok.nextToken ();
                        if (gotPackage) {
                            // Hopefully the package name, but first a sanity check...
                            StringTokenizer ptok = new StringTokenizer (theTok, "."); // NOI18N
                            boolean ok = ptok.hasMoreTokens ();
                            while (ptok.hasMoreTokens ()) {
                                String component = ptok.nextToken ();
                                if (component.length () == 0) {
                                    ok = false;
                                    break;
                                }
                                if (! Character.isJavaIdentifierStart (component.charAt (0))) {
                                    ok = false;
                                    break;
                                }
                                for (int pos = 1; pos < component.length (); pos++) {
                                    if (! Character.isJavaIdentifierPart (component.charAt (pos))) {
                                        ok = false;
                                        break;
                                    }
                                }
                            }
                            if (ok) {
                                pkg = theTok;
                                packageKnown = true;
                                break;
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
                            break;
                        }
                    }
                }
            } catch (IOException e1) {
                TopManager.getDefault ().notifyException (e1);
            } finally {
                try {
                    if (rd != null) rd.close ();
                } catch (IOException e2) {
                    TopManager.getDefault ().notifyException (e2);
                }
            }
        }
        // Now try to go through the package name piece by piece and get the right parent directory.
        if (pkg == null) {
            pkg = ""; // assume default package // NOI18N
        }
        String prefix = pkg.replace ('.', File.separatorChar);
        File dir = f.getParentFile ();
        String pkgtouse = ""; // NOI18N
        while (! pkg.equals ("") && dir != null) { // NOI18N
            int lastdot = pkg.lastIndexOf ('.');
            String trypkg;
            String trypart;
            if (lastdot == -1) {
                trypkg = ""; // NOI18N
                trypart = pkg;
            } else {
                trypkg = pkg.substring (0, lastdot);
                trypart = pkg.substring (lastdot + 1);
            }
            if (dir.getName ().equals (trypart) && dir.getParentFile () != null) {
                // Worked so far.
                dir = dir.getParentFile ();
                pkg = trypkg;
                if (pkgtouse.equals ("")) // NOI18N
                    pkgtouse = trypart;
                else
                    pkgtouse = trypart + "." + pkgtouse; // NOI18N
            } else {
                // No dice.
                packageKnown = false;
                break;
            }
        }
        // Ask what to mount (if anything). Prompt appropriately with the possible
        // mount points, as well as the recommended one if there is one (i.e. for valid *.java).
        File[] dirToMount = new File[] { null };
        String[] mountPackage = new String[] { null };
        int pkgLevel = 0;
        if (! pkgtouse.equals ("")) { // NOI18N
            int pos = -1;
            do {
                pos = pkgtouse.indexOf ('.', pos + 1);
                pkgLevel++;
            } while (pos != -1);
        }
        if (! packageKnown) pkgLevel = -1;
        askForMountPoint (f, pkgLevel, dirToMount, mountPackage);
        if (dirToMount[0] == null) return null;
        // Mount it.
        LocalFileSystem fs = new LocalFileSystem ();
        try {
            fs.setRootDirectory (dirToMount[0]);
        } catch (PropertyVetoException e3) {
            TopManager.getDefault ().notifyException (e3);
            return null;
        } catch (IOException e4) {
            TopManager.getDefault ().notifyException (e4);
            return null;
        }
        Repository repo = TopManager.getDefault ().getRepository ();
        if (repo.findFileSystem (fs.getSystemName ()) != null) {
            TopManager.getDefault ().notify (new NotifyDescriptor.Message
                                             (MessageFormat.format (NbBundle.getBundle (OpenFile.class).getString ("MSG_wasAlreadyMounted"),
                                                                    new Object[] { fs.getSystemName () })));
            return null;
        }
        repo.addFileSystem (fs);
        return fs.findResource (mountPackage[0].replace ('.', '/') + (mountPackage[0].equals ("") ? "" : "/") + f.getName ()); // NOI18N
    }

    /** Ask what dir to mount to access a given file.
    * First may display a dialog asking whether the user wishes to select the default,
    * or edit the package selection.
    * @param f the file which should be accessible
    * @param pkgLevel the suggested depth of the package; 0 = default, 1 = single component, 2 = foo.bar, etc.; -1 if no suggested package
    * @param dirToMount 0th elt will contain the directory to mount (null to cancel the mount)
    * @param mountPackage 0th elt will contain the name of the package (possibly empty, not null) the file will be in
    */
    private static void askForMountPoint (File f, int pkgLevel, final File[] dirToMount, final String[] mountPackage) {
        final Vector dirs = new Vector (); // list of mountable dir names; Vector<File>
        final Vector pkgs = new Vector (); // list of resulting package names; Vector<String>
        String pkg = ""; // NOI18N
        for (File dir = f.getParentFile (); dir != null; dir = dir.getParentFile ()) {
            dirs.add (dir);
            pkgs.add (pkg);
            if (! pkg.equals ("")) pkg = "." + pkg; // NOI18N
            pkg = dir.getName () + pkg;
        }

        // If no guess, always show full dialog.
        if (pkgLevel != -1) {
            String guessed = (String) pkgs.elementAt (pkgLevel);
            Object yesOption = new JButton (SettingsBeanInfo.getString ("LBL_quickMountYes"));
            Object noOption = new JButton (SettingsBeanInfo.getString ("LBL_quickMountNo"));
            Object cancelOption = new JButton(SettingsBeanInfo.getString("LBL_cancelButton"));
            Object result = TopManager.getDefault ().notify (new NotifyDescriptor
                            ("".equals (guessed) ? // NOI18N
                             SettingsBeanInfo.getString ("MSG_quickMountDefault", f.getName ()) :
                             SettingsBeanInfo.getString ("MSG_quickMount", f.getName (), guessed), // message
                             SettingsBeanInfo.getString ("LBL_quickMountTitle"), // title
                             NotifyDescriptor.YES_NO_OPTION, // optionType
                             NotifyDescriptor.QUESTION_MESSAGE, // messageType
                             new Object[] { yesOption, noOption, cancelOption }, // options
                             yesOption // initialValue
                            ));
            if (result.equals (yesOption)) {
                dirToMount[0] = (File) dirs.elementAt (pkgLevel);
                mountPackage[0] = guessed;
                return;
            } else if (! result.equals (noOption)) {
                // Dialog closed--just stop everything.
                return;
            }
        }

        final JPanel panel = new JPanel ();
        panel.setLayout (new BorderLayout (0, 5));
        panel.setBorder (new javax.swing.border.EmptyBorder (8, 8, 8, 8));

        JTextArea textArea = new JTextArea ();
        textArea.setBackground (Color.lightGray);
        textArea.setFont (new Font ("SansSerif", Font.PLAIN, 11)); // NOI18N
        textArea.setText (SettingsBeanInfo.getString (pkgLevel == -1 ? "TXT_whereMountNoSuggest" : "TXT_whereMountSuggest", f.getName ()));
        textArea.setEditable (false);
        textArea.setLineWrap (true);
        textArea.setWrapStyleWord (true);
        panel.add (textArea, BorderLayout.NORTH);

        final JList list = new JList (pkgs);
        list.setVisibleRowCount (5);
        list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        if (pkgLevel != -1) list.setSelectedIndex (pkgLevel);
        list.setCellRenderer (new ListCellRenderer () {
            private Icon folderIcon = new ImageIcon (OpenFile.class.getResource ("folder.gif")); // NOI18N
            private Icon rootFolderIcon = new ImageIcon (OpenFile.class.getResource ("rootFolder.gif")); // NOI18N
            public Component getListCellRendererComponent (JList lst, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String pkg2 = (String) value;
                JLabel lab = new JLabel ();
                if (pkg2.equals ("")) { // NOI18N
                    lab.setText (SettingsBeanInfo.getString ("LBL_packageWillBeDefault"));
                    lab.setIcon (rootFolderIcon);
                } else {
                    lab.setText (SettingsBeanInfo.getString ("LBL_packageWillBe", pkg2));
                    lab.setIcon (folderIcon);
                }
                if (isSelected) {
                    lab.setBackground (lst.getSelectionBackground ());
                    lab.setForeground (lst.getSelectionForeground ());
                } else {
                    lab.setBackground (lst.getBackground ());
                    lab.setForeground (lst.getForeground ());
                }
                lab.setEnabled (lst.isEnabled ());
                lab.setFont (lst.getFont ());
                lab.setOpaque (true);
                return lab;
            }
        });
        panel.add (new JScrollPane (list), BorderLayout.CENTER);

        // Name of mount point:
        final JLabel label = new JLabel ();
        label.setFont (new Font ("Monospaced", Font.PLAIN, 12)); // NOI18N
        panel.add (label, BorderLayout.SOUTH);
        panel.setPreferredSize (new Dimension (450, 300));

        final JButton okButton = new JButton (SettingsBeanInfo.getString ("LBL_okButton"));
        JButton cancelButton = new JButton (SettingsBeanInfo.getString ("LBL_cancelButton"));

        list.addListSelectionListener (new ListSelectionListener () {
                                           public void valueChanged (ListSelectionEvent ev) {
                                               updateLabelEtcFromList (label, list, dirs, okButton);
                                           }
                                       });
        updateLabelEtcFromList (label, list, dirs, okButton);

        final Dialog[] dialog = new Dialog[1];
        dialog[0] = TopManager.getDefault ().createDialog
                    (new DialogDescriptor
                     (panel,                   // object
                      SettingsBeanInfo.getString ("LBL_wizTitle"), // title
                      true,                    // modal
                      new Object[] { okButton, cancelButton }, // options
                      okButton,                // initial
                      DialogDescriptor.DEFAULT_ALIGN, // align
                      new HelpCtx (OpenFile.class.getName () + ".dialog"), // help // NOI18N
                      new ActionListener () { // listener
                          public void actionPerformed (ActionEvent evt) {
                              if (evt.getSource () == okButton) {
                                  int idx = list.getSelectedIndex ();
                                  if (idx != -1) {
                                      dirToMount[0] = (File) dirs.elementAt (idx);
                                      mountPackage[0] = (String) pkgs.elementAt (idx);
                                  } else {
                                      System.err.println ("Should not have accepted OK button");
                                  }
                              }
                              dialog[0].dispose ();
                          }
                      }));
        dialog[0].show ();

    }

    private static void updateLabelEtcFromList (JLabel label, JList list, Vector dirs, JButton okButton) {
        int idx = list.getSelectedIndex ();
        if (idx == -1) {
            label.setText (" "); // NOI18N
            okButton.setEnabled (false);
        } else {
            File dir = (File) dirs.elementAt (idx);
            label.setText (SettingsBeanInfo.getString ("LBL_dirWillBe", dir.getAbsolutePath ()));
            okButton.setEnabled (true);
        }
    }

    /** Filtered input stream for Java sources - it simply excludes
      * comments and some useless whitespaces from the original stream.
      */
    public static class SourceInputStream extends FilterInputStream
    {
        private int preRead = -1;
        private boolean inString = false;
        private boolean backslashLast = false;
        private boolean separatorLast = false;
        static private final char separators[] = { '.' }; // dot is enough here...
        static private final char whitespaces[] = { ' ', '\t', '\r', '\n' };
        
        public SourceInputStream(InputStream in) {
            super(in);
        }
        
        public int read() throws IOException {
            byte[] data = {-1};
            doRead(data, 0, 1);
            return data[0];               
        }
        
        public int read(byte[] b) throws IOException {
            return doRead(b, 0, b.length);
        }
        
        public int read(byte[] b, int off, int len) throws IOException {
            return doRead(b, off, len);
        }

        /** Read bytes from the input stream and filter them. */
        private int doRead(byte[] data, int pos, int len) throws IOException {
            int numRead = 0,
                c;
            
            while (numRead < len) {
                if (preRead != -1) {
                    c = preRead;
                    preRead = -1;
                }
                else {
                    c = in.read();
                    if (c == -1) // end of stream reached
                        return numRead > 0 ? numRead : -1;
                }
                
                if (c == '/' && !inString) { // a comment could start here
                    preRead = in.read();
                    if (preRead != '*' && preRead != '/') { // it's not a comment
                        data[pos++] = (byte) c;
                        numRead++;
                        if (preRead == -1) // end of stream reached
                            return numRead;
                    }
                    else { // we have run into the comment - skip it
                        if (preRead == '*') { // comment started with /*
                            preRead = -1;
                            do {
                                c = moveToChar('*');
                                if (c == 0) {
                                    c = in.read();
                                    if (c == '*') preRead = c;
                                }
                            } while (c != '/' && c != -1);
                        }
                        else { // comment started with //
                            preRead = -1;
                            c = moveToChar('\n');
                            if (c == 0) preRead = '\n';
                        }
                        if (c == -1) return -1;   // end of stream reached
                    }
                }
                else { // normal valid character
                    if (!inString) { // not inside a string " ... "
                        if (isWhitespace(c)) { // reduce some whitespaces
                            while (true) {
                                preRead = in.read();
                                if (preRead == -1) // end of stream reached
                                    return numRead > 0 ? numRead : -1;

                                if (isSeparator(preRead)) {
                                    c = preRead;
                                    preRead = -1;
                                    break;
                                }
                                else if (!isWhitespace(preRead)) {
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
                        }
                        else separatorLast = isSeparator(c);
                    }
                    else { // we are just in a string
                        if (c == '\"' || c == '\'') {
                            if (!backslashLast)
                                inString = false;
                            else backslashLast = false;
                        }
                        else backslashLast = (c == '\\');
                    }

                    data[pos++] = (byte) c;
                    numRead++;
                }
            }
            return numRead;
        }
        
        private int moveToChar(int c) throws IOException {
            int cc;
            if (preRead != -1) {
                cc = preRead;
                preRead = -1;
             }
             else cc = in.read();
             
             while (cc != -1 && cc != c) {
                 cc = in.read();
             }
             
             return cc == -1 ? -1 : 0;
        }

        static private boolean isSeparator(int c) {
            for (int i=0; i < separators.length; i++) {
                if (c == separators[i]) return true;
            }
            return false;
        }

        static private boolean isWhitespace(int c) {
            for (int i=0; i < whitespaces.length; i++) {
                if (c == whitespaces[i]) return true;
            }
            return false;
        }
    }


    /** For debugging purposes only. */
    static final boolean TRACE = false;
    /** For debugging purposes only. */
    static void t(String str) {
        if (TRACE)
            System.out.println("OpenFile> "+str);
    }
}

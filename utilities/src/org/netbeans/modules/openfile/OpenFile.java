/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
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
        rd = new BufferedReader (new InputStreamReader (new FileInputStream (f)));
      scan:
        while (true) {
          String line = rd.readLine ();
          if (line == null) {
            packageKnown = true; // i.e. valid termination of search, default pkg
            break;
          }
          // Will not handle package statements broken across lines, oh well.
          if (line.indexOf ("package") == -1) continue; // NOI18N
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
                break scan;
              } else {
                // Keep on looking for valid package statement.
                gotPackage = false;
                continue;
              }
            } else if (theTok.equals ("package")) { // NOI18N
              gotPackage = true;
            } else if (theTok.equals ("{")) { // NOI18N
              // Most likely we can stop if hit opening brace of class def.
              // Usually people leave spaces around it.
              packageKnown = true; // valid end of search, default pkg
              break scan;
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
      Object result = TopManager.getDefault ().notify (new NotifyDescriptor
         ("".equals (guessed) ? // NOI18N
            SettingsBeanInfo.getString ("MSG_quickMountDefault", f.getName ()) :
            SettingsBeanInfo.getString ("MSG_quickMount", f.getName (), guessed), // message
          SettingsBeanInfo.getString ("LBL_quickMountTitle"), // title
          NotifyDescriptor.YES_NO_OPTION, // optionType
          NotifyDescriptor.QUESTION_MESSAGE, // messageType
          new Object[] { yesOption, noOption }, // options
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
      public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
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
          lab.setBackground (list.getSelectionBackground ());
          lab.setForeground (list.getSelectionForeground ());
        } else {
          lab.setBackground (list.getBackground ());
          lab.setForeground (list.getForeground ());
        }
        lab.setEnabled (list.isEnabled ());
        lab.setFont (list.getFont ());
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
  
}

/*
 * Log
 *  32   Gandalf   1.31        1/15/00  Jesse Glick     Somewhat nicer 
 *       select-mount-point dialog (icons etc.). Also can close quickie dialog 
 *       to cancel whole open.
 *  31   Gandalf   1.30        1/15/00  Jesse Glick     #5271 - opening multiple
 *       files at once which share a new mount point.
 *  30   Gandalf   1.29        1/13/00  Jesse Glick     NOI18N
 *  29   Gandalf   1.28        1/12/00  Jesse Glick     I18N.
 *  28   Gandalf   1.27        1/7/00   Jesse Glick     -line option for line 
 *       numbers.
 *  27   Gandalf   1.26        1/6/00   Jan Jancura     Icon removed from 
 *       NotifyDesc.
 *  26   Gandalf   1.25        1/4/00   Jesse Glick     Friendlier mount 
 *       dialogs.
 *  25   Gandalf   1.24        11/10/99 Jesse Glick     Fixed race condition in 
 *       mount dialog.
 *  24   Gandalf   1.23        11/2/99  Jesse Glick     Commented out testing 
 *       code.
 *  23   Gandalf   1.22        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  22   Gandalf   1.21        10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  21   Gandalf   1.20        8/27/99  Jesse Glick     Fixed #3628--opening a 
 *       file which was just created on disk can fail.
 *  20   Gandalf   1.19        8/17/99  Jesse Glick     Changed handling of 
 *       return status code to be more immediate and simplified. Fixes #2420 and
 *       #3297.
 *  19   Gandalf   1.18        7/29/99  Ian Formanek    Improved appearance
 *  18   Gandalf   1.17        7/19/99  Jesse Glick     Fixed mount dialog to 
 *       use DialogDescriptor, not WizardDescriptor.
 *  17   Gandalf   1.16        7/10/99  Jesse Glick     Open File module moved 
 *       to core.
 *  16   Gandalf   1.15        7/10/99  Jesse Glick     Tweaks.
 *  15   Gandalf   1.14        7/10/99  Jesse Glick     Mount-point dialog 
 *       works.
 *  14   Gandalf   1.13        7/10/99  Jesse Glick     Changing the mounting 
 *       algorithm.
 *  13   Gandalf   1.12        7/10/99  Jesse Glick     Splitting server from 
 *       opening functionality, etc.
 *  12   Gandalf   1.11        7/10/99  Jesse Glick     Sundry clean-ups (mostly
 *       bundle usage).
 *  11   Gandalf   1.10        6/26/99  Jesse Glick     
 *  10   Gandalf   1.9         6/25/99  Jesse Glick     Installing Open File 
 *       menu item in File menu.
 *  9    Gandalf   1.8         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/25/99  Jesse Glick     Comments.
 *  7    Gandalf   1.6         5/25/99  Jesse Glick     Added -wait.
 *  6    Gandalf   1.5         5/25/99  Jaroslav Tulach Waits for notification 
 *       that the open command succeeded.
 *  5    Gandalf   1.4         5/22/99  Jesse Glick     Licenses.
 *  4    Gandalf   1.3         5/22/99  Jesse Glick     Handling options, and 
 *       doc.
 *  3    Gandalf   1.2         5/22/99  Jesse Glick     Support for opening 
 *       archive files, and also better display for root folders.
 *  2    Gandalf   1.1         5/22/99  Jesse Glick     If Java file does not 
 *       exist in mounted fs, tries to mount it in the correct package.
 *  1    Gandalf   1.0         5/19/99  Jesse Glick     
 * $
 */

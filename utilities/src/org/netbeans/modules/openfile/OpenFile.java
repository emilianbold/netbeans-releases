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

package com.netbeans.examples.modules.openfile;

import java.beans.*;
import java.io.*;
import java.net.InetAddress;
import java.util.*;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.nodes.*;

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
  * @return whether to reply immediately even if should be waiting
  * @exception IOException if the file cannot be found
  */
  static boolean open (File f, boolean wait, InetAddress addr, int port) throws IOException {
    FileObject fo = find (f);
    
    if (fo != null) {
      DataObject obj = DataObject.find (fo);
      OpenCookie open = (OpenCookie) obj.getCookie (OpenCookie.class);
      ViewCookie view = (ViewCookie) obj.getCookie (ViewCookie.class);
      if (open != null || view != null) {
        if (open != null)
          open.open ();
        else
          view.view ();
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
        if (wait) {
            TopManager.getDefault ().notify (new NotifyDescriptor.Message (SettingsBeanInfo.getString ("MSG_cannotOpenWillClose", f)));
            return true;
        }
      }
      return false;
    } else {
      throw new FileNotFoundException (f.toString ());
    }
  }

  /** Try to find the file object corresponding to a given file on disk.
  * Can produce a folder, mount directories, etc. as needed.
  * @param f the file on local disk
  * @return file object or <code>null</code> if not found
  */
  private static FileObject find (File f) {
    String fileName = f.toString ();
    String fileNameUpper = fileName.toUpperCase ();
    // Handle ZIP/JAR files by mounting and displaying.
    if (fileNameUpper.endsWith (".ZIP") || fileNameUpper.endsWith (".JAR")) {
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
        repo2.addFileSystem (jfs);
        exist = jfs;
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
          if (resource.startsWith ("/"))
            resource = resource.substring (1);
          else if (resource.length () > 0)
            continue;           // e.g. root = /tmp/foo but file = /tmp/foobar
          FileObject fo = fs.findResource (resource);
          if (fo != null) {
            return fo;
          } else {
            TopManager.getDefault ().notify (new NotifyDescriptor.Message ("Should have found " + fileName + " in " + root));
            return null;
          }
        }
      }
    }
    // Not found. For Java files, it is reasonable to mount the package root.
    if (fileNameUpper.endsWith (".JAVA")) {
      // Try to find the package name and then infer a directory to mount.
      BufferedReader rd = null;
      String pkg = null;
      try {
        rd = new BufferedReader (new InputStreamReader (new FileInputStream (f)));
      scan:
        while (true) {
          String line = rd.readLine ();
          if (line == null) break;
          // Will not handle package statements broken across lines, oh well.
          if (line.indexOf ("package") == -1) continue;
          StringTokenizer tok = new StringTokenizer (line, " \t;");
          boolean gotPackage = false;
          while (tok.hasMoreTokens ()) {
            String theTok = tok.nextToken ();
            if (gotPackage) {
              // Hopefully the package name, but first a sanity check...
              StringTokenizer ptok = new StringTokenizer (theTok, ".");
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
                break scan;
              } else {
                // Keep on looking for valid package statement.
                gotPackage = false;
                continue;
              }
            } else if (theTok.equals ("package")) {
              gotPackage = true;
            } else if (theTok.equals ("{")) {
              // Most likely we can stop if hit opening brace of class def.
              // Usually people leave spaces around it.
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
      // Now try to go through the package name piece by piece and get the right parent directory.
      if (pkg == null) pkg = ""; // assume default package
      String prefix = pkg.replace ('.', File.separatorChar);
      File dir = f.getParentFile ();
      if (dir != null) {
        String pkgtouse = "";
        while (! pkg.equals ("")) { // [PENDING] check for dir == null
          int lastdot = pkg.lastIndexOf ('.');
          String trypkg;
          String trypart;
          if (lastdot == -1) {
            trypkg = "";
            trypart = pkg;
          } else {
            trypkg = pkg.substring (0, lastdot);
            trypart = pkg.substring (lastdot + 1);
          }
          if (dir.getName ().equals (trypart) && dir.getParentFile () != null) {
            // Worked so far.
            dir = dir.getParentFile ();
            pkg = trypkg;
            if (pkgtouse.equals (""))
              pkgtouse = trypart;
            else
              pkgtouse = trypart + "." + pkgtouse;
          } else {
            // No dice.
            break;
          }
        }
        // Mount it.
        LocalFileSystem fs = new LocalFileSystem ();
        try {
          fs.setRootDirectory (dir);
        } catch (PropertyVetoException e3) {
          TopManager.getDefault ().notifyException (e3);
          return null;
        } catch (IOException e4) {
          TopManager.getDefault ().notifyException (e4);
          return null;
        }
        Repository repo = TopManager.getDefault ().getRepository ();
        if (repo.findFileSystem (fs.getSystemName ()) != null) {
          TopManager.getDefault ().notify (new NotifyDescriptor.Message (fs.getSystemName () + " was already mounted??"));
          return null;
        }
        repo.addFileSystem (fs);
        String basename = f.getName ();
        // [PENDING] handle .JAVA here too
        return fs.find (pkgtouse, basename.substring (0, basename.lastIndexOf (".java")), "java");
      }
    }
    return null;
  }
  
}

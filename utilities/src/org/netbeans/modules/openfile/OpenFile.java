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
import java.net.*;
import java.util.*;

import com.netbeans.ide.*;
import com.netbeans.ide.cookies.OpenCookie;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.filesystems.FileSystem;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.modules.ModuleInstall;
import com.netbeans.ide.nodes.*;

/** Acts as a server to open files when requested.
*
* @author Jaroslav Tulach, Jesse Glick
*/
public class OpenFile extends Object implements ModuleInstall, Runnable {
  /** max length of transferred data */
  private static final int LENGTH = 512;
  /** how long to wait between requests */
  private static final int TIMEOUT = 10000;
  /** true if we should stop due to uninstallation */
  private static boolean stop;
  
  public void installed () {
    restored ();
  }

  public void restored () {
    stop = false;
    new Thread (this, "OpenFile").start ();
  }

  public void uninstalled () {
    stop = true;
  }

  public boolean closing () {
    stop = true;
    return true;
  }
  
  /** Run the server.
  * If the server is stopped from the Control Panel, waits until it is
  * started again (if it is started again).
  * When enabled, calls {@link #server}.
  */
  public void run () {
    final Object wait = new Object ();
    PropertyChangeListener pcl = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent ev) {
        if (Settings.PROP_RUNNING.equals (ev.getPropertyName ())) {
          synchronized (wait) {
            wait.notifyAll ();
          }
        }
      }
    };
    while (true) {
      if (Settings.DEFAULT.isRunning ()) {
        try {
          server ();
        } catch (IOException ex) {
          TopManager.getDefault ().notifyException (ex);
        }
      }
      try {
        Settings.DEFAULT.addPropertyChangeListener (pcl);
        synchronized (wait) {
          wait.wait ();
        }
      } catch (InterruptedException interr) {
      }
      Settings.DEFAULT.removePropertyChangeListener (pcl);
      // now continue again
    }
  }
  
  /** the socket to use */
  private static DatagramSocket s;
  /** set up the socket */
  private static void initSocket () throws IOException {
    s = new DatagramSocket (Settings.DEFAULT.getPort ());
    s.setSoTimeout (TIMEOUT);
  }
  /** Waits on the connection.
  */
  private static void server () throws IOException {
    initSocket ();
    // Make sure the socket is changed if the user changes the port number.
    PropertyChangeListener pcl = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent ev) {
        if (Settings.PROP_PORT.equals (ev.getPropertyName ())) {
          try {
            initSocket ();
          } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
          }
        }
      }
    };
    Settings.DEFAULT.addPropertyChangeListener (pcl);
    DatagramPacket p = new DatagramPacket (new byte[LENGTH], LENGTH);
    try {
      while (!stop && Settings.DEFAULT.isRunning ()) {
        p.setLength (LENGTH);
        try {
          s.receive (p);
        } catch (java.io.InterruptedIOException ex) {
          // go on
          continue;
        }
        // Check access:
        if (Settings.DEFAULT.getAccess () == Settings.ACCESS_LOCAL) {
          if (! p.getAddress ().equals (InetAddress.getLocalHost ())) {
            TopManager.getDefault ().notify (new NotifyDescriptor.Message
                                             ("Rejecting attempted open-file access from host " + p.getAddress ()));
            continue;
          }
        }
        // Try to open the requested file:
        String fileName = new String (p.getData (), p.getOffset (), p.getLength ());
        TopManager.getDefault ().setStatusText ("Opening " + fileName);
        try {
          open (new File (fileName));
        } catch (IOException ex) {
          TopManager.getDefault ().notifyException (ex);
        }
      }
    } finally {
      if (pcl != null) Settings.DEFAULT.removePropertyChangeListener (pcl);
      if (s != null) s.close ();
      s = null;
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
    // First see if it is present in an existing LocalFileSystem.
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
          FileObject fo = fs.findResource (resource);
          if (fo != null) {
            return fo;
          }
        }
      }
    }
    // Not found. For Java files, it is reasonable to mount the package root.
    if (fileName.endsWith (".java")) {
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
        while (! pkg.equals ("")) {
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
        return fs.find (pkgtouse, basename.substring (0, basename.lastIndexOf (".java")), "java");
      }
    }
    // Handle ZIP/JAR files by mounting and displaying.
    if (fileName.endsWith (".zip") || fileName.endsWith (".jar")) {
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
    return null;
  }
  
  /** Open the file either by calling {@link OpenCookie}, or by
  * showing it in the Explorer.
  * Uses {@link #find} to figure out what the right file object is.
  * @param f file on local disk
  * @exception IOException if the file cannot be found
  */
  public static void open (File f) throws IOException {
    FileObject fo = find (f);
    
    if (fo != null) {
      DataObject obj = DataObject.find (fo);      
      OpenCookie open = (OpenCookie)obj.getCookie (OpenCookie.class);
      if (open != null) {
        open.open ();
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
      }
    } else {
      throw new FileNotFoundException (f.toString ());
    }
  }
  
  /** Test run. */
  public static void main (String[] args) throws Exception {
    server ();
  }
}

/*
* Log
*  4    Gandalf   1.3         5/22/99  Jesse Glick     Handling options, and 
*       doc.
*  3    Gandalf   1.2         5/22/99  Jesse Glick     Support for opening 
*       archive files, and also better display for root folders.
*  2    Gandalf   1.1         5/22/99  Jesse Glick     If Java file does not 
*       exist in mounted fs, tries to mount it in the correct package.
*  1    Gandalf   1.0         5/19/99  Jesse Glick     
* $
*/

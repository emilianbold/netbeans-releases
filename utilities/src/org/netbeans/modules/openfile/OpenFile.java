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

import java.beans.PropertyVetoException;
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

/** 
*
* @author Jaroslav Tulach
*/
public class OpenFile extends Object implements ModuleInstall, Runnable {
  /** the port to listen at */
  static final int port = 7318;
  /** max lenght of transfered data */
  private static final int LENGTH = 512;
  /** how long to wait between requests */
  private static final int TIMEOUT = 10000;
  /** true if we should stop */
  private static boolean stop;
  
  /**
   * Called when the module is first installed into the IDE.
   * Should perform whatever setup functions are required.
   * <p>Typically, would do one-off functions, and then also call {@link #restored}.
  */
  public void installed () {
    restored ();
  }

  /**
   * Called when an already-installed module is restored (at IDE startup time).
   * Should perform whatever initializations are required.
   */
  public void restored () {
    stop = false;
    new Thread (this, "OpenFile").start ();
  }

  /**
   * Called when the module is uninstalled (from a running IDE).
   * Should remove whatever functionality from the IDE that it had registered.
  */
  public void uninstalled () {
    stop = true;
  }

  /**
   * Called when the IDE is about to exit.
   * The module may cancel the exit if it is not prepared to be shut down.
  * @return <code>true</code> if it is ok to exit the IDE
  */
  public boolean closing () {
    stop = true;
    return true;
  }
  
  /** Run */
  public void run () {
    try {
      server ();
    } catch (IOException ex) {
      TopManager.getDefault ().notifyException (ex);
    }
  }
  
  /** Waits on the connection.
  */
  private static void server () throws IOException {
    DatagramSocket s = new DatagramSocket (port);
    s.setSoTimeout (TIMEOUT);
    DatagramPacket p = new DatagramPacket (new byte[LENGTH], LENGTH);
    try {
      while (!stop) {
        p.setLength (LENGTH);
        try {
          s.receive (p);
        } catch (java.io.InterruptedIOException ex) {
          // go on
          continue;
        }
        String fileName = new String (p.getData (), p.getOffset (), p.getLength ());
        TopManager.getDefault ().setStatusText ("Opening " + fileName);
        try {
          open (new File (fileName));
        } catch (IOException ex) {
          TopManager.getDefault ().notifyException (ex);
        }
      }
    } finally {
      s.close ();
    }
  }
  
  
  /** Tries to find file object for given file on disk.
  * @param f the file on local disk
  * @return file object or null if not found
  */
  private static FileObject find (File f) {
    String fileName = f.toString ();
    String fileNameUpper = fileName.toUpperCase ();
    
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
    // not found
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
      if (pkg == null) pkg = ""; // assume default package
      String prefix = pkg.replace ('.', File.separatorChar);
      File dir = f.getParentFile ();
      if (dir != null) {
        // XXX get dir name, look for ending in prefix, clip that, mount it hidden, find this file in that fs...
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
      return exist.getRoot ();
    }
    return null;
  }
  
  /** Open the file either by calling open cookie, or by 
  * showing it in explorer.
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
  
  public static void main (String[] args) throws Exception {
    server ();
  }
}

/*
* Log
*  3    Gandalf   1.2         5/22/99  Jesse Glick     Support for opening 
*       archive files, and also better display for root folders.
*  2    Gandalf   1.1         5/22/99  Jesse Glick     If Java file does not 
*       exist in mounted fs, tries to mount it in the correct package.
*  1    Gandalf   1.0         5/19/99  Jesse Glick     
* $
*/

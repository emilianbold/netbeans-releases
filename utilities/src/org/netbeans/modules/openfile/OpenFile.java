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

import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.*;

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
    try {
      DataFolder folder = DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "File");
      DataObject[] oldkids = folder.getChildren ();
      InstanceDataObject inst = InstanceDataObject.create (folder, "OpenFile", OpenFileAction.class);
      DataObject[] newkids = new DataObject[oldkids.length + 1];
      boolean found = false;
      // Try to put it after the open-Explorer action, else at the end of the menu.
      for (int i = 0, j = 0; i < oldkids.length; i++, j++) {
        newkids[j] = oldkids[i];
        if (! found && (i == oldkids.length - 1 || oldkids[i].getName ().equals ("OpenExplorer"))) {
          newkids[++j] = inst;
          found = true;
        }
      }
      folder.setOrder (newkids);
    } catch (IOException e) {
      TopManager.getDefault ().notifyException (e);
    }
    restored ();
  }

  public void restored () {
    stop = false;
    new Thread (this, "OpenFile").start ();
  }

  public void uninstalled () {
    stop = true;
    try {
      DataFolder folder = DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "File");
      if (! InstanceDataObject.remove (folder, "OpenFile", OpenFileAction.class))
        throw new IOException ("Could not remove action");
    } catch (IOException e) {
      TopManager.getDefault ().notifyException (e);
    }
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
        String fileName = new String (p.getData (), p.getOffset () + 1, p.getLength () - 1);
        boolean wait = (p.getData ()[p.getOffset ()] == (byte) 'Y');
        TopManager.getDefault ().setStatusText ("Opening " + fileName + (wait ? " (and waiting)" : ""));
        
        byte res;
        boolean replyAnyway = true;
        try {
          replyAnyway = open (new File (fileName), wait, p.getAddress (), p.getPort ());
          res = (byte) (replyAnyway ? 1 : 0);
        } catch (IOException ex) {
          TopManager.getDefault ().notifyException (ex);
          res = 1;
        }
        
        // send reply (unless we are waiting for file to be saved)
        if (!wait || replyAnyway || res != 0) {
          p.getData ()[0] = res;
          p.setLength (1);
          s.send (p);
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
    // Handle ZIP/JAR files by mounting and displaying.
    if (fileName.endsWith (".zip") || fileName.endsWith (".jar") ||
        fileName.endsWith (".ZIP") || fileName.endsWith (".JAR")) {
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
          FileObject fo = fs.findResource (resource);
          if (fo != null) {
            return fo;
          }
        }
      }
    }
    // Not found. For Java files, it is reasonable to mount the package root.
    if (fileName.endsWith (".java") || fileName.endsWith (".JAVA")) {
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
    return null;
  }
  
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
  public static boolean open (File f, boolean wait, InetAddress addr, int port) throws IOException {
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
          waitFor (obj, addr, port);
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
            TopManager.getDefault ().notify (new NotifyDescriptor.Message ("File cannot be opened, will tell launcher it is closed immediately."));
            return true;
        }
      }
      return false;
    } else {
      throw new FileNotFoundException (f.toString ());
    }
  }

  // Addresses and ports of waiting launchers, based on the DO they wait on.  
  private static final Map addresses = new HashMap (); // Map<DataObject, InetAddress>
  private static final Map ports = new HashMap (); // Map<DataObject, Integer>
  // Listener on all waiting DOs that notices when they are saved (or deleted).
  private static final PropertyChangeListener waitingListener = new PropertyChangeListener () {
    public void propertyChange (PropertyChangeEvent ev) {
      DataObject obj = (DataObject) ev.getSource ();
      if (DataObject.PROP_VALID.equals (ev.getPropertyName ())) {
        // If destroyed, report an error.
        if (! obj.isValid ()) {
          unWait (obj, (byte) 1);
        }
      } else if (DataObject.PROP_MODIFIED.equals (ev.getPropertyName ())) {
        // Don't do anything when it *becomes* modified, only when unmodified.
        if (! obj.isModified ()) {
          unWait (obj, (byte) 0);
        }
      }
    }
    // Notify the launcher that it is done waiting on a DO.
    private void unWait (DataObject obj, byte status) {
      obj.removePropertyChangeListener (waitingListener);
      if (s != null) {
        InetAddress addr = (InetAddress) addresses.remove (obj);
        Integer port = (Integer) ports.remove (obj);
        DatagramPacket p = new DatagramPacket (new byte[] { status }, 1, addr, port.intValue ());
        try {
          s.send (p);
        } catch (IOException e) {
          TopManager.getDefault ().notifyException (e);
        }
      } else {
        TopManager.getDefault ().notify (new NotifyDescriptor.Message (new String[] {
          "File " + obj.getName () + " was saved, but the Open File server was not running.",
          "Manually halt the launcher process."
        }));
      }
    }
  };
  /** Register a callback so that the launcher will be notified when the file is modified & saved.
  * @param obj the object to wait for
  * @param addr the address to send a message back to
  * @param port the port to send a message back to
  */
  public static void waitFor (DataObject obj, InetAddress addr, int port) {
    addresses.put (obj, addr);
    ports.put (obj, new Integer (port));
    obj.addPropertyChangeListener (waitingListener);
  }

  /** Test run. */
  public static void main (String[] args) throws Exception {
    server ();
  }
}

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

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.*;
import java.util.Enumeration;

import com.netbeans.ide.TopManager;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.cookies.OpenCookie;

import com.netbeans.ide.modules.ModuleInstall;

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
        TopManager.getDefault ().getNodeOperation ().explore (obj.getNodeDelegate ());
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
*  1    Gandalf   1.0         5/19/99  Jesse Glick     
* $
*/
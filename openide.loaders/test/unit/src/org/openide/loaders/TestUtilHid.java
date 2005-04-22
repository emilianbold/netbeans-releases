/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.openide.filesystems.*;

/** Copy of TestUtilHid from filesystems tests.
 * @author  rm111737
 */
public class TestUtilHid {
    public static FileSystem createLocalFileSystem(String name, String[] resources) throws IOException {
        File f = File.createTempFile (name, ".tmp");
        f.delete ();
        f = new File (f.getParent (), name);
        f.mkdirs ();
        return createLocalFileSystem (f, resources);
    }

    public static FileSystem createLocalFileSystem(File mountPoint, String[] resources) throws IOException {
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {                        
            File f = new File (mountPoint,resources[i]);
            if (f.isDirectory() || resources[i].endsWith("/")) {
                f.mkdirs();
            }
            else {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (IOException iex) {
                    throw new IOException ("While creating " + resources[i] + " in " + mountPoint.getAbsolutePath() + ": " + iex.toString() + ": " + f.getAbsolutePath() + " with resource list: " + Arrays.asList(resources));
                }
            }
        }
        
        LocalFileSystem lfs = new StatusFileSystem();
        try {
        lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }

    public final static  void destroyLocalFileSystem (String testName) throws IOException {            
    }

    static class StatusFileSystem extends LocalFileSystem {
        Status status = new Status () {
            public String annotateName (String name, java.util.Set files) {
                return name;
            }

            public java.awt.Image annotateIcon (java.awt.Image icon, int iconType, java.util.Set files) {
                return icon;
            }
        };        
        
        public org.openide.filesystems.FileSystem.Status getStatus() {
            return status;
        }
        
    }
}

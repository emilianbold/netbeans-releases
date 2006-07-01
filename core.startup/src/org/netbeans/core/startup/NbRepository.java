/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openide.filesystems.*;

import org.netbeans.core.startup.layers.SessionManager;
import org.openide.util.NbBundle;

/** Default repository.
 */
public final class NbRepository extends Repository {
    /** name of system folder to be located in the USER_DIR and HOME_DIR */
    static final String SYSTEM_FOLDER = "config"; // NOI18N

    /**
     * Create a repository based on the normal system file system.
     */
    public NbRepository () {
        super (createDefaultFileSystem());
        // make sure the factory for nbfs and other protocols is on
        Main.initializeURLFactory ();
    }

    /** Creates defalt file system.
    */
    private static FileSystem createDefaultFileSystem () {
        String systemDir = System.getProperty("system.dir"); // NOI18N
        
        if (systemDir != null) {
            // initiliaze the filesystem for this property 

            try {
                return SessionManager.getDefault().create(new File (systemDir), null, new File[0]);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new InternalError ();
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
                throw new InternalError ();
            }
        }
        
        String homeDir = CLIOptions.getHomeDir ();
        if (homeDir != null) {
            // -----------------------------------------------------------------------------------------------------
            // 1. Initialization and checking of netbeans.home and netbeans.user directories

            File homeDirFile = new File (CLIOptions.getHomeDir ());
            String ud = CLIOptions.getUserDir ();
            File userDirFile = new File (ud);
            if (!homeDirFile.exists ()) {
                System.err.println (NbBundle.getMessage(NbRepository.class, "CTL_Netbeanshome_notexists"));
                doExit (2);
            }
            if (!homeDirFile.isDirectory ()) {
                System.err.println (NbBundle.getMessage(NbRepository.class, "CTL_Netbeanshome1"));
                doExit (3);
            }
            if (!ud.equals("memory")) { // NOI18N
                if (!userDirFile.exists ()) {
                    System.err.println (NbBundle.getMessage(NbRepository.class, "CTL_Netbeanshome2"));
                    doExit (4);
                }
                if (!userDirFile.isDirectory ()) {
                    System.err.println (NbBundle.getMessage(NbRepository.class, "CTL_Netbeanshome3"));
                    doExit (5);
                }
            }
            
            // #27151: may also be additional install dirs
            List<File> extradirs = new ArrayList<File>();
            String nbdirs = System.getProperty("netbeans.dirs");
            if (nbdirs != null) {
                StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
                while (tok.hasMoreTokens()) {
                    File f = new File(tok.nextToken(), SYSTEM_FOLDER);
                    if (f.isDirectory()) {
                        extradirs.add(f);
                    }
                }
            }

            // -----------------------------------------------------------------------------------------------------
            // 7. Initialize FileSystems

            
            // system FS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            {
                Exception exc;
                try {
                    File u = new File (userDirFile, SYSTEM_FOLDER);
                    File h = new File (homeDirFile, SYSTEM_FOLDER);
                    return SessionManager.getDefault().create(u, h, extradirs.toArray(new File[extradirs.size()]));
                } catch (IOException ex) {
                    exc = ex;
                } catch (PropertyVetoException ex) {
                    exc = ex;
                } catch (RuntimeException ex) {
                    exc = ex;
                }

                exc.printStackTrace ();
                Object[] arg = new Object[] {systemDir};
                System.err.println (new java.text.MessageFormat(
                    NbBundle.getMessage(NbRepository.class, "CTL_Cannot_mount_systemfs")
                ).format(arg));
                doExit (3);
            }
        }

        try {
            return SessionManager.getDefault().create(null, null, new File[0]);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new InternalError ();
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
            throw new InternalError ();
        }
    }

    
    //
    // methods that delegate to TM
    //
    
    private static void doExit (int value) {
        org.netbeans.TopSecurityManager.exit(value);
    }
    
}

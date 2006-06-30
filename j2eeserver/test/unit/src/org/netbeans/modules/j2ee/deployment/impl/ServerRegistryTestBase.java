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

package org.netbeans.modules.j2ee.deployment.impl;

import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import java.util.*;
import java.io.*;
import java.util.logging.*;

/**
 *
 * @author Pavel Buzek
 */
public class ServerRegistryTestBase extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
    protected ServerRegistryTestBase (String name) {
        super (name);
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[] {
                Lookups.fixed(new Object[] {"repo"}, new Conv()),
                Lookups.metaInfServices(Lkp.class.getClassLoader()),
                Lookups.singleton(Lkp.class.getClassLoader()),
            });
        }
        private static final class Conv implements InstanceContent.Convertor {
            public Conv() {}
            public Object convert(Object obj) {
                assert obj == "repo";
                try {
                    return new Repo();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            public String displayName(Object obj) {
                return obj.toString();
            }
            public String id(Object obj) {
                return obj.toString();
            }
            public Class type(Object obj) {
                assert obj == "repo";
                return Repository.class;
            }
        }
    }
    private File scratchF;
    
    private void mkdir(String path) {
//        System.out.println ("mkdir:"+path);
        new File(scratchF, path.replace('/', File.separatorChar)).mkdirs();
    }
    protected boolean runInEQ() {
        return true;
    }
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        scratchF = getWorkDir();
        mkdir("system/J2EE/InstalledServers");
        mkdir("system/J2EE/DeploymentPlugins");
        System.setProperty("SYSTEMDIR", new File(scratchF, "system").getAbsolutePath());
        FileObject sfs = Repository.getDefault().getDefaultFileSystem().getRoot();
        assertNotNull("no default FS", sfs);
        FileObject j2eeFolder = sfs.getFileObject("J2EE");
        assertNotNull("have J2EE", j2eeFolder);
//        Enumeration enumDest = sfs.getChildren (true);
//        while (enumDest.hasMoreElements()) {
//            FileObject f = (FileObject) enumDest.nextElement();
//            System.out.println ("    dest file:" + f.getPath ());
//        }
    }
    
     private static final class Repo extends Repository {
        
        public Repo() throws Exception {
            super(mksystem());
        }
        
        private static FileSystem mksystem() throws Exception {
            LocalFileSystem lfs = new LocalFileSystem();
            lfs.setRootDirectory(new File(System.getProperty("SYSTEMDIR")));
            java.net.URL layerFile = Repo.class.getClassLoader().getResource ("org/netbeans/tests/j2eeserver/plugin/layer.xml");
            assert layerFile != null;
            XMLFileSystem layer = new XMLFileSystem (layerFile);
            FileSystem layers [] = new FileSystem [] {lfs, layer};
            MultiFileSystem mfs = new MultiFileSystem (layers);
            return mfs;
        }
        
    }
   
}

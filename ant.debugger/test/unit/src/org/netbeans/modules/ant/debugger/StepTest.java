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

package org.netbeans.modules.ant.debugger;

import java.io.File;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.junit.NbTestCase;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;



/**
 * Tests Ant debuggerstepping actions: step in, step out and step over.
 *
 * @author Jan Jancura
 */
public class StepTest extends NbTestCase {
    
    static {
        System.setProperty ("org.openide.util.Lookup", Lkp.class.getName ());
    }

    private String          sourceRoot = System.getProperty ("debuggerant.dir");

    public StepTest (String s) {
        super (s);
    }
    
    public void testStepOver () throws Exception {
        File file = new File (sourceRoot + "build.xml");
        file = FileUtil.normalizeFile(file);
        DebuggerAntLogger.getDefault ().debugFile (file);
        FileObject fileObject = FileUtil.toFileObject (file);
        ActionUtils.runTarget (
            fileObject, 
            new String[] {"run"},
            null
        );
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            try {
                setLookups(new Lookup[] {
                    Lookups.fixed(new Object[] {
                        new IFL(),
                        Class.forName("org.netbeans.modules.masterfs.MasterURLMapper").newInstance(),
                        new DebuggerAntLogger ()
                    }),
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }
}

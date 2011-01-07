/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.masterfs.filebasedfs.FileUtilTest.TestFileChangeListener;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj;
import org.netbeans.modules.masterfs.providers.ProvidedExtensionsTest;

/**
 * @author Jaroslav Tulach
 */
public class RecursiveListenerOnOffTest extends NbTestCase {
    static {
        System.getProperties().put("org.netbeans.modules.masterfs.watcher.disable", "true");
        MockServices.setServices(ProvidedExtensionsTest.AnnotationProviderImpl.class);
    }

    private final Logger LOG;

    public RecursiveListenerOnOffTest(String name) {
        super(name);
        LOG = Logger.getLogger("TEST." + name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    public void testRecursiveListenerIsOn() throws Exception {
        clearWorkDir();
        
        final File rootF = getWorkDir();
        final File dirF = new File(rootF, "dir");
        File fileF = new File(dirF, "file1");
        File subdirF = new File(dirF, "subdir");
        File subfileF = new File(subdirF, "subfile");
        File subsubdirF = new File(subdirF, "subsubdir");
        File subsubfileF = new File(subsubdirF, "subsubfile");
        subsubdirF.mkdirs();

        TestFileChangeListener fcl = new TestFileChangeListener();
        FileUtil.addRecursiveListener(fcl, dirF);
        
        FileObject fo = FileUtil.toFileObject(subsubdirF);
        assertNotNull("Found", fo);
        assertEquals("It is folder", FolderObj.class, fo.getClass());
        FolderObj obj = (FolderObj)fo;
        
        assertTrue("There is a listener around", obj.hasRecursiveListener());
        
        FileUtil.addRecursiveListener(fcl, subdirF);
        
        assertTrue("There is still a listener around", obj.hasRecursiveListener());
        
        FileUtil.removeRecursiveListener(fcl, dirF);

        assertTrue("Listener still remains around", obj.hasRecursiveListener());
        
        FileUtil.removeRecursiveListener(fcl, subdirF);
        assertFalse("No Listener anymore", obj.hasRecursiveListener());

        LOG.info("OK");
    }
}

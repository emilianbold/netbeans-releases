/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.masterfs.filebasedfs.naming;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NamingFactoryTest extends NbTestCase {
    
    public NamingFactoryTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    

    public void testDontForciblyUnregisterFileName() throws Exception {
        File f = new File(getWorkDir(), "child.txt");
        f.createNewFile();
        
        FileNaming parent = NamingFactory.fromFile(getWorkDir());
        
        FileNaming ch1 = NamingFactory.fromFile(parent, f, true);
        FileNaming ch2 = NamingFactory.fromFile(parent, f, true);
        
        assertSame(
            "Regardless of an attempt to ignore cache, "
            + "we need to return the same object", 
            ch1, ch2
        );
    }
    
    
    public void testInvalidatePrevFolder() throws Exception {
        FileNaming parent = NamingFactory.fromFile(getWorkDir());
        
        File f = new File(getWorkDir(), "child");
        f.mkdir();
        
        FileObject dir = FileUtil.toFileObject(f);
        assertTrue("It is a directory", dir.isFolder());

        f.delete();
        f.createNewFile();
        
        FileNaming middleName = NamingFactory.fromFile(parent, f, true);
        assertFalse("No longer a folder", middleName instanceof FolderName);
        
        FileObject file = FileUtil.toFileObject(f);
        assertTrue("It is a file: " + file + " valid: " + file.isValid(), file.isData());
        
        assertFalse("Old file object is no longer valid", dir.isValid());
        assertTrue("New file object is valid", file.isValid());
        
        f.delete();
        f.mkdir();

        FileNaming newNaming = NamingFactory.fromFile(parent, f, true);
        assertFalse("No longer a file", newNaming.isFile());
        
        FileObject newDir = FileUtil.toFileObject(f);
        assertTrue("It is a dir: " + newDir + " valid: " + newDir.isValid(), newDir.isFolder());
        
        assertFalse("Oldest file object is no longer valid", dir.isValid());
        assertFalse("Middle file object is no longer valid", file.isValid());
        assertTrue("Newest is valid", newDir.isValid());
        
    }
}

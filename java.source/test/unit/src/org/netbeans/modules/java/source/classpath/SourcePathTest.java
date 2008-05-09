/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.classpath;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 *
 * @author Tomas Zezula
 */
public class SourcePathTest extends NbTestCase {
    
    public SourcePathTest (String name) {
        super (name);
    }
    
    
    public void testSourcePath () throws Exception {
        
        final File wd = this.getWorkDir();
        final File cache = new File (wd,"cache");   //NOI18N
        cache.mkdir();
        TestUtilities.setCacheFolder(cache);
        final File r1 = new File (wd,"root1"); //NOI18N
        r1.mkdir();
        final File r2 = new File (wd,"root2"); //NOI18N
        r2.mkdir();
        
        final ClassPath base = ClassPathSupport.createClassPath(new URL[] {
            r1.toURI().toURL(),
            r2.toURI().toURL(),
        });
        
        final ClassPath sp1 = SourcePath.create(base, true);
        assertEquals (base,sp1); 
        
        final ClassPath sp2 = SourcePath.create(base, false);
        assertTrue (sp2.entries().isEmpty());
        
        ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run() throws IOException, InterruptedException {
                ClassIndexManager.getDefault().createUsagesQuery(base.entries().get(0).getURL(), true);
                return null;
            }
        });
        assertEquals(1,sp2.entries().size());
        assertEquals(base.entries().get(0).getURL(), sp2.entries().get(0).getURL());
        ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run() throws IOException, InterruptedException {
                ClassIndexManager.getDefault().createUsagesQuery(base.entries().get(1).getURL(), true);
                return null;
            }
        });
        assertEquals (base,sp2);
        
        final ClassPath sp3 = SourcePath.create(base, false);
        assertEquals (base,sp3);
    }
    
    private static void assertEquals (ClassPath ecp, ClassPath rcp) {
        List<? extends ClassPath.Entry> ee = ecp.entries();
        List<? extends ClassPath.Entry> re = rcp.entries();
        assertEquals(ee.size(), re.size());
        for (int i=0; i<ee.size(); i++) {
            assertEquals(ee.get(i), re.get(i));
        }
    }

}

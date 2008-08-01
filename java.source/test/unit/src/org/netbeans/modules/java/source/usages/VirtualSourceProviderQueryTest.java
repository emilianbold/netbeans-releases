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

package org.netbeans.modules.java.source.usages;

import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.parsing.FileObjects;

/**
 *
 * @author Tomas Zezula
 */
public class VirtualSourceProviderQueryTest extends NbTestCase {
        
    public VirtualSourceProviderQueryTest (final String name) {
        super(name);
    }
    
    @Override
    public void setUp () throws Exception {
        clearWorkDir();
        MockServices.setServices(TestVirtualSourceProvider.class);
    }
    
    public void testVirtualSourceProvider () throws Exception {
        final File root = new File (getWorkDir(),"src");    //NOI18N
        root.mkdir();
        final File[] data = prepareData(root);
        final Iterable<VirtualSourceProviderQuery.Binding> res = VirtualSourceProviderQuery.translate(Arrays.asList(data), root);
        assertEquals(new String[] {"a","b","c","d"}, res);      //NOI18N
    }
    
    private static File[] prepareData (final File root) {
        final File[] result = new File[4];
        result[0] = new File (root, "a.groovy");  //NOI18N
        result[1] = new File (root, "b.groovy");  //NOI18N
        result[2] = new File (root, "c.scala");   //NOI18N
        result[3] = new File (root, "d.scala");   //NOI18N
        return result;        
    }
    
    private static void assertEquals (final String[] expected, Iterable<VirtualSourceProviderQuery.Binding> data) {
        final Set<String> es = new HashSet<String>();
        es.addAll(Arrays.asList(expected));
        for (VirtualSourceProviderQuery.Binding p : data) {
            assertTrue (es.remove(p.virtual.inferBinaryName()));
        }
        assertTrue(es.isEmpty());
    }
    
    public static class TestVirtualSourceProvider implements VirtualSourceProvider {

        public Set<String> getSupportedExtensions() {
            final Set<String> result = new HashSet<String>();
            result.add("groovy");   //NOI18N
            result.add ("scala");   //NOI18N
            return result;
        }

        public boolean index () {
            return true;
        }

        public void translate(Iterable<File> files, File sourceRoot, VirtualSourceProvider.Result r) {
            final Set<String> ext = new HashSet<String>();
            final CharSequence d = "";  //NOI18N
            for (File f : files) {
                ext.add(FileObjects.getExtension(f.getName()));
                String rp = FileObjects.getRelativePath(sourceRoot, f);
                int index = rp.lastIndexOf('.');    //NOI18N
                if (index >= 0) {
                    rp = rp.substring(0, index);
                }
                r.add(f, "", rp, d);
            }
            assertEquals(1, ext.size());
        }
        
    }
    
}

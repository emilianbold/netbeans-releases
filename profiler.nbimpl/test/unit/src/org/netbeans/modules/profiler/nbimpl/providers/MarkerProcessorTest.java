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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.profiler.categorization.api.Category;
import org.netbeans.modules.profiler.categorization.api.CategoryContainer;
import org.netbeans.modules.profiler.categorization.api.definitions.PackageCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.SingleTypeCategoryDefinition;
import org.netbeans.modules.profiler.categorization.api.definitions.SubtypeCategoryDefinition;
import org.netbeans.modules.profiler.nbimpl.BaseProjectTest;
import org.netbeans.modules.profiler.nbimpl.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Bachorik
 */
public class MarkerProcessorTest extends BaseProjectTest {
    private MarkerProcessor instance;
    private Category root;
    
    public MarkerProcessorTest(String name) {
        super(name);
    }
    
//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(suite());
//    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(MarkerProcessorTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        instance = new MarkerProcessor(getProject());
        
        root = new CategoryContainer("a", "a");
    }
    
    public void testSubTypeInclusiveNoOverride() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                      "files.TestFileInputStream.close()V",
                      "files.TestFileInputStream.read([B)I",
                      "files.TestFileInputStream.read()I",
                      "files.TestFileInputStream.read([BII)I",
                      "java.io.FileInputStream.close()V",
                      "java.io.FileInputStream.read([B)I",
                      "java.io.FileInputStream.read([BII)I",
                      "java.io.FileInputStream.read()I"}));
        SubtypeCategoryDefinition sub = new SubtypeCategoryDefinition(root, 
                                            FileInputStream.class.getName(), 
                                            new String[]{
                                                "read", 
                                                "close"
                                            }, 
                                            null
        );
        checkAll(sub, rslt);
    }
    
    public void testSubTypeExclusiveNoOverride() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                      "files.TestFileInputStream.close()V",
                      "files.TestFileInputStream.skip(J)J",
                      "files.TestFileInputStream.available()I",
                      "files.TestFileInputStream.mark(I)V",
                      "files.TestFileInputStream.reset()V",
                      "java.io.FileInputStream.skip(J)J",
                      "java.io.FileInputStream.available()I",
                      "java.io.FileInputStream.close()V",
                      "java.io.InputStream.reset()V",
                      "java.io.InputStream.mark(I)V",
                      
                      
        }));
        SubtypeCategoryDefinition sub = new SubtypeCategoryDefinition(root, 
                                            FileInputStream.class.getName(), 
                                            null, 
                                            new String[]{
                                                "read", 
                                                "flush",
                                                "getFD",
                                                "get",
                                                "getChannel",
                                                "finalize",
                                                "markSupported"
                                            });
        checkAll(sub, rslt);
    }
    
    public void testSubTypeInclusiveOverride() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                      "java.io.FilterInputStream.read([B)I",
                      "java.io.BufferedInputStream.read([BII)I",
                      "java.io.BufferedInputStream.close()V",
                      "java.io.BufferedInputStream.read()I",
                      "streams.TestInputStream.close()V",
                      "streams.TestInputStream.read()I",
                      "streams.TestInputStream.read([B)I",
                      "streams.TestInputStream.read([BII)I"
        }));
        
        SubtypeCategoryDefinition sub = new SubtypeCategoryDefinition(root, 
                                            BufferedInputStream.class.getName(), 
                                            new String[]{
                                                "read", 
                                                "close"}, 
                                            null
        );
        checkAll(sub, rslt);
    }
    
    public void testSubTypeExclusiveOverride() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                      "java.io.BufferedInputStream.read([BII)I",
                      "streams.TestInputStream.read()I",
                      "java.io.BufferedInputStream.read()I",
                      "java.io.BufferedInputStream.reset()V"
                        }));
        SubtypeCategoryDefinition sub = new SubtypeCategoryDefinition(root, BufferedInputStream.class.getName(), null, new String[]{"read", "reset"});
        checkNone(sub, rslt);
    }
    
    public void testSubTypeInclusiveOverride2() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                    "files.TestFileOutputStream.write(I)V",
                    "files.TestFileOutputStream.write([B)V",
                    "files.TestFileOutputStream.write([BII)V",
                    "files.TestFileOutputStream.flush()V",
                    "files.ChildTestOutputStream.write(I)V",
                    "files.ChildTestOutputStream.write([B)V",
                    "files.ChildTestOutputStream.write([BII)V",
                    "files.ChildTestOutputStream.flush()V",
                    "java.io.FileOutputStream.write(I)V",
                    "java.io.FileOutputStream.write([B)V",
                    "java.io.FileOutputStream.write([BII)V",
                    "java.io.OutputStream.flush()V"
                    
        }));

        SubtypeCategoryDefinition sub = new SubtypeCategoryDefinition(root, 
                                            FileOutputStream.class.getName(), 
                                            new String[]{
                                                "write", 
                                                "flush"
                                            }, 
                                            null
        );

        checkAll(sub, rslt);
    }
    
    public void testSubTypeExclusiveOverride2() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                    "files.TestFileOutputStream.write(I)V",
                    "files.ChildTestOutputStream.write(I)V",
                    "java.io.FileOutputStream.write(I)V",
                    "java.io.FileOutputStream.write([B)V",
                    "java.io.FileOutputStream.write([BII)V",
                    "files.ChildTestOutputStream.flush()V"}));

        SubtypeCategoryDefinition sub = new SubtypeCategoryDefinition(root, FileOutputStream.class.getName(), null, new String[]{"write", "flush"});

        checkNone(sub, rslt);
    }
    
    public void testTypeInclusive() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                      "ui.TestUIManager.method()V"}));
        SingleTypeCategoryDefinition sub = new SingleTypeCategoryDefinition(root, "ui.TestUIManager", new String[]{"method", "nonmethod"}, null);
        checkAll(sub, rslt);
    }
    
    public void testTypeExclusive() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                      "ui.TestUIManager.method()V"}));
        SingleTypeCategoryDefinition sub = new SingleTypeCategoryDefinition(root, "ui.TestUIManager", null, new String[]{"method", "nonmethod"});
        checkNone(sub, rslt);
    }
    
    public void testPackageFlat() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                      "files.*"}));
        PackageCategoryDefinition sub = new PackageCategoryDefinition(root, "files", false);
        checkAll(sub, rslt);
    }
    
    public void testPackageRecursive() {
        Set<String> rslt = new HashSet<String>(Arrays.asList(new String[]{
                      "files.**"}));
        PackageCategoryDefinition sub = new PackageCategoryDefinition(root, "files", true);
        checkAll(sub, rslt);
    }
    
    private void checkAll(SubtypeCategoryDefinition def, Set<String> expected) {
        instance.process(def);
        
        checkAll(expected);
    }
    
    private void checkNone(SubtypeCategoryDefinition def, Set<String> expected) {
        instance.process(def);
        
        
        checkNone(expected);
    }
    
    private void checkAll(SingleTypeCategoryDefinition def, Set<String> expected) {
        instance.process(def);
        
        checkAll(expected);
    }
    
    private void checkNone(SingleTypeCategoryDefinition def, Set<String> expected) {
        instance.process(def);
        
        checkNone(expected);
    }
    
    private void checkAll(PackageCategoryDefinition def, Set<String> expected) {
        instance.process(def);
        
        Set<String> sigs = new HashSet<String>();
        for(MarkMapping mm : instance.getMappings()) {
            sigs.add(mm.markMask.getClassName());
        }
        assertEquals(expected, sigs);
    }
    
    private void checkAll(Set<String> expected) {
        Set<String> sigs = new HashSet<String>();
        for(MarkMapping mm : instance.getMappings()) {
            sigs.add(mm.markSig);
        }
        assertEquals(expected, sigs);
    }
    
    private void checkNone(Set<String> expected) {
        Set<String> sigs = new HashSet<String>();
        for(MarkMapping mm : instance.getMappings()) {
            sigs.add(mm.markSig);
        }
        
        Set<String> backup = new HashSet<String>(expected);
        expected.removeAll(sigs);
        if (expected.size() != backup.size()) {
            backup.removeAll(expected);
            fail(format(null, printSet(Collections.EMPTY_SET), printSet(backup)));
        }
    }
    
    private static <T> void assertEquals(Set<T> req, Set<T> actual) {
        assertEquals(req, actual, null);
    }
    
    private static <T> void assertEquals(Set<T> req, Set<T> actual, String message) {
        Set<T> over = new HashSet<T>(actual);
        Set<T> under = new HashSet<T>(req);
        over.removeAll(req);
        under.removeAll(actual);
        
        if (!over.isEmpty() || !under.isEmpty()) {
            fail(format(message, printSet(under), printSet(actual)));
        }
    }
    
    private static <T> String printSet(Set<T> s) {
        StringBuilder sb = new StringBuilder();
        for(T e : s) {
            sb.append(e.toString()).append("\n");
        }
        return sb.toString();
    }
}

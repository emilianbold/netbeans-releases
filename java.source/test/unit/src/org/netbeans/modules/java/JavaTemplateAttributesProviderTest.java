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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Pokorsky
 */
public class JavaTemplateAttributesProviderTest extends NbTestCase {
    
    public JavaTemplateAttributesProviderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        CPP.clear();
        MockServices.setServices(JavaDataLoader.class, CPP.class, JavaTemplateAttributesProvider.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAttributesFor() throws Exception {
        this.clearWorkDir();
        File wd = this.getWorkDir();
        FileObject froot = FileUtil.toFileObject(wd);
        
        FileObject ftarget = FileUtil.createFolder(froot, "pkg");
        CPP.register(ftarget, ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {froot}));
        FileObject ftemplate = FileUtil.createData(ftarget, "EmptyClass.java");
        ftemplate.setAttribute("javax.script.ScriptEngine", "freemarker");
        
        
        DataObject template = DataObject.find(ftemplate);
        DataFolder target = DataFolder.findFolder(ftarget);
        String name = "TargetClass";
        JavaTemplateAttributesProvider instance = new JavaTemplateAttributesProvider();
        Map<String, ? extends Object> result = instance.attributesFor(template, target, name);
        
        assertEquals("pkg", result.get("package"));
    }
    
    public static class CPP implements ClassPathProvider {
        
        private static final Map<FileObject,Map<String,ClassPath>> data = new HashMap<FileObject,Map<String,ClassPath>>();
        
        static void clear () {
            data.clear();
        }
        
        static void register (FileObject fo, String type, ClassPath cp) {
            Map<String,ClassPath> m = data.get (fo);
            if (m == null) {
                m = new HashMap<String,ClassPath>();
                data.put (fo,m);
            }
            m.put (type,cp);
        }
            
        public ClassPath findClassPath(FileObject file, String type) {
            
            Map<String,ClassPath> m = data.get (file);
            if (m == null) {
                return null;
            }
            return m.get (type);
        }        
    }

}

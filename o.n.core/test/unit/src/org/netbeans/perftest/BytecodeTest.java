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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.perftest;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import junit.framework.TestCase;
import junit.framework.*;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.DescendingVisitor;
import com.sun.org.apache.bcel.internal.classfile.EmptyVisitor;
import com.sun.org.apache.bcel.internal.classfile.Field;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.LineNumberTable;
import com.sun.org.apache.bcel.internal.classfile.LocalVariableTable;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.Type;
import java.beans.BeanDescriptor;
import org.netbeans.*;
import org.netbeans.core.startup.ModuleSystem;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author radim
 */
public class BytecodeTest extends NbTestCase {
    
    public BytecodeTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(BytecodeTest.class);
        
        return suite;
    }
    
    /** Verification that classfiles built in production build do not contain 
     * variable information to reduce their size and improve performance.
     * Line table and source info are OK.
     * Likely to fail for custom CVS unless they used -Dbuild.compiler.debuglevel=source,lines
     */
    public void testBytecode() throws Exception {
        JavaClass clz = 
                new ClassParser(Main.class.getResourceAsStream("Main.class"), "Main.class").parse();
        assertNotNull("classfile of Main parsed");
        
        Set<Violation> violations = new HashSet<Violation>();
        MyVisitor v = new MyVisitor();
        new DescendingVisitor(clz,v).visit();
        if (v.foundLocalVarTable()) {
            violations.add(new Violation(Main.class.getName(), "startup classpath", "local var table found"));
        }
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            if (!f.getName().endsWith(".jar"))
                continue;
            
            // list of 3rd party libs
            // perhaps we can strip this debug info from these
            if ("commons-logging-1.0.4.jar".equals(f.getName())
            ||  "servlet-2.2.jar".equals(f.getName())
            ||  "servlet2.5-jsp2.1-api.jar".equals(f.getName())
            ||  "jaxws-tools.jar".equals(f.getName())
            ||  "jaxws-rt.jar".equals(f.getName())
            ||  "jaxb-impl.jar".equals(f.getName())
            ||  "jaxb-api.jar".equals(f.getName())
            ||  "sjsxp.jar".equals(f.getName())
            ||  "resolver-1_1_nb.jar".equals(f.getName())
            ||  "webserver.jar".equals(f.getName())
            ||  "swing-layout-1.0.1.jar".equals(f.getName())
            ||  "jmi.jar".equals(f.getName())
            ||  "persistence-tool-support.jar".equals(f.getName())
            ||  "ini4j.jar".equals(f.getName())
            ||  "svnClientAdapter.jar".equals(f.getName())
            ||  "lucene-core-2.0.0.jar".equals(f.getName())
            ||  "javac-impl.jar".equals(f.getName())
            ||  "java-parser.jar".equals(f.getName())) 
                continue;
            
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    System.out.println("testing entry "+entry);
                    clz = new ClassParser(jar.getInputStream(entry), entry.getName()).parse();
                    assertNotNull("classfile of "+entry.toString()+" parsed");
                    
                    v = new MyVisitor();
                    new DescendingVisitor(clz,v).visit();
                    if (v.foundLocalVarTable()) {
                        violations.add(new Violation(entry.toString(), jar.getName(), "local var table found"));
                    }
                    
                    break;
                }
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Some classes in IDE contain variable table information:\n");
            for (Violation viol: violations) {
                msg.append(viol.entry).append(" in ").append(viol.jarFile).append('\n');
            }
            fail(msg.toString());
        }
        //                    assertTrue (entry.toString()+" should have line number table", v.foundLineNumberTable());
    }
    
    private static class Violation {
        String entry;
        String jarFile;
        String comment;
        Violation(String entry, String jarFile, String comment) {
            this.entry = entry;
            this.jarFile = jarFile;
            this.comment = comment;
        }
    }

    private static class MyVisitor extends EmptyVisitor {
        private boolean localVarTable;
        private boolean lineNumberTable;
        
        public void visitLocalVariableTable(LocalVariableTable obj) {
            localVarTable = true;
        }
        
        public boolean foundLocalVarTable() {
            return localVarTable;
        }

        public void visitLineNumberTable(LineNumberTable obj) {
            lineNumberTable = true;
        }
        
        public boolean foundLineNumberTable() {
            return lineNumberTable;
        }

    }

    private static class BIVisitor extends EmptyVisitor {
        
        private static Type pdType = Type.getType("[Ljava/beans/PropertyDescriptor;");
        private static Type bdType = Type.getType("Ljava/beans/BeanDescriptor;");
        private static Type mdType = Type.getType("[Ljava/beans/MethodDescriptor;");
        private static Type edType = Type.getType("[Ljava/beans/EventSetDescriptor;");
        private boolean hasDescFields;
        private boolean hasStaticMethods;
        
        public void visitField(Field obj) {
            if (obj.isStatic()) {
//                System.out.println("signature "+obj.getSignature());
                Type name = Type.getReturnType(obj.getSignature());
                if (pdType.equals(name) ||
                        bdType.equals(name) ||
                        mdType.equals(name) ||
                        edType.equals(name)) {
                    hasDescFields = true;
                }
            }
        }

        public void visitMethod(Method obj) {
            if (obj.isStatic()) { // && obj.getArgumentTypes().length == 0) {
                String name = obj.getName();
                if ("getBdescriptor".equals(name) ||
                        "getMdescriptor".equals(name) ||
                        "getEdescriptor".equals(name) ||
                        "getPdescriptor".equals(name)) {
                    hasStaticMethods = true;
                }
            }
        }
        
        public boolean foundDescFields() {
            return hasDescFields;
        }

        public boolean foundStaticMethods() {
            return hasStaticMethods;
        }

    }

    /** Scan of BeanInfo classes to check if they held descriptors statically
     */
    public void testBeanInfos() throws Exception {
        JavaClass clz;
        assertNotNull("classfile of Main parsed");
        
        Set<Violation> violations = new HashSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            if (!f.getName().endsWith(".jar"))
                continue;
            
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.getName().endsWith("BeanInfo.class")) {
                    System.out.println("testing entry "+entry);
                    clz = new ClassParser(jar.getInputStream(entry), entry.getName()).parse();
                    assertNotNull("classfile of "+entry.toString()+" parsed");
                    
                    BIVisitor v = new BIVisitor();
                    new DescendingVisitor(clz,v).visit();
                    if (v.foundDescFields()) {
                        violations.add(new Violation(entry.toString(), jar.getName(), " found fields that should be avoided"));
                    }
                    if (v.foundStaticMethods()) {
                        violations.add(new Violation(entry.toString(), jar.getName(), " found methods that should be avoided"));
                    }
                }
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Some BeanInfo classes should be more optimized:\n");
            for (Violation v: violations) {
                msg.append(v.entry).append(" in ").append(v.jarFile).append(v.comment).append('\n');
            }
            fail(msg.toString());
        }
    }
}

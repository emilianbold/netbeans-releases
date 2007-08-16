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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;

/**
 *
 * @author radim
 */
public class BytecodeTest extends NbTestCase {
    
    private Logger LOG;

    public BytecodeTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(BytecodeTest.class);
        
        return suite;
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        
        super.setUp();
    }
    
    // TODO test for ModuleInstall subclasses - they should override at least one important method

    /** Verification that classfiles built in production build do not contain 
     * variable information to reduce their size and improve performance.
     * Line table and source info are OK.
     * Likely to fail for custom CVS unless they used -Dbuild.compiler.debuglevel=source,lines
     */
    public void testBytecode() throws Exception {
        JavaClass clz = 
                new ClassParser(Main.class.getResourceAsStream("Main.class"), "Main.class").parse();
        assertNotNull("classfile of Main parsed", clz);
        
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
            ||  "jaxb-xjc.jar".equals(f.getName())
            ||  "jaxb-api.jar".equals(f.getName())
            ||  "saaj-impl.jar".equals(f.getName())
            ||  "activation.jar".equals(f.getName())
            ||  "streambuffer.jar".equals(f.getName())
            ||  "sjsxp.jar".equals(f.getName())
            ||  "resolver-1_1_nb.jar".equals(f.getName())
            ||  "webserver.jar".equals(f.getName())
            ||  "swing-layout-1.0.2.jar".equals(f.getName())
            ||  "beansbinding-0.5.jar".equals(f.getName())
            ||  f.getName().matches("appframework-.*\\.jar")
//            ||  "jmi.jar".equals(f.getName())
            ||  "persistence-tool-support.jar".equals(f.getName())
            ||  "ini4j.jar".equals(f.getName())
            ||  "svnClientAdapter.jar".equals(f.getName())
            ||  "lucene-core-2.1.0.jar".equals(f.getName())
            ||  "javac-impl.jar".equals(f.getName())
            ||  "java-parser.jar".equals(f.getName())) 
                continue;
            
            // #97282 - profiler
            if (f.getName().contains("profiler")) {
                continue;
            }
            
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    LOG.log(Level.FINE, "testing entry {0}", entry);
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
    
    private static class Violation implements Comparable<Violation> {
        String entry;
        String jarFile;
        String comment;
        Violation(String entry, String jarFile, String comment) {
            this.entry = entry;
            this.jarFile = jarFile;
            this.comment = comment;
        }
    
        public int compareTo(Violation v2) {
            String second = v2.entry + v2.jarFile;
            return (entry +jarFile).compareTo(second);
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

    private static class StaticsVisitor extends EmptyVisitor {
        
        private static Type imageType = Type.getType("Ljava/awt/Image;");
        private static Type image1Type = Type.getType("Ljavax/swing/ImageIcon;");
        private static Type image2Type = Type.getType("Ljavax/swing/Icon;");
        private static Type bType = Type.getType("Ljava/util/ResourceBundle;");
        private static Type b2Type = Type.getType("Lorg/openide/util/NbBundle;");
        
        private boolean hasImageFields;
        private boolean hasPropFields;
        
        @Override public void visitField(Field obj) {
            if (obj.isStatic()) {
//                System.out.println("signature "+obj.getSignature());
                Type name = Type.getReturnType(obj.getSignature());
                if (imageType.equals(name) ||
                        image1Type.equals(name) ||
                        image2Type.equals(name)) {
                    hasImageFields = true;
                }
                if (bType.equals(name) ||
                        b2Type.equals(name)) {
                    hasPropFields = true;
                }
            }
        }

        public boolean foundStaticFields() {
            return hasImageFields | hasPropFields;
        }

        public String fieldTypes() {
            if (hasImageFields) {
                return hasPropFields? "images and bundle resources": "images";
            }
            else {
                return hasPropFields? "bundle resources": "none";
            }
        }
    }

    /** Scan of BeanInfo classes to check if they held descriptors statically
     */
    public void testBeanInfos() throws Exception {
        JavaClass clz;
        
        Set<Violation> violations = new TreeSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            if (!f.getName().endsWith(".jar"))
                continue;
            
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.getName().endsWith("BeanInfo.class")) {
                    LOG.log(Level.FINE, "testing entry {0}", entry);
                    if (entry.getName().endsWith("JXPathBasicBeanInfo.class")) {
                        continue;
                    }
                    
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
    /** Scan of all classes to check if they held statically things like Images or ResourceBundles
     */
    public void testStaticRefs() throws Exception {
        JavaClass clz;
        
        // TODO need to exclude some usages that are justified
        
        Set<Violation> violations = new TreeSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            if (!f.getName().endsWith(".jar"))
                continue;
            
            if (f.getName().endsWith("servlet-2.2.jar") 
                    || f.getName().endsWith("servlet2.5-jsp2.1-api.jar")
                    || f.getName().endsWith("javaee.jar")
                    || f.getName().endsWith("javac-impl.jar")
                    || f.getName().endsWith("jaxb-impl.jar")
                    || f.getName().endsWith("jaxb-xjc.jar")
                    || f.getName().endsWith("saaj-impl.jar")
                    || f.getName().endsWith("jh-2.0_04.jar")
                    || f.getName().endsWith("xerces-2.8.0.jar")
                    || f.getName().endsWith("svnClientAdapter.jar")
                    || f.getName().endsWith("beansbinding-0.5.jar")
                    || f.getName().endsWith("persistence-tool-support.jar")    // issue #96439
                    || f.getName().endsWith("org-netbeans-modules-websvc-core.jar")    // issue #96453
                    || f.getName().endsWith("org-netbeans-modules-websvc-jaxrpc.jar")
                    || f.getName().endsWith("org-netbeans-modules-websvc-design.jar") // issue #99971
                    || f.getName().endsWith("org-netbeans-modules-j2ee-sun-appsrv.jar")    // issue #96439
                    || f.getName().endsWith("org-netbeans-modules-j2ee-sun-appsrv81.jar")
                    || f.getName().endsWith("org-netbeans-modules-j2ee-ejbjarproject.jar")    // issue #96423
                    || f.getName().endsWith("org-netbeans-modules-j2ee-earproject.jar")
                    || f.getName().endsWith("org-netbeans-modules-j2ee-clientproject.jar")
                    || f.getName().endsWith("org-netbeans-modules-j2ee-blueprints.jar")
                    || f.getName().endsWith("org-netbeans-modules-j2ee-archive.jar")
                    || f.getName().endsWith("org-netbeans-modules-j2ee-ddloaders.jar")
                    || f.getName().endsWith("org-netbeans-modules-j2ee-dd.jar")
                    || f.getName().endsWith("org-netbeans-modules-j2ee-api-ejbmodule.jar")
                    || f.getName().endsWith("org-netbeans-modules-web-project.jar")    // issue #96427
                    || f.getName().endsWith("org-netbeans-modules-web-core-syntax.jar")
                    || f.getName().endsWith("org-netbeans-modules-java-source.jar") // issue #96461
                    || f.getName().endsWith("org-netbeans-modules-java-project.jar")
                    || f.getName().endsWith("org-netbeans-modules-java-j2seproject.jar")
                    || f.getName().endsWith("org-netbeans-modules-java-platform.jar")
                    || f.getName().endsWith("org-netbeans-modules-j2ee-sun-ddui.jar")) {    // issue #96422
                continue;
            }
            // #97283 - profiler
            if (f.getName().contains("jfluid")
            || f.getName().contains("profiler")
                    ) {
                continue;
            }
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if ("org/openide/explorer/view/VisualizerNode.class".equals(entry.getName()) // default node icon si OK
                            || "org/openide/awt/JInlineMenu.class".equals(entry.getName()) // empty icon si OK
                            || "org/openide/awt/DynaMenuModel.class".equals(entry.getName()) // empty icon si OK
                            || "org/netbeans/swing/tabcontrol/TabData.class".equals(entry.getName()) // empty icon si OK
                            || "org/openide/explorer/propertysheet/PropertySheet.class".equals(entry.getName())) { // deprecated kept for compat
                        continue;
                    } else if (entry.getName().startsWith("org/netbeans/modules/editor/java/JavaCompletionItem") // #96442
//                            || entry.getName().startsWith("org/netbeans/api/visual") // 99964
                            ) { 
                        continue;
                    }

                    LOG.log(Level.FINE, "testing entry {0}", entry);
                    clz = new ClassParser(jar.getInputStream(entry), entry.getName()).parse();
                    assertNotNull("classfile of "+entry.toString()+" parsed");
                    
                    StaticsVisitor v = new StaticsVisitor();
                    new DescendingVisitor(clz,v).visit();
                    if (v.foundStaticFields()) {
                        violations.add(new Violation(entry.toString(), jar.getName(), " has static fields of type "+v.fieldTypes()));
                    }
                }
            }
        }
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Some classes retain memory permanently (").append(violations.size()).append("):\n");
            for (Violation v: violations) {
                msg.append(v.entry).append(v.comment).append(" (").append(v.jarFile).append(")\n");
            }
            fail(msg.toString());
        }
    }
    
    /** Check that we are not loading classes hungrily. 
     * DataLoader(String) is prefered to avoid loading of DataObject classes.
     */
    public void testDataLoaders() throws Exception {
        Enumeration<DataLoader> loaders = DataLoaderPool.getDefault().allLoaders();
        while (loaders.hasMoreElements()) {
            DataLoader ldr = loaders.nextElement();
            if ("org.netbeans.modules.cnd.loaders.CCDataLoader".equals(ldr.getClass().getName())) { // #97612
                continue;
            }
            
            try { 
                // XXX not enough better is to test that all ctors only call super(String)
                Constructor ctor = ldr.getClass().getDeclaredConstructor(Class.class);
                assertNull(ldr.getClass().getName()+".<init>(String) is better are usualy enough", ctor);
            } catch (NoSuchMethodException ex) {
                // expected path - OK
            }
        }
    }
    
    public void testDuplicateClasses() throws Exception {
        SortedMap<String, List<String>> res2jars = new TreeMap<String, List<String>>();
        
        Set<Violation> violations = new TreeSet<Violation>();
        for (File f: org.netbeans.core.startup.Main.getModuleSystem().getModuleJars()) {
            if (!f.getName().endsWith(".jar"))
                continue;
            
            if (f.getName().endsWith("servlet-2.2.jar") 
                    || f.getName().endsWith("servlet2.5-jsp2.1-api.jar")
                    || f.getName().endsWith("cdc-pp-awt-layout.jar")) // #105314
                continue;
                    
            if (f.getName().endsWith("tsalljlayoutclient601dev.jar") // #105628 
                    || f.getName().endsWith("tsalljlayoutserver601dev.jar") // #105628 
                    || f.getName().endsWith("lucene-core-2.1.0.jar") // #105329 
                    || f.getName().endsWith("batik-mod.jar") // #100892
                    || f.getName().endsWith("batik-all.jar") // #100892
                    || f.getName().endsWith("JGo5.1.jar") // #105319 
                    || f.getName().endsWith("JGoLayout5.1.jar") // #105319 
                    || f.getName().endsWith("JGoInstruments5.1.jar")) // #105319 
                continue;
            
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class")) 
                    continue;
                
                LOG.log(Level.FINE, "testing entry {0}", entry);
                List<String> jars = res2jars.get(name);
                if (jars == null) {
                    jars = new LinkedList<String>();
                    res2jars.put(name, jars);
                }
                if (!jars.contains(jar.getName())) { // avoid errors for multiply loaded JARs
                    jars.add(jar.getName());
                }
            }
        }
        boolean fail = false;
        StringBuilder msg = new StringBuilder("There are some duplicated classes in IDE\n");
        for (Map.Entry<String, List<String>> entry: res2jars.entrySet()) {
            if (entry.getValue().size() > 1) {
                fail = true;
                msg.append(entry.getKey()).append(" is contained in ").
                        append(entry.getValue().size()).append(" files: ").
                        append(entry.getValue().toString()).append('\n');
            }
        }
        if (fail) {
            fail(msg.toString());
        }
    }
}

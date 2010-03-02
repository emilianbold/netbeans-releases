/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source.usages;

import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.classfile.Access;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.AnnotationComponent;
import org.netbeans.modules.classfile.ArrayElementValue;
import org.netbeans.modules.classfile.CPClassInfo;
import org.netbeans.modules.classfile.CPFieldInfo;
import org.netbeans.modules.classfile.CPInterfaceMethodInfo;
import org.netbeans.modules.classfile.CPMethodInfo;
import org.netbeans.modules.classfile.ClassElementValue;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.ConstantPool;
import org.netbeans.modules.classfile.ElementValue;
import org.netbeans.modules.classfile.EnumElementValue;
import org.netbeans.modules.classfile.InvalidClassFormatException;
import org.netbeans.modules.classfile.LocalVariableTableEntry;
import org.netbeans.modules.classfile.LocalVariableTypeTableEntry;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.NestedElementValue;
import org.netbeans.modules.classfile.Variable;
import org.netbeans.modules.classfile.Parameter;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.java.source.util.LMListener;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;




/**
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class BinaryAnalyser {

    public enum Result {
        FINISHED,
        CANCELED,
        CLOSED,
    };

    public static final class Changes {

        static final List<ElementHandle<TypeElement>> NO_CHANGES = Collections.emptyList();

        public final List<ElementHandle<TypeElement>> added;
        public final List<ElementHandle<TypeElement>> removed;
        public final List<ElementHandle<TypeElement>> changed;

        private Changes (final List<ElementHandle<TypeElement>> added, final List<ElementHandle<TypeElement>> removed, final List<ElementHandle<TypeElement>> changed) {
            this.added = added;
            this.removed = removed;
            this.changed = changed;
        }

    }

    private static final Logger LOGGER = Logger.getLogger(BinaryAnalyser.class.getName());
    static final String OBJECT = Object.class.getName();

    private static boolean FULL_INDEX = Boolean.getBoolean("org.netbeans.modules.java.source.usages.BinaryAnalyser.fullIndex");     //NOI18N

    private final Index index;
    private final File cacheRoot;
    private final Map<Pair<String,String>,Object[]> refs = new HashMap<Pair<String,String>,Object[]>();
    private final Set<Pair<String,String>> toDelete = new HashSet<Pair<String,String>> ();
    private final LMListener lmListener;
    private Continuation cont;

    public BinaryAnalyser (Index index, File cacheRoot) {
       assert index != null;
       this.index = index;
       this.cacheRoot = cacheRoot;
       this.lmListener = new LMListener();
    }

    /**
     * Checks validity of underlying index.
     * @return
     * @throws IOException
     */
    public boolean isValid() throws IOException {
        return this.index.isValid(true);
    }

    /** Analyses a classpath root.
     * @param scanning context
     *
     */
    public final Result start (final @NonNull Context ctx) throws IOException, IllegalArgumentException  {
        return start(ctx.getRootURI(), ctx);
    }

    /**
     *
     * @param url of root to be indexed
     * @param canceled - not used
     * @param shutdown - not used
     * @return result of indexing
     * @throws IOException
     * @throws IllegalArgumentException
     * @deprecated Only used by unit tests, the start method is used by impl dep by tests of several modules, safer to keep it.
     */
    @Deprecated
    public final Result start (final @NonNull URL url, final AtomicBoolean canceled, final AtomicBoolean shutdown) throws IOException, IllegalArgumentException  {
        return start (url, SPIAccessor.getInstance().createContext(FileUtil.createMemoryFileSystem().getRoot(), url,
                JavaIndex.NAME, JavaIndex.VERSION, null, false, false, false, null));
    }

    public Result resume () throws IOException {
        assert cont != null;
        return cont.execute();
    }


    public Changes finish () throws IOException {
        if (cont == null) {
            return new Changes(Changes.NO_CHANGES, Changes.NO_CHANGES, Changes.NO_CHANGES);
        }
        final List<Pair<ElementHandle<TypeElement>,Long>> newState = cont.finish();
        final List<Pair<ElementHandle<TypeElement>,Long>> oldState = loadCRCs(cacheRoot);
        cont = null;
        store();
        storeCRCs(cacheRoot, newState);
        return diff(oldState,newState);
    }

    //<editor-fold defaultstate="collapsed" desc="Private helper methods">
    private final Result start (final URL root, final @NonNull Context ctx) throws IOException, IllegalArgumentException  {
        Parameters.notNull("ctx", ctx); //NOI18N
        assert cont == null;
        final String mainP = root.getProtocol();
        if ("jar".equals(mainP)) {          //NOI18N
            URL innerURL = FileUtil.getArchiveFile(root);
            if ("file".equals(innerURL.getProtocol())) {  //NOI18N
                //Fast way
                File archive = new File (URI.create(innerURL.toExternalForm()));
                if (archive.exists() && archive.canRead()) {
                    if (!isUpToDate(null,archive.lastModified())) {
                        index.clear();
                        try {
                            final ZipFile zipFile = new ZipFile(archive);
                            prebuildArgs(zipFile, root);
                            final Enumeration<? extends ZipEntry> e = zipFile.entries();
                            cont = new ZipContinuation (zipFile, e, ctx);
                            return cont.execute();
                        } catch (ZipException e) {
                            LOGGER.warning("Broken zip file: " + archive.getAbsolutePath());
                        }
                    }
                }
                else {
                    return deleted();
                }
            }
            else {
                FileObject rootFo =  URLMapper.findFileObject(root);
                if (rootFo != null) {
                    if (!isUpToDate(null,rootFo.lastModified().getTime())) {
                        index.clear();
                        Enumeration<? extends FileObject> todo = rootFo.getData(true);
                        cont = new FileObjectContinuation (todo, ctx);
                        return cont.execute();
                    }
                }
                else {
                    return deleted();
                }
            }
        }
        else if ("file".equals(mainP)) {    //NOI18N
            //Fast way
            File rootFile = new File (URI.create(root.toExternalForm()));
            if (rootFile.isDirectory()) {
                String path = rootFile.getAbsolutePath ();
                if (path.charAt(path.length()-1) != File.separatorChar) {
                    path = path + File.separatorChar;
                }
                LinkedList<File> todo = new LinkedList<File> ();
                if (rootFile.isDirectory() && rootFile.canRead()) {
                    File[] children = rootFile.listFiles();
                    if (children != null) {
                        todo.addAll(Arrays.asList(children));
                    }
                }
                cont = new FolderContinuation (todo, path, ctx);
                return cont.execute();
            }
        }
        else {
            FileObject rootFo =  URLMapper.findFileObject(root);
            if (rootFo != null) {
                index.clear();
                Enumeration<? extends FileObject> todo = rootFo.getData(true);
                cont = new FileObjectContinuation (todo, ctx);
                return cont.execute();
            }
            else {
                return deleted();
            }
        }
        return Result.FINISHED;
    }

    private List<Pair<ElementHandle<TypeElement>,Long>> loadCRCs(final File indexFolder) throws IOException {
        List<Pair<ElementHandle<TypeElement>,Long>> result = new LinkedList<Pair<ElementHandle<TypeElement>, Long>>();
        final File file = new File (indexFolder,"crc.properties");  //NOI18N
        if (file.canRead()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));   //NOI18N

            try {
                String line;
                while ((line=in.readLine())!=null) {
                    final String[] parts = line.split("=");    //NOI18N
                    if (parts.length == 2) {
                        try {
                            final ElementHandle<TypeElement> handle = ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, parts[0]);
                            final Long crc = Long.parseLong(parts[1]);
                            result.add(Pair.of(handle, crc));
                        } catch (NumberFormatException e) {
                            //Log and pass
                        }
                    }
                }
            } finally {
                in.close();
            }
        }
        return result;
    }

    private void storeCRCs(final File indexFolder, final List<Pair<ElementHandle<TypeElement>,Long>> state) throws IOException {
        final File file = new File (indexFolder,"crc.properties");  //NOI18N
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));   //NOI18N
        try {
            for (Pair<ElementHandle<TypeElement>,Long> pair : state) {
                StringBuilder sb = new StringBuilder(pair.first.getQualifiedName());
                sb.append('='); //NOI18N
                sb.append(pair.second.longValue());
                out.println(sb.toString());
            }
        } finally {
            out.close();
        }
    }

    static Changes diff (List<Pair<ElementHandle<TypeElement>,Long>> oldState, List<Pair<ElementHandle<TypeElement>,Long>> newState) {
        final List<ElementHandle<TypeElement>> changed = new LinkedList<ElementHandle<TypeElement>>();
        final List<ElementHandle<TypeElement>> removed = new LinkedList<ElementHandle<TypeElement>>();
        final List<ElementHandle<TypeElement>> added = new LinkedList<ElementHandle<TypeElement>>();

        final Iterator<Pair<ElementHandle<TypeElement>,Long>> oldIt = oldState.iterator();
        final Iterator<Pair<ElementHandle<TypeElement>,Long>> newIt = newState.iterator();
        Pair<ElementHandle<TypeElement>,Long> oldE = null;
        Pair<ElementHandle<TypeElement>,Long> newE = null;
        while (oldIt.hasNext() && newIt.hasNext()) {
            if (oldE == null) {
                oldE = oldIt.next();
            }
            if (newE == null) {
                newE = newIt.next();
            }
            int ni = oldE.first.getQualifiedName().compareTo(newE.first.getQualifiedName());
            if (ni == 0) {
                if (oldE.second.longValue() == 0 || oldE.second.longValue() != newE.second.longValue()) {
                    changed.add(oldE.first);
                }
                oldE = newE = null;
            }
            else if (ni < 0) {
                removed.add(oldE.first);
                oldE = null;
            }
            else if (ni > 0) {
                added.add(newE.first);
                newE = null;
            }
        }
        if (oldE != null) {
            removed.add(oldE.first);
        }
        while (oldIt.hasNext()) {
            removed.add(oldIt.next().first);
        }
        if (newE != null) {
            added.add(newE.first);
        }
        while (newIt.hasNext()) {
            added.add(newIt.next().first);
        }
        return new Changes(added, removed, changed);
    }

    private static String nameToString( ClassName name ) {
        return name.getInternalName().replace('/', '.');        // NOI18N
    }

    private static void addUsage (final Map<ClassName, Set<ClassIndexImpl.UsageType>> usages, final ClassName name, final ClassIndexImpl.UsageType usage) {
        if (OBJECT.equals(name.getExternalName())) {
            return;
        }
        Set<ClassIndexImpl.UsageType> uset = usages.get(name);
        if (uset == null) {
            uset = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
            usages.put(name, uset);
        }
        uset.add(usage);
    }

    /**
     * Prebuilds argument names for {@link javax.swing.JComponent} to speed up first
     * call of code completion on swing classes. Has no semantic impact only improves performance,
     * so it's can be safely disabled.
     * @param archiveFile the archive
     * @param archiveUrl url of an archive
     */
    private static void prebuildArgs (final ZipFile archiveFile, final URL archiveUrl) {
        final ZipEntry jce = archiveFile.getEntry(FileObjects.convertPackage2Folder(javax.swing.JComponent.class.getName())+'.'+FileObjects.CLASS);   //NOI18N
        if (jce != null) {                                   //NOI18N
            //On the IBM VMs the swing is in separate jar (graphics.jar) where no j.l package exists, don't prebuild such an archive.
            //The param names will be created on deamand
            final ZipEntry oe = archiveFile.getEntry(FileObjects.convertPackage2Folder(Object.class.getName())+'.'+FileObjects.CLASS);   //NOI18N
            if (oe != null) {
                class DevNullDiagnosticListener implements DiagnosticListener<JavaFileObject> {
                    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE, "Diagnostic reported during prebuilding args: {0}", diagnostic.toString()); //NOI18N
                        }
                    }
                };
                ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new URL[]{archiveUrl}),
                    ClassPathSupport.createClassPath(new URL[0]),
                    ClassPathSupport.createClassPath(new URL[0]));
                final JavacTaskImpl jt = JavacParser.createJavacTask(cpInfo, new DevNullDiagnosticListener(), null, null, null, null);
                TreeLoader.preRegister(jt.getContext(), cpInfo);
                TypeElement jc = jt.getElements().getTypeElement(javax.swing.JComponent.class.getName());
                if (jc != null) {
                    List<ExecutableElement> methods = ElementFilter.methodsIn(jc.getEnclosedElements());
                    for (ExecutableElement method : methods) {
                        List<? extends VariableElement> params = method.getParameters();
                        if (!params.isEmpty()) {
                            params.get(0).getSimpleName();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void store() throws IOException {
        try {
            if (this.refs.size()>0 || this.toDelete.size()>0) {
                this.index.store(this.refs,this.toDelete);
            }
        } finally {
            refs.clear();
            toDelete.clear();
        }
    }

    private final boolean isUpToDate(String resourceName, long resourceMTime) throws IOException {
        return this.index.isUpToDate(resourceName, resourceMTime);
    }

    private Result deleted () throws IOException {
        return (cont = new DeletedContinuation()).execute();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Class file introspection">
    private final void delete (final String className) throws IOException {
        assert className != null;
        this.toDelete.add(Pair.<String,String>of(className,null));
    }

    private boolean accepts(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1 || (index+1) == name.length()) {
            return false;
        }
        return "CLASS".equalsIgnoreCase(name.substring(index+1));  // NOI18N
    }

    private void analyse (final InputStream inputStream) throws IOException {
        final ClassFile classFile = new ClassFile(inputStream);
        final ClassName className = classFile.getName ();
        final String classNameStr = nameToString (className);
        this.delete (classNameStr);
        final Map <ClassName, Set<ClassIndexImpl.UsageType>> usages = performAnalyse(classFile, classNameStr);
        ElementKind kind = ElementKind.CLASS;
        if (classFile.isEnum()) {
            kind = ElementKind.ENUM;
        }
        else if (classFile.isAnnotation()) {
            kind = ElementKind.ANNOTATION_TYPE;
        }
        else if ((classFile.getAccess() & Access.INTERFACE) == Access.INTERFACE) {
            kind = ElementKind.INTERFACE;
        }
        final String classNameType = classNameStr + DocumentUtil.encodeKind(kind);
        final Pair<String,String> pair = Pair.<String,String>of(classNameType, null);
        final List <String> references = getClassReferences (pair);
        for (Map.Entry<ClassName,Set<ClassIndexImpl.UsageType>> entry : usages.entrySet()) {
            ClassName name = entry.getKey();
            Set<ClassIndexImpl.UsageType> usage = entry.getValue();
            references.add (DocumentUtil.encodeUsage( nameToString(name), usage));
        }
    }

    @SuppressWarnings ("unchecked")    //NOI18N, the classfile module is not generic
    private Map <ClassName,Set<ClassIndexImpl.UsageType>> performAnalyse(final ClassFile classFile, final String className) throws IOException {
        final Map <ClassName, Set<ClassIndexImpl.UsageType>> usages = new HashMap <ClassName, Set<ClassIndexImpl.UsageType>> ();
        //Add type signature of this class
        String signature = classFile.getTypeSignature();
        if (signature != null) {
            try {
                ClassName[] typeSigNames = ClassFileUtil.getTypesFromClassTypeSignature (signature);
                for (ClassName typeSigName : typeSigNames) {
                    addUsage(usages, typeSigName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
            } catch (final RuntimeException re) {
                final StringBuilder message = new StringBuilder ("BinaryAnalyser: Cannot read type: " + signature+" cause: " + re.getLocalizedMessage() + '\n');    //NOI18N
                final StackTraceElement[] elements = re.getStackTrace();
                for (StackTraceElement e : elements) {
                    message.append(e.toString());
                    message.append('\n');   //NOI18N
                }
                LOGGER.warning(message.toString());    //NOI18N
            }
        }

        // 0. Add the superclass
        ClassName scName = classFile.getSuperClass();
        if ( scName != null ) {
            addUsage (usages, scName, ClassIndexImpl.UsageType.SUPER_CLASS);
        }

        // 1. Add interfaces
        Collection<ClassName> interfaces = classFile.getInterfaces();
        for( ClassName ifaceName : interfaces ) {
            addUsage (usages, ifaceName, ClassIndexImpl.UsageType.SUPER_INTERFACE);
        }

        if (!FULL_INDEX) {
            //1a. Add top-level class annotations:
            handleAnnotations(usages, classFile.getAnnotations(), true);
        }

        if (FULL_INDEX) {
            //1b. Add class annotations:
            handleAnnotations(usages, classFile.getAnnotations(), false);

            //2. Add filed usages
            final ConstantPool constantPool = classFile.getConstantPool();
            Collection<? extends CPFieldInfo> fields = constantPool.getAllConstants(CPFieldInfo.class);
            for (CPFieldInfo field : fields) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(field.getClassID()));
                if (name != null) {
                    addUsage (usages, name, ClassIndexImpl.UsageType.FIELD_REFERENCE);
                }
            }

            //3. Add method usages
            Collection<? extends CPMethodInfo> methodCalls = constantPool.getAllConstants(CPMethodInfo.class);
            for (CPMethodInfo method : methodCalls) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(method.getClassID()));
                if (name != null) {
                    addUsage (usages, name, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                }
            }
            methodCalls = constantPool.getAllConstants(CPInterfaceMethodInfo.class);
            for (CPMethodInfo method : methodCalls) {
                ClassName name = ClassFileUtil.getType(constantPool.getClass(method.getClassID()));
                if (name != null) {
                    addUsage (usages, name, ClassIndexImpl.UsageType.METHOD_REFERENCE);
                }
            }

            //4, 5, 6, 8 Add method type refs (return types, param types, exception types) and local variables.
            Collection<Method> methods = classFile.getMethods();
            for (Method method : methods) {
                handleAnnotations(usages, method.getAnnotations(), false);

                String jvmTypeId = method.getReturnType();
                ClassName type = ClassFileUtil.getType (jvmTypeId);
                if (type != null) {
                    addUsage(usages, type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
                List<Parameter> params =  method.getParameters();
                for (Parameter param : params) {
                    jvmTypeId = param.getDescriptor();
                    type = ClassFileUtil.getType (jvmTypeId);
                    if (type != null) {
                        addUsage(usages, type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                }
                CPClassInfo[] classInfos = method.getExceptionClasses();
                for (CPClassInfo classInfo : classInfos) {
                    type = classInfo.getClassName();
                    if (type != null) {
                        addUsage(usages, type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                }
                jvmTypeId = method.getTypeSignature();
                if (jvmTypeId != null) {
                    try {
                        ClassName[] typeSigNames = ClassFileUtil.getTypesFromMethodTypeSignature (jvmTypeId);
                        for (ClassName typeSigName : typeSigNames) {
                            addUsage(usages, typeSigName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                    } catch (IllegalStateException is) {
                        LOGGER.warning("Invalid method signature: "+className+"::"+method.getName()+" signature is:" + jvmTypeId);  // NOI18N
                    }
                }
                Code code = method.getCode();
                if (code != null) {
                    LocalVariableTableEntry[] vars = code.getLocalVariableTable();
                    for (LocalVariableTableEntry var : vars) {
                        type = ClassFileUtil.getType (var.getDescription());
                        if (type != null) {
                            addUsage(usages, type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                    }
                    LocalVariableTypeTableEntry[] varTypes = method.getCode().getLocalVariableTypeTable();
                    for (LocalVariableTypeTableEntry varType : varTypes) {
                        try {
                            ClassName[] typeSigNames = ClassFileUtil.getTypesFromFiledTypeSignature (varType.getSignature());
                            for (ClassName typeSigName : typeSigNames) {
                                addUsage(usages, typeSigName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                            }
                        } catch (IllegalStateException is) {
                            LOGGER.warning("Invalid local variable signature: "+className+"::"+method.getName());  // NOI18N
                        }
                    }
                }
            }
            //7. Add Filed Type References
            Collection<Variable> vars = classFile.getVariables();
            for (Variable var : vars) {
                handleAnnotations(usages, var.getAnnotations(), false);

                String jvmTypeId = var.getDescriptor();
                ClassName type = ClassFileUtil.getType (jvmTypeId);
                if (type != null) {
                    addUsage (usages, type, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
                jvmTypeId = var.getTypeSignature();
                if (jvmTypeId != null) {
                    try {
                        ClassName[] typeSigNames = ClassFileUtil.getTypesFromFiledTypeSignature (jvmTypeId);
                        for (ClassName typeSigName : typeSigNames) {
                            addUsage(usages, typeSigName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                        }
                    } catch (IllegalStateException is) {
                        LOGGER.warning("Invalid field signature: "+className+"::"+var.getName()+" signature is: "+jvmTypeId);  // NOI18N
                    }
                }
            }

            //9. Remains
            Collection<? extends CPClassInfo> cis = constantPool.getAllConstants(CPClassInfo.class);
            for (CPClassInfo ci : cis) {
                ClassName ciName = ClassFileUtil.getType(ci);
                if (ciName != null && !usages.keySet().contains (ciName)) {
                    addUsage(usages, ciName, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }
            }
        }
        return usages;
    }

    private List<String> getClassReferences (final Pair<String,String> name) {
        assert name != null;
        Object[] cr = this.refs.get (name);
        if (cr == null) {
            cr = new Object[] {
                new ArrayList<String> (),
                null,
                null
            };
            this.refs.put (name, cr);
        }
        return (ArrayList<String>) cr[0];
    }

    private void handleAnnotations(final Map<ClassName, Set<UsageType>> usages, Iterable<? extends Annotation> annotations, boolean onlyTopLevel) {
        for (Annotation a : annotations) {
            addUsage(usages, a.getType(), ClassIndexImpl.UsageType.TYPE_REFERENCE);

            if (onlyTopLevel) {
                continue;
            }

            List<ElementValue> toProcess = new LinkedList<ElementValue>();

            for (AnnotationComponent ac : a.getComponents()) {
                toProcess.add(ac.getValue());
            }

            while (!toProcess.isEmpty()) {
                ElementValue ev = toProcess.remove(0);

                if (ev instanceof ArrayElementValue) {
                    toProcess.addAll(Arrays.asList(((ArrayElementValue) ev).getValues()));
                }

                if (ev instanceof NestedElementValue) {
                    Annotation nested = ((NestedElementValue) ev).getNestedValue();

                    addUsage(usages, nested.getType(), ClassIndexImpl.UsageType.TYPE_REFERENCE);

                    for (AnnotationComponent ac : nested.getComponents()) {
                        toProcess.add(ac.getValue());
                    }
                }

                if (ev instanceof ClassElementValue) {
                    addUsage(usages, ((ClassElementValue) ev).getClassName(), ClassIndexImpl.UsageType.TYPE_REFERENCE);
                }

                if (ev instanceof EnumElementValue) {
                    String type = ((EnumElementValue) ev).getEnumType();
                    ClassName className = ClassFileUtil.getType(type);

                    if (className != null) {
                        addUsage(usages, className, ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    }
                }
            }
        }
    }
    //</editor-fold>

    private static abstract class Continuation {

        private List<Pair<ElementHandle<TypeElement>,Long>> result;

        protected Continuation () {
            this.result = new ArrayList<Pair<ElementHandle<TypeElement>, Long>>();
        }

        protected abstract Result doExecute () throws IOException;

        protected abstract void doFinish () throws IOException;

        protected final void report (final ElementHandle<TypeElement> te, final long crc) {
            this.result.add(Pair.of(te, crc));
        }

        public final Result execute () throws IOException {
            return doExecute();
        }

        public final List<Pair<ElementHandle<TypeElement>,Long>> finish () throws IOException {
            doFinish();
            Collections.sort(result, new Comparator() {
                public int compare(Object o1, Object o2) {
                    final Pair<ElementHandle<TypeElement>,Long> p1 = (Pair<ElementHandle<TypeElement>,Long>) o1;
                    final Pair<ElementHandle<TypeElement>,Long> p2 = (Pair<ElementHandle<TypeElement>,Long>) o2;
                    return p1.first.getQualifiedName().compareTo(p2.first.getQualifiedName());
                }
            });
            return result;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Continuation implementations (Zip, FileObject, java.io.File, Deleted)">
    private class ZipContinuation extends Continuation {

        private final ZipFile zipFile;
        private final Enumeration<? extends ZipEntry> entries;
        private final Context ctx;

        public ZipContinuation (final @NonNull ZipFile zipFile, final @NonNull Enumeration<? extends ZipEntry> entries, final @NonNull Context ctx) {
            assert zipFile != null;
            assert entries != null;
            assert ctx != null;
            this.zipFile = zipFile;
            this.entries = entries;
            this.ctx = ctx;
        }

        protected Result doExecute () throws IOException {
            while(entries.hasMoreElements()) {
                ZipEntry ze;

                try {
                    ze = (ZipEntry)entries.nextElement();
                } catch (InternalError err) {
                    //#174611:
                    LOGGER.log(Level.INFO, "Broken zip file: " + zipFile.getName(), err);
                    return Result.FINISHED;
                }

                if ( !ze.isDirectory()  && accepts(ze.getName()))  {
                    cont.report (ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, FileObjects.convertFolder2Package(FileObjects.stripExtension(ze.getName()))),ze.getCrc());
                    InputStream in = new BufferedInputStream (zipFile.getInputStream( ze ));
                    try {
                        analyse(in);
                    } catch (InvalidClassFormatException icf) {
                        LOGGER.warning("Invalid class file format: "+ new File(zipFile.getName()).toURI() + "!/" + ze.getName());     //NOI18N
                    } catch (IOException x) {
                        Exceptions.attachMessage(x, "While scanning: " + ze.getName());                                         //NOI18N
                        throw x;
                    }
                    finally {
                        in.close();
                    }
                    if (lmListener.isLowMemory()) {
                        store();
                    }
                }
                //Partinal cancel not supported by parsing API
                //if (cancel.getAndSet(false)) {
                //    this.store();
                //    return Result.CANCELED;
                //}
                if (ctx.isCancelled()) {
                    return Result.CLOSED;
                }
            }
            return Result.FINISHED;
        }

        protected void doFinish () throws IOException {
            this.zipFile.close();
        }
    }

    private class FolderContinuation extends Continuation {

        private final LinkedList<File> todo;
        private final String rootPath;
        private final Context ctx;

        public FolderContinuation (final @NonNull LinkedList<File> todo, final @NonNull String rootPath, final @NonNull Context ctx) {
            assert todo != null;
            assert rootPath != null;
            assert ctx != null;
            this.todo = todo;
            this.rootPath = rootPath;
            this.ctx = ctx;
        }

        public Result doExecute () throws IOException {
            while (!todo.isEmpty()) {
                File file = todo.removeFirst();
                if (file.isDirectory() && file.canRead()) {
                    File[] c = file.listFiles();
                    if (c!= null) {
                        todo.addAll(Arrays.asList (c));
                    }
                }
                else if (accepts(file.getName())) {
                    String filePath = file.getAbsolutePath();
                    long fileMTime = file.lastModified();
                    int dotIndex = filePath.lastIndexOf('.');
                    int slashIndex = filePath.lastIndexOf('/');
                    int endPos;
                    if (dotIndex>slashIndex) {
                        endPos = dotIndex;
                    }
                    else {
                        endPos = filePath.length();
                    }
                    String relativePath = FileObjects.convertFolder2Package (filePath.substring(rootPath.length(), endPos));
                    cont.report(ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, relativePath), 0L);
                    if (accepts(file.getName()) && !isUpToDate (relativePath, fileMTime)) {
                        toDelete.add(Pair.<String,String>of (relativePath,null));
                        try {
                            InputStream in = new BufferedInputStream(new FileInputStream(file));
                            try {
                                analyse(in);
                            } catch (InvalidClassFormatException icf) {
                                LOGGER.warning("Invalid class file format: " + file.getAbsolutePath());      //NOI18N

                            } finally {
                                in.close();
                            }
                        } catch (IOException ex) {
                            //unreadable file?
                            LOGGER.warning("Cannot read file: " + file.getAbsolutePath());      //NOI18N
                            LOGGER.log(Level.FINE, null, ex);
                        }
                        if (lmListener.isLowMemory()) {
                            store();
                        }
                    }
                }
                // Partinal cancel not supported by parsing API
                //if (cancel.getAndSet(false)) {
                //    this.store();
                //    return Result.CANCELED;
                //}
                if (ctx.isCancelled()) {
                    return Result.CLOSED;
                }
            }
            return Result.FINISHED;
        }

        public void doFinish () throws IOException {
        }
    }

    private class FileObjectContinuation extends  Continuation {

        private final Enumeration<? extends FileObject> todo;
        private FileObject root;
        private final Context ctx;

        public FileObjectContinuation (final @NonNull Enumeration<? extends FileObject> todo, final @NonNull Context ctx) {
            assert todo != null;
            assert ctx != null;
            this.todo = todo;
            this.ctx = ctx;
        }

        public Result doExecute () throws IOException {
            while (todo.hasMoreElements()) {
                FileObject fo = todo.nextElement();
                if (accepts(fo.getName())) {
                    final String rp = FileObjects.stripExtension(FileUtil.getRelativePath(root, fo));
                    cont.report(ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, FileObjects.convertFolder2Package(rp)), 0L);
                    InputStream in = new BufferedInputStream (fo.getInputStream());
                    try {
                        analyse (in);
                    } catch (InvalidClassFormatException icf) {
                        LOGGER.warning("Invalid class file format: "+FileUtil.getFileDisplayName(fo));      //NOI18N
                    }
                    finally {
                        in.close();
                    }
                    if (lmListener.isLowMemory()) {
                        store();
                    }
                }
                // Partinal cancel not supported by parsing API
                //if (cancel.getAndSet(false)) {
                //    this.store();
                //    return Result.CANCELED;
                //}
                if (ctx.isCancelled()) {
                    return Result.CLOSED;
                }
            }
            return Result.FINISHED;
        }

        public void doFinish () throws IOException {

        }
    }

    private class DeletedContinuation extends Continuation {

        @Override
        protected Result doExecute() throws IOException {
            index.clear();
            return Result.FINISHED;
        }

        @Override
        protected void doFinish() throws IOException {
        }
    }
    //</editor-fold>
}

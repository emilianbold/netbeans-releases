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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
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
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.SuspendSupport;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.Utilities;




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
        public final boolean preBuildArgs;

        private Changes (
                final List<ElementHandle<TypeElement>> added,
                final List<ElementHandle<TypeElement>> removed,
                final List<ElementHandle<TypeElement>> changed,
                final boolean preBuildArgs) {
            this.added = added;
            this.removed = removed;
            this.changed = changed;
            this.preBuildArgs = preBuildArgs;
        }

    }

    private static final String ROOT = "/"; //NOI18N
    private static final String TIME_STAMPS = "timestamps.properties";   //NOI18N
    private static final String CRC = "crc.properties"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(BinaryAnalyser.class.getName());
    private static final String JCOMPONENT = javax.swing.JComponent.class.getName();
    static final String OBJECT = Object.class.getName();    

    private static boolean FULL_INDEX = Boolean.getBoolean("org.netbeans.modules.java.source.usages.BinaryAnalyser.fullIndex");     //NOI18N

    private final ClassIndexImpl.Writer writer;
    private final File cacheRoot;
    private final List<Pair<Pair<String,String>,Object[]>> refs = new ArrayList<Pair<Pair<String, String>, Object[]>>();
    private final Set<Pair<String,String>> toDelete = new HashSet<Pair<String,String>> ();
    private final LowMemoryWatcher lmListener;
    private Continuation cont;
    //@NotThreadSafe
    private Pair<LongHashMap<String>,Set<String>> timeStamps;

    BinaryAnalyser (final @NonNull ClassIndexImpl.Writer writer, final @NonNull File cacheRoot) {
       Parameters.notNull("writer", writer);   //NOI18N
       Parameters.notNull("cacheRoot", cacheRoot);  //NOI18N
       this.writer = writer;
       this.cacheRoot = cacheRoot;
       this.lmListener = LowMemoryWatcher.getInstance();
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
        return start (
                url,
                SPIAccessor.getInstance().createContext(
                    FileUtil.createMemoryFileSystem().getRoot(),
                    url,
                    JavaIndex.NAME,
                    JavaIndex.VERSION,
                    null,
                    false,
                    false,
                    false,
                    SuspendSupport.NOP,
                    null,
                    null));
    }

    public Result resume () throws IOException {
        assert cont != null;
        return cont.execute();
    }


    public Changes finish () throws IOException {
        if (cont == null) {
            return new Changes(Changes.NO_CHANGES, Changes.NO_CHANGES, Changes.NO_CHANGES, false);
        }
        if (!cont.hasChanges() && timeStampsEmpty()) {
            assert refs.isEmpty();
            assert toDelete.isEmpty();
            return new Changes(Changes.NO_CHANGES, Changes.NO_CHANGES, Changes.NO_CHANGES, false);
        }
        final List<Pair<ElementHandle<TypeElement>,Long>> newState = cont.finish();
        final List<Pair<ElementHandle<TypeElement>,Long>> oldState = loadCRCs(cacheRoot);
        final boolean preBuildArgs = cont.preBuildArgs();
        cont = null;
        store();
        storeCRCs(cacheRoot, newState);
        storeTimeStamps();
        return diff(oldState,newState, preBuildArgs);
    }

    //<editor-fold defaultstate="collapsed" desc="Private helper methods">
    private Result start (final URL root, final @NonNull Context ctx) throws IOException, IllegalArgumentException  {
        Parameters.notNull("ctx", ctx); //NOI18N
        assert cont == null;
        final String mainP = root.getProtocol();
        if ("jar".equals(mainP)) {          //NOI18N
            final URL innerURL = FileUtil.getArchiveFile(root);
            if ("file".equals(innerURL.getProtocol())) {  //NOI18N
                //Fast way
                final File archive = Utilities.toFile(URI.create(innerURL.toExternalForm()));
                if (archive.canRead()) {
                    if (!isUpToDate(ROOT,archive.lastModified())) {
                        writer.clear();
                        try {
                            final ZipFile zipFile = new ZipFile(archive);
                            final Enumeration<? extends ZipEntry> e = zipFile.entries();
                            cont = new ZipContinuation (zipFile, e, ctx);
                            return cont.execute();
                        } catch (ZipException e) {
                            LOGGER.log(Level.WARNING, "Broken zip file: {0}", archive.getAbsolutePath());
                        }
                    }
                } else {
                    return deleted();
                }
            } else {
                final FileObject rootFo =  URLMapper.findFileObject(root);
                if (rootFo != null) {
                    if (!isUpToDate(ROOT,rootFo.lastModified().getTime())) {
                        writer.clear();
                        Enumeration<? extends FileObject> todo = rootFo.getData(true);
                        cont = new FileObjectContinuation (todo, rootFo, ctx);
                        return cont.execute();
                    }
                } else {
                    return deleted();
                }
            }
        } else if ("file".equals(mainP)) {    //NOI18N
            //Fast way
            final File rootFile = Utilities.toFile(URI.create(root.toExternalForm()));
            if (rootFile.isDirectory()) {
                String path = rootFile.getAbsolutePath ();
                if (path.charAt(path.length()-1) != File.separatorChar) {
                    path = path + File.separatorChar;
                }
                LinkedList<File> todo = new LinkedList<File> ();
                File[] children = rootFile.listFiles();
                if (children != null) {
                    todo.addAll(Arrays.asList(children));
                }
                cont = new FolderContinuation (todo, path, ctx);
                return cont.execute();
            } else if (!rootFile.exists()) {
                return deleted();
            }
        } else {
            final FileObject rootFo =  URLMapper.findFileObject(root);
            if (rootFo != null) {
                writer.clear();
                Enumeration<? extends FileObject> todo = rootFo.getData(true);
                cont = new FileObjectContinuation (todo, rootFo, ctx);
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
        final File file = new File (indexFolder,CRC);
        if (file.canRead()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));   //NOI18N

            try {
                String line;
                while ((line=in.readLine())!=null) {
                    final String[] parts = line.split("=");    //NOI18N
                    if (parts.length == 2) {
                        try {
                            final ElementHandle<TypeElement> handle = ElementHandle.createTypeElementHandle(ElementKind.CLASS, parts[0]);
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
        final File file = new File (indexFolder,CRC);
        if (state.isEmpty()) {
            file.delete();
        } else {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));   //NOI18N
            try {
                for (Pair<ElementHandle<TypeElement>,Long> pair : state) {
                    StringBuilder sb = new StringBuilder(pair.first.getBinaryName());
                    sb.append('='); //NOI18N
                    sb.append(pair.second.longValue());
                    out.println(sb.toString());
                }
            } finally {
                out.close();
            }
        }
    }
    
    @NonNull
    private Pair<LongHashMap<String>,Set<String>> getTimeStamps() throws IOException {
        if (timeStamps == null) {
            final LongHashMap<String> map = new LongHashMap<String>();
            final File f = new File (cacheRoot, TIME_STAMPS); //NOI18N
            if (f.exists()) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8")); //NOI18N
                try {
                    String line;
                    while (null != (line = in.readLine())) {
                        int idx = line.indexOf('='); //NOI18N
                        if (idx != -1) {
                            try {
                                long ts = Long.parseLong(line.substring(idx + 1));
                                map.put(line.substring(0, idx), ts);
                            } catch (NumberFormatException nfe) {
                                LOGGER.log(Level.FINE, "Invalid timestamp: line={0}, timestamps={1}, exception={2}", new Object[] { line, f.getPath(), nfe }); //NOI18N
                            }
                        }
                    }
                } finally {
                    in.close();
                }
            }
            timeStamps = Pair.<LongHashMap<String>,Set<String>>of(map,new HashSet<String>(map.keySet()));
        }
        return timeStamps;
    }

    private void storeTimeStamps() throws IOException {
        final File f = new File (cacheRoot, TIME_STAMPS);
        if (timeStamps == null) {
            f.delete();
        } else {
            timeStamps.first.keySet().removeAll(timeStamps.second);
            final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8")); //NOI18N
            try {
                // write data
                for(LongHashMap.Entry<String> entry : timeStamps.first.entrySet()) {
                    out.write(entry.getKey());
                    out.write('='); //NOI18N
                    out.write(Long.toString(entry.getValue()));
                    out.newLine();
                }
                out.flush();
            } finally {
                timeStamps = null;
                out.close();
            }
        }
    }

    private boolean timeStampsEmpty() {
        return timeStamps == null || timeStamps.second.isEmpty();
    }

    private boolean isUpToDate(final String resourceName, final long timeStamp) throws IOException {
        final Pair<LongHashMap<String>,Set<String>> ts = getTimeStamps();
        long oldTime = ts.first.put(resourceName,timeStamp);
        ts.second.remove(resourceName);
        return oldTime == timeStamp;
    }

    static Changes diff (
            final List<Pair<ElementHandle<TypeElement>,Long>> oldState,
            final List<Pair<ElementHandle<TypeElement>,Long>> newState,
            final boolean preBuildArgs
            ) {
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
            int ni = oldE.first.getBinaryName().compareTo(newE.first.getBinaryName());
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
        return new Changes(added, removed, changed, preBuildArgs);
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
    
    private void releaseData() {
        refs.clear();
        toDelete.clear();
    }
    
    private void flush() throws IOException {
        try {
            if (this.refs.size()>0 || this.toDelete.size()>0) {
                this.writer.deleteAndFlush(this.refs,this.toDelete);
            }
        } finally {
            releaseData();
        }
    }

    private void store() throws IOException {
        try {
            // do unconditionally, so pending flushed changes are committed, at least.
            this.writer.deleteAndStore(this.refs,this.toDelete);
        } finally {
            releaseData();
        }
    }
    
    private Result deleted () throws IOException {
        return (cont = new DeletedContinuation()).execute();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Class file introspection">
    private void delete (final String className) throws IOException {
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
                        LOGGER.log(Level.WARNING, "Invalid method signature: {0}::{1} signature is:{2}",
                                new Object[] {
                                    className,
                                    method.getName(),
                                    jvmTypeId});  // NOI18N
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
                            LOGGER.log(Level.WARNING, "Invalid local variable signature: {0}::{1}",
                                    new Object[]{
                                        className,
                                        method.getName()});  // NOI18N
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
                        LOGGER.log(Level.WARNING, "Invalid field signature: {0}::{1} signature is: {2}",
                                new Object[]{
                                    className, var.getName(),
                                    jvmTypeId});  // NOI18N
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
        Object[] cr = new Object[] {
            new ArrayList<String> (),
            null,
            null
        };
        this.refs.add(Pair.<Pair<String,String>,Object[]>of(name, cr));
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
        private boolean changed;
        private byte preBuildArgsState;

        protected Continuation () {
            this.result = new ArrayList<Pair<ElementHandle<TypeElement>, Long>>();
        }

        protected abstract Result doExecute () throws IOException;

        protected abstract void doFinish () throws IOException;

        protected final void report (final ElementHandle<TypeElement> te, final long crc) {
            this.result.add(Pair.of(te, crc));
            //On the IBM VMs the swing is in separate jar (graphics.jar) where no j.l package exists, don't prebuild such an archive.
            //The param names will be created on deamand
            final String binName = te.getBinaryName();
            if (OBJECT.equals(binName)) {
                preBuildArgsState|=1;
            } else if (JCOMPONENT.equals(binName)) {
                preBuildArgsState|=2;
            }
        }
        
        protected final void markChanged() {
            this.changed = true;
        }

        public final Result execute () throws IOException {
            return doExecute();
        }

        public final List<Pair<ElementHandle<TypeElement>,Long>> finish () throws IOException {
            doFinish();
            Collections.sort(result, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    final Pair<ElementHandle<TypeElement>,Long> p1 = (Pair<ElementHandle<TypeElement>,Long>) o1;
                    final Pair<ElementHandle<TypeElement>,Long> p2 = (Pair<ElementHandle<TypeElement>,Long>) o2;
                    return p1.first.getBinaryName().compareTo(p2.first.getBinaryName());
                }
            });
            return result;
        }
        
        public final boolean hasChanges() {
            return changed;
        }
        
        public final boolean preBuildArgs() {
            return preBuildArgsState == 3;
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
            markChanged();  //Always dirty, created only for dirty root
        }

        @Override
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
                    cont.report (ElementHandle.createTypeElementHandle(ElementKind.CLASS, FileObjects.convertFolder2Package(FileObjects.stripExtension(ze.getName()))),ze.getCrc());
                    InputStream in = new BufferedInputStream (zipFile.getInputStream( ze ));
                    try {
                        analyse(in);
                    } catch (InvalidClassFormatException icf) {
                        LOGGER.log(Level.WARNING, "Invalid class file format: {0}!/{1}",
                                new Object[]{
                                    Utilities.toURI(new File(zipFile.getName())),
                                    ze.getName()});     //NOI18N
                    } catch (IOException x) {
                        Exceptions.attachMessage(x, "While scanning: " + ze.getName());                                         //NOI18N
                        throw x;
                    }
                    finally {
                        in.close();
                    }
                    if (lmListener.isLowMemory()) {
                        flush();
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

        @Override
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

        @Override
        public Result doExecute () throws IOException {
            while (!todo.isEmpty()) {
                File file = todo.removeFirst();
                if (file.isDirectory()) {
                    File[] c = file.listFiles();
                    if (c!= null) {
                        todo.addAll(Arrays.asList (c));
                    }
                } else if (accepts(file.getName())) {
                    String filePath = file.getAbsolutePath();
                    long fileMTime = file.lastModified();
                    int dotIndex = filePath.lastIndexOf('.');
                    int slashIndex = filePath.lastIndexOf(File.separatorChar);
                    int endPos;
                    if (dotIndex>slashIndex) {
                        endPos = dotIndex;
                    }
                    else {
                        endPos = filePath.length();
                    }
                    String relativePath = FileObjects.convertFolder2Package (filePath.substring(rootPath.length(), endPos), File.separatorChar);
                    cont.report(ElementHandle.createTypeElementHandle(ElementKind.CLASS, relativePath), fileMTime);
                    if (!isUpToDate (relativePath, fileMTime)) {
                        markChanged();
                        toDelete.add(Pair.<String,String>of (relativePath,null));
                        try {
                            InputStream in = new BufferedInputStream(new FileInputStream(file));
                            try {
                                analyse(in);
                            } catch (InvalidClassFormatException icf) {
                                LOGGER.log(Level.WARNING, "Invalid class file format: {0}", file.getAbsolutePath());      //NOI18N

                            } finally {
                                in.close();
                            }
                        } catch (IOException ex) {
                            //unreadable file?
                            LOGGER.log(Level.WARNING, "Cannot read file: {0}", file.getAbsolutePath());      //NOI18N
                            LOGGER.log(Level.FINE, null, ex);
                        }
                        if (lmListener.isLowMemory()) {
                            flush();
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
            
            for (String deleted : getTimeStamps().second) {
                delete(deleted);
                markChanged();
            }
            return Result.FINISHED;
        }

        @Override
        public void doFinish () throws IOException {
        }
    }

    private class FileObjectContinuation extends  Continuation {

        private final Enumeration<? extends FileObject> todo;
        private final FileObject root;
        private final Context ctx;

        public FileObjectContinuation (final @NonNull Enumeration<? extends FileObject> todo, final @NonNull FileObject root, final @NonNull Context ctx) {
            assert todo != null;
            assert ctx != null;
            this.todo = todo;
            this.root = root;
            this.ctx = ctx;
            markChanged();  //Always dirty, created only for dirty root
        }

        @Override
        public Result doExecute () throws IOException {
            while (todo.hasMoreElements()) {
                FileObject fo = todo.nextElement();
                if (accepts(fo.getName())) {
                    final String rp = FileObjects.stripExtension(FileUtil.getRelativePath(root, fo));
                    cont.report(ElementHandle.createTypeElementHandle(ElementKind.CLASS, FileObjects.convertFolder2Package(rp)), 0L);
                    InputStream in = new BufferedInputStream (fo.getInputStream());
                    try {
                        analyse (in);
                    } catch (InvalidClassFormatException icf) {
                        LOGGER.log(Level.WARNING, "Invalid class file format: {0}", FileUtil.getFileDisplayName(fo));      //NOI18N
                    }
                    finally {
                        in.close();
                    }
                    if (lmListener.isLowMemory()) {
                        flush();
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

        @Override
        public void doFinish () throws IOException {

        }
    }

    private class DeletedContinuation extends Continuation {
        
        public DeletedContinuation() throws IOException {
            final Pair<LongHashMap<String>, Set<String>> ts = getTimeStamps();
            if (!ts.first.isEmpty()) {
                markChanged();
            }
        }

        @Override
        protected Result doExecute() throws IOException {
            if (hasChanges()) {
                writer.clear();
            }
            return Result.FINISHED;
        }

        @Override
        protected void doFinish() throws IOException {
        }
    }
    //</editor-fold>
}

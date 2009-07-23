/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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
import org.netbeans.api.java.source.ClasspathInfo;
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
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.java.source.util.LowMemoryEvent;
import org.netbeans.modules.java.source.util.LowMemoryListener;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;




/**
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class BinaryAnalyser implements LowMemoryListener {

    public enum Result {
        FINISHED,
        CANCELED,
        CLOSED,
    };
    
    private static final Logger LOGGER = Logger.getLogger(BinaryAnalyser.class.getName());
    static final String OBJECT = Object.class.getName();                        
    
    private static boolean FULL_INDEX = Boolean.getBoolean("org.netbeans.modules.java.source.usages.BinaryAnalyser.fullIndex");     //NOI18N
    
    private final Index index;
    private final Map<Pair<String,String>,Object[]> refs = new HashMap<Pair<String,String>,Object[]>();
    private final Set<Pair<String,String>> toDelete = new HashSet<Pair<String,String>> ();
    private final AtomicBoolean lowMemory;
    private Continuation cont;

    public BinaryAnalyser (Index index) {
       assert index != null;
       this.index = index;
       this.lowMemory = new AtomicBoolean (false);
    }
    
        /** Analyses a classpath root.
     * @param URL the classpath root, either a folder or an archive file.
     *     
     */
    public final Result start (final URL root, final AtomicBoolean cancel, final AtomicBoolean closed) throws IOException, IllegalArgumentException  {
        assert root != null;        
        assert cont == null;
        LowMemoryNotifier.getDefault().addLowMemoryListener (BinaryAnalyser.this);
        try {
            String mainP = root.getProtocol();
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
                                cont = new ZipContinuation (zipFile, e, cancel, closed);
                                return cont.execute();
                            } catch (ZipException e) {
                                LOGGER.warning("Broken zip file: " + archive.getAbsolutePath());
                            }
                        }
                    }
                }
                else {
                    FileObject rootFo =  URLMapper.findFileObject(root);
                    if (rootFo != null) {
                        if (!isUpToDate(null,rootFo.lastModified().getTime())) {
                            index.clear();
                            Enumeration<? extends FileObject> todo = rootFo.getData(true);
                            cont = new FileObjectContinuation (todo,cancel,closed);
                            return cont.execute();
                        }
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
                    cont = new FolderContinuation (todo, path, cancel,closed);
                    return cont.execute();
                }
            }
            else {
                FileObject rootFo =  URLMapper.findFileObject(root);
                if (rootFo != null) {
                    index.clear();
                    Enumeration<? extends FileObject> todo = rootFo.getData(true);
                    cont = new FileObjectContinuation (todo,cancel,closed);
                    return cont.execute();
                }
            }
            return Result.FINISHED;
        } finally {
            LowMemoryNotifier.getDefault().removeLowMemoryListener(BinaryAnalyser.this);
        }                
    }
    
    public Result resume () throws IOException {
        assert cont != null;
        return cont.execute();
    }
    
    
    public long finish () throws IOException {
        long time = 0;
        if (cont != null) {
            time = cont.finish();
            cont = null;
        }
        long startTime = System.currentTimeMillis();
        try {
            store();
        } finally {
            long endTime =System.currentTimeMillis();            
            time += (endTime-startTime);
        }
        return time;
    }
    
    public void clear () throws IOException {
        if (cont != null) {
            cont.finish();
            cont = null;
        }
        index.clear();
    }
    
    
        /** Analyses a folder 
     *  @param folder to analyze
     *  @param rootURL the url of root, it would be nicer to pass an URL type,
     *  but the {@link URL#toExternalForm} from some strange reason does not cache result,
     *  the String type is faster.
     */
    private Result analyseFolder (final LinkedList<File>  todo, final String rootPath, final AtomicBoolean cancel, AtomicBoolean closed) throws IOException {
        while (!todo.isEmpty()) {
            File file = todo.removeFirst();
            if (file.isDirectory() && file.canRead()) {
                File[] c = file.listFiles();
                if (c!= null) {
                    todo.addAll(Arrays.asList (c));
                }
            }
            else if (this.accepts(file.getName())) {
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
                if (this.accepts(file.getName()) && !isUpToDate (relativePath, fileMTime)) {
                    this.toDelete.add(Pair.<String,String>of (relativePath,null));
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
                    if (this.lowMemory.getAndSet(false)) {
                        this.store();
                    }
                }
            }
            if (cancel.getAndSet(false)) {
                this.store();
                return Result.CANCELED;
            }
            if (closed.get()) {
                return Result.CLOSED;
            }
        }
        return Result.FINISHED;
    }
    
        //Private helper methods
    /** Analyses a zip file */
    private Result analyseArchive (final ZipFile zipFile, final Enumeration<? extends ZipEntry> e, AtomicBoolean cancel, AtomicBoolean closed) throws IOException {
        while(e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry)e.nextElement();
            if ( !ze.isDirectory()  && this.accepts(ze.getName()))  {
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
                if (this.lowMemory.getAndSet(false)) {
                    this.store();
                }
            }
            if (cancel.getAndSet(false)) {
                this.store();
                return Result.CANCELED;
            }
            if (closed.get()) {
                return Result.CLOSED;
            }
        }
        return Result.FINISHED;
    }
    
    private Result analyseFileObjects (final Enumeration<? extends FileObject> todo, final AtomicBoolean cancel, final AtomicBoolean closed) throws IOException {
        while (todo.hasMoreElements()) {            
            FileObject fo = todo.nextElement();            
            if (this.accepts(fo.getName())) {
                InputStream in = new BufferedInputStream (fo.getInputStream());
                try {
                    analyse (in);
                } catch (InvalidClassFormatException icf) {
                    LOGGER.warning("Invalid class file format: "+FileUtil.getFileDisplayName(fo));      //NOI18N
                }
                finally {
                    in.close();
                }
                if (this.lowMemory.getAndSet(false)) {
                    this.store();
                }
            }
            if (cancel.getAndSet(false)) {
                this.store();
                return Result.CANCELED;
            }
            if (closed.get()) {
                return Result.CLOSED;
            }
        }
        return Result.FINISHED;
    }
    
    //Cleans up usages of deleted class
    private final void delete (final String className) throws IOException {
        assert className != null;
        if (!this.index.isValid(false)) {
            return;
        }
        this.toDelete.add(Pair.<String,String>of(className,null));
    }
    
    public void lowMemory (final LowMemoryEvent event) {
        this.lowMemory.set(true);
    }

    // Implementation of StreamAnalyser ----------------------------------------           
    
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
    
    
    //Private methods
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

    // Static private methods ---------------------------------------------------------          
    
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
                final JavacTaskImpl jt = JavacParser.createJavacTask(cpInfo, new DevNullDiagnosticListener(), null,null);
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
    
    
    private static abstract class Continuation {
        
        private long time;
        
        protected abstract Result doExecute () throws IOException;
        protected abstract void doFinish () throws IOException;
        
        public final Result execute () throws IOException {
            final long startTime = System.currentTimeMillis();
            try {
                return doExecute();
            } finally {
                final long endTime = System.currentTimeMillis();
                time += (endTime - startTime);
            }
        }
                
        public final long finish () throws IOException {
            doFinish();  
            return time;
        }        
    }
    
    private class ZipContinuation extends Continuation {
        
        private final ZipFile zipFile;
        private final Enumeration<? extends ZipEntry> entries;
        private final AtomicBoolean cancel;
        private final AtomicBoolean closed;
        
        public ZipContinuation (final ZipFile zipFile, final Enumeration<? extends ZipEntry> entries, final AtomicBoolean cancel, final AtomicBoolean closed) {
            assert zipFile != null;
            assert entries != null;
            assert cancel != null;
            this.zipFile = zipFile;
            this.entries = entries;
            this.cancel = cancel;
            this.closed = closed;
        }
        
        protected Result doExecute () throws IOException {
            return analyseArchive(zipFile, entries, cancel, closed);
        }
        
        protected void doFinish () throws IOException {
            this.zipFile.close();
        }
    }
    
    private class FolderContinuation extends Continuation {
        
        private final LinkedList<File> todo;
        private final String rootPath;
        private final AtomicBoolean cancel;
        private final AtomicBoolean closed;
        
        public FolderContinuation (final LinkedList<File> todo, final String rootPath, final AtomicBoolean cancel, final AtomicBoolean closed) {
            assert todo != null;
            assert rootPath != null;
            assert cancel != null;
            this.todo = todo;
            this.rootPath = rootPath;
            this.cancel = cancel;
            this.closed = closed;
        }
        
        public Result doExecute () throws IOException {            
            return analyseFolder(todo, rootPath, cancel, closed);
        }
        
        public void doFinish () throws IOException {                        
        }        
    }
    
    private class FileObjectContinuation extends  Continuation {
        
        private final Enumeration<? extends FileObject> todo;
        private final AtomicBoolean cancel;
        private final AtomicBoolean closed;
        
        public FileObjectContinuation (final Enumeration<? extends FileObject> todo, final AtomicBoolean cancel, final AtomicBoolean closed) {
            assert todo != null;
            assert cancel != null;
            this.todo = todo;
            this.cancel = cancel;
            this.closed = closed;
        }
        
        public Result doExecute () throws IOException {
            return analyseFileObjects(todo, cancel, closed);
        }
        
        public void doFinish () throws IOException {
            
        }        
    }
    
}

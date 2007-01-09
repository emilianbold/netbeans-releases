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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.usages;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.classfile.Access;
import org.netbeans.modules.classfile.CPClassInfo;
import org.netbeans.modules.classfile.CPFieldInfo;
import org.netbeans.modules.classfile.CPInterfaceMethodInfo;
import org.netbeans.modules.classfile.CPMethodInfo;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.ConstantPool;
import org.netbeans.modules.classfile.InvalidClassFormatException;
import org.netbeans.modules.classfile.LocalVariableTableEntry;
import org.netbeans.modules.classfile.LocalVariableTypeTableEntry;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.Variable;
import org.netbeans.modules.classfile.Parameter;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.util.LowMemoryEvent;
import org.netbeans.modules.java.source.util.LowMemoryListener;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;




/**
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class BinaryAnalyser implements LowMemoryListener {
    
    static final String OBJECT = Object.class.getName();                        
    
    private final Index index;
    private final Map<String,List<String>> refs = new HashMap<String,List<String>>();
    private final Set<String> toDelete = new HashSet<String> ();
    private final AtomicBoolean lowMemory;
    private boolean cacheCleared;

    public BinaryAnalyser (Index index) {
       assert index != null;
       this.index = index;
       this.lowMemory = new AtomicBoolean (false);
    }
    
        /** Analyses a classpath root.
     * @param URL the classpath root, either a folder or an archive file.
     *     
     */
    public final void analyse (final File root, final ProgressHandle handle) throws IOException, IllegalArgumentException  {
        assert root != null;        
            ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void> () {
                public Void run () throws IOException {
                LowMemoryNotifier.getDefault().addLowMemoryListener (BinaryAnalyser.this);
                try {
                    if (root.isDirectory()) {        //NOI18N                    
                        String path = root.getAbsolutePath ();
                        if (path.charAt(path.length()-1) != File.separatorChar) {
                            path = path + File.separatorChar;
                        }                    
                        cacheCleared = false;
                        analyseFolder(root, path);                    
                    }
                    else {
                        if (root.exists() && root.canRead()) {
                            if (!isUpToDate(null,root.lastModified())) {
                                index.clear();
                                if (handle != null) { //Tests don't provide handle
                                    handle.setDisplayName (String.format(NbBundle.getMessage(RepositoryUpdater.class,"MSG_Analyzing"),root.getAbsolutePath()));
                                }
                                final ZipFile zipFile = new ZipFile(root);
                                try {
                                    analyseArchive( zipFile );
                                }
                                finally {
                                    zipFile.close();
                                }
                            }
                        }
                    }
                } finally {
                    LowMemoryNotifier.getDefault().removeLowMemoryListener(BinaryAnalyser.this);
                }
                store();
                return null;
            }});
    }        
    
        /** Analyses a folder 
     *  @param folder to analyze
     *  @param rootURL the url of root, it would be nicer to pass an URL type,
     *  but the {@link URL#toExternalForm} from some strange reason does not cache result,
     *  the String type is faster.
     */
    private void analyseFolder (final File  folder, final String rootPath) throws IOException {
        if (folder.exists() && folder.canRead()) {
            File[] children = folder.listFiles();  
            for (File file : children) {
                if (file.isDirectory()) {
                    analyseFolder(file, rootPath);
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
                    if (!isUpToDate (relativePath, fileMTime)) {
                        if (!cacheCleared) {
                            this.index.clear();                            
                            cacheCleared = true;
                        }
                        InputStream in = new BufferedInputStream (new FileInputStream (file));
                        try {
                            analyse (in);
                        } catch (InvalidClassFormatException icf) {
                            Logger.getLogger(BinaryAnalyser.class.getName()).info("Invalid class file format: "+file.getAbsolutePath());      //NOI18N
                        }
                        if (this.lowMemory.getAndSet(false)) {
                            this.store();
                        }
                    }
                }
            }
        }
    }
    
        //Private helper methods
    /** Analyses a zip file */
    private void analyseArchive ( ZipFile zipFile ) throws IOException {        
        for( Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
            ZipEntry ze = (ZipEntry)e.nextElement();
            if ( !ze.isDirectory()  && this.accepts(ze.getName()))  {
                try {
                    analyse( zipFile.getInputStream( ze ) );
                } catch (InvalidClassFormatException icf) {
                    Logger.getLogger(BinaryAnalyser.class.getName()).info("Invalid class file format: "+ new File(zipFile.getName()).toURI() + "!/" + ze.getName());     //NOI18N
                } catch (IOException x) {
                    Exceptions.attachMessage(x, "While scanning: " + ze.getName());                                         //NOI18N
                    throw x;
                }
                if (this.lowMemory.getAndSet(false)) {
                    this.store();
                }
            }
        }        
    }    
    
    //Cleans up usages of deleted class
    private final void delete (final String className) throws IOException {
        assert className != null;
        if (!this.index.isValid(false)) {
            return;
        }
        this.toDelete.add(className);
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
    
    private void analyse (InputStream inputStream ) throws IOException {
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
        final List <String> references = getClassReferences (classNameType);
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
                Logger.getLogger("global").log (Level.INFO, message.toString());    //NOI18N
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
                    Logger.getLogger("global").warning("Invalid method signature: "+className+"::"+method.getName()+" signature is:" + jvmTypeId);  // NOI18N
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
                        Logger.getLogger("global").warning("Invalid local variable signature: "+className+"::"+method.getName());  // NOI18N
                    }
                }
            }
        }                                    
        //7. Add Filed Type References                        
        Collection<Variable> vars = classFile.getVariables();
        for (Variable var : vars) {
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
                    Logger.getLogger("global").warning("Invalid field signature: "+className+"::"+var.getName()+" signature is: "+jvmTypeId);  // NOI18N
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
        
        return usages;
    }        
    
    private List<String> getClassReferences (final String className) {
        assert className != null;
        List<String> cr = this.refs.get (className);
        if (cr == null) {
            cr = new ArrayList<String> ();
            this.refs.put (className, cr);
        }
        return cr;
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
}

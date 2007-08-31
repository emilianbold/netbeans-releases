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

import java.io.IOException;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class ResultConvertor<T> {

    public abstract T convert (ElementKind kind, String value);    


    public static ResultConvertor<FileObject> fileObjectConvertor (final FileObject... roots) {
        assert roots != null;
        return new FileObjectConvertor (roots);
    }
    
    public static ResultConvertor<ElementHandle<TypeElement>> elementHandleConvertor () {
        return new ElementHandleConvertor ();
    }
    
    public static ResultConvertor<String> identityConvertor () {
        return new IdentityConvertor ();
    }
    
    
    private static class FileObjectConvertor extends ResultConvertor<FileObject> {                
        
        private FileObject[] roots;
        
        private FileObjectConvertor (final FileObject... roots) {
            this.roots = roots;
        }
        
        public FileObject convert (ElementKind kind, String value) {
            for (FileObject root : roots) {
                FileObject result = resolveFile (root, value);
                if (result != null) {
                    return result;
                }
            }
            ClassIndexManager cim = ClassIndexManager.getDefault();
            for (FileObject root : roots ) {
                try {
                    ClassIndexImpl impl = cim.getUsagesQuery(root.getURL());
                    if (impl != null) {
                        String sourceName = impl.getSourceName(value);
                        if (sourceName != null) {
                            FileObject result = root.getFileObject(sourceName);
                            if (result != null) {
                                return result;
                            }
                        }
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }                
            }
            return null;
        }
        
        private static FileObject resolveFile (final FileObject root, String classBinaryName) {
            assert classBinaryName != null;
            classBinaryName = classBinaryName.replace('.', '/');    //NOI18N
            int index = classBinaryName.lastIndexOf('/');           //NOI18N
            FileObject folder;
            String name;
            if (index<0) {
                folder = root;
                name = classBinaryName;
            }
            else {
                assert index>0 : classBinaryName;
                assert index<classBinaryName.length() - 1 : classBinaryName;
                folder = root.getFileObject(classBinaryName.substring(0,index));
                name = classBinaryName.substring(index+1);
            }
            if (folder == null) {
                return null;
            }
            index = name.indexOf('$');                              //NOI18N
            if (index>0) {
                name = name.substring(0, index);
            }
            for (FileObject child : folder.getChildren()) {
                if (FileObjects.JAVA.equalsIgnoreCase(child.getExt()) && name.equals(child.getName())) {
                    return child;
                }
            }
            return null;
        }
    }
    
    private static class ElementHandleConvertor extends ResultConvertor<ElementHandle<TypeElement>> {
        
        public ElementHandle<TypeElement> convert (final ElementKind kind, final String value) {
            return createTypeHandle(kind, value);
        }
        
        @SuppressWarnings ("unchecked") // NOI18N
        private static ElementHandle<TypeElement> createTypeHandle (final ElementKind kind, final String binaryName) {
            assert binaryName != null;
            return ElementHandleAccessor.INSTANCE.create(kind, binaryName);
        }
        
    }
    
    private static class IdentityConvertor extends ResultConvertor<String> {
        
        public String convert (ElementKind kind, String value) {
            return value;
        }
    }
}

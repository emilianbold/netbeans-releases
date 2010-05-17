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

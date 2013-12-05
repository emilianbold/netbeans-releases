/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.ui.Icons;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class JavaSymbolDescriptor extends SymbolDescriptor {
    
    private final String displayName;
    private final ElementHandle<TypeElement> owner;
    private final ElementHandle<?> me;
    private final ElementKind kind;
    private final Set<Modifier> modifiers;
    private final FileObject root;
    private final Project project;
    private final ClassIndexImpl ci;
    private FileObject cachedFo;
    private volatile String cachedPath;

    public JavaSymbolDescriptor (
            @NonNull final String displayName,
            @NonNull final ElementKind kind,
            @NonNull final Set<Modifier> modifiers,
            @NonNull final ElementHandle<TypeElement> owner,
            @NonNull final ElementHandle<?> me,
            @NullAllowed final Project project,
            @NonNull final FileObject root,
            @NonNull final ClassIndexImpl ci) {
        assert displayName != null;
        assert kind != null;
        assert modifiers != null;
        assert owner != null;
        assert me != null;
        assert root != null;
        assert ci != null;
        this.displayName = displayName;        
        this.kind = kind;
        this.modifiers = modifiers;
        this.owner = owner;
        this.me = me;
        this.root = root;
        this.project = project;
        this.ci = ci;
    }
    
    @Override
    public Icon getIcon() {
        return Icons.getElementIcon(kind, modifiers);
    }
        
    @Override
    public String getSymbolName() {
        return displayName;
    }
    
    @Override
    public String getOwnerName() {
        return owner.getQualifiedName();
    }
    

    @Override
    public FileObject getFileObject() {
        if (cachedFo == null) {
            final ClasspathInfo cpInfo = ClasspathInfo.create(ClassPath.EMPTY,
                    ClassPath.EMPTY, ClassPathSupport.createClassPath(root));
            cachedFo = SourceUtils.getFile(owner, cpInfo);
        }
        return cachedFo;
    }

    @Override
    @NonNull
    public String getFileDisplayPath() {
        String res = cachedPath;
        if (res == null) {
            final File rootFile = FileUtil.toFile(root);
            if (rootFile != null) {
                try {
                    final String binaryName = owner.getBinaryName();
                    String relativePath = ci.getSourceName(binaryName);
                    if (relativePath == null) {
                        relativePath = binaryName;
                        int lastDot = relativePath.lastIndexOf('.');    //NOI18N
                        int csIndex = relativePath.indexOf('$', lastDot);     //NOI18N
                        if (csIndex > 0 && csIndex < relativePath.length()-1) {
                            relativePath = binaryName.substring(0, csIndex);
                        }
                        relativePath = String.format(
                            "%s.%s",    //NOI18N
                            FileObjects.convertPackage2Folder(relativePath, File.separatorChar),
                            FileObjects.JAVA);
                    }
                    res = new File (rootFile, relativePath).getAbsolutePath();
                } catch (IOException | InterruptedException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            if (res == null) {
                final FileObject fo = getFileObject();
                res = fo == null ?
                    "" :    //NOI18N
                    FileUtil.getFileDisplayName(fo);
            }
            cachedPath = res;
        }
        return res;
    }



    @Override
    public void open() {
        FileObject file = getFileObject();
        if (file != null) {
	    ClasspathInfo cpInfo = ClasspathInfo.create(file);
	    
	    ElementOpen.open(cpInfo, me);
        }
    }
   
    @Override
    public String getProjectName() {
        final ProjectInformation info = getProjectInfo();
        return info == null ?
            "" :    //NOI18N
            info.getDisplayName();
    }

    @Override
    public Icon getProjectIcon() {
        final ProjectInformation info = getProjectInfo();
        return info == null ?
            null :
            info.getIcon();
    }

    @Override
    public int getOffset() {
        //todo: fixme
        return -1;
    }

    @NonNull
    public ElementKind getElementKind() {
        return kind;
    }

    @NonNull
    public Set<? extends Modifier> getModifiers() {
        return modifiers;
    }
    
    @CheckForNull
    private ProjectInformation getProjectInfo() {
        return project == null ?
            null :
            project.getLookup().lookup(ProjectInformation.class);   //Intentionally does not use ProjectUtils.getInformation() it does project icon annotation which is expensive
    }
    
}

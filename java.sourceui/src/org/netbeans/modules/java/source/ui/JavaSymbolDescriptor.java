/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.ui.Icons;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.filesystems.FileObject;
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
    private FileObject cachedFo;    

    public JavaSymbolDescriptor ( final String displayName,
            final ElementKind kind, final Set<Modifier> modifiers,
            final ElementHandle<TypeElement> owner, final ElementHandle<?> me,
            final Project project, final FileObject root) {        
        assert displayName != null;
        assert kind != null;
        assert modifiers != null;
        assert owner != null;
        assert me != null;
        this.displayName = displayName;        
        this.kind = kind;
        this.modifiers = modifiers;
        this.owner = owner;
        this.me = me;
        this.root = root;
        this.project = project;
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
            final ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new URL[0]),
                    ClassPathSupport.createClassPath(new URL[0]), ClassPathSupport.createClassPath(root));
            cachedFo = SourceUtils.getFile(owner, cpInfo);
        }
        return cachedFo;
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
        return project == null ? "" : ProjectUtils.getInformation(project).getDisplayName();
    }

    @Override
    public Icon getProjectIcon() {
        return project == null ? null : ProjectUtils.getInformation(project).getIcon();
    }

    @Override
    public int getOffset() {
        //todo: fixme
        return -1;
    }
    
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourcePackageInfo;
import org.netbeans.modules.profiler.nbimpl.javac.ClasspathInfoFactory;
import org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx;
import org.netbeans.modules.profiler.nbimpl.javac.JavacClassInfo;
import org.netbeans.modules.profiler.nbimpl.javac.JavacPackageInfo;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.modules.profiler.spi.java.ProfilerTypeUtilsProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
@ProjectServiceProvider(service=ProfilerTypeUtilsProvider.class, projectTypes={
    @ProjectType(id="org-netbeans-modules-java-j2seproject"), // NOI18N
    @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),  // NOI18N
    @ProjectType(id="org-netbeans-modules-j2ee-earproject"),  // NOI18N
    @ProjectType(id="org-netbeans-modules-apisupport-project"), // NOI18N
    @ProjectType(id="org-netbeans-modules-web-project"), // NOI18N
    @ProjectType(id="org-netbeans-modules-ant-freeform", position=1230), // NOI18N, 
    @ProjectType(id="org-netbeans-modules-maven/jar"), // NOI18N
    @ProjectType(id="org-netbeans-modules-maven/war"), // NOI18N
    @ProjectType(id="org-netbeans-modules-maven/ejb"), // NOI18N
    @ProjectType(id="org-netbeans-modules-maven/nbm") // NOI18N
})

public class ProfilerTypeUtilsImpl extends ProfilerTypeUtilsProvider {
    private Project project;
    
    public ProfilerTypeUtilsImpl(Project prj) {
        this.project = prj;
    }
    
    @Override
    public Collection<SourceClassInfo> getMainClasses() {
        List<SourceClassInfo> classes = new ArrayList<SourceClassInfo>();
        FileObject[] srcRoots = ProjectUtilities.getSourceRoots(project, false);
        for(ElementHandle<TypeElement> handle : SourceUtils.getMainClasses(srcRoots)) {
            classes.add(resolveClass(handle.getBinaryName()));
        }
        
        return classes;
    }

    @Override
    public SourceClassInfo resolveClass(final String className) {
        if (project != null) {
            ClasspathInfo cpInfo = ClasspathInfoFactory.infoFor(project);
            ElementHandle<TypeElement> eh = ElementUtilitiesEx.resolveClassByName(className, cpInfo, false);
            return eh != null ? new JavacClassInfo(eh, cpInfo) : null;
        }
        return null;
    }
    
    @Override
    public Collection<SourcePackageInfo> getPackages(boolean subprojects, SourcePackageInfo.Scope scope) {
        Collection<SourcePackageInfo> pkgs = new ArrayList<SourcePackageInfo>();
        
        ClasspathInfo cpInfo = ClasspathInfoFactory.infoFor(project, subprojects, scope == SourcePackageInfo.Scope.SOURCE, scope == SourcePackageInfo.Scope.DEPENDENCIES);
        // #170201: A misconfigured(?) project can have no source roots defined, returning NULL as its ClasspathInfo
        // ignore such a project
        if (cpInfo != null) {
            Set<ClassIndex.SearchScope> sScope = new HashSet<ClassIndex.SearchScope>();
            if (scope == SourcePackageInfo.Scope.SOURCE) {
                sScope.add(ClassIndex.SearchScope.SOURCE);
            }
            if (scope == SourcePackageInfo.Scope.DEPENDENCIES) {
                sScope.add(ClassIndex.SearchScope.DEPENDENCIES);
            }
            for (String pkgName : cpInfo.getClassIndex().getPackageNames("", true, sScope)) { // NOI18N
                pkgs.add(new JavacPackageInfo(cpInfo, pkgName, pkgName, scope));
            }
        }        
        return pkgs;
    }
}
 
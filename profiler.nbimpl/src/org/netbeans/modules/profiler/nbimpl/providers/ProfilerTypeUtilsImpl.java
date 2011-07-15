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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.modules.profiler.spi.java.ProfilerTypeUtilsProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup.Provider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ServiceProvider(service=ProfilerTypeUtilsProvider.class)
public class ProfilerTypeUtilsImpl implements ProfilerTypeUtilsProvider {

    @Override
    public String[] getSubclasses(String className, Provider project) {
        Set<TypeElement> subclasses = ElementUtilitiesEx.getSubclasses(className, project.getLookup().lookup(Project.class));
        int index = 0;
        String[] subclassesNames = new String[subclasses.size()];

        Iterator it = subclasses.iterator();

        while (it.hasNext()) {
            TypeElement subclass = (TypeElement) it.next();
            subclassesNames[index++] = ElementUtilities.getBinaryName(subclass);
        }

        return subclassesNames;
    }

    @Override
    public Collection<String> getMainClasses(Provider project) {
        List<String> classNames = new ArrayList<String>();
        Project nbproject = project.getLookup().lookup(Project.class);
        FileObject[] srcRoots = ProjectUtilities.getSourceRoots(nbproject, false);
        for(ElementHandle<TypeElement> handle : SourceUtils.getMainClasses(srcRoots)) {
            classNames.add(handle.getQualifiedName());
        }
        
        return classNames;
    }

    @Override
    public FileObject findFile(final String className, final Provider project) {
        if (className == null) {
            return null;
        }

        final FileObject[] resolvedFileObject = new FileObject[1];

        final JavaSource js = ElementUtilitiesEx.getSources(project.getLookup().lookup(Project.class));

        try {
            // use the prepared javasource repository and perform a task
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController controller)
                        throws Exception {
                    TypeElement resolvedClass = ElementUtilitiesEx.resolveClassByName(className, controller);

                    if (resolvedClass != null) {
                        resolvedFileObject[0] = org.netbeans.api.java.source.SourceUtils.getFile(ElementHandle.create(resolvedClass),
                                controller.getClasspathInfo());
                    }
                }
            }, false);
        } catch (IOException ex) {
            ProfilerLogger.log(ex);
        }

        return resolvedFileObject[0];
    }
    
    
}

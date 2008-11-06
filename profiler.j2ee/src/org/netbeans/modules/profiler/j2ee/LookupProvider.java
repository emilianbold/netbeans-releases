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
package org.netbeans.modules.profiler.j2ee;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import java.util.List;
import org.netbeans.modules.profiler.categories.Categorization;
import org.netbeans.modules.profiler.categories.CategoryBuilder;
import org.netbeans.modules.profiler.projectsupport.AbstractProjectLookupProvider;

/**
 *
 * @author Jiri Sedlacek
 */
public class LookupProvider {
    @org.netbeans.spi.project.LookupProvider.Registration(projectType="org-netbeans-modules-web-project")
    public static class WebProject extends AbstractProjectLookupProvider {
        @Override
        protected List getAdditionalLookups(final Project project) {
            return new ArrayList() {
            {
                add(new Categorization(project, new CategoryBuilder(project, "org-netbeans-modules-web-project"))); // NOI18N
            }
        };
        }
        
    }
    
    @org.netbeans.spi.project.LookupProvider.Registration(projectType="org-netbeans-modules-j2ee-earproject")
    public static class EarProject extends AbstractProjectLookupProvider {
        @Override
        protected List getAdditionalLookups(final Project project) {
            return new ArrayList() {
            {
                Collection<CategoryBuilder> builders = new ArrayList<CategoryBuilder>();
                builders.add(new CategoryBuilder(project, "org-netbeans-modules-j2ee-ejbjarproject")); // NOI18N
                builders.add(new CategoryBuilder(project, "org-netbeans-modules-web-project")); // NOI18N
                add(new Categorization(project, builders)); // NOI18N
            }
        };
        }
        
    }
    
    @org.netbeans.spi.project.LookupProvider.Registration(projectType="org-netbeans-modules-j2ee-ejbjarproject")
    public static class EjbJarProject extends AbstractProjectLookupProvider {
        @Override
        protected List getAdditionalLookups(final Project project) {
            return new ArrayList() {
            {
                add(new Categorization(project, new CategoryBuilder(project, "org-netbeans-modules-j2ee-ejbjarproject"))); // NOI18N
            }
        };
        }
        
    }
}

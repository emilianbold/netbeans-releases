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

package org.netbeans.modules.spring.beans;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Andrei Badea
 */
public class ProjectLookupProvider implements LookupProvider {

    private final Kind kind;
    
    @LookupProvider.Registration(projectType={
        "org-netbeans-modules-java-j2seproject",
        "org-netbeans-modules-j2ee-ejbjarproject"
    })
    public static ProjectLookupProvider standard() {
        return new ProjectLookupProvider(Kind.NON_WEB);
    }

    @LookupProvider.Registration(projectType="org-netbeans-modules-web-project")
    public static ProjectLookupProvider web() {
        return new ProjectLookupProvider(Kind.WEB);
    }

    @LookupProvider.Registration(projectType="org-netbeans-modules-maven")
    public static ProjectLookupProvider simple() {
        return new ProjectLookupProvider(Kind.SIMPLE);
    }

    private ProjectLookupProvider(Kind kind) {
        this.kind = kind;
    }

    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project project = baseContext.lookup(Project.class);
        if (project == null) {
            throw new IllegalStateException("Lookup " + baseContext + " does not contain a Project");
        }
        List<Object> instances = new ArrayList<Object>(3);
        instances.add(new ProjectSpringScopeProvider(project));
        if (kind != Kind.SIMPLE) {
            instances.add(new RecommendedTemplatesImpl(kind == Kind.WEB));
            instances.add(new SpringConfigFileLocationProviderImpl(project));
        }
        return Lookups.fixed(instances.toArray(new Object[instances.size()]));
    }

    enum Kind {

        // For most projects.
        NON_WEB,

        // For web projects, whose config file providers are provided by the Web MVC support
        // (since it needs to use the WebModule API).
        WEB,

        // For Maven projects, which implement everything.
        SIMPLE
    }
}

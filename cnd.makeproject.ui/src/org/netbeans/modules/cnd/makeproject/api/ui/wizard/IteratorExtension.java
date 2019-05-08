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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.api.ui.wizard;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 */
public interface IteratorExtension {

    /**
     * Method discover additional project artifacts by folder or binary file
     * 
     * @param map input/output map
     */
    void discoverArtifacts(Map<String,Object> map);

    /**
     * Method delegates a project creating to discovery.
     * Instantiate make project in simple mode.
     * 
     * @param wizard
     * @return set of make projects
     * @throws java.io.IOException
     */
    Set<FileObject> createProject(WizardDescriptor wizard) throws IOException;

    /**
     * Method invoke discovery for created project.
     * 
     * @param map input map
     * @param project that will be configured or created
     * @param projectKind fullness of configured project
     */
    void discoverProject(Map<String,Object> map, Project project, ProjectKind projectKind);
    
    /**
     * Adds headers items in the project, changes exclude/include state of headers items
     * according to code model. Returns immediately, listens until parse in done,
     * tunes project on parse finish.
     * 
     * @param project 
     */
    void discoverHeadersByModel(Project project);

    /**
     * Method disable code model for project
     * 
     * @param project
     */
    void disableModel(Project project);

    public enum ProjectKind {
        Minimal, // include in project com
        IncludeDependencies,
        CreateDependencies
    }
}

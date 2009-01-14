/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.spi;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.w3c.dom.Document;

/**
 * Service representing the ability to create Hudson jobs for local projects.
 */
public interface ProjectHudsonJobCreator {

    /**
     * Checks whether this creator can handle a given project.
     * Should return as quickly as possible, i.e. just check basic project type.
     * @param project a local project
     * @return true if this project type is suitable
     */
    boolean canHandle(Project project);

    /**
     * Produces a suggested Hudson job name for a project.
     * This might for example use {@link ProjectInformation#getName}.
     * The actual job which gets created might have a uniquified name.
     * @param project a local project for which {@link #canHandle} is true
     * @return a proposed code name for the project as a Hudson job
     */
    String jobName(Project project);

    /**
     * Provides the desired initial configuration for a project.
     * Should only be called in case {@link #canHandle} is true.
     * @param project a local project for which {@link #canHandle} is true
     * @param configXml a document initially consisting of just {@code <project/>}
     *                  to be populated with subelements
     *                  following the format of {@code ${workdir}/jobs/${projname}/config.xml}
     * @throws IOException in case project metadata cannot be read or is malformed
     */
    void configure(Project project, Document configXml) throws IOException;

}

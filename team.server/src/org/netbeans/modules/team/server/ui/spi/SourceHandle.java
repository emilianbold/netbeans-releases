/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.team.server.ui.spi;

import java.io.File;
import org.netbeans.modules.team.ide.spi.IDEProject;

/**
 * Abstraction for a single source repository (a line in 'Sources' section).
 *
 * @author S. Aubrecht
 */
public abstract class SourceHandle {

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    /**
     *
     * @return True if 'get' link is available, false to render the repository as disabled.
     */
    public abstract boolean isSupported();

    /**
     * Returns SCM feature's name, deduced from the Teams server response.<br><br>
     * e.g the usage should be with KenaiService.Names.*, such as:<br><br>
     * <pre>
     * // source is an instance of SourceHandle...
     * String featureName = source.getScmFeatureName();
     * if (featureName.equals(KenaiService.Names.SUBVERSION)) { ... }
     * </pre>
     * @return the name of the issue tracking feature
     */
    public abstract String getScmFeatureName();

    /**
     * getter for max 5 recent projects
     * @return
     */
    public abstract Iterable<IDEProject> getRecentProjects();

    /**
     * getter for last checked out working directory
     * can return null
     * @return
     */
    public abstract File getWorkingDirectory();
}

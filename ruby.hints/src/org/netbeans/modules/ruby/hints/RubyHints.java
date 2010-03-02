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

package org.netbeans.modules.ruby.hints;

import org.jrubyparser.ast.Node;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Utility methods for hints.
 *
 * @author Erno Mononen
 */
public abstract class RubyHints {

    private static final String RAILS_PROJECT = "RailsProject"; //NOI18N

    private RubyHints() {
    }

    /**
     * Checks whether the given <code>node</code> is <code>null</code> or
     * represents an <code>InvisibleNode</code>.
     *
     * @param node
     * @return true if the node was null or invisible.
     */
    static boolean isNullOrInvisible(Node node) {
        return node == null || node.isInvisible();
    }

    static boolean isInRailsProject(FileObject file) {
        return isRailsProject(FileOwnerQuery.getOwner(file));
    }

    static boolean isInRails3Project(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        // assumes that the presence of script/rails means it is a rails 3 project
        FileObject railsScript = project.getProjectDirectory().getFileObject("script/rails");
        return isRailsProject(project) && railsScript != null && railsScript.isValid();
    }

    private static boolean isRailsProject(Project project) {
        // Ugly!!
        if (project == null || project.getClass().getName().indexOf(RAILS_PROJECT) == -1) { // NOI18N
            return false;
        }
        return true;
    }

}

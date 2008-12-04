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

package org.netbeans.nbbuild;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

/**
 * Converts cluster.path from value specified in platform.properties
 * to value usable by harness.
 *
 * TODO - describe actual changes
 *
 * @author Richard Michalsky
 */
public class ConvertClusterPath extends Task {
    private String from;
    private String id;
    private String basedir;

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConvertClusterPath() {

    }

    @Override
    public void execute() throws BuildException {
        try {
            if (from == null || from.length() == 0)
                throw new BuildException("From parameter not specified.");
            if (id == null || id.length() == 0)
                throw new BuildException("Id for converted path not specified.");
            if (basedir == null || basedir.length() == 0)
                basedir = getProject().getBaseDir().getAbsolutePath();

            log("Converting cluster.path from '" + from + "' relative to '" + basedir + "'.", Project.MSG_VERBOSE);
            FileUtils fu = FileUtils.getFileUtils();
            Project fakeproj = new Project();
            fakeproj.setBasedir(basedir);
            Path convPath = new Path(fakeproj, from);
            log("Converted path: '" + convPath.toString() + "'.", Project.MSG_VERBOSE);
            getProject().addReference(id, convPath);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }


}

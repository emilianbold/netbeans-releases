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

package org.netbeans.modules.project.ant;

import java.io.File;
import java.net.URI;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.queries.CollocationQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * A CollocationQueryImplementation implementation that collocates files based on
 * projects they are in.
 * @author Milos Kleint
 * @since org.netbeans.modules.project.ant/1 1.18
 * 
 * TODO should this class move to project.api module? Som that the behaviour stays
 * even if ant based projects are disabled or missing
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.CollocationQueryImplementation.class, position=500)
public class FileOwnerCollocationQueryImpl implements CollocationQueryImplementation {


    /** Creates a new instance of FileOwnerCollocationQueryImpl */
    public FileOwnerCollocationQueryImpl() {
    }

    public File findRoot(File file) {
        File f = file;
        URI uri = f.toURI();
        Project prj = FileOwnerQuery.getOwner(uri);
        if (prj == null) {
            return null;
        }
        File parentF = f;
        while (prj != null && parentF != null) {
            f = parentF;
            parentF = parentF.getParentFile();
            if (parentF != null) {
                prj = FileOwnerQuery.getOwner(parentF.toURI());
            } else {
                prj = null;
            }
        }
        return f;
        
    }

    public boolean areCollocated(File file1, File file2) {
        File root = findRoot (file1);
        boolean first = true;
        if (root == null) {
            root = findRoot (file2);
            first = false;
        }
        if (root != null) {
            String rootpath = root.getAbsolutePath() + File.separator;
            String check = (first ? file2.getAbsolutePath() : file1.getAbsolutePath()) + File.separator;
            return check.startsWith(rootpath);
        }
        return false;
    }



}

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

package org.netbeans.modules.queries;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.queries.CollocationQueryImplementation;

/**
 * The CollocationQueryImplementation which makes all the paths in the single tree
 * relative. On the UNIX all the files lies within a single tree, all the files
 * are collocated. On the Windows (the VMS) all the files from single disk (volume) are collocated,
 * files lying on the different disks (volumes) are not collocated.
 * @author Tomas Zezula
 */
public class AlwaysRelativeCollocationQuery implements CollocationQueryImplementation {

    private File[] roots;

    /** Creates a new instance of AlwaysRelativeCollocationQuery */
    public AlwaysRelativeCollocationQuery() {
    }

    public File findRoot(File file) {
        final File[] roots = getFileSystemRoots ();
        if (roots.length == 0) {
            assert false : "Cannot find filesystem roots";
            return null;
        }
        else if (roots.length == 1) {
            //On UNIX always relative
            return roots[0];
        }
        else {
            final Set<File> rootsSet = new HashSet<File>(Arrays.asList(this.roots != null ? this.roots : roots));
            return getRoot (file, rootsSet);
        }
    }

    public boolean areCollocated(File file1, File file2) {
        File root1 = findRoot (file1);
        File root2 = findRoot (file2);
        return root1 != null && root1.equals(root2);
    }

    // ---------------- Unit test helper methods -----------------------

    private File[] getFileSystemRoots () {
        if (this.roots != null) {
            return this.roots;
        }
        else {
            return File.listRoots();
        }
    }

    private File getRoot(File f, final Set<File> roots) {
        //We have to compare the file to File.listRoots(),
        //the test file.getParent() == null does not work on Windows
        //when the file was selected from the JFileChooser and user browsed
        //through the "This Computer" node
        while (f != null && !roots.contains(f)) {
            f = f.getParentFile();
        }
        return f;
    }

    final void setFileSystemRoots (File[] roots) {
        this.roots = roots;
    }

}

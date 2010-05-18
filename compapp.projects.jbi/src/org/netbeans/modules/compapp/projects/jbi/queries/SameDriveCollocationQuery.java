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

package org.netbeans.modules.compapp.projects.jbi.queries;

import org.netbeans.spi.queries.CollocationQueryImplementation;

import java.io.File;

/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class SameDriveCollocationQuery implements CollocationQueryImplementation {
    /**
     * Default constructor for lookup.
     */
    public SameDriveCollocationQuery() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param file1 DOCUMENT ME!
     * @param file2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean areCollocated(File file1, File file2) {
        if (file1.equals(file2)) {
            return false;
        }

        String f1 = file1.getAbsolutePath();
        String f2 = file2.getAbsolutePath();
        int idx1 = f1.indexOf(':'); // NOI18N
        int idx2 = f2.indexOf(':'); // NOI18N

        if ((idx1 > 0) && (idx1 == idx2)) {
            return (f1.substring(0, idx1).compareToIgnoreCase(f2.substring(0, idx2)) == 0);
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public File findRoot(File file) {
        return null;
    }
}

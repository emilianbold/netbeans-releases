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

package org.netbeans.bluej;

import java.net.URL;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author mkleint
 */
public class BluejUnitTestForSourceQuery implements MultipleRootsUnitTestForSourceQueryImplementation {

    private BluejProject project;
    
    /** Creates a new instance of BluejUnitTestForSourceQuery */
    public BluejUnitTestForSourceQuery(BluejProject proj) {
        project = proj;
    }

    /**
     * Returns the test roots for a given source root.
     *
     * @param source a Java package root with sources
     * @return a corresponding Java package roots with unit tests. The
     *     returned URLs need not point to an existing folder. It can be null
     *     when no mapping from source to unit test is known.
     */
    public URL[] findUnitTests(FileObject source) {
        return new URL[] { URLMapper.findURL(project.getProjectDirectory(), URLMapper.EXTERNAL) }; 
    }

    /**
     * Returns the source roots for a given test root.
     *
     * @param unitTest a Java package roots with unit tests
     * @return a corresponding Java package roots with sources. It can be null
     *     when no mapping from unit test to source is known.
     */
    public URL[] findSources(FileObject unitTest) {
        return new URL[] { URLMapper.findURL(project.getProjectDirectory(), URLMapper.EXTERNAL) }; 
    }
    
}

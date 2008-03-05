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

package org.netbeans.modules.gsfpath.spi.classpath;

import java.net.URL;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.classpath.ClassPathAccessor;
import org.openide.filesystems.FileUtil;

/**
 * Most general way to create {@link ClassPath} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link ClassPathImplementation} and use this factory.
 * See also {@link org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport}
 * for easier ways to create classpaths.
 * @since org.netbeans.modules.gsfpath.api/1 1.4
 */
public final class ClassPathFactory {

    private ClassPathFactory() {
    }

    /**
     * Create API classpath instance for the given SPI classpath.
     * @param spiClasspath instance of SPI classpath
     * @return instance of API classpath
     */
    public static ClassPath createClassPath(ClassPathImplementation spiClasspath) {
//        assert checkEntries (spiClasspath) : "ClassPathImplementation contains invalid root: " + spiClasspath.toString();    //Commented, not to decrease the performance even in the dev build.
        return ClassPathAccessor.DEFAULT.createClassPath(spiClasspath);
    }
    
    
    private static boolean checkEntries (ClassPathImplementation spiClasspath) {
        for (PathResourceImplementation impl : spiClasspath.getResources()) {
            URL[] roots = impl.getRoots();
            for (URL root : roots) {
                if (FileUtil.isArchiveFile(root)) {
                    return false;
                }
                if (root.toExternalForm().endsWith("/")) {  // NOI18N
                    return false;
                }
            }
        }
        return true;
    }

}

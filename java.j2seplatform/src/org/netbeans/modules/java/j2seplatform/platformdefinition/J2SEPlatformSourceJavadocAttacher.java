/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=SourceJavadocAttacherImplementation.class,position=150)
public class J2SEPlatformSourceJavadocAttacher implements SourceJavadocAttacherImplementation {

    @Override
    public Result attachSources(final URL root) throws IOException {
        return attach(root, J2SEPlatformCustomizer.SOURCES);
    }

    @Override
    public Result attachJavadoc(final URL root) throws IOException {
        return attach(root, J2SEPlatformCustomizer.JAVADOC);
    }

    private Result attach(final URL root, int mode) {
        final J2SEPlatformImpl platform = findOwner(root);
        if (platform == null) {
            return Result.UNSUPPORTED;
        }
        final J2SEPlatformCustomizer.PathModel model = new J2SEPlatformCustomizer.PathModel(platform, mode);
        if (J2SEPlatformCustomizer.select(model,new File[1],null)) {
            return Result.ATTACHED;
        }
        return Result.CANCELED;
    }

    private J2SEPlatformImpl findOwner(final URL root) {
        for (JavaPlatform p : JavaPlatformManager.getDefault().getPlatforms(null, new Specification(J2SEPlatformImpl.PLATFORM_J2SE, null))) {
            if (!(p instanceof J2SEPlatformImpl)) {
                //Cannot handle unknown platform
                continue;
            }
            final J2SEPlatformImpl j2sep = (J2SEPlatformImpl) p;
            if (j2sep.isBroken()) {
                continue;
            }
            for (ClassPath.Entry entry : j2sep.getBootstrapLibraries().entries()) {
                if (root.equals(entry.getURL())) {
                    return j2sep;
                }
            }
        }
        return null;
    }

}

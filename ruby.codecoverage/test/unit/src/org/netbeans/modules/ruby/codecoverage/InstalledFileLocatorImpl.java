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
package org.netbeans.modules.ruby.codecoverage;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.ruby.platform.TestUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

@org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class)
public final class InstalledFileLocatorImpl extends InstalledFileLocator {

    public InstalledFileLocatorImpl() {
    }

    public @Override File locate( String relativePath, String codeNameBase, boolean localized) {
        if (relativePath.equals("modules/org-netbeans-modules-ruby-project.jar")) {
            return new File(TestUtil.getXTestJRubyHome().getParentFile(), relativePath.replace("/", File.separator));
        } else if ("coverage/rcov_wrapper.rb".equals(relativePath) || "coverage/rake_wrapper.rb".equals(relativePath)) {
            try {
                return new File(TestUtil.getXTestJRubyHome().getParentFile(), relativePath.replace("/", File.separator)).getCanonicalFile();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                throw new RuntimeException("Couldn't find coverage files");
            }
        } else if (relativePath.equals("rake_tasks_info.rb")) {
            String script = System.getProperty("xtest.rake_tasks_info.rb");
            if (script == null) {
                throw new RuntimeException("xtest.rake_tasks_info.rb property has to be set when running within binary distribution");
            }
            return new File(script);
        }
        return null;
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.api.ruby.platform;

import java.io.File;
import org.openide.modules.InstalledFileLocator;

@org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class)
public final class InstalledFileLocatorImpl extends InstalledFileLocator {

    public InstalledFileLocatorImpl() {
    }

    public @Override File locate( String relativePath, String codeNameBase, boolean localized) {
        if (relativePath.equals("ruby/debug-commons-0.9.5/classic-debug.rb")) {
            File rubydebugDir = RubyTestBase.getDirectory("rubydebug.dir", true);
            File cd = new File(rubydebugDir, "classic-debug.rb");
            if (!cd.isFile()) {
                throw new RuntimeException("classic-debug found in " + rubydebugDir);
            }
            return cd;
        } else if (relativePath.equals("jruby-1.3.1")) {
            return TestUtil.getXTestJRubyHome();
        } else if (relativePath.equals("platform_info.rb")) {
            String script = System.getProperty("xtest.platform_info.rb");
            if (script == null) {
                throw new RuntimeException("xtest.platform_info.rb property has to be set when running within binary distribution");
            }
            return new File(script);
        } else {
            return null;
        }
    }
    
}

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

package org.netbeans.modules.ide.ergonomics;

import java.lang.reflect.Method;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ProjectFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class VerifyFullIDETest extends NbTestCase {
    public VerifyFullIDETest(String n) {
        super(n);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(VerifyFullIDETest.class).
            gui(false).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules(".*")
        );
    }

    public void testGetAllProjectFactories() throws Exception {
        iterateRegistrations(ProjectFactory.class, null);
    }

    public void testGetAllAntProjects() throws Exception {
        Class<?> ant = Class.forName(
            "org.netbeans.spi.project.support.ant.AntBasedProjectType",
            true,
            Thread.currentThread().getContextClassLoader()
        );
        iterateRegistrations(ant, ant.getDeclaredMethod("getType"));
    }

    public void testGetAllRakeProjects() throws Exception {
        Class<?> rake = Class.forName(
            "org.netbeans.modules.ruby.spi.project.support.rake.RakeBasedProjectType",
            true,
            Thread.currentThread().getContextClassLoader()
        );
        iterateRegistrations(rake, rake.getDeclaredMethod("getType"));
    }

    private void iterateRegistrations(Class<?> what, Method info) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Object f : Lookup.getDefault().lookupAll(what)) {
            sb.append(f.getClass().getName());
            if (info != null) {
                Object more = info.invoke(f);
                sb.append(" info: ").append(more);
            }
            sb.append('\n');
        }
        fail("Factories:\n" + sb);
    }


}

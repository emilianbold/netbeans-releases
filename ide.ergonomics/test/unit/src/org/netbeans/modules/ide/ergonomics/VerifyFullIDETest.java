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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.Feature2LayerMapping;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.spi.debugger.ui.AttachType;
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
        StringBuilder sb = new StringBuilder();
        Map<String,String> all = Feature2LayerMapping.projectFiles();

        all.put("Fine", "org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton");
        all.put("OK", "org.netbeans.modules.ruby.modules.project.rake.RakeBasedProjectFactorySingleton");
        all.put("skip", "org.netbeans.modules.ide.ergonomics.fod.FeatureProjectFactory");
        all.put("test", "org.netbeans.modules.ide.ergonomics.fod.TestFactory");

        iterateRegistrations(sb, ProjectFactory.class, null, all);

        if (!all.isEmpty()) {
            fail("No all IDE projects are registered for ergonomics mode:\n" + sb);
        }
    }

    public void testGetAllNbProjects() throws Exception {
        Map<String,String> all = Feature2LayerMapping.nbprojectTypes();
        StringBuilder sb = new StringBuilder();

        Class<?> ant = Class.forName(
            "org.netbeans.spi.project.support.ant.AntBasedProjectType",
            true,
            Thread.currentThread().getContextClassLoader()
        );
        iterateRegistrations(sb, ant, ant.getDeclaredMethod("getType"), all);
        Class<?> rake = Class.forName(
            "org.netbeans.modules.ruby.spi.project.support.rake.RakeBasedProjectType",
            true,
            Thread.currentThread().getContextClassLoader()
        );
        iterateRegistrations(sb, rake, rake.getDeclaredMethod("getType"), all);

        if (!all.isEmpty()) {
            fail("No all IDE projects are registered for ergonomics mode:\n" + sb);
        }
    }

    private void iterateRegistrations(
        StringBuilder sb, Class<?> what, Method info, Map<String,String> all
    ) throws Exception {
        for (Object f : Lookup.getDefault().lookupAll(what)) {
            sb.append(f.getClass().getName());
            if (info != null) {
                Object more = info.invoke(f);
                sb.append(" info: ").append(more);
                Object value = all.get(more);
                if (f.getClass().getName().equals(value)) {
                    sb.append(" OK");
                    all.remove(more);
                } else {
                    sb.append(" not present");
                    all.put("FAIL", more.toString());
                }
            } else {
                if (all.values().remove(f.getClass().getName())) {
                    sb.append(" OK");
                } else {
                    all.put("FAIL", f.getClass().getName());
                    sb.append(" not present");
                }
            }
            sb.append('\n');
        }
    }

    public void testGetAllDebuggers() {
       List<String> lazyDebuggers = new ArrayList<String>();
       Iterator<? extends FeatureInfo> it = Feature2LayerMapping.featureTypesLookup().lookupAll(FeatureInfo.class).iterator();
       for (; it.hasNext(); ) {
           FeatureInfo featureInfo = it.next();
           if (featureInfo.getAttachTypeName() != null) {
               lazyDebuggers.add(featureInfo.getAttachTypeName());
           }
       }
       List<String> originalDebuggers = new ArrayList<String>();
       for (Object o : DebuggerManager.getDebuggerManager().lookup(null, AttachType.class)) {
            AttachType attachType = (AttachType) o;
            if (attachType.getTypeDisplayName() != null) {
                originalDebuggers.add(attachType.getTypeDisplayName());
            }
       }
       Collections.sort(lazyDebuggers);
       Collections.sort(originalDebuggers);
//       boolean first = originalDebuggers.containsAll(lazyDebuggers);
//       boolean second = lazyDebuggers.containsAll(originalDebuggers);
       assertEquals("Lists differs!", lazyDebuggers, originalDebuggers);
    }
}

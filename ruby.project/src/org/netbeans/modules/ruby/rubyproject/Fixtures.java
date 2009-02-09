/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rubyproject.RakeParameters.RakeParameter;
import org.netbeans.modules.ruby.rubyproject.rake.RakeTask;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Erno Mononen
 */
public final class Fixtures {

    private static final Logger LOGGER = Logger.getLogger(Fixtures.class.getName());
    private static final List<String> FIXTURE_TASKS = Arrays.asList("db:fixtures:load");//NOI18N

    private Fixtures() {
    }

    /**
     * @return true if the given task represents a fixture task.
     */
    static boolean isFixtureTask(RakeTask task) {
        if (task == null) {
            return false;
        }
        return FIXTURE_TASKS.contains(task.getTask());
    }

    /**
     * @return the fixtures in the given project.
     */
    public static List<Fixture> getFixtures(Project project) {
        FileObject projectDir = project.getProjectDirectory();

        FileObject fixturesDir = projectDir.getFileObject("test/fixtures"); // NOI18N

        if (fixturesDir == null) {
            return Collections.<Fixture>emptyList();
        }

        List<Fixture> fixtures = new ArrayList<Fixture>();

        for (FileObject fo : fixturesDir.getChildren()) {
            String name = fo.getName();
            if (fo.getMIMEType().equals(RubyInstallation.YAML_MIME_TYPE) || fo.isFolder()) {
                fixtures.add(new Fixture(name, fo.isFolder()));
            }
        }

        Collections.sort(fixtures);
        return fixtures;
    }

    /**
     * Represents a single fixture.
     */
    public static class Fixture implements RakeParameter, Comparable<Fixture> {

        /**
         * The name of the fixture.
         */
        private final String name;
        private final boolean dir;

        public Fixture(String name, boolean dir) {
            this.name = name;
            this.dir = dir;
        }

        /**
         * @return this migration in the right format to be passed
         * as a param for a migrate Rake task.
         */
        public String toRakeParam() {
            return dir ? "FIXTURES_DIR=" + name : "FIXTURES=" + name;  //NOI18N
        }

        public int compareTo(Fixture o) {
            return toRakeParam().compareTo(o.toRakeParam());
        }

        @Override
        public String toString() {
            // overriding to make this display correctly in
            // an editable combo box
            return toRakeParam();
        }

    }
}

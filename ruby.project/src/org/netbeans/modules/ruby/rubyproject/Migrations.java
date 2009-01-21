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
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.rubyproject.rake.RakeTask;
import org.openide.filesystems.FileObject;

/**
 * Utility methods for dealing with {@link Migration}s.
 *
 * @author Erno Mononen
 */
public final class Migrations {

    private static final Logger LOGGER = Logger.getLogger(Migrations.class.getName());
    /**
     * The pattern for recognizing sequential migrations, e.g. 001_something.rb.
     */
    private static final Pattern SEQ_PATTERN = Pattern.compile("^\\d\\d\\d_.*"); //NOI18N
    /**
     * The pattern for recognizing UTC timestamp migrations, e.g. 20080825092811_something.rb.
     * (can be used since Rails 2.1).
     */
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d_.*"); //NOI18N

    private static final List<String> MIGRATE_TASKS = Arrays.asList("db:migrate", "db:migrate:down", "db:migrate:up");//NOI18N

    private Migrations() {
    }

    /**
     * @return true if the given task represents a db migration task.
     */
    public static boolean isMigrateTask(RakeTask task) {
        if (task == null) {
            return false;
        }
        return MIGRATE_TASKS.contains(task.getTask());
    }

    /**
     * @return the migrations in the given project.
     */
    public static List<Migration> getMigrations(Project project) {
        FileObject projectDir = project.getProjectDirectory();

        FileObject migrate = projectDir.getFileObject("db/migrate"); // NOI18N

        if (migrate == null) {
            return Collections.<Migration>emptyList();
        }

        List<Migration> result = new ArrayList<Migration>();

        for (FileObject fo : migrate.getChildren()) {
            String name = fo.getName();
            if (fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
                Long version = getMigrationVersion(name);
                String description = getMigrationDescription(name);
                if (version != null && description != null) {
                    result.add(new Migration(version, description));
                } else {
                    // likely not a migration file, so just log a msg
                    LOGGER.finer("Could not parse version and description for: " + name);
                }
            }
        }

        Collections.sort(result);
        return result;
    }

    private static boolean isSequentialMigration(String name) {
        return SEQ_PATTERN.matcher(name).matches();
    }

    private static boolean isTimestampMigration(String name) {
        return TIMESTAMP_PATTERN.matcher(name).matches();
    }

    /**
     * Gets the version of the given migration.
     *
     * @param name the name of a migration file.
     * @return the version, or <code>null</code> if the given
     * <code>name</code> didn't represent a migration file.
     * @see #SEQ_PATTERN
     * @see #TIMESTAMP_PATTERN
     */
    public static Long getMigrationVersion(String name) {
        if (isSequentialMigration(name)) {
            return Long.parseLong(name.substring(0, 3));
        } else if (isTimestampMigration(name)) {
            return Long.parseLong(name.substring(0, 14));
        }
        return null;
    }

    /**
     * Gets the descripion for the given migration.
     *
     * @param name the name of a migration file.
     * @return the description, i.e. the name of the migration class,
     * or <code>null</code> if the given
     * <code>name</code> didn't represent a migration file.
     */
    public static String getMigrationDescription(String name) {
        if (isSequentialMigration(name)) {
            return RubyUtils.underlinedNameToCamel(name.substring(4));
        } else if (isTimestampMigration(name)) {
            return RubyUtils.underlinedNameToCamel(name.substring(15));
        }
        return null;

    }

    /**
     * Represents a single migration.
     */
    public static class Migration implements Comparable<Migration> {

        /**
         * The version of the migration.
         */
        private final Long version;
        /**
         * The description, e.g. CreateUsers.
         */
        private final String description;

        /**
         * Constructs a new migration.
         * 
         * @param version the version of the migration.
         * @param description the description, e.g. CreateProducts.
         */
        public Migration(Long version, String description) {
            this.version = version;
            this.description = description;
        }

        /**
         * @see #description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @see #version
         */
        public Long getVersion() {
            return version;
        }

        /**
         * @return this migration in the right format to be passed
         * as a param for a migrate Rake task.
         */
        public String toRakeParam() {
            return "VERSION=" + getVersion();  //NOI18N
        }

        public String getDisplayName() {
            return toRakeParam() + " (" + getDescription() + ")"; //NOI18N
        }

        public int compareTo(Migration o) {
            return getVersion().compareTo(o.getVersion());
        }

        @Override
        public String toString() {
            // overriding to make this display correctly in 
            // an editable combo box
            return getDisplayName();
        }

    }
}

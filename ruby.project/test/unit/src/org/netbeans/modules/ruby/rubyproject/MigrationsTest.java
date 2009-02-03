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

import junit.framework.TestCase;
import org.netbeans.modules.ruby.rubyproject.rake.RakeTask;

/**
 *
 * @author Erno Mononen
 */
public class MigrationsTest extends TestCase {

    public MigrationsTest() {
    }

    public void testIsMigrateTask() {
        RakeTask dbMigrate = new RakeTask("db:migrate", "", "");
        RakeTask dbMigrateUp = new RakeTask("db:migrate:up", "", "");
        RakeTask dbTestClone = new RakeTask("db:test:clone", "", "");
        RakeTask spec = new RakeTask("spec", "", "");

        assertTrue(Migrations.isMigrateTask(dbMigrate));
        assertTrue(Migrations.isMigrateTask(dbMigrateUp));

        assertFalse(Migrations.isMigrateTask(dbTestClone));
        assertFalse(Migrations.isMigrateTask(spec));

    }

    public void testGetMigrationVersion() {
        String sequential = "001_create_users.rb";
        assertEquals(Long.valueOf("1"), Migrations.getMigrationVersion(sequential));

        String sequential2 = "999_create_users.rb";
        assertEquals(Long.valueOf("999"), Migrations.getMigrationVersion(sequential2));

        String timestamp = "20080825092811_create_users.rb";
        assertEquals(Long.valueOf("20080825092811"), Migrations.getMigrationVersion(timestamp));

        String notMigration = "just_some_file.rb";
        assertNull(Migrations.getMigrationVersion(notMigration));

    }

    public void testGetMigrationDescription() {
        String sequential = "001_create_users.rb";
        assertEquals("CreateUsers.rb", Migrations.getMigrationDescription(sequential));

        String sequential2 = "999_create_users.rb";
        assertEquals("CreateUsers.rb", Migrations.getMigrationDescription(sequential2));

        String timestamp = "20080825092811_create_users_and_groups.rb";
        assertEquals("CreateUsersAndGroups.rb", Migrations.getMigrationDescription(timestamp));

        String notMigration = "just_some_file.rb";
        assertNull(Migrations.getMigrationDescription(notMigration));

    }

}
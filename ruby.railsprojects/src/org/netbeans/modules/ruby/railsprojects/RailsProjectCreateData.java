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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;

/**
 * Encapsulates data required for creating a new Rails project (empty 
 * or from existing sources).
 *
 * @author Erno Mononen
 */
public class RailsProjectCreateData {
    
    /**
     * The top-level directory for the project.
     */
    private final File dir;
    /**
     * The name for the project.
     */
    private final String name;
    /**
     * Specifies whether to generate base directory structure or not.
     */
    private final boolean create;
    /**
     * The database to use.
     */
    private final String database;
    /**
     * Specifies whether the database should be accessed using JDBC.
     */
    private final boolean jdbc;
    /**
     * Specifies whether the project might be deployed as a .war file.
     */
    private final boolean deploy;

    /**
     * Constructs a new RailsProjectCreateData instance.
     * @param dir the top-level directory for the project 
     * (need not yet exist but if it does it must be empty).
     * @param name the name for the project.
     * @param create specifies whether to generate base directory structure or not (use
     *        false for existing application)
     * @param database the type of the database to use, e.g. mysql, JavaDB etc.
     * @param jdbc specifies whether JDBC should be used for accessing the database.
     * @param deploy specifies whether the Rake support targets for deploying 
     * the project as a .war file should be added.
     */
    public RailsProjectCreateData(File dir, String name, boolean create, String database, boolean jdbc, boolean deploy) {
        this.dir = dir;
        this.name = name;
        this.create = create;
        this.database = database;
        this.jdbc = jdbc;
        this.deploy = deploy;
    }

    /**
     * @see #create
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * @see #database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @see #deploy
     */
    public boolean isDeploy() {
        return deploy;
    }

    /**
     * @see #dir
     */
    public File getDir() {
        return dir;
    }

    /**
     * @see #jdbc
     */
    public boolean isJdbc() {
        return jdbc;
    }

    /**
     * @see #name
     */
    public String getName() {
        return name;
    }
    
}


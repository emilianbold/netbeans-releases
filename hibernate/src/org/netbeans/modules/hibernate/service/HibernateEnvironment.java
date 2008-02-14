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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hibernate.service;

import java.sql.SQLException;
import java.util.ArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.openide.util.Exceptions;

/**
 * This class provides the service for NetBeans projects.
 * This class abstracts the services provided by Hibernate and
 * wrapps them around to provide a more meaningful and high level services.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateEnvironment {
    /** private cache of all hibernate configurations for the current project. */
    private ArrayList<HibernateConfiguration> configurations = new ArrayList<HibernateConfiguration>();

    /** Handle to the current project to which this HibernateEnvironment is bound*/
    private Project project;

    /**
     * Creates a new hibernate environment for this NetBeans project.
     *
     * @param project NB project.
     */
    public HibernateEnvironment(Project project) {
        this.project = project;
    }


    /**
     * Empty constructor used to create Hibernate Environments without NB projects.
     */
    public HibernateEnvironment() {
    }


    /**
     * Utility method to update the private cache of hibernate configurations.
     *
     * @param hibernateConfiguration hibernate configuration that need to be added
     *        to the private cache.
     */

    public void addConfiguration(HibernateConfiguration hibernateConfiguration) {
        configurations.add(hibernateConfiguration);
    }

    /**
     * Utility method to update the private cache of hibernate configurations.
     *
     * @param hibernateConfiguration the hibenrate configuration that need to be
     *        removed from the cache.
     */
    public void removeConfiguration(HibernateConfiguration hibernateConfiguration) {
        configurations.remove(hibernateConfiguration);
    }

    /**
     * Connects to the DB using supplied HibernateConfigurations and gets the list of
     * all table names.
     *
     * @param configurations vararg of Hibernate Configurations.
     * @return array list of strings of table names.
     */
    public ArrayList<String> getAllDatabaseTables(HibernateConfiguration... configurations) {
        try {
            return HibernateUtil.getAllDatabaseTables(configurations);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Returns all hibernate configurations from private cache.
     *
     * @return array list of hibernate configurations.
     */
    public ArrayList<HibernateConfiguration> getAllHibernateConfigurations() {
        return configurations;
    }

    /**
     * Returns all mappings registered with this HibernateConfiguration.
     *
     * @param hibernateConfiguration hibernate configuration.
     */
    //TODO this method is a prototype..
    public ArrayList<String> getAllHibernateMappingsFromConfiguration(HibernateConfiguration hibernateConfiguration) {
        ArrayList<String> mappingsFromConfiguration = new ArrayList<String>();
        SessionFactory fact = hibernateConfiguration.getSessionFactory();
        int count = 0;
        for(boolean val : fact.getMapping()) {
            String propName = fact.getAttributeValue(fact.MAPPING,
                    count++, "resource"); //NOI18N
            mappingsFromConfiguration.add(propName);
        }
        return mappingsFromConfiguration;
    }

    /**
     * Returns the NetBeans project to which this HibernateEnvironment instance is bound.
     *
     * @return NetBeans project.
     */
    public Project getProject() {
        return project;
    }
}

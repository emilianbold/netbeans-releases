/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author Dongmei Cao
 */
public class Util {

    private static final String[] defaultJPA20Keys = new String[]{"javax.persistence.lock.timeout", "javax.persistence.query.timeout", "javax.persistence.validation.group.pre-persist", "javax.persistence.validation.group.pre-update", "javax.persistence.validation.group.pre-remove"};
    private static final String[] eclipselink20Keys = new String[]{"eclipselink.temporal.mutable", "eclipselink.cache.type.default", "eclipselink.cache.size.default", "eclipselink.cache.shared.default", "eclipselink.flush-clear.cache", "eclipselink.orm.throw.exceptions", "eclipselink.exception-handler", "eclipselink.weaving", "eclipselink.weaving.lazy", "eclipselink.weaving.changetracking", "eclipselink.weaving.fetchgroups", "eclipselink.weaving.internal", "eclipselink.weaving.eager", "eclipselink.session.customizer", "eclipselink.validation-only", "eclipselink.classloader", "eclipselink.profiler", "eclipselink.persistence.context.reference-mode", "eclipselink.jdbc.bind-parameters", "eclipselink.jdbc.native-sql", "eclipselink.jdbc.batch-writing", "eclipselink.jdbc.cache-statements", "eclipselink.jdbc.cache-statements.size", "eclipselink.jdbc.exclusive-connection.is-lazy", "eclipselink.jdbc.exclusive-connection.mode", "eclipselink.jdbc.read-connections.max", "eclipselink.jdbc.read-connections.min", "eclipselink.jdbc.read-connections.shared", "eclipselink.jdbc.write-connections.max", "eclipselink.jdbc.write-connections.min", "eclipselink.logging.logger", "eclipselink.logging.level", "eclipselink.logging.timestamp", "eclipselink.logging.thread", "eclipselink.logging.session", "eclipselink.logging.exceptions", "eclipselink.logging.file", "eclipselink.session-name", "eclipselink.sessions-xml", "eclipselink.session-event-listener", "eclipselink.session.include.descriptor.queries", "eclipselink.target-database", "eclipselink.target-server", "eclipselink.application-location", "eclipselink.create-ddl-jdbc-file-name", "eclipselink.drop-ddl-jdbc-file-name", "eclipselink.ddl-generation.output-mode", "eclipselink.weaving.changetracking", "eclipselink.canonicalmodel.prefix", "eclipselink.canonicalmodel.suffix", "eclipselink.canonicalmodel.subpackage"};//TODO: handle properties {propname.entityname}

    /*
     * return all properties for specific provider, except some handled specially
     */
    public static ArrayList<String> getAllPropNames(Provider propCat) {
        ArrayList<String> results = new ArrayList<String>();
        if (Persistence.VERSION_2_0.equals(ProviderUtil.getVersion(propCat))) {
            Collections.addAll(results, defaultJPA20Keys);
        }
        results.addAll(propCat.getPropertyNames());
        if (ProviderUtil.ECLIPSELINK_PROVIDER.equals(propCat)) {
            Collections.addAll(results, eclipselink20Keys);//TODO: should it be moved into a provider
        }
        return results;
    }

    public static ArrayList<String> getPropsNamesExceptGeneral(Provider propCat) {
        ArrayList<String> propsList = getAllPropNames(propCat);
        propsList.remove(propCat.getJdbcDriver());
        propsList.remove(propCat.getJdbcUsername());
        propsList.remove(propCat.getJdbcUrl());
        propsList.remove(propCat.getJdbcPassword());
        propsList.remove(propCat.getTableGenerationPropertyName());
        return propsList;
    }

    /**
     * Gets the properties that are not defined in the configuration file yet
     * 
     * @param propCat The property category
     * @param sessionFactory The session factory that contains the properties
     * @return Array of property names
     */
    public static ArrayList<String> getAvailPropNames(Provider propCat, PersistenceUnit pu) {

        List<String> propsList = getPropsNamesExceptGeneral(propCat);

        if (pu != null) {
            ArrayList<String> availProps = new ArrayList<String>(propsList);

            for (int i = 0; i < pu.getProperties().sizeProperty2(); i++) {
                String propName = pu.getProperties().getProperty2(i).getName();
                if (!availProps.remove(propName)
                        && availProps.contains("javax.persistence." + propName)) {
                    availProps.remove(propName);
                }
            }

            return availProps;
        }

        return new ArrayList<String>();
    }

//    // Gets the list of mapping files from HibernateEnvironment.
//    public static String[] getMappingFilesFromProject(FileObject fileObj) {
//        Project enclosingProject = FileOwnerQuery.getOwner(fileObj);
//        HibernateEnvironment env = enclosingProject.getLookup().lookup(HibernateEnvironment.class);
//        if(env != null) {
//            return env.getAllHibernateMappings().toArray(new String[]{});
//        } else {
//            return new String[0];
//        }
//    }
    public static SourceGroup[] getJavaSourceGroups(PUDataObject dObj) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        if (proj == null) {
            return new SourceGroup[]{};
        }
        Sources sources = ProjectUtils.getSources(proj);
        SourceGroup[] toRet = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (toRet != null && toRet.length != 0) {
            return toRet;
        }
        return sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }

    public static String getResourcePath(SourceGroup[] groups, FileObject fo) {
        return getResourcePath(groups, fo, '.', false);
    }

    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator) {
        return getResourcePath(groups, fo, separator, false);
    }

    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator, boolean withExt) {
        for (int i = 0; i < groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root, fo)) {
                String relativePath = FileUtil.getRelativePath(root, fo);
                if (relativePath != null) {
                    if (separator != '/') {
                        relativePath = relativePath.replace('/', separator);
                    }
                    if (!withExt) {
                        int index = relativePath.lastIndexOf((int) '.');
                        if (index > 0) {
                            relativePath = relativePath.substring(0, index);
                        }
                    }
                    return relativePath;
                } else {
                    return "";
                }
            }
        }
        return "";
    }
}

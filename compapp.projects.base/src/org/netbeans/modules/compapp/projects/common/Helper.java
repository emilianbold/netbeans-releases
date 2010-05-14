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
package org.netbeans.modules.compapp.projects.common;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.EntryType;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This class is a helper class that provide set of utility methods used for
 * the implicit catalog support in projects.
 * @author chikkala
 */
public class Helper {

    /** logger */
    private static final Logger sLogger = Logger.getLogger(Helper.class.getName());

    /**
     * used for creating the DefaultProjectCatalogSupport added to the project lookup.
     * This is required for a crossproject referencing
     * @param prj
     * @param antHelper
     * @param refHelper
     * @return
     */
    public static ProjectCatalogSupport createDefaultProjectCatalogSupport(Project prj, AntProjectHelper antHelper, ReferenceHelper refHelper) {
        return new DefaultProjectCatalogSupport(prj, antHelper, refHelper);
    }

    /**
     * used for creating the ImplicitCatalogSupport added to the project lookup.
     * This is required for a managing implicit namespace referencing
     * @param prj
     * @param antHelper
     * @param refHelper
     * @return
     */
    public static ProjectCatalogSupport createImplicitCatalogSupport(Project prj, AntProjectHelper antHelper, ReferenceHelper refHelper) {
        return new ImplicitCatalogSupport(prj, antHelper, refHelper);
    }

    /**
     * return true if the project supports both DefaultProjectCatalogSupport and the 
     * ImplicitCatalogSupport
     * @param prj project to check.
     * @return true or false
     */
    public static boolean hasImplicitCatalogSupport(Project prj) {
        assert prj != null;
        DefaultProjectCatalogSupport prjCatSupport = prj.getLookup().lookup(DefaultProjectCatalogSupport.class);
        ImplicitCatalogSupport icatSupport = prj.getLookup().lookup(ImplicitCatalogSupport.class);
        return prjCatSupport != null && icatSupport != null;
    }

    /**
     * returns the fileobject correpsonding to the namespace by looking up the implicit catalog.
     * @param prj project in which to lookup for the fileobject correpsonding to the namespace.
     * @param namespace  namespace for which the fileobject to be resolved.
     * @param type EntryType WSDL or XSD. can be null. if null, return first entry found.
     * @return fileobject
     */
    public static FileObject resolveImplicitReference(Project prj, String namespace, EntryType type) {
        FileObject fo = null;
        assert prj != null;
        ImplicitCatalogSupport icatSupport = ImplicitCatalogSupport.getInstance(prj);
        try {
            fo = icatSupport.resolveImplicitReference(namespace, type);
        } catch (Exception ex) {
            sLogger.log(Level.FINE, ex.getMessage(), ex);
        }
        return fo;
    }

    /**
     * returns the fileobject correpsonding to the namespace by looking up the implicit catalog.
     * @param source fileobject in which the namespace is being used.
     * @param namespace namespace for which the fileobject to be resolved.
     * @param type EntryType WSDL or XSD. can be null. if null, return first entry found.
     * @return fileobject 
     * @throws java.io.IOException
     */
    public static FileObject resolveImplicitReference(FileObject source, String namespace, EntryType type) throws IOException {
        Project prj = FileOwnerQuery.getOwner(source);
        return resolveImplicitReference(prj, namespace, type);
    }
}

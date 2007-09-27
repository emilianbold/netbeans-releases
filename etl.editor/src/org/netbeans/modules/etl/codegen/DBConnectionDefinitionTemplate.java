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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.etl.codegen;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConnectionParameters;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import com.sun.sql.framework.utils.StringUtil;

/**
 * DBConnectionDefinitionTemplate basicaly reads the template and stores for codegen
 *
 * @author Sudhi Seshachala
 * @version $Revision$
 */

public class DBConnectionDefinitionTemplate {
    public static final String KEY_DATABASE_NAME = "DatabaseName";

    public static final String KEY_HOST_NAME = "HostName";
    public static final String KEY_HOST_PORT = "HostPort";
    public static final String KEY_LOCATION_NAME = "LocationName";
    public static final String KEY_METADATA_DIR = "MetadataDir";
    public static final String KEY_PARAM_LIST = "ParamList";
    private static final String TEMPLATE = "org/netbeans/modules/etl/codegen/CodegenConnectionDefinitionTemplate.xml";

    private Map connectionDefinitions = new HashMap();

    /**
     * Default Constructor
     *
     * @throws BaseException, if fails to read Template.
     */
    public DBConnectionDefinitionTemplate() throws BaseException {
        Element connectionTemplateRoot = XmlUtil.loadXMLFile(TEMPLATE);
        parseXML(connectionTemplateRoot);
    }

    /**
     * Returns a DB Connection Definition template for a given db type
     *
     * @param dbType database type
     * @return a DB Connection Definition template for a given db type
     */
    public SQLDBConnectionDefinition getDBConnectionDefinition(String dbType) {
        if (!StringUtil.isNullString(dbType)) {
            SQLDBConnectionDefinition orig = (SQLDBConnectionDefinition) connectionDefinitions.get(dbType.toLowerCase());
            if (orig != null) {
                orig = (SQLDBConnectionDefinition) orig.cloneObject();
            }
            return orig;
        }
        return null;
    }

    private void parseXML(Element connectionTemplateRoot) throws BaseException {
        if (connectionTemplateRoot == null) {
            throw new BaseException("Invalid connection template:" + TEMPLATE);
        }

        NodeList children = connectionTemplateRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(DBConnectionParameters.CONNECTION_DEFINITION_TAG)) {
                SQLDBConnectionDefinition conDefnTemplate = SQLModelObjectFactory.getInstance().createDBConnectionDefinition((Element) child);
                connectionDefinitions.put(conDefnTemplate.getName().toLowerCase(), conDefnTemplate);
            }
        }
    }

}


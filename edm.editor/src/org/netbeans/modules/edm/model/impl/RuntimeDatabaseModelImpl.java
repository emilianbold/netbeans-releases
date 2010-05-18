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
package org.netbeans.modules.edm.model.impl;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.edm.model.RuntimeDatabaseModel;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.SQLObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.edm.model.EDMException;

/**
 * Runtime DbModel is one instance per sql definition and it contains only one instance of
 * RuntimeInputImpl and only one instance of RuntimeOutputImpl
 * 
 * @author Ritesh Adval
 */
public class RuntimeDatabaseModelImpl extends SQLDBModelImpl implements RuntimeDatabaseModel {

    /** Creates a new instance of RuntimeDatabaseModelImpl */
    public RuntimeDatabaseModelImpl() {
        super();
        type = SQLConstants.RUNTIME_DBMODEL;
        this.setModelName("RuntimeDbModel");
        this.setDisplayName("Runtime DbModel");
    }

    /**
     * Get runtime input
     * 
     * @return RuntimeInput
     */
    public RuntimeInput getRuntimeInput() {
        List tbls = this.getTables();
        Iterator it = tbls.iterator();

        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (sqlObj.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                return (RuntimeInput) sqlObj;
            }
        }

        return null;
    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     * 
     * @param dbElement DOM element containing XML marshalled version of a
     * @exception EDMException thrown while parsing XML, or if member variable element is
     *            null
     */
    public void parseXML(Element dbElement) throws EDMException {
        if (dbElement == null) {
            throw new EDMException("Must supply non-null org.w3c.dom.Element ref for element.No <" + RUNTIME_MODEL_TAG + "> element found.");
        }

        if (!RUNTIME_MODEL_TAG.equals(dbElement.getNodeName())) {
            throw new EDMException("Invalid root element; expected " + RUNTIME_MODEL_TAG + ", got " + dbElement.getNodeName());
        }

        super.parseCommonAttributesAndTags(dbElement);
        name = dbElement.getAttribute(NAME);
        String typeStr = dbElement.getAttribute(TYPE);

        NodeList childNodeList = null;

        childNodeList = dbElement.getChildNodes();

        if (STRTYPE_RUNTIME.equals(typeStr)) {
            type = SQLConstants.RUNTIME_DBMODEL;
            childNodeList = dbElement.getElementsByTagName(RuntimeInput.TAG_RUNTIME_INPUT);
            parseRuntimeInput(childNodeList);
        } else {
            throw new EDMException("Missing or invalid modelType attribute: " + typeStr);
        }
    }

    /**
     * Gets xml representation of this DatabaseModel instance.
     * 
     * @param prefix for this xml.
     * @return Return the xml representation of data source metadata.
     * @exception EDMException - exception
     */
    public String toXMLString(String prefix) throws EDMException {
        StringBuilder xml = new StringBuilder(INIT_XMLBUF_SIZE);
        if (prefix == null) {
            prefix = "";
        }

        xml.append(prefix).append('<').append(RUNTIME_MODEL_TAG).append(" ").append(NAME).append("=\"").append(name.trim()).append("\"");

        if (id != null && id.trim().length() != 0) {
            xml.append(" ").append(ID).append("=\"").append(id.trim()).append("\"");
        }

        if (displayName != null && displayName.trim().length() != 0) {
            xml.append(" ").append(DISPLAY_NAME).append("=\"").append(displayName.trim()).append('"');
        }

        if (type == SQLConstants.RUNTIME_DBMODEL) {
            xml.append(' ').append(TYPE).append("=\"").append(STRTYPE_RUNTIME).append('"');
        }

        xml.append(">\n");

        // write out tables
        writeTables(prefix, xml);

        xml.append(prefix).append("</").append(RUNTIME_MODEL_TAG).append(">\n");
        return xml.toString();
    }

    /**
     * Extracts SourceTable instances from the given NodeList.
     * 
     * @param tableNodeList Nodes to be unmarshalled
     * @throws EDMException if error occurs while parsing
     */
    protected void parseRuntimeInput(NodeList tableNodeList) throws EDMException {
        for (int i = 0; i < tableNodeList.getLength(); i++) {
            if (tableNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element tableElement = (Element) tableNodeList.item(i);

                RuntimeInput runtime = SQLModelObjectFactory.getInstance().createRuntimeInput();
                runtime.setParentObject(this);

                runtime.parseXML(tableElement);
                addTable(runtime);
            }
        }
    }
}


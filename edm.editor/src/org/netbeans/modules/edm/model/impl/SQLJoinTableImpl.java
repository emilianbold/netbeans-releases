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

import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.model.GUIInfo;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLJoinTable;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SourceTable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.NbBundle;


/**
 * @author Ritesh Adval
 */
public class SQLJoinTableImpl extends AbstractSQLObject implements SQLJoinTable {

    private static final String LOG_CATEGORY = SQLJoinTableImpl.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLJoinTableImpl.class.getName());
    private GUIInfo guiInfo = new GUIInfo();
    private SourceTable table;

    /**
     * Creates a new default instance of SQLJoinTable.
     */
    public SQLJoinTableImpl() {
        this.type = SQLConstants.JOIN_TABLE;
    }

    /**
     * Creates a new instance of SQLJoinTable.
     * 
     * @param tbl underlying SourceTable to associate with this instance.
     */
    public SQLJoinTableImpl(SourceTable tbl) {
        this();
        this.table = tbl;
    }

    /**
     * Creates a new instance of SQLJoinTableImpl
     * 
     * @param src SQLJoinTable from which to copy attributes, etc.
     * @throws EDMException if error occurs while copying from src
     */
    public SQLJoinTableImpl(SQLJoinTable src) throws EDMException {
        this();

        if (src == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(SQLJoinTableImpl.class, "ERROR_null_SQLJoinTable_instance"));
        }

        try {
            copyFrom(src);
        } catch (Exception ex) {
            throw new EDMException(NbBundle.getMessage(SQLJoinTableImpl.class, "ERROR_can_not_create_SQLJoinTableImpl"), ex);
        }
    }

    /**
     * Overrides default implementation.
     * 
     * @return cloned instance of this object
     * @throws CloneNotSupportedException if this cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        SQLJoinTable cond = null;
        try {
            cond = new SQLJoinTableImpl(this);
        } catch (Exception ex) {
            mLogger.log(Level.INFO,NbBundle.getMessage(SQLJoinTableImpl.class, "ERROR_can_not_create_clone")+this.toString(),ex);
            throw new CloneNotSupportedException(NbBundle.getMessage(SQLJoinTableImpl.class, "ERROR_can_not_create_clone") + this.toString());
        }
        return cond;
    }

    /**
     * All SQL objects are cloneable.
     * 
     * @return cloned instance of this object
     * @throws CloneNotSupportedException if this cannot be cloned.
     */
    public Object cloneSQLObject() throws CloneNotSupportedException {
        return this.clone();
    }

    /**
     * Gets display name of this object.
     * 
     * @return String representing display name.
     */
    public String getDisplayName() {
        return table.getDisplayName();
    }

    /**
     * Gets GUI-related attributes for this instance in the form of a GuiInfo instance.
     * 
     * @return associated GuiInfo instance
     * @see GUIInfo
     */
    public GUIInfo getGUIInfo() {
        return this.guiInfo;
    }

    /**
     * Gets name of this object.
     * 
     * @return String representing object name.
     */
    public String getName() {
        return table.getName();
    }

    /**
     * Gets source table associated with this instance.
     * 
     * @return SourceTable to associate with this instance.
     */
    public SourceTable getSourceTable() {
        return table;
    }

    /**
     * Populates the member variables and collections of this SQLObject instance, parsing
     * the given DOM Element as the source for reconstituting its contents.
     * 
     * @param columnElement DOM element containing XML marshalled version of this
     *        SQLObject instance
     * @throws EDMException if element is null or error occurs during parsing
     */
    public void parseXML(Element xmlElement) throws EDMException {
        super.parseXML(xmlElement);

        NodeList childNodeList = xmlElement.getChildNodes();
        if (childNodeList != null && childNodeList.getLength() != 0) {
            for (int i = 0; i < childNodeList.getLength(); i++) {
                if (childNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNodeList.item(i);
                    String tagName = childElement.getTagName();

                    if (TagParserUtility.TAG_OBJECTREF.equals(tagName)) {
                        secondPassParse(childElement);
                    } else if (GUIInfo.TAG_GUIINFO.equals(tagName)) {
                        this.guiInfo = new GUIInfo(childElement);
                    }
                }
            }
        }
    }

    /**
     * Parses elements which require a second round of parsing to resolve their
     * references.
     * 
     * @param element DOM element containing XML marshalled version of this SQLObject
     *        instance
     * @throws EDMException if element is null or error occurs during parsing
     */
    public void secondPassParse(Element element) throws EDMException {
        SQLJoinView jView = (SQLJoinView) this.getParentObject();
        SQLDefinition definition = TagParserUtility.getAncestralSQLDefinition(jView);

        SQLObject obj = TagParserUtility.parseXMLObjectRefTag(definition, element);

        // If obj is null it may not be parsed yet so do a second parse...
        // it registers this TargetColumn instance to be parsed a second time
        // to resolve the value reference
        if (obj == null) {
            definition.addSecondPassSQLObject(this, element);
        } else {
            setSourceTable((SourceTable) obj);
        }
    }

    /**
     * Sets source table for this object to the given instance.
     * 
     * @param sTable SourceTable to associate with this instance
     */
    public void setSourceTable(SourceTable sTable) {
        this.table = sTable;
    }

    /**
     * Generates XML document representing this object's content, using the given String
     * as a prefix for each line.
     * 
     * @param prefix String to be prepended to each line of the generated XML document
     * @return String containing XML representation
     * @exception EDMException - exception
     * @see SQLObject#toXMLString(java.lang.String)
     */
    public String toXMLString(String prefix) throws EDMException {
        StringBuilder buffer = new StringBuilder(500);
        if (prefix == null) {
            prefix = "";
        }

        final String childPrefix = prefix + "\t";

        buffer.append(prefix).append(getHeader());
        buffer.append(toXMLAttributeTags(prefix));
        buffer.append(TagParserUtility.toXMLObjectRefTag(this.getSourceTable(), childPrefix));

        buffer.append(this.guiInfo.toXMLString(childPrefix));
        buffer.append(prefix).append(getFooter());

        return buffer.toString();
    }

    private void copyFrom(SQLJoinTable src) throws EDMException {
        super.copyFromSource(src);

        // copy gui info
        GUIInfo gInfo = src.getGUIInfo();
        this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;

        // copy source table as it no cloning as it is referenced object
        this.table = src.getSourceTable();
    }
}


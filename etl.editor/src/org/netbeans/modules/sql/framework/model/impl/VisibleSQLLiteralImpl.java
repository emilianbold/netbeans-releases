/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.model.impl;

import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;

/**
 * Represents a string or number literal value.
 * 
 * @author Ritesh Adval, Sudhi Seshachala
 * @version $Revision$
 */
public class VisibleSQLLiteralImpl extends SQLLiteralImpl implements VisibleSQLLiteral {

    /* GUI state info */
    private GUIInfo guiInfo = new GUIInfo();

    /** Creates a new instance of SQLLiteral */
    public VisibleSQLLiteralImpl() {
        type = SQLConstants.VISIBLE_LITERAL;
    }

    public VisibleSQLLiteralImpl(String aDisplayName, String value, int jdbcType) throws BaseException {
        super(aDisplayName, value, jdbcType);
        type = SQLConstants.VISIBLE_LITERAL;
    }

    public VisibleSQLLiteralImpl(VisibleSQLLiteral src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("can not create VisibleSQLLiteral using copy constructor src is null");
        }

        super.copyFrom(src);

        // copy gui info
        GUIInfo gInfo = src.getGUIInfo();
        this.guiInfo = gInfo != null ? (GUIInfo) gInfo.clone() : null;

    }

    public Object clone() {
        return new VisibleSQLLiteralImpl(this);
    }

    /**
     * Gets GUI-related attributes for this instance in the form of a GuiInfo instance.
     * 
     * @return associated GuiInfo instance
     * @see GUIInfo
     */
    public GUIInfo getGUIInfo() {
        return guiInfo;
    }

    /**
     * Parses the given xmlElement
     * 
     * @exception BaseException while parsing
     * @param xmlElement to be parsed
     */
    public void parseXML(Element xmlElement) throws BaseException {
        super.parseXML(xmlElement);

        NodeList guiInfoList = xmlElement.getElementsByTagName(GUIInfo.TAG_GUIINFO);
        if (guiInfoList != null && guiInfoList.getLength() != 0) {
            Element elem = (Element) guiInfoList.item(0);
            guiInfo = new GUIInfo(elem);
        }
    }

    /**
     * Overrides parent implementation to append UI state information.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     */
    public String toXMLString(String prefix) {
        StringBuffer buf = new StringBuffer(200);

        buf.append(prefix).append(getHeader());
        buf.append(toXMLAttributeTags(prefix));
        buf.append(guiInfo.toXMLString(prefix + "\t"));
        buf.append(prefix).append(getFooter());

        return buf.toString();
    }
}

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
package org.netbeans.modules.sql.framework.model.impl;

import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.etl.exception.BaseException;

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
        StringBuilder buf = new StringBuilder(200);

        buf.append(prefix).append(getHeader());
        buf.append(toXMLAttributeTags(prefix));
        buf.append(guiInfo.toXMLString(prefix + "\t"));
        buf.append(prefix).append(getFooter());

        return buf.toString();
    }
}

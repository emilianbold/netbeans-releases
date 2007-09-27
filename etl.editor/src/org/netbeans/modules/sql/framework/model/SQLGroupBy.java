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

package org.netbeans.modules.sql.framework.model;

import java.util.List;

import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public interface SQLGroupBy {

    /**
     * Gets the list of columns participating as group by clauses
     * 
     * @return List of SourceColumn instances
     */
    public List getColumns();

    public SQLCondition getHavingCondition();

    public Object getParentObject();

    public boolean isValid();

    /**
     * Parses the XML content, if any, represented by the given DOM element.
     * 
     * @param groupByElement DOM element to be parsed for groupBy content
     * @exception BaseException thrown while parsing XML, or if groupByElement is null
     */
    public void parseXML(Element groupByElement) throws BaseException;

    /**
     * @see SQLGroupBy#removeExpression
     */
    public void removeColumn(SQLObject obj);

    public void setColumns(List newColumnList);

    public void setHavingCondition(SQLCondition having);

    public void setParentObject(Object obj);

    /**
     * Generates XML document representing this object's content, using the given String
     * as a prefix for each line.
     * 
     * @param prefix String to be prepended to each line of the generated XML document
     * @return String containing XML representation
     */
    public String toXMLString(String prefix) throws BaseException;
}


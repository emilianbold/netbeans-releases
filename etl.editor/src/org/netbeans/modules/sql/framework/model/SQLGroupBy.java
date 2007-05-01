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


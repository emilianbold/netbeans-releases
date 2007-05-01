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
package org.netbeans.modules.mashup.db.model;

import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.w3c.dom.Element;


/**
 * Interface to contain general connection/directory information for flatfile
 * data sources.
 * 
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface FlatfileDBConnectionDefinition extends DBConnectionDefinition, Cloneable {

    /**
     * @param xmlElement
     */
    void parseXML(Element xmlElement);

    /**
     * Sets new name for this DBConnectionDefinition.
     * 
     * @param newName new name for DBConnectionDefinition
     */
    void setName(String newName);

    /**
     * @param prefix
     * @return
     */
    String toXMLString(String prefix);

    void setConnectionURL(String aUrl);

}


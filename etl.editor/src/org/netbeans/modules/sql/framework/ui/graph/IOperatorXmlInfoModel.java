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
package org.netbeans.modules.sql.framework.ui.graph;

import org.openide.nodes.Node;

/**
 * @author radval
 */
public interface IOperatorXmlInfoModel {
    public static final int CATEGORY_ALL = 0;
    public static final int CATEGORY_TRANSFORM = 1; // Main canvas
    public static final int CATEGORY_FILTER = 2; // Data filter
    public static final int CATEGORY_VALIDATION = 4; // Validation
    public static final int CATEGORY_HAVING = 16; // Having Clause

    public Node getRootNode();

    public IOperatorXmlInfo findOperatorXmlInfo(String operatorName);

}


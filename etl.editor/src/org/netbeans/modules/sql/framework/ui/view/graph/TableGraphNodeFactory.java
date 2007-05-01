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

package org.netbeans.modules.sql.framework.ui.view.graph;

import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author radval To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class TableGraphNodeFactory extends AbstractGraphFactory {

    private static Map tableToGraphNodeMap = new HashMap();

    static {
        tableToGraphNodeMap.put(String.valueOf(SQLConstants.SOURCE_TABLE), "org.netbeans.modules.sql.framework.ui.view.graph.SQLSourceTableArea");
        tableToGraphNodeMap.put(String.valueOf(SQLConstants.TARGET_TABLE), "org.netbeans.modules.sql.framework.ui.view.graph.SQLTargetTableArea");
        tableToGraphNodeMap.put(String.valueOf(SQLConstants.RUNTIME_INPUT), "org.netbeans.modules.sql.framework.ui.view.graph.SQLRuntimeInputArea");
        tableToGraphNodeMap.put(String.valueOf(SQLConstants.RUNTIME_OUTPUT), "org.netbeans.modules.sql.framework.ui.view.graph.SQLRuntimeOutputArea");
    }

    public IGraphNode createGraphNode(SQLCanvasObject obj) throws BaseException {
        SQLBasicTableArea tableArea = null;

        String className = (String) tableToGraphNodeMap.get(String.valueOf(obj.getObjectType()));
        if (className != null) {
            try {
                Class cls = Class.forName(className);
                tableArea = (SQLBasicTableArea) cls.newInstance();
                tableArea.initialize(obj);
            } catch (Exception ex) {
                throw new BaseException("failed to create gui representation of " + obj.getDisplayName());
            }
        }
        return tableArea;
    }
}


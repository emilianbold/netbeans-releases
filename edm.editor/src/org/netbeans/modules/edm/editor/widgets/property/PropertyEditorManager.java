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
package org.netbeans.modules.edm.editor.widgets.property;

import java.util.HashMap;
import java.util.Map;

import org.openide.util.Exceptions;


/**
 *
 * @author Nithya
 */
public class PropertyEditorManager {
    
    private static Map<String, String> editorMap = new HashMap<String, String>();
    
    static {
        editorMap.put("JOIN_CONDITION",
                "org.netbeans.modules.edm.editor.widgets.property.editor.JoinConditionCustomEditor");
        editorMap.put("JOIN_TYPE",
                "org.netbeans.modules.edm.editor.widgets.property.editor.JoinTypeCustomEditor");
        editorMap.put("FILTER_CONDITION",
                "org.netbeans.modules.edm.editor.widgets.property.editor.ExtractionConditionCustomEditor");
        editorMap.put("HAVING_CONDITION",
                "org.netbeans.modules.edm.editor.widgets.property.editor.HavingConditionCustomEditor");        
        editorMap.put("RESPONSE_TYPE",
                "org.netbeans.modules.edm.editor.widgets.property.editor.ResponseTypeMenuEditor");        
    }
    
    private PropertyEditorManager() {
    }
    
    public static Class getPropertyEditor(String obj) {
        try {
            String editorClass = editorMap.get(obj);
            return Class.forName(editorClass).newInstance().getClass();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
}

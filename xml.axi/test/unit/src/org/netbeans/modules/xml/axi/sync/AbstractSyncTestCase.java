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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.axi.sync;

import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;

        
/**
 * Abstract sync test case. The sync test cases update the
 * underlying schema model and then syncs up the AXI model
 * based on events obtained from schema model.
 *
 * Note: the AXI model must be initialized. See setUp().
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractSyncTestCase extends AbstractTestCase {
            
    /**
     * AbstractSyncTestCase
     */
    public AbstractSyncTestCase(String testName,
            String schemaFile, String globalElement) {
        super(testName, schemaFile, globalElement);
    }
        
    GlobalElement findGlobalElement(String name) {
        for(GlobalElement element : getSchemaModel().getSchema().getElements()) {
            if(element.getName().equals(name))
                return element;
        }
        return null;
    }
    
    GlobalComplexType findGlobalComplexType(String name) {
        for(GlobalComplexType type : getSchemaModel().getSchema().getComplexTypes()) {
            if(type.getName().equals(name))
                return type;
        }
        return null;
    }
    
    GlobalGroup findGlobalGroup(String name) {
        for(GlobalGroup group : getSchemaModel().getSchema().getGroups()) {
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }

    GlobalAttribute findGlobalAttribute(String name) {
        for(GlobalAttribute attr : getSchemaModel().getSchema().getAttributes()) {
            if(attr.getName().equals(name))
                return attr;
        }
        return null;
    }
    
    GlobalAttributeGroup findGlobalAttributeGroup(String name) {
        for(GlobalAttributeGroup group : getSchemaModel().getSchema().getAttributeGroups()) {
            if(group.getName().equals(name))
                return group;
        }
        return null;
    }    
}

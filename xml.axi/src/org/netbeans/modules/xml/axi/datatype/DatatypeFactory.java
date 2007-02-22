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

package org.netbeans.modules.xml.axi.datatype;

import java.util.List;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.impl.DatatypeFactoryImpl;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleType;

/**
 *
 * @author Ayub Khan
 */
public abstract class DatatypeFactory {
    
    private static DatatypeFactory instance;
    
    /** Creates a new instance of DatatypeFactory */
    public static DatatypeFactory getDefault() {
        if(instance == null)
            instance = new DatatypeFactoryImpl();
        return instance;
    }
    
    /**
     * Creates an AXI Datatype, given a schema component like
     * global element, local element, local attribute, global attribute etc.
     */
    public abstract Datatype getDatatype(AXIModel axiModel, SchemaComponent component);
    
    /**
     * returns a list of Applicable Schema facets for the given primitive type
     */
    public abstract List<Class<? extends SchemaComponent>> getApplicableSchemaFacets(SimpleType st);
    
    /**
     * Creates an AXI Datatype, given a typeName (built-in types
     * like "string").
     */
    public abstract Datatype createPrimitive(String typeName);
}

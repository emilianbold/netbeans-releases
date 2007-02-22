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
package org.netbeans.modules.xml.axi;

import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.axi.impl.SchemaGeneratorFactoryImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Ayub Khan
 */
public abstract class SchemaGeneratorFactory {
    
    public enum TransformHint{
        OK,
        SAME_DESIGN_PATTERN,
        INVALID_SCHEMA,
        NO_GLOBAL_ELEMENTS,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ATTRIBUTES,
        GLOBAL_ELEMENTS_HAVE_NO_CHILD_ELEMENTS_AND_ATTRIBUTES,
        GLOBAL_ELEMENTS_HAVE_NO_GRAND_CHILDREN,
        NO_ATTRIBUTES,
        WILL_REMOVE_TYPES,
        WILL_REMOVE_GLOBAL_ELEMENTS,
        WILL_REMOVE_GLOBAL_ELEMENTS_AND_TYPES,
        CANNOT_REMOVE_TYPES,
        CANNOT_REMOVE_GLOBAL_ELEMENTS,
        CANNOT_REMOVE_GLOBAL_ELEMENTS_AND_TYPES;
    }
    
    private static SchemaGeneratorFactory instance;
    
    /** Creates a new instance of SchemaGeneratorFactory */
    public static SchemaGeneratorFactory getDefault() {
        if(instance == null)
            instance = new SchemaGeneratorFactoryImpl();
        return instance;
    }
    
    /*
     * infers design pattern
     *
     */
    public abstract SchemaGenerator.Pattern inferDesignPattern(AXIModel am);
    
    /*
     * Updates schema using a a particular design pattern
     *
     */
    public abstract void updateSchema(SchemaModel sm,
            SchemaGenerator.Pattern pattern) throws BadLocationException, IOException;
    
    /*
     * returns list of all master axi global elements
     *
     * @param am - AXIModel
     * @return ges - list of all master axi global elements
     */    
    public abstract List<Element> findMasterGlobalElements(AXIModel am);
    
    /*
     * can transforms schema using a a particular design pattern
     *
     * @param sm - SchemaModel
     * @param currentPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param targetPattern - if null then checks only if the schema is valid and well-formed for transform
     */
    public abstract TransformHint canTransformSchema(SchemaModel sm,
            SchemaGenerator.Pattern currentPattern, SchemaGenerator.Pattern targetPattern);
    
    /*
     * can transforms schema using a a particular design pattern
     *
     * @param sm - SchemaModel
     * @param currentPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param targetPattern - if null then checks only if the schema is valid and well-formed for transform
     * @param ges - list of all master axi global elements
     */
    public abstract TransformHint canTransformSchema(SchemaModel sm,
            SchemaGenerator.Pattern currentPattern, SchemaGenerator.Pattern targetPattern,
            List<Element> ges);
    
    /*
     * transforms schema using a a particular design pattern
     *
     */
    public abstract void transformSchema(SchemaModel sm,
            SchemaGenerator.Pattern targetPattern) throws IOException;
}

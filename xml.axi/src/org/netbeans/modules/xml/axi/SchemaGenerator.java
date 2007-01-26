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
package org.netbeans.modules.xml.axi;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.visitor.*;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Ayub Khan
 */
public abstract class SchemaGenerator extends DefaultVisitor {
    
    private SchemaGenerator.Mode mode;
    
    public static SchemaGenerator.Pattern DEFAULT_DESIGN_PATTERN = Pattern.RUSSIAN_DOLL;
    
    public enum Pattern {
        RUSSIAN_DOLL,
        VENITIAN_BLIND,
        GARDEN_OF_EDEN,
        SALAMI_SLICE,
        MIXED;
    }
    
    public enum Mode {
        TRANSFORM,
        UPDATE;
    }
    
    /**
     * Creates a new instance of SchemaGenerator
     */
    public SchemaGenerator(Mode mode) {
        super();
        this.mode = mode;
    }
    
    /*
     * returns mode
     *
     */
    public Mode getMode() {
        return mode;
    }
    
    /*
     * Updates schema using a a particular design pattern
     *
     */
    public abstract void updateSchema(SchemaModel sm) throws BadLocationException, IOException;
    
    /*
     * Transforms schema using a particular design pattern
     *
     */
    public abstract void transformSchema(SchemaModel sm) throws IOException;
    
    public void visit(Element element) {
        visitChildren(element);
    }
    
    public void visit(Attribute attribute) {
        visitChildren(attribute);
    }
    
    public void visit(Compositor compositor) {
        visitChildren(compositor);
    }
    
    protected void visitChildren(AXIComponent component) {
        for(AXIComponent child: component.getChildren()) {
            child.accept(this);
        }
    }
    
    public static interface UniqueId {
        int nextId();
    }
    
    public static interface PrimitiveCart {
        void add(Datatype d, SchemaComponent referer);
        Set<Map.Entry<SchemaComponent, Datatype>> getEntries();
        GlobalSimpleType getDefaultPrimitive();
        public GlobalSimpleType getPrimitiveType(String typeName);
    }
}

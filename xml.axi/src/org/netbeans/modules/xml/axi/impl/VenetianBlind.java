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

package org.netbeans.modules.xml.axi.impl;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.schema.model.*;

/**
 *
 * @author Ayub Khan
 */
public class VenetianBlind extends GardenOfEden {
    
    /**
     * Creates a new instance of VenetianBlind
     */
    public VenetianBlind(SchemaGenerator.Mode mode) {
        super(mode);
    }
    
    protected SchemaGenerator.Pattern getSchemaDesignPattern() {
        return SchemaGenerator.Pattern.VENITIAN_BLIND;
    }
    
    public void visit(Element element) {
        prepareLocalElement(element);
    }
    
    protected void setPeer(final Element element,
            final org.netbeans.modules.xml.schema.model.Element e,
            final ElementReference eref) {
        if(element.getPeer() != null && element.getChildren().size() > 0 &&
                SchemaGeneratorUtil.isGlobalElement(element)) {
            LocalType lct = SchemaGeneratorUtil.getLocalComplexType(e);
            if(lct == null)
                lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
            assert lct != null;
            scParent = lct;
        } else
            super.setPeer(element, e, eref);
    }
    
    protected SchemaComponent getParent(
            AXIComponent axiparent) throws IllegalArgumentException {
        SchemaComponent scParent = null;
        if(axiparent instanceof Element &&
                (SchemaGeneratorUtil.isGlobalElement(axiparent) /*||
                                        isSimpleElementStructure((Element)axiparent)*/)) {
            SchemaComponent e = axiparent.getPeer();
            if(e instanceof ElementReference)
                e = ((ElementReference)e).getRef().get();
            assert e != null;
            SchemaComponent lct = SchemaGeneratorUtil.getLocalComplexType(e);
            if(lct == null) {
                lct = SchemaGeneratorUtil.getGlobalComplexType(e);
                if(lct == null)
                    lct = SchemaGeneratorUtil.createLocalComplexType(sm, e);
            }
            assert lct != null;
            scParent = lct;
        } else {
            scParent = super.getParent(axiparent);
        }
        return scParent;
    }
}

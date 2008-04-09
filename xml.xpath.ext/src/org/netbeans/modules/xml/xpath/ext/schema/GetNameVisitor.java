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
package org.netbeans.modules.xml.xpath.ext.schema;

import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This schema visitor is inteneded to obtain name from a Schema Component.
 * 
 * It seems that the name can be obtained without any visitors. 
 * But there is well known problem with referenced elements. 
 * The ElementReference interface doesn't extend Named interface. 
 * But the ElementReferneceImpl class extends NamedImpl. 
 * So the code like the following works wrong: 
 * 
 *  if (schemaComponent instanceof Named) {
 *      ((Named)schemaComponent).getName();
 *  }
 * 
 * @author nk160297
 */
public class GetNameVisitor extends DefaultSchemaVisitor {

    private String mResultName = null;
    
    public GetNameVisitor() {
    }

    public String getName() {
        return mResultName;
    }
    
    // ----------------------------------------------
    
    @Override
    public void visit(AnyElement any) {
        mResultName = "Any";
    }
    
    @Override
    public void visit(GlobalSimpleType gst) {
        mResultName = gst.getName();
    }
    
    @Override
    public void visit(GlobalComplexType gt) {
        mResultName = gt.getName();
    }
    
    @Override
    public void visit(LocalAttribute la) {
        mResultName = la.getName();
    }
    
    @Override
    public void visit(GlobalAttribute ga) {
        mResultName = ga.getName();
    }
    
    @Override
    public void visit(LocalElement le) {
        mResultName = le.getName();
    }
    
    @Override
    public void visit(GlobalElement ge) {
        mResultName = ge.getName();
    }
    
    // --------------- References ------------------
    
    @Override
    public void visit(ElementReference er) {
        NamedComponentReference<GlobalElement> geRef = er.getRef();
        if (geRef != null) {
            GlobalElement ge = geRef.get();
            if (ge != null) {
                visit(ge);
            }
        }
    }
    
    @Override
    public void visit(AttributeReference ar) {
        NamedComponentReference<GlobalAttribute> gaRef = ar.getRef();
        if (gaRef != null) {
            GlobalAttribute ga = gaRef.get();
            if (ga != null) {
                visit(ga);
            }
        }
    }
    
    @Override
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> gagRef = agr.getGroup();
        if (gagRef != null) {
            GlobalAttributeGroup gag = gagRef.get();
            if (gag != null) {
                visit(gag);
            }
        }
    }
    
    @Override
    public void visit(GroupReference gr) {
        NamedComponentReference<GlobalGroup> ggRef = gr.getRef();
        if (ggRef != null) {
            GlobalGroup gg = ggRef.get();
            if (gg != null) {
                visit(gg);
            }
        }
    }
    
}

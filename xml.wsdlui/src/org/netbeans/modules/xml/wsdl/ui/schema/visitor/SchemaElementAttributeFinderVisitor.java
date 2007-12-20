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

/*
 * SchemaElementAttributeFinderVisitor.java
 *
 * Created on April 10, 2006, 3:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.schema.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.Attribute.Use;

/**
 *
 * @author radval
 */
public class SchemaElementAttributeFinderVisitor extends AbstractXSDVisitor {
    
    private List<Attribute> mAttrList = new ArrayList<Attribute>();
    
    private HashMap<String, Attribute> mAttrMap = new HashMap<String, Attribute>();
    
    private boolean noProhibited = false;
    
    
    private Element mElement = null;
    /** Creates a new instance of SchemaElementAttributeFinderVisitor */
    public SchemaElementAttributeFinderVisitor(Element elem) {
        mElement = elem;
    }
    
    public SchemaElementAttributeFinderVisitor(Element elem, boolean noProhibited) {
        mElement = elem;
        this.noProhibited = noProhibited;
    }
    
    public List<Attribute> getAttributes() {
        return this.mAttrList;
    }
    
    
    
    @Override
    public void visit(All all) {
        //Assuming no attributes can be defined at this level for the element (in which we are interested)
        //dont do anything
    }

    @Override
    public void visit(Choice choice) {
        //Assuming no attributes can be defined at this level for the element (in which we are interested)
        //dont do anything
    }

    @Override
    public void visit(GroupReference gr) {
        //Assuming no attributes can be defined at this level for the element (in which we are interested)
        //dont do anything
    }

    @Override
    public void visit(Sequence s) {
        //Assuming no attributes can be defined at this level for the element (in which we are interested)
        //dont do anything
    }

    
    
    @Override
    public void visit(GlobalElement ge) {
        if (ge.equals(mElement)) {
            super.visit(ge);
        }
    }

    @Override
    public void visit(LocalElement le) {
        if (le.equals(mElement)) {
            super.visit(le);
        }
    }

    @Override
    public void visit(LocalAttribute la) {

        if (noProhibited && la.getUse() != null && la.getUse().equals(Use.PROHIBITED)) {
            //if coming from restriction, removes it.
            if (mAttrMap.containsKey(la.getName())) {
                mAttrList.remove(mAttrMap.remove(la.getName()));
            }
            return;
        }
        if (!mAttrMap.containsKey(la.getName())) {
            mAttrMap.put(la.getName(), la);
            this.mAttrList.add(la);
        }
    }
    
    @Override
    public void visit(GlobalAttribute ga) {
        if (!mAttrMap.containsKey(ga.getName())) {
            mAttrMap.put(ga.getName(), ga);
            this.mAttrList.add(ga);
        }
    }
    
    
}

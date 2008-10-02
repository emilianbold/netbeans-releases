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

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This schema visitor is inteneded to look for a children elements or attributes 
 * It looks only children at the next lavel. 
 * 
 * @author nk160297
 */
public abstract class AbstractSchemaSearchVisitor extends DefaultSchemaVisitor {
    
    
    // Indicates if it necessary to look the global objects only.
    // It is used when the schema is the parent of searching.
    protected boolean lookGlobalOnly = false; 
   
    public AbstractSchemaSearchVisitor() {
    }
    
    /**
     * This method checks if the current schema component satisfies 
     * the search conditions.
     */ 
    protected abstract void checkComponent(SchemaComponent sc);

    // ----------------------------------------------
    
    @Override
    public void visit(LocalAttribute la) {
        checkComponent(la);
    }
    
    @Override
    public void visit(GlobalAttribute ga) {
        checkComponent(ga);
    }
    
    @Override
    public void visit(LocalElement le) {
        checkComponent(le);
    }
    
    @Override
    public void visit(GlobalElement ge) {
        checkComponent(ge);
    }
    
    // --------------- References ------------------
    
    @Override
    public void visit(ElementReference er) {
        // # 105159, #130053
        if (!isXdmDomUsed(er)) {
            checkComponent(er);
        }
        //
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
        // # 105159, #130053
        if (!isXdmDomUsed(ar)) {
            checkComponent(ar);
        }
        //
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
    
    // --------------- Visit containers -------------
    
    @Override
    public void visit(Schema s) {
        visitChildren(s);
    }
    
    @Override
    public void visit(All a) {
        visitChildren(a);
    }
    
    @Override
    public void visit(GlobalAttributeGroup gag) {
        visitChildren(gag);
    }
    
    @Override
    public void visit(Choice c) {
        visitChildren(c);
    }
    
    @Override
    public void visit(SimpleContent sc) {
        visitChildren(sc);
    }
    
    @Override
    public void visit(ComplexContent cc) {
        visitChildren(cc);
    }
    
    @Override
    public void visit(SimpleExtension se) {
        NamedComponentReference<GlobalType> gtRef = se.getBase();
        if (gtRef != null) {
            GlobalType gt = gtRef.get();
            if (gt != null) {
                visitChildren(gt);
            }
        }
        //
        visitChildren(se);
    }
    
    @Override
    public void visit(ComplexExtension ce) {
        NamedComponentReference<GlobalType> gtRef = ce.getBase();
        if (gtRef != null) {
            GlobalType gt = gtRef.get();
            if (gt != null) {
                visitChildren(gt);
            }
        }
        //
        visitChildren(ce);
    }
   @Override
   public void visit(GlobalComplexType gct) {
       if (!lookGlobalOnly) {
           visitChildren(gct);
       }
   }

   @Override
   public void visit(LocalComplexType lct) {
       if (!lookGlobalOnly) {
           visitChildren(lct);
       }
   }

   @Override
   public void visit(GlobalGroup gg) {
       if (!lookGlobalOnly) {
           visitChildren(gg);
       }
   } 

    @Override
    public void visit(Redefine r) {
        visitChildren(r);
    }
    
    @Override
    public void visit(ComplexContentRestriction ccr) {
        visitChildren(ccr);
    }
    
    @Override
    public void visit(Sequence s) {
        visitChildren(s);
    }
    
    @Override
    public void visit(Union u) {
        visitChildren(u);
    }
    
    // ----------------------------------------------
    
    protected void visitChildren(SchemaComponent sc) {
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
        }
    }
    
    //-----------------------------------------------
    
    /**
     * This auxiliary method is a workaround fro the issuer #130053
     * @param sc
     * @return
     */
    protected boolean isXdmDomUsed(SchemaComponent sc) {
        org.w3c.dom.Element domElement = sc.getPeer();
        String packageName = domElement.getClass().getPackage().getName();
        return "org.netbeans.modules.xml.xdm.nodes".equals(packageName); // NOI18N
    }
 
    protected String fastGetRefName(NamedComponentReference ref) {
        if (ref == null) {
            return null;
        }
        //
        String refString = ref.getRefString();
        String[] splitRefString = refString.split(":", 2);
        String result = null;
        //
        if (splitRefString.length == 1) {
            result = splitRefString[0];
        } else if (splitRefString.length == 2) {
            result = splitRefString[1];
        }
        return result;
    }
    
}

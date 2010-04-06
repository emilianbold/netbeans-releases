/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.xpath;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
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
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This schema visitor is inteneded to look for a child element or attribute 
 * It looks only children at the next lavel. 
 * The method visitChildren() has to be used. 
 * The name and namespace of the sought children is specified in the constructor.
 * 
 * The special case is looking for a global element or attribute. 
 * In this case the parent schema component is the Schema object. 
 * 
 * @author nk160297
 */
public class FindChildSchemaVisitor extends DefaultSchemaVisitor {
    
    private String mySoughtName;
    private String mySoughtNamespace;
    private boolean isAttribute; // hints that the sought object is an attribute
    
    private SchemaComponent myFound = null;
    
    public FindChildSchemaVisitor(
            String soughtName, String soughtNamespace, boolean isAttribute) {
        mySoughtName = soughtName;
        mySoughtNamespace = soughtNamespace;
        this.isAttribute = isAttribute;
    }
    
    public boolean isChildFound() {
        return myFound != null;
    }
    
    public SchemaComponent getFound() {
        return myFound;
    }
    
    /**
     * Looks for nested element or attribute. 
     * The name and namespace of the required subcomponent is specified in the constructor.
     * The result can be found with the help of the getFound() method.
     */ 
    public void lookForSubcomponent(SchemaComponent sc) {
        if (sc instanceof Element) {
            assert sc instanceof TypeContainer;
            //
            NamedComponentReference<? extends GlobalType> typeRef = 
                    ((TypeContainer)sc).getType();
            if (typeRef != null) {
                GlobalType globalType = typeRef.get();
                if (globalType != null) {
                    globalType.accept(this);
                }
            }
            //
            if (!isChildFound()) {
                LocalType localType = ((TypeContainer)sc).getInlineType();
                if (localType != null) {
                    localType.accept(this);
                }
            }
        } else if (sc instanceof ComplexType) {
            visitChildren(sc);
        } else if (sc instanceof Schema) {
            // Look for a global schema object
            visitChildren(sc);
        } else {
            // Other elements can't containg nested elements or attributes
        }
    }
    
    // ----------------------------------------------
    
    public void visit(LocalAttribute la) {
        if (isAttribute) {
            checkComponent(la);
        }
    }
    
    public void visit(GlobalAttribute ga) {
        if (isAttribute) {
            checkComponent(ga);
        }
    }
    
    public void visit(LocalElement le) {
        if (!isAttribute) {
            checkComponent(le);
        }
    }
    
    public void visit(GlobalElement ge) {
        if (!isAttribute) {
            checkComponent(ge);
        }
    }
    
    // --------------- References ------------------
    
    public void visit(ElementReference er) {
        // # 105159
        if ( !isAttribute) {
            checkComponent(er);
        }
        if (isChildFound()) {
            return;
        }
        NamedComponentReference<GlobalElement> geRef = er.getRef();

        if (geRef != null) {
            GlobalElement ge = geRef.get();
            if (ge != null) {
                visit(ge);
            }
        }
    }
    
    public void visit(AttributeReference ar) {
        NamedComponentReference<GlobalAttribute> gaRef = ar.getRef();
        if (gaRef != null) {
            GlobalAttribute ga = gaRef.get();
            if (ga != null) {
                visit(ga);
            }
        }
    }
    
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> gagRef = agr.getGroup();
        if (gagRef != null) {
            GlobalAttributeGroup gag = gagRef.get();
            if (gag != null) {
                visit(gag);
            }
        }
    }
    
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
    
    
    public void visit(Schema s) {
        visitChildren(s);
    }
    
    public void visit(All a) {
        visitChildren(a);
    }
    
    public void visit(GlobalAttributeGroup gag) {
        visitChildren(gag);
    }
    
    public void visit(Choice c) {
        visitChildren(c);
    }
    
    public void visit(SimpleContent sc) {
        visitChildren(sc);
    }
    
    public void visit(ComplexContent cc) {
        visitChildren(cc);
    }
    
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
    
    public void visit(GlobalComplexType gct) {
        visitChildren(gct);
    }
    
    public void visit(LocalComplexType lct) {
        visitChildren(lct);
    }
    
    public void visit(GlobalGroup gg) {
        visitChildren(gg);
    }
    
    public void visit(Redefine r) {
        // NOT ABSOLUTELY CLEAR YET
        visitChildren(r);
    }
    
    public void visit(ComplexContentRestriction ccr) {
        visitChildren(ccr);
    }
    
    public void visit(Sequence s) {
        visitChildren(s);
    }
    
    public void visit(Union u) {
        visitChildren(u);
    }
    
    // ----------------------------------------------
    
    private void visitChildren(SchemaComponent sc) {
        if (isChildFound()) {
            return;
        }
        //
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
            //
            if (isChildFound()) {
                return;
            }
        }
    }
    
    private void checkComponent(SchemaComponent sc) {
        if (sc instanceof Named) {
            String namespace = sc.getModel().getEffectiveNamespace(sc);
            String name = ((Named)sc).getName();
            if (mySoughtName.equals(name)) {
                //
                // Compare namespace as well if it is specified
                if (mySoughtNamespace != null) {
                    if (mySoughtNamespace.equals(namespace)) {
                        myFound = sc;
                    }
                } else {
                    myFound = sc;
                }
            } 
        }
    }
}

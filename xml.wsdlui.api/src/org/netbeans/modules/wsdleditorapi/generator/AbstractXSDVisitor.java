/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * AbstractXSDVisitor.java
 *
 * Created on March 28, 2006, 6:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdleditorapi.generator;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.FractionDigits;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Key;
import org.netbeans.modules.xml.schema.model.KeyRef;
import org.netbeans.modules.xml.schema.model.Length;
import org.netbeans.modules.xml.schema.model.List;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalGroupDefinition;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.MaxExclusive;
import org.netbeans.modules.xml.schema.model.MaxInclusive;
import org.netbeans.modules.xml.schema.model.MaxLength;
import org.netbeans.modules.xml.schema.model.MinExclusive;
import org.netbeans.modules.xml.schema.model.MinInclusive;
import org.netbeans.modules.xml.schema.model.MinLength;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Selector;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SequenceDefinition;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentDefinition;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.SimpleTypeDefinition;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.TotalDigits;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.Unique;
import org.netbeans.modules.xml.schema.model.Whitespace;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public abstract class AbstractXSDVisitor implements XSDVisitor {
    
    /** Creates a new instance of AbstractXSDVisitor */
    public AbstractXSDVisitor() {
    }
    
    public void visit(All all) {
            Collection<LocalElement> allElements = all.getElements();
            Iterator<LocalElement> it = allElements.iterator();
            while(it.hasNext()) {
                LocalElement element = it.next();
                visit(element);
            }
    }
    
    
    public void visit(Annotation ann) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(AnyAttribute anyAttr) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(AnyElement any) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(AppInfo appinfo) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> gagRef = agr.getGroup();
        if(gagRef != null && gagRef.get() != null) {
            visit(gagRef.get());
        }
    }
    
    public void visit(AttributeReference reference) {
        NamedComponentReference<GlobalAttribute> gaRef = reference.getRef();
        if(gaRef != null && gaRef.get() != null) {
            visit(gaRef.get());
        }
        
    }
    
    public void visit(Choice choice) {
            java.util.List<SchemaComponent> children =  choice.getChildren();
            Iterator<SchemaComponent> it = children.iterator();

            while(it.hasNext()) {
                SchemaComponent comp = it.next();
                if(comp instanceof AnyElement) {
                    visit((AnyElement) comp);
                } else if(comp instanceof Choice) {
                    visit((Choice) comp);
                } else if(comp instanceof ElementReference) {
                    visit((ElementReference) comp);
                } else if(comp instanceof GroupReference) {
                    visit((GroupReference) comp);
                } else if(comp instanceof LocalElement) {
                    visit((LocalElement) comp);
                } else if(comp instanceof Sequence) {
                    visit((Sequence) comp);
                } 
            }
    }

   
    
    public void visit(Documentation d) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(ElementReference er) {
        NamedComponentReference<GlobalElement> ref = er.getRef();
        if(ref != null && ref.get() != null) {
            visit(ref.get());
        }
        
    }
    
    public void visit(Enumeration e) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Field f) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(FractionDigits fd) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(GlobalAttribute ga) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(GlobalAttributeGroup gag) {
        java.util.List<SchemaComponent> children = gag.getChildren();
                Iterator<SchemaComponent> it = children.iterator();
                
                while(it.hasNext()) {
                    SchemaComponent sc = it.next();
                    if(sc instanceof  LocalAttribute) {
                        visit((LocalAttribute) sc);
                    } else if(sc instanceof AttributeReference) {
                        visit((AttributeReference) sc );
                    } else if(sc instanceof  AttributeGroupReference) {
                        visit((AttributeGroupReference) sc);
                    }
                }
                
}
    
    
    public void visit(GlobalElement ge) {
        visitTypeContainer(ge);
    }
   
    
    public void visit(GroupReference gr) {
        NamedComponentReference<GlobalGroup> ggRef = gr.getRef();
        if(ggRef != null && ggRef.get() != null) {
            visit(ggRef.get());
        }
        
    }
    
    
    public void visit(Import im) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Include include) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Key key) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(KeyRef kr) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Length length) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(List l) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(LocalAttribute la) {
        // TODO Auto-generated method stub
        
    }
    
    
    public void visit(LocalElement le) {
        visitTypeContainer(le);
    }
    
    
    public void visit(MaxExclusive me) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(MaxInclusive mi) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(MaxLength ml) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(MinExclusive me) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(MinInclusive mi) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(MinLength ml) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Notation notation) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Pattern p) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Redefine rd) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Schema s) {
        java.util.List<SchemaComponent> children =  s.getChildren();
        Iterator<SchemaComponent> it = children.iterator();
       
        while(it.hasNext()) {
            SchemaComponent sc = it.next();
            if(sc instanceof GlobalAttributeGroup) {
                visit((GlobalAttributeGroup) sc);
            }else if(sc instanceof GlobalAttribute) {
                visit((GlobalAttribute) sc);
            }else if(sc instanceof GlobalComplexType) {
                visit((GlobalComplexType) sc);
            }else if(sc instanceof GlobalElement) {
                visit((GlobalElement) sc);
            }else if(sc instanceof GlobalGroup) {
                visit((GlobalGroup) sc);
            }else if(sc instanceof Import) {
                visit((Import) sc);
            }else if(sc instanceof Include) {
                visit((Include) sc);
            }else if(sc instanceof Redefine) {
                visit((Redefine) sc);
            }
        }
    }
    
    public void visit(Selector s) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(SimpleContent sc) {
        SimpleContentDefinition scd = sc.getLocalDefinition();
        if(scd instanceof  SimpleContentRestriction) {
            visit((SimpleContentRestriction) scd);
        } else if(scd instanceof SimpleExtension) {
            visit((SimpleExtension) scd);
        }
        
    }
    
    public void visit(SimpleContentRestriction scr) {
        NamedComponentReference<GlobalType> baseRef = scr.getBase();
        if(baseRef != null && baseRef.get() != null) {
            visit(baseRef.get());
        } 
        
        java.util.List<SchemaComponent> children =  scr.getChildren();
        Iterator<SchemaComponent> it = children.iterator();
        
        while(it.hasNext()) {
            SchemaComponent sc = it.next();
            if(sc instanceof AttributeGroupReference) {
                visit((AttributeGroupReference) sc);
            } else if(sc instanceof AttributeReference) {
                visit((AttributeReference) sc);
            } else if(sc instanceof Enumeration) {
                visit((Enumeration) sc);
            } else if(sc instanceof FractionDigits) {
                visit((FractionDigits) sc);
            } else if(sc instanceof  LocalAttribute) {
                visit((LocalAttribute) sc);
            }else if(sc instanceof LocalSimpleType) {
                visit((LocalSimpleType) sc);
            }else if(sc instanceof Length) {
                visit((Length) sc);
            }else if(sc instanceof MaxExclusive) {
                visit((MaxExclusive) sc);
            }else if(sc instanceof MinExclusive) {
                visit((MinExclusive) sc);
            }else if(sc instanceof MaxLength) {
                visit((MaxLength) sc);
            }else if(sc instanceof MinLength) {
                visit((MinLength) sc);
            }
        }
        
    }
    
    public void visit(SimpleExtension se) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(SimpleTypeRestriction str) {
        NamedComponentReference<GlobalSimpleType> baseRef =  str.getBase();
        if(baseRef != null && baseRef.get() != null) {
            visit(baseRef.get());
        }
        
        
    }
    
    public void visit(TotalDigits td) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Union u) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Unique u) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(Whitespace ws) {
        // TODO Auto-generated method stub
        
    }
    
    public void visit(LocalComplexType type) {
        visit((ComplexType) type);
    }
    
    public void visit(LocalSimpleType type) {
        visit((SimpleType) type);
    }
    
    public void visit(Sequence s) {
        visit(s.getContent());
    }
    
    
    public void visit(ComplexContentRestriction ccr) {
        NamedComponentReference<GlobalComplexType> baseRef = ccr.getBase();
        if(baseRef != null) {
            GlobalComplexType gType = baseRef.get();
            if(gType != null) {
                visit(gType);
            }
        }
        
        java.util.List children = ccr.getChildren();
        Iterator it = children.iterator();
        while(it.hasNext()) {
            Object child = it.next();
            
            if(child instanceof AnyAttribute) {
                visit((AnyAttribute) child);
            } else if(child instanceof AttributeGroupReference) {
                visit((AttributeGroupReference) child);
            } else if(child instanceof AttributeReference) {
                visit((AttributeReference) child);
            } else if (child instanceof LocalAttribute) {
                visit((LocalAttribute) child);
            } else if (child instanceof ComplexTypeDefinition) {
                visit((ComplexTypeDefinition) child);
            }
        }
    }
    
    public void visit(ComplexExtension ce) {
        NamedComponentReference<GlobalType> baseRef = ce.getBase();
        if(baseRef != null) {
            GlobalType gType = baseRef.get();
            if(gType != null) {
                visit(gType);
            }
        }
        
        java.util.List children = ce.getChildren();
        Iterator it = children.iterator();
        while(it.hasNext()) {
            Object child = it.next();
            
            if(child instanceof LocalAttribute) {
                visit((LocalAttribute) child);
            }else if(child instanceof AnyAttribute) {
                visit((AnyAttribute) child);
            } else if(child instanceof AttributeGroupReference) {
                visit((AttributeGroupReference) child);
            } else if(child instanceof AttributeReference) {
                visit((AttributeReference) child);
            } else if (child instanceof ComplexExtensionDefinition) {
                visit((ComplexExtensionDefinition) child);
            }
        }
    }
    
    public void visit(ComplexContent cc) {
            ComplexContentDefinition ccd = cc.getLocalDefinition();
            if(ccd != null) {
                    visit(ccd);
            }
    }

    

    

    public void visit(GlobalComplexType gct) {
            visit((ComplexType) gct);
    }


    public void visit(GlobalGroup gd) {
            LocalGroupDefinition lgd = gd.getDefinition();
            if(lgd != null) {
                if(lgd instanceof Choice) {
                    visit((Choice) lgd);
                } else if(lgd instanceof All) {
                    visit((All) lgd );
                } else if(lgd instanceof  Sequence) {
                    visit((Sequence)lgd);
                }
            }
    }

    public void visit(GlobalSimpleType gst) {
            visit((SimpleType) gst);
    }
    
    protected void visitTypeContainer(TypeContainer tc) {
        if(tc.getType() != null) {
            GlobalType gt = tc.getType().get();
            visit(gt);
        } else {
            LocalType lt = tc.getInlineType();
            visit(lt);
        }
    }
    
    protected void visit(GlobalType gt) {
        if(gt instanceof ComplexType) {
            visit((GlobalComplexType) gt);
        } else if(gt instanceof SimpleType) {
            visit((GlobalSimpleType) gt);
        }
    }
    
    protected void visit(LocalType lt) {
        if(lt instanceof ComplexType) {
            visit((LocalComplexType) lt);
        } else if(lt instanceof SimpleType) {
            visit((LocalSimpleType) lt);
        }
    }
    
   private void visit(ComplexTypeDefinition ctd) {
        if (ctd instanceof All) {
            visit((All) ctd);
        } else if (ctd instanceof Choice) {
            visit((Choice) ctd);
        } else if (ctd instanceof Sequence) {
            visit((Sequence) ctd);
        } else if(ctd instanceof ComplexContent) {
            visit((ComplexContent) ctd);
        } else if(ctd instanceof GroupReference) {
            visit((GroupReference) ctd);
        } else if (ctd instanceof SimpleContent) {
            visit((SimpleContent) ctd);
        }
    }
     
    private void visit(ComplexContentDefinition ccd) {
        
        if(ccd instanceof ComplexContentRestriction) {
            visit((ComplexContentRestriction) ccd);
        } else if(ccd instanceof ComplexExtension) {
            visit((ComplexExtension) ccd);
        }
        
    }
    
    private void visit(ComplexExtensionDefinition ced) {
        if (ced instanceof All) {
            visit((All) ced);
        } else if (ced instanceof Choice) {
            visit((Choice) ced);
        } else if (ced instanceof Sequence) {
            visit((Sequence) ced);
        } else if(ced instanceof GroupReference) {
            visit((GroupReference) ced);
        }
    }
    
    private void visit(java.util.List<SequenceDefinition> sdList) {
        Iterator<SequenceDefinition> it = sdList.iterator();
        while(it.hasNext()) {
            SequenceDefinition sd = it.next();
            if(sd instanceof Sequence) {
                visit((Sequence) sd);
            } else if(sd instanceof AnyElement) {
                visit((AnyElement) sd);
            } else if(sd instanceof Choice) {
                visit((Choice) sd);
            } else if(sd instanceof ElementReference) {
                visit((ElementReference) sd);
            } else if(sd instanceof GroupReference) {
                visit((GroupReference) sd);
            } else if(sd instanceof LocalElement) {
                visit((LocalElement) sd);
            }
            
        }
    }
    
    private void visit(SimpleType st) {
        SimpleTypeDefinition std = st.getDefinition();
        if(std != null) {
            visit(std);
        }
    }
    
    private void visit(SimpleTypeDefinition std) {
        if(std instanceof List) {
            visit((List) std);
        } else if(std instanceof Union) {
            visit((Union) std);
        } else if(std instanceof SimpleTypeRestriction) {
            visit((SimpleTypeRestriction) std);
        }
    }
    
    private void visit(ComplexType ct) {
        java.util.List<SchemaComponent> children =  ct.getChildren();
        Iterator<SchemaComponent> it = children.iterator();
        while(it.hasNext()) {
            SchemaComponent sc = it.next();
            if(sc instanceof  AnyAttribute) {
                visit((AnyAttribute) sc );
            } else if(sc instanceof AttributeGroupReference) {
                visit((AttributeGroupReference) sc);
            }else if(sc instanceof AttributeReference) {
                visit((AttributeReference) sc);
            }else if(sc instanceof LocalAttribute) {
                visit((LocalAttribute) sc);
            }else if(sc instanceof ComplexTypeDefinition) {
                visit((ComplexTypeDefinition) sc);
            }
        }
        
        //search TypeContainer
        //getAttributeGroupReferences
        
    }
}

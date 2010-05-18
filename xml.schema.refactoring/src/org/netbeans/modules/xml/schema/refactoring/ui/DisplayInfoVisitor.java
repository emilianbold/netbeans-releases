/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * DisplayInfoVisitor.java
 *
 * Created on October 27, 2005, 6:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.ui;

import java.text.MessageFormat;
import java.util.List;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.Named;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Jeri Lockhart
 */
public class DisplayInfoVisitor extends DefaultSchemaVisitor {
    
    private DisplayInfo info;
    private static final String EMPTY_STRING = ""; // NOI18N
    
    /** Creates a new instance of DisplayInfoVisitor */
    public DisplayInfoVisitor() {
        
    }
    
    public DisplayInfo getDisplayInfo(Component comp){
        info = new DisplayInfo();
        if (comp instanceof Named){
            info.setName(((Named)comp).getName());
        }
        if (comp instanceof GlobalSimpleType){
            visit((GlobalSimpleType)comp);
        } else if (comp instanceof GlobalComplexType){
            visit((GlobalComplexType)comp);
        } else if (comp instanceof LocalComplexType){
            visit((LocalComplexType)comp);
        } else if (comp instanceof GlobalElement){
            visit((GlobalElement)comp);
        } else if (comp instanceof LocalElement){
            visit((LocalElement)comp);
        } else if (comp instanceof Sequence){
            visit((Sequence)comp);
        } else if (comp instanceof ComplexContent){
            visit((ComplexContent)comp);
        } else if (comp instanceof SimpleContentRestriction){
            visit((SimpleContentRestriction)comp);
        } else if (comp instanceof ComplexContentRestriction){
            visit((ComplexContentRestriction)comp);
        } else if (comp instanceof SimpleContent){
            visit((SimpleContent)comp);
        } else if (comp instanceof ComplexExtension){
            visit((ComplexExtension)comp);
        } else if (comp instanceof SimpleExtension){
            visit((SimpleExtension)comp);
        } else if (comp instanceof SimpleTypeRestriction){
            visit((SimpleTypeRestriction)comp);
        } else if (comp instanceof LocalSimpleType){
            visit((LocalSimpleType)comp);
        } else if (comp instanceof GlobalAttribute){
            visit((GlobalAttribute)comp);
        } else if (comp instanceof GlobalAttributeGroup){
            visit((GlobalAttributeGroup)comp);
        } else if (comp instanceof GlobalGroup){
            visit((GlobalGroup)comp);
        } else if (comp instanceof LocalAttribute){
            visit((LocalAttribute)comp);
        } else if (comp instanceof ElementReference){
            visit((ElementReference)comp);
        } else if (comp instanceof Union){
            visit((Union)comp);
        } else if (comp instanceof Schema){
            visit((Schema)comp);
        } else if (comp instanceof Redefine){
            visit((Redefine)comp);
        } else {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "DisplayInfoVisitor unrecognized type  "
                    + comp==null?"":comp.getClass().toString());    //NOI18N
        }
        return info;
        
    }
    
    @Override
            public void visit(GlobalSimpleType gst) {
        super.visit(gst);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Global_Simple_Type"));
    }
    
    
    @Override
            public void visit(GlobalElement ge) {
        
        super.visit(ge);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Global_Element"));
        
        NamedComponentReference type = ge.getType();
        if (type != null){
            GlobalType gt = ge.getType().get();
            // TODO Replace this workaround for CR 6373793
            info.setElementType(gt==null?
                NbBundle.getMessage(DisplayInfoVisitor.class,"LBL_Undetermined_Type")
                :gt.getName());
        }
        
    }
    
    
    @Override
            public void visit(GlobalComplexType gct) {
        
        super.visit(gct);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Global_Complex_Type"));
    }
    
    @Override
            public void visit(LocalElement le) {
        
        super.visit(le);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Local_Element"));
        
        NamedComponentReference type = le.getType();
        if (type != null){
            GlobalType gt = le.getType().get();
            // TODO Replace this workaround for CR 6373793
            info.setElementType(gt==null?
                NbBundle.getMessage(DisplayInfoVisitor.class,"LBL_Undetermined_Type")
                :gt.getName());
        }
    }
    
    @Override
            public void visit(Sequence s) {
        
        super.visit(s);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Sequence"));
    }
    
    @Override
            public void visit(LocalComplexType type) {
        
        super.visit(type);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Local_Complex_Type"));
    }
    
    @Override
            public void visit(ComplexContent cc) {
        
        super.visit(cc);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_ComplexContent"));
    }
    
    @Override
            public void visit(SimpleContentRestriction scr) {
        
        super.visit(scr);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_SimpleContentRestriction"));
    }
    
    @Override
            public void visit(ComplexContentRestriction ccr) {
        
        super.visit(ccr);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_ComplexContentRestriction"));
    }
    
    @Override
            public void visit(SimpleContent sc) {
        
        super.visit(sc);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_SimpleContent"));
    }
    
    @Override
            public void visit(ComplexExtension ce) {
        
        super.visit(ce);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_ComplexExtension"));
    }
    
    @Override
            public void visit(SimpleExtension se) {
        
        super.visit(se);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_SimpleExtension"));
    }
    
    @Override
            public void visit(SimpleTypeRestriction str) {
        
        super.visit(str);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_SimpleTypeRestriction"));
    }
    
    @Override
            public void visit(LocalSimpleType type) {
        
        super.visit(type);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_LocalSimpleType"));
    }
    
    @Override
            public void visit(GlobalAttribute ga) {
        
        super.visit(ga);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_GlobalAttribute"));
        
        NamedComponentReference type = ga.getType();
        if (type != null){
            GlobalType gt = ga.getType().get();
            // TODO Replace this workaround for CR 6373793
            info.setElementType(gt==null?
                NbBundle.getMessage(DisplayInfoVisitor.class,"LBL_Undetermined_Type")
                :gt.getName());
        }
    }
    
    @Override
            public void visit(GlobalAttributeGroup gag) {
        
        super.visit(gag);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_GlobalAttributeGroup"));
    }
    
    @Override
            public void visit(GlobalGroup gd) {
        
        super.visit(gd);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_GlobalGroup"));
    }
    
    @Override
            public void visit(LocalAttribute la) {
        
        super.visit(la);
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_LocalAttribute"));
        
        NamedComponentReference type = la.getType();
        if (type != null){
            GlobalType gt = la.getType().get();
            // TODO Replace this workaround for CR 6373793
            info.setElementType(gt==null?
                NbBundle.getMessage(DisplayInfoVisitor.class,"LBL_Undetermined_Type")
                :gt.getName());
        }
    }
    
    public void visit(ElementReference er) {
        super.visit(er);
        // has no name
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Local_Element"));      
        
        info.setElementType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Global_Element"));
        
        NamedComponentReference<GlobalElement> ref = 
                    er.getRef();
            if (ref != null){
                info.setName(MessageFormat.format(NbBundle.getMessage(
                        DisplayInfoVisitor.class,
                        "LBL_References_Ref"),
                        new Object[] {ref.getQName().getLocalPart()}));
            }
                
                 
                        
    }
     
    public void visit(Schema schema) {
        super.visit(schema);
        // has no name
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Schema"));      
        
       info.setName(schema.getTargetNamespace());
                
                 
                        
    }
     
    public void visit(Union union) {
        super.visit(union);
        // has no name
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Union")); 
        
        List<NamedComponentReference<GlobalSimpleType>> members =
                union.getMemberTypes();
        StringBuffer names = new StringBuffer(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Union_Members"));
        names.append(" ");  //NOI18N
        if (members.size() < 1){
            names.append(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Union_Members_None"));
        }
        else {
            for (NamedComponentReference n:members){
                names.append(n.getQName().toString());
                names.append(" ");//NOI18N
            }
        }
        // Named Members: TypeA TypeB TypeC
        // Named Members: [none]
       info.setName(names.toString());
                        
    }
    
     
    public void visit(Redefine redefine) {
        super.visit(redefine);
        // has no name
        info.setCompType(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Redefine"));  
        
        info.setName(NbBundle.getMessage(DisplayInfoVisitor.class,
                "LBL_Redefinition"));
                        
    }
    
    
    public static class DisplayInfo {
        
        private String name = EMPTY_STRING;
        private String compType = EMPTY_STRING;
        private String elementType = EMPTY_STRING;
        
        
        public DisplayInfo() {
            
        }
        
        /**
         * Getter for property name.
         * @return Value of property name.
         */
        public String getName() {
            
            return this.name;
        }
        
        /**
         * Setter for property name.
         * @param name New value of property name.
         */
        public void setName(String name) {
            if (name == null ){
                name = EMPTY_STRING;
            }
            
            this.name = name;
        }
        
        
        /**
         * Getter for property compType.
         * @return Value of property compType.
         */
        public String getCompType() {
            
            return this.compType;
        }
        
        /**
         * Setter for property compType.
         * @param compType New value of property compType.
         */
        public void setCompType(String compType) {
            if (compType == null ){
                compType = EMPTY_STRING;
            }
            
            this.compType = compType;
        }
        
        
        /**
         * The type of the GlobalElement, LocalElement, GlobalAttribute, or LocalAttribute
         * Getter for property elementType.
         * @return Value of property elementType.
         */
        public String getElementType() {
            
            return this.elementType;
        }
        
        /**
         * The type of the GlobalElement, LocalElement, GlobalAttribute, or LocalAttribute
         * @param elementType New value of property elementType.
         */
        public void setElementType(String elementType) {
            if (elementType == null ){
                elementType = EMPTY_STRING;
            }
            
            this.elementType = elementType;
        }
        
    }
    
    
}

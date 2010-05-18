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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.editor.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class XSDToIEPAttributeNameVisitor extends AbstractXSDVisitor {

    private List<AttributeNameToType> mAttrNameList = new ArrayList<AttributeNameToType>();
    
    private Collection<GlobalSimpleType> mBuiltInSimpleTypes;
    
    
    public XSDToIEPAttributeNameVisitor() {
        SchemaModel model = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        mBuiltInSimpleTypes = model.getSchema().getSimpleTypes();
    }
    
    public List<AttributeNameToType> getAttributeNameToTypeList() {
        return this.mAttrNameList;
    }
    
    @Override
    public void visit(GlobalElement ge) {
        super.visit(ge);
       
    }

    @Override
    public void visit(GlobalComplexType gct) {
        super.visit(gct);
        
    }

    
    @Override
    protected void visit(GlobalType gt) {
        super.visit(gt);
    }
    

    @Override
    public void visit(GlobalAttribute ga) {
        String name = ga.getName();
        String typeName = null;
        
        Object  type = null;
        
        LocalType lt = ga.getInlineType();
        if(lt == null) {
            NamedComponentReference typeRef = ga.getType();
            type = typeRef.get();
        }
        
        if(type instanceof GlobalSimpleType) {
            GlobalSimpleType st = (GlobalSimpleType) type;
            
            if(mBuiltInSimpleTypes.contains(st)) {
                typeName = st.getName();
            }
            
            String iepType = XSDTypeToIEPTypeConvertor.getIEPType(typeName);
        
            AttributeNameToType nameToType = new AttributeNameToType(name, iepType);
        
            this.mAttrNameList.add(nameToType);
        }
    }

    @Override
    public void visit(LocalAttribute la) {
        String name = la.getName();
        String typeName = null;
        
        Object  type = null;
        
        LocalType lt = la.getInlineType();
        if(lt == null) {
            NamedComponentReference typeRef = la.getType();
            type = typeRef.get();
        }
        
        if(type instanceof GlobalSimpleType) {
            GlobalSimpleType st = (GlobalSimpleType) type;
            
            if(mBuiltInSimpleTypes.contains(st)) {
                typeName = st.getName();
            }
            
            String iepType = XSDTypeToIEPTypeConvertor.getIEPType(typeName);
        
            AttributeNameToType nameToType = new AttributeNameToType(name, iepType);
        
            this.mAttrNameList.add(nameToType);
        } 
    }

    @Override
    public void visit(LocalElement le) {
        String name = le.getName();
        String typeName = null;
        
        Object  type = null;
        
        LocalType lt = le.getInlineType();
        if(lt == null) {
            NamedComponentReference typeRef = le.getType();
            type = typeRef.get();
            
        }
        
        if(type instanceof GlobalSimpleType) {
            GlobalSimpleType st = (GlobalSimpleType) type;
            
            if(mBuiltInSimpleTypes.contains(st)) {
                typeName = st.getName();
            }
            
            String iepType = XSDTypeToIEPTypeConvertor.getIEPType(typeName);
        
            AttributeNameToType nameToType = new AttributeNameToType(name, iepType);
        
            this.mAttrNameList.add(nameToType);
        } 
        
        
    }

    
    public static class AttributeNameToType {
        
        private String mAttributeName;
        
        private String mAttributeType;
        
        public AttributeNameToType(String name, String type) {
            this.mAttributeName = name;
            this.mAttributeType = type;
        }
        
        public String getAttributeName() {
            return this.mAttributeName;
        }
        
        public String getAttributeType() {
            return this.mAttributeType;
        }
        
    }
}

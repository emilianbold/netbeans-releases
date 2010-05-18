/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.iep.editor.xsd.nodes;

import java.util.Collection;
import org.netbeans.modules.iep.editor.wizard.AbstractXSDVisitor;
import org.netbeans.modules.iep.editor.wizard.XSDTypeToIEPTypeConvertor;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class SchemaComponentIEPTypeFinderVisitor extends AbstractXSDVisitor {

    private String mIEPType = null;
    
    private Collection<GlobalSimpleType> mBuiltInSimpleTypes;
    
    
    public SchemaComponentIEPTypeFinderVisitor() {
        SchemaModel model = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        mBuiltInSimpleTypes = model.getSchema().getSimpleTypes();
    }
    
   
    public String getIEPType() {
        return mIEPType;
    }
    
    @Override
    public void visit(GlobalElement ge) {
        super.visit(ge);
    }
    
    protected void visit(GlobalType gt) {
        if(gt instanceof ComplexType) {
            mIEPType = XSDTypeToIEPTypeConvertor.getIEPCLOBType();
        } else if(gt instanceof SimpleType) {
            //visit((GlobalSimpleType) gt);
            visit((GlobalSimpleType) gt);
        }
    }
    
    protected void visit(LocalType lt) {
        if(lt instanceof ComplexType) {
            mIEPType = XSDTypeToIEPTypeConvertor.getIEPCLOBType();
        } else if(lt instanceof SimpleType) {
            visit((LocalSimpleType) lt);
        }
    }
    
    public void visit(GlobalSimpleType gst) {
        if(mBuiltInSimpleTypes.contains(gst)) {
            String typeName = gst.getName();
            mIEPType = XSDTypeToIEPTypeConvertor.getIEPType(typeName);
        } else {
            //user defined simple type which probably extends simple type
            super.visit(gst);
        }
    }
    
  
    @Override
    public void visit(GlobalAttribute ga) {
        
        LocalType lt = ga.getInlineType();
        if(lt != null) {
            visit(lt);
        } else {
            NamedComponentReference<GlobalSimpleType> typeRef = ga.getType();
            if(typeRef != null && typeRef.get() != null) {
                visit(typeRef.get());
            }
        }
        
    }

    @Override
    public void visit(LocalAttribute la) {
        LocalType lt = la.getInlineType();
        if(lt != null) {
            visit(lt);
        } else {
            NamedComponentReference<GlobalSimpleType> typeRef = la.getType();
            if(typeRef != null && typeRef.get() != null) {
                visit(typeRef.get());
            }
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


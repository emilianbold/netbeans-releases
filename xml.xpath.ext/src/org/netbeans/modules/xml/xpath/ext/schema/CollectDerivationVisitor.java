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

package org.netbeans.modules.xml.xpath.ext.schema;

import java.util.HashMap;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This visitor populates a derivation map based on a schema model. 
 * The key is a deriving Schema component. The value is a Schema component 
 * from which the key component is derived. 
 * 
 * @author nk160297
 */
public class CollectDerivationVisitor extends DefaultSchemaVisitor {

    private GlobalType mProcessedType;

    // Key --- derived from --> Value
    private HashMap<GlobalType, GlobalType> mDerivationMap;
    private boolean mSimpleTypesOnly;
    
    public CollectDerivationVisitor(HashMap<GlobalType, GlobalType> derivationMap,
            boolean simpleTypesOnly) {
        assert derivationMap != null; 
        //
        mDerivationMap = derivationMap;
        mSimpleTypesOnly = simpleTypesOnly;
    }
    
    public HashMap<GlobalType, GlobalType> collectDerivationFrom(
            SchemaModel sModel) {
        if (sModel != null) {
            Schema schema = sModel.getSchema();
            if (schema != null) {
                visit(schema);
            }
        }
        //
        return mDerivationMap;
    }

    //=========================================================================
    
    @Override
    public void visit(Schema schema) {
        // Looks for global type children only.
        assert schema != null;
        for (GlobalType gType: schema.getChildren(GlobalType.class)) {
            mProcessedType = gType;
            gType.accept(this);
        }
    }
    
    @Override
    public void visit(GlobalSimpleType type) {
        visitChildren(type);
    }

    @Override
    public void visit(GlobalComplexType type) {
        if (mSimpleTypesOnly) {
            return;
        }
        visitChildren(type);
    }
    
    //=========================================================================
    
    @Override
    public void visit(SimpleContent sc) {
        visitChildren(sc);
    }
    
    @Override
    public void visit(ComplexContent cc) {
        if (mSimpleTypesOnly) {
            return;
        }
        visitChildren(cc);
    }
    
    //=========================================================================
    
    @Override
    public void visit(SimpleExtension se) {
        checkBaseTypeRef(se.getBase());
    }
    
    @Override
    public void visit(ComplexExtension ce) {
        if (mSimpleTypesOnly) {
            return;
        }
        checkBaseTypeRef(ce.getBase());
    }
    
    @Override
    public void visit(SimpleContentRestriction scr) {
        checkBaseTypeRef(scr.getBase());
    }
    
    @Override
    public void visit(ComplexContentRestriction ccr) {
        if (mSimpleTypesOnly) {
            return;
        }
        checkBaseTypeRef(ccr.getBase());
    }
    
    @Override
    public void visit(SimpleTypeRestriction str) {
        checkBaseTypeRef(str.getBase());
    }
    
    // ----------------------------------------------
    
    protected void checkBaseTypeRef(
            NamedComponentReference<? extends GlobalType> gTypeRef) {
        if (gTypeRef != null) {
            GlobalType derivedFrom = gTypeRef.get();
            if (derivedFrom != null) {
                assert mProcessedType != derivedFrom; // check for other case
                mDerivationMap.put(mProcessedType, derivedFrom);
            }
        }
    }
    
    protected void visitChildren(SchemaComponent sc) {
        for (SchemaComponent child: sc.getChildren()) {
            child.accept(this);
        }
    }
    
}

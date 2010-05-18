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

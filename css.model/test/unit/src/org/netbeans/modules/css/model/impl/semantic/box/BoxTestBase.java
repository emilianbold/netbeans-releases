/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl.semantic.box;

import java.util.Collection;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.*;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.box.Box;
import org.netbeans.modules.css.model.api.semantic.box.BoxElement;
import org.netbeans.modules.css.model.api.semantic.box.BoxType;
import org.netbeans.modules.css.model.impl.semantic.ModelBuilderNodeVisitor;
import org.netbeans.modules.css.model.impl.semantic.NodeModel;
import org.netbeans.modules.css.model.impl.semantic.PropertyModelId;
import org.netbeans.modules.css.model.impl.semantic.Utils;

/**
 *
 * @author marekfukala
 */
public abstract class BoxTestBase extends CssTestBase {

    public BoxTestBase(String testName) {
        super(testName);
    }

    protected void dumpBox(Box box) {
        Utils.dumpBox(box);
    }
    
    protected void assertBox(BoxProvider provider, BoxType boxType, 
            String top, String right, String bottom, String left) {
        Box box = provider.getBox(boxType);
        assertNotNull(box);
         
        BoxElement e = box.getEdge(Edge.TOP);
        assertEquals(top, e == null ? null : e.asText());

        e = box.getEdge(Edge.RIGHT);
        assertEquals(right, e == null ? null : e.asText());

        e = box.getEdge(Edge.BOTTOM);
        assertEquals(bottom, e == null ? null : e.asText());

        e = box.getEdge(Edge.LEFT);
        assertEquals(left, e == null ? null : e.asText());

    }

    protected void assertBox(String propertyName, CharSequence value, BoxType type, String trbl) {
        assertBox(propertyName, value, type, trbl, trbl, trbl, trbl);
    }
    
    protected void assertBox(String propertyName, CharSequence value, BoxType boxType, 
            String top, String right, String bottom, String left) {
        
        PropertyDefinition model = Properties.getPropertyDefinition(null, propertyName);
        ResolvedProperty val = new ResolvedProperty(model, value);

        Node root = val.getParseTree();
        if(isDebugMode()) {
            System.out.println("generated parse tree:");
            dumpTree(root);
        }
        
        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.BOX);
        root.accept(modelvisitor);

        Collection<NodeModel> nodeModels = modelvisitor.getModels();
        assertNotNull(nodeModels);
        assertTrue(nodeModels.size() > 0);
        
        for(NodeModel nm : nodeModels) {
            BoxProvider provider = (BoxProvider) nm;
            Box box = provider.getBox(boxType);
            if(box != null) {
                assertBox(provider, boxType, top, right, bottom, left);
                return ;
            }
            
        }
        
        //if we get here something is wrong
        assertTrue(String.format("No box model found for box type %s", boxType.name()), false); 
        
    }
    
    
    protected boolean isDebugMode() {
        return false;
    }

    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.properties.model;

import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyModel;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.model.ModelBuilderNodeVisitor;
import org.netbeans.modules.css.lib.api.properties.model.NodeModel;
import org.netbeans.modules.css.lib.api.properties.model.PropertyModelId;

/**
 *
 * @author marekfukala
 */
public class BorderWidthTest extends BorderTestBase {

    public BorderWidthTest(String name) {
        super(name);
    }

    public void testBoxOneArg() {
        PropertyModel model = Properties.getPropertyModel("border-width");
        ResolvedProperty val = new ResolvedProperty(model, "medium");

        Node root = val.getParseTree();
        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.BORDER);
        root.accept(modelvisitor);

        BorderWidth borderColor = (BorderWidth) modelvisitor.getModel();
        assertNotNull(borderColor);

//        dumpBox(borderColor);
        assertBox(borderColor, "medium", "medium", "medium", "medium");
    }
    
    public void testBoxTwoArgs() {
        PropertyModel model = Properties.getPropertyModel("border-width");
        ResolvedProperty val = new ResolvedProperty(model, "thin 2px");

        Node root = val.getParseTree();
//        dumpTree(root);
        
        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.BORDER);
        root.accept(modelvisitor);

        BorderWidth borderColor = (BorderWidth) modelvisitor.getModel();
        assertNotNull(borderColor);

//        dumpBox(borderColor);
        assertBox(borderColor, "thin", "2px", "thin", "2px");
    }
    
    public void testBoxThreeArgs() {
        PropertyModel model = Properties.getPropertyModel("border-width");
        ResolvedProperty val = new ResolvedProperty(model, "thin thick 20px");

        Node root = val.getParseTree();
//        dumpTree(root);
        
        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.BORDER);
        root.accept(modelvisitor);

        BorderWidth borderColor = (BorderWidth) modelvisitor.getModel();
        assertNotNull(borderColor);

//        dumpBox(borderColor);
        assertBox(borderColor, "thin", "thick", "20px", "thick");
    }
    
    public void testBoxFourArgs() {
        PropertyModel model = Properties.getPropertyModel("border-width");
        ResolvedProperty val = new ResolvedProperty(model, "10px 20cm thin thick");

        Node root = val.getParseTree();
//        dumpTree(root);
        
        ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.BORDER);
        root.accept(modelvisitor);

        BorderWidth borderColor = (BorderWidth) modelvisitor.getModel();
        assertNotNull(borderColor);

//        dumpBox(borderColor);
        assertBox(borderColor, "10px", "20cm", "thin", "thick");
    }

   
}

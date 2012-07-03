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

import org.netbeans.modules.css.model.impl.semantic.box.DeclarationsBoxModelProvider;
import org.netbeans.modules.css.model.api.semantic.box.EditableBox;
import org.netbeans.modules.css.model.api.semantic.box.BoxType;
import org.netbeans.modules.css.model.api.semantic.Edge;
import java.util.concurrent.atomic.AtomicBoolean;

import org.netbeans.modules.css.model.ModelTestBase;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.model.impl.semantic.Utils;

/**
 *
 * @author marekfukala
 */
public class DeclarationsPaddingModelTest extends ModelTestBase {

    public DeclarationsPaddingModelTest(String name) {
        super(name);
    }

    public void testModifySemanticModel() {

        String code =
                "div { \n"
                + "\tcolor: red;\n"
                + "\tpadding : 3px;\n"
                + "\tfont: cursive;\n"
                + "\tpadding-left: 10px\n"
                + "}";

        final Model model = createModel(code);

        model.runWriteTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {

                Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
                assertNotNull(ds);

                DeclarationsBoxModelProvider dbm = new DeclarationsBoxModelProvider(model, ds);
                EditableBox padding = dbm.getBox(BoxType.PADDING);
                
                assertNotNull(padding);
                Utils.dumpBox(padding);

                assertBox(padding, "3px", "3px", "3px", "10px");

                //edit the box 
                BoxEdgeSize newPaddingWidth = BoxEdgeSize.parseValue("10px");

                padding.setEdge(Edge.RIGHT, newPaddingWidth);
//                assertBox(padding, "3px", "10px", "3px", "10px");

                padding.setEdge(Edge.TOP, BoxEdgeSize.parseValue("auto"));
//                assertBox(padding, "auto", "10px", "3px", "10px");


//                Utils.dumpBox(padding);

            }
        });

//        System.out.println("Original source:");
//        System.out.println(model.getOriginalSource());
//
//        System.out.println("Modified source:");
//        System.out.println(model.getModelSource());

//        Model model2 = createModel(model.getModelSource().toString());
//        Declarations ds = model2.getStyleSheet().getBody().getRules().get(0).getDeclarations();
//        EditableBox<PaddingWidth> padding = new DeclarationsPaddingBoxModel(ds);
//
//        Utils.dumpBox(padding);
//

    }

    public void testSetBottomEdge() {
        final Model model = createModel("div { padding: 1px 3em 3em 1px; }");
        model.runWriteTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {

                Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
                assertNotNull(ds);

                DeclarationsBoxModelProvider dbm = new DeclarationsBoxModelProvider(model, ds);
                EditableBox padding = dbm.getBox(BoxType.PADDING);
                
                assertNotNull(padding);
                assertBox(padding, "1px", "3em", "3em", "1px");

                BoxEdgeSize newPaddingWidth = BoxEdgeSize.parseValue("1px");
                padding.setEdge(Edge.BOTTOM, newPaddingWidth);
//                assertBox(padding, "1px", "3em", "1px", "1px");
                

            }
        });

        System.out.println(model.getModelSource());

    }
}

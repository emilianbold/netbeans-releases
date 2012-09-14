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

import org.netbeans.modules.css.model.ModelTestBase;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.model.api.semantic.box.BoxElement;
import org.netbeans.modules.css.model.api.semantic.box.BoxType;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.box.EditableBox;

/**
 *
 * @author marekfukala
 */
public class DeclarationsBoxModelTest extends ModelTestBase {

    public DeclarationsBoxModelTest(String name) {
        super(name);
    }

    public void testModel() {

        String code =
                "div { \n"
                + "\tmargin : 3px;\n"
                + "\tmargin-left: 10px;\n"
                + "\tborder: solid 2px;\n"
                + "\tborder-color: red green;\n"
                + "\tborder-top-style: dashed;\n"
                + "}";

        final Model model = createModel(code);

        model.runWriteTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {

                Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
                assertNotNull(ds);

                DeclarationsBoxModelProvider dmodel = new DeclarationsBoxModelProvider(model, ds);
                
                EditableBox box;
                
                box = dmodel.getBox(BoxType.MARGIN);
                assertBox(box, "3px", "3px", "3px", "10px");
                
                box = dmodel.getBox(BoxType.BORDER_COLOR);
                assertBox(box, "red", "green", "red", "green");
                
                box = dmodel.getBox(BoxType.BORDER_STYLE);
                assertBox(box, "dashed", "solid", "solid", "solid");
                
                box = dmodel.getBox(BoxType.BORDER_WIDTH);
                assertBox(box, "2px");

            }
        });

    }
    
    public void testModel2() {

        String code =
                "div { \n"
                + "\tmargin-left: 10px;\n"
                + "\tmargin : 3px;\n"
                + "}";

        final Model model = createModel(code);

        model.runWriteTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {

                Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
                assertNotNull(ds);

                DeclarationsBoxModelProvider dmodel = new DeclarationsBoxModelProvider(model, ds);
                
                EditableBox box;
                
                box = dmodel.getBox(BoxType.MARGIN);
                assertBox(box, "3px", "3px", "3px", "3px");
            }
        });

    }
    
    public void testModelModify1() {

        String code =
                "div { \n"
                + "\tmargin-left: 10px;\n"
                + "\tmargin : 3px;\n"
                + "}";

        final Model model = createModel(code);

        model.runWriteTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {

                Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
                assertNotNull(ds);

                DeclarationsBoxModelProvider dmodel = new DeclarationsBoxModelProvider(model, ds);
                
                EditableBox box;
                
                box = dmodel.getBox(BoxType.MARGIN);
                assertBox(box, "3px", "3px", "3px", "3px");
                
                BoxElement be = box.createElement("5cm");
                box.setEdge(Edge.TOP, be);
                
            }
        });

    }


}

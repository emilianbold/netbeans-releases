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
package org.netbeans.modules.css.model.impl.semantic;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.css.lib.api.properties.model.*;
import org.netbeans.modules.css.model.ModelTestBase;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.StyleSheet;

/**
 *
 * @author marekfukala
 */
public class DeclarationsBorderModelTest extends ModelTestBase {

    public DeclarationsBorderModelTest(String name) {
        super(name);
    }

    public void testBorder() {
        assertBox("border: red;", "red");
        assertBox("border: solid;", "solid");
        assertBox("border: 1pt;", "1pt");
    }
    
    public void testBorderMulti() {
        assertBox("border: red solid;", "red solid"); //color_width_style
        assertBox("border: solid red;", "red solid");
        assertBox("border: 2px red;", "red 2px");
        assertBox("border: 2px red solid;", "red 2px solid");
        assertBox("border: dashed 2px red;", "red 2px dashed");
    }
    
    public void testBorderSingleEdge() {
        assertBox("border-top: red solid;", "red solid", null, null, null); //color_width_style
        assertBox("border-left: 10px;", null, null, null, "10px"); //color_width_style
    }
    
    public void testBorderAndSingleEdge() {
        assertBox("border: green; border-top: red solid;", "red solid", "green", "green", "green"); //color_width_style
        assertBox("border: green 2px; border-right: red;", "green 2px", "red", "green 2px", "green 2px"); //color_width_style
    }
    
    public void testBorderOverwritten() {
        assertBox("border: green; border: solid;", "solid");
        assertBox("border: 2px; border: green;", "green");
    }
    
    public void testMultipleSingleEdge() {
        assertBox("border-top: 2px; border-right: red 2px; border-left: dashed", "2px", "red 2px", null, "dashed");
    }
    
    public void testBorderColor() {
        assertBox("border-color: red", "red");
    }
    
    public void testBorderStyle() {
        assertBox("border-style: solid", "solid");
    }
    
    public void testBorderWidth() {
        assertBox("border-width: 1px", "1px");
    }
    
    public void testBorderOverriding() {
        assertBox("border: green; border-color: red", "red");
        assertBox("border: green; border-width: 2px", "green 2px");
        
        //border erase everything
        assertBox("border-width: 20px;border-color: green; border: solid;","solid");
        
        //border overridden
        assertBox("border: solid; border-width: 20px;border-color: green;","green 20px solid");
    }
    
    private void assertBox(String declarations, final String all) {
        assertBox(declarations, all, all, all, all);
    }
    
    private void assertBox(String declarations, final String top, final String right, final String bottom, final String left) {
        StringBuilder ruleCode = new StringBuilder();
        
        ruleCode.append("div {\n");
        ruleCode.append(declarations);
        ruleCode.append("\n");
        ruleCode.append("}");
        
        final Model model = createModel(ruleCode.toString());

        model.runReadTask(new Model.ModelTask() {

            @Override
            public void run(StyleSheet styleSheet) {

                Declarations ds = styleSheet.getBody().getRules().get(0).getDeclarations();
                assertNotNull(ds);

                EditableBox<BoxEdgeBorder> box = new DeclarationsBorderModel(model, ds);
                assertNotNull(box);
//                Utils.dumpBox(margin);

                assertBox(box, top, right, bottom, left);

            }
        });
        
    }
    
    
}

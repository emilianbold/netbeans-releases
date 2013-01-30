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

import org.netbeans.modules.css.model.api.semantic.box.BoxType;
import java.util.concurrent.atomic.AtomicBoolean;

import org.netbeans.modules.css.model.api.ModelTestBase;
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
        assertBox("border: red;", BoxType.BORDER_COLOR, "red");
        assertBox("border: solid;", BoxType.BORDER_STYLE, "solid");
        assertBox("border: 1pt;", BoxType.BORDER_WIDTH, "1pt");
    }

    public void testBorderMulti() {
        assertBox("border: red solid;", BoxType.BORDER_COLOR, "red"); 
        assertBox("border: red solid;", BoxType.BORDER_STYLE, "solid"); 
        
        assertBox("border: solid red;", BoxType.BORDER_COLOR, "red");
        assertBox("border: solid red;", BoxType.BORDER_STYLE, "solid");
        
        assertBox("border: 2px red;", BoxType.BORDER_COLOR, "red");
        assertBox("border: 2px red;", BoxType.BORDER_WIDTH, "2px");
        
        assertBox("border: 2px red solid;", BoxType.BORDER_COLOR, "red");
        assertBox("border: 2px red solid;", BoxType.BORDER_STYLE, "solid");
        assertBox("border: 2px red solid;", BoxType.BORDER_WIDTH, "2px");
        
        assertBox("border: dashed 2px red;", BoxType.BORDER_COLOR, "red");
        assertBox("border: dashed 2px red;", BoxType.BORDER_STYLE, "dashed");
        assertBox("border: dashed 2px red;", BoxType.BORDER_WIDTH, "2px");
    }

    public void testBorderSingleEdge() {
        assertBox("border-top: red solid;", BoxType.BORDER_COLOR, "red", null, null, null); 
        assertBox("border-top: red solid;", BoxType.BORDER_STYLE, "solid", null, null, null); 
        
        assertBox("border-left: 10px;", BoxType.BORDER_WIDTH, null, null, null, "10px");
    }

    public void testBorderAndSingleEdge() {
        assertBox("border: green; border-top: red solid;", BoxType.BORDER_COLOR, "red", "green", "green", "green");
        assertBox("border: green; border-top: red solid;", BoxType.BORDER_STYLE, "solid", null, null, null);
        
        assertBox("border: green 2px; border-right: red;", BoxType.BORDER_COLOR, "green", "red", "green", "green");
        assertBox("border: green 2px; border-right: red;", BoxType.BORDER_WIDTH, "2px", null, "2px", "2px");
    }

    public void testBorderOverwritten() {
        assertBox("border: green; border: solid;", BoxType.BORDER_STYLE, "solid");
        assertBox("border: green; border: solid;", BoxType.BORDER_COLOR, null);
        
        assertBox("border: 2px; border: green;", BoxType.BORDER_COLOR, "green");
        assertBox("border: 2px; border: green;", BoxType.BORDER_WIDTH, null);
    }

    public void testMultipleSingleEdge() {
        assertBox("border-top: 2px; border-right: red 2px; border-left: dashed", 
                BoxType.BORDER_COLOR, null, "red", null, null);
        
        assertBox("border-top: 2px; border-right: red 2px; border-left: dashed", 
                BoxType.BORDER_STYLE, null, null, null, "dashed");
        
        assertBox("border-top: 2px; border-right: red 2px; border-left: dashed", 
                BoxType.BORDER_WIDTH, "2px", "2px", null, null);
    }

    public void testBorderColor() {
        assertBox("border-color: red", BoxType.BORDER_COLOR, "red");
        assertBox("border-color: red", BoxType.BORDER_WIDTH, null);
        assertBox("border-color: red", BoxType.BORDER_STYLE, null);
    }

    public void testBorderStyle() {
        assertBox("border-style: solid", BoxType.BORDER_STYLE, "solid");
        assertBox("border-style: solid", BoxType.BORDER_COLOR, null);
        assertBox("border-style: solid", BoxType.BORDER_WIDTH, null);
    }

    public void testBorderWidth() {
        assertBox("border-width: 1px", BoxType.BORDER_WIDTH, "1px");
        assertBox("border-width: 1px", BoxType.BORDER_COLOR, null);
        assertBox("border-width: 1px", BoxType.BORDER_STYLE, null);
    }

    public void testBorderOverriding() {
        assertBox("border: green; border-color: red", BoxType.BORDER_COLOR, "red");
        
        assertBox("border: green; border-width: 2px",  BoxType.BORDER_COLOR,"green");
        assertBox("border: green; border-width: 2px",  BoxType.BORDER_WIDTH,"2px");

        //border erase everything
        assertBox("border-width: 20px;border-color: green; border: solid;", BoxType.BORDER_STYLE, "solid");
        assertBox("border-width: 20px;border-color: green; border: solid;", BoxType.BORDER_WIDTH, null);
        assertBox("border-width: 20px;border-color: green; border: solid;", BoxType.BORDER_COLOR, null);

        //border overridden
        assertBox("border: solid; border-width: 20px;border-color: green;", BoxType.BORDER_COLOR, "green");
        assertBox("border: solid; border-width: 20px;border-color: green;", BoxType.BORDER_WIDTH, "20px");
        assertBox("border: solid; border-width: 20px;border-color: green;", BoxType.BORDER_STYLE, "solid");
    }

}

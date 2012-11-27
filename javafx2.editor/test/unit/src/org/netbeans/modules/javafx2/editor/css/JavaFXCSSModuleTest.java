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
package org.netbeans.modules.javafx2.editor.css;

import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;

/**
 *
 * @author marekfukala
 */
public class JavaFXCSSModuleTest extends CssTestBase {

    public JavaFXCSSModuleTest(String testName) {
        super(testName);
    }

    public void testFxBorderColor() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-border-color");
        assertNotNull(pd);
        
        assertResolve(pd, "derive(-fx-base, 80%) "
                + "linear-gradient(to bottom, derive(-fx-base,80%) 20%, derive(-fx-base,-10%) 90%)"
                + "derive(-fx-base, 10%) "
                + "linear-gradient(to bottom, derive(-fx-base,80%) 20%, derive(-fx-base,-10%) 90%),        "
                + "transparent -fx-table-header-border-color -fx-table-header-border-color -fx-table-header-border-color");
    }
    
    public void testFxFont() {
        PropertyDefinition pd = Properties.getPropertyDefinition("-fx-font");
        assertNotNull(pd);
        
        assertResolve(pd, " bold 36px \"Amble\"");
    }
    
    public void testLinearGradient() {
        PropertyDefinition pd = Properties.getPropertyDefinition("@fx-linear-gradient");
        assertNotNull(pd);
        
        assertResolve(pd, "linear-gradient(red, blue)");
        assertResolve(pd, "linear-gradient(red, blue, green)");
        
        assertResolve(pd, "linear-gradient(repeat, red, blue)");
        assertResolve(pd, "linear-gradient(reflect, red, blue)");
        
        assertResolve(pd, "linear-gradient(to left, red, blue)");
        assertResolve(pd, "linear-gradient(to top, red, blue)");
        assertResolve(pd, "linear-gradient(to left top, red, blue)");
        assertResolve(pd, "linear-gradient(to right bottom, red, blue)");
        
        assertResolve(pd, "linear-gradient(from 1% 2% to 3% 4%, red, blue)");
        assertResolve(pd, "linear-gradient(from 1cm 2cm to 3cm 4cm, red, blue)");
        assertResolve(pd, "linear-gradient(from 1% 2% to 3cm 4cm, red, blue)");
        
        assertResolve(pd, "linear-gradient( from 0% 0% to 50% 50%, #fcf7b6, #a59c31)");
    
        assertResolve(pd, "linear-gradient(to right, derive(-fx-base,-30%), derive(-fx-base,-60%))");

        assertResolve(pd, "linear-gradient(to right, derive(-fx-base,65%) 2%, derive(-fx-base,-20%) 95%)");
        
    }
    
}

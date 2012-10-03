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
package org.netbeans.modules.css.model.impl;

import java.io.IOException;
import org.netbeans.modules.css.lib.TestUtil;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelTestBase;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.openide.filesystems.FileObject;


/**
 *
 * @author marekfukala
 */
public class ModelElementTest extends ModelTestBase {

    public ModelElementTest(String name) {
        super(name);
    }
    
    public void testAcceptVisitorGeneric_speed() throws IOException {
        FileObject file = getTestFile("testfiles/bootstrap.css");
        String content = file.asText();
        
        System.out.println("Testing performance on " + file.getNameExt() + " ... ");
        
        long a = System.currentTimeMillis();
        CssParserResult result = TestUtil.parse(content);
        long b = System.currentTimeMillis();
        System.out.println("file parsing took " + (b-a) + "ms.");
        
        Model model = createModel(result);
        long c = System.currentTimeMillis();
        System.out.println("model creation took " + (c-b) + "ms.");

        StyleSheet s = getStyleSheet(model);
        assertNotNull(s);
        
        ModelVisitor visitor = new ModelVisitor.Adapter() {

            @Override
            public void visitRule(Rule rule) {
                //no-op
            }
            
        };
        
        s.accept(visitor);
        long d = System.currentTimeMillis();
        
        System.out.println("visiting took " + (d-c) + "ms.");
        
    }
    
}

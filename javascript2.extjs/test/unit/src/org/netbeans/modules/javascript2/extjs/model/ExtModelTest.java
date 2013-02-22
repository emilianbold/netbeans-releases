/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.extjs.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.ModelTestBase;

/**
 *
 * @author Petr Pisl
 */
public class ExtModelTest extends ModelTestBase {
    
    public ExtModelTest(String testName) {
        super(testName);
    }
    
    public void testExtDefineMethod() throws Exception {
        Model model = getModel("testfiles/completion/defineMethod/defineMethod.js");
        assertNotNull(model);
        JsObject  global = model.getGlobalObject();
        assertEquals(3, global.getProperties().size());
        JsObject jsObject = global.getProperty("NetBeans");
        assertNotNull(jsObject);
        jsObject = jsObject.getProperty("stuff");
        assertNotNull(jsObject);
        jsObject = jsObject.getProperty("engineer");
        assertNotNull(jsObject);
        JsObject developer = jsObject.getProperty("developer");
        assertNotNull(developer);
        assertFalse(developer.getModifiers().contains(Modifier.PRIVATE));
        assertEquals(1, developer.getAssignments().size());
        TypeUsage type = developer.getAssignments().iterator().next();
        assertEquals("Anonym$0", type.getType());
    }
}

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
package org.netbeans.modules.css.prep.editor.scss;

import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.css.editor.module.main.CssModuleTestBase;
import org.netbeans.modules.css.prep.editor.model.CPModel;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author marekfukala
 */
public class ScssCompletionTest extends CssModuleTestBase {
    
    public ScssCompletionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        CPModel.topLevelSnapshotMimetype = getTopLevelSnapshotMimetype();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown(); 
        CPModel.topLevelSnapshotMimetype = null;
    }
    
    @Override
    protected String getTopLevelSnapshotMimetype() {
        return "text/scss";
    }

    @Override
    protected String getCompletionItemText(CompletionProposal cp) {
        return cp.getInsertPrefix();
    }
    
    public void testVarCompletionInSimplePropertyValue() throws ParseException {
        checkCC("$var: 1; h1 { color: $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1; h1 { color: $v| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1; h1 { color: $va| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1; h1 { color: $var| }", arr("$var"), Match.EXACT);
        
        checkCC("$var: 1; h1 { font: red $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1; h1 { font: red $v| }", arr("$var"), Match.EXACT);
        
        checkCC("$var1: 1; $var2: 2; h1 { font: red $v| }", arr("$var1", "$var2"), Match.EXACT);
        checkCC("$var1: 1; $var2: 2; h1 { font: red $var2| }", arr("$var2"), Match.EXACT);
    }
    
    public void testVarCompletionInMixinBody() throws ParseException {
//        checkCC("$var: 1;  @mixin my { $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1;  @mixin my { $v| }", arr("$var"), Match.EXACT);
        
        //this fails as the $foo is not parsed - see CPModelTest.testVariablesInMixinWithError_fails
//        checkCC("$var: 1;  @mixin my { $foo: $v| }", arr("$var", "$foo"), Match.CONTAINS);
        checkCC("$var: 1;  @mixin my { $foo: $v| }", arr("$var"), Match.CONTAINS);
        
    }
    
    public void testVarCompletionInRuleBody() throws ParseException {
        checkCC("$var: 1;  .clz { $| }", arr("$var"), Match.EXACT);
        checkCC("$var: 1;  .clz { $v| }", arr("$var"), Match.EXACT);
        
        //this fails as the $foo is not parsed - see CPModelTest.testVariablesInMixinWithError_fails
//        checkCC("$var: 1;  .clz { $foo: $v| }", arr("$var", "$foo"), Match.CONTAINS);
        checkCC("$var: 1;  .clz { $foo: $v| }", arr("$var"), Match.CONTAINS);
        
    }
    
    public void testKeywordsCompletion() throws ParseException {
        checkCC("@| ", arr("@mixin"), Match.CONTAINS);
        checkCC("@mix| ", arr("@mixin"), Match.EXACT);
    }
    
    public void testContentKeywordsCompletion() throws ParseException {
        checkCC("@| ", arr("@content"), Match.CONTAINS);
        checkCC("@cont| ", arr("@content"), Match.EXACT);
    }
    
    public void testMixinsCompletion() throws ParseException {
        checkCC("@mixin mymixin() {}\n @include | ", arr("mymixin"), Match.CONTAINS);
        checkCC("@mixin mymixin() {}\n @include mymi| ", arr("mymixin"), Match.EXACT);
    }
    
}

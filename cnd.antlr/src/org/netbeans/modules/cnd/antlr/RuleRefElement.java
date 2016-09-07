/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

class RuleRefElement extends AlternativeElement {
    protected String targetRule; // which rule is being called?
    protected String args = null;		 // were any args passed to rule?
    protected String idAssign = null;	 // is the return type assigned to a variable?
    protected String label;


    public RuleRefElement(Grammar g, Token t, int autoGenType_) {
        super(g, t, autoGenType_);
        targetRule = t.getText();
        //		if ( Character.isUpperCase(targetRule.charAt(0)) ) { // lexer rule?
        if (t.getType() == ANTLRTokenTypes.TOKEN_REF) { // lexer rule?
            targetRule = CodeGenerator.encodeLexerRuleName(targetRule);
        }
    }

//	public RuleRefElement(Grammar g, String t, int line, int autoGenType_) {
//		super(g, autoGenType_);
//		targetRule = t;
//		if ( Character.isUpperCase(targetRule.charAt(0)) ) { // lexer rule?
//			targetRule = CodeGenerator.lexerRuleName(targetRule);
//		}
//		this.line = line;
//	}

    public void generate(Context context) {
        grammar.generator.gen(this, context);
    }

    public String getArgs() {
        return args;
    }

    public String getIdAssign() {
        return idAssign;
    }

    public String getLabel() {
        return label;
    }

    public Lookahead look(int k) {
        return grammar.theLLkAnalyzer.look(k, this);
    }

    public void setArgs(String a) {
        args = a;
    }

    public void setIdAssign(String id) {
        idAssign = id;
    }

    public void setLabel(String label_) {
        label = label_;
    }

    public String toString() {
        if (args != null)
            return " " + targetRule + args;
        else
            return " " + targetRule;
    }
}

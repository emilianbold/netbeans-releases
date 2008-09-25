/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.css.editor.model;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * Immutable content of a CSS rule.
 *
 * a rule example:
 * h1 {
 *      color: red;
 * }
 *
 * @author Marek Fukala
 */
public class CssRule {

    private Position ruleNameOffset, ruleOpenBracketOffset, ruleCloseBracketOffset;
    //private int ruleNameOffset, ruleOpenBracketOffset, ruleCloseBracketOffset;
    private String ruleName;
    private CssRuleContent styleData;
    
    public CssRule(Document doc, String ruleName, int ruleNameOffset, int ruleOpenBracketOffset, int ruleCloseBracketOffset,CssRuleContent styleData) throws BadLocationException {
        this.ruleName = ruleName;
        this.ruleNameOffset = doc == null ? new SimplePosition(ruleNameOffset) : doc.createPosition(ruleNameOffset);
        this.ruleOpenBracketOffset = doc == null ? new SimplePosition(ruleOpenBracketOffset) : doc.createPosition(ruleOpenBracketOffset);
        this.ruleCloseBracketOffset = doc == null ? new SimplePosition(ruleCloseBracketOffset) : doc.createPosition(ruleCloseBracketOffset);
        this.styleData = styleData;
    }

    /** @return an instance of {@link CssRuleContent} which represents the items inside the css rule.
     * It also allows to listen on the changes in the rule items.
     */
    public CssRuleContent ruleContent() {
        return styleData;
    }
    
    /** @return the css rule name */
    public String name() {
        return ruleName;
    }

    /** @return offset of the rule name in the model's document. */
    public int getRuleNameOffset() {
        return ruleNameOffset.getOffset();
    }
    
    /** @return offset of the rule's closing bracket in the model's document. */
    public int getRuleCloseBracketOffset() {
        return ruleCloseBracketOffset.getOffset();
    }

    /** @return offset of the rule's opening bracket in the model's document. */
    public int getRuleOpenBracketOffset() {
        return ruleOpenBracketOffset.getOffset();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CssRule) {
            CssRule r = (CssRule)obj;
            if(name().equals(r.name()) 
                    && getRuleNameOffset() == r.getRuleNameOffset() 
                    && getRuleCloseBracketOffset() == r.getRuleCloseBracketOffset() 
                    && getRuleOpenBracketOffset() == r.getRuleOpenBracketOffset()
                    && ruleContent().equals(r.ruleContent())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.ruleNameOffset != null ? this.ruleNameOffset.hashCode() : 0);
        hash = 17 * hash + (this.ruleOpenBracketOffset != null ? this.ruleOpenBracketOffset.hashCode() : 0);
        hash = 17 * hash + (this.ruleCloseBracketOffset != null ? this.ruleCloseBracketOffset.hashCode() : 0);
        hash = 17 * hash + (this.ruleName != null ? this.ruleName.hashCode() : 0);
        hash = 17 * hash + (this.styleData != null ? this.styleData.hashCode() : 0);
        return hash;
    }

    
    
    @Override
    public String toString() {
        return "CssRule[" + name() + "\n" + ruleContent().toString() + "]"; //NOI18N
    }
    
    static class SimplePosition implements Position {
        private int offset;
        SimplePosition(int offset) {
            this.offset = offset;
        }
        public int getOffset() {
            return offset;
        }
    }
    
}

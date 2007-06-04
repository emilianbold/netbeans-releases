/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.css.model;

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

    private int ruleNameOffset, ruleOpenBracketOffset, ruleCloseBracketOffset;
    private String ruleName;
    private CssRuleContent styleData;
    
    public CssRule(String ruleName, int ruleNameOffset, int ruleOpenBracketOffset, int ruleCloseBracketOffset,CssRuleContent styleData) {
        this.ruleName = ruleName;
        this.ruleNameOffset = ruleNameOffset;
        this.ruleOpenBracketOffset = ruleOpenBracketOffset;
        this.ruleCloseBracketOffset = ruleCloseBracketOffset;
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
        return ruleNameOffset;
    }
    
    /** @return offset of the rule's closing bracket in the model's document. */
    public int getRuleCloseBracketOffset() {
        return ruleCloseBracketOffset;
    }

    /** @return offset of the rule's opening bracket in the model's document. */
    public int getRuleOpenBracketOffset() {
        return ruleOpenBracketOffset;
    }
    
    
}

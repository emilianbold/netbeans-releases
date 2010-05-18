/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xslt.core.text.completion.support.grammar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Alex Petrov (23.07.2008)
 */
public class XSLGrammarProducerImpl_1_0 implements XSLGrammarProducer, XSLGrammarConstants {
    public static String Xsl_Version = XSL_VERSION_1_0;
    
    /** 
     * Contains a mapping from XSL namespace element names to set of names of
     * allowed XSL children. Neither the element name keys nor the names in the
     * value set should contain the namespace prefix.
     */
    protected Map<String, Set<String>> elementDecls;

    /** 
     * Contains a mapping from XSL namespace element names to set of names of
     * allowed XSL attributes for that element.  The element name keys should
     * not contain the namespace prefix.
     */
    protected Map<String, Set<String>> attrDecls;

    /** 
     * A Set of XSL attributes which should be allowd for result elements
     */
    protected Set<String> resultElementAttr;

    /** 
     * A Set of elements which should be allowed at template level in XSL stylesheet 
     */
    protected Set<String> template;

    /** 
     * Contains a mapping from XSL namespace element names to an attribute name which
     * should contain XPath expression.  The element name keys should
     * not contain the namespace prefix.
     */
    protected Map<String, String> exprAttributes;

    /** 
     * A set containing all functions allowed in XSLT 
     */
    protected Set<String> xslFunctions;

    /** 
     * A set containing XPath axes 
     */
    protected Set<String> xpathAxes;

    public Map<String, Set<String>> getElementDecls() {
        if (elementDecls == null) {
            elementDecls = new HashMap<String, Set<String>>();
            attrDecls = new HashMap<String, Set<String>>();

            // Commonly used variables
            Set<String> emptySet = new TreeSet<String>();
            String spaceAtt = "xml:space";  // NOI18N
            Set<String> tmpSet;

            ////////////////////////////////////////////////
            // Initialize common sets

            Set<String> charInstructions = new TreeSet<String>(Arrays.asList(new String[]{"apply-templates", // NOI18N
            "call-template","apply-imports","for-each","value-of","when", // NOI18N
            "copy-of","number","choose","if","text","copy", // NOI18N
            "variable","message","fallback"})); // NOI18N

            Set<String> instructions = new TreeSet<String>(charInstructions);
            instructions.addAll(Arrays.asList(new String[]{"processing-instruction", // NOI18N
            "comment","element","attribute"})); // NOI18N

            Set<String> charTemplate = charInstructions; // We don't care about PCDATA

            template = new TreeSet<String>(instructions);
            template.add(RESULT_ELEMENTS);

            Set<String> topLevel = new TreeSet<String>(Arrays.asList(new String[] {
                "import","include","strip-space", // NOI18N
                "preserve-space","output","key","decimal-format","attribute-set", // NOI18N
                "variable","param","template","namespace-alias"})); // NOI18N

            Set<String> topLevelAttr = new TreeSet<String>(Arrays.asList(new String[]{
                "extension-element-prefixes", // NOI18N
                "exclude-result-prefixes","id","version",spaceAtt})); // NOI18N

            resultElementAttr = new TreeSet<String>(Arrays.asList(new String[]{"extension-element-prefixes", // NOI18N
            "exclude-result-prefixes","use-attribute-sets","version"})); // NOI18N

            ////////////////////////////////////////////////
            // Add items to elementDecls and attrDecls maps

            // xsl:stylesheet
            elementDecls.put("stylesheet", topLevel); // NOI18N
            attrDecls.put("stylesheet", topLevelAttr); // NOI18N

            // xsl:transform
            elementDecls.put("transform", topLevel); // NOI18N
            attrDecls.put("transform", topLevelAttr); // NOI18N

            // xsl:import
            elementDecls.put("import", emptySet); // NOI18N
            attrDecls.put("import", new TreeSet<String>(Arrays.asList(new String[]{"href"}))); // NOI18N

            // xxsl:include
            elementDecls.put("include", emptySet); // NOI18N
            attrDecls.put("include", new TreeSet<String>(Arrays.asList(new String[]{"href"}))); // NOI18N

            // xsl:strip-space
            elementDecls.put("strip-space", emptySet); // NOI18N
            attrDecls.put("strip-space", new TreeSet<String>(Arrays.asList(new String[]{"elements"}))); // NOI18N

            // xsl:preserve-space
            elementDecls.put("preserve-space", emptySet); // NOI18N
            attrDecls.put("preserve-space", new TreeSet<String>(Arrays.asList(new String[]{"elements"}))); // NOI18N

            // xsl:output
            elementDecls.put("output", emptySet); // NOI18N
            attrDecls.put("output", new TreeSet<String>(Arrays.asList(new String[]{"method", // NOI18N
            "version","encoding","omit-xml-declaration","standalone","doctype-public", // NOI18N
            "doctype-system","cdata-section-elements","indent","media-type"}))); // NOI18N

            // xsl:key
            elementDecls.put("key", emptySet); // NOI18N
            attrDecls.put("key", new TreeSet<String>(Arrays.asList(new String[]{"name","match","use"}))); // NOI18N

            // xsl:decimal-format
            elementDecls.put("decimal-format", emptySet); // NOI18N
            attrDecls.put("decimal-format", new TreeSet<String>(Arrays.asList(new String[]{"name", // NOI18N
            "decimal-separator","grouping-separator","infinity","minus-sign","NaN", // NOI18N
            "percent","per-mille","zero-digit","digit","pattern-separator"}))); // NOI18N

            // xsl:namespace-alias
            elementDecls.put("namespace-alias", emptySet); // NOI18N
            attrDecls.put("namespace-alias", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "stylesheet-prefix","result-prefix"}))); // NOI18N

            // xsl:template
            tmpSet = new TreeSet<String>(instructions);
            tmpSet.add(RESULT_ELEMENTS);
            tmpSet.add("param"); // NOI18N
            elementDecls.put("template", tmpSet); // NOI18N
            attrDecls.put("template", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "match","name","priority","mode",spaceAtt}))); // NOI18N

            // xsl:value-of
            elementDecls.put("value-of", emptySet); // NOI18N
            attrDecls.put("value-of", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
            "select","disable-output-escaping"}))); // NOI18N

            // xsl:copy-of
            elementDecls.put("copy-of", emptySet); // NOI18N
            attrDecls.put("copy-of", new TreeSet<String>(Arrays.asList(new String[]{"select"}))); // NOI18N

            // xsl:number
            elementDecls.put("number", emptySet); // NOI18N
            attrDecls.put("number", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "level","count","from","value","format","lang","letter-value", // NOI18N
                "grouping-separator","grouping-size"}))); // NOI18N

            // xsl:apply-templates
            elementDecls.put("apply-templates", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "sort","with-param"}))); // NOI18N
            attrDecls.put("apply-templates", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "select","mode"}))); // NOI18N

            // xsl:apply-imports
            elementDecls.put("apply-imports", emptySet); // NOI18N
            attrDecls.put("apply-imports", emptySet); // NOI18N

            // xsl:for-each
            tmpSet = new TreeSet<String>(instructions);
            tmpSet.add(RESULT_ELEMENTS);
            tmpSet.add("sort"); // NOI18N
            elementDecls.put("for-each", tmpSet); // NOI18N
            attrDecls.put("for-each", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
            "select",spaceAtt}))); // NOI18N

            // xsl:sort
            elementDecls.put("sort", emptySet); // NOI18N
            attrDecls.put("sort", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "select","lang","data-type","order","case-order"}))); // NOI18N

            // xsl:if
            elementDecls.put("if", template); // NOI18N
            attrDecls.put("if", new TreeSet<String>(Arrays.asList(new String[]{"test",spaceAtt}))); // NOI18N

            // xsl:choose
            elementDecls.put("choose", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "when","otherwise"}))); // NOI18N
            attrDecls.put("choose", new TreeSet<String>(Arrays.asList(new String[]{spaceAtt}))); // NOI18N

            // xsl:when
            elementDecls.put("when", template); // NOI18N
            attrDecls.put("when", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "test",spaceAtt}))); // NOI18N

            // xsl:otherwise
            elementDecls.put("otherwise", template); // NOI18N
            attrDecls.put("otherwise", new TreeSet<String>(Arrays.asList(new String[]{spaceAtt}))); // NOI18N

            // xsl:attribute-set
            elementDecls.put("attribute-set", new TreeSet<String>(Arrays.asList(new String[] { // NOI18N
                "attribute"})));
            attrDecls.put("attribute-set", new TreeSet<String>(Arrays.asList(new String[] { // NOI18N
                "name","use-attribute-sets"})));

            // xsl:call-template
            elementDecls.put("call-template", new TreeSet<String>(Arrays.asList(new String[]{"with-param"}))); // NOI18N
            attrDecls.put("call-template", new TreeSet<String>(Arrays.asList(new String[]{"name"}))); // NOI18N

            // xsl:with-param
            elementDecls.put("with-param", template); // NOI18N
            attrDecls.put("with-param", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "name","select"}))); // NOI18N

            // xsl:variable
            elementDecls.put("variable", template); // NOI18N
            attrDecls.put("variable", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "name","select"}))); // NOI18N

            // xsl:param
            elementDecls.put("param", template); // NOI18N
            attrDecls.put("param", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "name","select"}))); // NOI18N

            // xsl:text
            elementDecls.put("text", emptySet); // NOI18N
            attrDecls.put("text", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "disable-output-escaping"}))); // NOI18N

            // xsl:processing-instruction
            elementDecls.put("processing-instruction", charTemplate); // NOI18N
            attrDecls.put("processing-instruction", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "name",spaceAtt}))); // NOI18N

            // xsl:element
            elementDecls.put("element", template); // NOI18N
            attrDecls.put("element", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "name","namespace","use-attribute-sets",spaceAtt}))); // NOI18N

            // xsl:attribute
            elementDecls.put("attribute", charTemplate); // NOI18N
            attrDecls.put("attribute", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                "name","namespace",spaceAtt}))); // NOI18N

            // xsl:comment
            elementDecls.put("comment", charTemplate); // NOI18N
            attrDecls.put("comment", new TreeSet<String>(Arrays.asList(new String[]{spaceAtt}))); // NOI18N

            // xsl:copy
            elementDecls.put("copy", template); // NOI18N
            attrDecls.put("copy", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                spaceAtt,"use-attribute-sets"}))); // NOI18N

            // xsl:message
            elementDecls.put("message", template); // NOI18N
            attrDecls.put("message", new TreeSet<String>(Arrays.asList(new String[]{ // NOI18N
                spaceAtt,"terminate"}))); // NOI18N

            // xsl:fallback
            elementDecls.put("fallback", template); // NOI18N
            attrDecls.put("fallback", new TreeSet<String>(Arrays.asList(new String[]{spaceAtt}))); // NOI18N
        }
        return elementDecls;
    }

    public Map<String, Set<String>> getAttrDecls() {
        if (attrDecls == null) {
            getElementDecls();
        }
        return attrDecls;
    }

    public Set<String> getResultElementAttr() {
        if (resultElementAttr == null) {
            getElementDecls();
        }
        return resultElementAttr;
    }

    public Set<String> getTemplate() {
        if (template == null) {
            getElementDecls();
        }
        return template;
    }

    public Set<String> getXslFunctions() {
        if (xslFunctions == null) {
            xslFunctions = new TreeSet<String>(Arrays.asList(new String[]{
                "boolean(","ceiling(","concat(", "contains(","count(","current(","document(", // NOI18N
                "false(", "floor(","format-number(","generate-id(", // NOI18N
                "id(","local-name(","key(","lang(","last(","name(","namespace-uri(", "normalize-space(", // NOI18N
                "not(","number(","position(","round(","starts-with(","string(", // NOI18N
                "string-length(", "substring(","substring-after(","substring-before(", "sum(", // NOI18N
                "system-property(","translate(",   "true(","unparsed-entity-uri("})); // NOI18N
        }
        return xslFunctions;
    }

    public Set<String> getXPathAxes() {
        if (xpathAxes == null) {
            xpathAxes = new TreeSet<String>(Arrays.asList(new String[]{"ancestor::", "ancestor-or-self::", // NOI18N
            "attribute::", "child::", "descendant::", "descendant-or-self::", "following::", // NOI18N
            "following-sibling::", "namespace::", "parent::", "preceding::", // NOI18N
            "preceding-sibling::", "self::"})); // NOI18N
        }
        return xpathAxes;
    }

    public Map<String, String> getExprAttributes() {
        if (exprAttributes == null) {
            exprAttributes = new HashMap<String, String>();
            exprAttributes.put("key", "use"); // NOI18N
            exprAttributes.put("value-of", "select"); // NOI18N
            exprAttributes.put("copy-of", "select"); // NOI18N
            exprAttributes.put("number", "value"); // NOI18N
            exprAttributes.put("apply-templates", "select"); // NOI18N
            exprAttributes.put("for-each", "select"); // NOI18N
            exprAttributes.put("sort", "select"); // NOI18N
            exprAttributes.put("if", "test"); // NOI18N
            exprAttributes.put("when", "test"); // NOI18N
            exprAttributes.put("with-param", "select"); // NOI18N
            exprAttributes.put("variable", "select"); // NOI18N
            exprAttributes.put("param", "select"); // NOI18N
        }
        return exprAttributes;
    }
}
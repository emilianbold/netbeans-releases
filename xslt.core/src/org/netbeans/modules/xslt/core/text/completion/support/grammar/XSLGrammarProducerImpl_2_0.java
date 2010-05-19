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
public class XSLGrammarProducerImpl_2_0 extends XSLGrammarProducerImpl_1_0 {
    static { 
        Xsl_Version = XSL_VERSION_2_0;
    }

    @Override
    public Map<String, Set<String>> getElementDecls() {
        if (elementDecls == null) {
            elementDecls = new HashMap<String, Set<String>>();
            attrDecls = new HashMap<String, Set<String>>();

            Set<String> emptySet = new TreeSet<String>();
            String spaceAtt = "xml:space";  // NOI18N
            Set<String> tmpSet;

            Set<String> charInstructions = new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "analyze-string","apply-imports","apply-templates","call-template",
                "choose","copy","copy-of","fallback","for-each","for-each-group","if",
                "message","next-match","number","perform-sort","sequence","text",
                "value-of","variable","when"}));

            Set<String> instructions = new TreeSet<String>(charInstructions);
            instructions.addAll(Arrays.asList(new String[] {// NOI18N
                "processing-instruction","comment","element","attribute"}));

            Set<String> charTemplate = charInstructions; // We don't care about PCDATA

            template = new TreeSet<String>(instructions);
            template.add(RESULT_ELEMENTS);

            resultElementAttr = new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "default-collation","exclude-result-prefixes","extension-element-prefixes",
                "use-when","version","xpath-default-namespace"}));

            // subelements of the element "stylesheet"/"transform"
            Set<String> topLevel = new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "attribute-set","character-map","decimal-format","document","function",
                "import","import-schema","include","key",
                "namespace","namespace-alias","output","param","preserve-space",
                "result-document","strip-space","template","variable"}));
  
            // attributes of the element "stylesheet"/"transform"
            Set<String> topLevelAttr = new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "default-collation","default-validation",
                "exclude-result-prefixes","extension-element-prefixes","id",
                "input-type-annotations","version","xpath-default-namespace"}));
            
            // xsl:stylesheet
            elementDecls.put("stylesheet", topLevel);
            attrDecls.put("stylesheet", topLevelAttr);

            // xsl:transform
            elementDecls.put("transform", topLevel);
            attrDecls.put("transform", topLevelAttr);

            // xsl:character-map
            elementDecls.put("character-map", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "output-character"})));
            attrDecls.put("character-map", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "name","use-character-maps"})));

            // xsl:output-character
            elementDecls.put("output-character", emptySet);
            attrDecls.put("output-character", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "character","string"})));

            // xsl:function
            elementDecls.put("function", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "param"})));
            attrDecls.put("function", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "name","as","override"})));
            
            // xsl:decimal-format
            elementDecls.put("decimal-format", emptySet);
            attrDecls.put("decimal-format", new TreeSet<String>(Arrays.asList(new String[] {
                "name","decimal-separator","grouping-separator","infinity",
                "minus-sign","NaN","percent","per-mille","zero-digit","digit",
                "pattern-separator"})));

            // xsl:document
            elementDecls.put("document", emptySet);
            attrDecls.put("document", new TreeSet<String>(Arrays.asList(new String[] {
                "validation","type"})));

            // xsl:import
            elementDecls.put("import", emptySet);
            attrDecls.put("import", new TreeSet<String>(Arrays.asList(new String[] {
                "href"})));

            // xsl:import-schema
            elementDecls.put("import-schema", emptySet);
            attrDecls.put("import-schema", new TreeSet<String>(Arrays.asList(new String[] {
                "namespace","schema-location"})));

            // xsl:include
            elementDecls.put("include", emptySet);
            attrDecls.put("include", new TreeSet<String>(Arrays.asList(new String[] {
                "href"})));

            // xsl:strip-space
            elementDecls.put("strip-space", emptySet);
            attrDecls.put("strip-space", new TreeSet<String>(Arrays.asList(new String[] {
                "elements"})));

            // xsl:preserve-space
            elementDecls.put("preserve-space", emptySet);
            attrDecls.put("preserve-space", new TreeSet<String>(Arrays.asList(new String[] {
                "elements"})));

            // xsl:output
            elementDecls.put("output", emptySet);
            attrDecls.put("output", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "byte-order-mark", "cdata-section-elements","doctype-public",
                "doctype-system","encoding","escape-uri-attributes","include-content-type",
                "indent","media-type",
                "method","name","normalization-form","omit-xml-declaration","standalone",
                "undeclare-prefixes","use-character-maps","version"})));
            
            // xsl:key
            elementDecls.put("key", emptySet);
            attrDecls.put("key", new TreeSet<String>(Arrays.asList(new String[] {
                "name","match","use","collation"})));

            // xsl:decimal-format
            elementDecls.put("decimal-format", emptySet);
            attrDecls.put("decimal-format", new TreeSet<String>(Arrays.asList(
                new String[] {// NOI18N
                "name","decimal-separator","grouping-separator","infinity","minus-sign","NaN",
                "percent","per-mille","zero-digit","digit","pattern-separator"})));

            // xsl:namespace
            elementDecls.put("namespace", emptySet);
            attrDecls.put("namespace", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "name","select"})));

            // xsl:namespace-alias
            elementDecls.put("namespace-alias", emptySet);
            attrDecls.put("namespace-alias", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "stylesheet-prefix","result-prefix"})));

            // xsl:template
            tmpSet = new TreeSet<String>(instructions);
            tmpSet.add(RESULT_ELEMENTS);
            tmpSet.add("param");
            elementDecls.put("template", tmpSet);
            attrDecls.put("template", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "as","match","name","priority","mode",spaceAtt})));

            // xsl:value-of
            elementDecls.put("value-of", emptySet);
            attrDecls.put("value-of", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
            "disable-output-escaping","select","separator"})));

            // xsl:copy-of
            elementDecls.put("copy-of", emptySet);
            attrDecls.put("copy-of", new TreeSet<String>(Arrays.asList(new String[] {
                "copy-namespaces","select","type","validation"})));

            // xsl:number
            elementDecls.put("number", emptySet);
            attrDecls.put("number", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "count","format","from","grouping-separator","grouping-size",
                "lang","level","letter-value","ordinal","select","value"})));
            
            // xsl:analyze-string
            elementDecls.put("analyze-string", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "matching-substring","non-matching-substring","fallback"})));
            attrDecls.put("analyze-string", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "flags","regex","select"})));
              
            // xsl:apply-templates
            elementDecls.put("apply-templates", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "sort","with-param"})));
            attrDecls.put("apply-templates", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "select","mode"})));

            // xsl:apply-imports
            elementDecls.put("apply-imports", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "with-param"})));
            attrDecls.put("apply-imports", emptySet);

            // xsl:next-match
            elementDecls.put("next-match", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "fallback","with-param"})));
            attrDecls.put("next-match", emptySet);

            // xsl:for-each
            tmpSet = new TreeSet<String>(instructions);
            tmpSet.add(RESULT_ELEMENTS);
            tmpSet.add("sort");
            elementDecls.put("for-each", tmpSet);
            attrDecls.put("for-each", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "select",spaceAtt})));

            // xsl:for-each-group
            tmpSet = new TreeSet<String>(instructions);
            tmpSet.add(RESULT_ELEMENTS);
            tmpSet.add("sort");
            elementDecls.put("for-each-group", tmpSet);
            attrDecls.put("for-each-group", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "collation","group-by","group-adjacent","group-starting-with",
                "group-ending-with","select",spaceAtt})));
            
            // xsl:sort
            elementDecls.put("sort", emptySet);
            attrDecls.put("sort", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "collation","select","lang","data-type","order","case-order","stable"})));

            // xsl:perform-sort
            elementDecls.put("perform-sort", new TreeSet<String>(Arrays.asList(new String[] {
                "sort"})));
            attrDecls.put("perform-sort", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "select"})));
  
            // xsl:if
            elementDecls.put("if", template);
            attrDecls.put("if", new TreeSet<String>(Arrays.asList(new String[] {
                "test",spaceAtt})));

            // xsl:choose
            elementDecls.put("choose", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "when","otherwise"})));
            attrDecls.put("choose", new TreeSet<String>(Arrays.asList(new String[]{spaceAtt})));

            // xsl:when
            elementDecls.put("when", template);
            attrDecls.put("when", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "test",spaceAtt})));

            // xsl:otherwise
            elementDecls.put("otherwise", template);
            attrDecls.put("otherwise", new TreeSet<String>(Arrays.asList(new String[] {
                spaceAtt})));

            // xsl:attribute-set
            elementDecls.put("attribute-set", new TreeSet<String>(Arrays.asList(new String[] {
                "attribute"})));
            attrDecls.put("attribute-set", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "name","use-attribute-sets"})));

            // xsl:call-template
            elementDecls.put("call-template", new TreeSet<String>(Arrays.asList(new String[] {
                "with-param"})));
            attrDecls.put("call-template", new TreeSet<String>(Arrays.asList(
                new String[] {"name"})));

            // xsl:with-param
            elementDecls.put("with-param", template);
            attrDecls.put("with-param", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "as","name","select","tunnel"})));
    
            // xsl:variable
            elementDecls.put("variable", template);
            attrDecls.put("variable", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "as","name","select"})));
            
            // xsl:param
            elementDecls.put("param", template);
            attrDecls.put("param", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "as","name","required","select","tunnel"})));
            
            // xsl:text
            elementDecls.put("text", emptySet);
            attrDecls.put("text", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "disable-output-escaping"})));

            // xsl:processing-instruction
            elementDecls.put("processing-instruction", charTemplate);
            attrDecls.put("processing-instruction", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "name","select",spaceAtt})));
  
            // xsl:element
            elementDecls.put("element", template);
            attrDecls.put("element", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "name","namespace","inherit-namespaces","type",
                "use-attribute-sets","validation",spaceAtt})));

            // xsl:attribute
            elementDecls.put("attribute", charTemplate);
            attrDecls.put("attribute", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "name","namespace","select","separator","type","validation",spaceAtt})));
            
            // xsl:comment
            elementDecls.put("comment", charTemplate);
            attrDecls.put("comment", new TreeSet<String>(Arrays.asList(new String[]{spaceAtt})));

            // xsl:copy
            elementDecls.put("copy", template);
            attrDecls.put("copy", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "copy-namespaces","inherit-namespaces","type","use-attribute-sets",
                "validation",spaceAtt})));

            // xsl:message
            elementDecls.put("message", template);
            attrDecls.put("message", new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "select","terminate",spaceAtt})));
            
            // xsl:fallback
            elementDecls.put("fallback", template);
            attrDecls.put("fallback", new TreeSet<String>(Arrays.asList(new String[] {
                spaceAtt})));
            
            // xsl:sequence
            elementDecls.put("sequence", new TreeSet<String>(Arrays.asList(new String[] {
                "fallback"})));
            attrDecls.put("sequence", new TreeSet<String>(Arrays.asList(new String[] {
                "select"})));
  
            // xsl:result-document
            elementDecls.put("result-document", template);
            attrDecls.put("result-document", new TreeSet<String>(Arrays.asList(new String[] {
                "byte-order-mark","cdata-section-elements","doctype-public",
                "doctype-system","encoding","escape-uri-attributes","format","href",
                "include-content-type","indent","media-type","method",
                "normalization-form","omit-xml-declaration","output-version",
                "standalone","type","undeclare-prefixes","use-character-maps","validation"})));
        }
        return elementDecls;
    }

    @Override
    public Map<String, String> getExprAttributes() {
        if (exprAttributes == null) {
            exprAttributes = super.getExprAttributes();
            exprAttributes.put("comment", "select"); // NOI18N
            exprAttributes.put("for-each-group", "select"); // NOI18N
            //exprAttributes.put("for-each-group", "group-by"); // NOI18N
            //exprAttributes.put("for-each-group", "group-adjacent"); // NOI18N
            exprAttributes.put("include", "use-when"); // NOI18N
            exprAttributes.put("message", "select"); // NOI18N
            exprAttributes.put("namespace", "select"); // NOI18N
            //exprAttributes.put("number", "select"); // NOI18N
            exprAttributes.put("perform-sort", "select"); // NOI18N
            exprAttributes.put("processing-instruction", "select"); // NOI18N
            exprAttributes.put("sequence", "select"); // NOI18N
        }
        return exprAttributes;
    }
    
    @Override
    public Set<String> getXslFunctions() {
        if (xslFunctions == null) {
            xslFunctions = new TreeSet<String>(Arrays.asList(new String[] {// NOI18N
                "abs(","add-dayTimeDuration-to-date(","add-dayTimeDuration-to-dateTime(",
                "add-dayTimeDuration-to-time(","add-dayTimeDurations(","add-yearMonthDuration-to-date(",
                "add-yearMonthDuration-to-dateTime(","add-yearMonthDurations(","adjust-date-to-timezone(",
                "adjust-dateTime-to-timezone(","adjust-time-to-timezone(","avg(","base-uri(",
                "base64Binary-equal(","boolean(","boolean-equal(","boolean-greater-than(",
                "boolean-less-than(","ceiling(","codepoint-equal(","codepoints-to-string(",
                "collection(","compare(","concat(","concatenate(","contains(","count(",
                "current-date(","current-dateTime(","current-time(","data(","date-equal(",
                "date-greater-than(","date-less-than(","dateTime(","dateTime-equal(",
                "dateTime-greater-than(","dateTime-less-than(","day-from-date(","day-from-dateTime(",
                "days-from-duration(","dayTimeDuration-greater-than(","dayTimeDuration-less-than(",
                "deep-equal(","default-collation(","distinct-values(","divide-dayTimeDuration(",
                "divide-dayTimeDuration-by-dayTimeDuration(","divide-yearMonthDuration(",
                "divide-yearMonthDuration-by-yearMonthDuration(","doc(","doc-available(",
                "document-uri(","duration-equal(","empty(","encode-for-uri(","ends-with(",
                "error(","escape-html-uri(","exactly-one(","except(","exists(","false(","floor(",
                "gDay-equal(","gMonth-equal(","gMonthDay-equal(","gYear-equal(","gYearMonth-equal(",
                "hexBinary-equal(","hours-from-dateTime(","hours-from-duration(","hours-from-time(",
                "id(","idref(","implicit-timezone(","in-scope-prefixes(","index-of(",
                "insert-before(","intersect(","iri-to-uri(","is-same-node(","lang(","last(",
                "local-name(","local-name-from-QName(","lower-case(","matches(","max(","min(",
                "minutes-from-dateTime(","minutes-from-duration(","minutes-from-time(","month-from-date(",
                "month-from-dateTime(","months-from-duration(","multiply-dayTimeDuration(",
                "multiply-yearMonthDuration(","name(","namespace-uri(","namespace-uri-for-prefix(",
                "namespace-uri-from-QName(","nilled(","node-after(","node-before(","node-name(",
                "normalize-space(","normalize-unicode(","not(","NOTATION-equal(","number(",
                "numeric-add(","numeric-divide(","numeric-equal(","numeric-greater-than(",
                "numeric-integer-divide(","numeric-less-than(","numeric-mod(",
                "numeric-multiply(","numeric-subtract(","numeric-unary-minus(",
                "numeric-unary-plus(","one-or-more(","position(","prefix-from-QName(","QName(",
                "QName-equal(","remove(","replace(","resolve-QName(","resolve-uri(",
                "reverse(","root(","round(","round-half-to-even(","seconds-from-dateTime(",
                "seconds-from-duration(","seconds-from-time(","starts-with(","static-base-uri(",
                "string(","string-join(","string-length(","string-to-codepoints(","subsequence(",
                "substring(","substring-after(","substring-before(","subtract-dates(","subtract-dateTimes(",
                "subtract-dayTimeDuration-from-date(","subtract-dayTimeDuration-from-dateTime(",
                "subtract-dayTimeDuration-from-time(","subtract-dayTimeDurations(","subtract-times(",
                "subtract-yearMonthDuration-from-date(","subtract-yearMonthDuration-from-dateTime(",
                "subtract-yearMonthDurations(","sum(","time-equal(","time-greater-than(",
                "time-less-than(","timezone-from-date(","timezone-from-dateTime(","timezone-from-time(",
                "to(","tokenize(","trace(","translate(","true(","union(","unordered(","upper-case(",
                "year-from-date(","year-from-dateTime(","yearMonthDuration-greater-than(",
                "yearMonthDuration-less-than(","years-from-duration(","zero-or-one("
                }));
        }
        return xslFunctions;
    }
}
/*
 * XSLGrammarQuery.java
 *
 * Created on 16. júlí 2002, 15:53
 */

package org.netbeans.modules.xsl.grammar;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.xml.spi.model.*;
import org.openide.util.enum.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  asgeir
 */
public class XSLGrammarQuery {

    private final static Map elementDecls = new HashMap();
    
    private final static Map attrDecls = new HashMap();
    
    private final static Set resultElementAttr;
    
    private final static Object resultElements = new Object();

    private static final String[] aCharInstructions = {"apply-templates", // NOI18N
        "call-template","apply-imports","for-each","value-of", // NOI18N
        "copy-of","number","choose","if","text","copy", // NOI18N
        "variable","message","fallback"}; // NOI18N
        
    private static final String[] aInstructionsExtra = {"processing-instruction", // NOI18N
        "comment","element","attribute"}; // NOI18N
        
    private static final String[] aTopLevel = {"import","include","strip-space", // NOI18N
        "preserve-space","output","key","decimal-format","attribute-set", // NOI18N
        "variable","param","template","namespace-alias"}; // NOI18N
    
    private static final String[] aTopLevelAttr = {"extension-element-prefixes",
        "exclude-result-prefixes","id","version","xml:space"};
        
    // Those attributes are in the xsl namespace
    private static final String[] aResultElementsAttr = {"extension-element-prefixes",
        "exclude-result-prefixes","use-attribute-sets","version"};
        
    static {
        Set tmpSet;
        
        Set charInstructions = new HashSet(Arrays.asList(aCharInstructions));
      
        Set instructions = new HashSet(charInstructions);
        instructions.addAll(Arrays.asList(aInstructionsExtra));
        
        Set charTemplate = charInstructions; // We don't care about PCDATA
        
        Set template = new HashSet(instructions);
        template.add(resultElements);
        
        Set topLevel = new HashSet(Arrays.asList(aTopLevel));
        
        Set topLevelAttr = new HashSet(Arrays.asList(aTopLevelAttr));
        resultElementAttr = new HashSet(Arrays.asList(aResultElementsAttr));
        
        // xsl:stylesheet
        elementDecls.put("stylesheet", topLevel);
        attrDecls.put("stylesheet", topLevelAttr);

        // xsl:transform
        elementDecls.put("transform", topLevel);
        attrDecls.put("transform", topLevelAttr);
        
        // xsl:import
        elementDecls.put("import", new HashSet());
        attrDecls.put("import", new HashSet(Arrays.asList(new String[]{"href"})));

        // xxsl:include
        elementDecls.put("include", new HashSet());
        attrDecls.put("include", new HashSet(Arrays.asList(new String[]{"href"})));

        // xsl:strip-space
        elementDecls.put("strip-space", new HashSet());
        attrDecls.put("strip-space", new HashSet(Arrays.asList(new String[]{"elements"})));

        // xsl:preserve-space
        elementDecls.put("preserve-space", new HashSet());
        attrDecls.put("preserve-space", new HashSet(Arrays.asList(new String[]{"elements"})));

        // xsl:output
        elementDecls.put("output", new HashSet());
        attrDecls.put("output", new HashSet(Arrays.asList(new String[]{"method",
            "version","encoding","omit-xml-declaration","standalone","doctype-public",
            "doctype-system","cdata-section-elements","indent","media-type"})));

        // xsl:key
        elementDecls.put("key", new HashSet());
        attrDecls.put("key", new HashSet(Arrays.asList(new String[]{"name","match","use"})));

        // xsl:decimal-format
        elementDecls.put("decimal-format", new HashSet());
        attrDecls.put("decimal-format", new HashSet(Arrays.asList(new String[]{"name",
            "decimal-separator","grouping-separator","infinity","minus-sign","NaN",
            "percent","per-mille","zero-digit","digit","pattern-separator"})));

        // xsl:namespace-alias
        elementDecls.put("namespace-alias", new HashSet());
        attrDecls.put("namespace-alias", new HashSet(Arrays.asList(new String[]{
            "stylesheet-prefix","result-prefix"})));

        // xsl:template
        tmpSet = new HashSet(instructions);
        tmpSet.add(resultElements);
        tmpSet.add("param");
        elementDecls.put("template", tmpSet);
        attrDecls.put("template", new HashSet(Arrays.asList(new String[]{
            "match","name","priority","mode","xml:space"})));

        // xsl:value-of
        elementDecls.put("value-of", new HashSet());
        attrDecls.put("value-of", new HashSet(Arrays.asList(new String[]{
            "select","disable-output-escaping"})));

        // xsl:copy-of
        elementDecls.put("copy-of", new HashSet());
        attrDecls.put("copy-of", new HashSet(Arrays.asList(new String[]{"select"})));

        // xsl:number
        elementDecls.put("number", new HashSet());
        attrDecls.put("number", new HashSet(Arrays.asList(new String[]{
            "level","count","from","value","format","lang","letter-value",
            "grouping-separator","grouping-size"})));

        // xsl:apply-templates
        elementDecls.put("apply-templates", new HashSet(Arrays.asList(new String[]{
            "sort","with-param"})));
        attrDecls.put("apply-templates", new HashSet(Arrays.asList(new String[]{
            "select","mode"})));

        // xsl:apply-imports
        elementDecls.put("apply-imports", new HashSet());
        attrDecls.put("apply-imports", new HashSet());

        // xsl:for-each
        tmpSet = new HashSet(instructions);
        tmpSet.add(resultElements);
        tmpSet.add("sort");
        elementDecls.put("for-each", tmpSet);
        attrDecls.put("for-each", new HashSet(Arrays.asList(new String[]{
            "select","xml:space"})));
            
        // xsl:sort
        elementDecls.put("sort", new HashSet());
        attrDecls.put("sort", new HashSet(Arrays.asList(new String[]{
            "select","lang","data-type","order","case-order"})));
            
    }
       
    /** Creates a new instance of XSLGrammarQuery */
    public XSLGrammarQuery() {
    }
    
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param ctx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
     * @return enumeration of <code>GrammarResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes
     *         Every list member represents one possibility.  
     */
    public Enumeration queryElements(HintContext ctx) {
        QueueEnumeration list = new QueueEnumeration();
        return list;
    }

    /**
     * @stereotype query
     * @output list of results that can be queried on name, and attributes
     * @time Performs fast up to 300 ms. 
     * @param ctx represents virtual attribute <code>Node</code> to be replaced. Its parent is a element node.
     * @return enumeration of <code>GrammarResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.  
     */
    public Enumeration queryAttributes(HintContext ctx) {
        QueueEnumeration list = new QueueEnumeration();
        return list;
    }

    /**
     * Return options for value at given context.
     * It could be also used for completing of value parts such as Ant or XSLT property names (how to trigger it?).
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @input ctx represents virtual Node that has to be replaced (parent can be either Attr or Element), its own attributes does not name sense, it can be used just as the navigation start point.
     * @return enumeration of <code>GrammarResult</code>s (TEXT_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.  
     */
    public Enumeration queryValues(HintContext ctx) {
        QueueEnumeration list = new QueueEnumeration();
        return list;
    }

    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @return enumeration of <code>GrammarResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    public Enumeration queryEntities(String prefix) {
        QueueEnumeration list = new QueueEnumeration();
        return list;
    }

    /**
     * Allow to get names of <b>declared notations</b>.
     * @return enumeration of <code>GrammarResult</code>s (NOTATION_NODEs)
     */    
    public Enumeration queryNotations(String prefix) {
        QueueEnumeration list = new QueueEnumeration();
        return list;
    }
    
    private static class ResultElements {
    }
}

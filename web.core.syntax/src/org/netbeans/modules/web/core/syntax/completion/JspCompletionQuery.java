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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax.completion;

import java.util.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.html.HTMLCompletionQuery;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.deprecated.JspTagTokenContext;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;
import org.openide.loaders.DataObject;
import org.netbeans.spi.editor.completion.CompletionItem;


/**
 * JSP completion support finder
 *
 * @author Petr Nejedly
 * @author Tomasz.Slota@Sun.COM
 */

public class JspCompletionQuery implements CompletionQuery {
    
    /**
     * @see filterNonStandardXMLEntities(List, List)
     **/
    private static final Set stdXMLEntities = new TreeSet();
    
    static{
        stdXMLEntities.add("&lt;");
        stdXMLEntities.add("&gt;");
        stdXMLEntities.add("&apos;");
        stdXMLEntities.add("&quot;");
        stdXMLEntities.add("&amp;");
    }
    
    protected CompletionQuery contentQuery;
    
    public JspCompletionQuery(CompletionQuery contentQuery) {
        super();
        this.contentQuery = contentQuery;
    }
    
    /** Perform the query on the given component. The query usually
     * gets the component's document, the caret position and searches back
     * to find the last command start. Then it inspects the text up to the caret
     * position and returns the result.
     * @param component the component to use in this query.
     * @param offset position in the component's document to which the query will
     *   be performed. Usually it's a caret position.
     * @param support syntax-support that will be used during resolving of the query.
     * @return result of the query or null if there's no result.
     */
    public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support) {
        BaseDocument doc = (BaseDocument)component.getDocument();
        JspSyntaxSupport sup = (JspSyntaxSupport)support.get(JspSyntaxSupport.class);
        
        try {
            SyntaxElement elem = sup.getElementChain( offset );
            if (elem == null)
                // this is a legal option, when I don't have anything to say just return null
                return null;
            
            CompletionData jspData;
            switch (elem.getCompletionContext()) {
                // TAG COMPLETION
            case JspSyntaxSupport.TAG_COMPLETION_CONTEXT :
                return queryJspTag(component, offset, sup,
                        (SyntaxElement.Tag)elem);
                
                // ENDTAG COMPLETION
            case JspSyntaxSupport.ENDTAG_COMPLETION_CONTEXT :
                jspData = queryJspEndTag(offset, sup,
                        (SyntaxElement.EndTag)elem, doc);
                return result(component, offset, jspData);
                
                //DIRECTIVE COMPLETION IN JSP SCRIPTLET (<%| should offer <%@taglib etc.)
            case JspSyntaxSupport.SCRIPTINGL_COMPLETION_CONTEXT:
                return queryJspDirectiveInScriptlet(component, offset, sup, elem, doc);
                
                // DIRECTIVE COMPLETION
            case JspSyntaxSupport.DIRECTIVE_COMPLETION_CONTEXT :
                return queryJspDirective(component, offset, sup,
                        (SyntaxElement.Directive)elem, doc);
                
                // EXPRESSION LANGUAGE
            case JspSyntaxSupport.EL_COMPLETION_CONTEXT:
                return queryEL(component, offset, sup, elem, doc);
                
                // CONTENT LANGUAGE
            case JspSyntaxSupport.CONTENTL_COMPLETION_CONTEXT :
                // html results
                CompletionQuery.Result contentLResult = (contentQuery == null) ?
                    null :
                    contentQuery.query(component, offset, support);
                
                // JSP tags results
                jspData = queryJspTagInContent(offset, sup, doc);
                
                // JSP directive results
                CompletionQuery.Result jspDirec = queryJspDirectiveInContent(component, offset, sup, doc);
                
                //return null (do not popup completion) if there are no items in any of the completions
                if(jspData.completionItems.isEmpty() && jspDirec.getData().isEmpty() && (contentLResult == null || contentLResult.getData().isEmpty()))
                    return null;
                
                CompletionQuery.Result jspRes = result(component, offset, jspData);
                
                //merge result items
                ArrayList all = new ArrayList();
                all.addAll(jspDirec.getData());
                all.addAll(jspRes.getData());
                if(contentLResult != null){
                    DataObject dobj = NbEditorUtilities.getDataObject(doc);
                    
                    if(dobj != null && JspUtils.getJSPColoringData(doc, dobj.getPrimaryFile()).isXMLSyntax()){
                        filterNonStandardXMLEntities(all, contentLResult.getData());
                    } else{
                        all.addAll(contentLResult.getData());
                    }
                }
                
                int htmlAnchorOffset = contentLResult == null || contentLResult.getData().isEmpty() ? - 1 : ((HTMLCompletionQuery.HTMLCompletionResult)contentLResult).getSubstituteOffset();
                
                CompletionQuery.Result result = new JspCompletionResult(component,
                        NbBundle.getMessage(JSPKit.class, "CTL_JSP_Completion_Title"), all,
                        offset, jspData.removeLength, htmlAnchorOffset);
                
                return result;
            }
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * A hack-fix for #70366
     */
    private void filterNonStandardXMLEntities(List completionItemsRep, List htmlSuggestions) {
        Iterator it = htmlSuggestions.iterator();
        
        while (it.hasNext()){
            CompletionQuery.ResultItem item = (CompletionQuery.ResultItem) it.next();
            
            String itemText = item.getItemText();
            boolean filterOut = false;
            
            // check if entity is suggested
            if (itemText.startsWith("&") && itemText.endsWith(";")){
                // only allow well known XML entities
                if (!stdXMLEntities.contains(itemText)){
                    filterOut = true;
                }
            }
            
            if (!filterOut){
                completionItemsRep.add(item);
            }
        }
    }
    
    /** a new CC api hack - the result item needs to know its offset, formerly got from result - now cannot be used. */
    private void setResultItemsOffset(List/*<CompletionItem>*/ items, int removeLength, int ccoffset) {
        Iterator i = items.iterator();
        while(i.hasNext()) {
            Object obj = i.next();
            if(obj instanceof org.netbeans.modules.web.core.syntax.completion.ResultItem)
                ((org.netbeans.modules.web.core.syntax.completion.ResultItem)obj).setSubstituteOffset(ccoffset - removeLength);
        }
    }
    
    private void setResultItemsOffset(CompletionData cd, int ccoffset) {
        setResultItemsOffset(cd.completionItems, cd.removeLength, ccoffset);
    }
    
    /** Gets a list of completion items for JSP tags.
     * @param component editor component
     * @param offset position of caret
     * @param sup JSP syntax support
     * @param elem syntax element representing the current JSP tag
     * @return list of completion items
     */
    protected CompletionQuery.Result queryJspTag(JTextComponent component, int offset,
            JspSyntaxSupport sup, SyntaxElement.Tag elem) throws BadLocationException {
        BaseDocument doc = (BaseDocument)component.getDocument();
        // find the current item
        List compItems = new ArrayList();
        int removeLength = 0;
        
        TokenItem item = sup.getItemAtOrBefore(offset);
        
        if (item == null) {
            return result(component, offset, new CompletionData(compItems, 0));
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        String token = item.getImage().trim();
        
        // SYMBOL
        if (id == JspTagTokenContext.SYMBOL) {
            if (tokenPart.equals("<")) { // NOI18N
                // just after the beginning of the tag
                removeLength = 0;
                addTagPrefixItems(sup, compItems, sup.getTagPrefixes("")); // NOI18N
            }
            if (tokenPart.endsWith("\"")) { // NOI18N
                // try an attribute value
                String attrName = findAttributeForValue(sup, item);
                if (attrName != null) {
                    AttributeValueSupport attSup =
                            AttributeValueSupport.getSupport(true, elem.getName(), attrName);
                    if (attSup != null) {
                        return attSup.getResult(component, offset, sup, elem, ""); // NOI18N
                    }
                }
            }
            if(tokenPart.endsWith(">") && !tokenPart.endsWith("/>")) {
                compItems = sup.getAutocompletedEndTag(offset);
            }
            
            
        }
        
        // TAG
        if (id == JspTagTokenContext.TAG
                || id == JspTagTokenContext.WHITESPACE
                || id == JspTagTokenContext.EOL) {
            // inside a JSP tag name
            if (isBlank(tokenPart.charAt(tokenPart.length() - 1))
                    || tokenPart.equals("\n")) {
                // blank character - do attribute completion
                removeLength = 0;
                addAttributeItems(sup, compItems, elem, sup.getTagAttributes(elem.getName(), ""), null); // NOI18N
            } else {
                int colonIndex = tokenPart.indexOf(":"); // NOI18N
                if (colonIndex == -1) {
                    removeLength = tokenPart.length();
                    addTagPrefixItems(sup, compItems, sup.getTagPrefixes(tokenPart));
                } else {
                    String prefix = tokenPart.substring(0, colonIndex);
                    removeLength = tokenPart.length();
                    addTagPrefixItems(sup, compItems, prefix, sup.getTags(tokenPart), elem);
                }
            }
        }
        
        // ATTRIBUTE
        if (id == JspTagTokenContext.ATTRIBUTE) {
            // inside or after an attribute
            if (isBlank(tokenPart.charAt(tokenPart.length() - 1))) {
                // blank character - do attribute completion
                removeLength = 0;
                addAttributeItems(sup, compItems, elem, sup.getTagAttributes(elem.getName(), ""), null); // NOI18N
            } else {
                removeLength = tokenPart.length();
                addAttributeItems(sup, compItems, elem, sup.getTagAttributes(elem.getName(), tokenPart), token);
            }
        }
        
        // ATTRIBUTE VALUE
        if (id == JspTagTokenContext.ATTR_VALUE) {
            // inside or after an attribute
            String valuePart = tokenPart.trim();
            //return empty completion if the CC is not invoked inside a quotations
            if(valuePart.length() == 0) return result(component, offset, new CompletionData(compItems, 0));
            
            item = item.getPrevious();
            while ((item != null) && (item.getTokenID() == JspTagTokenContext.ATTR_VALUE)) {
                valuePart = item.getImage() + valuePart;
                item = item.getPrevious();
            }
            // get rid of the first quote
            valuePart = valuePart.substring(1);
            removeLength = valuePart.length();
            String attrName = findAttributeForValue(sup, item);
            if (attrName != null) {
                AttributeValueSupport attSup =
                        AttributeValueSupport.getSupport(true, elem.getName(), attrName);
                if (attSup != null) {
                    CompletionQuery.Result result = attSup.getResult(component, offset, sup, elem, valuePart);
                    if(!(attSup instanceof AttrSupports.FilenameSupport))
                        setResultItemsOffset(result.getData(), valuePart.length(), offset);
                    return result;
                }
            }
            
        }
        
        return /*List<JspResultItem.PrefixTag>*/ result(component, offset, new CompletionData(compItems, removeLength));
    }
    
    /** Gets a list of completion items for JSP tags.
     * @param offset position of caret
     * @param sup JSP syntax support
     * @param elem syntax element representing the current JSP tag
     * @return list of completion items
     */
    protected CompletionData queryJspEndTag(int offset, JspSyntaxSupport sup,
            SyntaxElement.EndTag elem, BaseDocument doc) throws BadLocationException {
        // find the current item
        List compItems = new ArrayList();
        int removeLength = 0;
        
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return new CompletionData(compItems, 0);
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        
        removeLength = tokenPart.length();
        return new CompletionData(sup.getPossibleEndTags(offset, tokenPart), removeLength);
    }
    
    /** Gets a list of completion items for EL */
    protected CompletionQuery.Result queryEL(JTextComponent component, int offset, JspSyntaxSupport sup,
            SyntaxElement elem, BaseDocument doc) throws BadLocationException {
        ELExpression elExpr = new ELExpression(sup);
        ArrayList complItems = new ArrayList();
        
        switch (elExpr.parse(offset)){
        case ELExpression.EL_START:
            // implicit objects
            for (ELImplicitObjects.ELImplicitObject implOb : ELImplicitObjects.getELImplicitObjects(elExpr.getReplace())) {
                complItems.add(new JspCompletionItem.ELImplicitObject(implOb.getName(), implOb.getType()));
            }
            
            // defined beans on the page
            BeanData[] beans = sup.getBeanData();
            if (beans != null){
                for (int i = 0; i < beans.length; i++) {
                    if (beans[i].getId().startsWith(elExpr.getReplace()))
                        complItems.add(new JspCompletionItem.ELBean(beans[i].getId(), beans[i].getClassName()));
                }
            }
            //Functions
            List functions = ELFunctions.getFunctions(sup, elExpr.getReplace());
            Iterator iter = functions.iterator();
            while (iter.hasNext()) {
                ELFunctions.Function fun = (ELFunctions.Function) iter.next();
                complItems.add(new JspCompletionItem.ELFunction(
                        fun.getPrefix(),
                        fun.getName(),
                        fun.getReturnType(),
                        fun.getParameters()));
            }
            break;
        case ELExpression.EL_BEAN:
        case ELExpression.EL_IMPLICIT:
            
            List<CompletionItem> items = elExpr.getPropertyCompletionItems(elExpr.getObjectClass());
            complItems.addAll(items);
            
            break;
        }
        
        return result(component, offset, new CompletionData(complItems, elExpr.getReplace().length()));
    }
    
    /** Gets a list of JSP directives which can be completed just after <% in java scriptlet context */
    protected CompletionQuery.Result queryJspDirectiveInScriptlet(JTextComponent component, int offset, JspSyntaxSupport sup,
            SyntaxElement elem, BaseDocument doc) throws BadLocationException {
        
        List compItems = new ArrayList();
        
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return result(component, offset, new CompletionData(compItems, 0));
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        
        if(id == JspTagTokenContext.SYMBOL2 && tokenPart.equals("<%"))
            addDirectiveItems(sup, compItems, sup.getDirectives("")); // NOI18N
        
        return result(component, offset, new CompletionData(compItems, 1 /*removeLength*/));
    }
    
    
    /** Gets a list of completion items for JSP directives.
     * @param component editor component
     * @param offset position of caret
     * @param sup JSP syntax support
     * @param elem syntax element representing the current JSP tag
     * @return list of completion items
     */
    protected CompletionQuery.Result queryJspDirective(JTextComponent component, int offset, JspSyntaxSupport sup,
            SyntaxElement.Directive elem, BaseDocument doc) throws BadLocationException {
        // find the current item
        List compItems = new ArrayList();
        int removeLength = 0;
        
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return result(component, offset, new CompletionData(compItems, 0));
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        String token = item.getImage().trim();
        
        // SYMBOL
        if (id.getNumericID() == JspTagTokenContext.SYMBOL_ID) {
            if (tokenPart.startsWith("<")) { // NOI18N
                //calculate a position of the potential replacement
                removeLength = tokenPart.length() - 1;
                addDirectiveItems(sup, compItems, sup.getDirectives("")); // NOI18N
            }
            if (tokenPart.endsWith("\"")) { // NOI18N
                // try an attribute value
                String attrName = findAttributeForValue(sup, item);
                if (attrName != null) {
                    AttributeValueSupport attSup =
                            AttributeValueSupport.getSupport(false, elem.getName(), attrName);
                    if (attSup != null) {
                        return attSup.getResult(component, offset, sup, elem, ""); // NOI18N
                    }
                }
            }
        }
        
        // DIRECTIVE
        if (id.getNumericID() == JspTagTokenContext.TAG_ID
                || id.getNumericID() == JspTagTokenContext.WHITESPACE_ID
                || id.getNumericID() == JspTagTokenContext.EOL_ID) {
            // inside a JSP directive name or after a whitespace
            if (isBlank(tokenPart.charAt(tokenPart.length() - 1))
                    || tokenPart.equals("\n")) {
                TokenItem prevItem = item.getPrevious();
                TokenID prevId = prevItem.getTokenID();
                String prevToken = prevItem.getImage().trim();
                if (prevId.getNumericID() == JspTagTokenContext.TAG_ID
                        ||  prevId.getNumericID() == JspTagTokenContext.ATTR_VALUE_ID
                        ||  prevId.getNumericID() == JspTagTokenContext.WHITESPACE_ID
                        ||  prevId.getNumericID() == JspTagTokenContext.EOL_ID) {
                    // blank character - do attribute completion
                    removeLength = 0;
                    addAttributeItems(sup, compItems, elem, sup.getDirectiveAttributes(elem.getName(), ""), null); // NOI18N
                } else if (prevId.getNumericID() == JspTagTokenContext.SYMBOL_ID && prevToken.equals("<%@")) { // NOI18N
                    // just after the beginning of the directive
                    removeLength = tokenPart.length() + 2;
                    addDirectiveItems(sup, compItems, sup.getDirectives("")); // NOI18N
                }
            } else {
                boolean add = true;
                //I need to get the whitespace token length before the tag name
                int whitespaceLength = 0;
                TokenItem prevItem = item.getPrevious();
                TokenID prevId = prevItem.getTokenID();
                //test whether there is a space before the currently completed tagname
                if(prevId.getNumericID() == JspTagTokenContext.TAG_ID && "".equals(prevItem.getImage().trim())) //try to trim the token image - just for sure since I am not absolutely sure if the TAG_ID is only for whitespaces in this case.
                    whitespaceLength = prevItem.getImage().length();
                
                
                List list = sup.getDirectives(tokenPart);
                if (list.size() == 1){
                    Object directive = list.get(0);
                    //is the cc invoce just after the directive?
                    if (directive instanceof TagInfo && ((TagInfo)directive).getTagName().equalsIgnoreCase(tokenPart))
                        add = false;
                }
                if (add){
                    removeLength = whitespaceLength + tokenPart.length() + 2;
                    addDirectiveItems(sup, compItems, list);
                }
            }
        }
        
        // ATTRIBUTE
        if (id.getNumericID() == JspTagTokenContext.ATTRIBUTE_ID) {
            // inside or after an attribute
            if (isBlank(tokenPart.charAt(tokenPart.length() - 1))) {
                // blank character - do attribute completion
                removeLength = 0;
                addAttributeItems(sup, compItems, elem, sup.getDirectiveAttributes(elem.getName(), ""), null); // NOI18N
            } else {
                removeLength = tokenPart.length();
                addAttributeItems(sup, compItems, elem, sup.getDirectiveAttributes(elem.getName(), tokenPart), token);
            }
        }
        
        // ATTRIBUTE VALUE
        if (id.getNumericID() == JspTagTokenContext.ATTR_VALUE_ID) {
            // inside or after an attribute
            String valuePart = tokenPart;
            item = item.getPrevious();
            while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.ATTR_VALUE_ID)) {
                valuePart = item.getImage() + valuePart;
                item = item.getPrevious();
            }
            // get rid of the first quote
            valuePart = valuePart.substring(1);
            removeLength = valuePart.length();
            String attrName = findAttributeForValue(sup, item);
            if (attrName != null) {
                AttributeValueSupport attSup =
                        AttributeValueSupport.getSupport(false, elem.getName(), attrName);
                //we cannot set substitute offset for file cc items
                if (attSup != null) {
                    CompletionQuery.Result result = attSup.getResult(component, offset, sup, elem, valuePart); // NOI18N
                    if(!(attSup instanceof AttrSupports.FilenameSupport))
                        setResultItemsOffset(result.getData(), valuePart.length(), offset);
                    return result;
                }
            }
            
        }
        
        return result(component, offset, new CompletionData(compItems, removeLength));
    }
    
    
    protected CompletionData queryJspTagInContent(int offset, JspSyntaxSupport sup, BaseDocument doc) throws BadLocationException {
        // find the current item
        List compItems = new ArrayList();
        int removeLength = 0;
        
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return new CompletionData(compItems, 0);
        }
        
        String tokenPart = item.getImage().substring(0,
                (offset - item.getOffset()) >= item.getImage().length() ? item.getImage().length() : offset - item.getOffset());
        int ltIndex = tokenPart.lastIndexOf('<');
        if (ltIndex != -1) {
            tokenPart = tokenPart.substring(ltIndex + 1);
        }
        while (ltIndex == -1) {
            item = item.getPrevious();
            if (item == null) {
                return new CompletionData(compItems, 0);
            }
            String newImage = item.getImage();
            ltIndex = newImage.lastIndexOf('<');
            if (ltIndex != -1)
                tokenPart = newImage.substring(ltIndex + 1) + tokenPart;
            else {
                tokenPart = newImage + tokenPart;
            }
            if (tokenPart.length() > 20) {
                return new CompletionData(compItems, 0);
            }
        }
        // we found ltIndex, tokenPart is either the part of the token we are looking for
        // or '/' + what we are looking for
        if (tokenPart.startsWith("/")) { // NOI18N
            tokenPart = tokenPart.substring(1);
            compItems = sup.getPossibleEndTags(offset, tokenPart, true); //get only first end tag
        } else {
            addTagPrefixItems(sup, compItems, sup.getTagPrefixes(tokenPart));
        }
        removeLength = tokenPart.length();
        return new CompletionData(compItems, removeLength);
    }
    
    protected CompletionQuery.Result queryJspDirectiveInContent(JTextComponent component, int offset, JspSyntaxSupport sup, BaseDocument doc) throws BadLocationException {
        // find the current item
        List compItems = new ArrayList();
        int removeLength = 0;
        
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            //return empty completion result
            return result(component, offset, new CompletionData(compItems, 0));
        }
        
        String tokenPart = item.getImage().substring(0,
                (offset - item.getOffset()) >= item.getImage().length() ? item.getImage().length() : offset - item.getOffset());
        
        //if (tokenPart.lastIndexOf('<') == -1 || !tokenPart.equals("<")) -- the condition is strange - the some should be !tokenPart.equals("<")
        if(!tokenPart.equals("<") && !tokenPart.equals("<%")) // NOI18N
            //return empty completion result
            return result(component, offset, new CompletionData(compItems, 0));
        
        //the removeLenght has to be set 0 if the CC is invoked right after <, 1 for <%
        if("<%".equals(tokenPart)) removeLength = 1; else removeLength = 0; // NOI18N
        
        addDirectiveItems(sup, compItems, sup.getDirectives("")); // NOI18N
        
        return result(component, offset, new CompletionData(compItems, removeLength));
    }
    
    private boolean isBlank(char c) {
        return c == ' ';
    }
    
    /** Finds an attribute name, assuming that the item is either
     * SYMBOL after the attribute name or ATTR_VALUE after this attribute name.
     * May return null if nothing found.
     */
    protected String findAttributeForValue(JspSyntaxSupport sup, TokenItem item) {
        // get before any ATTR_VALUE
        while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.ATTR_VALUE_ID))
            item = item.getPrevious();
        // now collect the symbols
        String symbols = ""; // NOI18N
        while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID)) {
            symbols = item.getImage() + symbols;
            item = item.getPrevious();
        }
        // two quotes at the end are not allowed
        if (!sup.isValueBeginning(symbols))
            return null;
        String attributeName = ""; // NOI18N
        //there may be a whitespace before the equals sign - trace over the whitespace
        //due to a bug in jsp tag syntax parser the whitespace has tag-directive tokenID
        //so I need to use the token image to recognize whether it is a whitespace
        while ((item != null) && (item.getImage().trim().length() == 0)) {
            item = item.getPrevious();
        }
        //now there should be either tag name or attribute name
        while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.ATTRIBUTE_ID)) {
            attributeName = item.getImage() + attributeName;
            item = item.getPrevious();
        }
        if (attributeName.trim().length() > 0)
            return attributeName.trim();
        return null;
    }
    
    /** Adds to the list of items <code>compItemList</code> new TagPrefix items with prefix
     * <code>prefix</code> for list of tag names <code>tagStringItems</code>.
     * @param set - <code>SyntaxElement.Tag</code>
     */
    private void addTagPrefixItems(JspSyntaxSupport sup, List compItemList, String prefix, List tagStringItems, SyntaxElement.Tag set) {
        for (int i = 0; i < tagStringItems.size(); i++) {
            Object item = tagStringItems.get(i);
            if (item instanceof TagInfo)
                compItemList.add(new JspCompletionItem.PrefixTag(prefix , (TagInfo)item, set));
            else
                compItemList.add(new JspCompletionItem.PrefixTag(prefix + ":" + (String)item)); // NOI18N
        }
    }
    
    /** Adds to the list of items <code>compItemList</code> new TagPrefix items for prefix list
     * <code>prefixStringItems</code>, followed by all possible tags for the given prefixes.
     */
    private void addTagPrefixItems(JspSyntaxSupport sup, List compItemList, List prefixStringItems) {
        for (int i = 0; i < prefixStringItems.size(); i++) {
            String prefix = (String)prefixStringItems.get(i);
            // now get tags for this prefix
            List tags = sup.getTags(prefix, ""); // NOI18N
            for (int j = 0; j < tags.size(); j++) {
                Object item = tags.get(j);
                if (item instanceof TagInfo)
                    compItemList.add(new JspCompletionItem.PrefixTag(prefix , (TagInfo)item));
                else
                    compItemList.add(new JspCompletionItem.PrefixTag(prefix + ":" + (String)item)); // NOI18N
            }
        }
    }
    
    /** Adds to the list of items <code>compItemList</code> new TagPrefix items with prefix
     * <code>prefix</code> for list of tag names <code>tagStringItems</code>.
     */
    private void addDirectiveItems(JspSyntaxSupport sup, List compItemList, List directiveStringItems) {
        for (int i = 0; i < directiveStringItems.size(); i++) {
            Object item = directiveStringItems.get(i);
            if(item instanceof TagInfo){
                TagInfo ti = (TagInfo) item;
                compItemList.add(new JspCompletionItem.Directive( ti.getTagName(), ti));
            } else
                compItemList.add(new JspCompletionItem.Directive( (String)item));
        }
    }
    
    /** Adds to the list of items <code>compItemList</code> new Attribute items.
     * Only those which are not already in tagDir
     * @param sup the syntax support
     * @param compItemList list to add to
     * @param tagDir tag or directive element
     * @param attributeItems list of strings containing suitable values (String or TagAttributeInfo)
     * @param currentAttr current attribute, may be null
     */
    private void addAttributeItems(JspSyntaxSupport sup, List compItemList,
            SyntaxElement.TagDirective tagDir, List attributeItems, String currentAttr) {
        for (int i = 0; i < attributeItems.size(); i++) {
            Object item = attributeItems.get(i);
            String attr;
            if (item instanceof TagAttributeInfo)
                attr = ((TagAttributeInfo)item).getName();
            else
                attr = (String)item;
            boolean isThere = tagDir.getAttributes().keySet().contains(attr);
            if (!isThere || attr.equalsIgnoreCase(currentAttr) ||
                    (currentAttr != null && attr.startsWith(currentAttr) && attr.length()>currentAttr.length() && !isThere)) {
                if (item instanceof TagAttributeInfo)
                    //XXX This is hack for fixing issue #45302 - CC is to aggressive.
                    //The definition of the tag and declaration doesn't allow
                    //define something like "prefix [uri | tagdir]". In the future
                    //it should be rewritten definition of declaration, which allow
                    //to do it.
                    if ("taglib".equalsIgnoreCase(tagDir.getName())){ //NOI18N
                        if (attr.equalsIgnoreCase("prefix")  //NOI18N
                                || (attr.equalsIgnoreCase("uri") && !tagDir.getAttributes().keySet().contains("tagdir")) //NOI18N
                                || (attr.equalsIgnoreCase("tagdir") && !tagDir.getAttributes().keySet().contains("uri"))) //NOI18N
                            compItemList.add(new JspCompletionItem.Attribute((TagAttributeInfo)item));
                    } else {
                    compItemList.add(new JspCompletionItem.Attribute((TagAttributeInfo)item));
                    } else
                        compItemList.add(new JspCompletionItem.Attribute((String)item));
            }
        }
    }
    
    private CompletionQuery.Result result(JTextComponent component, int offset, CompletionData complData) {
        setResultItemsOffset(complData, offset);
        return new JspCompletionResult(component,
                NbBundle.getMessage(JSPKit.class, "CTL_JSP_Completion_Title"), complData.completionItems,
                offset, complData.removeLength, -1);
    }
    
    /** Class which encapsulates a list of completion items and length of
     * the part which should be replaced. */
    public static class CompletionData {
        
        public List completionItems;
        public int removeLength;
        
        public CompletionData(List items, int length) {
            this.completionItems = items;
            this.removeLength = length;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("------ completion items, remove " + removeLength + " : ----------\n"); // NOI18N
            for (int i = 0; i < completionItems.size(); i++) {
                CompletionQuery.ResultItem item =
                        (CompletionQuery.DefaultResultItem)completionItems.get(i);
                sb.append(item.getItemText());
                sb.append("\n");   // NOI18N
            }
            return sb.toString();
        }
        
        
    }
    
    static interface SubstituteOffsetProvider {
        public int getSubstituteOffset();
    }
    
    public static class JspCompletionResult extends CompletionQuery.DefaultResult implements SubstituteOffsetProvider {
        private int substituteOffset;
        public JspCompletionResult(JTextComponent component, String title, List data, int offset, int len, int htmlAnchorOffset ) {
            super(component, title, data, offset, len);
            substituteOffset = htmlAnchorOffset == -1 ? offset - len : htmlAnchorOffset;
        }
        
        public int getSubstituteOffset() {
            return substituteOffset;
        }
    }
}

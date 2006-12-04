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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.ext.CompletionQuery;
import org.openide.util.NbBundle;
import org.netbeans.modules.web.core.syntax.*;

/** Support for attribute value completion for JSP tags and directives.
 *
 * @author  Petr Jiricka
 * @version
 */
public abstract class AttributeValueSupport extends Object {

    private static Map supports;

    public static void putSupport(AttributeValueSupport support) {
        if (supports == null)
            initialize();
        // trick so we can construct a 'dummy' key element and get the 'real' element
        supports.put(support, support);
    }
    
    public static AttributeValueSupport getSupport(boolean tag, String longName, String attrName) {
        if (supports == null)
            initialize();
        AttributeValueSupport support = new AttributeValueSupport.Default (tag, longName, attrName);
        return (AttributeValueSupport)supports.get(support);
    }
    
    private static void initialize() {
        supports = new HashMap();
        // jsp:useBean
        putSupport(new AttrSupports.ScopeSupport(true, "jsp:useBean", "scope"));     // NOI18N
        putSupport(new AttrSupports.PackageClassSupport(true, "jsp:useBean", "class")); // NOI18N
        // jsp:getProperty, jsp:setProperty
        putSupport(new AttrSupports.GetSetPropertyName(true, "jsp:getProperty", "name")); // NOI18N
        putSupport(new AttrSupports.GetSetPropertyName(true, "jsp:setProperty", "name")); // NOI18N
        putSupport(new AttrSupports.GetPropertyProperty());
        putSupport(new AttrSupports.SetPropertyProperty());
        // @taglib
        putSupport(new AttrSupports.TaglibURI());
        putSupport(new AttrSupports.TaglibTagdir());
        // @page
        putSupport(new AttrSupports.PackageClassSupport(false, "page", "import")); // NOI18N
        putSupport(new AttrSupports.PackageClassSupport(false, "page", "extends")); // NOI18N
        putSupport(new AttrSupports.PageLanguage());
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "session")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "autoFlush")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "isThreadSafe")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "isErrorPage")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport (false, "page", "errorPage")); //NOI18N
        putSupport(new AttrSupports.EncodingSupport(false, "page", "pageEncoding")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "page", "isELIgnored")); // NOI18N
        // @tag 
        putSupport(new AttrSupports.PackageClassSupport(false, "tag", "import")); // NOI18N
        putSupport(new AttrSupports.EncodingSupport(false, "tag", "pageEncoding")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "tag", "isELIgnored")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(false, "tag", "small-icon")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport(false, "tag", "large-icon")); // NOI18N
        // @attribute
        putSupport(new AttrSupports.TrueFalseSupport(false, "attribute", "required")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "attribute", "fragment")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(false, "attribute", "rtexprvalue")); // NOI18N
        // @variable
        putSupport(new AttrSupports.TrueFalseSupport(false, "variable", "declare")); // NOI18N
        putSupport(new AttrSupports.VariableScopeSupport(false, "variable", "scope")); // NOI18N
        putSupport(new AttrSupports.PackageClassSupport(false, "variable", "variable-class")); // NOI18N
        // @include
        putSupport(new AttrSupports.FilenameSupport (false, "include", "file")); //NOI18N
        putSupport(new AttrSupports.FilenameSupport (true, "jsp:directive.include", "file")); //NOI18N
        
        // jsp:include, jsp:forward
        putSupport(new AttrSupports.FilenameSupport (true, "jsp:include", "page")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport (true, "jsp:forward", "page")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:include", "flush")); // NOI18N
        
        putSupport(new AttrSupports.ScopeSupport(true, "jsp:doBody", "scope")); // NOI18N
        
        putSupport(new AttrSupports.ScopeSupport(true, "jsp:invoke", "scope")); // NOI18N
        // PENDING - add supports for known attributes
        
        // jsp:directive.page
        putSupport(new AttrSupports.PackageClassSupport(true, "jsp:directive.page", "import")); // NOI18N
        putSupport(new AttrSupports.PackageClassSupport(true, "jsp:directive.page", "extends")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "session")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "autoFlush")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "isThreadSafe")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "isErrorPage")); // NOI18N
        putSupport(new AttrSupports.FilenameSupport (true, "jsp:directive.page", "errorPage")); //NOI18N
        putSupport(new AttrSupports.EncodingSupport(true, "jsp:directive.page", "pageEncoding")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:directive.page", "isELIgnored")); // NOI18N
        
        putSupport(new AttrSupports.YesNoTrueFalseSupport(true, "jsp:output", "omit-xml-declaration")); // NOI18N
        putSupport(new AttrSupports.RootVersionSupport(true, "jsp:root", "version")); // NOI18N
        putSupport(new AttrSupports.PluginTypeSupport(true, "jsp:plugin", "type")); // NOI18N
        putSupport(new AttrSupports.TrueFalseSupport(true, "jsp:attribute", "trim")); // NOI18N
        
    }
    
    protected boolean tag;
    protected String longName;
    protected String attrName;

    /** Creates new AttributeValueSupport 
     * @param isTag whether this support is for tag or directive
     * @param longName either directive name or tag name including prefix
     * @param attribute name
     */
    public AttributeValueSupport(boolean tag, String longName, String attrName) {
        this.tag = tag;
        this.longName = longName;
        this.attrName = attrName;
    }
    
    public boolean equals(Object obj) {
        AttributeValueSupport sup2 = (AttributeValueSupport)obj;
        return (tag == sup2.tag) &&
               (longName.equals(sup2.longName)) &&
               (attrName.equals(sup2.attrName));
    }
    
    public int hashCode() {
        return longName.hashCode() + attrName.hashCode();
    }

    /** Returns the complete result. */
    public abstract CompletionQuery.Result getResult(JTextComponent component, 
        int offset, JspSyntaxSupport sup, SyntaxElement.TagDirective item, 
        String valuePart);
    
    /** Default implementation of AttributeValueSupport. 
     *  Only getPossibleValues method needs to be overriden for simple
     *  attribute support.
     */
    public static class Default extends AttributeValueSupport {
        
        /** Creates new DefaultAttributeValueSupport 
         * @param isTag whether this support is for tag or directive
         * @param longName either directive name or tag name including prefix
         * @param attribute name
         */
        public Default(boolean tag, String longName, String attrName) {
            super(tag, longName, attrName);
        }
        
        /** Allows subclasses to override the default title. */
        protected String completionTitle() {
            return NbBundle.getMessage (JSPKit.class, "CTL_JSP_Completion_Title");
        }
    
        /** Builds List of completion items.
         *  It uses results from <CODE>possibleValues</CODE> to build the list.
         */
        protected List createCompletionItems(int offset, JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            //int valuePartLength = valuePart.length();
            List values = sup.filterList(possibleValues(sup, item), valuePart);
            List items = new ArrayList();
            for (int i = 0; i < values.size(); i++) {
                items.add(new JspCompletionItem.AttributeValue((String)values.get(i)));
            }
            return items;
        }
    
        /** Should return a list of Strings containing all possible values 
         * for this attribute. May return null if no options are available.
         */
        protected List possibleValues(JspSyntaxSupport sup, SyntaxElement.TagDirective item) {
            return new ArrayList ();
        }
        
        /** Returns the complete result that contains elements from getCompletionItems.  
         *  This implemantation uses createCompletionItems for obtaing of results but may be 
         *  overriden.
         */
        public CompletionQuery.Result getResult (JTextComponent component, int offset, 
            JspSyntaxSupport sup, SyntaxElement.TagDirective item, String valuePart) {
            List items = createCompletionItems (offset, sup, item, valuePart);
            int valuePartLength = valuePart.length ();
            
            return new JspCompletionQuery.JspCompletionResult(component, completionTitle(), 
                items, offset - valuePartLength, valuePartLength, -1);
        }
        
    }
    
}
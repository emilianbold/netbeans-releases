/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.doclet;

import java.util.*;
import java.text.DateFormat;

import org.xml.sax.Locator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.AttributeList;

import org.xml.sax.DocumentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;

import org.netbeans.tax.*;
import org.netbeans.tax.decl.*;

/**
 * Creates [X]HTML documentation for DTD. 
 * <p>There is a special doclet comment preceding element declaration.
 * The comment is delimited by "<!---"  and "-->" i.e. starting
 * delimiter uses three "---".
 *
 * <p>Generated element content model consists of hyperlinked structure.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class DTDDoclet {

    private RefList s; //tmp holding whole result
    
    private RefList elementIndex = new RefList();
    
    private String comment; //last suitable comment or null if none

    /* This ellement is just being commented.
     * Used for "referenced by" creation.
     */
    private String currentElement = null;
    
    /* Hold element name keyed RefLists used at
     * "referenced by" section.
     */
    private HashMap elementRefs = new HashMap();
    
    // configurable output format
    
    private final String HEADLINE1 = "h2"; // NOI18N
    private final String HEADLINE2 = "b"; // NOI18N
    
    private final String LIST = "ul"; // NOI18N
    private final String LIST_ITEM = "li"; // NOI18N
    
    private final String PAR = "p"; // NOI18N
    
    private final String ROOT = "html"; // NOI18N
    private final String BODY = "body"; // NOI18N
    
    /** Creates new DTDDoclet engine*/
    public DTDDoclet() {
        
    }

    public String createDoclet (TreeDTD dtd) {
        if (dtd == null)
            return ""; // NOI18N
        
        Iterator it;        
        s = new RefList();
        
        s.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"); // NOI18N
        
        s.appendStartTag(ROOT).append("\n<head><title>DTD grammar documentation</title></head>\n"); // NOI18N
        s.append("\n<!-- Generated on " + DateFormat.getDateInstance().format(new Date()) + " by Forte XML module. -->\n"); // NOI18N
        s.appendStartTag(BODY);
        
        headline1(Util.getString("TEXT_Element_Index"));
        
        s.append(elementIndex);
        
        headline1(Util.getString("TEXT_Element_Details"));
                        
        it = dtd.getChildNodes(TreeChild.class, true).iterator();
        
        Vector list = new Vector();
        while (it.hasNext()) {
            TreeNode child = (TreeNode)it.next();
	    if (child instanceof TreeElementDecl) {
		commentElement((TreeElementDecl) child);
		comment = null;
	    } else if (child instanceof TreeComment) {
		comment = decodeComment((TreeComment) child);
            } else if (child instanceof TreeText) {
                // do not clear comment
	    } else {
		// disable comment
		comment = null;
            }
        }

        
        // create index at elementIndex reference position.
        // strings are sorted by alphabet
        
        TreeSet index = new TreeSet();  
        it = elementRefs.keySet().iterator();
                
        while (it.hasNext()) {
            index.add(it.next());
        }
        
        elementIndex.appendStartTag(LIST);
        it = index.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            listitem(elementIndex, "<a href=\"#" + next + "\">" + next + "</a>"); // NOI18N
        }
        elementIndex.appendEndTag(LIST);
        
        
        // terminate the html page
        //
        
        s.appendEndTag(BODY).appendEndTag(ROOT);
        return s.toString();
    }

    //~~~~~~~~~~~~~~~ DOCUMENTATION STUFF ~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /** Comment is paired just with the next element declaration and 
     * it must statt with <!---.
     */
    private String decodeComment(TreeComment node) {
        return node.getData().startsWith("-") ? node.getData().substring(1).trim() : null; // NOI18N
    }
    

    
    private void commentElement(TreeElementDecl node) {
        String tag = node.getName();
        currentElement = tag;
        
        s.append("\n<a name=\"" + tag + "\"></a>\n"); // NOI18N
        
        headline1(tag);        
        s.append(comment == null ? "" : "<p>" + comment + "</p>"); // NOI18N
        commentAttributes(node);
        
        headline2(Util.getString("TEXT_ContentModel"));        
        TreeElementDecl.ContentType type = node.getContentType();
        commentContentModel(type);

        headline2(Util.getString("TEXT_Referenced_by"));
        s.append(getRefList(tag));
        s.append("\n\n"); // NOI18N
    } 
    
    /*
     * attributes are commented like Java fields declarations.
     *  <modifier> <type> <name> = <default>
     */
    private void commentAttributes(TreeElementDecl element) {
        
        Iterator it = element.getAttributeDefs().iterator();
        
        if (!!! it.hasNext())
	    return;
        
        headline2(Util.getString("TEXT_Declared_Attributes"));
        s.appendStartTag(LIST).append("<tt>"); // NOI18N
        
        while (it.hasNext()) {
            TreeAttlistDeclAttributeDef next = (TreeAttlistDeclAttributeDef) it.next();
            
            String defVal  = next.getDefaultValue();
            defVal = defVal.length() == 0 ? "" : " = " + defVal; // NOI18N
            
            String defType = next.getDefaultTypeName() == null ? "#DEFAULT" : next.getDefaultTypeName(); // NOI18N
            
            String text = "";  // NOI18N
            
            if (next.getType()  == next.TYPE_ENUMERATED) {
                text = defType + " ENUMERATION " +  next.getEnumeratedTypeString(); // NOI18N
            } else {
                text = defType + " " + next.getTypeName(); // NOI18N
            }
            
            text += " " + next.getName() + defVal;  // NOI18N
            
            listitem(s, text);
        }
        
        s.append("</tt>").appendEndTag(LIST); // NOI18N
    }

    /*
     * Content model documentation consists of links to element definitions.
     * It is recursive implementation.  
     */
    private void commentContentModel (TreeElementDecl.ContentType type) {

        if (type instanceof EMPTYType) {
            
            s.append("EMPTY"); // NOI18N
            return;
            
        } else if (type instanceof ANYType) {

            s.append("ANY"); // NOI18N
            return;
            
        } 

        
        if (type instanceof LeafType) {
            
            LeafType leaf = (LeafType)type;
            String tag = leaf.getName();
            s.append("<a href=\"#").append(tag).append("\">"); // NOI18N
            s.append(tag).append("</a>").append(leaf.getMultiplicity()); // NOI18N
            
            // append "tag reference from currentElement" as fragment to force uniques // NOI18N
            // because one content model can reference same tag more times
            RefList refs = getRefList(tag);
            String prefixSeparator = ", ";  // NOI18N
            if (refs.isEmpty()) {
                prefixSeparator = "";  // NOI18N
            }
            
            refs.appendUniq(prefixSeparator + "<a href=\"#" + currentElement + "\">" + currentElement + "</a>"); // NOI18N
            
            return;
        }


        s.append("(");  // NOI18N

        if (type.allowText()) {
                    
            s.append("#PCDATA"); // NOI18N
            
        } 
        

        // it is a group, select proper separator  
        
        String separator = "";  // NOI18N
        if (type instanceof ChildrenType) {
            String sepChar = ((ChildrenType)type).getSeparator();
            
            //',' always directly follows previous word otherwise put and extra space
            String prefix = sepChar.equals(",") == false ? "&nbsp;" : "";  // NOI18N
            separator = prefix + sepChar + " "; // NOI18N
        }
        
        // do not prepend separator if first
        boolean prependSeparator = type.allowText();  //#PCDATA  
        
        // recursive descend through given content model groups
        
        if (type instanceof TypeCollection) {
            TypeCollection col = (TypeCollection) type;
            Collection types = col.getTypes();
                                    
            Iterator it = types.iterator();
            while (it.hasNext()) {
                TreeElementDecl.ContentType next = (TreeElementDecl.ContentType) it.next();
                
                if (prependSeparator) {
                    s.append (separator);
                }
                
                prependSeparator = true;
                    
                commentContentModel(next);
            }                                    
        }

        s.append(")" + type.getMultiplicity());  // NOI18N
        
    }

    private void headline1(String text) {
        s.append("\n<hr />\n").appendStartTag(HEADLINE1).append(text).appendEndTag(HEADLINE1).append("\n"); // NOI18N
    }

    private void headline2(String text) {
        s.append("\n").appendStartTag(PAR).appendStartTag(HEADLINE2).append(text).appendEndTag(HEADLINE2).appendEndTag(PAR).append("\n"); // NOI18N
    }
    
    private void listitem(RefList s, String text) {
        s.appendStartTag(LIST_ITEM).append(text).appendEndTag(LIST_ITEM).append("\n"); // NOI18N
    }
    
    //~~~~~~~~~~~~~~~~~ BACKGROUND STUFF ~~~~~~~~~~~~~~~~~~~~~~~

    
    

    /*
     * @return RefList for given tag, never null
     */
    private RefList getRefList(String element) {
  
        RefList toret = (RefList) elementRefs.get(element);
        
        if (toret == null) {
            toret = new RefList();
            elementRefs.put(element, toret);
        }
        
        return toret;
    }
    
        
    /** Ref list allows embedding an empty list that is contructed later. 
     * The implementation mimics StringBuffer append() behaviour.
     */
    private class RefList extends LinkedList {
        
        /** Serial Version UID */
        private static final long serialVersionUID = 4291872863957329823L;

        /**
         * @param obj accepts String, StringBuffer or RefList instances.
         */
        public boolean add(Object obj) {
            if (obj instanceof StringBuffer || obj instanceof RefList || obj instanceof String) {
                return super.add(obj);
            } else {
                throw new ClassCastException("String or RefList required."); // NOI18N
            }
        }
        
        public RefList append(String s) {
            if (size() != 0 && (getLast() instanceof StringBuffer)) {
                StringBuffer last = (StringBuffer) getLast();
                last.append(s);
            } else {
                add(new StringBuffer(s));                
            }
            return this;
        }

        public RefList append(RefList s) {
            add(s);
            return this;
        }
        
        /** Append it as fragment only if this fragment does not exist */
        public RefList appendUniq(String fragment) {
            if (!contains(fragment)) add(fragment);
            return this;
        }
        
        // append start tag
        RefList appendStartTag(String tag) {
            return append("<" +  tag + ">"); // NOI18N
        }
        
        RefList appendEndTag(String tag) {
            return append("</" + tag + ">"); // NOI18N
        }

        
        public String toString() {
            Iterator it = iterator();
            StringBuffer buf = new StringBuffer();
            while(it.hasNext()) {
                buf.append(it.next().toString());
            }
            return buf.toString();            
        }
                
    }
}

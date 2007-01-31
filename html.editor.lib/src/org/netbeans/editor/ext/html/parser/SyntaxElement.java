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


package org.netbeans.editor.ext.html.parser;


import org.netbeans.editor.ext.html.*;
import java.util.*;
import javax.swing.text.*;
import org.netbeans.editor.ext.*;
import org.openide.ErrorManager;

/**This class is used during the analysis of the HTML code.
 *
 * It is an element of the dynamically created chain of other SyntaxElements.
 * The access to it is done through the HTMLSyntaxSupport, which also takes
 * care of dynamically extending it when needed.
 *
 * @author  Petr Nejedly
 * @author  Marek.Fukala@Sun.com
 * @version 1.0
 */
public class SyntaxElement {
    
    public static final int TYPE_COMMENT = 0;
    public static final int TYPE_DECLARATION = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_TEXT = 3;
    public static final int TYPE_TAG = 4;
    public static final int TYPE_ENDTAG = 5;
    public static final int TYPE_SCRIPT = 6;
    
    public static final String[] TYPE_NAMES =
            new String[]{"comment","declaration","error","text","tag","endtag","script"};
    
    private SyntaxElement previous;
    private SyntaxElement next;
    private SyntaxParser parser;
    
    int offset;
    int length;
    int type;
    
    /** Creates new SyntaxElement */
    public SyntaxElement( SyntaxParser parser, int from, int to, int type ) {
        this.offset = from;
        this.length = to-from;
        this.type = type;
        this.parser = parser;
    }
    
    public int getElementOffset() {
        return offset;
    }
    
    public int getElementLength() {
        return length;
    }
    
    public int getType() {
        return type;
    }
    
    
    public String getText() {
        try {
            return parser.getDocument().getText(getElementOffset(), getElementLength());
        }catch(BadLocationException ble) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ble);
        }
        return null;
    }
    
    public SyntaxElement getPrevious() throws BadLocationException {
        if( previous == null ) {
            previous = parser.getPreviousElement( offset );
            if( previous != null ) previous.next = this;
        }
        return previous;
    }

    public SyntaxElement getNext() throws BadLocationException {
        if( next == null ) {
            next = parser.getNextElement( offset+length );
            if( next != null ) next.previous = this;
        }
        return next;
    }

    
    public String toString() {
        String textContent = getType() == TYPE_TEXT ? getText() : "";
        return "Element(" +TYPE_NAMES[type]+")[" + offset + "," + (offset+length-1) + "] \"" + textContent + ""; // NOI18N
    }
    
    
    /**
     * Declaration models SGML declaration with emphasis on &lt;!DOCTYPE
     * declaration, as other declarations are not allowed inside HTML.
     * It represents unknown/broken declaration or either public or system
     * DOCTYPE declaration.
     */
    public static class Declaration extends SyntaxElement {
        private String root;
        private String publicID;
        private String file;
        
        
        /**
         * Creates a model of SGML declaration with some properties of
         * DOCTYPE declaration.
         * @param doctypeRootElement the name of the root element for a DOCTYPE.
         *  Can be null to express that the declaration is not DOCTYPE
         *  declaration or is broken.
         * @param doctypePI public identifier for this DOCTYPE, if available.
         *  null for system doctype or other/broken declaration.
         * @param doctypeFile system identifier for this DOCTYPE, if available.
         *  null otherwise.
         */
        public Declaration( SyntaxParser parser, int from, int to,
                String doctypeRootElement,
                String doctypePI, String doctypeFile
                ) {
            super( parser, from, to, TYPE_DECLARATION );
            root = doctypeRootElement;
            publicID = doctypePI;
            file = doctypeFile;
        }
        
        /**
         * @return the name of the root element for a DOCTYPE declaration
         * or null if the declatarion is not DOCTYPE or is broken.
         */
        public String getRootElement() {
            return root;
        }
        
        /**
         * @return a public identifier of the PUBLIC DOCTYPE declaration
         * or null for SYSTEM DOCTYPE and broken or other declaration.
         */
        public String getPublicIdentifier() {
            return publicID;
        }
        
        /**
         * @return a system identifier of both PUBLIC and SYSTEM DOCTYPE
         * declaration or null for PUBLIC declaration with system identifier
         * not specified and broken or other declaration.
         */
        public String getDoctypeFile() {
            return file;
        }
        
    }
    
    public static class Named extends SyntaxElement {
        String name;
        
        public Named( SyntaxParser parser, int from, int to, int type, String name ) {
            super( parser, from, to, type );
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        public String toString() {
            return super.toString() + " - \"" + name + '"'; // NOI18N
        }
    }
    
    
    public static class Tag extends org.netbeans.editor.ext.html.parser.SyntaxElement.Named {
        Collection attribs;
        private boolean empty = false;
        
        public Tag( SyntaxParser parser, int from, int to, String name, Collection attribs) {
            this(parser, from, to, name, attribs, false);
        }
        
        public Tag( SyntaxParser parser, int from, int to, String name, Collection attribs, boolean isEmpty ) {
            super( parser, from, to, TYPE_TAG, name );
            this.attribs = attribs;
            this.empty = isEmpty;
        }
        
        public boolean isEmpty() {
            return empty;
        }
        
        public Collection getAttributes() {
            return attribs;
        }
        
        public String toString() {
            StringBuffer ret = new StringBuffer( super.toString() );
            ret.append( " - {" );   // NOI18N
            
            for( Iterator i = attribs.iterator(); i.hasNext(); ) {
                ret.append( i.next() );
                ret.append( ", "  );    // NOI18N
            }
            
            ret.append( "}" );      //NOI18N
            if(isEmpty()) ret.append(" (EMPTY TAG)");
            
            return ret.toString();
        }
    }
    
}

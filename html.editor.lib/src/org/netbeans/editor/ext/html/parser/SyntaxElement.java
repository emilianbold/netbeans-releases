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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.editor.ext.html.parser;


import java.util.*;
import javax.swing.text.*;
import org.openide.ErrorManager;

/**
 * Represents a semantic element of html code.
 *
 * @author  Petr Nejedly
 * @author  Marek.Fukala@Sun.com
 */
public class SyntaxElement {
    
    public static final int TYPE_COMMENT = 0;
    public static final int TYPE_DECLARATION = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_TEXT = 3;
    public static final int TYPE_TAG = 4;
    public static final int TYPE_ENDTAG = 5;
    public static final int TYPE_ENTITY_REFERENCE = 6;
    
    public static final String[] TYPE_NAMES =
            new String[]{"comment","declaration","error","text","tag","endtag","entity reference"};
    
    private ParserSource source;
    
    private int offset;
    private int length;
    private int type;
    
    SyntaxElement( ParserSource doc, int offset, int length, int type ) {
        this.offset = offset;
        this.length = length;
        this.type = type;
        this.source = doc;
    }
    
    public int offset() {
        return offset;
    }
    
    public int length() {
        return length;
    }
    
    public int type() {
        return type;
    }
    
    public String text() {
        try {
            return source.getText(offset(), length()).toString();
        }catch(BadLocationException ble) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ble);
        }
        return null;
    }

    @Override
    public String toString() {
        //String textContent = type() == TYPE_TEXT ? text() : "";
        String textContent = text();
        return "Element(" +TYPE_NAMES[type]+")[" + offset + "," + (offset+length-1) + "] \"" + textContent + "\""; // NOI18N
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
        public Declaration( ParserSource document, int from, int length,
                String doctypeRootElement,
                String doctypePI, String doctypeFile
                ) {
            super( document, from, length, TYPE_DECLARATION );
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
        
        public Named( ParserSource document, int from, int to, int type, String name ) {
            super( document, from, to, type );
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
        private List<TagAttribute> attribs;
        private boolean empty, openTag;
                
        public Tag( ParserSource document, int from, int length, String name, List attribs, boolean openTag, boolean isEmpty ) {
            super( document, from, length, openTag ? TYPE_TAG : TYPE_ENDTAG, name );
            this.attribs = attribs;
            this.openTag = openTag;
            this.empty = isEmpty;
        }
        
        public boolean isEmpty() {
            return empty;
        }
        
        /** @return true if the tag represents an open tag, false if the tag is a close tag.
         */
        public boolean isOpenTag() {
            return openTag;
        }
        
        public List<TagAttribute> getAttributes() {
            return attribs == null ? Collections.EMPTY_LIST : attribs;
        }
        
        public TagAttribute getAttribute(String name) {
            return getAttribute(name, true);
        }
        
        public TagAttribute getAttribute(String name, boolean ignoreCase) {
            if(attribs == null) {
                return null;
            }
            for(TagAttribute ta : attribs) {
                if(ta.getName().equals(name)) {
                    return ta;
                }
            }
            return null;
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
    
    public static class TagAttribute {
        
        private String name, value;
        private int nameOffset, valueOffset, valueLength;
        
        TagAttribute(String name, String value, int nameOffset, int valueOffset, int valueLength) {
            this.name = name;
            this.value = value;
            this.nameOffset = nameOffset;
            this.valueOffset = valueOffset;
            this.valueLength = valueLength;
        }
        
        public String getName() {
            return name;
        }
        
        void setName(String name) {
            this.name = name;
        }
        
        public String getValue() {
            return value;
        }
        
        public int getValueLength() {
            return valueLength;
        }
        
        void setValue(String value) {
            this.value = value;
        }
        
        public int getNameOffset() {
            return nameOffset;
        }
        
        void setNameOffset(int ofs) {
            this.nameOffset = ofs;
        }
        
        public int getValueOffset() {
            return valueOffset;
        }
        
        void setValueOffset(int ofs) {
            this.valueOffset = ofs;
        }
        
        public String toString() {
            return "TagAttribute[name=" + getName() + "; value=" + getValue() + "; nameOffset=" + getNameOffset() + "; valueOffset=" + getValueOffset() +"]";
        }
       
        //backward compatibility
        public boolean equals(Object o) {
            if (!(o instanceof TagAttribute)) {
                return false;
            } else {
                return getName().equals(((TagAttribute)o).getName());
            }
        }
    }
}

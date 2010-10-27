/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;

/**
 * Represents a semantic element of html code.
 *
 * @author  Petr Nejedly
 * @author  Marek.Fukala@Sun.com
 */
public abstract class SyntaxElement {
    
    public static final int TYPE_COMMENT = 0;
    public static final int TYPE_DECLARATION = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_TEXT = 3;
    public static final int TYPE_TAG = 4;
    public static final int TYPE_ENDTAG = 5;
    public static final int TYPE_ENTITY_REFERENCE = 6;
    
    public static final String[] TYPE_NAMES =
            new String[]{"comment","declaration","error","text","tag","endtag","entity reference"}; //NOI18N

    private CharSequence source;

    private List<ProblemDescription> problems;
    
    private int offset;
    private int length;
    
    private SyntaxElement( CharSequence doc, int offset, int length) {
        assert offset >=0 : "start offset must be >= 0 !";
        assert length >=0 : "element length must be positive!";

        this.offset = offset;
        this.length = length;
        this.source = doc;
    }
    
    public int offset() {
        return offset;
    }
    
    public int length() {
        return length;
    }
    
    public abstract int type();
    
    public CharSequence text() {
        return source.subSequence(offset(), offset() + length());
    }

    public List<ProblemDescription> getProblems() {
        return problems;
    }

    synchronized void addProblem(ProblemDescription problem) {
        assert problem != null;
        if(problems == null) {
            problems = Collections.singletonList(problem); //save some memory for just one problem per element
        } else {
            if(problems.size() == 1) {
                ProblemDescription existing = problems.get(0);
                problems = new ArrayList<ProblemDescription>();
                problems.add(existing);
            }
            problems.add(problem);
        }
    }

    @Override
    public String toString() {
        //String textContent = type() == TYPE_TEXT ? text() : "";
        CharSequence textContent = text();
        return "Element(" +TYPE_NAMES[type()]+")[" + offset + "," + (offset+length-1) + "] \"" + textContent + "\""; // NOI18N
    }

    public static class SharedTextElement extends SyntaxElement {

        private static final String TO_STRING = "<n/a>"; //NOI18N

        public SharedTextElement() {
            super(null, 0, 0);
        }

        @Override
        public int length() {
            assert false;
            return super.length();
        }

        @Override
        public int offset() {
            assert false;
            return super.offset();
        }

        @Override
        public CharSequence text() {
            return TO_STRING;
        }

        @Override
        public int type() {
            return TYPE_TEXT;
        }

    }

    public static class Error extends SyntaxElement {

        public Error(CharSequence doc, int offset, int length) {
            super(doc, offset, length);
        }

        @Override
        public int type() {
            return TYPE_ERROR;
        }


    }

    public static class EntityReference extends SyntaxElement {

        public EntityReference(CharSequence doc, int offset, int length) {
            super(doc, offset, length);
        }

        @Override
        public int type() {
            return TYPE_ENTITY_REFERENCE;
        }


    }

    public static class Comment extends SyntaxElement {

        public Comment(CharSequence doc, int offset, int length) {
            super(doc, offset, length);
        }

        @Override
        public int type() {
            return TYPE_COMMENT;
        }

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
        private String doctypeName;
        
        
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
        public Declaration( CharSequence document, int from, int length,
                String doctypeRootElement,
                String doctypePI, String doctypeFile, String doctypeName
                ) {
            super( document, from, length );
            root = doctypeRootElement;
            publicID = doctypePI;
            file = doctypeFile;
            this.doctypeName = doctypeName;
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

        /**
         * @return the declaration id name, e.g. DOCTYPE for <!DOCTYPE ... > declaration
         */
        public String getDeclarationName() {
            return doctypeName;
        }

        public boolean isValidDoctype() {
            return "doctype".equalsIgnoreCase(getDeclarationName()) && getRootElement() != null; //NOI18N
        }

        @Override
        public int type() {
            return TYPE_DECLARATION;
        }


        
    }
    
    public static abstract class Named extends SyntaxElement {
        String name;
        
        public Named( CharSequence document, int from, int to, String name ) {
            super( document, from, to);
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return super.toString() + " - \"" + name + '"'; // NOI18N
        }
    }
    
    
    public static class Tag extends org.netbeans.editor.ext.html.parser.SyntaxElement.Named {
        private List<TagAttribute> attribs;
        private boolean empty, openTag;
                
        public Tag( CharSequence document, int from, int length, String name, List attribs, boolean openTag, boolean isEmpty ) {
            super( document, from, length, name );
            this.attribs = attribs;
            this.openTag = openTag;
            this.empty = isEmpty;
        }

        @Override
        public int type() {
            return openTag ? TYPE_TAG : TYPE_ENDTAG;
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
            for(TagAttribute ta : getAttributes()) {
                if(ta.getName().equals(name)) {
                    return ta;
                }
            }
            return null;
        }
        
        @Override
        public String toString() {
            StringBuffer ret = new StringBuffer( super.toString() );
            ret.append( " - {" );   // NOI18N

            for( Iterator i = getAttributes().iterator(); i.hasNext(); ) {
                ret.append( i.next() );
                ret.append( ", "  );    // NOI18N
            }
            
            ret.append( "}" );      //NOI18N
            if(isEmpty()) ret.append(" (EMPTY TAG)"); //NOI18N
           
            return ret.toString();
        }
    }
    
    public static class TagAttribute {
        
        private String name, value;
        private int nameOffset, valueOffset, valueLength;
        
        public TagAttribute(String name, String value, int nameOffset, int valueOffset, int valueLength) {
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
        
        @Override
        public String toString() {
            return "TagAttribute[name=" + getName() + "; value=" + getValue() + "; nameOffset=" + getNameOffset() + "; valueOffset=" + getValueOffset() +"]"; //NOI18N
        }
       
        //backward compatibility
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TagAttribute)) {
                return false;
            } else {
                return getName().equals(((TagAttribute)o).getName());
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }
}

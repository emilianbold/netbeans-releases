/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.editor.index;

import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.openide.filesystems.FileObject;


/**
 * An element coming from the Lucene index - not tied to an AST.
 * To obtain an equivalent AST element, use AstUtilities.getForeignNode().
 * 
 * @author Tor Norbye
 */
public abstract class IndexedElement extends PHPElement {

    protected ElementKind kind;
    protected String name;
    protected String in;
    protected PHPIndex index;
    protected String fileUrl;
    protected Document document;
    protected FileObject fileObject;
    protected int flags;
    protected String attributes;
    protected String signature;
    protected boolean smart;
    protected boolean inherited = true;

    IndexedElement(String name, String in, PHPIndex index, String fileUrl, String attributes, int flags, ElementKind kind) {
        this.name = name;
        this.in = in;
        this.index = index;
        this.fileUrl = fileUrl;
        this.attributes = attributes;
        this.flags = flags;
        this.kind = kind;
    }
    
    public String getSignature() {
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            if (in != null) {
                sb.append(in);
                sb.append('.');
            }
            sb.append(name);
            signature = sb.toString();
        }

        return signature;
    }
    
    public PHPIndex getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public String getIn() {
        return in;
    }
    
    @Override
    public ElementKind getKind() {
        return kind;
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (isStatic()) {
            return AstElement.STATIC;
        }
        return Collections.emptySet();
    }

    public String getFilenameUrl() {
        return fileUrl;
    }

    public Document getDocument() throws IOException {
        if (document == null) {
            FileObject fo = getFileObject();

            if (fo == null) {
                return null;
            }

            document = NbUtilities.getBaseDocument(fileObject, true);
        }

        return document;
    }

    public ParserFile getFile() {
        boolean platform = false; // XXX FIND OUT WHAT IT IS!

        return new DefaultParserFile(getFileObject(), null, platform);
    }

    @Override
    public FileObject getFileObject() {
        if ((fileObject == null) && (fileUrl != null)) {
            fileObject = PHPIndex.getFileObject(fileUrl);

            if (fileObject == null) {
                // Don't try again
                fileUrl = null;
            }
        }

        return fileObject;
    }

    protected int getAttributeSection(int section) {
        assert section != 0; // Obtain directly, and logic below (+1) is wrong
        int attributeIndex = 0;
        for (int i = 0; i < section; i++) {
            attributeIndex = attributes.indexOf(';', attributeIndex+1);
        }
        
        assert attributeIndex != -1;
        return attributeIndex + 1;
    }
    
//    int getDocOffset() {
//        int docOffsetIndex = getAttributeSection(DOC_INDEX);
//        if (docOffsetIndex != -1) {
//            int docOffset = IndexedElement.decode(attributes, docOffsetIndex,-1);
//            return docOffset;
//        }
//        return -1;
//    }
    
//    protected List<String> getComments() {
//        int docOffsetIndex = getAttributeSection(DOC_INDEX);
//        if (docOffsetIndex != -1) {
//            int docOffset = IndexedElement.decode(attributes, docOffsetIndex,-1);
//            if (docOffset == -1) {
//                return null;
//            }
//            try {
//                BaseDocument doc = (BaseDocument) getDocument();
//                if (doc == null) {
//                    return null;
//                }
//                if (docOffset < doc.getLength()) {
//                    //return LexUtilities.gatherDocumentation(null, doc, docOffset);
//                    OffsetRange range = OffsetRange.NONE;// LexUtilities.getCommentBlock(doc, docOffset, false);
//                    if (range != OffsetRange.NONE) {
//                        String comment = doc.getText(range.getStart(), range.getLength());
//                        String[] lines = comment.split("\n");
//                        List<String> comments = new ArrayList<String>();
//                        for (int i = 0, n = lines.length; i < n; i++) {
//                            String line = lines[i];
//                            line = line.trim();
//                            if (i == n-1 && line.endsWith("*/")) {
//                                line = line.substring(0,line.length()-2);
//                            }
//                            if (line.startsWith("/**")) {
//                                comments.add(line.substring(3));
//                            } else if (line.startsWith("/*")) {
//                                comments.add(line.substring(2));
//                            } else if (line.startsWith("//")) {
//                                comments.add(line.substring(2));
//                            } else if (line.startsWith("*")) {
//                                comments.add(line.substring(1));
//                            } else {
//                                comments.add(line);
//                            }
//                        }
//                        return comments;
//                    }
//                    return Collections.emptyList();
//                }
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (IOException ioe) {
//                Exceptions.printStackTrace(ioe);
//                return null;
//            }
//        }
//            
//        return null;
//    }

//    public String getType() {
//        int typeIndex = getAttributeSection(TYPE_INDEX);
//        int endIndex = attributes.indexOf(';', typeIndex);
//        if (endIndex > typeIndex) {
//            return attributes.substring(typeIndex, endIndex);
//        }
//        
//        return null;
//    }

    public void setSmart(boolean smart) {
        this.smart = smart;
    }

    public boolean isSmart() {
        return smart;
    }
    
    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    public boolean isInherited() {
        return inherited;
    }

    protected static final int NAME_INDEX = 0;
//    protected static final int IN_INDEX = 1;
//    protected static final int CASE_SENSITIVE_INDEX = 2;
//    protected static final int FLAG_INDEX = 3;
    protected static final int ARG_INDEX = 1;
    protected static final int OFFSET_INDEX = 2;
//    protected static final int NODE_INDEX = 5;
//    protected static final int DOC_INDEX = 6;
//    protected static final int BROWSER_INDEX = 7;
//    protected static final int TYPE_INDEX = 8;
    
    
    
    // ------------- Flags/attributes -----------------

    // This should go into IndexedElement
    
    // Other attributes:
    // is constructor? prototype?
    
    // Plan: Stash a single item for class entries so I can search by document for the class.
    // Add more types into the types

    /** Return a string (suitable for persistence) encoding the given flags */
    public static String encode(int flags) {
        return Integer.toString(flags,16);
    }
    
    /** Return flag corresponding to the given encoding chars */
    public static int decode(String s, int startIndex, int defaultValue) {
        int value = 0;
        for (int i = startIndex, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if (c == ';') {
                if (i == startIndex) {
                    return defaultValue;
                }
                break;
            }

            value = value << 4;
 
            if (c > '9') {
                value += c-'a'+10;
            } else {
                value += c-'0';
            }
        }
        
        return value;
    }
    
    public boolean isPublic() {
        return (flags & BodyDeclaration.Modifier.PUBLIC) == 0;
    }

    public boolean isPrivate() {
        return (flags & BodyDeclaration.Modifier.PRIVATE) != 0;
    }
    
    public boolean isStatic() {
        return (flags & BodyDeclaration.Modifier.STATIC) != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedElement other = (IndexedElement) obj;
        if (!getSignature().equals(other.getSignature())) {
            return false;
        }
//        if (this.flags != other.flags) {
//            return false;
//        }
        if (!getKind().equals(other.getKind())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + getSignature().hashCode();
//        hash = 53 * hash + flags;
        hash = 53 * hash + getKind().hashCode();
        return hash;
    }
    
    public int getOffset(){
        return -1;
    }
}

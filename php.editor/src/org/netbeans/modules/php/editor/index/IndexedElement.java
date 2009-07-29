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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.GsfUtilities;
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
    private String fileUrl;
    protected Document document;
    protected FileObject fileObject;
    protected int flags;
    protected String textSignature;
    protected boolean smart;
    protected boolean inherited = true;
    protected boolean resolved = true;
    protected int offset;

    IndexedElement(String name, String in, PHPIndex index, String fileUrl, int offset, int flags, ElementKind kind) {
        this.name = name;
        this.in = in;
        this.index = index;
        this.fileUrl = fileUrl;
        this.offset = offset;
        this.flags = flags;
        this.kind = kind;
        
        if (fileUrl != null && fileUrl.contains(" ")){
            throw new IllegalArgumentException("fileURL may not contain spaces!");
        }
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public String getSignature() {
        if (textSignature == null) {
            StringBuilder sb = new StringBuilder();
            if (in != null) {
                sb.append(in);
                sb.append('.');
            }
            sb.append(name);
            textSignature = sb.toString();
        }

        return textSignature;
    }
    
    public PHPIndex getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name;
    }
    
    // used e.g. for online docs
    public String getDisplayName(){
        String modifiersStr = getModifiersString();
        return  modifiersStr.length() == 0 ? getName() : modifiersStr + " " + getName(); //NOI18N
    }
    
    public String getModifiersString(){
        return BodyDeclaration.Modifier.toString(flags);
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
        Set<Modifier> retval = new HashSet<Modifier>();
        if (isStatic()) {
            retval.add(Modifier.STATIC);
        } else if (isPublic()) {
            retval.add(Modifier.PUBLIC);
        }  else if (isProtected()) {
            retval.add(Modifier.PROTECTED);
        } else if (isPrivate()) {
            retval.add(Modifier.PRIVATE);
        }
        return retval;
    }

    public String getFilenameUrl() {
        return fileUrl;
    }

    public boolean isPlatform(){
        return false; //TODO implement me
    }

    public Document getDocument() throws IOException {
        if (document == null) {
            FileObject fo = getFileObject();

            if (fo == null) {
                return null;
            }

            document = GsfUtilities.getDocument(fileObject, true);
        }

        return document;
    }

    @Override
    @CheckForNull // see issue #147457
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
        return (flags & BodyDeclaration.Modifier.PUBLIC) != 0;
    }

    public boolean isProtected() {
        return (flags & BodyDeclaration.Modifier.PROTECTED) != 0;
    }

    public boolean isPrivate() {
        return (flags & BodyDeclaration.Modifier.PRIVATE) != 0;
    }
    
    public boolean isStatic() {
        return (flags & BodyDeclaration.Modifier.STATIC) != 0;
    }

    public boolean isFinal() {
        return (flags & BodyDeclaration.Modifier.FINAL) != 0;
    }

    public boolean isAbstract() {
        return (flags & BodyDeclaration.Modifier.ABSTRACT) != 0;
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

    public int getFlags() {
        return flags;
    }    
}

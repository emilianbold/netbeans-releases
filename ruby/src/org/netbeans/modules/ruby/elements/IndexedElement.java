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
package org.netbeans.modules.ruby.elements;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.swing.text.Document;

import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.openide.filesystems.FileObject;


/**
 * A program element coming from the persistent index.
 *
 * @author Tor Norbye
 */
public abstract class IndexedElement extends RubyElement {
    /** This method is documented */
    public static final int DOCUMENTED = 1 << 0;
    /** This method is protected */
    public static final int PROTECTED = 1 << 1;
    /** This method is private */
    public static final int PRIVATE = 1 << 2;
    /** This method is top level (implicit member of Object) */
    public static final int TOPLEVEL = 1 << 3;
    /** This element is "static" (e.g. it's a classvar for fields, class method for methods etc) */
    public static final int STATIC = 1 << 4;
    /** This element is deliberately not documented (rdoc :nodoc:) */
    public static final int NODOC = 1 << 5;
    
    protected String fileUrl;
    protected final String clz;
    protected final String fqn;
    protected final RubyIndex index;
    protected final String require;
    protected final String attributes;
    protected Set<Modifier> modifiers;
    protected int flags;
    protected int docLength = -1;
    private Document document;
    private FileObject fileObject;

    protected IndexedElement(RubyIndex index, String fileUrl, String fqn,
        String clz, String require, String attributes, int flags) {
        this.index = index;
        this.fileUrl = fileUrl;
        this.fqn = fqn;
        this.require = require;
        this.attributes = attributes;
        // XXX Why do methods need to know their clz (since they already have fqn)
        this.clz = clz;
        this.flags = flags;
    }

    public abstract String getSignature();

    public final String getFileUrl() {
        return fileUrl;
    }

    public final String getRequire() {
        return require;
    }

    public final String getFqn() {
        return fqn;
    }

    @Override
    public String toString() {
        return getSignature() + ":" + getFileUrl();
    }

    public final String getClz() {
        return clz;
    }

    public RubyIndex getIndex() {
        return index;
    }

    @Override
    public String getIn() {
        return getClz();
    }

    public String getFilenameUrl() {
        return fileUrl;
    }

    public Document getDocument() {
        if (document == null) {
            FileObject fo = getFileObject();

            if (fo == null) {
                return null;
            }

            document = GsfUtilities.getDocument(fileObject, true);
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
            fileObject = RubyIndex.getFileObject(fileUrl);

            if (fileObject == null) {
                // Don't try again
                fileUrl = null;
            }
        }

        return fileObject;
    }

    @Override
    public final Set<Modifier> getModifiers() {
        if (modifiers == null) {
            Modifier access = Modifier.PUBLIC;
            if (isPrivate()) {
                access = Modifier.PRIVATE;
            } else if (isProtected()) {
                access = Modifier.PROTECTED;
            }
            boolean isStatic = isStatic();
            
            if (access != Modifier.PUBLIC) {
                if (isStatic) {
                    modifiers = EnumSet.of(access, Modifier.STATIC);
                } else {
                    modifiers = EnumSet.of(access);
                }
            } else if (isStatic) {
                modifiers = EnumSet.of(Modifier.STATIC);
            } else {
                modifiers = Collections.emptySet();
            }
        }
        return modifiers;
    }

    /** Return the length of the documentation for this class, in characters */
    public int getDocumentationLength() {
        return isDocumented() ? 1 : 0;
    }
    
    /** Return a string (suitable for persistence) encoding the given flags */
    public static char flagToFirstChar(int flags) {
        char first = (char)(flags >>= 4);
        if (first >= 10) {
            return (char)(first-10+'a');
        } else {
            return (char)(first+'0');
        }
    }

    /** Return a string (suitable for persistence) encoding the given flags */
    public static char flagToSecondChar(int flags) {
        char second = (char)(flags & 0xf);
        if (second >= 10) {
            return (char)(second-10+'a');
        } else {
            return (char)(second+'0');
        }
    }
    
    /** Return a string (suitable for persistence) encoding the given flags */
    public static String flagToString(int flags) {
        return (""+flagToFirstChar(flags)) + flagToSecondChar(flags);
    }
    
    /** Return flag corresponding to the given encoding chars */
    public static int stringToFlag(String s, int startIndex) {
        return stringToFlag(s.charAt(startIndex), s.charAt(startIndex+1));
    }
    
    /** Return flag corresponding to the given encoding chars */
    public static int stringToFlag(char first, char second) {
        int high = 0;
        int low = 0;
        if (first > '9') {
            high = first-'a'+10;
        } else {
            high = first-'0';
        }
        if (second > '9') {
            low = second-'a'+10;
        } else {
            low = second-'0';
        }
        return (high << 4) + low;
    }
    
    public boolean isDocumented() {
        return (flags & DOCUMENTED) != 0;
    }
    
    public boolean isPublic() {
        return (flags & PRIVATE & PROTECTED) == 0;
    }

    public boolean isPrivate() {
// XXX hmmm        not symmetric, see what the old semantics was for why I needed both?
        return ((flags & PRIVATE) != 0) || ((flags & PROTECTED) != 0);
    }
    
    public boolean isProtected() {
        return (flags & PROTECTED) != 0;
    }
    
    public boolean isTopLevel() {
        return (flags & TOPLEVEL) != 0;
    }

    public boolean isStatic() {
        return (flags & STATIC) != 0;
    }
    
    public boolean isNoDoc() {
        return (flags & NODOC) != 0;
    }
    
    public static String decodeFlags(int flags) {
        StringBuilder sb = new StringBuilder();
        if ((flags & DOCUMENTED) != 0) {
            sb.append("|DOCUMENTED");
        }
        if ((flags & PRIVATE) != 0) {
            sb.append("|PRIVATE");
        }
        if ((flags & PROTECTED) != 0) {
            sb.append("|PROTECTED");
        }
        if ((flags & TOPLEVEL) != 0) {
            sb.append("|TOPLEVEL");
        }
        if ((flags & STATIC) != 0) {
            sb.append("|STATIC");
        }
        if ((flags & NODOC) != 0) {
            sb.append("|NODOC");
        }
        
        return sb.toString();
    }

    /**
     * Returns whether this method pertains to the Module class, which is handle
     * in a kind of special manner in Ruby.
     *
     * @return whether the element is declared in the Module
     */
    public boolean doesBelongToModule() {
        return "Module".equals(getFqn()); // NOI18N
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

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
    
    protected final FileObject file;
    protected final String clz;
    protected final String fqn;
    protected final String require;
    protected final String attributes;
    protected final int flags;
    protected int docLength = -1;

    private Set<Modifier> modifiers;
    private final RubyIndex index;
    private Document document;
    private final FileObject context;
    protected RubyType type;

    protected IndexedElement(RubyIndex index, FileObject file, String fqn,
            String clz, String require, String attributes,
            int flags, FileObject context, RubyType type) {
        this.index = index;
        this.file = file;
        this.fqn = fqn;
        this.require = require;
        this.attributes = attributes;
        // XXX Why do methods need to know their clz (since they already have fqn)
        this.clz = clz;
        this.flags = flags;
        this.context = context;
        this.type = type;
    }

    protected IndexedElement(RubyIndex index, IndexResult result, String fqn,
            String clz, String require, String attributes,
            int flags, FileObject context, RubyType type) {
        this(index, result.getFile(), fqn, clz, require, attributes, flags, context, type);
    }

    protected IndexedElement(RubyIndex index, IndexResult result, String fqn,
            String clz, String require, String attributes,
            int flags, FileObject context) {
        this(index, result, fqn, clz, require, attributes, flags, context, null);
    }

    public abstract String getSignature();

    public final String getRequire() {
        return require;
    }

    public final String getFqn() {
        return fqn;
    }

    @Override
    public RubyType getType() {
        if (type == null && attributes != null) {
            int lastSemiColon = attributes.lastIndexOf(';');
            if (lastSemiColon != -1) {
                int last2SemiColon = attributes.lastIndexOf(';', lastSemiColon - 1);
                if (lastSemiColon != -1) {
                    String typesS = attributes.substring(last2SemiColon + 1, lastSemiColon);
                    type = parseTypes(typesS);
                }
            }
        }
        if (type == null) {
            type = RubyType.unknown();
        }
        return type;
    }

    private RubyType parseTypes(final String types) {
        if (types.length() == 0) {
            return RubyType.unknown();
        }
        if (!types.contains("|")) { // just one type
            return RubyType.create(types);
        }
        return new RubyType(types.split("\\|")); // NOI18N
    }

    @Override
    public String toString() {
        return getSignature();
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

    public Document getDocument() {
        if (document == null) {
            FileObject fo = getFileObject();
            if (fo == null) {
                return null;
            }
            document = GsfUtilities.getDocument(fo, true);
        }
        return document;
    }

//    public ParserFile getFile() {
//        boolean platform = false; // XXX FIND OUT WHAT IT IS!
//
//        return new DefaultParserFile(getFileObject(), null, platform);
//    }
    
    @Override
    public FileObject getFileObject() {
        return file;
    }

    public String getFileUrl() {
        if (file == null) {
            // there's no file for e.g. for dynamic methods
            return null;
        }
        File f = FileUtil.toFile(file);
        try {
            return f == null ? null : f.toURI().toURL().toExternalForm();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
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
        if (first == ';'){
            return 0;
        }
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
    
    // For testsuite
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

    // For testsuite
    public static int stringToFlags(String string) {
        int flags = 0;
        if (string.indexOf("|DOCUMENTED") != -1) {
            flags += DOCUMENTED;
        }
        if (string.indexOf("|PRIVATE") != -1) {
            flags += PRIVATE;
        }
        if (string.indexOf("|PROTECTED") != -1) {
            flags += PROTECTED;
        }
        if (string.indexOf("|TOPLEVEL") != -1) {
            flags += TOPLEVEL;
        }
        if (string.indexOf("|STATIC") != -1) {
            flags += STATIC;
        }
        if (string.indexOf("|NODOC") != -1) {
            flags += NODOC;
        }

        return flags;
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

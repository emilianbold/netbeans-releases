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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.ruby.RubyIndex;
import org.openide.filesystems.FileObject;


/**
 * A class describing a Ruby method that is in "textual form" (signature, filename, etc.)
 * obtained from the code index.
 *
 * @todo Consider computing a lot of the extra info (requires, filenames, etc.)
 *  lazily - by just stashing the index map into the RubyMethod. Hopefully Lucene
 *  won't mind if we hang on to this information...
 * @todo Make an IndexedAttribute subclass which just has a different kind!
 *
 * @author Tor Norbye
 */
public final class IndexedMethod extends IndexedElement implements MethodElement {
    
    /** This method takes a (possibly optional, see BLOCK_OPTIONAL) block */
    public static final int BLOCK = 1 << 6;
    /** This method takes an optional block */
    public static final int BLOCK_OPTIONAL = 1 << 7;
    /** Deprecated? */
    /** Parenthesis or space delimited? */

    public static enum MethodType { METHOD, ATTRIBUTE, DBCOLUMN };
    
    protected final String signature;
    private String[] args;
    private String name;
    private List<String> parameters;
    private boolean smart;
    private boolean inherited; 
    private MethodType methodType = MethodType.METHOD;

    private IndexedMethod(String signature, RubyIndex index, String fileUrl, String fqn,
            String clz, String require, String attributes, int flags, FileObject context) {
        super(index, fileUrl, fqn, clz, require, attributes, flags, context);
        this.signature = signature;
    }

    public static IndexedMethod create(RubyIndex index, String signature, String fqn, String clz,
            String fileUrl, String require, String attributes, int flags, FileObject context) {
        return new IndexedMethod(signature, index, fileUrl, fqn, clz, require, attributes, flags, context);
    }
    
    public MethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(MethodType methodType) {
        this.methodType = methodType;
    }

    @Override
    public String toString() {
        return getSignature();
    }

    public String getName() {
        if (name == null) {
            int parenIndex = signature.indexOf('(');

            if (parenIndex == -1) {
                name = signature;
            } else {
                name = signature.substring(0, parenIndex);
            }
        }

        return name;
    }

    @Override
    public String getSignature() {
        return fqn + "#" + signature;
    }

    public String[] getArgs() {
        if (args == null) {
            // Parse signature
            int parenIndex = signature.indexOf('(');

            if (parenIndex == -1) {
                return new String[0];
            }

            String argsPortion = signature.substring(parenIndex + 1, signature.length() - 1);
            args = argsPortion.split(","); // NOI18N
        }

        return args;
    }

    public List<String> getParameters() {
        if (parameters == null) {
            String[] argArray = getArgs();

            if ((argArray != null) && (argArray.length > 0)) {
                parameters = new ArrayList<String>(argArray.length);

                for (String arg : argArray) {
                    parameters.add(arg);
                }
            } else {
                parameters = Collections.emptyList();
            }
        }

        return parameters;
    }

    public boolean isDeprecated() {
        return false;
    }

    public ElementKind getKind() {
        if (((name == null) && signature.startsWith("initialize(")) || // NOI18N
                ((name != null) && name.equals("initialize"))) { // NOI18N

            return ElementKind.CONSTRUCTOR;
        } else {
            return ElementKind.METHOD;
        }
    }
    
    public boolean isSmart() {
        return smart;
    }
    
    public void setSmart(boolean smart) {
        this.smart = smart;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedMethod other = (IndexedMethod) obj;
        if (this.signature != other.signature && (this.signature == null || !this.signature.equals(other.signature))) {
            return false;
        }
        if (this.fqn != other.fqn && (this.fqn == null || !this.fqn.equals(other.fqn))) {
            return false;
        }
        if (this.flags != other.flags) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.signature != null ? this.signature.hashCode() : 0);
        hash = 53 * hash + (this.fqn != null ? this.fqn.hashCode() : 0);
        hash = 53 * hash + flags;
        return hash;
    }
    
    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }
    
    public boolean hasBlock() {
        return (flags & BLOCK) != 0;
    }

    public boolean isBlockOptional() {
        return (flags & BLOCK_OPTIONAL) != 0;
    }
    
    public static String decodeFlags(int flags) {
        StringBuilder sb = new StringBuilder();
        sb.append(IndexedElement.decodeFlags(flags));

        if ((flags & BLOCK) != 0) {
            sb.append("|BLOCK");
        }
        if ((flags & BLOCK_OPTIONAL) != 0) {
            sb.append("|BLOCK_OPTIONAL");
        }
        if (sb.length() > 0) {
            sb.append("|");
        }
        
        return sb.toString();
    }

    // For testsuite
    public static int stringToFlags(String string) {
        int flags = IndexedElement.stringToFlags(string);

        int blockIndex = string.indexOf("|BLOCK_OPTIONAL");
        if (blockIndex != -1) {
            flags += BLOCK_OPTIONAL;
            if (string.indexOf("|BLOCK") != blockIndex || string.lastIndexOf("|BLOCK") != blockIndex) {
                flags += BLOCK;
            }
        } else if (string.indexOf("|BLOCK") != -1) {
            flags += BLOCK;
        }

        return flags;
    }
    
    public String getEncodedAttributes() {
        return attributes;
    }

    @Override
    public Set<? extends String> getTypes() {
        if (types == null) {
            int lastSemiColon = attributes.lastIndexOf(';');
            if (lastSemiColon != -1) {
                int last2SemiColon = attributes.lastIndexOf(';', lastSemiColon -1);
                if (lastSemiColon != -1) {
                    String typesS = attributes.substring(last2SemiColon + 1, lastSemiColon);
                    types = parseTypes(typesS);
                }
            }
        }
        if (types == null) {
            types = Collections.emptySet();
        }
        return types;
    }

    private Set<? extends String> parseTypes(final String typesS) {
        if (typesS.length() == 0) {
            return Collections.emptySet();
        }
        if (!typesS.contains("|")) { // just one type
            return Collections.singleton(typesS);
        }
        return new HashSet<String>(Arrays.asList(typesS.split("\\|"))); // NOI18N

    }
}

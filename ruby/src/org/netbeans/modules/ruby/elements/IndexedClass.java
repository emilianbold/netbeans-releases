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

import java.util.Set;

import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyType;
import org.openide.filesystems.FileObject;

/**
 * A class describing a Ruby class that is rin "textual form" (signature,
 * filename, etc.) obtained from the code index.
 *
 * @author Tor Norbye
 */
public final class IndexedClass extends IndexedElement implements ClassElement {
    
    /** This class is a module rather than a proper class */
    public static final int MODULE = 1 << 6;

    private String in;

    protected IndexedClass(RubyIndex index, IndexResult result, String fqn,
        String clz, String require, String attributes, int flags, FileObject context) {
        super(index, result, fqn, clz, require, attributes, flags, context);
    }

    public static IndexedClass create(RubyIndex index, String clz, String fqn, IndexResult result,
        String require, String attributes, int flags, FileObject context) {
        IndexedClass c =
            new IndexedClass(index, result, fqn, clz, require, attributes, flags, context);

        return c;
    }

    @Override
    public String getIn() {
        if (in == null) {
            if (fqn.endsWith("::" + clz)) {
                in = fqn.substring(0, fqn.length() - (clz.length() + 2));
            } else if ((require != null) && (require.length() > 0)) {
                // Show the require path instead
                in = require;
            }
        }

        return in;
    }

    @Override
    public RubyType getType() {
        // type is not relevant here; the type of the class/module is the class itself
        return RubyType.unknown();
    }
    // XXX Is this necessary?
    public String getSignature() {
        return fqn;
    }

    public String getName() {
        return getClz();
    }

    public ElementKind getKind() {
        return (flags & MODULE) != 0 ? ElementKind.MODULE : ElementKind.CLASS;
    }

    public Set<String> getIncludes() {
        return null;
    }
    
//    @Override
//    public boolean equals(Object o) {
//        //return ((IndexedClass)o).fqn.equals(fqn);
//        return super.equals(o);
//    }
//
//    @Override
//    public int hashCode() {
//        //return fqn.hashCode();
//        return super.hashCode();
//    }
    
    /** Return the length of the documentation for this class, in characters */
    @Override
    public int getDocumentationLength() {
        if (docLength == -1) {
            if (attributes != null) {
                int docIndex = attributes.indexOf(';');

                if (docIndex != -1) {
                    int end = attributes.indexOf(';', docIndex+1);
                    if (end == -1) {
                        end = attributes.length();
                    }                        
                    docLength = Integer.parseInt(attributes.substring(docIndex + 1, end));
                    return docLength;
                } else {
                    // Unknown length - just use 1 to indicate positive document length
                    docLength = 1;
                }
            }
            
            docLength = super.getDocumentationLength();
        }

        return docLength;
    }
    
    // For testsuite
    public static String decodeFlags(int flags) {
        StringBuilder sb = new StringBuilder();
        sb.append(IndexedElement.decodeFlags(flags));

        if ((flags & MODULE) != 0) {
            sb.append("|MODULE");
        }
        if (sb.length() > 0) {
            sb.append("|");
        }
        
        return sb.toString();
    }

    // For testsuite
    public static int stringToFlags(String string) {
        int flags = IndexedElement.stringToFlags(string);

        if (string.indexOf("|MODULE") != -1) {
            flags += MODULE;
        }

        return flags;
    }
}

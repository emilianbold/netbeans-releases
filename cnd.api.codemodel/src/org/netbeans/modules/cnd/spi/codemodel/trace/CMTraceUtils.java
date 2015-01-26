/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.spi.codemodel.trace;

import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMToken;
import org.netbeans.modules.cnd.api.codemodel.CMUnifiedSymbolResolution;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclaration;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclarationContext;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntity;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntityReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMInclude;
import org.netbeans.modules.cnd.api.codemodel.visit.CMReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitLocation;
import org.netbeans.modules.cnd.spi.codemodel.CMCursorImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMSourceLocationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMSourceRangeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTokenImplementation;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIIndexAccessor;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMDeclarationContextImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMDeclarationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMEntityImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMIncludeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMReferenceImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMVisitLocationImplementation;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 * A static utility class
 * Tracing
 *
 * @author Vladimir Kvashin
 */
public class CMTraceUtils {
    public static final boolean SKIP_FUNCTION_BODY = getBoolean("cm.skip.body", false); // NOI18N
    public static final boolean INDEX_SOURCE_FILE = getBoolean("cm.index.sourcefile", true); // NOI18N
    public static final boolean INDEX_ON_PARSE = getBoolean("cnd.codemodel.indexedparse", true); // NOI18N

    private CMTraceUtils() {
    }

    public static boolean getBoolean(String name, boolean defaultValue) {
        String text = System.getProperty(name);
        if (text != null) {
            defaultValue = Boolean.getBoolean(text);
        }
        return defaultValue;
    }

    public static int getInt(String name, int result) {
        String text = System.getProperty(name);
        if (text != null) {
            try {
                result = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                // default value
            }
        }
        return result;
    }
    
    /**
     * String representation for testing/debugging purposes. NB: the method is
     * used in unit tests, so after changing it don't forget to update unit
     * tests golden files
     */
    public static String toString(CMDeclarationContext dc) {
        return (dc == null) ? "null"
                : toString(APIIndexAccessor.get().getDeclarationContextImpl(dc));
    }
    
    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMDeclarationContextImplementation dc) {
        return (dc == null) ? "null"
                : String.format("%s %s", dc, dc);
    }
    
    /**
     * String representation for testing/debugging purposes. NB: the method is
     * used in unit tests, so after changing it don't forget to update unit
     * tests golden files
     */
    public static String toString(CMReference ref) {
        return (ref == null) ? "null"
                : toString(APIIndexAccessor.get().getReferenceImpl(ref));
    }
    
    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMReferenceImplementation ref) {
        return (ref == null) ? "null"
                : String.format("%s %s", ref.getKind(), toString(ref.getRange()));
    }
    
    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMDiagnostic diag) {
        return (diag == null) ? "null"
                : toString(APIAccessor.get().getDiagnosticImpl(diag));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMDiagnosticImplementation diag) {
        return (diag == null) ? "null"
                : String.format("%s %s", diag.getSeverity(), diag.getFormattedText());
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMInclude inc) {
        return (inc == null) ? "null"
                : String.format("%s angled=%b from %s", inc.getFileName(), inc.isAngled(), toString(inc.getHashLocation()));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMIncludeImplementation inc) {
        return (inc == null) ? "null"
                : String.format("%s angled=%b", inc.getFileName(), inc.isAngled());
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMDeclaration decl) {
        return (decl == null) ? "null"
                : String.format("Decl of %s loc=%s",
                toString(decl.getEntity()),
                toString(decl.getLocation()));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMDeclarationImplementation decl) {
        return (decl == null) ? "null"
                : String.format("Decl of %s loc=%s", 
                toString(decl.getEntity()),
                toString(decl.getLocation()));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMSourceLocation location) {
        return (location == null) ? "null" : toString(APIAccessor.get().getSourceLocationImpl(location));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMSourceLocationImplementation location) {
        return (location == null) ? "null"
                : String.format("%d:%d(%d)", location.getLine(), location.getColumn(), location.getOffset());
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMVisitLocation location) {
        return (location == null) ? "null"
                : String.format("%s[%d:%d(%d)]", toString(location.getFile()), location.getLine(), location.getColumn(), location.getOffset());
    }

    public static String toString(CMFile file) {
        if (file == null) {
            return "null";
        }
        CharSequence name = file.getName();
        if (name == null || name.length() == 0) {
            return "<no_name>";
        }
        String dir = CndPathUtilities.getDirName(file.getFilePath().toString());
        if (dir == null || dir.length() == 0) {
            return name.toString();
        }
        String dirName = CndPathUtilities.getBaseName(dir);
        if (dirName == null || dirName.length() == 0) {
            return name.toString();
        }
        return dirName + "/" + name;
    }
    
    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMVisitLocationImplementation location) {
        return (location == null) ? "null"
                : String.format("%d:%d [%d]", location.getLine(), location.getColumn(), location.getOffset());
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMEntityImplementation entity) {
        if (entity == null) {
            return "null"; //NOI18N
        }
        CMUnifiedSymbolResolution usr = entity.getUSR();
        return String.format("%s name=%s usr=%s", entity.getKind(), entity.getName(),
                ((usr == null) ? "null" : usr.getText()));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMEntity entity) {
        if (entity == null) {
            return "null"; //NOI18N
        }
        CMUnifiedSymbolResolution usr = entity.getUSR();
        return String.format("%s name=%s usr=%s", entity.getKind(), entity.getName(), 
                ((usr == null) ? "null" : usr.getText()));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMEntityReference ref) {
        return (ref == null) ? "null"
                : String.format("kind=%s loc=%s refEntity=%s", ref.getKind(), toString(ref.getLocation()), toString(ref.getReferencedEntity()));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMCursor cursor) {
        return (cursor == null) ? "null" : toString(APIAccessor.get().getCursorImpl(cursor));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMCursorImplementation cursor) {
        return toString(cursor, 1);
    }
    
    private static String toString(CMCursorImplementation cursor, int depth) {
        if (cursor == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cursor.getKind()).append(' ');
        sb.append("displayName=\"").append(cursor.getDisplayName()).append("\" ");
        sb.append("kind=\"").append(cursor.getKind()).append("\" ");
        sb.append("USR=\"").append(cursor.getUSR()).append("\" ");
        sb.append("spelling name=\"").append(cursor.getSpellingName()).append("\" ");
        sb.append("reference USR=\"").append(cursor.getReferencedEntityCursor().getUSR()).append("\" ");
        sb.append("parent USR=\"").append(cursor.getSemanticParent().getUSR()).append("\" ");
        sb.append("parent kind=\"").append(cursor.getSemanticParent().getKind()).append("\" ");
        sb.append("lexical parent USR=\"").append(cursor.getLexicalParent().getUSR()).append("\" ");
        sb.append("lexical parent kind=\"").append(cursor.getLexicalParent().getKind()).append("\" ");
        sb.append("extent=").append(toString(cursor.getExtent())).append(' ');
        if (depth > 0) {
            sb.append("\n\tcanonical {\n\t\"").append(toString(cursor.getCanonical(), 0)).append("\n\t}\" ");
        } else {
            sb.append("\n\tcanonical USR=\"").append(cursor.getCanonical().getUSR()).append("\" ");
        }
        return sb.toString();
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMSourceRange range) {
        return (range == null) ? "null" : toString(APIAccessor.get().getSourceRangeImpl(range));
    }

    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMSourceRangeImplementation range) {
        if (range == null) {
            return "null";
        }
        CMSourceLocationImplementation start = range.getStart();
        CMSourceLocationImplementation end = range.getEnd();
        CharSequence fileName = "null";
        if (start != null) {
            CMFileImplementation cmFile = start.getFile();
            if (cmFile != null) {
                fileName = cmFile.getName();
            }
        }
        return String.format("[%s %s-%s]", fileName, toString(start), toString(end));
    }
    
    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMToken token) {
        return (token == null) ? "null" : toString(APIAccessor.get().getTokenImpl(token));
    }    
    /**
     * String representation for testing/debugging purposes.
     * NB: the method is used in unit tests,
     * so after changing it don't forget to update unit tests golden files
     */
    public static String toString(CMTokenImplementation token) {
        if (token == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(token.getSpelling()).append(" <"); // NOI18N
        sb.append(token.getKind()).append("> "); // NOI18N
        sb.append(String.format("[%s]", toString(token.getLocation()))); // NOI18N
        return sb.toString();
    }
}

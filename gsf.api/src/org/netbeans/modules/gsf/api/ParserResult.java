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

package org.netbeans.modules.gsf.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;


/**
 * Result from a Parser. Typically subclassed by each parser implementation
 * to stash additional information it might need, such as an AST root node,
 * for use by related clients of the parse tree such as the code completion
 * or declaration finders.
 * 
 * @todo Stash the errors on the parser result too? Sounds reasonable!
 *
 * @author Tor Norbye
 */
public abstract class ParserResult {
    @NonNull protected final ParserFile file;
    @CheckForNull private List<Error> errors;
    @NonNull private Parser parser;
    @CheckForNull protected TranslatedSource translatedSource;
    @NonNull private String mimeType;
    @NonNull private CompilationInfo info;
    @NonNull protected UpdateState updateState = UpdateState.NOT_SUPPORTED;
    /**
     * Edit version corresponding to the version of the file the parser result was run against
     */
    private int editVersion = -1;
    /**
     * Flag, whether the parser result is valid. If is not valid, then index, codefolding
     * and semantic coloring will not be refreshed.
     */
    private final boolean valid;

    /** Creates a new instance of ParserResult */
    public ParserResult(@NonNull Parser parser, @NonNull ParserFile file, @NonNull String mimeType) {
        this(parser, file, mimeType, true);
    }

    public ParserResult(@NonNull Parser parser, @NonNull ParserFile file, @NonNull String mimeType, boolean isValid) {
        this.parser = parser;
        this.file = file;
        this.mimeType = mimeType;
        this.valid = isValid;
    }

    @NonNull
    public String getMimeType() {
        return mimeType;
    }

    @NonNull
    public Parser getParser() {
        return parser;
    }

    // TODO - don't expose this
    public void setTranslatedSource(@NonNull TranslatedSource translatedSource) {
        this.translatedSource = translatedSource;
    }

    @CheckForNull
    public TranslatedSource getTranslatedSource() {
        return translatedSource;
    }

    /**
     * Return true iff this is a valid parser result.
     * A parser result is valid if it did not contain fatal errors.
     * 
     * @return
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Returns the errors in the file represented by the {@link CompilationInfo}
     * for one particular mime type.
     *
     * @todo Rename to getErrors()
     * @return an list of {@link Error}
     */
    @NonNull
    public List<Error> getDiagnostics() {
        if (errors == null) {
            return Collections.emptyList();
        }
        ArrayList<Error> localErrors =
            new ArrayList<Error>(errors.size());

        for (Error m : errors) {
            assert file.getFileObject() == m.getFile();
            //if (this.fo == m.getFile()) {
                localErrors.add(m);
            //}
        }

        return localErrors;
    }
    
    public boolean hasErrors() {
        if (errors == null) {
            return false;
        }
        for (Error m : errors) {
            if (/*this.fo == m.getFile() &&*/ m.getSeverity() == Severity.ERROR) {
                return true;
            }
        }
        
        return false;
    }

    public void addError(@NonNull Error message) {
        if (errors == null) {
            errors = new ArrayList<Error>();
        }
        errors.add(message);
    }

    /** AST tree - optional; for debugging only */
    @CheckForNull
    public abstract AstTreeNode getAst();

    // XXX Make top level, and document debugging purpose.
    // And remove all references to it in the client code.
    public interface AstTreeNode extends TreeNode {
        public Object getAstNode();
        public int getStartOffset();
        public int getEndOffset();
    }
    
    @NonNull
    public ParserFile getFile() {
        return file;
    }

    @NonNull
    public CompilationInfo getInfo() {
        return info;
    }

    public void setInfo(@NonNull CompilationInfo info) {
        this.info = info;
    }

    /**
     * Get the edit version for this parser result. Used along
     * with {@link EditHistory#getCombinedEdits(int,EditHistory)} to
     * produce a edit history delta between two parser results.
     * @return the edit version this parser result was seen with
     */
    public int getEditVersion() {
        return editVersion;
    }

    /**
     * Set the edit version. This is normally called by the infrastructure.
     * @param editVersion The editVersion this parser result is associated with.
     */
    public void setEditVersion(int editVersion) {
        this.editVersion = editVersion;
    }

    @NonNull
    public UpdateState getUpdateState() {
        return updateState;
    }

    public void setUpdateState(@NonNull UpdateState updateState) {
        this.updateState = updateState;
    }

    /**
     * This enum constant represents various states of incremental updating support.
     * Services can either indicate that they don't support incremental updating, or
     * report various stages of incremental support.
     *
     * @author Tor Norbye
     */
    public enum UpdateState {
        /**
         * This parser does not support incremental parsing
         */
        NOT_SUPPORTED,

        /**
         * Incremental update supported but failed for some reason or other.
         * A separate full parse request should be submitted.
         */
        FAILED,

        /**
         * Incremental update performed, and there was NO change from the previous result.
         * This means that the abstract syntax tree is identical (including source
         * offsets) to the previous one.
         */
        NO_CHANGE,

        /**
         * Incremental update performed, and there was no semantic change from the previous
         * result. This means that the shape and meaning of the abstract syntax tree is identical,
         * but source offsets may have changed. This would be the case if the user edited
         * a comment or source whitespace for example.
         */
        NO_SEMANTIC_CHANGE,

        /**
         * Incremental update supported, and completed.
         */
        UPDATED;

        public boolean isUnchanged() {
            return this == NO_CHANGE || this == NO_SEMANTIC_CHANGE;
        }
    };
}

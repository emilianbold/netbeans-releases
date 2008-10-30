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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.api;

import org.netbeans.modules.csl.api.annotations.NonNull;

/**
 * An embedding model constructs a TranslatedSource which
 * provides translated source code and offsets.
 * 
 * @author Tor Norbye
 */
public abstract class TranslatedSource {
    @NonNull public abstract String getSource();

    public abstract int getAstOffset(int lexicalOffset);
    public abstract int getLexicalOffset(int astOffset);
    /** The start of the translation section in the source document. Usually 0. */
    public abstract int getSourceStartOffset();
    /** The end of the translation section in the source document. Usually doc.getLength() */
    public abstract int getSourceEndOffset();

    /**
     * Create a new TranslatedSource
     * @param embeddingModel The embedding model responsible for creating this translated source
     */
    protected TranslatedSource(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * Return the embedding model associated with this translated source
     * @return the embedding model which created this translated source
     */
    @NonNull
    public EmbeddingModel getModel() {
        return embeddingModel;
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

    private int editVersion = -1;
    private EmbeddingModel embeddingModel;
}

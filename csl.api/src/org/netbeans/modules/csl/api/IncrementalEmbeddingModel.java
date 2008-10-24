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
package org.netbeans.modules.csl.api;

import java.util.Collection;
import org.netbeans.modules.csl.api.annotations.NonNull;

/**
 * <p>Implementations of this interface are EmbeddingModels that support
 * incremental updates. When it does, then the GSF infrastructure will keep its
 * most recent TranslatedSource collection and will pass it along with
 * an editing history object to perform incremental updates.</p>
 * <p>For more information about incremental parsing, see the
 * <a href="../../../../../incremental-parsing.html">incremental updating</a>
 * document.</p>
 *
 * @author Tor Norbye
 */
public interface IncrementalEmbeddingModel extends EmbeddingModel {

    public enum UpdateState {

        /**
         * Updating the virtual source failed for some reason or other.
         * The infrastructure should generate a new virtual source instead.
         */
        FAILED,

        /**
         * The update succeeded, and the virtual source was not affected by
         * the change. This means that the parse tree for the virtual source does
         * not have to be regenerated (or the results analyzed again).
         * (The offset mapping for the translated source source-to-generated
         * conversion functions have been updated.)
         */
        COMPLETED,

        /**
         * The update succeeded, and the virtual source code generated was affected
         * by the edits. The virtual source should be regenerated.
         * (The offset mapping for the translated source source-to-generated
         * conversion functions have been updated.)
         */
        UPDATED
    };

    /**
     * Update the collection of {@link TranslatedSource} objects given a series of edits.
     * The objects in the collection are allowed to change.
     */
    @NonNull
    UpdateState update(@NonNull EditHistory history, @NonNull Collection<? extends TranslatedSource> previousTranslation);
}

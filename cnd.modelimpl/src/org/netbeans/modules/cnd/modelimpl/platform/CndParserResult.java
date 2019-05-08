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

package org.netbeans.modules.cnd.modelimpl.platform;

import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.util.WeakListeners;

/**
 *
 */
public class CndParserResult  extends Result implements TokenHierarchyListener {

    private final CsmFile file;
    private final long fileVersion;
    private final long docVersion;
    private boolean invalid = false;

    /*package*/CndParserResult(CsmFile file, Snapshot snapshot, long fileVersion, long docVersion) {
        super(snapshot);
        this.file = file;
        this.fileVersion = fileVersion;
        this.docVersion = docVersion;
        // when snapshot is document based we'd like to be sensitive to
        // rebuild of TokenHierarchy, i.e. when language flavor of document
        // is changed and document is relexed;
        // or DocumentLanguageFlavorProvider.addProperty took long time
        // and Parsing API was invoked on the document with default language flavor
        final Document doc = snapshot.getSource().getDocument(false);
        if (doc != null) {
            TokenHierarchy<Document> hierarchy = TokenHierarchy.get(doc);
            if (hierarchy != null) {
                hierarchy.addTokenHierarchyListener(WeakListeners.create(TokenHierarchyListener.class, CndParserResult.this, hierarchy));
            }
        }
    }

    @Override
    protected void invalidate() {
    }

    public CsmFile getCsmFile() {
        return file;
    }
    
    /*package*/ long getFileVersion() {
        return fileVersion;
    }
    
    /*package*/ long getDocumentVersion() {
        return docVersion;
    }

    /*package*/ boolean isInvalid() {
        return invalid;
    }

    @Override
    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        if (evt.type() == TokenHierarchyEventType.REBUILD) {
            this.invalid = true;
        }
    }
}

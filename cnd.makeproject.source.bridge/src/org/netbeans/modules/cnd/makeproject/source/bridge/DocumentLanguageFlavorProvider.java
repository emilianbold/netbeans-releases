/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.source.bridge;

import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.source.spi.CndSourcePropertiesProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * bridge which affects editor bahavior based on options from makeproject.
 * @author Vladimir Voskresensky
 */
@ServiceProvider(path=CndSourcePropertiesProvider.REGISTRATION_PATH, service=CndSourcePropertiesProvider.class, position=1000)
public final class DocumentLanguageFlavorProvider implements CndSourcePropertiesProvider {

    @Override
    public void addProperty(DataObject dob, StyledDocument doc) {
        // check if it should have C++11 flavor
        Language<?> language = (Language<?>) doc.getProperty(Language.class);
        if (language != CppTokenId.languageCpp()) {
            return;
        }
        if (true) { // workaround till not fixed IZ#210309
            NativeFileItemSet nfis = dob.getLookup().lookup(NativeFileItemSet.class);
            for (NativeFileItem nativeFileItem : nfis.getItems()) {
                if (nativeFileItem.getLanguageFlavor() == NativeFileItem.LanguageFlavor.CPP11) {
                    InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
                    Filter<?> filter = CndLexerUtilities.getGccCpp11Filter();
                    lexerAttrs.setValue(language, CndLexerUtilities.LEXER_FILTER, filter, true);  // NOI18N
                    return;
                }
            }
            return;
        }
        FileObject primaryFile = dob.getPrimaryFile();
        if (primaryFile == null) {
            return;
        }
        Project owner = FileOwnerQuery.getOwner(primaryFile);
        if (owner == null) {
            return;
        }
        NativeProject np = owner.getLookup().lookup(NativeProject.class);
        if (np == null) {
            return;
        }
        NativeFileItem nfi = np.findFileItem(primaryFile);
        if (nfi == null) {
            return;
        }
        if (nfi.getLanguageFlavor() == NativeFileItem.LanguageFlavor.CPP11) {
            InputAttributes lexerAttrs = (InputAttributes) doc.getProperty(InputAttributes.class);
            Filter<?> filter = CndLexerUtilities.getGccCpp11Filter();
            lexerAttrs.setValue(language, CndLexerUtilities.LEXER_FILTER, filter, true);  // NOI18N
        }
    }
}

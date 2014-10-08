/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.knockout.javascript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.netbeans.modules.javascript2.knockout.index.KnockoutCustomElement;
import org.netbeans.modules.javascript2.knockout.index.KnockoutIndex;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Roman Svitanic
 */
@CompletionProvider.Registration(priority = 7)
public class KnockoutJsCodeCompletion implements CompletionProvider {

    private static final String MIMETYPE = "text/html/text/javascript"; //NOI18N
    private static final String PROP_NAME = "name"; //NOI18N
    private static final String PROP_PARAMS = "params"; //NOI18N

    @Override
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        if (!MIMETYPE.equals(ccContext.getParserResult().getSnapshot().getMimePath().getPath())) {
            // we are interested only in html documets
            return Collections.emptyList();
        }
        Document document = ccContext.getParserResult().getSnapshot().getSource().getDocument(true);
        int dOffset = ccContext.getCaretOffset();  // document offset
        ((AbstractDocument) document).readLock();
        try {
            List<CompletionProposal> result = new ArrayList<>();
            KnockoutContext koContext = KnockoutContext.findContext(document, dOffset);
            switch (koContext) {
                case COMPONENT_EMPTY:
                case COMPONENT_CONF_NAME:
                    result.addAll(findComponentNames(ccContext));
                    break;
                case COMPONENT_CONF_EMPTY:
                    if (prefix.isEmpty() || PROP_NAME.startsWith(prefix)) {
                        result.add(new KnockoutCodeCompletionItem.KOComponentOptionItem(
                                new KnockoutJsElement(PROP_NAME, ElementKind.PROPERTY), ccContext));
                    }
                    if (prefix.isEmpty() || PROP_PARAMS.startsWith(prefix)) {
                        result.add(new KnockoutCodeCompletionItem.KOComponentOptionConfigItem(
                                new KnockoutJsElement(PROP_PARAMS, ElementKind.PROPERTY), ccContext));
                    }
                    break;
                case COMPONENT_CONF_PARAMS:
                    if (prefix.isEmpty() || PROP_PARAMS.startsWith(prefix)) {
                        result.add(new KnockoutCodeCompletionItem.KOComponentOptionConfigItem(
                                new KnockoutJsElement(PROP_PARAMS, ElementKind.PROPERTY), ccContext));
                    }
                    break;
            }
            return result;
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
    }

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        return null;
    }

    private Collection<? extends CompletionProposal> findComponentNames(CodeCompletionContext ccContext) {
        FileObject fo = ccContext.getParserResult().getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return Collections.emptyList();
        }
        Collection<CompletionProposal> result = new ArrayList<>();
        KnockoutIndex knockoutIndex = null;
        try {
            knockoutIndex = KnockoutIndex.get(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (knockoutIndex != null) {
            Collection<KnockoutCustomElement> customElements = knockoutIndex.getCustomElements(ccContext.getPrefix(), false);
            List<String> componentNames = new ArrayList<>(); //list of names of custom KO components, to prevent duplicates in CC
            if (customElements != null) {
                for (KnockoutCustomElement kce : customElements) {
                    String name = kce.getName();
                    ElementHandle element = new KnockoutJsElement(name, ElementKind.CLASS);
                    if ((ccContext.getPrefix().isEmpty() || name.startsWith(ccContext.getPrefix()))
                            && !componentNames.contains(name)) {
                        // we don't have this component yet, add it now
                        result.add(new KnockoutCodeCompletionItem.KOComponentItem(element, ccContext));
                        componentNames.add(name);
                    }
                }
            }
        }
        return result;
    }

}

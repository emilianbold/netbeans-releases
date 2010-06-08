/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor.completion.entries;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem.BuiltInFunction;
import org.netbeans.modules.php.smarty.editor.completion.TplCompletionItem.VariableModifiers;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Fousek
 */
public class TplCodeCompletionData {

    private final static Collection<TplCompletionItem> completionItems = new ArrayList<TplCompletionItem>();
    private final static String[] completionTypes = {"built-in-functions", "variable-modifiers"};

    static {
        loadCCData();
    }

    public static Collection<TplCompletionItem> getCCData() {
        return completionItems;
    }

    private static void loadCCData() {
        for (String completionType : completionTypes) {
            Collection<EntryMetadata> ccList = parseCCData(completionType);
            if (completionType.equals("built-in-functions")) {
                for (EntryMetadata entryMetadata : ccList) {
                    completionItems.add(new BuiltInFunction(entryMetadata.getKeyword(), 0, entryMetadata.getHelp(), entryMetadata.getHelpUrl()));
                }
            }
            else if (completionType.equals("variable-modifiers")) {
                for (EntryMetadata entryMetadata : ccList) {
                    completionItems.add(new VariableModifiers(entryMetadata.getKeyword(), 0, entryMetadata.getHelp(), entryMetadata.getHelpUrl()));
                }
            }
        }
    }

    private static Collection<EntryMetadata> parseCCData(String filePath) {
        Collection<EntryMetadata> ccList = new ArrayList<EntryMetadata>();
        InputStream inputStream = TplCodeCompletionData.class.getResourceAsStream("defs/" + filePath + ".xml"); //NOI18N

        try {
            Collection<EntryMetadata> ccData = CodeCompletionEntries.readAllCodeCompletionEntriesFromXML(inputStream);
            ccList.addAll(ccData);

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return ccList;
    }
}

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
package org.netbeans.modules.languages.php;

import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.php.model.FunctionDefinition;
import org.netbeans.modules.php.model.Statement;
import org.openide.util.Exceptions;

/**
 * @author ads
 *
 */
public class PhpIndexer implements Indexer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Indexer#isIndexable(org.netbeans.modules.gsf.api.ParserFile)
     */
    public boolean isIndexable(ParserFile file) {
        String ext = file.getNameExt();
        return ext.endsWith(".php") || ext.endsWith(".phtml"); // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.Indexer#updateIndex(org.netbeans.modules.gsf.api.Index, org.netbeans.modules.gsf.api.ParserResult)
     */
    public void updateIndex(Index index, ParserResult result)
            throws IOException {
        new Job(index, result).process();
    }

    class Job {

        private Index index;
        private ParserResult result;

        public Job(Index index, ParserResult result) {
            this.result = result;
            this.index = index;
        }

        public void process() {
            PhpParseResult r = (PhpParseResult) result;
            try {
                r.getModel().readLock();
                List<Statement> statements = r.getModel().getStatements();
                ParserFile file = r.getFile();

                String url;
                try {
                    url = file.getFileObject().getURL().toExternalForm();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                    return;
                }

                if (!file.isPlatform()) {
                    Set<Map<String, String>> indexedList = Collections.emptySet();
                    Set<Map<String, String>> notIndexedList = Collections.emptySet();
                    Map<String, String> toDelete = new HashMap<String, String>();
                    toDelete.put("source", url);

                    try {
                        index.gsfStore(indexedList, notIndexedList, toDelete);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }

                

                Set<Map<String, String>> indexedList = new HashSet<Map<String, String>>();
                Set<Map<String, String>> notIndexedList = new HashSet<Map<String, String>>();

                Map<String, String> urli = new HashMap<String, String>();
                indexedList.add(urli);
                urli.put("source", url);

                for (Statement statement : statements) {
                    if (statement.getElementType() != FunctionDefinition.class) {
                        continue;
                    }

                    // Add indexed info
                    Map<String, String> indexed = new HashMap<String, String>();
                    indexedList.add(indexed);

                    Map<String, String> notIndexed = new HashMap<String, String>();
                    notIndexedList.add(notIndexed);

                    
                    FunctionDefinition func = (FunctionDefinition) statement;

                    String signature = func.getDeclaration().getName();
                    indexed.put("func", signature);
                }

                try {
                    Map<String, String> toDelete = Collections.emptyMap();
                    index.gsfStore(indexedList, notIndexedList, toDelete);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            } finally {
                r.getModel().readUnlock();
            }
        }
    }
}

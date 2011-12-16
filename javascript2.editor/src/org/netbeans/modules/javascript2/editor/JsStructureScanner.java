/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.*;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.model.FileScope;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.ModelElement;
import org.netbeans.modules.javascript2.editor.model.Scope;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Source;

/**
 *
 * @author Petr Pisl
 */
public class JsStructureScanner implements StructureScanner {

    //private static final String LAST_CORRECT_FOLDING_PROPERTY = "LAST_CORRECT_FOLDING_PROPERY";
    
    private static final String FOLD_CODE_BLOCKS = "codeblocks"; //NOI18N
    
    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
         
        JsParserResult result = (JsParserResult) info;
        final Model model = result.getModel();
        FileScope fileScope = model.getFileScope();
        
        
            
            List<Scope> scopes = getEmbededScopes(fileScope, null);
            for (Scope scope : scopes) {
                OffsetRange offsetRange = scope.getBlockRange();
                if (offsetRange == null) continue;
                    
                getRanges(folds, FOLD_CODE_BLOCKS).add(offsetRange);
                
            }
//            Source source = info.getSnapshot().getSource();
//            assert source != null : "source was null";
//            Document doc = source.getDocument(false);
//            
//            if (doc != null){
//                doc.putProperty(LAST_CORRECT_FOLDING_PROPERTY, folds);
//            }
            return folds;
        
        //return Collections.emptyMap();
    }
    
    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }
    
    private List<Scope>  getEmbededScopes(Scope scope, List<Scope> collectedScopes) {
        if (collectedScopes == null) {
            collectedScopes = new ArrayList<Scope>();
        }
        List<? extends ModelElement> elements = scope.getElements();
        for (ModelElement element : elements) {
            if (element instanceof Scope) {
                collectedScopes.add((Scope) element);
                getEmbededScopes((Scope) element, collectedScopes);
            }
        }
        return collectedScopes;
    }

    @Override
    public Configuration getConfiguration() {
        // TODO return a configuration to alow filter items. 
        return null;
    }
    
}

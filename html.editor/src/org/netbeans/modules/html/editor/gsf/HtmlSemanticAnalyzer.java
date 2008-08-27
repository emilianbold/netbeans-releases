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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.gsf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import org.netbeans.editor.ext.html.parser.SyntaxElement.TagAttribute;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.html.editor.HTMLKit;

/**
 *
 * @author marek
 */
public class HtmlSemanticAnalyzer implements SemanticAnalyzer {

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo ci) throws Exception {

        cancelled = false; //resume
        
        if (cancelled) {
            return;
        }

        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<OffsetRange, Set<ColoringAttributes>>();

        Iterator<? extends ParserResult> presultIterator = ci.getEmbeddedResults(HTMLKit.HTML_MIME_TYPE).iterator();
        if(!presultIterator.hasNext()) {
            return;
        }
        
        ParserResult presult = presultIterator.next();
        final TranslatedSource source = presult.getTranslatedSource();
        
        HtmlParserResult htmlResult = (HtmlParserResult) presult;
        
        if (cancelled) {
            return;
        }
        
        //just a test - highlight all tags' ids
        Set<TagAttribute> ids = htmlResult.elementsIds();
        for(TagAttribute ta : ids) {
            int start = ta.getValueOffset();
            if (source != null) {
                start = source.getLexicalOffset(start);
                if (start == -1) {
                    start = 0;
                }
            }
            
            // We assume that the start and end are always mapped to the same delta,
            // e.g. tags don't span embedding regions
            int end = start + ta.getValueLength();

            OffsetRange range = new OffsetRange(start, end);
            highlights.put(range, ColoringAttributes.METHOD_SET);
        }    

        semanticHighlights = highlights;

    }
}

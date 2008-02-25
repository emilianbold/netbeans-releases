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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.text.AbstractDocument;

import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.lexer.PhpTokenId;
import org.netbeans.modules.php.model.PhpModel;


/**
 * @author ads
 *
 */
public class StructureAnalyzer implements StructureScanner {

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.StructureScanner#folds(org.netbeans.modules.gsf.api.CompilationInfo)
     */
    public Map<String,List<OffsetRange>> folds(CompilationInfo info) {
        ParserResult result = info.getParserResult();
        assert result instanceof PhpParseResult;
        PhpModel model = ((PhpParseResult)result).getModel();
        Map<String,List<OffsetRange>> foldMap = new HashMap<String,List<OffsetRange>>();
        List<OffsetRange> folds = new LinkedList<OffsetRange>();
        foldMap.put("codeblocks", folds); // NOI18N

        addPhpBlockFolds( folds , model );
        
        addPhpInternalFolds( folds, model);
        
        return foldMap;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.StructureScanner#scan(org.netbeans.modules.gsf.api.CompilationInfo, org.netbeans.modules.gsf.api.HtmlFormatter)
     */
    public List<? extends StructureItem> scan( CompilationInfo info,
            HtmlFormatter formatter )
    {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }
    
    private void addPhpBlockFolds( List<OffsetRange> folds, PhpModel model ) {
        if (getDocument(model) != null) {
            getDocument(model).readLock();
        }
        try {
            TokenHierarchy hierarchy = TokenHierarchy.get(model.getDocument());
            TokenSequence seq = hierarchy.tokenSequence();
            int begin = 0;
            boolean flag = false;
            TokenId delim = null;
            seq.moveStart();
            while (seq.moveNext()) {
                Token token = seq.token();
                if (token == null) {
                    continue;
                }
                if (isStartDelimeter(token) && !flag) {
                    flag = true;
                    delim = token.id();
                    begin = seq.offset();
                }
                else if (isEndDelimeter(token) && flag && isPair( delim, token )) {
                    flag = false;
                    int end = seq.offset() + token.length();
                    folds.add(new OffsetRange(begin, end));
                }
            }
        }
        finally {
            if (getDocument(model) != null) {
                getDocument(model).readUnlock();
            }
        }
    }
    
    private boolean isStartDelimeter( Token token ) {
        return  token.id() == PhpTokenId.DELIMITER 
            || token.id() == PhpTokenId.DELIMITER1 || 
            token.id() == PhpTokenId.DELIMITER2 ;
    }
    
    
    private boolean isEndDelimeter( Token token ) {
        return  token.id() == PhpTokenId.DELIMITER ||
        token.id() == PhpTokenId.DELIMITER_END;
    }
    
    private boolean isPair( TokenId first , Token second ) {
        if ( first == PhpTokenId.DELIMITER ) {
            return second.id() == PhpTokenId.DELIMITER;
        }
        if ( first == PhpTokenId.DELIMITER1 || first == PhpTokenId.DELIMITER2 ) {
            return second.id() == PhpTokenId.DELIMITER_END;
        }
        else {
            assert false;
            return false;
        }
    }
    
    private AbstractDocument getDocument( PhpModel model ) {
        if ( model.getDocument() instanceof AbstractDocument ) {
            return (AbstractDocument) model.getDocument();
        }
        else {
            return null;
        }
    }

    private void addPhpInternalFolds( List<OffsetRange> folds, PhpModel model ) {
        // TODO 
    }

}

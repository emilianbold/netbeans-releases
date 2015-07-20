/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.hints;

import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
/**
 *
 * @author Danila Sergeyev
 */
public class ReplaceWithPragmaOnce implements Fix {
    private final BaseDocument doc;
    private final CsmFile file;
    private final int guardBlockStart;
    private final int guardBlockEnd;
    
    public ReplaceWithPragmaOnce(Document doc, CsmFile file, int guardBlockStart, int guardBlockEnd) {
        this.doc = (BaseDocument) doc;
        this.file = file;
        this.guardBlockStart = guardBlockStart;
        this.guardBlockEnd = guardBlockEnd;
    }
    
    @Override
    public String getText() {
        return NbBundle.getMessage(ReplaceWithPragmaOnce.class, "HINT_Pragma"); //NOI18N
    }
    
    @Override
    public ChangeInfo implement() throws Exception {
        CsmFileInfoQuery query = CsmFileInfoQuery.getDefault();
        
        // get offsets of #ifndef - #define
        Position startPosition = NbDocument.createPosition(doc, guardBlockStart, Position.Bias.Forward);
        Position endPosition = NbDocument.createPosition(doc, guardBlockEnd, Position.Bias.Backward);
        
        // get offsets of #endif
        int lineCount = query.getLineCount(file); // number of the last non-empty line
        int startLastLineOffset = (int) query.getOffset(file, lineCount, 1);
        int lastOffset = file.getText().length();
        final String endifMacro = "#endif";  // NOI18N
        if (file.getText(startLastLineOffset, lastOffset).toString().contains(endifMacro)) {
            Position startEndifPosition = NbDocument.createPosition(doc, startLastLineOffset, Position.Bias.Forward);
            Position endEndifPosition = NbDocument.createPosition(doc, lastOffset, Position.Bias.Backward);
            doc.replace(startPosition.getOffset(), endPosition.getOffset() - startPosition.getOffset(), "#pragma once", null); // NOI18N
            doc.replace(startEndifPosition.getOffset(), endEndifPosition.getOffset() - startEndifPosition.getOffset(), "", null); // NOI18N
        }
        return null;
    }
}

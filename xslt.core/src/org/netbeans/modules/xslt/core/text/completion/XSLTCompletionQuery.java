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

package org.netbeans.modules.xslt.core.text.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.xslt.core.text.completion.handler.HandlerAttributeEnumValues;
import org.netbeans.modules.xslt.core.text.completion.handler.HandlerCallTemplateName;
import org.netbeans.modules.xslt.core.text.completion.handler.HandlerCoreXPathFunctions;
import org.netbeans.modules.xslt.core.text.completion.handler.HandlerUseAttributeSets;
import org.netbeans.modules.xslt.core.text.completion.handler.HandlerWithParamName;
import org.netbeans.modules.xslt.core.text.completion.handler.XSLTCompletionHandler;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;

/**
 * @author Alex Petrov (30.04.2008)
 */
public class XSLTCompletionQuery extends AsyncCompletionQuery implements 
    Runnable, XSLTEditorComponentHolder {
    private static final List<XSLTCompletionHandler> xsltCompletionHandlers = 
        new ArrayList<XSLTCompletionHandler>(Arrays.asList(
            new XSLTCompletionHandler[] { 
                // don't change the sequence of handlers in this list
                new HandlerAttributeEnumValues(),
                new HandlerCallTemplateName(),
                new HandlerWithParamName(),
                new HandlerUseAttributeSets(),
                new HandlerCoreXPathFunctions()
            }
        ));
    
    private CompletionResultSet resultSet;
    private int caretOffset;
    private Document document;
    private JEditorPane srcEditorPane;
    
    @Override
    protected void query(CompletionResultSet resultSet, Document document, int caretOffset) {
        this.resultSet = resultSet;
        this.document = document;
        this.caretOffset = caretOffset;
        StyledDocument styledDoc = (StyledDocument) document;
        styledDoc.render(this);
    }
    
    public void run() {
        makeResultSet();
    }
    
    private void makeResultSet() {
        if ((resultSet == null) || (document == null) || 
            (srcEditorPane == null) || (caretOffset < 0)) {
            resultSet.finish();
            return;
        }
        for (XSLTCompletionHandler completionHandler : xsltCompletionHandlers) {
            List<XSLTCompletionResultItem> resultItemList = 
                completionHandler.getResultItemList(this);
            if (! resultItemList.isEmpty()) {
                resultSet.addAllItems(resultItemList);
                break;
            }
        }
        resultSet.setAnchorOffset(caretOffset);
        resultSet.finish();
    }

    @Override
    protected void prepareQuery(JTextComponent component) {
        super.prepareQuery(component);
        srcEditorPane = (XSLTCompletionUtil.getXsltDataEditorSupport() == null ? 
            null : (JEditorPane) component);
    }

    public JEditorPane getSourceEditorComponent() {
        return srcEditorPane;
    }
}

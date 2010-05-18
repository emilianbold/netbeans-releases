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

package org.netbeans.modules.xslt.tmap.model.api.completion;
    
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.HandlerImportNamespace;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.HandlerInvokeOpName;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.HandlerInvokePortType;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.HandlerOperationOpName;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.HandlerParamTypeValue;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.HandlerServicePortType;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.HandlerTransformSourceResult;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.TMapCompletionHandler;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;

/**
 * @author Alex Petrov (26.06.2008)
 */
public class TMapCompletionQuery extends AsyncCompletionQuery implements 
    Runnable, TMapEditorComponentHolder {
    private static final List<TMapCompletionHandler> tmapCompletionHandlers = 
        new ArrayList<TMapCompletionHandler>(Arrays.asList(
            new TMapCompletionHandler[] { 
                // don't change the sequence of handlers in this list
                new HandlerImportNamespace(),
                new HandlerServicePortType(),
                new HandlerInvokePortType(),
                new HandlerOperationOpName(),
                new HandlerInvokeOpName(),
                new HandlerTransformSourceResult(),
                new HandlerParamTypeValue()
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
        for (TMapCompletionHandler completionHandler : tmapCompletionHandlers) {
            List<TMapCompletionResultItem> resultItemList = 
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
        srcEditorPane = (TMapCompletionUtil.getTMapDataEditorSupport() == null ? 
            null : (JEditorPane) component);
        // srcEditorPane = getXsltSourceEditor();
    }

    public JEditorPane getEditorComponent() {
        return srcEditorPane;
    }
}
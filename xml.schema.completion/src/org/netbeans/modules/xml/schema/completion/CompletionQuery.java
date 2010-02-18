/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.filesystems.FileObject;

/**
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 * @author Alex Petrov (Alexey.Petrov@Sun.Com)
 */
public class CompletionQuery extends AsyncCompletionQuery {
    /**
     * Creates a new instance of CompletionQuery
     */
    public CompletionQuery(FileObject primaryFile) {
        this.primaryFile = primaryFile;
    }    
    
    @Override
    protected void prepareQuery(JTextComponent component) {
        this.component = component;
    }
    
    @Override
    protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
        XMLSyntaxSupport support = 
            (XMLSyntaxSupport) ((BaseDocument) doc).getSyntaxSupport();

        CompletionResultItem endTagResultItem = CompletionUtil.getEndTagCompletionItem(
            component, (BaseDocument) doc);
        List<CompletionResultItem> completionItems = null;
        if (! support.noCompletion(component) &&
           (CompletionUtil.canProvideCompletion((BaseDocument) doc))) {
            completionItems = getCompletionItems(doc, caretOffset);
        }
        if (endTagResultItem != null) resultSet.addItem(endTagResultItem);
        if ((completionItems != null) && (completionItems.size() > 0)) {
            resultSet.addAllItems(completionItems);
        } else if ((endTagResultItem != null) &&
                   (! (endTagResultItem instanceof TagLastCharResultItem))) {
            endTagResultItem.setExtraPaintGap(-CompletionPaintComponent.DEFAULT_ICON_TEXT_GAP);
        }
        resultSet.finish();
    }
        
    /**
     * This method is needed for unit testing purposes.
     */
    List<CompletionResultItem> getCompletionItems(Document doc, int caretOffset) {
        List<CompletionResultItem> completionItems = null;
        
        //Step 1: create a context
        XMLSyntaxSupport support = (XMLSyntaxSupport) ((BaseDocument)doc).getSyntaxSupport();
        context = new CompletionContextImpl(primaryFile, support, caretOffset);
        
        //Step 2: Accumulate all models and initialize the context
        if(!context.initContext() || !context.initModels() ) {
            return null;
        }
                
        //Step 3: Query
        switch (context.getCompletionType()) {
            case COMPLETION_TYPE_ELEMENT_VALUE:
                completionItems = CompletionUtil.getElementValues(context);
                if ((completionItems != null) && (completionItems.size() > 0)) {
                    break;
                }

            case COMPLETION_TYPE_ELEMENT:
                completionItems = CompletionUtil.getElements(context);
                break;
                
            case COMPLETION_TYPE_ATTRIBUTE:
                completionItems = CompletionUtil.getAttributes(context);
                break;
            
            case COMPLETION_TYPE_ATTRIBUTE_VALUE:
                completionItems = CompletionUtil.getAttributeValues(context);
                break;            
            
            case COMPLETION_TYPE_ENTITY:
                break;
            
            case COMPLETION_TYPE_NOTATION:
                break;
                
            default:
                break;
        }
        
        return completionItems;
    }
            
    private JTextComponent component;
    private FileObject primaryFile;
    private CompletionContextImpl context;
}
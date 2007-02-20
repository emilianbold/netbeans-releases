/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.editor.jspel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.completion.JspCompletionItem;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
/**
 *
 * @author Petr Pisl
 */
public class JSFELCompletionProvider implements CompletionProvider{
    
    /** Creates a new instance of JSFELCompletionProvider */
    public JSFELCompletionProvider() {
    }
    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE)
            return new AsyncCompletionTask(new CCQuery(component.getCaret().getDot()), component);
//        else if (queryType == DOCUMENTATION_QUERY_TYPE)
//            return new AsyncCompletionTask(new DocQuery(/*null*/), component);
        return null;
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    static final class CCQuery extends AsyncCompletionQuery {
        private int creationCaretOffset;
        private JTextComponent component;
        
        CCQuery(int caretOffset) {
            this.creationCaretOffset = caretOffset;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int offset) {
            ExtSyntaxSupport sup = (ExtSyntaxSupport)Utilities.getSyntaxSupport(component);
            FileObject fObject = NbEditorUtilities.getFileObject(doc);
            WebModule wm = null;
            if (fObject != null)
                wm = WebModule.getWebModule(fObject);
            if (sup instanceof JspSyntaxSupport && wm != null){
                JSFELExpression elExpr = new JSFELExpression (wm, (JspSyntaxSupport)sup);
                ArrayList complItems = new ArrayList();

                switch (elExpr.parse(offset)){
                    case JSFELExpression.EL_START:
                        List <ManagedBean>beans = JSFBeanCache.getBeans(wm);
                        
                        for (ManagedBean bean : beans){
                            if (bean.getManagedBeanName().startsWith(elExpr.getReplace())){
                                complItems.add(new JSFResultItem.JSFBean(bean.getManagedBeanName(), bean.getManagedBeanClass()));
                            }
                        }
                        break;
                    case JSFELExpression.EL_JSF_BEAN:
                        List<CompletionItem> items = elExpr.getPropertyCompletionItems(elExpr.getObjectClass());
                        complItems.addAll(items);
                        break;
                }
                for (int i = 0; i < complItems.size(); i++)
                    ((JspCompletionItem.JspResultItem)complItems.get(i)).setSubstituteOffset(offset - elExpr.getReplace().length());
                resultSet.addAllItems(complItems);
            }
            resultSet.finish();
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
    }
   
    
    static class DocQuery extends AsyncCompletionQuery {
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
        }
    }
}

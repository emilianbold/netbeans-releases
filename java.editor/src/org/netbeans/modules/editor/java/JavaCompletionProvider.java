/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.*;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;

import org.netbeans.editor.TokenID;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.ExtUtilities;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.spi.editor.completion.*;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionProvider implements CompletionProvider {
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        int ret = 0;
        if (typedText != null && typedText.endsWith(".")) // NOI18N
            ret |= COMPLETION_QUERY_TYPE;
        if (typedText != null && (typedText.endsWith("()")) || typedText.endsWith("();") || typedText.endsWith("().") || typedText.endsWith("(),")) // NOI18N
            ret |= TOOLTIP_QUERY_TYPE;
        if (ret != 0) {
            try {
                TokenID token = ((ExtSyntaxSupport)Utilities.getSyntaxSupport(component)).getTokenID(component.getCaret().getDot());
                int id = token != null ? token.getNumericID() : 0;
                if (id == JavaTokenContext.STRING_LITERAL_ID || id == JavaTokenContext.CHAR_LITERAL_ID || id == JavaTokenContext.LINE_COMMENT_ID || id == JavaTokenContext.BLOCK_COMMENT_ID)
                    return 0;
            } catch (BadLocationException e) {
            }
        }
        return ret;
    }
    
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE)
            return new AsyncCompletionTask(new Query(), component);
        else if (queryType == DOCUMENTATION_QUERY_TYPE)
            return new AsyncCompletionTask(new DocQuery(null), component);
        else if (queryType == TOOLTIP_QUERY_TYPE)
            return new AsyncCompletionTask(new ToolTipQuery(), component);
        return null;
    }
    
    static final class Query extends AsyncCompletionQuery {
        
        private JTextComponent component;
        
        private NbJavaJMICompletionQuery.JavaResult queryResult;
        
        private int queryCaretOffset;
        
        private int queryAnchorOffset;
        
        private String filterPrefix;
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            NbJavaJMICompletionQuery query = new NbJavaJMICompletionQuery(true);
            NbJavaJMICompletionQuery.JavaResult res = (NbJavaJMICompletionQuery.
                    JavaResult)query.query(component, caretOffset,
                    Utilities.getSyntaxSupport(component)
            );
            if (res != null) {
                queryCaretOffset = caretOffset;
                queryAnchorOffset = res.getSubstituteOffset();
                resultSet.setTitle(res.getTitle());
                resultSet.setAnchorOffset(queryAnchorOffset);
                resultSet.addAllItems(res.getData());
                queryResult = res;
            }
            resultSet.finish();
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            filterPrefix = null;
            if (caretOffset >= queryCaretOffset) {
                if (queryAnchorOffset > -1) {
                    try {
                        filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                        if (!isJavaIdentifierPart(filterPrefix)) {
                            filterPrefix = null;
                        }
                    } catch (BadLocationException e) {
                        // filterPrefix stays null -> no filtering
                    }
                }
            }
            return (filterPrefix != null);
        }        
        
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null) {
                resultSet.setTitle(queryResult.getTitle());
                resultSet.setAnchorOffset(queryAnchorOffset);
                resultSet.addAllItems(getFilteredData(queryResult.getData(), filterPrefix));
                resultSet.finish();
            }
        }

        private boolean isJavaIdentifierPart(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i))) ) {
                    return false;
                }
            }
            return true;
        }
        
        private Collection getFilteredData(Collection data, String prefix) {
            List ret = new ArrayList();
            for (Iterator it = data.iterator(); it.hasNext();) {
                CompletionQuery.ResultItem itm = (CompletionQuery.ResultItem) it.next();
                if (JMIUtils.startsWith(itm.getItemText(), prefix))
                    ret.add(itm);
            }
            return ret;
        }
        
        private String getFilteredTitle(String title, String prefix) {
            int lastIdx = title.lastIndexOf('.');
            String ret = lastIdx == -1 ? prefix : title.substring(0, lastIdx + 1) + prefix;
            if (title.endsWith("*")) // NOI18N
                ret += "*"; // NOI18N
            return ret;
        }
    }
    
    static class DocQuery extends AsyncCompletionQuery {
        
        private Object item;
        
        private JTextComponent component;
        
        DocQuery(Object item) {
            this.item = item;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item == null)
                item = JMIUtils.findItemAtCaretPos(component);
            if (item != null) {
                resultSet.setDocumentation(new DocItem(getAssociatedObject(item),
                        ExtUtilities.getExtEditorUI(component)));
            }
            resultSet.finish();
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        private Object getAssociatedObject(Object item) {
            Object ret = item;
            if (item instanceof NbJMIResultItem) {
                ret = ((NbJMIResultItem)item).getAssociatedObject();
                if (ret instanceof JMIUtils.FakeConstructor)
                    ret = ((JMIUtils.FakeConstructor)ret).getDeclaringClass();
            }
            if (ret instanceof Feature)
                ret = JMIUtils.getDefintion((Feature)ret);
            if (ret instanceof ClassDefinition)
                ret = JMIUtils.getSourceElementIfExists((ClassDefinition)ret);
            return ret;
        }

        private class DocItem implements CompletionDocumentation {
            
            private String text;
            private MyJavaDoc doc;
            private Action goToSource = null;
            private ExtEditorUI ui;
            
            public DocItem(final Object item, ExtEditorUI ui) {
                this.ui = ui;
                doc = new MyJavaDoc(ui);
                doc.setItem(item);
                Resource res = item instanceof Element ? ((Element)item).getResource() : null;
                if (res != null && res.getName().endsWith(".java")) //NOI18N
                    goToSource = new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            JMIUtils.openElement((Element)item);
                            if (e != null) {
                                Completion.get().hideDocumentation();
                            }
                        }
                    };
            }
            
            public CompletionDocumentation resolveLink(String link) {
                return new DocItem(doc.parseLink(link, (JavaClass)null), ui);
            }
            
            public String getText() {
                return text;
            }
            
            public URL getURL() {
                return null;
            }
            
            public Action getGotoSourceAction() {
                return goToSource;
            }
            
            private class MyJavaDoc extends NbJMICompletionJavaDoc {
                
                private MyJavaDoc(ExtEditorUI ui) {
                    super(ui);
                }

                private void setItem(Object item) {
                    new MyJavaDocParser(item).run();
                }
                
                private class MyJavaDocParser extends NbJMICompletionJavaDoc.JMIParsingThread {
                    private MyJavaDocParser(Object content) {
                        super(content);
                    }
                    
                    protected void showJavaDoc(final String preparedText) {
                        text = preparedText;                                
                    }
                }
            }
        }
    }

    static class ToolTipQuery extends AsyncCompletionQuery {
        
        private JTextComponent component;
        
        private int queryCaretOffset;
        
        private int queryAnchorOffset;
        
        private JToolTip queryToolTip;
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            NbJavaJMICompletionQuery query = new NbJavaJMICompletionQuery(true);
            BaseDocument bdoc = (BaseDocument)doc;
            NbJavaJMICompletionQuery.JavaResult res = (NbJavaJMICompletionQuery.
                    JavaResult)query.tipQuery(component, caretOffset,
                    bdoc.getSyntaxSupport(), false);
            if (res != null) {
                queryCaretOffset = caretOffset;
                List list = new ArrayList();
                int idx = -1;
                for (Iterator it = res.getData().iterator(); it.hasNext();) {
                    Object o = it.next();
                    if (o instanceof NbJMIResultItem.CallableFeatureResultItem) {
                        NbJMIResultItem.CallableFeatureResultItem item = (NbJMIResultItem.CallableFeatureResultItem) o;
                        List parms = item.createParamsList();
                        if (parms.size() > 0) {
                            idx = item.getCurrentParamIndex();
                        } else {
                            parms.add(NbBundle.getMessage(JavaCompletionProvider.class, "JCP-no-parameters"));
                        }
                        list.add(parms);
                    }
                }

                resultSet.setAnchorOffset(queryAnchorOffset = res.getSubstituteOffset() + 1);
                resultSet.setToolTip(queryToolTip = new MethodParamsTipPaintComponent(list, idx));
            }
            resultSet.finish();
        }
        
        protected void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected boolean canFilter(JTextComponent component) {
            String text = null;
            int caretOffset = component.getCaretPosition();
            Document doc = component.getDocument();
            try {
                if (caretOffset - queryCaretOffset > 0)
                    text = doc.getText(queryCaretOffset, caretOffset - queryCaretOffset);
                else if (caretOffset - queryCaretOffset < 0)
                    text = doc.getText(caretOffset, queryCaretOffset - caretOffset);
            } catch (BadLocationException e) {
            }
            if (text == null || text.indexOf(',') != -1 || text.indexOf('(') != -1 || text.indexOf(')') != -1) // NOI18N
                return false;
            
            return true;
        }
        
        protected void filter(CompletionResultSet resultSet) {
            resultSet.setAnchorOffset(queryAnchorOffset);
            resultSet.setToolTip(queryToolTip);
            resultSet.finish();
        }
    }
}


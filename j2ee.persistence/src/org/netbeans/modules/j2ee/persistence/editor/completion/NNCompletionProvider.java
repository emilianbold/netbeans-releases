///*
// * The contents of this file are subject to the terms of the Common Development
// * and Distribution License (the License). You may not use this file except in
// * compliance with the License.
// *
// * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
// * or http://www.netbeans.org/cddl.txt.
// *
// * When distributing Covered Code, include this CDDL Header Notice in each file
// * and include the License file at http://www.netbeans.org/cddl.txt.
// * If applicable, add the following below the CDDL Header, with the fields
// * enclosed by brackets [] replaced by your own identifying information:
// * "Portions Copyrighted [year] [name of copyright owner]"
// *
// * The Original Software is NetBeans. The Initial Developer of the Original
// * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
// * Microsystems, Inc. All Rights Reserved.
// */
//
//package org.netbeans.modules.j2ee.persistence.editor.completion;
//
//import javax.swing.text.Document;
//import javax.swing.text.JTextComponent;
//import org.netbeans.editor.Utilities;
//import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
//import org.netbeans.spi.editor.completion.CompletionProvider;
//import org.netbeans.spi.editor.completion.CompletionResultSet;
//import org.netbeans.spi.editor.completion.CompletionTask;
//import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
//import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
//import org.openide.util.NbBundle;
//24.10.2006
///**
// *
// * @author abadea
// */
//public class NNCompletionProvider implements CompletionProvider {
//    
//    public int getAutoQueryTypes(JTextComponent component, String typedText) {
//        return 0;
//    }
//
//    public CompletionTask createTask(int queryType, JTextComponent component) {
//        if (queryType == CompletionProvider.COMPLETION_QUERY_TYPE) {
//            return new AsyncCompletionTask(new Query(component.getCaret().getDot()), component);
//        }
//        return null;
//    }
//    
//    static final class Query extends AsyncCompletionQuery {
//        
//        private JTextComponent component;
//        
//        private int creationCaretOffset;
//        private int queryCaretOffset;
//        
//        private int queryAnchorOffset;
//        
//        private String filterPrefix;
//        
//        Query(int caretOffset) {
//            this.creationCaretOffset = caretOffset;
//        }
//        
//        protected void preQueryUpdate(JTextComponent component) {
////            int caretOffset = component.getCaretPosition();
////            Document doc = component.getDocument();
////            if (caretOffset >= creationCaretOffset) {
////                try {
////                    if (isJavaIdentifierPart(doc.getText(creationCaretOffset, caretOffset - creationCaretOffset)))
////                        return;
////                } catch (BadLocationException e) {
////                }
////            }
////            Completion.get().hideCompletion();
//        }        
//        
//        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
//            if (JavaMetamodel.getManager().isScanInProgress()) {
//                resultSet.setWaitText(NbBundle.getMessage(NNCompletionProvider.class, "scanning-in-progress")); //NOI18N
//            }
//            NNCompletionQuery query = new NNCompletionQuery(true);
//            NNCompletionQuery.DefaultResult res = (NNCompletionQuery.DefaultResult)query.query(component, caretOffset, Utilities.getSyntaxSupport(component));
//            if (res != null) {
//                queryCaretOffset = caretOffset;
//                //queryAnchorOffset = res.getSubstituteOffset();
//                resultSet.setTitle(res.getTitle());
//                // resultSet.setAnchorOffset(queryAnchorOffset);
//                resultSet.addAllItems(res.getData());
//                // queryResult = res;
//            }
//            resultSet.finish();
//        }
//        
//        protected void prepareQuery(JTextComponent component) {
//            this.component = component;
//        }
//        
////        private boolean isJavaIdentifierPart(String text) {
////            for (int i = 0; i < text.length(); i++) {
////                if (!(Character.isJavaIdentifierPart(text.charAt(i))) ) {
////                    return false;
////                }
////            }
////            return true;
////        }
//        
//        // TODO: filtering
//    }
//}

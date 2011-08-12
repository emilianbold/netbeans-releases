///*
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// *
// * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
// *
// * The contents of this file are subject to the terms of either the GNU
// * General Public License Version 2 only ("GPL") or the Common
// * Development and Distribution License("CDDL") (collectively, the
// * "License"). You may not use this file except in compliance with the
// * License. You can obtain a copy of the License at
// * http://www.netbeans.org/cddl-gplv2.html
// * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
// * specific language governing permissions and limitations under the
// * License.  When distributing the software, include this License Header
// * Notice in each file and include the License file at
// * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
// * particular file as subject to the "Classpath" exception as provided
// * by Sun in the GPL Version 2 section of the License file that
// * accompanied this code. If applicable, add the following below the
// * License Header, with the fields enclosed by brackets [] replaced by
// * your own identifying information:
// * "Portions Copyrighted [year] [name of copyright owner]"
// *
// * Contributor(s):
// *
// * The Original Software is NetBeans. The Initial Developer of the Original
// * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
// * Microsystems, Inc. All Rights Reserved.
// *
// * If you wish your version of this file to be governed by only the CDDL
// * or only the GPL Version 2, indicate your decision by adding
// * "[Contributor] elects to include this software in this distribution
// * under the [CDDL or GPL Version 2] license." If you do not indicate a
// * single choice of license, a recipient has the option to distribute
// * your version of this file under either the CDDL, the GPL Version 2 or
// * to extend the choice of license to its licensees as provided above.
// * However, if you add GPL Version 2 code and therefore, elected the GPL
// * Version 2 license, then the option applies only if the new code is
// * made subject to such option by the copyright holder.
// */
//
//package org.netbeans.modules.j2ee.persistence.editor.completion;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import javax.swing.text.BadLocationException;
//import javax.swing.text.JTextComponent;
//import org.netbeans.editor.TokenItem;
//import org.netbeans.editor.ext.CompletionQuery;
//import org.netbeans.editor.ext.Completion;
//import org.netbeans.editor.ext.ExtEditorUI;
//import org.netbeans.editor.BaseDocument;
//import org.netbeans.editor.Utilities;
//import org.netbeans.editor.ext.java.JCExpression;
//import org.netbeans.editor.ext.java.JCFinder;
//import org.netbeans.editor.ext.java.JavaCompletionQuery;
//import org.netbeans.editor.ext.java.JavaSyntaxSupport;
//import org.netbeans.editor.ext.java.JavaTokenContext;
//import org.netbeans.jmi.javamodel.*;
//import org.netbeans.modules.editor.NbEditorUtilities;
//import org.netbeans.modules.editor.java.JCFinderFactory;
//import org.netbeans.modules.editor.java.NbJavaJMISyntaxSupport;
//import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.EntityMappings;
//import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
//import org.netbeans.modules.j2ee.persistence.editor.completion.db.DBCompletionContextResolver;
//import org.netbeans.modules.javacore.JMManager;
//import org.openide.ErrorManager;
//import org.openide.filesystems.FileObject;
//import org.openide.loaders.DataObject;
//
///**
// * Completion query for Java EE 5 aNNotations.
// *
// * @author Dusan Balek, Andrei Badea, Marek Fukala
// */
//public class NNCompletionQuery extends JavaCompletionQuery {
//    
//    private List resolvers;
//    
//    public NNCompletionQuery(boolean isJava15) {
//        setJava15(isJava15);
//        initResolvers();
//    }
//    
//    private void initResolvers() {
//        //XXX temporary - should be registered somehow better
//        resolvers = new ArrayList();
//        resolvers.add(new DBCompletionContextResolver());
//    }
//    
//    protected JCFinder getFinder() {
//        FileObject fo = getFileObject();
//        return JCFinderFactory.getDefault().getFinder(fo);
//    }
//    
//    protected CompletionQuery.Result getResult(JTextComponent component, JavaSyntaxSupport sup, boolean openingSource, int offset, JCExpression exp) {
//        Completion completion = ((ExtEditorUI)Utilities.getEditorUI(component)).getCompletion();
//        boolean autoPopup = completion != null ? completion.provokedByAutoPopup : false;
//        ArrayList results = new ArrayList();
//        
//        //query all available CompletionContextResolver-s
//        Iterator resolversItr = resolvers.iterator();
//        while(resolversItr.hasNext()) {
//            CompletionContextResolver resolver = (CompletionContextResolver)resolversItr.next();
//            Context ctx = new Context(component, (NbJavaJMISyntaxSupport)sup.get(NbJavaJMISyntaxSupport.class), openingSource, offset, autoPopup);
//            if(ctx.getEntityMappings() == null) {
//                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "No EnitityMappings defined.");
//                break;
//            } else {
//                results.addAll(resolver.resolve(exp, ctx));
//            }
//        }
//        return new CompletionQuery.DefaultResult(component, "*", results, exp.getTokenCount() == 0 ? offset : exp.getTokenOffset(0), 0); // NOI18N
//    }
//    
//    private FileObject getFileObject() {
//        BaseDocument bDoc = getBaseDocument();
//        DataObject dobj = NbEditorUtilities.getDataObject(bDoc);
//        return dobj.getPrimaryFile();
//    }
//    
//    public final class Context {
//        
//        /** Text component */
//        private JTextComponent component;
//        
//        /** Syntax support for the given document */
//        private NbJavaJMISyntaxSupport sup;
//        
//        /** Whether the query is performed to open the source file. It has slightly
//         * different handling in some situations.
//         */
//        private boolean openingSource;
//        
//        /** End position of the scanning - usually the caret position */
//        private int endOffset;
//        
//        private JavaClass curCls;
//        
//        private PersistenceUnit[] pus;
//        private EntityMappings emaps;
//        
//        /** True when code completion is invoked by auto popup. In such case, code completion returns no result
//         * after "new ". To get a result, code completion has to be invoked manually (using Ctrl-Space). */ // NOI18N
//        private boolean autoPopup;
//        
//        private String completedMemberName, completedMemberJavaClassName;
//        
//        private NNParser nnparser;
//        private NNParser.NN parsednn = null;
//        
//        public Context(JTextComponent component, NbJavaJMISyntaxSupport sup, boolean openingSource, int endOffset, boolean autoPopup) {
//            this.component = component;
//            this.sup = sup;
//            this.openingSource = openingSource;
//            this.endOffset = endOffset;
//            this.autoPopup = autoPopup;
//            
//            FileObject documentFO = getFileObject();
//            if(documentFO != null) {
//                try {
//                    this.pus = PersistenceUtils.getPersistenceUnits(documentFO);
//                } catch (IOException e) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//                }
//                this.emaps = PersistenceUtils.getEntityMappings(documentFO);
//            }
//            
//            this.nnparser = new NNParser(getBaseDocument());
//        }
//        
//        /** Must be run under MDR transaction! */
//        public JavaClass getJavaClass() {
//            assert Thread.currentThread() == JMManager.getTransactionMutex().getThread();
//            
//            curCls = sup.getJavaClass(endOffset);
//            if (curCls == null) {
//                curCls = sup.getTopJavaClass();
//            }
//            return curCls;
//        }
//        
//        public BaseDocument getBaseDocument() {
//            return sup.getDocument();
//        }
//        
//        public FileObject getFileObject() {
//            return NbEditorUtilities.getFileObject(getBaseDocument());
//        }
//        
//        /** @return an arrat of PUs which this sourcefile belongs to. */
//        public PersistenceUnit[] getPersistenceUnits() {
//            return this.pus;
//        }
//        
//        public EntityMappings getEntityMappings() {
//            return this.emaps;
//        }
//        
//        public int getCompletionOffset() {
//            return endOffset;
//        }
//        
//        public NbJavaJMISyntaxSupport getSyntaxSupport() {
//            return sup;
//        }
//        
//        public NNParser.NN getParsedAnnotation() {
//            synchronized (nnparser) {
//                if(parsednn == null) {
//                    parsednn = nnparser.parseAnnotation(getCompletionOffset());
//                }
//                return parsednn;
//            }
//        }
//        
//        public String getCompletedMemberClassName() {
//            if(completedMemberJavaClassName == null) initCompletedMemberContext();
//            return completedMemberJavaClassName;
//        }
//        
//        public String getCompletedMemberName() {
//            if(completedMemberName== null) initCompletedMemberContext();
//            return completedMemberName;
//        }
//        
//        
//        private void initCompletedMemberContext() {
//            //parse the text behind the cursor and try to find identifiers.
//            //it seems to be impossible to use JMI model for this since it havily
//            //relies on the state of the source (whether it contains errors, which types etc.)
//            String type = null;
//            String genericType = null;
//            String propertyName = null;
//            NNParser nnp = new NNParser(getBaseDocument()); //helper parser
//            try {
//                TokenItem ti = sup.getTokenChain(getCompletionOffset(), getCompletionOffset() + 1);
//                while(ti != null && propertyName == null) {
//                    
//                    //skip all annotations between the CC offset and the completed member
//                    if(ti.getTokenID() == JavaTokenContext.ANNOTATION) {
//                        //parse to find NN end
//                        NNParser.NN parsed = nnp.parseAnnotation(ti.getOffset() + 1);
//                        if(parsed != null) {
//                            //parse after the NN end (skip)
//                            ti = sup.getTokenChain(parsed.getEndOffset(), parsed.getEndOffset() + 1);
//                            continue;
//                        }
//                    }
//                    
//                    //test whether we have just found a type and '<' character after
//                    if(genericType != null && ti.getTokenID() == JavaTokenContext.LT) {
//                        //maybe a start of generic
//                        TokenItem ti2 = ti.getNext();
//                        if(ti2.getTokenID() == JavaTokenContext.IDENTIFIER) {
//                            //found generic
//                            genericType = ti2.getImage();
//                            ti = ti.getNext(); //skip the next IDENTIFIER token so it is not considered as property name
//                        } else {
//                            //false alarm
//                            genericType = null;
//                        }
//                    } else if(ti.getTokenID() == JavaTokenContext.IDENTIFIER) {
//                        if(type == null) {
//                            type = ti.getImage();
//                            genericType = type;
//                        } else {
//                            propertyName = ti.getImage();
//                        }
//                    }
//                    ti = ti.getNext();
//                }
//            } catch (BadLocationException ex) {
//                ex.printStackTrace();
//            }
//            
//            completedMemberName = propertyName;
//            completedMemberJavaClassName = genericType == null ? type : genericType;
//        }
//        
//    }
//}

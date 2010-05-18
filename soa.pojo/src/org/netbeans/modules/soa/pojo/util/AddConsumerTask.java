/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.pojo.util;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author sgenipudi
 */
public class AddConsumerTask<T> extends  ModificationTask<WorkingCopy> {

    private ExecutableElement mExecElem = null;
    private  Map<String,Object> mProps = null;
    private Exception myException = null;
    private static int start=0;

    public AddConsumerTask(ExecutableElement method, Map<String,Object> props) {
        mExecElem = method;
        mProps = props;
    }

   public void run(WorkingCopy workingCopy) throws Exception {
        Iterator<Entry<Object, Object>> itr;
        try {
            workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClassTree = classTree;
            TreeMaker make = workingCopy.getTreeMaker();

            ExecutableElement method = (ExecutableElement) mProps.get(GeneratorUtil.POJO_SELECTED_METHOD);
            Trees trees = workingCopy.getTrees();
            start++;

            //Import org.glassfish.openesb.pojose.api.res.POJOContext
            // org.glassfish.openesb.pojose.api.annotation.POJOResource
            //org.glassfish.openesb.pojose.api.annotation.Endpoint
            //javax.jbi.servicedesc.ServiceEndpoint
            List<? extends ImportTree> importLists =cut.getImports();
            List<String> importedClassList = new ArrayList<String>();
            for (ImportTree imp: importLists) {
                importedClassList.add(imp.getQualifiedIdentifier().toString());
            }
            CompilationUnitTree oldCopy = cut;
            CompilationUnitTree newCopy = null;
            if (! importedClassList.contains(GeneratorUtil.CTX_QUAL_CLASS_ANNOTATION )) {
                newCopy = make.addCompUnitImport(
                cut,
                make.Import(make.Identifier(GeneratorUtil.CTX_QUAL_CLASS_ANNOTATION  ), false)

                );
              oldCopy = newCopy;
            }
            //workingCopy.rewrite(cut, copy);
            //cut = workingCopy.getCompilationUnit();
            //classTree = (ClassTree) cut.getTypeDecls().get(0);
            if (! importedClassList.contains(GeneratorUtil.RSRC_QUAL_CLASS_ANNOTATION )) {
                newCopy = make.addCompUnitImport(
                    oldCopy,
                    make.Import(make.Identifier(GeneratorUtil.RSRC_QUAL_CLASS_ANNOTATION), false)
                );
                oldCopy = newCopy;
            }

            if (! importedClassList.contains(GeneratorUtil.CONS_QUAL_CLASS_ANNOTATION )) {
                newCopy = make.addCompUnitImport(
                    oldCopy,
                    make.Import(make.Identifier(GeneratorUtil.CONS_QUAL_CLASS_ANNOTATION), false)
                );
                oldCopy = newCopy;
            }
              if (! importedClassList.contains(GeneratorUtil.CONS_EP_QUAL_CLASS_ANNOTATION )) {
                newCopy = make.addCompUnitImport(
                    oldCopy,
                    make.Import(make.Identifier(GeneratorUtil.CONS_EP_QUAL_CLASS_ANNOTATION), false)
                );
                oldCopy = newCopy;
            }
            String[] listOfImports = this.getImportList(this.mProps);
            for ( String importName: listOfImports) {
                  if (! importedClassList.contains(importName)) {
                    newCopy = make.addCompUnitImport(
                        oldCopy,
                        make.Import(make.Identifier(importName), false)
                    );
                    oldCopy = newCopy;
                }
            }


            if ( newCopy != null) {
                workingCopy.rewrite(cut, newCopy);

            }
             cut = workingCopy.getCompilationUnit();
             classTree = (ClassTree) cut.getTypeDecls().get(0);


             //Preprocess the class.
             List<? extends Tree> memberTreeList = classTree.getMembers();
             Properties listOfMemberNames = new Properties();
             Map<String, Properties>  mapAnnotations = new HashMap<String, Properties>();
             Map<String, Map<String,Properties>> mapMemberNamesAnnotations = new HashMap<String, Map<String,Properties>>();
             for ( Tree memberTree: memberTreeList) {
                 if ( memberTree.getKind() == Tree.Kind.VARIABLE) {
                     VariableTree vt = (VariableTree) memberTree;
                     if (  vt.getType().getKind() == Tree.Kind.IDENTIFIER) {
                         IdentifierTree idt = (IdentifierTree) vt.getType();
                         listOfMemberNames.put( vt.getName().toString(),idt.getName().toString() );
                     } else {
                        listOfMemberNames.put( vt.getName().toString(),vt.getType().toString() );
                     }

                     List<? extends AnnotationTree> annTreeList = vt.getModifiers().getAnnotations();
                     if ( annTreeList != null) {
                         for (AnnotationTree anTre: annTreeList) {

                             Tree annType = anTre.getAnnotationType();//anTre.getKind() ANNOTATION
                             if ( annType.getKind() == Tree.Kind.IDENTIFIER) {//annType.getKind() IDENTIFIER
                                 // ((IdentifierTree)annType).getName().toString() "Endpoint"
                                 String annName = ((IdentifierTree)annType).getName().toString();
                                 Properties annProps = new Properties();
                                 String annKey = null;
                                 String annValue = null;
                                 List<? extends ExpressionTree> listOfAnnArgs = anTre.getArguments();
                                 for (ExpressionTree exp : listOfAnnArgs) {//ASSIGNMENT
                                     if ( exp.getKind() == Tree.Kind.ASSIGNMENT) {
                                         AssignmentTree asmtTree =(AssignmentTree) exp;
                                         ExpressionTree asmtExpTree = asmtTree.getExpression();
                                         if ( asmtExpTree.getKind() == Tree.Kind.STRING_LITERAL) {
                                             LiteralTree asmtExpLiteralTree = (LiteralTree) asmtExpTree;
                                             annKey =(String) asmtExpLiteralTree.getValue();
                                         }
                                         ExpressionTree asmtVarTree = asmtTree.getVariable();
                                         if ( asmtVarTree.getKind() == Tree.Kind.IDENTIFIER) {
                                             IdentifierTree asmtVarIdentTree = (IdentifierTree)asmtVarTree;
                                             annValue = asmtVarIdentTree.getName().toString();
                                         }
                                         if ( annKey != null && annValue != null) {
                                            annProps.put(annValue, annKey);
                                         }

                                     }
                                 }
                                 mapAnnotations.put(annName, annProps);
                                 mapMemberNamesAnnotations.put(vt.getName().toString(), mapAnnotations);
                             }
                         }
                     }

                 }
             }
            //POJOResource  annotation
            String resourceVar = GeneratorUtil.POJO_CTX_VARIABLE;//"mPojoResource"+System.currentTimeMillis(); //NOI18N
            boolean bUseCtxName = true;
           // resourceVar = resourceVar.substring(0, 16)+start;
            if ( !listOfMemberNames.containsKey(resourceVar) ) {
                bUseCtxName = true;
            } else {
                String ctxType = listOfMemberNames.getProperty(resourceVar);
                if ( ctxType.equals(GeneratorUtil.CTX_QUAL_CLASS_ANNOTATION)||  ctxType.equals(GeneratorUtil.CTX_ANNOTATION)) {
                    bUseCtxName = false;
                } else {
                    if ( listOfMemberNames.containsValue(GeneratorUtil.CTX_QUAL_CLASS_ANNOTATION) ||
                         listOfMemberNames.containsValue(GeneratorUtil.CTX_ANNOTATION)) {
                        itr = listOfMemberNames.entrySet().iterator();
                        while  ( itr.hasNext()) {
                            Entry<Object,Object> entry =itr.next();
                            if ( entry.getValue().equals(GeneratorUtil.CTX_QUAL_CLASS_ANNOTATION) ||
                                entry.getValue().equals(GeneratorUtil.CTX_ANNOTATION)  ) {
                                resourceVar = (String) entry.getKey();
                                bUseCtxName = false;
                                break;
                            }
                        }
                    } else {
                        resourceVar = resourceVar+System.currentTimeMillis();
                        resourceVar = resourceVar.substring(0, 5)+start;
                    }
                }
            }
            if ( bUseCtxName) {
                ModifiersTree modifiers = handleModifiersAndAnnotations(make, workingCopy,
                        GeneratorUtil.RSRC_ANNOTATION, null);

                VariableTree variableTree = GeneratorUtil.createField(make, workingCopy,
                    modifiers,
                    resourceVar,
                    GeneratorUtil.CTX_ANNOTATION, //NOI18N
                    null
                );


                newClassTree = make.addClassMember(classTree, variableTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
            String ctxVar = resourceVar;
            //Service Endpoint
            cut = workingCopy.getCompilationUnit();

            String consIntfNs = (String) this.mProps.get(GeneratorUtil.POJO_CONSUMER_INTERFACE_NS);//NOI18N
            String intfName = (String) this.mProps.get(GeneratorUtil.POJO_CONSUMER_INTERFACE_NAME);//NOI18N
            String opnName = (String) this.mProps.get(GeneratorUtil.POJO_CONSUMER_OPERATION_NAME);//NOI18N
            String inMessageTypeName = (String) this.mProps.get(GeneratorUtil.POJO_CONSUMER_INPUT_MESSAGE_TYPE);//NOI18N
            String inMessageTypeNameNS = (String) this.mProps.get(GeneratorUtil.POJO_CONSUMER_INPUT_MESSAGE_TYPE_NS);//NOI18N
            if ( inMessageTypeName == null) {
                inMessageTypeName ="";
            }
            if ( inMessageTypeNameNS == null) {
                inMessageTypeNameNS ="";
            }

            boolean bCreateServiceEndpointAnnotation = true;

            resourceVar = fetchResourceVariable(mapMemberNamesAnnotations, intfName, consIntfNs, opnName,inMessageTypeName,GeneratorUtil.CONS_EP_ANNOTATION);
            if ( resourceVar == null ) {
                classTree = (ClassTree) cut.getTypeDecls().get(0);
                resourceVar = "sep"+GeneratorUtil.getHungarianNotation(intfName)+GeneratorUtil.getHungarianNotation(opnName); //NOI18N
                if ( resourceVar.length() > 30) {
                    resourceVar = "sep"+GeneratorUtil.getHungarianNotation(intfName); //NOI18N
                }
                VariableScanner vs = new VariableScanner(workingCopy.getJavaSource());
                List<String> listVariables = vs.getVariableNames();
                resourceVar =GeneratorUtil.getVariableName(listVariables, resourceVar);
                MethodExistenceCheckUtil mu = new MethodExistenceCheckUtil(workingCopy.getJavaSource(), GeneratorUtil.POJO_DEFAULT_RECEIVE_OPERATION, null, false);

//                resourceVar = resourceVar.substring(0, 20)+start;
                Map<String, Object> svEpMap = new HashMap<String, Object>();
                String serviceName =resourceVar.substring(1, resourceVar.length()-1);

                svEpMap.put(GeneratorUtil.NAME_CONST,serviceName );//NOI18N
                svEpMap.put(GeneratorUtil.INTF_QNAME_CONST,new QName(consIntfNs,intfName).toString());
                svEpMap.put(GeneratorUtil.IN_MSGTYPE_QNAME_CONST, new QName(inMessageTypeNameNS,inMessageTypeName).toString());//NOI18N
                svEpMap.put(GeneratorUtil.OPN_QNAME_CONST, new QName(consIntfNs, opnName).toString());//NOI18N
                svEpMap.put(GeneratorUtil.SERVICE_QNAME, new QName(consIntfNs, serviceName+GeneratorUtil.POJO_SERVICE_SUFFIX).toString());//NOI18N
    // @Endpoint(name = "ep1", serviceName = "bpl1", interfaceName = "NoNSPortType", interfaceNS = "bpl1", inMessageType = "NoNSOperationRequest", operationName = "NoNSOperation")

                ModifiersTree modifiers1 = handleModifiersAndAnnotations(make, workingCopy,
                        GeneratorUtil.CONS_EP_ANNOTATION, svEpMap);

                VariableTree variableTree1 = GeneratorUtil.createField(make, workingCopy,
                    modifiers1,
                    resourceVar,
                    GeneratorUtil.CONSUMER_ANNOTATION, //NOI18N
                    null
                );

                newClassTree = make.addClassMember(newClassTree, variableTree1);
                workingCopy.rewrite(classTree, newClassTree);
            }

            cut = workingCopy.getCompilationUnit();
            classTree = (ClassTree) cut.getTypeDecls().get(0);

            MethodTree methodTree = (MethodTree) trees.getTree(ElementHandle.create(method).resolve(workingCopy));
             TreeUtilities treeUtilities = workingCopy.getTreeUtilities();
             appendStatementsToMethod(make, workingCopy, treeUtilities, methodTree,resourceVar, ctxVar);

            workingCopy.rewrite(classTree, newClassTree);
        } catch ( Exception ex) {
            myException = ex;
            throw ex;
        }
   }

    private String fetchResourceVariable(Map<String, Map<String, Properties>> mapMemberNamesAnnotations, String intfName, String consIntfNs, String opnName, String inMessageTypeName, String searchAnn) {
        String resourceName = null;
        //variable to annotation relation.
        Set<Entry<String, Map<String, Properties>>> memberAnnSet = mapMemberNamesAnnotations.entrySet();
        Iterator<Entry<String, Map<String, Properties>>> memberAnnSetItr = memberAnnSet.iterator();
        //variable to annotation map.
        Entry<String, Map<String, Properties>> memberAnnSetEntry;
        Map<String, Properties> mapAnnSet;
        while ( memberAnnSetItr.hasNext()) {
            memberAnnSetEntry = memberAnnSetItr.next();
            mapAnnSet = memberAnnSetEntry.getValue();
            //get the annotation map
            Properties annPropValues = mapAnnSet.get(searchAnn);
            if ( annPropValues != null && annPropValues.size() > 0) {
                if (annPropValues.containsKey(GeneratorUtil.OPN_QNAME_CONST) ) {
                    String annOpn = annPropValues.getProperty(GeneratorUtil.OPN_QNAME_CONST);
                    String annIntf = annPropValues.getProperty(GeneratorUtil.INTF_QNAME_CONST);
                    String annInMsgType = annPropValues.getProperty(GeneratorUtil.IN_MSGTYPE_QNAME_CONST);
                    // String intfName, String consIntfNs, String opnName, String inMessageTypeName

                    if  (annOpn != null && annIntf != null && annInMsgType != null &&
                           intfName != null && consIntfNs != null && opnName != null && inMessageTypeName != null ) {


                        QName opnQName = QName.valueOf(annOpn);
                        QName intfQName = QName.valueOf(annIntf);
                        QName inMsgQName = QName.valueOf(annInMsgType);
                        if ( opnQName.getLocalPart().equals(opnName) && intfQName.getLocalPart().equals(intfName)&& inMsgQName.getNamespaceURI().equals(consIntfNs) && inMsgQName.getLocalPart().equals(inMessageTypeName)) {

                            resourceName= memberAnnSetEntry.getKey();
                            break;
                        }
                    }
                }
            }
        }
        return resourceName;
    }
////////////

    private ModifiersTree handleModifiersAndAnnotations(TreeMaker make, WorkingCopy workingCopy,
            String annotationType, Map<String, Object> annotationArguments) throws Exception {
        Set<Modifier> modifierSet = EnumSet.of(Modifier.PRIVATE);
        List<ExpressionTree> annoArgList = new ArrayList<ExpressionTree> ();

        if ( annotationArguments != null)  {
           for (String key : annotationArguments.keySet()) {
                Object val = annotationArguments.get(key);
                ExpressionTree annoArg = GeneratorUtil.createAnnotationArgument(make, key, val);
                annoArgList.add(annoArg);
            }
        }
        AnnotationTree annotationTree = GeneratorUtil.createAnnotation(make, workingCopy,
                    annotationType, annoArgList);

        return make.Modifiers(modifierSet, Collections.singletonList(annotationTree));
    }

    private List<String> getSynchInvokeString(String serviceEndpoint) {
        List<String> listOfStatements = new ArrayList<String>();
        return listOfStatements;
    }
//////
private void appendStatementsToMethod(TreeMaker make, WorkingCopy workingCopy, TreeUtilities treeUtilities, MethodTree methodTree, String varName, String ctx) throws Exception {
        String statementToAdd =
                "{String inputMessage = \"\";\n}";
        BlockTree newBlockTree = treeUtilities.parseStaticBlock(statementToAdd, null);
        StatementTree statementTree = newBlockTree.getStatements().get(0);

        /*
        String comment1 = "For JMS Request-Reply pattern, user can optionally set";
        String comment2 = "CorrelationID on the Request Message, and then use";
        String comment3 = "Message Selector to selectively receive Reply Message with";
        String comment4 = "corresponding CorrelationID.  Don't forget on the Replier";
        String comment5 = "side to set CorrelationID on the Reply Message.";
        String comment6 = "For example:";
        String comment7 = this.requestMsgVariableName + ".setJMSCorrelationID(\"some CorrelationID\");";

        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment1), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment2), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment3), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment4), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment5), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment6), true);
        make.addComment(statementTree,
                Comment.create(Comment.Style.LINE, -2, -2, -2, comment7), true);
        */
        BlockTree oldBodyTree = methodTree.getBody();
        List<StatementTree> statementList = new ArrayList<StatementTree> ();
        //statementList.add(statementTree);
        Tree identTree = make.Identifier("");

        statementToAdd =getStatement( ctx, varName,this.mProps);
//                "{{String inputMessage = \"\";\n try {\nString outputMsg = (String)"+ctx+".sendSynchInOut("+varName+", inputMessage, org.glassfish.openesb.pojose.api.res.POJOContext.MessageObjectType.String);\n } catch (Exception ex) {\nex.printStackTrace();\n}}}";
        newBlockTree = treeUtilities.parseStaticBlock(statementToAdd, null);
        statementTree = newBlockTree.getStatements().get(0);

        make.addComment(statementTree,  Comment.create("Consumer Invoke - Begin"), true);//NOI18N
        make.addComment(statementTree,  Comment.create("Consumer Invoke - End"), false);//NOI18N
        statementList.add(statementTree);

        statementList.addAll(oldBodyTree.getStatements());
        BlockTree newBodyTree = make.Block(statementList, false);

        workingCopy.rewrite(oldBodyTree, newBodyTree);
    }

    @Override
    public Exception getException() {
        return myException;
    }

    String[] getImportList(Map map) {
        String[] returnArry = null;
        POJOSupportedDataTypes inPdt = (POJOSupportedDataTypes) map.get(GeneratorUtil.POJO_CONSUMER_INPUT_TYPE);
        POJOSupportedDataTypes outPdt = (POJOSupportedDataTypes) map.get(GeneratorUtil.POJO_CONSUMER_OUTPUT_TYPE);
        List<String> listOfImports = new ArrayList<String>();
        String imp = null;
        if ( inPdt != null) {
            imp =  inPdt.formatToString(inPdt, true);
            if (imp != null &&  !imp.equals("") && imp.contains(".")) {
                listOfImports.add(imp);
            }
        }
        if ( outPdt != null) {
            imp =  inPdt.formatToString(outPdt, false);
            if (imp != null &&  !imp.equals("") && imp.contains(".")) {
                listOfImports.add(imp);
            }
        }
        returnArry = listOfImports.toArray(new String[0]);
        return returnArry;
    }

    String getStatement(String ctx, String varName, Map map) {
        StringBuffer strBuff = new StringBuffer();

        POJOSupportedDataTypes inPdt = (POJOSupportedDataTypes) map.get(GeneratorUtil.POJO_CONSUMER_INPUT_TYPE);
        POJOSupportedDataTypes outPdt = (POJOSupportedDataTypes) map.get(GeneratorUtil.POJO_CONSUMER_OUTPUT_TYPE);
        boolean bMessageExchangeMode = false;
        if (outPdt == POJOSupportedDataTypes.Void) {
            outPdt = null;
        }
        if (inPdt == POJOSupportedDataTypes.Void) {
            inPdt = null;
        }

        Boolean isSynchronous = (Boolean) map.get(GeneratorUtil.POJO_CONSUMER_INVOKE_TYPE);
        String methodName = null;

        if (isSynchronous == Boolean.TRUE) {
            if (inPdt != null && inPdt.equals(POJOSupportedDataTypes.MessageExchange)) {
                methodName = "sendSynch";//NOI18N
                bMessageExchangeMode = true;
            }
            if (methodName == null) {
                if (outPdt == null) {
                    methodName = "sendSynchInOnly";//NOI18N
                } else {
                    methodName = "sendSynchInOut";//NOI18N
                }
            }

            String inputType = null;
            if (inPdt != null) {
                inputType = getDataType(inPdt);
            }
            String outType = null;
            if (outPdt != null) {
                outType = getDataType(outPdt);
            }

            strBuff.append("{\n{");//NOI18N
            if (inputType != null) {
                strBuff.append(inputType);
                strBuff.append(" inputMessage = null;\n");//NOI18N
            }
            strBuff.append("try {\n");//NOI18N
            if (outPdt != null) {
                strBuff.append(outType);
                strBuff.append(" outputMsg = "); //NOI18N
                strBuff.append(" ("); //NOI18N
                strBuff.append(outType);
                strBuff.append(") "); //NOI18N
            }

            strBuff.append(varName); //NOI18N
            //strBuff.append(ctx); //NOI18N
            strBuff.append("."); //NOI18N
            strBuff.append(methodName); //NOI18N
            strBuff.append("("); //NOI18N
            if (!bMessageExchangeMode) {
                //strBuff.append(varName); //NOI18N
                if (inPdt != null) {
                    strBuff.append(" inputMessage"); //NOI18N
                } else {
                    strBuff.append(" null"); //NOI18N
                }

                if (outPdt != null) {
                    strBuff.append(","); //NOI18N
                    strBuff.append(getPOJORuntimeDT(outPdt));
                }
            } else {
                strBuff.append("inputMessage"); //NOI18N
            }
            strBuff.append(");\n } catch (Exception ex) {\nex.printStackTrace();\n}");//NOI18N
            strBuff.append("}\n}");//NOI18N
        //Consumer Invoke - Begin
        } else {
            strBuff.append(" asynchronous ");
        }

        return strBuff.toString();
    }

    private String getPOJORuntimeDT(POJOSupportedDataTypes dt) {
        if ( dt == null ) {
            return null;
        }
        if ( dt.equals(POJOSupportedDataTypes.String)) {
            return "org.glassfish.openesb.pojose.api.Consumer.MessageObjectType.String";//NOI18N
        }
        if (dt.equals(POJOSupportedDataTypes.MessageExchange)) {
            return "org.glassfish.openesb.pojose.api.Consumer.MessageObjectType.String";//NOI18N
        }
        if ( dt.equals(POJOSupportedDataTypes.Source)) {
            return "org.glassfish.openesb.pojose.api.Consumer.MessageObjectType.Source";//NOI18N
        }
        if ( dt.equals(POJOSupportedDataTypes.Document)) {
            return "org.glassfish.openesb.pojose.api.Consumer.MessageObjectType.Document";//NOI18N
        }
        if ( dt.equals(POJOSupportedDataTypes.Node)) {
            return "org.glassfish.openesb.pojose.api.Consumer.MessageObjectType.Node";//NOI18N
        }
        return null;

    }

    private String getDataType(POJOSupportedDataTypes dt) {
        if ( dt == null ) {
            return null;
        }

        if ( dt.equals(POJOSupportedDataTypes.String)) {
            return "String";//NOI18N
        }
        if (dt.equals(POJOSupportedDataTypes.MessageExchange)) {
            return "MessageExchange";//NOI18N
        }
        if (dt.equals(POJOSupportedDataTypes.NormalizedMessage)) {
            return "NormalizedMessage";//NOI18N
        }

        if ( dt.equals(POJOSupportedDataTypes.Source)) {
            return "Source";//NOI18N
        }
        if ( dt.equals(POJOSupportedDataTypes.Node)) {
            return "Node";//NOI18N
        }
        if ( dt.equals(POJOSupportedDataTypes.Document)) {
            return "Document";//NOI18N
        }

        return null;
    }

}

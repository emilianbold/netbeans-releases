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
package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.awt.Component;
import java.awt.Graphics;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilterBuilder;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenProcessor;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.xref.CsmTemplateBasedReferencedObject;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletion.BaseType;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem.TemplateParameterResultItem;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.completion.csm.CompletionResolver;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.AntiLoop;
import org.netbeans.modules.cnd.modelutil.CsmPaintComponent;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 *
 * @author Vladimir Voskresensky
 * @version 1.00
 */
abstract public class CsmCompletionQuery {

    private BaseDocument baseDocument;
    private static final String NO_SUGGESTIONS = NbBundle.getMessage(CsmCompletionQuery.class, "completion-no-suggestions");
    private static final String PROJECT_BEEING_PARSED = NbBundle.getMessage(CsmCompletionQuery.class, "completion-project-beeing-parsed");
    private static final boolean TRACE_COMPLETION = Boolean.getBoolean("cnd.completion.trace");
    private static CsmItemFactory itemFactory;

    private static final int MAX_DEPTH = 15;

    // the only purpose of this method is that NbJavaCompletionQuery
    // can use it to retrieve baseDocument's fileobject and create correct
    // CompletionResolver with the correct classpath of project to which the file belongs
    protected BaseDocument getBaseDocument() {
        return baseDocument;
    }

    abstract protected CompletionResolver getCompletionResolver(boolean openingSource, boolean sort, boolean inIncludeDirective);

    abstract protected CsmFinder getFinder();

    abstract protected QueryScope getCompletionQueryScope();

    abstract protected FileReferencesContext getFileReferencesContext();

    public static enum QueryScope {

        LOCAL_QUERY,
        SMART_QUERY,
        GLOBAL_QUERY,
    };

    public CsmCompletionQuery() {
        super();
        initFactory();
    }

    protected void initFactory() {
        setCsmItemFactory(new CsmCompletionQuery.DefaultCsmItemFactory());
    }

    public CsmCompletionResult query(JTextComponent component, int offset, boolean instantiateTypes) {
        boolean sort = false; // TODO: review
        return query(component, offset, false, sort, instantiateTypes);
    }

    /**
     * Perform the query on the given component. The query usually
     * gets the component's baseDocument, the caret position and searches back
     * to find the last command start. Then it inspects the text up to the caret
     * position and returns the result.
     *
     * @param component the component to use in this query.
     * @param offset position in the component's baseDocument to which the query will
     *   be performed. Usually it's a caret position.
     * @param support syntax-support that will be used during resolving of the query.
     * @param openingSource whether the query is performed to open the source file.
     *  The query tries to return exact matches if this flag is true
     * @return result of the query or null if there's no result.
     */
    public CsmCompletionResult query(JTextComponent component, int offset,
            boolean openingSource, boolean sort, boolean instantiateTypes) {
        BaseDocument doc = (BaseDocument) component.getDocument();
        return query(component, doc, offset, openingSource, sort, instantiateTypes);
    }

    public static boolean checkCondition(final Document doc, final int dot, boolean takeLock) {
        if (!takeLock) {
            return _checkCondition(doc, dot);
        }
        final AtomicBoolean res = new AtomicBoolean(false);
        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).render(new Runnable() {
                public void run() {
                    res.set(_checkCondition(doc, dot));
                }
            });
        } else {
            res.set(_checkCondition(doc, dot));
        }
        return res.get();
    }

    private static boolean _checkCondition(Document doc, int dot) {
        return !CompletionSupport.isPreprocCompletionEnabled(doc, dot) && CompletionSupport.isCompletionEnabled(doc, dot);
    }

//    private boolean parseExpression(CsmCompletionTokenProcessor tp, TokenSequence<?> cppTokenSequence, int startOffset, int lastOffset) {
//        boolean processedToken = false;
//        while (cppTokenSequence.moveNext()) {
//            if (cppTokenSequence.offset() >= lastOffset) {
//                break;
//            }
//            Token<CppTokenId> token = (Token<CppTokenId>) cppTokenSequence.token();
//            if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
//                TokenSequence<?> embedded = cppTokenSequence.embedded();
//                if (cppTokenSequence.offset() < startOffset) {
//                    embedded.move(startOffset);
//                }
//                processedToken |= parseExpression(tp, embedded, startOffset, lastOffset);
//            } else {
//                processedToken = true;
//                tp.token(token, cppTokenSequence.offset());
//            }
//        }
//        return processedToken;
//    }
    public CsmCompletionResult query(JTextComponent component, final BaseDocument doc, final int offset,
            boolean openingSource, boolean sort, boolean instantiateTypes) {
        // remember baseDocument here. it is accessible by getBaseDocument() {

        // method for subclasses of JavaCompletionQuery, ie. NbJavaCompletionQuery
        baseDocument = doc;

        CsmCompletionResult ret = null;

        CompletionSupport sup = CompletionSupport.get(doc);
        if (sup == null || !checkCondition(doc, offset, true)) {
            return null;
        }

        try {
            // find last separator position
            final int lastSepOffset = sup.getLastCommandSeparator(offset);
            final CsmCompletionTokenProcessor tp = new CsmCompletionTokenProcessor(offset, lastSepOffset);
            final CndTokenProcessor<Token<CppTokenId>> etp = CsmExpandedTokenProcessor.create(doc, tp, offset);
            if(etp instanceof CsmExpandedTokenProcessor) {
                tp.setMacroCallback((CsmExpandedTokenProcessor)etp);
            }
            tp.enableTemplateSupport(true);
            doc.readLock();
            try {
                CndTokenUtilities.processTokens(etp, doc, lastSepOffset, offset);
            } finally {
                doc.readUnlock();
            }
            sup.setLastSeparatorOffset(tp.getLastSeparatorOffset());
//            boolean cont = true;
//            while (cont) {
//                sup.tokenizeText(tp, ((lastSepOffset < offset) ? lastSepOffset + 1 : offset), offset, true);
//                cont = tp.isStopped() && (lastSepOffset = sup.findMatchingBlock(tp.getCurrentOffest(), true)[0]) < offset - 1;
//            }

            // Check whether there's an erroneous token state under the cursor
            boolean errState = false;
            CppTokenId lastValidTokenID = tp.getLastValidTokenID();
            if (lastValidTokenID != null) {
                switch (lastValidTokenID) {
//                case STAR:
//                    errState = true;
//                    break;
                    case BLOCK_COMMENT:
                        if (tp.getLastValidTokenText() == null || !tp.getLastValidTokenText().endsWith("*/") // NOI18N
                                ) {
                            errState = true;
                        }
                        break;

                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                        errState = true;
                        break;
                    default:
                        if (CppTokenId.PREPROCESSOR_KEYWORD_CATEGORY.equals(lastValidTokenID.primaryCategory())) {
                            // this provider doesn't handle preprocessor tokens
                            errState = true;
                        } else {
                            errState = tp.isErrorState();
                        }
                }
            }

            if (!errState) {

                CsmCompletionExpression exp = tp.getResultExp();
                if (TRACE_COMPLETION) {
                    System.err.println("expression " + exp);
                }
                ret = getResult(component, doc, openingSource, offset, exp, sort, isInIncludeDirective(doc, offset), instantiateTypes);
            } else if (TRACE_COMPLETION) {
                System.err.println("Error expression " + tp.getResultExp());
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return ret;
    }

    abstract protected boolean isProjectBeeingParsed(boolean openingSource);

    private CsmCompletionResult getResult(JTextComponent component, Document doc, boolean openingSource, int offset, CsmCompletionExpression exp, boolean sort, boolean inIncludeDirective, boolean instantiateTypes) {
        CompletionResolver resolver = getCompletionResolver(openingSource, sort, inIncludeDirective);
        if (resolver != null) {
            CompletionSupport sup = CompletionSupport.get(doc);
            CsmOffsetableDeclaration context = sup.getDefinition(offset, getFileReferencesContext());
            Context ctx = new Context(component, sup, openingSource, offset, getFinder(), resolver, context, sort, instantiateTypes);
            ctx.resolveExp(exp);
            if (ctx.result != null) {
                ctx.result.setSimpleVariableExpression(isSimpleVariableExpression(exp));
            }
            if (TRACE_COMPLETION) {
                CompletionItem[] array = ctx.result == null ? new CompletionItem[0] : ctx.result.getItems().toArray(new CompletionItem[ctx.result.getItems().size()]);
                //Arrays.sort(array, CompletionItemComparator.BY_PRIORITY);
                System.err.println("Completion Items " + array.length);
                for (int i = 0; i < array.length; i++) {
                    CompletionItem completionItem = array[i];
                    System.err.println(completionItem.toString());
                }
            }
            return ctx.result;
        } else {
            boolean isProjectBeeingParsed = isProjectBeeingParsed(openingSource);
            return new CsmCompletionResult(component, getBaseDocument(), Collections.EMPTY_LIST, "", exp, 0, isProjectBeeingParsed, null, instantiateTypes);
        }
//	CsmCompletionResult result = null;
//
//	// prepare input values
//	String title = "*";
//	int cntM1 = exp.getTokenCount() - 1;
//	int substituteOffset = offset;
//	int substituteLength = 0;
//	String prefix = "";
//	boolean exactMatch = false;
//        int id = exp.getExpID();
//        // TODO: must be in resolver
//	if (cntM1 >= 0 &&
//                id != CsmCompletionExpression.NEW &&
//                id != CsmCompletionExpression.TYPE &&
//                id != CsmCompletionExpression.CASE &&
//                id != CsmCompletionExpression.DOT_OPEN &&
//                id != CsmCompletionExpression.ARROW_OPEN &&
//                id != CsmCompletionExpression.PARENTHESIS &&
//                id != CsmCompletionExpression.PARENTHESIS_OPEN) {
//	    substituteOffset = exp.getTokenOffset(cntM1);
//	    substituteLength = exp.getTokenLength(cntM1);
//	    title = formatName(exp.getTokenText(cntM1), true);
//	    prefix = exp.getTokenText(cntM1);
//	}
//        // prepare sorting
//        Class kitClass = Utilities.getKitClass(component);
//        boolean caseSensitive = isCaseSensitive(kitClass);
//        boolean naturalSort = isNaturalSort(kitClass);
//
//        int emptyOffset = exp.getTokenOffset(0);
//	// try to resolve
//	if (resolver != null && resolver.resolve(emptyOffset, prefix, exactMatch)) {
//	    List data = resolver.getResult();
//            if (data.size() == 0) {
//                title = NO_SUGGESTIONS;
//            }
//
//	    int classDisplayOffset = 0;
//	    result = new CsmCompletionResult(component, data,
//					    title, exp,
//					    substituteOffset, substituteLength,
//					    classDisplayOffset);
//	}
//	return result;
    }

    // ================= help methods to generate CsmCompletionResult ==========
    private String formatName(String name, boolean appendStar) {
        return (name != null) ? (appendStar ? (name + '*') : name)
                : (appendStar ? "*" : ""); // NOI18N
    }

    private String formatType(CsmType type, boolean useFullName, boolean appendColon) {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
//                sb.append(type.format(useFullName));
            sb.append(type.getText());
        }
        if (appendColon) {
            sb.append(CsmCompletion.SCOPE);
        }
        return sb.toString();
    }

    private static String formatType(CsmType type, boolean useFullName,
            boolean appendDblComma, boolean appendStar) {
        StringBuilder sb = new StringBuilder();
        if (type != null && type.getClassifier() != null) {
//                sb.append(type.format(useFullName));
            sb.append(useFullName ? type.getClassifier().getQualifiedName() : type.getClassifier().getName());
        }
        if (appendDblComma) {
            sb.append(CsmCompletion.SCOPE);
        }
        if (appendStar) {
            sb.append('*'); //NOI18N
        }
        return sb.toString();
    }

// commented out: isn't used any more (except for commented out code fragments)
//    private static String getNamespaceName(CsmClassifier classifier) {
//        CsmNamespace ns = null;
//        if (CsmKindUtilities.isClass(classifier)) {
//            ns = ((CsmClass)classifier).getContainingNamespace();
//        }
//        return ns != null ? ns.getQualifiedName() : ""; //NOI18N
//    }
    /** Finds the fields, methods and the inner classes.
     */
//    static List findFieldsAndMethods(JCFinder finder, String curPkg, CsmClass cls, String name,
//                                     boolean exactMatch, boolean staticOnly, boolean inspectParentClasses) {
//        // Find inner classes
//        List ret = new ArrayList();
    // [TODO]
//        if (staticOnly) {
//            JCPackage pkg = finder.getExactPackage(cls.getPackageName());
//            if (pkg != null) {
//                ret = finder.findClasses(pkg, cls.getName() + '.' + name, false);
//            }
//        }
//
//        // XXX: this is hack, we should rather create JCFinder2 iface with findFields,
//        // findMethods methods accepting current package parameter
//        if (finder instanceof JCBaseFinder) {
//            // Add fields
//            ret.addAll(((JCBaseFinder)finder).findFields(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
//            // Add methods
//            ret.addAll(((JCBaseFinder)finder).findMethods(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
//        } else {
//            // Add fields
//            ret.addAll(finder.findFields(cls, name, exactMatch, staticOnly, inspectParentClasses));
//            // Add methods
//            ret.addAll(finder.findMethods(cls, name, exactMatch, staticOnly, inspectParentClasses));
//        }
//
//        return ret;
//    }
    /** Finds the fields, methods and the inner classes.
     */
//    static List findFieldsAndMethods(CsmFinder finder, String curNamespace, CsmClassifier classifier, String name,
//                                     boolean exactMatch, boolean staticOnly, boolean inspectParentClasses) {
//        // Find inner classes
//        List ret = new ArrayList();
//        if (!CsmKindUtilities.isClass(classifier)) {
//            return ret;
//        }
//        CsmClass cls = (CsmClass)classifier;
//        if (staticOnly) {
////            CsmNamespace pkg = finder.getExactNamespace(getNamespaceName(cls));
//            CsmNamespace ns = cls.getContainingNamespace();
//            if (ns != null) {
//                ret = finder.findClasses(ns, cls.getName() + '.' + name, false);
//            }
//        }
//
//        // XXX: this is hack, we should rather create JCFinder2 iface with findFields,
//        // findMethods methods accepting current package parameter
////        if (finder instanceof JCBaseFinder) {
////            // Add fields
////            ret.addAll(((JCBaseFinder)finder).findFields(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
////            // Add methods
////            ret.addAll(((JCBaseFinder)finder).findMethods(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
////        } else {
//            // Add fields
//            List res = finder.findFields(cls, name, exactMatch, staticOnly, inspectParentClasses);
//            if (res != null) {
//                ret.addAll(res);
//            }
//            // Add methods
//            res = finder.findMethods(cls, name, exactMatch, staticOnly, inspectParentClasses);
//            if (res != null) {
//                ret.addAll(res);
//            }
////        }
//
//        return ret;
//    }
    static List<CsmClassifier> findNestedClassifiers(CsmFinder finder, CsmOffsetableDeclaration context, CsmClassifier classifier, String name,
            boolean exactMatch, boolean inspectParentClasses, boolean sort) {
        // Find inner classes
        List<CsmClassifier> ret = new ArrayList<CsmClassifier>();
        classifier = CsmBaseUtilities.getOriginalClassifier(classifier, finder.getCsmFile());
        if (!CsmKindUtilities.isClass(classifier)) {
            return ret;
        }
        CsmClass cls = (CsmClass) classifier;

        // Add fields
        List<CsmClassifier> res = finder.findNestedClassifiers(context, cls, name, exactMatch, inspectParentClasses, sort);
        if (res != null) {
            ret.addAll(res);
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    static List<CsmObject> findFieldsAndMethods(CsmFinder finder, CsmOffsetableDeclaration context, CsmClassifier classifier, String name,
            boolean exactMatch, boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses, boolean scopeAccessedClassifier, boolean skipConstructors, boolean sort) {
        // Find inner classes
        List ret = new ArrayList();
        classifier = CsmBaseUtilities.getOriginalClassifier(classifier, finder.getCsmFile());
        if (!CsmKindUtilities.isClass(classifier)) {
            return ret;
        }
        CsmClass cls = (CsmClass) classifier;
        CsmFunction contextFunction = CsmBaseUtilities.getContextFunction(context);
        CsmClass contextClass = CsmBaseUtilities.getContextClass(context);
//        if (staticOnly) {
////            CsmNamespace pkg = finder.getExactNamespace(getNamespaceName(cls));
//            CsmNamespace ns = cls.getContainingNamespace();
//            if (ns != null) {
//                ret = finder.findClasses(ns, cls.getName() + '.' + name, false);
//            }
//        }
        if (CsmInheritanceUtilities.isAssignableFrom(contextClass, cls)) {
            staticOnly = false;
        }
        // Add fields
        List res = finder.findFields(context, cls, name, exactMatch, staticOnly, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier, sort);
        if (res != null) {
            ret.addAll(res);
        }
        // add enumerators
        res = finder.findEnumerators(context, cls, name, exactMatch, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier, sort);
        if (res != null) {
            ret.addAll(res);
        }

        // in global context add all methods, but only direct ones
        if (contextFunction == null) {
            staticOnly = false;
            context = cls;
        }
        // Add methods
        res = finder.findMethods(context, cls, name, exactMatch, staticOnly, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier, sort);
        if (res != null) {
            if (!skipConstructors) {
                ret.addAll(res);
            } else {
                // add all but skip constructors
                for (Object mtd : res) {
                    if (!CsmKindUtilities.isConstructor(((CsmObject) mtd))) {
                        ret.add(mtd);
                    }
                }
            }
        }
        return ret;
    }

    /** Finds the fields, methods and the inner classes.
     */
//    static List findFields(CsmFinder finder, CsmContext context, CsmClassifier classifier, String name,
//                                     boolean exactMatch, boolean staticOnly, boolean inspectParentClasses) {
//        // Find inner classes
//        List ret = new ArrayList();
//        CsmClass cls = null;
//        if (CsmKindUtilities.isClass(classifier)) {
//            cls = (CsmClass)classifier;
//        }
//
//        // XXX: this is hack, we should rather create JCFinder2 iface with findFields,
//        // findMethods methods accepting current package parameter
////        if (finder instanceof JCBaseFinder) {
////            // Add fields
////            ret.addAll(((JCBaseFinder)finder).findFields(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
////            // Add methods
////            ret.addAll(((JCBaseFinder)finder).findMethods(curPkg, cls, name, exactMatch, staticOnly, inspectParentClasses));
////        } else {
//            // Add fields
//            List res = finder.findFields(cls, name, exactMatch, staticOnly, inspectParentClasses);
//            if (res != null) {
//                ret.addAll(res);
//            }
//            // Add methods
//            res = finder.findMethods(cls, name, exactMatch, staticOnly, inspectParentClasses);
//            if (res != null) {
//                ret.addAll(res);
//            }
////        }
//
//        return ret;
//    }
    static enum ExprKind {

        NONE, SCOPE, ARROW, DOT
    }

    private static CsmClassifier getClassifier(CsmType type, CsmFile contextFile) {
//        if (type instanceof CsmCompletion.BaseType || type instanceof CsmCompletion.OffsetableType) {
//            new Exception(type.getClass().getName() + type).printStackTrace();
//        }
//        boolean resolveTypeChain = true;
        CsmClassifier cls = CsmBaseUtilities.getClassifier(type, contextFile, true);
        return cls;
    }

    private static CsmFunction getOperator(CsmClassifier classifier, CsmFile contextFile, CsmFunction.OperatorKind opKind) {
        if (!CsmKindUtilities.isClass(classifier)) {
            return null;
        }
        CsmClass cls = (CsmClass) classifier;
        CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter("operator " + opKind.getImage(), false, true, false); // NOI18N
        return getOperatorCheckBaseClasses(cls, contextFile, filter, opKind, new AntiLoop());
    }

    private static CsmFunction getOperatorCheckBaseClasses(CsmClass cls, CsmFile contextFile, CsmFilter filter, CsmFunction.OperatorKind opKind, AntiLoop antiLoop) {
        if (antiLoop.contains(cls)) {
            return null;
        }
        antiLoop.add(cls);
        Iterator<CsmMember> it = CsmSelect.getClassMembers(cls, filter);
        while (it.hasNext()) {
            CsmMember member = it.next();
            if (CsmKindUtilities.isOperator(member)) {
                if (((CsmFunction) member).getOperatorKind() == opKind) {
                    return (CsmFunction) member;
                }
            }
        }
        // now check base classes as well
        for (CsmInheritance csmInheritance : cls.getBaseClasses()) {
            CsmClassifier baseClassifier = getClassifier(csmInheritance.getAncestorType(), contextFile);
            if (CsmKindUtilities.isClass(baseClassifier)) {
                CsmFunction operatorFun = getOperatorCheckBaseClasses((CsmClass) baseClassifier, contextFile, filter, opKind, antiLoop);
                if (operatorFun != null) {
                    return operatorFun;
                }
            }
        }
        return null;
    }

    private static CsmType getOverloadedOperatorReturnType(CsmType type, CsmFile contextFile, CsmFunction.OperatorKind operator, int level) {
        if (type == null || type.isPointer() || type.getArrayDepth() > 0) {
            return null;
        }
        CsmType opType = null;
        CsmClassifier cls = getClassifier(type, contextFile);
        if (CsmKindUtilities.isClass(cls)) {
            CsmFunction op = CsmCompletionQuery.getOperator((CsmClass) cls, contextFile, operator);
            if (op != null) {
                opType = op.getReturnType();
                if ((!type.equals(opType)) && (level > 0)) {
                    if (operator == CsmFunction.OperatorKind.ARROW) {
                        // recursion only for ->
                        CsmType opType2 = getOverloadedOperatorReturnType(opType, contextFile, operator, level - 1);
                        if (opType2 != null) {
                            opType = opType2;
                        }
                    }
                } else {
                    CsmFile typeFile = type.getContainingFile();
                    System.err.printf("circular pointer delegation detected:%s, line %d/n", (typeFile != null ? typeFile.getAbsolutePath() : type), type.getStartOffset());//NOI18N
                    CndUtils.assertTrueInConsole(false, "Infinite recursion in file " + typeFile + " type " + type); //NOI18N
                }
            }
        }
        return opType;
    }

    private boolean isInIncludeDirective(BaseDocument doc, int offset) {
        if (true) {
            return false;
        }
        if (doc == null) {
            return false;
        }
        TokenSequence<CppTokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, false, false);
        if (cppTokenSequence == null) {
            return false;
        }
        boolean inIncludeDirective = false;
        if (cppTokenSequence.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            @SuppressWarnings("unchecked")
            TokenSequence<CppTokenId> embedded = (TokenSequence<CppTokenId>) cppTokenSequence.embedded();
            if (CndTokenUtilities.moveToPreprocKeyword(embedded)) {
                switch (embedded.token().id()) {
                    case PREPROCESSOR_INCLUDE:
                    case PREPROCESSOR_INCLUDE_NEXT:
                        inIncludeDirective = true;
                }
            }
        }
        return inIncludeDirective;
    }

    class Context {

        private boolean sort;
        /** Text component */
        private JTextComponent component;
        /**
         * Syntax support for the given baseDocument
         */
        private CompletionSupport sup;
        /** Whether the query is performed to open the source file. It has slightly
         * different handling in some situations.
         */
        private boolean openingSource;
        /** End position of the scanning - usually the caret position */
        private int endOffset;
        /** If set to true true - find the type of the result expression.
         * It's stored in the lastType variable or lastNamespace if it's a namespace.
         * The result variable is not populated.
         * False means that the code completion output should be collected.
         */
        private boolean findType;
        /** Whether currently scanning either the package or the class name
         * so the results should limit the search to the static fields and methods.
         */
        private boolean staticOnly = false;
        private boolean memberPointer = false;
        /**
         * stores information where there is class or variable was resolved
         */
        private boolean scopeAccessedClassifier = false;
        /** Last package found when scanning dot expression */
        private CsmNamespace lastNamespace;
        /** Last type found when scanning dot expression */
        private CsmType lastType;
        /** Result list when code completion output is generated */
        private CsmCompletionResult result;
        /** Helper flag for recognizing constructors */
        private boolean isConstructor;
        /** Finder associated with this Context. */
        /** Finder associated with this Context. */
        private final CsmFinder finder;
        private final CsmFile contextFile;
        /** Completion resolver associated with this Context. */
        private CompletionResolver compResolver;
        /** function or class in context */
        private CsmOffsetableDeclaration contextElement;
        private final boolean instantiateTypes;

        public Context(JTextComponent component,
                CompletionSupport sup, boolean openingSource, int endOffset,
                CsmFinder finder,
                CompletionResolver compResolver, CsmOffsetableDeclaration contextElement, boolean sort, boolean instantiateTypes) {
            this.component = component;
            this.sup = sup;
            this.openingSource = openingSource;
            this.endOffset = endOffset;
            this.finder = finder;
            this.contextFile = finder == null ? null : finder.getCsmFile();
            this.compResolver = compResolver;
            this.contextElement = contextElement;
            this.sort = sort;
            this.instantiateTypes = instantiateTypes;
        }

        private int convertOffset(int pos) {
            return sup.doc2context(pos);
        }

        private boolean resolve(int varPos, String var, boolean match) {
            varPos = convertOffset(varPos);
            return (compResolver.refresh() && compResolver.resolve(varPos, var, match));
        }

        private void setFindType(boolean findType) {
            this.findType = findType;
        }

        @Override
        protected Object clone() {
            return new Context(component, sup, openingSource, endOffset, finder, compResolver, contextElement, sort, instantiateTypes);
        }

        private CsmClassifier extractLastTypeClassifier(ExprKind expKind) {
            // Found type
            CsmClassifier cls;
            if (lastType.getArrayDepth() == 0 || (expKind == ExprKind.ARROW)) {
                // Not array or deref array with arrow
                cls = getClassifier(lastType, contextFile);
            } else {
                // Array of some depth
                cls = CsmCompletion.OBJECT_CLASS_ARRAY; // Use Object in this case
            }
            return cls;
        }

        private Collection<CsmFunction> getConstructors(CsmClass cls) {
            Collection<CsmFunction> out = new ArrayList<CsmFunction>();
            CsmFilterBuilder filterBuilder = CsmSelect.getFilterBuilder();
            CsmSelect.CsmFilter filter = filterBuilder.createCompoundFilter(
                    filterBuilder.createKindFilter(CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION),
                    filterBuilder.createNameFilter(cls.getName(), true, true, false));
            Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, filter);
            while (classMembers.hasNext()) {
                CsmMember csmMember = classMembers.next();
                if (CsmKindUtilities.isConstructor(csmMember)) {
                    out.add((CsmConstructor)csmMember);
                }
            }
            return out;
        }

        private CsmType extractFunctionType(Collection<CsmFunction> mtdList, CsmCompletionExpression genericNameExp) {
            CsmType out = null;
            if (mtdList.isEmpty()) {
                return null;
            }
            for (CsmFunction fun : mtdList) {
                if (genericNameExp != null && CsmKindUtilities.isTemplate(fun)) {
                    CsmObject inst = createInstantiation((CsmTemplate) fun, genericNameExp);
                    if (CsmKindUtilities.isFunction(inst)) {
                        fun = (CsmFunction) inst;
                    }
                }
                if (CsmKindUtilities.isConstructor(fun)) {
                    CsmClassifier cls = ((CsmConstructor) fun).getContainingClass();
                    out = CsmCompletion.getType(cls, 0, false, 0, false);
                } else {
                    out = fun.getReturnType();
                }
                if (out != null) {
                    break;
                }
            }
            return out;
        }

        /*private CsmClassifier resolveTemplateParameter(CsmClassifier cls, CsmType type) {
        if (cls instanceof CsmClassifierBasedTemplateParameter) {
        CsmClassifierBasedTemplateParameter tp = (CsmClassifierBasedTemplateParameter) cls;
        String n = tp.getName().toString();
        CsmScope container = tp.getScope();
        if (CsmKindUtilities.isTemplate(container)) {
        CsmTemplate template = (CsmTemplate) container;
        List<CsmTemplateParameter> formal = template.getTemplateParameters();
        List<CsmType> fact = type.getInstantiationParams();
        for (int i = 0; i < fact.size() && i < formal.size(); i++) {
        CsmTemplateParameter formalParameter = formal.get(i);
        CsmType factParameter = fact.get(i);
        String name = formalParameter.getName().toString();
        if (name.equals(n)) {
        return factParameter.getClassifier();
        }
        }
        }
        }
        return cls;
        }*/
        private CsmType resolveType(CsmCompletionExpression exp) {
            CsmType typ = exp.getCachedType();
            if (typ == null) {
                Context ctx = (Context) clone();
                ctx.setFindType(true);
                // when resolve type use full scope of search
                QueryScope old = ctx.compResolver.setResolveScope(QueryScope.GLOBAL_QUERY);
                try {
                    if (ctx.resolveExp(exp)) {
                        typ = ctx.lastType;
                    }
                } finally {
                    exp.cacheType(typ);
                    // restore old
                    ctx.compResolver.setResolveScope(old);
                }
            }
            return typ;
        }

        private boolean isProjectBeeingParsed() {
            return CsmCompletionQuery.this.isProjectBeeingParsed(openingSource);
        }

        private ExprKind extractKind(CsmCompletionExpression exp, int i, int startIdx, boolean lastDot, boolean reset) {
            ExprKind kind = ExprKind.NONE;
            int tokCount = exp.getTokenCount();
            if (i == startIdx) {
                kind = ExprKind.NONE;
            } else if (i - 1 < tokCount) {
                switch (exp.getTokenID(i - 1)) {
                    case ARROW:
                        kind = ExprKind.ARROW;
                        if (reset) {
                            scopeAccessedClassifier = false;
                        }
                        break;
                    case DOT:
                        kind = ExprKind.DOT;
                        if (reset) {
                            scopeAccessedClassifier = false;
                        }
                        break;
                    case SCOPE:
                        kind = ExprKind.SCOPE;
                        if (reset) {
                            scopeAccessedClassifier = true;
                        }
                        break;
                    default:
                        System.err.println("unexpected token " + exp.getTokenID(i));
                }
            } else if (lastDot) {
                switch (exp.getExpID()) {
                    case CsmCompletionExpression.ARROW:
                    case CsmCompletionExpression.ARROW_OPEN:
                        kind = ExprKind.ARROW;
                        if (reset) {
                            scopeAccessedClassifier = false;
                        }
                        break;
                    case CsmCompletionExpression.DOT:
                    case CsmCompletionExpression.DOT_OPEN:
                        kind = ExprKind.DOT;
                        if (reset) {
                            scopeAccessedClassifier = false;
                        }
                        break;
                    case CsmCompletionExpression.SCOPE:
                    case CsmCompletionExpression.SCOPE_OPEN:
                        kind = ExprKind.SCOPE;
                        if (reset) {
                            scopeAccessedClassifier = true;
                        }
                        break;
                    default:
                        System.err.println("unexpected expression" + exp);
                }
            }
            return kind;
        }

        private boolean resolveParams(CsmCompletionExpression exp, boolean lastDot, /*out*/ ExprKind[] lastKind) {
            boolean ok = true;
            int parmCnt = exp.getParameterCount(); // Number of items in the dot exp
            // Fix for IZ#139143 : unresolved identifiers in "(*cur.object).*cur.creator"
            // Resolving should start after the last "->*" or ".*".
            int startIdx = 0;
            int tokCount = exp.getTokenCount();
            for (int i = tokCount - 1; 0 <= i; --i) {
                CppTokenId token = exp.getTokenID(i);
                if (token == CppTokenId.DOTMBR || token == CppTokenId.ARROWMBR) {
                    startIdx = i + 1;
                    break;
                }
            }
            ExprKind kind = ExprKind.NONE;
            ExprKind nextKind = ExprKind.NONE;
            int lastInd = parmCnt - 1;
            AtomicBoolean derefOfTHIS = new AtomicBoolean(false);
            for (int i = startIdx; i < parmCnt && ok; i++) { // resolve all items in exp
                kind = extractKind(exp, i, startIdx, lastDot, true);
                nextKind = extractKind(exp, i + 1, startIdx, lastDot, false);
                /*resolve arrows*/
                if ((kind == ExprKind.ARROW && !derefOfTHIS.get()) && (i != startIdx) && (i < parmCnt || lastDot || findType) && (lastType != null) && (lastType.getArrayDepth() == 0)) {
                    CsmType opType = getOverloadedOperatorReturnType(lastType, contextFile, CsmFunction.OperatorKind.ARROW, MAX_DEPTH);
                    if (opType != null) {
                        lastType = opType;
                    }
                }
                derefOfTHIS.set(false);
                ok = resolveItem(exp.getParameter(i), (i == startIdx),
                        (!lastDot && i == lastInd),
                        kind, nextKind, derefOfTHIS);
            }
            if (ok && lastDot) {
                kind = extractKind(exp, tokCount + 1, startIdx, true, true);
                /*resolve arrows*/
                if ((kind == ExprKind.ARROW && !derefOfTHIS.get()) && (lastDot || findType) && (lastType != null) && (lastType.getArrayDepth() == 0)) {
                    CsmType opType = getOverloadedOperatorReturnType(lastType, contextFile, CsmFunction.OperatorKind.ARROW, MAX_DEPTH);
                    if (opType != null) {
                        lastType = opType;
                    }
                }
            }
            lastKind[0] = kind;
            return ok;
        }

        @SuppressWarnings({"fallthrough", "unchecked"})
        boolean resolveExp(CsmCompletionExpression exp) {
            boolean lastDot = false; // dot at the end of the whole expression?
            boolean ok = true;

            switch (exp.getExpID()) {
                case CsmCompletionExpression.DOT_OPEN: // Dot expression with the dot at the end
                case CsmCompletionExpression.ARROW_OPEN: // Arrow expression with the arrow at the end
                    lastDot = true;
                // let it flow to DOT
                // nobreak
                case CsmCompletionExpression.DOT: // Dot expression
                case CsmCompletionExpression.ARROW: // Arrow expression
                    ExprKind lastParamKind[] = new ExprKind[]{ExprKind.NONE};
                    ok = resolveParams(exp, lastDot, lastParamKind);

                    if (ok && lastDot) { // Found either type or package help
                        // Need to process dot at the end of the expression
                        int tokenCntM1 = exp.getTokenCount() - 1;
                        int substPos = exp.getTokenOffset(tokenCntM1) + exp.getTokenLength(tokenCntM1);
                        if (lastType != null) { // Found type
                            CsmClassifier cls = extractLastTypeClassifier(lastParamKind[0]);
                            List<CsmObject> res;
                            if (openingSource) {
                                res = new ArrayList<CsmObject>();
                                res.add(lastType.getClassifier());
                            } else { // not source-help
                                res = findFieldsAndMethods(finder, contextElement, cls, "", false, staticOnly && !memberPointer, false, true, this.scopeAccessedClassifier, true, sort); // NOI18N
                            }
                            // Get all fields and methods of the cls
                            result = new CsmCompletionResult(component, getBaseDocument(), res, formatType(lastType, true, true, true),
                                    exp, substPos, 0, cls.getName().length() + 1, isProjectBeeingParsed(), contextElement, instantiateTypes);
                        } else { // Found namespace (otherwise ok would be false)
                            if (true) {
                                // in C++ it's not legal to have NS-> or NS.
                                result = null;
                                break;
                            }
                            String searchPkg = (lastNamespace.isGlobal() ? "" : lastNamespace.getQualifiedName()) + CsmCompletion.SCOPE;
                            List res;
                            if (openingSource) {
                                res = new ArrayList();
                                res.add(lastNamespace); // return only the package
                            } else {
                                res = finder.findNestedNamespaces(lastNamespace, "", false, false); // find all nested namespaces
                            }
                            result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*',
                                    exp, substPos, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                        }
                    }
                    break;

                case CsmCompletionExpression.SCOPE_OPEN: // Scope expression with the arrow at the end
                    lastDot = true;
                // let it flow to SCOPE
                // nobreak
                case CsmCompletionExpression.SCOPE: // Scope expression
                    staticOnly = true;
                    lastParamKind = new ExprKind[]{ExprKind.NONE};
                    ok = resolveParams(exp, lastDot, lastParamKind);

                    if (ok && lastDot) { // Found either type or namespace help
                        // Need to process dot at the end of the expression
                        int tokenCntM1 = exp.getTokenCount() - 1;
                        int substPos = exp.getTokenOffset(tokenCntM1) + exp.getTokenLength(tokenCntM1);
                        if (lastType != null) { // Found type
                            CsmClassifier cls = extractLastTypeClassifier(ExprKind.SCOPE);
                            List res;
                            if (openingSource) {
                                res = new ArrayList();
                                res.add(lastType.getClassifier());
                            } else { // not source-help
//                            CsmClass curCls = sup.getClass(exp.getTokenOffset(tokenCntM1));
//                            res = findFieldsAndMethods(finder, curCls == null ? null : getNamespaceName(curCls),
//                                    cls, "", false, staticOnly, false); // NOI18N
                                res = findFieldsAndMethods(finder, contextElement, cls, "", false, staticOnly && !memberPointer, false, true, this.scopeAccessedClassifier, false, sort); // NOI18N
                                List nestedClassifiers = findNestedClassifiers(finder, contextElement, cls, "", false, true, sort);
                                res.addAll(nestedClassifiers);
                            }
                            // Get all fields and methods of the cls
                            result = new CsmCompletionResult(component, getBaseDocument(), res, formatType(lastType, true, true, true),
                                    exp, substPos, 0, 0/*cls.getName().length() + 1*/, isProjectBeeingParsed(), contextElement, instantiateTypes);
                        } else { // Found package (otherwise ok would be false)
                            String searchPkg = (lastNamespace.isGlobal() ? "" : lastNamespace.getQualifiedName()) + CsmCompletion.SCOPE;
                            List res;
                            if (openingSource) {
                                res = new ArrayList();
                                res.add(lastNamespace); // return only the package
                            } else {
                                res = finder.findNestedNamespaces(lastNamespace, "", false, false); // find all nested namespaces

                                String text = null;
                                try {
                                    int firstTokenIdx = exp.getTokenOffset(0);
                                    int cmdStartIdx = sup.getLastCommandSeparator(firstTokenIdx);
                                    if (cmdStartIdx >= 0) {
                                        text = sup.getDocument().getText(cmdStartIdx, firstTokenIdx - cmdStartIdx);
                                    }
                                } catch (BadLocationException e) {
                                    // ignore and provide full list of items
                                }

                                // if not "using namespace" or "namespace A = " then add elements
                                if (text != null && -1 == text.indexOf("namespace")) { //NOI18N
                                    res.addAll(finder.findNamespaceElements(lastNamespace, "", false, false, false)); // namespace elements //NOI18N
                                }
                            }
                            result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*', //NOI18N
                                    exp, substPos, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                        }
                    }
                    break;

                case CsmCompletionExpression.NEW: // 'new' keyword
                {
                    List res = finder.findClasses(null, "", false, false); // Find all classes by name // NOI18N
                    result = new CsmCompletionResult(component, getBaseDocument(), res, "*", exp, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                    break;
                }

                case CsmCompletionExpression.LABEL: {
                    String name = exp.getParameter(0).getTokenText(0);
                    List res = finder.findLabel(contextElement, name, false, false);
                    result = new CsmCompletionResult(component, getBaseDocument(), res, "*", exp, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                    break;
                }

                case CsmCompletionExpression.CASE:
                    // TODO: check with NbJavaJMICompletionQuery
                    // FIXUP: now just analyze expression after "case "
                    exp = exp.getParameter(0);
                // nobreak
                default: // The rest of the situations is resolved as a singleton item
                    AtomicBoolean derefOfTHIS = new AtomicBoolean(false);
                    ok = resolveItem(exp, true, true, ExprKind.NONE, ExprKind.NONE, derefOfTHIS);
                    break;
            }

            return ok;
        }

        private CsmType getPredefinedType(CsmCompletionExpression item) {
            CsmFile containingFile = getFinder().getCsmFile();
            int startOffset = item.getTokenOffset(0);
            int lastInd = item.getTokenCount() - 1;
            int endtOffset = item.getTokenOffset(lastInd) + item.getTokenLength(lastInd);
            return CsmCompletion.getPredefinedType(containingFile, startOffset, endtOffset, item.getType());
        }
        /** Resolve one item from the expression connected by dots.
         * @param item expression item to resolve
         * @param first whether this expression is the first one in a dot expression
         * @param last whether this expression is the last one in a dot expression
         */
        @SuppressWarnings({"fallthrough", "unchecked"})
        boolean resolveItem(CsmCompletionExpression item, boolean first, boolean last, ExprKind kind, ExprKind nextKind, AtomicBoolean derefOfThisOUT) {
            boolean cont = true; // whether parsing should continue or not
            boolean methodOpen = false; // helper flag for unclosed methods
            boolean skipConstructors = (kind != ExprKind.NONE && kind != ExprKind.SCOPE);
            switch (item.getExpID()) {
                case CsmCompletionExpression.CONSTANT: // Constant item
                    if (first) {
                        lastType = getPredefinedType(item); // Get the constant type
                        staticOnly = false;
                    } else { // Not the first item in a dot exp
                        cont = false; // impossible to have constant inside the expression
                    }
                    break;

                case CsmCompletionExpression.VARIABLE: // Variable or special keywords
                    switch (item.getTokenID(0)) {
                        case THIS: // 'this' keyword
                            if (first) { // first item in expression
                                CsmClass cls = sup.getClass(item.getTokenOffset(0));
                                if (cls != null) {
                                    derefOfThisOUT.set(true);
                                    lastType = CsmCompletion.getType(cls, 0, false, 0, false);
                                    staticOnly = false;
                                }
                            } else { // 'something.this'
                                staticOnly = false;
                            }
                            break;

//                    case CLASS: // 'class' keyword
//                        if (!first) {
//                            lastType = CsmCompletion.CLASS_TYPE;
//                            staticOnly = false;
//                        } else {
//                            cont = false;
//                        }
//                        break;

                        default: // Regular constant
                            String var = item.getTokenText(0);
                            int varPos = item.getTokenOffset(0) + item.getTokenLength(0);
                            if (first) { // try to find variable for the first item
                                if (last && !findType) { // both first and last item
                                    CompletionResolver.Result res = null;
                                    if (isConstructor) {
                                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_CLASSES |
                                                CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS |
                                                CompletionResolver.RESOLVE_GLOB_NAMESPACES |
                                                CompletionResolver.RESOLVE_LIB_NAMESPACES |
                                                CompletionResolver.RESOLVE_LIB_CLASSES |
                                                CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS |
                                                CompletionResolver.RESOLVE_LOCAL_CLASSES);
                                    } else {
                                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                                    }
                                    if (resolve(varPos, var, openingSource)) {
                                        res = compResolver.getResult();
                                    }
                                    result = new CsmCompletionResult(component, getBaseDocument(), res, var + '*', item, item.getTokenOffset(0), item.getTokenLength(0), 0, isProjectBeeingParsed(), contextElement, instantiateTypes);  //NOI18N
                                } else { // not last item or finding type
                                    // find type of variable
                                    if (nextKind != ExprKind.SCOPE) {
                                        if (first && !findType) {
                                            lastType = findExactVarType(var, varPos);
                                        }
                                        if (lastType == null) {
                                            // try to find with resolver
                                            CompletionResolver.Result res = null;
                                            compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                                            if (resolve(varPos, var, true)) {
                                                res = compResolver.getResult();
                                                List<? extends CsmObject> vars = new ArrayList<CsmObject>();
                                                res.addResulItemsToCol(vars);
                                                for (CsmObject firstElem : vars) {
                                                    if (CsmKindUtilities.isVariable(firstElem)) {
                                                        CsmVariable varElem = (CsmVariable) firstElem;
                                                        lastType = varElem.getType();
                                                    } else if (findType && CsmKindUtilities.isClassifier(firstElem)) {
                                                        lastType = CsmCompletion.getType((CsmClassifier) firstElem, 0, false, 0, false);
                                                    }
                                                    // stop on the first
                                                    if (lastType != null) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (lastType != null) { // variable found
                                        staticOnly = false;
                                    } else { // no variable found
//                                    scopeAccessedClassifier = (kind == ExprKind.SCOPE);
                                        if (var.length() == 0) {
                                            lastNamespace = finder.getCsmFile().getProject().getGlobalNamespace();
                                        } else {
                                            compResolver.setResolveTypes(
                                                    CompletionResolver.RESOLVE_CLASSES |
                                                    CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS |
                                                    CompletionResolver.RESOLVE_LIB_CLASSES |
                                                    CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS |
                                                    CompletionResolver.RESOLVE_LOCAL_CLASSES);
                                            boolean foundVisibleClassifier = false;
                                            if (resolve(varPos, var, true)) {
                                                Collection<? extends CsmObject> res = compResolver.getResult().addResulItemsToCol(new ArrayList<CsmObject>());
                                                if (!res.isEmpty()) {
                                                    lastType = null;
                                                    CsmObject classifierCandidate = null;
                                                    for (CsmObject obj : res) {
                                                        if (lastType == null) {
                                                            if (CsmKindUtilities.isClassifier(obj)) {
                                                                // remember the first
                                                                if (classifierCandidate == null) {
                                                                    classifierCandidate = obj;
                                                                }
                                                                // prefer visible classifier
                                                                if (CsmIncludeResolver.getDefault().isObjectVisible(contextFile, obj)) {
                                                                    lastType = CsmCompletion.getType((CsmClassifier) obj, 0, false, 0, false);
                                                                    foundVisibleClassifier = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (lastType == null && classifierCandidate != null) {
                                                        lastType = CsmCompletion.getType((CsmClassifier) classifierCandidate, 0, false, 0, false);
                                                    }
                                                }
                                            }
                                            if (!foundVisibleClassifier) {
                                                QueryScope old = compResolver.setResolveScope(QueryScope.GLOBAL_QUERY);
                                                compResolver.setResolveTypes(
                                                        CompletionResolver.RESOLVE_GLOB_NAMESPACES |
                                                        CompletionResolver.RESOLVE_LIB_NAMESPACES);
                                                if (resolve(varPos, var, true)) {
                                                    Collection<? extends CsmObject> res = compResolver.getResult().addResulItemsToCol(new ArrayList<CsmObject>());
                                                    if (!res.isEmpty()) {
                                                        CsmObject ns = res.iterator().next();
                                                        if (CsmKindUtilities.isNamespaceAlias(ns)) {
                                                            lastNamespace = ((CsmNamespaceAlias) ns).getReferencedNamespace();
                                                        } else if (CsmKindUtilities.isNamespace(ns)) {
                                                            lastNamespace = (CsmNamespace) ns;
                                                        }
                                                    }
                                                }
                                                // restore old
                                                compResolver.setResolveScope(old);
                                                if (lastNamespace != null) {
                                                    // clean up invisible backup
                                                    lastType = null;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else { // not the first item
                                boolean needToCheckNS = true;
                                if (lastType != null) { // last was type
                                    needToCheckNS = false;
                                    if (findType || !last) {
                                        boolean inner = false;
                                        int ad = lastType.getArrayDepth();
                                        if (staticOnly && ad == 0) { // can be inner class
                                            CsmClassifier classifier = getClassifier(lastType, contextFile);
                                            if (CsmKindUtilities.isClass(classifier)) {
                                                CsmClass clazz = (CsmClass) classifier;
                                                List<CsmClassifier> classes = finder.findNestedClassifiers(contextElement, clazz, var, true, true, this.sort);
                                                if (classes != null && !classes.isEmpty()) {
                                                    lastType = CsmCompletion.createType(classes.get(0), 0, 0, false);
                                                    inner = true;
                                                }
                                            }
                                        }
                                        if (!inner) { // not inner class name
                                            if (ad == 0 || (kind == ExprKind.ARROW)) { // zero array depth or deref array as pointer
                                                CsmClassifier classifier = getClassifier(lastType, contextFile);
                                                if (CsmKindUtilities.isClass(classifier)) {
                                                    CsmClass clazz = (CsmClass) classifier;
                                                    List elemList = finder.findFields(contextElement, clazz, var, true, staticOnly, true, true, scopeAccessedClassifier, this.sort);
                                                    if (kind == ExprKind.ARROW || kind == ExprKind.DOT) {
                                                        // try base classes names like in this->Base::foo()
                                                        // or like in a.Base::foo()
                                                        List<CsmClass> baseClasses = finder.findBaseClasses(contextElement, clazz, var, true, this.sort);
                                                        if (elemList == null) {
                                                            elemList = baseClasses;
                                                        } else if (baseClasses != null) {
                                                            elemList.addAll(baseClasses);
                                                        }
                                                    }
                                                    if (elemList != null && elemList.size() > 0) { // match found
                                                        CsmObject csmObj = (CsmObject) elemList.get(0);
                                                        lastType = CsmCompletion.getObjectType(csmObj);
                                                        staticOnly = false;
                                                    } else if (kind == ExprKind.ARROW) {
                                                    } else { // no match found
                                                        lastType = null;
                                                        cont = false;
                                                    }
                                                } else {
                                                    lastType = null;
                                                    cont = false;
                                                }
                                            } else { // array depth > 0 but no array dereference
                                                cont = false;
                                            }
                                        }
                                    } else { // last and searching for completion output
                                        scopeAccessedClassifier = (kind == ExprKind.SCOPE);
//                                    CsmClass curCls = sup.getClass(varPos);
                                        CsmClassifier cls = extractLastTypeClassifier(kind);
                                        if (cls == null) {
                                            lastType = null;
                                            cont = false;
                                        } else {
                                            // IZ#143044, IZ#160677
                                            // There is no need for searching in parents for global declarations/definitions
                                            // in case of csope access
                                            boolean inspectParentClasses = (this.contextElement != null || !scopeAccessedClassifier);
                                            List res = findFieldsAndMethods(finder, contextElement, cls, var, openingSource, staticOnly && !memberPointer, false, inspectParentClasses, this.scopeAccessedClassifier, skipConstructors, sort);
                                            List nestedClassifiers = findNestedClassifiers(finder, contextElement, cls, var, openingSource, true, sort);
                                            res.addAll(nestedClassifiers);
                                            // add base classes as well
                                            if (kind == ExprKind.ARROW || kind == ExprKind.DOT) {
                                                // try base classes names like in this->Base::foo()
                                                // or like in a.Base::foo()
                                                List<CsmClass> baseClasses = finder.findBaseClasses(contextElement, cls, var, openingSource, sort);
                                                res.addAll(baseClasses);
                                            }
                                            if (res.isEmpty() && scopeAccessedClassifier && lastNamespace != null) {
                                                needToCheckNS = true;
                                            } else {
                                                result = new CsmCompletionResult(
                                                        component, getBaseDocument(),
                                                        //                                                 findFieldsAndMethods(finder, curCls == null ? null : getNamespaceName(curCls), cls, var, false, staticOnly, false),
                                                        res,
                                                        formatType(lastType, true, true, false) + var + '*',
                                                        item,
                                                        0/*cls.getName().length() + 1*/,
                                                        isProjectBeeingParsed(), contextElement, instantiateTypes);
                                            }
                                        }
                                    }
                                }
                                if (lastNamespace != null && needToCheckNS) { // currently package
                                    String searchPkg = (lastNamespace.isGlobal() ? "" : (lastNamespace.getQualifiedName() + CsmCompletion.SCOPE)) + var;
                                    if (findType || !last) {
                                        List res = finder.findNestedNamespaces(lastNamespace, var, true, false); // find matching nested namespaces
                                        CsmNamespace curNs = res.isEmpty() ? null : (CsmNamespace) res.get(0);
                                        if (curNs != null) {
                                            lastNamespace = curNs;
                                            lastType = null;
                                        } else { // package doesn't exist
                                            res = finder.findNamespaceElements(lastNamespace, var, true, false, true);
//                                        if(res.isEmpty()) {
//                                            res = finder.findStaticNamespaceElements(lastNamespace, endOffset, var, true, false, true);
//                                        }
                                            CsmObject obj = res.isEmpty() ? null : (CsmObject) res.iterator().next();
                                            lastType = CsmCompletion.getObjectType(obj);
                                            cont = (lastType != null);
                                            lastNamespace = null;
                                        }
                                    } else { // last and searching for completion output
                                        if (last) { // get all matching fields/methods/packages
                                            List res = finder.findNestedNamespaces(lastNamespace, var, openingSource, false); // find matching nested namespaces
                                            res.addAll(finder.findNamespaceElements(lastNamespace, var, openingSource, false, false)); // matching classes
                                            res.addAll(finder.findStaticNamespaceElements(lastNamespace, var, openingSource)); // matching static elements
                                            result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*', item, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                        }
                                    }
                                }
                            }
                            break;
                    }
                    break;

                case CsmCompletionExpression.ARRAY:
//                cont = resolveItem(item.getParameter(0), first, false, ExprKind.NONE);
                    lastType = resolveType(item.getParameter(0));
                    cont = false;
                    if (lastType != null) { // must be type
                        if (item.getParameterCount() == 2) { // index in array follows
                            int ptrDepth = lastType.getPointerDepth();
                            int arrDepth = lastType.getArrayDepth();
                            if (ptrDepth > 0) {
                                ptrDepth--;
                                lastType = CsmCompletion.getType(lastType.getClassifier(), ptrDepth, ptrDepth>0, arrDepth, lastType.isConst());
                            } else if (arrDepth > 0) {
                                arrDepth--;
                                lastType = CsmCompletion.getType(lastType.getClassifier(), ptrDepth, ptrDepth>0, arrDepth, lastType.isConst());
                            } else {
                                CsmClassifier cls = getClassifier(lastType, contextFile);
                                if (cls != null) {
                                    CsmFunction opArray = CsmCompletionQuery.getOperator(cls, contextFile, CsmFunction.OperatorKind.ARRAY);
                                    if (opArray != null) {
                                        lastType = opArray.getReturnType();
                                    }
                                }
                            }
                            cont = true;
                        } else { // no index, increase array depth
                            lastType = CsmCompletion.getType(lastType.getClassifier(), lastType.getPointerDepth(), lastType.isPointer(),
                                    lastType.getArrayDepth() + 1, lastType.isConst());
                            cont = true;
                        }
                    }
                    break;

                case CsmCompletionExpression.INSTANCEOF:
                    lastType = CsmCompletion.BOOLEAN_TYPE;
                    break;

                case CsmCompletionExpression.GENERIC_TYPE: {
                    CsmType typ = resolveType(item.getParameter(0));
                    if (typ != null) {
                        lastType = typ;
                        CsmClassifier cls = getClassifier(lastType, contextFile);
                        if (cls != null && CsmKindUtilities.isTemplate(cls)) {
                            CsmObject obj = createInstantiation((CsmTemplate)cls, item);
                            if (obj != null && CsmKindUtilities.isClass(obj)) {
                                lastType = CsmCompletion.createType((CsmClass)obj, 0, 0, false);
                            }
                        }
                    }
                    break;
                }
                case CsmCompletionExpression.GENERIC_TYPE_OPEN:
                case CsmCompletionExpression.OPERATOR:
                {
                    CompletionResolver.Result res = null;
//                    CsmClassifier cls = null;
//                    if (findType) {
//                        lastType = resolveType(item.getParameter(0));
//                        cls = extractLastTypeClassifier(ExprKind.NONE);
//                    }
                    Collection<CsmFunction> mtdList = new LinkedHashSet<CsmFunction>();
                    compResolver.setResolveTypes(CompletionResolver.RESOLVE_FUNCTIONS);
                    String operatorPrefix = "operator " + item.getTokenText(0);  // NOI18N
                    if (resolve(item.getTokenOffset(0), operatorPrefix, false)) {
                        res = compResolver.getResult();
                    }
                    res.addResulItemsToCol(mtdList);
                    result = new CsmCompletionResult(component, getBaseDocument(), res, operatorPrefix, item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                    lastType = null;
                    switch (item.getTokenID(0)) {
                        case EQ: // Assignment operators
                        case PLUSEQ:
                        case MINUSEQ:
                        case STAREQ:
                        case SLASHEQ:
                        case AMPEQ:
                        case BAREQ:
                        case CARETEQ:
                        case PERCENTEQ:
                        case LTLTEQ:
                        case GTGTEQ:
                            if (item.getParameterCount() > 0) {
                                lastType = resolveType(item.getParameter(0));
                                staticOnly = false;
                            }
                            break;

                        case LT: // Binary, result is boolean
                        case GT:
                        case LTEQ:
                        case GTEQ:
                        case EQEQ:
                        case NOTEQ:
                        case AMPAMP: // Binary, result is boolean
                        case BARBAR:
                            lastType = CsmCompletion.BOOLEAN_TYPE;
                            // nobreak;

                        case PLUS:
                        case MINUS:
                            if (findType && mtdList.isEmpty() && lastType == null) {
                                if (item.getParameterCount() > 0) {
                                    lastType = resolveType(item.getParameter(0));
                                    staticOnly = false;
                                }
                                break;
                            }
                            // nobreak;
                            
                        case LTLT: // Always binary
                        case GTGT:
//                    case RUSHIFT:
                        case STAR:
                        case SLASH:
                        case AMP:
                        case BAR:
                        case CARET:
                        case PERCENT:
                            if (findType && !mtdList.isEmpty()) {
                                List<CsmType> typeList = getTypeList(item, 0);
                                // check exact overloaded operator
                                Collection<CsmFunction> filtered = CompletionSupport.filterMethods(mtdList, typeList, false, false);
                                if (filtered.size() > 0) {
                                    mtdList = filtered;
                                    lastType = extractFunctionType(mtdList, null);
                                } else if (item.getParameterCount() > 0) {
                                    lastType = resolveType(item.getParameter(0));
                                    staticOnly = false;
                                }
                            } else if (lastType != null) {
                                // simple backup
                                switch (item.getParameterCount()) {
                                    case 2:
                                        CsmType typ1 = resolveType(item.getParameter(0));
                                        CsmType typ2 = resolveType(item.getParameter(1));
                                        if (typ1 != null && typ2 != null && typ1.getArrayDepth() == 0 && typ2.getArrayDepth() == 0 && CsmCompletion.isPrimitiveClass(typ1.getClassifier()) && CsmCompletion.isPrimitiveClass(typ2.getClassifier())) {
                                            lastType = sup.getCommonType(typ1, typ2);
                                        }
                                        break;
                                    case 1: // get the only one parameter
                                        CsmType typ = resolveType(item.getParameter(0));
                                        if (typ != null && CsmCompletion.isPrimitiveClass(typ.getClassifier())) {
                                            lastType = typ;
                                        }
                                        break;
                                }
                            }
                            break;

                        case COLON:
                            switch (item.getParameterCount()) {
                                case 2:
                                    CsmType typ1 = resolveType(item.getParameter(0));
                                    CsmType typ2 = resolveType(item.getParameter(1));
                                    if (typ1 != null && typ2 != null) {
                                        lastType = sup.getCommonType(typ1, typ2);
                                    }
                                    break;

                                case 1:
                                    lastType = resolveType(item.getParameter(0));
                                    break;
                            }
                            break;

                        case QUESTION:
                            if (item.getParameterCount() >= 2) {
                                lastType = resolveType(item.getParameter(1)); // should be colon
                            }
                            break;
                    }
                    break;
                }
                case CsmCompletionExpression.UNARY_OPERATOR:
                    if (item.getParameterCount() > 0) {
                        lastType = resolveType(item.getParameter(0));
                        staticOnly = false;
                    }
                    break;

                case CsmCompletionExpression.MEMBER_POINTER_OPEN:
                    if (item.getParameterCount() > 0) {
                        if (item.getTokenCount() == 1) {
                            switch (item.getTokenID(0)) {
                                case AMP:
                                    memberPointer = true;
                                    break;
                            }
                        }
                        cont = resolveExp(item.getParameter(0));
                        memberPointer = false;
                    }
                    break;
                case CsmCompletionExpression.MEMBER_POINTER:
                    if (item.getParameterCount() > 0) {
                        lastType = resolveType(item.getParameter(0));
                        staticOnly = false;
                        CsmFunction.OperatorKind opKind = null;
                        if (item.getTokenCount() == 1) {
                            switch (item.getTokenID(0)) {
                                case AMP:
                                    opKind = CsmFunction.OperatorKind.ADDRESS;
                                    break;
                                case STAR:
                                    opKind = CsmFunction.OperatorKind.POINTER;
                                    break;
                            }
                        }
                        if (opKind != null) {
                            CsmType opType = CsmCompletionQuery.getOverloadedOperatorReturnType(lastType, contextFile, opKind, MAX_DEPTH);
                            if (opType != null) {
                                lastType = opType;
                            } else if (lastType != null) {
                                int ptrDepth = lastType.getPointerDepth();
                                if (ptrDepth > 0 && opKind == CsmFunction.OperatorKind.POINTER) {
                                    ptrDepth--;
                                }
                                lastType = CsmCompletion.getType(getClassifier(lastType, contextFile), ptrDepth, lastType.isReference(), lastType.getArrayDepth(), lastType.isConst());
                            }
                        }
                    // TODO: need to convert lastType into reference based on item token '&' or '*'
                    // and nested pointer expressions
                    }
                    break;

                case CsmCompletionExpression.CONVERSION:
                    lastType = resolveType(item.getParameter(0));
                    staticOnly = false;
                    break;

                case CsmCompletionExpression.TYPE_REFERENCE:
                    if (item.getParameterCount() > 0) {
                        CsmCompletionExpression param = item.getParameter(0);
                        staticOnly = false;
                        lastType = resolveType(param);
                        // TODO: we need to wrap lastType with pointer and address-of
                        // based on the zero token of 'item' expression
                        if(lastType != null) {
                            lastType = new BaseType(lastType.getClassifier(), lastType.getPointerDepth() + 1, lastType.isReference(), lastType.getArrayDepth(), lastType.isConst());
                        }
                    }
                    break;

                case CsmCompletionExpression.TYPE:
                    if (findType) {
                        if (item.getParameterCount() > 0) {
                            lastType = resolveType(item.getParameter(0));
                        } else {
                            lastType = getPredefinedType(item);
                        }
                    }
                    if (!findType || lastType == null) {
                        // this is the case of code completion on parameter or unresolved predefined type
                        int nrTokens = item.getTokenCount();
                        if (nrTokens >= 1) {
                            String varName = item.getTokenText(nrTokens - 1);
                            int varPos = item.getTokenOffset(nrTokens - 1);
                            compResolver.setResolveTypes(CompletionResolver.RESOLVE_LOCAL_VARIABLES | CompletionResolver.RESOLVE_CLASSES | CompletionResolver.RESOLVE_TEMPLATE_PARAMETERS | CompletionResolver.RESOLVE_GLOB_NAMESPACES | CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS);
                            if (resolve(varPos, varName, openingSource)) {
                                CompletionResolver.Result res = compResolver.getResult();
                                if (findType) {
                                    CsmClassifier cls = null;
                                    Collection<CsmObject> elems = new ArrayList<CsmObject>();
                                    res.addResulItemsToCol(elems);
                                    for (CsmObject csmObject : elems) {
                                        if (CsmKindUtilities.isVariable(csmObject)) {
                                            lastType = ((CsmVariable)csmObject).getType();
                                            break;
                                        } else if (CsmKindUtilities.isClassifier(csmObject)) {
                                            cls = (CsmClassifier)csmObject;
                                            break;
                                        }
                                    }
                                    if (lastType == null && cls != null) {
                                        boolean _const = false;
                                        for (int i = 0; i < item.getTokenCount() -1; i++) {
                                            _const |= item.getTokenText(i).equals("const"); // NOI18N
                                        }
                                        lastType = CsmCompletion.getType(cls, 0, false, 0, _const);
                                    }
                                }
                                result = new CsmCompletionResult(component, getBaseDocument(), res, varName + '*', item, varPos, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                            }
                        }
                    }
                    break;

                case CsmCompletionExpression.PARENTHESIS:
                    lastType = resolveType(item.getParameter(item.getParameterCount() - 1));
                    break;

                case CsmCompletionExpression.CONSTRUCTOR: // constructor can be part of a DOT expression
                    isConstructor = true;
                    cont = resolveExp(item.getParameter(0));
                    staticOnly = false;
                    break;

                case CsmCompletionExpression.METHOD_OPEN: // Unclosed method
                    methodOpen = true;
                // let it flow to method
                // nobreak
                case CsmCompletionExpression.METHOD: // Closed method
                    CsmCompletionExpression mtdNameExp = item.getParameter(0);
                    CsmCompletionExpression genericNameExp = null;
                    while (mtdNameExp.getExpID() == CsmCompletionExpression.GENERIC_TYPE) {
                        genericNameExp = mtdNameExp;
//                        lastType = resolveType(mtdNameExp);
                        if (mtdNameExp.getParameterCount() > 0) {
                            mtdNameExp = mtdNameExp.getParameter(0);
                        } else {
                            break;
                        }
                    }
                    String mtdName = mtdNameExp.getTokenText(0);

                    CsmCompletionExpression param = item.getParameter(0);
                    if(param.getExpID() == CsmCompletionExpression.METHOD) {
                        boolean oldFindType = findType;
                        setFindType(true);
                        cont = resolveExp(param);
                        setFindType(oldFindType);
                    }

                    // this() invoked, offer constructors
//                if( ("this".equals(mtdName)) && (item.getTokenCount()>0) ){ //NOI18N
//                    CsmClassifier cls = sup.getClass(item.getTokenOffset(0));
//                    if (cls != null) {
//                        cls = finder.getExactClassifier(cls.getQualifiedName());
//                        if (cls != null) {
//                            isConstructor = true;
//                            mtdName = cls.getName();
//                        }
//                    }
//                }

                    // super() invoked, offer constructors for super class
//                if( ("super".equals(mtdName)) && (item.getTokenCount()>0) ){ //NOI18N
//                    CsmClassifier cls = sup.getClass(item.getTokenOffset(0));
//                    if (cls != null) {
//                        cls = finder.getExactClassifier(cls.getQualifiedName());
//                        if (cls != null) {
//                            cls = cls.getSuperclass();
//                            if (cls != null) {
//                                isConstructor = true;
//                                mtdName = cls.getName();
//                            }
//                        }
//                    }
//                }

                    if (isConstructor) { // Help for the constructor
                        CsmClassifier cls = null;
                        if (first) {
                            cls = CompletionSupport.getClassFromName(CsmCompletionQuery.this.getFinder(), mtdName, true);
                        } else { // not first
//                        if ((last)&&(lastNamespace != null)) { // valid package
//                            cls = JCUtilities.getExactClass(finder, mtdName, (lastNamespace.isGlobal() ? "" : lastNamespace.getName()));
//                        } else if (lastType != null) {
//                            if(last){ // inner class
//                                cls = JCUtilities.getExactClass(finder, mtdName,
//                                lastType.getClassifier().getFullName());
//                            }else{
//                                if (lastType.getArrayDepth() == 0) { // Not array
//                                    cls = lastType.getClassifier();
//                                } else { // Array of some depth
//                                    cls = CsmCompletion.OBJECT_CLASS_ARRAY; // Use Object in this case
//                                }
//                            }
//                        }
                        }
                        if (cls == null) {
                            cls = findExactClass(mtdName, mtdNameExp.getTokenOffset(0));
                        }
                        if (cls != null) {
                            lastType = CsmCompletion.getType(cls, 0, false, 0, false);
//
//                        List ctrList = (finder instanceof JCBaseFinder) ?
//                            JCUtilities.getConstructors(cls, ((JCBaseFinder)finder).showDeprecated()) :
//                            JCUtilities.getConstructors(cls);
//                        String parmStr = "*"; // NOI18N
//                        List typeList = getTypeList(item, 1);
//                        List filtered = sup.filterMethods(ctrList, typeList, methodOpen);
//                        if (filtered.size() > 0) {
//                            ctrList = filtered;
//                            parmStr = formatTypeList(typeList, methodOpen);
//                        }
//                        List mtdList = finder.findMethods(cls, mtdName, true, false, first);
//                        if (mtdList.size() > 0) {
//                            if (last && !findType) {
//                                result = new CsmCompletionResult(component, mtdList,
//                                                        formatType(lastType, true, true, false) + mtdName + '(' + parmStr + ')',
//                                                        item, endOffset, 0, 0);
//                            } else {
//                                    lastType = ((CsmMethod)mtdList.get(0)).getReturnType();
//                                    staticOnly = false;
//                            }
//                        } else{
//                            result = new CsmCompletionResult(component, ctrList,
//                            formatType(lastType, true, false, false) + '(' + parmStr + ')',
//                            item, endOffset, 0, 0);
//                        }
                        } else {
                            isConstructor = false;
                        }
                    }
                    if (true || isConstructor == false) {
                        // Help for the method

                        // when use hyperlink => method() is passed as methodOpen, but we
                        // want to resolve "method"
                        // otherwise we need all in current context
                        if (!methodOpen || openingSource) {
                            Collection<CsmFunction> mtdList = new LinkedHashSet<CsmFunction>();
                            if (first && !(isConstructor && lastType != null)) { // already resolved for constructor
                                // resolve all functions in context
                                int varPos = mtdNameExp.getTokenOffset(0);
                                boolean look4Constructors = findType || openingSource;
                                if (look4Constructors) {
                                    Collection<? extends CsmObject> candidates = new ArrayList<CsmObject>();
                                    // try to resolve the most visible
                                    compResolver.setResolveTypes(CompletionResolver.RESOLVE_FUNCTIONS | CompletionResolver.RESOLVE_CONTEXT_CLASSES);
                                    if (resolve(varPos, mtdName, true)) {
                                        compResolver.getResult().addResulItemsToCol(candidates);
                                    }
                                    for (CsmObject object : candidates) {
                                        if (CsmKindUtilities.isClass(object)) {
                                            mtdList.addAll(getConstructors((CsmClass) object));
                                        } else if (CsmKindUtilities.isFunction(object)) {
                                            mtdList.add((CsmFunction) object);
                                        }
                                    }
                                }
                                compResolver.setResolveTypes(CompletionResolver.RESOLVE_FUNCTIONS);
                                if (resolve(varPos, mtdName, openingSource)) {
                                    compResolver.getResult().addResulItemsToCol(mtdList);
                                }
                                if (!last || findType) {
                                    Collection<? extends CsmObject> candidates = new ArrayList<CsmObject>();
                                    compResolver.setResolveTypes(CompletionResolver.RESOLVE_VARIABLES | CompletionResolver.RESOLVE_LOCAL_VARIABLES);
                                    if (resolve(varPos, mtdName, true)) {
                                        compResolver.getResult().addResulItemsToCol(candidates);
                                    }
                                    for (CsmObject object : candidates) {
                                        if (CsmKindUtilities.isVariable(object)) {
                                            CsmType varType = ((CsmVariable) object).getType();
                                            if (varType != null) {
                                                CsmClassifier cls = getClassifier(varType, contextFile);
                                                CsmFunction funCall = cls == null ? null : CsmCompletionQuery.getOperator(cls, contextFile, CsmFunction.OperatorKind.CAST);
                                                if (funCall != null) {
                                                    mtdList.add(funCall);
                                                }
                                            }
                                        }
                                    }
                                }
                                if (lastType != null && (!last || findType)) {
                                    CsmClassifier cls = getClassifier(lastType, contextFile);
                                    CsmFunction funCall = cls == null ? null : CsmCompletionQuery.getOperator(cls, contextFile, CsmFunction.OperatorKind.CAST);
                                    if (funCall != null) {
                                        mtdList.add(funCall);
                                    }
                                }
                            } else {
                                // if prev expression was resolved => get it's class
                                if (lastType != null) {
                                    CsmClassifier classifier = extractLastTypeClassifier(kind);
                                    // try to find method in last resolved class appropriate for current context
                                    if (CsmKindUtilities.isClass(classifier)) {
                                        // IZ#143044
                                        // There is no need for searching in parents for global declarations/definitions
                                        boolean inspectParentClasses = (this.contextElement != null);
                                        mtdList.addAll(finder.findMethods(this.contextElement, (CsmClass) classifier, mtdName, true, false, first, inspectParentClasses, scopeAccessedClassifier, this.sort));
                                        if ((!last || findType) && (mtdList == null || mtdList.size() == 0)) {
                                            // could be pointer to function-type field
                                            lastType = null;
                                            List<CsmField> foundFields = finder.findFields(this.contextElement, (CsmClass) classifier, mtdName, true, false, first, true, scopeAccessedClassifier, this.sort);
                                            if (foundFields != null && !foundFields.isEmpty()) {
                                                // we found field with correct name, check if it has function pointer type
                                                for (CsmField csmField : foundFields) {
                                                    CsmType fldType = csmField.getType();
                                                    if (CsmKindUtilities.isFunctionPointerType(fldType)) {
                                                        // that was a function-type field
                                                        lastType = fldType;
                                                    }
                                                    if (fldType != null) {
                                                        CsmClassifier cls = getClassifier(fldType, contextFile);
                                                        CsmFunction funCall = cls == null ? null : CsmCompletionQuery.getOperator(cls, contextFile, CsmFunction.OperatorKind.CAST);
                                                        if (funCall != null) {
                                                            lastType = funCall.getReturnType();
                                                        }
                                                    }
                                                }
                                            }
                                            return (lastType != null);
                                        }
                                    }
                                } else if (lastNamespace != null) {
                                    CsmNamespace curNs = lastNamespace;
                                    lastNamespace = null;
                                    List<CsmNamespace> res = finder.findNestedNamespaces(curNs, mtdName, openingSource, false); // find matching nested namespaces
                                    for (CsmNamespace csmNamespace : res) {
                                        lastNamespace = csmNamespace;
                                        break;
                                    }
                                    List<CsmObject> elems = finder.findNamespaceElements(curNs, mtdName, openingSource, false, false); // matching classes
//                                    elems.addAll(finder.findStaticNamespaceElements(lastNamespace, mtdName, openingSource)); // matching static elements
                                    for (CsmObject obj: elems) {
                                        if (CsmKindUtilities.isFunction(obj)) {
                                            mtdList.add((CsmFunction)obj);
                                        } else if (CsmKindUtilities.isTypedef(obj)) {
                                            lastType = ((CsmTypedef)obj).getType();
                                            break;
                                        } else if (CsmKindUtilities.isClassifier(obj)) {
                                            lastType = CsmCompletion.getType((CsmClassifier)obj, 0, false, 0, false);
                                            break;
                                        }
                                    }
                                    if (findType && mtdList.isEmpty()) {
                                        return lastType != null || lastNamespace != null;
                                    }
                                }
                            }
                            if (mtdList == null || mtdList.size() == 0) {
                                // If we have not found method and (lastType != null) it could be default constructor.
                                if (!isConstructor) {
                                    if (first) {
                                        // It could be default constructor call without "new"
                                        CsmClassifier cls = null;
                                        //cls = sup.getClassFromName(CsmCompletionQuery.this.getFinder(), mtdName, true);
                                        if (cls == null) {
                                            cls = findExactClass(mtdName, mtdNameExp.getTokenOffset(0));
                                        }
                                        if (cls != null) {
                                            if (CsmKindUtilities.isTemplate(cls) && genericNameExp != null) {
                                                CsmObject inst = createInstantiation((CsmTemplate)cls, genericNameExp);
                                                if (CsmKindUtilities.isClassifier(inst)) {
                                                    cls = (CsmClassifier) inst;
                                                }
                                            }
                                            lastType = CsmCompletion.getType(cls, 0, false, 0, false);
                                        }
                                    } else {
                                        lastType = null;
                                    }
                                    if (lastType == null && (!last || findType)) {
                                        lastType = findBuiltInFunctionReturnType(mtdName,  mtdNameExp.getTokenOffset(0));
                                    }
                                }
                                return lastType != null;
                            }
                            String parmStr = "*"; // NOI18N
                            List typeList = getTypeList(item, 1);
                            Collection<CsmFunction> filtered = CompletionSupport.filterMethods(mtdList, typeList, methodOpen, true);
                            if (filtered.size() > 0) {
                                mtdList = filtered;
                                parmStr = formatTypeList(typeList, methodOpen);
                            }
                            if (mtdList.size() > 0) {
                                if (last && !findType) {
                                    result = new CsmCompletionResult(component, getBaseDocument(), mtdList,
                                            formatType(lastType, true, true, false) + mtdName + '(' + parmStr + ')',
                                            item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                                } else {
                                    lastType = extractFunctionType(mtdList, genericNameExp);
                                    staticOnly = false;
                                }
                            } else {
                                lastType = null; // no method found
                                cont = false;
                            }
                        } else { // package.method() is invalid
                            // this is the case of code completion after opening paren "method(|"
                            int varPos = endOffset; // mtdNameExp.getTokenOffset(0);
                            compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                            if (resolve(varPos, "", false)) {
                                CompletionResolver.Result res = compResolver.getResult();
                                result = new CsmCompletionResult(component, getBaseDocument(), res, mtdName + '*', mtdNameExp, varPos, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes);
                            }

//                        } else {
//                            lastNamespace = null;
//                            cont = false;
//                        }
                        }
                    }
                    break;
            }

            if ((result == null || result.getItems().isEmpty()) && lastType != null) {
                if(lastType.isTemplateBased() ||
                        CsmFileReferences.isTemplateParameterInvolved(lastType) ||
                        CsmFileReferences.hasTemplateBasedAncestors(lastType)) {
                    Collection<CsmObject> data = new ArrayList();
                    data.add(new TemplateBasedReferencedObjectImpl());
                    result = new CsmCompletionResult(component, getBaseDocument(), data, "title", item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                }
            }

            if (last && !first && (result == null || result.getItems().isEmpty()) && lastType != null) {
                CsmClassifier classifier = lastType.getClassifier();
                if(CsmKindUtilities.isInstantiation(classifier)) {
                    boolean instantiatedByTemplateParam = false;
                    CsmInstantiation inst = (CsmInstantiation)classifier;
                    Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = inst.getMapping();
                    for (CsmTemplateParameter templateParam : mapping.keySet()) {
                        CsmSpecializationParameter specParam = mapping.get(templateParam);
                        if(CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
                            CsmType type = ((CsmTypeBasedSpecializationParameter)specParam).getType();
                            if(type != null && type.isTemplateBased()) {
                                instantiatedByTemplateParam = true;
                                break;
                            }
                        }
                    }
                    if(instantiatedByTemplateParam) {
                        if(!CsmInstantiationProvider.getDefault().getSpecializations(classifier, contextFile, endOffset).isEmpty()) {
                            Collection<CsmObject> data = new ArrayList();
                            data.add(new TemplateBasedReferencedObjectImpl());
                            result = new CsmCompletionResult(component, getBaseDocument(), data, "title", item, endOffset, 0, 0, isProjectBeeingParsed(), contextElement, instantiateTypes); // NOI18N
                        }
                    }
                }
            }

            if (lastType == null && lastNamespace == null) { // !!! shouldn't be necessary
                cont = false;
            }
            return cont;
        }

        private CsmObject createInstantiation(CsmTemplate template, CsmCompletionExpression exp) {
            if (exp.getExpID() == CsmCompletionExpression.GENERIC_TYPE) {
                CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
                List<CsmSpecializationParameter> params = new ArrayList<CsmSpecializationParameter>();
                int paramsNumber = (template.getTemplateParameters().size() < exp.getParameterCount() - 1) ? template.getTemplateParameters().size() : exp.getParameterCount() - 1;
                for (int i = 0; i < paramsNumber; i++) {
                    CsmCompletionExpression paramInst = exp.getParameter(i + 1);
                    if (paramInst != null) {
                        switch (paramInst.getExpID()) {
                            case CsmCompletionExpression.CONSTANT:
                                params.add(ip.createExpressionBasedSpecializationParameter(paramInst.getTokenText(0),
                                        contextFile, paramInst.getTokenOffset(0), paramInst.getTokenOffset(0) + paramInst.getTokenLength(0)));
                                break;
                            default:
                                CsmType type = resolveType(paramInst);
                                if (type != null) {
                                    params.add(ip.createTypeBasedSpecializationParameter(type));
                                } else {
                                    params.add(ip.createExpressionBasedSpecializationParameter(paramInst.getTokenText(0),
                                            contextFile, paramInst.getTokenOffset(0), paramInst.getTokenOffset(0) + paramInst.getTokenLength(0)));
                                }
                        }
                    } else {
                        break;
                    }
                }
                int contextOffset = exp.getTokenOffset(0);
                return ip.instantiate(template, params, getFinder().getCsmFile(), contextOffset);
            }
            return null;
        }

        private CsmType findBuiltInFunctionReturnType(String mtdName, int tokenOffset) {
            CsmType out = null;
            if ("typeid".contentEquals(mtdName)) { // NOI18N
                CsmClassifier cls = getFinder().getExactClassifier("std::type_info"); // NOI18N
                if (cls == null) {
                    CsmNamespace ns = findExactNamespace("std", tokenOffset); // NOI18N
                    if (ns != null) {
                        List<CsmClassifier> findClasses = getFinder().findClasses(ns, mtdName, true, false);
                        for (CsmClassifier csmClassifier : findClasses) {
                            cls = csmClassifier;
                            break;
                        }
                    }
                }
                if (cls != null) {
                    out = CsmCompletion.getType(cls, 0, false, 0, false);
                }
            }
            return out;
        }

        private CsmNamespace findExactNamespace(final String var, final int varPos) {
            CsmNamespace ns = null;
            compResolver.setResolveTypes(CompletionResolver.RESOLVE_GLOB_NAMESPACES | CompletionResolver.RESOLVE_LIB_NAMESPACES);
            if (resolve(varPos, var, true)) {
                CompletionResolver.Result res = compResolver.getResult();
                Collection<? extends CsmObject> addResulItemsToCol = res.addResulItemsToCol(new ArrayList<CsmObject>());
                for (CsmObject csmObject : addResulItemsToCol) {
                    if (CsmKindUtilities.isNamespace(csmObject)) {
                        return (CsmNamespace) csmObject;
                    } else if (CsmKindUtilities.isNamespaceAlias(csmObject)) {
                        ns = ((CsmNamespaceAlias) csmObject).getReferencedNamespace();
                        if (ns != null) {
                            return ns;
                        }
                    }
                }
            }
            return ns;
        }

        private CsmClassifier findExactClass(final String var, final int varPos) {
            CsmClassifier cls = null;
            compResolver.setResolveTypes(CompletionResolver.RESOLVE_CLASSES | CompletionResolver.RESOLVE_LIB_CLASSES | CompletionResolver.RESOLVE_CLASS_NESTED_CLASSIFIERS);
            if (resolve(varPos, var, true)) {
                CompletionResolver.Result res = compResolver.getResult();
                Collection<? extends CsmObject> allItems = res.addResulItemsToCol(new ArrayList<CsmObject>());
                for (CsmObject item : allItems) {
                    if (CsmKindUtilities.isClassifier(item)) {
                        cls = CsmBaseUtilities.getOriginalClassifier((CsmClassifier) item, contextFile);
                    }
                    if (cls != null) {
                        break;
                    }
                }
            }
            return cls;
        }

        private CsmType findExactVarType(final String var, final int varPos) {
            return sup.findExactVarType(finder.getCsmFile(), var, varPos, getFileReferencesContext());   
        }

        private List<CsmType> getTypeList(CsmCompletionExpression item, int firstChildIdx) {
            int parmCnt = item.getParameterCount();
            List<CsmType> typeList = new ArrayList<CsmType>();
            if (parmCnt > firstChildIdx) { // will try to filter by parameters
                for (int i = firstChildIdx; i < parmCnt; i++) {
                    CsmCompletionExpression parm = item.getParameter(i);
                    CsmType typ = resolveType(parm);
                    typeList.add(typ);
                }
            }
            return typeList;
        }
    }

    private static String formatTypeList(List typeList, boolean methodOpen) {
        StringBuilder sb = new StringBuilder();
        if (typeList.size() > 0) {
            int cntM1 = typeList.size() - 1;
            for (int i = 0; i <= cntM1; i++) {
                CsmType t = (CsmType) typeList.get(i);
                if (t != null) {
// XXX                    sb.append(t.format(false));
                    sb.append(t.getText());
                } else {
                    sb.append('?'); //NOI18N
                }
                if (i < cntM1) {
                    sb.append(", "); // NOI18N
                }
            }
            if (methodOpen) {
                sb.append(", *"); // NOI18N
            }
        } else { // no parameters
            if (methodOpen) {
                sb.append("*"); // NOI18N
            }
        }
        return sb.toString();
    }

    public static class CsmCompletionResult {

        /** First offset in the name of the (inner) class
         * to be displayed. It's used to display the inner classes
         * of the main class to exclude the initial part of the name.
         */
        private int classDisplayOffset;
        /** Expression to substitute */
        private CsmCompletionExpression substituteExp;
        /** Starting position of the text to substitute */
        private int substituteOffset;
        /** Length of the text to substitute */
        private int substituteLength;
        /** Component to update */
        private JTextComponent component;
        /**
         * baseDocument to work with
         */
        private BaseDocument baseDocument;
        private List<CompletionItem> items;

        public CsmCompletionResult(JTextComponent component, BaseDocument doc, Collection data, String title,
                CsmCompletionExpression substituteExp, int classDisplayOffset, boolean isProjectBeeingParsed, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
            this(component, doc, data, title, substituteExp, substituteExp.getTokenOffset(0),
                    substituteExp.getTokenLength(0), classDisplayOffset, isProjectBeeingParsed, contextElement, instantiateTypes);
        }

        public CsmCompletionResult(JTextComponent component, BaseDocument doc, CompletionResolver.Result res, String title,
                CsmCompletionExpression substituteExp, int substituteOffset,
                int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
            this(component, doc,
                    convertData(res, classDisplayOffset, substituteExp, substituteOffset, contextElement, instantiateTypes),
                    true,
                    title,
                    substituteExp,
                    substituteOffset,
                    substituteLength, classDisplayOffset, isProjectBeeingParsed);
        }

        public CsmCompletionResult(JTextComponent component, BaseDocument doc, Collection data, String title,
                CsmCompletionExpression substituteExp, int substituteOffset,
                int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
            this(component, doc,
                    convertData(data, classDisplayOffset, substituteExp, substituteOffset, contextElement, instantiateTypes),
                    true, title, substituteExp, substituteOffset,
                    substituteLength, classDisplayOffset, isProjectBeeingParsed);
        }

        public CsmCompletionResult(JTextComponent component, BaseDocument doc, List<CompletionItem> data, boolean updateTitle, String title,
                CsmCompletionExpression substituteExp, int substituteOffset,
                int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed) {
//            super(component,
//                    updateTitle ? getTitle(data, title, isProjectBeeingParsed) : title,
//                    data,
//                    substituteOffset,
//                    substituteLength);

            this.component = component;
            this.baseDocument = doc;
            this.substituteExp = substituteExp;
            this.substituteOffset = substituteOffset;
            this.substituteLength = substituteLength;
            this.classDisplayOffset = classDisplayOffset;
            this.items = data;
        }

        public List<CompletionItem> getItems() {
            return Collections.unmodifiableList(items);
        }

        private static String getTitle(List data, String origTitle, boolean isProjectBeeingParsed) {
            if (CsmUtilities.DEBUG) {
                System.out.println("original title (resolved type) was " + origTitle); //NOI18N
            }
            String out = NO_SUGGESTIONS;
            if (data != null && data.size() > 0) {
                out = origTitle;
            }
            if (isProjectBeeingParsed) {
                out = MessageFormat.format(PROJECT_BEEING_PARSED, new Object[]{out});
            }
            return out;
        }

        protected JTextComponent getComponent() {
            return component;
        }

        protected int getSubstituteLength() {
            return substituteLength;
        }

        public int getSubstituteOffset() {
            return substituteOffset;
        }

        protected CsmCompletionExpression getSubstituteExp() {
            return substituteExp;
        }

        protected int getClassDisplayOffset() {
            return classDisplayOffset;
        }
        private boolean simpleVariableExpression;

        private void setSimpleVariableExpression(boolean simple) {
            this.simpleVariableExpression = simple;
        }

        public boolean isSimpleVariableExpression() {
            return simpleVariableExpression;
        }
    }

    private static boolean isSimpleVariableExpression(CsmCompletionExpression exp) {
        switch (exp.getExpID()) {
            case CsmCompletionExpression.DOT_OPEN: // Dot expression with the dot at the end
            case CsmCompletionExpression.ARROW_OPEN: // Arrow expression with the arrow at the end
            case CsmCompletionExpression.DOT: // Dot expression
            case CsmCompletionExpression.ARROW: // Arrow expression
            case CsmCompletionExpression.SCOPE_OPEN: // Scope expression with the arrow at the end
            case CsmCompletionExpression.SCOPE: // Scope expression
            case CsmCompletionExpression.NEW: // 'new' keyword
                return false;
        }
        return true;
    }

    //========================== Items Factory ===============================
    protected void setCsmItemFactory(CsmItemFactory itemFactory) {
        CsmCompletionQuery.itemFactory = itemFactory;
    }

    public static CsmItemFactory getCsmItemFactory() {
        return itemFactory;
    }

    public interface CsmItemFactory {

        public CsmResultItem.LocalVariableResultItem createLocalVariableResultItem(CsmVariable var);

        public CsmResultItem createLabelResultItem(CsmLabel csmStatement);

        public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld);

        public CsmResultItem.EnumeratorResultItem createMemberEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);

        public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes);

        public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes);

        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN);

        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.ForwardClassResultItem createForwardClassResultItem(CsmClassForwardDeclaration cls, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.FileLocalVariableResultItem createFileLocalVariableResultItem(CsmVariable var);

        public CsmResultItem.EnumeratorResultItem createFileLocalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);

        public CsmResultItem.FileLocalFunctionResultItem createFileLocalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes);

        public CsmResultItem.MacroResultItem createFileLocalMacroResultItem(CsmMacro mac);

        public CsmResultItem.MacroResultItem createFileIncludedProjectMacroResultItem(CsmMacro mac);

        public CsmResultItem.TemplateParameterResultItem createTemplateParameterResultItem(CsmTemplateParameter par);

        public CsmResultItem.GlobalVariableResultItem createGlobalVariableResultItem(CsmVariable var);

        public CsmResultItem.EnumeratorResultItem createGlobalEnumeratorResultItem(CsmEnumerator enm, int enumtrDisplayOffset, boolean displayFQN);

        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction mtd, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes);

        public CsmResultItem.MacroResultItem createGlobalMacroResultItem(CsmMacro mac);

        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath);

        public CsmResultItem.NamespaceAliasResultItem createNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath);

        public CsmResultItem.ClassResultItem createLibClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.EnumResultItem createLibEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN);

        public CsmResultItem.TypedefResultItem createLibTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.MacroResultItem createFileIncludedLibMacroResultItem(CsmMacro mac);

        public CsmResultItem.MacroResultItem createLibMacroResultItem(CsmMacro mac);

        public CsmResultItem.GlobalVariableResultItem createLibGlobalVariableResultItem(CsmVariable var);

        public CsmResultItem.EnumeratorResultItem createLibGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);

        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean instantiateTypes);

        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath);

        public CsmResultItem.NamespaceAliasResultItem createLibNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath);
    }
    private static final int FAKE_PRIORITY = 1000;

    public static final class DefaultCsmItemFactory implements CsmItemFactory {
        public DefaultCsmItemFactory() {
        }

        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
            return new CsmResultItem.NamespaceResultItem(pkg, displayFullNamespacePath, FAKE_PRIORITY);
        }

        public CsmResultItem.NamespaceAliasResultItem createNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath) {
            return new CsmResultItem.NamespaceAliasResultItem(alias, displayFullNamespacePath, FAKE_PRIORITY);
        }

        public CsmResultItem.EnumeratorResultItem createMemberEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return createGlobalEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN);
        }

        public CsmResultItem.EnumeratorResultItem createFileLocalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return createGlobalEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN);
        }

        public CsmResultItem.EnumeratorResultItem createGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.EnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        public CsmResultItem.MacroResultItem createFileLocalMacroResultItem(CsmMacro mac) {
            return createGlobalMacroResultItem(mac);
        }

        public CsmResultItem.MacroResultItem createFileIncludedProjectMacroResultItem(CsmMacro mac) {
            return createGlobalMacroResultItem(mac);
        }

        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.ClassResultItem(cls, classDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        public CsmResultItem.ForwardClassResultItem createForwardClassResultItem(CsmClassForwardDeclaration cls, int classDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.ForwardClassResultItem(cls, classDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.EnumResultItem(enm, enumDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.TypedefResultItem(def, classDisplayOffset, displayFQN, FAKE_PRIORITY);
        }

        public CsmResultItem.ClassResultItem createLibClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN) {
            return createClassResultItem(cls, classDisplayOffset, displayFQN);
        }

        public CsmResultItem.EnumResultItem createLibEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return createEnumResultItem(enm, enumDisplayOffset, displayFQN);
        }

        public CsmResultItem.TypedefResultItem createLibTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return createTypedefResultItem(def, classDisplayOffset, displayFQN);
        }

        public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld) {
            return new CsmResultItem.FieldResultItem(fld, FAKE_PRIORITY);
        }

        public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new CsmResultItem.MethodResultItem(mtd, substituteExp, FAKE_PRIORITY, isDeclaration, instantiateTypes);
        }

        public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new CsmResultItem.ConstructorResultItem(ctr, substituteExp, FAKE_PRIORITY, isDeclaration, instantiateTypes);
        }

        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new CsmResultItem.GlobalFunctionResultItem(fun, substituteExp, FAKE_PRIORITY, isDeclaration, instantiateTypes);
        }

        public CsmResultItem.GlobalVariableResultItem createGlobalVariableResultItem(CsmVariable var) {
            return new CsmResultItem.GlobalVariableResultItem(var, FAKE_PRIORITY);
        }

        public CsmResultItem.LocalVariableResultItem createLocalVariableResultItem(CsmVariable var) {
            return new CsmResultItem.LocalVariableResultItem(var, FAKE_PRIORITY);
        }

        public CsmResultItem.FileLocalVariableResultItem createFileLocalVariableResultItem(CsmVariable var) {
            return new CsmResultItem.FileLocalVariableResultItem(var, FAKE_PRIORITY);
        }

        public CsmResultItem.FileLocalFunctionResultItem createFileLocalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new CsmResultItem.FileLocalFunctionResultItem(fun, substituteExp, FAKE_PRIORITY, isDeclaration, instantiateTypes);
        }

        public CsmResultItem.MacroResultItem createGlobalMacroResultItem(CsmMacro mac) {
            return new CsmResultItem.MacroResultItem(mac, FAKE_PRIORITY);
        }

        public CsmResultItem.MacroResultItem createFileIncludedLibMacroResultItem(CsmMacro mac) {
            return createGlobalMacroResultItem(mac);
        }

        public CsmResultItem.MacroResultItem createLibMacroResultItem(CsmMacro mac) {
            return createGlobalMacroResultItem(mac);
        }

        public CsmResultItem.GlobalVariableResultItem createLibGlobalVariableResultItem(CsmVariable var) {
            return createGlobalVariableResultItem(var);
        }

        public CsmResultItem.EnumeratorResultItem createLibGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return createGlobalEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN);
        }

        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean instantiateTypes) {
            return createGlobalFunctionResultItem(fun, substituteExp, false, instantiateTypes);
        }

        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
            return createNamespaceResultItem(pkg, displayFullNamespacePath);
        }

        public CsmResultItem.NamespaceAliasResultItem createLibNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath) {
            return createNamespaceAliasResultItem(alias, displayFullNamespacePath);
        }

        public TemplateParameterResultItem createTemplateParameterResultItem(CsmTemplateParameter par) {
            return new CsmResultItem.TemplateParameterResultItem(par, FAKE_PRIORITY);
        }

        public CsmResultItem createLabelResultItem(CsmLabel csmStatement) {
            return new CsmResultItem.LabelResultItem(csmStatement, FAKE_PRIORITY);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // convert data into CompletionItem
    private static List<CompletionItem> convertData(Collection dataList, int classDisplayOffset, CsmCompletionExpression substituteExp, int substituteOffset, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
        List<CompletionItem> ret = new ArrayList<CompletionItem>();
        for (Object obj : dataList) {
            CsmResultItem item = createResultItem(obj, classDisplayOffset, substituteExp, contextElement, instantiateTypes);
            assert item != null : "why null item? object " + obj;
            if (item != null) {
                item.setSubstituteOffset(substituteOffset);
                ret.add(item);
            }
        }
        return ret;
    }

    private static CsmResultItem createResultItem(Object obj, int classDisplayOffset, CsmCompletionExpression substituteExp, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
        if (CsmKindUtilities.isCsmObject(obj)) {
            CsmObject csmObj = (CsmObject) obj;
            assert (!CsmKindUtilities.isMethod(csmObj) || CsmKindUtilities.isMethodDeclaration(csmObj)) : "completion result can not have method definitions " + obj;
            if (CsmKindUtilities.isNamespace(csmObj)) {
                return getCsmItemFactory().createNamespaceResultItem((CsmNamespace) csmObj, false);
            } else if (CsmKindUtilities.isEnum(csmObj)) {
                return getCsmItemFactory().createEnumResultItem((CsmEnum) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isEnumerator(csmObj)) {
                return getCsmItemFactory().createGlobalEnumeratorResultItem((CsmEnumerator) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isClass(csmObj)) {
                return getCsmItemFactory().createClassResultItem((CsmClass) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isClassForwardDeclaration(csmObj)) {
                return getCsmItemFactory().createForwardClassResultItem((CsmClassForwardDeclaration) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isField(csmObj)) {
                return getCsmItemFactory().createFieldResultItem((CsmField) csmObj);
            } else if (CsmKindUtilities.isConstructor(csmObj)) { // must be checked before isMethod, because constructor is method too
                return getCsmItemFactory().createConstructorResultItem((CsmConstructor) csmObj, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            } else if (CsmKindUtilities.isMethodDeclaration(csmObj)) {
                return getCsmItemFactory().createMethodResultItem((CsmMethod) csmObj, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            } else if (CsmKindUtilities.isGlobalFunction(csmObj)) {
                if (CsmBaseUtilities.isFileLocalFunction((CsmFunction) csmObj)) {
                    return getCsmItemFactory().createFileLocalFunctionResultItem((CsmFunction) csmObj, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
                } else {
                    return getCsmItemFactory().createGlobalFunctionResultItem((CsmFunction) csmObj, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
                }
            } else if (CsmKindUtilities.isGlobalVariable(csmObj)) {
                return getCsmItemFactory().createGlobalVariableResultItem((CsmVariable) csmObj);
            } else if (CsmKindUtilities.isFileLocalVariable(csmObj)) {
                return getCsmItemFactory().createFileLocalVariableResultItem((CsmVariable) csmObj);
            } else if (CsmKindUtilities.isLocalVariable(csmObj)) {
                return getCsmItemFactory().createLocalVariableResultItem((CsmVariable) csmObj);
            } else if (CsmKindUtilities.isMacro(csmObj)) {
                return getCsmItemFactory().createGlobalMacroResultItem((CsmMacro) csmObj);
            } else if (CsmKindUtilities.isTypedef(csmObj)) {
                return getCsmItemFactory().createTypedefResultItem((CsmTypedef) csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isStatement(csmObj)) {
                return getCsmItemFactory().createLabelResultItem((CsmLabel) csmObj);
            } else if (CsmKindUtilities.isNamespaceAlias(csmObj)) {
                return getCsmItemFactory().createNamespaceAliasResultItem((CsmNamespaceAlias) csmObj, false);
            } else if(csmObj instanceof CsmTemplateBasedReferencedObject) {
                return new TemplateBasedReferencedObjectResultItem((CsmObject) obj);
            }
        }
        return null;
    }

    private static List<CompletionItem> convertData(CompletionResolver.Result res, int classDisplayOffset, CsmCompletionExpression substituteExp, int substituteOffset, CsmOffsetableDeclaration contextElement, boolean instantiateTypes) {
        if (res == null) {
            return Collections.<CompletionItem>emptyList();
        }
        List<CompletionItem> out = new ArrayList<CompletionItem>(res.size());
        CsmItemFactory factory = getCsmItemFactory();
        CsmResultItem item;
        for (CsmVariable elem : res.getLocalVariables()) {
            item = factory.createLocalVariableResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmTemplateParameter elem : res.getTemplateparameters()) {
            item = factory.createTemplateParameterResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmField elem : res.getClassFields()) {
            item = factory.createFieldResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmEnumerator elem : res.getClassEnumerators()) {
            item = factory.createMemberEnumeratorResultItem(elem, classDisplayOffset, false);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmMethod elem : res.getClassMethods()) {
            if (CsmKindUtilities.isConstructor(elem)) {
                item = factory.createConstructorResultItem((CsmConstructor) elem, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            } else {
                item = factory.createMethodResultItem(elem, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            }
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmClassifier elem : res.getProjectClassesifiersEnums()) {
            if (CsmKindUtilities.isClass(elem)) {
                item = factory.createClassResultItem((CsmClass) elem, classDisplayOffset, false);
            } else if (CsmKindUtilities.isClassForwardDeclaration(elem)) {
                CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) elem;
                if (fd.getCsmClass() != null) {
                    item = factory.createClassResultItem(fd.getCsmClass(), classDisplayOffset, false);
                } else {
                    // TODO fix me!
                    continue;
                }
            } else if (CsmKindUtilities.isTypedef(elem)) {
                item = factory.createTypedefResultItem((CsmTypedef) elem, classDisplayOffset, false);
            } else {
                assert CsmKindUtilities.isEnum(elem);
                item = factory.createEnumResultItem((CsmEnum) elem, classDisplayOffset, false);
            }
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmVariable elem : res.getFileLocalVars()) {
            item = factory.createFileLocalVariableResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmEnumerator elem : res.getFileLocalEnumerators()) {
            item = factory.createFileLocalEnumeratorResultItem(elem, classDisplayOffset, false);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmMacro elem : res.getFileLocalMacros()) {
            item = factory.createFileLocalMacroResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmFunction elem : res.getFileLocalFunctions()) {
            item = factory.createFileLocalFunctionResultItem(elem, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmMacro elem : res.getInFileIncludedProjectMacros()) {
            item = factory.createFileIncludedProjectMacroResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmVariable elem : res.getGlobalVariables()) {
            item = factory.createGlobalVariableResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmEnumerator elem : res.getGlobalEnumerators()) {
            item = factory.createGlobalEnumeratorResultItem(elem, classDisplayOffset, false);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmMacro elem : res.getGlobalProjectMacros()) {
            item = factory.createGlobalMacroResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmFunction elem : res.getGlobalProjectFunctions()) {
            item = factory.createGlobalFunctionResultItem(elem, substituteExp, isDeclaration(substituteExp, contextElement), instantiateTypes);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmNamespace elem : res.getGlobalProjectNamespaces()) {
            item = factory.createNamespaceResultItem(elem, false);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmNamespaceAlias elem : res.getProjectNamespaceAliases()) {
            item = factory.createNamespaceAliasResultItem(elem, false);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmClassifier elem : res.getLibClassifiersEnums()) {
            if (CsmKindUtilities.isClass(elem)) {
                item = factory.createLibClassResultItem((CsmClass) elem, classDisplayOffset, false);
            } else if (CsmKindUtilities.isTypedef(elem)) {
                item = factory.createLibTypedefResultItem((CsmTypedef) elem, classDisplayOffset, false);
            } else {
                assert CsmKindUtilities.isEnum(elem);
                item = factory.createLibEnumResultItem((CsmEnum) elem, classDisplayOffset, false);
            }
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmMacro elem : res.getInFileIncludedLibMacros()) {
            item = factory.createFileIncludedLibMacroResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmMacro elem : res.getLibMacros()) {
            item = factory.createLibMacroResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmVariable elem : res.getLibVariables()) {
            item = factory.createLibGlobalVariableResultItem(elem);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmEnumerator elem : res.getLibEnumerators()) {
            item = factory.createLibGlobalEnumeratorResultItem(elem, classDisplayOffset, false);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmFunction elem : res.getLibFunctions()) {
            item = factory.createLibGlobalFunctionResultItem(elem, substituteExp, instantiateTypes);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmNamespace elem : res.getLibNamespaces()) {
            item = factory.createLibNamespaceResultItem(elem, false);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }

        for (CsmNamespaceAlias elem : res.getLibNamespaceAliases()) {
            item = factory.createLibNamespaceAliasResultItem(elem, false);
            assert item != null;
            item.setSubstituteOffset(substituteOffset);
            out.add(item);
        }
        return out;
    }

    private static boolean isDeclaration(CsmCompletionExpression substituteExp, CsmOffsetableDeclaration scopeElement) {
        int expId = substituteExp.getExpID();
        return scopeElement == null && (expId == CsmCompletionExpression.VARIABLE || expId == CsmCompletionExpression.SCOPE || expId == CsmCompletionExpression.SCOPE_OPEN);
    }

    private static class TemplateBasedReferencedObjectImpl implements CsmTemplateBasedReferencedObject {

        public int getNameStartOffset() {
            return 0;
        }

        public int getNameEndOffset() {
            return 0;
        }

        public CharSequence getName() {
            return "template based ref object"; // NOI18N
        }

        public CsmFile getContainingFile() {
            return null;
        }

        public int getStartOffset() {
            return 0;
        }

        public int getEndOffset() {
            return 0;
        }

        public Position getStartPosition() {
            return null;
        }

        public Position getEndPosition() {
            return null;
        }

        public CharSequence getText() {
            return getName();
        }
    }

    private static class TemplateBasedReferencedObjectResultItem extends CsmResultItem {

        TemplateBasedReferencedObjectResultItem(CsmObject obj) {
            super(obj, 0);
        }

        @Override
        public String getItemText() {
            return "TemplateBasedReferencedObjectResultItem for " + getAssociatedObject(); // NOI18N
        }

        @Override
        protected Component getPaintComponent(boolean isSelected) {
            return new CsmPaintComponent() {

                @Override
                protected void draw(Graphics g) {
                }

                @Override
                public String toString() {
                    return "fake TemplateBasedReferencedObjectResultItem paint component"; // NOI18N
                }
            };
        }

        @Override
        public String toString() {
            return "TemplateBasedReferencedObjectResultItem for " + getAssociatedObject(); // NOI18N
        }
    }
}

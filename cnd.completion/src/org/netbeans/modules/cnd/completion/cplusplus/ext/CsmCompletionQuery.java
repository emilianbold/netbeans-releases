/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus.ext;
import java.text.MessageFormat;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.editor.cplusplus.CCSettingsNames;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.completion.csm.CompletionResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
* 
*
* @author Vladimir Voskresensky
* @version 1.00
*/

abstract public class CsmCompletionQuery implements CompletionQuery {

    private BaseDocument baseDocument;
    
    private static final String NO_SUGGESTIONS = NbBundle.getMessage(CsmCompletionQuery.class, "completion-no-suggestions");
    private static final String PROJECT_BEEING_PARSED = NbBundle.getMessage(CsmCompletionQuery.class, "completion-project-beeing-parsed");

    private static final boolean TRACE_COMPLETION = Boolean.getBoolean("cnd.completion.trace");
    
    private static CsmItemFactory itemFactory;

    // the only purpose of this method is that NbJavaCompletionQuery
    // can use it to retrieve baseDocument's fileobject and create correct
    // CompletionResolver with the correct classpath of project to which the file belongs
    protected BaseDocument getBaseDocument(){
        return baseDocument;
    }
    
    abstract protected  CompletionResolver getCompletionResolver(boolean openingSource, boolean sort);

    abstract protected CsmFinder getFinder();

    
    public CsmCompletionQuery(){
        super();
        initFactory();
    }
    
    protected void initFactory(){
        setCsmItemFactory(new CsmCompletionQuery.DefaultCsmItemFactory());        
    }
    
    public CompletionQuery.Result query(JTextComponent component, int offset,
                                        SyntaxSupport support) {
        boolean sort = false; // TODO: review
        return query(component, offset, support, false, sort);
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
    public CompletionQuery.Result query(JTextComponent component, int offset,
                                        SyntaxSupport support, boolean openingSource, boolean sort) {
        BaseDocument doc = (BaseDocument)component.getDocument();
        return query(component, doc, offset, support, openingSource, sort);
    }

    public CompletionQuery.Result query(JTextComponent component, BaseDocument doc, int offset,
                                        SyntaxSupport support, boolean openingSource, boolean sort) {    
        // remember baseDocument here. it is accessible by getBaseDocument()
        // method for subclasses of JavaCompletionQuery, ie. NbJavaCompletionQuery
        baseDocument = doc;
        
        CompletionQuery.Result ret = null;

        CsmSyntaxSupport sup = (CsmSyntaxSupport)support.get(CsmSyntaxSupport.class);

	if (sup == null || sup.isCompletionDisabled(offset)) {
	    return null;
	}
	
        try {
            // find last separator position
            int lastSepOffset = sup.getLastCommandSeparator(offset);
            CsmCompletionTokenProcessor tp = new CsmCompletionTokenProcessor(offset);
            tp.setJava15(false);

            boolean cont = true;
            while (cont) {
                sup.tokenizeText(tp, (((lastSepOffset + 1) <= offset) ? lastSepOffset + 1 : offset), offset, true);
                cont = tp.isStopped() && (lastSepOffset = sup.findMatchingBlock(tp.getCurrentOffest(), true)[0]) < offset - 1;
            }

            // Check whether there's an erroneous token state under the cursor
            boolean errState = false;
            TokenID lastValidTokenID = tp.getLastValidTokenID();
            if (lastValidTokenID != null) {
                switch (lastValidTokenID.getNumericID()) {
//                case CCTokenContext.MUL_ID:
//                    errState = true;
//                    break;
                case CCTokenContext.BLOCK_COMMENT_ID:
                    if (tp.getLastValidTokenText() == null
                            || !tp.getLastValidTokenText().endsWith("*/") // NOI18N
                    ) {
                        errState = true;
                    }
                    break;

                case CCTokenContext.LINE_COMMENT_ID:
                    errState = true;
                    break;
                default:
                    errState = tp.isErrorState();
                }
            }

            if (!errState) {
                // refresh classes info before querying
                sup.refreshClassInfo();

                CsmCompletionExpression exp = tp.getResultExp();
                if (TRACE_COMPLETION) {
                    System.err.println("expression " + exp);
                }
                ret = getResult(component, sup, openingSource, offset, exp, sort);
            } else if (TRACE_COMPLETION) {
                System.err.println("Error expression " + tp.getResultExp());
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return ret;
    }

    abstract protected boolean isProjectBeeingParsed(boolean openingSource);
        
    protected CompletionQuery.Result getResult(JTextComponent component, CsmSyntaxSupport sup, boolean openingSource, int offset, CsmCompletionExpression exp, boolean sort) {
	CompletionResolver resolver = getCompletionResolver(openingSource, sort);
        if (resolver != null) {
            CsmOffsetableDeclaration context = sup.getDefinition(offset);
            Context ctx = new Context(component, sup, openingSource, offset, getFinder(), resolver, context, sort);
            ctx.resolveExp(exp);
            if (TRACE_COMPLETION) {
                CompletionItem[] array =  ctx.result == null ? new CompletionItem[0] : (CompletionItem[])ctx.result.getData().toArray(new CompletionItem[ctx.result.getData().size()]);
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
            return new CsmCompletionResult(component, getBaseDocument(), Collections.EMPTY_LIST, "", exp, 0, isProjectBeeingParsed);
        }
//	CompletionQuery.Result result = null;
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

    // ================= help methods for sorting csm results =================
    protected static boolean isCaseSensitive(Class kitClass) {
        boolean b = SettingsUtil.getBoolean(kitClass,
            ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
            ExtSettingsDefaults.defaultCompletionCaseSensitive);
        return b;
    }
    
    protected static boolean isNaturalSort(Class kitClass) {
        boolean b = SettingsUtil.getBoolean(kitClass,
            ExtSettingsNames.COMPLETION_NATURAL_SORT,
            ExtSettingsDefaults.defaultCompletionNaturalSort);
        return b;
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
    
    static List findNestedClassifiers(CsmFinder finder, CsmOffsetableDeclaration context, CsmClassifier classifier, String name,
                                     boolean exactMatch, boolean inspectParentClasses, boolean sort) {
        // Find inner classes
        List ret = new ArrayList();
        classifier = CsmBaseUtilities.getOriginalClassifier(classifier);
        if (!CsmKindUtilities.isClass(classifier)) {
            return ret;
        }
        CsmClass cls = (CsmClass)classifier;

        // Add fields
        List res = finder.findNestedClassifiers(context, cls, name, exactMatch, inspectParentClasses, sort);
        if (res != null) {
            ret.addAll(res);
        }
        
        return ret;
    }
    
    static List findFieldsAndMethods(CsmFinder finder, CsmOffsetableDeclaration context, CsmClassifier classifier, String name,
                                     boolean exactMatch, boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier,boolean sort) {
        // Find inner classes
        List ret = new ArrayList();
        classifier = CsmBaseUtilities.getOriginalClassifier(classifier);
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
        List res = finder.findFields(context, cls, name, exactMatch, staticOnly, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier,sort);
        if (res != null) {
            ret.addAll(res);
        }
        // add enumerators
        res = finder.findEnumerators(context, cls, name, exactMatch, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier,sort);
        if (res != null) {
            ret.addAll(res);
        }

        // in global context add all methods, but only direct ones
        if (contextFunction == null) {
            staticOnly = false;
            context = cls;
        }
        // Add methods
        res = finder.findMethods(context, cls, name, exactMatch, staticOnly, inspectOuterClasses, inspectParentClasses, scopeAccessedClassifier,sort);
        if (res != null) {
            ret.addAll(res);
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
    
    class Context {

        private boolean sort;
        
        /** Text component */
        private JTextComponent component;

        /**
         * Syntax support for the given baseDocument
         */
        private CsmSyntaxSupport sup;

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
        private CsmFinder finder;

        /** Completion resolver associated with this Context. */
        private CompletionResolver compResolver;

        /** function or class in context */
        private CsmOffsetableDeclaration contextElement;

        public Context(JTextComponent component,
                       CsmSyntaxSupport sup, boolean openingSource, int endOffset,
                       CsmFinder finder,
                        CompletionResolver compResolver, CsmOffsetableDeclaration contextElement, boolean sort) {
            this.component = component;
            this.sup = sup;
            this.openingSource = openingSource;
            this.endOffset = endOffset;
	    this.finder = finder;
            this.compResolver = compResolver;
            this.contextElement = contextElement;      
            this.sort = sort;
        }
        
//        public Context(JTextComponent component,
//                       CsmSyntaxSupport sup, boolean openingSource, int endOffset,
//                       JCFinder jcFinder, CsmFinder finder) {
//            this.component = component;
//            this.sup = sup;
//            this.openingSource = openingSource;
//            this.endOffset = endOffset;
////            this.jcFinder= jcFinder;
//	    this.finder = finder;
//        }

        public void setFindType(boolean findType) {
            this.findType = findType;
        }

        protected Object clone() {
//            return new Context(component, sup, openingSource, endOffset, jcFinder, finder);
            return new Context(component, sup, openingSource, endOffset, finder, compResolver, contextElement, sort);
        }

        private CsmType resolveType(CsmCompletionExpression exp) {
            Context ctx = (Context)clone();
            ctx.setFindType(true);
            CsmType typ = null;
            if (ctx.resolveExp(exp)) {
                typ = ctx.lastType;
            }
            return typ;
        }
        
        private boolean isProjectBeeingParsed() {
            return CsmCompletionQuery.this.isProjectBeeingParsed(openingSource);
        }
        
        boolean resolveExp(CsmCompletionExpression exp) {
            boolean lastDot = false; // dot at the end of the whole expression?
            boolean ok = true;

//            if (exp.getExpID() == CsmCompletionExpression.CPPINCLUDE) { // #include statement
//                exp = exp.getParameterCount() == 2 ? exp.getParameter(1) : exp.getParameter(0);
//                return false;
//            }

            switch (exp.getExpID()) {
            case CsmCompletionExpression.DOT_OPEN: // Dot expression with the dot at the end
            case CsmCompletionExpression.ARROW_OPEN: // Arrow expression with the arrow at the end
                lastDot = true;
                // let it flow to DOT
            case CsmCompletionExpression.DOT: // Dot expression
            case CsmCompletionExpression.ARROW: // Arrow expression
                int parmCnt = exp.getParameterCount(); // Number of items in the dot exp
                ExprKind kind = (exp.getExpID() == CsmCompletionExpression.ARROW || exp.getExpID() == CsmCompletionExpression.ARROW_OPEN) ?
                                ExprKind.ARROW : ExprKind.DOT;
                for (int i = 0; i < parmCnt && ok; i++) { // resolve all items in a dot exp
                    ok = resolveItem(exp.getParameter(i), (i == 0),
                                     (!lastDot && i == parmCnt - 1),
                                    kind);
                }

                if (ok && lastDot) { // Found either type or package help
                    // Need to process dot at the end of the expression
                    int tokenCntM1 = exp.getTokenCount() - 1;
                    int substPos = exp.getTokenOffset(tokenCntM1) + exp.getTokenLength(tokenCntM1);
                    if (lastType != null) { // Found type
                        CsmClassifier cls;
                        if (lastType.getArrayDepth() == 0) { // Not array
                            cls = lastType.getClassifier();
                        } else { // Array of some depth
                            cls = CsmCompletion.OBJECT_CLASS_ARRAY; // Use Object in this case
                        }
                        List res;
                        if (openingSource) {
                            res = new ArrayList();
                            res.add(lastType.getClassifier());
                        } else { // not source-help
//                            CsmClass curCls = sup.getClass(exp.getTokenOffset(tokenCntM1));
//                            res = findFieldsAndMethods(finder, curCls == null ? null : getNamespaceName(curCls), 
//                                    cls, "", false, staticOnly, false); // NOI18N
                            res = findFieldsAndMethods(finder, contextElement, cls, "", false, staticOnly, false, true,this.scopeAccessedClassifier,sort); // NOI18N
                        }
                        // Get all fields and methods of the cls
                        result = new CsmCompletionResult(component, getBaseDocument(), res, formatType(lastType, true, true, true),
                                                exp, substPos, 0, cls.getName().length() + 1, isProjectBeeingParsed());
                    } else { // Found package (otherwise ok would be false)
                        if (true) {
                            // in C++ it's not legal to have NS-> or NS.
                            result = null;
                            break;
                        }
                        String searchPkg = (lastNamespace.isGlobal() ? "" : lastNamespace.getName()) + CsmCompletion.SCOPE;
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
                                if (cmdStartIdx < 0) {
                                    text = sup.getDocument().getText(0, firstTokenIdx);
                                    cmdStartIdx = text.lastIndexOf(0x0A);
                                    if (cmdStartIdx != -1) {
                                        text = text.substring(cmdStartIdx + 1);
                                    }
                                } else {
                                    text = sup.getDocument().getText(cmdStartIdx, firstTokenIdx - cmdStartIdx);
                                }
                            } catch (BadLocationException e) {
                                // ignore and provide full list of items
                            }

                            if (text != null && -1 == text.indexOf("package")) { //NOI18N
                                res.addAll(finder.findClasses(lastNamespace, "", false, false)); // package classes
                            }
                        }
                        result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*',
                                                exp, substPos, 0, 0, isProjectBeeingParsed());
                    }
                }
                break;
                
            case CsmCompletionExpression.SCOPE_OPEN: // Scope expression with the arrow at the end
                lastDot = true;
                // let it flow to SCOPE
            case CsmCompletionExpression.SCOPE: // Scope expression
                staticOnly = true;
                parmCnt = exp.getParameterCount(); // Number of items in the dot exp

                for (int i = 0; i < parmCnt && ok; i++) { // resolve all items in a dot exp
                    ok = resolveItem(exp.getParameter(i), (i == 0),
                                     (!lastDot && i == parmCnt - 1),
                                    ExprKind.SCOPE);
                }

                if (ok && lastDot) { // Found either type or namespace help
                    // Need to process dot at the end of the expression
                    int tokenCntM1 = exp.getTokenCount() - 1;
                    int substPos = exp.getTokenOffset(tokenCntM1) + exp.getTokenLength(tokenCntM1);
                    if (lastType != null) { // Found type
                        CsmClassifier cls;
                        if (lastType.getArrayDepth() == 0) { // Not array
                            cls = lastType.getClassifier();
                        } else { // Array of some depth
                            cls = CsmCompletion.OBJECT_CLASS_ARRAY; // Use Object in this case
                        }
                        List res;
                        if (openingSource) {
                            res = new ArrayList();
                            res.add(lastType.getClassifier());
                        } else { // not source-help
//                            CsmClass curCls = sup.getClass(exp.getTokenOffset(tokenCntM1));
//                            res = findFieldsAndMethods(finder, curCls == null ? null : getNamespaceName(curCls), 
//                                    cls, "", false, staticOnly, false); // NOI18N
                            res = findFieldsAndMethods(finder, contextElement, cls, "", false, staticOnly, false, true,this.scopeAccessedClassifier,sort); // NOI18N
                            List nestedClassifiers = findNestedClassifiers(finder, contextElement, cls, "", false, true, sort);
                            res.addAll(nestedClassifiers);                            
                        }
                        // Get all fields and methods of the cls
                        result = new CsmCompletionResult(component, getBaseDocument(), res, formatType(lastType, true, true, true),
                                                exp, substPos, 0, 0/*cls.getName().length() + 1*/, isProjectBeeingParsed());
                    } else { // Found package (otherwise ok would be false)
                        String searchPkg = (lastNamespace.isGlobal() ? "" : lastNamespace.getName()) + CsmCompletion.SCOPE;
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
                                if (cmdStartIdx < 0) {
                                    text = sup.getDocument().getText(0, firstTokenIdx);
                                    cmdStartIdx = text.lastIndexOf(0x0A);
                                    if (cmdStartIdx != -1) {
                                        text = text.substring(cmdStartIdx + 1);
                                    }
                                } else {
                                    text = sup.getDocument().getText(cmdStartIdx, firstTokenIdx - cmdStartIdx);
                                }
                            } catch (BadLocationException e) {
                                // ignore and provide full list of items
                            }

                            if (text != null && -1 == text.indexOf("namespace")) { //NOI18N
                                res.addAll(finder.findNamespaceElements(lastNamespace, "", false, false)); // namespace elements //NOI18N
                            }
                        }
                        result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*',  //NOI18N
                                                exp, substPos, 0, 0, isProjectBeeingParsed());
                    }
                }
                break;
                
            case CsmCompletionExpression.NEW: // 'new' keyword
                List res = finder.findClasses(null, "", false, false); // Find all classes by name // NOI18N
                result = new CsmCompletionResult(component, getBaseDocument(), res, "*", exp, endOffset, 0, 0, isProjectBeeingParsed()); // NOI18N
                break;

            case CsmCompletionExpression.CASE:
                // TODO: check with NbJavaJMICompletionQuery
                // FIXUP: now just analyze expression after "case "
                exp = exp.getParameter(0);
                
            default: // The rest of the situations is resolved as a singleton item
                ok = resolveItem(exp, true, true, ExprKind.NONE);
                break;
            }

            return ok;
        }

        /** Resolve one item from the expression connected by dots.
        * @param item expression item to resolve
        * @param first whether this expression is the first one in a dot expression
        * @param last whether this expression is the last one in a dot expression
        */
        boolean resolveItem(CsmCompletionExpression item, boolean first, boolean last, ExprKind kind) {
            boolean cont = true; // whether parsing should continue or not
            boolean methodOpen = false; // helper flag for unclosed methods

            switch (item.getExpID()) {
            case CsmCompletionExpression.CONSTANT: // Constant item
                if (first) {
                    lastType = CsmCompletion.getPredefinedType(item.getType()); // Get the constant type
                    staticOnly = false;
                } else { // Not the first item in a dot exp
                    cont = false; // impossible to have constant inside the expression
                }
                break;            
                
            case CsmCompletionExpression.VARIABLE: // Variable or special keywords
                switch (item.getTokenID(0).getNumericID()) {
                    case CCTokenContext.THIS_ID: // 'this' keyword
                        if (first) { // first item in expression
                            CsmClass cls = sup.getClass(item.getTokenOffset(0));
                            if (cls != null) {
                                lastType = CsmCompletion.getType(cls, 0);
                                staticOnly = false;
                            }
                        } else { // 'something.this'
                            staticOnly = false;
                        }
                        break;

//                    case CCTokenContext.SUPER_ID: // 'super' keyword
//                        if (first) { // only allowed as the first item
//                            CsmClass cls = sup.getClass(item.getTokenOffset(0));
//                            if (cls != null) {
//                                cls = finder.getExactClass(cls.getFullName());
//                                if (cls != null) {
//                                    cls = cls.getSuperclass();
//                                    if (cls != null) {
//                                        lastType = CsmCompletion.getType(cls, 0);
//                                        staticOnly = false;
//                                    }
//                                }
//                            }
//                        } else {
//                            cont = false;
//                        }
//                        break;

//                    case CCTokenContext.CLASS_ID: // 'class' keyword
//                        if (!first) {
//                            lastType = CsmCompletion.CLASS_TYPE;
//                            staticOnly = false;
//                        } else {
//                            cont = false;
//                        }
//                        break;

                    default: // Regular constant
                        String var = item.getTokenText(0);
                        int varPos = item.getTokenOffset(0);
                        if (first) { // try to find variable for the first item
                            if (last && !findType) { // both first and last item
                                CompletionResolver.Result res = null;
                                compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                                if (compResolver.refresh() && compResolver.resolve(varPos, var, openingSource)) {
                                    res = compResolver.getResult();
                                }
//                                CsmClass cls = sup.getClass(varPos); // get baseDocument class
//                                if (cls != null) {
//                                    res.addAll(findFieldsAndMethods(finder, getNamespaceName(cls), cls, var, false,
//                                                                    sup.isStaticBlock(varPos), true));
//                                }
//                                if (var.length() > 0 || !openingSource) {
//                                    res.addAll(finder.findNestedNamespaces(var, false, false)); // add matching packages
//                                    if (var.length() > 0) { // if at least one char
//                                        res.addAll(finder.findClasses(null, var, false)); // add matching classes
//                                        if (cls!=null){
//                                            // add matching inner classes too
////XXX                                            JCPackage pkg = finder.getExactPackage(cls.getPackageName());
////                                            List lst = finder.findClasses(pkg, cls.getName()+"."+var, false); // NOI18N
////                                            for (int i=0; i<lst.size(); i++){
////                                                if (!res.contains(lst.get(i))){
////                                                    res.add(lst.get(i));
////                                                }
////                                            }
//                                        }
//                                        
//                                        List importedCls = sup.getImportedInnerClasses();
//                                        for (int i=0; i<importedCls.size(); i++){
//                                            CsmClass iCls = (CsmClass)importedCls.get(i);
//                                            if (iCls.getName().indexOf("."+var)>0 && !res.contains(iCls)){ // NOI18N
//                                                res.add(iCls);
//                                            }
//                                        }
//                                    }
//                                    
//                                }
                                result = new CsmCompletionResult(component, getBaseDocument(), res, var + '*', item, 0, isProjectBeeingParsed());  //NOI18N
                            } else { // not last item or finding type
                                if (kind != ExprKind.SCOPE) {
                                    lastType = (CsmType)sup.findType(var, varPos);
                                }
                                if (lastType != null) { // variable found
                                    staticOnly = false;
                                } else { // no variable found
                                    if (kind == ExprKind.SCOPE) {
                                        scopeAccessedClassifier = true;
                                    }
                                    lastNamespace = kind != ExprKind.SCOPE ? null : finder.getExactNamespace(var); // try package
                                    if (lastNamespace == null) { // not package, let's try class name
                                        CsmClass cls = sup.getClassFromName(var, true);
                                        if (cls == null) { // class not found
                                            // try now resolver
                                            if (kind == ExprKind.SCOPE) {
                                                lastNamespace = findExactNamespace(var, varPos);
                                            }
                                            if (lastNamespace == null) {
                                                // try class name
                                                cls = kind == ExprKind.ARROW ? null : findExactClass(var, varPos);
                                                if (cls == null) {
                                                    cont = false;
                                                }
                                            }
                                        }
                                        if (cls != null) {
                                            lastType = CsmCompletion.getType(cls, 0);
                                        }
                                    }
                                }
                            }
                        } else { // not the first item
                            if (lastType != null) { // last was type
                                if (findType || !last) {
                                    boolean inner = false;
                                    int ad = lastType.getArrayDepth();
                                    if (staticOnly && ad == 0) { // can be inner class
                                        CsmClassifier cls = finder.getExactClassifier(lastType.getClassifier().getQualifiedName() + CsmCompletion.SCOPE + var);
                                        if (cls != null) {
                                            lastType = CsmCompletion.getType(cls, 0);
                                            inner = true;
                                        }
                                    }

                                    if (!inner) { // not inner class name
                                        if (ad == 0) { // zero array depth
                                            if (CsmKindUtilities.isClass(lastType.getClassifier())) {
                                                CsmClass clazz = (CsmClass)lastType.getClassifier();
                                                List fldList = finder.findFields(clazz, clazz, var, true, staticOnly, true, true,scopeAccessedClassifier, this.sort);
//                                                // add enumerators
//                                                List enumerators = finder.findEnumerators(clazz, clazz, var, true, true, true, this.sort);
//                                                if (enumerators != null) {
//                                                    fldList.addAll(enumerators);
//                                                }                                                
                                                if (fldList != null && fldList.size() > 0) { // match found
                                                    CsmField fld = (CsmField)fldList.get(0);
                                                    lastType = fld.getType();
                                                    staticOnly = false;
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
//                                    CsmClass curCls = sup.getClass(varPos);
                                    CsmClassifier cls;
                                    if (lastType.getArrayDepth() == 0) { // Not array
                                        cls = lastType.getClassifier();
                                    } else { // Array of some depth
                                        cls = CsmCompletion.OBJECT_CLASS_ARRAY; // Use Object in this case
                                    }
                                    List res = findFieldsAndMethods(finder, contextElement, cls, var, false, staticOnly, false, true,this.scopeAccessedClassifier,sort);
                                    List nestedClassifiers = findNestedClassifiers(finder, contextElement, cls, var, false, true, sort);
                                    res.addAll(nestedClassifiers);
                                    result = new CsmCompletionResult(
                                                 component, getBaseDocument(), 
//                                                 findFieldsAndMethods(finder, curCls == null ? null : getNamespaceName(curCls), cls, var, false, staticOnly, false),
                                                 res,
                                                 formatType(lastType, true, true, false) + var + '*',
                                                 item,
                                                 0/*cls.getName().length() + 1*/,
                                                 isProjectBeeingParsed());
                                }
                            } else { // currently package
                                String searchName = (lastNamespace.isGlobal() ? "" : (lastNamespace.getName() + CsmCompletion.SCOPE)) + var;
                                if (findType || !last) {
                                    lastNamespace = finder.getExactNamespace(searchName);
                                    if (lastNamespace == null) { // package doesn't exist
                                        CsmClassifier cls = finder.getExactClassifier(searchName);
                                        if (cls != null) {
                                            lastType = CsmCompletion.getType(cls, 0);
                                        } else {
                                            lastType = null;
                                            cont = false;
                                        }
                                    }
                                } else { // last and searching for completion output
                                    if (last) { // get all matching fields/methods/packages
                                        String searchPkg = (lastNamespace.isGlobal() ? "" : lastNamespace.getName()) + CsmCompletion.SCOPE + var;
                                        List res = finder.findNestedNamespaces(lastNamespace, var, openingSource, false); // find matching nested namespaces
                                        res.addAll(finder.findNamespaceElements(lastNamespace, var, openingSource, false)); // matching classes
                                        result = new CsmCompletionResult(component, getBaseDocument(), res, searchPkg + '*', item, 0, isProjectBeeingParsed());
                                    }
                                }
                            }
                        }
                        break;

                }
                break;

            case CsmCompletionExpression.ARRAY:
                cont = resolveItem(item.getParameter(0), first, false, ExprKind.NONE);
                if (cont) {
                    cont = false;
                    if (lastType != null) { // must be type
                        if (item.getParameterCount() == 2) { // index in array follows
                            CsmType arrayType = resolveType(item.getParameter(1));
                            if (arrayType != null && arrayType.equals(CsmCompletion.INT_TYPE)) {
                               lastType = CsmCompletion.getType(lastType.getClassifier(),
                                                    Math.max(lastType.getArrayDepth() - 1, 0));
                                cont = true;
                            }
                        } else { // no index, increase array depth
                            lastType = CsmCompletion.getType(lastType.getClassifier(),
                                                              lastType.getArrayDepth() + 1);
                            cont = true;
                        }
                    }
                }
                break;

            case CsmCompletionExpression.INSTANCEOF:
                lastType = CsmCompletion.BOOLEAN_TYPE;
                break;

            case CsmCompletionExpression.OPERATOR:
                CompletionResolver.Result res = null;
                CsmClass curCls = sup.getClass(item.getTokenOffset(0)); // 
//                if (curCls != null) { //find all methods and fields for "this" class
//                    res.addAll(findFieldsAndMethods(finder, getNamespaceName(curCls), curCls, "", false,
//                    res.addAll(findFieldsAndMethods(finder, curCls, curCls, "", false,
//                    sup.isStaticBlock(item.getTokenOffset(0)), true));
//                } else {
                    compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                    if (compResolver.refresh() && compResolver.resolve(item.getTokenOffset(0), "", openingSource)) {
                        res = compResolver.getResult();
                    }                     
//                }
//                res.addAll(finder.findNestedNamespaces("", false, false)); // find all packages
//                res.addAll(finder.findClasses(null, "", false)); // find all classes
                
                result = new CsmCompletionResult(component, getBaseDocument(), res, "*", item, endOffset, 0, 0, isProjectBeeingParsed()); // NOI18N
                 
                switch (item.getTokenID(0).getNumericID()) {
                    case CCTokenContext.EQ_ID: // Assignment operators
                    case CCTokenContext.PLUS_EQ_ID:
                    case CCTokenContext.MINUS_EQ_ID:
                    case CCTokenContext.MUL_EQ_ID:
                    case CCTokenContext.DIV_EQ_ID:
                    case CCTokenContext.AND_EQ_ID:
                    case CCTokenContext.OR_EQ_ID:
                    case CCTokenContext.XOR_EQ_ID:
                    case CCTokenContext.MOD_EQ_ID:
                    case CCTokenContext.LSHIFT_EQ_ID:
                    case CCTokenContext.RSSHIFT_EQ_ID:
//                    case CCTokenContext.RUSHIFT_EQ_ID:
                        if (item.getParameterCount() > 0) {
                            lastType = resolveType(item.getParameter(0));
                            staticOnly = false;
                        }
                        break;

                    case CCTokenContext.LT_ID: // Binary, result is boolean
                    case CCTokenContext.GT_ID:
                    case CCTokenContext.LT_EQ_ID:
                    case CCTokenContext.GT_EQ_ID:
                    case CCTokenContext.EQ_EQ_ID:
                    case CCTokenContext.NOT_EQ_ID:
                    case CCTokenContext.AND_AND_ID: // Binary, result is boolean
                    case CCTokenContext.OR_OR_ID:
                        lastType = CsmCompletion.BOOLEAN_TYPE;
                        break;

                    case CCTokenContext.LSHIFT_ID: // Always binary
                    case CCTokenContext.RSSHIFT_ID:
//                    case CCTokenContext.RUSHIFT_ID:
                    case CCTokenContext.MUL_ID:
                    case CCTokenContext.DIV_ID:
                    case CCTokenContext.AND_ID:
                    case CCTokenContext.OR_ID:
                    case CCTokenContext.XOR_ID:
                    case CCTokenContext.MOD_ID:

                    case CCTokenContext.PLUS_ID:
                    case CCTokenContext.MINUS_ID:
                        switch (item.getParameterCount()) {
                        case 2:
                            CsmType typ1 = resolveType(item.getParameter(0));
                            CsmType typ2 = resolveType(item.getParameter(1));
                            if (typ1 != null && typ2 != null
                                    && typ1.getArrayDepth() == 0
                                    && typ2.getArrayDepth() == 0
                                    && CsmCompletion.isPrimitiveClass(typ1.getClassifier())
                                    && CsmCompletion.isPrimitiveClass(typ2.getClassifier())
                               ) {
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
                        break;

                    case CCTokenContext.COLON_ID:
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

                    case CCTokenContext.QUESTION_ID:
                        if (item.getParameterCount() >= 2) {
                            lastType = resolveType(item.getParameter(1)); // should be colon
                        }
                        break;
                }
                break;

            case CsmCompletionExpression.UNARY_OPERATOR:
                if (item.getParameterCount() > 0) {
                    lastType = resolveType(item.getParameter(0));
                    staticOnly = false;
                }
                break;

            case CsmCompletionExpression.MEMBER_POINTER:
                if (item.getParameterCount() > 0) {
                    lastType = resolveType(item.getParameter(0));
                    staticOnly = false;
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
                lastType = resolveType(param);
                // TODO: we need to wrap lastType with pointer and address-of
                // based on the zero token of 'item' expression
                staticOnly = false;
               }
                break;
                
            case CsmCompletionExpression.TYPE:
                if (findType) {
                    lastType = CsmCompletion.getPredefinedType(item.getType());
                }
                if (!findType || lastType == null) {
                    // this is the case of code completion on parameter or unresolved predefined type
                    int nrTokens = item.getTokenCount();
                    if (nrTokens > 1) {
                        String varName = item.getTokenText(nrTokens - 1);
                        int varPos = item.getTokenOffset(nrTokens - 1);
                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_LOCAL_VARIABLES | CompletionResolver.RESOLVE_CLASSES);
                        if (compResolver.refresh() && compResolver.resolve(varPos, varName, false)) {
                            res = compResolver.getResult();
                            if (findType) {
                                CsmClassifier cls = null;
                                Iterator it = res.getProjectClassesifiersEnums().iterator();
                                if (!it.hasNext()) {
                                    it = res.getLibClassifiersEnums().iterator();
                                }
                                if (it.hasNext()) {
                                    cls = (CsmClassifier) it.next();
                                }
                                if (cls != null) {
                                    lastType = CsmCompletion.getType(cls, 0);
                                }
                            }
                            result = new CsmCompletionResult(component, getBaseDocument(), res, varName + '*', item, varPos, 0, 0, isProjectBeeingParsed());
                        }                  
                    }
                }
                break;

            case CsmCompletionExpression.PARENTHESIS:
                cont = resolveItem(item.getParameter(0), first, last, kind);
                break;

            case CsmCompletionExpression.CONSTRUCTOR: // constructor can be part of a DOT expression
                isConstructor = true;
                cont = resolveExp(item.getParameter(0));
                staticOnly = false;
                break;

            case CsmCompletionExpression.METHOD_OPEN: // Unclosed method
                methodOpen = true;
                // let it flow to method
            case CsmCompletionExpression.METHOD: // Closed method
                CsmCompletionExpression mtdNameExp = item.getParameter(0);
                String mtdName = mtdNameExp.getTokenText(0);

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
                    CsmClass cls = null;
                    if (first) {
                        cls = sup.getClassFromName(mtdName, true);
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
                    if (cls != null) {
                     lastType = CsmCompletion.getType(cls, 0);
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
                if (isConstructor == false) {
                    // Help for the method

                    // when use hyperlink => method() is passed as methodOpen, but we 
                    // want to resolve "method"
                    // otherwise we need all in current context
                    if (!methodOpen || openingSource) {
                        List mtdList = new ArrayList();
                        if (first) {
                            // resolve all functions in context
                            int varPos = mtdNameExp.getTokenOffset(0);
                            compResolver.setResolveTypes(CompletionResolver.RESOLVE_FUNCTIONS);
                            if (compResolver.refresh() && compResolver.resolve(varPos, mtdName, true)) {
                                compResolver.getResult().addResulItemsToCol(mtdList);
                            }
                        } else {
                            // if prev expression was resolved => get it's class
                            if (lastType != null) {
                                CsmClassifier classifier;
                                if (lastType.getArrayDepth() == 0) { // Not array
                                    classifier = lastType.getClassifier();
                                } else { // Array of some depth
                                    classifier = CsmCompletion.OBJECT_CLASS_ARRAY; // Use Object in this case
                                }
                                // try to find method in last resolved class appropriate for current context
                                if (CsmKindUtilities.isClass(classifier)) {
                                    mtdList = finder.findMethods(this.contextElement, (CsmClass)classifier, mtdName, true, false, first, true,scopeAccessedClassifier, this.sort);
                                }
                            }
                        }
                        if (mtdList == null || mtdList.size() == 0) {
                            lastType = null;
                            return false;
                        }
                        String parmStr = "*"; // NOI18N
                        List typeList = getTypeList(item, 1);
                        List filtered = sup.filterMethods(mtdList, typeList, methodOpen);
                        if (filtered.size() > 0) {
                            mtdList = filtered;
                            parmStr = formatTypeList(typeList, methodOpen);
                        }
                        if (mtdList.size() > 0) {
                            if (last && !findType) {
                                result = new CsmCompletionResult(component, getBaseDocument(), mtdList,
                                        formatType(lastType, true, true, false) + mtdName + '(' + parmStr + ')',
                                        item, endOffset, 0, 0, isProjectBeeingParsed());
                            } else {
                                if (mtdList.size() > 0) {
                                    lastType = ((CsmFunction)mtdList.get(0)).getReturnType();
                                    staticOnly = false;
                                }
                            }
                        } else {
                            lastType = null; // no method found
                            cont = false;
                        }
                    } else { // package.method() is invalid
                        // this is the case of code completion after opening paren "method(|"
                        int varPos = endOffset; // mtdNameExp.getTokenOffset(0);
                        compResolver.setResolveTypes(CompletionResolver.RESOLVE_CONTEXT);
                        if (compResolver.refresh() && compResolver.resolve(varPos, "", false)) {
                            res = compResolver.getResult();
                            result = new CsmCompletionResult(component, getBaseDocument(), res, mtdName + '*', mtdNameExp, varPos, 0, 0, isProjectBeeingParsed());
                        }                              

//                        } else {
//                            lastNamespace = null;
//                            cont = false;
//                        }
                    }
                }
                break;
            case CsmCompletionExpression.GENERIC_TYPE: // Closed method
                CsmType typ = resolveType(item.getParameter(0));
                if (typ != null) {
                    lastType = typ;
                }
                break;
            }

            if (lastType == null && lastNamespace == null) { // !!! shouldn't be necessary
                cont = false;
            }
            return cont;
        }

        private CsmNamespace findExactNamespace(final String var, final int varPos) {
            CsmNamespace ns = null;
            compResolver.setResolveTypes(CompletionResolver.RESOLVE_GLOB_NAMESPACES);      
            if (compResolver.refresh() && compResolver.resolve(varPos, var, true)) {
                CompletionResolver.Result res = compResolver.getResult();
                Iterator it = res.getGlobalProjectNamespaces().iterator();
                ns = it.hasNext() ? (CsmNamespace) it.next()  : null;
            }                            
            return ns;
        }

        private CsmClass findExactClass(final String var, final int varPos) {
            CsmClass cls = null;
            compResolver.setResolveTypes(CompletionResolver.RESOLVE_CLASSES);
            if (compResolver.refresh() && compResolver.resolve(varPos, var, true)) {
                CompletionResolver.Result res = compResolver.getResult();
                Iterator it = res.getProjectClassesifiersEnums().iterator();
                while (it.hasNext()) {
                    CsmObject obj = (CsmObject) it.next();
                    if (CsmKindUtilities.isClass(obj)) {
                        cls = (CsmClass)obj;
                        break;
                    }
                }
            }
            return cls;
        }
        
        private List getTypeList(CsmCompletionExpression item, int firstChildIdx) {
            int parmCnt = item.getParameterCount();
            ArrayList typeList = new ArrayList();
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
                CsmType t = (CsmType)typeList.get(i);
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

    public static class CsmCompletionResult extends CompletionQuery.DefaultResult {

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
        
        public CsmCompletionResult(JTextComponent component, BaseDocument doc, List data, String title,
                   CsmCompletionExpression substituteExp, int classDisplayOffset, boolean isProjectBeeingParsed) {
            this(component, doc, data, title, substituteExp, substituteExp.getTokenOffset(0),
                 substituteExp.getTokenLength(0), classDisplayOffset, isProjectBeeingParsed);
        }
      
        public CsmCompletionResult(JTextComponent component, BaseDocument doc, CompletionResolver.Result res, String title,
                   CsmCompletionExpression substituteExp, int classDisplayOffset, boolean isProjectBeeingParsed) {
            this(component, doc, res, title, substituteExp, substituteExp.getTokenOffset(0),
                 substituteExp.getTokenLength(0), classDisplayOffset, isProjectBeeingParsed);
        }
        
        public CsmCompletionResult(JTextComponent component, BaseDocument doc, CompletionResolver.Result res, String title,
                   CsmCompletionExpression substituteExp, int substituteOffset,
                   int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed) {
            this(component, doc, 
                    convertData(res, classDisplayOffset, substituteExp, substituteOffset), 
                    true, 
                    title, 
                    substituteExp, 
                    substituteOffset, 
                    substituteLength, classDisplayOffset, isProjectBeeingParsed);
        }
        
        public CsmCompletionResult(JTextComponent component, BaseDocument doc, List data, String title,
                   CsmCompletionExpression substituteExp, int substituteOffset,
                   int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed) {
            this(component, doc, convertData(data, classDisplayOffset, substituteExp, substituteOffset), true, title, substituteExp, substituteOffset, 
                    substituteLength, classDisplayOffset, isProjectBeeingParsed);
        }
        
        public CsmCompletionResult(JTextComponent component, BaseDocument doc, List data, boolean updateTitle, String title,
                   CsmCompletionExpression substituteExp, int substituteOffset,
                   int substituteLength, int classDisplayOffset, boolean isProjectBeeingParsed) {
            super(component, 
                    updateTitle ? getTitle(data, title, isProjectBeeingParsed) : title, 
                    data, 
                    substituteOffset, 
                    substituteLength);
            
            this.component = component;
            this.baseDocument = doc;
            this.substituteExp = substituteExp;
            this.substituteOffset = substituteOffset;
            this.substituteLength = substituteLength;
            this.classDisplayOffset = classDisplayOffset;
        }        
        
        private static String getTitle(List data, String origTitle, boolean isProjectBeeingParsed) {
            if (CsmUtilities.DEBUG) System.out.println("original title (resolved type) was " + origTitle); //NOI18N
            String out = NO_SUGGESTIONS;
            if (data != null && data.size() > 0) {
                out = origTitle;
            }
            if (isProjectBeeingParsed) {
                out = MessageFormat.format(PROJECT_BEEING_PARSED, new Object[] {out});
            }
            return out;
        }
        
        protected JTextComponent getComponent(){
            return component;
        }
        
        protected int getSubstituteLength(){
            return substituteLength;
        }
        
        public int getSubstituteOffset(){
            return substituteOffset;
        }
        
        protected CsmCompletionExpression getSubstituteExp(){
            return substituteExp;
        }
        
        protected int getClassDisplayOffset(){
            return classDisplayOffset;
        }
        
        /** Get the text that is normally filled into the text if enter is pressed. */
        protected String getMainText(Object dataItem) {
            String text = null;
            if (dataItem instanceof CsmResultItem) {
                dataItem = ((CsmResultItem)dataItem).getAssociatedObject();
            }
            if (CsmKindUtilities.isCsmObject(dataItem)) { 
                CsmObject csmObj = (CsmObject)dataItem;
                if (CsmKindUtilities.isClass(csmObj)) {
                    text = ((CsmClass)csmObj).getName();
                    if (classDisplayOffset > 0 && classDisplayOffset < text.length()) { // Only the last name for inner classes
                        text = text.substring(classDisplayOffset);
                    }
                } else if (CsmKindUtilities.isVariable(csmObj)) {
                    text = ((CsmVariable)csmObj).getName();
                } else if (CsmKindUtilities.isFunctionDeclaration(csmObj)) {
                    CsmFunction mtd = (CsmFunction)csmObj;
                    text = mtd.getName();
                }
            }
            return text;
        }

        /** Get the text that is common to all the entries in the query-result */
        protected String getCommonText(String prefix) {
            List data = getData();
            int cnt = data.size();
            int prefixLen = prefix.length();
            String commonText = null;
            for (int i = 0; i < cnt; i++) {
                String mainText = getMainText(data.get(i));
                if (mainText != null && mainText.startsWith(prefix)) {
                    mainText = mainText.substring(prefixLen);
                    if (commonText == null) {
                        commonText = mainText;
                    }
                    // Get largest common part
                    int minLen = Math.min(mainText.length(), commonText.length());
                    int commonInd;
                    for (commonInd = 0; commonInd < minLen; commonInd++) {
                        if (mainText.charAt(commonInd) != commonText.charAt(commonInd)) {
                            break;
                        }
                    }
                    if (commonInd != 0) {
                        commonText = commonText.substring(0, commonInd);
                    } else {
                        return null; // no common text
                    }
                }
            }
            return prefix + ((commonText != null) ? commonText : ""); // NOI18N
        }

        /** Update the text in response to pressing TAB key.
        * @return whether the text was successfully updated
        */
        public boolean substituteCommonText(int dataIndex) {
            
            List data = getData();
            if( data.size() == 0 ) return false;

            Object obj = getData().get( dataIndex );
            if (obj instanceof CompletionQuery.ResultItem){
                //return super.substituteCommonText(dataIndex); [PENDING] 
                // how to get getCommonText to CompletionQuery.ResultItem ???
            }
            
            BaseDocument doc = baseDocument;
            try {
                String prefix = doc.getText(substituteOffset, substituteLength);
                String commonText = getCommonText(prefix);
                if (commonText != null) {
                    if(substituteExp!=null){
                        if( (substituteExp.getExpID()==CsmCompletionExpression.METHOD_OPEN) || (substituteExp.getExpID()==CsmCompletionExpression.METHOD) ) 
                            return true;
                    }
                    doc.atomicLock();
                    try {
                        doc.remove(substituteOffset, substituteLength);
                        doc.insertString(substituteOffset, commonText, null);
                    } finally {
                        doc.atomicUnlock();
                    }
                }
            } catch (BadLocationException e) {
                // no updating
            }
            return true;
        }

        /** Update the text in response to pressing ENTER.
        * @return whether the text was successfully updated
        */
        public boolean substituteText(int dataIndex, boolean shift ) {
            Object actData = getData().get( dataIndex );
            if (actData instanceof CompletionQuery.ResultItem){
                return super.substituteText(dataIndex, shift);
            }
            
            // the rest part of code is here only for backward compatibility...
            // it should be removed later if all data will be CompletionQuery.ResultItem
            

            BaseDocument doc = baseDocument;
            String text = null;
            int selectionStartOffset = -1;
            int selectionEndOffset = -1;
            Object replacement = getData().get(dataIndex);

            if (CsmKindUtilities.isCsmObject(replacement)) {
                CsmObject csmRepl = (CsmObject)replacement;
                if (CsmKindUtilities.isClass(csmRepl)) {
                    text = ((CsmClass)csmRepl).getName();
                    if (classDisplayOffset > 0
                            && classDisplayOffset < text.length()
                       ) { // Only the last name for inner classes
                        text = text.substring(classDisplayOffset);
                    }

                } else if (CsmKindUtilities.isVariable(csmRepl)) {
                    text = ((CsmVariable)csmRepl).getName();

                } else if (CsmKindUtilities.isFunctionDeclaration(csmRepl)) {
                    CsmFunction mtd = (CsmFunction)csmRepl;
                    switch ((substituteExp != null) ? substituteExp.getExpID() : -1) {
                    case CsmCompletionExpression.METHOD:
                        // no substitution
                        break;

                    case CsmCompletionExpression.METHOD_OPEN:
                        CsmParameter[] parms = (CsmParameter[]) mtd.getParameters().toArray(new CsmParameter[0]);
                        if (parms.length == 0) {
                            text = ")"; // NOI18N
                        } else { // one or more parameters
                            int ind = substituteExp.getParameterCount();
                            boolean addSpace = false;
                            Formatter f = doc.getFormatter();
                            if (f instanceof ExtFormatter) {
                                Object o = ((ExtFormatter)f).getSettingValue(CCSettingsNames.CC_FORMAT_SPACE_AFTER_COMMA);
                                if ((o instanceof Boolean) && ((Boolean)o).booleanValue()) {
                                    addSpace = true;
                                }
                            }

                            try {
                                if (addSpace && (ind == 0 || (substituteOffset > 0
                                                              && Character.isWhitespace(doc.getText(substituteOffset - 1, 1).charAt(0))))
                                   ) {
                                    addSpace = false;
                                }
                            } catch (BadLocationException e) {
                            }

                            if (ind < parms.length) {
                                text = addSpace ? " " : ""; // NOI18N
                                selectionStartOffset = text.length();
                                text += parms[ind].getName();
                                selectionEndOffset = text.length();
                            }
                        }
                        break;

                    default:
                        text = getMainText(csmRepl);
                        boolean addSpace = false;
                        Formatter f = doc.getFormatter();
                        if (f instanceof ExtFormatter) {
                            Object o = ((ExtFormatter)f).getSettingValue(CCSettingsNames.CC_FORMAT_SPACE_BEFORE_PARENTHESIS);
                            if ((o instanceof Boolean) && ((Boolean)o).booleanValue()) {
                                addSpace = true;
                            }
                        }

                        if (addSpace) {
                            text += ' ';
                        }
                        text += '(';

                        parms = (CsmParameter[])mtd.getParameters().toArray(new CsmParameter[0]);
                        if (parms.length > 0) {
                            selectionStartOffset = text.length();
                            text += parms[0].getName();
                            selectionEndOffset = text.length();
                        } else {
                            text += ")"; // NOI18N
                        }
                        break;
                    }
                }
            }

            if (text != null) {
                // Update the text
                doc.atomicLock();
                try {
                    // bugfix of #41492
                    String textToReplace = doc.getText(substituteOffset, substituteLength);
                    if (text.equals(textToReplace)) return false;
                    doc.remove(substituteOffset, substituteLength);
                    doc.insertString(substituteOffset, text, null);
                    if (selectionStartOffset >= 0 && component != null) { // component could be null in non-UI tests
                        component.select(substituteOffset + selectionStartOffset,
                                         substituteOffset + selectionEndOffset);
                    }
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
            }

            return true;
             
        }
    }

    //========================== Items Factory ===============================

    protected void setCsmItemFactory(CsmItemFactory itemFactory){
        this.itemFactory = itemFactory;
    }
    
    public static CsmItemFactory getCsmItemFactory(){
        return itemFactory;
    }
 
    public interface CsmItemFactory{
        public CsmResultItem.LocalVariableResultItem createLocalVariableResultItem(CsmVariable var);
        public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld);
        public CsmResultItem.EnumeratorResultItem createMemberEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);
        public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp);
        public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp);

        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN);
        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN);
        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN);

        public CsmResultItem.FileLocalVariableResultItem createFileLocalVariableResultItem(CsmVariable var);
        public CsmResultItem.EnumeratorResultItem createFileLocalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);
        
        public CsmResultItem.MacroResultItem createFileLocalMacroResultItem(CsmMacro mac);
        public CsmResultItem.MacroResultItem createFileIncludedProjectMacroResultItem(CsmMacro mac);
        
        public CsmResultItem.GlobalVariableResultItem createGlobalVariableResultItem(CsmVariable var);
        public CsmResultItem.EnumeratorResultItem createGlobalEnumeratorResultItem(CsmEnumerator enm, int enumtrDisplayOffset, boolean displayFQN);
        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction mtd, CsmCompletionExpression substituteExp);
        public CsmResultItem.MacroResultItem createGlobalMacroResultItem(CsmMacro mac);

        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath);
        
        public CsmResultItem.ClassResultItem createLibClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN);
        public CsmResultItem.EnumResultItem createLibEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN);
        public CsmResultItem.TypedefResultItem createLibTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN);
        
        public CsmResultItem.MacroResultItem createFileIncludedLibMacroResultItem(CsmMacro mac);
        public CsmResultItem.MacroResultItem createLibMacroResultItem(CsmMacro mac);
        
        public CsmResultItem.GlobalVariableResultItem createLibGlobalVariableResultItem(CsmVariable var);
        public CsmResultItem.EnumeratorResultItem createLibGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN);
    
        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp);
        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath);
    }
    
    private static final int FAKE_PRIORITY = 1000;
    public static class DefaultCsmItemFactory implements CsmItemFactory{
        public DefaultCsmItemFactory(){
        }

        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
            return new CsmResultItem.NamespaceResultItem(pkg, displayFullNamespacePath, FAKE_PRIORITY);
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
        
        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            return new CsmResultItem.ClassResultItem(cls, classDisplayOffset, displayFQN, FAKE_PRIORITY);
        }
        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.EnumResultItem(enm, enumDisplayOffset, displayFQN, FAKE_PRIORITY);  
        }  
        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return new CsmResultItem.TypedefResultItem(def, classDisplayOffset, displayFQN, FAKE_PRIORITY); 
        }
        
        public CsmResultItem.ClassResultItem createLibClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            return createClassResultItem(cls, classDisplayOffset, displayFQN);
        }
        
        public CsmResultItem.EnumResultItem createLibEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return createEnumResultItem(enm, enumDisplayOffset, displayFQN);  
        }  
        
        public CsmResultItem.TypedefResultItem createLibTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return createLibTypedefResultItem(def, classDisplayOffset, displayFQN); 
        }
        
        public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld){
            return new CsmResultItem.FieldResultItem(fld, FAKE_PRIORITY);
        }
        public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp){
            return new CsmResultItem.MethodResultItem(mtd, substituteExp, FAKE_PRIORITY); 
        }
        public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp){
            return new CsmResultItem.ConstructorResultItem(ctr, substituteExp, FAKE_PRIORITY);
        }

        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp) {
            return new CsmResultItem.GlobalFunctionResultItem(fun, substituteExp, FAKE_PRIORITY); 
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

        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp) {
            return createGlobalFunctionResultItem(fun, substituteExp);
        }

        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
            return createNamespaceResultItem(pkg, displayFullNamespacePath);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // convert data into CompletionItem
    private static List convertData(List dataList, int classDisplayOffset, CsmCompletionExpression substituteExp, int substituteOffset){
        Iterator iter = dataList.iterator();
        List ret = new ArrayList();
        while (iter.hasNext()){
            Object obj = iter.next();
            CsmResultItem item;
            if (obj instanceof CompletionQuery.ResultItem){
                assert false : " why item element here?";
                item = (CsmResultItem)obj;
            }else{
                item = createResultItem(obj, classDisplayOffset, substituteExp);
            }
            assert item != null : "why null item? object " + obj;
            if (item != null) {
                item.setSubstituteOffset(substituteOffset);
                ret.add(item);
            }
        }
        return ret;
    }

    private static CsmResultItem createResultItem(Object obj, int classDisplayOffset, CsmCompletionExpression substituteExp){
        if (CsmKindUtilities.isCsmObject(obj)) {
            CsmObject csmObj = (CsmObject)obj;
            assert (!CsmKindUtilities.isMethod(csmObj) || CsmKindUtilities.isMethodDeclaration(csmObj)) : "completion result can not have method definitions " + obj;
            if (CsmKindUtilities.isNamespace(csmObj)) {
                return getCsmItemFactory().createNamespaceResultItem((CsmNamespace)csmObj, false);
            } else if (CsmKindUtilities.isEnum(csmObj)) {
                return getCsmItemFactory().createEnumResultItem((CsmEnum)csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isEnumerator(csmObj)) {
                return getCsmItemFactory().createGlobalEnumeratorResultItem((CsmEnumerator)csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isClass(csmObj)) {
                return getCsmItemFactory().createClassResultItem((CsmClass)csmObj, classDisplayOffset, false);
            } else if (CsmKindUtilities.isField(csmObj)) { 
                return getCsmItemFactory().createFieldResultItem((CsmField)csmObj);
            } else if (CsmKindUtilities.isConstructor(csmObj)) { // must be checked before isMethod, because constructor is method too
                return getCsmItemFactory().createConstructorResultItem((CsmConstructor)csmObj, substituteExp);
            } else if (CsmKindUtilities.isMethodDeclaration(csmObj)) { 
                return getCsmItemFactory().createMethodResultItem((CsmMethod)csmObj, substituteExp);
            } else if (CsmKindUtilities.isGlobalFunction(csmObj)) {
                return getCsmItemFactory().createGlobalFunctionResultItem((CsmFunction)csmObj, substituteExp);
            } else if (CsmKindUtilities.isGlobalVariable(csmObj)) {
                return getCsmItemFactory().createGlobalVariableResultItem ((CsmVariable)csmObj);
            } else if (CsmKindUtilities.isFileLocalVariable(csmObj)) {
                return getCsmItemFactory().createFileLocalVariableResultItem ((CsmVariable)csmObj);
            } else if (CsmKindUtilities.isLocalVariable(csmObj)) {
                return getCsmItemFactory().createLocalVariableResultItem ((CsmVariable)csmObj);
            } else if (CsmKindUtilities.isMacro(csmObj)) {
                return getCsmItemFactory().createGlobalMacroResultItem ((CsmMacro)csmObj);
            } else if (CsmKindUtilities.isTypedef(csmObj)) {
                return getCsmItemFactory().createTypedefResultItem((CsmTypedef)csmObj, classDisplayOffset, false);
            }
        }
        return null;
    }
    
    private static List convertData(CompletionResolver.Result res, int classDisplayOffset, CsmCompletionExpression substituteExp, int substituteOffset) {        
        if (res == null) {
            return Collections.EMPTY_LIST;
        }
        List out = new ArrayList(res.size());
        CsmItemFactory factory = getCsmItemFactory();
        CsmResultItem item;
        for (CsmVariable elem : res.getLocalVariables()) {
            item = factory.createLocalVariableResultItem(elem);
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
                item = factory.createConstructorResultItem((CsmConstructor)elem, substituteExp);
            } else {
                item = factory.createMethodResultItem(elem, substituteExp);
            }
            assert item != null;
            item.setSubstituteOffset(substituteOffset);    
            out.add(item);              
        }

        for (CsmClassifier elem : res.getProjectClassesifiersEnums()) {
            if (CsmKindUtilities.isClass(elem)) {
                item = factory.createClassResultItem((CsmClass)elem, classDisplayOffset, false);
            } else if (CsmKindUtilities.isTypedef(elem)) {
                item = factory.createTypedefResultItem((CsmTypedef)elem, classDisplayOffset, false);
            } else {
                assert CsmKindUtilities.isEnum(elem);
                item = factory.createEnumResultItem((CsmEnum)elem, classDisplayOffset, false);
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
            item = factory.createGlobalFunctionResultItem(elem, substituteExp);
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
        
        for (CsmClassifier elem : res.getLibClassifiersEnums()) {
            if (CsmKindUtilities.isClass(elem)) {
                item = factory.createLibClassResultItem((CsmClass)elem, classDisplayOffset, false);
            } else if (CsmKindUtilities.isTypedef(elem)) {
                item = factory.createLibTypedefResultItem((CsmTypedef)elem, classDisplayOffset, false);
            } else {
                assert CsmKindUtilities.isEnum(elem);
                item = factory.createLibEnumResultItem((CsmEnum)elem, classDisplayOffset, false);
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
            item = factory.createLibGlobalFunctionResultItem(elem, substituteExp);
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
        
        return out;
    }
    
}

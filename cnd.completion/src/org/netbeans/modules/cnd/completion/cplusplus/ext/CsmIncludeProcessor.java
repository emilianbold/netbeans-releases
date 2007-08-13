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

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContextPath;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCSyntax;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
* Mapping of colorings to particular token types
*
* @author Miloslav Metelka, Vladimir Voskresensky
* @version 1.00
*/

public class CsmIncludeProcessor implements TokenProcessor {

    /** Initial length of the document to be scanned. It should be big enough
    * so that only one pass is necessary. If the initial section is too
    * long, then this value is doubled and the whole parsing restarted.
    */
    private static final int INIT_SCAN_LEN = 4096;

    private static final int INIT = 0; // at the line begining before import kwd
    private static final int AFTER_IMPORT = 1; // right after the import kwd
    private static final int INSIDE_EXP = 2; // inside import expression
    // inside import expression mixed from several different tokens
    // exp string buffer is used in this case
    private static final int INSIDE_MIXED_EXP = 3;

    /** Short names to classes map */
    private HashMap name2Class = new HashMap(501);

    private char[] buffer;

    private ArrayList infoList = new ArrayList();

    /** Current state of the imports parsing */
    private int state;

    /** Whether parsing package statement instead of import statment.
    * They have similair syntax so only this flag distinguishes them.
    */
    private boolean parsingPackage;

    /** Start of the whole import statement */
    private int startPos;

    /** Start position of the particular import expression */
    private int expPos;

    private boolean eotReached;

    private StringBuilder exp = new StringBuilder();

    /** Whether the star was found at the end of package expression */
    private boolean star;

    /** The end of the import section. Used for optimized reparsing */
    private int posEndOfImportSection;

    /** Disable reparing when change is not in import section */
    private boolean disableReparsing;

    CCSyntax debugSyntax = new CCSyntax(); // !!! debugging syntax
    
    private int startOffset;
    private int endOffset;
    private int lastStartOffset = -1;
    private int lastEndOffset = -1;
    private boolean firstFiring = true;
    
    private EventListenerList listenerList = new EventListenerList();
    
    private int bufferStartOffset;

    private CsmSyntaxSupport sup;
    
    private boolean useCustomImports = false;

    public CsmIncludeProcessor(CsmSyntaxSupport sup) {
        this.sup = sup;
        posEndOfImportSection = -1;
        disableReparsing = false;
    }
    
    public synchronized void update(BaseDocument doc) {
        // optimalization of the parsing
        if (disableReparsing)
            return;
        
        if (useCustomImports) return;

        // bugfix - deadlock #43192
        initJavaLangPkg();
        
        doc.readLock();
        try {
            int scanLen = INIT_SCAN_LEN;
            int docLen = doc.getLength();
            boolean wholeDoc = false;
            do {
                if (scanLen >= docLen) {
                    scanLen = docLen;
                    wholeDoc = true;
                }
                eotReached = false;
                init();
                try {
                    doc.getSyntaxSupport().tokenizeText(this, 0, scanLen, false);
                } catch (BadLocationException e) {
                    // Can't update
                }
                scanLen *= 4; // increase the scanning size
            } while (!wholeDoc && eotReached);
            if (lastEndOffset!=endOffset || lastStartOffset!=startOffset){
                lastStartOffset = startOffset;
                lastEndOffset = endOffset;
                if (!firstFiring) {
                    fireChange(new ChangeEvent(this));
                }
            }
        } finally {
            doc.readUnlock();
        }
        buffer = null;
    }

    /** Appends imports from customImportsMap to ImportsMap 
     *  If customImportsMap is null, ImportsMap will be cleared and 
     *  set up with default java.lang.* classes
     */
    public void appendCustomImportsMap(Map customImportsMap){
        if (useCustomImports == false){
            initJavaLangPkg();
        }
        useCustomImports = true;
        if (customImportsMap == null){
            initJavaLangPkg();
        }else{
            name2Class.putAll(customImportsMap);
        }
    }
    
    /** Gets unmodifiable imports map */
    public Map getImportsMap(){
        return Collections.unmodifiableMap(name2Class);
    }
    
    private void initJavaLangPkg(){
        name2Class.clear(); // clear current mappings
        // add java.lang package by default
        // XXX
//        CsmNamespace pkg = sup.getFinder().getExactNamespace("java.lang"); // NOI18N
//        if (pkg != null) {
//            CsmClass[] classes = pkg.getClasses();
//            for (int i = 0; i < classes.length; i++) {
//                name2Class.put(classes[i].getName(), classes[i]);
//            }
//        }
    }
    
    protected void init() {
        exp.setLength(0);
        state = INIT;
        star = false;
        parsingPackage = false;
        startOffset = Integer.MAX_VALUE;
        endOffset = Integer.MIN_VALUE;
        infoList.clear();
    }

    
    private CsmClass checkForInnerClass(String innerClassName){
        Iterator it = name2Class.values().iterator();
        while(it.hasNext()){
            CsmClass cls = (CsmClass)it.next();
            if (cls != null && cls.getQualifiedName().endsWith("."+innerClassName)){ //NOI18N
                return cls;
            }
        }
        return null;
    }
    

    /** Returns innerclasses from import section */
    public List getInnerClasses(){
        Iterator it = name2Class.values().iterator();
        List ret = new ArrayList();
        while(it.hasNext()){
            CsmClass cls = (CsmClass)it.next();
            if (cls != null && 
                cls.getName().indexOf(".") >0 ){ //NOI18N
                ret.add(cls);
            }
        }
        return ret;
    }
    
    public int getStartOffset(){
        return (startOffset == Integer.MAX_VALUE) ? -1 : startOffset;
    }

    public int getEndOffset(){
        return (endOffset == Integer.MIN_VALUE) ? -1 : endOffset;
    }
    
    public boolean isEmpty() {
        return (getStartOffset() == -1); // should be sufficient test
    }
    
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }
    
    private void fireChange(ChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener)listeners[i + 1]).stateChanged(evt);
            }
        }
    }

        
    public CsmClassifier getClazz(String className) {
        CsmFinder finder = sup.getFinder();
        CsmClassifier ret =  (CsmClass)name2Class.get(className);// first try package scope 
        if (ret == null) ret = checkForInnerClass(className);// second try package scopes innerclasses
        if (ret == null) {
            ret = finder.getExactClassifier(className);
        }else{
            if (finder.getExactClassifier(ret.getQualifiedName()) != null){
                // get updated class (#23649)
                ret = finder.getExactClassifier(ret.getQualifiedName());
            }
        }
        return ret;
    }

    protected void packageStatementFound(int packageStartPos, int packageEndPos, String packageExp) {
        CsmNamespace pkg = sup.getFinder().getExactNamespace(packageExp);
        if (pkg != null) {
            // XXX
//            CsmClass[] classes = pkg.getClasses();
//            for (int i = 0; i < classes.length; i++) {
//                name2Class.put(classes[i].getName(), classes[i]);
//            }
        }
    }

    protected void importStatementFound(int importStartPos, int importEndPos, String importExp, boolean starAtEnd) {
        if (importStartPos < startOffset) startOffset = importStartPos;
        if (importEndPos > endOffset) endOffset = importEndPos;
        CsmFinder finder = sup.getFinder();
        Info info = new Info(importStartPos, importEndPos, starAtEnd);
        CsmClassifier cls = finder.getExactClassifier(importExp);
        if (cls != null) {
            info.cls = cls;
            if (star) { // !!! dodelat
            } else { // only this single class
                name2Class.put(cls.getName(), cls);
            }
        } else { // not a direct class, try package
            CsmNamespace pkg = finder.getExactNamespace(importExp);
            if (pkg != null) {
                info.pkg = pkg;
                if (starAtEnd) { // only useful with star
                    // XXX
//                    CsmClass[] classes = pkg.getClasses();
//                    for (int i = 0; i < classes.length; i++) {
//                        name2Class.put(classes[i].getName(), classes[i]);
//                    }
                }
            } else { // not package, will be class
                String pkgName = importExp;
                String simplePkgName = null;
                int ind;
                while((ind = pkgName.lastIndexOf('.')) >= 0) {
                    pkgName = pkgName.substring(0, ind);
                    if (simplePkgName == null) {
                        simplePkgName = pkgName;
                    }
                    /*
                     * Removing this heuristic because of the bug #31481
                     *
                    pkg = finder.getExactNamespace(pkgName);
                    if (pkg != null) { // found valid package, but unknown class
                        cls = JavaCompletion.getSimpleClass(importExp, pkgName.length());
                        info.cls = cls;
                        info.unknownImport=importExp;
                        if (star) {
                            // don't add in this case, can change in the future
                        } else {
                            name2Class.put(cls.getName(), cls);
                        }
                        break;
                    }
                     **/
                }

                if (cls == null) {
                    // didn't found a direct package, assume last is class name
                    if (simplePkgName != null) { // at least one dot in importExp
//XXX                        cls = JavaCompletion.getSimpleClass(importExp, simplePkgName.length());
                        if (star) {
                            // don't add in this case, can change in the future
                        } else {
                            name2Class.put(cls.getName(), cls);
                        }
                    }
                }
            }
        }
        if ((info.cls==null) && (info.pkg==null)) {
            info.unknownImport=importExp;
        }
        infoList.add(info);
    }
    
    /** Returns true if className is in import, but in a package, that hasn't updated DB */
    public boolean isUnknownImport(String className){
        for(int i = 0; i<infoList.size(); i++){
            String unknown = ((Info)infoList.get(i)).unknownImport;
            if ( (unknown!=null) && (unknown.indexOf(className) >- 1) ) return true;
        }
        return false;
    }
    
    /** Returns all imports that aren't in Code Completion DB yet */
    protected List getUnknownImports(){
        ArrayList ret = new ArrayList();
        for(int i = 0; i<infoList.size(); i++){
            String unknownImport = ((Info)infoList.get(i)).unknownImport;
            if (unknownImport !=null) {
                if (((Info)infoList.get(i)).star) unknownImport = unknownImport+".*"; //NOI18N
                ret.add(unknownImport);
            }
        }
        return ret;
    }
    
// VK: never used - commented this out
//    /** Returns true if the given class is in the import statement directly or
//     *  indirectly (package.name.*)  */
//    public boolean isIncluded(CsmClass cls){
//        if (cls==null) return false;
//        
//        String clsFullName=cls.getQualifiedName();
//        String pkgName = cls.getContainingNamespace().getQualifiedName();
//        
//        if (name2Class.containsValue(cls)) return true;
//        
//        for (int i = 0; i<infoList.size(); i++){
//            CsmClassifier infoClass=((Info)infoList.get(i)).cls;
//            CsmNamespace infoPackage = ((Info)infoList.get(i)).pkg;
//            
//            if ((clsFullName!=null) && (infoClass!=null)){
//                if (clsFullName.equals(infoClass.getQualifiedName())){
//                    return true;
//                }
//            }
//            if ((pkgName!=null) && (infoPackage!=null)){
//                if (pkgName.equals(infoPackage.getName())){
//                    return true;
//                }
//            }
//            
//        }
//        return false;
//    }

    public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
    int tokenBufferOffset, int tokenLen) {
        boolean cont = true;
        int tokenOffset = bufferStartOffset + tokenBufferOffset;
        switch (tokenID.getNumericID()) {
            case CCTokenContext.IDENTIFIER_ID:
                switch (state) {
                    case AFTER_IMPORT:
                        expPos = tokenOffset;
                        state = INSIDE_EXP;
                        break;

                    case INSIDE_MIXED_EXP:
                        exp.append(buffer, tokenOffset, tokenLen);
                        // let it flow to INSIDE_EXP
                    case INSIDE_EXP:
                        if (star) { // not allowed after star was found
                            cont = false;
                        }
                        break;
                }
                break;

            case CCTokenContext.DOT_ID:
                switch (state) {
                    case INIT: // ignore standalone dot
                        break;

                    case AFTER_IMPORT:
                        cont = false; // dot after import keyword
                        break;

                    case INSIDE_MIXED_EXP:
                        exp.append('.'); //NOI18N
                        // let it flow to INSIDE_EXP
                    case INSIDE_EXP:
                        if (star) { // not allowed after star was found
                            cont = false;
                        }
                        break;
                }
                break;

            case CCTokenContext.SEMICOLON_ID:
                String impExp = null;
                switch (state) {
                    case INIT: // ignore semicolon
                        break;

                    case AFTER_IMPORT: // semicolon after import kwd
                        cont = false;
                        break;

                    case INSIDE_EXP:
                        impExp = new String(buffer, expPos,
                                            (star ? (tokenOffset - 2) : tokenOffset) - expPos);
                        break;

                    case INSIDE_MIXED_EXP:
                        impExp = exp.toString();
                        exp.setLength(0);
                        break;
                }

                if (impExp != null) {
                    if (parsingPackage) {
                        packageStatementFound(startPos, tokenOffset + 1, impExp);
                    } else { // parsing import statement
                        importStatementFound(startPos, tokenOffset + 1, impExp, star);
                    }
                    star = false;
                    parsingPackage = false;
                    state = INIT;
                }
                break;

            case CCTokenContext.MUL_ID:
                if (star || parsingPackage) {
                    cont = false;
                } else {
                    switch (state) {
                        case INIT: // ignore star at the begining
                            break;

                        case AFTER_IMPORT:
                            cont = false; // star after import kwd
                            break;

                        case INSIDE_EXP:
                            star = true;
                            if (tokenOffset == 0 || buffer[tokenOffset - 1] != '.') {
                                cont = false;
                            }
                            break;

                        case INSIDE_MIXED_EXP:
                            int len = exp.length();
                            if (len > 0 && exp.charAt(len - 1) == '.') {
                                exp.setLength(len - 1); // remove ending dot
                                star = true;
                            } else { // error
                                cont = false;
                            }
                            break;
                    }
                }
                break;

//            case CCTokenContext.PACKAGE_ID:
//                switch (state) {
//                    case INIT:
//                        parsingPackage = true;
//                        state = AFTER_IMPORT; // the same state is used
//                        break;
//
//                    default:
//                        cont = false; // error in other states
//                        break;
//                }
//                break;

            case CCTokenContext.EXPORT_ID:
                switch (state) {
                    case INIT:
                        parsingPackage = false;
                        state = AFTER_IMPORT;
                        startPos = tokenOffset;
                        break;

                    default:
                        cont = false; // error in other states
                        break;
                }
                break;

            case CCTokenContext.WHITESPACE_ID:
            case CCTokenContext.LINE_COMMENT_ID:
            case CCTokenContext.BLOCK_COMMENT_ID:
                switch (state) {
                case INSIDE_EXP:
                    if(tokenOffset-expPos<0){
                        cont=false;
                        break;
                    }
                    // Need to continue as string
                    exp.append(buffer, expPos, tokenOffset - expPos);
                    state = INSIDE_MIXED_EXP;
                    break;
                }
                break;

            default:
                // when we get here, it means that all packages and imports
                // were already parsed. the rest of the document will be skipped
                // and so this is right place to set end of import section
                if (posEndOfImportSection == -1 || tokenOffset+tokenLen > posEndOfImportSection){
                    posEndOfImportSection = tokenOffset+tokenLen;
                }
                if (firstFiring) firstFiring = false;
                cont = false;
                break;
        }

        return cont;
    }

    private String debugState(int state) {
        switch (state) {
        case INIT:
            return "INIT"; // NOI18N
        case AFTER_IMPORT:
            return "AFTER_IMPORT"; // NOI18N
        case INSIDE_EXP:
            return "INSIDE_EXP"; // NOI18N
        case INSIDE_MIXED_EXP:
            return "INSIDE_MIXED_EXP"; // NOI18N
        }
        return "UNKNOWN STATE"; // NOI18N
    }

    public int eot(int offset) {
        eotReached = true; // will be rescanned
        return 0;
    }

    public void nextBuffer(char[] buffer, int offset, int len,
                           int startPos, int preScan, boolean lastBuffer) {
        this.buffer = buffer;
        bufferStartOffset = startPos - offset;
    }
    
    /** Optimalization for document parsing. The owner of JavaImport instance 
     * can call this function to inform the JavaImport where the change
     * has occured in the document. If this function is not called, the whole
     * document is parsed. If it is, the parsing is done only when the import
     * section of the document is being modified.
     * @param offset offset of the change in document
     */
    public void documentModifiedAtPosition(int offset)
    {
        documentModifiedAtPosition(offset, null);
    }
    
    public void documentModifiedAtPosition(int offset, final BaseDocument doc){
        // if end of import section has already been found, then check 
        // if change is in import section or not
        if (posEndOfImportSection != -1)
        {
            if (offset > posEndOfImportSection)
            {
                // reparing is not necessary, because change is after the import section
                disableReparsing = true;     
            }
            else
            {
                // the document must be completely reparsed
                disableReparsing = false;
                posEndOfImportSection = -1;
                
                // bugfix of #43455
                //org.openide.util.RequestProcessor.getDefault().post(new Runnable(){
                CsmModelAccessor.getModel().enqueue(new Runnable() {
                    public void run(){
                        update(doc);
                    }
                });
                
            }
        }
    }

    class Info {

        Info(int startPos, int endPos, boolean star) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.star = star;
        }

        int startPos;

        int endPos;

        boolean star;

        CsmNamespace pkg;

        CsmClassifier cls;
         
        String unknownImport;        

    }

}

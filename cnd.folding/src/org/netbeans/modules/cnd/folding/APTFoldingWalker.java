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

package org.netbeans.modules.cnd.folding;

import antlr.TokenStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.*;

/**
 * implementation of walker used for folding
 * Responsible for creating folds:
 * - sequental #include directives
 * - #if* #endif
 * - block comments
 * - sequental line comments
 * @author Vladimir Voskresensky
 */
/*package*/ class APTFoldingWalker extends APTWalker {
    
    private static final int IFDEF_FOLD = CppFoldRecord.IFDEF_FOLD;
    private static final int INCLUDES_FOLD = CppFoldRecord.INCLUDES_FOLD;

    private APTFoldingCommentFilter filter = null;
    private List<CppFoldRecord> includeFolds = new ArrayList<CppFoldRecord>();
    private List<CppFoldRecord> ifdefFolds = new ArrayList<CppFoldRecord>();
    
    public APTFoldingWalker(APTFile apt) {
        super(apt, null);
    }
    
    public TokenStream getFilteredTokenStream(APTLanguageFilter lang) {
        return lang.getFilteredStream(getTokenStream());
    }
    
    public TokenStream getTokenStream() {
        // get original
        // remove comments and hanlde includes
        filter = new APTFoldingCommentFilter(super.getTokenStream());
        return filter;
    }        

    public List<CppFoldRecord> getFolders() {
        List<CppFoldRecord> filterFolds = filter.getFolders();
        List<CppFoldRecord> out = new ArrayList<CppFoldRecord>(filterFolds.size() + includeFolds.size() + ifdefFolds.size());   
        out.addAll(filterFolds);
        out.addAll(includeFolds);
        out.addAll(ifdefFolds);
        return out;
    }
    
    protected void onInclude(APT apt) {
        include(apt);
    }
    
    protected void onIncludeNext(APT apt) {
        include(apt);
    }
    
    protected boolean onIf(APT apt) {
        return onStartPreprocNode(apt);
    }
    
    protected boolean onIfdef(APT apt) {
        return onStartPreprocNode(apt);
    }
    
    protected boolean onIfndef(APT apt) {
        return onStartPreprocNode(apt);
    }
    
    protected void onDefine(APT apt) {
        onOtherPreprocNode(apt);
    }

    protected void onUndef(APT apt) {
        onOtherPreprocNode(apt);
    }

    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        onOtherPreprocNode(apt);
        return true;
    }

    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        onOtherPreprocNode(apt);
        return true;
    }
    
    protected void onEndif(APT apt, boolean wasInBranch) {
        createEndifFold(apt);
    }
    
    protected void onOtherNode(APT apt) {
        onOtherPreprocNode(apt);
    }
    
    protected void onStreamNode(APT apt) {
        addIncludesIfNeeded();
    }
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    private Stack<APT> ppStartDirectives = new Stack<APT>();
        
    private boolean onStartPreprocNode(APT apt) {
        filter.onPreprocNode(apt);
        ppStartDirectives.push(apt);        
        return true;
    }
    
    private void createEndifFold(APT end) {
        filter.onPreprocNode(end);
        addIncludesIfNeeded();
        // there could be errors with unbalanced directives => check 
        if (!ppStartDirectives.empty()) {
            APT start = (APT) ppStartDirectives.pop();
            // we want fold after full "#if A" directive
            int startFold = start.getEndOffset();
            int endFold = end.getEndOffset();
            if (APTFoldingUtils.isStandalone()) {
                ifdefFolds.add(new CppFoldRecord(IFDEF_FOLD, start.getToken().getLine(), startFold, ((APTToken)end.getToken()).getEndLine(), endFold));
            } else {
                ifdefFolds.add(new CppFoldRecord(IFDEF_FOLD, startFold, endFold));
            }
        }        
    }

    private void include(APT apt) {
        filter.onPreprocNode(apt);
        if (firstInclude == null) {
            firstInclude = apt;
        }
        lastInclude = apt;
    }

    private void addIncludesIfNeeded() {
        if (lastInclude != firstInclude) {
            assert (lastInclude != null);
            assert (firstInclude != null);
            // we want fold after #include string
            int start = ((APTToken)firstInclude.getToken()).getEndOffset();
            int end = lastInclude.getEndOffset();
            if (start < end) {
                if (APTFoldingUtils.isStandalone()) {
                    includeFolds.add(new CppFoldRecord(INCLUDES_FOLD, ((APTToken)firstInclude.getToken()).getLine(), start, ((APTToken)lastInclude.getToken()).getEndLine(), end));
                } else {                
                    includeFolds.add(new CppFoldRecord(INCLUDES_FOLD, start, end));
                }
            }
        }
        lastInclude = null;
        firstInclude = null;
    }    
    
    private  void onOtherPreprocNode(APT apt) {
        filter.onPreprocNode(apt);
        addIncludesIfNeeded();
    }    
    
    /** 
     * overrides APTWalker.stopOnErrorDirective 
     * We should be able to make folds after #error as well
     */
    protected boolean stopOnErrorDirective() {
	return false;
    }
    
    private APT firstInclude = null;
    private APT lastInclude = null;
}

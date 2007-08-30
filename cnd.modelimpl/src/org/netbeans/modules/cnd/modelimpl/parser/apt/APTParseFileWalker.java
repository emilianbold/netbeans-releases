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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.*;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.SimpleOffsetableImpl;

/**
 * implementation of walker used when parse files/collect macromap
 * @author Vladimir Voskresensky
 */
public class APTParseFileWalker extends APTProjectFileBasedWalker {
    
    private boolean createMacroAndIncludes;
    
    public APTParseFileWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler) {
        super(base, apt, file, preprocHandler);
        this.createMacroAndIncludes = false;
    }
    
    public void addMacroAndIncludes(boolean create) {
        this.createMacroAndIncludes = create;
    }
    
    protected boolean needMacroAndIncludes() {
        return this.createMacroAndIncludes;
    }
    
    public TokenStream getFilteredTokenStream(APTLanguageFilter lang) {
        return lang.getFilteredStream(getTokenStream());
    }
    
    @Override
    public TokenStream getTokenStream() {
        setMode(ProjectBase.GATHERING_TOKENS);
        // in this phase we should create objects for #define and #include
        addMacroAndIncludes(true);
        // get original
        TokenStream ts = super.getTokenStream();
        // remove comments
        ts = new APTCommentsFilter(ts);
        // expand macros
        ts = new APTMacroExpandedStream(ts, getMacroMap());
        return ts;
    }
    
    @Override
    protected void onDefine(APT apt) {
        super.onDefine(apt);
        if (needMacroAndIncludes()) {
            getFile().addMacro(createMacro((APTDefine)apt));
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of abstract methods
    
    @Override
    protected void postInclude(APTInclude apt, FileImpl included) {
        if (needMacroAndIncludes()) {
            getFile().addInclude(createInclude(apt, included));
        }
    }
    
    protected FileImpl includeAction(ProjectBase inclFileOwner, String inclPath, int mode, APTInclude apt) throws IOException {
        try {
            return inclFileOwner.onFileIncluded(getStartProject(), inclPath, getPreprocHandler(), mode);
        } catch (NullPointerException ex) {
            APTUtils.LOG.log(Level.SEVERE, "file without project!!!", ex);// NOI18N
        } finally {
            getIncludeHandler().popInclude();
        }
        return null;
    }
        
    ////////////////////////////////////////////////////////////////////////////
    // implementation details

    private MacroImpl createMacro(APTDefine define) {
        
        List<String> params = null;
        Collection<Token> paramTokens = define.getParams();
        if( paramTokens != null ) {
            params = new ArrayList<String>();
            for (Token elem : paramTokens) {
                if( APTUtils.isID(elem) ) {
                    params.add(elem.getText());
                }
            }
        }
        
        SimpleOffsetableImpl pos = getOffsetable((APTToken)define.getToken());
        List bodyTokens = define.getBody();
        APTToken last;
        String body = "";
        if (bodyTokens.isEmpty()) {
            last = (APTToken) define.getName();
        } else {
            last = (APTToken) bodyTokens.get(bodyTokens.size() - 1);
            APTToken start = (APTToken) bodyTokens.get(0);
            // FIXUP (performance/memory). For now:
            // 1) nobody uses macros.getText
            // 2) its realization is ineffective
            // so we temporarily switch this off
            body = ""; //file.getText( start.getOffset(), last.getEndOffset());
        }
        setEndPosition(pos,last);
        
        return new MacroImpl(define.getName().getText(), params, body/*sb.toString()*/, getFile(), pos);
    }
    
    private IncludeImpl createInclude(final APTInclude apt, final FileImpl included) {
        SimpleOffsetableImpl inclPos = getOffsetable((APTToken)apt.getToken());
        setEndPosition(inclPos,getLastToken(apt.getInclude()));
        IncludeImpl incImpl = new IncludeImpl(apt.getFileName(getMacroMap()), apt.isSystem(getMacroMap()), included, getFile(), inclPos);
        return incImpl;
    }
    
    private SimpleOffsetableImpl getOffsetable(APTToken token) {
        return new SimpleOffsetableImpl(token.getLine(), token.getColumn(), token.getOffset());
    }
    
    private void setEndPosition(SimpleOffsetableImpl offsetable, APTToken token) {
        if( token != null && !APTUtils.isEOF(token)) {
            offsetable.setEndPosition(token.getEndLine(), token.getEndColumn(), token.getEndOffset());
        } else {
            assert offsetable.getStartPosition() != null;
            offsetable.setEndPosition(offsetable.getStartPosition());
        }
    }
    
    private APTToken getLastToken(TokenStream ts) {
        try {
            Token last = ts.nextToken();
            for( Token curr = null; ! APTUtils.isEOF(curr = ts.nextToken()); ) {
                last = curr;
            }
            return (APTToken)last;
        } catch( TokenStreamException e ) {
            e.printStackTrace(System.err);
            return null;
        }
    }    

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.impl.support.*;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.*;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserThreadManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.SimpleOffsetableImpl;

/**
 * implementation of walker used when parse files/collect macromap
 * @author Vladimir Voskresensky
 */
public class APTParseFileWalker extends APTWalker {
    private APTPreprocState preprocState;
    private String startPath;
    private FileImpl file;  
    private int mode;
    private boolean createMacroAndIncludes;
    
    public APTParseFileWalker(APTFile apt, FileImpl file, APTPreprocState preprocState) {
        super(apt, preprocState == null ? null: preprocState.getMacroMap());
        this.file = file;
        this.startPath = file.getAbsolutePath();
        this.preprocState = preprocState;
        this.mode = ProjectBase.GATHERING_MACROS;
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
    
    public TokenStream getTokenStream() {
        this.mode = ProjectBase.GATHERING_TOKENS;
        // in this phase we should create objects for #define and #include
        addMacroAndIncludes(true);
        // get original
        TokenStream ts = super.getTokenStream();
        // remove comments
        ts = new APTCommentsFilter(ts);
        // expand macros
        ts = new APTParserMacroExpandedStream(ts, preprocState.getMacroMap());         
        return ts;
    }
    
    protected APTIncludeHandler getIncludeHandler() {
        return preprocState == null ? null: preprocState.getIncludeHandler();
    }
    
    protected void onInclude(APT apt) {
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startPath);
            String resolvedPath = resolver.resolveInclude((APTInclude)apt, getMacroMap());
            if (resolvedPath == null) {
                if (ParserThreadManager.instance().isStandalone()) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + file.getName() + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING, 
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            include(resolvedPath, (APTInclude)apt);
        }
    }

    protected void onIncludeNext(APT apt) {
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startPath);           
            String resolvedPath = resolver.resolveIncludeNext((APTIncludeNext)apt, getMacroMap());
            if (resolvedPath == null) {
                if (ParserThreadManager.instance().isStandalone()) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + file.getName() + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING, 
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
	    // TODO: reflect include next in API and add implementation here
            include(resolvedPath, (APTInclude) apt);
        }
    }

    protected void onDefine(APT apt) {
        APTDefine define = (APTDefine)apt;
        getMacroMap().define(define.getName(), define.getParams(), define.getBody());
        if (needMacroAndIncludes()) {
            file.addMacro(createMacro(define));
        }
    }
    
    protected MacroImpl createMacro(APTDefine define) {
        
        List/*<String>*/ params = null;
        Collection paramTokens = define.getParams();
        if( paramTokens != null ) {
            params = new ArrayList/*<String>*/();
            for (Iterator iter = paramTokens.iterator(); iter.hasNext();) {
                Token elem = (Token) iter.next();
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
        setEndPosition(pos, (APTToken) last);
        
        return new MacroImpl(define.getName().getText(), params, body/*sb.toString()*/, file, pos);
    }

    protected void onUndef(APT apt) {
        APTUndefine undef = (APTUndefine)apt;
        getMacroMap().undef(undef.getName());
    }

    protected boolean onIf(APT apt) {
        return eval(apt);
    }

    protected boolean onIfdef(APT apt) {
        return eval(apt);
    }

    protected boolean onIfndef(APT apt) {
        return eval(apt);
    }

    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch && eval(apt);
    }

    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch;
    }

    protected void onEndif(APT apt, boolean wasInBranch) {
    }

//    protected Token onToken(Token token) {
//        return token;
//    }
       
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    private boolean eval(APT apt) {
        APTUtils.LOG.log(Level.FINE, "eval condition for {0}", new Object[] {apt});// NOI18N
        boolean res = false;
        try {
            res = APTConditionResolver.evaluate(apt, getMacroMap());
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on evaluating condition node " + apt, ex);// NOI18N
        }
        return res;
    }

    private void include(String path, APTInclude apt) {
        FileImpl included = null;
        if (path != null && getIncludeHandler().pushInclude(path, apt.getToken().getLine())) {
            ProjectBase curProject = file.getProjectImpl();
            if (curProject != null) {
                ProjectBase inclFileOwner = curProject.resolveFileProjectOnInclude(path);
                included = includeAction(inclFileOwner, path, preprocState, mode, apt);
            } else {
                APTUtils.LOG.log(Level.SEVERE, "file {0} without project!!!", new Object[] {file});// NOI18N
            }
        }
        
        if (needMacroAndIncludes()) {
            file.addInclude(createInclude(apt, included));
        }
    }

    protected FileImpl includeAction(ProjectBase inclFileOwner, String inclPath, APTPreprocState preprocState, int mode, APTInclude apt) {
        try {
            return inclFileOwner.onFileIncluded(inclPath, preprocState, mode);
        } catch (NullPointerException ex) {
            APTUtils.LOG.log(Level.SEVERE, "file without project!!!", ex);// NOI18N
        } finally {
            getIncludeHandler().popInclude(); 
        }    
        return null;
    }
    
    private IncludeImpl createInclude(final APTInclude apt, final FileImpl included) {  
        SimpleOffsetableImpl inclPos = getOffsetable((APTToken)apt.getToken());
        setEndPosition(inclPos, (APTToken)getLastToken(apt.getInclude()));
        IncludeImpl incImpl = new IncludeImpl(apt.getFileName(getMacroMap()), apt.isSystem(getMacroMap()), included, file, inclPos);
        return incImpl;
    }

    private SimpleOffsetableImpl getOffsetable(APTToken token) {
	return new SimpleOffsetableImpl(token.getLine(), token.getColumn(), token.getOffset());
    }
    
    private void setEndPosition(SimpleOffsetableImpl offsetable, APTToken token) {
	if( token != null && !APTUtils.isEOF(token)) {
	    offsetable.setEndPosition(token.getEndLine(), token.getEndColumn(), token.getEndOffset());
	}
    }
    
    private APTToken getLastToken(TokenStream ts) {
	try {
	    Token last = ts.nextToken();
	    for( Token curr = null; ! APTUtils.isEOF(curr = ts.nextToken()); ) {
		last = curr;
	    }
	    return (APTToken)last;
	}
	catch( TokenStreamException e ) {
	    e.printStackTrace(System.err);
	    return null;
	}
    }
    
}

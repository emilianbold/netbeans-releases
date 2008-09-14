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
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;

/**
 * implementation of walker used when parse files/collect macromap
 * @author Vladimir Voskresensky
 */
public class APTParseFileWalker extends APTProjectFileBasedWalker {
    
    /** 
     * A callback that should be invoked
     * when each conditional is evaluated
     */
    public interface EvalCallback {
        void onEval(APT apt, boolean result);
    }

    private boolean createMacroAndIncludes;
    private final EvalCallback evalCallback;

    public APTParseFileWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler) {
        this(base, apt, file, preprocHandler, null);
    }

    public APTParseFileWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler, EvalCallback evalCallback) {
        super(base, apt, file, preprocHandler);
        this.createMacroAndIncludes = false;
        this.evalCallback = evalCallback;
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
            APTUtils.LOG.log(Level.SEVERE, "NPE when processing file", ex);// NOI18N
	    DiagnosticExceptoins.register(ex);
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
            DiagnosticExceptoins.register(e);
            return null;
        }
    }

    @Override
    protected void onEval(APT apt, boolean result) {
        if (evalCallback != null) {
            evalCallback.onEval(apt, result);
        }
    }
}

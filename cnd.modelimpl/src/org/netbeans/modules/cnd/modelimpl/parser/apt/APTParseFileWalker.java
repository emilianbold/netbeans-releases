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
import org.netbeans.modules.cnd.apt.structure.APTError;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.*;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.IncludeImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ErrorDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.SimpleOffsetableImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

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
    private final boolean triggerParsingActivity;
    private final EvalCallback evalCallback;

    public APTParseFileWalker(ProjectBase base, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler, boolean triggerParsingActivity, EvalCallback evalCallback, APTFileCacheEntry cacheEntry) {
        super(base, apt, file, preprocHandler, cacheEntry);
        this.createMacroAndIncludes = false;
        this.evalCallback = evalCallback;
        this.triggerParsingActivity = triggerParsingActivity;
    }

    public void addMacroAndIncludes(boolean create) {
        this.createMacroAndIncludes = create;
    }

    protected boolean needMacroAndIncludes() {
        return this.createMacroAndIncludes;
    }

    public final boolean isTriggerParsingActivity() {
        return triggerParsingActivity;
    }

    public TokenStream getFilteredTokenStream(APTLanguageFilter lang) {
        return lang.getFilteredStream(getTokenStream());
    }

    @Override
    public TokenStream getTokenStream() {
        return getTokenStream(true);
    }

    public TokenStream getTokenStream(boolean filtered) {
        setMode(ProjectBase.GATHERING_TOKENS);
        // get original
        TokenStream ts = super.getTokenStream();
        // expand macros
        ts = new APTMacroExpandedStream(ts, getMacroMap());
        if (filtered) {
            // remove comments
            ts = new APTCommentsFilter(ts);
        }
        return ts;
    }

    @Override
    protected void onDefine(APT apt) {
        super.onDefine(apt);
        if (needMacroAndIncludes()) {
            MacroImpl macro = createMacro((APTDefine) apt);
            if (macro != null) {
                getFile().addMacro(macro);
            }
        }
    }

    @Override
    protected void onErrorNode(APT apt) {
        super.onErrorNode(apt);
        if (needMacroAndIncludes()) {
            getFile().addError(createError((APTError)apt));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of abstract methods
    @Override
    protected void postInclude(APTInclude apt, FileImpl included) {
        if (needMacroAndIncludes()) {
            getFile().addInclude(createInclude(apt, included), included == null);
        }
    }

    protected FileImpl includeAction(ProjectBase inclFileOwner, CharSequence inclPath, int mode, APTInclude apt, APTMacroMap.State postIncludeState) throws IOException {
        try {
            return inclFileOwner.onFileIncluded(getStartProject(), inclPath, getPreprocHandler(), postIncludeState, mode, isTriggerParsingActivity());
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
    private ErrorDirectiveImpl createError(APTError error) {
        APTToken token = error.getToken();
        SimpleOffsetableImpl pos = getOffsetable(token);
        setEndPosition(pos, token);
        return ErrorDirectiveImpl.create(this.getFile(), token.getTextID(), pos);
    }

    private MacroImpl createMacro(APTDefine define) {
        // create even for invalid macro (to have possibility of showing error HL)
        List<CharSequence> params = null;
        Collection<APTToken> paramTokens = define.getParams();
        if (paramTokens != null) {
            params = new ArrayList<CharSequence>();
            for (APTToken elem : paramTokens) {
                if (APTUtils.isID(elem)) {
                    params.add(NameCache.getManager().getString(elem.getTextID()));
                }
            }
        }

        SimpleOffsetableImpl pos = getOffsetable(define.getToken());
        List<APTToken> bodyTokens = define.getBody();
        APTToken last;
        String body = "";
        if (bodyTokens.isEmpty()) {
            last = define.getName();
        } else {
            last = bodyTokens.get(bodyTokens.size() - 1);
            //APTToken start = (APTToken) bodyTokens.get(0);
            // FIXUP (performance/memory). For now:
            // 1) nobody uses macros.getText
            // 2) its realization is ineffective
            // so we temporarily switch this off
            body = ""; //file.getText( start.getOffset(), last.getEndOffset());
        }
        setEndPosition(pos, last);

        return new MacroImpl(define.getName().getTextID(), params, body/*sb.toString()*/, getFile(), pos);
    }

    private IncludeImpl createInclude(final APTInclude apt, final FileImpl included) {
        SimpleOffsetableImpl inclPos = getOffsetable(apt.getToken());
        APTToken lastToken = getLastToken(apt.getInclude());
        if(lastToken == null || APTUtils.isEOF(lastToken)) {
            lastToken = apt.getToken();
        }
        setEndPosition(inclPos, lastToken);
        IncludeImpl incImpl = new IncludeImpl(apt.getFileName(getMacroMap()), apt.isSystem(getMacroMap()), included, getFile(), inclPos);
        return incImpl;
    }

    private SimpleOffsetableImpl getOffsetable(APTToken token) {
        return new SimpleOffsetableImpl(token.getLine(), token.getColumn(), token.getOffset());
    }

    private void setEndPosition(SimpleOffsetableImpl offsetable, APTToken token) {
        if (token != null && !APTUtils.isEOF(token)) {
            offsetable.setEndPosition(token.getEndLine(), token.getEndColumn(), token.getEndOffset());
        } else {
            assert offsetable.getStartPosition() != null;
            offsetable.setEndPosition(offsetable.getStartPosition());
        }
    }

    private APTToken getLastToken(TokenStream ts) {
        try {
            Token last = ts.nextToken();
            for (Token curr = null; !APTUtils.isEOF(curr = ts.nextToken());) {
                last = curr;
            }
            return (APTToken) last;
        } catch (TokenStreamException e) {
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

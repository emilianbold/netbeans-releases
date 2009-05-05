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

import antlr.TokenStream;
import antlr.TokenStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTElif;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTIf;
import org.netbeans.modules.cnd.apt.structure.APTIfdef;
import org.netbeans.modules.cnd.apt.structure.APTIfndef;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.openide.filesystems.FileUtil;


/**
 * Basic walker to find macroes for semantic highlighting
 * TODO: maybe it should be one walker for any semantic HL activity, because
 * they used altogether.
 *
 * @author Sergey Grinev
 */
public class APTFindMacrosWalker extends APTDefinesCollectorWalker {
    protected final Map<CharSequence, CsmFile> macro2file = new HashMap<CharSequence, CsmFile>();

    public APTFindMacrosWalker(APTFile apt, CsmFile csmFile, APTPreprocHandler preprocHandler) {
        super(apt, csmFile, preprocHandler);
    }

    @Override
    protected void onDefine(APT apt) {
        APTDefine defineNode = (APTDefine) apt;
        APTToken name = defineNode.getName();
        if (name != null) {
            MacroInfo mi = new MacroInfo(csmFile, defineNode.getOffset(), defineNode.getEndOffset(), null);
            CsmReference mf = new MacroReference(csmFile, name, mi);
            references.add(mf);
        }
        analyzeList(defineNode.getBody());
        super.onDefine(apt);
    }

    @Override
    protected boolean onIf(APT apt) {
        analyzeStream(((APTIf) apt).getCondition(), false);
        return super.onIf(apt);
    }

    @Override
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        analyzeStream(((APTElif) apt).getCondition(), false);
        return super.onElif(apt, wasInPrevBranch);
    }

    @Override
    protected boolean onIfndef(APT apt) {
        analyzeToken(((APTIfndef) apt).getMacroName(), false);
        return super.onIfndef(apt);
    }

    @Override
    protected boolean onIfdef(APT apt) {
        analyzeToken(((APTIfdef) apt).getMacroName(), false);
        return super.onIfdef(apt);
    }
    private final List<CsmReference> references = new ArrayList<CsmReference>();

    @Override
    protected void onUndef(APT apt) {
        analyzeToken(((APTUndefine) apt).getName(), false);
        super.onUndef(apt);
    }

    @Override
    protected void onInclude(APT apt) {
        analyzeStream(((APTInclude)apt).getInclude(), true);
        super.onInclude(apt);
    }

    @Override
    protected void onIncludeNext(APT apt) {
        analyzeStream(((APTIncludeNext)apt).getInclude(), true);
        super.onIncludeNext(apt);
    }

    public List<CsmReference> getCollectedData() {
        return references;
    }

    @Override
    public TokenStream getTokenStream() {
        TokenStream ts = super.getTokenStream();
        analyzeStream(ts, true);
        return null; // tokenstream set to EOF? it's no good
    }

    private CsmReference analyzeToken(APTToken token, boolean addOnlyIfNotFunLikeMacro) {
        CsmReference mf = null;
        boolean funLike = false;
        if (token != null && !APTUtils.isEOF(token)) {
            APTMacro m = getMacroMap().getMacro(token);
            if (m != null) {
                // macro either doesn't need params or has "(" after name
                funLike = m.isFunctionLike();
                switch(m.getKind()){
                    case DEFINED:
                        MacroInfo mi = getMacroInfo(token);
                        if (mi == null) {
                            CsmFile macroContainter = getMacroFile(m);
                            if (macroContainter != null) {
                                mi = new MacroInfo(macroContainter, m.getName().getOffset(), m.getName().getEndOffset(), m.getFile().getPath());
                            }
                        }
                        if (mi != null) {
                            mf = new MacroReference(csmFile, token, mi);
                        } else {
                            // as backup
                            mf = new SysMacroReference(csmFile, token, m);
                        }
                        break;
                    case COMPILER_PREDEFINED:
                    case POSITION_PREDEFINED:
                    case USER_SPECIFIED:
                    default:
                        mf = new SysMacroReference(csmFile, token, m);
                        break;
                }
            }
        }
        if (mf != null) {
            // add any not fun-like macro
            // or add all if specified by input parameter
            if (!funLike || !addOnlyIfNotFunLikeMacro) {
                references.add(mf);
                // clear return value, because already added
                mf = null;
            }
        }
        return mf;
    }

    private void analyzeList(List<APTToken> tokens) {
        if (tokens != null) {
            for (APTToken token : tokens) {
                analyzeToken(token, false);
            }
        }
    }

    private void analyzeStream(TokenStream ts, boolean checkFunLikeMacro) {
        if (ts != null) {
            try {
                for (APTToken token = (APTToken) ts.nextToken(); !APTUtils.isEOF(token); ) {
                    CsmReference mr = analyzeToken(token, checkFunLikeMacro);
                    token = (APTToken) ts.nextToken();
                    if (mr != null) {
                        // it is fun-like macro candidate
                        assert checkFunLikeMacro;
                        // add only if next token is "("
                        if (token.getType() == APTTokenTypes.LPAREN) {
                            references.add(mr);
                        }
                    }
                }
            } catch (TokenStreamException ex) {
		DiagnosticExceptoins.register(ex);
            }
        }
    }

    private static class SysMacroReference extends OffsetableBase implements CsmReference {

        private final CsmObject ref;

        public SysMacroReference(CsmFile file, APTToken token, APTMacro macro) {
            super(file, token.getOffset(), token.getEndOffset());
            CsmMacro.Kind kind;
            switch(macro.getKind()) {
                case COMPILER_PREDEFINED:
                    kind = CsmMacro.Kind.COMPILER_PREDEFINED;
                    break;
                case POSITION_PREDEFINED:
                    kind = CsmMacro.Kind.POSITION_PREDEFINED;
                    break;
                case DEFINED:
                    kind = CsmMacro.Kind.DEFINED;
                    break;
                case USER_SPECIFIED:
                    kind = CsmMacro.Kind.USER_SPECIFIED;
                    break;
                default:
                    System.err.println("unexpected kind in macro " + macro);
                    kind = CsmMacro.Kind.USER_SPECIFIED;
                    break;
            }
            ref = MacroImpl.createSystemMacro(token.getTextID(), APTUtils.stringize(macro.getBody(), false), ((ProjectBase) file.getProject()).getUnresolvedFile(), kind);
        }

        public CsmObject getReferencedObject() {
            return ref;
        }

        public CsmObject getOwner() {
            return null;
        }

        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DECLARATION;
        }

        @Override
        public CharSequence getText() {
            return TextCache.getManager().getString(super.getText());
        }        
    }

    private static class MacroReference extends OffsetableBase implements CsmReference {

        private CsmObject ref;
        private final CharSequence macroName;
        private final MacroInfo mi;

        public MacroReference(CsmFile file, APTToken macro, MacroInfo mi) {
            super(file, macro.getOffset(), macro.getEndOffset());
            this.macroName = macro.getTextID();
            assert macroName != null;
//        this.isSystem = isSystem != null ? isSystem.booleanValue() : mi != null;
//        assert !(isSystem != null && isSystem.booleanValue() && mi != null);
            this.mi = mi;
        }

        public CsmObject getReferencedObject() {
            if (ref == null && mi != null) {
                CsmFile target = getTargetFile();
                if (target != null) {
                    CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(macroName, true, true, false);
                    for (Iterator<CsmMacro> it = CsmSelect.getMacros(target, filter); it.hasNext();) {
                        CsmMacro macro = it.next();
                        if (macro!=null && mi.startOffset == macro.getStartOffset()) {
                            ref = macro;
                        }
                    }
                    if (ref == null) {
                        // reference was made so it was macro during APTFindMacrosWalker's walk. Parser missed this variance of header and
                        // we have to create MacroImpl for skipped filepart on the spot (see IZ#130897)
                        if (target instanceof Unresolved.UnresolvedFile) {
                            ref = MacroImpl.createSystemMacro(macroName, "", target, CsmMacro.Kind.USER_SPECIFIED);
                        } else {
                            ref = new MacroImpl(macroName, null, "", target, new OffsetableBase(target, mi.startOffset, mi.endOffset), CsmMacro.Kind.DEFINED);
                        }
                    }
                }
            }
            return ref;
        }

        private CsmFile getTargetFile() {
            CsmFile current = UIDCsmConverter.UIDtoFile(mi.targetFile);
            if (current != null && mi.includePath != null) {
                File searchFile = new File(mi.includePath.toString());
                ProjectBase targetPrj = ((ProjectBase) current.getProject()).findFileProject(searchFile.getAbsolutePath());
                if (targetPrj == null) {
                    searchFile = FileUtil.normalizeFile(searchFile);
                    targetPrj = ((ProjectBase) current.getProject()).findFileProject(searchFile.getAbsolutePath());
                }
                if (targetPrj != null) {
                    current = targetPrj.getFile(searchFile, false);
                    // if file belongs to project, it should be not null
                    // but info could be obsolete
                }
                // try full model?
//                if (current == null) {
//                    current = CsmModelAccessor.getModel().findFile(mi.includePath);
//                }
            }
            return current;
        }

        public CsmObject getOwner() {
            return getTargetFile();
        }
        
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DECLARATION;
        }

        @Override
        public CharSequence getText() {
            return DefaultCache.getManager().getString(super.getText());
        }
    }

    private CsmFile getMacroFile(APTMacro m) {
        CsmFile out = null;
        if (m.getFile() != null) {
            CharSequence path = m.getFile().getPath();
            out = macro2file.get(path);
            if (out == null) {
                ProjectBase targetPrj = ((ProjectBase) csmFile.getProject()).findFileProject(path);
                if (targetPrj != null) {
                    out = targetPrj.getFile(new File(path.toString()), false);
                    // if file belongs to project, it should be not null
                    // but info could be obsolete
                }
                if (out != null) {
                    macro2file.put(path, out);
                }
            }
        }
        return out;
    }
}
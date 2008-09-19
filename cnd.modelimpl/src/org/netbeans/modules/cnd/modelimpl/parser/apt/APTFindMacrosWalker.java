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
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
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
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.utils.cache.TextCache;


/**
 * Basic walker to find macroes for semantic highlighting
 * TODO: maybe it should be one walker for any semantic HL activity, because
 * they used altogether.
 *
 * @author Sergey Grinev
 */
public class APTFindMacrosWalker extends APTDefinesCollectorWalker {

    public APTFindMacrosWalker(APTFile apt, CsmFile csmFile, APTPreprocHandler preprocHandler) {
        super(apt, csmFile, preprocHandler);
    }

    @Override
    protected void onDefine(APT apt) {
        APTDefine defineNode = (APTDefine) apt;
        APTToken name = (APTToken) defineNode.getName();
        addReference(name, new MacroInfo(csmFile, defineNode.getOffset(), defineNode.getEndOffset(), null));
        analyzeList(defineNode.getBody());
        super.onDefine(apt);
    }

    @Override
    protected boolean onIf(APT apt) {
        analyzeStream(((APTIf) apt).getCondition());
        return super.onIf(apt);
    }

    @Override
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        analyzeStream(((APTElif) apt).getCondition());
        return super.onElif(apt, wasInPrevBranch);
    }

    @Override
    protected boolean onIfndef(APT apt) {
        analyzeToken((APTToken) ((APTIfndef)apt).getMacroName());
        return super.onIfndef(apt);
    }

    @Override
    protected boolean onIfdef(APT apt) {
        analyzeToken((APTToken) ((APTIfdef)apt).getMacroName());
        return super.onIfdef(apt);
    }
    private final List<CsmReference> references = new ArrayList<CsmReference>();

    @Override
    protected void onUndef(APT apt) {
        analyzeToken((APTToken) ((APTUndefine)apt).getName());
        super.onUndef(apt);
    }

    @Override
    protected void onInclude(APT apt) {
        analyzeStream(((APTInclude)apt).getInclude());
        super.onInclude(apt);
    }

    @Override
    protected void onIncludeNext(APT apt) {
        analyzeStream(((APTIncludeNext)apt).getInclude());
        super.onIncludeNext(apt);
    }

    public List<CsmReference> getCollectedData() {
        return references;
    }

    @Override
    public TokenStream getTokenStream() {
        TokenStream ts = super.getTokenStream();
        analyzeStream(ts);
        return null; // tokenstream set to EOF? it's no good
    }

    private void analyzeToken(Token token) {
        APTToken apttoken = (APTToken) token;
        if (apttoken != null) {
            APTMacro m = getMacroMap().getMacro(apttoken);
            if (m != null) {
                if (m.isSystem()) {
                    addSysReference(apttoken, m);
                } else {
                    MacroInfo mi = macroRefMap.get(apttoken.getText());
                    if (mi != null) {
                        addReference(apttoken, mi);
                    } else {
                        // this is user-defined macro (iz132150)
                        // XXX: update to API call then iz132308 will be fixed
                        addSysReference(apttoken, m);
                    }
                }
            }
//            else if (apttoken.getType() == CPPTokenTypes.ID_DEFINED) {
//                addReference(apttoken, macroRefMap.get(apttoken.getText()));
//            }
        }
    }

    private void analyzeList(List<Token> tokens) {
        if (tokens != null) {
            for (Token token : tokens) {
                analyzeToken(token);
            }
        }
    }

    private void analyzeStream(TokenStream ts) {
        if (ts != null) {
            try {
                for (Token token = ts.nextToken(); !APTUtils.isEOF(token); token = ts.nextToken()) {
                    analyzeToken(token);
                }
            } catch (TokenStreamException ex) {
		DiagnosticExceptoins.register(ex);
            }
        }
    }

    private void addSysReference(APTToken token, APTMacro macro) {
        references.add(new SysMacroReference(csmFile, token, macro));
    }

    private void addReference(APTToken token, MacroInfo mi) {
        if (token != null) {
            MacroReference mf = new MacroReference(csmFile, token, mi);
            references.add(mf);
        }
    }

    private static class SysMacroReference extends OffsetableBase implements CsmReference {

        private final CsmObject ref;

        public SysMacroReference(CsmFile file, APTToken token, APTMacro macro) {
            super(file, token.getOffset(), token.getEndOffset());
            ref = MacroImpl.createSystemMacro(token.getText(), APTUtils.stringize(macro.getBody(), false), ((ProjectBase) file.getProject()).getUnresolvedFile());
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
            return TextCache.getString(super.getText());
        }        
    }

    private static class MacroReference extends OffsetableBase implements CsmReference {

        private CsmObject ref;
        private final String macroName;
        private final MacroInfo mi;

        public MacroReference(CsmFile file, APTToken macro, MacroInfo mi) {
            super(file, macro.getOffset(), macro.getEndOffset());
            this.macroName = macro.getText();
            assert macroName != null;
//        this.isSystem = isSystem != null ? isSystem.booleanValue() : mi != null;
//        assert !(isSystem != null && isSystem.booleanValue() && mi != null);
            this.mi = mi;
        }

        public CsmObject getReferencedObject() {
            if (ref == null && mi != null) {
                CsmFile target = getTargetFile();
                if (target != null) {
                    Collection<CsmMacro> macrosCollection = target.getMacros();
                    List<CsmMacro> macros = new ArrayList<CsmMacro>(macrosCollection);
                    for (int i = macros.size() - 1; i >= 0; i--) {
                        CsmMacro macro = macros.get(i);
                        if (macro!=null && mi.startOffset == macro.getStartOffset()) {
                            ref = macro;
                            break;
                        }
                    }
                    if (ref == null) {
                        // reference was made so it was macro during APTFindMacrosWalker's walk. Parser missed this variance of header and
                        // we have to create MacroImpl for skipped filepart on the spot (see IZ#130897)
                        if (target instanceof Unresolved.UnresolvedFile) {
                            ref = MacroImpl.createSystemMacro(macroName, "", target);
                        } else {
                            ref = new MacroImpl(macroName, null, "", target, new OffsetableBase(target, mi.startOffset, mi.endOffset), false);
                        }
                    }
                }
            }
            return ref;
        }

        private CsmFile getTargetFile() {
            CsmFile current = UIDCsmConverter.UIDtoFile(mi.targetFile);
            if (current != null && mi.includePath != null) {
                ProjectBase targetPrj = ((ProjectBase) current.getProject()).findFileProject(mi.includePath);
                if (targetPrj != null) {
                    current = targetPrj.getFile(new File(mi.includePath));
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
            return TextCache.getString(super.getText());
        }
    }
}
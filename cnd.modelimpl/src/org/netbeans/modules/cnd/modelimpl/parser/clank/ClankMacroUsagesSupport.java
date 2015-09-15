/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 * Misc static methods used for processing of macros
 * @author vkvashin
 */
public class ClankMacroUsagesSupport {

    private ClankMacroUsagesSupport() {
    }

    public static List<CsmReference> getMacroUsages(FileImpl fileImpl, FileImpl startFile, ClankDriver.ClankFileInfo foundFileInfo) {
        if (foundFileInfo == null) {
            return Collections.emptyList();
        }
        List<CsmReference> res = new ArrayList<>();        
        ClankMacroUsagesSupport.addPreprocessorDirectives(fileImpl, res, foundFileInfo);
        ClankMacroUsagesSupport.addMacroExpansions(fileImpl, res, startFile, foundFileInfo);
        Collections.sort(res, new Comparator<CsmReference>() {
            @Override
            public int compare(CsmReference o1, CsmReference o2) {
                return o1.getStartOffset() - o2.getStartOffset();
            }
        });
        return res;
    }

    public static void addPreprocessorDirectives(FileImpl curFile, List<CsmReference> res, ClankDriver.ClankFileInfo cache) {
        assert res != null;
        assert curFile != null;
        assert cache != null;
        for (ClankDriver.ClankPreprocessorDirective cur : cache.getPreprocessorDirectives()) {
            if (cur instanceof ClankDriver.ClankMacroDirective) {
                addMacro(curFile, res, (ClankDriver.ClankMacroDirective) cur);
            }
        }
    }

    public static void addMacroExpansions(FileImpl curFile, List<CsmReference> res, FileImpl startFile, ClankDriver.ClankFileInfo cache) {
        for (ClankDriver.MacroExpansion cur : cache.getMacroExpansions()) {
            ClankDriver.ClankMacroDirective directive = cur.getReferencedMacro();
            if (directive != null) {
                res.add(MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getStartOfset() + cur.getMacroNameLength(), startFile, directive));
            } else {
                // TODO: process invalid macro definition
                assert false : "Not found referenced ClankMacroDirective " + cur;
            }
        }
        for (ClankDriver.MacroUsage cur : cache.getMacroUsages()) {
            ClankDriver.ClankMacroDirective directive = cur.getReferencedMacro();
            if (directive != null) {
                res.add(MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getEndOfset(), startFile, directive));
            } else {
                // TODO: process invalid macro definition
                assert false : "Not found referenced ClankMacroDirective " + cur;
            }
        }
    }

    private static void addMacro(FileImpl curFile, List<CsmReference> res, ClankDriver.ClankMacroDirective ppDirective) {
        if (!ppDirective.isDefined()) {
            // only #define are handled by old model, not #undef
            return;
        }
        CsmMacro.Kind kind = CsmMacro.Kind.DEFINED;
        List<CharSequence> params = ppDirective.getParameters();
        CharSequence name = ppDirective.getMacroName();
        String body = "";
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        int macroNameOffset = ppDirective.getMacroNameOffset();
        CsmMacro impl = MacroImpl.create(name, params, body/*sb.toString()*/, curFile, startOffset, endOffset, kind);
        MacroDeclarationReference macroDeclarationReference = new MacroDeclarationReference(curFile, impl, macroNameOffset);
        res.add(macroDeclarationReference);
    }
}

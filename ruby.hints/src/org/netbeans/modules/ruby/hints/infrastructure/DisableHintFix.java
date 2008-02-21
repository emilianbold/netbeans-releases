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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints.infrastructure;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.fpi.gsf.CompilationInfo;
import org.netbeans.modules.ruby.hints.options.HintsAdvancedOption;
import org.netbeans.modules.ruby.hints.options.HintsSettings;
import org.netbeans.modules.ruby.hints.spi.Rule;
import org.netbeans.modules.ruby.hints.spi.UserConfigurableRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.util.NbBundle;

/**
 *
 * @author Tor Norbye
 */
public class DisableHintFix implements EnhancedFix {
    private final UserConfigurableRule rule;
    private final CompilationInfo info;
    private final int caretPos;
    private final String sortText;
    
    public DisableHintFix(UserConfigurableRule rule, CompilationInfo info, int caretPos, String sortText) {
        this.rule = rule;
        this.info = info;
        this.caretPos = caretPos;
        this.sortText = sortText;
    }

    public String getText() {
        return NbBundle.getMessage(HintsAdvancedOption.class, "DisableHint");
    }
    
    public ChangeInfo implement() throws Exception {
        HintsSettings.setEnabled(RulesManager.getInstance().getPreferences(rule, null), false);

        // Force a refresh
        // HACK ALERT!
        RubyHintsProvider provider = new RubyHintsProvider();
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        if (caretPos == -1) {
            provider.computeHints(info, result);
            HintsController.setErrors(info.getFileObject(), 
                    "org.netbeans.modules.gsfret.hints.infrastructure.HintsTask", result);
        } else {
            provider.computeSuggestions(info, result, caretPos);
            HintsController.setErrors(info.getFileObject(), 
                    "org.netbeans.modules.gsfret.hints.infrastructure.SuggestionsTask", result);
        }
        
        return null;
    }

    public CharSequence getSortText() {
        return sortText;
    }
}

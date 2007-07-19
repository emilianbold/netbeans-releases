/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints.infrastructure;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.modules.ruby.hints.options.HintsAdvancedOption;
import org.netbeans.modules.ruby.hints.options.HintsSettings;
import org.netbeans.modules.ruby.hints.spi.Rule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.util.NbBundle;

/**
 *
 * @author Tor Norbye
 */
public class DisableHintFix implements Fix {
    private Rule rule;
    private CompilationInfo info;
    private int caretPos;
    
    public DisableHintFix(Rule rule, CompilationInfo info, int caretPos) {
        this.rule = rule;
        this.info = info;
        this.caretPos = caretPos;
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
                    "org.netbeans.modules.retouche.hints.infrastructure.HintsTask", result);
        } else {
            provider.computeSuggestions(info, result, caretPos);
            HintsController.setErrors(info.getFileObject(), 
                    "org.netbeans.modules.retouche.hints.infrastructure.SuggestionsTask", result);
        }
        
        return null;
    }
}

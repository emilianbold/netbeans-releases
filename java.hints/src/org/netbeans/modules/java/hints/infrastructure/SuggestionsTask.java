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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.editor.semantic.ScanningCancellableTask;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;

/**
 *
 * @author Jan Lahoda
 */
public class SuggestionsTask extends ScanningCancellableTask<CompilationInfo> {
    
    public SuggestionsTask() {
    }
    
    public void run(CompilationInfo info) throws Exception {
        resume();
        
        Map<Kind, List<TreeRule>> suggestions = new HashMap<Kind, List<TreeRule>>();
        
        suggestions.putAll(RulesManager.getInstance().getHints(true));
        
        for (Entry<Kind, List<TreeRule>> e : RulesManager.getInstance().getSuggestions().entrySet()) {
            List<TreeRule> rules = suggestions.get(e.getKey());
            
            if (rules != null) {
                List<TreeRule> res = new LinkedList<TreeRule>();
                
                res.addAll(rules);
                res.addAll(e.getValue());
                
                suggestions.put(e.getKey(), res);
            } else {
                suggestions.put(e.getKey(), e.getValue());
            }
        }
        
        if (suggestions.isEmpty()) {
            HintsController.setErrors(info.getFileObject(), SuggestionsTask.class.getName(), Collections.<ErrorDescription>emptyList());
            return ;
        }
        
        int position = CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject());
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        if (position != (-1)) {
            TreePath tp = info.getTreeUtilities().pathFor(position);
            
            while (tp != null) {
                if (isCancelled())
                    return ;
                
                Kind k = tp.getLeaf().getKind();
                List<TreeRule> rules = suggestions.get(k);
                
                if (rules != null && !HintsTask.isInGuarded(info, tp)) {
                    for (TreeRule rule : rules) {
                        if (isCancelled())
                            return ;
                        
                        boolean enabled = true;
                        
                        if (rule instanceof AbstractHint) {
                            enabled = HintsSettings.isEnabled((AbstractHint)rule);
                        }
                        
                        if (enabled) {
                            List<ErrorDescription> errors = rule.run(info, tp);
                            
                            if (errors != null) {
                                result.addAll(errors);
                            }
                        }
                    }
                }
                
                tp = tp.getParentPath();
            }
        }
        
        if (isCancelled())
            return ;
        
        HintsController.setErrors(info.getFileObject(), SuggestionsTask.class.getName(), result);
    }
    
}

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
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.editor.semantic.ScanningCancellableTask;
import org.netbeans.modules.java.hints.spi.TreeRule;
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
        Map<Kind, List<TreeRule>> suggestions = RulesManager.getInstance().getSuggestions();
        
        if (suggestions.isEmpty()) {
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
                
                if (rules != null) {
                    for (TreeRule rule : rules) {
                        if (isCancelled())
                            return ;
                        
                        List<ErrorDescription> errors = rule.run(info, tp);
                        
                        if (errors != null) {
                            result.addAll(errors);
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

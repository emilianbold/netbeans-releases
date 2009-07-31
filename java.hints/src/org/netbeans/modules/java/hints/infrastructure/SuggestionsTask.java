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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.editor.semantic.ScanningCancellableTask;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;

/**
 *
 * @author Jan Lahoda
 */
public class SuggestionsTask extends ScanningCancellableTask<CompilationInfo> {

    private final List<ErrorDescription> result;
    
    public SuggestionsTask() {
        result = new ArrayList<ErrorDescription>();
    }
    
    public void run(CompilationInfo info) throws Exception {
        resume();
        result.clear();
        
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

    public List<ErrorDescription> getSuggestions() {
        return result;
    }

}

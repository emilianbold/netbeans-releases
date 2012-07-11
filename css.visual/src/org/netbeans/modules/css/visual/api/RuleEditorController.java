/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual.api;

import javax.swing.JComponent;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.openide.util.Parameters;

/**
 *
 * @author marekfukala
 */
public final class RuleEditorController {
    
    private RuleEditorPanel peer;

    public static RuleEditorController createInstance() {
        return new RuleEditorController(new RuleEditorPanel());
    }
    
    private RuleEditorController(RuleEditorPanel peer) {
        this.peer = peer;
    }
    
    public JComponent getRuleEditorComponent() {
        return peer;
    }
    
    /**
     * Sets the css source model to the {@link RuleEditorPanel}.
     * 
     * All subsequent actions refers to this model.
     * 
     * @param cssSourceModel an instance of {@link Model}
     */
    public void setModel(Model cssSourceModel) {
        Parameters.notNull("cssSourceModel", cssSourceModel);
        peer.setModel(cssSourceModel);
    }
    
    /**
     * Sets the given css rule as the context.
     * 
     * @param rule a non null instance of {@link Rule). <b>MUST belong to the selected css model instance!</b>
     */
    public void setRule(Rule rule) {
        Parameters.notNull("rule", rule);
        peer.setRule(rule);
    }
    
    /**
     * Switches the panel to the 'no selected rule mode'. 
     * The panel will show some informational message instead of the css rule properties.
     */
    public void setNoRuleState() {
        peer.setNoRuleState();
    }
    
    public void setSortMode(SortMode sortMode) {
        peer.setSortMode(sortMode);
    }
    
    /**
     * Show physical propertis + all existing unused properties.
     * 
     * Shows physical properties of the selected rule first, then all
     * of the existing unused properties. The unused the properties are sorted
     * alphabetically.
     */
    public void setShowAllProperties(boolean enabled) {
        peer.setShowAllProperties(enabled);
    }
    
     /**
     * Show property categories.
     * 
     * Shows categories for the css properties. In each category the physical
     * properties of the selected rule are show first, then the rest of 
     * existing css properties belonging to the category. All unused properties 
     * sorted  alphabetically, categories also sorted alphabetically.
     */
    public void setShowCategories(boolean enabled) {
        peer.setShowCategories(enabled);
    }
    
    
    /**
     * Registers an instance of {@link RuleEditorListener} to the component.
     * @param listener
     * @return true if the listeners list changed
     */
    public boolean addRuleEditorListener(RuleEditorListener listener) {
        return peer.addRuleEditorListener(listener);
    }
    
    /**
     * Unregisters an instance of {@link RuleEditorListener} from the component.
     * @param listener
     * @return true if the listeners list changed (listener removed)
     */
    public boolean removeRuleEditorListener(RuleEditorListener listener) {
        return peer.removeRuleEditorListener(listener);
    }
    
    
}

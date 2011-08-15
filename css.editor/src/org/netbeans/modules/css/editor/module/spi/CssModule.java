/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.web.common.api.Pair;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class CssModule {
    
    public Collection<RenderingEngine> getExtraRenderingEngines() {
        return null;
    }
    
    public Collection<PropertyDescriptor> getPropertyDescriptors() {
        return Collections.emptyList();
    }
    
    /**
     * If one wants to customize the css completion beyond the level which the PropertyDescriptor-s
     * allows, may use this method to achieve this.
     * 
     * @param context the code completion context
     * @return a list of completion proposals
     */
    public List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        return Collections.emptyList();
    }
  
    /**
     * May return null if there are no semantic highlights to show.
     * @param <T>
     * @param context
     * @param result
     * @return 
     */
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        return null;
    }
    
    /**
     * May return null if there are no mark occurrences areas
     * 
     * @param <T>
     * @param context
     * @param result
     * @return 
     */
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {
        return null;
    }
    
     /**
     * May return null if there are no folds
     * 
     * @param <T>
     * @param context
     * @param result
     * @return 
     */
    public <T extends Map<String, List<OffsetRange>>> NodeVisitor<T> getFoldsNodeVisitor(FeatureContext context, T result) {
        return null;
    }
    
     /**
     * May return null if there is no declaration at the caret position
     * 
     * @param <T>
     * @param context
     * @param result
     * @return 
     */
    public Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclaration(Document document, int caretOffset) {
        return null;
    }
    
    
     /**
     * May return null if there are no structure items
     * 
     * @param <T>
     * @param context
     * @param result
     * @return 
     */
    public <T extends List<StructureItem>> NodeVisitor<T> getStructureItemsNodeVisitor(FeatureContext context, T result) {
        return null;
    }
    
}

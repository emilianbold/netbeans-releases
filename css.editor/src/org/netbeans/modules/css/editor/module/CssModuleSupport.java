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
package org.netbeans.modules.css.editor.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureCancel;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.PropertyDescriptor;
import org.netbeans.modules.css.editor.properties.parser.PropertyModel;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.web.common.api.Pair;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class CssModuleSupport {
    
    private static final Map<String, PropertyModel> PROPERTY_MODELS = new HashMap<String, PropertyModel>();
    
    public static Collection<? extends CssModule> getModules() {
        return Lookup.getDefault().lookupAll(CssModule.class);
    }
    
    public static Map<OffsetRange, Set<ColoringAttributes>> getSemanticHighlights(FeatureContext context, FeatureCancel cancel) {
        Map<OffsetRange, Set<ColoringAttributes>> all = new HashMap<OffsetRange, Set<ColoringAttributes>>();
        final Collection<NodeVisitor<Map<OffsetRange, Set<ColoringAttributes>>>> visitors = new ArrayList<NodeVisitor<Map<OffsetRange, Set<ColoringAttributes>>>>();
        
        for(CssModule module: getModules()) {
            NodeVisitor<Map<OffsetRange, Set<ColoringAttributes>>> visitor = module.getSemanticHighlightingNodeVisitor(context, all);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if(visitor != null) {
                visitors.add(visitor);
            }
        }
        
        if(cancel.isCancelled()) {
            return Collections.emptyMap();
        }
        
        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for(NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);
        
        return all;
    }
    
    public static Set<OffsetRange> getMarkOccurrences(EditorFeatureContext context, FeatureCancel cancel) {
        Set<OffsetRange> all = new HashSet<OffsetRange>();
        final Collection<NodeVisitor<Set<OffsetRange>>> visitors = new ArrayList<NodeVisitor<Set<OffsetRange>>>();
        
        for(CssModule module: getModules()) {
            NodeVisitor<Set<OffsetRange>> visitor = module.getMarkOccurrencesNodeVisitor(context, all); 
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if(visitor != null) {
                visitors.add(visitor);
            }
        }
        
        if(cancel.isCancelled()) {
            return Collections.emptySet();
        }
        
        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for(NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);
        
        return all;
        
    }
    
    public static Map<String, List<OffsetRange>> getFolds(FeatureContext context, FeatureCancel cancel) {
        Map<String, List<OffsetRange>> all = new HashMap<String, List<OffsetRange>>();
        final Collection<NodeVisitor<Map<String, List<OffsetRange>>>> visitors = new ArrayList<NodeVisitor<Map<String, List<OffsetRange>>>>();
        
        for(CssModule module: getModules()) {
            NodeVisitor<Map<String, List<OffsetRange>>> visitor = module.getFoldsNodeVisitor(context, all); 
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if(visitor != null) {
                visitors.add(visitor);
            }
        }
        
        if(cancel.isCancelled()) {
            return Collections.emptyMap();
        }
        
        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for(NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);
        
        return all;
        
    }
    
    public static Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclarationLocation(Document document, int caretOffset, FeatureCancel cancel) {
        for(CssModule module: getModules()) {
            if(cancel.isCancelled()) {
                return null;
            }
            Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> declarationLocation = module.getDeclaration(document, caretOffset);
            if(declarationLocation != null) {
                return declarationLocation;
            }
        }
        return null;
    }
     
    public static List<StructureItem> getStructureItems(FeatureContext context, FeatureCancel cancel) {
        List<StructureItem> all = new ArrayList<StructureItem>();
        final Collection<NodeVisitor<List<StructureItem>>> visitors = new ArrayList<NodeVisitor<List<StructureItem>>>();
        
        for(CssModule module: getModules()) {
            NodeVisitor<List<StructureItem>> visitor = module.getStructureItemsNodeVisitor(context, all); 
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if(visitor != null) {
                visitors.add(visitor);
            }
        }
        
        if(cancel.isCancelled()) {
            return Collections.emptyList();
        }
        
        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for(NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);
        
        return all;
        
    }
    
    //todo cache
    public static Map<String, PropertyDescriptor> getPropertyDescriptors() {
        Map<String, PropertyDescriptor> all = new HashMap<String, PropertyDescriptor>();
        for(CssModule module : getModules()) {
            for(PropertyDescriptor pd: module.getPropertyDescriptors()) {
                all.put(pd.getName(), pd);
            }
        }
        return all;
    }
    
    public static PropertyModel getProperty(String name) {
        synchronized (PROPERTY_MODELS) {
            PropertyModel model = PROPERTY_MODELS.get(name);
            if(model == null) {
                model = new PropertyModel(getPropertyDescriptors().get(name));
                PROPERTY_MODELS.put(name, model);
            }
            return model;
        }
        
    }
    
    public static List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        List<CompletionProposal> all = new ArrayList<CompletionProposal>();
        for(CssModule module : getModules()) {
            all.addAll(module.getCompletionProposals(context));
        }
        return all;
    }
    
}

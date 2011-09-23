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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureCancel;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.editor.module.spi.Property;
import org.netbeans.modules.css.editor.module.spi.PropertySupportResolver;
import org.netbeans.modules.css.editor.properties.parser.GrammarParser;
import org.netbeans.modules.css.editor.properties.parser.PropertyModel;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.web.common.api.Pair;
import org.openide.util.Lookup;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssModuleSupport {

    private static final Logger LOGGER = Logger.getLogger(CssModuleSupport.class.getSimpleName());
    //TODO possibly add support for refreshing the cached data based on css module changes in the lookup
    private static final AtomicReference<Map<String, Property>> PROPERTIES = new AtomicReference<Map<String, Property>>();
    private static final Map<String, PropertyModel> PROPERTY_MODELS = new HashMap<String, PropertyModel>();

    public static Collection<? extends CssEditorModule> getModules() {
        return Lookup.getDefault().lookupAll(CssEditorModule.class);
    }

    public static Map<OffsetRange, Set<ColoringAttributes>> getSemanticHighlights(FeatureContext context, FeatureCancel cancel) {
        Map<OffsetRange, Set<ColoringAttributes>> all = new HashMap<OffsetRange, Set<ColoringAttributes>>();
        final Collection<NodeVisitor<Map<OffsetRange, Set<ColoringAttributes>>>> visitors = new ArrayList<NodeVisitor<Map<OffsetRange, Set<ColoringAttributes>>>>();

        for (CssEditorModule module : getModules()) {
            NodeVisitor<Map<OffsetRange, Set<ColoringAttributes>>> visitor = module.getSemanticHighlightingNodeVisitor(context, all);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        if (cancel.isCancelled()) {
            return Collections.emptyMap();
        }

        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for (NodeVisitor visitor : visitors) {
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

        for (CssEditorModule module : getModules()) {
            NodeVisitor<Set<OffsetRange>> visitor = module.getMarkOccurrencesNodeVisitor(context, all);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        if (cancel.isCancelled()) {
            return Collections.emptySet();
        }

        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for (NodeVisitor visitor : visitors) {
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

        for (CssEditorModule module : getModules()) {
            NodeVisitor<Map<String, List<OffsetRange>>> visitor = module.getFoldsNodeVisitor(context, all);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        if (cancel.isCancelled()) {
            return Collections.emptyMap();
        }

        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for (NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);

        return all;

    }

    public static Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclarationLocation(Document document, int caretOffset, FeatureCancel cancel) {
        for (CssEditorModule module : getModules()) {
            if (cancel.isCancelled()) {
                return null;
            }
            Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> declarationLocation = module.getDeclaration(document, caretOffset);
            if (declarationLocation != null) {
                return declarationLocation;
            }
        }
        return null;
    }

    public static List<StructureItem> getStructureItems(FeatureContext context, FeatureCancel cancel) {
        List<StructureItem> all = new ArrayList<StructureItem>();
        final Collection<NodeVisitor<List<StructureItem>>> visitors = new ArrayList<NodeVisitor<List<StructureItem>>>();

        for (CssEditorModule module : getModules()) {
            NodeVisitor<List<StructureItem>> visitor = module.getStructureItemsNodeVisitor(context, all);
            //modules may return null visitor instead of a dummy empty visitor 
            //to speed up the parse tree visiting when there're no result
            if (visitor != null) {
                visitors.add(visitor);
            }
        }

        if (cancel.isCancelled()) {
            return Collections.emptyList();
        }

        cancel.attachCancelAction(new Runnable() {

            @Override
            public void run() {
                for (NodeVisitor visitor : visitors) {
                    visitor.cancel();
                }
            }
        });

        NodeVisitor.visitChildren(context.getParseTreeRoot(), visitors);

        return all;

    }

    public static Collection<Property> getProperties() {
        PROPERTIES.compareAndSet(null, loadProperties());
        return PROPERTIES.get().values();
    }

    private static Map<String, Property> loadProperties() {
        Map<String, Property> all = new HashMap<String, Property>();
        for (CssEditorModule module : getModules()) {
            for (Property pd : module.getProperties()) {
                Property original = all.put(pd.getName(), pd);
                if (original != null) {
                    //TODO - add support for more property values for one property name
                    //LOGGER.warning(String.format("Duplicate property %s found, offending css module: %s", pd.getName(), module));
                }
            }
        }
        return all;
    }

    public static Property getProperty(String propertyName) {
        PROPERTIES.compareAndSet(null, loadProperties());

        //try to resolve the refered element name with the at-sign prefix so
        //the property appearance may contain link to appearance, which in fact
        //will be resolved as the @appearance property:
        //
        //appearance=<appearance> |normal
        //@appearance=...
        //
        StringBuilder sb = new StringBuilder().append(GrammarParser.INVISIBLE_PROPERTY_PREFIX).append(propertyName);
        Property invisibleProperty = PROPERTIES.get().get(sb.toString());
        
        return invisibleProperty != null ? invisibleProperty : PROPERTIES.get().get(propertyName);
    }

    public static PropertyModel getPropertyModel(String name) {
        synchronized (PROPERTY_MODELS) {
            PropertyModel model = PROPERTY_MODELS.get(name);
            if (model == null) {
                Property pd = getProperty(name);
                model = pd != null ? new PropertyModel(pd) : null;
                PROPERTY_MODELS.put(name, model);
            }
            return model;
        }

    }

    public static List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        List<CompletionProposal> all = new ArrayList<CompletionProposal>();
        for (CssEditorModule module : getModules()) {
            all.addAll(module.getCompletionProposals(context));
        }
        return all;
    }

    //todo: cache results of most of the methods below!!!!!!!!!!!!!!!
    //TODO: the pseudo elements and classes should be context aware, not simple strings ... later
    public static Collection<String> getPseudoClasses() {
        Collection<String> all = new HashSet<String>();
        for (CssEditorModule module : getModules()) {
            Collection<String> vals = module.getPseudoClasses();
            if (vals != null) {
                all.addAll(vals);
            }
        }
        return all;
    }

    public static Collection<String> getPseudoElements() {
        Collection<String> all = new HashSet<String>();
        for (CssEditorModule module : getModules()) {
            Collection<String> vals = module.getPseudoElements();
            if (vals != null) {
                all.addAll(vals);
            }
        }
        return all;
    }

    public static SortedSet<Browser> getBrowsers() {
        //sort by browser name
        SortedSet<Browser> all = new TreeSet<Browser>(new Comparator<Browser>() {

            @Override
            public int compare(Browser t, Browser t1) {
                return t.getName().compareTo(t1.getName());
            }
        });
        for (CssEditorModule module : getModules()) {
            Collection<Browser> extraBrowsers = module.getExtraBrowsers();
            if (extraBrowsers != null) {
                all.addAll(extraBrowsers);
            }
        }
        return all;
    }

    public static boolean isPropertySupported(String propertyName, Browser browser) {
        for (CssEditorModule module : getModules()) {
            PropertySupportResolver.Factory factory = module.getPropertySupportResolverFactory();
            if (factory != null) {
                PropertySupportResolver resolver = factory.createPropertySupportResolver(browser);
                if (resolver != null) {
                    if (resolver.isPropertySupported(propertyName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static HelpResolver getHelpResolver() {
        return new HelpResolver() {

            @Override
            public String getHelp(Property property) {
                StringBuilder sb = new StringBuilder();
                for (HelpResolver resolver : getSortedHelpResolvers()) {
                    String help = resolver.getHelp(property);
                    if (help != null) {
                        sb.append(help);
                    }
                }
                return sb.toString();
            }

            @Override
            public URL resolveLink(Property property, String link) {
                for (HelpResolver resolver : getSortedHelpResolvers()) {
                    URL url = resolver.resolveLink(property, link);
                    if (url != null) {
                        return url;
                    }
                }
                return null;
            }

            @Override
            public int getPriority() {
                return 0;
            }
        };

    }

    private static Collection<HelpResolver> getSortedHelpResolvers() {
        List<HelpResolver> list = new ArrayList<HelpResolver>();
        for (CssEditorModule module : getModules()) {
            Collection<HelpResolver> resolvers = module.getHelpResolvers();
            if (resolvers != null) {
                list.addAll(resolvers);
            }
        }

        Collections.sort(list, new Comparator<HelpResolver>() {

            @Override
            public int compare(HelpResolver t1, HelpResolver t2) {
                int i1 = t1.getPriority();
                int i2 = t2.getPriority();
                return new Integer(i1).compareTo(new Integer(i2));
            }
        });

        return list;
    }
}

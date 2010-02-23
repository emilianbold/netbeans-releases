/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.hints.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.csl.api.Rule.SelectionRule;
import org.netbeans.modules.csl.api.Rule.UserConfigurableRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Tor Norbye
 */
public class GsfHintsManager extends HintsProvider.HintsManager {
    public GsfHintsManager(String mimeType, HintsProvider provider, Language language) {
        this.mimeType = mimeType;
        this.provider = provider;

        this.id = language.getMimeType().replace('/', '_') + '_';
        
        // XXX Start listening on the rules folder, to handle module set changes.
        initErrors();
        initHints();
        initSuggestions();
        initSelectionHints();
        initBuiltins();
    }

    @Override
    public boolean isEnabled(UserConfigurableRule rule) {
        return HintsSettings.isEnabled(this, rule);
    }

    // The logger
    private static final Logger LOG = Logger.getLogger(GsfHintsManager.class.getName()); // NOI18N

    // Extensions of files
    private static final String INSTANCE_EXT = ".instance";

    // Non GUI attribute for NON GUI rules
    private static final String NON_GUI = "nonGUI"; // NOI18N
    
    private static final String RULES_FOLDER = "csl-hints/";  // NOI18N
    private static final String ERRORS = "/errors"; // NOI18N
    private static final String HINTS = "/hints"; // NOI18N
    private static final String SUGGESTIONS = "/suggestions"; // NOI18N
    private static final String SELECTION = "/selection"; // NOI18N

    // Maps of registered rules
    private Map<?,List<? extends ErrorRule>> errors = new HashMap<Object, List<? extends ErrorRule>>();
    private Map<?,List<? extends AstRule>> hints = new HashMap<Object,List<? extends AstRule>>();
    private Map<?,List<? extends AstRule>> suggestions = new HashMap<Object, List<? extends AstRule>>();
    private List<SelectionRule> selectionHints = new ArrayList<SelectionRule>();

    // Tree models for the settings GUI
    private TreeModel errorsTreeModel;
    private TreeModel hintsTreeModel;
    private TreeModel suggestionsTreeModel;
    
    private String mimeType;
    private HintsProvider provider;
    private String id;


    public Map<?,List<? extends ErrorRule>> getErrors() {
        return errors;
    }

    public Map<?,List<? extends AstRule>> getHints() {
        return hints;
    }

    public List<? extends SelectionRule> getSelectionHints() {
        return selectionHints;
    }

    public Map<?,List<? extends AstRule>> getHints(boolean onLine, RuleContext context) {
        Map<Object, List<? extends AstRule>> result = new HashMap<Object, List<? extends AstRule>>();
        
        for (Entry<?, List<? extends AstRule>> e : getHints().entrySet()) {
            List<AstRule> nueRules = new LinkedList<AstRule>();
            
            for (AstRule r : e.getValue()) {
                Preferences p = HintsSettings.getPreferences(this, r, null);
                
                if (p == null) {
                    if (!onLine) {
                        if (!r.appliesTo(context)) {
                            continue;
                        }
                        nueRules.add(r);
                    }
                    continue;
                }
                
                if (HintsSettings.getSeverity(this, r) == HintSeverity.CURRENT_LINE_WARNING) {
                    if (onLine) {
                        if (!r.appliesTo(context)) {
                            continue;
                        }
                        nueRules.add(r);
                    }
                } else {
                    if (!onLine) {
                        if (!r.appliesTo(context)) {
                            continue;
                        }
                        nueRules.add(r);
                    }
                }
            }
            
            if (!nueRules.isEmpty()) {
                result.put(e.getKey(), nueRules);
            }
        }
        
        return result;
    }
    
    public Map<?,List<? extends AstRule>> getSuggestions() {
        return suggestions;
    }

    TreeModel getErrorsTreeModel() {
        return errorsTreeModel;
    }

    public TreeModel getHintsTreeModel() {
        return hintsTreeModel;
    }

    public String getId() {
        return id;
    }

    TreeModel getSuggestionsTreeModel() {
        return suggestionsTreeModel;
    }

    // Private methods ---------------------------------------------------------

    private void initErrors() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        errorsTreeModel = new DefaultTreeModel( rootNode );
        FileObject folder = FileUtil.getConfigFile(RULES_FOLDER + mimeType + ERRORS);
        List<Pair<Rule,FileObject>> rules = readRules( folder );
        categorizeErrorRules(rules, errors, folder, rootNode);
    }
    
    private void initHints() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        hintsTreeModel = new DefaultTreeModel( rootNode );
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + mimeType + HINTS ); // NOI18N
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeAstRules( rules, hints, folder, rootNode );
    }


    private void initSuggestions() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        suggestionsTreeModel = new DefaultTreeModel( rootNode );
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + mimeType + SUGGESTIONS ); // NOI18N
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeAstRules(rules, suggestions, folder, rootNode);
    }

    private void initSelectionHints() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        suggestionsTreeModel = new DefaultTreeModel( rootNode );
        FileObject folder = FileUtil.getConfigFile( RULES_FOLDER + mimeType + SELECTION ); // NOI18N
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeSelectionRules(rules, selectionHints, folder, rootNode);
    }

    private void initBuiltins() {
        List<Rule> extraRules = provider.getBuiltinRules();
        if (extraRules != null) {
            Map errorMap = errors;
            List selectionList = selectionHints;
            Map hintsMap = hints;
            for (Rule rule : extraRules) {
                if (rule instanceof ErrorRule) {
                    ErrorRule errorRule = (ErrorRule)rule;
                    for (Object key : errorRule.getCodes()) {
                        List list = errors.get(key);
                        if (list == null) {
                            list = new ArrayList<ErrorRule>(2);
                            errorMap.put(key, list);
                        }
                        list.add(rule);
                    }
                } else if (rule instanceof SelectionRule) {
                    selectionList.add(rule);
                } else if (rule instanceof AstRule) {
                    AstRule astRule = (AstRule)rule;
                    for (Object key : astRule.getKinds()) {
                        List list = hints.get(key);
                        if (list == null) {
                            list = new ArrayList<AstRule>(2);
                            hintsMap.put(key, list);
                        }
                        list.add(rule);
                    }
                } else {
                    assert false : "Unexpected rule type " + rule;
                }
            }
        }
    }

    /** Read rules from system filesystem */
    private static List<Pair<Rule,FileObject>> readRules( FileObject folder ) {

        List<Pair<Rule,FileObject>> rules = new LinkedList<Pair<Rule,FileObject>>();
        
        if (folder == null) {
            return rules;
        }

        //HashMap<FileObject,DefaultMutableTreeNode> dir2node = new HashMap<FileObject,DefaultMutableTreeNode>();

        // XXX Probably not he best order
        Enumeration e = folder.getData( true );
        while( e.hasMoreElements() ) {
            FileObject o = (FileObject)e.nextElement();
            String name = o.getNameExt().toLowerCase();

            if ( o.canRead() ) {
                Rule r = null;
                if ( name.endsWith( INSTANCE_EXT ) ) {
                    r = instantiateRule(o);
                }
                if ( r != null ) {
                    rules.add( new Pair<Rule,FileObject>( r, o ) );
                }
            }
        }
        return rules;
    }

    private static void categorizeErrorRules(List<Pair<Rule,FileObject>> rules,
                                             Map<?,List<? extends ErrorRule>> dest,
                                             FileObject rootFolder,
                                             DefaultMutableTreeNode rootNode ) {

        Map<FileObject,DefaultMutableTreeNode> dir2node = new HashMap<FileObject, DefaultMutableTreeNode>();
        dir2node.put(rootFolder, rootNode);

        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();

            if ( rule instanceof ErrorRule ) {
                addRule( (ErrorRule)rule, (Map)dest );
                FileObject parent = fo.getParent();
                DefaultMutableTreeNode category = dir2node.get( parent );
                if ( category == null ) {
                    category = new DefaultMutableTreeNode( parent );
                    rootNode.add( category );
                    dir2node.put( parent, category );
                }
                category.add( new DefaultMutableTreeNode( rule, false ) );
            }
            else {
                LOG.log( Level.WARNING, "The rule defined in " + fo.getPath() + "is not instance of ErrorRule" );
            }
        }
    }

    private static void categorizeAstRules( List<Pair<Rule,FileObject>> rules,
                                             Map<?,List<? extends AstRule>> dest,
                                             FileObject rootFolder,
                                             DefaultMutableTreeNode rootNode ) {

        Map<FileObject,DefaultMutableTreeNode> dir2node = new HashMap<FileObject, DefaultMutableTreeNode>();
        dir2node.put(rootFolder, rootNode);

        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();

            if ( rule instanceof AstRule ) {
                
                Object nonGuiObject = fo.getAttribute(NON_GUI);
                boolean toGui = true;
                
                if ( nonGuiObject != null && 
                     nonGuiObject instanceof Boolean &&
                     ((Boolean)nonGuiObject).booleanValue() ) {
                    toGui = false;
                }
                
                addRule( (AstRule)rule, (Map)dest );
                FileObject parent = fo.getParent();
                DefaultMutableTreeNode category = dir2node.get( parent );
                if ( category == null ) {
                    category = new DefaultMutableTreeNode( parent );
                    rootNode.add( category );
                    dir2node.put( parent, category );
                }
                if ( toGui ) {
                    category.add( new DefaultMutableTreeNode( rule, false ) );
                }
            }
            else {
                LOG.log( Level.WARNING, "The rule defined in " + fo.getPath() + "is not instance of AstRule" );
            }

        }
    }

    private static void categorizeSelectionRules(List<Pair<Rule,FileObject>> rules,
                                             List<? extends SelectionRule> dest,
                                             FileObject rootFolder,
                                             DefaultMutableTreeNode rootNode ) {
        Map<FileObject,DefaultMutableTreeNode> dir2node = new HashMap<FileObject, DefaultMutableTreeNode>();
        dir2node.put(rootFolder, rootNode);

        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();

            if ( rule instanceof SelectionRule ) {
                addRule((SelectionRule)rule, (List)dest );
                FileObject parent = fo.getParent();
                DefaultMutableTreeNode category = dir2node.get( parent );
                if ( category == null ) {
                    category = new DefaultMutableTreeNode( parent );
                    rootNode.add( category );
                    dir2node.put( parent, category );
                }
                category.add( new DefaultMutableTreeNode( rule, false ) );
            }
            else {
                LOG.log( Level.WARNING, "The rule defined in " + fo.getPath() + "is not instance of SelectionRule" );
            }
        }
    }
    
    private static void addRule( AstRule rule, Map<? super Object,List<AstRule>> dest ) {

        for(Object kind : rule.getKinds() ) {
            List<AstRule> l = dest.get( kind );
            if ( l == null ) {
                l = new LinkedList<AstRule>();
                dest.put( kind, l );
            }
            l.add( rule );
        }
    }

    @SuppressWarnings("unchecked")
    private static void addRule( ErrorRule rule, Map<? super Object,List<ErrorRule>> dest ) {

        for(Object code : (Set<Object>) rule.getCodes()) {
            List<ErrorRule> l = dest.get( code );
            if ( l == null ) {
                l = new LinkedList<ErrorRule>();
                dest.put( code, l );
            }
            l.add( rule );
        }
    }

    @SuppressWarnings("unchecked")
    private static void addRule(SelectionRule rule, List<? super SelectionRule> dest ) {
        dest.add(rule);
    }
    
    private static Rule instantiateRule( FileObject fileObject ) {
        try {
            DataObject dobj = DataObject.find(fileObject);
            InstanceCookie ic = dobj.getCookie( InstanceCookie.class );
            Object instance = ic.instanceCreate();
            
            if (instance instanceof Rule) {
                return (Rule) instance;
            } else {
                return null;
            }
        } catch( IOException e ) {
            LOG.log(Level.INFO, null, e);
        } catch ( ClassNotFoundException e ) {
            LOG.log(Level.INFO, null, e);
        }

        return null;
    }
    
    public final ErrorDescription createDescription(Hint desc, RuleContext context, boolean allowDisableEmpty, boolean last) {
        Rule rule = desc.getRule();
        HintSeverity severity;
        if (rule instanceof UserConfigurableRule) {
            severity = HintsSettings.getSeverity(this, (UserConfigurableRule)rule);
        } else {
            severity = rule.getDefaultSeverity();
        }
        OffsetRange range = desc.getRange();
        List<org.netbeans.spi.editor.hints.Fix> fixList;
        ParserResult info = context.parserResult;
        
        if (desc.getFixes() != null && desc.getFixes().size() > 0) {
            fixList = new ArrayList<org.netbeans.spi.editor.hints.Fix>(desc.getFixes().size());
            
            // TODO print out priority with left flushed 0's here
            // this is just a hack
            String sortText = Integer.toString(10000+desc.getPriority());
            
            for (org.netbeans.modules.csl.api.HintFix fix : desc.getFixes()) {
                fixList.add(new FixWrapper(fix, sortText));
                
                if (fix instanceof PreviewableFix) {
                    PreviewableFix previewFix = (PreviewableFix)fix;
                    if (previewFix.canPreview() && !isTest) {
                        fixList.add(new PreviewHintFix(info, previewFix, sortText));
                    }
                }
            }
            
            if (last && rule instanceof UserConfigurableRule && !isTest) {
                // Add a hint for opening options dialog
                fixList.add(new DisableHintFix(this, context));
            }
        } else if (last && allowDisableEmpty && rule instanceof UserConfigurableRule && !isTest) {
            // Add a hint for openening options dialog
            fixList = Collections.<org.netbeans.spi.editor.hints.Fix>singletonList(new DisableHintFix(this, context));
        } else {
            fixList = Collections.emptyList();
        }
        return ErrorDescriptionFactory.createErrorDescription(
                severity.toEditorSeverity(), 
                desc.getDescription(), fixList, desc.getFile(), range.getStart(), range.getEnd());
    }
    
    public final void refreshHints(RuleContext context) {
        List<ErrorDescription> result = getHints(this, context);
        HintsController.setErrors(context.parserResult.getSnapshot().getSource().getFileObject(),
                context.caretOffset == -1 ? HintsTask.class.getName() : SuggestionsTask.class.getName(), result);
    }
    
    private static final List<ErrorDescription> getHints(GsfHintsManager hintsManager, RuleContext context) {
        int caretPos = context.caretOffset;
        HintsProvider provider = hintsManager.provider;

        // Force a refresh
        // HACK ALERT!
        List<Hint> descriptions = new ArrayList<Hint>();
        if (caretPos == -1) {
            provider.computeHints(hintsManager, context, descriptions);
            List<ErrorDescription> result = new ArrayList<ErrorDescription>(descriptions.size());
            for (int i = 0; i < descriptions.size(); i++) {
                Hint desc = descriptions.get(i);
                boolean allowDisable = true;
                ErrorDescription errorDesc = hintsManager.createDescription(desc, context, allowDisable, i == descriptions.size()-1);
                result.add(errorDesc);
            }

            return result;
        } else {
            provider.computeSuggestions(hintsManager, context, descriptions, caretPos);
            List<ErrorDescription> result = new ArrayList<ErrorDescription>(descriptions.size());
            for (int i = 0; i < descriptions.size(); i++) {
                Hint desc = descriptions.get(i);
                boolean allowDisable = true;                
                ErrorDescription errorDesc = hintsManager.createDescription(desc, context, allowDisable, i == descriptions.size()-1);
                result.add(errorDesc);
            }

            return result;
        }
        // TODO - compute errors as well
    }


    static final void refreshHints(ResultIterator controller) {
        List<ErrorDescription> allHints = new ArrayList<ErrorDescription>();
        collectHints(controller, allHints);
        HintsController.setErrors(controller.getSnapshot().getSource().getFileObject(), HintsTask.class.getName(), allHints);
    }

    private static void collectHints(ResultIterator controller, List<ErrorDescription> allHints) {
        String mimeType = controller.getSnapshot().getMimeType();
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        if (language == null) {
            return;
        }

        HintsProvider provider = language.getHintsProvider();
        if (provider == null) {
            return;
        }

        ParserResult parserResult = null;
        try {
            Parser.Result pr = controller.getParserResult();
            if (pr instanceof ParserResult) {
                parserResult = (ParserResult) pr;
            }
        } catch (ParseException e) {
            LOG.log(Level.WARNING, null, e);
        }

        if (parserResult == null) {
            return;
        }
        
        GsfHintsManager hintsManager = language.getHintsManager();
        RuleContext context = hintsManager.createRuleContext(parserResult, language, -1, -1, -1);
        List<ErrorDescription> hints = getHints(hintsManager, context);
        allHints.addAll(hints);

        for(Embedding e : controller.getEmbeddings()) {
            collectHints(controller.getResultIterator(e), allHints);
        }
    }

    public RuleContext createRuleContext (ParserResult parserResult, Language language, int caretOffset, int selectionStart, int selectionEnd) {
        RuleContext context = provider.createRuleContext();
        context.manager = this;
        context.parserResult = parserResult;
        context.caretOffset = caretOffset;
        context.selectionStart = selectionStart;
        context.selectionEnd = selectionEnd;
        context.doc = (BaseDocument) parserResult.getSnapshot().getSource().getDocument(false);
        if (context.doc == null) {
            // Document closed
            return null;
        }
        
// XXX: parsingapi
//        Collection<? extends ParserResult> embeddedResults = info.getEmbeddedResults(language.getMimeType());
//        context.parserResults = embeddedResults != null ? embeddedResults : Collections.EMPTY_LIST;
//        if (context.parserResults.size() > 0) {
//            context.parserResult = embeddedResults.iterator().next();
//        }

        return context;
    }
    
    boolean isTest = false;
    
    private OptionsPanelController panelController;
    public synchronized OptionsPanelController getOptionsController() {
        if ( panelController == null ) {
            panelController = new HintsOptionsPanelController(this);
        }
        
        return panelController;
    }
    
    /** For testing purposes only! */
    public void setTestingRules(Map<?,List<? extends ErrorRule>> errors,
            Map<?,List<? extends AstRule>> hints,
            Map<?,List<? extends AstRule>> suggestions,
            List<SelectionRule> selectionHints) {
        this.errors = errors;
        this.hints = hints;
        this.suggestions = suggestions;
        this.selectionHints = selectionHints;
        
        isTest = true;
     }

    @Override
    public Preferences getPreferences(UserConfigurableRule rule) {
        return HintsSettings.getPreferences(this, rule, null);
    }
}

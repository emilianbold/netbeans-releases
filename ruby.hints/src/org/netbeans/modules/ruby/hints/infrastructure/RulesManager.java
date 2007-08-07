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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.hints.infrastructure;

import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.modules.ruby.hints.options.HintsSettings;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.netbeans.modules.ruby.hints.spi.ErrorRule;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Rule;
import org.openide.util.NbPreferences;

/** 
 * Manages rules read from the system filesystem.
 *
 * (Copied from java/hints)
 * 
 * @author Petr Hrebejk
 */
public class RulesManager {

    // The logger
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.ruby.hints"); // NOI18N

    // Extensions of files
    private static final String INSTANCE_EXT = ".instance";

    // Non GUI attribute for NON GUI rules
    private static final String NON_GUI = "nonGUI"; // NOI18N
    
    private static final String RULES_FOLDER = "org-netbeans-modules-ruby-hints/rules/";  // NOI18N
    private static final String ERRORS = "errors"; // NOI18N
    private static final String HINTS = "hints"; // NOI18N
    private static final String SUGGESTIONS = "suggestions"; // NOI18N

    // Maps of registered rules
    private static Map<String,List<ErrorRule>> errors = new HashMap<String, List<ErrorRule>>();
    private static Map<Integer,List<AstRule>> hints = new HashMap<Integer,List<AstRule>>();
    private static Map<Integer,List<AstRule>> suggestions = new HashMap<Integer, List<AstRule>>();

    // Tree models for the settings GUI
    private static TreeModel errorsTreeModel;
    private static TreeModel hintsTreeModel;
    private static TreeModel suggestionsTreeModel;

    private static RulesManager INSTANCE;

    private RulesManager() {
        // XXX Start listening on the rules forder. To handle module set changes.
        initErrors();
        initHints();
        initSuggestions();
    }

    public static synchronized RulesManager getInstance() {
        if ( INSTANCE == null ) {
            INSTANCE = new RulesManager();
        }
        return INSTANCE;
    }
    
//    public boolean isEnabled(Rule rule) {
//        return HintsSettings.isEnabled(rule, getPreferences(rule, HintsSettings.getCurrentProfileId()));        
//    }
    
    public HintSeverity getSeverity(Rule rule) {
        return HintsSettings.getSeverity(rule, getPreferences(rule, HintsSettings.getCurrentProfileId()));        
    }

    /** Gets preferences node, which stores the options for given hint. It is not
     * necessary to override this method unless you want to create some special
     * behavior. The default implementation will create the the preferences node
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @profile Profile to get the node for. May be null for current profile
     * @return Preferences node for given hint.
     */
    public Preferences getPreferences(Rule rule, String profile) { 
        profile = profile == null ? HintsSettings.getCurrentProfileId() : profile;
        return NbPreferences.forModule(this.getClass()).node(profile).node(rule.getId());
    }
    
    public Map<String,List<ErrorRule>> getErrors() {
        return errors;
    }

    public Map<Integer,List<AstRule>> getHints() {
        return hints;
    }

    public Map<Integer,List<AstRule>> getHints(boolean onLine, CompilationInfo info) {
        Map<Integer, List<AstRule>> result = new HashMap<Integer, List<AstRule>>();
        
        for (Entry<Integer, List<AstRule>> e : getHints().entrySet()) {
            List<AstRule> nueRules = new LinkedList<AstRule>();
            
            for (AstRule r : e.getValue()) {
                if (!r.appliesTo(info)) {
                    continue;
                }

                Preferences p = getPreferences(r, null);
                
                if (p == null) {
                    if (!onLine) {
                        nueRules.add(r);
                    }
                    continue;
                }
                
                if (getSeverity(r) == HintSeverity.CURRENT_LINE_WARNING) {
                    if (onLine) {
                        nueRules.add(r);
                    }
                } else {
                    if (!onLine) {
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
    
    public Map<Integer,List<AstRule>> getSuggestions() {
        return suggestions;
    }

    public TreeModel getErrorsTreeModel() {
        return errorsTreeModel;
    }

    public TreeModel getHintsTreeModel() {
        return hintsTreeModel;
    }

    public TreeModel getSuggestionsTreeModel() {
        return suggestionsTreeModel;
    }

    // Private methods ---------------------------------------------------------

    private static void initErrors() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        errorsTreeModel = new DefaultTreeModel( rootNode );
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject folder = fs.getRoot().getFileObject( RULES_FOLDER + ERRORS );
        List<Pair<Rule,FileObject>> rules = readRules( folder );
        categorizeErrorRules(rules, errors, folder, rootNode);
    }

    private static void initHints() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        hintsTreeModel = new DefaultTreeModel( rootNode );
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject folder = fs.getRoot().getFileObject( RULES_FOLDER + HINTS );
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeAstRules( rules, hints, folder, rootNode );
    }


    private static void initSuggestions() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        suggestionsTreeModel = new DefaultTreeModel( rootNode );
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject folder = fs.getRoot().getFileObject( RULES_FOLDER + SUGGESTIONS );
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeAstRules(rules, suggestions, folder, rootNode);
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

    private static void categorizeErrorRules( List<Pair<Rule,FileObject>> rules,
                                             Map<String,List<ErrorRule>> dest,
                                             FileObject rootFolder,
                                             DefaultMutableTreeNode rootNode ) {

        Map<FileObject,DefaultMutableTreeNode> dir2node = new HashMap<FileObject, DefaultMutableTreeNode>();
        dir2node.put(rootFolder, rootNode);

        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();

            if ( rule instanceof ErrorRule ) {
                addRule( (ErrorRule)rule, dest );
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
                                             Map<Integer,List<AstRule>> dest,
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
                
                addRule( (AstRule)rule, dest );
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

    private static void addRule( AstRule rule, Map<Integer,List<AstRule>> dest ) {

        for( Integer kind : rule.getKinds() ) {
            List<AstRule> l = dest.get( kind );
            if ( l == null ) {
                l = new LinkedList<AstRule>();
                dest.put( kind, l );
            }
            l.add( rule );
        }

    }

    @SuppressWarnings("unchecked")
    private static void addRule( ErrorRule rule, Map<String,List<ErrorRule>> dest ) {

        for(String code : (Set<String>) rule.getCodes()) {
            List<ErrorRule> l = dest.get( code );
            if ( l == null ) {
                l = new LinkedList<ErrorRule>();
                dest.put( code, l );
            }
            l.add( rule );
        }

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
}

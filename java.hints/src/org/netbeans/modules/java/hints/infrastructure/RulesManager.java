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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import com.sun.source.tree.Tree;
import java.io.IOException;
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
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.Rule;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/** Manages rules read from the system filesystem.
 *
 * @author Petr Hrebejk
 */
public class RulesManager {

    // The logger
    public static Logger LOG = Logger.getLogger("org.netbeans.modules.java.hints"); // NOI18N

    // Extensions of files
    private static final String INSTANCE_EXT = ".instance";

    // Non GUI attribute for NON GUI rules
    private static final String NON_GUI = "nonGUI"; // NOI18N
    
    private static final String RULES_FOLDER = "org-netbeans-modules-java-hints/rules/";  // NOI18N
    private static final String ERRORS = "errors"; // NOI18N
    private static final String HINTS = "hints"; // NOI18N
    private static final String SUGGESTIONS = "suggestions"; // NOI18N

    // Maps of registered rules
    private static Map<String,List<ErrorRule>> errors = new HashMap<String, List<ErrorRule>>();
    private static Map<Tree.Kind,List<TreeRule>> hints = new HashMap<Tree.Kind,List<TreeRule>>();
    private static Map<Tree.Kind,List<TreeRule>> suggestions = new HashMap<Tree.Kind, List<TreeRule>>();

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

    public Map<String,List<ErrorRule>> getErrors() {
        return errors;
    }

    public Map<Tree.Kind,List<TreeRule>> getHints() {
        return hints;
    }

    public Map<Tree.Kind,List<TreeRule>> getHints(boolean onLine) {
        Map<Tree.Kind, List<TreeRule>> result = new HashMap<Tree.Kind, List<TreeRule>>();
        
        for (Entry<Tree.Kind, List<TreeRule>> e : getHints().entrySet()) {
            List<TreeRule> nueRules = new LinkedList<TreeRule>();
            
            for (TreeRule r : e.getValue()) {
                if (!(r instanceof AbstractHint)) {
                    if (!onLine)
                        nueRules.add(r);
                    continue;
                }
                
                AbstractHint ah = (AbstractHint) r;
                
                Preferences p = ah.getPreferences(null);
                
                if (p == null) {
                    if (!onLine)
                        nueRules.add(r);
                    continue;
                }
                
                if (ah.getSeverity() == AbstractHint.HintSeverity.CURRENT_LINE_WARNING) {
                    if (onLine)
                        nueRules.add(r);
                } else {
                    if (!onLine)
                        nueRules.add(r);
                }
            }
            
            if (!nueRules.isEmpty()) {
                result.put(e.getKey(), nueRules);
            }
        }
        
        return result;
    }
    
    public Map<Tree.Kind,List<TreeRule>> getSuggestions() {
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
        categorizeTreeRules( rules, hints, folder, rootNode );
    }


    private static void initSuggestions() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        suggestionsTreeModel = new DefaultTreeModel( rootNode );
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject folder = fs.getRoot().getFileObject( RULES_FOLDER + SUGGESTIONS );
        List<Pair<Rule,FileObject>> rules = readRules(folder);
        categorizeTreeRules(rules, suggestions, folder, rootNode);
    }

    /** Read rules from system filesystem */
    private static List<Pair<Rule,FileObject>> readRules( FileObject folder ) {

        List<Pair<Rule,FileObject>> rules = new LinkedList<Pair<Rule,FileObject>>();
        
        if (folder == null) {
            return rules;
        }

        HashMap<FileObject,DefaultMutableTreeNode> dir2node = new HashMap<FileObject,DefaultMutableTreeNode>();

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

    private static void categorizeTreeRules( List<Pair<Rule,FileObject>> rules,
                                             Map<Tree.Kind,List<TreeRule>> dest,
                                             FileObject rootFolder,
                                             DefaultMutableTreeNode rootNode ) {

        Map<FileObject,DefaultMutableTreeNode> dir2node = new HashMap<FileObject, DefaultMutableTreeNode>();
        dir2node.put(rootFolder, rootNode);

        for( Pair<Rule,FileObject> pair : rules ) {
            Rule rule = pair.getA();
            FileObject fo = pair.getB();

            if ( rule instanceof TreeRule ) {
                
                Object nonGuiObject = fo.getAttribute(NON_GUI);
                boolean toGui = true;
                
                if ( nonGuiObject != null && 
                     nonGuiObject instanceof Boolean &&
                     ((Boolean)nonGuiObject).booleanValue() ) {
                    toGui = false;
                }
                
                addRule( (TreeRule)rule, dest );
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
                LOG.log( Level.WARNING, "The rule defined in " + fo.getPath() + "is not instance of TreeRule" );
            }

        }
    }

    private static void addRule( TreeRule rule, Map<Tree.Kind,List<TreeRule>> dest ) {

        for( Tree.Kind kind : rule.getTreeKinds() ) {
            List<TreeRule> l = dest.get( kind );
            if ( l == null ) {
                l = new LinkedList<TreeRule>();
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


    // Unused code -------------------------------------------------------------

//    /** Rules to be run on elements */
//    private static Map<ElementKind,List<ElementRule>> elementRules = new HashMap<ElementKind,List<ElementRule>>();


//    private static void addRule( ElementRule rule ) {
//
//        for( ElementKind kind : rule.getElementKinds()) {
//            List<ElementRule> l = elementRules.get( kind );
//            if ( l == null ) {
//                l = new LinkedList<ElementRule>();
//                elementRules.put( kind, l );
//            }
//            l.add( rule );
//        }
//
//    }

//    private static class ElementWalker extends ElementScanner6<List<ErrorDescription>,CompilationInfo> {
//
//        private List<ErrorDescription> warnings = new LinkedList<ErrorDescription>();
//
//        @Override
//        public List<ErrorDescription> scan( Element element, CompilationInfo compilationInfo ) {
//
//            if ( element == null ) {
//                return warnings;
//            }
//
//            List<ElementRule> rules = elementRules.get( element.getKind() ); // Find list of rules associated with given kind
//            if ( rules != null ) {
//                for (ElementRule rule : rules) { // Run the rules for given node
//                    List<ErrorDescription> w = rule.run( compilationInfo, element, runNumber );
//                    if ( w != null ) {
//                        warnings.addAll( w );
//                    }
//                }
//            }
//
//            super.scan( element, compilationInfo );
//
//            return warnings;
//        }
//
//    }
//

//    /** Runs all rules registered to ElementKinds */
//    private static List<ErrorDescription> runElementRules() {
//        ElementScanner6<List<ErrorDescription>,CompilationInfo> v = new ElementWalker();
//        // XXX How to implement?
//        return Collections.<ErrorDescription>emptyList();
//    }


}

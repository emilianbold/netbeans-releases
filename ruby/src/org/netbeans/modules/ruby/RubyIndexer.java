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
package org.netbeans.modules.ruby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jruby.ast.CallNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.FCallNode;
import org.jruby.ast.ListNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.SClassNode;
import org.jruby.ast.SelfNode;
import org.jruby.ast.StrNode;
import org.jruby.ast.types.INameNode;
import org.jruby.util.ByteList;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.Indexer;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.StructureAnalyzer.AnalysisResult;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.ClassElement;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.ModuleElement;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;


/**
 * @todo Index global variables
 * @todo Think about searching for modules; I will now hit EVERY class that refers to it
 *   (as their parent) whereas I really only want to hit individual module entries, right?
 * @todo Do I index anything outside of module or classnodes? How do these get deleted?
 * @todo Index migration and model files specially to extract database information that
 *   I can use to build dynamic model object attributes
 * @todo Index require_gem separately?
 *
 * @author Tor Norbye
 */
public class RubyIndexer implements Indexer {
    //private static final boolean INDEX_UNDOCUMENTED = Boolean.getBoolean("ruby.index.undocumented");
    private static final boolean INDEX_UNDOCUMENTED = true;
    private static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");
    
    // Class/Module Document
    static final String FIELD_EXTENDS_NAME = "extends"; //NOI18N
    static final String FIELD_FQN_NAME = "fqn"; //NOI18N
    static final String FIELD_IN = "in"; //NOI18N
    static final String FIELD_FILENAME = "source"; // NOI18N
    static final String FIELD_CLASS_NAME = "class"; //NOI18N
    static final String FIELD_CASE_INSENSITIVE_CLASS_NAME = "class-ig"; //NOI18N
    static final String FIELD_REQUIRE = "require"; //NOI18N
    static final String FIELD_REQUIRES = "requires"; //NOI18N
    static final String FIELD_INCLUDES = "includes"; //NOI18N
    static final String FIELD_EXTEND_WITH = "extendWith"; //NOI18N

    /**
     * A method definition. This is all compressed into a single line to make
     * Lucene searching faster (I initially tried having a separate document
     * for each method with separate attributes for the methods).
     * <p>
     * <pre>
     * Format:  methodname+args+;+modifiers+;+blockargs+;+returntypes;hashnames
     * </pre>
     * Only methodname is mandatory, but all previous elements must be
     * present if a later element is specified.
     * <p>
     *
     * The methodname can be any valid Ruby method name - including operator
     *    names like those identified by {@link RubyUtils#isOperator}. The
     *    method name is mandatory.
     *  optional:  (arg1,arg2,arg3="def",arg4=null,&block)
     * <p>
     *
     * The args should be of the format (arg1,arg2,...argn,optional1=val1,..,&blockarg)
     * In particular, the parens should be there, and elements should be separated by
     * comma.
     * <p>
     *
     * The method modifiers is two characters in hex which reflects the bit map
     * managed by IndexedElement. It records the following attributes (roughly)
     *    -> private
     *    -> protected
     *    -> static/classvar
     *    -> documented
     *    -> top level (implicit Object) ,
     *    -> database column
     *    -> This method takes a block
     *    -> This method MAY take a block (optional)
     * <p>
     * The block portion lists the block names to be used if this method
     * takes a block. This may be empty if the block names
     * are unknown or if the block doesn't take any parameters.
     * <p>
     * The returntypes should be a comma separated list of types this method
     * is known to return. 
     * <p>
     * The hashnames portion lists the possible values for each of the
     * parameters in the earlier signature. Each argument is listed, along
     * with a set of possible hashkeys allowed for that parameter. Optionally,
     * each hashkey is augmented with a "type" which indicates what kinds of
     * values are expected for the key.
     * The format of this list is as follows:
     * <pre>
     *   argname(key1|key2:keytype|key3...)
     * </pre>
     * Here, the :keytype part is optional, and keytype can be one of a number
     * of predefined types. If the first letter is uppercase, it denotes a
     * (possibly fully qualified) class name such as String. If it's
     * lowercase, some possibilities include "action", "controller"
     * (for Rails), "bool" (true or false), etc.
     * <p>
     * Examples:
     *   TODO
     */
    static final String FIELD_METHOD_NAME = "method"; //NOI18N

    /** Attributes: "i" -> private, "o" -> protected, ", "s" - static/notinstance, "d" - documented */
    static final String FIELD_FIELD_NAME = "field"; //NOI18N
    static final String FIELD_ATTRIBUTE_NAME = "attribute"; //NOI18N
    static final String FIELD_CONSTANT_NAME = "constant"; //NOI18N

    /** Attributes: hh;nnnn where hh is a hex representing flags in IndexedClass, and nnnn is the documentation length */
    static final String FIELD_CLASS_ATTRS = "attrs"; //NOI18N
                                                     // TODO: Add class info to tell whether methods are static

    static final String FIELD_DB_TABLE = "dbtable"; //NOI18N
    static final String FIELD_DB_VERSION = "dbversion"; //NOI18N
    static final String FIELD_DB_COLUMN = "dbcolumn"; //NOI18N
    /** Special version the schema.rb is marked with (rather than a migration number) */
    static final String SCHEMA_INDEX_VERSION = "schema"; // NOI18N
    
    // Method Document
    //static final String FIELD_PARAMS = "params"; //NOI18N
    //static final String FIELD_RDOC = "rdoc"; //NOI18N
    //
    /** Attributes: "i" -> private, "o" -> protected, ", "s" - static/notinstance, "D" - documented */
    //static final String FIELD_METHOD_ATTRS = "mattrs"; //NOI18N
    // TODO: Return types

    // No point doing case insensitive indexing of method names since in Ruby
    // they all tend to be fully lowercase anyway
    //static final String FIELD_CASE_INSENSITIVE_METHOD_NAME = "method-ig"; //NOI18N
    public RubyIndexer() {
    }

    public void updateIndex(Index index, ParserResult result)
        throws IOException {
        Node root = AstUtilities.getRoot(result);
        RubyParseResult r = (RubyParseResult)result;

        if (root == null) {
            return;
        }

        // I used to suppress indexing files that have had automatic cleanup to
        // remove in-process editing. However, that makes code completion not
        // work for local classes etc. that are being queried. I used to handle
        // that by doing local AST searches but this had a lot of problems
        // (not handling scoping and inheritance well etc.) so now I'm using the
        // index for everything.
        //  if (r.getSanitizedRange() != OffsetRange.NONE) {
        //     return;
        //  }

        TreeAnalyzer analyzer = new TreeAnalyzer(index, r);
            analyzer.analyze();
    }

    public boolean isIndexable(ParserFile file) {
        //return file.getExtension().equalsIgnoreCase("rb");
        return file.getNameExt().endsWith(".rb");
    }

    private static int getModifiersFlag(Set<Modifier> modifiers) {
        int flags = modifiers.contains(Modifier.STATIC) ? IndexedMethod.STATIC : 0;
        if (modifiers.contains(Modifier.PRIVATE)) {
            flags |= IndexedMethod.PRIVATE;
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            flags |= IndexedMethod.PROTECTED;
        }
        
        return flags;
    }

    private static class TreeAnalyzer {
        private final ParserFile file;
        private String url;
        private String requires;
        private final RubyParseResult result;
        private final BaseDocument doc;
        private final Index index;
        private int docMode;
        
        private TreeAnalyzer(Index index, RubyParseResult result) {
            this.index = index;
            this.result = result;
            this.file = result.getFile();

            FileObject fo = file.getFileObject();

            if (fo != null) {
                this.doc = AstUtilities.getBaseDocument(fo, true);
            } else {
                this.doc = null;
            }

            try {
                url = file.getFileObject().getURL().toExternalForm();

                // Make relative URLs for urls in the libraries
                url = RubyIndex.getPreindexUrl(url);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        private String getRequireString(Set<String> requireSet) {
            if ((requireSet != null) && (requireSet.size() > 0)) {
                StringBuilder sb = new StringBuilder(20 * requireSet.size());

                for (String s : requireSet) {
                    if (sb.length() > 0) {
                        sb.append(","); // NOI18N
                    }

                    sb.append(s);
                }

                return sb.toString();
            }

            return null;
        }

        // TODO - combine with getRequireString
        private String getIncludedString(Set<String> includes) {
            if ((includes != null) && (includes.size() > 0)) {
                StringBuilder sb = new StringBuilder(20 * includes.size());

                for (String include : includes) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }

                    sb.append(include);
                }

                return sb.toString();
            }

            return null;
        }

        public void analyze() throws IOException {
            // Delete old contents of this file - iff we're dealing with a user source file
            if (!file.isPlatform()) {
                Set<Map<String, String>> indexedList = Collections.emptySet();
                Set<Map<String, String>> notIndexedList = Collections.emptySet();
                Map<String, String> toDelete = new HashMap<String, String>();
                toDelete.put(FIELD_FILENAME, url);

                try {
                    index.gsfStore(indexedList, notIndexedList, toDelete);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
            
            String fileName = file.getNameExt();
            // DB migration?
            if (Character.isDigit(fileName.charAt(0)) && fileName.matches("^\\d\\d\\d_.*")) { // NOI18N
                FileObject fo = file.getFileObject();
                if (fo != null && fo.getParent() != null && fo.getParent().getName().equals("migrate")) { // NOI18N
                    handleMigration();
                    // Don't exit here - proceed to also index the class as Ruby code
                }
            } else if ("schema.rb".equals(fileName)) { //NOI18N
                FileObject fo = file.getFileObject();
                if (fo != null && fo.getParent() != null && fo.getParent().getName().equals("db")) { // NOI18N
                    handleMigration();
                    // Don't exit here - proceed to also index the class as Ruby code
                }
            }
            
            //Node root = result.getRootNode();

            // Compute the requires for this file first such that
            // each class or module recorded in the index for this
            // file can reference their includes
            AnalysisResult ar = result.getStructure();
            requires = getRequireString(ar.getRequires());
            List<?extends AstElement> structure = ar.getElements();

            // Rails special case
            if (fileName.startsWith("acti")) { // NOI18N
                if ("action_controller.rb".equals(fileName)) { // NOI18N
                    // Locate "ActionController::Base.class_eval do"
                    // and take those include statements and stick them into ActionController::Base
                    handleRailsBase("ActionController"); // NOI18N
                    return;
                } else if ("active_record.rb".equals(fileName)) { // NOI18N
                    handleRailsBase("ActiveRecord"); // NOI18N
                    // HACK
                    handleMigrations();
                    return;
                } else if ("action_mailer.rb".equals(fileName)) { // NOI18N
                    handleRailsBase("ActionMailer"); // NOI18N
                    return;
                } else if ("action_view.rb".equals(fileName)) { // NOI18N
                    handleRailsBase("ActionView"); // NOI18N
                    
                    // HACK
                    handleActionViewHelpers();
                    
                    return;
                //} else if ("action_web_service.rb".equals(fileName)) { // NOI18N
                    // Uh oh - we have two different kinds of class eval here - one for ActionWebService, one for ActionController!
                    // Gotta make this shiznit smarter!
                    //handleRailsBase("ActionWebService::Base", "Base", "ActionWebService"); // NOI18N
                    //handleRailsBase("ActionController:Base", "Base", "ActionController"); // NOI18N
                }
            }

            if ((structure == null) || (structure.size() == 0)) {
                if (requires != null) {
                    // It's just a requires-file... but we SHOULD index these such
                    // that they show up in require-completion (and more importantly,
                    // "master" files that require secondary files establish important
                    // relationships here for my transitive require statement closure
                    Set<Map<String, String>> indexedList = new HashSet<Map<String, String>>();
                    Set<Map<String, String>> notIndexedList = new HashSet<Map<String, String>>();

                    // Add indexed info
                    Map<String, String> indexed = new HashMap<String, String>();
                    indexedList.add(indexed);

                    Map<String, String> notIndexed = new HashMap<String, String>();
                    notIndexedList.add(notIndexed);

                    if (requires != null) {
                        notIndexed.put(FIELD_REQUIRES, requires);
                    }

                    addRequire(indexed);

                    // Indexed so we can locate these documents when deleting/updating
                    indexed.put(FIELD_FILENAME, url);

                    try {
                        Map<String, String> toDelete = Collections.emptyMap();
                        index.gsfStore(indexedList, notIndexedList, toDelete);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }

                return;
            }

            analyze(structure);
        }

        /** There's some pretty complicated dynamic behavior in Rails in how
         * the ActionController::Base class is decorated with module mixins;
         * my code cannot handle this directly, but it's a really important
         * special case to handle such that code completion works in the very
         * key controller classes edited by users. (This logic is replicated
         * in several other classes too - ActiveRecord etc.)
         */
        private void handleRailsBase(String classIn) {
            Node root = AstUtilities.getRoot(result);

            if (root == null) {
                return;
            }

            Set<String> includeSet = new HashSet<String>();
            Set<String> requireSet = new HashSet<String>();
            scan(root, includeSet, requireSet);

            Set<Map<String, String>> indexedList = new HashSet<Map<String, String>>();
            Set<Map<String, String>> notIndexedList = new HashSet<Map<String, String>>();

            // Add indexed info
            Map<String, String> indexed = new HashMap<String, String>();
            indexedList.add(indexed);

            Map<String, String> notIndexed = new HashMap<String, String>();
            notIndexedList.add(notIndexed);

            // TODO:
            //addIncluded(indexed);
            String r = getRequireString(requireSet);
            if (r != null) {
                notIndexed.put(FIELD_REQUIRES, r);
            }

            addRequire(indexed);

            String includes = getIncludedString(includeSet);

            if (includes != null) {
                notIndexed.put(FIELD_INCLUDES, includes);
            }

            int flags = 0;
            notIndexed.put(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags));

            indexed.put(FIELD_FQN_NAME, classIn + "::Base"); // NOI18N
            indexed.put(FIELD_CASE_INSENSITIVE_CLASS_NAME, "base"); // NOI18N
            indexed.put(FIELD_CLASS_NAME, "Base"); // NOI18N
            notIndexed.put(FIELD_IN, classIn);

            // Indexed so we can locate these documents when deleting/updating
            indexed.put(FIELD_FILENAME, url);

            try {
                Map<String, String> toDelete = Collections.emptyMap();
                index.gsfStore(indexedList, notIndexedList, toDelete);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        /** Add an entry for a class which provides the given includes */
        private void addClassIncludes(String className, String fqn, String in, int flags, String includes) {
            //Set<String> includeSet = new HashSet<String>();

            Set<Map<String, String>> indexedList = new HashSet<Map<String, String>>();
            Set<Map<String, String>> notIndexedList = new HashSet<Map<String, String>>();

            // Add indexed info
            Map<String, String> indexed = new HashMap<String, String>();
            indexedList.add(indexed);

            Map<String, String> notIndexed = new HashMap<String, String>();
            notIndexedList.add(notIndexed);

            if (includes != null) {
                notIndexed.put(FIELD_INCLUDES, includes);
            }

            notIndexed.put(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags));

            indexed.put(FIELD_FQN_NAME, fqn);
            indexed.put(FIELD_CASE_INSENSITIVE_CLASS_NAME, className.toLowerCase());
            indexed.put(FIELD_CLASS_NAME, className);
            if (in != null) {
                notIndexed.put(FIELD_IN, in);
            }

            // Indexed so we can locate these documents when deleting/updating
            indexed.put(FIELD_FILENAME, url);

            try {
                Map<String, String> toDelete = Collections.emptyMap();
                index.gsfStore(indexedList, notIndexedList, toDelete);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
        /** Add an include of ActiveRecord::ConnectionAdapters::SchemaStatements from ActiveRecord::Migration.
         * This is a hack. I'm not sure how the SchemaStatements methods end up in a Migration. I've sent
         * some querying e-mails to find out; if you, the reader, know - please let me know so I can track
         * down why the source modeller isn't finding this relationship. */
        private void handleMigrations() {
            int flags = 0;
            addClassIncludes("Migration", "ActiveRecord::Migration", "ActiveRecord", flags, 
                    "ActiveRecord::ConnectionAdapters::SchemaStatements");
        }

        /** Handle a migration file */
        private void handleMigration() {
            Node root = AstUtilities.getRoot(result);

            if (root == null) {
                return;
            }

            // Look for self.up methods and register all column deltas
            // create_table: create new table
            //  t.column: add column to the given table
            // add_column, remove_column: add column to the given table
            // rename_table, rename_column: renaming
            // rename_column, change_column - ditto for columns
            
            // It's going to be hard to deal with something like mephisto's 044_store_single_filter.rb
            // where it's doing conditional logic on the models... schema.rb is more useful here.
            // In the next release, try to use schema.rb if it's known to exist and be up to date
            
            // Find self.up
            String fileName = file.getFileObject().getName();
            String migrationClass = RubyUtils.underlinedNameToCamel(fileName.substring(4)); // Strip off version prefix
            Node top = null;
            String version;
            if ("schema".equals(fileName)) { // NOI18N
                top = root;
                // Use a special version greater than all allowed migrations such
                // that I can find this when querying
                version = SCHEMA_INDEX_VERSION; // NOI18N
            } else {
                version = fileName.substring(0,3);
                String sig = migrationClass + "#up"; // NOI18N
                Node def = AstUtilities.findBySignature(root, sig);

                if (def == null) {
                    return;
                }
                
                top = def;
            }
            
            // Iterate through the file and find tables and such
            // Map from table name to column names
            Map<String,List<String>> items = new HashMap<String,List<String>>();
            scanMigration(top, items, null);
            
            if (items.size() > 0) {
                for (Map.Entry<String,List<String>> entry : items.entrySet()) {
                    Set<Map<String, String>> indexedList = new HashSet<Map<String, String>>();
                    Set<Map<String, String>> notIndexedList = new HashSet<Map<String, String>>();
                    Map<String, String> indexed = new HashMap<String, String>();
                    indexedList.add(indexed);
                    Map<String, String> notIndexed = new HashMap<String, String>();
                    notIndexedList.add(notIndexed);

                    // Indexed so we can locate these documents when deleting/updating
                    indexed.put(FIELD_FILENAME, url);

                    String tableName = entry.getKey();
                    indexed.put(FIELD_DB_TABLE, tableName);
                    notIndexed.put(FIELD_DB_VERSION, version);
                    
                    List<String> columns = entry.getValue();
                    for (String column : columns) {
                        Map<String, String> ru = new HashMap<String, String>();
                        notIndexedList.add(ru);
                        ru.put(FIELD_DB_COLUMN, column);
                    }

                    try {
                        Map<String, String> toDelete = Collections.emptyMap();
                        index.gsfStore(indexedList, notIndexedList, toDelete);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }
        
        private void scanMigration(Node node, Map<String,List<String>> items, String currentTable) {
            if (node.nodeId == NodeTypes.FCALLNODE) {
                // create_table etc.
                String name = AstUtilities.getCallName(node);
                if ("create_table".equals(name)) { // NOI18N
                    // Compute the call name, and any column names
                    List childNodes = node.childNodes();
                    if (childNodes.size() > 0) {
                        Node child = (Node)childNodes.get(0);
                        if (child.nodeId == NodeTypes.ARRAYNODE) {
                            List grandChildren = child.childNodes();
                            if (grandChildren.size() > 0) {
                                Node grandChild = (Node)grandChildren.get(0);
                                if (grandChild.nodeId == NodeTypes.SYMBOLNODE || 
                                        grandChild.nodeId == NodeTypes.STRNODE) {
                                    String tableName = getString(grandChild);
                                    items.put(tableName, new ArrayList<String>());
                                    if (childNodes.size() > 1) {
                                        Node n = (Node) childNodes.get(1);
                                        if (n.nodeId == NodeTypes.ITERNODE) {
                                            scanMigration(n, items, tableName);
                                        }
                                    }
                                    
                                    return;
                                }
                            }
                        }
                    }
                } else if ("add_column".equals(name) || "remove_column".equals(name)) { // NOI18N
                    List<Node> symbols = new ArrayList<Node>();
                    // Ugh - this won't work right for complicated migrations which
                    // are for example iterating over table like Mephisto's store_single_filter
                    // migration
                    AstUtilities.addNodesByType(node, new int[] { NodeTypes.SYMBOLNODE, NodeTypes.STRNODE }, symbols);
                    if (symbols.size() >= 2) {
                        String tableName = getString(symbols.get(0));
                        String columnName = getString(symbols.get(1));
                        String columnType = null;
                        boolean isAdd = "add_column".equals(name); // NOI18N
                        if (isAdd && symbols.size() >= 3) {
                            columnType = getString(symbols.get(2));
                        }

                        List<String> list = items.get(tableName);
                        if (list == null) {
                            list = new ArrayList<String>();
                            items.put(tableName, list);
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(columnName);
                        sb.append(';');
                        if (isAdd) {
                            if (columnType != null) {
                                sb.append(columnType);
                            }
                        } else {
                            sb.append("-");
                        }
                        list.add(sb.toString());
                    }
                    
                    return;
                } else if ("rename_column".equals(name)) { // NOI18N
                    // Simulate as a delete old, add new
                    List<Node> symbols = new ArrayList<Node>();
                    // Ugh - this won't work right for complicated migrations which
                    // are for example iterating over table like Mephisto's store_single_filter
                    // migration
                    AstUtilities.addNodesByType(node, new int[] { NodeTypes.SYMBOLNODE, NodeTypes.STRNODE }, symbols);
                    if (symbols.size() >= 3) {
                        String tableName = getString(symbols.get(0));
                        String oldCol = getString(symbols.get(1));
                        String newCol = getString(symbols.get(2));

                        List<String> list = items.get(tableName);
                        if (list == null) {
                            list = new ArrayList<String>();
                            items.put(tableName, list);
                        }
                        list.add(oldCol + ";-"); // NOI18N
                        list.add(newCol);
                    }
                    
                    return;
                }
            } else if (node.nodeId == NodeTypes.CALLNODE && currentTable != null) {
                // t.column, applying to an outer table
                String name = AstUtilities.getCallName(node);
                if ("column".equals(name)) {  // NOI18N
                    List childNodes = node.childNodes();
                    if (childNodes.size() >= 2) {
                        Node child = (Node)childNodes.get(0);
                        if (child.nodeId != NodeTypes.DVARNODE) {
                            // Not a call on the block var corresponding to the table 
                            // Later, validate more closely that we're making a call
                            // on the actual block variable passed in from the create_table call!
                            return;
                        }

                        child = (Node)childNodes.get(1);
                        List<Node> symbols = new ArrayList<Node>();
                        AstUtilities.addNodesByType(child, new int[] { NodeTypes.SYMBOLNODE, NodeTypes.STRNODE }, symbols);
                        if (symbols.size() >= 2) {
                            String columnName = getString(symbols.get(0));
                            String columnType = getString(symbols.get(1));
                        
                            List<String> list = items.get(currentTable);
                            if (list == null) {
                                list = new ArrayList<String>();
                                items.put(currentTable, list);
                            }
                            StringBuilder sb = new StringBuilder();
                            sb.append(columnName);
                            sb.append(';');
                            sb.append(columnType);
                            list.add(sb.toString());
                        }

                    }

                    return;
                }
            }

            @SuppressWarnings("unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {
                scanMigration(child, items, currentTable);
            }            
        }

        private String getString(Node node) {
            if (node.nodeId == NodeTypes.STRNODE) {
                return ((StrNode)node).getValue().toString();
            } else {
                return ((INameNode)node).getName();
            }
        }
        /** 
         * Action view has some special loading behavior of helpers - see actionview's
         * "load_helpers" method.
         * @todo Make sure that the Partials loading is working too
         */
        private void handleActionViewHelpers() {
            FileObject fo = file.getFileObject();
            if (fo == null || fo.getParent() == null) {
                return;
            }
            assert fo.getName().equals("action_view");
            
            FileObject helpers = fo.getParent().getFileObject("action_view/helpers"); // NOI18N
            if (helpers == null) {
                return;
            }
            
            StringBuilder include = new StringBuilder();
            
            for (FileObject helper : helpers.getChildren()) {
                String name = helper.getName();
                if (name.endsWith("_helper")) { // NOI18N
                    String className = RubyUtils.underlinedNameToCamel(name);
                    String fqn = "ActionView::Helpers::" + className; // NOI18N
                    if (include.length() > 0) {
                        include.append(",");
                    }
                    include.append(fqn);
                }
            }
            
            if (include.length() > 0) {
                int flags = 0;
                addClassIncludes("Base", "ActionView::Base", "ActionView", flags, 
                        include.toString());
            }
        }
        
        private boolean scan(Node node, Set<String> includes, Set<String> requires) {
            boolean found = false;

            if (node instanceof FCallNode) {
                String name = ((INameNode)node).getName();

                if (name.equals("require")) { // XXX Load too?

                    Node argsNode = ((FCallNode)node).getArgsNode();

                    if (argsNode instanceof ListNode) {
                        ListNode args = (ListNode)argsNode;

                        if (args.size() > 0) {
                            Node n = args.get(0);

                            // For dynamically computed strings, we have n instanceof DStrNode
                            // but I can't handle these anyway
                        
                            if (n instanceof StrNode) {
                                ByteList require = ((StrNode)n).getValue();

                                if ((require != null) && (require.length() > 0)) {
                                    requires.add(require.toString());
                                }
                            }
                        }
                    }
                } else if ((includes != null) && name.equals("include")) {
                    Node argsNode = ((FCallNode)node).getArgsNode();

                    if (argsNode instanceof ListNode) {
                        ListNode args = (ListNode)argsNode;

                        if (args.size() > 0) {
                            Node n = args.get(0);

                            if (n instanceof Colon2Node) {
                                includes.add(AstUtilities.getFqn((Colon2Node)n));
                            } else if (n instanceof INameNode) {
                                includes.add(((INameNode)n).getName());
                            }
                        }
                    }
                }
            } else if (node instanceof CallNode) {
                // Look for ActionController::Base.class_eval do block to make
                // sure we have the right special case
                CallNode call = (CallNode)node;

                if (call.getName().equals("class_eval")) { // NOI18N
                    found = true;

                    // TODO Make sure the receivernode is ActionController::Base?
                }
            }

            @SuppressWarnings("unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (scan(child, includes, requires)) {
                    found = true;
                }
            }

            return found;
        }

        private void analyze(List<?extends AstElement> structure) {
            List<AstElement> topLevelMethods = null;

            for (Element o : structure) {
                // Todo: Iterate over the structure and index them
                // fields, classes, etc.
                AstElement element = (AstElement)o;
            
                switch (element.getKind()) {
                case MODULE:
                case CLASS:
                    analyzeClassOrModule(element);

                    break;

                case METHOD:
                    // Method defined outside of an explicit class: added to Object.
                    // I only track these in the user's own classes - and skip tests.
                    if (shouldIndexTopLevel()) {
                        if (topLevelMethods == null) {
                            topLevelMethods = new ArrayList<AstElement>();
                        }
                        topLevelMethods.add(element);
                    }
                    break;

                case CONSTRUCTOR:
                case FIELD:
                case ATTRIBUTE:
                case CONSTANT:

                    // Methods, fields, attributes or constants outside of an explicit
                    // class or module: Added to Object/Kernel

                    // TODO - index us!
                    break;
                }
            }
            
            if (topLevelMethods != null) {
                analyzeTopLevelMethods(topLevelMethods);
            }
        }
        
        private boolean shouldIndexTopLevel() {
            // Don't index top level methods in the libraries
            if (!file.isPlatform() && !PREINDEXING) {
                String name = file.getNameExt();
                // Don't index spec methods or test methods
                if (!name.endsWith("_spec.rb") && !name.endsWith("_test.rb")) {
                    // Don't index stuff in the vendor directory
                    if (url == null || url.indexOf("/vendor/") == -1) {
                        return true;
                    }
                }
            }

            return false;
        }

        private void analyzeClassOrModule(AstElement element) {
            int previousDocMode = docMode;
            try {
                int flags = 0;

                boolean nodoc = false;
                if (PREINDEXING) {
                    // Should we skip this class? This is true for :nodoc: marked
                    // classes for example. We do NOT want to skip all children;
                    // in ActiveRecord for example we have this:
                    //    module ActiveRecord
                    //      module ConnectionAdapters # :nodoc:
                    //        module SchemaStatements
                    // and we definitely WANT to index SchemaStatements even though
                    // ConnectionAdapters is not there
                    int newDocMode = RubyIndexerHelper.isNodocClass(element, file.getFileObject(), doc);
                    if (newDocMode == RubyIndexerHelper.DOC) {
                        docMode = RubyIndexerHelper.DEFAULT_DOC;
                    } else if (newDocMode == RubyIndexerHelper.NODOC_ALL) {
                        flags |= IndexedElement.NODOC;
                        nodoc = true;
                        docMode = RubyIndexerHelper.NODOC_ALL;
                    } else if (newDocMode == RubyIndexerHelper.NODOC || docMode == RubyIndexerHelper.NODOC_ALL) {
                        flags |= IndexedElement.NODOC;
                        nodoc = true;                    
                    }
                }


                // Add a document
                Set<Map<String, String>> indexedList = new HashSet<Map<String, String>>();
                Set<Map<String, String>> notIndexedList = new HashSet<Map<String, String>>();

                // Add indexed info
                Map<String, String> indexed = new HashMap<String, String>();
                indexedList.add(indexed);

                Map<String, String> notIndexed = new HashMap<String, String>();
                notIndexedList.add(notIndexed);

                String fqn;

                Node node = element.getNode();

                if (element.getKind() == ElementKind.CLASS) {
                    ClassElement classElement = (ClassElement)element;
                    fqn = classElement.getFqn();

                    if (node instanceof SClassNode) {
                        Node receiver = ((SClassNode)node).getReceiverNode();

                        if (receiver instanceof Colon2Node) {
                            fqn = AstUtilities.getFqn((Colon2Node)receiver);
                        } else if (receiver instanceof INameNode) {
                            // TODO - do I need to prefix the old fqn here?
                            fqn = ((INameNode)receiver).getName();
                        } else {
                            // Some other weird class def, like  class << myvariable
                            // - I won't index those.
                            return;
                        }
                    } else {
                        ClassNode clz = (ClassNode)node;
                        Node superNode = clz.getSuperNode();
                        String superClass = null;

                        if (superNode != null) {
                            superClass = AstUtilities.getSuperclass(clz);
                        }

                        if (superClass != null) {
                            indexed.put(FIELD_EXTENDS_NAME, superClass);
                        }
                    }

                    String includes = getIncludedString(classElement.getIncludes());

                    if (includes != null) {
                        notIndexed.put(FIELD_INCLUDES, includes);
                    }
                } else {
                    assert element.getKind() == ElementKind.MODULE;

                    ModuleElement moduleElement = (ModuleElement)element;
                    fqn = moduleElement.getFqn();

                    String extendWith = moduleElement.getExtendWith();

                    if (extendWith != null) {
                        notIndexed.put(FIELD_EXTEND_WITH, extendWith);
                    }

                    flags |= IndexedClass.MODULE;
                }

                String name = element.getName();

                String in;
                int classIndex = fqn.lastIndexOf("::");

                if (classIndex != -1) {
                    in = fqn.substring(0, classIndex);
                } else {
                    in = null;
                }

                boolean isDocumented = isDocumented(node);
                int documentSize = getDocumentSize(node);
                if (documentSize > 0) {
                    flags |= IndexedElement.DOCUMENTED;
                }

                StringBuilder attributes = new StringBuilder();
                attributes.append(IndexedElement.flagToFirstChar(flags));
                attributes.append(IndexedElement.flagToSecondChar(flags));
                if (documentSize > 0) {
                    attributes.append(";");
                    attributes.append(Integer.toString(documentSize));
                }
                notIndexed.put(FIELD_CLASS_ATTRS, attributes.toString());

                /* Don't prune modules without documentation because
                 * this may be an existing module that we're defining
                 * new (documented) classes for*/
                if (file.isPlatform() && (element.getKind() == ElementKind.CLASS) &&
                        !INDEX_UNDOCUMENTED && !isDocumented) {
                    // XXX No, I might still want to recurse into the children -
                    // I may have classes with documentation in an undocumented
                    // module!!
                    return;
                }

                indexed.put(FIELD_FQN_NAME, fqn);
                indexed.put(FIELD_CASE_INSENSITIVE_CLASS_NAME, name.toLowerCase());
                indexed.put(FIELD_CLASS_NAME, name);

                if (in != null) {
                    notIndexed.put(FIELD_IN, in);
                }

                addRequire(indexed);

                // TODO:
                //addIncluded(indexed);
                if (requires != null) {
                    notIndexed.put(FIELD_REQUIRES, requires);
                }

                // Indexed so we can locate these documents when deleting/updating
                indexed.put(FIELD_FILENAME, url);

                // Add the fields, etc.. Recursively add the children classes or modules if any
                for (AstElement child : element.getChildren()) {
                    switch (child.getKind()) {
                    case CLASS:
                    case MODULE: {
                        if (child.getNode() instanceof SClassNode &&
                                ((SClassNode)child.getNode()).getReceiverNode() instanceof SelfNode) {
                            // This is a class << self entry; I want to attach all these methods
                            // to the current class.
                            for (AstElement grandChild : child.getChildren()) {
                                switch (grandChild.getKind()) {
                                case CONSTRUCTOR:
                                case METHOD: {
                                    indexMethod(grandChild, indexedList, notIndexedList, false, nodoc);

                                    break;
                                }

                                case CLASS:
                                case MODULE: {
                                    analyzeClassOrModule(grandChild);

                                    break;
                                }

                                case FIELD: {
                                    indexField(grandChild, indexedList, notIndexedList, nodoc);

                                    break;
                                }

                                case ATTRIBUTE: {
                                    indexAttribute(grandChild, indexedList, notIndexedList, nodoc);

                                    break;
                                }

                                case CONSTANT: {
                                    indexConstant(grandChild, indexedList, notIndexedList, nodoc);

                                    break;
                                }
                                }
                            }
                        } else {
                            analyzeClassOrModule(child);
                        }

                        break;
                    }

                    case CONSTRUCTOR:
                    case METHOD: {
                        indexMethod(child, indexedList, notIndexedList, false, nodoc);

                        break;
                    }

                    case FIELD: {
                        indexField(child, indexedList, notIndexedList, nodoc);

                        break;
                    }

                    case ATTRIBUTE: {
                        indexAttribute(child, indexedList, notIndexedList, nodoc);

                        break;
                    }

                    case CONSTANT: {
                        indexConstant(child, indexedList, notIndexedList, nodoc);

                        break;
                    }
                    }
                }

                Map<String, String> toDelete = Collections.emptyMap();
                index.gsfStore(indexedList, notIndexedList, toDelete);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                docMode = previousDocMode;
            }
        }

        private void analyzeTopLevelMethods(List<? extends AstElement> children) {
            // Add a document
            Set<Map<String, String>> indexedList = new HashSet<Map<String, String>>();
            Set<Map<String, String>> notIndexedList = new HashSet<Map<String, String>>();

            // Add indexed info
            Map<String, String> indexed = new HashMap<String, String>();
            indexedList.add(indexed);

            Map<String, String> notIndexed = new HashMap<String, String>();
            notIndexedList.add(notIndexed);

            String name = "Object";
            String in = null;
            String fqn = "Object";

            int flags = 0;
            notIndexed.put(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags));
            indexed.put(FIELD_FQN_NAME, fqn);
            indexed.put(FIELD_CASE_INSENSITIVE_CLASS_NAME, name.toLowerCase());
            indexed.put(FIELD_CLASS_NAME, name);
            addRequire(indexed);
            if (requires != null) {
                notIndexed.put(FIELD_REQUIRES, requires);
            }

            // Indexed so we can locate these documents when deleting/updating
            indexed.put(FIELD_FILENAME, url);

            // TODO - find a way to combine all these methods (from this file) into a single item
            
            // Add the fields, etc.. Recursively add the children classes or modules if any
            for (AstElement child : children) {
                assert child.getKind() == ElementKind.CONSTRUCTOR || child.getKind() == ElementKind.METHOD;
                indexMethod(child, indexedList, notIndexedList, true, false);
                // XXX what about fields, constants, attributes?
            }

            try {
                Map<String, String> toDelete = Collections.emptyMap();
                index.gsfStore(indexedList, notIndexedList, toDelete);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        
        private void indexMethod(AstElement child, Set<Map<String, String>> indexedList,
            Set<Map<String, String>> notIndexedList, boolean topLevel, boolean nodoc) {
            Map<String, String> ru;
            ru = new HashMap<String, String>();

            MethodDefNode childNode = (MethodDefNode)child.getNode();
            String signature = AstUtilities.getDefSignature(childNode);
            Set<Modifier> modifiers = child.getModifiers();
            
            int flags = getModifiersFlag(modifiers);

            if (nodoc) {
                flags |= IndexedElement.NODOC;
            }

            if (topLevel) {
                flags |= IndexedElement.TOPLEVEL;
            }

            boolean methodIsDocumented = isDocumented(childNode);
            if (methodIsDocumented) {
                flags |= IndexedElement.DOCUMENTED;
            }

            if (flags != 0) {
                StringBuilder sb = new StringBuilder(signature);
                sb.append(';');
                sb.append(IndexedElement.flagToFirstChar(flags));
                sb.append(IndexedElement.flagToSecondChar(flags));
                signature = sb.toString();
            }
            
            if (PREINDEXING) {
                Node root = AstUtilities.getRoot(result);
                signature = RubyIndexerHelper.getMethodSignature(child, root, indexedList, notIndexedList, flags, signature, file.getFileObject(), doc);
                if (signature == null) {
                    return;
                }
            }

            ru.put(FIELD_METHOD_NAME, signature);

            // Storing a lowercase method name is kinda pointless in
            // Ruby because the convention is to use all lowercase characters
            // (using _ to separate words rather than camel case) so we're
            // bloating the database for very little practical use here...
            //ru.put(FIELD_CASE_INSENSITIVE_METHOD_NAME, name.toLowerCase());
            if (child.getName().equals("initialize")) {
                // Create static method alias "new"; rdoc also seems to do this
                Map<String, String> ru2;
                ru2 = new HashMap<String, String>();
                indexedList.add(ru2);

                // Change signature
                // TODO - don't do this for methods annotated :notnew: 
                signature = signature.replaceFirst("initialize", "new"); // NOI18N
                                                                         // Make it static

                if ((flags & IndexedElement.STATIC) == 0) {
                    // Add in static flag
                    flags |= IndexedElement.STATIC;
                    char first = IndexedElement.flagToFirstChar(flags);
                    char second = IndexedElement.flagToSecondChar(flags);
                    int attributeIndex = signature.indexOf(';');
                    if (attributeIndex == -1) {
                        signature = ((signature+ ";") + first) + second;
                    } else {
                        signature = (signature.substring(0, attributeIndex+1) + first) + second + signature.substring(attributeIndex+3);
                    }
                }
                ru2.put(FIELD_METHOD_NAME, signature);
            }

            indexedList.add(ru);
        }

        private void indexAttribute(AstElement child, Set<Map<String, String>> indexedList,
            Set<Map<String, String>> notIndexedList, boolean nodoc) {
            Map<String, String> ru;
            ru = new HashMap<String, String>();
            indexedList.add(ru);

            
            String attribute = child.getName();

            boolean isDocumented = isDocumented(child.getNode());

            int flags = isDocumented ? IndexedMethod.DOCUMENTED : 0;
            if (nodoc) {
                flags |= IndexedElement.NODOC;
            }

            char first = IndexedElement.flagToFirstChar(flags);
            char second = IndexedElement.flagToSecondChar(flags);
            
            if (isDocumented) {
                attribute = attribute + (";" + first) + second;
            }
            
            ru.put(FIELD_ATTRIBUTE_NAME, attribute);
        }

        private void indexConstant(AstElement child, Set<Map<String, String>> indexedList,
            Set<Map<String, String>> notIndexedList, boolean nodoc) {
            Map<String, String> ru;
            ru = new HashMap<String, String>();
            indexedList.add(ru);

            int flags = 0; // TODO
            if (nodoc) {
                flags |= IndexedElement.NODOC;
            }

            // TODO - add the RHS on the right
            ru.put(FIELD_CONSTANT_NAME, child.getName());
        }

        private void indexField(AstElement child, Set<Map<String, String>> indexedList,
            Set<Map<String, String>> notIndexedList, boolean nodoc) {
            Map<String, String> ru;
            ru = new HashMap<String, String>();
            indexedList.add(ru);

            String signature = child.getName();
            int flags = getModifiersFlag(child.getModifiers());
            if (nodoc) {
                flags |= IndexedElement.NODOC;
            }

            if (flags != 0) {
                StringBuilder sb = new StringBuilder(signature);
                sb.append(';');
                sb.append(IndexedElement.flagToFirstChar(flags));
                sb.append(IndexedElement.flagToSecondChar(flags));
                signature = sb.toString();
            }

            // TODO - gather documentation on fields? naeh
            ru.put(FIELD_FIELD_NAME, signature);
        }

        private int getDocumentSize(Node node) {
            if (doc != null) {
                List<String> comments = AstUtilities.gatherDocumentation(null, doc, node);

                if ((comments != null) && (comments.size() > 0)) {
                    int size = 0;

                    for (String line : comments) {
                        size += line.length();
                    }

                    return size;
                }
            }

            return 0;
        }

        private boolean isDocumented(Node node) {
            if (doc != null) {
                List<String> comments = AstUtilities.gatherDocumentation(null, doc, node);

                if ((comments != null) && (comments.size() > 0)) {
                    return true;
                }
            }

            return false;
        }

        private void addRequire(Map<String, String> ru) {
            // Don't generate "require" clauses for anything in generated ruby;
            // these classes are all built in and do not require any includes
            // (besides, the file names are bogus - they are just derived from
            // the class name by the stub generator)
            FileObject fo = file.getFileObject();
            String folder = (fo.getParent() != null) && fo.getParent().getParent() != null ?
                fo.getParent().getParent().getNameExt() : "";

            if (folder.equals("rubystubs") && fo.getName().startsWith("stub_")) {
                return;
            }

            // Index for require-completion
            String relative = file.getRelativePath();

            if (relative != null) {
                if (relative.endsWith(".rb")) { // NOI18N
                    relative = relative.substring(0, relative.length() - 3);
                    ru.put(FIELD_REQUIRE, relative);
                }
            }
        }
    }
}

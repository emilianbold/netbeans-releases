/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.ClassNode;
import org.jrubyparser.ast.Colon2Node;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.SClassNode;
import org.jrubyparser.ast.SelfNode;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.INameNode;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.ruby.RubyStructureAnalyzer.AnalysisResult;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.ClassElement;
import org.netbeans.modules.ruby.elements.Element;
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
 * @todo Remove the FILENAME url since it's maintained elsewhere (in infrastructure) now!
 *
 * @author Tor Norbye
 */
public class RubyIndexer extends EmbeddingIndexer {

    private static final Logger LOG = Logger.getLogger(RubyIndexer.class.getName());
    
    //private static final boolean INDEX_UNDOCUMENTED = Boolean.getBoolean("ruby.index.undocumented");
    private static final boolean INDEX_UNDOCUMENTED = true;

    /**
     * For unit tests, makes the indexer behave as when operating with 
     * user's sources in a project.
     */
    static boolean userSourcesTest = false;
    
    // Class/Module Document
    static final String FIELD_EXTENDS_NAME = "extends"; //NOI18N
    static final String FIELD_FQN_NAME = "fqn"; //NOI18N
    static final String FIELD_IN = "in"; //NOI18N
    static final String FIELD_CLASS_NAME = "class"; //NOI18N
    static final String FIELD_CASE_INSENSITIVE_CLASS_NAME = "class-ig"; //NOI18N
    static final String FIELD_REQUIRE = "require"; //NOI18N
    static final String FIELD_REQUIRES = "requires"; //NOI18N
    static final String FIELD_INCLUDES = "includes"; //NOI18N
    static final String FIELD_EXTEND_WITH = "extendWith"; //NOI18N

    // Rails 2.0 shorthand migrations; see http://dev.rubyonrails.org/changeset/6667?new_path=trunk
    final private static String[] FIXED_COLUMN_TYPES = new String[] {  
        // MUST BE SORTED - this array is binary searched!
        "binary", "boolean", "date", "datetime",  "decimal", "float", "integer", // NOI18N
        "string", "text", "time", "timestamp" }; // NOI18N
    
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
    static final String FIELD_GLOBAL_NAME = "global"; //NOI18N
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

//    public String getPersistentUrl(File file) {
//        String url;
//        try {
//            url = file.toURI().toURL().toExternalForm();
//            // Make relative URLs for urls in the libraries
//            return RubyIndex.getPreindexUrl(url);
//        } catch (MalformedURLException ex) {
//            Exceptions.printStackTrace(ex);
//            return file.getPath();
//        }
//
//    }

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        Node root = AstUtilities.getRoot(parserResult);
        RubyParseResult r = AstUtilities.getParseResult(parserResult);

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

        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return;
        }

        TreeAnalyzer analyzer =
                new TreeAnalyzer(r, support, indexable, new ContextKnowledge(null, root, r));
        analyzer.analyze();

        for (IndexDocument doc : analyzer.getDocuments()) {
            support.addDocument(doc);
        }
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

    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "ruby"; // NOI18N
        public static final int VERSION = 9;
        
        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(indexable, snapshot)) {
                return new RubyIndexer();
            } else {
                return null;
            }
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        private boolean isIndexable(Indexable indexable, Snapshot snapshot) {
            String extension = snapshot.getSource().getFileObject().getExt();
            if (extension.equals("rb")) { // NOI18N
                return true;
            }
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport support = IndexingSupport.getInstance(context);
                for (Indexable indexable : deleted) {
                    support.removeDocuments(indexable);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {

        }
        
        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }
    }

    private static class TreeAnalyzer {

        private final FileObject file;
        private final IndexingSupport support;
        private final Indexable indexable;
        private String requires;
        private final RubyParseResult result;
        private int docMode;
        private final List<IndexDocument> documents;
        private String url;
        private final boolean platform;
        private final ContextKnowledge knowledge;

        private TreeAnalyzer(RubyParseResult result,
                IndexingSupport support,
                Indexable indexable,
                ContextKnowledge knowledge) {

            this.result = result;
            this.file = RubyUtils.getFileObject(result);
            this.support = support;
            this.indexable = indexable;
            this.documents = new ArrayList<IndexDocument>();
            this.platform = RubyUtils.isPlatformFile(file);
            this.knowledge = knowledge;
        }

        private String getRequireString(Set<String> requireSet) {
            return asCommaSeparatedString(requireSet);
        }

        private String getIncludedString(Set<String> includes) {
            return asCommaSeparatedString(includes);
        }

        private String asCommaSeparatedString(Set<String> strings) {
            if (strings != null && strings.size() > 0) {
                StringBuilder sb = new StringBuilder(20 * strings.size());

                for (String each : strings) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }

                    sb.append(each);
                }

                return sb.toString();
            }

            return null;
        }
        
        List<IndexDocument> getDocuments() {
            return documents;
        }

        public void analyze() {
            try {
                url = file.getURL().toExternalForm();

                // Make relative URLs for urls in the libraries
                url = RubyIndex.getPreindexUrl(url);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            String fileName = file.getNameExt();
            // DB migration?
            if (Character.isDigit(fileName.charAt(0)) &&
                    (fileName.matches("^\\d\\d\\d_.*") || fileName.matches("^\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d_.*"))) { // NOI18N
                if (file != null && file.getParent() != null && file.getParent().getName().equals("migrate")) { // NOI18N
                    handleMigration();
                    // Don't exit here - proceed to also index the class as Ruby code
                }
            } else if ("schema.rb".equals(fileName)) { //NOI18N
                if (file != null && file.getParent() != null && file.getParent().getName().equals("db")) { // NOI18N
                    handleMigration();
                    // Don't exit here - proceed to also index the class as Ruby code
                }
            }

            //Node root = file.getRootNode();

            // Compute the requires for this file first such that
            // each class or module recorded in the index for this
            // file can reference their includes
            AnalysisResult ar = result.getStructure();
            requires = getRequireString(ar.getRequires());
            List<?extends AstElement> structure = ar.getElements();

            // Rails special case
            // in case of 2.3.2 fall through to do normal indexing as well, these special cases
            // are needed for rails < 2.3.2, normal indexing handles 2.3.2 classes.
            // if rails is in vendor/rails, we can't tell the version from
            // the path, so playing safe and falling through to do also normal
            // indexing in that case
            boolean fallThrough = RubyUtils.isRails23OrHigher(file.getPath())
                    || file.getPath().contains("vendor/rails"); //NOI18N
            if (fileName.startsWith("acti")) { // NOI18N
                if ("action_controller.rb".equals(fileName)) { // NOI18N
                    // Locate "ActionController::Base.class_eval do"
                    // and take those include statements and stick them into ActionController::Base
                    handleRailsBase("ActionController"); // NOI18N
                    if (!fallThrough) {
                        return;
                    }
                } else if ("active_record.rb".equals(fileName)) { // NOI18N
                    handleRailsBase("ActiveRecord"); // NOI18N
//                    handleRailsClass("ActiveRecord", "ActiveRecord" + "::Migration", "Migration", "migration");
                    // HACK
                    handleMigrations();
                    if (!fallThrough) {
                        return;
                    }
                } else if ("action_mailer.rb".equals(fileName)) { // NOI18N
                    handleRailsBase("ActionMailer"); // NOI18N
                    if (!fallThrough) {
                        return;
                    }
                } else if ("action_view.rb".equals(fileName)) { // NOI18N
                    handleRailsBase("ActionView"); // NOI18N

                    // HACK
                    handleActionViewHelpers();
                    if (!fallThrough) {
                        return;
                    }

                //} else if ("action_web_service.rb".equals(fileName)) { // NOI18N
                    // Uh oh - we have two different kinds of class eval here - one for ActionWebService, one for ActionController!
                    // Gotta make this shiznit smarter!
                    //handleRailsBase("ActionWebService::Base", "Base", "ActionWebService"); // NOI18N
                    //handleRailsBase("ActionController:Base", "Base", "ActionController"); // NOI18N
                }
            } else if (fileName.equals("assertions.rb") && url.endsWith("lib/action_controller/assertions.rb")) { // NOI18N
                handleRailsClass("Test::Unit", "Test::Unit::TestCase", "TestCase", "TestCase"); // NOI18N
                if (!fallThrough) {
                    return;
                }
            } else if (fileName.equals("schema_definitions.rb")) {
                handleSchemaDefinitions();
                // Fall through - also do normal indexing on the file
            }

            if ((structure == null) || (structure.size() == 0)) {
                if (requires != null) {
                    IndexDocument document = support.createDocument(indexable);
                    documents.add(document);
                    if (requires != null) {
                        document.addPair(FIELD_REQUIRES, requires, false, true);
                    }

                    addRequire(document);
                }

                return;
            }

            analyze(structure);
        }

        private void handleSchemaDefinitions() {
            // Make sure we're in Rails 2.0...
            if (url.indexOf("activerecord-2") == -1) { // NOI18N
                return;
            }
            
            Node root = AstUtilities.getRoot(result);

            if (root == null) {
                return;
            }

            Set<String> includeSet = new HashSet<String>();
            Set<String> requireSet = new HashSet<String>();
            scan(root, includeSet, requireSet);

            IndexDocument document = support.createDocument(indexable);
            documents.add(document);

            // TODO:
            //addIncluded(indexed);
            String r = getRequireString(requireSet);
            if (r != null) {
                document.addPair(FIELD_REQUIRES, r, false, true);
            }

            addRequire(document);

            String includes = getIncludedString(includeSet);

            if (includes != null) {
                document.addPair(FIELD_INCLUDES, includes, false, true);
            }

            int flags = 0;
            document.addPair(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags), false, true);

            String clz = "TableDefinition";
            String classIn = "ActiveRecord::ConnectionAdapters";
            String classFqn = classIn + "::" + clz;
            String clzNoCase = clz.toLowerCase();
            
            document.addPair(FIELD_FQN_NAME, classFqn, true, true);
            document.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, clzNoCase, true, true);
            document.addPair(FIELD_CLASS_NAME, clz, true, true);
            document.addPair(FIELD_IN, classIn, false, true);

            // Insert methods:
            for (String type : new String[] { "string", "text", "integer", "float", "decimal", "datetime", "timestamp", "time", "date", "binary", "boolean" }) { // NOI18N
                Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC);

                int mflags = getModifiersFlag(modifiers);
                StringBuilder sb = new StringBuilder();
                sb.append(type);
                sb.append("(names,options);"); // NOI18N
                sb.append(IndexedElement.flagToFirstChar(mflags));
                sb.append(IndexedElement.flagToSecondChar(mflags));
                sb.append(";;;options(=>limit|default:nil|null:bool|precision|scale)"); // NOI18N

                String signature = sb.toString();

                document.addPair(FIELD_METHOD_NAME, signature, true, true);
            }
        }

        private void handleRailsBase(String classIn) {
            handleRailsClass(classIn, classIn + "::Base", "Base", "base"); // NOI18N
        }

        /** There's some pretty complicated dynamic behavior in Rails in how
         * the ActionController::Base class is decorated with module mixins;
         * my code cannot handle this directly, but it's a really important
         * special case to handle such that code completion works in the very
         * key controller classes edited by users. (This logic is replicated
         * in several other classes too - ActiveRecord etc.)
         */
        private void handleRailsClass(String classIn, String classFqn, String clz, String clzNoCase) {
            Node root = knowledge.getRoot();

            IndexDocument document = support.createDocument(indexable);
            documents.add(document);

            Set<String> includeSet = new HashSet<String>();
            Set<String> requireSet = new HashSet<String>();
            scan(root, includeSet, requireSet);

            // TODO:
            //addIncluded(indexed);
            String r = getRequireString(requireSet);
            if (r != null) {
                document.addPair(FIELD_REQUIRES, r, false, true);
            }

            addRequire(document);

            String includes = getIncludedString(includeSet);

            if (includes != null) {
                document.addPair(FIELD_INCLUDES, includes, false, true);
            }

            int flags = 0;
            document.addPair(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags), false, true);

            document.addPair(FIELD_FQN_NAME, classFqn, true, true);
            document.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, clzNoCase, true, true);
            document.addPair(FIELD_CLASS_NAME, clz, true, true);
            document.addPair(FIELD_IN, classIn, false, true);
        }

        /** Add an entry for a class which provides the given includes */
        private void addClassIncludes(String className, String fqn, String in, int flags, String includes) {
            IndexDocument document = support.createDocument(indexable);
            documents.add(document);

            if (includes != null) {
                document.addPair(FIELD_INCLUDES, includes, false, true);
            }

            document.addPair(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags), false, true);

            document.addPair(FIELD_FQN_NAME, fqn, true, true);
            document.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, className.toLowerCase(), true, true);
            document.addPair(FIELD_CLASS_NAME, className, true, true);
            if (in != null) {
                document.addPair(FIELD_IN, in, false, true);
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
            Node root = knowledge.getRoot();

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
            String fileName = file.getName();
            Node top = null;
            String version;
            if ("schema".equals(fileName)) { // NOI18N
                top = root;
                // Use a special version greater than all allowed migrations such
                // that I can find this when querying
                version = SCHEMA_INDEX_VERSION; // NOI18N
            } else {
                String migrationClass;
                if (fileName.charAt(3) == '_') {
                    version = fileName.substring(0,3);
                    migrationClass = RubyUtils.underlinedNameToCamel(fileName.substring(4)); // Strip off version prefix
                } else {
                    // Rails 2.1 stores it in UTC format
                    version = fileName.substring(0,14);
                    migrationClass = RubyUtils.underlinedNameToCamel(fileName.substring(15)); // Strip off version prefix
                }
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
                    IndexDocument document = support.createDocument(indexable);
                    documents.add(document);
                    
                    String tableName = entry.getKey();
                    document.addPair(FIELD_DB_TABLE, tableName, true, true);
                    document.addPair(FIELD_DB_VERSION, version, false, true);
                    
                    List<String> columns = entry.getValue();
                    for (String column : columns) {
                        document.addPair(FIELD_DB_COLUMN, column, false, true);
                    }
                }
            }
        }
        
        private void scanMigration(Node node, Map<String,List<String>> items, String currentTable) {
            if (node.getNodeType() == NodeType.FCALLNODE) {
                // create_table etc.
                String name = AstUtilities.getCallName(node);
                if ("create_table".equals(name)) { // NOI18N
                    // Compute the call name, and any column names
                    List childNodes = node.childNodes();
                    if (childNodes.size() > 0) {
                        Node child = (Node)childNodes.get(0);
                        if (child.getNodeType() == NodeType.ARRAYNODE) {
                            List grandChildren = child.childNodes();
                            if (grandChildren.size() > 0) {
                                Node grandChild = (Node)grandChildren.get(0);
                                if (grandChild.getNodeType() == NodeType.SYMBOLNODE || 
                                        grandChild.getNodeType() == NodeType.STRNODE) {
                                    String tableName = getString(grandChild);
                                    items.put(tableName, new ArrayList<String>());
                                    if (childNodes.size() > 1) {
                                        Node n = (Node) childNodes.get(1);
                                        if (n.getNodeType() == NodeType.ITERNODE) {
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
                    AstUtilities.addNodesByType(node, new NodeType[] { NodeType.SYMBOLNODE, NodeType.STRNODE }, symbols);
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
                    AstUtilities.addNodesByType(node, new NodeType[] { NodeType.SYMBOLNODE, NodeType.STRNODE }, symbols);
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
            } else if (node.getNodeType() == NodeType.CALLNODE && currentTable != null) {
                // t.column, applying to an outer table
                String name = AstUtilities.getCallName(node);
                if ("column".equals(name)) {  // NOI18N
                    List childNodes = node.childNodes();
                    if (childNodes.size() >= 2) {
                        Node child = (Node)childNodes.get(0);
                        if (child.getNodeType() != NodeType.DVARNODE) {
                            // Not a call on the block var corresponding to the table 
                            // Later, validate more closely that we're making a call
                            // on the actual block variable passed in from the create_table call!
                            return;
                        }

                        child = (Node)childNodes.get(1);
                        List<Node> symbols = new ArrayList<Node>();
                        AstUtilities.addNodesByType(child, new NodeType[] { NodeType.SYMBOLNODE, NodeType.STRNODE }, symbols);
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
                } else if ("timestamps".equals(name)) { // NOI18N
                    List childNodes = node.childNodes();
                    if (childNodes.size() >= 1) {
                        Node child = (Node)childNodes.get(0);
                        if (child.getNodeType() != NodeType.DVARNODE) {
                            // Not a call on the block var corresponding to the table 
                            // Later, validate more closely that we're making a call
                            // on the actual block variable passed in from the create_table call!
                            return;
                        }
                        
                        // Insert timestamp codes
                        //    column(:created_at, :datetime)
                        //    column(:updated_at, :datetime)
                        List<String> list = items.get(currentTable);
                        if (list == null) {
                            list = new ArrayList<String>();
                            items.put(currentTable, list);
                        }
                        list.add("created_at;datetime"); // NOI18N
                        list.add("updated_at;datetime"); // NOI18N
                    }
                 } else {
                    // Rails 2.0 shorthand migrations; see http://dev.rubyonrails.org/changeset/6667?new_path=trunk
                    int columnTypeIndex = Arrays.binarySearch(FIXED_COLUMN_TYPES, name);
                    if (columnTypeIndex >= 0 && columnTypeIndex < FIXED_COLUMN_TYPES.length) {
                        String columnType = FIXED_COLUMN_TYPES[columnTypeIndex];
                        List childNodes = node.childNodes();
                        if (childNodes.size() >= 2) {
                            Node child = (Node)childNodes.get(0);
                            if (child.getNodeType() != NodeType.DVARNODE) {
                                // Not a call on the block var corresponding to the table 
                                // Later, validate more closely that we're making a call
                                // on the actual block variable passed in from the create_table call!
                                return;
                            }

                            child = (Node)childNodes.get(1);
                            List<Node> args = child.childNodes();
                            for (Node n : args) {
                                if (n.getNodeType() == NodeType.SYMBOLNODE || n.getNodeType() == NodeType.STRNODE) {
                                    String columnName = getString(n);
                                    
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
                        }

                        return;
                    
                    }
                }
            }

            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.isInvisible()) {
                    continue;
                }
                scanMigration(child, items, currentTable);
            }            
        }

        private String getString(Node node) {
            if (node.getNodeType() == NodeType.STRNODE) {
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
            if (file == null || file.getParent() == null) {
                return;
            }
            assert file.getName().equals("action_view");
            
            FileObject helpers = file.getParent().getFileObject("action_view/helpers"); // NOI18N
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
                                String require = ((StrNode)n).getValue();

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

            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.isInvisible()) {
                    continue;
                }
                if (scan(child, includes, requires)) {
                    found = true;
                }
            }

            return found;
        }

        private void analyze(List<?extends AstElement> structure) {
            List<AstElement> topLevelMethods = null;
            IndexDocument globalDoc = null;

            for (Element o : structure) {
                // Todo: Iterate over the structure and index them
                // fields, classes, etc.
                AstElement element = (AstElement)o;
            
                switch (element.getKind()) {
                case MODULE:
                case CLASS:
                    IndexDocument _doc = analyzeClassOrModule(element);
                    if (globalDoc == null) {
                        globalDoc = _doc;
                    }

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
                    
                case GLOBAL: {
                    if (globalDoc == null) {
                        globalDoc = support.createDocument(indexable);
                        documents.add(globalDoc);
                    }

                    indexGlobal(element, globalDoc/*, nodoc*/);

                    break;
                }

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
            if (!platform || userSourcesTest) {
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

        private IndexDocument analyzeClassOrModule(AstElement element) {
            int previousDocMode = docMode;
            IndexDocument document = null;
            try {
                int flags = 0;

                boolean nodoc = false;
                if (platform) {
                    // Should we skip this class? This is true for :nodoc: marked
                    // classes for example. We do NOT want to skip all children;
                    // in ActiveRecord for example we have this:
                    //    module ActiveRecord
                    //      module ConnectionAdapters # :nodoc:
                    //        module SchemaStatements
                    // and we definitely WANT to index SchemaStatements even though
                    // ConnectionAdapters is not there
                    int newDocMode = RubyIndexerHelper.isNodocClass(element, result.getSnapshot());
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


                document = support.createDocument(indexable);

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
                            return document;
                        }
                    } else {
                        ClassNode clz = (ClassNode)node;
                        Node superNode = clz.getSuperNode();
                        String superClass = null;

                        if (superNode != null) {
                            superClass = AstUtilities.getSuperclass(clz);
                        }

                        if (superClass != null) {
                            document.addPair(FIELD_EXTENDS_NAME, superClass, true, true);
                        }
                    }

                    String includes = getIncludedString(classElement.getIncludes());

                    if (includes != null) {
                        document.addPair(FIELD_INCLUDES, includes, false, true);
                    }
                } else {
                    assert element.getKind() == ElementKind.MODULE;

                    ModuleElement moduleElement = (ModuleElement)element;
                    fqn = moduleElement.getFqn();

                    String extendWith = moduleElement.getExtendWith();

                    if (extendWith != null) {
                        document.addPair(FIELD_EXTEND_WITH, extendWith, false, true);
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
                document.addPair(FIELD_CLASS_ATTRS, attributes.toString(), false, true);

                /* Don't prune modules without documentation because
                 * this may be an existing module that we're defining
                 * new (documented) classes for*/
                if (/*file.isPlatform() && */(element.getKind() == ElementKind.CLASS) &&
                        !INDEX_UNDOCUMENTED && !isDocumented) {
                    // XXX No, I might still want to recurse into the children -
                    // I may have classes with documentation in an undocumented
                    // module!!
                    return document;
                }

                document.addPair(FIELD_FQN_NAME, fqn, true, true);
                document.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, name.toLowerCase(), true, true);
                document.addPair(FIELD_CLASS_NAME, name, true, true);

                if (in != null) {
                    document.addPair(FIELD_IN, in, false, true);
                }

                addRequire(document);

                // TODO:
                //addIncluded(indexed);
                if (requires != null) {
                    document.addPair(FIELD_REQUIRES, requires, false, true);
                }

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
                                    indexMethod(grandChild, document, false, nodoc);

                                    break;
                                }

                                case CLASS:
                                case MODULE: {
                                    analyzeClassOrModule(grandChild);

                                    break;
                                }

                                case FIELD: {
                                    indexField(grandChild, document, nodoc);

                                    break;
                                }

                                case GLOBAL: {
                                    indexGlobal(grandChild, document/*, nodoc*/);

                                    break;
                                }

                                case ATTRIBUTE: {
                                    indexAttribute(grandChild, document, nodoc);

                                    break;
                                }

                                case CONSTANT: {
                                    indexConstant(grandChild, document, nodoc);

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
                        indexMethod(child, document, false, nodoc);

                        break;
                    }

                    case FIELD: {
                        indexField(child, document, nodoc);

                        break;
                    }

                    case GLOBAL: {
                        indexGlobal(child, document/*, nodoc*/);

                        break;
                    }
                    
                    case ATTRIBUTE: {
                        indexAttribute(child, document, nodoc);

                        break;
                    }

                    case CONSTANT: {
                        indexConstant(child, document, nodoc);

                        break;
                    }
                    }
                }

                documents.add(document);
            } finally {
                docMode = previousDocMode;
            }
            
            return document;
        }

        private void analyzeTopLevelMethods(List<? extends AstElement> children) {
            IndexDocument document = support.createDocument(indexable); // TODO Measure
            documents.add(document);

            String name = "Object";
            String in = null;
            String fqn = "Object";

            int flags = 0;
            document.addPair(FIELD_CLASS_ATTRS, IndexedElement.flagToString(flags), false, true);
            document.addPair(FIELD_FQN_NAME, fqn, true, true);
            document.addPair(FIELD_CASE_INSENSITIVE_CLASS_NAME, name.toLowerCase(), true, true);
            document.addPair(FIELD_CLASS_NAME, name, true, true);
            addRequire(document);
            if (requires != null) {
                document.addPair(FIELD_REQUIRES, requires, false, true);
            }

            // TODO - find a way to combine all these methods (from this file) into a single item
            
            // Add the fields, etc.. Recursively add the children classes or modules if any
            for (AstElement child : children) {
                assert child.getKind() == ElementKind.CONSTRUCTOR || child.getKind() == ElementKind.METHOD;
                indexMethod(child, document, true, false);
                // XXX what about fields, constants, attributes?
            }
        }
        
        private void indexMethod(AstElement child, IndexDocument document, boolean topLevel, boolean nodoc) {
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

            //XXX: this will skip TI for tests as it did in GSF where
            // platform was always false.
            if (platform && !userSourcesTest) {
                signature = RubyIndexerHelper.getMethodSignature(
                        child, flags, signature, file, knowledge);
                if (signature == null) {
                    return;
                }
            } else {
                if (!userSourcesTest) {
                    signature = RubyIndexerHelper.replaceAttributes(signature, flags);
                }
                signature = RubyIndexerHelper.getMethodSignatureForUserSources(child, signature, flags, knowledge);
            }
            document.addPair(FIELD_METHOD_NAME, signature, true, true);

            // Storing a lowercase method name is kinda pointless in
            // Ruby because the convention is to use all lowercase characters
            // (using _ to separate words rather than camel case) so we're
            // bloating the database for very little practical use here...
            //ru.put(FIELD_CASE_INSENSITIVE_METHOD_NAME, name.toLowerCase());
            if (child.getName().equals("initialize")) {
                // Create static method alias "new"; rdoc also seems to do this

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
                document.addPair(FIELD_METHOD_NAME, signature, true, true);
            }
        }

        private void indexAttribute(AstElement child, IndexDocument document, boolean nodoc) {
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
            
            document.addPair(FIELD_ATTRIBUTE_NAME, attribute, true, true);
        }

        private void indexConstant(AstElement child, IndexDocument document, boolean nodoc) {
//            int flags = 0; // TODO
//            if (nodoc) {
//                flags |= IndexedElement.NODOC;
//            }

            RubyType type = child.getType();
            StringBuilder signature = new StringBuilder(child.getName() + ';');
            if (type.isKnown()) {
                signature.append(type.asIndexedString());
            }

            document.addPair(FIELD_CONSTANT_NAME, signature.toString(), true, true);
        }

        private void indexField(AstElement child, IndexDocument document, boolean nodoc) {
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
            document.addPair(FIELD_FIELD_NAME, signature, true, true);
        }

        private void indexGlobal(AstElement child, IndexDocument document/*, boolean nodoc*/) {
            // Don't index globals in the libraries
            if (!platform || userSourcesTest) {

                String signature = child.getName();
//            int flags = getModifiersFlag(child.getModifiers());
//            if (nodoc) {
//                flags |= IndexedElement.NODOC;
//            }
//
//            if (flags != 0) {
//                StringBuilder sb = new StringBuilder(signature);
//                sb.append(';');
//                sb.append(IndexedElement.flagToFirstChar(flags));
//                sb.append(IndexedElement.flagToSecondChar(flags));
//                signature = sb.toString();
//            }

                // TODO - gather documentation on globals? naeh

                document.addPair(FIELD_GLOBAL_NAME, signature, true, true);
            }
        }

        private int getDocumentSize(Node node) {
            List<String> comments = AstUtilities.gatherDocumentation(result.getSnapshot(), node);

            if ((comments != null) && (comments.size() > 0)) {
                int size = 0;

                for (String line : comments) {
                    size += line.length();
                }

                return size;
            }

            return 0;
        }

        private boolean isDocumented(Node node) {
            List<String> comments = AstUtilities.gatherDocumentation(result.getSnapshot(), node);

            if ((comments != null) && (comments.size() > 0)) {
                return true;
            }

            return false;
        }

        private void addRequire(IndexDocument document) {
            // Don't generate "require" clauses for anything in generated ruby;
            // these classes are all built in and do not require any includes
            // (besides, the file names are bogus - they are just derived from
            // the class name by the stub generator)
            String folder = (file.getParent() != null) && file.getParent().getParent() != null ?
                file.getParent().getParent().getNameExt() : "";

            if (folder.equals(RubyPlatform.RUBYSTUBS) && file.getName().startsWith("stub_")) {
                return;
            }

            // Index for require-completion
            String relative = indexable.getRelativePath();

            if (relative != null) {
                if (relative.endsWith(".rb")) { // NOI18N
                    relative = relative.substring(0, relative.length() - 3);
                    document.addPair(FIELD_REQUIRE, relative, true, true);
                }
            }
        }
    }


    
// no preindexing in parsing API
//
//    private static FileObject preindexedDb;
//    /** For testing only */
//    public static void setPreindexedDb(FileObject preindexedDb) {
//        RubyIndexer.preindexedDb = preindexedDb;
//    }
//
//    public FileObject getPreindexedDb() {
//        if (preindexedDb == null) {
//            File preindexed = InstalledFileLocator.getDefault().locate(
//                    "preindexed", "org.netbeans.modules.ruby", false); // NOI18N
//            if (preindexed == null || !preindexed.isDirectory()) {
//                throw new RuntimeException("Can't locate preindexed directory. Installation might be damaged"); // NOI18N
//            }
//            preindexedDb = FileUtil.toFileObject(preindexed);
//        }
//        return preindexedDb;
//    }
//
//    static boolean isPreindexing() {
//        // part of a platform
//        return false;
//    }
}

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

package org.netbeans.modules.languages.features;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.EditorParser;


/**
 *
 * @author Jan Jancura
 */
public class DatabaseManager implements ParserManagerListener {
    
    private NbEditorDocument            doc;
    private EditorParser                parser;

    
    /** Creates a new instance of AnnotationManager */
    public DatabaseManager (Document doc) {
        
        this.doc = (NbEditorDocument) doc;
        parser = EditorParser.get (doc);
        parser.addListener (this);
    }

    public void parsed (State state, ASTNode root) {
        if (root == null) return;
        astNodeToDatabaseContext.put (root, parse (root, doc));
        //S ystem.out.println (rootContext.getAsText ());
    }

    static DatabaseContext parse (ASTNode ast, Document doc) {
        DatabaseContext rootContext = new DatabaseContext (null, null, ast.getOffset (), ast.getEndOffset ());
        List<ASTItem> path = new ArrayList<ASTItem> ();
        path.add (ast);
        List<DatabaseItem> unresolvedUsages = new ArrayList<DatabaseItem> ();
        process (path, rootContext, unresolvedUsages, doc);
        Iterator<DatabaseItem> it2 = unresolvedUsages.iterator ();
        while (it2.hasNext ()) {
            DatabaseUsage usage = (DatabaseUsage) it2.next ();
            DatabaseContext context = (DatabaseContext) it2.next ();
            DatabaseDefinition definition = rootContext.getDefinition (usage.getName (), usage.getOffset ());
            if (definition != null) {
                definition.addUsage (usage);
                context.addUsage (usage);
                usage.setDatabaseDefinition (definition);
            }
        }
        return rootContext;
    }
    
    private static void process (
        List<ASTItem> path, 
        DatabaseContext context, 
        List<DatabaseItem> unresolvedUsages,
        Document doc
    ) {
        ASTItem last = path.get (path.size () - 1);
        Iterator<ASTItem> it = last.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item =  it.next ();
            path.add (item);
            try {
                Language language = LanguagesManager.getDefault ().
                    getLanguage (item.getMimeType ());
                ASTPath astPath = ASTPath.create (path);
                Feature feature = language.getFeature ("SEMANTIC_DECLARATION", astPath);
                if (feature != null) {
                    SyntaxContext sc = SyntaxContext.create (doc, astPath);
                    String name = ((String) feature.getValue ("name", sc)).trim ();
                    String type = (String) feature.getValue ("type", sc);
                    if (name != null && name.length() > 0) {
                        //S ystem.out.println("add " + name + " " + item);
                        String local = (String) feature.getValue ("local", sc);
                        if (local != null) {
                            DatabaseContext c = context;
                            while (c != null && !local.equals (c.getType ()))
                                c = c.getParent ();
                            if (c != null) 
                                type = "local";
                        }
                        DatabaseContext con = context;
                        if ("method".equals(type)) { // NOI18N
                            con = con.getParent();
                            if (con == null) {
                                con = context;
                            }
                        }
                        con.addDefinition (new DatabaseDefinition (name, type, item.getOffset (), item.getEndOffset ()));
                    }
                }
                feature = language.getFeature ("SEMANTIC_CONTEXT", astPath);
                if (feature != null) {
                    String type = (String) feature.getValue ("type");
                    DatabaseContext newContext = new DatabaseContext (context, type, item.getOffset (), item.getEndOffset ());
                    context.addContext (item, newContext);
                    process (path, newContext, unresolvedUsages, doc);
                    path.remove (path.size () - 1);
                    continue;
                }
                feature = language.getFeature ("SEMANTIC_USAGE", astPath);
                if (feature != null) {
                    SyntaxContext sc = SyntaxContext.create (doc, astPath);
                    String name = (String) feature.getValue ("name", sc);
                    DatabaseDefinition definition = context.getDefinition (name, item.getOffset ());
                    DatabaseUsage usage = new DatabaseUsage (name, item.getOffset (), item.getEndOffset ());
                    if (definition != null) {
                        definition.addUsage (usage);
                        usage.setDatabaseDefinition (definition);
                        context.addUsage (usage);
                    } else {
                        unresolvedUsages.add (usage);
                        unresolvedUsages.add (context);
                    }
                }
            } catch (LanguageDefinitionNotFoundException ex) {
            }
            process (path, context, unresolvedUsages, doc);
            path.remove (path.size () - 1);
        }
    }
    
    private static Map<ASTNode,DatabaseContext> astNodeToDatabaseContext = new WeakHashMap<ASTNode,DatabaseContext> ();
    
    public static DatabaseContext getRoot (ASTNode ast) {
        return astNodeToDatabaseContext.get (ast);
    }
}


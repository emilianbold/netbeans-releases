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
package org.netbeans.api.languages;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.modules.languages.parser.LanguageDefinitionNotFoundException;
import org.openide.ErrorManager;


/**
 * Represents parser implementation.
 * 
 * @author Jan Jancura
 */
public abstract class ParserManager {
    
    /**
     * State of parser.
     */
    public static enum State {
        /** Parser is running. */
        PARSING, 
        /** Parsed witouut errors. */
        OK, 
        /** Parser with errors. */
        ERROR, 
        /** Parser has not been started yet. */
        NOT_PARSED
    }
    
    
    private static Map<Document,WeakReference<ParserManager>> managers = 
        new WeakHashMap<Document,WeakReference<ParserManager>> ();
    
    /**
     * Returns parser for given {@link javax.swing.text.Document}.
     * 
     * @return parser for given {@link javax.swing.text.Document}
     */
    public static synchronized ParserManager get (Document doc) {
        WeakReference<ParserManager> wr = managers.get (doc);
        ParserManager pm = wr != null ? wr.get () : null;
        if (pm == null) {
            String mimeType = (String) doc.getProperty("mimeType");
            try {
                ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage(mimeType);
            } catch (LanguageDefinitionNotFoundException e) {
                return null;
            } catch (ParseException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }
            pm = new ParserManagerImpl (doc);
            managers.put (doc, new WeakReference<ParserManager> (pm));
            //Utils.startTest ("ParserManager.managers", managers);
        }
        return pm;
    }

    /**
     * Returns state of parser.
     * 
     * @retrun a state of parser
     */
    public abstract State getState ();
    
    /**
     * Returns AST tree root node.
     * 
     * @throws in the case of errors in document
     * @retrun AST tree root node
     */
    public abstract ASTNode getAST () throws ParseException;
    
    /**
     * Registers ParserManagerListener.
     * 
     * @param l ParserManagerListener to be registerred
     */
    public abstract void addListener (ParserManagerListener l);
    
    /**
     * Unregisters ParserManagerListener.
     * 
     * @param l ParserManagerListener to be unregisterred
     */
    public abstract void removeListener (ParserManagerListener l);
    
    /**
     * Registers ASTEvaluator.
     * 
     * @param l ASTEvaluator to be unregisterred
     */
    public abstract void addASTEvaluator (ASTEvaluator e);
    
    /**
     * Unregisters ASTEvaluator.
     * 
     * @param l ASTEvaluator to be unregisterred
     */
    public abstract void removeASTEvaluator (ASTEvaluator e);
}




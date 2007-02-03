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
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.modules.languages.Utils;

/**
 *
 * @author Jan Jancura
 */
public abstract class ParserManager {
    
    public static final int PARSING = 1;
    public static final int OK = 2;
    public static final int ERROR = 3;
    public static final int NOT_PARSED = 4;
    
    
    private static Map managers = new WeakHashMap ();
    
    public static synchronized ParserManager get (NbEditorDocument doc) {
        WeakReference wr = (WeakReference) managers.get (doc);
        ParserManager pm = wr != null ? (ParserManager) wr.get () : null;
        if (pm == null) {
            pm = new ParserManagerImpl (doc);
            managers.put (doc, new WeakReference (pm));
            //Utils.startTest ("ParserManager.managers", managers);
        }
        return pm;
    }

    
    public abstract int getState ();
    
    public abstract ASTNode getAST () throws ParseException;
    
    public abstract void addListener (ParserManagerListener l);
    
    public abstract void removeListener (ParserManagerListener l);
}




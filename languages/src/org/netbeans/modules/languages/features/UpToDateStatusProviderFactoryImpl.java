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

import org.netbeans.api.languages.ASTNode;
import javax.swing.text.Document;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;

/**
 *
 * @author Jan Jancura
 */
public class UpToDateStatusProviderFactoryImpl implements UpToDateStatusProviderFactory {
    
    /** Creates a new instance of UpToDateStatusProvider */
    public UpToDateStatusProviderFactoryImpl () {
    }

    public UpToDateStatusProvider createUpToDateStatusProvider (Document document) {
        return new UpToDateStatusProviderImpl ((NbEditorDocument) document);
    }
    
    private static class UpToDateStatusProviderImpl extends UpToDateStatusProvider {
        
        private ParserManager parserManager;
        
        
        private UpToDateStatusProviderImpl (NbEditorDocument doc) {
            parserManager = ParserManager.get (doc);
            parserManager.addListener (new ParserManagerListener () {
                public void parsed (State state, ASTNode ast) {
                    firePropertyChange (PROP_UP_TO_DATE, null, null);
                }
            });
        }
        
        public UpToDateStatus getUpToDate () {
            switch (parserManager.getState ()) {
                case ERROR:
                    return UpToDateStatus.UP_TO_DATE_DIRTY;
                case OK:
                    return UpToDateStatus.UP_TO_DATE_OK;
                case PARSING:
                    return UpToDateStatus.UP_TO_DATE_PROCESSING;
            }
            return UpToDateStatus.UP_TO_DATE_PROCESSING;
        }
    }
}



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

package org.netbeans.modules.gsf;

import org.netbeans.api.gsf.GsfLanguage;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.DrawLayerFactory;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Settings;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.NbEditorDocument;

/**
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 */
public class GsfDocument extends NbEditorDocument {
    private Language language;
    private Formatter formatter;
    private Class kitClass;
    
    public GsfDocument(Class kitClass, Language language) {
        super(kitClass);
        if (language.getGsfLanguage() != null) {
            putProperty(org.netbeans.api.lexer.Language.class, language.getGsfLanguage().getLexerLanguage());
        }
        
        this.language = language;
    }

    @Override
    public boolean isIdentifierPart(char ch) {
        GsfLanguage gsfLanguage = language.getGsfLanguage();
        if (gsfLanguage != null) {
            return gsfLanguage.isIdentifierChar(ch);
        }
        
        return super.isIdentifierPart(ch);
    }
    
    @Override
    public boolean addLayer(DrawLayer layer, int visibility) {
        if (DrawLayerFactory.SyntaxLayer.class.equals(layer.getClass()))
            return false;
        
        return super.addLayer(layer, visibility);
    }

    /** Get the formatter for this document. */
    public Formatter getFormatter() {
        if (formatter == null) {
            // NbDocument will go looking in the settings map for a formatter - let's make
            // sure it finds one with -MY- kit associated with it (without this it finds
            // a BaseKit one which has the wrong formatting defaults!
            Settings.setValue(getKitClass(), FORMATTER, 
                    new ExtFormatter(GsfEditorKitFactory.GsfEditorKit.class));
            formatter = super.getFormatter();
        }
        
        return formatter;
    }
}

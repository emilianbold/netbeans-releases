/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.gen.antlr;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.lexer.gen.DescriptionReader;
import org.netbeans.modules.lexer.gen.LanguageGenerator;
import org.netbeans.modules.lexer.gen.MutableTokenId;
import org.netbeans.modules.lexer.gen.LanguageData;
import org.netbeans.modules.lexer.gen.TokenTypes;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;
import org.xml.sax.SAXException;

/**
 * Language class generator for antlr generated lexers.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class AntlrLanguageGenerator extends LanguageGenerator {
    
    public String generate(String langClassName, String lexerClassName,
    String tokenTypesClassName, File xmlLangDescFile)
    throws ClassNotFoundException, SAXException, IOException {

        LanguageData data = new LanguageData();
        data.setLanguageClassName(langClassName);
        data.setLexerClassName(lexerClassName);

        // Apply token types class info
        if (tokenTypesClassName != null) {
            Class tokenTypesClass = Class.forName(tokenTypesClassName);
            TokenTypes tokenTypes = new AntlrTokenTypes(tokenTypesClass);
            data.registerTokenTypes(tokenTypes);
        }

        // Apply possible xml description
        if (xmlLangDescFile != null) {
            DescriptionReader xmlLangDesc = new DescriptionReader(
                xmlLangDescFile.getAbsolutePath());
            
            xmlLangDesc.applyTo(data);
        }
        

        // Update int ids that do not have counterparts in token types
        data.updateUnassignedIntIds();

        return createSource(data);
    }
    
}


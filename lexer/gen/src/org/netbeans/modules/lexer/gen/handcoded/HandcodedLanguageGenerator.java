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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.gen.handcoded;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.lexer.gen.DescriptionReader;
import org.netbeans.modules.lexer.gen.LanguageGenerator;
import org.netbeans.modules.lexer.gen.LanguageData;
import org.netbeans.modules.lexer.gen.util.LexerGenUtilities;
import org.xml.sax.SAXException;

/**
 * Language class generator for handcoded lexers.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class HandcodedLanguageGenerator extends LanguageGenerator {

    public String generate(String langClassName, String lexerClassName,
    String tokenTypesClassName, File xmlLangDescFile)
    throws SAXException, IOException {

        LanguageData data = new LanguageData();
        data.setLanguageClassName(langClassName);
        data.setLexerClassName(lexerClassName);

        // Apply possible xml description
        if (xmlLangDescFile != null) {
            DescriptionReader xmlLangDesc = new DescriptionReader(
                xmlLangDescFile.getAbsolutePath());

            xmlLangDesc.applyTo(data);
        }

        // Update int ids that do not specify integer ids specifically
        data.updateUnassignedIntIds();

        return createSource(data);
    }

}


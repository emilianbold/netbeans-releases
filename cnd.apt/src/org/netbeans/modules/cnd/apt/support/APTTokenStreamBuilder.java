/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this path except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each path
 * and include the License path at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.support;

import antlr.TokenStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import org.netbeans.modules.cnd.apt.impl.support.generated.APTLexer;

/**
 * Creates token stream for input path
 * 
 * @author Vladimir Voskresensky
 */
public class APTTokenStreamBuilder {
    private APTTokenStreamBuilder() {
    }   
    
//    public static TokenStream buildTokenStream(File file) throws FileNotFoundException {  
//        String path = file.getAbsolutePath();
//        // could be problems with closing this stream
//        InputStream stream = new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE);        
//        return buildTokenStream(path, stream);
//    }
    
    public static TokenStream buildTokenStream(String text) {  
        Reader reader = new StringReader(text);
        return  buildTokenStream(text, reader);
    }  
    
    public static TokenStream buildTokenStream(String name, InputStream stream) {  
        APTLexer lexer = new APTLexer(stream);
        lexer.init(name, 0);
        return lexer;
    }    
    
    public static TokenStream buildTokenStream(String name, Reader in) {  
        APTLexer lexer = new APTLexer(in);
        lexer.init(name, 0);
        return lexer;
    }     
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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



/*
 * File       : LexerTester.java
 * Created on : Feb 3, 2004
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.JavaLexer;



import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;

/**
 * @author Aztec
 */
public class LexerTester<Lexer extends TokenStream>
{
    Lexer lex;
    HashMap<String, String> m_tokenMap = new HashMap<String, String>();
    public LexerTester(Lexer lexer, String tokenFile)
    {
        lex = lexer;
        initializeMap(tokenFile);
    }
    
    public void lexDump(String outFile)
    {
        
        try
        {
            File out = new File(outFile);
            if(!out.exists()) out.createNewFile();
            
            DataOutputStream destFileStrm 
                = new DataOutputStream(new FileOutputStream(out));            
            
            Token t = lex.nextToken();
            
            while(t.getType() != Token.EOF_TYPE)
            {
                String x = m_tokenMap.get(String.valueOf(t.getType())) + ":" + t.getText();
                destFileStrm.writeBytes(x);
            
                t = lex.nextToken();
            }
            
            destFileStrm.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (TokenStreamException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private void initializeMap(String tokenFile)
    {
        try
        {
            File tokFile = new File(tokenFile);
            if(!tokFile.exists()) return;
            
            BufferedReader rdr = new BufferedReader(new FileReader(tokFile));
            rdr.readLine();
            rdr.readLine();
            String str;
            StringTokenizer strTok = null;
            
            while((str = rdr.readLine()) != null)
            {        
                strTok = new StringTokenizer(str, "=");
                String name = strTok.nextToken();
                String number = null;
                while(strTok.hasMoreTokens())
                {
                    number = strTok.nextToken();
                }
                m_tokenMap.put(number, name);
            }
            
        }
        catch (NumberFormatException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public static void main(String[] args)
    {
        try
        {
            JavaLexer lex = new JavaLexer(new FileReader(new File(args[0])));
                
            LexerTester test = new LexerTester(lex, args[1]);
            test.lexDump(args[2]);
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

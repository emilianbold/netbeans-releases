
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

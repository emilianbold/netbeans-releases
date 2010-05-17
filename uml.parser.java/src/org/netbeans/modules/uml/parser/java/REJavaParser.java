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
 * REParser.java
 *
 * Created on March 2, 2005, 7:38 AM
 */

package org.netbeans.modules.uml.parser.java;

import org.netbeans.modules.uml.parser.java.JavaLexer;
import org.netbeans.modules.uml.parser.java.JavaRecognizer;
import org.netbeans.modules.uml.parser.java.JavaTokenTypes;
import org.netbeans.modules.uml.parser.java.JavaTreeParser;
import org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.IREJavaParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.dom4j.Node;

import antlr.ANTLRException;
import antlr.CharBuffer;
import antlr.CommonASTWithLocationsAndHidden;
import antlr.CommonHiddenStreamToken;
import antlr.RecognitionException;
import antlr.TokenStreamHiddenTokenFilter;

import org.netbeans.modules.uml.core.reverseengineering.reframework.IParserData;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.CommentGather;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ParserEventController;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ProcessTypeKind;
/**
 *
 * @author Administrator
 */
public class REJavaParser implements IREJavaParser
{
   
   /** Creates a new instance of REParser */
   public REJavaParser()
   {
   }
   
   private ParserEventController m_EventController = 
            new ParserEventController(new CommentGather(JavaTokenTypes.SL_COMMENT, 
                                                        JavaTokenTypes.ML_COMMENT),
                                                        "Java");

    private String                m_Filename;
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.IREJavaParser#notifyPackageEvent(org.dom4j.Node)
     */
    public void notifyPackageEvent(Node eventData)
    {
        // Missing in C++ code.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.IREJavaParser#notifyDependencyEvent(org.dom4j.Node)
     */
    public void notifyDependencyEvent(Node eventData)
    {
        // Missing in C++ code.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.IREJavaParser#notifyClassEvent(org.dom4j.Node)
     */
    public void notifyClassEvent(Node eventData)
    {
        // Missing in C++ code.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.IREJavaParser#notifyActionEvent(org.dom4j.Node)
     */
    public void notifyActionEvent(Node eventData)
    {
        // Missing in C++ code.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.IREJavaParser#notifyError(antlr.RecognitionException)
     */
    public void notifyError(RecognitionException e)
    {
        // Missing in C++ code.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#parseFile(java.lang.String, java.lang.String)
     */
    public void parseFile(String filename, String charset)
    {
        try
        {
	    InputStreamReader reader = null;
	    if (charset != null) {
		try {
		    reader = new InputStreamReader(new FileInputStream(filename), charset);
		} catch (UnsupportedEncodingException uee) {
		    // catch it here thus giving chance 
		    // to default charset version below
		}
	    }
	    if (reader == null) {
		reader = new InputStreamReader(new FileInputStream(filename));
	    }
            BufferedReader bufr = new BufferedReader(reader);
            CharBuffer buffer = new CharBuffer(bufr);
            processStreamAsFile(buffer, filename);
            bufr.close();
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#parseFile(java.lang.String)
     */
    public void parseFile(String filename)
    {
	parseFile(filename, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#parseOperation(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation)
     */
    public void parseOperation(String filename, String charset, IREOperation operation)
    {
        if (filename != null && operation != null)
        {
            // Now that I have the parser I can create a OperationBuffer and 
            // give it to the parser to start parsing.
            long start = getPosition(operation, "StartPosition"),
                 end   = getPosition(operation, "EndPosition");
            // This is non-ideal, but easier than creating a constrained Reader
            // TODO: Fix this to use a constrained Reader so that we 
            // don't introduce a memory bottleneck here.
            String text = extractText(filename, charset, (int) start, (int) end);
            StringReader read = new StringReader(text);
            CharBuffer buf = new CharBuffer(read);
            processStreamAsFragment(buf, filename);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#parseOperation(java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation)
     */
    public void parseOperation(String filename, IREOperation operation)
    {
	parseOperation(filename, null, operation);
    }

    private long getPosition(IParserData data, String tagname)
    {
        long pos = -1;
        
        ITokenDescriptor desc = data.getTokenDescriptor(tagname);
        if (desc != null && tagname.equals(desc.getType()))
        {
            pos = desc.getPosition();
            if ("EndPosition".equals(tagname))
                pos += desc.getLength();
        }
        return pos;
    }
    
    private String extractText(String filename, String charset, int start, int end)
    {
        if (end < start)
            return null;

        try 
        {
	    InputStreamReader reader = null;
	    if (charset != null) {
		try {
		    reader = new InputStreamReader(new FileInputStream(filename), charset);
		} catch (UnsupportedEncodingException uee) {
		    // catch it here thus giving chance 
		    // to default charset version below
		}
	    }
	    if (reader == null) {
		reader = new InputStreamReader(new FileInputStream(filename));
	    }
            BufferedReader bufr = new BufferedReader(reader);
            bufr.skip(start);
            
            char[] readc = new char[end - start];
            int readchars = bufr.read(readc);
            bufr.close();
            reader.close();
            return new String(readc, 0, readchars);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the state listener for the parser.  The state listener will recieve 
     * state events as the parser changes state.  When the state filter filters
     * out a state a state events will not be sent to the state listener.
     * 
     * @param pVal [out] The state listener.
     * @see #get_StateFilter(IStateFilter* *pVal)
     */
    public IStateListener getStateListener()
    {
        return m_EventController.getStateListener();
    }

    /**
     * Sets the state listener for the parser.  The state listener will recieve 
     * state events as the parser changes state.  When the state filter filters
     * out a state a state events will not be sent to the state listener.
     * 
     * @param newVal [in] The state listener.
     * @see #put_StateFilter(IStateFilter* newVal)
     */
    public void setStateListener(IStateListener stateListener)
    {
        m_EventController.setStateListener(stateListener);
    }

    /**
     * Gets the state filter for the parser.  The state filter determines 
     * if a state is to be filtered or not.  When a state is filtered all sub 
     * states are also filtered.  The token listener will not recieve any 
     * token events found while in a filtered state.
     * 
     * @param pVal [out] The state filter.
     * @see #get_StateListener(IStateListener* *pVal)
     */
    public IStateFilter getStateFilter()
    {
        return m_EventController.getStateFilter();
    }

    /**
     * Sets the state filter for the parser.  The state filter determines 
     * if a state is to be filtered or not.  When a state is filtered all sub 
     * states are also filtered.  The token listener will not recieve any 
     * token events found while in a filtered state.
     * 
     * @param newVal [in] The state filter.
     * @see #put_StateListener (IStateListener* newVal)
     */
    public void setStateFilter(IStateFilter filter)
    {
        m_EventController.setStateFilter(filter);
    }

    /**
     * Get the the interface that will process tokens found while
     * parsing a file.  Tokens will not be sent while in a state
     * that has bee filtered out, 
     * 
     * @param pVal [out] The token processor.
     */
    public ITokenProcessor getTokenProcessor()
    {
        return m_EventController.getTokenProcessor();
    }

    /**
     * Set the the interface that will process tokens found while
     * parsing a file.  Tokens will not be sent while in a state
     * that has bee filtered out, 
     * 
     * @param newVal [in] The token processor.
     */
    public void setTokenProcessor(ITokenProcessor tokenProcessor)
    {
        m_EventController.setTokenProcessor(tokenProcessor);
    }

    /**
     * Get the the interface that will be used to filter tokens
     * before they are sent to the token processor.  Tokens will 
     * not be sent if they have be filtered out.
     * 
     * @param pVal [out] The token filter.
     */
    public ITokenFilter getTokenFilter()
    {
        return m_EventController.getTokenFilter();
    }

    /**
     * Set the the interface that will be used to filter tokens
     * before they are sent to the token processor.  Tokens will 
     * not be sent if they have be filtered out.
     * 
     * @param pVal [out] The token filter.
     */
    public void setTokenFilter(ITokenFilter filter)
    {
        m_EventController.setTokenFilter(filter);
    }

    /**
     * Get the the interface that will recieve the error information 
     * will parsing the file.
     * 
     * @param pVal [out] The error listener.
     */
    public IErrorListener getErrorListener()
    {
        return m_EventController.getErrorListener();
    }

    /**
     * Set the the interface that will recieve the error information 
     * will parsing the file.
     * 
     * @param newVal [int] The error listener.
     */
    public void setErrorListener(IErrorListener errorListener)
    {
        m_EventController.setErrorListener(errorListener);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParser#processStreamByType(java.lang.String, int)
     */
    public void processStreamByType(String stream, int type)
    {
        StringReader sr = new StringReader(stream);
        CharBuffer cb = new CharBuffer(sr);
        if (type == ProcessTypeKind.PTK_PROCESS_FILE)
            processStreamAsFile(cb, null);
        else if (type == ProcessTypeKind.PTK_PROCESS_FRAGMENT)
            processStreamAsFragment(cb, null);
    }
    
    private void processStreamAsFragment(CharBuffer buffer, String filename)
    {
        // Create a scanner that reads from the input stream
        JavaLexer lexer = new JavaLexer(buffer);
        if (filename != null)
            lexer.setFilename( filename );
        lexer.setTokenObjectClass(CommonHiddenStreamToken.class.getName());
//        lexer.setTokenObjectFactory(&antlr::CommonHiddenStreamToken::factory);
        
        lexer.setEventController(m_EventController);

        // Now initialize create and initialize a filter to keep track of 
        // WS and Comments.
        TokenStreamHiddenTokenFilter filter = 
            new TokenStreamHiddenTokenFilter(lexer);
        filter.hide(JavaLexer.SL_COMMENT);
        filter.hide(JavaLexer.ML_COMMENT);

        // Create a parser that reads from the scanner
        JavaRecognizer parser = new JavaRecognizer(filter);
        parser.setASTNodeClass(CommonASTWithLocationsAndHidden.class.getName());

//        parser.setASTNodeFactory(&antlr::CommonASTWithLocationsAndHidden::factory);

        try
        {
           parser.setEventController(m_EventController);  
           parser.methodCompilationUnit();
        }
        catch(ANTLRException e)
        {
           m_EventController.errorFound(e.getMessage(), -1, -1, filename);  
        }

        try
        {
           JavaTreeParser treeParser = new JavaTreeParser();
           treeParser.setEventController(m_EventController);
           treeParser.parseMethodBody(parser.getAST());
        }
        catch(ANTLRException e)
        {
           m_EventController.errorFound(e.getMessage(), -1, -1, filename);  
        }
    }

    private void processStreamAsFile(CharBuffer buffer, String filename)
    {
        if (filename != null)
            m_EventController.setFilename(filename);
        try
        {
            // Create a scanner that reads from the input stream
            JavaLexer lexer = new JavaLexer(buffer);
            //JavaLexer lexer(s);
            if (filename != null)
                lexer.setFilename( filename );
            
            lexer.setTokenObjectClass(CommonHiddenStreamToken.class.getName());
//            lexer.setTokenObjectFactory(CommonHiddenStreamToken.class);
            lexer.setEventController(m_EventController);
   
            // Now initialize create and initialize a filter to keep track of 
            // WS and Comments.
            TokenStreamHiddenTokenFilter filter = new TokenStreamHiddenTokenFilter(lexer);
            filter.hide(JavaLexer.WS);
            filter.hide(JavaLexer.SL_COMMENT);
            filter.hide(JavaLexer.ML_COMMENT);
   
            // Create a parser that reads from the scanner
            //           JavaRecognizer parser(lexer);
            JavaRecognizer parser = new JavaRecognizer(filter);
            parser.setASTNodeClass(CommonASTWithLocationsAndHidden.class.getName());
   
            //***********************************************************************
            // Antlr 2.7.2
            //***********************************************************************
//            parser.setASTFactory(new ASTFactory())
//            ASTFactory myFactory =
//                    new ASTFactory();
            //          antlr::ASTFactory my_factory("CommonASTWithLocationsAndHidden",
            //                                       &antlr::CommonASTWithLocationsAndHidden::factory);
            //           // tell the parser about the factory
            //        parser.setASTFactory(&my_factory);
            // 
            //        // let the parser initialize the factory
            //        parser.initializeFactory();
            //          parser.setASTNodeFactory(&my_factory);
   
            //***********************************************************************
            // Antlr 2.7.1
            //***********************************************************************
//            parser.setASTFactory(CommonAST)
//            parser.setASTNodeFactory(&antlr::CommonASTWithLocationsAndHidden::factory);
   
            if (filename != null)
                parser.setFilename( filename );
   
            try
            {
               // start parsing at the compilationUnit rule
               parser.setEventController(m_EventController);
               parser.compilationUnit();      
            }
            catch(ANTLRException e)
            {
               m_EventController.errorFound(e.getMessage(), -1, -1, filename);  
            }
            catch (Exception e)
            {
               m_EventController.errorFound(e.getMessage(), -1, -1, filename); 
            }

//      #if _DEBUG
//
//               ASTUtilities::DumpTree(_T("c:\\TestDump.txt"), parser.getAST(), false);
//      #endif

            boolean errorOccurred = false;
            try
            {
//                java.io.PrintStream curOut = System.out;
//                PrintStream newOut = new PrintStream("C:\\Temp\\ASTDump.txt");
//                System.setOut(newOut);
//                antlr.DumpASTVisitor visitor = new antlr.DumpASTVisitor();
//                visitor.visit(parser.getAST());
//                newOut.flush();
//                System.setOut(curOut);
                
                JavaTreeParser treeParser = new JavaTreeParser();
                treeParser.setEventController(m_EventController);
                treeParser.compilationUnit(parser.getAST());
            }
            catch(ANTLRException e)
            {
                m_EventController.errorFound(e.getMessage(), -1, -1, filename);  
            }
            catch (Exception e)
            {
                e.printStackTrace();
                errorOccurred = true;
                m_EventController.errorFound(e.getMessage(), -1, -1, filename); 
            }

            if(errorOccurred)
            {
                m_EventController.errorFound("Unable to complete parsing the file.  Possible Stack Overflow.", -1, -1, filename);
            }

            // The AST Tree will desctruct in a recursive mannor.  When the 
            // tree is deep the descruction process can cause a stack overflow.  To
            // protect against the stack overflow DeleteTree will desctruct the AST
            // tree in a non-recursive mannor.
//            ASTUtilities astUtils = new ASTUt;
//            astUtils.DeleteTree(parser.getAST());
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         finally
         {
            m_EventController.clear();
         }
    }   
}

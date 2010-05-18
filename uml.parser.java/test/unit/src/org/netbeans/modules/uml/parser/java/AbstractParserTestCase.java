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


package org.netbeans.modules.uml.parser.java;

import java.util.ArrayList;

import junit.framework.TestCase;
import java.io.File;
import java.net.URL;
import org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.REJavaParser;
/**
 * 
 */
public abstract class AbstractParserTestCase extends TestCase {
    
    // Retrieved Tokens are stored
    protected ArrayList<String[]> tokens = new ArrayList<String[]>();
    
    // Retrieved States are stored
    protected ArrayList<String> states = new ArrayList<String>();
    
    //Istansiate Parser for parse the Java Files
    private static REJavaParser parser=new REJavaParser();
    
    //Store testDataPath which contains URL of Data folder
    private static String testDataPath=null;
    
    @Override
            protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void clearAll() {
        clearTokens();
        clearStates();
    }
    
    protected void clearTokens() {
        tokens = new ArrayList<String[]>();
    }
    
    protected void clearStates() {
        states = new ArrayList<String>();
    }
    
    protected void addToken(String tokenName, String tokenType) {
        String ast[] = { tokenName, tokenType };
        tokens.add(ast);
    }
    
    protected void addState(String o) {
        states.add(o);
    }
    
    /**
     * Compares expected States with Retrieved States
     */
    protected boolean compareStatesWith(String expectedStates[]) {
        if (states.size() != expectedStates.length) {
            System.out.println("\nNumber Of States\n \tResult:" + states.size()
            + "Expected:" + expectedStates.length);
            showStates();
            return false;
        }
        
        for (int i = 0; i < states.size(); i++)
            if (!states.get(i).equals(expectedStates[i])) {
            System.out.println("\nState Obtained:" + states.get(i)
            + " Expected State:" + expectedStates[i]);
            showStates();
            return false;
            }
        
        return true;
    }
    
    /**
     * Compares expected Tokens with Retrieved Tokens
     */
    protected boolean compareTokensWith(String expectedTokens[][]) {
        
        if (tokens.size() != expectedTokens.length) {
            System.out.println("\nNumber Of Tokens\n \tResult:" + tokens.size()
            + " Expected:" + expectedTokens.length);
            showTokens();
            return false;
        }
        
        for (int i = 0; i < tokens.size(); i++) {
            String ast[] = (String[]) tokens.get(i);
            for (int j = 0; j < 2; j++) {
                if (!ast[j].equals(expectedTokens[i][j])) {
                    System.out.println("\nToken Obtained:" + ast[j]
                            + " Expected Token:" + expectedTokens[i][j]);
                    showTokens();
                    return false;
                }
            }
        }
        
        return true;
        
    }
    
    /**
     * Parse and test the tokens and states are coming properly.
     */
    protected void execute(String fileName, String expectedStates[],
            String expectedTokens[][]) {
//        fileName =getDataDir() + fileName;
//        clearAll();
//        
//        if (parser != null) {
//            ParserTestListener listener = new ParserTestListener(this);
//            parser.setStateListener(listener);
//            parser.setTokenProcessor(listener);
//            
//            parser.parseFile(fileName);
//            
//            assertTrue("States Mismatch",compareStatesWith(expectedStates));
//            assertTrue("Tokens Mismatch",compareTokensWith(expectedTokens));
//        } else
//            fail("REJavaParser is  Not instanciated");
    }
    
    /**
     * Shows the states
     */
    private void showStates() {
        System.out.println("\n");
        for (String state : states) {
            System.out.print("\"" + state + "\",");
        }
        
        System.out.println("");
   }
  
    /**
     * Shows the tokens
     */
     private void showTokens() {
       
        System.out.println("");
        
        System.out.print("{ ");
        for (String[] pToken : tokens) {
            System.out.print("{\"" + pToken[0] + "\",  \"" + pToken[1]
                    + "\"} ,");
        }
        System.out.println(" }");
        
    }
     
    /**
     * Get the path of  test Data Folder
     */
    public String  getDataDir() {
        if (testDataPath!=null)
            return testDataPath;
        String s = System.getProperty("xtest.data");
        if(s != null)
            return  s;
        String s1 = getClass().getName();
        URL url = getClass().getResource(s1.substring(s1.lastIndexOf('.') + 1) + ".class");
        File  file = (new File(url.getFile())).getParentFile();
        for(int i = 0; (i = s1.indexOf('.', i) + 1) > 0;)
            file = file.getParentFile();
        testDataPath=file.getParent() + "\\data\\Java5ParserTestJavaFiles\\";
        return testDataPath ;
    }
}

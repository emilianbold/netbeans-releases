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

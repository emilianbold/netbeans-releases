package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import antlr.CommonASTWithLocationsAndHidden;
import antlr.CommonHiddenStreamToken;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.JavaTokenTypes;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.CommentGather;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ParserEventController;

public abstract class AbstractUmlParserTestCase extends AbstractUMLTestCase {
    
//    /* Begin:Initialize Product */
//    private static ICoreProduct prod;
//    static {
//        CoreProductManager.instance().setCoreProduct(new ADProduct());
//        prod = CoreProductManager.instance().getCoreProduct();
//        prod.initialize();
//    }
//    
//    /* End: Initialize Product */
    
    /** Store testDataPath which contains URL of Data folder */
    private static String testDataPath = null;
    
    /** Instanciate EventController */
    private ParserEventController m_EventController=null ;
    
    /** Instanciate Listeners-Token JavaUMLParserProcessor */
    private UMLParserTestListener javaUMLParserProcessor=null;
    
    
    
   protected void setUp() throws Exception {
		super.setUp();
	
        if(m_EventController==null) {
            m_EventController =  new ParserEventController(
                    new CommentGather(JavaTokenTypes.SL_COMMENT,
                    JavaTokenTypes.ML_COMMENT), "Java");
            
            javaUMLParserProcessor = new UMLParserTestListener();
            
            m_EventController.setTokenProcessor(javaUMLParserProcessor);
            m_EventController.setStateListener(javaUMLParserProcessor);
            m_EventController.setTokenFilter(javaUMLParserProcessor);
            m_EventController.setStateFilter(javaUMLParserProcessor);
        }
        
    }
    
    /**
     * This method is the main entrance to all the UMLParser TestCases.
     *
     * @param String
     *            ClassName TestCase Name is passed
     */
    public void execute(String className) {      
        javaUMLParserProcessor.clearList();
		String fileNameInput = getDataDir() + "InputFiles" + File.separator
				+ className + "InputFile.txt";
		assertTrue("Input File Not Found", new File(fileNameInput).exists());
        
        // Read States and Tokens from Input file
        String strInput = readFile(fileNameInput);
		assertNotSame("Input File contains Empty String:", strInput, "");
        
        int level = -1;
        StringTokenizer strTokens = new StringTokenizer(strInput, "\r\n");
        while (strTokens.hasMoreTokens()) {
            String strToken = (String) strTokens.nextElement();
            if (strToken.trim().equals(""))
                continue;
            
            // Initial State Finding Algorithm
            if (!strToken.startsWith(" ")) {
                if (strToken.startsWith("{"))
                    throw new RuntimeException("Instead of State, Token Found");
                
                // If Same class has more than one root tag then we need close
                // all states of previous root node
                while (level > -1) {
                    m_EventController.stateEnd();
                    level--;
                }
                m_EventController.stateBegin(strToken);
                level++;
                continue;
            }
            
            // Calculate The Level
            int tmpLevel = 0;
            while (strToken.startsWith(" ")) {
                strToken = strToken.substring(1);
                tmpLevel++;
            }
            
            // Close The States Which are unwanted
            while (tmpLevel <= level) {
                m_EventController.stateEnd();
                level--;
            }
            
            // check strToken contains state or token
            if (!strToken.startsWith("{")) {
                level = tmpLevel;
                m_EventController.stateBegin(strToken);
            } else {
                m_EventController.tokenFound(
                        makeASTNode(getTokenName(strToken)),
                        getTokenType(strToken));
            }
            
        }
        
        // Close All States
        while (level > -1) {
            m_EventController.stateEnd();
            level--;
        }
        
        compareXMI(className);
        
    }
    
    /**
     * Compare Expected and retrived XMIData
     */
    private void compareXMI(String className) {
        
		String fileNameResult = getDataDir() + "ResultFiles" + File.separator
				+ className + "ResultFile.txt";
		assertTrue("Result File Not Found", new File(fileNameResult).exists());
        
        // Retrieve XMI Data
        ArrayList<String> xmiTags = javaUMLParserProcessor.getXMIData();
        String retrivedResult = "";
        for (String xmiTag : xmiTags) {
            retrivedResult += xmiTag;
        }
        
        // read Expected Result from ResultFile
        String expectedResult = readFile(fileNameResult);
		assertNotSame("Result File contains Empty String:", expectedResult, "");
        if (!retrivedResult.equals(expectedResult))
            System.out.println("\nTestCase " + className
                    + ":\nRetrived XMIData:\n" + retrivedResult);
        
        assertEquals("Expected and Retrived XMI Data are Different",
				expectedResult,retrivedResult );
    }
    
    private String getTokenName(String strToken) {
        int index = strToken.lastIndexOf(",");
        String name = strToken.substring(1, index).trim();
        return name;
    }
    
    private String getTokenType(String strToken) {
        int index = strToken.lastIndexOf(",");
        String type = strToken.substring(index + 1, strToken.length() - 2)
        .trim();
        return type;
    }
    
    /** Creates AST Node using the Token Name */
    private CommonASTWithLocationsAndHidden makeASTNode(String name) {
        CommonASTWithLocationsAndHidden astNode = new CommonASTWithLocationsAndHidden();
        astNode.initialize(new CommonHiddenStreamToken(name));
        return astNode;
    }
    
    /**
     * Get the path of test Data Folder
     */
    private String getDataDir() {
        if (testDataPath != null)
            return testDataPath;
        testDataPath = System.getProperty("xtest.data");
        if (testDataPath != null) 	
			return (testDataPath += File.separator + "UMLParserTestFiles"
					+ File.separator);
       
        String s1 = getClass().getName();
        URL url = getClass().getResource(
                s1.substring(s1.lastIndexOf('.') + 1) + ".class");
        File file = (new File(url.getFile())).getParentFile();
        for (int i = 0; (i = s1.indexOf('.', i) + 1) > 0;)
            file = file.getParentFile();
		testDataPath = file.getParent() + File.separator + "data"
				+ File.separator + "UMLParserTestFiles" + File.separator;
        return testDataPath;
    }
    
    /**
     * Reads the File and forms as String Data.
     *
     * @param String
     *            FileName to be read
     * @return String
     */
    public static String readFile(String fileName) {
        String str = "";
        try {
            FileInputStream p = new FileInputStream(fileName);
            int ch = -1;
            while ((ch = p.read()) != -1)
                str += (char) ch;
        } catch (Exception ewe) {
            ewe.printStackTrace();
        }
        return str;
    }
    
}

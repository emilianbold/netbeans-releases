/*
 * IndentCoreGenerator.java
 *
 * Created on March 16, 2002, 5:50 PM
 */

package org.netbeans.test.editor.indentation.programmatic;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.EntityResolver;
import java.io.InputStream;
import org.xml.sax.InputSource;
import java.io.File;
import java.io.Writer;
import java.io.FileWriter;

/**
 *
 * @author  lahvac
 */
public class IndentCoreGenerator {
    
    /** Creates a new instance of IndentCoreGenerator */
    public IndentCoreGenerator() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String generatorLocation = "/home/lahvac/31/tests/editor/test/qa-functional/src/org/netbeans/test/editor/indentation/programmatic";
        cases = new Vector();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        factory.setValidating(true);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        builder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String a, String b) {
                return new InputSource(IndentCoreGenerator.class.getResourceAsStream("definition.dtd"));
            }
        });

        Document doc = builder.parse(IndentCoreGenerator.class.getResourceAsStream("definition.xml"), generatorLocation);
        DefinitionScanner scanner = new DefinitionScanner(doc);

        scanner.visitDocument();

        Writer output = new FileWriter(new File(generatorLocation, "IndentCorePerformer.java"));
        
        output.write("package " + IndentCoreGenerator.class.getPackage().getName() + ";\n");
        output.write("import java.io.*;\n");
        output.write("import java.util.*;\n");
        output.write("import org.netbeans.junit.NbTestCase;\n");
        output.write("import org.netbeans.junit.NbTestSuite;\n");
	output.write("import java.io.File;\n");
        output.write("public class IndentCorePerformer extends NbTestCase {\n");
        output.write("    public IndentCorePerformer(String testCase) {\n");
        output.write("        super(testCase);\n");
        output.write("    }\n");
	output.write("    public void tearDown() throws Exception {\n");
	output.write("        assertFile(\"Output does not match golden file.\", getGoldenFile(), new File(getWorkDir(), this.getName() + \".ref\"), null, new org.netbeans.junit.diff.SimpleDiff());\n");
	output.write("    }\n");
        
        Iterator iterator = cases.iterator();

        while (iterator.hasNext()) {
            TestCase testCase = (TestCase) iterator.next();

            output.write(testCase.generate());
        }
        output.write("}\n");
        output.close();
    }
    
    private static Vector cases;
    
    public static void addTestCase(TestCase testCase) {
        cases.add(testCase);
    }
    
    public static class TestCase {
        private String  name;
        private String  textLocation;
        private String  textMIMEType;
        private Map     indentProperties;
        private boolean willFail;
        private String  failReason;
        
        public TestCase() {
            failReason = "";
            name = "";
            textLocation = "";
            textMIMEType = "";
            indentProperties = new HashMap();
            willFail = false;
        }
        
        public Map getIndentProperties() {
            return indentProperties;
        }
        
        public void addIndentProperty(String name, String value) {
            indentProperties.put(name, value);
        }
        
        public String generate() {
            StringBuffer result = new StringBuffer();
            
            result.append("    public void test");
            result.append(getName());
            result.append("() throws Exception {\n");
            result.append("        PrintWriter log = null;\n");
            result.append("        PrintWriter ref = null;\n");
            result.append("        try {\n");
            result.append("            log = new PrintWriter(getLog());\n");
            result.append("            ref = new PrintWriter(getRef());\n");
            result.append("            Map indentationProperties = new HashMap();\n");
            for (Iterator iterator = getIndentProperties().keySet().iterator();
                 iterator.hasNext();
                 ) {
                String key = (String) iterator.next();
                
                result.append("            indentationProperties.put(\"" + key + "\", \"" + getIndentProperties().get(key).toString() + "\");\n");
            }
            result.append("            new IndentCore().run(log, ref, \"" + getTextLocation() + "\", \"" + getTextMIMEType() + "\", indentationProperties);\n");
            result.append("        } finally {\n");
            result.append("            log.flush();\n");
            result.append("            ref.flush();\n");
            result.append("        }\n");
            result.append("    }\n");
            
            return result.toString();
        }
        
        /** Getter for property name.
         * @return Value of property name.
         */
        public java.lang.String getName() {
            return name;
        }
        
        /** Setter for property name.
         * @param name New value of property name.
         */
        public void setName(java.lang.String name) {
            this.name = name;
        }
        
        /** Getter for property failReason.
         * @return Value of property failReason.
         */
        public java.lang.String getFailReason() {
            return failReason;
        }
        
        /** Setter for property failReason.
         * @param failReason New value of property failReason.
         */
        public void setFailReason(java.lang.String failReason) {
            this.failReason = failReason;
        }
        
        /** Getter for property textLocation.
         * @return Value of property textLocation.
         */
        public java.lang.String getTextLocation() {
            return textLocation;
        }
        
        /** Setter for property textLocation.
         * @param textLocation New value of property textLocation.
         */
        public void setTextLocation(java.lang.String textLocation) {
            this.textLocation = textLocation;
        }
        
        /** Getter for property textMIMEType.
         * @return Value of property textMIMEType.
         */
        public java.lang.String getTextMIMEType() {
            return textMIMEType;
        }
        
        /** Setter for property textMIMEType.
         * @param textMIMEType New value of property textMIMEType.
         */
        public void setTextMIMEType(java.lang.String textMIMEType) {
            this.textMIMEType = textMIMEType;
        }
        
        /** Getter for property willFail.
         * @return Value of property willFail.
         */
        public boolean isWillFail() {
            return willFail;
        }
        
        /** Setter for property willFail.
         * @param willFail New value of property willFail.
         */
        public void setWillFail(boolean willFail) {
            this.willFail = willFail;
        }
        
    }
    
}

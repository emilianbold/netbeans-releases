package org.netbeans.modules.soa.palette.java.constructs;

/**
 *
 * @author lyu
 */
public class CodeGen_JUnit extends CodeGen {
        public CodeGen_JUnit(){
            super(null);
        }
        public String generateCode(XSD_DOMNode root, String generatedClassName, Object obj) throws CodeGenException, NoSuchMethodException {
            // Variables
                String s;
                
            // Generate the code
                s = "";
                s = s + generateImportStatements();
                s = s + "\n";
                s = s + "public class " + generatedClassName + " {\n";
                s = s + "    // TODO Constants \n";
                s = s + "        private static final String _TODO = \"\"; \n";
                s = s + "        private static final int _TODO_1 = 1; \n";
                s = s + "\n";
                
                s = s + "    // Private Functions\n";
                s = s + "        public static String marshal(Object obj) throws Exception {\n";
                s = s + "            // Variables\n";
                s = s + "                ByteArrayOutputStream baos;\n";
                s = s + "                JAXBContext jaxbCtx;\n";
                s = s + "                Marshaller marshaller;\n";
                s = s + "\n";
                s = s + "            // Create JAXBContext and Marshaller and Unmarshaller\n";
                s = s + "                jaxbCtx = JAXBContext.newInstance(obj.getClass().getPackage().getName());\n";
                s = s + "                marshaller = jaxbCtx.createMarshaller();\n";
                s = s + "                marshaller.setProperty(Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N\n";
                s = s + "                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);\n";
                s = s + "\n";
                s = s + "            // Marshal\n";
                s = s + "                baos = new ByteArrayOutputStream();\n";
                s = s + "                try {\n";
                s = s + "                    marshaller.marshal(obj, baos);\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    marshaller.marshal( new JAXBElement(new QName(\"uri\",\"" + root.getElementName() + "\"), " + root.getNodeClass().getName() + ".class, obj ), baos);\n";
                s = s + "                }\n";
                s = s + "                return baos.toString();\n";
                s = s + "        }\n";                
                s = s + "\n";
                s = s + "    // Public Functions\n";
                s = s + "        public static String generateXML() throws Exception {\n";
                s = s + Utilities.indent(generateCodeForNode(root, 0, obj, "\t"), "            ");
                s = s + "            // Marshal\n";
                s = s + "            return marshal(" + root.getName().toLowerCase() + ");\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "    // Main Function\n";
                s = s + "        public static void main(String[] args) {\n";
                s = s + "            // Dump the generated XML\n";
                s = s + "                try {\n";
                s = s + "                    System.out.println(" + generatedClassName + ".generateXML());\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    E.printStackTrace();\n";
                s = s + "                }\n";
                s = s + "        }\n";
                
                
/*                        
                
                s = s + "    // Main Function \n";
                s = s + "        public static void main(String[] args) {\n";
                s = s + "            // Data Type Factory \n";
                s = s + "            DatatypeFactory datatypefactory = new DatatypeFactoryImpl();\n";
                s = s + "\n";
                
                s = s + indent(generateCodeForNode(root, 0, obj), "            ");
                
                s = s + "            try {\n";
                s = s + "                // Variables\n";
                s = s + "                    ByteArrayOutputStream baos;\n";
                s = s + "                    JAXBContext jaxbCtx;\n";
                s = s + "                    Object jaxbObject;\n";
                s = s + "                    Marshaller marshaller;\n";
                s = s + "                    String s;\n";
                s = s + "                    Unmarshaller unmarshaller;\n";
                s = s + "\n";
                s = s + "                // Create JAXBContext and Marshaller and Unmarshaller\n";
                s = s + "                    jaxbCtx = JAXBContext.newInstance(" + root.getName().toLowerCase() + ".getClass().getPackage().getName());\n";
                s = s + "                    marshaller = jaxbCtx.createMarshaller();\n";
                s = s + "                    marshaller.setProperty(Marshaller.JAXB_ENCODING, \"UTF-8\"); //NOI18N\n";
                s = s + "                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);\n";
                s = s + "                    unmarshaller = jaxbCtx.createUnmarshaller();\n";
                s = s + "\n";
                s = s + "                // Marshal #1\n";
                s = s + "                    baos = new ByteArrayOutputStream();\n";
                s = s + "                    marshaller.marshal(" + root.getName().toLowerCase() + ", baos);\n";
                s = s + "                    System.out.println(\"Marshal #1\");\n";
                s = s + "                    System.out.println(\"----------\");\n";
                s = s + "                    System.out.println(baos.toString());\n";
                s = s + "\n";
                s = s + "                // Unmarshal\n";
                s = s + "                    jaxbObject = unmarshaller.unmarshal(new ByteArrayInputStream(baos.toByteArray()));\n";
                s = s + "\n";
                s = s + "                // Marshal #2\n";
                s = s + "                    baos = new ByteArrayOutputStream();\n";
                s = s + "                    System.out.println(\"Marshal #2\");\n";
                s = s + "                    System.out.println(\"----------\");\n";
                s = s + "                    marshaller.marshal(jaxbObject, baos);\n";
                s = s + "                    System.out.println(baos.toString());\n";
                s = s + "            } catch (Exception E) {\n";
                s = s + "                E.printStackTrace();\n";
                s = s + "            }\n";
                s = s + "\n";
                s = s + "        }\n";
 */ 
                s = s + "}\n";
                
            // Success!
                return s;
        }        
        
        public String generateTestCase(XSD_DOMNode root, String generatedClassName, Object obj, String xml) throws CodeGenException {
            // Variables
                String s;
                String sa[];
                String stemp;
                
            // Generate the code
                s = "";
                s = s + "import java.io.ByteArrayInputStream;\n";
                s = s + "import java.lang.reflect.Field;\n";
                s = s + "import java.lang.reflect.Method;\n";
                s = s + "import java.util.List;\n";
                s = s + "import javax.xml.bind.JAXBContext;\n";
                s = s + "import javax.xml.bind.Unmarshaller;\n";
                s = s + "import junit.framework.TestCase;\n";
                s = s + "\n";
                s = s + "public class " + generatedClassName + "_TestCase extends TestCase {\n";
                s = s + "    // TestCase overrides\n";
                s = s + "        protected void setUp() {\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "        protected void tearDown() {\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "    // Private Functions\n";
                s = s + "        private boolean compareFields(Object obj1, Field f1, Object obj2, Field f2) {\n";
                s = s + "            // Variables\n";
                s = s + "                String r1;\n";
                s = s + "                String r2;\n";
                s = s + "\n";
                s = s + "            // Sanity check\n";
                s = s + "                if (f1.getType().equals(String.class)) {\n";
                s = s + "                    fail(\"Unhandled field type: \" + f1.getType());\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Compare the fields\n";
                s = s + "                r1 = \"\";\n";
                s = s + "                r2 = \"\";\n";
                s = s + "                try {\n";
                s = s + "                    r1 = f1.get(obj1).toString();\n";
                s = s + "                    r2 = f2.get(obj2).toString();\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    fail(E.toString());\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Success?\n";
                s = s + "                return (r1.equals(r2));\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "        private boolean compareLists(List l1, List l2) {\n";
                s = s + "            // Sanity check\n";
                s = s + "                if (l1.size() != l2.size()) {\n";
                s = s + "                    fail(\"Lists are different sizes!\");\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Compare list contents\n";
                s = s + "                for (int i = 0; i < l1.size(); i++) {\n";
                s = s + "                    if (!compareObjects(l1.get(i), l2.get(i))) {\n";
                s = s + "                        return false;\n";
                s = s + "                    }\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Success!\n";
                s = s + "                return true;\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "        private boolean compareMethods(Object obj1, Method m1, Object obj2, Method m2) {\n";
                s = s + "            // Variables\n";
                s = s + "                Object r1;\n";
                s = s + "                Object r2;\n";
                s = s + "\n";
                s = s + "            // Sanity Check\n";
                s = s + "                if (!m1.getReturnType().equals(m2.getReturnType())) {\n";
                s = s + "                    fail(\"Method return types differ\");\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Exit if the method is a setter\n";
                s = s + "                if ((m1.getName().startsWith(\"set\")) && (m2.getName().startsWith(\"set\"))) {\n";
                s = s + "                    return true;\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Exit if static function\n";
                s = s + "                if ((m1.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0 ) {\n";
                s = s + "                    return true;\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Invoke the methods\n";
                s = s + "                r1 = null;\n";
                s = s + "                r2 = null;\n";
                s = s + "                try {\n";
                s = s + "                    r1 = m1.invoke(obj1);\n";
                s = s + "                    r2 = m2.invoke(obj2);\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    fail(E.toString());\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Compare the returned objects\n";
                s = s + "                return compareObjects(r1, r2);\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "        private boolean compareObjects(Object obj1, Object obj2) {\n";
                s = s + "            // Variables\n";
                s = s + "                Field field1;\n";
                s = s + "                Field field2;\n";
                s = s + "                Field fieldarray[] = obj1.getClass().getDeclaredFields();\n";
                s = s + "                Method method1;\n";
                s = s + "                Method method2;\n";
                s = s + "                Method methodarray[] = obj2.getClass().getMethods();\n";
                s = s + "\n";
                s = s + "            // Special Case for Lists\n";
                s = s + "                if ((List.class.isAssignableFrom(obj1.getClass())) && \n";
                s = s + "                    (List.class.isAssignableFrom(obj2.getClass()))) {\n";
                s = s + "                    return compareLists((List) obj1, (List) obj2);\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Special Case for Primitives\n";
                s = s + "                if (isPrimitiveType(obj1.getClass().getName())) {\n";
                s = s + "                    return obj1.equals(obj2);\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Compare Fields\n";
                s = s + "                try {\n";
                s = s + "                    for (int i = 0; i < fieldarray.length; i++ ) {\n";
                s = s + "                        if ((fieldarray[i].getModifiers() & java.lang.reflect.Modifier.PUBLIC) != 0) {\n";
                s = s + "                            field1 = obj1.getClass().getField(fieldarray[i].getName());\n";
                s = s + "                            field2 = obj2.getClass().getField(fieldarray[i].getName());\n";
                s = s + "                            if (!compareFields(obj1, field1, obj2, field2)) {\n";
                s = s + "                                return false;\n";
                s = s + "                            }\n";
                s = s + "                        }\n";
                s = s + "                    }\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    fail(E.toString());\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Compare Methods\n";
                s = s + "                try {\n";
                s = s + "                    for (int i = 0; i < methodarray.length; i++ ) {\n";
                s = s + "                        if (((methodarray[i].getModifiers() & java.lang.reflect.Modifier.PUBLIC) != 0) &&\n";
                s = s + "                            ((methodarray[i].getModifiers() & java.lang.reflect.Modifier.STATIC) ==0 ) &&\n";
                s = s + "                            (!methodarray[i].getDeclaringClass().equals(Object.class)) && \n";
                s = s + "                            (!methodarray[i].getDeclaringClass().equals(java.lang.String.class)) &&\n";
                s = s + "                            (!methodarray[i].getDeclaringClass().equals(java.lang.Enum.class))) {\n";
                s = s + "                                method1 = obj1.getClass().getMethod(methodarray[i].getName(), methodarray[i].getParameterTypes());\n";
                s = s + "                                method2 = obj2.getClass().getMethod(methodarray[i].getName(), methodarray[i].getParameterTypes());\n";
                s = s + "                                if (!compareMethods(obj1, method1, obj2, method2)) {\n";
                s = s + "                                    return false;\n";
                s = s + "                                }\n";
                s = s + "                        }\n";
                s = s + "                    }\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    fail(E.toString());\n";
                s = s + "                }\n";
                s = s + "\n";
                s = s + "            // Success!\n";
                s = s + "                return true;\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "        public static boolean isPrimitiveType(String className) {\n";
                s = s + "            // Variables\n";
                s = s + "                boolean retVal;\n";
                s = s + "\n";
                s = s + "            // Check if primitive type\n";
                s = s + "                retVal = false;\n";
                s = s + "                if (className.equals(\"java.lang.Boolean\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.lang.Byte\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.lang.Double\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.lang.Float\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.lang.Integer\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.lang.Long\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.lang.Object\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.lang.Short\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.lang.String\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.math.BigDecimal\")) retVal = true;\n";
                s = s + "                if (className.equals(\"java.math.BigInteger\")) retVal = true;\n";
                s = s + "                if (className.equals(\"javax.xml.datatype.Duration\")) retVal = true;\n";
                s = s + "                if (className.equals(\"javax.xml.datatype.XMLGregorianCalendar\")) retVal = true;\n";
                s = s + "                if (className.equals(\"com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl\")) retVal = true;\n";
                s = s + "                if (className.equals(\"javax.xml.namespace.QName\")) retVal = true;\n";
                s = s + "                if (className.equals(\"[B\")) retVal = true;\n";
                s = s + "\n";
                s = s + "            // Success!\n";
                s = s + "                return retVal;\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "    // Test Functions\n";
                s = s + "        public void testCodeGeneration() {\n";
                s = s + "            // Variables\n";
                s = s + "                String GeneratedXML;\n";
                s = s + "\n";
                s = s + "            // Test\n";
                s = s + "                try {\n";
                s = s + "                    GeneratedXML = " + generatedClassName + ".generateXML();\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    // Failure\n";
                s = s + "                        fail(E.toString());\n";
                s = s + "                }\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "        public void testUnmarshalOfGeneratedCode() {\n";
                s = s + "            // Variables\n";
                s = s + "                JAXBContext jaxbCtx;\n";
                s = s + "                Object jaxbObject;\n";
                s = s + "                Unmarshaller unmarshaller;\n";
                s = s + "                String GeneratedXML;\n";
                s = s + "\n";
                s = s + "            // Test\n";
                s = s + "                try {\n";
                s = s + "                    // GenerateCode\n";
                s = s + "                        GeneratedXML = " + generatedClassName + ".generateXML();\n";
                s = s + "\n";
                s = s + "                    // Unmarshal\n";
                s = s + "                        jaxbCtx = JAXBContext.newInstance(" + obj.getClass().getName() + ".class.getPackage().getName());\n";
                s = s + "                        unmarshaller = jaxbCtx.createUnmarshaller();\n";
                s = s + "                        jaxbObject = unmarshaller.unmarshal(new ByteArrayInputStream(GeneratedXML.getBytes()));\n";
                s = s + "\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    // Failure\n";
                s = s + "                        fail(E.toString());\n";
                s = s + "                }\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "        public void testCorrectnessOfGeneratedCode() {\n";
                s = s + "            // Variables\n";
                s = s + "                JAXBContext jaxbCtx;\n";
                s = s + "                Object jaxbObjectGenerated;\n";
                s = s + "                Object jaxbObjectOriginal;\n";
                s = s + "                Unmarshaller unmarshaller;\n";                
                s = s + "                String xml;\n";
                s = s + "\n";
                s = s + "            // Original XML\n";
                s = s + "                xml = \"\";\n";
                s = s + "                xml = xml + \"\";\n";
                sa = xml.split("\n");
                for (int i = 0; i < sa.length; i++) {
                    stemp = sa[i];
                    stemp = stemp.replaceAll("\r", "");
                    stemp = stemp.replaceAll("\n", "\\n");
                    stemp = stemp.replaceAll("\"", "\\\\\"");
                    s = s + "                xml = xml + \"" + stemp + "\\n\";\n";
                }                
                s = s + "\n";
                s = s + "            // Perform Test\n";
                s = s + "                try {\n";
                s = s + "                    // Unmarshal init\n";
                s = s + "                        jaxbCtx = JAXBContext.newInstance(" + obj.getClass().getName() + ".class.getPackage().getName());\n";
                s = s + "                        unmarshaller = jaxbCtx.createUnmarshaller();\n";
                s = s + "\n";
                s = s + "                    // Unmarshal original XML\n";
                s = s + "                        jaxbObjectGenerated = unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));\n";
                s = s + "                        if (jaxbObjectGenerated instanceof javax.xml.bind.JAXBElement) {\n";
                s = s + "                            jaxbObjectGenerated = ((javax.xml.bind.JAXBElement) jaxbObjectGenerated).getValue();\n";
                s = s + "                        }\n";
                s = s + "\n";
                s = s + "                    // Unmarshal generated XML\n";
                s = s + "                        jaxbObjectOriginal = unmarshaller.unmarshal(new ByteArrayInputStream(" + generatedClassName + ".generateXML().getBytes()));\n";
                s = s + "                        if (jaxbObjectOriginal instanceof javax.xml.bind.JAXBElement) {\n";
                s = s + "                            jaxbObjectOriginal = ((javax.xml.bind.JAXBElement) jaxbObjectOriginal).getValue();\n";
                s = s + "                        }\n";
                s = s + "\n";
                s = s + "                    // Compare\n";
                s = s + "                        if (!compareObjects(jaxbObjectGenerated, jaxbObjectOriginal)) {\n";
                s = s + "                            fail(\"Generated XML and Original XML do not match!\");\n";
                s = s + "                        }\n";
                s = s + "                } catch (Exception E) {\n";
                s = s + "                    // Failure\n";
                s = s + "                        fail(E.toString());\n";
                s = s + "                }\n";
                s = s + "        }\n";
                s = s + "}\n";
                
            // Success!
                return s;
        }        
        
        
        public String generateTestSuite(XSD_DOMNode root, String generatedClassName, Object obj) throws CodeGenException {
            // Variables
                String s;
                
            // Generate the code
                s = "";        
                s = s + "import junit.framework.Test;\n";
                s = s + "import junit.framework.TestSuite;\n";
                s = s + "\n";
                s = s + "public class " + generatedClassName + "_TestSuite {\n";
                s = s + "// Public functions\n";
                s = s + "        public static Test suite() {\n";
                s = s + "            // Variables\n";
                s = s + "                TestSuite suite = new TestSuite();\n";
                s = s + "\n";
                s = s + "            // Initialize Suite\n";
                s = s + "                suite.addTestSuite(" + generatedClassName + "_TestCase.class);\n";
                s = s + "\n";
                s = s + "            // Return suite\n";
                s = s + "                return suite;\n";
                s = s + "        }\n";
                s = s + "\n";
                s = s + "    // Main Function\n";
                s = s + "        public static void main(String[] args) {\n";
                s = s + "            // Run the suite\n";
                s = s + "                junit.textui.TestRunner.run(suite());\n";
                s = s + "        }\n";
                s = s + "}\n";
                
            // Success!
                return s;
        }

}

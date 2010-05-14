package org.netbeans.modules.soa.palette.java.constructs;

import java.lang.reflect.Method;

/**
 *
 * @author lyu
 */
public class Utilities {
        /**
         * Generates code to access a child node or attribute of a node
         * For example, if we have a property named "AAA" and we want to get the
         * getter for the property, we would pass in a prefix of "get" and a
         * propName of "AAA" and this function will search through the methods
         * defined in jaxbNodeclass for a method which matches "getAAA" but the
         * match is done with case insensitivity since Jaxb may have mangled the
         * casing of the function name.
         * 
         * @param jaxbNodeClass  Jaxb generated class for the node
         * @param prefix         Accessor prefix, i.e get or set
         * @param propName       Name of the child node or attribute to access
         * 
         * @return               Function name used to access the property.
         * 
         * @throws NoSuchMethodException
         */
        public static String getAccessorFunctionName(Class jaxbNodeclass, String prefix, String propName) throws NoSuchMethodException {
            // Variables
                Method[] ma;
                String normalizedPropName;
                String s;
                
            // Strip off any strange characters from the propName
                normalizedPropName = "";
                for (int i = 0; i < propName.length(); i++) {
                    s = propName.substring(i, i + 1);
                    if (s.indexOf("_") < 0) {
                        normalizedPropName = normalizedPropName + s;
                    }
                }                
                
            // Search through the methods
                ma = jaxbNodeclass.getMethods();
                for (int i = 0; i < ma.length; i++) {
                    if (ma[i].getName().equalsIgnoreCase(prefix + normalizedPropName)) {
                        return ma[i].getName();
                    }
                }
                        
            // Error!
                throw new NoSuchMethodException("Could not find accessor for '" + prefix + normalizedPropName + "' [" + propName + "] in class '" + jaxbNodeclass.getName() + "'");
        }        

        /**
         * Codegens a default value for a variable type.  For example if type is java.lang.string then this function
         * would return "".  If type is java.lang.Float then this function would return (float) 0.
         * 
         * @param type  String representation of the type of variable to generate the default value for
         * 
         * @return      Code for a default value of the variable type.
         * 
         * @throws IllegalArgumentException
         */
        public static String generateDefaultValue(String type) throws IllegalArgumentException {
            // Return default values
                if (type.equals("java.lang.Boolean")) {
                    return "false";
                }
                if (type.equals("java.lang.Byte")) {
                    return "(byte) 0";
                }
                if (type.equals("java.lang.Double")) {
                    return "(double) 0";
                }
                if (type.equals("java.lang.Float")) {
                    return "(float) 0";
                }
                if (type.equals("java.lang.Integer")) {
                    return "0";
                }
                if (type.equals("java.lang.Long")) {
                    return "(long) 0";
                }
                if (type.equals("java.lang.Object")) {
                    return "null";
                }
                if (type.equals("java.lang.Short")) {
                    return "(short) 0";
                }
                if (type.equals("java.lang.String")) {
                    return "\"\"";
                }
                if (type.equals("java.math.BigDecimal")) {                       
                    return "new BigDecimal(\"0\")";
                }
                if (type.equals("java.math.BigInteger")) {
                    return "new BigInteger(\"0\")";
                }
                if (type.equals("javax.xml.datatype.Duration")) {
                    return "datatypefactory.newDuration(0)";
                }
                if (type.equals("javax.xml.datatype.XMLGregorianCalendar")) {
                    return "datatypefactory.newXMLGregorianCalendar()";
                }
                if (type.equals("javax.xml.namespace.QName")) {
                    return "new QName(\"\")";
                }
                if (type.equals("[B")) {
                    return "new BigInteger(\"0\").toByteArray()";                    
                }
                
            // Error!
                throw new IllegalArgumentException("Cannot generate default value for '" + type + "'");
        }
        
        /**
         * This function will take a spring, split it along newlines, then indent
         * each line by the amount specified in the indent parameter and then
         * reassemble the contents back into one string.  This function is useful
         * for indenting blocks of code.
         * 
         * @param s      String to indent
         * @param indent Amount to indent.  For example to indent by 3 spaces
         *               pass in "   " for this parameter.
         * 
         * @return       The indented string
         */
        public static String indent(String s, String indent) {
            // Variables
                String[] sa;
                
            // Split the string based on newlines
                sa = s.split("\r\n|\r|\n");
                
            // Indent the string
                s = "";
                for (int i = 0; i < sa.length; i++) {
                    s = s + indent + sa[i] + "\r\n";
                }
                
            // Success!
                return s;
        }
        
        /**
         * This function returns true if the class named by the className is 
         * considered a primitive type.  For example a className of "java.lang.String"
         * would return true since a String is considered a primitive type, while
         * a className of "com.stc.jaxbgen.schema2.Z" would return false since
         * that class is not considered a java primitive type.
         * 
         * @param className  Name of the class to check
         * 
         * @return           True if the name of the class repesents a 
         *                   primitive type, false otherwise.
         */
        public static boolean isPrimitiveType(String className) {
            // Variables
                boolean retVal;
                
            // Check if primitive type
                retVal = false;
                if (className.equals("java.lang.Boolean")) retVal = true;
                if (className.equals("java.lang.Byte")) retVal = true;
                if (className.equals("java.lang.Double")) retVal = true;
                if (className.equals("java.lang.Float")) retVal = true;
                if (className.equals("java.lang.Integer")) retVal = true;
                if (className.equals("java.lang.Long")) retVal = true;
                if (className.equals("java.lang.Object")) retVal = true;
                if (className.equals("java.lang.Short")) retVal = true;
                if (className.equals("java.lang.String")) retVal = true;
                if (className.equals("java.math.BigDecimal")) retVal = true;
                if (className.equals("java.math.BigInteger")) retVal = true;
                if (className.equals("javax.xml.datatype.Duration")) retVal = true;
                if (className.equals("javax.xml.datatype.XMLGregorianCalendar")) retVal = true;
                if (className.equals("javax.xml.namespace.QName")) retVal = true;
                if (className.equals("[B")) retVal = true;
                
            // Success!
                return retVal;            
        }

        /**
         * This function codegens code to turn a string value into a safely typecast
         * value of the type specified by the type parameter.  For example if type
         * is "java.lang.Boolean" and value is "TRUE", this function will codegen
         * 'Boolean.valueOf("TRUE")'
         * 
         * @param type  Type of variable to safely cast to
         * @param value String value to cast from
         * @return      Generated code
         * @throws java.lang.ClassCastException
         */
        public static String StringToInstance(String type, String value) throws ClassCastException {
            
            // Clean up the newlines in the value 
                value = value.replaceAll("\n", "\\\\n");
                value = value.replaceAll("\r", "\\\\r");
                        
            // Try to do a safe conversion
                if (type.equals("java.lang.Boolean")) {
                    return "Boolean.valueOf(\"" + value + "\")";
                }
                if (type.equals("java.lang.Byte")) {
                    return "Byte.valueOf(\"" + value + "\")";
                }
                if (type.equals("java.lang.Double")) {
                    return "Double.valueOf(\"" + value + "\")";
                }
                if (type.equals("java.lang.Float")) {
                    return "Float.valueOf(\"" + value + "\")";
                }
                if (type.equals("java.lang.Integer")) {
                    return "Integer.valueOf(\"" + value + "\")";
                }
                if (type.equals("java.lang.Long")) {
                    return "(long) 0";
                }
                if (type.equals("java.lang.Object")) {                            
                    return "\"" + value + "\"";
                }
                if (type.equals("java.lang.Short")) {
                    return "Short.valueOf(\"" + value + "\")";
                }
                if (type.equals("java.lang.String")) {
                    return "\"" + value + "\"";
                }
                if (type.equals("java.math.BigDecimal")) {                       
                    return "new BigDecimal(\"" + value + "\")";
                }
                if (type.equals("java.math.BigInteger")) {
                    return "new BigInteger(\"" + value + "\")";
                }
                if (type.equals("javax.xml.datatype.Duration")) {
                    return "DatatypeFactory.newInstance().newDuration(\"" + value + "\")";
                }
                if (type.equals("javax.xml.datatype.XMLGregorianCalendar")) {
                    return "DatatypeFactory.newInstance().newXMLGregorianCalendar(\"" + value + "\")";
                }
                if (type.equals("javax.xml.namespace.QName")) {
                    return "new QName(\"" + value + "\")";
                }
                if (type.equals("[B")) {
                    return "BigInteger.valueOf((\"" + value + "\").toByteArray()";                    
                }
            
            // Error!
                throw new ClassCastException("Cannot convert \"" + value + "\" to type \"" + type + "\"");
        }
}

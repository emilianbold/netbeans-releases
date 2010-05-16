package org.netbeans.modules.soa.palette.java.constructs;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 *
 * @author lyu
 */
public class CodeGen {
    // Inner Classes
        /**
         * Exception class which codegen may throw
         */
        public class CodeGenException extends Exception {
            public CodeGenException(String msg) {
                super(msg);
            }
        }

    // Constants
    // private final static String REPEATING_TODO_TAG = "_TODO_1";
        private final static String REPEATING_TODO_TAG = "1";  //NOI18N

    // Member Variables
        private Stack<String> mAccessorPrefixStack;
        private Stack<Class> parentClasses;
        private ConstructsProgressReporter reporter = null;
        

    // Private Functions
        /**
         * Generates code for a repeating node.
         *
         * @param node                     node to generate code for
         * @param nestedRepeatingNodeLevel How deep of a nesting level of repeating
         *                                 nodes this node is in.  This information
         *                                 is used to generate index numbers for
         *                                 for loop variables.
         * @param obj                      Instance object of an unmarshalled jaxb
         *                                 object to extract value data from
         *
         * @return                         Generated code
         *
         * @throws org.netbeans.modules.soabi.palette.java.constructs.CodeGen.CodeGenException
         * @throws java.lang.NoSuchMethodException
         */
        private String generateCodeForRepeatingNode(XSD_DOMNode node, 
                int nestedRepeatingNodeLevel, Object obj, String indentTabs) throws CodeGenException, NoSuchMethodException {
            // Variables
                String className;
                List list;
                String loopVarName1;
                String loopVarName2;
                Object retVal;
                String s;
                String s2;
                boolean currentNodeSameAsAncestor = false;

            // Get classname and varName
                className = node.getNodeClass().getName().replace('$', '.'); //NOI18N

            // Check if this is a primitive
                if (Utilities.isPrimitiveType(className)) {
                    return generateCodeForRepeatingPrimitiveNode(node, 
                            nestedRepeatingNodeLevel, obj, indentTabs + "\t");
                }

            // Generate comments for the fixed attributes
                s = ""; //NOI18N
                s = s + indentTabs + "// " + node.getElementName() + "\n"; //NOI18N

                if (!parentClasses.contains(node.getNodeClass())){
                    parentClasses.push(node.getNodeClass());
                } else {
                    s = s + indentTabs + "// Class is same as on of the ancestors node class, child class/properties will not be set. \n"; //NOI18N
                    currentNodeSameAsAncestor = true;
                }
                
            // Loop through the instances available
                list = (List) obj;
                if (list == null) {
                    list = new Vector();
                    list.add(""); //NOI18N
                }
                for (int i = 0; i < list.size(); i++) {
                    // Generate code for the loop
                        loopVarName1 = "i" + nestedRepeatingNodeLevel; //NOI18N
                        loopVarName2 = "j" + nestedRepeatingNodeLevel; //NOI18N
                        s = s + indentTabs + "for (int " + loopVarName1 + " = " + mAccessorPrefixStack.peek() + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "get", node.getName()) + "().size(), " + loopVarName2 + " = 0" + "; " + loopVarName2 + " < " + REPEATING_TODO_TAG + "; " + loopVarName1 + "++, " + loopVarName2 + "++) {\n"; //NOI18N
                        s = s +  indentTabs + "\t" + mAccessorPrefixStack.peek() + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "get", node.getName()) + "().add(new " + className + "());\n"; //NOI18N
                        s = s + " \n"; //NOI18N

                    // Generate code to add an instance of the repeating node
                        s2 = mAccessorPrefixStack.peek() + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "get", node.getName()) + "().get(" + loopVarName1 + ")"; //NOI18N
                        mAccessorPrefixStack.push(s2);

                        if (( obj == null) && (currentNodeSameAsAncestor)){
                            s = s +  indentTabs + "}\n"; //NOI18N
                            s = s + " \n"; //NOI18N
                            mAccessorPrefixStack.pop();
                            break; // do not proceed with infinite loop.
                        } else {
                            // Generate code for the children
                            for (int j = 0; j < node.getChildCount(); j++) {
                                // Try to get the child from the obj
                                    retVal = null;
                                    try {
                                        String funcName = Utilities.getAccessorFunctionName(node.getNodeClass(), "get", node.getChild(j).getName()); //NOI18N
                                        retVal = node.getNodeClass().getMethod(funcName).invoke(list.get(i));
                                    } catch (Exception E) {
                                        retVal = null;
                                    }

                                // Generate code for the child
                                    if (( retVal != null) || (obj == null)) {
                                        s = s + generateCodeForNode(node.getChild(j), nestedRepeatingNodeLevel + 1, retVal, indentTabs + "\t"); //NOI18N
                                    }
                            }
                            s = s +  indentTabs + "}\n"; //NOI18N
                            s = s + " \n"; //NOI18N

                            // Cleanup
                            mAccessorPrefixStack.pop();
                        }
                }
                
                if (!currentNodeSameAsAncestor){
                    parentClasses.pop();
                }                                

                this.reporter.processedNode();
                // Success!
                return s;
        }

        /**
         * Generates code for a repeating primitive node, usually a field or a value.
         *
         * @param node                     node to generate code for
         * @param nestedRepeatingNodeLevel How deep of a nesting level of repeating
         *                                 nodes this node is in.  This information
         *                                 is used to generate index numbers for
         *                                 for loop variables.
         * @param obj                      Instance object of an unmarshalled jaxb
         *                                 object to extract value data from
         *
         * @return                         Generated code
         *
         * @throws org.netbeans.modules.soabi.palette.java.constructs.CodeGen.CodeGenException
         * @throws java.lang.NoSuchMethodException
         */
        private String generateCodeForRepeatingPrimitiveNode(XSD_DOMNode node, 
                int nestedRepeatingNodeLevel, 
                Object obj, String indentTabs) throws CodeGenException, NoSuchMethodException {
            // Variables
                String className;
                List list;
                String loopVarName1;
                String loopVarName2;
                String s;

            // Get classname and varName
                className = node.getNodeClass().getName().replace('$', '.'); //NOI18N

            // Generate comments for the fixed attributes
                s = ""; //NOI18N
                s = s + indentTabs + "// " + node.getElementName() + "\n"; //NOI18N

            // Loop through the instances available
                list = (List) obj;
                if (list == null) {
                    list = new Vector();
                    list.add(""); //NOI18N
                }
                for (int i = 0; i < list.size(); i++) {
                    // Generate code for the loop
                        loopVarName1 = "i" + nestedRepeatingNodeLevel; //NOI18N
                        loopVarName2 = "j" + nestedRepeatingNodeLevel; //NOI18N
                        s = s + indentTabs + "for (int " + loopVarName1 + " = " + mAccessorPrefixStack.peek() + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "get", node.getName()) + "().size(), " + loopVarName2 + " = 0" + "; " + loopVarName2 + " < " + REPEATING_TODO_TAG + "; " + loopVarName1 + "++, " + loopVarName2 + "++) {\n"; //NOI18N
                        s = s + indentTabs + "\t" + mAccessorPrefixStack.peek() + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "get", node.getName()) + "().add(new " + className + "(\"" + list.get(i).toString() + "\"));\n"; //NOI18N
                        s = s + indentTabs + "}\n"; //NOI18N
                        s = s + " \n"; //NOI18N
                }

                this.reporter.processedNode();
                // Success!
                return s;
        }

        /**
         * Generates code for the instantiation and default initialization of the jaxb generated classes which
         * represent the node passed in through parameter node.  The return code will also contain code to
         * instantiate and initialize all the children of this node.
         *
         * @param node  The node to generate code for
         *
         * @param nestedRepeatingNodeLevel  The number of nested levels of repeating nodes above this node.
         *
         * @return  Generated code which instantiates and initializes this node and it's children.
         *
         * @throws mapperprototype.CodeGen.CodeGenException
         */
        protected String generateCodeForNode(XSD_DOMNode node, 
                int nestedRepeatingNodeLevel, 
                Object obj,
                String indentTabs) throws CodeGenException, NoSuchMethodException {
            // Variables
                String className;
                Object retVal;
                String s;
                String s2;
                String varName;
                boolean currentNodeSameAsAncestor = false;
            // Check for repeating node
                if (node.isRepeating()) {
                    return generateCodeForRepeatingNode(node, nestedRepeatingNodeLevel, obj, indentTabs + "\t");
                }

            // Check if this node is a primitive type (those savages!)
                if (Utilities.isPrimitiveType(node.getNodeClass().getName())) {
                    // Super hack to get around untyped elements which mysteriously show up as ElementNSImpl
                        if (obj instanceof com.sun.org.apache.xerces.internal.dom.ElementNSImpl) {
                            obj = ((com.sun.org.apache.xerces.internal.dom.ElementNSImpl) obj).getFirstChild();
                            if (obj instanceof com.sun.org.apache.xerces.internal.dom.TextImpl) {
                                obj = ((com.sun.org.apache.xerces.internal.dom.TextImpl) obj).getData();
                            }
                        }

                    // Safety Check
                        if (obj == null) {
                            obj = ""; //NOI18N
                        }

                    // Generate code for the primitive type
                        s = ""; //NOI18N
                        s = s + indentTabs + "// " + node.getName() + "\n"; //NOI18N
                        s = s + indentTabs + mAccessorPrefixStack.peek().toString();
                        s = s + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "set", node.getName()); //NOI18N
                        s = s + "(" + Utilities.StringToInstance(node.getNodeClass().getName(), obj.toString()) + ");\n"; //NOI18N
                        s = s + "\n"; //NOI18N
                        this.reporter.processedNode();
                        return s;
                }

            // Get classname and varName
                className = node.getNodeClass().getName().replace('$', '.'); //NOI18N
                varName = node.getName().toLowerCase();

            // Special Case for enumeration
                if (node.isEnum()) {
                    s = ""; //NOI18N
                    s = s + indentTabs + "// " + node.getElementName() + "\n"; //NOI18N
                    s = s + indentTabs + mAccessorPrefixStack.peek() + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "set", node.getName()) + "(" + className + ".valueOf(\"" + node.getNodeClass().getFields()[0].getName() + "\"));\n"; //NOI18N
                    this.reporter.processedNode();                    
                    return s;
                }

            // Generate code to create an instance of this node
                s = ""; //NOI18N
                s = s + indentTabs + "// " + node.getElementName() + "\n"; //NOI18N

                if (!parentClasses.contains(node.getNodeClass())){
                    parentClasses.push(node.getNodeClass());
                } else {
                    s = s + indentTabs + "// Class is same as on of the ancestors node class, child class/properties will not be set. \n"; //NOI18N
                    currentNodeSameAsAncestor = true;
                }
                
                if (!mAccessorPrefixStack.empty()) {
                    s = s + indentTabs + mAccessorPrefixStack.peek() + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "set", node.getName()) + "(new " + className + "());\n"; //NOI18N
                    s2 = mAccessorPrefixStack.peek() + "." + Utilities.getAccessorFunctionName(node.getParent().getNodeClass(), "get", node.getName()) + "()"; //NOI18N
                } else {
                    s2 = varName;
                    s = s + indentTabs + className + " " + varName + " = new " + className + "();\n"; //NOI18N
                }
                mAccessorPrefixStack.push(s2);

                s = s + " \n"; //NOI18N

                if (!currentNodeSameAsAncestor){
                // Generate code for all the children
                    for (int i = 0; i < node.getChildCount(); i++) {
                        // Try to get the child from the obj
                            retVal = null;
                            try {
                                String funcName = Utilities.getAccessorFunctionName(node.getNodeClass(), "get", node.getChild(i).getName()); //NOI18N
                                retVal = node.getNodeClass().getMethod(funcName).invoke(obj);

                            } catch (Exception E) {
                                retVal = null;
                            }

                        // Generate code for the children
                            if (( retVal != null) || (obj == null)) {
                                s = s + generateCodeForNode(node.getChild(i), nestedRepeatingNodeLevel, retVal, indentTabs + "\t");
                            }
                    }
                }

            // Cleanup
                mAccessorPrefixStack.pop();

                if (!currentNodeSameAsAncestor){
                    parentClasses.pop();
                }                
                
                this.reporter.processedNode();                                
                // Success!
                return s;
        }

        /**
         * Generates code for all the necessary java imports needed to compile the code generated
         * by the other code generation functions.
         *
         * @return  Generated import statements.
         */
        protected List<String> generateImportStatements() {
            List<String> ret = new ArrayList<String>();
            ret.add("com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl"); //NOI18N
            ret.add("java.io.ByteArrayInputStream"); //NOI18N
            ret.add("java.io.ByteArrayOutputStream"); //NOI18N
            ret.add("java.math.BigDecimal"); //NOI18N
            ret.add("java.math.BigInteger"); //NOI18N
            ret.add("javax.xml.bind.JAXBContext"); //NOI18N
            ret.add("javax.xml.bind.JAXBElement"); //NOI18N
            ret.add("javax.xml.bind.Marshaller"); //NOI18N
            ret.add("javax.xml.bind.Unmarshaller"); //NOI18N
            ret.add("javax.xml.datatype.DatatypeFactory"); //NOI18N
            ret.add("javax.xml.namespace.QName"); //NOI18N
            return ret;
        }

    // Constructors
        public CodeGen(ConstructsProgressReporter rep) {
            this.reporter = rep;
            clear();
        }

    // Public functions
        public void clear() {
            // Clear the member variables
                mAccessorPrefixStack = new Stack<String>();
                parentClasses = new Stack<Class>();
        }
}

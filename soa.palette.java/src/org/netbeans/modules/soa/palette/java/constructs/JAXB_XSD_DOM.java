package org.netbeans.modules.soa.palette.java.constructs;

import java.text.ParseException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lyu
 */
public class JAXB_XSD_DOM {
    // Member Variables
        private XSD_DOMNode mRoot;
        private Class mXmlElementClass;
        private Class mXmlEnumClass;
        private Class mXmlRootElementClass;
        private Class mXmlTypeClass;
        private JAXBClassParseContext classParserCtx;
    
    // Constructor
        /**
         * Default Constructor for this class
         */
        public JAXB_XSD_DOM() {
            mRoot = null;
        }

    public JAXBClassParseContext getClassParserCtx() {
        return classParserCtx;
    }

    public void setClassParserCtx(JAXBClassParseContext classParserCtx) {
        this.classParserCtx = classParserCtx;
    }
        
        
    // Private Functions
        /**
         * Helper function to invoke a method on an instance of a class.  The args
         * should contain an alternating list of class types and actual arguments.
         * For example if the method to be invoked takes two parameters String and
         * Integer the args list would look like { String.class, "Hello World",
         * Integer.class, 5 }
         * 
         * @param c          class which contains the method to invoke
         * @param obj        instance of the class to invoke the method on
         * @param methodName name of the method to invoke
         * @param args       variable length argument list of arguments to invoke the
         *                   method with.  The list should alternate between class types
         *                   and actual parameters.
         * 
         * @return           whatever is returned by the invoked method
         * 
         * @throws java.lang.NoSuchMethodException
         * @throws java.lang.IllegalAccessException
         * @throws java.lang.reflect.InvocationTargetException
         */
        private Object invoke(Class c, Object obj, String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            // Variables
                Class[] cpa;
                Method m;
                Object[] opa;

            // Build the class parameter array
                cpa = new Class[args.length / 2];
                for (int i = 0; i < args.length; i = i + 2) {
                    cpa[i / 2] =(Class) args[i];
                }

            // Build the object parameter array
                opa = new Object[args.length / 2];
                for (int i = 0; i < args.length; i = i + 2) {
                    opa[(i / 2)] = args[i + 1];
                }

            // Find the method
                m = c.getMethod(methodName, cpa);

            // Invoke
                return m.invoke(obj, opa);
        }
        
        /**
         * This function takes a jaxbClass and parses the contents into an
         * XSD_DOMNode.  The XSD_DOMNode can then be used the codegen to generate
         * code for the jaxbClass.  This function will recursively travel down
         * the children of the jaxbClass to build a XSD_DOMNode tree.
         * 
         * @param node      XSD_DOMNode to parse this jaxbClass information into  
         * @param jaxbClass The jaxbClass for which to extract the information from.
         * 
         * @throws java.text.ParseException
         */
        private void parseClass(XSD_DOMNode node, Class jaxbClass, 
                JAXBClassParseContext ctx) throws ParseException {            
            // Variables
                Field f;
                Field[] fa;
                String[] sa;
                Object xmlenum;
                Object xmltype;
                List<Field> parsedFields = new ArrayList<Field>();

            // Handle Primitive Types
                if (Utilities.isPrimitiveType(jaxbClass.getName())) {
                    node.setNodeClass(jaxbClass);
                    ctx.setPrimitiveClassUsed(jaxbClass);
                    return;                    
                }

            // Handle super classes
                if ((!jaxbClass.getSuperclass().equals(Object.class)) &&
                    (!jaxbClass.getSuperclass().equals(java.lang.Enum.class))) {
                    parseClass(node, jaxbClass.getSuperclass(), ctx);
                }
                
            // Save the type of this class
                node.setNodeClass(jaxbClass);
                ctx.setParsedJAXB_XSD_DOM(jaxbClass, node);
                
            // Check if this class is an enumeration
                xmlenum = jaxbClass.getAnnotation(mXmlEnumClass);
                if (xmlenum != null) {
                    node.setIsEnum(true);
                    return;
                }
                
            // Retrieve the XML annotation for this class
                xmltype = jaxbClass.getAnnotation(mXmlTypeClass);
                
            // Loop through the props
                try {
                    sa = (String[]) invoke(mXmlTypeClass, xmltype, "propOrder");                    
                } catch (Exception E) {
                    sa = new String[0];
                }                        
                for (int i = 0; i < sa.length; i++) {
                    // Retrieve the field for this prop
                        try {
                            f = jaxbClass.getDeclaredField(sa[i]);
                        } catch (NoSuchFieldException E) {
                            continue;
                        }
                        
                    // Parse the field
                        parseField(node, f, ctx);
                        
                    // Add the field to the parsed list
                        parsedFields.add(f);                        
                }
                
             // Loop through the rest of the fields
                fa = jaxbClass.getDeclaredFields();
                for (int i = 0; i < fa.length; i++) {
                    // Skip if already parsed
                        if (parsedFields.contains(fa[i])) {
                            continue;
                        }
                    
                    // Parse the field
                        parseField(node, fa[i], ctx);
                }
        }
        
        /**
         * This function will parse the contents of a field into an XSD_DOMNode.
         * 
         * @param node  XSD_DOMNode to place the parsed field information into
         * @param field The field to extract the information from.
         * 
         * @throws java.text.ParseException
         */
        private void parseField(XSD_DOMNode node, Field field, 
                JAXBClassParseContext ctx) throws ParseException {
            // Variables
                XSD_DOMNode childnode;
                XSD_DOMNode prevParsedNode = null;
                Class c;
                String name;
                Type t;
                Object xmlelement;
                boolean parseCls = true;
                        
            // Retreive the name of the field                                            
                name = field.getName();

            // Get the class of this field
                t = field.getGenericType();
                if (t instanceof ParameterizedType) {
                    c = (Class) (((ParameterizedType) t).getActualTypeArguments()[0]);
                } else {
                    c = (Class) t;
                }
                
                prevParsedNode = ctx.getParsedJAXB_XSD_DOM(c);
                if (prevParsedNode != null){
                    childnode = prevParsedNode.deepClone();
                    ctx.addNodeCreated(); // should be more than one
                    parseCls = false;
                } else {
                    // Create a child node
                    childnode = new XSD_DOMNode(); 
                    ctx.addNodeCreated();
                }

                childnode.setName(name);

            // Set the Element Name
                xmlelement = field.getAnnotation(mXmlElementClass);
                if (xmlelement != null) {
                    String s = "";
                    try {
                        s = invoke(mXmlElementClass, xmlelement, "name").toString();                        
                    } catch (Exception E) {
                        System.err.println(E);
                    }                    
                    childnode.setElementName(s);
                }

            // Check if this is a repeating element
                childnode.setIsRepeating(List.class.isAssignableFrom(field.getType()));

            // Parse the child node
                if (parseCls){
                    parseClass(childnode, c, ctx);                    
                }
                childnode.setParent(node);
                node.addChild(childnode);
        }
                
    // Public Functions
        /**
         * Returns the root node of this JAXB_XSD_DOM
         * 
         * @return The root node of this JAXB_XSD_DOM
         */
        public XSD_DOMNode getRootNode() {
            return mRoot;
        }
                
        /**
         * This function will parse the jaxbclass in parameter c into this 
         * JAXB_XSD_DOM.
         * 
         * @param c The jaxbclass to parse
         * 
         * @throws java.text.ParseException
         */
        public void parse(Class c, JAXBClassParseContext ctx) throws ParseException {
            // Variables
                String s;
                Object xmlrootelement;
                Object xmltype;
                
            // Initialize he root element
                mRoot = new XSD_DOMNode();
                ctx.addNodeCreated();
                
            // Safety Block
                try {                
                    // Initialize Class Types
                        mXmlElementClass = c.getClassLoader().loadClass("javax.xml.bind.annotation.XmlElement");
                        mXmlEnumClass = c.getClassLoader().loadClass("javax.xml.bind.annotation.XmlEnum");
                        mXmlRootElementClass = c.getClassLoader().loadClass("javax.xml.bind.annotation.XmlRootElement");
                        mXmlTypeClass = c.getClassLoader().loadClass("javax.xml.bind.annotation.XmlType");

                    // Parse
                        parseClass(mRoot, c, ctx);

                    // Set the root element name
                        xmlrootelement = c.getAnnotation(mXmlRootElementClass);
                        if (xmlrootelement != null) {
                            s = invoke(mXmlRootElementClass, xmlrootelement, "name").toString();
                            mRoot.setName(s);
                            mRoot.setElementName(s);
                        } else {
                            xmltype = c.getAnnotation(mXmlTypeClass);
                            s = invoke(mXmlTypeClass, xmltype, "name").toString();
                            mRoot.setName(s.replaceAll("\\.", ""));
                            mRoot.setElementName(s.replaceAll("\\.", ""));
                        }
                } catch (Exception E) {
                    System.err.println(E);
                }
        } 
}
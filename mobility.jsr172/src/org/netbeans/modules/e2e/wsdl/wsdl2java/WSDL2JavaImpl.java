/*
 * WSDL2JavaImpl.java
 *
 * Created on October 30, 2006, 10:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl.wsdl2java;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.schema.Element;
import org.netbeans.modules.e2e.api.schema.SchemaConstruct;
import org.netbeans.modules.e2e.api.schema.Type;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.BindingOperation;
import org.netbeans.modules.e2e.api.wsdl.Definition;
import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.Part;
import org.netbeans.modules.e2e.api.wsdl.Port;
import org.netbeans.modules.e2e.api.wsdl.PortType;
import org.netbeans.modules.e2e.api.wsdl.Service;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPAddress;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPBinding;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPOperation;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.schema.SchemaConstants;
import org.netbeans.modules.e2e.wsdl.WSDLParser;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Michal Skvor
 */
public class WSDL2JavaImpl implements WSDL2Java {
        
    private WSDLParser wsdlParser;
    private Definition definition;
    
    private Set<QName> usedTypeNames;
    private Set<QName> usedParameterTypes;
            
    private Map<QName, Integer> uniqueTypeName;
    
    private WSDL2Java.Configuration configuration;
    
    /** Creates a new instance of WSDL2JavaImpl */
    public WSDL2JavaImpl( WSDL2Java.Configuration configuration ) {
        this.configuration = configuration;
        
        wsdlParser = new WSDLParser();        
    }

    public void generate() {
        uniqueTypeName = new HashMap();
        
        definition = wsdlParser.parse( configuration.getWSDLFileName());
        
        try {
            validate();

            generateInterfaces();
            generateTypes();
            generateStub();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    public List<ValidationResult> validate() {
        // TODO: Hack
        definition = wsdlParser.parse( configuration.getWSDLFileName());
        return new WSDLValidator( wsdlParser.getValidationResults(), definition ).validate();
    }

    
    private void generateInterfaces() throws Exception {        
        Set<QName> usedTypes = new HashSet();
        
        for( String serviceName : definition.getServices().keySet()) {
            
            File outputDirectoryF = new File( configuration.getOutputDirectory());
            FileObject outputDirectoryFO = FileUtil.toFileObject( FileUtil.normalizeFile( outputDirectoryF ));
            FileObject outputFileDirectoryFO = outputDirectoryFO.getFileObject( configuration.getPackageName().replace( '.', '/' ));           // NOI18N
            FileObject outputFile = outputFileDirectoryFO.getFileObject( serviceName, "java" );
            if( outputFile == null  ) {
                outputFile = outputFileDirectoryFO.createData( serviceName, "java" );
            }
            
            OutputFileFormatter off = new OutputFileFormatter( outputFile );
            
            Service service = definition.getService( serviceName );
            if( configuration.getPackageName() != null && !"".equals( configuration.getPackageName().trim())) {
                off.write( "package " + configuration.getPackageName() + ";" );
            }
            off.write( "\n\n" );
            off.write( "public interface " + serviceName + " extends java.rmi.Remote {\n" );
            
            for( Port port : service.getPorts()) {
                for( ExtensibilityElement ee : port.getExtensibilityElements()) {
                    if( SOAPConstants.ADDRESS.equals( ee.getElementType())) {
                        PortType portType = port.getBinding().getPortType();
                        for( Operation operation : portType.getOperations()) {
                            
                            String operationDocumentation = operation.getDocumentation();
                            if( operationDocumentation == null ) operationDocumentation = "";
                            off.write( "\n/**\n * " + operationDocumentation + "\n*/\n");
                            
                            String messageName = operation.getOutput().getMessage().getName();
                            Message message = definition.getMessage( messageName );
                            for( Part part : message.getParts()) {
                                QName elementName = part.getElementName();
                                QName typeName = part.getTypeName();
                                Type type;
                                Element element = null;
                                if( elementName != null ) {
                                    element = definition.getSchemaHolder().getSchemaElement( elementName );
                                    type = element.getType();
                                } else if( typeName != null ) {
                                    type = definition.getSchemaHolder().getSchemaType( typeName );
                                } else {
                                    System.err.println(" ERROR ");
                                    break;
                                }
                                
                                String javaTypeName = "";
                                boolean isArray = definition.getSchemaHolder().getSchemaElement( elementName ).getMaxOccurs() > 1;
                                if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
                                    javaTypeName = type.getJavaTypeName();
                                } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
                                    if( type.getSubconstructs().size() == 0 ) {
                                        javaTypeName = "void";
                                    } else if( type.getSubconstructs().size() == 1 ) {
                                        SchemaConstruct sc = type.getSubconstructs().get( 0 );
                                        if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                                            Element sce = (Element)sc;
                                            isArray = sce.getMaxOccurs() > 1;
                                            if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                                                if( sce.getMinOccurs() == 0 || sce.isNillable()) {
                                                    javaTypeName = getWrapperTypeName( sce.getType());
                                                } else {
                                                    javaTypeName = sce.getType().getJavaTypeName();
                                                }
                                            } else {
                                                javaTypeName = sce.getName().getLocalPart();
                                            }
                                        }
                                    } else {
                                        javaTypeName = elementName.getLocalPart();
                                    }
                                }
                                
                                off.write( "public " + javaTypeName + ( isArray ? "[] " : " " ));
                                break;
                            }

                            off.write( operation.getName() + "( ");
                            
                            messageName = operation.getInput().getMessage().getName();
                            message = definition.getMessage( messageName );
                            for( Iterator<Part> it = message.getParts().iterator(); it.hasNext(); ) {
                                Part part = it.next();
                                QName elementName = part.getElementName();
                                QName typeName = part.getTypeName();
                                Type type;
                                if( elementName != null ) {
                                    type = definition.getSchemaHolder().getSchemaElement( elementName ).getType();
                                } else if( typeName != null ) {
                                    type = definition.getSchemaHolder().getSchemaType( typeName );
                                } else {
                                    System.err.println(" ERROR ");
                                    break;
                                }
                                
                                if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
                                    off.write( type.getJavaTypeName() + " " + elementName.getLocalPart());
                                } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
                                    if( type.getSubconstructs().size() == 0 ) {
                                    } else {
                                        for( Iterator<SchemaConstruct> scs = type.getSubconstructs().iterator(); scs.hasNext(); ) {
                                            SchemaConstruct sc = scs.next();
                                            if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                                                Element sce = (Element)sc;
                                                if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                                                    String javaTypeName;
                                                    if( sce.getMinOccurs() == 0 || sce.isNillable()) {
                                                        javaTypeName = getWrapperTypeName( sce.getType());
                                                    } else {
                                                        javaTypeName = sce.getType().getJavaTypeName();
                                                    }
                                                    off.write( javaTypeName );
                                                } else {
                                                    off.write( sce.getType().getName().getLocalPart());
                                                    usedTypes.add( sce.getType().getName());
                                                }
                                                off.write(" " + sce.getName().getLocalPart());
                                                if( scs.hasNext()) off.write(", ");
                                            }
                                        }
                                    }
                                }                                
                            }
                            off.write(" ) throws java.rmi.RemoteException;\n");
                        }
                    }
                }
            }
            off.write("\n}\n");
            off.close();
        }        
        
        System.err.println(" --- Used Types --- ");
        usedTypeNames = new HashSet();
        for( QName typeName : usedTypes ) {
            usedTypeNames.addAll( traverseTypes( usedTypes, typeName ));
        }
        
        for( QName typeName : usedTypeNames ) {
            System.err.println(" - " + typeName.getLocalPart());
        }
    }
    
    private String getWrapperTypeName( Type type ) {
        QName typeName = type.getName();
        if( SchemaConstants.TYPE_INT.equals( typeName )) {
            return "Integer";
        } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
            return "Boolean";
        } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
            return "Byte";
        } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
            return "Double";
        } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
            return "Float";
        } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
            return "Long";
        } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
            return "Short";
        } else if( SchemaConstants.TYPE_BASE64_BINARY.equals( typeName )) {
            return "byte[]";
        } else if( SchemaConstants.TYPE_HEX_BINARY.equals( typeName )) {
            return "byte[]";
        } else if( SchemaConstants.TYPE_STRING.equals( typeName )) {
            return "String";
        }
        return type.getName().getLocalPart();
    }
    
    /**
     * Traverses used types by methods and creates complete list of used types
     */
    public Set<QName> traverseTypes( Set<QName> types, QName typeName ) {
        Set<QName> result = new HashSet();
        result.add( typeName );
        
        Type type = definition.getSchemaHolder().getSchemaType( typeName );
        if( type == null ) {
            type = definition.getSchemaHolder().getSchemaElement( typeName ).getType();
        } 
        if( type == null ) {
            System.err.println(" ERROR ");
        }
        if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    Element sce = (Element) sc;
                    if( Type.FLAVOR_SEQUENCE == sce.getType().getFlavor()) {
                        result.add( sce.getType().getName());
                        result.addAll( traverseTypes( types, sce.getType().getName()));
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Generates all used complex types
     */
    private void generateTypes() throws Exception {
        for( QName typeName : usedTypeNames ) {                        
            Type type = definition.getSchemaHolder().getSchemaType( typeName );
            if( type == null ) {
                type = definition.getSchemaHolder().getSchemaElement( typeName ).getType();
            } 
            if( type == null ) {
                System.err.println(" ERROR ");
            }
            File outputDirectoryF = new File( configuration.getOutputDirectory());
            FileObject outputDirectoryFO = FileUtil.toFileObject( FileUtil.normalizeFile( outputDirectoryF ));
            FileObject outputFileDirectoryFO = outputDirectoryFO.getFileObject( configuration.getPackageName().replace( '.', '/' ));           // NOI18N
            FileObject outputFile = outputFileDirectoryFO.getFileObject( typeName.getLocalPart(), "java" );
            if( outputFile == null  ) {
                outputFile = outputFileDirectoryFO.createData( typeName.getLocalPart(), "java" );
            }
            
            OutputFileFormatter off = new OutputFileFormatter( outputFile );
            
            if( configuration.getPackageName() != null && !"".equals( configuration.getPackageName().trim())) {
                off.write( "package " + configuration.getPackageName() + ";");
            }
            off.write( "\n" );
            off.write( "public class " + typeName.getLocalPart() + " {\n" );
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    Element sce = (Element) sc;
                    String propertyName = sce.getName().getLocalPart();
                    String propertyVariableName = propertyName.substring( 0, 1 ).toLowerCase() + propertyName.substring( 1 );
                    String propertyType = sce.getType().getName().getLocalPart();
                    boolean isArray = sce.getMaxOccurs() > 1;
                    if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                        if( sce.getMinOccurs() == 0 || sce.isNillable()) {
                            propertyType = getWrapperTypeName( sce.getType());
                        } else {
                            propertyType = sce.getType().getJavaTypeName();
                        }
                        //propertyType = sce.getType().getJavaTypeName();
                    }
                    if( WSDL2Java.Configuration.TYPE_JAVA_BEANS == configuration.getGenerateType()) {
                        // Generate code in JavaBeans style
                        
                        off.write( "\n" );
                        off.write( "private " + propertyType + (isArray ? "[] " : " ") + propertyVariableName + ";\n\n" );
                        
                        off.write( "public void " + setter( propertyName ) + "( " + propertyType + (isArray ? "[] " : " ") + propertyVariableName + " ) {\n" );
                        off.write( "this." + propertyVariableName + " = " + propertyVariableName + ";\n" );
                        off.write( "}\n\n" );
                        
                        off.write( "public " + propertyType + (isArray ? "[] " : " ") + getter( propertyName ) + "() {\n" );
                        off.write( "return " + propertyVariableName + ";\n" );
                        off.write( "}\n" );
                    } else if( WSDL2Java.Configuration.TYPE_STRUCTURES == configuration.getGenerateType()) {
                        // Generate code in struct style
                        
                        off.write( "\n" );
                        off.write( "public " + propertyType + (isArray ? "[] " : " ") + propertyVariableName + ";\n" );
                    }
                }
            }
            off.write( "}\n" );
            off.close();
        }
    }
    
    private void generateStub() throws Exception {
        usedParameterTypes = new HashSet();
        Set<QName> operationQNames = new HashSet();
        
        for( String serviceName : definition.getServices().keySet()) {
            
            Service service = definition.getService( serviceName );
            for( Port port : service.getPorts()) {
                for( ExtensibilityElement ee : port.getExtensibilityElements()) {
                    if( SOAPConstants.ADDRESS.equals( ee.getElementType())) {
            
                        SOAPAddress soapAddress = (SOAPAddress) ee;
                        
                        File outputDirectoryF = new File( configuration.getOutputDirectory());
                        FileObject outputDirectoryFO = FileUtil.toFileObject( FileUtil.normalizeFile( outputDirectoryF ));
                        FileObject outputFileDirectoryFO = outputDirectoryFO.getFileObject( configuration.getPackageName().replace( '.', '/' ));           // NOI18N
                        FileObject outputFile = outputFileDirectoryFO.getFileObject( serviceName + "_Stub", "java" );
                        if( outputFile == null ) {
                            outputFile = outputFileDirectoryFO.createData( serviceName + "_Stub", "java" );
                        }

                        OutputFileFormatter off = new OutputFileFormatter( outputFile );

                        if( configuration.getPackageName() != null && !"".equals( configuration.getPackageName().trim())) {
                            off.write( "package " + configuration.getPackageName() + ";\n" );
                        }
                        off.write( "\n" );
                        off.write( "import javax.xml.rpc.JAXRPCException;\n" );
                        off.write( "import javax.xml.namespace.QName;\n" );
                        off.write( "import javax.microedition.xml.rpc.Operation;\n" );
                        off.write( "import javax.microedition.xml.rpc.Type;\n" );
                        off.write( "import javax.microedition.xml.rpc.ComplexType;\n" );
                        off.write( "import javax.microedition.xml.rpc.Element;\n\n" );

                        off.write( "public class " + serviceName + "_Stub implements " + serviceName + ", javax.xml.rpc.Stub {\n" );

                        off.write( "\n" );
                        off.write( "private String[] _propertyNames;\n" );
                        off.write( "private Object[] _propertyValues;\n" );
                        off.write( "\n" );
                        off.write( "public " + serviceName + "_Stub() {\n" );
                        off.write( "_propertyNames = new String[] { ENDPOINT_ADDRESS_PROPERTY };\n" );
                        off.write( "_propertyValues = new Object[] { \"" + soapAddress.getLocationURI() + "\" };\n" );
                        off.write( "}\n\n" );

                        off.write( "public void _setProperty( String name, Object value ) {\n" );
                                off.write( "int size = _propertyNames.length;\n" );
                        off.write( "for (int i = 0; i < size; ++i) {\n" );
                                    off.write( "if( _propertyNames[i].equals( name )) {\n" );
                            off.write( "_propertyValues[i] = value;\n" );
                                    off.write( "return;\n" );
                                    off.write( "}\n" );
                        off.write( "}\n" );

                        off.write( "String[] newPropNames = new String[size + 1];\n" );
                        off.write( "System.arraycopy(_propertyNames, 0, newPropNames, 0, size);\n" );
                        off.write( "_propertyNames = newPropNames;\n" );
                        off.write( "Object[] newPropValues = new Object[size + 1];\n" );
                        off.write( "System.arraycopy(_propertyValues, 0, newPropValues, 0, size);\n" );
                        off.write( "_propertyValues = newPropValues;\n" );
                        off.write( "\n" );
                        off.write( "_propertyNames[size] = name;\n" );
                        off.write( "_propertyValues[size] = value;\n" );
                        off.write( "}\n\n" );

                        off.write( "public Object _getProperty(String name) {\n" );
                        off.write( "for (int i = 0; i < _propertyNames.length; ++i) {\n" );
                                    off.write( "if (_propertyNames[i].equals(name)) {\n" );
                        off.write( "return _propertyValues[i];\n" );
                        off.write( "}\n" );
                        off.write( "}\n" );
                        off.write( "if (ENDPOINT_ADDRESS_PROPERTY.equals(name) || USERNAME_PROPERTY.equals(name) || PASSWORD_PROPERTY.equals(name)) {\n" );
                                    off.write( "return null;\n" );
                        off.write( "}\n" );
                        off.write( "if (SESSION_MAINTAIN_PROPERTY.equals(name)) {\n" );
                                    off.write( "return new java.lang.Boolean(false);\n" );
                        off.write( "}\n" );
                        off.write( "throw new JAXRPCException(\"Stub does not recognize property: \" + name);\n" );
                        off.write( "}\n\n" );

                        off.write( "protected void _prepOperation(Operation op) {\n" );
                        off.write( "for (int i = 0; i < _propertyNames.length; ++i) {\n" );
                                    off.write( "op.setProperty(_propertyNames[i], _propertyValues[i].toString());\n" );
                        off.write( "}\n" );
                        off.write( "}\n" );
            
                        Binding binding = port.getBinding();
                        PortType portType = binding.getPortType();
                        String returnTypeName = "", paramTypeName = "";
                        for( Operation operation : portType.getOperations()) {
                            BindingOperation bindingOperation = binding.getBindingOperation( operation.getName());
                            
                            operationQNames.add( new QName( definition.getTargetNamespace(), operation.getName()));
                            
                            String messageName = operation.getOutput().getMessage().getName();
                            Message message = definition.getMessage( messageName );
                            for( Part part : message.getParts()) {
                                QName elementName = part.getElementName();
                                QName typeName = part.getTypeName();
                                Type type;
                                if( elementName != null ) {
                                    type = definition.getSchemaHolder().getSchemaElement( elementName ).getType();
                                } else if( typeName != null ) {
                                    type = definition.getSchemaHolder().getSchemaType( typeName );
                                } else {
                                    System.err.println(" ERROR ");
                                    break;
                                }
                                
                                String javaTypeName = "";
                                boolean isArray = definition.getSchemaHolder().getSchemaElement( elementName ).getMaxOccurs() > 1;
                                if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
                                    javaTypeName = type.getJavaTypeName();
                                } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
                                    if( type.getSubconstructs().size() == 0 ) {
                                        javaTypeName = "void";
                                    } else if( type.getSubconstructs().size() == 1 ) {
                                        SchemaConstruct sc = type.getSubconstructs().get( 0 );
                                        if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                                            Element sce = (Element)sc;
                                            isArray = sce.getMaxOccurs() > 1;
                                            if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                                                if( sce.getMinOccurs() == 0 || sce.isNillable()) {
                                                    javaTypeName = getWrapperTypeName( sce.getType());
                                                } else {
                                                    javaTypeName = sce.getType().getJavaTypeName();
                                                }
                                                //javaTypeName = sce.getType().getJavaTypeName();
                                            } else {
                                                javaTypeName = sce.getName().getLocalPart();
                                            }
                                        }
                                    } else {
                                        javaTypeName = elementName.getLocalPart();
                                    }
                                }
                                off.write( "\n" );
                                off.write( "public " + javaTypeName + ( isArray ? "[] " : " " ));
                                returnTypeName = elementName.getLocalPart();
                                usedParameterTypes.add( elementName );
                                break;
                            }

                            off.write( operation.getName() + "( ");
                            
                            messageName = operation.getInput().getMessage().getName();
                            message = definition.getMessage( messageName );
                            for( Iterator<Part> it = message.getParts().iterator(); it.hasNext(); ) {
                                Part part = it.next();
                                QName elementName = part.getElementName();
                                QName typeName = part.getTypeName();
                                Type type;
                                if( elementName != null ) {
                                    type = definition.getSchemaHolder().getSchemaElement( elementName ).getType();
                                } else if( typeName != null ) {
                                    type = definition.getSchemaHolder().getSchemaType( typeName );
                                } else {
                                    System.err.println(" ERROR ");
                                    break;
                                }
                                if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
                                    System.err.print( type.getJavaTypeName() + " " + elementName.getLocalPart());
                                } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
                                    for( Iterator<SchemaConstruct> scs = type.getSubconstructs().iterator(); scs.hasNext(); ) {
                                        SchemaConstruct sc = scs.next();
                                        if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                                            Element sce = (Element)sc;
                                            if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                                                String javaTypeName;
                                                if( sce.getMinOccurs() == 0 || sce.isNillable()) {
                                                    javaTypeName = getWrapperTypeName( sce.getType());
                                                } else {
                                                    javaTypeName = sce.getType().getJavaTypeName();
                                                }
                                                off.write( javaTypeName );
                                                //off.write( sce.getType().getJavaTypeName());
                                            } else {
                                                off.write( sce.getName().getLocalPart());
                                            }
                                            off.write( " " + sce.getName().getLocalPart());
                                            if( scs.hasNext()) off.write( ", " );
                                        }
                                    }
                                }
                                paramTypeName = elementName.getLocalPart();
                                usedParameterTypes.add( elementName );
                            }
                            
                            off.write( " ) throws java.rmi.RemoteException {\n" );
                            for( Iterator<Part> it = message.getParts().iterator(); it.hasNext(); ) {
                                Part part = it.next();
                                QName elementName = part.getElementName();
                                QName typeName = part.getTypeName();
                                Type type;
                                if( elementName != null ) {
                                    type = definition.getSchemaHolder().getSchemaElement( elementName ).getType();
                                } else if( typeName != null ) {
                                    type = definition.getSchemaHolder().getSchemaType( typeName );
                                } else {
                                    System.err.println(" ERROR ");
                                    break;
                                }
                                
                                if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
                                    off.write( "Object[] inputObject = new Object[1];\n" );
                                    off.write( "inputObject[0] = " + wrapPrimitiveType( type, elementName.getLocalPart()) + ";\n" );
                                } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
                                    // TODO: first check proper types to not allocate larger field
                                    off.write( "Object[] inputObject = new Object[" + type.getSubconstructs().size() + "];\n" );
                                    int i = 0;
                                    for( Iterator<SchemaConstruct> scs = type.getSubconstructs().iterator(); scs.hasNext(); ) {
                                        SchemaConstruct sc = scs.next();
                                        if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                                            Element sce = (Element)sc;
                                            if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                                                String javaVariable;
                                                if( sce.getMinOccurs() == 0 || sce.isNillable()) {
                                                    javaVariable = sce.getName().getLocalPart();
                                                } else {
                                                    javaVariable = wrapPrimitiveType( sce.getType(), sce.getName().getLocalPart());
                                                }
                                                off.write( "inputObject[" + i + "] = " + javaVariable + ";\n" );
//                                                off.write( "inputObject[" + i + "] = " + wrapPrimitiveType( sce.getType(), sce.getName().getLocalPart()) + ";\n" );
                                            } else {
                                                off.write( "inputObject[" + i + "] = " + sce.getName().getLocalPart() + ";\n" );
                                            }
//                                            System.err.print(" " + sce.getName().getLocalPart());
//                                            if( scs.hasNext()) System.err.print(", ");
                                            i++;
                                        }
                                    }
                                }                                
                                
                                // TODO: create wrapper types
                            }
                            off.write( "\n" );
                            off.write( "Operation op = Operation.newInstance( _qname_" + operation.getName() + ", _type_" + paramTypeName + ", _type_" + returnTypeName + " );\n" );
                            off.write( "_prepOperation( op );\n");
                            off.write( "op.setProperty( Operation.SOAPACTION_URI_PROPERTY, \"" );
                            ExtensibilityElement exe = bindingOperation.getExtensibilityElements().get( 0 );
                            if( exe instanceof SOAPOperation ) {
                                SOAPOperation so = (SOAPOperation) exe;
                                off.write( so.getSoapActionURI());
                            }
                            off.write( "\" );\n" );
                            off.write( "Object resultObj;\n" );
                            off.write( "try {\n" );
                            off.write( "resultObj = op.invoke( inputObject );\n" );
                            off.write( "} catch( JAXRPCException e ) {\n" );
                            off.write( "Throwable cause = e.getLinkedCause();\n" );
                            off.write( "if( cause instanceof java.rmi.RemoteException ) {\n" );
                            off.write( "throw (java.rmi.RemoteException) cause;\n" );
                            off.write( "}\n" );
                            off.write( "throw e;\n" );
                            off.write( "}\n" );
                            off.write( "\n" );
                            
                            messageName = operation.getOutput().getMessage().getName();
                            message = definition.getMessage( messageName );
                            for( Part part : message.getParts()) {
                                QName elementName = part.getElementName();
                                QName typeName = part.getTypeName();
                                Type type;
                                if( elementName != null ) {
                                    type = definition.getSchemaHolder().getSchemaElement( elementName ).getType();
                                } else if( typeName != null ) {
                                    type = definition.getSchemaHolder().getSchemaType( typeName );
                                } else {
                                    System.err.println(" ERROR ");
                                    break;
                                }
                        
                                traverseReturnType( off, "resultObj", "result", definition.getSchemaHolder().getSchemaElement( elementName ), 0 );
                                off.write( "return result;\n" );
                                break;
                            }
                            off.write("}\n");
                        }
                        
                        // Collect Qnames and Elements        
                        Set<QName> qnames = new HashSet( operationQNames );
                        Set<Element> elements = new HashSet();
                        for( QName parameterName : usedParameterTypes ) {
                            SchemaConstruct sc;
                            sc = definition.getSchemaHolder().getSchemaElement( parameterName );
                            if( sc == null ) continue;
                            traverseParameterTypes( sc, qnames, elements );
                        }
                        off.write( "\n" );
                        for( QName q : qnames ) {
                            off.write( "protected static final QName ");
                            off.write( "_qname_" + q.getLocalPart() + " = new QName( " );
                            off.write( '"' + q.getNamespaceURI() + "\", \"" + q.getLocalPart() + "\" );\n" );
                        }

                        for( Element e : elements ) {
                            off.write( "protected static final Element _type_" + e.getName().getLocalPart() + ";\n" );
                        }
                        off.write( "\nstatic {\n" );

                        for( QName parameterName : usedParameterTypes ) {
                            SchemaConstruct sc;
                            sc = definition.getSchemaHolder().getSchemaElement( parameterName );
                            initTypes( off, sc, qnames, elements );
                        }
                        off.write( "}\n\n" );

                        off.write( "private static ComplexType _complexType( Element[] elements ) {\n" );
                            off.write( "ComplexType result = new ComplexType();\n" );
                            off.write( "result.elements = elements;\n" );
                            off.write( "return result;\n" );
                        off.write( "}\n" );   
                        off.write( "}\n" );

                        off.close();                
                    }
                }                    
            }
        }
    }
    
    private String wrapPrimitiveType( Type type, String value ) {
        QName typeName = type.getName();
        if( SchemaConstants.TYPE_INT.equals( typeName )) {
            return "new java.lang.Integer(" + value + ")";
        } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
            return "new java.lang.Boolean(" + value + ")";
        } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
            return "new java.lang.Byte(" + value + ")";
        } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
            return "new java.lang.Double(" + value + ")";
        } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
            return "new java.lang.Float(" + value + ")";
        } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
            return "new java.lang.Long(" + value + ")";
        } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
            return "new java.lang.Short(" + value + ")";
        }
        
        return value;
    }
    
    private String unwrapPrimitiveType( Element element, String value ) {
        Type type = element.getType();
        QName typeName = type.getName();
        String unwrapped;
        if( SchemaConstants.TYPE_INT.equals( typeName )) {
            unwrapped = "(java.lang.Integer)";
        } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
            unwrapped = "(java.lang.Boolean)";
        } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
            unwrapped = "(java.lang.Byte)";
        } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
            unwrapped = "(java.lang.Double)";
        } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
            unwrapped = "(java.lang.Float)";
        } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
            unwrapped = "(java.lang.Long)";
        } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
            unwrapped = "(java.lang.Short)";
        } else {
            unwrapped = "(" + type.getJavaTypeName() + ")";
        }
        unwrapped += value;
        if( element.getMinOccurs() > 0 && !element.isNillable()) {
            if( SchemaConstants.TYPE_INT.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").intValue()";
            } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").booleanValue();";
            } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").byteValue();";
            } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").doubleValue();";
            } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").floatValue();";
            } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").longValue();";
            } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").shortValue();";
            }
        }
        
        return unwrapped;
    }
    
    private String getter( String variable ) {
        return "get" + variable.substring( 0, 1 ).toUpperCase() + variable.substring( 1 );
    }

    private String setter( String variable ) {
        return "set" + variable.substring( 0, 1 ).toUpperCase() + variable.substring( 1 );
    }
    

    private void initTypes( OutputFileFormatter off, SchemaConstruct sc, Set<QName> qnames, Set<Element> elements ) {
        if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
            Element e = (Element) sc;
            Type t = e.getType();
            
            QName elementName = e.getName();
            
            if( Type.FLAVOR_SEQUENCE == e.getType().getFlavor()) {
                String ctName = elementName.getLocalPart();
                                     
                if( elements.contains( e )) {
                    off.write( "_type_" + ctName + " = new Element( " );
                } else {
                    off.write( "new Element( " );
                }
                if( qnames.contains( e.getName())) {
                    off.write( "_qname_" + ctName );
                } else {
                    off.write( "new QName( \"" + e.getName().getNamespaceURI() + "\", \"" + e.getName().getLocalPart() + "\" )" );
                }
                off.write( ", _complexType( new Element[] {\n" );                
                
                for( SchemaConstruct scc : t.getSubconstructs()) {
                    initTypes( off, scc, qnames, elements );
                    off.write( ",\n" );
                }
                off.write( "}))" );
                if( elements.contains( e )) {
                    off.write( ";\n" );
                } 
            } else if( Type.FLAVOR_PRIMITIVE == e.getType().getFlavor()) {
                String eName = e.getName().getLocalPart();
                if( elements.contains( e )) {
                    off.write( "_type_" + eName + " = new Element( " );
                } else {
                    off.write( "new Element( " );
                }
                if( qnames.contains( e.getName())) {
                    off.write( "_qname_" + eName + ", " );
                } else {
                    off.write( "new QName( \"" + e.getName().getNamespaceURI() + "\", \"" + e.getName().getLocalPart() + "\" ), " );
                }
                int minOccurs = e.getMinOccurs();
                int maxOccurs = e.getMinOccurs();
                if( SchemaConstants.TYPE_STRING.equals( t.getName())) {
                    off.write( "Type.STRING" );
                } else if( SchemaConstants.TYPE_INT.equals( t.getName())) {
                    off.write( "Type.INT" );
                } else if( SchemaConstants.TYPE_BOOLEAN.equals( t.getName())) {
                    off.write( "Type.BOOLEAN" );
                } else if( SchemaConstants.TYPE_SHORT.equals( t.getName())) {
                    off.write( "Type.SHORT" );
                } else if( SchemaConstants.TYPE_BYTE.equals( t.getName())) {
                    off.write( "Type.BYTE" );
                } else if( SchemaConstants.TYPE_LONG.equals( t.getName())) {
                    off.write( "Type.LONG" );
                } else if( SchemaConstants.TYPE_FLOAT.equals( t.getName())) {
                    off.write( "Type.FLOAT" );
                } else if( SchemaConstants.TYPE_DOUBLE.equals( t.getName())) {
                    off.write( "Type.DOUBLE" );
                } else if( SchemaConstants.TYPE_BASE64_BINARY.equals( t.getName())) {
                    off.write( "Type.BYTE" );
                    maxOccurs = Element.UNBOUNDED;
                } else if( SchemaConstants.TYPE_HEX_BINARY.equals( t.getName())) {
                    off.write( "Type.BYTE" );
                    maxOccurs = Element.UNBOUNDED;
                } else {
                    System.err.println("Error type");
                }
                off.write( ", " + minOccurs + ", " );
                if( maxOccurs == Element.UNBOUNDED ) {
                    off.write( "Element.UNBOUNDED" );
                } else {
                    off.write( "" + maxOccurs );
                }
                off.write( ", " + e.isNillable() + " )" );
            }
        }
    }
            
    public void traverseParameterTypes( SchemaConstruct sc, Set<QName> qnames, Set<Element> elements ) {
        if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
            Element e = (Element)sc;
            qnames.add( e.getName());
            elements.add( e );
//            if( Type.FLAVOR_SEQUENCE == e.getType().getFlavor()) {
//                for( SchemaConstruct scc : e.getType().getSubconstructs()) {
//                    traverseParameterTypes( scc, qnames, elements );
//                }
//            }
        }
    }
    
    public void traverseReturnType( OutputFileFormatter off, String holderName, String result, Element element, int item ) {
        Type type = element.getType();
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            String javaTypeName;
            if( element.getMinOccurs() == 0 || element.isNillable()) {
                javaTypeName = getWrapperTypeName( element.getType());
            } else {
                javaTypeName = type.getJavaTypeName();
            }
            if( element.getMaxOccurs() > 1 ) {
                off.write( javaTypeName + "[] " + result + ";\n" );
                off.write( "Object " + element.getName().getLocalPart() + "Obj = ((Object[]) " + holderName + ")[" + item + "];\n" );
                off.write( result + " = (" + javaTypeName + "[]) " + element.getName().getLocalPart() + "Obj;\n" );
            } else {
                off.write( javaTypeName + " " + result + ";\n" );
                off.write( "Object " + element.getName().getLocalPart() + "Obj = ((Object[]) " + holderName + ")[" + item + "];\n" );
                off.write( result + " = " + unwrapPrimitiveType( element, element.getName().getLocalPart() + "Obj" ) + ";\n" );
            }
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            if( type.getSubconstructs().size() == 0 ) {
            } else if( type.getSubconstructs().size() == 1 ) {
                SchemaConstruct sc = type.getSubconstructs().get( 0 );
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    Element sce = (Element) sc;
                    traverseReturnType( off, holderName, result, sce, 0 );
                }
            } else {
                if( element.getMaxOccurs() > 1 ) {
                    
                    off.write( element.getName().getLocalPart() + "[] " + result + ";\n" );
                    String resultObj = element.getName().getLocalPart().substring( 0, 1 ).toLowerCase() + element.getName().getLocalPart().substring( 1 );
                    off.write( "Object[] " + resultObj + "Obj = (Object[]) ((Object[]) " + holderName + " ) [" + item + "];\n" );
                    off.write( "if( " + resultObj + "Obj == null ) {\n" );
                    off.write( result + " = null;\n" );
                    off.write( "} else {\n" );
                    off.write( "int " + result + "ArraySize = " + resultObj + "Obj.length;\n" );
                    off.write( result + " = new " + element.getName().getLocalPart() + "[" + result + "ArraySize];\n" );
                    off.write( "for( int " + result + "ArrayIndex = 0; " + result + "ArrayIndex < " + result + "ArraySize; " + result + "ArrayIndex++ ) {\n" );
                    off.write( "if( " + resultObj + "Obj[ " + result + "ArrayIndex ] == null ) {\n" );
                    off.write( result + "[ " + result + "ArrayIndex ] = null;\n" );
                    off.write( "} else {\n" );
                    off.write( result + "[ " + result + "ArrayIndex ] = new " + element.getName().getLocalPart() + "();\n" );
                    
                    int i = 0;
                    for( SchemaConstruct sc : type.getSubconstructs()) {
                        if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                            Element sce = (Element) sc;

                            String variableName = sce.getName().getLocalPart();
                            traverseReturnType( off, resultObj + "Obj[ " + result + "ArrayIndex ]", result + "Obj_" + variableName, sce, i );
                            if( WSDL2Java.Configuration.TYPE_JAVA_BEANS == configuration.getGenerateType()) {
                                off.write( result + "[ " + result + "ArrayIndex ]." + setter( variableName ) + "( " + result + "Obj_" + variableName + " );\n" );
                            } else if( WSDL2Java.Configuration.TYPE_STRUCTURES == configuration.getGenerateType()) {
                                off.write( result + "[ " + result + "ArrayIndex ]." + variableName + " = " + result + "Obj_" + variableName + ";\n" );
                            }
                        }
                        i++;
                    }
                    off.write( "}\n" );
                    off.write( "}\n" );
                    off.write( "}\n" );
                    
                } else {
                    off.write( element.getName().getLocalPart() + " " + result + ";\n" );
                    //off.write( "Object[] " + result + "Obj = (Object[]) ((Object[]) " + result + "Obj) [" + item + "];\n" );
                    off.write( "if( " + result + "Obj == null ) {\n");
                    off.write( result + " = null;\n" );
                    off.write( "} else {\n" );
                    off.write( result + " = new " + element.getName().getLocalPart() + "();\n" );
                    int i = 0;
                    for( SchemaConstruct sc : type.getSubconstructs()) {
                        if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                            Element sce = (Element) sc;
                            String variableName = sce.getName().getLocalPart();
                            traverseReturnType( off, result + "Obj", result + "Obj_" + variableName, sce, i );
                            if( WSDL2Java.Configuration.TYPE_JAVA_BEANS == configuration.getGenerateType()) {
                                off.write( result + "." + setter( variableName ) + "( " + result + "Obj_" + variableName + " );\n" );
                            } else if( WSDL2Java.Configuration.TYPE_STRUCTURES == configuration.getGenerateType()) {
                                off.write( result + "." + variableName + " = " + result + "Obj_" + variableName + ";\n" );
                            }
                        }
                        i++;
                    }
                    off.write( "}\n" );
                }
            }
        }        
    }
    
    public String getUniqueTypeName( QName name ) {
        Integer index = uniqueTypeName.get( name );
        if( index != null ) {
            return name.getLocalPart() + "_" + index;
        }
        int max = 0;
        for( QName q : uniqueTypeName.keySet()) {
            if( q.getLocalPart().equals( name.getLocalPart())) {
                int i = uniqueTypeName.get( q );
                if( i > max ) max = i;
            }
        }
        max++;
        uniqueTypeName.put( name, max );
        return name.getLocalPart() + "_" + max;
    }    
}


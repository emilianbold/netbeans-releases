/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.e2e.wsdl.wsdl2java;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.schema.Element;
import org.netbeans.modules.e2e.api.schema.SchemaConstruct;
import org.netbeans.modules.e2e.api.schema.SchemaConstruct.ConstructType;
import org.netbeans.modules.e2e.api.schema.Type;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.BindingOperation;
import org.netbeans.modules.e2e.api.wsdl.Definition;
import org.netbeans.modules.e2e.api.wsdl.Input;
import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.Output;
import org.netbeans.modules.e2e.api.wsdl.Part;
import org.netbeans.modules.e2e.api.wsdl.Port;
import org.netbeans.modules.e2e.api.wsdl.PortType;
import org.netbeans.modules.e2e.api.wsdl.Service;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPAddress;
import org.netbeans.modules.e2e.api.wsdl.extensions.soap.SOAPOperation;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.schema.SchemaConstants;
import org.netbeans.modules.e2e.wsdl.WSDLException;
import org.netbeans.modules.e2e.wsdl.WSDLParser;
import org.netbeans.modules.e2e.wsdl.extensions.soap.SOAPConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class WSDL2JavaImpl implements WSDL2Java {
        
    private WSDLParser wsdlParser;
    private Definition definition;
    
    private Set<QName> usedParameterTypes;
    private Set<QName> usedReturnTypes;
            
    private Map<QName, Integer> uniqueTypeName;
    
    private WSDL2Java.Configuration configuration;
    
    /** Creates a new instance of WSDL2JavaImpl */
    public WSDL2JavaImpl( WSDL2Java.Configuration configuration ) {
        this.configuration = configuration;
        
        wsdlParser = new WSDLParser( configuration.getOriginalWSDLUrl() );        
    }

    public boolean generate() {
        uniqueTypeName = new HashMap<QName, Integer>();
        
        try {
            definition = wsdlParser.parse( configuration.getWSDLFileName());
                    
            // Check for validity of the WSDL
            boolean valid = true;
            for( ValidationResult vr: validate()) {
                if( ValidationResult.ErrorLevel.FATAL.equals( vr.getErrorLevel())) {
                    valid = false;
                    break;
                }
            }
            
            if( valid ) {
                generateInterfaces();
                generateTypes();
                generateStub();
            }
        } catch( WSDLException e ) {
            e.printStackTrace();
            return false;
        } catch( Exception e ) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "xxxxxx", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public List<ValidationResult> validate() {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
        try {
            definition = wsdlParser.parse( configuration.getWSDLFileName());
            validationResults.addAll( wsdlParser.getValidationResults());
            
            // Check of errors during the WSDL parsing. If fatal, return the validation results
            // from the parsing process
            for( ValidationResult result : validationResults ) {
                if( ValidationResult.ErrorLevel.FATAL.equals( result.getErrorLevel())) {
                    return validationResults;
                }
            }
            
            // Do the validation
            WSDLValidator validator = new WSDLValidator( wsdlParser.getValidationResults(), definition );
            validationResults = validator.validate();
        } catch( WSDLException e ) {
            // Drop e because the error is already in validationResults
            // e.printStackTrace();
        } catch( Exception e ) {
            e.printStackTrace();
        }
        return validationResults;
    }    
    
    /**
     *  @return null when void type
     */
    public Element getReturnElement( Element element ) {
        if( element.getMaxOccurs() > 1 ) return element;
        Type type = element.getType();
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            return element;
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            if( type.getSubconstructs().size() == 0 ) {
                return element;
            } else if( type.getSubconstructs().size() == 1 ) {
                return getSimplifiedElement(
                        (Element) type.getSubconstructs().get( 0 ));
            }
        }
        return element;
    }
    
    public List<Element> getParameterElements( Element element ) {
        List<Element> params = new ArrayList<Element>();
        params.add( element );
        
        if( element.getMaxOccurs() > 1 ) {
            return params;
        }
        Type type = element.getType();
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            return params;
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            params = new ArrayList<Element>();
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                    Element sce = (Element) sc;
                    params.add( sce );
                }
            }
            return params;
        }
        return params;
    }
    
    public String getJavaTypeName( Element e ) {
        boolean isArray = e.getMaxOccurs() > 1;
        String javaTypeName = "";
        Type type = e.getType();
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            if( e.isNillable() || e.getMinOccurs() == 0 ) {
                javaTypeName = getWrapperTypeName( e.getType());
            } else {
                javaTypeName = type.getJavaTypeName();
            }
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            if( type.getSubconstructs().size() == 0 ) {
                javaTypeName = "void";
            } else {
                javaTypeName = e.getType().getName() == null ? e.getName().getLocalPart() : e.getType().getName().getLocalPart();
            }
        }
        return javaTypeName + ( isArray ? "[]" : "" );
    }

    private boolean isElementComplex( Element e ) {
        boolean isArray = e.getMaxOccurs() > 1;
        if( isArray ) return true;
        
        Type type = e.getType();
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            if( e.isNillable()) {
                return true;
            } else {
                return false;
            }
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            if( type.getSubconstructs().size() == 0 ) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }
    
    private void generateInterfaces() throws Exception {        
        Set<QName> usedTypes = new HashSet<QName>();
        
        usedReturnTypes = new HashSet<QName>();
        usedParameterTypes = new HashSet<QName>();
        
        Set<QName> usedReturnTypeNames = new HashSet<QName>();
        Set<QName> usedParameterTypeNames = new HashSet<QName>();
        
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
                off.write( "package " + configuration.getPackageName() + ";\t\t" );
            }
            off.write( "\n" );
            off.write( "import javax.xml.namespace.QName;\n" );
            off.write( "\n" );

            off.write( "public interface " + serviceName + " extends java.rmi.Remote {\n" );
            
            for( Port port : service.getPorts()) {
                for( ExtensibilityElement ee : port.getExtensibilityElements()) {
                    if( SOAPConstants.ADDRESS.equals( ee.getElementType())) {
                        PortType portType = port.getBinding().getPortType();
                        for( Operation operation : portType.getOperations()) {
                            
                            String operationDocumentation = operation.getDocumentation();
                            if( operationDocumentation == null ) operationDocumentation = "";
                            off.write( "\n/**\n * " + operationDocumentation + "\n*/\n");
                            
                            Output output = operation.getOutput();                            
                            if (output != null) {
                                for( Part part : output.getMessage().getParts()) {
                                    Element re = definition.getSchemaHolder().
                                        getSchemaElement( part.getElementName());
                                    Element e = getReturnElement( re );
       
                                    if( isElementComplex( e )) {
                                        usedTypes.add( e.getName());
                                        usedReturnTypeNames.add( re.getName());
                                    }
                                    String javaTypeName = getJavaTypeName( e );
                                    off.write( "public " + javaTypeName + " " );
                                    break;
                                }                                
                            } else {
                                off.write( "public void " );                                
                            }


                            off.write( operation.getJavaName() + "(");
                            
                            Input input = operation.getInput();
                            if (input != null){
                                for( Part part : input.getMessage().getParts()) {
                                    Element element = definition.getSchemaHolder().getSchemaElement( part.getElementName());
                                    List<Element> params = getParameterElements( element );
                                    for( Iterator<Element> it = params.iterator(); it.hasNext(); ) {
                                        Element e = it.next();
                                        if( isElementComplex( e )) {
                                            usedTypes.add( e.getName());
                                            usedParameterTypeNames.add( e.getName());
                                        }
                                        Type type = e.getType();
                                        String javaTypeName = getJavaTypeName( e );
                                        off.write( javaTypeName + " " + e.getName().getLocalPart());
                                        if( it.hasNext()) off.write( ", " );
                                    }
                                }
                            } 
                            off.write(") throws java.rmi.RemoteException;\n");
                        }
                    }
                }
            }
            off.write("\n}\n");
            off.close();
        }        
                
        for( QName typeName : usedParameterTypeNames ) {
            usedParameterTypes.addAll( traverseTypes( usedParameterTypeNames, typeName ));
        }
        
        for( QName typeName : usedReturnTypeNames ) {
            usedReturnTypes.addAll( traverseTypes( usedParameterTypeNames, typeName ));
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
        Set<QName> result = new HashSet<QName>();
        result.add( typeName );
        Element element = definition.getSchemaHolder().getSchemaElement( typeName );
        Type type = element.getType();
        if( type == null ) {
            throw new IllegalArgumentException( "Invalid element type." );
        }
        if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    Element sce = (Element) sc;
                    if( Type.FLAVOR_SEQUENCE == sce.getType().getFlavor()) {
                        result.add( sce.getName());
                        result.addAll( traverseTypes( types, sce.getName()));
                    }
                }
            }
        }
        return result;
    }
        
    private Set<QName> getUsedTypes( QName typeName ) {
        Set<QName> result = new HashSet<QName>();
        Element element = definition.getSchemaHolder().getSchemaElement( typeName );
        Type type = null;
        if( element == null ) {
            type = definition.getSchemaHolder().getSchemaType( typeName );
        } else {
            type = element.getType();
        }
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) return result;
        if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            result.add( type.getName());
            
            SchemaConstruct parent = type.getParent();
            if( parent != null ) result.addAll( getUsedTypes( parent.getName()));
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    Element sce = (Element)sc;
                    if( Type.FLAVOR_SEQUENCE == sce.getType().getFlavor()) {
                        result.addAll( getUsedTypes( sce.getName()));
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
        Set<Element> usedArrayTypes = new HashSet<Element>();
                
        for( QName type : usedReturnTypes ) {            
            Element e = getReturnElement( definition.getSchemaHolder().getSchemaElement( type ));
            usedArrayTypes.addAll( generateType( e ));
        }
        for( QName type : usedParameterTypes ) {
            Element e = definition.getSchemaHolder().getSchemaElement( type );
            usedArrayTypes.addAll( generateType( e ));
        }
        if( configuration.getGenerateDataBinding()) {
            generateDataBindingArrays( usedArrayTypes ); 
        }
        
    }
    
    private Set<Element> generateType( Element element ) throws Exception {
        Set<Element> arrayTypes = new HashSet<Element>();
        
        Type type = element.getType();
        if( type == null ) {
            throw new IllegalArgumentException( "Invalid element type." );
        }
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) return arrayTypes;
        String name = type.getName() == null ? element.getName().getLocalPart() : type.getName().getLocalPart();
        File outputDirectoryF = new File( configuration.getOutputDirectory());
        FileObject outputDirectoryFO = FileUtil.toFileObject( FileUtil.normalizeFile( outputDirectoryF ));
        FileObject outputFileDirectoryFO = outputDirectoryFO.getFileObject( configuration.getPackageName().replace( '.', '/' ));           // NOI18N
        FileObject outputFile = outputFileDirectoryFO.getFileObject( name, "java" );
        if( outputFile == null  ) {
            outputFile = outputFileDirectoryFO.createData( name, "java" );
        }

        OutputFileFormatter off = new OutputFileFormatter( outputFile );

        if( configuration.getPackageName() != null && !"".equals( configuration.getPackageName().trim())) {
            off.write( "package " + configuration.getPackageName() + ";\n");
        }
        off.write( "\n" );
        off.write( "import javax.xml.namespace.QName;\n" );
        if( configuration.getGenerateDataBinding()) {
            off.write( "import org.netbeans.microedition.databinding.DataSet;\n" );
            off.write( "import org.netbeans.microedition.databinding.DataBindingException;\n" );
//                off.write( "import org.netbeans.microedition.databinding.DataSource;\n" );
            off.write( "\n" );
            if( type.getParent() == null ) {
                off.write( "public class " + name + " implements DataSet {\n" );
            } else {
                Type parentType = definition.getSchemaHolder().getSchemaType( type.getParent().getName());
                String parentName = parentType.getName().getLocalPart();
                off.write( "public class " + name + " extends " + parentName + " implements DataSet {\n" );
            }
        } else {
            if( type.getParent() == null ) {
                off.write( "public class " + name + " {\n" );
            } else {
                Type parentType = definition.getSchemaHolder().getSchemaType( type.getParent().getName());
                String parentName = parentType.getName().getLocalPart();
                off.write( "public class " + name + " extends " + parentName + " {\n" );
            }
        }
        for( SchemaConstruct sc : getElements( type )) {
//        for( SchemaConstruct sc : type.getSubconstructs()) {
            if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                Element sce = (Element) sc;
                sce = getSimplifiedElement( sce );
//                String propertyName = sce.getName().getLocalPart();
                String propertyName = getPropertyName( sce );
                String propertyVariableName = propertyName.substring( 0, 1 ).toLowerCase() + propertyName.substring( 1 );
                String propertyType = getPropertyType( sce );
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
                    if( !configuration.getGenerateDataBinding()) {
                        off.write( "private " + propertyType + (isArray ? "[] " : " ") + propertyVariableName + ";\n" );
                    } else {
                        if( !isArray ) {
                            off.write( "private " + propertyType + " " + propertyVariableName + ";\n" );
                        } else {
                            off.write( "private " + propertyType + "ArrayItem " + propertyVariableName + "_array_item = " + 
                                "new " + propertyType + "ArrayItem();\n");
                        }
                    }
                    off.write( "\n" );

                    off.write( "public void " + setter( propertyName ) + "( " + propertyType + (isArray ? "[] " : " ") + propertyVariableName + " ) {\n" );
                    if( isArray && configuration.getGenerateDataBinding()) {
                        off.write( propertyVariableName + "_array_item.setArray(" + propertyVariableName + ");\n" );
                    } else {
                        off.write( "this." + propertyVariableName + " = " + propertyVariableName + ";\n" );
                    }
                    off.write( "}\n\n" );

                    off.write( "public " + propertyType + (isArray ? "[] " : " ") + getter( propertyName ) + "() {\n" );
                    if( isArray && configuration.getGenerateDataBinding()) {
                        off.write( "return " + propertyVariableName + "_array_item.getArray();\n" );
                    } else {
                        off.write( "return " + propertyVariableName + ";\n" );
                    }
                    off.write( "}\n" );

                } else if( WSDL2Java.Configuration.TYPE_STRUCTURES == configuration.getGenerateType()) {
                    // Generate code in struct style

                    off.write( "\n" );
                    off.write( "public " + propertyType + (isArray ? "[] " : " ") + propertyVariableName + ";\n" );
                }

                if( sce.getMaxOccurs() > 1 ) {
                    arrayTypes.add( sce );
                }

            }
        }
        off.write( "\n" );

        if( configuration.getGenerateDataBinding()) {
            // getType
            off.write( "public Class getType(String dataItemName) {\n" );
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    // Fix for #162200 - Generated WS client is compiled with errors
                    Element sce = getSimplifiedElement((Element) sc);
                    String propertyName = sce.getName().getLocalPart();
                    String propertyVariableName = propertyName.substring( 0, 1 ).toLowerCase() + propertyName.substring( 1 );
                    String propertyType = sce.getType().getName().getLocalPart();
                    boolean isArray = sce.getMaxOccurs() > 1;
                    if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                        propertyType = getWrapperTypeName( sce.getType());
                    }

                    off.write( "if( \"" + propertyVariableName + "\".equals(dataItemName)) {\n" );
                    if( isArray ) {
                        off.write( "return org.netbeans.microedition.databinding.IndexableDataSet.class;\n" );
                    } else {
                        off.write( "return " + propertyType + ".class;\n" );
                    }
                    off.write( "}\n" );
                }
            }
            off.write( "throw new IllegalArgumentException( \"Invalid data item name \" + dataItemName );\n" );
            off.write( "}\n" );
            off.write( "\n" );

            // getValue
            off.write( "public Object getValue(String dataItemName) {\n" );
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    // Fix for #162200 - Generated WS client is compiled with errors
                    Element sce = getSimplifiedElement((Element) sc);
                    String propertyName = sce.getName().getLocalPart();
                    String propertyVariableName = propertyName.substring( 0, 1 ).toLowerCase() + propertyName.substring( 1 );
                    //String propertyType = sce.getType().getName().getLocalPart();

                    off.write( "if( \"" + propertyVariableName + "\".equals(dataItemName)) {\n" );
//                        if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
//                            off.write( "return " + wrapPrimitiveType( sce.getType(), propertyVariableName ) + ";\n" );
//                        } else {
                        if( sce.getMaxOccurs() > 1 ) {
                            off.write( "return " + propertyVariableName + "_array_item;\n" );
                        } else {
                        off.write( "return " + wrapPrimitiveType( sce.getType(), propertyVariableName ) + ";\n" );
//                                off.write( "return " + propertyVariableName + ";\n" );
                        }
//                        }
                    off.write( "}\n" );
                }
            }
            off.write( "throw new IllegalArgumentException( \"Invalid data item name \" + dataItemName );\n" );
            off.write( "}\n" );
            off.write( "\n" );

            // setValue
            off.write( "public void setValue(String dataItemName, Object value) throws DataBindingException {\n" );
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    // Fix for #162200 - Generated WS client is compiled with errors
                    Element sce = getSimplifiedElement((Element) sc);
                    String propertyName = sce.getName().getLocalPart();
                    String propertyVariableName = propertyName.substring( 0, 1 ).toLowerCase() + propertyName.substring( 1 );
                    String propertyType = sce.getType().getName().getLocalPart();

                    // Generate set only for non array fields
                    if( sce.getMaxOccurs() <= 1 ) {
                        off.write( "if( \"" + propertyVariableName + "\".equals(dataItemName)) {\n" );
                        if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                            off.write( propertyVariableName + " = " + unwrapPrimitiveType( sce, " value" ) + ";\n" );
                        } else {
                            off.write( propertyVariableName + " = (" + propertyType +") value;\n" );
                        }
                        off.write( "}\n" );
                    }
                }
            }
            off.write( "}\n" );
            off.write( "\n" );

            // setAsString
            off.write( "public void setAsString(String dataItemName, String value) throws DataBindingException {\n" );
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                    Element sce = getSimplifiedElement((Element) sc);
                    String propertyName = sce.getName().getLocalPart();
                    String propertyVariableName = propertyName.substring( 0, 1 ).toLowerCase() + propertyName.substring( 1 );
                    //String propertyType = sce.getType().getName().getLocalPart();

                    // Generate set only for non array fields
                    if( sce.getMaxOccurs() <= 1 ) {
                        off.write( "if( \"" + propertyVariableName + "\".equals(dataItemName)) {\n" );
                        if( Type.FLAVOR_PRIMITIVE == sce.getType().getFlavor()) {
                            off.write( propertyVariableName + " = " + parsePrimitiveType( sce, "value" ) + ";\n" );
                        } else {
                            off.write( "throw new DataBindingException( \"Illegal assigment.\");\n" );
                        }
                        off.write( "}\n" );
                    }
                }
            }
            off.write( "}\n" );
            off.write( "\n" );
            off.write( "public boolean isReadOnly(String dataItemName) {\n" );
            off.write( "return false;\n" );
            off.write( "}\n" );
        }            
        off.write( "}\n" );
        off.close();

        return arrayTypes;
    }
    
    private String getPropertyName( Element e ) {
        QName propertyQName = e.getName();
        if( propertyQName == null ) {
            if( e.getType().getFlavor() == Type.FLAVOR_SEQUENCE ) {
                if(((Type)e.getType()).getSubconstructs().size() == 1 ) {
                    propertyQName = ((Element)((Type)e.getType()).getSubconstructs().get( 0 )).getName();
                    if( propertyQName != null ) {
                        return propertyQName.getLocalPart();
                    } else {
                        throw new IllegalStateException( "Cannot get name for element." );
                    }
                }
            }
        } 
        return propertyQName.getLocalPart();
    }
    
    private String getPropertyType( Element e ) {
        QName propertyQName = e.getType().getName();
        if( propertyQName == null ) {
            if( e.getType().getFlavor() == Type.FLAVOR_SEQUENCE ) {
                if(((Type)e.getType()).getSubconstructs().size() == 1 ) {
                    propertyQName = ((Element)((Type)e.getType()).getSubconstructs().get( 0 )).getType().getName();
                    if( propertyQName != null ) {
                        return propertyQName.getLocalPart();
                    } else {
                        throw new IllegalStateException( "Cannot get name for element." );
                    }
                } else {
                    return getSimplifiedElement( e ).getName().getLocalPart();
                }
            }
        } 
        return propertyQName.getLocalPart();
    }
    
    private Element getSimplifiedElement( Element e ) {
        Element result = e;
        if( e.getType().getFlavor() == Type.FLAVOR_SEQUENCE ) {
            if( e.getType().getSubconstructs().size() == 1 ) {
                SchemaConstruct sc = e.getType().getSubconstructs().get( 0 );
                if( sc.getConstructType() == ConstructType.ELEMENT ) {
                    result = getSimplifiedElement((Element) sc );
                }
            }
        }
        return result;
    }
    
    /**
     * Generates structures for databinding arrays
     */
    private void generateDataBindingArrays( Set<Element> types ) throws Exception {
        for( Element element : types ) {
            //Element element = definition.getSchemaHolder().getSchemaElement( qName );
            Type type = element.getType();
            if( type == null ) {
                throw new IllegalArgumentException( "Invalid element type." );
            }

            String typeName = "";
            if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
                typeName = type.getJavaTypeName();
            } else {
                typeName = type.getName() == null ? element.getName().getLocalPart() : type.getName().getLocalPart();
            }
            String name = typeName + "ArrayItem";
            File outputDirectoryF = new File( configuration.getOutputDirectory());
            FileObject outputDirectoryFO = FileUtil.toFileObject( FileUtil.normalizeFile( outputDirectoryF ));
            FileObject outputFileDirectoryFO = outputDirectoryFO.getFileObject( configuration.getPackageName().replace( '.', '/' ));           // NOI18N
            FileObject outputFile = outputFileDirectoryFO.getFileObject( name, "java" );
            if( outputFile == null  ) {
                outputFile = outputFileDirectoryFO.createData( name, "java" );
            }
            
            OutputFileFormatter off = new OutputFileFormatter( outputFile );
            
            if( configuration.getPackageName() != null && !"".equals( configuration.getPackageName().trim())) {
                off.write( "package " + configuration.getPackageName() + ";\n");
            }
            off.write( "\n" );
            off.write( "import org.netbeans.microedition.databinding.IndexableDataSet;\n" );
            off.write( "import org.netbeans.microedition.databinding.DataBinder;\n" );
            off.write( "import org.netbeans.microedition.databinding.DataBindingException;\n" );
//                off.write( "import org.netbeans.microedition.databinding.DataSource;\n" );
            off.write( "\n" );
            if( type.getParent() == null ) {
                off.write( "public class " + name + " implements IndexableDataSet {\n" );
            } else {
                Type parentType = definition.getSchemaHolder().getSchemaType( type.getParent().getName());
                String parentName = parentType.getName().getLocalPart();
                off.write( "public class " + name + " extends " + parentName + " implements IndexableDataSet {\n" );
            }
            off.write( "\n" );
            
            // field representing the array
            off.write( "private " + typeName + "[] values = new " + typeName + "[] {};\n" );
            off.write( "\n" );
            
            // default constructor
            off.write( "/**\n" );
            off.write( " * Public default constructor for class " + name + "\n" );
            off.write( " */\n" );
            off.write( "public " + name + "() {\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // constructor with parameter
            off.write( "public " + name + "( " + typeName + "[] values ) {\n" );
            off.write( "this.values = values;\n" );
            off.write( "}\n" ); 
            off.write( "\n" );
            
            // isReadOnly
            off.write( "public boolean isReadOnly() {\n" );
            off.write( "return false;\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // getSize
            off.write( "public int getSize() {\n" );
                off.write( "return values.length;\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // getRow
            off.write( "public Object getRow(int index) throws DataBindingException {\n" );
                off.write( "if(index >=0 && index < values.length) {\n" );
                if( type.getFlavor() == Type.FLAVOR_PRIMITIVE ) {
                    off.write( "return " + wrapPrimitiveType( type, "values[index]" ) + ";\n" );
                } else {
                    off.write( "return values[index];\n" );
                }   
            off.write( "}\n" );
            off.write( "throw new DataBindingException(\"Index is out of range.\");\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // setRow
            off.write( "public void setRow( int index, Object value ) throws DataBindingException {\n" );
            off.write( "if( index >=0 && index < values.length ) {\n" );
            if( type.getFlavor() == Type.FLAVOR_PRIMITIVE ) {
                if( element == null ) {
                    off.write( "values[index] = " + unwrapPrimitiveType( type, "value" ) + ";\n" );
                } else {
                    off.write( "values[index] = " + unwrapPrimitiveType( element, "value" ) + ";\n" );
                }
            } else {
                off.write( "values[index] = (" + typeName + ") value;\n" );
            } 
            off.write( "DataBinder.fireDataSetChanged( this, new Integer( index ));\n" );
            off.write( "}\n" );
            off.write( "throw new DataBindingException(\"Index is out of range.\");\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // insertRow
            off.write( "public void insertRow(int index, Object value) throws DataBindingException {\n" );
            off.write( "if( index >= 0 && index <= values.length ) {\n" );
            off.write( typeName + " _newArray[] = new " + typeName + "[values.length + 1];\n" );
            off.write( "if( index > 1 ) System.arraycopy(values, 0, _newArray, 0, index - 1 );\n" );
            off.write( "if( index < values.length ) System.arraycopy(values, index, _newArray, index + 1, values.length - index );\n" );
            off.write( "_newArray[index] = (" + typeName + ")value;\n" );
            off.write( "values = _newArray;\n" );
            off.write( "DataBinder.fireDataSetChanged( this, new Integer( index ));" );
            off.write( "}\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // deleteRow
            off.write( "public void deleteRow(int index) throws DataBindingException {\n" );
            off.write( "if( index >= 0 && index < values.length ) {\n" );
            off.write( typeName + " _newArray[] = new " + typeName + "[values.length - 1];\n" );
            off.write( "if(index > 0) System.arraycopy(values, 0, _newArray, 0, index - 1);\n" );
            off.write( "if(index == 0) System.arraycopy(values, index + 1, _newArray, index, values.length - index - 1 );\n" );
            off.write( "}\n" );
            off.write( "DataBinder.fireDataSetChanged( this, new Integer( index ));" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // getType
            off.write( "public Class getType( String arg0 ) throws DataBindingException {\n" );
            off.write( "return " + typeName + ".class;\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // getValue
            off.write( "public Object getValue(String attribute) throws DataBindingException {\n" );
            off.write( "if( \"length\".equals( attribute )) {\n" );
            off.write( "return new Integer( values.length );\n" );
            off.write( "}\n" );
            off.write( "throw new DataBindingException( \"Invalid attribute name.\" );\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // setValue
            off.write( "public void setValue(String attribute, Object value) throws DataBindingException {\n" );
            off.write( "throw new DataBindingException(\"Invalid attribute name.\");\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // setAsString
            off.write( "public void setAsString(String attribute, String value) throws DataBindingException {\n" );
            off.write( "throw new DataBindingException(\"Invalid attribute name.\");\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // isReadOnly
            off.write( "public boolean isReadOnly(String attribute) throws DataBindingException {\n" );
            off.write( "return true;\n" );
            off.write( "}\n" );

            // getArray
            off.write( typeName + "[] getArray() {\n" );
                off.write( "return values;\n" );
            off.write( "}\n" );
            off.write( "\n" );
            
            // setArray
            off.write( "void setArray(" + typeName + " values[] ) {\n" );
            off.write( "this.values = values;\n" );
            off.write( "}\n" );
                    
            off.write( "}\n" );
            off.close();
        }        
    }

    private void generateStub() throws Exception {
        usedParameterTypes = new HashSet<QName>();
        Set<QName> primitiveTypeArrays = new HashSet<QName>();
        Set<QName> operationQNames = new HashSet<QName>();
        
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
                                    off.write( "return new Boolean(false);\n" );
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
                        
                        Set<Element> fromObjects = new HashSet<Element>();
                        Set<Element> toObjects = new HashSet<Element>();
                        
                        for( Operation operation : portType.getOperations()) {
                            BindingOperation bindingOperation = binding.getBindingOperation( operation.getName());
                            
                            operationQNames.add( new QName( definition.getTargetNamespace(), operation.getName()));
                            
                            QName messageName = operation.getOutput().getMessage().getQName();
                            Message message = definition.getMessage( messageName );
                            for( Part part : message.getParts()) {
                                Element element = definition.getSchemaHolder().getSchemaElement( part.getElementName());
                                returnTypeName = element.getName().getLocalPart();
                                Element e = getReturnElement( element );
                                String javaTypeName = getJavaTypeName( e );
                                
                                off.write( "\n" );
                                off.write( "public " + javaTypeName + " " );
                                usedParameterTypes.add( element.getName());
                                break;
                            }

                            off.write( operation.getJavaName() + "(");
                            
                            Input input = operation.getInput();
                            if (input != null) {
                                for( Part part : input.getMessage().getParts()) {
                                    Element element = definition.getSchemaHolder().getSchemaElement( part.getElementName());
                                    paramTypeName = element.getName().getLocalPart();
                                    usedParameterTypes.add( element.getName());
                                    List<Element> params = getParameterElements( element );
                                    for( Iterator<Element> it = params.iterator(); it.hasNext(); ) {
                                        Element e = it.next();
                                        Type type = e.getType();
                                        String javaTypeName = getJavaTypeName( e );
                                        off.write( javaTypeName + " " + e.getName().getLocalPart());
                                        if( it.hasNext()) off.write( ", " );
                                    }
                                }
                            }
                            
                            off.write( ") throws java.rmi.RemoteException {\n" );
                            // Wrap to Object[] array                            
                            for( Iterator<Part> it = input.getMessage().getParts().iterator(); it.hasNext(); ) {
                                Part part = it.next();
                                Element e = definition.getSchemaHolder().getSchemaElement( part.getElementName());
                                
                                Type type = e.getType();
                                if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
                                    if( e.isNillable() || e.getMinOccurs() == 0 ) {
                                        off.write( "Object inputObject = " + e.getName().getLocalPart() + ";\n" );
                                    } else {
                                        off.write( "Object inputObject = " + wrapPrimitiveType( type, e.getName().getLocalPart()) + ";\n" );
                                    }
                                } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {                                    
                                    off.write( "Object inputObject[] = new Object[] {\n" );
                                    for( Iterator<SchemaConstruct> scit = type.getSubconstructs().iterator(); scit.hasNext(); ) {
                                        SchemaConstruct sc = scit.next();
                                        if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                                            Element sce = (Element) sc;
                                            boolean isArray = sce.getMaxOccurs() > 1;
                                            Type t = sce.getType();
                                            if( Type.FLAVOR_PRIMITIVE == t.getFlavor()) {
                                                if( !isArray ) {
                                                    if( sce.isNillable() || sce.getMinOccurs() == 0 ) {
                                                        // Wrapper non array
                                                        off.write( sce.getName().getLocalPart());
                                                    } else {
                                                        // Primitive non array
                                                        off.write( wrapPrimitiveType( t, sce.getName().getLocalPart()));
                                                    }
                                                } else {
                                                    if( sce.isNillable() || sce.getMinOccurs() == 0 ) {
                                                        off.write( sce.getName().getLocalPart());
                                                    } else { 
//                                                        off.write( wrapPrimitiveType( t, sce.getName().getLocalPart()));
                                                        // XXX: Check
                                                        off.write( sce.getName().getLocalPart());
                                                    }
                                                }
                                            } else if( Type.FLAVOR_SEQUENCE == t.getFlavor()) {
                                                String typeName = sce.getType().getJavaName();
                                                if( typeName == null )
                                                    typeName = sce.getType().getName() == null ? sce.getName().getLocalPart() : sce.getType().getName().getLocalPart();                                                
                                                off.write( typeName + "_" + ( isArray ? "Array" : "" ) + "toObject( " + sce.getName().getLocalPart() + " )" );
                                                toObjects.add( sce );
                                            }
                                            if( scit.hasNext()) off.write( ", " );
                                            off.write( "\n" );
                                        }
                                    }
                                    off.write( "};\n" );
                                }                                
                            }
                            off.write( "\n" );
                            off.write( "Operation op = Operation.newInstance( _qname_operation_" + operation.getName() + ", _type_" + paramTypeName + ", _type_" + returnTypeName + " );\n" );
                            off.write( "_prepOperation( op );\n");
                            for( ExtensibilityElement exe : bindingOperation.getExtensibilityElements()) {
                                if( exe instanceof SOAPOperation ) {
                                    SOAPOperation so = (SOAPOperation) exe;
                                    if (so.getSoapActionURI() != null){
                                        off.write( "op.setProperty( Operation.SOAPACTION_URI_PROPERTY, \"" );
                                        off.write( so.getSoapActionURI());
                                        off.write( "\" );\n" );
                                    }
                                }
                            }
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
                            
                            Output output = operation.getOutput();
                            if (output != null) {
                                message = definition.getMessage( output.getMessage().getQName() );
                                for( Part part : message.getParts()) {
                                    Element e = getReturnElement( definition.getSchemaHolder().getSchemaElement( part.getElementName()));
                                    Type type = e.getType();
                                    boolean isArray = e.getMaxOccurs() > 1;
                                    if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
                                        if( !isArray ) {
                                            off.write( "return " + unwrapPrimitiveType( e, "((Object[])resultObj)[0]" ) + ";\n");
                                        } else {
                                            if( !e.isNillable() && !(e.getMinOccurs() == 0 )) {
                                                off.write( "return " + type.getJavaTypeName().replace( '.', '_' ) + "_ArrayfromObject((Object []) resultObj);\n" );
                                            } else {
                                                // Handle string object properly
                                                if( SchemaConstants.TYPE_STRING.equals( type.getName())) {
                                                    off.write( "String[] _res = new String[((Object [])((Object []) resultObj)[0]).length];\n" );
                                                    off.write( "System.arraycopy(((Object []) resultObj)[0], 0, _res, 0, _res.length);\n" );
                                                    off.write( "return _res;\n" );
                                                } else {
                                                    off.write( "return " + unwrapPrimitiveType( e, "((Object[])resultObj)[0]" ) + ";\n" );
                                                }
                                            }
                                            //fromObjects.add( e );
                                        }
                                    } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
                                        if( type.getSubconstructs().size() == 0 ) {
                                            // void
                                        } else {
                                            String typeName = e.getType().getJavaTypeName();
                                            if( typeName == null ) typeName = e.getType().getName() == null ? e.getName().getLocalPart() : e.getType().getName().getLocalPart();
                                            if( !isArray ) {
                                                off.write( "return " + typeName.replace( '.', '_' ) + "_fromObject((Object[])resultObj);\n" );
                                            } else {
                                                off.write( "return " + typeName.replace( '.', '_' ) + "_ArrayfromObject((Object[]) resultObj);\n" );
                                            }
                                            fromObjects.add( e );
                                        }
                                    }
                                    break;
                                }
                                
                            }
                            off.write("}\n");
                        }
                        
                        // Generate object methods
                        // Traversing
                        Set<SchemaConstruct> to = new HashSet<SchemaConstruct>();
                        for( Element e : toObjects ) {
                            to.addAll( traverseObjectElements( getSimplifiedElement( e ), to ));
                        }
                        Set<SchemaConstruct> from = new HashSet<SchemaConstruct>();
                        for( Element e : fromObjects ) {
                            from.addAll( traverseObjectElements( getSimplifiedElement( e ), from ));
                        }
                        // toObject methods
                        Set<String> usedToMethods = new HashSet<String>();
                        for( SchemaConstruct sc : to ) {
                            String typeName = sc.getName().getLocalPart();
                            boolean isA = false;
                            if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                                Element e = (Element) sc;
                                if( e.getMaxOccurs() > 1 ) isA = true;
                                if( e.getType().getName() != null ) typeName = e.getType().getName().getLocalPart();
                            }                           
                            String methodName = typeName + "_" + ( isA ? "Array" : "" ) + "toObject";
                            if( usedToMethods.contains( methodName )) continue; else usedToMethods.add( methodName );
                            off.write( "\n");
                            off.write( "private static Object " + methodName + "( " + typeName + ( isA ? "[]" : "" ) + " obj ) {\n" );
                            off.write( "if(obj == null) return null;\n" );
                            Type type = null;
                            if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                                Element e = (Element) sc;
                                type = e.getType();
                            } else if( SchemaConstruct.ConstructType.TYPE.equals( sc.getConstructType())) {
                                type = (Type) sc;
                            } else {
                                throw new InvalidParameterException( "Invalid SchemaConstruct type" );
                            }                                                        
                            String resultVariableName = ( isA ? "resultArray" : "result" );
                            if( isA ) {
                                off.write( "Object result[] = new Object[ obj.length ];\n" );
                                off.write( "for( int i = 0; i < obj.length; i++ ) {\n" );
                                off.write( "Object[] " + resultVariableName + " = new Object[ " + type.getSubconstructs().size() + " ];\n" );
                            } else {                            
                                off.write( "Object result[] = new Object[ " + type.getSubconstructs().size() + " ];\n" );
                            }
                            int i = 0;
                            for( Element sce : getElements( type )) {
                                Type t = sce.getType();
                                String variableName = sce.getName().getLocalPart();
                                boolean isArray = sce.getMaxOccurs() > 1;
                                off.write( resultVariableName + "[" + i + "] = " );
                                if( Type.FLAVOR_PRIMITIVE == t.getFlavor()) {
                                    if( !isArray ) {
                                        if( sce.isNillable() || ( sce.getMinOccurs() == 0 && sce.getMaxOccurs() == 1 )) {
                                            off.write( "obj" + ( isA ? "[i]" : "" ) + "." + getter( variableName ) + "()" );
                                        } else {
                                        off.write( wrapPrimitiveType( t, "obj" + ( isA ? "[i]" : "" ) + "." + getter( variableName ) + "()" ));
                                        }
                                    } else {
                                        if( sce.isNillable()) {
                                            off.write( "obj" + ( isA ? "[i]" : "" ) + "." + getter( variableName ) + "()" );
                                        } else {
                                            off.write( "???" );
                                        }
                                    }
                                } else if( Type.FLAVOR_SEQUENCE == t.getFlavor()) {
                                    String tn = t.getName() == null ? sce.getName().getLocalPart() : t.getName().getLocalPart();
                                    if( !isArray ) {
                                        off.write( tn + "_toObject( obj" + ( isA ? "[i]" : "" ) + "." + getter( variableName ) + "())" );
                                    } else {
                                        off.write( tn + "_ArraytoObject( obj" + ( isA ? "[i]" : "" ) + "." + getter( variableName ) + "())" );
                                    }
                                }
                                off.write( ";\n" );
                                i++;
                            }
                            if( isA ) {
                                off.write( "result[i] = " + resultVariableName +";\n" );
                                off.write( "}\n" );
                            }
                            off.write( "return result;\n" );
                            off.write( "}\n" );
                        }
                        // fromObject methods
                        Set<String> usedFromMethods = new HashSet<String>();
                        for( SchemaConstruct sc : from ) {
                            String typeName = sc.getName().getLocalPart();
                            boolean isA = false;
                            if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                                Element e = (Element) sc;
                                if( e.getMaxOccurs() > 1 ) isA = true;
                                typeName = e.getType().getJavaTypeName();
                                typeName = getPropertyType( e );
//                                if( typeName == null ) typeName = getPropertyType( e );
//                                if( typeName == null && e.getType().getName() != null ) typeName = e.getType().getName().getLocalPart();
                            }
                            String methodName = typeName.replace( '.', '_') + "_" + ( isA ? "Array" : "" )+ "fromObject";
                            if( usedFromMethods.contains( methodName )) continue; else usedFromMethods.add( methodName );
                            off.write( "\n");
                            off.write( "private static " + typeName + ( isA ? "[]" : "" ) + " " + methodName + "( Object obj[] ) {\n" );
                            off.write( "if(obj == null) return null;\n" );
                            off.write( typeName + " result" + ( isA ? "[]" : "" )+ " = new " + typeName + ( isA ? "[obj.length]" : "()" ) + ";\n" );
                            String objectVariableName = ( isA ? "oo" : "obj" );
                            if( isA ) {
                                off.write( "for( int i = 0; i < obj.length; i++ ) {\n" );
                                off.write( "result[i] = new " + typeName + "();\n" );
                                off.write( "Object[] " + objectVariableName + " = (Object[]) obj[i];\n" );
                            }
                            Type type = null;
                            if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                                Element e = (Element) sc;
                                type = e.getType();
                            } else if( SchemaConstruct.ConstructType.TYPE.equals( sc.getConstructType())) {
                                type = (Type) sc;
                            } else {
                                throw new InvalidParameterException( "Invalid SchemaConstruct type" );
                            }
                            int i = 0;
                            for( Element sce : getElements( type )) {
                                Element e = getSimplifiedElement( sce );
                                Type t = e.getType();
//                                Type t = sce.getType();
//                                String variableName = sce.getName().getLocalPart();
                                String variableName = getPropertyName( e );
                                boolean isArray = e.getMaxOccurs() > 1;
                                if( Type.FLAVOR_PRIMITIVE == t.getFlavor()) {
                                    String tn = getPropertyType( e );
                                    if( !isArray ) {
                                        off.write( "result" + ( isA ? "[i]" : "" ) + "." + setter( variableName ) + "(" + unwrapPrimitiveType( sce, objectVariableName + "[" + i + "]" ) + ");\n" );
                                    } else {
                                        // Make repackager from wrapped to primitive array
                                        if( SchemaConstants.TYPE_STRING.equals( t.getName())) {
                                            off.write( "result" + ( isA ? "[i]" : "" ) + "." + setter( variableName ) + "((String[])" + objectVariableName + "[" + i + "]" + ");\n" );
                                        } else {
                                            primitiveTypeArrays.add( t.getName());
                                            off.write( "result" + ( isA ? "[i]" : "" ) + "." + setter( variableName ) + "( Unroll_" + tn + "_Array(("+ getWrapperTypeName( t ) + "[]) " + objectVariableName + "[" + i + "]" + "));\n" );
                                        }
                                    }
                                } else if( Type.FLAVOR_SEQUENCE == t.getFlavor()) {
                                    String tn = getPropertyType( e );
//                                    String tn = t.getName() == null ? sce.getName().getLocalPart() : t.getName().getLocalPart();
                                    if( !isArray ) {
                                        off.write( "result" + ( isA ? "[i]" : "" ) + "." + setter( variableName ) + "(" + tn + "_fromObject((Object[]) " + objectVariableName + "[" + i + "] ));\n" );
                                    } else {
                                        off.write( "result" + ( isA ? "[i]" : "" ) + "." + setter( variableName ) + "(" + tn + "_ArrayfromObject((Object[]) " + objectVariableName + "[" + i + "] ));\n" );
                                    }
                                }
                                i++;
                            }
                            if( isA ) {
                                off.write( "}\n" );
                            }
                            off.write( "return result;\n" );
                            off.write( "}\n" );
                        }
                        // Make primitive array type unrollers
                        for( QName qn : primitiveTypeArrays ) {
                            off.write( "\n" );
                            String typeName = null;
                            String wrapperName = null;
                            if( SchemaConstants.TYPE_INT.equals( qn )) {
                                typeName = "int";   wrapperName = "Integer";
                            } else if( SchemaConstants.TYPE_BOOLEAN.equals( qn )) {
                                typeName = "boolean";  wrapperName = "Boolean";
                            } else if( SchemaConstants.TYPE_BYTE.equals( qn )) {
                                typeName = "byte";  wrapperName = "Byte";
                            } else if( SchemaConstants.TYPE_DOUBLE.equals( qn )) {
                                typeName = "double";  wrapperName = "Double";
                            } else if( SchemaConstants.TYPE_FLOAT.equals( qn )) {
                                typeName = "float";  wrapperName = "Float";
                            } else if( SchemaConstants.TYPE_LONG.equals( qn )) {
                                typeName = "long";  wrapperName = "Long";
                            } else if( SchemaConstants.TYPE_SHORT.equals( qn )) {
                                typeName = "short";  wrapperName = "Short";
                            }
                            off.write( "private static " + typeName + "[] Unroll_" + typeName + "_Array( " + wrapperName + " a[] ) {\n" );
                            off.write( typeName + " result[] = new " + typeName + "[ a.length ];\n" );
                            off.write( "for( int i = 0; i < a.length; i++ ) {\n" );
                            off.write( "result[i] = a[i]." + typeName + "Value();\n" );
                            off.write( "}\n" );
                            off.write( "return result;\n" );
                            off.write( "}" );
                            Integer a = 5;
                        }
                        
                        // Collect Qnames and Elements        
                        Set<QName> qnames = new HashSet<QName>();
                        Set<Element> elements = new HashSet<Element>();
                        for( QName parameterName : usedParameterTypes ) {
                            SchemaConstruct sc;
                            sc = definition.getSchemaHolder().getSchemaElement( parameterName );
                            if( sc == null ) continue;
                            traverseParameterTypes( sc, qnames, elements );
                        }
                        off.write( "\n" );
                        for( QName q : operationQNames ) {
                            off.write( "protected static final QName ");
                            off.write( "_qname_operation_" + q.getLocalPart() + " = new QName( " );
                            off.write( '"' + q.getNamespaceURI() + "\", \"" + q.getLocalPart() + "\" );\n" );
                        }
                        for( QName q : qnames ) {
                            off.write( "protected static final QName ");
                            off.write( "_qname_" + q.getLocalPart() + " = new QName( " );
                            off.write( '"' + q.getNamespaceURI() + "\", \"" + q.getLocalPart() + "\" );\n" );
                        }

                        // _type_ static declaration 
                        for( Element e : elements ) {
                            off.write( "protected static final Element _type_" + e.getName().getLocalPart() + ";\n" );
                        }
                        off.write( "\nstatic {\n" );

                        // static _type initialization
                        for( QName parameterName : usedParameterTypes ) {
                            SchemaConstruct sc;
                            sc = definition.getSchemaHolder().getSchemaElement( parameterName );
                            off.write( "_type_" + sc.getName().getLocalPart() + " = " );
                            initTypes( off, sc, qnames, elements );
                            off.write( ";\n" );
                        }
                        off.write( "}\n\n" );

                        // Static wrapper for complex types
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

    public List<Element> getElements( Type type ) {
        List<Element> elements = new ArrayList<Element>();
        if( type.getParent() != null ) {
            elements.addAll( getElements((Type)type.getParent()));
        }
        for( SchemaConstruct sc : type.getSubconstructs()) {
            if( SchemaConstruct.ConstructType.ELEMENT == sc.getConstructType()) {
                Element e = (Element)sc;
                elements.add( e );
            }
        }
        return elements;
    }

    private Set<SchemaConstruct> traverseObjectElements( SchemaConstruct schemaConstruct, Set<SchemaConstruct> elements ) {
        Set<SchemaConstruct> result = new HashSet<SchemaConstruct>();
        if( result.contains( schemaConstruct )) { 
            return Collections.emptySet();
        } else {
            elements.add( schemaConstruct );
        }
        Type type = null;
        Element element = null;
        if( SchemaConstruct.ConstructType.ELEMENT.equals( schemaConstruct.getConstructType())) {
            element = (Element) schemaConstruct;
            type = element.getType();
        } else if( SchemaConstruct.ConstructType.TYPE.equals( schemaConstruct.getConstructType())) {
            type = (Type) schemaConstruct;
        } else {
            throw new InvalidParameterException( "Invalid SchemaConstruct type" );
        }
        if( Type.FLAVOR_PRIMITIVE == type.getFlavor()) {
            if( element != null ) {
                // Is array
                if( element.getMaxOccurs() > 1 ) {
                    result.add( element );
                    return result;
                }                
            }
        } else if( Type.FLAVOR_SEQUENCE == type.getFlavor()) {
            for( SchemaConstruct sc : type.getSubconstructs()) {
                if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
                    Element sce = getSimplifiedElement((Element) sc );
                    Type scetype = sce.getType();
                    if( Type.FLAVOR_SEQUENCE == scetype.getFlavor()) {
                        elements.add( sce );
                        result.addAll( traverseObjectElements( sce, elements ));
                    }
                }
            }
        }
        return result;
    }
    
    private String wrapPrimitiveType( Type type, String value ) {
        QName typeName = type.getName();
        if( SchemaConstants.TYPE_INT.equals( typeName )) {
            return "new Integer(" + value + ")";
        } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
            return "new Boolean(" + value + ")";
        } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
            return "new Byte(" + value + ")";
        } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
            return "new Double(" + value + ")";
        } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
            return "new Float(" + value + ")";
        } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
            return "new Long(" + value + ")";
        } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
            return "new Short(" + value + ")";
        }
        
        return value;
    }
    
    private String unwrapPrimitiveType( Type type, String value ) {
        QName typeName = type.getName();
        if( SchemaConstants.TYPE_INT.equals( typeName )) {
            return "((Integer)" + value + ").intValue()";
        } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
            return "((Boolean)" + value + ").booleanValue()";
        } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
            return "((Byte)" + value + ").byteValue()";
        } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
            return "((Double)" + value + ").doubleValue()";
        } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
            return "((Float)" + value + ").floatValue()";
        } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
            return "((Long)" + value + ").longValue()";
        } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
            return "((Short)" + value + ").shortValue()";
        } else if( SchemaConstants.TYPE_STRING.equals( typeName )) {
            return "(String)" + value;
        }
        return value;
    }
    
    private String unwrapPrimitiveType( Element element, String value ) {
        Type type = element.getType();
        QName typeName = type.getName();
        String unwrapped = "";
        if( element.getMaxOccurs() == Type.UNBOUNDED ) {
            unwrapped = "[]";
        }
        if( SchemaConstants.TYPE_INT.equals( typeName )) {
            unwrapped = "(Integer " + unwrapped + ")";
        } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
            unwrapped = "(Boolean " + unwrapped + ")";
        } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
            unwrapped = "(Byte " + unwrapped + ")";
        } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
            unwrapped = "(Double " + unwrapped + ")";
        } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
            unwrapped = "(Float " + unwrapped + ")";
        } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
            unwrapped = "(Long " + unwrapped + ")";
        } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
            unwrapped = "(Short " + unwrapped + ")";
        } else {
            unwrapped = "(" + type.getJavaTypeName() + " " + unwrapped + ")";
        }
        unwrapped += value;
        if( element.getMinOccurs() > 0 && !element.isNillable()) {
            if( SchemaConstants.TYPE_INT.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").intValue()";
            } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").booleanValue()";
            } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").byteValue()";
            } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").doubleValue()";
            } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").floatValue()";
            } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").longValue()";
            } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
                unwrapped = "(" + unwrapped + ").shortValue()";
            }
        }
        
        return unwrapped;
    }
    
    private String parsePrimitiveType( Element element, String value ) {
        Type type = element.getType();
        QName typeName = type.getName();
        String parse = wrapPrimitiveType( type, value );
        if( element.getMinOccurs() > 0 && !element.isNillable()) {
            if( SchemaConstants.TYPE_INT.equals( typeName )) {
                parse = "Integer.parseInt(" + value + ")";
            } else if( SchemaConstants.TYPE_BOOLEAN.equals( typeName )) {
                parse = "value.equals( \"true\" ) ? true : false";
            } else if( SchemaConstants.TYPE_BYTE.equals( typeName )) {
                parse = "Byte.parseByte(" + value + ")";
            } else if( SchemaConstants.TYPE_DOUBLE.equals( typeName )) {
                parse = "Double.parseDouble(" + value + ")";
            } else if( SchemaConstants.TYPE_FLOAT.equals( typeName )) {
                parse = "Float.parseFloat(" + value + ")";
            } else if( SchemaConstants.TYPE_LONG.equals( typeName )) {
                parse = "Long.parseLong(" + value + ")";
            } else if( SchemaConstants.TYPE_SHORT.equals( typeName )) {
                parse = "Short.parseShort(" + value + ")";
            }
        }
        
        return parse;
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
                                     
                off.write( "new Element( " );
                if( qnames.contains( e.getName())) {
                    off.write( "_qname_" + ctName );
                } else {
                    off.write( "new QName( \"" + e.getName().getNamespaceURI() + "\", \"" + e.getName().getLocalPart() + "\" )" );
                }
                off.write( ", _complexType( new Element[] {\n" );                
                
                for( Iterator<Element> scit = getElements( t ).iterator(); scit.hasNext(); ) {
//                for( Iterator<SchemaConstruct> scit = t.getSubconstructs().iterator(); scit.hasNext(); ) {
                    initTypes( off, scit.next(), qnames, elements );
                    if( scit.hasNext()) off.write( ",\n" );
                }
                if( e.getMinOccurs() != 0 || e.getMaxOccurs() != 1 || e.isNillable()) {
                    off.write( "}), " + e.getMinOccurs() + ", " + 
                            ( e.getMaxOccurs() == Type.UNBOUNDED ? "Element.UNBOUNDED" : e.getMaxOccurs()) + ", " + e.isNillable()+ " )" );
                } else {
                    off.write( "}))" );
                }
            } else if( Type.FLAVOR_PRIMITIVE == e.getType().getFlavor()) {
                String eName = e.getName().getLocalPart();
                off.write( "new Element( " );
                if( qnames.contains( e.getName())) {
                    off.write( "_qname_" + eName + ", " );
                } else {
                    off.write( "new QName( \"" + e.getName().getNamespaceURI() + "\", \"" + e.getName().getLocalPart() + "\" ), " );
                }
                int minOccurs = e.getMinOccurs();
                int maxOccurs = e.getMaxOccurs();
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
                } else if( SchemaConstants.TYPE_QNAME.equals( t.getName())) {
                    off.write( "new ComplexType() /* TODO: Must implement */" );
                } else {
//                    System.err.println("Error type");
                }
                if( minOccurs != 1 || maxOccurs != 1 || e.isNillable()) {
                    off.write( ", " + minOccurs + ", " );
                    if( maxOccurs == Element.UNBOUNDED ) {
                        off.write( "Element.UNBOUNDED" );
                    } else {
                        off.write( "" + maxOccurs );
                    }
                    off.write( ", " + e.isNillable());
                }
                off.write( " )" );
            }
        }
    }
            
    public void traverseParameterTypes( SchemaConstruct sc, Set<QName> qnames, Set<Element> elements ) {
        if( SchemaConstruct.ConstructType.ELEMENT.equals( sc.getConstructType())) {
            Element e = (Element)sc;
            qnames.add( e.getName());
            elements.add( e );
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


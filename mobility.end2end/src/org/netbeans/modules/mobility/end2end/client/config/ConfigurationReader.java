/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigurationReader.java
 *
 * Created on June 27, 2005, 9:40 AM
 *
 */
package org.netbeans.modules.mobility.end2end.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.netbeans.modules.mobility.end2end.classdata.AbstractService;
import org.netbeans.modules.mobility.end2end.classdata.ClassData;
import org.netbeans.modules.mobility.end2end.classdata.ClassService;
import org.netbeans.modules.mobility.end2end.classdata.OperationData;
import org.netbeans.modules.mobility.end2end.classdata.PortData;
import org.netbeans.modules.mobility.end2end.classdata.TypeData;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.loaders.DataObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Michal Skvor
 */
public class ConfigurationReader {
    
    private final static String TYPE = "type";
    private final static String NAME = "name";
    
    private ConfigurationReader() {
        //to avoid instantiation
    }
    
    public static synchronized Configuration read( final DataObject doj ) throws Exception {
        final Configuration configuration = new Configuration();
        
        final Document doc = XMLUtil.parse(DataObjectAdapters.inputSource(doj), false, false, null, null);
        
        // Parse ws client config
        final Element root = doc.getDocumentElement();
        final String serviceType = root.getAttribute( "serviceType" );    // NOI18N
        if( Configuration.CLASS_TYPE.equals( serviceType )) {
            configuration.setServiceType( Configuration.CLASS_TYPE );
        } else if( Configuration.WSDLCLASS_TYPE.equals( serviceType )) {
            configuration.setServiceType( Configuration.WSDLCLASS_TYPE );
        } else if( Configuration.JSR172_TYPE.equals( serviceType )) {
            configuration.setServiceType( Configuration.JSR172_TYPE );
        } else {
            throw new InvalidConfigFileException( "Invalid service type:" + serviceType ); // NOI18N
        }
        // Checking of service version supported
        final String configFileVersion = root.getAttribute( "version" ); // NOI18N
        if( !"1.0".equals( configFileVersion )) { // NOI18N
            throw new InvalidConfigFileException( "Invalid config file version: " + configFileVersion ); // NOI18N
        }
        
        // Client
        final NodeList clientConfig = root.getElementsByTagName( "client" ); // NOI18N
        if( clientConfig.getLength() > 1 ) {
            throw new InvalidConfigFileException( "Config file has more than one client section" ); // NOI18N
        } else if( clientConfig.getLength() == 0 ) {
            throw new InvalidConfigFileException( "Config file has no client section" ); // NOI18N
        }
        configuration.setClientConfiguration( parseClient((Element)clientConfig.item( 0 )));
        
        if( ! Configuration.JSR172_TYPE.equals( configuration.getServiceType() )){
            // Server
            final NodeList serverConfig = root.getElementsByTagName( "server" ); // NOI18N
            if( serverConfig.getLength() > 1 ) {
                throw new InvalidConfigFileException( "Config file has more than one server section" ); // NOI18N
            } else if( serverConfig.getLength() == 0 ) {
                throw new InvalidConfigFileException( "Config file has no server section" ); // NOI18N
            }
            configuration.setServerConfiguration( parseServer((Element)serverConfig.item( 0 )));
        }
        // Services
        
        final NodeList services = root.getElementsByTagName( "services" ); // NOI18N
        if( services.getLength() > 1 ) {
            throw new InvalidConfigFileException( "Config file has more than one services section" ); // NOI18N
        } else if( services.getLength() == 0 ) {
            throw new InvalidConfigFileException( "Config file has no services section" ); // NOI18N
        }
        final NodeList service = ((Element)services.item( 0 )).getElementsByTagName( "service" ); // NOI18N
        if( service.getLength() > 1 ) {
            throw new InvalidConfigFileException( "Config file has more than one services" ); // NOI18N
        } else if ( services.getLength() == 0 ) {
            throw new InvalidConfigFileException( "Config file has no registered services" ); // NOI18N
        }
        configuration.setServices( parseServices((Element)service.item( 0 ), serviceType ));
        
        return configuration;
    }
    
    static private ClientConfiguration parseClient( final Element element ) throws Exception {
        
        final ClientConfiguration cc = new ClientConfiguration();
        final String projectName = getElementValue( element, "project" ); // NOI18N
        if( "".equals( projectName )) {
            throw new InvalidConfigFileException( "Client project name is empty string" ); // NOI18N
        }
        cc.setProjectName( projectName );
        
        final NodeList classes = element.getElementsByTagName( "class" ); // NOI18N
        if( classes.getLength() > 1 ) {
            throw new InvalidConfigFileException( "Client has more than one class tag" ); // NOI18N
        } else if( classes.getLength() == 0 ) {
            throw new InvalidConfigFileException( "Client has no class tag" ); // NOI18N
        }
        final Element clazz = (Element)classes.item( 0 );
        final String type = clazz.getAttribute( TYPE ); // NOI18N
        if( "".equals( type )) {
            throw new InvalidConfigFileException( "Client class type is empty string" ); // NOI18N
        }
        final String location = clazz.getAttribute( "location" ); // NOI18N
        final ClassDescriptor cd = new ClassDescriptor( type, location );
        cc.setClassDescriptor( cd );
        
        final Properties props = new Properties();
        final NodeList properties = element.getElementsByTagName( "property" );   // NOI18N
        for( int i = 0; i < properties.getLength(); i++ ) {
            final Element property = (Element)properties.item( i );
            final String key = property.getAttribute( NAME ); // NOI18N
            if( "".equals( key )) {
                throw new IllegalAccessException( "Key name is empty string" ); // NOI18N
            }
            final String value = property.getAttribute( "value" ); // NOI18N
            props.put( key, value );
        }
        cc.setProperties( props );
        
        return cc;
    }
    
    static private ServerConfiguration parseServer( final Element element ) throws Exception {
        
        final ServerConfiguration sc = new ServerConfiguration();
        
        final Element project = (Element)element.getElementsByTagName( "project" ).item( 0 ); // NOI18N
        final String projectName = getElementValue( element, "project" ); // NOI18N
        if( "".equals( projectName )) {
            throw new InvalidConfigFileException( "Server project name is empty string" ); // NOI18N
        }
        sc.setProjectName( projectName );
        sc.setProjectPath( project.getAttribute( "path" )); // NOI18N
        
        final NodeList classes = element.getElementsByTagName( "class" ); // NOI18N
        if( classes.getLength() > 1 ) {
            throw new InvalidConfigFileException( "Server has more than one class tag" ); // NOI18N
        } else if( classes.getLength() == 0 ) {
            throw new InvalidConfigFileException( "Server has no class tag" ); // NOI18N
        }
        final Element clazz = (Element)classes.item( 0 ); // NOI18N
        final String type = clazz.getAttribute( TYPE ); // NOI18N
        if( "".equals( type )) {
            throw new InvalidConfigFileException( "Server class type is empty string" ); // NOI18N
        }
        final String location = clazz.getAttribute( "location" ); // NOI18N
        final String mapping = clazz.getAttribute( "mapping" ); // NOI18N
        final ClassDescriptor cd = new ClassDescriptor( type, location );
        cd.setMapping( mapping );
        sc.setClassDescriptor( cd );
        
        final Properties props = new Properties();
        final NodeList properties = element.getElementsByTagName( "property" );   // NOI18N
//        System.err.println(" properties = " + properties.getLength());
        for( int i = 0; i < properties.getLength(); i++ ) {
            final Element property = (Element)properties.item( i );
            final String key = property.getAttribute( NAME );   // NOI18N
            if( "".equals( key )) {
                throw new IllegalAccessException( "Key name is empty string" ); // NOI18N
            }
            final String value = property.getAttribute( "value" ); // NOI18N
            props.put( key, value );
        }
        sc.setProperties( props );
        
        return sc;
    }
    
    static private List<AbstractService> parseServices( final Element element, final String serviceType ) {
        
        final List<AbstractService> services = new ArrayList<AbstractService>();
        
        if( Configuration.CLASS_TYPE.equals( serviceType )) {
            final ClassService classService = new ClassService();
            final List<ClassData> classDataList = new ArrayList<ClassData>();
            final NodeList classes = element.getElementsByTagName( "class" ); // NOI18N
            for( int i = 0; i < classes.getLength(); i++ ) {
                final ClassData classData = parseClassService((Element)classes.item( i ));
                if( classData == null ) {
                    continue;
                }
                classDataList.add( classData );
            }
            classService.setData(classDataList);
            services.add(classService);
            
        } else if( Configuration.WSDLCLASS_TYPE.equals( serviceType )) {
            final WSDLService wsdlService = new WSDLService();
            final String wsdlURL = element.getAttribute( "url" );    // NOI18N
            final String wsdlFile = element.getAttribute( "file" );   // NOI18N
            final String name = element.getAttribute( NAME );  // NOI18N
            final String type = element.getAttribute( TYPE );  // NOI18N
            wsdlService.setName( name );
            wsdlService.setFile( wsdlFile );
            wsdlService.setUrl( wsdlURL );
            wsdlService.setType( type );
            
            final List<ClassData> portDataList = new ArrayList<ClassData>();
            final NodeList classes = element.getElementsByTagName( "port" ); // NOI18N
            for( int i = 0; i < classes.getLength(); i++ ) {
                final PortData pd = parseWsdlService((Element)classes.item( i ));
                if( pd == null ) continue;
                portDataList.add( pd );
            }
            wsdlService.setData( portDataList );
            services.add(wsdlService);
            
        } else if( Configuration.JSR172_TYPE.equals( serviceType )) {
            // jsr-172
            // TODO: JSR-172 parsing
            final WSDLService wsdlService = new WSDLService();
            final String wsdlURL = element.getAttribute( "url" );    // NOI18N
            final String wsdlFile = element.getAttribute( "file" );   // NOI18N
            wsdlService.setFile( wsdlFile );
            wsdlService.setUrl( wsdlURL );
            services.add(wsdlService);
        }
        
        return services;
    }
    
    static private ClassData parseClassService( final Element element ) {
        
        final String className = element.getAttribute( TYPE );  // NOI18N
        final ClassData cd = new ClassData( className );
        final NodeList methods = element.getElementsByTagName( "method" );    // NOI18N
        final List<OperationData> aMethods = new ArrayList<OperationData>();
        for( int i = 0; i < methods.getLength(); i++ ) {
            final Element method = (Element)methods.item( i );
            final String methodName = method.getAttribute( NAME ); // NOI18N
            final String returnTypeName = ((Element)method.getElementsByTagName( "return" ). // NOI18N
                    item( 0 )).getAttribute( TYPE );    // NOI18N
            final NodeList params = method.getElementsByTagName( "param" );   // NOI18N
            final List<TypeData> aParams = new ArrayList<TypeData>();
            for( int j = 0; j < params.getLength(); j++ ) {
                final Element param = (Element)params.item( j );
                final String paramTypeName = param.getAttribute( TYPE ); // NOI18N
                final String paramName = param.getAttribute( NAME ); // NOI18N
                // TODO: checks
                final TypeData td = new TypeData( paramName, paramTypeName );
                aParams.add( td );
            }
            final OperationData md = new OperationData( methodName );
            md.setReturnType( returnTypeName );
            md.setParameterTypes( aParams );
            aMethods.add( md );
        }
        cd.setOperations( aMethods );
        
        return cd;
    }
    
    static private PortData parseWsdlService( final Element element ) {
        
        final String portType = element.getAttribute( TYPE ); // NOI18N
        final String portName = element.getAttribute( NAME ); // NOI18N
        final PortData portData = new PortData( portType );
        portData.setName(portName);
        final NodeList operations = element.getElementsByTagName( "operation" ); // NOI18N
        final List<OperationData> operationsList = new ArrayList<OperationData>();
        for( int i = 0; i < operations.getLength(); i++ ) {
            final Element operation = (Element)operations.item( i );
            final String name = operation.getAttribute( NAME ); // NOI18N
            final String methodName = operation.getAttribute( "method" ); // NOI18N
            final String returnTypeName = ((Element)operation.getElementsByTagName( "return" ). // NOI18N
                    item( 0 )).getAttribute( TYPE );    // NOI18N
            final NodeList params = operation.getElementsByTagName( "param" );   // NOI18N
            final List<TypeData> paramList = new ArrayList<TypeData>();
            for( int j = 0; j < params.getLength(); j++ ) {
                final Element param = (Element)params.item( j );
                final String paramTypeName = param.getAttribute( TYPE ); // NOI18N
                final String paramName = param.getAttribute( NAME ); // NOI18N
                // TODO: checks
                final TypeData td = new TypeData( paramName, paramTypeName );
                paramList.add( td );
            }
            final OperationData operationData = new OperationData( name );
            operationData.setMethodName( methodName );
            operationData.setReturnType( returnTypeName );
            operationData.setParameterTypes( paramList );
            operationsList.add( operationData );
        }
        portData.setOperations( operationsList );
        
        return portData;
    }
    
    /**
     * Returns value of given element
     *
     * @param element Parent element where is the elementName
     * @param elementName element from which we want the value
     *
     * @return value of the elementName element
     */
    static private String getElementValue( final Element element, final String elementName ) {
        final Element e = (Element)element.getElementsByTagName( elementName ).item( 0 );
        return e.getChildNodes().item( 0 ).getNodeValue();
    }
}

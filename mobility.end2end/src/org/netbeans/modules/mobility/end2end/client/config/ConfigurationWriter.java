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
 * ConfigurationWriter.java
 *
 * Created on July 11, 2005, 10:08 AM
 *
 */
package org.netbeans.modules.mobility.end2end.client.config;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.netbeans.modules.mobility.end2end.classdata.AbstractService;
import org.netbeans.modules.mobility.end2end.classdata.ClassData;
import org.netbeans.modules.mobility.end2end.classdata.MethodData;
import org.netbeans.modules.mobility.end2end.classdata.OperationData;
import org.netbeans.modules.mobility.end2end.classdata.PortData;
import org.netbeans.modules.mobility.end2end.classdata.TypeData;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.openide.ErrorManager;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Michal Skvor
 */
public class ConfigurationWriter {
    
    private final static String TYPE = "type";
    private final static String NAME = "name";
    
    private ConfigurationWriter() {
        //to avoid instantiation
    }
    
    public synchronized static void write( final OutputStream os, final Configuration configuration ) throws Exception {
        final Document doc = XMLUtil.createDocument("wsclientconfig", null, null, null); //NOI18N
        
        // Root Element
        ///Element root = doc.createElement( "wsclientconfig" );
        final Element root = doc.getDocumentElement();
        root.setAttribute( "version", "1.0" ); //NOI18N
        final String serviceType = configuration.getServiceType();
        root.setAttribute( "serviceType", serviceType ); //NOI18N
        
        // Client config
        root.appendChild( serializeClient( doc, configuration.getClientConfiguration()));
        
        // Server config
        if (!Configuration.JSR172_TYPE.equals(configuration.getServiceType())){
            root.appendChild( serializeServer( doc, configuration.getServerConfigutation()));
        }
        
        // services
        root.appendChild( serializeServices( doc, configuration.getServices(), serviceType ));
        
        try {
            XMLUtil.write( doc, os, "UTF-8" ); //NOI18N
        } catch (IOException ioEx){
            ErrorManager.getDefault().notify(ioEx);
        }
    }
    
    private static Node serializeClient( final Document doc, final ClientConfiguration clientConfiguration ) {
        final Element cc = doc.createElement( "client" ); // NOI18N
        final Element project = doc.createElement( "project" ); // NOI18N
        final Node name = doc.createTextNode( clientConfiguration.getProjectName());
        project.appendChild( name );
        cc.appendChild( project );
        
        final Element clazz = doc.createElement( "class" ); // NOI18N
        final ClassDescriptor cd = clientConfiguration.getClassDescriptor();
        clazz.setAttribute( TYPE, cd.getType()); // NOI18N
        clazz.setAttribute( "location", cd.getLocation()); // NOI18N
        cc.appendChild( clazz );
        
        final Properties properties = clientConfiguration.getProperties();
        final Set keys = properties.keySet();
        for( final Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            final String key = i.next();
            final String value = properties.getProperty( key );
            final Element property = doc.createElement( "property" ); // NOI18N
            property.setAttribute( NAME, key ); // NOI18N
            property.setAttribute( "value", value ); // NOI18N
            
            cc.appendChild( property );
        }
        
        return cc;
    }
    
    private static Node serializeServer( final Document doc, final ServerConfiguration serverConfiguration ) {
        final Element cc = doc.createElement( "server" ); // NOI18N
        
        final Element project = doc.createElement( "project" ); // NOI18N
        project.setAttribute( "path", serverConfiguration.getProjectPath()); // NOI18N
        final Node name = doc.createTextNode( serverConfiguration.getProjectName());
        project.appendChild( name );
        cc.appendChild( project );
        
        final Element clazz = doc.createElement( "class" ); // NOI18N
        final ClassDescriptor cd = serverConfiguration.getClassDescriptor();
        clazz.setAttribute( TYPE, cd.getType()); // NOI18N
        clazz.setAttribute( "location", cd.getLocation()); // NOI18N
        clazz.setAttribute( "mapping", cd.getMapping()); // NOI18N
        cc.appendChild( clazz );
        
        final Properties properties = serverConfiguration.getProperties();
        final Set keys = properties.keySet();
        for( final Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            final String key = i.next();
            final String value = properties.getProperty( key );
            final Element property = doc.createElement( "property" ); // NOI18N
            property.setAttribute( NAME, key ); // NOI18N
            property.setAttribute( "value", value ); // NOI18N
            
            cc.appendChild( property );
        }
        
        return cc;
    }
    
    private static Node serializeServices( final Document doc, final List<AbstractService> services, final String serviceType ) {
        final Element ss = doc.createElement( "services" ); // NOI18N
        
        for( int i = 0; i < services.size(); i++ ) {
            final Element s = doc.createElement( "service" ); // NOI18N
            if( Configuration.CLASS_TYPE.equals( serviceType )) {
                final AbstractService service = services.get(i);
                final List<ClassData> classes = service.getData();
                for ( final ClassData classData : classes ) {
                    final Element clazz = doc.createElement( "class" ); // NOI18N
                    clazz.setAttribute( TYPE, classData.getType()); // NOI18N
                    final List<OperationData> methods = classData.getOperations();
                    for ( final MethodData method : methods ) {
                        final Element m = doc.createElement( "method" );  // NOI18N
                        m.setAttribute( NAME, method.getName());  // NOI18N
                        
                        final Element returnType = doc.createElement( "return" ); // NOI18N
                        returnType.setAttribute( TYPE, method.getReturnType()); // NOI18N
                        m.appendChild( returnType );
                        
                        final List<TypeData> returnTypes = method.getParameterTypes();
                        for ( final TypeData td : returnTypes ) {
                            final Element param = doc.createElement( "param" ); // NOI18N
                            param.setAttribute( NAME, td.getName()); // NOI18N
                            param.setAttribute( TYPE, td.getType()); // NOI18N
                            m.appendChild( param );
                        }
                        clazz.appendChild( m );
                    }
                    s.appendChild( clazz );
                }
                ss.appendChild( s );
                
            } else if( Configuration.WSDLCLASS_TYPE.equals( serviceType )) {
                final WSDLService service = (WSDLService)services.get(i);
                s.setAttribute( "url", service.getUrl());   // NOI18N
                s.setAttribute( "file", service.getFile()); // NOI18N
                s.setAttribute( NAME, service.getName()); // NOI18N
                s.setAttribute( TYPE, service.getType()); // NOI18N
                
                final List<ClassData> ports = service.getData();
                for ( final ClassData clData : ports ) {
                	final PortData portData=(PortData)clData;              
                    final Element port = doc.createElement( "port" ); // NOI18N
                    port.setAttribute( TYPE, portData.getType()); // NOI18N
                    port.setAttribute( NAME, portData.getName()); // NOI18N
                    final List<OperationData> operations = portData.getOperations();
                    for ( final OperationData operation : operations ) {
                        final Element m = doc.createElement( "operation" );  // NOI18N
                        m.setAttribute( "method", operation.getName());  // NOI18N
                        m.setAttribute( NAME, operation.getMethodName()); // NOI18N
                        final Element returnType = doc.createElement( "return" ); // NOI18N
                        returnType.setAttribute( TYPE, operation.getReturnType()); // NOI18N
                        m.appendChild( returnType );
                        
                        final List<TypeData> returnTypes = operation.getParameterTypes();
                        for ( final TypeData td : returnTypes ) {
                            final Element param = doc.createElement( "param" ); // NOI18N
                            param.setAttribute( NAME, td.getName()); // NOI18N
                            param.setAttribute( TYPE, td.getType()); // NOI18N
                            m.appendChild( param );
                        }
                        port.appendChild( m );
                    }
                    s.appendChild( port );
                }
                ss.appendChild( s );
            } else if( Configuration.JSR172_TYPE.equals( serviceType )) {
                final WSDLService service = (WSDLService)services.get(i);
                s.setAttribute( "url", service.getUrl());   // NOI18N
                s.setAttribute( "file", service.getFile()); // NOI18N
                ss.appendChild( s );
            }
        }
        
        return ss;
    }
}

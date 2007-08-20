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
 * ProxyGenerator.java
 *
 * Created on August 12, 2005, 11:35 AM
 *
 */
package org.netbeans.modules.mobility.end2end.codegenerator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.classdata.OperationData;
import org.netbeans.modules.mobility.end2end.classdata.PortData;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.client.config.ServerConfiguration;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.mobility.javon.OutputFileFormatter;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 *
 * @author suchys
 */
public class ProxyGenerator {
    final private E2EDataObject dataObject;
    private DataObject createdProxy;
    
    /** Creates a new instance of ProxyGenerator */
    public ProxyGenerator(E2EDataObject dataObject) {
        this.dataObject = dataObject;
    }
    
    public String generate() {
        String generatedProxyName = null;
        try {
            final Configuration configuration = dataObject.getConfiguration();
            final ServerConfiguration sc = configuration.getServerConfigutation();
            final WSDLService wsdlService = (WSDLService)configuration.getServices().get( 0 );
            
            final Sources s = ProjectUtils.getSources(dataObject.getServerProject());
            final SourceGroup sourceGroup = Util.getPreselectedGroup(
                    s.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA ),
                    sc.getClassDescriptor().getLocation());
            final FileObject srcDirectory = sourceGroup.getRootFolder();
            final ClassPath cp = ClassPath.getClassPath( srcDirectory, ClassPath.SOURCE );
            final FileObject fo = cp.getRoots()[0]; //TODO fix me - find src or test folder @see sc.getProjectPath();
            String targetFolderName = ( sc.getClassDescriptor().getPackageName()).replace( '.', '/' );
            String pkgName = sc.getClassDescriptor().getPackageName();
            
            FileObject targetFolder = fo.getFileObject( targetFolderName );
            if( targetFolder == null ){
                targetFolder = FileUtil.createFolder( fo, targetFolderName );
            }
            
            final PortData pd = (PortData)wsdlService.getData().get( 0 );
            String proxyClassName = wsdlService.getType();
            proxyClassName = proxyClassName.substring(proxyClassName.lastIndexOf('.') + 1); // NOI18N
            proxyClassName = proxyClassName + "_Proxy"; // NOI18N
            
            generatedProxyName = pkgName.length() > 0 ? pkgName + "." + proxyClassName : proxyClassName;
                
            FileObject outputFile = targetFolder.getFileObject( proxyClassName, "java" );
            if( outputFile == null ) {
                outputFile = targetFolder.createData( proxyClassName, "java" );
            }
            
            // Get Nodes from the J2EE
            final Project serverProject = Util.getServerProject( configuration );
            Node rootNode = JAXWSClientView.getJAXWSClientView().createJAXWSClientView( serverProject );
            
            for( Node nn : rootNode.getChildren().getNodes()) {
                if( nn.getDisplayName().equals( wsdlService.getName()))
                    rootNode = nn;
            }
                        
            FileObject generatedClientFO = 
                    serverProject.getProjectDirectory().getFileObject( "build/generated/wsimport/client/" );
            // Add all paths to the ClasspathInfo structure
            List<ClasspathInfo> classpaths = Collections.singletonList( ClasspathInfo.create( generatedClientFO ));
            // Get the registry for all available classes
            ClassDataRegistry registry = ClassDataRegistry.getRegistry( ClassDataRegistry.DEFAULT_PROFILE, classpaths );
            
            String servicePackage = null;
            String portClassName = null;
            String portGetterName = null;
            List<MethodData> methodList = new ArrayList<MethodData>();
            for( Node serviceNode : rootNode.getChildren().getNodes()) {                
                for( Node portNode : serviceNode.getChildren().getNodes()) {
                    WsdlPort wsdlPort = portNode.getLookup().lookup( WsdlPort.class );
                    if( wsdlPort == null ) break;
                    portGetterName = wsdlPort.getPortGetter();
                    portClassName = wsdlPort.getJavaName();
                    String nnn = wsdlPort.getJavaName();
                    org.netbeans.modules.mobility.e2e.classdata.ClassData cd = registry.getClassData( wsdlPort.getJavaName());
                    servicePackage = cd.getPackage();
                    for( Node operationNode : portNode.getChildren().getNodes()) {
                        WsdlOperation wsdlOperation = operationNode.getLookup().lookup( WsdlOperation.class );
                        org.netbeans.modules.mobility.e2e.classdata.MethodData methodData = null;
                        for( org.netbeans.modules.mobility.e2e.classdata.MethodData md : cd.getMethods()) {
                            if( md.getName().equals( wsdlOperation.getJavaName())) {
                                methodList.add( md );
                            }
                        }
                    }
                }
            }
            
            OutputFileFormatter off = new OutputFileFormatter( outputFile );

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine eng = mgr.getEngineByName( "freemarker" );
            Bindings bind = eng.getContext().getBindings( ScriptContext.ENGINE_SCOPE );

            FileObject template = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject( "Templates/Server/Proxy.java" );
            
            bind.put( "proxyClassPackage", sc.getClassDescriptor().getPackageName());
            bind.put( "proxyClassName", proxyClassName );
            bind.put( "servicePackage", servicePackage );
            bind.put( "service", wsdlService );
            
            bind.put( "methods", methodList );
            bind.put( "portGetterName", portGetterName );
            bind.put( "portClassName", portClassName );
            
            Writer w = null;
            Reader is = null;
            try {
                w = new StringWriter();
                is = new InputStreamReader( template.getInputStream());

                eng.getContext().setWriter( w );
                eng.getContext().setAttribute( FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE );
                eng.getContext().setAttribute( ScriptEngine.FILENAME, template.getNameExt(), ScriptContext.ENGINE_SCOPE );

                eng.eval( is );
            } catch( Exception e ) {
                e.printStackTrace();
            } finally {
                if( w != null ) {
                    off.write( w.toString());
                    w.close();
                }
                if( is != null ) is.close();
                off.close();
            }                  
        } catch( Exception e ) {                
            ErrorManager.getDefault().notify( e );
            return null;
        }
        return generatedProxyName;
        
    }    
}

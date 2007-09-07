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
 * Jsr172Generator.java
 *
 * Created on August 29, 2005, 1:57 PM
 *
 */
package org.netbeans.modules.mobility.jsr172.generator;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2Java;
import org.netbeans.modules.e2e.api.wsdl.wsdl2java.WSDL2JavaFactory;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
//import org.netbeans.jmi.javamodel.Resource;
//import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.mobility.end2end.client.config.ClassDescriptor;
import org.netbeans.modules.mobility.end2end.client.config.ClientConfiguration;
//import org.netbeans.modules.mobility.jsr172.validator.WSIValidator;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.spi.mobility.end2end.ServiceGeneratorResult;
//import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author suchys
 */
public class Jsr172Generator {
    
    private Jsr172Generator () {
        //To avoid instantiation
    }
        
    public static ServiceGeneratorResult generate(final E2EDataObject e2EDataObject) {
        ServiceGeneratorResult result = null;
                        
        final ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage( Jsr172Generator.class, "MSG_GeneratingJsr172" ));
        ph.start();
        ph.switchToIndeterminate();        
//        try {            
            final ClientConfiguration configuration = e2EDataObject.getConfiguration().getClientConfiguration();
            final ClassDescriptor cd = configuration.getClassDescriptor();
            final WSDLService service = (WSDLService) e2EDataObject.getConfiguration().getServices().get(0);
            String file = service.getFile();
            file = (cd.getPackageName().replace('.','/')) + '/' + file; //NOI18N
            final Sources sources = e2EDataObject.getClientProject().getLookup().lookup( Sources.class );
            final SourceGroup sg = sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA )[0];
            final FileObject wsdlPosition = sg.getRootFolder().getFileObject(file);
            if (wsdlPosition == null){
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(Jsr172Generator.class, "MSG_file_disapeared", service.getFile()), NotifyDescriptor.ERROR_MESSAGE)); //NOI18N
                return null;
            }
            //ask for folder otherwise MDR and FileSystems might be confused by external generator
            final FileObject refFolder = sg.getRootFolder().getFileObject( cd.getPackageName().replace( '.','/' ));
            refFolder.getChildren( true );
//            //--
            final File f = FileUtil.toFile( wsdlPosition );
            final String configFileData = MessageFormat.format( CONFIG_FILE_BODY, new Object[]{ f.getAbsolutePath(), cd.getPackageName() } );
            
            final String wsdlUrl = f.toURI().toString();
//            System.err.println(" - WSDL url - " + wsdlUrl );
                                     
            WSDL2Java.Configuration config = new WSDL2Java.Configuration();
            config.setWSDLFileName( wsdlUrl );
            config.setOutputDirectory( FileUtil.toFile( sg.getRootFolder()).getAbsolutePath());
            config.setPackageName( cd.getPackageName());
            WSDL2Java wsdl2java = WSDL2JavaFactory.getWSDL2Java( config );
            
            final Properties properties = configuration.getProperties();
            if( properties.getProperty( "DataBinding" ) != null ) {
                config.setGenerateDataBinding( properties.getProperty( "DataBinding" ).equals( "true" ));
            }
            
            boolean generationResult = wsdl2java.generate();
            if( generationResult ) {
                StatusDisplayer.getDefault().setStatusText( NbBundle.getMessage( Jsr172Generator.class,"MSG_Success" )); //NOI18N                
                
                // Append DataBinding library whether the data binding is enabled
                if( config.getGenerateDataBinding()) {
                    Util.registerDataBindingLibrary(e2EDataObject.getClientProject());
                }
            } else {
                StatusDisplayer.getDefault().setStatusText( NbBundle.getMessage( Jsr172Generator.class,"MSG_Failure" )); //NOI18N
            }
            
            ph.finish();
        
        return result;
    }
    
//    private static class CT extends CompileTool172 {
//        CT(OutputStream os){
//            super(os, "wscompile"); //NOI18N
//            doCompilation = false;
//        }
//    }
//    
    public final static String CONFIG_FILE_BODY =
            "<?xml version=''1.0'' encoding=''UTF-8'' ?>\n" + //NOI18N
            "\t<configuration xmlns=''http://java.sun.com/xml/ns/jax-rpc/ri/config''>\n" + //NOI18N
            "\t\t<wsdl location=''{0}'' packageName=''{1}''>\n" + //NOI18N
            "\t</wsdl>\n" + //NOI18N
            "</configuration>\n"; //NOI18N
    
}

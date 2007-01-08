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
            System.err.println(" - WSDL url - " + wsdlUrl );
            
//            System.setProperty( "http.proxyHost", "webcache.holland.sun.com" );
//            System.setProperty( "http.proxyPort", "8080" );
                        
            WSDL2Java.Configuration config = new WSDL2Java.Configuration();
            config.setWSDLFileName( wsdlUrl );
            config.setOutputDirectory( sg.getRootFolder().getPath());
            config.setPackageName( cd.getPackageName());
            WSDL2Java wsdl2java = WSDL2JavaFactory.getWSDL2Java( config );
            
            wsdl2java.generate();
//            File tempFile = null;
//            FileLock flck = null;
//            try {
//                ph.progress(NbBundle.getMessage( Jsr172Generator.class, "MSG_GeneratingTempJsr172" )); //NOI18N
//                tempFile = File.createTempFile("jsr172compile", "xml"); //NOI18N
//                final FileObject tempFo = FileUtil.toFileObject(FileUtil.normalizeFile(tempFile));
//                flck = tempFo.lock();
//                final BufferedOutputStream bos = new BufferedOutputStream(tempFo.getOutputStream(flck));
//                bos.write(configFileData.getBytes());
//                bos.close();
//            } catch (IOException ex) {
//                ErrorManager.getDefault().notify(ex);
//            } finally {
//                if (flck != null){
//                    flck.releaseLock();
//                }
//            }
//            
//            final Properties properties = configuration.getProperties();
//            boolean cldc10 = false;
//            if( "true".equals( properties.getProperty( "cldc10" ))) { //NOI18N
//                cldc10 = true;
//            }
//            final String[] input = {
//                "-gen:client", //NOI18N
//                "-keep", //NOI18N
//                "-s", //NOI18N
//                FileUtil.toFile(sg.getRootFolder()).getAbsolutePath(),
//                "-d", //NOI18N
//                tempFile.getParent(),
//                (!cldc10 ? "-cldc1.1" : "-cldc1.0" ), //NOI18N
//                "-f:wsi", //NOI18N
//                "-verbose", //NOI18N
//                tempFile.getAbsolutePath()
//            };
//            ph.progress(NbBundle.getMessage( Jsr172Generator.class, "MSG_GeneratingStubs172" )); //NOI18N
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            final CompileTool172 c172 = new CT(baos);
//            c172.run(input);
//            
//            final Properties p = new Properties();
//            baos.reset();
//            //get the model here
//            final String[] input2 = {"-gen", tempFile.getAbsolutePath()}; //NOI18N
//            final WSIValidator wsiValidator = new WSIValidator( baos, "wscompile", p ); //NOI18N
//            wsiValidator.run( input2 );
//            baos.close();
//            baos = null;
//            
//            refFolder.refresh(false);
//            final FileObject[] children = refFolder.getChildren();
//            JavaModel.getJavaRepository().beginTrans(false);
//            try {
//                String expectedName = ""; //NOI18N
//                if (wsiValidator.getJavaInterfaces().size() > 0){
//                    final JavaInterface jin = wsiValidator.getJavaInterfaces().iterator().next();
//                    expectedName = jin.getRealName() + "_Stub"; //NOI18N
//                }
//                final List<Method> methods = new ArrayList<Method>();
//                final List<Type> params = new ArrayList<Type>();
//                for (FileObject child : children ) {
//                    child.refresh();
//                    if ("java".equals(child.getExt())){
//                        final Resource resource = JavaModel.getResource(child);
//                        final JavaClass jc = (JavaClass) resource.getClassifiers().get(0);
//                        if (!jc.getName().equals(expectedName)){
//                            continue;
//                        }
//                        methods.clear();
//                        for ( final JavaInterface ji : wsiValidator.getJavaInterfaces() ) {
//                            for ( final JavaMethod jm : (List<JavaMethod>)ji.getMethodsList() ) {
//                                final String name = jm.getName();
//                                params.clear();
//                                for ( final JavaParameter elem : (List<JavaParameter>)jm.getParametersList() ) {
//                                    params.add(JavaModel.getDefaultExtent().getType().resolve(elem.getType().getName()));
//                                }
//                                methods.add(jc.getMethod(name, params, false));
//                            }
//                        }
//                        result = new ServiceGeneratorResult(jc, methods.toArray(new Method[methods.size()]), null );
//                        break;
//                    }
//                }
//            } catch (Exception ex){
//                ErrorManager.getDefault().notify(ex);
//            } finally {
//                JavaModel.getJavaRepository().endTrans();
//                if (tempFile != null)
//                    tempFile.delete();
//            }
//            
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Jsr172Generator.class,"MSG_Success")); //NOI18N
//        } catch (Exception ex) {
//            ErrorManager.getDefault().notify(ex);
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Jsr172Generator.class,"MSG_Failure")); //NOI18N
//            return null;
//        } finally {
            ph.finish();
//        }
        
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

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
 * ConnectionGenerator.java
 *
 * Created on August 22, 2005, 5:49 PM
 *
 */
package org.netbeans.modules.mobility.end2end.codegenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.e2e.mapping.Javon;
import org.netbeans.modules.mobility.e2e.mapping.JavonMappingImpl;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.client.config.ClassDescriptor;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.spi.mobility.end2end.ServiceGeneratorResult;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author suchys
 */
public class ConnectionGenerator {
    
    private ConnectionGenerator()
    {
        //to avoid instantiation
    }
    
    public static synchronized ServiceGeneratorResult generate( final E2EDataObject dataObject ) {        
        if ( dataObject.getServerProject() == null ){
            final NotifyDescriptor.Message dd  =
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage( E2EDataObject.class, "ERR_ServerProjectNotOpened", // NOI18N
                    dataObject.getConfiguration().getServerConfigutation().getProjectName()));
            DialogDisplayer.getDefault().notify( dd );
            if( Util.openProject(dataObject.getConfiguration().getServerConfigutation().getProjectPath()) == null ){
                return null;
            }
        }
        
        // Call save before generate
        final SaveCookie saveCookie = dataObject.getCookie( SaveCookie.class );
        if( saveCookie != null ) {
            try {
                saveCookie.save();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify( ex );
            }
        }
        
        // Get configuration
        final Configuration config = dataObject.getConfiguration();
        if( config == null ) {
            final NotifyDescriptor.Message dd  = new NotifyDescriptor.Message(
                    NbBundle.getMessage( E2EDataObject.class, "ERR_ConfigurationFileCorrupted" )); // NOI18N
            DialogDisplayer.getDefault().notify(dd);
            return null;
        }
        
        if( Configuration.WSDLCLASS_TYPE.equals( config.getServiceType())){
            final FileObject fo = dataObject.getServerProject().getProjectDirectory().getFileObject( "build/generated/wsimport/" ); //NOI18N
            if( fo == null ) {
                DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message( NbBundle.getMessage( ConnectionGenerator.class, "MSG_WebProjectNotBuilt" )));
                return null;
            }
        }
        
        final ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage( ConnectionGenerator.class, "MSG_GeneratingJavonBridge" )); // NOI18N
        ph.start();
        ph.switchToIndeterminate();
        ph.progress( NbBundle.getMessage( ConnectionGenerator.class, "MSG_GeneratingProxyStubs" )); // NOI18N
        // FIXME: check for proper type
//        config.getServices();
//        
        if( Configuration.WSDLCLASS_TYPE.equals( config.getServiceType())) {
            ph.progress( NbBundle.getMessage( ConnectionGenerator.class, "MSG_GeneratingProxyStubs" ));
            final ProxyGenerator pg = new ProxyGenerator(dataObject);
            final String className = pg.generate();
            if( className == null ) {
                ph.finish();
                return null;
            }
            config.getServices().get( 0 ).getData().get( 0 ).setProxyClassType( className );
        }
//        JavonOutput[] outputs;
//        Type type = null;
//        
//        final InputOutput io = IOProvider.getDefault().getIO(
//                NbBundle.getMessage( ConnectionGenerator.class, "LBL_JavonTab" ) // NOI18N
//                , true);
//        final OutputWriter ow = io.getOut();
        try {
            ph.progress( NbBundle.getMessage( ConnectionGenerator.class, "MSG_ScanningDataStructures" )); // NOI18N
            
            final JavonMappingImpl mapping = dataObject.getMapping();
//            //ph.progress(70);
            ph.progress( NbBundle.getMessage( ConnectionGenerator.class, "MSG_CreatingJavaFiles" )); // NOI18N
//            
            Javon javon = new Javon( mapping );
            javon.generate( ph );
//            Streams.setOut(ow);
//            Streams.setErr(ow);
//            outputs = new Main().run( mapping, "" ); // NOI18N
//            
//            for( int j = 0; j < outputs.length; j++ ) {
//                final String list[] = outputs[j].getCreatedFiles();
//                for( int i = 0; i < list.length; i++ ) {
//                    final File f = new File(list[i]);
//                    final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
//                    fo.refresh();
//                    JavaModel.getResource(fo);
//                }
//            }
//            //add servlet to container
//            Util.addServletToWebProject(dataObject.getServerProject(), dataObject.getConfiguration().getServerConfigutation().getClassDescriptor().getType());
//            
//            final ClassDescriptor clientClassDescriptor = dataObject.getConfiguration().getClientConfiguration().getClassDescriptor();
//            final Sources s = ProjectUtils.getSources(dataObject.getClientProject());
//            final SourceGroup sourceGroup = Util.getPreselectedGroup(
//                    s.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA ),
//                    clientClassDescriptor.getLocation());
//            final FileObject srcDirectory = sourceGroup.getRootFolder();
//            final ClassPath cp = ClassPath.getClassPath(srcDirectory,ClassPath.SOURCE);
//            JavaModel.getJavaRepository().beginTrans(false);
//            try {
//                JavaModel.setClassPath(cp);
//                type = JavaModel.getDefaultExtent().getType().resolve(clientClassDescriptor.getType());
//            } catch (Exception e){
//                ErrorManager.getDefault().notify(e);
//            } finally {
//                JavaModel.getJavaRepository().endTrans();
//            }
        } catch( Exception e ) {
//            ow.print(e.getMessage());
//            io.select();
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
        } finally {
            ph.finish();
        }
//        if (type != null){
//            //ow.println("Run / Redeploy Web Project to get changes reflected!");
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage( ConnectionGenerator.class, "MSG_SuccessGenerated" )); // NOI18N
//        } else {
//            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage( ConnectionGenerator.class, "MSG_Failure" )); // NOI18N
//            return null;
//        }
//        final JavaClass resultClass = (JavaClass)type;
//        final List<Feature> features = resultClass.getFeatures();
//        final List<Method> methods = new ArrayList<Method>();
//        for ( final Feature elem : features ) {
//            if (elem instanceof Method){
//                final Method m = (Method)elem;
//                if ("getGroupedResults".equals(m.getName())){ //NOI18N //not supported
//                    continue;
//                }
//                if (m.getName().endsWith("Grouped")){ //NOI18N //not supported
//                    continue;
//                }
//                if ( Modifier.isPublic(m.getModifiers()) ){
//                    methods.add(m);
//                }
//            }
//        }
//        return new ServiceGeneratorResult(resultClass,
//                methods.toArray(new Method[methods.size()]),
//                Util.getServerURL(dataObject.getServerProject(), dataObject.getConfiguration()));
        return null;
    }
}

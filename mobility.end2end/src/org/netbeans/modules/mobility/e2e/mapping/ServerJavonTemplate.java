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

package org.netbeans.modules.mobility.e2e.mapping;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.javon.JavonTemplate;
import org.netbeans.modules.mobility.javon.OutputFileFormatter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class ServerJavonTemplate extends JavonTemplate {
    
    private static final String SERVLET     = "server-servlet";
    private static final String GATEWAYS    = "server-gateways";
    private static final String INVOCATION  = "server-invocation";
    private static final String UTILITY     = "server-utility";
    
    
    private static final String[] OUTPUTS = { SERVLET, GATEWAYS, INVOCATION, UTILITY };
    private static final Set<String> outputSet;
    
    static {
        outputSet = new HashSet();
        for( String output : OUTPUTS ) {
            outputSet.add( output );
        }        
    }
    
    /** Creates a new instance of ServerJavonTemplate 
     * 
     * @param mapping 
     */
    public ServerJavonTemplate( JavonMapping mapping ) {
        super( mapping );        
    }
    
    public Set<String> getTargets() {
        return new HashSet( Arrays.asList( OUTPUTS ));
    }

    public boolean generateTarget( ProgressHandle ph, String target ) {
        if( !outputSet.contains( target )) return false;
        
        String templateName = "";
        String outputDirectoryName = mapping.getServerMapping().getOutputDirectory();
        String outputFileName = "";
        
        if( SERVLET.equals( target )) {
            templateName = "Templates/Server/Servlet.java";     // NOI18N
            outputFileName = mapping.getServerMapping().getClassName();
            ph.progress( NbBundle.getMessage( ServerJavonTemplate.class, "MSG_Servlet" ));
        } else if( GATEWAYS.equals( target )) {
            templateName = "Templates/Server/Gateways.java"; // NOI18N
            outputFileName = "JavonGateways"; // NOI18N
            ph.progress( NbBundle.getMessage( ServerJavonTemplate.class, "MSG_Gateways" ));
        } else if( INVOCATION.equals( target )) {
            templateName = "Templates/Server/InvocationAbstraction.java"; // NOI18N
            outputFileName = "InvocationAbstraction"; // NOI18N
            ph.progress( NbBundle.getMessage( ServerJavonTemplate.class, "MSG_Invocation" ));
        } else if( UTILITY.equals( target )) {
            templateName = "Templates/Server/Utility.java"; // NOI18N
            outputFileName = "Utility"; // NOI18N
            ph.progress( NbBundle.getMessage( ServerJavonTemplate.class, "MSG_Utility" ));
        }
        try {            
            mapping.setProperty( "target", "server" ); // NOI18N
            
            FileObject outputDir = FileUtil.toFileObject( FileUtil.normalizeFile( new File( outputDirectoryName )));
            outputDir = outputDir.getFileObject( mapping.getServerMapping().getPackageName().replace( '.', '/' ));

            FileObject outputFile = outputDir.getFileObject( outputFileName, "java" );
            if( outputFile == null ) {
                outputFile = outputDir.createData( outputFileName, "java" );
            }
            OutputFileFormatter off = new OutputFileFormatter( outputFile );

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine eng = mgr.getEngineByName( "freemarker" );
            Bindings bind = eng.getContext().getBindings( ScriptContext.ENGINE_SCOPE );

            FileObject template = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject( templateName );        
            bind.put( "mapping", mapping );
            bind.put( "registry", mapping.getRegistry());
            
            // Prepare return and parameter types
            Set<ClassData> returnTypes = new HashSet<ClassData>();
            Set<ClassData> parameterTypes = new HashSet<ClassData>();
            
            Set<ClassData> returnInstanceTypes = new HashSet<ClassData>();
            Set<ClassData> parameterInstanceTypes = new HashSet<ClassData>();
            
            Utils utils = new Utils( mapping.getRegistry());
            
            for( JavonMapping.Service service : mapping.getServiceMappings()) {
                returnTypes.addAll( service.getReturnTypes());
                parameterTypes.addAll( service.getParameterTypes());
            }
            bind.put( "returnTypes", returnTypes );
            bind.put( "parameterTypes", parameterTypes );
            
            returnInstanceTypes = utils.filterInstances( returnTypes );
            parameterInstanceTypes = utils.filterInstances( parameterTypes );
            bind.put( "returnInstanceTypes", returnInstanceTypes );
            bind.put( "parameterInstanceTypes", parameterInstanceTypes );
            
            Set<ClassData> instanceTypes = new HashSet<ClassData>();
            instanceTypes.addAll( returnTypes );
            instanceTypes.addAll( parameterTypes );
            instanceTypes = utils.filterInstances( instanceTypes );
            bind.put( "instanceTypes", instanceTypes );
            
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
//                            System.err.println( "" + w.toString());
                    w.close();
                }
                if( is != null ) is.close();
                off.close();
            }                  
        } catch( Exception e ) {                
            e.printStackTrace();
            return false;
        }
        
        // Register servlet to the project
        final OpenProjects openProject = OpenProjects.getDefault();
        final Project[] openedProjects = openProject.getOpenProjects();
        Project serverProject = null;
        for ( final Project p : openedProjects ) {
            final ProjectInformation pi = p.getLookup().lookup( ProjectInformation.class );
            final String webProjectName = pi.getName();
            if( mapping.getServerMapping().getProjectName().equals( webProjectName )) {
                serverProject = p;
            }
        }
        if( serverProject != null ) {
            System.err.println(" ~ " + mapping.getServletURL());
            Util.addServletToWebProject( serverProject, mapping );
        }
        
        return true;
    }
}

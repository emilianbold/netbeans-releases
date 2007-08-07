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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
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
public class ClientJavonTemplate extends JavonTemplate {
        
    private static final String CLIENT_OUTPUT = "client-template";
    
    /**
     * 
     * @param mapping 
     */
    public ClientJavonTemplate( JavonMapping mapping ) {
        super( mapping );
    }
    
    public Set<String> getTargets() {
        Set<JavonMapping.Service> services = mapping.getServiceMappings();
        Set<String> targets = new HashSet<String>();
        for( JavonMapping.Service service : services ) {
            targets.add( service.getType());
        }
        return Collections.unmodifiableSet( targets );
    }

    public boolean generateTarget( ProgressHandle ph, String target ) {
        if( mapping.getServiceMapping( target ) != null ) {
            ph.progress( NbBundle.getMessage( ClientJavonTemplate.class, "MSG_Client" ));   // NOI18N
            try {
                mapping.setProperty( "target", "client" );
                
                JavonMapping.Service service = mapping.getServiceMapping( target );
                FileObject outputDir = FileUtil.toFileObject( FileUtil.normalizeFile( 
                        new File( mapping.getClientMapping().getOutputDirectory())));
                outputDir = outputDir.getFileObject( mapping.getClientMapping().getPackageName().replace( '.', '/' ));

                FileObject outputFile = outputDir.getFileObject( service.getClassName(), "java" );
                if( outputFile == null ) {
                    outputFile = outputDir.createData( service.getClassName(), "java" );
                }
                OutputFileFormatter off = new OutputFileFormatter( outputFile );

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine eng = mgr.getEngineByName( "freemarker" );
                Bindings bind = eng.getContext().getBindings( ScriptContext.ENGINE_SCOPE );

                FileObject template = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject( "Templates/Client/Client.java" );        
                bind.put( "mapping", mapping );
                bind.put( "registry", mapping.getRegistry());
                bind.put( "returnTypes", service.getReturnTypes());
                bind.put( "parameterTypes", service.getParameterTypes());
                bind.put( "service", service );
                bind.put( "utils", new Utils( mapping.getRegistry()));

                // Compute imports for JavaBeans
                Set<String> imports = new HashSet<String>();
                String getClientPackage = mapping.getClientMapping().getPackageName();
                for( ClassData cd : service.getParameterTypes()) {
                    if( cd.isPrimitive()) continue;
                    imports.add( cd.getFullyQualifiedName());
                }
                for( ClassData cd : service.getReturnTypes()) {
                    if( cd.isPrimitive()) continue;
                    imports.add( cd.getFullyQualifiedName());
                }
                bind.put( "imports", imports );
                
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
        }
        return true;
    }
}

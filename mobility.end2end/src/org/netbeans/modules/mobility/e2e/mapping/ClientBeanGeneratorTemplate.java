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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.javon.JavonMapping.Service;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.javon.JavonTemplate;
import org.netbeans.modules.mobility.javon.OutputFileFormatter;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class ClientBeanGeneratorTemplate extends JavonTemplate {

    private static final String BEANS_OUTPUT = "client-template";
    
    public ClientBeanGeneratorTemplate( JavonMapping mapping ) {
        super( mapping );
    }

    public Set<String> getTargets() {
        Set<String> targets = new HashSet( 1 );
        targets.add( BEANS_OUTPUT );
        return targets;
    }

    public boolean generateTarget( ProgressHandle ph, String target ) {
        if( BEANS_OUTPUT.equals( target )) {
            ph.progress( NbBundle.getMessage( ClientBeanGeneratorTemplate.class, "MSG_Bean_Generation" ));
            Set<Service> services = mapping.getServiceMappings();
            Map<String, ClassData> types = new HashMap<String, ClassData>();
            for( Service service : services ) {
                for( ClassData type : service.getSupportedTypes()) {
                    types.put( type.getFullyQualifiedName(), type);
                }
            }
            ph.start( types.keySet().size());
            int progress = 0;
            for( String typeName : types.keySet()) {
                progress++;
                ClassData type = types.get( typeName );
                JavonSerializer serializer = mapping.getRegistry().getTypeSerializer( type );
                if( serializer instanceof BeanTypeSerializer ) {
                    try {
                        BeanTypeSerializer bts = (BeanTypeSerializer) serializer;
//                        System.err.println(" - generating type: " + typeName);

                        FileObject outputDir = FileUtil.toFileObject( FileUtil.normalizeFile( 
                            new File( mapping.getClientMapping().getOutputDirectory())));                    
                        String packageName = type.getPackage();
                        StringTokenizer token = new StringTokenizer( packageName, "." );
                        FileObject destination = FileUtil.createFolder( outputDir, packageName.replace( '.', '/' ));
                        FileObject beanFile = destination.getFileObject( type.getName(), "java" );
                        if( beanFile == null ) {
                                beanFile = destination.createData( type.getName(), "java" );
                        }
                        
                        generateBean( beanFile, type );
                        ph.progress( progress );
                    } catch( IOException e ) {
                        ErrorManager.getDefault().notify( e );
                    }
                }
            }
            ph.switchToIndeterminate();
            return true;
        }
        return false;
    }

    private boolean generateBean( FileObject outputFile, ClassData beanType ) {
        try {
            OutputFileFormatter off = new OutputFileFormatter( outputFile );

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine eng = mgr.getEngineByName( "freemarker" );
            Bindings bind = eng.getContext().getBindings( ScriptContext.ENGINE_SCOPE );

            FileObject template = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject( "Templates/Client/Bean.java" );        
            bind.put( "mapping", mapping );
            bind.put( "registry", mapping.getRegistry());
            bind.put( "bean", beanType );
            bind.put( "createStubs", mapping.getProperty( "create-stubs" ).equals( "true" ));
            bind.put( "utils", new Utils( mapping.getRegistry()));
            
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
            return false;
        }
        return true;
    }
}

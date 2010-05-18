/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.mobility.e2e.mapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.end2end.output.OutputLogger;
import org.netbeans.modules.mobility.end2end.output.OutputLogger.LogLevel;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.javon.JavonMapping.Service;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.javon.JavonTemplate;
import org.netbeans.modules.mobility.javon.OutputFileFormatter;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class ClientBeanGeneratorTemplate extends JavonTemplate {

    private static final String BEANS_OUTPUT = "client-template"; //NOI18N
    
    public ClientBeanGeneratorTemplate( JavonMapping mapping ) {
        super( mapping );
    }

    public Set<String> getTargets() {
        Set<String> targets = new HashSet<String>( 1 );
        targets.add( BEANS_OUTPUT );
        return targets;
    }

    public boolean generateTarget( ProgressHandle ph, String target ) {
        boolean result = true;
        if( BEANS_OUTPUT.equals( target )) {
            mapping.setProperty( "target", "client" );
            String beanGeneration = NbBundle.getMessage( 
                    ClientBeanGeneratorTemplate.class, "MSG_Bean_Generation" ); //NOI18N
            ph.progress( beanGeneration );
            OutputLogger.getInstance().log( beanGeneration) ;
            Set<Service> services = mapping.getServiceMappings();
            Map<String, ClassData> types = new HashMap<String, ClassData>();
            for( Service service : services ) {
                for( ClassData type : service.getSupportedTypes()) {
                    types.put( type.getFullyQualifiedName(), type);
                }
            }
//            ph.start( types.keySet().size());
            int progress = 0;
            for( String typeName : types.keySet()) {
                ClassData type = types.get( typeName );
                JavonSerializer serializer = mapping.getRegistry().getTypeSerializer( type );
                if (serializer instanceof BeanTypeSerializer) {
                    // BeanTypeSerializer bts = (BeanTypeSerializer) serializer;
                    // System.err.println(" - generating type: " + typeName);

                    FileObject outputDir = FileUtil.toFileObject(FileUtil
                            .normalizeFile(new File(mapping.getClientMapping()
                                    .getOutputDirectory())));
                    String packageName = type.getPackage();
                    // StringTokenizer token = new StringTokenizer( packageName,
                    // "." ); //NOI18N
                    String packageFolder = packageName.replace('.', '/');// NOI18N
                    OutputLogger.getInstance()
                            .log(
                                    MessageFormat.format(NbBundle.getMessage(
                                            ClientBeanGeneratorTemplate.class,
                                            "MSG_DestinationFolderCreation"),
                                            new File(
                                                    FileUtil.toFile(outputDir),
                                                    packageFolder)));// NOI18N
                    FileObject destination = null;
                    try {
                        destination = FileUtil.createFolder(outputDir,
                                packageFolder);
                    }
                    catch (IOException e) {
                        OutputLogger.getInstance().log(
                                LogLevel.ERROR,
                                MessageFormat.format(NbBundle.getMessage(
                                        ClientBeanGeneratorTemplate.class,
                                        "MSG_FailFolderCreation"), new File(
                                        FileUtil.toFile(outputDir),
                                        packageFolder)));// NOI18N
                        generationFailed(e, type.getName());
                        return false;
                    }
                    FileObject beanFile = destination.getFileObject(type
                            .getName(), "java"); // NOI18N
                    if (beanFile == null) {
                        OutputLogger.getInstance().log(
                                MessageFormat.format(NbBundle.getMessage(
                                        ClientBeanGeneratorTemplate.class,
                                        "MSG_BeanFileCreation"), new File(
                                        FileUtil.toFile(outputDir), type
                                                .getName())));// NOI18N
                        try {
                            beanFile = destination.createData(type.getName(),
                                    "java"); // NOI18N
                        }
                        catch (IOException e) {
                            OutputLogger.getInstance().log(
                                    LogLevel.ERROR,
                                    MessageFormat.format(NbBundle.getMessage(
                                            ClientBeanGeneratorTemplate.class,
                                            "MSG_FailBeanCreation"), new File(
                                            FileUtil.toFile(outputDir), type
                                                    .getName())));// NOI18N
                            generationFailed(e, FileUtil.toFile(outputDir));
                            return false;
                        }
                    }
                    if ( !generateBean(beanFile, type) ){
                        result = false;
                    }
                    if ("true".equals(mapping.getProperty("databinding"))
                            && beanFile != null)
                    {
                        Project p = FileOwnerQuery.getOwner(beanFile);
                        OutputLogger.getInstance().log(
                                NbBundle.getMessage(
                                        ClientBeanGeneratorTemplate.class,
                                        "MSG_RegisterDatabindingLibrary"));// NOI18N
                        Util.registerDataBindingLibrary(p);
                    }
                    // ph.progress( progress );
                    OutputLogger.getInstance().log(
                            MessageFormat.format(NbBundle.getMessage(
                                    ClientBeanGeneratorTemplate.class,
                                        "MSG_BeanGenerated"),
                                        FileUtil.toFile(beanFile)));
                }
                progress++;
            }
            ph.switchToIndeterminate();
        }
        return result;
    }

    private boolean generateBean( FileObject outputFile, ClassData beanType ) {
        try {
            OutputFileFormatter off = new OutputFileFormatter( outputFile );

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine eng = mgr.getEngineByName( "freemarker" ); //NOI18N
            Bindings bind = eng.getContext().getBindings( ScriptContext.ENGINE_SCOPE );

            FileObject template = FileUtil.getConfigFile( "Templates/Client/Bean.java" ); //NOI18N
            OutputLogger.getInstance().log(
                    NbBundle.getMessage(ClientBeanGeneratorTemplate.class,
                            "MSG_ConfigureBindings"));//NOI18N
            bind.put( "mapping", mapping ); //NOI18N
            bind.put( "registry", mapping.getRegistry()); //NOI18N
            bind.put( "bean", beanType ); //NOI18N
            bind.put( "createStubs", mapping.getProperty( "create-stubs" ).equals( "true" )); //NOI18N
            bind.put( "utils", new Utils( mapping.getRegistry())); //NOI18N
            
            Writer w = null;
            Reader is = null;
            OutputLogger.getInstance().log(MessageFormat.format(
                    NbBundle.getMessage(ClientBeanGeneratorTemplate.class,
                        "MSG_GenerateBean" ),FileUtil.toFile(outputFile)));//NOI18N
            for( Entry<String, String> entry :beanType.getInvaidFields().entrySet()){
                String field = entry.getKey();
                String type = entry.getValue();
                OutputLogger.getInstance().log( LogLevel.WARNING, 
                        MessageFormat.format(
                                NbBundle.getMessage(ClientBeanGeneratorTemplate.class,
                                    "MSG_InvalidField" ), beanType.getClassName(),
                                    field , type ));//NOI18N
            }
            try {
                w = new StringWriter();
                is = new InputStreamReader( template.getInputStream());

                eng.getContext().setWriter( w );
                eng.getContext().setAttribute( FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE );
                eng.getContext().setAttribute( ScriptEngine.FILENAME, template.getNameExt(), ScriptContext.ENGINE_SCOPE );

                eng.eval( is );
            }
            catch (ScriptException e ){
                OutputLogger.getInstance().log(e);
                ErrorManager.getDefault().notify( e );
                return false;
            }
            catch ( FileNotFoundException e ){
                OutputLogger.getInstance().log(e);
                ErrorManager.getDefault().notify( e );
                return false;
            }
            finally {
                if( w != null ) {
                    off.write( w.toString());
                    w.close();
                }
                if( is != null ) is.close();
                off.close();
            }                  
        }
        catch (IOException e ){
            generationFailed(e , FileUtil.toFile(outputFile));
            return false;
        }
        return true;
    }
    
    private void generationFailed( Exception e , Object file){
        OutputLogger.getInstance().log(e);
        ErrorManager.getDefault().notify( e );
        OutputLogger.getInstance().log( MessageFormat.format(
                NbBundle.getMessage(ClientBeanGeneratorTemplate.class,
                "MSG_FailGenerateBean" ), file ));
    }
}

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
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;


/**
 * @author ads
 *
 */
public class DefaultWlClientCodeGenerator implements
        WLPolicyClientCodeGenerator
{
    
    protected static final String JAR = "jar";                                // NOI18N
    protected static final String ORACLE_WEBSERVICES = "oracle.webservices";  // NOI18N
    protected static final String ORACLE_WEBSERVICES_STANDALONE_CLIENT = 
                ORACLE_WEBSERVICES+".standalone.client";                    // NOI18N

    protected DefaultWlClientCodeGenerator(){
    }
    
    public DefaultWlClientCodeGenerator( String policyId ){
        this.policyId = policyId;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getPolicyId()
     */
    @Override
    public String getPolicyId() {
        throw new IllegalStateException("This method should be either overriden " +
        		"or never called on class"+DefaultWlClientCodeGenerator.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getPolicyAccessCode(java.util.Map)
     */
    @Override
    public String getPolicyAccessCode( Map<String, Object> context ) {
        return "WSBindingProvider wsbp = (WSBindingProvider)port;\n"+
        "Map<String, Object> requestContext = wsbp.getRequestContext();\n"+
        "// Override the endpoint - useful when switching target environments without regenerating the jax-ws client\n"+
        "requestContext.put(WSBindingProvider.ENDPOINT_ADDRESS_PROPERTY, \""+
        context.get( JaxWsCodeGenerator.WSDL_URL )+"\");\n"+
        "// Use request context to initialize poliy access \n" +
        "//requestContext.put( some_property_key,  some_property_value );\n";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getRequiredImports()
     */
    @Override
    public Collection<String> getRequiredImports() {
        return getDefaultImports();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getGeneratoinClientIds()
     */
    @Override
    public Collection<String> getGeneratoinClientIds() {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getDefaultGenerationClientId()
     */
    @Override
    public String getDefaultGenerationClientId() {
        return policyId;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#extendsProjectClasspath(java.util.Map)
     */
    @Override
    public void extendsProjectClasspath( Map<String, Object> context ) {
        Project project = (Project)context.get(WLWsClientMethodGeneratorStrategy.PROJECT);
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs == null || sgs.length < 1) {       
            return ;
        }
        
        FileObject sourceRoot = sgs[0].getRootFolder();
        List<URL> urls = getJarUrls(context, sgs);
        try {
            ProjectClassPathModifier.addRoots(urls.toArray( new URL[urls.size()]), sourceRoot,
                    ClassPath.COMPILE);
        }
        catch (IOException ex) {
            Logger.getLogger(getClass().getName()).info(
                    "Couldn't extends compile classpath with required jars " +
                    "for WL policy support");       // NOI18N
        }
    }
    
    public static Collection<String> getDefaultImports(){
        Collection<String> result =  new LinkedList<String>();
        result.add("weblogic.wsee.jws.jaxws.owsm.SecurityPolicyFeature");       // NOI18N
        result.add("com.sun.xml.ws.developer.WSBindingProvider");               // NOI18N
        result.add("javax.xml.ws.BindingProvider");                             // NOI18N   
        result.add("javax.xml.ws.WebServiceFeature");                           // NOI18N   
        result.add("java.util.Map");                                            // NOI18N
        return result;
    }
    
    protected List<URL> getJarUrls(Map<String, Object> context , SourceGroup[] sgs){
        List<URL> urls = new LinkedList<URL>();
        FileObject modules = (FileObject)context.get(
                WLWsClientMethodGeneratorStrategy.ORACLE_MODULES);
        for( FileObject dir :modules.getChildren() ){
            String name = dir.getName();
            if ( name.startsWith(ORACLE_WEBSERVICES)){
                FileObject jar = dir.getFileObject(ORACLE_WEBSERVICES_STANDALONE_CLIENT,
                        JAR); 
                if ( jar != null ){
                    addJar(urls, jar);
                }
            }
        }
        return urls;
    }
    
    protected void addJar( List<URL> urls, FileObject jar ) {
        if ( FileUtil.isArchiveFile( jar ) ){
            try {
                urls.add( FileUtil.getArchiveRoot(jar).getURL());
            }
            catch( FileStateInvalidException e ){
                Logger.getLogger( 
                        WLWss11SamlOrUsernameGenerator.class.getCanonicalName()).
                        log(Level.INFO, null, e);
            }
        }
    }
    
    /*static ClassPath getCompileClassPath(Project project, SourceGroup[] groups ){
        ClassPathProvider provider = project.getLookup().lookup( 
                ClassPathProvider.class);
        if ( provider == null ){
            return null;
        }
        ClassPath[] paths = new ClassPath[ groups.length];
        int i=0;
        for (SourceGroup sourceGroup : groups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, ClassPath.COMPILE);
            i++;
        }
        return ClassPathSupport.createProxyClassPath( paths );
    }*/

    private String policyId;
}

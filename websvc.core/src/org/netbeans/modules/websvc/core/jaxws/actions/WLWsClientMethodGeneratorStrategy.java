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


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;


/**
 * 
 * TODO : it should be renamed in case it have generic logic to J2EE server 
 * policies access or moved into the separate module with WebLogic specific 
 * implementation. 
 * This is the first draft of the OWSM related functionality and I keep it here.
 * It should be refactored later.
 *   
 * TODO: There is also Call WebService Operation UI action in Maven project.
 * Its implementation is different. One need to merge them later into the same
 * code base ( it seems it has a lot of duplicate code ).   
 * @author ads
 *
 */
@ServiceProvider(service=JaxWsClientMethodGeneratorStrategy.class)
public class WLWsClientMethodGeneratorStrategy extends JaxWsCodeGenerator 
    implements JaxWsClientMethodGeneratorStrategy 
{
    
    private static final String POLICY = "Policy";              // NOI18N
    
    private static final String POLICIES = "policies";          // NOI18N
    
    private static final String WSDL_POLICY_IDS = "policyIds";  // NOI18N
    
    private static final String ORACLE_POLICY = "oraclePolicy"; // NOI18N
    
    public static final String PROJECT = "project";            // NOI18N
    
    public static final String ORACLE_MODULES = "oracle_common/modules"; // NOI18N
    
    public static final String CHOSEN_POLICY_ID = "chosenPolicyId";// NOI18N
    
    public static final String WSDL_URL = "wsdlUrl";            // NOI18N    
    
    
    private static final String SECURITY_FEATURE = "securityFeature";  // NOI18N     
    
    private static final String SECURITY_POLICY_FEATURE = 
        "weblogic.wsee.jws.jaxws.owsm.SecurityPolicyFeature";   // NOI18N
    
    private static final String SECURITY_FEATURE_INIT = "private static final " +
    		"SecurityPolicyFeature[] securityFeature = new SecurityPolicyFeature[]" +
    		" { new SecurityPolicyFeature(\"oracle/";         // NOI18N
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCodeGenerator#isApplicable(org.netbeans.modules.websvc.api.jaxws.project.config.Client, org.netbeans.api.project.Project, org.openide.filesystems.FileObject, java.lang.String, java.util.Map)
     */
    @Override
    public boolean isApplicable( Client client, Project project, FileObject wsdl,
            String wsdlUrl , Map<String,Object> context) 
    {
        FileObject policies = null;
        try { 
            policies = getPolicies(project);
        }
        catch ( PoliciesNotFoundException e ){
            // if this is J2EE project but target server has no OWSM support then just quit
            return false;
        }
        if ( policies!=null ){
            /*
             *  if this is JEE project with WL target server which has OWSM then
             *  we can use special version of method generator. 
             */
            context.put( POLICIES , policies );
        }
        else {
            /*
             * Go through all registered server instances and try to find appropriate
             * WL server instance.
             */
            policies = getPolicies();
            if ( policies!= null ){
                context.put( POLICIES , policies );
            }
            else {
                return false;
            }
        }
        
        FileObject jar = FileUtil.getArchiveFile( policies );
        FileObject oracleCommonModules = null;
        if ( jar != null ){
            FileObject owsmPoliciesDir = jar.getParent();
            if ( owsmPoliciesDir != null ){
                oracleCommonModules = owsmPoliciesDir.getParent();
            }
        }
        context.put( ORACLE_MODULES, oracleCommonModules );
        context.put( PROJECT , project );
        if ( WLWsClientMethodGeneratorStrategy.class.equals(context.get( STRATEGY ))){
            return true;
        }
        
        return hasPolicy(wsdl, context);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCodeGenerator#getCompilerTask(java.lang.String, java.lang.String[], java.lang.String[], java.lang.String[], java.lang.String[], java.util.Map)
     */
    @Override
    protected CompilerTask getCompilerTask( String serviceJavaName,
            String[] serviceFName, String[] argumentDeclPart,
            String[] paramNames, String[] argumentInitPart , Map<String,Object> context)
    {
        Set<String> ids = (Set<String>)context.get(WSDL_POLICY_IDS);
        FileObject policiesDir = (FileObject)context.get(POLICIES);
        if ( ids == null || policiesDir == null){
            return super.getCompilerTask(serviceJavaName, serviceFName, 
                    argumentDeclPart, paramNames, argumentInitPart, context);
        }
        else {
            List<String> allOracleIds = new LinkedList<String>();
            Set<String> foundOracleIds = new HashSet<String>();
            for (FileObject fileObject : policiesDir.getChildren()) {
                String name = fileObject.getName();
                allOracleIds.add( name );
                if ( ids.contains( name )){
                    foundOracleIds.add( name );
                }
            }
            
            // only if there is oracle mentions in policies keep them in the active policies list 
            if ( context.get(ORACLE_POLICY) == null ){
                foundOracleIds.clear();
            }
            
            StringBuilder builder = new StringBuilder(); 
            generateOptionalCode( foundOracleIds , builder, context , allOracleIds );
            if ( context.get(CHOSEN_POLICY_ID) == null ){
                return super.getCompilerTask(serviceJavaName, serviceFName, 
                        argumentDeclPart, paramNames, argumentInitPart, context);
            }
            context.put(WSDL_POLICY_IDS, foundOracleIds);
            return new WLCompilerTask(serviceJavaName, serviceFName, 
                    argumentDeclPart, paramNames, argumentInitPart, 
                    builder.toString());
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.JaxWsCodeGenerator#getClassModificationTask(java.lang.String, java.lang.String[], java.lang.String, java.util.Map)
     */
    @Override
    protected InsertTask getClassModificationTask( String serviceJavaName,
            String[] serviceFName, String wsdlUrl, Map<String, Object> context )
    {
        if ( context.get(CHOSEN_POLICY_ID) == null ){
            return super.getClassModificationTask(serviceJavaName, serviceFName, wsdlUrl,
                context);
        }
        else {
            Set<String> foundOracleIds = (Set<String>)context.get(WSDL_POLICY_IDS);
            String serviceId = context.get(CHOSEN_POLICY_ID).toString();
            Collection<? extends WLPolicyClientCodeGenerator> generators = 
                Lookup.getDefault().lookupAll(WLPolicyClientCodeGenerator.class);
            
            Collection<String> imports = new LinkedList<String>();
            String mainId = null;
            Collection<String> relatedIds = new LinkedList<String>();
            boolean found = false;
            for (WLPolicyClientCodeGenerator generator : generators){
                String id = generator.getPolicyId();
                if ( id.equals( serviceId)){
                    imports.addAll(generator.getRequiredImports());
                    mainId = generator.getDefaultGenerationClientId();
                    relatedIds.addAll( generator.getGeneratoinClientIds() );
                    found = true;
                }
                else if ( foundOracleIds.contains( id )){
                    imports.addAll(generator.getRequiredImports());
                    relatedIds.addAll( generator.getGeneratoinClientIds() );
                    relatedIds.add( generator.getDefaultGenerationClientId());
                }
            }
            if ( !found ){
                mainId = serviceId;
                imports.addAll( DefaultWlClientCodeGenerator.getDefaultImports());
            }
            return new WLInsertTask(serviceJavaName, serviceFName[0], wsdlUrl,
                    mainId , relatedIds , imports , context );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.JaxWsClientMethodGeneratorStrategy#insertDispatchMethod(javax.swing.text.Document, int, org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService, org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort, org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation, org.openide.filesystems.FileObject, java.lang.String, java.util.Map)
     */
    @Override
    public void insertDispatchMethod( Document document, int pos,
            WsdlService service, WsdlPort port, WsdlOperation operation,
            FileObject wsdl, String wsdlUrl, Map<String,Object> context )
    {
        super.insertDispatchMethod(document, pos, service, port, operation, wsdl, 
                wsdlUrl, context);        
    }
    
    private boolean hasPolicy( FileObject wsdl, Map<String,Object> context ) {
        boolean hasPolicy = false;
        boolean hasOraclePolicy = false;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            WsdlPolicyHandler handler = new WsdlPolicyHandler();
            saxParser.parse(FileUtil.toFile( wsdl), handler );
            hasPolicy = handler.hasPolicy();
            hasOraclePolicy = handler.hasOraclePolicy();
            if ( hasPolicy ){
                context.put( WSDL_POLICY_IDS, handler.getPolicyIds());
            }
            context.put( ORACLE_POLICY, hasOraclePolicy);
            return hasPolicy;
        }
        catch (ParserConfigurationException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, null, e);
        }
        catch (SAXException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, null, e);
        }
        catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, null, e);
        }
        return false;
    }
    
    private FileObject getPolicies( Project project ) throws PoliciesNotFoundException {
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(
                J2eeModuleProvider.class);
        File serverHome = null;
        if ( moduleProvider != null ){
            String serverInstanceID = moduleProvider.getServerInstanceID();
            serverHome = getServerHome(serverInstanceID);
            FileObject policies = getPolicies(serverHome );
            if ( policies == null ){
                throw new PoliciesNotFoundException();
            }
            return policies;
        }
        return null;
    }
    
    private FileObject getPolicies( ) {
        String[] serverInstanceIDs = Deployment.getDefault().getServerInstanceIDs();
        for (String id : serverInstanceIDs) {
            File serverHome = getServerHome(id);
            FileObject policies = getPolicies( serverHome );
            if ( policies != null ){
                return policies;
            }
        }
        return null;
    }
    
    private void generateOptionalCode( Set<String> foundOracleIds,
            StringBuilder builder , Map<String, Object> context , 
            List<String> allIds )
    {
        if ( foundOracleIds.isEmpty() ){
            choosePolicyId( builder , allIds , context );
            return;
        }
        Collection<? extends WLPolicyClientCodeGenerator> generators = 
            Lookup.getDefault().lookupAll(WLPolicyClientCodeGenerator.class);
        
        boolean showDialog = true;
        for (WLPolicyClientCodeGenerator generator : generators){
            if ( foundOracleIds.contains( generator.getPolicyId())){
                generator.extendsProjectClasspath(context);
                String code = generator.getPolicyAccessCode(context);
                if ( builder.length() == 0 ){
                    builder.append( code );
                    context.put(CHOSEN_POLICY_ID, generator.getPolicyId());
                }
                else {
                    builder.append("/*");       // NOI18N
                    builder.append(code);       
                    builder.append("*/");       // NOI18N
                }
                showDialog = false;
            }
        }
        if( showDialog ){
            choosePolicyId( builder , allIds, context );
        }
    }
    
    
    private void choosePolicyId( StringBuilder builder, List<String> allIds ,
            Map<String, Object> context )
    {
        OWSMPolicies policies = new OWSMPolicies( allIds );
        DialogDescriptor desc = new DialogDescriptor(policies, 
                NbBundle.getMessage(WLWsClientMethodGeneratorStrategy.class, 
                        "LBL_ChoosePolicy"));       // NOI18N
        if ( DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(desc) ){
            context.put(CHOSEN_POLICY_ID, policies.getId() );
            DefaultWlClientCodeGenerator generator = 
                new DefaultWlClientCodeGenerator(policies.getId());
            generator.extendsProjectClasspath(context);
            builder.append( generator.getPolicyAccessCode(context));
        }
    }

    private FileObject getPolicies( File serverHome ) {
        if ( serverHome == null ){
            return null;
        }
        /*
         *  TODO : this is definitely should be moved out here. Probably somehow 
         *  via JaxWS stack ( and new API for it ).
         */
        File rootInstallDir = serverHome.getParentFile();
        if ( rootInstallDir == null ){
            return null;
        }
        FileObject rootInstall = FileUtil.toFileObject( 
                FileUtil.normalizeFile(rootInstallDir));
        FileObject modules = rootInstall.getFileObject("oracle_common/modules/");//NOI18N 
        if ( modules == null ){
            return null ;
        }
        FileObject policiesFolder =null;
        for ( FileObject folder : modules.getChildren() ){
            if ( folder.getName().startsWith("oracle.wsm.policies")){// NOI18N 
                policiesFolder = folder;
                break;
            }
        }
        if ( policiesFolder == null ){
            return null;
        }
        FileObject[] jars = policiesFolder.getChildren();
        for (FileObject jar : jars) {
            FileObject archiveRoot = FileUtil.getArchiveRoot( jar );
            FileObject policies = archiveRoot.getFileObject(
                    "META-INF/policies/oracle/");       //      NOI18N 
            if ( policies != null ){
                return policies;
            }
        }
        return null;
    }

    private File getServerHome( String serverInstanceID ) {
        if(serverInstanceID != null) {
            try {
                J2eePlatform j2eePlatform = Deployment.getDefault().
                    getServerInstance(serverInstanceID).getJ2eePlatform();
                return j2eePlatform.getServerHome();
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, 
                        "Failed to find J2eePlatform", ex);         // NOI18N
            }
        }
        return null;
    }
    
    private static class PoliciesNotFoundException extends Exception {

    }
    
    private static class WLInsertTask extends InsertTask {
        public WLInsertTask( String serviceJavaName, String serviceFName,
                String wsdlUrl, String mainId, Collection<String> relatedIds, 
                Collection<String> imports , Map<String, Object> context )
        {
            super(serviceJavaName, serviceFName, wsdlUrl, context);
            clientPolicyId = mainId;
            relatedClientIds = relatedIds;
            this.imports = imports;
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.websvc.core.jaxws.actions.InsertTask#modifyJavaClass(org.netbeans.api.java.source.WorkingCopy, org.netbeans.api.java.source.TreeMaker, com.sun.source.tree.ClassTree, javax.lang.model.element.TypeElement)
         */
        @Override
        protected ClassTree modifyJavaClass( WorkingCopy workingCopy,
                TreeMaker make, ClassTree javaClass , TypeElement classElement )
        {
            javaClass = super.modifyJavaClass(workingCopy, make, javaClass, classElement);
            
            Collection<String> existingImports = getImports(workingCopy);
            CompilationUnitTree original = workingCopy.getCompilationUnit();
            CompilationUnitTree modified = original;
            for (String imp : imports) {
                if (!existingImports.contains(imp)) {
                    modified = make.addCompUnitImport(
                            modified, make.Import(make.Identifier(imp), false));
                }
            }
            workingCopy.rewrite(original, modified);
            
            return insertSecurityFetaureField(workingCopy, make, javaClass, 
                    classElement);
        }
        
        private ClassTree insertSecurityFetaureField(WorkingCopy workingCopy,
                TreeMaker make, ClassTree javaClass, TypeElement classElement )
        {
            for (VariableElement var : 
                ElementFilter.fieldsIn( classElement.getEnclosedElements())) 
            {
                TypeMirror varType = var.asType();
                if (!varType.getKind().equals(TypeKind.ARRAY)) {
                    continue;
                }
                TypeMirror componentType = ((ArrayType) varType)
                        .getComponentType();
                Element componentElement = workingCopy.getTypes().asElement(
                        componentType);
                if (componentElement instanceof TypeElement) {
                    String name = ((TypeElement) componentElement)
                            .getQualifiedName().toString();
                    if (name.equals(SECURITY_POLICY_FEATURE) && 
                            var.getSimpleName().contentEquals(SECURITY_FEATURE)) 
                    {
                        /*
                         *  there is no way to find existing comments. 
                         *  So if field is already in the class. Just return
                         */
                        return javaClass;
                    }
                }
            }
            Set<Modifier> modifiers = new HashSet<Modifier>();
            modifiers.add( Modifier.PRIVATE);
            modifiers.add( Modifier.STATIC);
            modifiers.add( Modifier.FINAL);
            
            ModifiersTree modifiersTree = make.Modifiers(
                    modifiers);
            
            TypeElement securityType = workingCopy.getElements().getTypeElement(
                    SECURITY_POLICY_FEATURE);   
            
            Tree securityTreeType = securityType != null ? make.Type(securityType.asType()) : 
                make.Identifier(SECURITY_POLICY_FEATURE);
            Tree securityTree  = make.ArrayType( securityTreeType );
            
            NewClassTree initClassTree = make.NewClass(null, 
                    Collections.<ExpressionTree>emptyList(), 
                    securityType != null ? make.Identifier( securityType): 
                        make.Identifier( SECURITY_POLICY_FEATURE), 
                        Collections.singletonList(
                                make.Literal("oracle/"+clientPolicyId)), null); // NOI18N
            NewArrayTree expressionTree = make.NewArray(securityTreeType, 
                    Collections.<ExpressionTree>emptyList(), 
                    Collections.singletonList(initClassTree) );
            VariableTree securityFeature = make.Variable(
                    modifiersTree, SECURITY_FEATURE,      
                    securityTree,
                    expressionTree);
            addComments(securityFeature, make);
            
            return make.insertClassMember(javaClass, 0, securityFeature);
        }
        
        private ClassTree addComments( VariableTree var , TreeMaker make )
        {
            for (String relatedId : relatedClientIds) {
                StringBuilder builder = new StringBuilder(SECURITY_FEATURE_INIT);
                builder.append(relatedId);
                builder.append("\")};\n");
                make.addComment(var,Comment.create(Comment.Style.LINE, 
                        builder.toString()),true);
            }
            return null;
        }

        public static Collection<String> getImports(CompilationController controller) {
            Set<String> imports = new HashSet<String>();
            CompilationUnitTree cu = controller.getCompilationUnit();
            
            if (cu != null) {
                List<? extends ImportTree> importTrees = cu.getImports();
                
                for (ImportTree importTree : importTrees) {
                    imports.add(importTree.getQualifiedIdentifier().toString());
                }
            }
            
            return imports;
        }
        
        
        private String clientPolicyId ;
        private Collection<String> relatedClientIds;
        private  Collection<String> imports ;
    }
    
    private static class WLCompilerTask extends CompilerTask {

        public WLCompilerTask( String serviceJavaName, String[] serviceFName, 
                String[] argumentDeclPart, String[] paramNames, 
                String[] argumentInitPart, String optionalCode )
        {
            super(serviceJavaName, serviceFName, argumentDeclPart, 
                    argumentInitPart);
            this.optionalCode = optionalCode;
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.websvc.core.jaxws.actions.CompilerTask#getInvocationBodyPortInitArguments(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        protected Object[] getInvocationBodyPortInitArguments(
                String portJavaName, String portGetterMethod,
                String returnTypeName, String operationJavaName )
        {
            return modifyArguments( super.getInvocationBodyPortInitArguments(
                    portJavaName, portGetterMethod,returnTypeName, operationJavaName));
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.websvc.core.jaxws.actions.CompilerTask#getMethodBodyPortInitArguments(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        protected Object[] getMethodBodyPortInitArguments( String portJavaName,
                String portGetterMethod, String returnTypeName,
                String operationJavaName )
        {
            return modifyArguments( super.getMethodBodyPortInitArguments(
                    portJavaName, portGetterMethod,returnTypeName, operationJavaName));
        }
        
        private Object[] modifyArguments( Object[] args )
        {
            if ( args[3] == null || args[3].toString().trim().length()==0){
                args[3] = optionalCode;
            }
            else {
                args[3] = args[3].toString()+optionalCode;
            }
            args[9] = SECURITY_FEATURE;        // NOI18N
            return args;
        }
        
        private String optionalCode;
    }
    
    private static final class WsdlPolicyHandler extends DefaultHandler {
        
        private static final String ID = "Id";                  // NOI18N
        private static final String ORACLE = "oracle";          // NOI18N
        
        private static final String COLON_ID = ":"+ID;                  // NOI18N
        private static final String COLON_POLICY = ":"+POLICY;          // NOI18N
        
        /* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement( String uri, String localName, String qName,
                Attributes attributes ) throws SAXException
        {
            super.startElement(uri, localName, qName, attributes);
            boolean policy = false;
            if ( localName != null && localName.equals(POLICY)){
                policy = true;
            }
            if ( qName != null && qName.endsWith(COLON_POLICY) ) {
                policy = true;
            }
            if ( !policy ){
                return;
            }
            else {
                hasPolicy = true;
            }
            int count = attributes.getLength();
            for (int i=0; i<count ; i++) {
                String value = attributes.getValue(i);
                if ( value.toLowerCase( Locale.ENGLISH).contains(ORACLE)){
                    hasOraclePolicy = true;
                }
                String attrLocalName = attributes.getLocalName(i);
                String attrQName = attributes.getQName(i);
                
                if ( (attrLocalName!=null && attrLocalName.equals(ID)) || 
                        (attrLocalName!= null && attrQName.endsWith(COLON_ID)))
                {
                    myPolicies.add( attributes.getValue(i));
                }
            }
        }
        
        boolean hasPolicy(){
            return hasPolicy;
        }
        
        boolean hasOraclePolicy(){
            return hasOraclePolicy;
        }
        
        Set<String> getPolicyIds(){
            return myPolicies;
        }
        
        private boolean hasPolicy;
        private boolean hasOraclePolicy;
        private Set<String> myPolicies = new HashSet<String>();
    }
}

/*
 * EjbLoader.java
 *
 * Created on April 28, 2004, 5:39 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbContainerVendor;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodReturn;
import org.netbeans.modules.visualweb.ejb.ui.ConfigureMethodsDialog;
import org.netbeans.modules.visualweb.ejb.util.Util;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;


/**
 * This class is used to load the jar file into rave and extract
 * the necessary information regarding the Ejbs from the jar.
 */
public class EjbLoader {
    
    // The package where all the client wrapper will be
    // FIXME The icon code needs to be fixed. Right now it's either broken or working by accident.
    public static final String CLIENT_WRAPPER_PACKAGE_NAME = "org.netbeans.modules.visualweb.ejb";
    
    // Two jar files needed for compiling the generated data provider and wrapper classes
    public static final String dataproviderJar = InstalledFileLocator.getDefault().locate("modules/ext/dataprovider.jar", null, false ).getAbsolutePath(); // NOI18N
    public static final String designTimeJar = InstalledFileLocator.getDefault().locate( "modules/ext/designtime.jar", null, false).getAbsolutePath(); // NOI18N
    
    private EjbGroup ejbGroup;
    private URLClassLoader classloader;
    private String warningMsg;
    
    public EjbLoader(EjbGroup ejbGroup) {
        this.ejbGroup = ejbGroup;
    }
    
    public void load() throws EjbLoadException {
        // Extract the deployment descriptors from the jar files (client jar file + dd location file if there is one )
        
        ArrayList jarFiles = new ArrayList();
        jarFiles.addAll( ejbGroup.getClientJarFiles() );
        if( ejbGroup.getDDLocationFile() != null )
            jarFiles.add( ejbGroup.getDDLocationFile() );
        
        DeploymentDescriptorExtractor extractor = new DeploymentDescriptorExtractor( jarFiles );
        Map descriptors = extractor.getDeploymentDescriptors();
        
        // Log a warning if there is no deployment descriptor found
        if( descriptors.isEmpty() ) {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbLoader" ).log( ErrorManager.WARNING, "NO EJB deployment descriptors found" );
            
            // No EJB deployment descriptors found in client jar files and/or deployment descriptors.
            // Looking for a reference to {0}. Add a Jar or Ear file containing deployment descriptors.
            String ddFileName = EjbContainerVendor.getVendorDDFileName( ejbGroup.getAppServerVendor() );
            String msg = NbBundle.getMessage( EjbLoader.class, "NO_DEPLOYMENT_DESCRIPTOR", ddFileName );
            
            throw new EjbLoadException( EjbLoadException.USER_ERROR, msg );
        }
        else {
            // Make sure that the user has selected the correct container for the jar file
            validateAppServerSelection( descriptors );
            
            // Load classes from the client jars
            classloader = EjbLoaderHelper.getEjbGroupClassLoader( ejbGroup );
            
            // Cleanup the classes list extracted from the client jars and set them to the ejbgroup
            setAllClazz( extractor.getAllClazz() );
            
            // Populate the session beans with business methods
            // Note: entity beans and mdbs later
            populateBeanInfo( descriptors );
            
            // Check to see whether there are any session EJBs in the provided
            // client jars. If not, warning here
            if( ejbGroup.getSessionBeans() == null || ejbGroup.getSessionBeans().isEmpty() ) {
                ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbLoader" ).log( ErrorManager.ERROR, "NO session EJBs found" );
                
                // No session EJBs found in given client jar files.
                // The given client jar files do not contain any session EJBs
                String msg = NbBundle.getMessage( EjbLoader.class, "NO_SESSION_EJBS_FOUND" );
                
                // If we have skipped some EJBs, then add the information to the message.
                if( warningMsg != null )
                    msg = msg + " " + warningMsg;
                
                throw new EjbLoadException( EjbLoadException.USER_ERROR, msg );
            }
            
            // Lastly (has be be last), check whether there is any warning we should give.
            // For now, the only warning is if there are any EJBs skipped
            if( warningMsg != null )
                throw new EjbLoadException( EjbLoadException.WARNING, warningMsg );
            
            // Got all the information. Delete the tmp files
            //cleanTempFiles( descriptors, extractor.getTmpJarFiles() );
        }
    }
    
    public boolean reload() throws EjbLoadException {
        
        // Before anything, lets check the existence of all the jar files
        checkFileExistence();
        
        // Lets remember the original ejbs so that we can copy over the method parameter names 
        // and return collection element classes
        Collection origEjbs = ejbGroup.getSessionBeans();
        
        // Clean up the old data
        ejbGroup.setSessionBeans( null );
        ejbGroup.setEntityBeans( null );
        ejbGroup.setMDBs( null );
        ejbGroup.setAllClazz( new HashSet() );
        
        // Do the load all over again
        load();
        
        if( ejbGroup.hasAnyConfigurableMethod() )
        {
            // Need to copy over the information (parameter names and return collection element classes) from the origial group
            // if possible
            Collection newEjbs = ejbGroup.getSessionBeans();
            copyOverUserInputs( origEjbs,  newEjbs );

            // Popup the dialog to allow configuring the method if there are anything to configure
            // When "OK" in the dialog is clicked, the wrapper classes will be regenerated
            ConfigureMethodsDialog dialog = new ConfigureMethodsDialog( ejbGroup, true );
            dialog.showDialog();

            if( dialog.isCancelled() )
                return false;
            else
            {
                ejbGroup = dialog.getEjbGroup();
                return true;
            }
        }
        else
        {
            // Regenerate the classes             
            createWrapperClientBeans(); 
            return true;
        }
    }
    
    private void copyOverUserInputs( Collection origEjbs, Collection newEjbs ) 
    {
        // Convert the collection to map
        Map origMap = new HashMap();
        for( Iterator iter = origEjbs.iterator(); iter.hasNext(); ) {
            EjbInfo ejb = (EjbInfo)iter.next();
            origMap.put( ejb.getJNDIName(),  ejb );
        }
        
        for( Iterator iter = newEjbs.iterator(); iter.hasNext(); ) {
            EjbInfo ejb = (EjbInfo)iter.next();
            EjbInfo origEjb = (EjbInfo)origMap.get( ejb.getJNDIName() );
            
            // Must be a new ejb just added. Move on
            if( origEjb == null )
                continue;
            
            copyOverMethodConfiguration( origEjb.getMethods(), ejb.getMethods() );
        }
    }
    
    private void copyOverMethodConfiguration( ArrayList origMethods, ArrayList newMethods ) {
        Map origMap = new HashMap();
        for( Iterator iter = origMethods.iterator(); iter.hasNext(); ) {
            MethodInfo method = (MethodInfo)iter.next();
            origMap.put( method.getSignature(),  method );
        }
        
        for( Iterator iter = newMethods.iterator(); iter.hasNext(); ) {
            MethodInfo method = (MethodInfo)iter.next();
            
            // Time to get the information
            if( method.isMethodConfigurable() )
            {
                MethodInfo origMethod = (MethodInfo)origMap.get( method.getSignature() );
                
                if( origMethod == null )
                    continue;
                
                // Parameter names first
                ArrayList params = method.getParameters();
                ArrayList origParams = origMethod.getParameters();
                
                for( int i = 0; i < params.size(); i ++ ) {
                    MethodParam param = (MethodParam)params.get( i );
                    MethodParam origParam = (MethodParam)origParams.get( i );
 
                    param.setName( origParam.getName() );
                }
                
                // Now reutrn collection element class
                if( method.getReturnType().isCollection() ) {
                    MethodReturn ret = method.getReturnType();
                    ret.setElemClassName( origMethod.getReturnType().getElemClassName() );
                }
            }
        }
    }
    
    public EjbGroup getEjbGroup() {
        return this.ejbGroup;
    }
    
    private void setAllClazz( Set allClasses )
    {
        for( Iterator iter = allClasses.iterator(); iter.hasNext(); )
        {
            String className = (String)iter.next();
            
            // No internal classes for now
            if( className.indexOf( '$' ) != -1 )
            {
                iter.remove();
                continue;
            }
            
            /*try {
                Class.forName( className, true, classloader );
            } catch( Throwable t ) {
                // bad one
                iter.remove();
            } */
        }
        ejbGroup.setAllClazz( allClasses ); 
    }
    
    private void populateBeanInfo( Map deploymentDescriptors ) throws EjbLoadException {
        // Populate the ejb information from the deployment descriptors extracted
        // from the client jar file. The ejb names, home interfaces, component interfaces
        // are extracted from the standard deployment descriptor - ejb-jar.xml. The
        // JNDI names are from the vendor specific xml file, for example, sun-ejb-jar.xml
        // Using Java reflection to get the business methods
        
        // Parse the standard deployment descriptors first to get
        // the session EJBs
        parseStdXmls( deploymentDescriptors.keySet() );
        
        // Then the vendor specific ones to get the jndi names
        Map ejbName2JndiNameMapping = parseVendorXmls( deploymentDescriptors.values() );
        
        // Now set the jndi name to the proper ejbs
        for( Iterator iter = ejbGroup.getSessionBeans().iterator(); iter.hasNext(); ) {
            EjbInfo info = (EjbInfo)iter.next();
            
            // Note: the map from sun appserver and weblogic deployment descriptor,
            // the map contains (ejbName, jndiName) pairs. But the map from the
            // websphere deployment descriptor cotains (bean id, jndi name )
            
            String jndiName = null;
            if( ejbGroup.isWebsphereAppServer() )
                jndiName = (String)ejbName2JndiNameMapping.get( info.getBeanId() );
            else
                jndiName = (String)ejbName2JndiNameMapping.get( info.getEjbName() );
            
            info.setJNDIName( jndiName );
            
            // Make the ejb ref name, which will be written into web.xml, the
            // same as the jndi name
            info.setWebEjbRef( jndiName );
        }
        
        // Buz methods
        populateBusinessMethods();
    }
    
    private void parseStdXmls( Collection stdXmls ) throws EjbLoadException 
    {
        Collection allSkippedEjbs = new ArrayList();
        
        for( Iterator iter = stdXmls.iterator(); iter.hasNext(); ) {
            String stdXml = (String)iter.next();
            StdDeploymentDescriptorParser parser = new StdDeploymentDescriptorParser( stdXml );
            ejbGroup.addSessionBeans( parser.parse() );
            
            if( parser.getSkippedEjbs() != null )
                allSkippedEjbs.addAll( parser.getSkippedEjbs() ); 
        }
        
        if( !allSkippedEjbs.isEmpty() )
        {
            StringBuffer skippedEjbsStr = new StringBuffer();
            boolean first = true;
            for( Iterator skippedEjbsIter = allSkippedEjbs.iterator(); skippedEjbsIter.hasNext(); )
            {
                if( first )
                    first = false;
                else 
                    skippedEjbsStr.append( ", " );

                skippedEjbsStr.append( (String)skippedEjbsIter.next() );
            }
            
            if( allSkippedEjbs.size() == 1 )
                // EJB {0} has been skipped because there are no packages defined for the home or/and remote interfaces.
                warningMsg = NbBundle.getMessage(StdDeploymentDescriptorParser.class, "SKIP_NO_PACKAGE_EJB_SINGLE", skippedEjbsStr.toString() );
            else
                // EJBs {0} have been skipped because there are no packages defined for the home or/and remote interfaces.
                warningMsg = NbBundle.getMessage(StdDeploymentDescriptorParser.class, "SKIP_NO_PACKAGE_EJBS", skippedEjbsStr.toString() );
        }
    }
    
    private Map parseVendorXmls( Collection vendorXmls ) throws EjbLoadException {
        Map ejbName2JndiNameMapping = new HashMap();
        for( Iterator iter = vendorXmls.iterator(); iter.hasNext(); ) {
            String vendorXml = (String)iter.next();
            
            if( ejbGroup.isSunAppServer() ) {
                SunDeploymentDescriptorParser parser = new SunDeploymentDescriptorParser( vendorXml );
                ejbName2JndiNameMapping.putAll( parser.parse() );
            }
            else if( ejbGroup.isWebLogicAppServer() ) {
                WeblogicDeploymentDescriptorParser parser = new WeblogicDeploymentDescriptorParser( vendorXml );
                ejbName2JndiNameMapping.putAll( parser.parse() );
            }
            else if( ejbGroup.isWebsphereAppServer() ) {
                WebsphereDeploymentDescriptorParser parser = new WebsphereDeploymentDescriptorParser( vendorXml );
                ejbName2JndiNameMapping.putAll( parser.parse() );
            }
        }
        
        return ejbName2JndiNameMapping;
    }
    
    private void populateBusinessMethods() throws EjbLoadException {
        // Now get the business methods of each session bean
        Collection sessionBeans = ejbGroup.getSessionBeans();
        
        for( Iterator iter = sessionBeans.iterator(); iter.hasNext(); ) {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            ArrayList allMethods = new ArrayList();
            
            // Get the business methods
            String remoteInterface = ejbInfo.getCompInterfaceName();
            
            try {
                allMethods.addAll( getBuzMethodInfos( remoteInterface ) );
            } catch( java.lang.ClassNotFoundException ex ) {
                // Looks like this session ejb is not in the specified client jars.
                // Remove it from the session bean list and go on
                iter.remove();
                continue;
            }
            
            // Get the create() methods in home interface
            String homeInterface = ejbInfo.getHomeInterfaceName();
            allMethods.addAll( getCreateMethodInfos( homeInterface ) );
            
            ejbInfo.setMethods( allMethods );
        }
        
        // The session beans collection might be changed.
        ejbGroup.setSessionBeans( sessionBeans );
    }
    
    private ArrayList getBuzMethodInfos( String interfaceName ) throws java.lang.ClassNotFoundException {
        try {
            Class c = Class.forName( interfaceName, true,  classloader );
            Method[] methods = c.getMethods();
            
            ArrayList methodInfos = new ArrayList();
            for( int i = 0; i < methods.length; i ++ ) {
                if( EjbMethodFilter.isEjbSpecMethod( methods[i] ) )
                    continue;
                
                String methodName = methods[i].getName();
                
                // Return type
                MethodReturn returnType = createMethodReturn(methods[i].getReturnType() );
                
                // Parameters
                Class[] pcs = methods[i].getParameterTypes();
                ArrayList parameters = new ArrayList();
                for( int pci = 0; pci < pcs.length; pci++ ) {
                    parameters.add( new MethodParam( "arg" + pci,Util.getTypeName(pcs[pci]) ) );
                }
                
                // Exceptions
                Class[] exceptionCls = methods[i].getExceptionTypes();
                ArrayList exceptions = new ArrayList();
                for( int exi = 0; exi < exceptionCls.length; exi ++ )
                    exceptions.add( exceptionCls[exi].getName() );
                
                // Ready to make a MethodInfo object
                methodInfos.add( new MethodInfo( methodName, methodName, parameters, returnType, exceptions) );
            }
            
            return methodInfos;
        }
        catch( java.lang.ClassNotFoundException ex ) {
            // Log a warning and go one
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbLoader" ).log( ErrorManager.INFORMATIONAL,
            "Remote interface (" + interfaceName + ") is not found in the client jars. Skipped." );
            
            throw ex;
        }
    }
    
    private ArrayList getCreateMethodInfos( String interfaceName ) throws EjbLoadException {
        try {
            Class c = Class.forName( interfaceName, true,  classloader );
            Method[] methods = c.getMethods();
            
            ArrayList methodInfos = new ArrayList();
            for( int i = 0; i < methods.length; i ++ ) {
                // Only look for create() metohds
                if( !methods[i].getName().equals( "create" ) )
                    continue;
                
                String methodName = methods[i].getName();
                MethodReturn returnType = createMethodReturn(methods[i].getReturnType() );
                
                // Parameters
                Class[] pcs = methods[i].getParameterTypes();
                ArrayList parameters = new ArrayList();
                for( int pci = 0; pci < pcs.length; pci++ ) {
                    parameters.add( new MethodParam("arg" + pci, pcs[pci].getName() ) );
                }
                
                // Exceptions
                Class[] exceptionCls = methods[i].getExceptionTypes();
                ArrayList exceptions = new ArrayList();
                for( int exi = 0; exi < exceptionCls.length; exi ++ )
                    exceptions.add( exceptionCls[exi].getName() );
                
                // Ready to make a MethodInfo object
                methodInfos.add( new MethodInfo( false, methodName, methodName, parameters, returnType, exceptions) );
            }
            
            return methodInfos;
        }
        catch( java.lang.ClassNotFoundException ex ) {
            // Log error
            String logMsg = "Error occurred when trying to get the method information. Cannot find class " + interfaceName;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbLoader" ).log( ErrorManager.ERROR, logMsg );
            ex.printStackTrace();
            
            // throw exception
            throw new EjbLoadException( ex.getMessage() );
        }
    }
    
    private MethodReturn createMethodReturn( Class returnClass )
    {
        MethodReturn returnType = new MethodReturn();
        
        if( Collection.class.isAssignableFrom( returnClass ) )
            returnType.setIsCollection( true );
        else
            returnType.setIsCollection( false );
        
        returnType.setClassName( Util.getTypeName( returnClass ) );
        
        return returnType;
    }
    
    public void createWrapperClientBeans() throws EjbLoadException {
        
        // Where the class will be generated
        String srcDir = getWrapperBeanSrcDir();
        
        // Load classes from the client jars if not yet
        if( classloader == null )
            classloader = EjbLoaderHelper.getEjbGroupClassLoader( ejbGroup );
        
        ArrayList allClassDescriptors = new ArrayList();
        
        // Generate source code for wrapper classes (bean, beanInfo), 
        // data provider bean classes (bean, beanInfo, designInfo),
        // then compile them and jar all the files into one jar file
        
        // One wrapper class per session ejb
        for( Iterator iter = ejbGroup.getSessionBeans().iterator(); iter.hasNext(); ) {
            EjbInfo ejb = (EjbInfo)iter.next();
            ClientBeanGenerator generator = new ClientBeanGenerator( ejb, classloader );
            allClassDescriptors.addAll( generator.generateClasses( srcDir ) );
        }
        
        // Data provider classes. One per non-void buz method
        generateDPClasses( allClassDescriptors );
        
        // Compile the wrapper bean and beanInfo classes
        ClientBeanWrapperCompiler compiler = new ClientBeanWrapperCompiler();
        ArrayList jarFiles =  new ArrayList( ejbGroup.getClientJarFiles() );
//      jarFiles.add( ejb20Jar );  // For EJB base classes
        List<File> javaEEJars = EjbLoaderHelper.getJavaEEClasspathEntries();
        for (File file : javaEEJars) {
        	String path = file.getAbsolutePath();
			jarFiles.add(path);
		}
        jarFiles.add( dataproviderJar );  // For the data provider classes
        jarFiles.add( designTimeJar ); // For the DesignInfo classes
        compiler.compile( srcDir, allClassDescriptors, jarFiles );
        
        // Time to jar ....
        // The name of the jar file will be the concatenation of the ejb group name (spaces will be replaced with "_"s)
        // with "ClientWrapper". For example, if the ejb group name is "Travel Center EJBs", then the wrapper
        // jar name will be Travel_Center_EJBsClientWrapper.jar

        if( ejbGroup.getClientWrapperBeanJar() == null )
        {
            // Must be the first time generating the jar file
            // It is for the add new ejb group case
            
            String jarName = ejbGroup.getName().replaceAll( " ", "_" ) + "ClientWrapper.jar"; // NOI18N
            String wrapperJarPath = srcDir + File.separator + jarName;
            ejbGroup.setClientWrapperBeanJar( wrapperJarPath );
        }
        
        if( ejbGroup.getDesignInfoJar() == null )
        {
            // Must be the first time generating the jar file
            // It is for the add new ejb group case
            
            String jarName = ejbGroup.getName().replaceAll( " ", "_" ) + "DesignTime.jar"; // NOI18N
            String jarPath = srcDir + File.separator + jarName;
            ejbGroup.setDesignInfoJar( jarPath );
        }
        
        ClientBeanWrapperJarGenerator.jarThemUp( ejbGroup.getName(), ejbGroup,  allClassDescriptors ); 
    }
    
    private void generateDPClasses( ArrayList allClassDescriptors ) throws EjbLoadException
    {
        // Business methods from session ejbs
        for( Iterator iter = ejbGroup.getSessionBeans().iterator(); iter.hasNext(); )
        {
            EjbInfo ejbInfo = (EjbInfo)iter.next();
            
            Map methodNameOccurrence = new HashMap();
            for( Iterator mIter = ejbInfo.getMethods().iterator(); mIter.hasNext(); )
            {
                MethodInfo mInfo = (MethodInfo)mIter.next();
                
                if( mInfo.isBusinessMethod() && !mInfo.getReturnType().isVoid() ) 
                {
                    // For overloadded method, an index will be append after the method name for the second method on
                    
                    String dpClassName = mInfo.getName();
                    
                    Integer occurrence = (Integer)methodNameOccurrence.get( mInfo.getName() );
                    if( occurrence == null )
                        occurrence = new Integer( 1 );
                    else
                    {
                        occurrence = new Integer( occurrence.intValue() + 1 );
                        dpClassName += occurrence;
                    }
                    
                    methodNameOccurrence.put( mInfo.getName(), occurrence );
                    
                    // The data provider class name is the ejb remote interface name + method name + (possible index number if method name is overloadded)
                    mInfo.setDataProvider( Util.getClassName(ejbInfo.getCompInterfaceName()) + Util.capitalize( dpClassName )); // NOI18N
                    
                    DataProviderGenerator dpGen = new DataProviderGenerator( ejbInfo.getBeanWrapperName(), mInfo, classloader );
                    allClassDescriptors.addAll( dpGen.generateClasses( getWrapperBeanSrcDir()) );
                }
            }
        }
    }
    
    private String getWrapperBeanSrcDir() {
        String srcDir = System.getProperty("netbeans.user"); // NOI18N
        srcDir = srcDir + File.separator + "ejb-datasource"; // NOI18N
        File srcDirF = new File( srcDir );
        if( !srcDirF.exists() )
            srcDirF.mkdirs();
        
        return srcDir;
    }
    
    private void validateAppServerSelection( Map descriptors ) throws EjbLoadException {
        // I think it is safe to just pick one of the vendor deployment descriptor
        // to validate
        
        String vendorDD = (String)descriptors.values().iterator().next();
        boolean invalid = false;
        if( (ejbGroup.isSunAppServer() && vendorDD.indexOf( "sun-ejb-jar" ) == -1) ||
        (ejbGroup.isWebLogicAppServer() && vendorDD.indexOf( "weblogic-ejb-jar" ) == -1 ) ||
        (ejbGroup.isWebsphereAppServer() && vendorDD.indexOf( "ibm-ejb-jar-bnd" ) == -1 ) ) {
            // Wrong vendor DD
            // The deployment descriptors contained in the client jar files and/or deployment descriptor file do not match those expected for application server {0}. Maybe the wrong application server is selected.
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.EjbLoader" ).log( ErrorManager.ERROR, "Incorrect application server selected for EJB set: " + ejbGroup.getName() );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, NbBundle.getMessage( EjbLoader.class, "WRONG_APP_SERVER_TYPE", ejbGroup.getAppServerVendor() ) );
        }
    }
    
    private void checkFileExistence() throws EjbLoadException {
        if( ejbGroup.getClientJarFiles() != null ) {
            for( Iterator iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext(); ) {
                String file = (String)iter.next();
                if( !(new File(file)).exists() ) {
                    String msg = NbBundle.getMessage( EjbLoader.class, "RELOAD_ERROR_FILE_NOT_FOUND", ejbGroup.getName(), file );
                    throw new EjbLoadException( EjbLoadException.USER_ERROR, msg );
                }
            }
        }
        
        if( ejbGroup.getDDLocationFile() != null && !(new File(ejbGroup.getDDLocationFile()).exists() ) ) {
            String msg = NbBundle.getMessage( EjbLoader.class, "RELOAD_ERROR_FILE_NOT_FOUND", ejbGroup.getName(), ejbGroup.getDDLocationFile() );
            throw new EjbLoadException( EjbLoadException.USER_ERROR, msg );
        }
    }
}

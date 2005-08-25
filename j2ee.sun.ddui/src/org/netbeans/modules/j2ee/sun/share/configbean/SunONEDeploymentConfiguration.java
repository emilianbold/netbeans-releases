/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;

import org.xml.sax.SAXException;

import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentConfigurationInterface;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan;
import org.netbeans.modules.j2ee.sun.share.plan.FileEntry;
import org.netbeans.modules.j2ee.sun.share.config.ConfigurationStorage;
import org.netbeans.modules.j2ee.sun.share.config.ConfigDataObject;
import org.netbeans.modules.j2ee.sun.share.config.DDRoot;
import org.netbeans.modules.j2ee.sun.share.config.DDFilesListener;



/** Manages the deployment plan I/O and access for initializing DConfigBeans
 * 
 * @author Vince Kraemer
 * @author Peter Williams
 */
public class SunONEDeploymentConfiguration implements Constants, SunDeploymentConfigurationInterface {
    
    private static final ResourceBundle beanBundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.configbean.Bundle");	// NOI18N

    // !PW FIXME workaround for linking ConfigDataObjects w/ the correct Deployment
    // Configuration object.  Key is primary File for configuration.
    private static WeakHashMap configurationMap = new WeakHashMap();
    
    public static void addConfiguration(File key, SunONEDeploymentConfiguration config) {
        configurationMap.put(key, config);
    }
    
    public static void removeConfiguration(File key) {
        configurationMap.remove(key);
    }
    
    public static SunONEDeploymentConfiguration getConfiguration(File key) {
        return (SunONEDeploymentConfiguration) configurationMap.get(key);
    }
    
    private DeployableObject dObj;
    private Map contentMap = new HashMap();
    private Map beanMap = new HashMap();
    
    /*
     * value to hold the module name used by the IDE to define the deployable object
     * this is a jsr88 extension for directory deployment: we need to find a good
     * dir name to put the bits that will be deployed.
     * */
    private String deploymentModuleName="_default_"; // NOI18N
    
    /** Configuration files and the directory they belong in, as specified by init.
     */
    private File [] configFiles;
    private File resourceDir;
    private boolean keepUpdated;
    
    private DDFilesListener ddFilesListener;
    
    /** Creates a new instance of SunONEDeploymentConfiguration
     * @param dObj The deployable object this object configures
     * @param dm The DeploymentManager that created the DeploymentConfiguration
     */
    public SunONEDeploymentConfiguration(DeployableObject dObj/*,
    SunDeploymentManager dm*/) {
                                                               
  //      Object params[] = new Object [] { dObj, dm };
   //     jsr88Logger.entering(SunONEDeploymentConfiguration.class.toString(), "<init>", params);
        
        this.dObj = dObj;
     //   this.dm = dm;
        
      //  jsr88Logger.exiting(SunONEDeploymentConfiguration.class.toString(), "<init>", params);
    }

    /**
     * SunONEDeploymentConfiguration initialization. This method should be called before
     * this class is being used.
     *
     * @param configFiles Sun specific DD files referenced by this J2EE module, 
     *   e.g. sun-web.xml, sun-ejb-jar.xml, sun-cmp-mappings.xml, etc.
     * @param resourceDir Directory that the DD files are or should be contained in.
     */
    public void init(File[] configFiles, File resourceDir, boolean keepUpdated) {
        assert configFiles != null && configFiles.length >= 1 : "No configuration files specified in SunONEDeploymentConfiguration.init()";
        assert resourceDir != null : "Directory for configuration files cannot be null.";
        
        System.out.println("SunONEDC.init(): resourceDir = " + resourceDir.getAbsoluteFile());
        for(int i = 0; i < configFiles.length; i++) {
            System.out.println("SunONEDC.init(): file[" + i + "] = " + configFiles[i]);
        }
        
        this.configFiles = configFiles;
        this.resourceDir = resourceDir;
        this.keepUpdated = keepUpdated;
        
        if(configFiles != null & configFiles.length > 0) {
            addConfiguration(configFiles[0], this);
        }
        
        J2eeModuleProvider provider = null;

        // Sync configuration instance with DataObjects if any of the configuration
        // files exist.  Otherwise, create the default configuration.
        try {
            try {
                // Find primary configuration file (e.g. sun-web.xml)
                FileObject fo = FileUtil.toFileObject(configFiles[0]);
                if(fo == null) {
                    provider = getProvider(configFiles[0].getParentFile());
                    if(provider != null) {
                        ConfigurationStorage storage = null;
                        try {
                            storage = new ConfigurationStorage(provider, this);
                            storage.save();
                        } finally {
                            if(storage != null) {
                                // !PW Might be nice to pass this to the data object so
                                // it doesn't have to create a new one.
                                storage.cleanup();
                                storage = null;
                            }
                        }
                    } else {
                        throw new IllegalStateException("No Project and/or J2eeModuleProvider located for " + configFiles[0].getPath());
                    }
                }
            } catch(IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } catch(InvalidModuleException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(SAXException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(ConfigurationException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        if(provider == null) {
            provider = getProvider(configFiles[0].getParentFile());
            if(provider == null) {
                throw new IllegalStateException("No Project and/or J2eeModuleProvider located for " + configFiles[0].getPath());
            }
        }

        if(keepUpdated) {
            // !PW Stepan thinks we might not need this anymore but I'm not so sure.
            // It was used in j2eeserver to enable autosaving of changes to the server
            // specific deployment descriptors when a change might have been made indirectly
            // by the user (for example, a wizard updating web.xml or ejb-jar.xml).
            ddFilesListener = new DDFilesListener(this, provider);
        }
    }
    
    public void dispose() {
        SunONEDeploymentConfiguration storedCfg = getConfiguration(configFiles[0]);
        if(storedCfg != null) {
            assert this == storedCfg;
            removeConfiguration(configFiles[0]);
        }
    }
    
    public void ensureResourceDefined(DDBean ddBean) {
        // !PW Moved here from ConfigurationSupportImpl.
        System.out.println("SunONEDeploymentConfiguration.ensureResourceDefined(" + ddBean + ")");
    }
    
    private J2eeModuleProvider getProvider(File file) {
        J2eeModuleProvider provider = null;
        if(file != null) {
            FileObject fo = FileUtil.toFileObject(file);
            if(fo != null) {
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
                }
            } else {
                provider = getProvider(file.getParentFile());
            }
        }
        return provider;
    }

    
    /** Retrieves DConfigBeanRoot associated with the specified DDBean root.  If
     *  this DCB has already created, retrieves it from cache, otherwise creates
     *  a new DCB via factory mechanism.
     *
     * @param dDBeanRoot
     * @throws ConfigurationException
     * @return
     */
    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot dDBeanRoot) throws ConfigurationException {
/*
 *		dcbroot = rootcache.get(ddroot)
 *		if dcbroot == null
 *			factory = lookup(moduletype)
 *			dcbroot = factory.create(ddroot)
 *			cache.add(dcbroot)
 *		return dcbroot
 */
        jsr88Logger.entering(this.getClass().toString(), "getDConfigBeanRoot", dDBeanRoot);
        
        if (null == dDBeanRoot) {
            throw Utils.makeCE("ERR_DDBeanIsNull", null, null);
        }
        
        if (null == dDBeanRoot.getXpath()) {
            throw Utils.makeCE("ERR_DDBeanHasNullXpath", null, null);
        }

        // If DDBean is not from our internal tree, normalize it to one that is.
        if(!(dDBeanRoot instanceof DDRoot)) {
            // If the root cache is empty, then it is likely that 
            assert getDCBRootCache().entrySet().size() > 0 : "No DDBeanRoots have been cached.  No way to normalize " + dDBeanRoot;
            dDBeanRoot = getStorage().normalizeDDBeanRoot(dDBeanRoot);
        }
        
        BaseRoot rootDCBean = (BaseRoot) getDCBRootCache().get(dDBeanRoot);
        
        if(null == rootDCBean) {
            DCBFactory factory = (DCBFactory) getDCBFactoryMap().get(dDBeanRoot.getXpath());
            if(factory != null) {
                rootDCBean = (BaseRoot) factory.createDCB(dDBeanRoot, null);
                
                if(rootDCBean != null) {
                    getDCBCache().put(dDBeanRoot, rootDCBean);
                    getDCBRootCache().put(dDBeanRoot, rootDCBean);
                }
            }
        }
        
        jsr88Logger.exiting(this.getClass().toString(), "getDConfigBeanRoot", dDBeanRoot);
        return rootDCBean;
    }
    
    /**
     * @return
     */
    public DeployableObject getDeployableObject() {
        return dObj;
    }
    
    /**
     * @param dConfigBeanRoot
     * @throws BeanNotFoundException
     */
    public void removeDConfigBean(DConfigBeanRoot dConfigBeanRoot) throws BeanNotFoundException {
        jsr88Logger.entering(this.getClass().toString(), "removeDConfigBean", dConfigBeanRoot);
        
        if(null != dConfigBeanRoot) {
            DDBeanRoot key = (DDBeanRoot) dConfigBeanRoot.getDDBean();
            BaseRoot deadBean = (BaseRoot) getDCBCache().remove(key);
            
            if(deadBean != null) {
                getDCBRootCache().remove(key);
            } else {
				Object [] args = new Object [1];
				args[0] = dConfigBeanRoot.toString();
				throw new BeanNotFoundException(MessageFormat.format(
					beanBundle.getString("ERR_DConfigBeanRootNotFoundOnRemove"), args));
            }
        }
        
        jsr88Logger.exiting(this.getClass().toString(), "removeDConfigBean", dConfigBeanRoot);
    }
    
    /** Restore the configuration object from a deployment plan .
     * This method reads the plan from the InputStream.  The plan is
     * not completely parsed, though. When a config bean needs to use data
     * is in the content, it will be converted into a bean graph.
     * @param inputStream
     * @throws ConfigurationException
     */
    public void restore(InputStream inputStream) throws ConfigurationException {
        jsr88Logger.entering(this.getClass().toString(), "restore", inputStream);
        
        restoreDConfigBean(inputStream, null);
        
        jsr88Logger.exiting(this.getClass().toString(), "restore", inputStream);
    }
    
    /**
     * @param inputStream
     * @param dDBeanRoot
     * @throws ConfigurationException
     * @return
     */
    public DConfigBeanRoot restoreDConfigBean(InputStream inputStream, DDBeanRoot dDBeanRoot) throws ConfigurationException {
        //Find the section of the deployment plan that implements the bean.
        jsr88Logger.finest("S1DepConfig:restoreDConfigBean(jiIS,DDBeanRoot)");
        // this stream has deployment plan format.
        //
        //jsr88Logger.finest("S1DepConfig:restore(jiIS)");
        
        DeploymentPlan dp = null;
		
        // Flush bean cache.  This forces reparsing of the new tree we load here.
        beanMap.clear();
		
        try {
            if (null != inputStream) {
                try {
                    if (this.dObj.getType().equals(ModuleType.WAR)) {
                        // read the sun-web.xml file in and conjure a
                        // deployment plan file object for it.
                        try {
                            SunWebApp sunW = DDProvider.getDefault().getWebDDRoot(inputStream);
                            dp = DeploymentPlan.createGraph();
                            FileEntry fe = new FileEntry();
                            fe.setName("sun-web.xml");
                            String s = new String();
                            java.io.StringWriter strWriter = new java.io.StringWriter();
                            sunW.write(strWriter);
                            fe.setContent(strWriter.toString());
                            dp.addFileEntry(fe);
                        } catch(DDException ex) {
                            // bad sun-web.xml file in stream.
                            jsr88Logger.finest(ex.getClass().getName() + " while processing sun-web.xml into a deployment plan: " + ex.getLocalizedMessage());
                            dp = DeploymentPlan.createGraph();
                        } catch(SAXException ex) {
                            // bad sun-web.xml file in stream.
                            jsr88Logger.finest(ex.getClass().getName() + " while processing sun-web.xml into a deployment plan: " + ex.getLocalizedMessage());
                            dp = DeploymentPlan.createGraph();
                        } catch(IOException ex) {
                            // error reading sun-web.xml file from stream.
                            jsr88Logger.finest(ex.getClass().getName() + " while processing sun-web.xml into a deployment plan: " + ex.getLocalizedMessage());
                            dp = DeploymentPlan.createGraph();
                        }
                    } else {
                        dp = DeploymentPlan.createGraph(inputStream);
                    }
                } catch (Schema2BeansRuntimeException s2bre) {
                    // vbk--todo : do more investigation before creating an
                    // empty plan object.
                    jsr88Logger.finest("the stream did not have a deployment plan");
                    dp = DeploymentPlan.createGraph();
                }
            } else {
                jsr88Logger.finest("the stream was null");
            }
            FileEntry [] entries = new FileEntry[0];
            if (null != dp) {
                FileEntry tentries[] = dp.getFileEntry();
                if (null != tentries) {
                    entries = tentries;
                }
            }
            for (int i = 0; i < entries.length ; i++) {
                String key = Utils.getFQNKey(entries[i].getUri(), entries[i].getName()); // !PW This is FileEntry.getUri(), not Base.getUri()
                contentMap.put(key, entries[i].getContent().getBytes());
            }
        } catch(Schema2BeansRuntimeException ex) {
            jsr88Logger.finest("Schema2Beans threw a Runtime Exception");
        } catch (Exception ex) {
            jsr88Logger.finest("foo");
            ConfigurationException ce =
            new ConfigurationException("bad plan stream");
            ce.initCause(ex);
            //throw ce;
        }
        
        // find the DConfigBean that corresponds to the
        // DDBeanRoot
        //
        List pending = new ArrayList();
        BaseRoot rootToRestore = null;
        
        if (null == dDBeanRoot) {
            // no rootbean, add roots from this deployment configuration
            Iterator rootIter = getDCBRootCache().entrySet().iterator();
            while(rootIter.hasNext()) {
                pending.add(((Map.Entry) rootIter.next()).getValue());
            }
            
            rootToRestore = getMasterDCBRoot();
        } else {
            rootToRestore = (BaseRoot) getDCBRootCache().get(dDBeanRoot);
            if(null != rootToRestore) {
                pending.add(rootToRestore);
            }
        }
        
        int index = 0;
        while (index < pending.size()) {
            Base current = (Base) pending.get(index);
            try {
                current.loadFromPlanFile(this);
            }
            catch (java.lang.IllegalStateException ise) {
                jsr88Logger.throwing(current.getClass().toString(), "loadFromPlanFile", ise);
                assert ise == null;
            }
            pending.addAll(current.getChildren());
            index++;
        }
        
        return rootToRestore;
    }
    
    /**
     * @param outputStream
     * @throws ConfigurationException
     */
    public void save(OutputStream outputStream) throws ConfigurationException {
        jsr88Logger.entering(this.getClass().toString(), "save", outputStream);
        
        saveDConfigBean(outputStream, null);
        
        jsr88Logger.exiting(this.getClass().toString(), "save", outputStream);
    }
    
    /**
     * @param outputStream
     * @param rootBean
     * @throws ConfigurationException
     */
    public void saveDConfigBean(OutputStream outputStream, DConfigBeanRoot rootBean) throws ConfigurationException {
        Object [] params = new Object[] {outputStream, rootBean} ;
        jsr88Logger.entering(this.getClass().toString(), "save", params);
        
        boolean useUriDataAtSave = false;
/*        if(null == rootBean || rootBean instanceof AppRoot) {
            useUriDataAtSave = true;
        }*/
        
        Map outputGraphs = new LinkedHashMap();
        
        // !PW FIXME added for beta -- remove after switching to DD API
        Map cmpGraphs = new LinkedHashMap();
        
        try {
            // Build the map of output bean graphs by calling addToGraphs() on
            // each root bean we have (or the one passed in, if any).
            //
            if(rootBean == null) {
                Iterator rootIter = getDCBRootCache().entrySet().iterator();
                while(rootIter.hasNext()) {
                    Base dcb = (Base) ((Map.Entry) rootIter.next()).getValue();
                    dcb.addToGraphs(outputGraphs, null, "");

                    // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
                    if(dcb instanceof EjbJarRoot) {
                        EjbJarRoot ejbJar = (EjbJarRoot) dcb;
                        Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                        if(cmpSnippet.hasDDSnippet()) {
                            String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                            cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                        }
                    }
                }
            } else {
                ((Base) rootBean).addToGraphs(outputGraphs, null, "");
                
                // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
                if(rootBean instanceof EjbJarRoot) {
                    EjbJarRoot ejbJar = (EjbJarRoot) rootBean;
                    Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                    if(cmpSnippet.hasDDSnippet()) {
                        String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                        cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                    }
                }
            }
            
            // combine the sun-XXX deployment descriptor bean graphs
            // in a deployment plan file graph.
            Set keys = outputGraphs.keySet();
            Iterator iter = keys.iterator();
            DeploymentPlan dp = new DeploymentPlan();
            CommonDDBean bean = null;
            while (iter.hasNext()) {
                Object k = iter.next();
                bean = (CommonDDBean) outputGraphs.get(k);
                if (null != bean) {
                    String keyString = (String) k;
                    String uri = Utils.getUriFromKey(keyString);
                    String fname = Utils.getFilenameFromKey(keyString);
                    FileEntry fe = new FileEntry();
                    fe.setName(fname);
                    if (useUriDataAtSave && uri.length() > 0) {
                        fe.setUri(uri);
                    }
                    String s = new String();
                    java.io.StringWriter strWriter = new java.io.StringWriter();
                    bean.write(strWriter);
                    fe.setContent(strWriter.toString());
                    dp.addFileEntry(fe);
                } else {
                    jsr88Logger.warning("no bean for key: " + k);
                }
            }
            
            // !PW FIXME also add cmp graphs
            keys = cmpGraphs.keySet();
            iter = keys.iterator();
            while (iter.hasNext()) {
                Object k = iter.next();
                org.netbeans.modules.schema2beans.BaseBean cmpRoot = 
                    (org.netbeans.modules.schema2beans.BaseBean) cmpGraphs.get(k);
                if (null != cmpRoot) {
                    String keyString = (String) k;
                    String uri = Utils.getUriFromKey(keyString);
                    String fname = Utils.getFilenameFromKey(keyString);
                    FileEntry fe = new FileEntry();
                    fe.setName(fname);
                    if (useUriDataAtSave && uri.length() > 0) {
                        fe.setUri(uri);
                    }
                    String s = new String();
                    java.io.StringWriter strWriter = new java.io.StringWriter();
                    cmpRoot.write(strWriter);
                    fe.setContent(strWriter.toString());
                    dp.addFileEntry(fe);
                } else {
                    jsr88Logger.warning("no bean for key: " + k);
                }
            }
            
            if (this.dObj.getType().equals(ModuleType.WAR)) {
                if (null != bean) {
                    bean.write(outputStream);
                }
            } else {
                dp.write(outputStream);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ConfigurationException ce = new ConfigurationException("error");
            ce.initCause(ex);
        }
        jsr88Logger.exiting(this.getClass().toString(), "save", params);
    }
    
/*    public void writeDDFilesIntoDirectory(java.io.File baseDir) {
        // create the deployment plan from its snippets
        Iterator rootIter = getDCBRootCache().entrySet().iterator();
        Map outputGraphs = new LinkedHashMap();
        Map cmpGraphs = new LinkedHashMap();    // !PW FIXME added for beta -- remove after switching to DD API
        while(rootIter.hasNext()) {
            Base dcb = (Base) ((Map.Entry) rootIter.next()).getValue();
            dcb.addToGraphs(outputGraphs, null, "");
            
            // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
            if(dcb instanceof EjbJarRoot) {
                EjbJarRoot ejbJar = (EjbJarRoot) dcb;
                Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                if(cmpSnippet.hasDDSnippet()) {
                    String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                    cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                }
            }
        }
        Set keys = outputGraphs.keySet();
        Iterator iter = keys.iterator();
        //		DeploymentPlan dp = new DeploymentPlan();
        while (iter.hasNext()) {
            Object k = iter.next();
            CommonDDBean bean = (CommonDDBean) outputGraphs.get(k);
            if (null != bean) {
                String keyString = (String) k;
                String uri = Utils.getUriFromKey(keyString);
                String fname = Utils.getFilenameFromKey(keyString);
                //				FileEntry fe = new FileEntry();
                //				fe.setName(fname);
                //				if (useUriDataAtSave && uri.length() > 0) {
                //					fe.setUri(uri);
                //				}
                //				String s = new String();
                java.io.File dest = Utils.createDestFile(baseDir, uri, fname);
                try {
                    java.io.FileWriter destWriter = new java.io.FileWriter(dest);
                    bean.write(destWriter);
                }
                catch (java.io.IOException ioe) {
                    jsr88Logger.severe("exception in writeDDFiles: " + ioe.getMessage());
                }
                catch (org.netbeans.modules.schema2beans.Schema2BeansException s2be) {
                    jsr88Logger.severe("exception in writeDDFiles: " + s2be.getMessage());
                }
            }
        }

        // !PW FIXME for cmp for beta
        keys = cmpGraphs.keySet();
        iter = keys.iterator();
        //		DeploymentPlan dp = new DeploymentPlan();
        while (iter.hasNext()) {
            Object k = iter.next();
            org.netbeans.modules.schema2beans.BaseBean cmpRoot = 
                (org.netbeans.modules.schema2beans.BaseBean) cmpGraphs.get(k);
            if (null != cmpRoot) {
                String keyString = (String) k;
                String uri = Utils.getUriFromKey(keyString);
                String fname = Utils.getFilenameFromKey(keyString);
                //				FileEntry fe = new FileEntry();
                //				fe.setName(fname);
                //				if (useUriDataAtSave && uri.length() > 0) {
                //					fe.setUri(uri);
                //				}
                //				String s = new String();
                java.io.File dest = Utils.createDestFile(baseDir, uri, fname);
                try {
                    java.io.FileWriter destWriter = new java.io.FileWriter(dest);
                    cmpRoot.write(destWriter);
                }
                catch (java.io.IOException ioe) {
                    jsr88Logger.severe("exception in writeDDFiles: " + ioe.getMessage());
                }
                catch (org.netbeans.modules.schema2beans.Schema2BeansException s2be) {
                    jsr88Logger.severe("exception in writeDDFiles: " + s2be.getMessage());
                }
            }
        }
        
        //		fe.setContent(strWriter.toString());
        //		dp.addFileEntry(fe);
    }
    */
    
    /** Get the schema2beans object graph that provides data for a DConfigBean
     * @param uri The uri for the descriptor source
     * @param fileName the name of the descriptor file
     * @param parser the ConfigParser that converts a stream into a bean graph
     * @param finder The ConfigFinder that accepts the parser's return value
     * and finds the subgraph for a DConfigBean
     * @return An Object to initialize the values in the DConfigBean
     */
    Object getBeans(String uri, String fileName, ConfigParser parser,
		ConfigFinder finder) {
        String key = Utils.getFQNKey(uri, fileName);
        Object retVal;
        Object root = beanMap.get(key);
        if (null == root) {
            // parse the content
            byte[] content = (byte[]) contentMap.get(key);
            if (null == content)
                return null;
            //Object root;
            //java.io.
            if (null == parser) {
                jsr88Logger.severe("Missing parser");
                return null;
            }
            try {
                root = parser.parse(new ByteArrayInputStream(content));
            }
            catch (Exception ex) {
                jsr88Logger.severe("content unparsable");
                return null;
            }
            beanMap.put(key, root);
        }
        retVal = finder.find(root);
        return retVal;
    }
    
    /* ------------------------- Utility Functions ------------------------
     */
    /** Loads and initializes the DConfigBean tree for this DeploymentConfiguration
     *  instance if it has not already been done yet.
     */
    public void ensureConfigurationLoaded() {
        // Retrieve storage, forcing it to be created if necessary, and initialized.
        getStorage();
    }

    /** Retrieves the ConfigurationStorage instance from the primary DataObject
     *  representing the saved version of this configuration.  The storage object
     *  should be created if necessary.  This will initialize the entire DConfigBean
     *  tree.
     */
    public ConfigurationStorage getStorage() {
        ConfigurationStorage storage = null;
        FileObject fo = FileUtil.toFileObject(configFiles[0]);
        if(fo != null) {
            try {
                DataObject dObj = DataObject.find(fo);
                storage = (ConfigurationStorage) dObj.getCookie(ConfigurationStorage.class);
            } catch(DataObjectNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return storage;
    }
    
    /** If this module is a web application, retrieve the WebAppRoot bean for it.
     *  You do not need to call ensureConfigurationLoaded() before calling this
     *  method, as this method will do so itself when it normalizes the DDRoot.
     */
    public WebAppRoot getWebAppRoot() {
        WebAppRoot war = null;
        
        DDBeanRoot root = dObj.getDDBeanRoot();
        if(null != root) {
            try {
                DConfigBeanRoot dcbRoot = getDConfigBeanRoot(getStorage().normalizeDDBeanRoot(root));
                if(dcbRoot instanceof WebAppRoot) {
                    war = (WebAppRoot) dcbRoot;
                }
            } catch (ConfigurationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DeployableObject with null DDBeanRoot!!!");
        }
        
        return war;
    }

    /** If this module is a ejbjar, retrieve the EjbJarRoot bean for it.
     *  You do not need to call ensureConfigurationLoaded() before calling this
     *  method, as this method will do so itself when it normalizes the DDRoot.
     */
    public EjbJarRoot getEjbJarRoot() {
        EjbJarRoot ejbJar = null;
        
        DDBeanRoot root = dObj.getDDBeanRoot();
        if(null != root) {
            try {
                DConfigBeanRoot dcbRoot = getDConfigBeanRoot(getStorage().normalizeDDBeanRoot(root));
                if(dcbRoot instanceof EjbJarRoot) {
                    ejbJar = (EjbJarRoot) dcbRoot;
                }
            } catch (ConfigurationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DeployableObject with null DDBeanRoot!!!");
        }
        
        return ejbJar;
    }
    
    /** Set the context root of the module this DeploymentConfiguration represents,
     *  if the deployable object is a WAR file.
     */
    public void setContextRoot(String contextRoot){
        if (dObj.getType().equals(ModuleType.WAR)) {
            WebAppRoot war = getWebAppRoot();
            if(war != null) {
                try {
                    war.setContextRoot(contextRoot);
                } catch(java.beans.PropertyVetoException ex){
                    // !PW Should not happen.
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "SunONEDeploymentConfiguration.setContextRoot() invoked on incorrect module type: " + dObj.getType());
        }        
    }
    
    /** Get the context root of the module this DeploymentConfiguration represents,
     *  if the deployable object is a WAR file.
     */
    public String getContextRoot() {
        String contextRoot = null;
        if (dObj.getType().equals(ModuleType.WAR)) {
            WebAppRoot war = getWebAppRoot();
            if(war != null) {
                contextRoot = war.getContextRoot();
            } else {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "SunONEDeploymentConfiguration.getContextRoot(): No WebAppRoot DConfigBean found for module.");
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "SunONEDeploymentConfiguration.getContextRoot() invoked on incorrect module type: " + dObj.getType());
        }
        return contextRoot;
    }
    
    /* Get the deploymentModuleName value which is usually passed in by an IDE
     * to define a good value for a directory name used for dir deploy actions.
     **/
    public String getDeploymentModuleName(){
        return deploymentModuleName;
    }
    
    /* Set the deploymentModuleName value which is usually passed in by an IDE
     * to define a good value for a directory name used for dir deploy actions.
     **/
    public void setDeploymentModuleName(String s){
        deploymentModuleName = s;
    }

    
    /* ------------------------------------------------------------------------
     * DDBeanRoot -> DConfigBeanRoot cache support
     *
     * moduleDCBCache is a map containing all root beans this configuration
     *   is responsible for.
     * completeDCBCache is the DCB cache for all DCB's by this configuration,
     *   it's root DCB's and their children.  Essentially all DCB's involved
     *   in a particular invocation of JSR-88.
     * patchCache is a list of any unpatched reference DCB's created as children
     *   of AppRoot (for now, could be anywhere we use a reference).  The key
     *   for such references is the ddBeanRoot they expect to be a reference to.
     */
    private Map moduleDCBCache = new LinkedHashMap(13);
    private Map completeDCBCache = new LinkedHashMap(63);
    private Map patchCache = new LinkedHashMap(13);
    
    /** Retrieve the cache containing all DConfigBeans owned by this configuration.
     *
     * @return Map containing all DConfigBeans owned by this configuration.
     */
    Map getDCBCache() {
        return completeDCBCache;
    }
    
    /** Retrieve the cache containing all root DConfigBeans owned by this configuration.
     *
     * @return Map containing all root DConfigBeans owned by this configuration.
     */
    Map getDCBRootCache() {
        return moduleDCBCache;
    }
    
    /** Retrieve the cache containing all reference DConfigBeans that are currently
     *  unpatched (no matching regular bean has been created for them yet.) owned
     *  by this configuration.
     *
     * @return Map containing all unpatched reference DConfigBeans owned by this
     *         configuration.
     */
    Map getPatchList() {
        return patchCache;
    }
    
    /** Retrieves the "master" DConfigBean root for this configuration (as opposed
     *  to just any root bean -- EAR files have multiple DConfigBeanRoots, the
     *  one representing sun-application.xml is the master in that case.)  For right
     *  now the logic just picks the first root in the cache on the assumption
     *  that the master was the first one created.  However there is no guarantee
     *  that will work so we should come up with something better.
     *
     * @return The DConfigBeanRoot owned by this configuration that is deemed to
     *   be the "master" root bean.  For example, in an EAR file, the master root
     *   bean is the bean representing sun-application.xml.
     */
    BaseRoot getMasterDCBRoot() {
        BaseRoot masterRoot = null;
        Iterator rootIterator = moduleDCBCache.entrySet().iterator();
        if(rootIterator.hasNext()) {
            masterRoot = (BaseRoot) ((Map.Entry) rootIterator.next()).getValue();
        }
        return masterRoot;
    }
    
        /* ------------------------------------------------------------------------
         * XPath to factory mapping support
         */
    private Map dcbFactoryMap = null;
    
    /** Retrieve the factory manager for this DConfigBean.  If one has not been
     *  constructed yet, create it.
     * @return
     */
    private Map getDCBFactoryMap() {
        if(dcbFactoryMap == null) {
            dcbFactoryMap = new HashMap(17);
            
            // Only factories that create a BaseRoot bean with no parent are
            // allowed here, e.g. DCBTopRootFactory.
            dcbFactoryMap.put("/application", new DCBTopRootFactory(AppRoot.class));				// EAR	// NOI18N
            dcbFactoryMap.put("/ejb-jar", new DCBTopRootFactory(EjbJarRoot.class));					// EJB	// NOI18N
            dcbFactoryMap.put("/web-app", new DCBTopRootFactory(WebAppRoot.class));					// WAR	// NOI18N
//            dcbFactoryMap.put("/application-client", new DCBTopRootFactory(AppClientRoot.class));	// RAR	// NOI18N
//            dcbFactoryMap.put("/connector", new DCBTopRootFactory(ConnectorRoot.class));			// CAR	// NOI18N
        }
        
        return dcbFactoryMap;
    }
    
    /** Factory that knows how to create and initialize root DConfigBeans from a
     *  DDBeanRoot passed in by the tool side of JSR-88.
     */
    private class DCBTopRootFactory implements DCBFactory {
        
        private Class dcbRootClass;
        
        DCBTopRootFactory(Class c) {
            dcbRootClass = c;
        }
        
        public Base createDCB(DDBean ddBean, Base dcbParent) throws ConfigurationException {
            if(ddBean == null) {
                throw Utils.makeCE("ERR_RootDDBeanIsNull", null, null);	// NOI18N
            }
			
            if(!(ddBean instanceof DDBeanRoot)) {
                Object [] args = new Object [1];
                args[0] = dcbRootClass.getName();
                throw Utils.makeCE("ERR_RootDDBeanWrongType", args, null); // NOI18N
            }
            
            DDBeanRoot ddbRoot = (DDBeanRoot) ddBean;
            BaseRoot newDCB = null;
            
            try {
                newDCB = (BaseRoot) dcbRootClass.newInstance();
                newDCB.init(ddbRoot, SunONEDeploymentConfiguration.this, ddbRoot);
            } catch(InstantiationException ex) {
                Object [] args = new Object [1];
                args[0] = dcbRootClass.getName();
                throw Utils.makeCE("ERR_UnexpectedInstantiateException", args, ex);	// NOI18N
            } catch(IllegalAccessException ex) {
                Object [] args = new Object [1];
                args[0] = dcbRootClass.getName();
                throw Utils.makeCE("ERR_UnexpectedIllegalAccessException", args, ex);	// NOI18N
            } catch (RuntimeException ex) {
                throw Utils.makeCE("ERR_UnexpectedRuntimeException", null, ex);	// NOI18N
            }
            
            return newDCB;
        }
    }
    
    // New methods to support the studio
    
    static private int BUF_LEN = 1024;
    
    // MS5 -- deal with the schema files...
    public void addFileToPlanForModule(File f, DeployableObject mod, ConfigurationStorage storage) throws ConfigurationException {
        // find the uri
        String uri = getUriForDeployableObject(mod, storage);
        // create the key
        String fname = f.getName();
        String key = Utils.getFQNKey(uri,fname);
        // read in the file's content
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = null;
        int totalRead = 0;
        try {
            fis = new FileInputStream(f);
            byte[] buf = new byte[BUF_LEN];
            int lastRead = 0;
            do {
                lastRead = fis.read(buf);
                if (lastRead > -1) {
                    baos.write(buf, 0, lastRead);
                    totalRead += lastRead;
                }
            }
            while (lastRead > -1);
        }
        catch (java.io.FileNotFoundException fnfe) {
            // somebody was being mean to us --
            //  log it. forget it. move on.
        }
        catch (java.io.IOException ioe) {
            // this is more drastic, Throw an exception here
            ConfigurationException ce = new ConfigurationException("Failed while reading"); // NOI18N
            ce.initCause(ioe);
            throw ce;
        }
        finally {
            if (null != fis)
                try {
                    fis.close();
                }
                catch (java.io.IOException ioe) {
                    // log this and move on
                }
        }
        //Document doc = GraphManager.createXmlDocument(fis, false);
        // convert it to the right form of xml
        //BaseBean bean = convertDocToBean(doc);
        // put it in the map
        if (totalRead > 0) {
            contentMap.put(key, baos.toByteArray());
        }
        else {
            // remove the old content
            contentMap.remove(key);
        }

        // refresh the configuration...
        //((BaseRoot) getDConfigBeanRoot(mod.getDDBeanRoot())).refresh();
        refreshGraphFromContentMap((BaseRoot) getDConfigBeanRoot(storage.normalizeDDBeanRoot(mod.getDDBeanRoot())));
    }
    
    public void extractFileFromPlanForModule(File f, DeployableObject mod, ConfigurationStorage storage) throws ConfigurationException {
        // find the uri
        String uri = getUriForDeployableObject(mod, storage);
        String fname = f.getName();
        // make sure the configuration is "saved"
        updateContentMap((BaseRoot) getDConfigBeanRoot(storage.normalizeDDBeanRoot(mod.getDDBeanRoot())));
        // create the key
        String key = Utils.getFQNKey(uri,fname);
        // get the bean
        byte[] content = (byte[]) contentMap.get(key);
        // save it into the file
        try {
            if (null != content) {
                File parentFile = f.getParentFile();
                FileObject folder = FileUtil.toFileObject(parentFile);
                if (folder == null) {
                    try {
                        folder = FileUtil.toFileObject(parentFile.getParentFile()).createFolder(parentFile.getName());
                    } catch (IOException ioe) {
                        throw new ConfigurationException(NbBundle.getMessage(SunONEDeploymentConfiguration.class, 
                                "MSG_FailedToCreateConfigFolder", parentFile.getAbsolutePath())); // NOI18N
                    }
                }
                FileLock lock = null;
                OutputStream out = null;
                try {
                    FileObject configFO = folder.getFileObject(fname);
                    if (configFO == null) {
                        configFO = folder.createData(fname);
                    }
                    lock = configFO.lock();
                    out = new BufferedOutputStream(configFO.getOutputStream(lock), 4096);
                    out.write(content);
                } finally {
                    if (out != null) {
                        try { out.close(); } catch(IOException ioe) {}
                    }
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }
    
    private String getUriForDeployableObject(DeployableObject mod, ConfigurationStorage storage) throws ConfigurationException {
        BaseRoot rootDCB = (BaseRoot) getDConfigBeanRoot(storage.normalizeDDBeanRoot(mod.getDDBeanRoot()));
        String retVal = rootDCB.getUriText();
        return retVal;
    }
    
    // this routine flushes the bean data into the content map
    //
    //void updateContentMap(BaseRoot br) {
    void updateContentMap(DConfigBeanRoot rootBean) throws ConfigurationException {
        jsr88Logger.entering(this.getClass().toString(), "save", rootBean);
        
//        boolean useUriDataAtSave = false;
//        if(null == rootBean || rootBean instanceof AppRoot) {
//            useUriDataAtSave = true;
//        }

        Map outputGraphs = new LinkedHashMap();
        Map cmpGraphs = new LinkedHashMap();    // !PW FIXME added for beta -- remove after switching to DD API
        try {
            // Build the map of output bean graphs by calling addToGraphs() on
            // each root bean we have (or the one passed in, if any).
            //
            if(rootBean == null) {
                Iterator rootIter = getDCBRootCache().entrySet().iterator();
                while(rootIter.hasNext()) {
                    Base dcb = (Base) ((Map.Entry) rootIter.next()).getValue();
                    dcb.addToGraphs(outputGraphs, null, ""); // NOI18N
                    
                    // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
                    if(dcb instanceof EjbJarRoot) {
                        EjbJarRoot ejbJar = (EjbJarRoot) dcb;
                        Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                        if(cmpSnippet.hasDDSnippet()) {
                            String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                            cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                        }
                    }
                }
            } else {
                ((Base) rootBean).addToGraphs(outputGraphs, null, "");
                // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
                if(rootBean instanceof EjbJarRoot) {
                    EjbJarRoot ejbJar = (EjbJarRoot) rootBean;
                    Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                    if(cmpSnippet.hasDDSnippet()) {
                        String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                        cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                    }
                }
            }
            
            // combine the sun-XXX deployment descriptor bean graphs
            // in a deployment plan file graph.
            Set keys = outputGraphs.keySet();
            Iterator iter = keys.iterator();
            DeploymentPlan dp = new DeploymentPlan();
            while (iter.hasNext()) {
                Object k = iter.next();
                CommonDDBean bean = (CommonDDBean) outputGraphs.get(k);
                if (null != bean) {
                    /*String keyString = (String) k;
                    String uri = Utils.getUriFromKey(keyString);
                    String fname = Utils.getFilenameFromKey(keyString);
                    FileEntry fe = new FileEntry();
                    fe.setName(fname);
                    if (useUriDataAtSave && uri.length() > 0) {
                        fe.setUri(uri);
                    }
                    String s = new String();
                    java.io.StringWriter strWriter = new java.io.StringWriter();*/
                    ByteArrayOutputStream baos = 
                        new ByteArrayOutputStream();
                    bean.write(baos);
                    contentMap.put(k, baos.toByteArray());
                    //fe.setContent(strWriter.toString());
                    //dp.addFileEntry(fe);
                } else {
                    jsr88Logger.warning("no bean for key: " + k); // NOI18N
                }
            }

            // !PW FIXME write out cmp graphs too.
            keys = cmpGraphs.keySet();
            iter = keys.iterator();
            while (iter.hasNext()) {
                Object k = iter.next();
                org.netbeans.modules.schema2beans.BaseBean cmpRoot = 
                    (org.netbeans.modules.schema2beans.BaseBean) cmpGraphs.get(k);
                if (null != cmpRoot) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    cmpRoot.write(baos);
                    contentMap.put(k, baos.toByteArray());
                } else {
                    jsr88Logger.warning("no bean for key: " + k); // NOI18N
                }
            }
            
            /*if (!this.dObj.getType().equals(ModuleType.EAR)) {
                if (null != bean) {
                    bean.write(outputStream);
                }
            } else {
                dp.write(outputStream);
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
            ConfigurationException ce = new ConfigurationException("error");
            ce.initCause(ex);
        }
        jsr88Logger.exiting(this.getClass().toString(), "save", rootBean); // NOI18N
    }
    
    private void refreshGraphFromContentMap(BaseRoot br) {
        List pending = new ArrayList();
        BaseRoot rootToRestore = br;
        
/*        if (null == dDBeanRoot) {
            // no rootbean, add roots from this deployment configuration
            Iterator rootIter = getDCBRootCache().entrySet().iterator();
            while(rootIter.hasNext()) {
                pending.add(((Map.Entry) rootIter.next()).getValue());
            }
            
            rootToRestore = getMasterDCBRoot();
        } else {
            rootToRestore = (BaseRoot) getDCBRootCache().get(dDBeanRoot);
            if(null != rootToRestore) {*/
        pending.add(rootToRestore);
        
        
        int index = 0;
        while (index < pending.size()) {
            Base current = (Base) pending.get(index);
            try {
                current.loadFromPlanFile(this);
            }
            catch (java.lang.IllegalStateException ise) {
                jsr88Logger.throwing(current.getClass().toString(), "loadFromPlanFile", // NOI18N
                ise);
                assert ise == null;
            }
            pending.addAll(current.getChildren());
            index++;
        }
        
    }

    // Formerly DeploymentPlanSplitter read/write config files.
    public void readDeploymentPlanFiles(ConfigurationStorage storage) throws ConfigurationException {
        int len = getValidatedNumberOfFiles(configFiles);
        for (int i = 0; i < len; i++) {
            addFileToPlanForModule(configFiles[i], dObj, storage);
        }
        
        for (int j = 0; j < configFiles.length; j++) {
            FileObject fo = FileUtil.toFileObject(configFiles [j]);
            if(fo != null) {
                fo.refresh(true);
            }
        }
    }
    
    public void writeDeploymentPlanFiles(ConfigurationStorage storage) throws ConfigurationException {
        int len = getValidatedNumberOfFiles(configFiles);
        for (int i = 0; i < len; i++) {
            extractFileFromPlanForModule(configFiles[i], dObj, storage);
        }
        
        for (int j = 0; j < configFiles.length; j++) {
            FileObject fo = FileUtil.toFileObject(configFiles [j]);
            if (fo != null) {
                fo.refresh(true);
            }
        }
    }
    
    int getValidatedNumberOfFiles(File[] files) throws ConfigurationException {
        int len = 0;
        if (null != files) {
            len = files.length;
        }
        
        // ?? This should check by module type
        if (len < 1) {
            throw new ConfigurationException("file list is too short"); // !PW should this be I18N????
        }
        
        return len;
    }    
}

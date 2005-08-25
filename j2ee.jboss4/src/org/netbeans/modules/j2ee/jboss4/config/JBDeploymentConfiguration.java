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

package org.netbeans.modules.j2ee.jboss4.config;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/** 
 * Base for JBoss DeploymentConfiguration implementations.
 *
 * @author  Pavel Buzek
 */
public abstract class JBDeploymentConfiguration implements DeploymentConfiguration {
    
    protected DeployableObject deplObj;
    
    protected DataObject dataObject;
    
    /** Creates a new instance of JBDeploymentConfiguration */
    public JBDeploymentConfiguration (DeployableObject deplObj) {
        this.deplObj = deplObj;
    }
    
    public DataObject getJBDataObject() {
        return dataObject;
    }
    
    // JSR-88 methods ---------------------------------------------------------
    
    public DeployableObject getDeployableObject () {
        return deplObj;
    }
    
    // JSR-88 methods empty implementation ------------------------------------
    
    public DConfigBeanRoot getDConfigBeanRoot (DDBeanRoot dDBeanRoot) 
    throws ConfigurationException {
        return null;
    }
    
    public void removeDConfigBean (DConfigBeanRoot dConfigBeanRoot) 
    throws BeanNotFoundException {
        throw new BeanNotFoundException ("bean not found "+dConfigBeanRoot); // NOI18N
    }
    
    public void restore (InputStream is) 
    throws ConfigurationException {
    }
    
    public DConfigBeanRoot restoreDConfigBean (InputStream is, DDBeanRoot dDBeanRoot) 
    throws ConfigurationException {
        return null;
    }
    
    public void saveDConfigBean (OutputStream os, DConfigBeanRoot dConfigBeanRoot) 
    throws ConfigurationException {
    }
    
    // helper methods -------------------------------------------------
    
    protected void writefile(final File file, final BaseBean bean) throws ConfigurationException {
        try {
            FileObject cfolder = FileUtil.toFileObject(file.getParentFile());
            if (cfolder == null) {
                File parentFile = file.getParentFile();
                try {
                    cfolder = FileUtil.toFileObject(parentFile.getParentFile()).createFolder(parentFile.getName());
                } catch (IOException ioe) {
                    throw new ConfigurationException(NbBundle.getMessage(JBDeploymentConfiguration.class, "MSG_FailedToCreateConfigFolder", parentFile.getAbsolutePath()));
                }
            }
            final FileObject folder = cfolder;
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = folder.getFileObject(name);
                        if (configFO == null) {
                            configFO = folder.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream (configFO.getOutputStream(lock), 4086);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try { os.close(); } catch(IOException ioe) {}
                        }
                        if (lock != null) 
                            lock.releaseLock();
                    }
                }
            });
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }
}

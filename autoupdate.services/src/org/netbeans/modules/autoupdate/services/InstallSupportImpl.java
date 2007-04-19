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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Restarter;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallSupportImpl {
    private InstallSupport support;
    private boolean progressRunning = false;
    private static Logger err = Logger.getLogger (InstallSupportImpl.class.getName ());
    
    public static final String UPDATE_DIR = "update"; // NOI18N
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String DOWNLOAD_DIR = UPDATE_DIR + FILE_SEPARATOR + "download"; // NOI18N
    public static final String NBM_EXTENTSION = ".nbm";
    private Map<UpdateElementImpl, File> element2Clusters;
    
    private static enum STEP {
        NOTSTARTED,
        DOWNLOAD,
        VALIDATION,
        INSTALLATION,
        RESTART,
        FINISHED,
        CANCEL
    }       

    private STEP currentStep = STEP.NOTSTARTED;
    
    // validation results
    private Collection<UpdateElement> trusted = new ArrayList<UpdateElement> ();
    private Collection<UpdateElement> signed = new ArrayList<UpdateElement> ();
    private Map<UpdateElement, Collection<Certificate>> certs = new HashMap<UpdateElement, Collection<Certificate>> ();
    
    private ExecutorService es = Executors.newSingleThreadExecutor();
    
    public InstallSupportImpl (InstallSupport installSupport) {
        support = installSupport;
    }
    
    public boolean doDownload (final ProgressHandle progress/*or null*/) throws OperationException {
        Callable<Boolean> downloadCallable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                assert support.getContainer().listInvalid().isEmpty();
                synchronized(this) {
                    currentStep = STEP.DOWNLOAD;
                }
                element2Clusters = new HashMap<UpdateElementImpl, File>();
                List<? extends OperationInfo> infos = support.getContainer().listAll();
                int size = 0;
                for (OperationInfo info : infos) {
                    size += info.getUpdateElement().getDownloadSize();
                }
                
                // start progress
                if (progress != null) {
                    progress.start();
                    progress.progress(NbBundle.getMessage(InstallSupportImpl.class, "InstallSupportImpl_Download_Estabilish"));
                    progressRunning = false;
                }
                
                int aggregateDownload = 0;
                
                try {
                    for (OperationInfo info : infos) {
                        synchronized(this) {
                            if (currentStep == STEP.CANCEL) return false;
                        }
                        int increment = doDownload(info, progress, aggregateDownload, size);
                        if (increment == -1) {
                            return false;
                        }
                        aggregateDownload += increment;
                    }
                }  finally {
                    // end progress
                    if (progress != null) {
                        progress.progress("");
                        progress.finish();
                    }
                }
                
                assert size == aggregateDownload : "Was downloaded " + aggregateDownload + ", planned was " + size;
                return true;
            }
        };
        
        boolean retval =  false;
        try {
            retval = es.submit(downloadCallable).get();
        } catch(InterruptedException iex) {
            Exceptions.printStackTrace(iex);
        } catch(ExecutionException iex) {
            if (! (iex.getCause() instanceof OperationException)) {
                Exceptions.printStackTrace(iex);
            } else {
                throw (OperationException) iex.getCause ();
            }
        }
        return retval;
    }

    public boolean doValidate (final Validator validator, final ProgressHandle progress/*or null*/) throws OperationException {
        assert validator != null;
        Callable<Boolean> validationCallable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                synchronized(this) {
                    assert currentStep != STEP.FINISHED;
                    if (currentStep == STEP.CANCEL) return false;
                    currentStep = STEP.VALIDATION;
                }
                assert support.getContainer().listInvalid().isEmpty();
                List<? extends OperationInfo> infos = support.getContainer().listAll();
                
                // start progress
                if (progress != null) {
                    progress.start();
                }
                
                int aggregateDownload = 0;
                
                try {
                    for (OperationInfo info : infos) {
                        synchronized(this) {
                            if (currentStep == STEP.CANCEL) return false;
                        }
                        UpdateElementImpl toUpdateImpl = Trampoline.API.impl(info.getUpdateElement());
                        boolean hasCustom = toUpdateImpl.getInstallInfo().getCustomInstaller() != null;
                        if (hasCustom) {
                        } else {
                            doValidate(info, progress, aggregateDownload);
                        }
                    }
                } finally {
                    // end progress
                    if (progress != null) {
                        progress.progress("");
                        progress.finish();
                    }
                }
                return true;
            }
        };
        boolean retval =  false;
        try {
            retval = es.submit(validationCallable).get();
        } catch(InterruptedException iex) {
            Exceptions.printStackTrace(iex);
        } catch(ExecutionException iex) {
            Exceptions.printStackTrace(iex);
        }
        return retval;
    }

    public Boolean doInstall (final Installer installer, final ProgressHandle progress/*or null*/) throws OperationException {
        assert installer != null;
        Callable<Boolean> installCallable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                synchronized(this) {
                    assert currentStep != STEP.FINISHED;
                    if (currentStep == STEP.CANCEL) return false;
                    currentStep = STEP.INSTALLATION;
                }
                assert support.getContainer().listInvalid().isEmpty();
                List<OperationInfo<InstallSupport>> infos = support.getContainer().listAll();
                if (progress != null) progress.start();
                
                boolean needsRestart = false;
                List<OperationInfo> customs = new ArrayList<OperationInfo> ();
                List<File> files4custom = new ArrayList<File> ();
                for (OperationInfo info : infos) {
                    synchronized(this) {
                        if (currentStep == STEP.CANCEL) return false;
                    }
                    UpdateElementImpl toUpdateImpl = Trampoline.API.impl(info.getUpdateElement());
                    OperationContainer c = support.getContainer();
                    
                    // find target dir
                    UpdateElement installed = info.getUpdateUnit().getInstalled();
                    File targetCluster = getTargetCluster(installed, toUpdateImpl);
                    
                    URL source = toUpdateImpl.getInstallInfo().getDistribution();
                    err.log(Level.FINE, "Source URL for " + toUpdateImpl.getCodeName() + " is " + source);
                    
                    boolean isNbmFile = source.getFile().toLowerCase(Locale.US).endsWith(NBM_EXTENTSION.toLowerCase(Locale.US));
                    
                    File dest = getDestination(targetCluster, toUpdateImpl.getCodeName(), isNbmFile);
                    
                    needsRestart |= needsRestart(installed != null, toUpdateImpl, dest);
                    
                    // find if UpdateElement has own installer
                    if (toUpdateImpl.getInstallInfo().getCustomInstaller() != null) {
                        customs.add(info);
                        files4custom.add(dest);
                    }
                }
                
                // XXX: solve custom installation first?
                for (OperationInfo info : customs) {
                    synchronized(this) {
                        if (currentStep == STEP.CANCEL) return false;
                    }
                    UpdateElementImpl toUpdateImpl = Trampoline.API.impl(info.getUpdateElement());
                    assert toUpdateImpl.getInstallInfo().getCustomInstaller() != null;
                    err.log(Level.FINE, "Call CustomInstaller[" + toUpdateImpl.getInstallInfo().getCustomInstaller() + "] of UpdateElement "
                            + info.getUpdateElement().getCodeName());
                    boolean res = toUpdateImpl.getInstallInfo().getCustomInstaller().install(
                            toUpdateImpl.getUpdateItemImpl().getUpdateItem(),
                            files4custom.get(customs.indexOf(info)));
                    if (! res) {
                        throw new OperationException(OperationException.ERROR_TYPE.INSTALL, "Installation of " + info + " failed.");
                    }
                }
                
                if (! needsRestart) {
                    synchronized(this) {
                        if (currentStep == STEP.CANCEL) return false;
                    }
                    List<File> filesInstallNow = getFileForInstall(customs, infos);
                    if (! filesInstallNow.isEmpty()) {
                        
                        // XXX: should run in single Thread
                        Thread th = org.netbeans.updater.UpdaterFrame.startFromIDE(
                                filesInstallNow.toArray(new File [0]),
                                refreshModulesListener,
                                NbBundle.getBranding());
                        
                        try {
                            th.join();
                            int rerunWaitCount = 0;
                            for (OperationInfo info : infos) {
                                Module module = Utils.toModule(info.getUpdateElement().getCodeName(),
                                        null);
                                for (; rerunWaitCount < 100 && (module == null || !module.isEnabled()); rerunWaitCount++) {
                                    Thread.currentThread().sleep(100);
                                    module = Utils.toModule(info.getUpdateElement().getCodeName(), null);
                                }
                            }
                        } catch(InterruptedException ie) {
                            th.interrupt();
                        }
                    }
                } else {
                    for (OperationInfo<InstallSupport> info : infos) {
                        Trampoline.API.impl(support.getContainer()).addScheduledForReboot(info.getUpdateElement());
                    }
                }
                
                try {
                    for (OperationInfo info : infos) {
                        UpdateUnit u = info.getUpdateUnit();
                        UpdateElement el = info.getUpdateElement();
                        Trampoline.API.impl(u).updateInstalled(el);
                    }
                } finally {
                    // end progress
                    if (progress != null) {
                        progress.progress("");
                        progress.finish();
                    }
                }
                
                return needsRestart ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        
        boolean retval =  false;
        try {
            retval = es.submit(installCallable).get();
        } catch(InterruptedException iex) {
            Exceptions.printStackTrace(iex);
        } catch(ExecutionException iex) {
            Exceptions.printStackTrace(iex);
        }
        return retval;
    }

    public void doRestart (Restarter validator,ProgressHandle progress/*or null*/) throws OperationException {
        synchronized(this) {
            assert currentStep != STEP.FINISHED;
            if (currentStep == STEP.CANCEL) return;
            currentStep = STEP.RESTART;
        }        
        Utilities.deleteInstall_Later();
        LifecycleManager.getDefault ().exit ();
    }
    
    public void doRestartLater(Restarter restarter) {    
        List<? extends OperationInfo> ois = support.getContainer().listAll();
        List<UpdateElement> elems = new ArrayList<UpdateElement>();
        
        for (OperationInfo operationInfo : ois) {
            elems.add(operationInfo.getUpdateElement());
        }
        Utilities.writeInstall_Later(new HashMap<UpdateElementImpl, File>(element2Clusters));                
    }

    public String getCertificate(Installer validator, UpdateElement uElement) {
        Collection<Certificate> c = certs.get (uElement);
        return c != null && c.size() > 0 ? c.iterator ().next ().toString () : null; // XXX allow more certificates for UpdateElement
    }

    public boolean isTrusted(Installer validator, UpdateElement uElement) {
        return trusted.contains (uElement);
    }

    public boolean isSigned(Installer validator, UpdateElement uElement) {
        return signed.contains (uElement);
    }

    public void doCancel () throws OperationException {
        synchronized(this) {
            currentStep = STEP.CANCEL;
        }
        List<OperationInfo> empty = Collections.emptyList ();
        for (File f : getFileForInstall (empty, support.getContainer ().listAll ())) {
            if (f != null && f.exists ()) {
                f.delete ();
            }
        }
    }
    
    private int doDownload (OperationInfo info, ProgressHandle progress, final int aggregateDownload, final int totalSize) throws OperationException {
        UpdateElement installed = info.getUpdateUnit ().getInstalled ();
        UpdateElementImpl toUpdateImpl = Trampoline.API.impl (info.getUpdateElement ());
        
        // find target dir
        File targetCluster = getTargetCluster (installed, toUpdateImpl);

        URL source = toUpdateImpl.getInstallInfo().getDistribution();
        err.log (Level.FINE, "Source URL for " + toUpdateImpl.getCodeName () + " is " + source);
        
        boolean isNbmFile = source.getFile ().toLowerCase (Locale.US).endsWith (NBM_EXTENTSION.toLowerCase (Locale.US));

        File dest = getDestination (targetCluster, toUpdateImpl.getCodeName(), isNbmFile);
        dest.delete ();

        int c = 0;
        
        // download
        try {
            String label = toUpdateImpl.getDisplayName ();
            c = copy (source, dest, progress, toUpdateImpl.getDownloadSize (), aggregateDownload, totalSize, label);
        } catch (IOException x) {
            err.log (Level.INFO, x.getMessage (), x);
            throw new OperationException (OperationException.ERROR_TYPE.PROXY, source.toString ());
        }
        
        return c;
    }

    private String doValidate (OperationInfo info, ProgressHandle progress, final int verified) throws OperationException {
        UpdateElement installed = info.getUpdateUnit ().getInstalled ();
        UpdateElementImpl toUpdateImpl = Trampoline.API.impl (info.getUpdateElement ());
        String status = null;
        
        // find target dir
        File targetCluster = getTargetCluster (installed, toUpdateImpl);

        File dest = getDestination (targetCluster, toUpdateImpl.getCodeName());
        assert dest.exists () : dest.getAbsolutePath();        

        int c = 0;
        
        // verify
        try {
            String label = toUpdateImpl.getDisplayName ();
            verifyNbm (info.getUpdateElement (), dest, progress, verified, label);
        } catch (Exception x) {
            err.log (Level.INFO, x.getMessage (), x);
        }

        return status;
    }

    static File getDestination (File targetCluster, String codeName, boolean isNbmFile) {
        err.log (Level.FINE, "Target cluster for " + codeName + " is " + targetCluster);
        File destDir = new File (targetCluster, DOWNLOAD_DIR);
        if (! destDir.exists ()) {
            destDir.mkdirs ();
        }
        String fileName = codeName.replace ('.', '-');
        File destFile = new File (destDir, fileName + (isNbmFile ? NBM_EXTENTSION : ""));
        err.log(Level.FINE, "Destination file for " + codeName + " is " + destFile);
        return destFile;
    }
    
    private static File getDestination (File targetCluster, String codeName) {
        return getDestination (targetCluster, codeName, true);
    }
    
    private int copy (URL source, File dest, 
            ProgressHandle progress, final int estimatedSize, final int aggregateDownload, final int totalSize,
            String label) throws MalformedURLException, IOException {
        
        int increment = 0;
        BufferedInputStream bsrc = new BufferedInputStream (source.openStream());
        BufferedOutputStream bdest = new BufferedOutputStream (new FileOutputStream (dest));
        
        err.log (Level.FINEST, "Copy " + source + " to " + dest + "[" + estimatedSize + "]");
        
        try {
            byte [] bytes = new byte [1024];
            int size;
            int c = 0;
            while ((size = bsrc.read (bytes)) != -1) {
                bdest.write (bytes, 0, size);
                increment += size;
                c += size;
                if (! progressRunning && progress != null) {
                    progress.switchToDeterminate (totalSize);
                    progressRunning = true;
                }
                if (c > 1024) {
                    if (progress != null) {
                        assert progressRunning;
                        progress.switchToDeterminate (totalSize);
                        int i = aggregateDownload + (increment < estimatedSize ? increment : estimatedSize);
                        progress.progress (label, i < totalSize ? i : totalSize);
                    }
                    c = 0;
                }
            }
            //assert estimatedSize == increment : "Increment (" + increment
            //        + ") of is equal to estimatedSize (" + estimatedSize + ").";
            if (estimatedSize != increment) {
                err.log (Level.FINEST, "Increment (" + increment + ") of is not equal to estimatedSize (" + estimatedSize + ").");
            }
        } catch (IOException ioe) {
            err.log (Level.INFO, "Writing content of URL " + source + " failed.", ioe);
        } finally {
            try {
                if (bsrc != null) bsrc.close ();
                if (bdest != null) bdest.flush ();
                if (bdest != null) bdest.close ();
            } catch (IOException ioe) {
                err.log (Level.INFO, ioe.getMessage (), ioe);
            }
        }
        err.log (Level.FINE, "Destination " + dest + " is successfully wrote. Size " + dest.length());
        
        return estimatedSize;
    }
    
    private void verifyNbm (UpdateElement el, File nbmFile, ProgressHandle progress, int verified, String label) {
        String res = null;
        try {
            Collection<Certificate> nbmCerts = getNbmCertificates (nbmFile, progress, verified, label);
            assert nbmCerts != null;
            if (nbmCerts.size () > 0) {
                assert nbmCerts.size () == 1 : "Only one certificate for " + nbmFile;
                certs.put (el, nbmCerts);
            }
            if (nbmCerts.isEmpty()) {
                res = "UNSIGNED";
            } else {
                List<Certificate> trustedCerts = new ArrayList<Certificate> ();
                for (KeyStore ks : Utilities.getKeyStore ()) {
                    trustedCerts.addAll (getCertificates (ks));
                }
                if (trustedCerts.containsAll (nbmCerts)) {
                    res = "TRUSTED";
                    trusted.add (el);
                    signed.add (el);
                } else {
                    res = "UNTRUSTED";
                    signed.add (el);
                }
            }
        } catch (IOException ioe) {
            err.log (Level.INFO, ioe.getMessage (), ioe);
            res = "BAD_DOWNLOAD";
        } catch (KeyStoreException kse) {
            err.log (Level.INFO, kse.getMessage (), kse);
            res = "CORRUPTED";
        }
        
        err.log (Level.FINE, "NBM " + nbmFile + " was verified as " + res);
    }
    
    private static Collection<Certificate> getCertificates (KeyStore keyStore) throws KeyStoreException {
        List<Certificate> certs = new ArrayList<Certificate> ();
        for (String alias: Collections.list (keyStore.aliases ())) {
            certs.add (keyStore.getCertificate (alias));
        }
        return certs;
    }
    
    private static Collection<Certificate> getNbmCertificates (File nbmFile,
            ProgressHandle progress,
            int verified,
            String label) throws IOException {
        
        JarFile jf = new JarFile (nbmFile);
        
        Set<Certificate> certs = new HashSet<Certificate> ();
        for (JarEntry entry : Collections.list (jf.entries ())) {
            verifyEntry (jf, entry, progress, verified, label);
            if (entry.getCertificates () != null) {
                certs.addAll (Arrays.asList(entry.getCertificates ()));
            }
        }
        
        return certs;
    }
    
    /**
     * @throws SecurityException
     */
    private static int verifyEntry (JarFile jf, JarEntry je, ProgressHandle progress, int verified, String label) throws IOException {
        InputStream is = null;
        try {
            is = jf.getInputStream (je);
            byte[] buffer = new byte[8192];
            int n;
            // we just read. this will throw a SecurityException
            int c = 0;
            while ((n = is.read (buffer, 0, buffer.length)) != -1) {
                c += n;
                if (c > 1024) {
                    verified += c;
                    if (progress != null) progress.progress (label);
                }
            }
        } finally {
            if (is != null) is.close ();
        }
        
        return verified;
    }
    
    private boolean needsRestart (boolean isUpdate, UpdateElementImpl toUpdateImpl, File dest) {
        return InstallManager.needsRestart (isUpdate, toUpdateImpl, dest);
    }
    
    private List<File> getFileForInstall (List<? extends OperationInfo> customs, List<? extends OperationInfo> infos) {
        assert customs != null;
        List<File> res = new ArrayList<File> ();
        for (OperationInfo info : infos) {
            if (customs.contains (info)) {
                err.log (Level.FINE, "UpdateElement " + info.getUpdateElement().getCodeName()
                        + " won't be installed by AU. It has own installer.");
                break;
            }
            UpdateElementImpl toUpdateImpl = Trampoline.API.impl (info.getUpdateElement ());
            OperationContainer c = support.getContainer ();
            
            // find target dir
            UpdateElement installed = info.getUpdateUnit ().getInstalled ();
            File targetCluster = getTargetCluster (installed, toUpdateImpl);

            URL source = toUpdateImpl.getInstallInfo().getDistribution();
            err.log (Level.FINE, "Source URL for " + toUpdateImpl.getCodeName () + " is " + source);

            File dest = getDestination (targetCluster, toUpdateImpl.getCodeName());
            res.add (dest);
        }
        return res;
    }
    
    private static final PropertyChangeListener refreshModulesListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent arg0) {
            // XXX: the modules list should be refresh automatically when config/Modules/ changes
            FileObject modulesRoot = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); // NOI18N
            err.log(Level.FINE,
                    "It\'s a hack: Call refresh on " + modulesRoot +
                    " file object.");
            if (modulesRoot != null) {
                modulesRoot.getParent().refresh();
                modulesRoot.refresh();
            }
        }
    };

    private File getTargetCluster(UpdateElement installed, UpdateElementImpl update) {
        File cluster = element2Clusters.get(update);
        if (cluster == null) {
            cluster = InstallManager.findTargetDirectory (installed, update);
            if (cluster != null) {
                element2Clusters.put(update, cluster);
            }
        }
        return cluster;
    }
}

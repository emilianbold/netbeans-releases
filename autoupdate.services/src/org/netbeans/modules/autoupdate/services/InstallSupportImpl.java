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
import java.security.AccessControlException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
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
    private Map<UpdateElementImpl, File> element2Clusters = null;
    private Set<File> downloadedFiles = null;
    private boolean isGlobal;
    private int wasDownloaded = 0;
    
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
    private Collection<UpdateElementImpl> trusted = new ArrayList<UpdateElementImpl> ();
    private Collection<UpdateElementImpl> signed = new ArrayList<UpdateElementImpl> ();
    private Map<UpdateElement, Collection<Certificate>> certs = new HashMap<UpdateElement, Collection<Certificate>> ();
    
    private ExecutorService es = null;
    
    public InstallSupportImpl (InstallSupport installSupport) {
        support = installSupport;
    }
    
    public boolean doDownload (final ProgressHandle progress/*or null*/, boolean isGlobal) throws OperationException {
        this.isGlobal = isGlobal;
        Callable<Boolean> downloadCallable = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                assert support.getContainer().listInvalid().isEmpty() : "Container contains no invalid OperationInfo, but " + support.getContainer().listInvalid();
                synchronized(this) {
                    currentStep = STEP.DOWNLOAD;
                }
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
                wasDownloaded = aggregateDownload;
                return true;
            }
        };
        
        boolean retval =  false;
        try {
            retval = getExecutionService ().submit (downloadCallable).get ();
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
                    progress.start (wasDownloaded);
                }
                
                int aggregateVerified = 0;
                
                try {
                    for (OperationInfo info : infos) {
                        synchronized(this) {
                            if (currentStep == STEP.CANCEL) return false;
                        }
                        UpdateElementImpl toUpdateImpl = Trampoline.API.impl(info.getUpdateElement());
                        boolean hasCustom = toUpdateImpl.getInstallInfo().getCustomInstaller() != null;
                        if (hasCustom) {
                            // XXX: validation of custom installed
                            assert false : "InstallSupportImpl cannot support CustomInstaller!";
                        } else {
                            aggregateVerified += doValidate (info, progress, aggregateVerified);
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
            retval = getExecutionService ().submit (validationCallable).get ();
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
                    assert currentStep != STEP.FINISHED : currentStep + " != STEP.FINISHED";
                    if (currentStep == STEP.CANCEL) return false;
                    currentStep = STEP.INSTALLATION;
                }
                assert support.getContainer ().listInvalid ().isEmpty () : "listInvalid() isEmpty() but " + support.getContainer ().listInvalid ();
                List<OperationInfo<InstallSupport>> infos = support.getContainer().listAll();
                Set<ModuleUpdateElementImpl> moduleImpls = new HashSet<ModuleUpdateElementImpl> ();
                Set<FeatureUpdateElementImpl> affectedFeatureImpls = new HashSet<FeatureUpdateElementImpl> ();
                
                if (progress != null) progress.start();
                
                for (OperationInfo info : infos) {
                    UpdateElementImpl toUpdateImpl = Trampoline.API.impl (info.getUpdateElement ());
                    switch (toUpdateImpl.getType ()) {
                    case KIT_MODULE :
                    case MODULE :
                        moduleImpls.add ((ModuleUpdateElementImpl) toUpdateImpl);
                        break;
                    case STANDALONE_MODULE :
                    case FEATURE :
                        affectedFeatureImpls.add ((FeatureUpdateElementImpl) toUpdateImpl);
                        moduleImpls.addAll (((FeatureUpdateElementImpl) toUpdateImpl).getContainedModuleElements ());
                        break;
                    default:
                        // XXX: what other types
                        assert false : "Unsupported type " + toUpdateImpl;
                    }
                }
                
                boolean needsRestart = false;
                for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
                    synchronized(this) {
                        if (currentStep == STEP.CANCEL) {
                            if (progress != null) progress.finish ();
                            return false;
                        }
                    }
                    
                    // skip installed element
                    if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                        continue;
                    }
                    
                    // find target dir
                    UpdateElement installed = moduleImpl.getUpdateUnit ().getInstalled ();
                    File targetCluster = getTargetCluster (installed, moduleImpl, isGlobal);
                    
                    URL source = moduleImpl.getInstallInfo ().getDistribution ();
                    err.log (Level.FINE, "Source URL for " + moduleImpl.getCodeName () + " is " + source);
                    
                    boolean isNbmFile = source.getFile().toLowerCase(Locale.US).endsWith(NBM_EXTENTSION.toLowerCase(Locale.US));
                    
                    File dest = getDestination(targetCluster, moduleImpl.getCodeName(), isNbmFile);
                    
                    needsRestart |= needsRestart(installed != null, moduleImpl, dest);
                }
                
                // store source of installed files
                Utilities.writeAdditionalInformation (getElement2Clusters ());
                
                if (! needsRestart) {
                    synchronized(this) {
                        if (currentStep == STEP.CANCEL) {
                            if (progress != null) progress.finish ();
                            return false;
                        }
                    }
                    
                    if (progress != null) progress.switchToDeterminate (moduleImpls.size ());

                    if (! getDownloadedFiles ().isEmpty ()) {
                        
                        // XXX: should run in single Thread
                        Thread th = org.netbeans.updater.UpdaterFrame.runFromIDE(
                                getDownloadedFiles ().toArray(new File [0]),
                                new RefreshModulesListener (progress),
                                NbBundle.getBranding(), false);
                        
                        try {
                            th.join();
                            for (ModuleUpdateElementImpl impl : moduleImpls) {
                                int rerunWaitCount = 0;
                                Module module = Utilities.toModule (impl.getCodeName(), impl.getSpecificationVersion ().toString ());
                                // XXX: consider again this
                                for (; rerunWaitCount < 100 && (module == null || !module.isEnabled()); rerunWaitCount++) {
                                    Thread.sleep(100);
                                    module = Utilities.toModule (impl.getCodeName(), impl.getSpecificationVersion ().toString ());
                                }
                                if (rerunWaitCount == 100) {
                                    err.log (Level.INFO, "Overflow checks of installed module " + module);
                                    th.interrupt();
                                    break;
                                }
                            }
                        } catch(InterruptedException ie) {
                            err.log (Level.INFO, ie.getMessage (), ie);
                            th.interrupt();
                        }
                    }
                } else {
                    for (ModuleUpdateElementImpl impl : moduleImpls) {
                        UpdateUnitFactory.getDefault ().scheduleForRestart (impl.getUpdateElement ());
                    }
                }
                
                try {
                    for (ModuleUpdateElementImpl impl : moduleImpls) {
                        UpdateUnit u = impl.getUpdateUnit ();
                        UpdateElement el = impl.getUpdateElement ();
                        Trampoline.API.impl(u).updateInstalled(el);
                    }
                    for (FeatureUpdateElementImpl impl : affectedFeatureImpls) {
                        UpdateUnit u = impl.getUpdateUnit ();
                        UpdateElement el = impl.getUpdateElement ();
                        Trampoline.API.impl(u).updateInstalled(el);
                    }
                } finally {
                    // end progress
                    if (progress != null) {
                        progress.progress("");
                        progress.finish();
                    }
                    downloadedFiles = null;
                }
                
                return needsRestart ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        
        boolean retval =  false;
        try {
            retval = getExecutionService ().submit (installCallable).get ();
        } catch(InterruptedException iex) {
            Exceptions.printStackTrace(iex);
        } catch(ExecutionException iex) {
            Exceptions.printStackTrace(iex);
        } finally {
            if (! retval) {
                getElement2Clusters ().clear ();
            }
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
        getElement2Clusters ().clear ();
        LifecycleManager.getDefault ().exit ();
    }
    
    public void doRestartLater(Restarter restarter) {    
        Utilities.writeInstall_Later(new HashMap<UpdateElementImpl, File>(getElement2Clusters ()));
        getElement2Clusters ().clear ();
    }

    public String getCertificate(Installer validator, UpdateElement uElement) {
        Collection<Certificate> certificates = certs.get (uElement);
        if (certificates != null) {
            String res = "";
            for (Certificate c :certificates) {
                res += c;
            }
            return res;
        } else {
            return null;
        }
    }

    public boolean isTrusted(Installer validator, UpdateElement uElement) {
        UpdateElementImpl impl = Trampoline.API.impl (uElement);
        boolean res = false;
        switch (impl.getType ()) {
        case KIT_MODULE :
        case MODULE :
            res = trusted.contains (impl);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl toUpdateFeatureImpl = (FeatureUpdateElementImpl) impl;
            Set<ModuleUpdateElementImpl> moduleImpls = toUpdateFeatureImpl.getContainedModuleElements ();
            res = ! moduleImpls.isEmpty ();
            for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
                // skip installed element
                if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                    continue;
                }
                
                res &= trusted.contains (moduleImpl);
            }
            break;
        default:
            // XXX: what other types
            assert false : "Unsupported type " + impl;
        }
        return res;
    }

    public boolean isSigned(Installer validator, UpdateElement uElement) {
        UpdateElementImpl impl = Trampoline.API.impl (uElement);
        boolean res = false;
        switch (impl.getType ()) {
        case KIT_MODULE :
        case MODULE :
            res = signed.contains (impl);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl toUpdateFeatureImpl = (FeatureUpdateElementImpl) impl;
            Set<ModuleUpdateElementImpl> moduleImpls = toUpdateFeatureImpl.getContainedModuleElements ();
            res = ! moduleImpls.isEmpty ();
            for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
                // skip installed element
                if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                    continue;
                }
                
                res &= signed.contains (moduleImpl);
            }
            break;
        default:
            // XXX: what other types
            assert false : "Unsupported type " + impl;
        }
        return res;
    }

    public void doCancel () throws OperationException {
        synchronized(this) {
            currentStep = STEP.CANCEL;
        }
        if (es != null) {
            try {
                es.shutdownNow ();
            } catch (AccessControlException ace) {
                err.log (Level.INFO, ace.getMessage (), ace);
            }
        }
        for (File f : getDownloadedFiles ()) {
            if (f != null && f.exists ()) {
                f.delete ();
            }
        }
        getDownloadedFiles ().clear ();
        
        // also mapping elements to cluster clear because global vs. local may be changed
        getElement2Clusters ().clear ();
    }
    
    private int doDownload (OperationInfo info, ProgressHandle progress, final int aggregateDownload, final int totalSize) throws OperationException {
        UpdateElement toUpdateElement = info.getUpdateElement();
        UpdateElementImpl toUpdateImpl = Trampoline.API.impl (toUpdateElement);
        int res = 0;
        switch (toUpdateImpl.getType ()) {
        case KIT_MODULE :
        case MODULE :
            res += doDownload (toUpdateImpl, progress, aggregateDownload, totalSize);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl toUpdateFeatureImpl = (FeatureUpdateElementImpl) toUpdateImpl;
            Set<ModuleUpdateElementImpl> moduleImpls = toUpdateFeatureImpl.getContainedModuleElements ();
            int nestedAggregateDownload = aggregateDownload;
            for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
                // skip installed element
                if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                    continue;
                }
                
                int increment = doDownload (moduleImpl, progress, nestedAggregateDownload, totalSize);
                if (increment == -1) {
                    return -1;
                }
                nestedAggregateDownload += increment;
                res += increment;
            }
            break;
        default:
            // XXX: what other types
            assert false : "Unsupported type " + toUpdateImpl;
        }
        return res;
    }
    
    private int doDownload (UpdateElementImpl toUpdateImpl, ProgressHandle progress, final int aggregateDownload, final int totalSize) throws OperationException {
        synchronized(this) {
            if (currentStep == STEP.CANCEL) {
                return -1;
            }
        }
        UpdateElement installed = toUpdateImpl.getUpdateUnit ().getInstalled ();
        
        // find target dir
        File targetCluster = getTargetCluster (installed, toUpdateImpl, isGlobal);
        assert targetCluster != null : "Target cluster for " + toUpdateImpl + " must exist.";
        if (targetCluster == null) {
            targetCluster = InstallManager.getUserDir ();
        }

        URL source = toUpdateImpl.getInstallInfo().getDistribution();
        err.log (Level.FINE, "Source URL for " + toUpdateImpl.getCodeName () + " is " + source);
        
        boolean isNbmFile = source.getFile ().toLowerCase (Locale.US).endsWith (NBM_EXTENTSION.toLowerCase (Locale.US));

        File dest = getDestination (targetCluster, toUpdateImpl.getCodeName(), isNbmFile);
        dest.delete ();

        int c = 0;
        
        // download
        try {
            String label = toUpdateImpl.getDisplayName ();
            getDownloadedFiles ().add (FileUtil.normalizeFile (dest));
            c = copy (source, dest, progress, toUpdateImpl.getDownloadSize (), aggregateDownload, totalSize, label);
        } catch (IOException x) {
            err.log (Level.INFO, x.getMessage (), x);
            throw new OperationException (OperationException.ERROR_TYPE.PROXY, source.toString ());
        }
        
        return c;
    }

    private int doValidate (OperationInfo info, ProgressHandle progress, final int verified) throws OperationException {
        UpdateElement toUpdateElement = info.getUpdateElement();
        UpdateElementImpl toUpdateImpl = Trampoline.API.impl (toUpdateElement);
        int increment = 0;
        switch (toUpdateImpl.getType ()) {
        case KIT_MODULE :
        case MODULE :
            increment = doValidate (toUpdateImpl, progress, verified);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl toUpdateFeatureImpl = (FeatureUpdateElementImpl) toUpdateImpl;
            Set<ModuleUpdateElementImpl> moduleImpls = toUpdateFeatureImpl.getContainedModuleElements ();
            int nestedVerified = verified;
            for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
                // skip installed element
                if (Utilities.isElementInstalled (moduleImpl.getUpdateElement ())) {
                    continue;
                }
                int singleIncrement = doValidate (moduleImpl, progress, nestedVerified);
                nestedVerified += singleIncrement;
                increment += singleIncrement;
            }
            break;
        default:
            // XXX: what other types
            assert false : "Unsupported type " + toUpdateImpl;
        }
        return increment;
    }
    
    private int doValidate (UpdateElementImpl toUpdateImpl, ProgressHandle progress, final int verified) throws OperationException {
        UpdateElement installed = toUpdateImpl.getUpdateUnit ().getInstalled ();
        
        // find target dir
        File targetCluster = getTargetCluster (installed, toUpdateImpl, isGlobal);

        File dest = getDestination (targetCluster, toUpdateImpl.getCodeName());
        assert dest.exists () : dest.getAbsolutePath();        
        
        int wasVerified = 0;

        // verify
        try {
            wasVerified = verifyNbm (toUpdateImpl.getUpdateElement (), dest, progress, verified);
        } catch (Exception x) {
            err.log (Level.INFO, x.getMessage (), x);
        }
        
        return wasVerified;
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
    
    private int verifyNbm (UpdateElement el, File nbmFile, ProgressHandle progress, int verified) {
        String res = null;
        try {
            verified += el.getDownloadSize ();
            if (progress != null) {
                progress.progress (el.getDisplayName (), verified < wasDownloaded ? verified : wasDownloaded);
            }
            Collection<Certificate> nbmCerts = getNbmCertificates (nbmFile);
            assert nbmCerts != null;
            if (nbmCerts.size () > 0) {
                certs.put (el, nbmCerts);
            }
            if (nbmCerts.isEmpty()) {
                res = "UNSIGNED";
            } else {
                List<Certificate> trustedCerts = new ArrayList<Certificate> ();
                UpdateElementImpl impl = Trampoline.API.impl(el);
                for (KeyStore ks : Utilities.getKeyStore ()) {
                    trustedCerts.addAll (getCertificates (ks));
                }
                if (trustedCerts.containsAll (nbmCerts)) {
                    res = "TRUSTED";
                    trusted.add (impl);
                    signed.add (impl);
                } else {
                    res = "UNTRUSTED";
                    signed.add (impl);
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
        return el.getDownloadSize ();
    }
    
    private static Collection<Certificate> getCertificates (KeyStore keyStore) throws KeyStoreException {
        List<Certificate> certs = new ArrayList<Certificate> ();
        for (String alias: Collections.list (keyStore.aliases ())) {
            certs.add (keyStore.getCertificate (alias));
        }
        return certs;
    }
    
    private static Collection<Certificate> getNbmCertificates (File nbmFile) throws IOException {
        Set<Certificate> certs = new HashSet<Certificate>();
        JarFile jf = new JarFile(nbmFile);
        try {
            for (JarEntry entry : Collections.list(jf.entries())) {
                verifyEntry(jf, entry);
                if (entry.getCertificates() != null) {
                    certs.addAll(Arrays.asList(entry.getCertificates()));
                }
            }
        } finally {
            jf.close();
        }

        return certs;
    }
    
    /**
     * @throws SecurityException
     */
    private static void verifyEntry (JarFile jf, JarEntry je) throws IOException {
        InputStream is = null;
        try {
            is = jf.getInputStream (je);
            byte[] buffer = new byte[8192];
            int n;
            int c = 0;
            while ((n = is.read (buffer, 0, buffer.length)) != -1);
        } finally {
            if (is != null) is.close ();
        }
        
        return;
    }
    
    private boolean needsRestart (boolean isUpdate, UpdateElementImpl toUpdateImpl, File dest) {
        return InstallManager.needsRestart (isUpdate, toUpdateImpl, dest);
    }
    
    private static final class RefreshModulesListener implements PropertyChangeListener  {
        private ProgressHandle handle;
        private int i;
        
        public RefreshModulesListener (ProgressHandle handle) {
            this.handle = handle;
            this.i = 0;
        }
        
        public void propertyChange(PropertyChangeEvent arg0) {
            if (org.netbeans.updater.UpdaterFrame.RUNNING.equals (arg0.getPropertyName ())) {
                if (handle != null) {
                    handle.progress (i++);
                }
            } else if (org.netbeans.updater.UpdaterFrame.FINISHED.equals (arg0.getPropertyName ())){
                // XXX: the modules list should be refresh automatically when config/Modules/ changes
                final FileObject modulesRoot = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); // NOI18N
                err.log(Level.FINE,
                        "It\'s a hack: Call refresh on " + modulesRoot +
                        " file object.");
                if (modulesRoot != null) {
                    try {
                        Repository.getDefault().getDefaultFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                modulesRoot.getParent().refresh();
                                modulesRoot.refresh();
                            }
                        });
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } else {
                assert false : "Unknown property " + arg0.getPropertyName ();
            }
        }
    };

    private File getTargetCluster(UpdateElement installed, UpdateElementImpl update, boolean isGlobal) {
        File cluster = getElement2Clusters ().get (update);
        if (cluster == null) {
            cluster = InstallManager.findTargetDirectory (installed, update, isGlobal);
            if (cluster != null) {
                getElement2Clusters ().put(update, cluster);
            }
        }
        return cluster;
    }
    
    private  Map<UpdateElementImpl, File> getElement2Clusters () {
        if (element2Clusters == null) {
            element2Clusters = new HashMap<UpdateElementImpl, File> ();
        }
        return element2Clusters;
    }
    
    private ExecutorService getExecutionService () {
        if (es == null || es.isShutdown ()) {
            es = Executors.newSingleThreadExecutor ();
        }
        return es;
    }
    
    private Set<File> getDownloadedFiles () {
        if (downloadedFiles == null) {
            downloadedFiles = new HashSet<File> ();
        }
        return downloadedFiles;
    }
}

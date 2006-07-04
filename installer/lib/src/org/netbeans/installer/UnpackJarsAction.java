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

package org.netbeans.installer;

import com.installshield.product.ProductAction;
import com.installshield.product.ProductActionSupport;
import com.installshield.product.ProductBuilderSupport;
import com.installshield.product.ProductException;
import com.installshield.product.RequiredBytesTable;
import com.installshield.util.FileAttributes;
import com.installshield.util.Log;
import com.installshield.wizard.service.MutableOperationState;
import com.installshield.wizard.service.exitcode.ExitCodeService;
import com.installshield.wizard.service.file.FileService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class UnpackJarsAction extends ProductAction {
    
    //return code incase an error returns
    public static final int UNPACK_JARS_UNHANDLED_ERROR = -400;

    private static final String JARS_CATALOG_FILE = "packedjars.xml";
    
    private int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String ideClusterDir;
    private String platformClusterDir;
    private String sbDestDir;
    
    private String statusDesc = "";
    private String nbInstallDir = "";
    private String rootInstallDir = "";
    private String uninstDir = "";
    private String tempPath = "";

    private List<JarItem> jarList = new ArrayList<JarItem>();

    private long totalOriginalSize;
    private long totalPackedSize;
    
    private boolean success = false;
    
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;

    private ProgressInfo progressInfo;
    
    public UnpackJarsAction () {
    }
    
    public void build (ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass("org.netbeans.installer.RunCommand$StreamAccumulator");
            support.putClass(Util.class.getName());
            support.putClass(UnpackJarsAction.ProgressThread.class.getName());
            support.putClass(UnpackJarsAction.ErrorCatcher.class.getName());
            support.putClass(UnpackJarsAction.JarItem.class.getName());
            support.putClass(UnpackJarsAction.ProgressInfo.class.getName());
            support.putClass(XMLUtil.class.getName());
            support.putClass(XMLUtil.CustomEntityResolver.class.getName());
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void init (ProductActionSupport support) {
	/*
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
        instDirPath = resolveString((String)pservice.getProductBeanProperty(productURL,null,"absoluteInstallLocation")); */
        rootInstallDir = resolveString("$P(absoluteInstallLocation)");
        if (Util.isMacOSX()) {
            nbInstallDir = rootInstallDir + File.separator 
            + resolveString("$L(org.netbeans.installer.Bundle,Product.nbLocationBelowInstallRoot)");
        } else {
            nbInstallDir = rootInstallDir;
        }
        logEvent(this, Log.DBG,"nbInstallDir: " + nbInstallDir);
        uninstDir = nbInstallDir + File.separator + "_uninst";
        logEvent(this, Log.DBG,"uninstDir: " + uninstDir);
        
        mutableOperationState = support.getOperationState();

        progressInfo = new ProgressInfo();
    }
    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.unpackJarsMessage)")
	+ " "
	+ resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.waitMessage)") ;
        
        support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = INSTALL;
            
            long startTime = System.currentTimeMillis();

            File catalog = new File(uninstDir, JARS_CATALOG_FILE);
            if (!catalog.exists()) {
                try {
                    logEvent(this, Log.ERROR,"# # # # # # # #");
                    logEvent(this, Log.ERROR,"Fatal error: Cannot find jar catalog file: " + catalog);
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Could not set exit code.");
                }
                return;
            }
            boolean ret;
            ret = parseCatalog(catalog.getAbsolutePath());
            if (!ret) {
                try {
                    logEvent(this, Log.ERROR,"# # # # # # # #");
                    logEvent(this, Log.ERROR,"Fatal error: Cannot parse jar catalog.");
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Could not set exit code.");
                }
                return;
            }

            startProgress();
            
            ret = unpackJars();
            if (!ret) {
                try {
                    logEvent(this, Log.ERROR,"# # # # # # # #");
                    logEvent(this, Log.ERROR,"Fatal error: Cannot unpack jars.");
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Could not set exit code.");
                }
                return;
            }
            
            long endTime = System.currentTimeMillis();

            logEvent(this, Log.DBG,"Unpack Jars took: " + (endTime - startTime) + "ms");
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        } finally {
            stopProgress();
        }
    }
    
    /** Does cleaning of unpacked jars. */
    public void uninstall(ProductActionSupport support) {
        init(support);
        installMode = UNINSTALL;
        //Perform work
        File catalog = new File(uninstDir, JARS_CATALOG_FILE);
        if (!catalog.exists()) {
            try {
                logEvent(this, Log.ERROR,"# # # # # # # #");
                logEvent(this, Log.ERROR,"Fatal error: Cannot find jar catalog file: " + catalog);
                ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
            } catch (Exception ex) {
                logEvent(this, Log.ERROR, "Could not set exit code.");
            }
            return;
        }
        boolean ret;
        ret = parseCatalog(catalog.getAbsolutePath());
        if (!ret) {
            try {
                logEvent(this, Log.ERROR,"# # # # # # # #");
                logEvent(this, Log.ERROR,"Fatal error: Cannot parse jar catalog.");
                ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
            } catch (Exception ex) {
                logEvent(this, Log.ERROR, "Could not set exit code.");
            }
            return;
        }
        deleteJars();
        catalog.delete();
    }
    
    /**
     * Returns difference between total size of uncompressed jars and total size
     * of compressed jars size to show correct total install size on Preview panel.
     * Total size of compressed jars is already included in beanCoreIDE.
     */
    public long getCheckSum() {
        return 72000000L;
    }

    /* Returns the required bytes table information.
     * @return required bytes table.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
        RequiredBytesTable req = new RequiredBytesTable();
        
        nbInstallDir = resolveString("$P(absoluteInstallLocation)");
        logEvent(this,Log.DBG,"getRequiredBytes nbInstallDir: " + nbInstallDir);
        req.addBytes(nbInstallDir, getCheckSum());
        
        return req;
    }

    /** Unpack jars. */
    private boolean unpackJars () {
        logEvent(this,Log.DBG,"unpackJars ENTER");

        FileService fileService = null;
        try {
            fileService = (FileService) getService(FileService.NAME);
            if (fileService == null) {
                logEvent(this,Log.ERROR,"Error: Cannot get FileService.");
            }
        } catch (Exception ex) {
            logEvent(this,Log.ERROR,"Error: Cannot get FileService.");
            logEvent(this,Log.ERROR,"Exception: " + ex.getMessage());
            Util.logStackTrace(this,ex);
        }
        FileAttributes attrs = new FileAttributes();
        attrs.setAttributes(FileAttributes.OWNER_READABLE | FileAttributes.OWNER_WRITEABLE |
        FileAttributes.GROUP_READABLE | FileAttributes.WORLD_READABLE);

        Pack200.Unpacker unpacker = Pack200.newUnpacker();
        long completedBytes = 0L;
        for (int i = 0; i < jarList.size(); i++) {
            JarItem item = jarList.get(i);
            progressInfo.setFileName(item.fileName);
            File fileIn = new File(item.fileName + ".pack.gz");
            File fileOut = new File(item.fileName);
            if (!fileIn.exists()) {
                logEvent(this,Log.ERROR,"Cannot find file: " + fileIn);
                return false;
            }
            logEvent(this,Log.DBG,"Unpacking file: " + fileIn);
            try {
                JarOutputStream os = new JarOutputStream(new FileOutputStream(fileOut));
                unpacker.unpack(fileIn, os);
                os.close();
            } catch (IOException ex) {
                logEvent(this,Log.ERROR,"Unpacking failed at item[" + i + "]: " + item.toString());
                Util.logStackTrace(this,ex);
                return false;
            }
            try {
                //Set for non Windows OS user+rw group+r other+r
                if (!Util.isWindowsOS() && (fileService != null)) {
                    fileService.setFileAttributes(fileOut.getAbsolutePath(),attrs);
                }
            } catch (Exception ex) {
                logEvent(this,Log.ERROR,"Error: Cannot set file attributes for: " + fileOut);
                logEvent(this,Log.ERROR,"Exception: " + ex.getMessage());
                Util.logStackTrace(this,ex);
            }
            fileOut.setLastModified(item.time);
            completedBytes += fileOut.length();
            progressInfo.setCompletedBytes(completedBytes);
            fileIn.delete();
        }
        return true;
    }

    /** Delete jars. */
    private void deleteJars () {
        logEvent(this,Log.DBG,"deleteJars ENTER");
        Pack200.Unpacker unpacker = Pack200.newUnpacker();
        for (int i = 0; i < jarList.size(); i++) {
            JarItem item = jarList.get(i);
            File file = new File(item.fileName);
            if (file.exists()) {
                logEvent(this,Log.DBG,"Deleting file: " + file);
                file.delete();
            } else {
                logEvent(this,Log.ERROR,"Cannot find file: " + file);
            }
        }
    }

    /** Parse jar catalog XML file. */
    private boolean parseCatalog (String inputFileName) {
        logEvent(this,Log.DBG,"parseCatalog ENTER");
        //Replace spaces in URL string
        String s;
        if (Util.isWindowsOS()) {
            //On Windows path starts with disk like C: so we must add additional slash
            s = "file:///" + inputFileName.replaceAll(" ","%20");
        } else {
            s = "file://" + inputFileName.replaceAll(" ","%20");
        }
        logEvent(this, Log.DBG,"URL: " + s);
        URL url = null;
        try {
            url = new URL(s);
        } catch (MalformedURLException ex) {
            logEvent(this,Log.ERROR,"Cannot find jar catalog. URL:" + s);
            Util.logStackTrace(this,ex);
            return false;
        }
        Document doc = null;
        try {
            doc = parseDocument(url);
        } catch (IOException ex) {
            logEvent(this,Log.ERROR,"Cannot parse jar catalog. Broken XML file:" + url.toString());
            Util.logStackTrace(this,ex);
            return false;
        } catch (SAXException ex) {
            logEvent(this,Log.ERROR,"Cannot parse jar catalog. Broken XML file:" + url.toString());
            Util.logStackTrace(this,ex);
            return false;
        }
        if (doc.getDocumentElement() == null ) {
            logEvent(this,Log.ERROR,"Missing root element. Broken XML file:" + url.toString());
            return false;
        } else {
            NodeList nodeList;

            nodeList = doc.getElementsByTagName("summary");
            System.out.println("summary len: " + nodeList.getLength());
            if (nodeList.getLength() == 0) {
                logEvent(this,Log.ERROR,"Missing summary in jar catalog.");
                return false;
            }
            Node node = nodeList.item(0);
            Node attr;

            attr = node.getAttributes().getNamedItem("total-original-size");
            s = attr.getNodeValue();
            totalOriginalSize = -1;
            try {
                totalOriginalSize = Long.parseLong(s);
            } catch (NumberFormatException ex) {
                logEvent(this,Log.ERROR,"Cannot parse value of total-original-size: " + s);
                Util.logStackTrace(this,ex);
            }

            attr = node.getAttributes().getNamedItem("total-packed-size");
            s = attr.getNodeValue();
            totalPackedSize = -1;
            try {
                totalPackedSize = Long.parseLong(s);
            } catch (NumberFormatException ex) {
                logEvent(this,Log.ERROR,"Cannot parse value of total-packed-size: " + s);
                Util.logStackTrace(this,ex);
            }

            nodeList = doc.getElementsByTagName("jar");
            System.out.println("jar len: " + nodeList.getLength());

            for (int i = 0, ind = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                String fileName;
                long time, origSize, packedSize;

                attr = node.getAttributes().getNamedItem("name");
                fileName = nbInstallDir + File.separator + attr.getNodeValue();
                //System.out.println("attValue:" + attr.getNodeValue());

                attr = node.getAttributes().getNamedItem("time");
                s = attr.getNodeValue();
                time = -1;
                try {
                    time = Long.parseLong(s);
                } catch (NumberFormatException ex) {
                    logEvent(this,Log.ERROR,"Cannot parse value of time: " + s);
                    Util.logStackTrace(this,ex);
                }
                //System.out.println("attValue:" + attr.getNodeValue());

                attr = node.getAttributes().getNamedItem("original-size");
                s = attr.getNodeValue();
                origSize = -1;
                try {
                    origSize = Long.parseLong(s);
                } catch (NumberFormatException ex) {
                    logEvent(this,Log.ERROR,"Cannot parse value of original-size: " + s);
                    Util.logStackTrace(this,ex);
                }
                //System.out.println("attValue:" + attr.getNodeValue());

                attr = node.getAttributes().getNamedItem("packed-size");
                s = attr.getNodeValue();
                packedSize = -1;
                try {
                    packedSize = Long.parseLong(s);
                } catch (NumberFormatException ex) {
                    logEvent(this,Log.ERROR,"Cannot parse value of packed-size: " + s);
                    Util.logStackTrace(this,ex);
                }

                JarItem item = new JarItem(fileName,time,origSize,packedSize);
                jarList.add(item);
                //System.out.println("attValue:" + attr.getNodeValue());
            }
        }
        return true;
    }

    private Document parseDocument (URL url) throws SAXException, IOException {
        Document document = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        InputSource is = new InputSource(reader);
        
        Document doc = XMLUtil.parse(is,false,false,new ErrorCatcher(),XMLUtil.createResolver());            
                
        return doc;
    }
    
    static class JarItem {
        String fileName;
        long time;
        long origSize;
        long packedSize;

        public JarItem (String fileName, long time, long origSize, long packedSize) {
            this.fileName = fileName;
            this.time = time;
            this.origSize = origSize;
            this.packedSize = packedSize;
        }

        public String toString() {
            return "File:" + fileName + " Time:" + time + " Original size:" + origSize
            + " Packed size:" + packedSize;
        }
    }

    class ErrorCatcher implements org.xml.sax.ErrorHandler {
        public void error (SAXParseException e) throws SAXParseException {
            // normally a validity error (though we are not validating currently)
            throw e;
        }

        public void warning (SAXParseException e) throws SAXParseException {
            //showParseError(e);
            // but continue...
            throw e;
        }

        public void fatalError (SAXParseException e) throws SAXParseException {
            throw e;
        }
    }
    
    private static int ESTIMATED_TIME = 3500; // tenths of seconds
    public int getEstimatedTimeToInstall() {
        return ESTIMATED_TIME;
    }
    
    public void startProgress() {
        progressThread = new ProgressThread();
        progressThread.start();
    }
    
    public void stopProgress() {
        //Method startProgress() must be called first
        if (progressThread == null) {
            return;
        }
        logEvent(this, Log.DBG,"in progress stop");
        progressThread.interrupt();
        logEvent(this, Log.DBG,"interrupting ProgressThread ");
        //wait until progressThread is interrupted
        while (progressThread.isAlive()) {
            logEvent(this, Log.DBG,"Waiting for progressThread to die...");
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {}
        }
        logEvent(this, Log.DBG,"ProgressThread interrupted");
        progressThread.finish();
        progressThread = null;
        
        Thread.currentThread().yield();
        logEvent(this, Log.DBG,"active Threads -> " + Thread.currentThread().activeCount());
    }

    /** Keep info about work done. */
    static class ProgressInfo {
        /** Bytes already processed. */
        private long completedBytes = 0L;
        /** File name currently processed. */
        private String fileName;

        synchronized long getCompletedBytes () {
            return completedBytes;
        }

        synchronized void setCompletedBytes (long completedBytes) {
            this.completedBytes = completedBytes;
        }

        synchronized String getFileName () {
            return fileName;
        }

        synchronized void setFileName (String fileName) {
            this.fileName = fileName;
        }
    }
    
    /** Inner class to update the progress pane while installation */
    class ProgressThread extends Thread {
        private boolean loop = true;
        private  MutableOperationState mos;
        
        //progress bar related variables
        private long percentageCompleted = 0L;
        private long percentageStart = 0L;
        private long checksum = 0L;
        
        //status detail related variables
        //progress dots (...) after the path if it is being shown since s while
        private final int MIN_DOTS = 3;
        private int fileCounter = 0;
        private String lastPathShown;
        
        //status description related variables
        private File logFile;
        private boolean doStatusDescUpdate = true;
        
        //variables related to pkg unzipping before installation. Only for Solaris
        private boolean isUnzipping = false;
        private File unzipLog;
        private BufferedReader unzipLogReader = null;
        private long startTime = 0L;
        
        public ProgressThread() {
            this.mos = mutableOperationState;
            checksum = totalOriginalSize;
        }
        
        public void run() {
            long sleepTime = 500L;
            percentageStart = mos.getProgress().getPercentComplete();
            logEvent(this, Log.DBG,"Starting percentageStart: " + percentageStart);
            while (loop) {
                try {
                    updateProgressBar();
                    Thread.currentThread().sleep(sleepTime);
                    if (isCanceled()) {
                        return;
                    }
                } catch (InterruptedException ex) {
                    loop = false;
                    return;
                } catch (Exception ex) {
                    loop = false;
                    String trace = Util.getStackTrace(ex);
                    logEvent(this, Log.DBG, trace);
                    logEvent(this, Log.ERROR, trace);
                    return;
                }
            }
        }
        
        public void finish() {
            loop = false;
            Thread.currentThread().yield();
            mos.setStatusDetail("");
            logEvent(this, Log.DBG,"Finishing");;
            if (!mos.isCanceled()) {
                mos.setStatusDescription("");
                /*percentageCompleted = mos.getProgress().getPercentComplete();
                for (; percentageCompleted <= 100; percentageCompleted++) {
                    logEvent(this, Log.DBG,"percentageCompleted = " + percentageCompleted + " updateCounter " + mos.getUpdateCounter());
                    mos.updatePercentComplete(ESTIMATED_TIME, 1L, 100L);
                }*/
            } else {
                String statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installationCancelled)");
                mos.setStatusDescription(statusDesc);
                mos.getProgress().setPercentComplete(0);
            }
            
        }
        
        /** Check if the operation is canceled. If not yield to other threads. */
        private boolean isCanceled() {
            if (mos.isCanceled() && loop) {
                logEvent(this, Log.DBG,"MOS is cancelled");
                loop = false;
            } else {
                Thread.currentThread().yield();
            }
            
            return mos.isCanceled();
        }
        
        /** Updates the progress bar. */
        private void updateProgressBar() {
            if (isCanceled()) {
                return;
            }
            long size = progressInfo.getCompletedBytes();
            long perc = (size * (100 - percentageStart)) / checksum;
            logEvent(this, Log.DBG,"installed size = " + size + " perc = " + perc);
            if (perc <= percentageCompleted) {
                return;
            }
            long increment = perc - percentageCompleted;
            mos.updatePercentComplete(ESTIMATED_TIME, increment, 100L);
            mos.setStatusDetail(progressInfo.getFileName());
            percentageCompleted = perc;
        }
        
    }
    
}

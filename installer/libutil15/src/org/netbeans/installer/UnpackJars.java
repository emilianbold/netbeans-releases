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

import com.installshield.util.FileAttributes;
import com.installshield.util.Log;
import com.installshield.wizard.service.file.FileService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

public class UnpackJars {
        
    private String nbInstallDir = "";

    private List<JarItem> jarList = new ArrayList<JarItem>();
    
    /** These 2 values are read from UnpackJarsAction when ProgressThread
     *  is initialized. */
    long totalOriginalSize;
    long totalPackedSize;
    
    private ProgressInfo progressInfo;
    
    public UnpackJars () {
    }
    
    void init (String nbInstallDir, Log log) {
        this.nbInstallDir = nbInstallDir;
        log.logEvent(this,Log.DBG,"nbInstallDir: " + nbInstallDir);
        
        progressInfo = new ProgressInfo();
    }
    
    /** 
     * Unpack jars.
     * @return 0 if it is successful, non zero otherwise
     */
    int unpackJars (Log log, FileService fileService) {
        log.logEvent(this,Log.DBG,"unpackJars ENTER");

        FileAttributes attrs = new FileAttributes();
        attrs.setAttributes(FileAttributes.OWNER_READABLE | FileAttributes.OWNER_WRITEABLE |
        FileAttributes.GROUP_READABLE | FileAttributes.WORLD_READABLE);

        Pack200.Unpacker unpacker = Pack200.newUnpacker();
        long completedBytes = 0L;
        for (int i = 0; i < jarList.size(); i++) {
            JarItem item = jarList.get(i);
            File fileIn = new File(item.fileName + ".pack");
            File fileOut = new File(item.fileName);
            progressInfo.setFileName(fileOut.getPath());
            if (!fileIn.exists()) {
                log.logEvent(this,Log.ERROR,"Cannot find file: " + fileIn);
                return -400;
            }
            log.logEvent(this,Log.DBG,"Unpacking file: " + fileIn);
            try {
                JarOutputStream os = new JarOutputStream(new FileOutputStream(fileOut));
                unpacker.unpack(fileIn, os);
                os.close();
            } catch (IOException ex) {
                log.logEvent(this,Log.ERROR,"Unpacking failed at item[" + i + "]: " + item.toString());
                Util.logStackTrace(log,ex);
                return -400;
            }
            String md5 = generateKey(log,fileOut);
            if (!md5.equals(item.md5)) {
                log.logEvent(this,Log.ERROR,"Unpacking failed at item[" + i + "]: " + item.toString());
                log.logEvent(this,Log.ERROR,"MD5 checksum is not the same."
                + " Old: " + item.md5 + " New: " + md5);
                return -401;
            }
                    
            try {
                //Set for non Windows OS user+rw group+r other+r
                if (!Util.isWindowsOS() && (fileService != null)) {
                    fileService.setFileAttributes(fileOut.getAbsolutePath(),attrs);
                }
            } catch (Exception ex) {
                log.logEvent(this,Log.ERROR,"Error: Cannot set file attributes for: " + fileOut);
                log.logEvent(this,Log.ERROR,"Exception: " + ex.getMessage());
                Util.logStackTrace(log,ex);
            }
            fileOut.setLastModified(item.time);
            completedBytes += fileOut.length();
            progressInfo.setCompletedBytes(completedBytes);
            fileIn.delete();
        }
        return 0;
    }

    /** Delete jars. */
    void deleteJars (Log log) {
        log.logEvent(this,Log.DBG,"deleteJars ENTER");
        Pack200.Unpacker unpacker = Pack200.newUnpacker();
        for (int i = 0; i < jarList.size(); i++) {
            JarItem item = jarList.get(i);
            File file = new File(item.fileName);
            if (file.exists()) {
                log.logEvent(this,Log.DBG,"Deleting file: " + file);
                file.delete();
            } else {
                log.logEvent(this,Log.ERROR,"Cannot find file: " + file);
            }
        }
    }

    /** Parse jar catalog XML file. */
    boolean parseCatalog (Log log, String inputFileName) {
        log.logEvent(this,Log.DBG,"parseCatalog ENTER");
        //Replace spaces in URL string
        String s;
        if (Util.isWindowsOS()) {
            //On Windows path starts with disk like C: so we must add additional slash
            s = "file:///" + inputFileName.replaceAll(" ","%20");
        } else {
            s = "file://" + inputFileName.replaceAll(" ","%20");
        }
        log.logEvent(this, Log.DBG,"URL: " + s);
        URL url = null;
        try {
            url = new URL(s);
        } catch (MalformedURLException ex) {
            log.logEvent(this,Log.ERROR,"Cannot find jar catalog. URL:" + s);
            Util.logStackTrace(log,ex);
            return false;
        }
        Document doc = null;
        try {
            doc = parseDocument(url);
        } catch (IOException ex) {
            log.logEvent(this,Log.ERROR,"Cannot parse jar catalog. Broken XML file:" + url.toString());
            Util.logStackTrace(log,ex);
            return false;
        } catch (SAXException ex) {
            log.logEvent(this,Log.ERROR,"Cannot parse jar catalog. Broken XML file:" + url.toString());
            Util.logStackTrace(log,ex);
            return false;
        }
        if (doc.getDocumentElement() == null ) {
            log.logEvent(this,Log.ERROR,"Missing root element. Broken XML file:" + url.toString());
            return false;
        } else {
            NodeList nodeList;

            nodeList = doc.getElementsByTagName("summary");
            if (nodeList.getLength() == 0) {
                log.logEvent(this,Log.ERROR,"Missing summary in jar catalog.");
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
                log.logEvent(this,Log.ERROR,"Cannot parse value of total-original-size: " + s);
                Util.logStackTrace(log,ex);
            }

            attr = node.getAttributes().getNamedItem("total-packed-size");
            s = attr.getNodeValue();
            totalPackedSize = -1;
            try {
                totalPackedSize = Long.parseLong(s);
            } catch (NumberFormatException ex) {
                log.logEvent(this,Log.ERROR,"Cannot parse value of total-packed-size: " + s);
                Util.logStackTrace(log,ex);
            }

            nodeList = doc.getElementsByTagName("jar");
            for (int i = 0, ind = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                String fileName;
                long time, origSize, packedSize;
                String md5;

                attr = node.getAttributes().getNamedItem("name");
                fileName = nbInstallDir + File.separator + attr.getNodeValue();
                //System.out.println("attValue:" + attr.getNodeValue());

                attr = node.getAttributes().getNamedItem("time");
                s = attr.getNodeValue();
                time = -1;
                try {
                    time = Long.parseLong(s);
                } catch (NumberFormatException ex) {
                    log.logEvent(this,Log.ERROR,"Cannot parse value of time: " + s);
                    Util.logStackTrace(log,ex);
                }
                //System.out.println("attValue:" + attr.getNodeValue());

                attr = node.getAttributes().getNamedItem("original-size");
                s = attr.getNodeValue();
                origSize = -1;
                try {
                    origSize = Long.parseLong(s);
                } catch (NumberFormatException ex) {
                    log.logEvent(this,Log.ERROR,"Cannot parse value of original-size: " + s);
                    Util.logStackTrace(log,ex);
                }
                //System.out.println("attValue:" + attr.getNodeValue());

                attr = node.getAttributes().getNamedItem("packed-size");
                s = attr.getNodeValue();
                packedSize = -1;
                try {
                    packedSize = Long.parseLong(s);
                } catch (NumberFormatException ex) {
                    log.logEvent(this,Log.ERROR,"Cannot parse value of packed-size: " + s);
                    Util.logStackTrace(log,ex);
                }
                
                attr = node.getAttributes().getNamedItem("md5");
                md5 = attr.getNodeValue();

                JarItem item = new JarItem(fileName,time,origSize,packedSize,md5);
                jarList.add(item);
                //System.out.println("attValue:" + attr.getNodeValue());
            }
        }
        return true;
    }
    
    /** 
     * Generate 32 byte long fingerprint of input file in string readable form
     * the same as produced by md5sum.
     */
    private String generateKey (Log log, File file) {
        String key = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5"); // NOI18N
        } catch (NoSuchAlgorithmException ex) {
            log.logEvent(this,Log.ERROR,"Cannot produce MD5 checksum.");
            Util.logStackTrace(log,ex);
            return key;
        }

        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (IOException ex) {
            log.logEvent(this,Log.ERROR,"Cannot produce MD5 checksum.");
            Util.logStackTrace(log,ex);
            return key;
        }
        ByteBuffer buff = null;
        try {
            buff = is.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        } catch (IOException ex) {
            log.logEvent(this,Log.ERROR,"Cannot produce MD5 checksum.");
            Util.logStackTrace(log,ex);
            return key;
        }

        md.update(buff);
        byte [] md5sum = md.digest();
        StringBuffer keyBuff = new StringBuffer(32);
        //Convert byte array to hexadecimal string to be used as key
        for (int i = 0; i < md5sum.length; i++) {
            int val = md5sum[i];
            if (val < 0) {
                val = val + 256;
            }
            String s = Integer.toHexString(val);
            if (s.length() == 1) {
                keyBuff.append("0"); // NOI18N
            }
            keyBuff.append(Integer.toHexString(val));
        }
        key = keyBuff.toString();
        return key;
    }

    ProgressInfo getProgressInfo () {
        return progressInfo;
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
        String md5;

        public JarItem (String fileName, long time, long origSize, long packedSize, String md5) {
            this.fileName = fileName;
            this.time = time;
            this.origSize = origSize;
            this.packedSize = packedSize;
            this.md5 = md5;
        }

        public String toString() {
            return "File:" + fileName + " Time:" + time + " Original size:" + origSize
            + " Packed size:" + packedSize
            + " MD5:" + md5;
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

}

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

package org.netbeans.modules.mobility.cldcplatform.startup;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author David Kaspar
 */
public class DefaultEmulatorInstall extends ModuleInstall {
    static private final String EMU_MARKER="emu_installed.mark";
    static private final String DSC_MARKER="dsc_installed.mark";
    
    public void restored() {
        new LibraryConverter();
        installEmulators();
        installDescriptors();
    }

    //new way of WTK2.5.2 installation - the old way should be removed after WTK 2.5.2 binaries integration
    private void installDescriptors() {
        FileObject descRoot = Repository.getDefault().getDefaultFileSystem().findResource("emulator-descriptor-inst"); //NOI18N
        FileObject platformsFolder;
        try {
            platformsFolder = FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "Services/Platforms/org-netbeans-api-java-Platform"); // NOI18N
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return;
        }
        byte[] data = new byte[256];
        if (descRoot != null) for (FileObject desc : descRoot.getChildren()) {
            InputStream is = null;
            OutputStreamWriter os = null;
            FileLock fl = null;
            File installRoot = FileUtil.toFile(desc);
            if (installRoot.isFile() && (installRoot = installRoot.getParentFile()) != null && (installRoot = installRoot.getParentFile()) != null && (installRoot = installRoot.getParentFile()) != null && desc.getExt().equals("xml")) {
                if (platformsFolder.getFileObject(desc.getNameExt()) != null) {
                    ErrorManager.getDefault().log("Emulator description file already installed: " + installRoot.getAbsolutePath()); // NOI18N
                } else try {    
                    is = desc.getInputStream();
                    StringBuffer sb = new StringBuffer(1024);
                    int len;
                    while ((len = is.read(data)) != -1) sb.append(new String(data, 0, len, "UTF-8")); //NOI18N
                    FileObject fo = platformsFolder.createData(desc.getNameExt());
                    fl = fo.lock();
                    os = new OutputStreamWriter(fo.getOutputStream(fl), "UTF8"); //NOI18N
                    os.write(MessageFormat.format(sb.toString(), new Object[]{XMLUtil.toAttributeValue(installRoot.getAbsolutePath())}));
                    desc.delete();
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                } finally {
                    if (is != null) try {is.close();} catch (IOException ioe) {}
                    if (os != null) try {os.close();} catch (IOException ioe) {}
                    if (fl != null && fl.isValid()) fl.releaseLock();
                }
            }
        }
    }
    
    public void installEmulators() {
        final File[] emulators = getAvailableEmulators();
        if (emulators == null  ||  emulators.length <= 0) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "No emulator available"); // NOI18N
            return;
        }
        for (int i = 0; i < emulators.length; i++) {
            final File emulator = emulators[i];
            final File target = getTargetFolder(emulator);
            final String root = getEmulatorRootInPack(emulator);
            if (root == null || target == null)
                continue;
            
            final File rootFolder = new File(target, root);
            
            /* If this file exist, installation of emulator was correct */
            if (new File(rootFolder,EMU_MARKER).exists()) {
                checkJavaPath(rootFolder);
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Emulator already installed: " + emulator.getAbsolutePath()); // NOI18N
                
                /* We need to check if description was previously installed
                    to avoid recreeating of platform */
                if (!new File(rootFolder,DSC_MARKER).exists()) {
                    String path;
                    try {
                        path = rootFolder.getCanonicalPath();
                    } catch (IOException e) {
                        path = rootFolder.getAbsolutePath();
                    }
                    if (!installDescriptionFile(emulator, path)) {
                        ErrorManager.getDefault().log("Emulator description file already installed: " + emulator.getAbsolutePath()); // NOI18N
                    }
                }
                continue;
            }
            installEmulator(emulator, target, root);
        }
    }
    
    private File[] getAvailableEmulators() {
        final ArrayList<File> ret = new ArrayList<File>();
        final InstalledFileLocator instance = InstalledFileLocator.getDefault();
        final File folder = instance.locate("emulators-inst", "org.netbeans.modules.kjava.emulators", false); // NOI18N
        if (folder == null  ||  ! folder.exists()  ||  ! folder.isDirectory())
            return new File[0];
        final File[] files = folder.listFiles();
        if (files != null) for (int j = 0; j < files.length; j++) {
            final File file = files[j];
            if (! file.exists()  ||  ! file.isFile() || isAlreadyInstalled(file))
                continue;
            final String name = file.getName().toLowerCase();
            if (name.endsWith(".zip")  ||  name.endsWith(".jar")) // NOI18N
                ret.add(file);
        }
        return ret.toArray(new File[ret.size()]);
    }
    
    /** Here we check if installed description is the same as one we want to install
     *  Comaprision is done in 2 steps - at first we compare name of the platform name and at second installation path
     *  if paths differs but name is the same we don't want to reinstall platfrom (return value true)
     *  if both paths and name are the same we may reinstall platform if previous installation fails (false)
     *  if name wasn't found in instaslled platforms names installation can proceed (false)
     */
    private boolean isAlreadyInstalled(final File emulatorPack) {
        final String desc[] = getDescriptionFile(emulatorPack);
        if (desc == null  ||  desc.length != 2)
            return false;
        
        final FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource("Services/Platforms/org-netbeans-api-java-Platform"); // NOI18N
        final FileObject df=platformsFolder.getFileObject(desc[0]);
        
        if (df!=null) {
            final File target = getTargetFolder(emulatorPack);
            final String root = getEmulatorRootInPack(emulatorPack);
            final File rootFolder = new File(target, root);
            Document doc=null;
            try {
                /* Parse the descripto xml to find the name of emulator */
                doc = XMLUtil.parse(new InputSource(df.getInputStream()), false, false, null, new EntityResolver() {
					public InputSource resolveEntity(@SuppressWarnings("unused") String publicId, @SuppressWarnings("unused") String systemId) {
                        return new InputSource(new ByteArrayInputStream(new byte[0]));
                    }
                });
            } catch (Exception ex) {return false;}
            
            final Attr st=doc.getDocumentElement().getAttributeNode("displayname");
            if (st==null) return false;
            final String displayName=st.getValue();
            
            final JavaPlatform pl[] = JavaPlatformManager.getDefault().getInstalledPlatforms();
            for ( JavaPlatform pli : pl ) {
                final String name=pli.getDisplayName();
                if (name.equals(displayName)) {
                	for ( FileObject obj : (Collection<FileObject>) pli.getInstallFolders() ) {
                        // Emulator is considered as installed when descriptor was found and installation directory is different from target directory
                        if (!rootFolder.getAbsolutePath().equals(FileUtil.toFile(obj).getAbsolutePath()))
                            return true;
                    }
                }
            }
        }
        return false;
    }
    
    private String archiveName2FolderName(final File emulatorPack) {
        if (emulatorPack != null) {
            String name = emulatorPack.getName().toLowerCase();
            if (name.endsWith(".zip")) // NOI18N
                name = name.substring(0, name.length() - ".zip".length()); // NOI18N
            else if (name.endsWith(".jar")) // NOI18N
                name = name.substring(0, name.length() - ".jar".length()); // NOI18N
            return name;
        }
        return "unknown"; // NOI18N
    }
    
    private File getTargetFolder(final File emulatorPack) {
        if (emulatorPack == null)
            return null;
        final FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        if (root == null)
            return null;
        File rootFolder = FileUtil.toFile(root);
        if (rootFolder == null)
            return null;
        rootFolder = rootFolder.getParentFile();
        if (rootFolder == null)
            return null;
        return new File(rootFolder, "emulators" + File.separator + archiveName2FolderName(emulatorPack)); // NOI18N
    }
    
    private String getEmulatorRootInPack(final File emulatorPack) {
        if (emulatorPack == null)
            return null;
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(emulatorPack)));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (! entry.isDirectory()) {
                    name = name.replace('\\', '/');
                    final int i = name.toLowerCase().indexOf("bin/emulator"); // NOI18N
                    if (i >= 0)
                        return name.substring(0, i).replace('/', File.separatorChar); // NOI18N
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "IOException while detecting emulator pack: " + emulatorPack.getAbsolutePath()); // NOI18N
        } finally {
            if (zis != null) {
                try { zis.closeEntry(); } catch (IOException e) {}
                try { zis.close(); } catch (IOException e) {}
            }
        }
        return null;
    }
    
    private void installEmulator(final File emulatorPack, final File targetFolder, final String root) {
        final File rootFolder = new File(targetFolder, root);
        String path;
        try {
            path = rootFolder.getCanonicalPath();
        } catch (IOException e) {
            path = rootFolder.getAbsolutePath();
        }
        
        rootFolder.mkdirs();
        if (!installDescriptionFile(emulatorPack, path)) {
            ErrorManager.getDefault().log("Emulator description file already installed: " + emulatorPack.getAbsolutePath()); // NOI18N
        }
        
        final RequestProcessor rp = new RequestProcessor();
        rp.post(new Runnable() {
            public void run() {
                if (! unpackEmulator(emulatorPack, targetFolder)) {
                    ErrorManager.getDefault().log("Failed to unpack emulator: " + emulatorPack.getAbsolutePath()); // NOI18N
                    return;
                }
                ErrorManager.getDefault().log("Emulator unpacked: " + emulatorPack.getAbsolutePath()); // NOI18N
                
                //Create a marker for finished installation
                try {
                    new File(rootFolder,EMU_MARKER).createNewFile();
                } catch (IOException ex) {}
                
                //Create a marker for post install task
                checkJavaPath(rootFolder);
                rp.stop();
            }
        }, 1000);
    }
    
    private boolean installDescriptionFile(final File emulatorPack, String rootPath) {
        final String desc[] = getDescriptionFile(emulatorPack);
        if (desc == null  ||  desc.length != 2)
            return false;
        final FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource("Services/Platforms/org-netbeans-api-java-Platform"); // NOI18N
        if (platformsFolder.getFileObject(desc[0]) != null) {
            ErrorManager.getDefault().log("Emulator description file already installed: " + emulatorPack.getAbsolutePath()); // NOI18N
            return false;
        }
        try {
            rootPath = XMLUtil.toAttributeValue(rootPath);
        } catch (CharConversionException cce) {
            ErrorManager.getDefault().notify(cce);
        }
        final String s = MessageFormat.format(desc[1], new Object[]{ rootPath });
        
        OutputStreamWriter bos = null;
        FileLock fl = null;
        try {
            final FileObject fo = platformsFolder.createData(desc[0]);
            fl = fo.lock();
            
            bos = new OutputStreamWriter(fo.getOutputStream(fl), "UTF8"); //NOI18N
            bos.write(s);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try { if (bos != null) bos.close(); } catch (IOException e) {}
            if (fl != null) fl.releaseLock();
        }
        //Create a marker for description installation
        try {
            new File(rootPath,DSC_MARKER).createNewFile();
        } catch (IOException ex) {}
        return true;
    }
    
    private String[] getDescriptionFile(final File emulatorPack) {
        if (emulatorPack == null)
            return null;
        ZipInputStream zis = null;
        byte[] data = new byte[256];
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(emulatorPack)));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null){
                final String name = entry.getName();
                if (! entry.isDirectory()) {
                    if (name.indexOf('/') == -1  &&  name.indexOf('\\') == -1  &&  name.toLowerCase().endsWith(".xml")) { // NOI18N
                        final StringBuffer sb = new StringBuffer(1024);
                        int len;
                        while ((len = zis.read(data)) != -1)
                            sb.append(new String(data, 0, len));
                        return new String[] { name, sb.toString() };
                    }
                }
                zis.closeEntry();
            }
            zis.close();
        } catch (IOException e) {
        } finally {
            data = null;
            if (zis != null) {
                try { zis.closeEntry(); } catch (IOException e) {}
                try { zis.close(); } catch (IOException e) {}
            }
        }
        return null;
    }
    
    protected boolean unpackEmulator(final File emulatorPack, final File targetFolder) {
        if (emulatorPack == null)
            return false;
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(emulatorPack)));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                final String name = entry.getName();
                if (entry.isDirectory()) {
                    final File dir = new File(targetFolder, name);
                    dir.mkdirs();
                } else {
                    if (name.indexOf('/') == -1  &&  name.indexOf('\\') == -1  &&  name.toLowerCase().endsWith(".xml")) { // NOI18N
                        zis.closeEntry();
                        continue;
                    }
                    
                    final File file = new File(targetFolder, name);
                    if (file.createNewFile()) {
                        BufferedOutputStream bos = null;
                        byte[] data = new byte[256];
                        try {
                            bos = new BufferedOutputStream(new FileOutputStream(file));
                            int len;
                            while ((len = zis.read(data)) != -1)
                                bos.write(data, 0, len);
                        } finally {
                            data = null;
                            if (bos != null) try { bos.close(); } catch (IOException e) {}
                        }
                    }
                }
                zis.closeEntry();
            }
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (zis != null) {
                try { zis.closeEntry(); } catch (IOException e) {}
                try { zis.close(); } catch (IOException e) {}
            }
        }
    }
    
    protected void checkJavaPath(final File file) {
        final String java = System.getProperty("java.home"); //NOI18N
        final File marker = new File( file, "java.mark" ); //NOI18N
        if (marker != null && marker.exists()){
            BufferedReader br = null;
            try{
                br = new BufferedReader(new FileReader(marker));
                final String javaVersion = br.readLine();
                if (javaVersion != null && javaVersion.equals(java)){
                    return;
                }
            } catch(IOException ioex){
            } finally {
                if (br != null){
                    try {
                        br.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        
        String path;
        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            path = file.getAbsolutePath();
        }
        final String rootPath = path;
        
        final RequestProcessor rp = new RequestProcessor();
        rp.post(new Runnable() {
            public void run() {
                try {
                    PostInstallJ2meAction.installAction(rootPath);
                    if (marker != null){
                        BufferedWriter bw = null;
                        try{
                            bw = new BufferedWriter(new FileWriter(marker));
                            bw.write(java);
                        } catch(IOException ioex){
                        } finally {
                            if (bw != null){
                                try {
                                    bw.close();
                                } catch (IOException ex) {
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                rp.stop();
            }
        }, 1000);
    }
}

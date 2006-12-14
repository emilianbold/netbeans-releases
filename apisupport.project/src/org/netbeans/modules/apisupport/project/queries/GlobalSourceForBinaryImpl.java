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

package org.netbeans.modules.apisupport.project.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.TestEntry;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Able to find sources in the NetBeans sources zip.
 *
 * @author Martin Krauskopf
 */
public final class GlobalSourceForBinaryImpl implements SourceForBinaryQueryImplementation {
    
    /** for use from unit tests */
    static boolean quiet = false;
    
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        try {
            { // #68685 hack - associate reasonable sources with XTest's versions of various test libs
                String binaryRootS = binaryRoot.toExternalForm();
                URL result = null;
                if (binaryRootS.startsWith("jar:file:")) { // NOI18N
                    if (binaryRootS.endsWith("/xtest/lib/nbjunit.jar!/")) { // NOI18N
                        result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/xtest/lib/nbjunit.jar!/".length()) + "/xtest/nbjunit/src/"); // NOI18N
                    } else if (binaryRootS.endsWith("/xtest/lib/nbjunit-ide.jar!/")) { // NOI18N
                        result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/xtest/lib/nbjunit-ide.jar!/".length()) + "/xtest/nbjunit/ide/src/"); // NOI18N
                    } else if (binaryRootS.endsWith("/xtest/lib/insanelib.jar!/")) { // NOI18N
                        result = new URL(binaryRootS.substring("jar:".length(), binaryRootS.length() - "/xtest/lib/insanelib.jar!/".length()) + "/performance/insanelib/src/"); // NOI18N
                    } else {
                        // tests.jar in test distribution 
                        TestEntry testJar = TestEntry.get(archiveURLToFile(binaryRoot));
                        if (testJar != null) {
                           result = testJar.getSrcDir();
                        }
                    }
                    final FileObject resultFO = result != null ? URLMapper.findFileObject(result) : null;
                    if (resultFO != null) {
                        return new SourceForBinaryQuery.Result() {
                            public FileObject[] getRoots() {
                                return new FileObject[] {resultFO};
                            }
                            public void addChangeListener(ChangeListener l) {}
                            public void removeChangeListener(ChangeListener l) {}
                        };
                    }
                }
            }
            NbPlatform supposedPlaf = null;
            for (Iterator it = NbPlatform.getPlatforms().iterator(); it.hasNext(); ) {
                NbPlatform plaf = (NbPlatform) it.next();
                // XXX more robust condition?
                if (binaryRoot.toExternalForm().indexOf(plaf.getDestDir().toURI().toURL().toExternalForm()) != -1) {
                    supposedPlaf = plaf;
                    break;
                }
            }
            if (supposedPlaf == null) {
                return null;
            }
            if (!binaryRoot.getProtocol().equals("jar")) { // NOI18N
                Util.err.log(binaryRoot + " is not an archive file."); // NOI18N
                return null;
            }
            File binaryRootF = archiveURLToFile(binaryRoot);
            FileObject fo = FileUtil.toFileObject(binaryRootF);
            if (fo == null) {
                Util.err.log("Cannot found FileObject for " + binaryRootF + "(" + binaryRoot + ")"); // NOI18N
                return null;
            }
  //          if (testCnb != null && supposedPlaf != null) {
                // test
   //             supposedPlaf.
   //         }
            return new NbPlatformResult(supposedPlaf, binaryRoot, fo.getName().replace('-', '.'));
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private static final class NbPlatformResult implements
            SourceForBinaryQuery.Result, PropertyChangeListener {
        
        private final List<ChangeListener> listeners = new ArrayList();
        private final NbPlatform platform;
        private final URL binaryRoot;
        private final String cnb;
        
        private boolean alreadyListening;
        
        NbPlatformResult(final NbPlatform platform, final URL binaryRoot, final String cnb) {
            this.platform = platform;
            this.binaryRoot = binaryRoot;
            this.cnb = cnb;
//            this.testType = testType;
//            this.testCluster = testCluster;
        }
        
        public FileObject[] getRoots() {
            final List<FileObject> candidates = new ArrayList();
            URL[] roots = platform.getSourceRoots();
            try {
                for (int i = 0; i < roots.length; i++) {
                    if (roots[i].getProtocol().equals("jar")) { // NOI18N
                        // suppose zipped sources
                        File nbSrcF = archiveURLToFile(roots[i]);
                        if (!nbSrcF.exists()) {
                            continue;
                        }
                        NetBeansSourcesParser nbsp;
                        try {
                            nbsp = NetBeansSourcesParser.getInstance(nbSrcF);
                        } catch (ZipException e) {
                            if (!quiet) {
                                Util.err.annotate(e, ErrorManager.UNKNOWN, nbSrcF + " does not seem to be a valid ZIP file.", null, null, null); // NOI18N
                                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                            }
                            continue;
                        }
                        if (nbsp == null) {
                            continue;
                        }
                        String pathInZip = nbsp.findSourceRoot(cnb);
                        if (pathInZip == null) {
                            continue;
                        }
                        URL u = new URL(roots[i], pathInZip);
                        FileObject entryFO = URLMapper.findFileObject(u);
                        if (entryFO != null) {
                            candidates.add(entryFO);
                        }
                    } else {
                        // Does not resolve nbjunit and similar from ZIPped
                        // sources. Not a big issue since the default distributed
                        // sources do not contain them anyway.
                        String relPath = resolveSpecialNBSrcPath(binaryRoot);
                        if (relPath == null) {
                            continue;
                        }
                        URL url = new URL(roots[i], relPath);
                        FileObject dir = URLMapper.findFileObject(url);
                        if (dir != null) {
                            candidates.add(dir);
                        } // others dirs are currently resolved by o.n.m.apisupport.project.queries.SourceForBinaryImpl
                    }
                }
            } catch (IOException ex) {
                throw new AssertionError(ex);
            }
            return (FileObject[]) candidates.toArray(new FileObject[candidates.size()]);
        }
        
        public void addChangeListener(ChangeListener l) {
            // start listening on NbPlatform
            synchronized (listeners) {
                listeners.add(l);
            }
            if (!alreadyListening) {
                platform.addPropertyChangeListener(this);
                alreadyListening = true;
            }
        }

        public void removeChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
            if (listeners.isEmpty()) {
                platform.removePropertyChangeListener(this);
                alreadyListening = false;
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == NbPlatform.PROP_SOURCE_ROOTS) {
                Iterator it;
                synchronized (listeners) {
                    if (listeners.isEmpty()) {
                        return;
                    }
                    it = new HashSet(listeners).iterator();
                }
                ChangeEvent ev = new ChangeEvent(this);
                while (it.hasNext()) {
                    ((ChangeListener) it.next()).stateChanged(ev);
                }
            }
        }
        
    }
    
    private static String resolveSpecialNBSrcPath(URL binaryRoot) throws MalformedURLException {
        String binaryRootS = binaryRoot.toExternalForm();
        String result = null;
        if (binaryRootS.startsWith("jar:file:")) { // NOI18N
            if (binaryRootS.endsWith("/modules/org-netbeans-modules-nbjunit.jar!/")) { // NOI18N
                result = "xtest/nbjunit/src/"; // NOI18N
            } else if (binaryRootS.endsWith("/modules/org-netbeans-modules-nbjunit-ide.jar!/")) { // NOI18N
                result = "xtest/nbjunit/ide/src/"; // NOI18N
            } else if (binaryRootS.endsWith("/modules/ext/insanelib.jar!/")) { // NOI18N
                result = "performance/insanelib/src/"; // NOI18N
            } else {
                result = null;
            }
        }
        return result;
    }

    private static File archiveURLToFile(final URL archiveURL) {
        return new File(URI.create(FileUtil.getArchiveFile(archiveURL).toExternalForm()));
    }
    
    public static final class NetBeansSourcesParser {
        
        /** Zip file to instance map. */
        private static final Map<File, NetBeansSourcesParser> instances = new HashMap();
        
        private static final String NBBUILD_ENTRY = "nbbuild/"; // NOI18N
        
        private Map<String,String> cnbToPrjDir;
        private final ZipFile nbSrcZip;
        private final String zipNBCVSRoot;
        
        /**
         * May return <code>null</code> if the given zip is not a valid
         * NetBeans sources zip.
         */
        public static NetBeansSourcesParser getInstance(File nbSrcZip) throws ZipException, IOException {
            NetBeansSourcesParser nbsp = (NetBeansSourcesParser) instances.get(nbSrcZip);
            if (nbsp == null) {
                ZipFile nbSrcZipFile = new ZipFile(nbSrcZip);
                String zipNBCVSRoot = NetBeansSourcesParser.findNBCVSRoot(nbSrcZipFile);
                if (zipNBCVSRoot != null) {
                    nbsp = new NetBeansSourcesParser(nbSrcZipFile, zipNBCVSRoot);
                    instances.put(nbSrcZip, nbsp);
                }
            }
            return nbsp;
        }
        
        NetBeansSourcesParser(ZipFile nbSrcZip, String zipNBCVSRoot) {
            this.nbSrcZip = nbSrcZip;
            this.zipNBCVSRoot = zipNBCVSRoot;
        }
        
        String findSourceRoot(final String cnb) {
            if (cnbToPrjDir == null) {
                try {
                    doScanZippedNetBeansOrgSources();
                } catch (IOException ex) {
                    Util.err.notify(ErrorManager.WARNING, ex);
                }
            }
            return (String) cnbToPrjDir.get(cnb);
        }
        
        private static String findNBCVSRoot(final ZipFile nbSrcZip) {
            String nbRoot = null;
            for (Enumeration<? extends ZipEntry> en = nbSrcZip.entries(); en.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) en.nextElement();
                if (!entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (!name.equals(NBBUILD_ENTRY) &&
                        !(name.endsWith(NBBUILD_ENTRY) && name.substring(name.indexOf('/') + 1).equals(NBBUILD_ENTRY))) {
                    continue;
                }
                ZipEntry xmlEntry = nbSrcZip.getEntry(name + "nbproject/project.xml"); // NOI18N
                if (xmlEntry != null) {
                    nbRoot = name.substring(0, name.length() - NBBUILD_ENTRY.length());
                    break;
                }
            }
            return nbRoot;
        }
        
        private void doScanZippedNetBeansOrgSources() throws IOException {
            cnbToPrjDir = new HashMap();
            for (Enumeration<? extends ZipEntry> en = nbSrcZip.entries(); en.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) en.nextElement();
                if (!entry.isDirectory()) {
                    continue;
                }
                String path = entry.getName().substring(0, entry.getName().length() - 1); // remove last slash
                if (this.zipNBCVSRoot != null && (!path.startsWith(this.zipNBCVSRoot) || path.equals(this.zipNBCVSRoot))) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(path, "/"); // NOI18N
                if (st.countTokens() > ModuleList.DEPTH_NB_ALL) {
                    continue;
                }
                String name = path.substring(path.lastIndexOf('/') + 1, path.length());
                if (ModuleList.EXCLUDED_DIR_NAMES.contains(name)) {
                    // #61579: known to not be project dirs, so skip to save time.
                    continue;
                }
                // XXX should read src.dir from properties
                ZipEntry src = nbSrcZip.getEntry(entry.getName() + "src/"); // NOI18N
                if (src == null || !src.isDirectory()) {
                    continue;
                }
                
                ZipEntry projectXML = nbSrcZip.getEntry(entry.getName() + "nbproject/project.xml"); // NOI18N
                if (projectXML == null) {
                    continue;
                }
                String cnb = parseCNB(projectXML);
                if (cnb != null) {
                    cnbToPrjDir.put(cnb, entry.getName() + "src/"); // NOI18N
                }
            }
        }
        
        private String parseCNB(final ZipEntry projectXML) throws IOException {
            Document doc;
            InputStream is = nbSrcZip.getInputStream(projectXML);
            try {
                doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
            } catch (SAXException e) {
                throw (IOException) new IOException(projectXML + ": " + e.toString()).initCause(e); // NOI18N
            } finally {
                is.close();
            }
            Element docel = doc.getDocumentElement();
            Element type = Util.findElement(docel, "type", "http://www.netbeans.org/ns/project/1"); // NOI18N
            String cnb = null;
            if (Util.findText(type).equals("org.netbeans.modules.apisupport.project")) { // NOI18N
                Element cfg = Util.findElement(docel, "configuration", "http://www.netbeans.org/ns/project/1"); // NOI18N
                Element data = Util.findElement(cfg, "data", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                if (data != null) {
                    cnb = Util.findText(Util.findElement(data, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED)); // NOI18N
                }
            }
            return cnb;
        }
        
    }
    
}

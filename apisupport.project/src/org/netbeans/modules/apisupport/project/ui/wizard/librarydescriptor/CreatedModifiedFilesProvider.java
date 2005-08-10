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

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JTextArea;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;


/**
 *
 * @author Radek Matous
 */
final class CreatedModifiedFilesProvider  {
    private static final String VOLUME_CLASS = "classpath";//NOI18N
    private static final String VOLUME_SRC = "src";//NOI18N
    private static final String VOLUME_JAVADOC = "javadoc";//NOI18N
    
    private static final String LIBRARY_LAYER_ENTRY = "org-netbeans-api-project-libraries/Libraries";//NOI18N
    
    
    static CreatedModifiedFiles createInstance(NewLibraryDescriptor.DataModel data)  {
        
        CreatedModifiedFiles retval = new CreatedModifiedFiles(data.getProject());
        addOperations(retval, data);
        
        return retval;
    }
    
    public static void setCreatedFiles(CreatedModifiedFiles fileManipulator, JTextArea component) {
        setCreatedModifiedFiles(fileManipulator, component, false);
    }
    
    public  static void setModifiedFiles(CreatedModifiedFiles fileManipulator, JTextArea component) {
        setCreatedModifiedFiles(fileManipulator, component, true);
    }
    
    private static void addOperations(CreatedModifiedFiles fileSupport, NewLibraryDescriptor.DataModel data)  {
        String packagePath;
        String libraryDescPath;
        String libraryDescRelativePath;
        URL template;
        CreatedModifiedFiles.Operation libDescrOperation;
        Map tokens;
        
        packagePath = getPackageRelativePath(data.getProject(), data.getPackageName());
        libraryDescPath = getLibraryDescriptor(data.getLibraryName()) ;
        template = CreatedModifiedFilesProvider.class.getResource("libdescriptemplate.xml");//NOI18N
        tokens = getTokens(fileSupport, data.getProject(), data);
        String layerEntry = getLibraryDescriptorEntryPath(data.getLibraryName());
        
        
        libraryDescRelativePath = getLibraryDescriptorRelativePath(packagePath,data.getLibraryName());
        String layerPath = getLayerRelativePath(data.getProject());
        File prjFile = FileUtil.toFile(data.getProject().getProjectDirectory());
        File layerFolder = new File(prjFile,layerPath).getParentFile();
        File libraryDescFile = new File(prjFile,libraryDescRelativePath);
        
        libraryDescPath = PropertyUtils.relativizeFile(layerFolder, libraryDescFile);

        libDescrOperation = fileSupport.createLayerEntry(layerEntry,libraryDescPath,
                template,libraryDescRelativePath ,tokens,null/*data.getLibraryDisplayName()*/, null);
        
        
        fileSupport.add(libDescrOperation);
        libDescrOperation = fileSupport.bundleKeyDefaultBundle(data.getLibraryName(), data.getLibraryDisplayName());
        fileSupport.add(libDescrOperation);
    }
    
    
    
    private static String getLayerRelativePath(NbModuleProject project) {
        ManifestManager mm = ManifestManager.getInstance(project.getManifest(), false);
        StringBuffer sb = new StringBuffer();
        
        sb.append(project.getSourceDirectoryPath()).append("/").append(mm.getLayer());//NOI18N
        
        return sb.toString(); // NOI18N;
    }
    
    
    private static String getPackagePlusBundle(NbModuleProject project) {
        StringBuffer sb = new StringBuffer();
        
        ManifestManager mm = ManifestManager.getInstance(project.getManifest(), false);
        
        String bundle = mm.getLocalizingBundle().replace('/', '.');
        if (bundle.endsWith(".properties")) { // NOI18N
            bundle = bundle.substring(0, bundle.length() - 11);
        }
        
        sb.append(bundle);
        return sb.toString();//NOI18N
    }
    
    private static String getBundleRelativePath(NbModuleProject project) {
        StringBuffer sb = new StringBuffer();
        
        ManifestManager mm = ManifestManager.getInstance(project.getManifest(), false);
        sb.append(project.getSourceDirectoryPath()).append("/").append(mm.getLocalizingBundle());//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    
    private static String getPackageRelativePath(NbModuleProject project, String fullyQualifiedPackageName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(project.getSourceDirectoryPath()).append("/").append(fullyQualifiedPackageName);//NOI18N
        
        return sb.toString().replace('.','/');//NOI18N
    }
    
    private static String getLibraryDescriptor(String libraryName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(libraryName).append(".xml");//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    private static String getLibraryDescriptorRelativePath(String packageRelativePath, String libraryName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(packageRelativePath).append("/").append(libraryName).append(".xml");//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    private static String getLibraryDescriptorEntryPath(String libraryName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(LIBRARY_LAYER_ENTRY).append("/").append(libraryName).append(".xml");//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    
    private static String transformURL(final URL url, final String pathPrefix, final String archiveName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append("jar:nbinst:///").append(pathPrefix).append(archiveName).append("!/");//NOI18N
        
        return sb.toString();
    }
    
    private static Map getTokens(CreatedModifiedFiles fileSupport, NbModuleProject project, NewLibraryDescriptor.DataModel data) {
        Map retval = new HashMap();
        Library library = data.getLibrary();
        retval.put("name_to_substitute",data.getLibraryName());//NOI18N
        retval.put("bundle_to_substitute",getPackagePlusBundle(project).replace('/','.'));//NOI18N
        
        Iterator it = library.getContent(VOLUME_CLASS).iterator();
        retval.put("classpath_to_substitute",getTokenSubstitution(it, fileSupport, project, "libs/"));//NOI18N
        
        it = library.getContent(VOLUME_SRC).iterator();
        retval.put("src_to_substitute",getTokenSubstitution(it, fileSupport, project, "sources/"));//NOI18N
        
        it = library.getContent(VOLUME_JAVADOC).iterator();
        retval.put("javadoc_to_substitute",getTokenSubstitution(it, fileSupport, project, "docs/"));//NOI18N
        
        return retval;
    }
    
    private static String getTokenSubstitution(Iterator it, CreatedModifiedFiles fileSupport,
            final NbModuleProject project, String pathPrefix) {
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            URL originalURL = (URL)it.next();
            String archiveName;
            archiveName = addArchiveToCopy(fileSupport, project, originalURL, "release/"+pathPrefix);//NOI18N
            if (archiveName != null) {
                String urlToString = transformURL(originalURL, pathPrefix, archiveName);//NOI18N
                sb.append("<resource>");//NOI18N
                sb.append(urlToString);
                if (it.hasNext()) {
                    sb.append("</resource>\n");//NOI18N
                } else {
                    sb.append("</resource>");//NOI18N
                }
            }
        }
        return sb.toString();
    }
    
    /** returns archive name or temporarily null cause there is no zip support for file protocol  */
    private static String addArchiveToCopy(CreatedModifiedFiles fileSupport,  final NbModuleProject project,
            URL originalURL, String pathPrefix) {
        
        String retval = null;
        
        URL archivURL = FileUtil.getArchiveFile(originalURL);
        if (archivURL != null && FileUtil.isArchiveFile(archivURL)) {
            FileObject archiv = URLMapper.findFileObject(archivURL);
            assert archiv != null;
            retval = archiv.getNameExt();
            StringBuffer sb = new StringBuffer();
            sb.append(pathPrefix).append(retval);
            fileSupport.add(fileSupport.createFile(sb.toString(),archivURL));
        } else {
            //TODO: probably add new Operation that will zip files first or add there
            // posibility to use instead of URL InputStream and use lazy ByteArrayInputStream.
        }
        return retval;
    }
    
    private  static void setCreatedModifiedFiles(CreatedModifiedFiles fileManipulator,
            JTextArea component, boolean modified) {
        
        String textToSet = generateText(fileManipulator, modified);
        if (textToSet.length() > 0) {
            component.setText(generateText(fileManipulator, modified));
        }
    }
    private static String generateText(CreatedModifiedFiles fileManipulator, boolean modified) {
        StringBuffer sb = new StringBuffer();
        String[] relPaths = (modified) ? fileManipulator.getModifiedPaths() :
            fileManipulator.getCreatedPaths();
        
        if (relPaths.length > 0) {
            for (int i = 0; i < relPaths.length; i++) {
                if (i > 0) {
                    sb.append("\n");//NOI18N
                }
                sb.append(relPaths[i]);
            }
        }
        
        return sb.toString();
    }
    
}

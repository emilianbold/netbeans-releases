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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
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
        
    static void addOperations(CreatedModifiedFiles fileSupport, NewLibraryDescriptor.DataModel data)  {
        String packagePath;
        packagePath = getPackageRelativePath(data.getProject(), data.getPackageName());
        
        String libraryDescPath;
        libraryDescPath = getLibraryDescriptor(data.getLibraryName()) ;
        
        URL template = CreatedModifiedFilesProvider.class.getResource("libdescriptemplate.xml");//NOI18N
        
        CreatedModifiedFiles.Operation libDescrOperation;
        
        Map tokens = getTokens(fileSupport, data.getProject(), data.getLibrary());
        String layerEntry = getLibraryDescriptorEntryPath(data.getLibraryName());
        libDescrOperation = fileSupport.createLayerEntry(layerEntry,libraryDescPath,template,data.getLibraryDisplayName(),tokens);
        
        fileSupport.add(libDescrOperation);        
    }
    
    static String getBundleRelativePath(NbModuleProject project) {
        StringBuffer sb = new StringBuffer();
        
        ManifestManager mm = ManifestManager.getInstance(project.getManifest(), false);
        sb.append(project.getSourceDirectoryPath()).append("/").append(mm.getLocalizingBundle());//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    
    static String getPackageRelativePath(NbModuleProject project, String fullyQualifiedPackageName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(project.getSourceDirectoryPath()).append("/").append(fullyQualifiedPackageName);//NOI18N
        
        return sb.toString().replace('.','/');//NOI18N
    }

    private static String getLibraryDescriptor(String libraryName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(libraryName).append(".xml");//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    /*private static String getLibraryDescriptorRelativePath(String packageRelativePath, String libraryName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(packageRelativePath).append("/").append(libraryName).append(".xml");//NOI18N
        
        return sb.toString();//NOI18N
    }*/
    
    private static String getLibraryDescriptorEntryPath(String libraryName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(LIBRARY_LAYER_ENTRY).append("/").append(libraryName).append(".xml");//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    
    private static URL transformURL(final URL url, final NbModuleProject project) {
        //TODO:
        return url;
    }
    
    private static Map getTokens(CreatedModifiedFiles fileSupport, NbModuleProject project, Library library) {
        Map retval = new HashMap();
        
        retval.put("name_to_substitute",library.getName());//NOI18N
        retval.put("bundle_to_substitute",getBundleRelativePath(project).replace('/','.'));//NOI18N
        
        Iterator it = library.getContent(VOLUME_CLASS).iterator();
        retval.put("classpath_to_substitute",getTokenSubstitution(it, fileSupport, project, "release/libs/"));//NOI18N
        
        it = library.getContent(VOLUME_SRC).iterator();
        retval.put("src_to_substitute",getTokenSubstitution(it, fileSupport, project, "release/sources/"));//NOI18N
        
        it = library.getContent(VOLUME_JAVADOC).iterator();
        retval.put("javadoc_to_substitute",getTokenSubstitution(it, fileSupport, project, "release/docs/"));//NOI18N
        
        return retval;
    }
    
    private static String getTokenSubstitution(Iterator it, CreatedModifiedFiles fileSupport, final NbModuleProject project, String pathPrefix) {
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            sb.append("<resource>");//NOI18N
            URL originalURL = (URL)it.next();
            addCopyOperation(fileSupport, project, originalURL, pathPrefix);
            
            URL url = transformURL(originalURL, project);
            sb.append(url.toExternalForm());
            if (it.hasNext()) {
                sb.append("</resource>\n");//NOI18N
            } else {
                sb.append("</resource>");//NOI18N
            }
        }
        return sb.toString();
    }
    
    private static void addCopyOperation(CreatedModifiedFiles fileSupport,  final NbModuleProject project, URL originalURL, String pathPrefix) {
        URL archivURL = FileUtil.getArchiveFile(originalURL);
        if (archivURL != null && FileUtil.isArchiveFile(archivURL)) {
            FileObject archiv = URLMapper.findFileObject(archivURL);
            assert archiv != null;
            StringBuffer sb = new StringBuffer();
            sb.append(pathPrefix).append(archiv.getNameExt());
            fileSupport.add(fileSupport.createFile(sb.toString(),archivURL));
        } else {
            //TODO: probably add new Operation that will zip files first or add there
            // posibility to use instead of URL InputStream and use lazy ByteArrayInputStream.
        }
    }    
}

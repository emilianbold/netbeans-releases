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

package org.netbeans.modules.java.source.ui;


import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 * @author Petr Hrebejk
 */
public class JavaTypeProvider implements TypeProvider {
    private static final Logger LOGGER = Logger.getLogger(JavaTypeProvider.class.getName());
    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath( new FileObject[0] );
    private Set<CacheItem> cache;
    private volatile boolean isCanceled = false;

    public String name() {
        return "java"; // NOI18N
    }

    public String getDisplayName() {
        // TODO - i18n
        return "Java Classes";
    }
    
    public void cleanup() {
        cache = null;
    }

    public void cancel() {
        isCanceled = true;
    }

    public JavaTypeProvider() {
    }
   
//    // This is essentially the code from OpenDeclAction
//    // TODO: Was OpenDeclAction used for anything else?
//    public void gotoType(TypeDescriptor type) {
//    //public void actionPerformed(ActionEvent e) {
//        Lookup lkp = WindowManager.getDefault().getRegistry().getActivated().getLookup();
//        DataObject activeFile = (DataObject) lkp.lookup(DataObject.class);
//        Element value = (Element) lkp.lookup(Element.class);
//        if (activeFile != null && value != null) {
//            JavaSource js = JavaSource.forFileObject(activeFile.getPrimaryFile());
//            if (js != null) {
//                ClasspathInfo cpInfo = js.getClasspathInfo();
//                assert cpInfo != null;
//                UiUtils.open(cpInfo,value);
//            }
//        }
//    }

       public List<? extends TypeDescriptor> getTypeNames(Project project, String text, SearchType searchType) {
            ClassIndex.NameKind nameKind;
            switch (searchType) {
            case EXACT_NAME: nameKind = ClassIndex.NameKind.SIMPLE_NAME; break;
            case PREFIX: nameKind = ClassIndex.NameKind.PREFIX; break;
            case CASE_INSENSITIVE_PREFIX: nameKind = ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX; break;
            case REGEXP: nameKind = ClassIndex.NameKind.REGEXP; break;
            case CASE_INSENSITIVE_REGEXP: nameKind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP; break;
            case CAMEL_CASE: nameKind = ClassIndex.NameKind.CAMEL_CASE; break;
            default: throw new RuntimeException("Unexpected search type: " + searchType);
            }
        
            long time;
            
            long cp, gss, gsb, sfb, gtn, add, sort;
            cp = gss = gsb = sfb = gtn = add = sort = 0;
            
            if (cache == null) {
                // Sources
                time = System.currentTimeMillis();
                ClassPath scp = RepositoryUpdater.getDefault().getScannedSources();
                FileObject roots[] = scp.getRoots();
                gss += System.currentTimeMillis() - time; 
                FileObject root[] = new FileObject[1];
                Set<CacheItem> sources = new HashSet<CacheItem>( roots.length );
                for (int i = 0; i < roots.length; i++ ) {                    
                    root[0] = roots[i];
                    time = System.currentTimeMillis();                
                    ClasspathInfo ci = ClasspathInfo.create( EMPTY_CLASSPATH, EMPTY_CLASSPATH, ClassPathSupport.createClassPath(root));               //create(roots[i]);
                    if ( isCanceled ) {
                        return null;
                    }
                    else {
                        sources.add( new CacheItem( roots[i], ci, false ) );
                    }                        
                    cp += System.currentTimeMillis() - time;
                }
                     
                
                
                // Binaries
                time = System.currentTimeMillis();                
                scp = RepositoryUpdater.getDefault().getScannedBinaries();
                roots = scp.getRoots(); 
                gsb += System.currentTimeMillis() - time;
                root = new FileObject[1];
                for (int i = 0; i < roots.length; i++ ) {
                    try {
                        if ( isCanceled ) {
                            return null;
                        }
                        time = System.currentTimeMillis();
                        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(roots[i].getURL());
                        if ( result.getRoots().length == 0 ) {
                            continue;
                        }       
                        sfb += System.currentTimeMillis() - time;                        
                        time = System.currentTimeMillis();                        
                        root[0] = roots[i];
                        ClasspathInfo ci = ClasspathInfo.create(ClassPathSupport.createClassPath(root), EMPTY_CLASSPATH, EMPTY_CLASSPATH );//create(roots[i]);                                
                        sources.add( new CacheItem( roots[i], ci, true ) );                                                
                        cp += System.currentTimeMillis() - time;
                    }
                    catch ( FileStateInvalidException e ) {
                        continue;
                    }                   
                    finally {
                        if ( isCanceled ) {
                            return null;
                        }
                    }
                }
                if ( !isCanceled ) {
                    cache = sources;
                }
                else {
                    return null;
                }
                
            }
            
            ArrayList<JavaTypeDescription> types = new ArrayList<JavaTypeDescription>(cache.size() * 20);
            
            for( CacheItem ci : cache ) {    
                time = System.currentTimeMillis();
                
                String textForQuery;
                switch( nameKind ) {
                    case REGEXP:
                    case CASE_INSENSITIVE_REGEXP:
                        String pattern = text + "*"; // NOI18N
                        pattern = pattern.replace( "*", ".*" ).replace( '?', '.' );
                        textForQuery = pattern;
                        break;
                    default:
                        textForQuery = text;
                }
                Set<ElementHandle<TypeElement>> names = ci.classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, nameKind, EnumSet.of( ci.isBinary ? ClassIndex.SearchScope.DEPENDENCIES : ClassIndex.SearchScope.SOURCE ));
                if ( isCanceled ) {
                    return null;
                }
                
                gtn += System.currentTimeMillis() - time;            
                time = System.currentTimeMillis();
                
//              Removed because of bad performance To reenable see diff between 1.15 and 1.16
//              ClassPath.Entry defEntry = ci.getDefiningEntry();
                for (ElementHandle<TypeElement> name : names) {
//                    Removed because of bad performance To reenable see diff between 1.15 and 1.16
//                    if (defEntry.includes(convertToSourceName(name.getBinaryName()))) {
                        JavaTypeDescription td = new JavaTypeDescription(ci, name );
                        types.add(td);
//                    }
                    if ( isCanceled ) {
                        return null;
                    }
                }
                add += System.currentTimeMillis() - time;
            }
            
            if ( !isCanceled ) {            
                time = System.currentTimeMillis();
                // Sorting is now done on the Go To Tpe dialog side
                // Collections.sort(types);
                sort += System.currentTimeMillis() - time;
                LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add + "  SORT: " + sort );
                return types;
            }
            else {
                return null;
            }
        }
  
    static class CacheItem {

        public final boolean isBinary;
        public final FileObject fileObject;
        public final ClasspathInfo classpathInfo;
        public String projectName;
        public Icon projectIcon;
        private ClassPath.Entry defEntry;
                
        public CacheItem ( FileObject fileObject, ClasspathInfo classpathInfo, boolean isBinary ) {
            this.isBinary = isBinary;
            this.fileObject = fileObject;
            this.classpathInfo = classpathInfo;
        }
        
//        Removed because of bad performance To reenable see diff between 1.15 and 1.16
//        
//        public ClassPath.Entry getDefiningEntry () {
//            if (defEntry == null) {
//                ClassPath defCp = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);                    
//                if (defCp != null) {
//                    for (ClassPath.Entry e : defCp.entries()) {
//                        if (fileObject.equals(e.getRoot())) {
//                            defEntry = e;
//                            break;
//                        }
//                    }
//                }
//            }
//            return defEntry;
//        }
        
        @Override
        public int hashCode () {
            return this.fileObject == null ? 0 : this.fileObject.hashCode();
        }
        
        @Override
        public boolean equals (Object other) {
            if (other instanceof CacheItem) {
                CacheItem otherItem = (CacheItem) other;
                return this.fileObject == null ? otherItem.fileObject == null : this.fileObject.equals(otherItem.fileObject);
            }
            return false;
        }
    
        public FileObject getRoot() {
            return fileObject;
        }
        
        public boolean isBinary() {
            return isBinary;
        }
        
        public synchronized String getProjectName() {
            if ( !isBinary && projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }
        
        public synchronized Icon getProjectIcon() {
            if ( !isBinary && projectIcon == null ) {
                initProjectInfo();
            }
            return projectIcon;
        }
        
        private void initProjectInfo() {
            Project p = FileOwnerQuery.getOwner(fileObject);                    
            if (p != null) {
                ProjectInformation pi = ProjectUtils.getInformation( p );
                projectName = pi.getDisplayName();
                projectIcon = pi.getIcon();
            }
        }
        
    }
}

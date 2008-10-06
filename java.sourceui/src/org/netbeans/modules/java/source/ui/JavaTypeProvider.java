/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.source.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.BinaryElementOpen;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Petr Hrebejk
 */
public class JavaTypeProvider implements TypeProvider {
    private static final Logger LOGGER = Logger.getLogger(JavaTypeProvider.class.getName());
    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath( new FileObject[0] );
    private Set<CacheItem> cache;
    private volatile boolean isCanceled = false;
    private final TypeElementFinder.Customizer customizer;
    private ClasspathInfo cpInfo;
    private GlobalPathRegistryListener pathListener;

    public String name() {
        return "java"; // NOI18N
    }

    public String getDisplayName() {
        // TODO - i18n
        return "Java Classes";
    }
    
    public void cleanup() {
        isCanceled = false;
        cache = null;
        if (pathListener != null)
            GlobalPathRegistry.getDefault().removeGlobalPathRegistryListener(pathListener);
    }

    public void cancel() {
        isCanceled = true;
    }
    
    public JavaTypeProvider() {
        this(null, null);
    }
   
    public JavaTypeProvider(ClasspathInfo cpInfo, TypeElementFinder.Customizer customizer) {
        this.cpInfo = cpInfo;
        this.customizer = customizer;
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

    public void computeTypeNames(Context context, final Result res) {
        isCanceled = false;
        String text = context.getText();
        SearchType searchType = context.getSearchType();
        
        boolean hasBinaryOpen = Lookup.getDefault().lookup(BinaryElementOpen.class) != null;
        final ClassIndex.NameKind nameKind;
        switch (searchType) {
        case EXACT_NAME: nameKind = ClassIndex.NameKind.SIMPLE_NAME; break;
        case CASE_INSENSITIVE_EXACT_NAME: nameKind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP; break;        
        case PREFIX: nameKind = ClassIndex.NameKind.PREFIX; break;
        case CASE_INSENSITIVE_PREFIX: nameKind = ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX; break;
        case REGEXP: nameKind = ClassIndex.NameKind.REGEXP; break;
        case CASE_INSENSITIVE_REGEXP: nameKind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP; break;
        case CAMEL_CASE: nameKind = ClassIndex.NameKind.CAMEL_CASE; break;
        default: throw new RuntimeException("Unexpected search type: " + searchType);
        }
        
        long time = 0;

        long cp, gss, gsb, sfb, gtn, add, sort;
        cp = gss = gsb = sfb = gtn = add = sort = 0;

        Future<Project[]> openProjectsTask = OpenProjects.getDefault().openProjects();
        try {
            openProjectsTask.get();
        } catch (InterruptedException ex) {
            LOGGER.fine(ex.getMessage());
        } catch (ExecutionException ex) {
            LOGGER.fine(ex.getMessage());
        }
        
        if (cache == null) {
            Set<CacheItem> sources = null;

            if (cpInfo == null) {
                // Sources
                time = System.currentTimeMillis();
                ClassPath scp = RepositoryUpdater.getDefault().getScannedSources();
                FileObject roots[] = scp.getRoots();
                gss += System.currentTimeMillis() - time; 
                FileObject root[] = new FileObject[1];
                sources = new HashSet<CacheItem>( roots.length );
                for (int i = 0; i < roots.length; i++ ) {                    
                    root[0] = roots[i];
                    time = System.currentTimeMillis();                
                    ClasspathInfo ci = ClasspathInfo.create( EMPTY_CLASSPATH, EMPTY_CLASSPATH, ClassPathSupport.createClassPath(root));               //create(roots[i]);
                    if ( isCanceled ) {
                        return;
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
                            return;
                        }
                        time = System.currentTimeMillis();
                        if (!hasBinaryOpen) {
                        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(roots[i].getURL());
                        if ( result.getRoots().length == 0 ) {
                            continue;
                        }       
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
                            return;
                        }
                    }
                }
            } else { // user provided classpath

                FileObject[] bootRoots = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT).getRoots();
                FileObject[] compileRoots = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE).getRoots();
                FileObject[] sourceRoots = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE).getRoots();
                sources = new HashSet<CacheItem>(bootRoots.length + compileRoots.length + sourceRoots.length);

                // bootPath
                for (int i = 0; i < bootRoots.length; i++ ) {                    
                    time = System.currentTimeMillis();                
                    ClasspathInfo ci = ClasspathInfo.create(ClassPathSupport.createClassPath(bootRoots[i]), EMPTY_CLASSPATH, EMPTY_CLASSPATH);
                    if ( isCanceled ) {
                        return;
                    }
                    else {
                        sources.add( new CacheItem( bootRoots[i], ci, true ) );
                    }                        
                    cp += System.currentTimeMillis() - time;
                }

                // classPath
                for (int i = 0; i < compileRoots.length; i++ ) {                    
                    time = System.currentTimeMillis();                
                    ClasspathInfo ci = ClasspathInfo.create(EMPTY_CLASSPATH, ClassPathSupport.createClassPath(compileRoots[i]), EMPTY_CLASSPATH);
                    if ( isCanceled ) {
                        return;
                    }
                    else {
                        sources.add( new CacheItem( compileRoots[i], ci, true ) );
                    }                        
                    cp += System.currentTimeMillis() - time;
                }

                // sourcePath
                for (int i = 0; i < sourceRoots.length; i++ ) {                    
                    time = System.currentTimeMillis();                
                    ClasspathInfo ci = ClasspathInfo.create(EMPTY_CLASSPATH, EMPTY_CLASSPATH, ClassPathSupport.createClassPath(sourceRoots[i]));
                    if ( isCanceled ) {
                        return;
                    }
                    else {
                        sources.add( new CacheItem( sourceRoots[i], ci, false ) );
                    }                        
                    cp += System.currentTimeMillis() - time;
                }

            }
                
            if ( !isCanceled ) {
                cache = sources;
            }
            else {
                return;
            }

        }

        ArrayList<JavaTypeDescription> types = new ArrayList<JavaTypeDescription>(cache.size() * 20);
        
        // is scan in progress? If so, provide a message to user.
        boolean scanInProgress = RepositoryUpdater.getDefault().isScanInProgress();
        if (scanInProgress) {
            // ui message
            String message = NbBundle.getMessage(JavaTypeProvider.class, "LBL_ScanInProgress_warning");
            res.setMessage(message);
        } else {
            res.setMessage(null);
        }

        final String textForQuery;
        switch( nameKind ) {
            case REGEXP:
            case CASE_INSENSITIVE_REGEXP:
                text = removeNonJavaChars(text);
                String pattern = searchType == SearchType.CASE_INSENSITIVE_EXACT_NAME ? text : text + "*"; // NOI18N
                pattern = pattern.replace( "*", ".*" ).replace( '?', '.' );
                textForQuery = pattern;
                break;
            default:
                textForQuery = text;
        }
        LOGGER.fine("Text For Query '" + text + "'.");
        if (customizer != null) {
            for(final CacheItem ci : cache) {
                time = System.currentTimeMillis();
                Set<ElementHandle<TypeElement>> names = customizer.query(
                        ci.classpathInfo, textForQuery, nameKind,
                        EnumSet.of(ci.isBinary ? ClassIndex.SearchScope.DEPENDENCIES : ClassIndex.SearchScope.SOURCE)
                    );
                for (ElementHandle<TypeElement> name : names) {
                    JavaTypeDescription td = new JavaTypeDescription(ci, name);
                    types.add(td);
                    if (isCanceled) {
                        return;
                    }
                }
            }

        } else {
            for(final CacheItem ci : cache) {
                @SuppressWarnings("unchecked")
                final Set<ElementHandle<TypeElement>> names = ci.classpathInfo.getClassIndex().getDeclaredTypes(
                            textForQuery, nameKind, EnumSet.of(ci.isBinary ? ClassIndex.SearchScope.DEPENDENCIES : ClassIndex.SearchScope.SOURCE)
                        );
                for (ElementHandle<TypeElement> name : names) {
                    JavaTypeDescription td = new JavaTypeDescription(ci, name);
                    types.add(td);
                    if (isCanceled) {
                        return;
                    }
                }
            }
            if (types.isEmpty() && scanInProgress) {
                try {
                    ClassPath cPath = ClassPathSupport.createClassPath(new URL[0]);
                    ClasspathInfo cInfo = ClasspathInfo.create(cPath, cPath, cPath);
                    JavaSource src = JavaSource.create(cInfo);
                    Future<Void> f = src.runWhenScanFinished(new Task<CompilationController>() {
                        public void run(CompilationController parameter) throws Exception {
                            LOGGER.fine("Restarting search...");
                            res.setMessage(null);
                        }
                    }, false);
                    f.get();
                    cache = null;
                    cpInfo = null;
                    computeTypeNames(context, res);
                    return;
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if ( isCanceled ) {
                return;
            }

            gtn += System.currentTimeMillis() - time;            
            time = System.currentTimeMillis();

            add += System.currentTimeMillis() - time;
        }
        
        if ( !isCanceled ) {            
            time = System.currentTimeMillis();
            // Sorting is now done on the Go To Tpe dialog side
            // Collections.sort(types);
            sort += System.currentTimeMillis() - time;
            LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add + "  SORT: " + sort );
            res.addResult(types);
        }
        
    }
    
    private static boolean isAllUpper( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( !Character.isUpperCase( text.charAt( i ) ) ) {
                return false;
            }
        }
        
        return true;
    }
   
    private static String removeNonJavaChars(String text) {
       StringBuilder sb = new StringBuilder();

       for( int i = 0; i < text.length(); i++) {
           char c = text.charAt(i);
           if( Character.isJavaIdentifierPart(c) || c == '*' || c == '?') {
               sb.append(c);
           }
       }
       return sb.toString();
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

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

package org.netbeans.modules.csl.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.Index;
import org.netbeans.modules.csl.api.NameKind;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.api.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.IndexSearcher.Descriptor;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.modules.csl.navigation.Icons;
import org.netbeans.modules.csl.source.usages.ClassIndexManager;
import org.netbeans.modules.csl.source.usages.RepositoryUpdater;
import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.symbol.SymbolProvider.Context;
import org.netbeans.spi.jumpto.symbol.SymbolProvider.Result;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Highly similar to GsfTypeProvider; this implements the SymbolProvider instead of
 * the TypeProvider interface from the jumpto module
 *
 * @author Tor Norbye
 */
public class GsfSymbolProvider implements SymbolProvider, IndexSearcher.Helper {
    private static final Logger LOGGER = Logger.getLogger(GsfSymbolProvider.class.getName());
    private static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath( new FileObject[0] );
    private Set<CacheItem> cache;
    private volatile boolean isCancelled = false;

    //@Override
    public void cleanup() {
        cache = null;
    }

    static class CacheItem {

        public final boolean isBinary;
        public final FileObject fileObject;
        public final ClasspathInfo classpathInfo;
        public String projectName;
        public Icon projectIcon;
//        private ClassPath.Entry defEntry;

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
            if (projectName == null) {
            try {
                java.net.URL url = fileObject.getURL();
                if (ClassIndexManager./*get(language).*/isBootRoot(url)) {
                   projectName = "Ruby Lib";
               }
            }
            catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            }
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


    public GsfSymbolProvider() {
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


    //@Override
    public void computeSymbolNames(Context context, Result res) {
            isCancelled = false;
            String text = context.getText();
            SearchType nameKind = context.getSearchType();

            long time;

            long cp, gss, gsb, sfb, gtn, add, sort;
            cp = gss = gsb = sfb = gtn = add = sort = 0;

            if (cache == null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("GoToTypeAction.getTypeNames recreates cache\n");
                }
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
                    ClasspathInfo ci = ClasspathInfo.create(EMPTY_CLASSPATH, EMPTY_CLASSPATH, ClassPathSupport.createClassPath(root));
                    //create(roots[i]);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("GoToTypeAction.getTypeNames created ClasspathInfo for source: " + FileUtil.getFileDisplayName(roots[i])+"\n");
                    }
//                    if ( isCanceled ) {
                    if ( isCancelled ) {
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
                        time = System.currentTimeMillis();
                        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(roots[i].getURL());
                        if ( result.getRoots().length == 0 ) {
                            continue;
                        }
                        sfb += System.currentTimeMillis() - time;
                        time = System.currentTimeMillis();
                        root[0] = roots[i];
                        ClasspathInfo ci = ClasspathInfo.create(ClassPathSupport.createClassPath(root), EMPTY_CLASSPATH, EMPTY_CLASSPATH );//create(roots[i]);
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("GoToTypeAction.getTypeNames created ClasspathInfo for binary: " + FileUtil.getFileDisplayName(roots[i])+"\n");
                        }
                        sources.add( new CacheItem( roots[i], ci, true ) );

                        cp += System.currentTimeMillis() - time;
                    }
                    catch ( FileStateInvalidException e ) {
                        continue;
                    }
                }
//                if ( !isCanceled ) {
                if ( !isCancelled ) {
                    cache = sources;
                }
                else {
                    return;
                }

            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("GoToTypeAction.getTypeNames collected : " + cache.size() +" elements\n");
            }

            //ArrayList<GsfTypeDescription> types = new ArrayList<GsfTypeDescription>(cache.size() * 20);
            ArrayList<SymbolDescriptor> types = new ArrayList<SymbolDescriptor>(cache.size() * 20);

            NameKind indexNameKind;
            switch (nameKind) {
            case CAMEL_CASE: indexNameKind = NameKind.CAMEL_CASE; break;
            case CASE_INSENSITIVE_PREFIX: indexNameKind = NameKind.CASE_INSENSITIVE_PREFIX; break;
            case CASE_INSENSITIVE_REGEXP: indexNameKind = NameKind.CASE_INSENSITIVE_REGEXP; break;
            case PREFIX: indexNameKind = NameKind.PREFIX; break;
            case REGEXP: indexNameKind = NameKind.REGEXP; break;
            case EXACT_NAME: indexNameKind = NameKind.EXACT_NAME; break;
	    case CASE_INSENSITIVE_EXACT_NAME: indexNameKind = NameKind.EXACT_NAME; break;
            default: throw new RuntimeException("Unexpected name kind: " + nameKind);
            }

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
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("GoToTypeAction.getTypeNames queries usages of: " + ci.classpathInfo+"\n");
                }

                //Set<? extends Element/*Handle<Element>*/> names = getTypes(index, textForQuery, indexNameKind,  EnumSet.of(ci.isBinary ? Index.SearchScope.DEPENDENCIES : Index.SearchScope.SOURCE ));
                Set<? extends SymbolDescriptor> names = getSymbols(ci.classpathInfo, textForQuery, indexNameKind,  EnumSet.of(ci.isBinary ? Index.SearchScope.DEPENDENCIES : Index.SearchScope.SOURCE ));
                //Set<ElementHandle<TypeElement>> names = ci.classpathInfo.getClassIndex().getDeclaredTypes(textForQuery, indexNameKind, EnumSet.of( ci.isBinary ? ClassIndex.SearchScope.DEPENDENCIES : ClassIndex.SearchScope.SOURCE ));
//                if ( isCanceled ) {
                if ( isCancelled ) {
                    return;
                }

                gtn += System.currentTimeMillis() - time;
                time = System.currentTimeMillis();

//              Removed because of bad performance To reenable see diff between 1.15 and 1.16
//              ClassPath.Entry defEntry = ci.getDefiningEntry();
                //for (Element/*Handle<Element>*/ name : names) {
                for (SymbolDescriptor td : names) {
//                    Removed because of bad performance To reenable see diff between 1.15 and 1.16
//                    if (defEntry.includes(convertToSourceName(name.getBinaryName()))) {
                        //GsfTypeDescription td = new GsfTypeDescription(ci, name );
                        types.add(td);
//                    }
//                    if ( isCanceled ) {
                    if ( isCancelled ) {
                        return;
                    }
                }
                add += System.currentTimeMillis() - time;
            }

            if ( !isCancelled ) {
                time = System.currentTimeMillis();
                // Sorting is now done on the Go To Tpe dialog side
                // Collections.sort(types);
                sort += System.currentTimeMillis() - time;
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add + "  SORT: " + sort );
                }
                res.addResult(types);
            }
        }

        private Set<? extends SymbolDescriptor> getSymbols(ClasspathInfo classpathInfo, String textForQuery, NameKind kind, EnumSet<Index.SearchScope> scope) {
            Set<GsfSymbolDescriptor> items = new HashSet<GsfSymbolDescriptor>();
            for (Language language : LanguageRegistry.getInstance()) {
                IndexSearcher searcher = language.getIndexSearcher();
                if (searcher != null) {
                    Index index = classpathInfo.getClassIndex(language.getMimeType());
                    try {
                        Set<? extends Descriptor> set = searcher.getSymbols(index, textForQuery, kind, scope, this);
                        if (set != null) {
                            for (Descriptor desc : set) {
                                GsfSymbolDescriptor d = new GsfSymbolDescriptor(desc);
                                items.add(d);
                            }
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            
            return items;
        }

    public Icon getIcon(ElementHandle element) {
        return Icons.getElementIcon(element.getKind(), element.getModifiers());
    }

    public void open(FileObject fileObject, ElementHandle element) {
        Source js = Source.create (fileObject);
        if (js != null) {
            UiUtils.open(js, element);
        }
    }

    public String name() {
        return "GSF"; // NOI18N
    }

    public String getDisplayName() {
        return LanguageRegistry.getInstance().getLanguagesDisplayName();
    }

    public void cancel() {
        isCancelled = true;
    }

    private class GsfSymbolDescriptor extends SymbolDescriptor {
        private Descriptor delegated;

        private GsfSymbolDescriptor(Descriptor delegated) {
            this.delegated = delegated;
        }

//        @Override
//        public String getSimpleName() {
//            return delegated.getSimpleName();
//        }
//
//        @Override
//        public String getOuterName() {
//            return delegated.getOuterName();
//        }
//
//        @Override
//        public String getTypeName() {
//            return delegated.getTypeName();
//        }
//
//        @Override
//        public String getContextName() {
//            return delegated.getContextName();
//        }

        @Override
        public Icon getIcon() {
            return delegated.getIcon();
        }

        @Override
        public String getProjectName() {
            return delegated.getProjectName();
        }

        @Override
        public Icon getProjectIcon() {
            return delegated.getProjectIcon();
        }

        @Override
        public FileObject getFileObject() {
            return delegated.getFileObject();
        }

        @Override
        public int getOffset() {
            return delegated.getOffset();
        }

        @Override
        public void open() {
            delegated.open();
        }

        @Override
        public String getSymbolName() {
            return delegated.getSimpleName();
        }

        @Override
        public String getOwnerName() {
            String owner = delegated.getContextName();
            if (owner == null) {
                owner = "";
            }

            return owner;
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
 *
 * Contributor(s): Andrei Badea
 *                 Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jumpto.type.GoToTypeAction;
import org.netbeans.modules.jumpto.type.Models;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openidex.search.FileObjectFilter;
import org.openidex.search.SearchInfoFactory;
/**
 *
 * @author Andrei Badea, Petr Hrebejk
 */
public class FileSearchAction extends AbstractAction implements FileSearchPanel.ContentProvider {

    /* package */ static final Logger LOGGER = Logger.getLogger(FileSearchAction.class.getName());
    
    private static ListModel EMPTY_LIST_MODEL = new DefaultListModel();
    private static final RequestProcessor rp = new RequestProcessor ("FileSearchAction-RequestProcessor",1);
    private Worker running;
    private RequestProcessor.Task task;
    private Dialog dialog;
    private JButton openBtn;
    private FileSearchPanel panel;
    private Dimension initialDimension;
    private Iterable<? extends FileProvider> providers;
    
    public FileSearchAction() {
        super( NbBundle.getMessage(FileSearchAction.class, "CTL_FileSearchAction") );
        // XXX this should be in initialize()?
        putValue("PopupMenuText", NbBundle.getBundle(FileSearchAction.class).getString("editor-popup-CTL_FileSearchAction")); // NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        return OpenProjects.getDefault().getOpenProjects().length > 0;
    }
    
    public void actionPerformed(ActionEvent arg0) {
        FileDescriptor[] typeDescriptors = getSelectedFiles();
        if (typeDescriptors != null) {
            for(FileDescriptor td: typeDescriptors){
                td.open();
            }
        }
    }

    // Implementation of content provider --------------------------------------


    public ListCellRenderer getListCellRenderer( JList list ) {
        return new Renderer( list );
    }


    public void setListModel( FileSearchPanel panel, String text ) {
        if (openBtn != null) {
            openBtn.setEnabled (false);
        }
        if ( running != null ) {
            running.cancel();
            task.cancel();
            running = null;
        }

        if ( text == null ) {
            panel.setModel(EMPTY_LIST_MODEL);
            return;
        }

        boolean exact = text.endsWith(" "); // NOI18N

        text = text.trim();

        if ( text.length() == 0) {
            panel.setModel(EMPTY_LIST_MODEL);
            return;
        }

        int wildcard = GoToTypeAction.containsWildCard(text);
        QuerySupport.Kind nameKind;

        if (exact) {
            //nameKind = panel.isCaseSensitive() ? QuerySupport.Kind.EXACT : QuerySupport.Kind.CASE_INSENSITIVE_EXACT;
            nameKind = QuerySupport.Kind.EXACT;
        }
        else if (wildcard != -1) {
            nameKind = panel.isCaseSensitive() ? QuerySupport.Kind.REGEXP : QuerySupport.Kind.CASE_INSENSITIVE_REGEXP;
            text = wildcards2regexp(text);
        }
        else if ((GoToTypeAction.isAllUpper(text) && text.length() > 1) || GoToTypeAction.isCamelCase(text)) {
            nameKind = QuerySupport.Kind.CAMEL_CASE;
        }
        else {
            nameKind = panel.isCaseSensitive() ? QuerySupport.Kind.PREFIX : QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;
        }

        // Compute in other thread

        synchronized( this ) {
            running = new Worker(text , nameKind, panel.getCurrentProject());
            task = rp.post( running, 220);
            if ( panel.time != -1 ) {
                LOGGER.fine( "Worker posted after " + ( System.currentTimeMillis() - panel.time ) + " ms."  );
            }
        }
    }

    public void closeDialog() {
        dialog.setVisible( false );
        cleanup();
    }

    public boolean hasValidContent () {
        return this.openBtn != null && this.openBtn.isEnabled();
    }

    // Private methods ---------------------------------------------------------

    private FileDescriptor[] getSelectedFiles() {
        FileDescriptor[] result = null;
        panel = new FileSearchPanel(this, findCurrentProject());
        dialog = createDialog(panel);
        dialog.setVisible(true);
        result = panel.getSelectedFiles();
        return result;
    }

   private Dialog createDialog( final FileSearchPanel panel) {
        openBtn = new JButton();
        Mnemonics.setLocalizedText(openBtn, NbBundle.getMessage(FileSearchAction.class, "CTL_Open"));
        openBtn.getAccessibleContext().setAccessibleDescription(openBtn.getText());
        openBtn.setEnabled( false );
        
        final Object[] buttons = new Object[] { openBtn, DialogDescriptor.CANCEL_OPTION };
        
        String title = NbBundle.getMessage(FileSearchAction.class, "MSG_FileSearchDlgTitle");
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                title,
                true,
                buttons,
                openBtn,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP, 
                new DialogButtonListener(panel));
        dialogDescriptor.setClosingOptions(buttons);

        Dialog d = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        d.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSearchAction.class, "AN_FileSearchDialog"));
        d.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSearchAction.class, "AD_FileSearchDialog"));
                
        // Set size
        d.setPreferredSize( new Dimension(  FileSearchOptions.getWidth(),
                                                 FileSearchOptions.getHeight() ) );
        
        // Center the dialog after the size changed.
        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * 9) / 10;
        int maxH = (r.height * 9) / 10;
        Dimension dim = d.getPreferredSize();
        dim.width = Math.min(dim.width, maxW);
        dim.height = Math.min(dim.height, maxH);
        initialDimension = dim;
        d.setBounds(Utilities.findCenterBounds(dim));
        d.addWindowListener(new WindowAdapter() {
            public @Override void windowClosed(WindowEvent e) {
                cleanup();
            }
        });

        return d;
    }
    
    /** For original of this code look at:
     *  org.netbeans.modules.project.ui.actions.ActionsUtil
     */
    private static Project findCurrentProject( ) {
        Lookup lookup = Utilities.actionsGlobalContext();

        // Maybe the project is in the lookup
        for (Project p : lookup.lookupAll(Project.class)) {
            return p;
        }
        // Now try to guess the project from dataobjects
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                return p;
            }
        }
        
       return OpenProjects.getDefault().getMainProject();
    }
    
//    JButton getOpenButton() {
//        return openBtn;
//    }

    private void cleanup() {

        if ( dialog != null ) { // Closing event for some reson sent twice
            //Free SPI
            synchronized (this) {
                providers = null;
            }
            // Save dialog size only when changed
            final int currentWidth = dialog.getWidth();
            final int currentHeight = dialog.getHeight();
            if (initialDimension != null && (initialDimension.width != currentWidth || initialDimension.height != currentHeight)) {
                FileSearchOptions.setHeight(currentHeight);
                FileSearchOptions.setWidth(currentWidth);
            }
            initialDimension = null;
            // Clean caches
            dialog.dispose();
            this.dialog = null;
            //GoToTypeAction.this.cache = null;
            FileSearchOptions.flush();
        }
    }

    private Iterable<? extends FileProvider> getProviders() {
        synchronized (this) {
            if (providers != null) {
                return providers;
            }
        }
        final List<FileProvider> result = new LinkedList<FileProvider>();
        for (FileProviderFactory fpf : Lookup.getDefault().lookupAll(FileProviderFactory.class)) {
            result.add(fpf.createFileProvider());
        }
        synchronized (this) {
            if (providers == null) {
                providers = Collections.unmodifiableList(result);
            }
            return providers;
        }
    }

    private static String wildcards2regexp(String pattern) {
        return pattern.replace(".", "\\.").replace( "*", ".*" ).replace( '?', '.' ).concat(".*"); //NOI18N
    }

    // Private classes ---------------------------------------------------------



    private class Worker implements Runnable {

        private volatile boolean isCanceled = false;
        private volatile FileProvider currentProvider;

        private final String text;
        private final QuerySupport.Kind searchType;
        private final Project currentProject;
        private final long createTime;

        public Worker(String text, QuerySupport.Kind searchType, Project currentProject) {
            this.text = text;
            this.searchType = searchType;
            this.currentProject = currentProject;
            this.createTime = System.currentTimeMillis();
            LOGGER.fine( "Worker for " + text + ", " + searchType + " - created after " + ( System.currentTimeMillis() - panel.time ) + " ms."  );
       }

        public void run() {

            LOGGER.fine( "Worker for " + text + " - started " + ( System.currentTimeMillis() - createTime ) + " ms."  );

            final List<? extends FileDescriptor> files = getFileNames( text );
            if ( isCanceled ) {
                LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );
                return;
            }
            final ListModel model = Models.fromList(files);
//            if (typeFilter != null) {
//                model = LazyListModel.create(model, GoToTypeAction.this, 0.1, "Not computed yet");
//            }
//            final ListModel fmodel = model;
//            if ( isCanceled ) {
//                LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );
//                return;
//            }

            if ( !isCanceled && model != null ) {
                LOGGER.fine( "Worker for text " + text + " finished after " + ( System.currentTimeMillis() - createTime ) + " ms."  );
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        panel.setModel(model);
                        if (openBtn != null && !files.isEmpty()) {
                            openBtn.setEnabled (true);
                        }
                    }
                });
            }


        }

        public void cancel() {
            if ( panel.time != -1 ) {
                LOGGER.fine( "Worker for text " + text + " canceled after " + ( System.currentTimeMillis() - createTime ) + " ms."  );
            }
            FileProvider provider;
            synchronized (this) {
                isCanceled = true;
                provider = currentProvider;
            }
            if (provider != null) {
                    provider.cancel();
            }
        }

        private List<? extends FileDescriptor> getFileNames(String text) {
            String searchField;
            switch (searchType) {
                case CASE_INSENSITIVE_PREFIX:
                case CASE_INSENSITIVE_REGEXP:
                    searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME; break;
                default:
                    searchField = FileIndexer.FIELD_NAME; break;
            }

            final Collection<FileObject> roots = new ArrayList<FileObject>(QuerySupport.findRoots((Project) null, null, Collections.<String>emptyList(), Collections.<String>emptyList()));
            try {
                QuerySupport q = QuerySupport.forRoots(FileIndexer.ID, FileIndexer.VERSION, roots.toArray(new FileObject [roots.size()]));
                Collection<? extends IndexResult> results = q.query(searchField, text, searchType);
                ArrayList<FileDescriptor> files = new ArrayList<FileDescriptor>();
                for(IndexResult r : results) {
                    FileObject file = r.getFile();
                    if (file == null || !file.isValid()) {
                        // the file has been deleted in the meantime
                        continue;
                    }

                    Project project = FileOwnerQuery.getOwner(file);
                    boolean preferred = project != null && currentProject != null ? project.getProjectDirectory() == currentProject.getProjectDirectory() : false;
                    FileDescriptor fd = new FileDescription(
                        file,
                        r.getRelativePath().substring(0, Math.max(r.getRelativePath().length() - file.getNameExt().length() - 1, 0)),
                        project);
                    FileProviderAccessor.getInstance().setFromCurrentProject(fd, preferred);
                    files.add(fd);
                    LOGGER.finer("Found: " + file.getPath() + ", project=" + project + ", currentProject=" + currentProject + ", preferred=" + preferred);
                }
                if (isCanceled) {
                    return files;
                }

                final Set<FileObject> excludes = new HashSet<FileObject>(roots);
                final Project[] projects = OpenProjects.getDefault().getOpenProjects();
                final List<FileObject> sgRoots = new LinkedList<FileObject>();
                for (Project p : projects) {
                    for (SourceGroup group: ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)) {
                        sgRoots.add(group.getRootFolder());
                    }
                }
                //Ask GTF providers
                final FileProvider.Context ctx = FileProviderAccessor.getInstance().createContext(text, toJumpToSearchType(searchType), currentProject);
                final FileProvider.Result fpR = FileProviderAccessor.getInstance().createResult(files,new String[1], ctx);
                for (FileProvider provider : getProviders()) {
                    currentProvider = provider;
                    try {
                        for (FileObject root : sgRoots) {
                            if (excludes.contains(root)) {
                                continue;
                            }
                            FileProviderAccessor.getInstance().setRoot(ctx, root);
                            boolean recognized = provider.computeFiles(ctx, fpR);
                            if (recognized) {
                                excludes.add(root);
                            }
                        }
                    } finally {
                        currentProvider = null;
                        if (isCanceled) {
                            return files;
                        }
                    }
                }

                //PENDING Now we have to search folders which not included in Search API
                Collection <FileObject> allFolders = new ArrayList<FileObject>();
                final FileObjectFilter[] filters = new FileObjectFilter[]{SearchInfoFactory.VISIBILITY_FILTER, SearchInfoFactory.SHARABILITY_FILTER};
                for (FileObject root : sgRoots) {
                    allFolders = searchSources(root, allFolders, excludes, filters);
                }
                //Looking for matching files in all found folders
                for (FileObject folder: allFolders) {
                    assert folder.isFolder();
                    Enumeration<? extends FileObject> filesInFolder = folder.getData(false);
                    while (filesInFolder.hasMoreElements()) {
                        FileObject file = filesInFolder.nextElement();
                        if (file.isFolder()) continue;

                        if (isMatchedFileObject(searchType, file, text)) {
                            Project project = FileOwnerQuery.getOwner(file);
                            boolean preferred = project != null && currentProject != null ? project.getProjectDirectory() == currentProject.getProjectDirectory() : false;
                            String relativePath = FileUtil.getRelativePath(project.getProjectDirectory(), file);
                            if (relativePath == null)
                                relativePath ="";
                            FileDescriptor fd = new FileDescription(
                                file,
                                relativePath,
                                project
                            );
                            FileProviderAccessor.getInstance().setFromCurrentProject(fd, preferred);
                            files.add(fd);
                        }
                    }
                }
                Collections.sort(files, new FDComarator(panel.isPreferedProject(), panel.isCaseSensitive()));
                return files;
            } catch (PatternSyntaxException pse) {
                return Collections.<FileDescriptor>emptyList();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
                return Collections.<FileDescriptor>emptyList();
            }
        }

        private SearchType toJumpToSearchType(final QuerySupport.Kind searchType) {
            switch (searchType) {
                case CAMEL_CASE:
                case CASE_INSENSITIVE_CAMEL_CASE:
                    return org.netbeans.spi.jumpto.type.SearchType.CAMEL_CASE;
                case CASE_INSENSITIVE_PREFIX:
                    return org.netbeans.spi.jumpto.type.SearchType.CASE_INSENSITIVE_PREFIX;
                case CASE_INSENSITIVE_REGEXP:
                    return org.netbeans.spi.jumpto.type.SearchType.CASE_INSENSITIVE_REGEXP;
                case EXACT:
                    return org.netbeans.spi.jumpto.type.SearchType.EXACT_NAME;
                case PREFIX:
                    return org.netbeans.spi.jumpto.type.SearchType.PREFIX;
                case REGEXP:
                    return org.netbeans.spi.jumpto.type.SearchType.REGEXP;
                default:
                    throw new IllegalArgumentException();
            }
        }
    } // End of Worker class

    private Collection<FileObject> searchSources(FileObject root, Collection<FileObject> result, Collection<? extends FileObject> exclude, FileObjectFilter[] filters) {
        if (root.getChildren().length == 0 || exclude.contains(root) || !checkAgainstFilters(root, filters)) {
            return result;
        } else {
//            if (!exclude.contains(root)) {
                result.add(root);
                Enumeration<? extends FileObject> subFolders = root.getFolders(false);
                while (subFolders.hasMoreElements()) {
                    searchSources(subFolders.nextElement(), result, exclude, filters);
                }
//            }
        }
        return result;
    }

    private boolean isMatchedFileObject(QuerySupport.Kind searchType, FileObject file, String text) {
        boolean isMatched = false;
        switch (searchType) {
            case EXACT: {
                isMatched = file.getNameExt().equals(text);
                break;
            }
            case PREFIX: {
                if (text.length() == 0) {
                    isMatched = true;
                } else {
                    isMatched = file.getNameExt().startsWith(text);
                }
                break;
            }
            case CASE_INSENSITIVE_PREFIX: {
                if (text.length() == 0) {
                    isMatched = true;
                } else {
                    isMatched = file.getNameExt().toLowerCase().startsWith(text.toLowerCase());
                }
                break;
            }
            case CAMEL_CASE: {
                if (text.length() == 0) {
                    throw new IllegalArgumentException ();
                }
                {
                    StringBuilder sb = new StringBuilder();
                    String prefix = null;
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(text, lastIndex + 1);
                        String token = text.substring(lastIndex, index == -1 ? text.length(): index);
                        if ( lastIndex == 0 ) {
                            prefix = token;
                        }
                        sb.append(token);
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                        lastIndex = index;
                    }
                    while(index != -1);

                    final Pattern pattern = Pattern.compile(sb.toString());
                    Matcher m = pattern.matcher(file.getNameExt());
                    isMatched = m.matches();
                }
                break;

            }

            case CASE_INSENSITIVE_REGEXP:
                if (text.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    if (Character.isJavaIdentifierStart(text.charAt(0))) {
                        Pattern pattern = Pattern.compile(text,Pattern.CASE_INSENSITIVE);
                        Matcher m = pattern.matcher(file.getNameExt());
                        isMatched = m.matches();
                    }
                    else {
                        Pattern pattern = Pattern.compile(text,Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
                        Matcher m = pattern.matcher(file.getNameExt());
                        isMatched = m.matches();
                    }
                    break;
                }
            case REGEXP:
                if (text.length() == 0) {
                    throw new IllegalArgumentException ();
                } else {
                    if (Character.isJavaIdentifierStart(text.charAt(0))) {
                        final Pattern pattern = Pattern.compile(text);
                        Matcher m = pattern.matcher(file.getNameExt());
                        isMatched = m.matches();
                    }
                    else {
                        final Pattern pattern = Pattern.compile(text,Pattern.DOTALL);
                        Matcher m = pattern.matcher(file.getNameExt());
                        isMatched = m.matches();
                    }
                    break;
                }

            case CASE_INSENSITIVE_CAMEL_CASE: {
                if (text.length() == 0) {
                    throw new IllegalArgumentException ();
                }
                {
                    StringBuilder sb = new StringBuilder();
                    String prefix = null;
                    int lastIndex = 0;
                    int index;
                    do {
                        index = findNextUpper(text, lastIndex + 1);
                        String token = text.substring(lastIndex, index == -1 ? text.length(): index);
                        if ( lastIndex == 0 ) {
                            prefix = token;
                        }
                        sb.append(token);
                        sb.append( index != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N
                        lastIndex = index;
                    }
                    while(index != -1);

                    final Pattern pattern = Pattern.compile(sb.toString(),Pattern.CASE_INSENSITIVE);
                    Matcher m = pattern.matcher(file.getNameExt());
                    isMatched = m.matches();
                }
                break;
            }
            default:
                throw new UnsupportedOperationException (searchType.toString());

        }
        return isMatched;
    }//checkMatch
    private static int findNextUpper(String text, int offset ) {

        for( int i = offset; i < text.length(); i++ ) {
            if ( Character.isUpperCase(text.charAt(i)) ) {
                return i;
            }
        }
        return -1;
    }

    private boolean checkAgainstFilters(FileObject folder, FileObjectFilter[] filters) {
        assert folder.isFolder();
        for (FileObjectFilter filter: filters) {
            if (filter.traverseFolder(folder) == FileObjectFilter.DO_NOT_TRAVERSE)
                return false;
        }
        return true;
    }
    private class DialogButtonListener implements ActionListener {
        
        private FileSearchPanel panel;
        
        public DialogButtonListener(FileSearchPanel panel) {
            this.panel = panel;
        }
        
        public void actionPerformed(ActionEvent e) {       
            if ( e.getSource() == openBtn) {
                panel.setSelectedFile();
            }
        }
    }

    //Inner classes
    public static class FDComarator implements Comparator<FileDescriptor> {

        private boolean usePrefered;
        private boolean caseSensitive;

        public FDComarator(boolean usePrefered, boolean caseSensitive ) {
            this.usePrefered = usePrefered;
            this.caseSensitive = caseSensitive;
        }

        public int compare(FileDescriptor o1, FileDescriptor o2) {

            // If prefered prefer prefered
            if ( usePrefered ) {
                if (FileProviderAccessor.getInstance().isFromCurrentProject(o1) && !FileProviderAccessor.getInstance().isFromCurrentProject(o2)) {
                    return -1;
                }
                if (!FileProviderAccessor.getInstance().isFromCurrentProject(o1) && FileProviderAccessor.getInstance().isFromCurrentProject(o2)) {
                    return 1;
                }
            }

            // File name
            int cmpr = compareStrings( o1.getFileName(), o2.getFileName(), caseSensitive );
            if ( cmpr != 0 ) {
                return cmpr;
            }

            // Project name
            cmpr = compareStrings( o1.getProjectName(), o2.getProjectName(), caseSensitive );
            if ( cmpr != 0 ) {
                return cmpr;
            }

            // Relative location
            cmpr = compareStrings( o1.getOwnerPath(), o2.getOwnerPath(), caseSensitive );

            return cmpr;

        }

        private int compareStrings(String s1, String s2, boolean caseSensitive) {
            if( s1 == null ) {
                s1 = "";    //NOI18N
            }
            if ( s2 == null ) {
                s2 = "";    //NOI18N
            }


            return caseSensitive ? s1.compareTo( s2 ) : s1.compareToIgnoreCase( s2 );
        }
    }

    private static class RendererComponent extends JPanel {
	private FileDescriptor fd;

	void setDescription(FileDescriptor fd) {
	    this.fd = fd;
	    putClientProperty(TOOL_TIP_TEXT_KEY, null);
	}

	@Override
	public String getToolTipText() {
	    String text = (String) getClientProperty(TOOL_TIP_TEXT_KEY);
	    if( text == null ) {
                if( fd != null) {
                    text = FileUtil.getFileDisplayName(fd.getFileObject());
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }

    public static class Renderer extends DefaultListCellRenderer implements ChangeListener {

        public  static Icon WAIT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/wait.gif", false); // NOI18N

        private RendererComponent rendererComponent;
        private JLabel jlName = new JLabel();
        private JLabel jlPath = new JLabel();
        private JLabel jlPrj = new JLabel();
        private int DARKER_COLOR_COMPONENT = 5;
        private int LIGHTER_COLOR_COMPONENT = 80;
        private Color fgColor;
        private Color fgColorLighter;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;
        private Color bgColorGreener;
        private Color bgColorDarkerGreener;

        private JList jList;

        private boolean colorPrefered;

        public Renderer( JList list ) {

            jList = list;

            Container container = list.getParent();
            if ( container instanceof JViewport ) {
                ((JViewport)container).addChangeListener(this);
                stateChanged(new ChangeEvent(container));
            }

            rendererComponent = new RendererComponent();
            rendererComponent.setLayout(new BorderLayout());
            rendererComponent.add( jlName, BorderLayout.WEST );
            rendererComponent.add( jlPath, BorderLayout.CENTER);
            rendererComponent.add( jlPrj, BorderLayout.EAST );


            jlName.setOpaque(false);
            jlPath.setOpaque(false);
            jlPrj.setOpaque(false);

            jlName.setFont(list.getFont());
            jlPath.setFont(list.getFont());
            jlPrj.setFont(list.getFont());


            jlPrj.setHorizontalAlignment(RIGHT);
            jlPrj.setHorizontalTextPosition(LEFT);

            // setFont( list.getFont() );
            fgColor = list.getForeground();
            fgColorLighter = new Color(
                                   Math.min( 255, fgColor.getRed() + LIGHTER_COLOR_COMPONENT),
                                   Math.min( 255, fgColor.getGreen() + LIGHTER_COLOR_COMPONENT),
                                   Math.min( 255, fgColor.getBlue() + LIGHTER_COLOR_COMPONENT)
                                  );

            bgColor = list.getBackground();
            bgColorDarker = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();


            bgColorGreener = new Color(
                                    Math.abs(bgColor.getRed() - 20),
                                    Math.min(255, bgColor.getGreen() + 10 ),
                                    Math.abs(bgColor.getBlue() - 20) );


            bgColorDarkerGreener = new Color(
                                    Math.abs(bgColorDarker.getRed() - 35),
                                    Math.min(255, bgColorDarker.getGreen() + 5 ),
                                    Math.abs(bgColorDarker.getBlue() - 35) );
        }

        public @Override Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean hasFocus) {

            // System.out.println("Renderer for index " + index );

            int height = list.getFixedCellHeight();
            int width = list.getFixedCellWidth() - 1;

            width = width < 200 ? 200 : width;

            // System.out.println("w, h " + width + ", " + height );

            Dimension size = new Dimension( width, height );
            rendererComponent.setMaximumSize(size);
            rendererComponent.setPreferredSize(size);

            if ( isSelected ) {
                jlName.setForeground(fgSelectionColor);
                jlPath.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);
                rendererComponent.setBackground(bgSelectionColor);
            }
            else {
                jlName.setForeground(fgColor);
                jlPath.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }

            if ( value instanceof FileDescriptor ) {
                FileDescriptor fd = (FileDescriptor)value;
                jlName.setIcon(fd.getIcon());
                jlName.setText(fd.getFileName());
                jlPath.setIcon(null);
                jlPath.setHorizontalAlignment(SwingConstants.LEFT);
                jlPath.setText(fd.getOwnerPath().length() > 0 ? " (" + fd.getOwnerPath() + ")" : " ()"); //NOI18N
                jlPrj.setText(fd.getProjectName());
                jlPrj.setIcon(fd.getProjectIcon());
                if ( !isSelected ) {
                    rendererComponent.setBackground( index % 2 == 0 ?
                        ( FileProviderAccessor.getInstance().isFromCurrentProject(fd) && colorPrefered ? bgColorGreener : bgColor ) :
                        ( FileProviderAccessor.getInstance().isFromCurrentProject(fd) && colorPrefered ? bgColorDarkerGreener : bgColorDarker ) );
                }
                rendererComponent.setDescription(fd);
            }
            else {
                jlName.setText( "" ); // NOI18M
                jlName.setIcon(null);
                jlPath.setIcon(Renderer.WAIT_ICON);
                jlPath.setHorizontalAlignment(SwingConstants.CENTER);
                jlPath.setText( value.toString() );
                jlPrj.setIcon(null);
                jlPrj.setText( "" ); // NOI18N
            }

            return rendererComponent;
        }

        @Override
        public void stateChanged(ChangeEvent event) {

            JViewport jv = (JViewport)event.getSource();

            jlName.setText( "Sample" ); // NOI18N
            jlName.setIcon( new ImageIcon() );

            jList.setFixedCellHeight(jlName.getPreferredSize().height);
            jList.setFixedCellWidth(jv.getExtentSize().width);
        }

        public void setColorPrefered( boolean colorPrefered ) {
            this.colorPrefered = colorPrefered;
        }

     }
}

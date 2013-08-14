/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 *                 markiewb@netbeans.org
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.search.provider.SearchFilter;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.jumpto.EntitiesListCellRenderer;
import org.netbeans.modules.jumpto.common.HighlightingNameFormatter;
import org.netbeans.modules.jumpto.common.Factory;
import org.netbeans.modules.jumpto.type.GoToTypeAction;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
/**
 *
 * @author Andrei Badea, Petr Hrebejk
 */
public class FileSearchAction extends AbstractAction implements FileSearchPanel.ContentProvider {

    /* package */ static final Logger LOGGER = Logger.getLogger(FileSearchAction.class.getName());
    private static final char LINE_NUMBER_SEPARATOR = ':';    //NOI18N
    private static final Pattern PATTERN_WITH_LINE_NUMBER = Pattern.compile("(.*)"+LINE_NUMBER_SEPARATOR+"(\\d*)");    //NOI18N
    
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
    
    @Override
    public void actionPerformed(ActionEvent arg0) {
        FileDescriptor[] typeDescriptors = getSelectedFiles();
        if (typeDescriptors != null) {
            JumpList.checkAddEntry();
            for(FileDescriptor td: typeDescriptors){
                td.open();
            }
        }
    }

    // Implementation of content provider --------------------------------------


    @Override
    public ListCellRenderer getListCellRenderer(
            @NonNull final JList list,
            @NonNull final Document nameDocument,
            @NonNull final ButtonModel caseSensitive) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("nameDocument", nameDocument);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        return new Renderer(list, nameDocument, caseSensitive);
    }


    @Override
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
        }
        else if ((GoToTypeAction.isAllUpper(text) && text.length() > 1) || GoToTypeAction.isCamelCase(text)) {
            nameKind = QuerySupport.Kind.CAMEL_CASE;
        }
        else {
            nameKind = panel.isCaseSensitive() ? QuerySupport.Kind.PREFIX : QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;
        }
                
        //Extract linenumber from search text
        //Pattern is like 'My*Object.java:123'
        final Matcher matcher = PATTERN_WITH_LINE_NUMBER.matcher(text);
        int lineNr;
        if (matcher.matches()) {
            text = matcher.group(1);
            try {
                lineNr = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException numberFormatException) {
                //prevent non convertable numbers
                lineNr=-1;
            }
        } else {
            lineNr = -1;
        }

        // Compute in other thread

        synchronized( this ) {
            running = new Worker(text , nameKind, panel.getCurrentProject(), lineNr);
            task = rp.post( running, 220);
            if ( panel.time != -1 ) {
                LOGGER.log( Level.FINE, "Worker posted after {0} ms.",  System.currentTimeMillis() - panel.time );
            }
        }
    }

    @Override
    public void closeDialog() {
        dialog.setVisible( false );
        cleanup();
    }

    @Override
    public boolean hasValidContent () {
        return this.openBtn != null && this.openBtn.isEnabled();
    }

    private static boolean isLineNumberChange(
            @NonNull final String oldText,
            @NonNull final String newText) {
        final int oldIndex = oldText.indexOf(LINE_NUMBER_SEPARATOR);
        final int newIndex = newText.indexOf(LINE_NUMBER_SEPARATOR);
        if (newIndex > 0) {
            if (oldIndex == newIndex) {
                return newText.substring(0,newIndex).equals(oldText.substring(0,oldIndex));
            } else {
                return newText.equals(oldText + LINE_NUMBER_SEPARATOR);
            }
        } else if (oldIndex > 0) {
            return oldText.equals(newText + LINE_NUMBER_SEPARATOR);
        } else {
            return false;
        }
    }

    // Private methods ---------------------------------------------------------

    private FileDescriptor[] getSelectedFiles() {
        FileDescriptor[] result = null;
        panel = new FileSearchPanel(this, findCurrentProject());
        dialog = createDialog(panel);

        Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
        if (arr.length > 0) {
            EditorCookie ec = arr[0].getCookie (EditorCookie.class);
            if (ec != null) {
                JEditorPane recentPane = NbDocument.findRecentEditorPane(ec);
                if (recentPane != null) {
                    String initSearchText = null;
                    if (org.netbeans.editor.Utilities.isSelectionShowing(recentPane.getCaret())) {
                        initSearchText = recentPane.getSelectedText();
                    }
                    if (initSearchText != null) {
                        panel.setInitialText(initSearchText);
                    } else {
                        FileObject fo = arr[0].getLookup().lookup(FileObject.class);
                        if (fo != null) {
                            panel.setInitialText(fo.getNameExt());
                        }
                    }
                }
            }
        }

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
    
    // Private classes ---------------------------------------------------------



    private class Worker implements Runnable {

        private volatile boolean isCanceled = false;
        private volatile FileProvider currentProvider;

        private final String text;
        private final QuerySupport.Kind searchType;
        private final Project currentProject;
        private final long createTime;
        private final int lineNr;

        public Worker(String text, QuerySupport.Kind searchType, Project currentProject, int lineNr) {
            this.text = text;
            this.lineNr = lineNr;
            this.searchType = searchType;
            this.currentProject = currentProject;
            this.createTime = System.currentTimeMillis();
            LOGGER.log( Level.FINE, "Worker for {0}, {1} - created after {2} ms.",
                    new Object[]{
                        text,
                        searchType,
                        System.currentTimeMillis() - panel.time
            });
       }

        @Override
        public void run() {

            LOGGER.log( Level.FINE, "Worker for {0} - started {1} ms.",
                    new Object[]{
                        text,
                        System.currentTimeMillis() - createTime
            });
            
            final List<? extends FileDescriptor> files = getFileNames();
            if ( isCanceled ) {
                LOGGER.log( Level.FINE, "Worker for {0} exited after cancel {1} ms.",
                        new Object[]{
                            text,
                            System.currentTimeMillis() - createTime
                });
                return;
            }
            final ListModel model = Models.refreshable(
                    Models.fromList(files),
                    new Factory<FileDescriptor, Pair<FileDescriptor,Runnable>>() {
                        @Override
                        public FileDescriptor create(@NonNull final Pair<FileDescriptor,Runnable> param) {
                            return new AsyncFileDescriptor(param.first(), param.second());
                        }
                    });

            if ( !isCanceled && model != null ) {
                LOGGER.log( Level.FINE, "Worker for text {0} finished after {1} ms.",
                        new Object[]{
                            text,
                            System.currentTimeMillis() - createTime
                });
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
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
                LOGGER.log( Level.FINE, "Worker for text {0} canceled after {1} ms.",
                        new Object[]{
                            text,
                            System.currentTimeMillis() - createTime
                });
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

        private List<? extends FileDescriptor> getFileNames() {
            try {
                String searchField;
                String indexQueryText;
                switch (searchType) {
                    case CASE_INSENSITIVE_PREFIX:
                        searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME; 
                        indexQueryText = text;
                        break;
                    case CASE_INSENSITIVE_REGEXP:
                        searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME;
                        indexQueryText = NameMatcherFactory.wildcardsToRegexp(text,true);
                        Pattern.compile(indexQueryText);    //Verify the pattern
                        break;
                    case REGEXP:                        
                        searchField = FileIndexer.FIELD_NAME;
                        indexQueryText = NameMatcherFactory.wildcardsToRegexp(text,true);
                        Pattern.compile(indexQueryText);    //Verify the pattern
                        break;
                    default:
                        searchField = FileIndexer.FIELD_NAME;
                        indexQueryText = text;
                        break;
                }

                ArrayList<FileDescriptor> files = new ArrayList<FileDescriptor>();
                
                // handled by providers and should be excluded from other searches
                final Set<FileObject> excludes = new HashSet<FileObject>();
                final Project[] projects = OpenProjects.getDefault().getOpenProjects();
                final List<FileObject> sgRoots = new LinkedList<FileObject>();
                for (Project p : projects) {
                    for (SourceGroup group : ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)) {
                        sgRoots.add(group.getRootFolder());
                    }
                }
                final SearchType jumpToSearchType = toJumpToSearchType(searchType);

                long st = System.currentTimeMillis();
                //Ask GTF providers
                final FileProvider.Context ctx = FileProviderAccessor.getInstance().createContext(text, jumpToSearchType, lineNr, currentProject);
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
                long et = System.currentTimeMillis();
                LOGGER.log(Level.FINE, "Providers Search: {0}ms", (et-st));
                    
                final Set<FileObject> roots = new LinkedHashSet<FileObject>(QuerySupport.findRoots((Project) null, null, Collections.<String>emptyList(), Collections.<String>emptyList()));
                roots.removeAll(excludes);
                
                // indexing-based search
                QuerySupport q = QuerySupport.forRoots(FileIndexer.ID, FileIndexer.VERSION, roots.toArray(new FileObject[roots.size()]));
                Collection<? extends IndexResult> results = q.query(searchField, indexQueryText, searchType);
                for (IndexResult r : results) {
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
                            project,
                            lineNr);
                    FileProviderAccessor.getInstance().setFromCurrentProject(fd, preferred);
                    files.add(fd);
                    LOGGER.log(Level.FINER, "Found: {0}, project={1}, currentProject={2}, preferred={3}",
                            new Object[]{
                                file.getPath(),
                                project, currentProject, preferred
                            });
                }
                excludes.addAll(roots);
                et = System.currentTimeMillis();
                LOGGER.log(Level.FINE, "Indexed Search: {0}ms", (et - st));
                if (isCanceled) {
                    return files;
                }
                
                //PENDING Now we have to search folders which not included in Search API
                st = System.currentTimeMillis();
                Collection <FileObject> allFolders = new HashSet<FileObject>();
                List<SearchFilter> filters = SearchInfoUtils.DEFAULT_FILTERS;
                for (FileObject root : sgRoots) {
                    allFolders = searchSources(root, allFolders, excludes, filters);
                }
                //Looking for matching files in all found folders
                final NameMatcher matcher = NameMatcherFactory.createNameMatcher(text, jumpToSearchType);
                for (FileObject folder: allFolders) {
                    assert folder.isFolder();
                    Enumeration<? extends FileObject> filesInFolder = folder.getData(false);
                    while (filesInFolder.hasMoreElements()) {
                        FileObject file = filesInFolder.nextElement();
                        if (file.isFolder()) continue;

                        if (matcher.accept(file.getNameExt())) {
                            Project project = FileOwnerQuery.getOwner(file);
                            boolean preferred = false;
                            String relativePath = null;
                            if(project != null) { // #176495
                               FileObject pd = project.getProjectDirectory();
                               preferred = currentProject != null ?
                                 pd == currentProject.getProjectDirectory() :
                                 false;
                                relativePath = FileUtil.getRelativePath(pd, file);
                            }
                            if (relativePath == null)
                                relativePath ="";
                            FileDescriptor fd = new FileDescription(
                                file,
                                relativePath,
                                project,
                                lineNr);
                            FileProviderAccessor.getInstance().setFromCurrentProject(fd, preferred);
                            files.add(fd);
                        }
                    }
                }
                et = System.currentTimeMillis();
                LOGGER.log(Level.FINE, "Unindexed search: {0}ms", (et-st));
                Collections.sort(files, 
                                 new FileComarator(panel.isPreferedProject(),
                                                   panel.isCaseSensitive()));
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

    private Collection<FileObject> searchSources(FileObject root, Collection<FileObject> result, Collection<? extends FileObject> exclude, List<SearchFilter> filters) {
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

    private boolean checkAgainstFilters(FileObject folder, List<SearchFilter> filters) {
        assert folder.isFolder();
        for (SearchFilter filter: filters) {
            if (filter.traverseFolder(folder) == SearchFilter.FolderResult.DO_NOT_TRAVERSE)
                return false;
        }
        return true;
    }
    private class DialogButtonListener implements ActionListener {
        
        private FileSearchPanel panel;
        
        public DialogButtonListener(FileSearchPanel panel) {
            this.panel = panel;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {       
            if ( e.getSource() == openBtn) {
                panel.setSelectedFile();
            }
        }
    }

    //Inner classes
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
                    text = fd.getFileDisplayPath();
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }

    private static final class AsyncFileDescriptor extends FileDescriptor implements Runnable {

        @StaticResource
        private static final String UNKNOWN_ICON_PATH = "org/netbeans/modules/jumpto/resources/unknown.gif";    //NOI18N
        private static final Icon UNKNOWN_ICON = ImageUtilities.loadImageIcon(UNKNOWN_ICON_PATH, false);
        private static final RequestProcessor LOAD_ICON_RP = new RequestProcessor(AsyncFileDescriptor.class.getName(), 1, false, false);

        private final FileDescriptor delegate;
        private final Runnable refreshCallback;
        private volatile Icon icon;

        AsyncFileDescriptor(
            @NonNull final FileDescriptor delegate,
            @NonNull final Runnable refreshCallback) {
            Parameters.notNull("delegate", delegate);   //NOI18N
            Parameters.notNull("refreshCallback", refreshCallback); //NOI18N
            this.delegate = delegate;
            this.refreshCallback = refreshCallback;
        }

        @Override
        public String getFileName() {
           return delegate.getFileName();
        }

        @Override
        public String getOwnerPath() {
            return delegate.getOwnerPath();
        }

        @Override
        public Icon getIcon() {
            if (icon != null) {
                return icon;
            }
            LOAD_ICON_RP.execute(this);
            return UNKNOWN_ICON;
        }

        @Override
        public String getProjectName() {
            return delegate.getProjectName();
        }

        @Override
        public Icon getProjectIcon() {
            return delegate.getProjectIcon();
        }

        @Override
        public void open() {
            delegate.open();
        }

        @Override
        public FileObject getFileObject() {
            final FileObject res = delegate.getFileObject();
            if (res == null) {
                LOGGER.log(
                    Level.FINE,
                    "FileDescriptor: {0} : {1} returned null from getFile", //NOI18N
                    new Object[]{
                        delegate,
                        delegate.getClass()
                    });
            }
            return res;
        }

        @Override
        public String getFileDisplayPath() {
            return delegate.getFileDisplayPath();
        }

        @Override
        public void run() {
            icon = delegate.getIcon();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refreshCallback.run();
                }
            });
        }

    }

    public static class Renderer extends EntitiesListCellRenderer implements ActionListener, DocumentListener {

        public  static Icon WAIT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/wait.gif", false); // NOI18N

        private final HighlightingNameFormatter fileNameFormatter;

        private RendererComponent rendererComponent;
        private JLabel jlName = HtmlRenderer.createLabel();
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

        private String textToFind = "";   //NOI18N
        private boolean caseSensitive;
        private JList jList;

        private boolean colorPrefered;

        public Renderer(
                @NonNull final JList list,
                @NonNull final Document nameDocument,
                @NonNull final ButtonModel caseSensitive) {
            jList = list;
            this.caseSensitive = caseSensitive.isSelected();
            resetName();
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


            jlPath.setOpaque(false);
            jlPrj.setOpaque(false);
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

            bgColor = new Color( list.getBackground().getRGB() );
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
            fileNameFormatter = HighlightingNameFormatter.createBoldFormatter();
            nameDocument.addDocumentListener(this);
            caseSensitive.addActionListener(this);
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
            resetName();
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
                final String formattedFileName = fileNameFormatter.formatName(
                    fd.getFileName(),
                    textToFind,
                    caseSensitive,
                    isSelected? fgSelectionColor : fgColor);
                jlName.setText(formattedFileName);
                jlPath.setIcon(null);
                jlPath.setHorizontalAlignment(SwingConstants.LEFT);
                jlPath.setText(fd.getOwnerPath().length() > 0 ? " (" + fd.getOwnerPath() + ")" : " ()"); //NOI18N
                setProjectName(jlPrj, fd.getProjectName());
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

        @Override
        public void actionPerformed(ActionEvent e) {
            caseSensitive = ((ButtonModel)e.getSource()).isSelected();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            try {
                textToFind = e.getDocument().getText(0, e.getDocument().getLength());
            } catch (BadLocationException ex) {
                textToFind = "";    //NOI18N
            }
        }

        private void resetName() {
            ((HtmlRenderer.Renderer)jlName).reset();
            jlName.setFont(jList.getFont());
            jlName.setOpaque(false);
            ((HtmlRenderer.Renderer)jlName).setHtml(true);
            ((HtmlRenderer.Renderer)jlName).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
        }

     }
}

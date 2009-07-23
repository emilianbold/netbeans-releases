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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
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
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
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
        FileDescription[] typeDescriptors = getSelectedFiles();
        if (typeDescriptors != null) {
            for(FileDescription td: typeDescriptors){
                td.open();
            }
        }
    }

    // Implementation of content provider --------------------------------------


    public ListCellRenderer getListCellRenderer( JList list ) {
        return new FileDescription.Renderer( list );
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

    private FileDescription[] getSelectedFiles() {
        FileDescription[] result = null;
//        try {
            panel = new FileSearchPanel(this, findCurrentProject());
            dialog = createDialog(panel);

//            Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
//            String initSearchText = null;
//            if (arr.length > 0) {
//                EditorCookie ec = arr[0].getCookie (EditorCookie.class);
//                if (ec != null) {
//                    JEditorPane[] openedPanes = ec.getOpenedPanes ();
//                    if (openedPanes != null) {
//                        initSearchText = org.netbeans.editor.Utilities.getSelectionOrIdentifier(openedPanes [0]);
//                        if (initSearchText != null && org.openide.util.Utilities.isJavaIdentifier(initSearchText)) {
//                            panel.setInitialText(initSearchText);
//                        }
//                    }
//                }
//            }

            dialog.setVisible(true);
            result = panel.getSelectedFiles();

//        } catch (IOException ex) {
//            ErrorManager.getDefault().notify(ex);
//        }
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
        //System.out.println("CLEANUP");
        //Thread.dumpStack();

        if ( dialog != null ) { // Closing event for some reson sent twice

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

    private static String wildcards2regexp(String pattern) {
        return pattern.replace(".", "\\.").replace( "*", ".*" ).replace( '?', '.' ).concat(".*"); //NOI18N
    }

    // Private classes ---------------------------------------------------------



    private class Worker implements Runnable {

        private volatile boolean isCanceled = false;

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

            final List<? extends FileDescription> files = getFileNames( text );
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
            synchronized (this) {
                isCanceled = true;
            }
        }

        private List<? extends FileDescription> getFileNames(String text) {
            String searchField;
            switch (searchType) {
                case CASE_INSENSITIVE_PREFIX:
                case CASE_INSENSITIVE_REGEXP:
                    searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME; break;
                    
                default:
                    searchField = FileIndexer.FIELD_NAME; break;
            }

            Collection<? extends FileObject> roots = QuerySupport.findRoots((Project) null, null, Collections.<String>emptyList(), Collections.<String>emptyList());
            try {
                QuerySupport q = QuerySupport.forRoots(FileIndexer.ID, FileIndexer.VERSION, roots.toArray(new FileObject [roots.size()]));
                Collection<? extends IndexResult> results = q.query(searchField, text, searchType);
                ArrayList<FileDescription> files = new ArrayList<FileDescription>();
                for(IndexResult r : results) {
                    FileObject file = r.getFile();
                    if (file == null || !file.isValid()) {
                        // the file has been deleted in the meantime
                        continue;
                    }

                    Project project = FileOwnerQuery.getOwner(file);
                    boolean preferred = project != null && currentProject != null ? project.getProjectDirectory() == currentProject.getProjectDirectory() : false;
                    FileDescription fd = new FileDescription(
                        file,
                        r.getRelativePath().substring(0, Math.max(r.getRelativePath().length() - file.getNameExt().length() - 1, 0)),
                        project,
                        preferred
                    );
                    files.add(fd);
                    LOGGER.finer("Found: " + file.getPath() + ", project=" + project + ", currentProject=" + currentProject + ", preferred=" + preferred);
                }
                //PENDING Now we have to search folders which not included in Search API
                Project[] projects = OpenProjects.getDefault().getOpenProjects();
                Enumeration <? extends FileObject> projectFolders;
                Collection <? extends FileObject> allFolders = new ArrayList<FileObject>();
                final FileObjectFilter[] filters = new FileObjectFilter[]{SearchInfoFactory.VISIBILITY_FILTER, SearchInfoFactory.SHARABILITY_FILTER};
                for (Project p : projects) {
                    Sources s  = ProjectUtils.getSources(p);
                    SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
                    for (SourceGroup group: groups) {
                        FileObject root = group.getRootFolder();
                          allFolders = searchSources(root, allFolders, roots, filters);
                    }
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
                            FileDescription fd = new FileDescription(
                                file,
                                relativePath,
                                project,
                                preferred
                            );
                            files.add(fd);
                        }
                    }
                }
                Collections.sort(files, new FileDescription.FDComarator(panel.isPreferedProject(), panel.isCaseSensitive()));
                return files;
            } catch (PatternSyntaxException pse) {
                return Collections.<FileDescription>emptyList();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
                return Collections.<FileDescription>emptyList();
            }


//            // TODO: Search twice, first for current project, then for all projects
//            List<TypeDescriptor> items;
//            // Multiple providers: merge results
//            items = new ArrayList<TypeDescriptor>(128);
//            String[] message = new String[1];
//            TypeProvider.Context context = TypeProviderAccessor.DEFAULT.createContext(null, text, nameKind);
//            TypeProvider.Result result = TypeProviderAccessor.DEFAULT.createResult(items, message);
//            if (typeProviders == null) {
//                typeProviders = Lookup.getDefault().lookupAll(TypeProvider.class);
//            }
//            for (TypeProvider provider : typeProviders) {
//                if (isCanceled) {
//                    return null;
//                }
//                current = provider;
//                long start = System.currentTimeMillis();
//                try {
//                    LOGGER.fine("Calling TypeProvider: " + provider);
//                    provider.computeTypeNames(context, result);
//                } finally {
//                    current = null;
//                }
//                long delta = System.currentTimeMillis() - start;
//                LOGGER.fine("Provider '" + provider.getDisplayName() + "' took " + delta + " ms.");
//
//            }
//            if ( !isCanceled ) {
//                //time = System.currentTimeMillis();
//                Collections.sort(items, new TypeComparator());
//                panel.setWarning(message[0]);
//                //sort += System.currentTimeMillis() - time;
//                //LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add + "  SORT: " + sort );
//                return items;
//            }
//            else {
//                return null;
//            }
        }
    } // End of Worker class

    private Collection<? extends FileObject> searchSources(FileObject root, Collection<? extends FileObject> result, Collection<? extends FileObject> exclude, FileObjectFilter[] filters) {
        if (root.getChildren().length == 0 || exclude.contains(root) || !checkAgainstFilters(root, filters)) {
            return result;
        } else {
//            if (!exclude.contains(root)) {
                ((Collection<FileObject>)result).add(root);
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
    
}

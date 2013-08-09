/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * markiewb@netbeans.org
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

package org.netbeans.modules.jumpto.type;

import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.ListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.jumpto.type.TypeBrowser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.JumpList;
import org.netbeans.modules.jumpto.EntitiesListCellRenderer;
import org.netbeans.modules.jumpto.common.HighlightingNameFormatter;
import org.netbeans.modules.jumpto.file.LazyListModel;
import org.netbeans.modules.sampler.Sampler;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.HtmlRenderer;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/** 
 * XXX split into action and support class, left this just to minimize diff
 * XXX Icons
 * XXX Don't look for all projects (do it lazy in filter or renderer)
 * @author Petr Hrebejk
 */
public class GoToTypeAction extends AbstractAction implements GoToPanel.ContentProvider, LazyListModel.Filter {
    
    static final Logger LOGGER = Logger.getLogger(GoToTypeAction.class.getName()); // Used from the panel as well
    
    private Collection<? extends SearchType> nameKinds;
    private static ListModel EMPTY_LIST_MODEL = new DefaultListModel();
    private static final RequestProcessor rp = new RequestProcessor ("GoToTypeAction-RequestProcessor",1);      //NOI18N
    private static final RequestProcessor PROFILE_RP = new RequestProcessor("GoToTypeAction-Profile",1);        //NOI18N
    private Worker running;
    private RequestProcessor.Task task;
    GoToPanel panel;
    private Dialog dialog;
    private JButton okButton;
    private Collection<? extends TypeProvider> typeProviders;
    private final Collection<? extends TypeProvider> implicitTypeProviders;
    private final TypeBrowser.Filter typeFilter;
    private final String title;
    private final boolean multiSelection;

    /** Creates a new instance of OpenTypeAction */
    public GoToTypeAction() {
        this(
            NbBundle.getMessage( GoToTypeAction.class, "DLG_GoToType" ),
            null,
            true
        );
    }
    
    public GoToTypeAction(String title, TypeBrowser.Filter typeFilter, boolean multiSelection, TypeProvider... typeProviders) {
        super( NbBundle.getMessage( GoToTypeAction.class,"TXT_GoToType") );
        putValue("PopupMenuText", NbBundle.getBundle(GoToTypeAction.class).getString("editor-popup-TXT_GoToType")); // NOI18N
        this.title = title;
        this.typeFilter = typeFilter;
        this.implicitTypeProviders = typeProviders.length == 0 ? null : Collections.unmodifiableCollection(Arrays.asList(typeProviders));
        this.multiSelection = multiSelection;
    }
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        final Iterable<? extends TypeDescriptor> selectedTypes = getSelectedTypes();
        if (selectedTypes.iterator().hasNext()) {
            JumpList.checkAddEntry();
            for (TypeDescriptor td : selectedTypes) {
                td.open();
            }
        }
    }
            
    public Iterable<? extends TypeDescriptor> getSelectedTypes() {
        return getSelectedTypes(true);
    }
    
    public Iterable<? extends TypeDescriptor> getSelectedTypes(final boolean visible) {
        return getSelectedTypes(visible, null);
    }

    public Iterable<? extends TypeDescriptor> getSelectedTypes(final boolean visible, String initSearchText) {
        Iterable<? extends TypeDescriptor> result = Collections.emptyList();
        try {
            panel = new GoToPanel(this, multiSelection);
            dialog = createDialog(panel);

            if (initSearchText != null) {
                panel.setInitialText(initSearchText);
            } else {
                Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
                if (arr.length > 0) {
                    EditorCookie ec = arr[0].getCookie (EditorCookie.class);
                    if (ec != null) {
                        JEditorPane recentPane = NbDocument.findRecentEditorPane(ec);
                        if (recentPane != null) {
                            initSearchText = org.netbeans.editor.Utilities.getSelectionOrIdentifier(recentPane);
                            if (initSearchText != null && org.openide.util.Utilities.isJavaIdentifier(initSearchText)) {
                                panel.setInitialText(initSearchText);
                            } else {
                                panel.setInitialText(arr[0].getName());
                            }
                        }
                    }
                }
            }
            
            dialog.setVisible(visible);
            result = panel.getSelectedTypes();

        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return result;
    }
    
    @Override
    public boolean isEnabled () {
        return OpenProjects.getDefault().getOpenProjects().length>0;
    }
    
    @Override
    public boolean accept(Object obj) {
        return typeFilter == null ? true : typeFilter.accept((TypeDescriptor) obj);
    }
    
    @Override
    public void scheduleUpdate(Runnable run) {
        SwingUtilities.invokeLater(run);
    }
    
    // Implementation of content provider --------------------------------------
    
    
    @Override
    public ListCellRenderer getListCellRenderer(
            @NonNull final JList list,
            @NonNull final ButtonModel caseSensitive) {
        Parameters.notNull("list", list);   //NOI18N
        Parameters.notNull("caseSensitive", caseSensitive); //NOI18N
        return new Renderer(list, caseSensitive);
    }
    
    
    @Override
    public void setListModel( GoToPanel panel, String text ) {
        assert SwingUtilities.isEventDispatchThread();
        if (okButton != null) {
            okButton.setEnabled (false);
        } 
        //handling http://netbeans.org/bugzilla/show_bug.cgi?id=178555
        //add a MouseListener to the messageLabel JLabel so that the search can be cancelled without exiting the dialog
        final GoToPanel goToPanel = panel;
        final MouseListener warningMouseListener = new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (running != null) {
                    running.cancel();
                    task.cancel();
                    running = null;
                }
                goToPanel.setListPanelContent(NbBundle.getMessage(GoToPanel.class, "TXT_SearchCanceled"),false); // NOI18N
            }
        };
        panel.setMouseListener(warningMouseListener);
        if ( running != null ) {
            running.cancel();
            task.cancel();
            running = null;
        }
        
        if ( text == null ) {
            panel.setModel(EMPTY_LIST_MODEL, -1);
            return;
        }
        
        boolean exact = text.endsWith(" "); // NOI18N
        
        text = text.trim();
        
        if ( text.length() == 0) {
            panel.setModel(EMPTY_LIST_MODEL, -1);
            return;
        }
        
        int wildcard = containsWildCard(text);
                
        if (exact) {
            nameKinds = Collections.singleton(
                panel.isCaseSensitive() ?
                    SearchType.EXACT_NAME :
                    SearchType.CASE_INSENSITIVE_EXACT_NAME);
        } else if ((isAllUpper(text) && text.length() > 1) || isCamelCase(text)) {
            nameKinds = Arrays.asList(
                new SearchType[] {
                    SearchType.CAMEL_CASE,
                    panel.isCaseSensitive() ?
                        SearchType.PREFIX :
                        SearchType.CASE_INSENSITIVE_PREFIX});
        } else if (wildcard != -1) {
            nameKinds = Collections.singleton(
                panel.isCaseSensitive() ?
                    SearchType.REGEXP :
                    SearchType.CASE_INSENSITIVE_REGEXP);
        } else {
            nameKinds = Collections.singleton(
                panel.isCaseSensitive() ?
                    SearchType.PREFIX :
                    SearchType.CASE_INSENSITIVE_PREFIX);
        }
        
        // Compute in other thread        
        running = new Worker( text , panel.isCaseSensitive(), panel.getTextId());
        task = rp.post( running, 220);
        if ( panel.time != -1 ) {
            LOGGER.log( Level.FINE, "Worker posted after {0} ms.", System.currentTimeMillis() - panel.time ); //NOI18N
        }
    }
    
    @Override
    public void closeDialog() {
        // Closing event can be sent several times.
        if (dialog == null ) { // #172568
            return; // OK - the dialog has already been closed.
        }
        dialog.setVisible( false );
        cleanup();
    }
    
    @Override
    public boolean hasValidContent () {
        return this.okButton != null && this.okButton.isEnabled();
    }
    
    // Private methods ---------------------------------------------------------
        
    public static boolean isAllUpper( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( !Character.isUpperCase( text.charAt( i ) ) ) {
                return false;
            }
        }
        
        return true;
    }
    
    public static int containsWildCard( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( text.charAt( i ) == '?' || text.charAt( i ) == '*' ) { // NOI18N
                return i;                
            }
        }        
        return -1;
    }
    
    private static Pattern camelCasePattern = Pattern.compile("(?:\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\p{Digit}|\\.|\\$)*){2,}"); // NOI18N
    
    public static boolean isCamelCase(String text) {
         return camelCasePattern.matcher(text).matches();
    }
    
    
    /** Creates the dialog to show
     */
   private Dialog createDialog( final GoToPanel panel) {
       
        okButton = new JButton (NbBundle.getMessage(GoToTypeAction.class, "CTL_OK"));
        okButton.getAccessibleContext().setAccessibleDescription(okButton.getText());
        okButton.setEnabled (false);
        panel.getAccessibleContext().setAccessibleName( NbBundle.getMessage( GoToTypeAction.class, "AN_GoToType") ); //NOI18N
        panel.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( GoToTypeAction.class, "AD_GoToType") ); //NOI18N
                        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            panel,                             // innerPane
            title, // displayName
            true,
            new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
            okButton,
            DialogDescriptor.DEFAULT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new DialogButtonListener( panel ) );                                 // Action listener
        
         dialogDescriptor.setClosingOptions(new Object[] {okButton, DialogDescriptor.CANCEL_OPTION});
            
        // panel.addPropertyChangeListener( new HelpCtxChangeListener( dialogDescriptor, helpCtx ) );
//        if ( panel instanceof HelpCtx.Provider ) {
//            dialogDescriptor.setHelpCtx( ((HelpCtx.Provider)panel).getHelpCtx() );
//        }
        
        Dialog d = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        
        // Set size when needed
        final int width = UiOptions.GoToTypeDialog.getWidth();
        final int height = UiOptions.GoToTypeDialog.getHeight();
        if (width != -1 && height != -1) {
            d.setPreferredSize(new Dimension(width,height));
        }
        
        // Center the dialog after the size changed.
        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * 9) / 10;
        int maxH = (r.height * 9) / 10;
        final Dimension dim = d.getPreferredSize();
        dim.width = Math.min(dim.width, maxW);
        dim.height = Math.min(dim.height, maxH);
        d.setBounds(Utilities.findCenterBounds(dim));
        initialDimension = dim;
        d.addWindowListener(new WindowAdapter() {
            public @Override void windowClosed(WindowEvent e) {
                cleanup();
            }
        });
        
        return d;

    } 
   
    private Dimension initialDimension;
    
    private void cleanup() {    
        assert SwingUtilities.isEventDispatchThread();
        if ( GoToTypeAction.this.dialog != null ) { // Closing event for some reson sent twice
        
            // Save dialog size only when changed
            final int currentWidth = dialog.getWidth();
            final int currentHeight = dialog.getHeight();
            if (initialDimension != null && (initialDimension.width != currentWidth || initialDimension.height != currentHeight)) {
                UiOptions.GoToTypeDialog.setHeight(currentHeight);
                UiOptions.GoToTypeDialog.setWidth(currentWidth);
            }
            initialDimension = null;
            // Clean caches
            GoToTypeAction.this.dialog.dispose();
            GoToTypeAction.this.dialog = null;                                    
            //1st) Cancel current task
            if ( running != null ) {
                running.cancel();
                task.cancel();
                running = null;
            }
            //2nd do clean up in the same thread as init to prevent races
            rp.submit(new Runnable(){
                @Override
                public void run() {
                    assert rp.isRequestProcessorThread();
                    if (typeProviders != null) {
                        for (TypeProvider provider : typeProviders) {
                            provider.cleanup();
                        }
                        typeProviders = null;
                    }
                }
            });            
        }
    }
    
    // Private classes ---------------------------------------------------------
       
    
    
    private class Worker implements Runnable {
        
        private volatile boolean isCanceled = false;
        private volatile TypeProvider current;
        private final String text;
        private final boolean caseSensitive;        
        private final long createTime;

        private int lastSize = -1;
        private final int textId;
        
        public Worker( String text, final boolean caseSensitive, int textId) {
            this.text = text;
            this.caseSensitive = caseSensitive;
            this.createTime = System.currentTimeMillis();
            this.textId = textId;
            LOGGER.log( Level.FINE, "Worker for {0} - created after {1} ms.",   //NOI18N
                    new Object[]{
                        text,
                        System.currentTimeMillis() - panel.time
                    });
        }

        @Override
        public void run() {            
            final Future<?> f = OpenProjects.getDefault().openProjects();
            if (!f.isDone()) {
                try {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            panel.updateMessage(NbBundle.getMessage(GoToTypeAction.class, "TXT_LoadingProjects"));
                        }
                    });
                    f.get();
                } catch (InterruptedException ex) {
                    LOGGER.fine(ex.getMessage());
                } catch (ExecutionException ex) {
                    LOGGER.fine(ex.getMessage());
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            panel.updateMessage(NbBundle.getMessage(GoToTypeAction.class, "TXT_Searching"));
                        }
                    });
                }
            }
            for (;;) {
                final int[] retry = new int[1];

                Profile profile = initializeProfiling();
                try {
                    LOGGER.log( Level.FINE, "Worker for {0} - started {1} ms.",     //NOI18N
                            new Object[]{
                                text,
                                System.currentTimeMillis() - createTime
                            });

                    final List<? extends TypeDescriptor> types = getTypeNames(text, retry);
                    if ( isCanceled ) {
                        LOGGER.log( Level.FINE, "Worker for {0} exited after cancel {1} ms.",   //NOI18N
                                new Object[]{
                                    text,
                                    System.currentTimeMillis() - createTime
                                });
                        return;
                    }
                    final int newSize = types.size();
                    //Optimistic the types just added, but safer is compare the collections.
                    //Unfortunatelly no TypeDescriptor impl provides equals.
                    if (lastSize != newSize) {
                        lastSize = newSize;
                        ListModel model = Models.fromList(types);
                        if (typeFilter != null) {
                            model = LazyListModel.create(model, GoToTypeAction.this, 0.1, "Not computed yet");
                        }
                        final ListModel fmodel = model;
                        if ( isCanceled ) {
                            LOGGER.log( Level.FINE, "Worker for {0} exited after cancel {1} ms.",   //NOI18N
                                    new Object[]{
                                        text,
                                        System.currentTimeMillis() - createTime
                                    });
                            return;
                        }

                        if ( !isCanceled && fmodel != null ) {
                            LOGGER.log( Level.FINE, "Worker for text {0} finished after {1} ms.",   //NOI18N
                                    new Object[]{
                                        text,
                                        System.currentTimeMillis() - createTime
                                    });
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    panel.setModel(fmodel, textId);
                                    if (okButton != null && !types.isEmpty()) {
                                        okButton.setEnabled (true);
                                    }
                                }
                            });
                        }
                    }
                } finally {
                    if (profile != null) {
                        try {
                            profile.stop();
                        } catch (Exception ex) {
                            LOGGER.log(Level.INFO, "Cannot stop profiling", ex);    //NOI18N
                        }
                    }
                }

                if (retry[0] > 0) {
                    try {
                        Thread.sleep(retry[0]);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    return;
                }
            } // for
        }
        
        public void cancel() {
            if ( panel.time != -1 ) {
                LOGGER.log( Level.FINE, "Worker for text {0} canceled after {1} ms.",   //NOI18N
                        new Object[]{
                            text,
                            System.currentTimeMillis() - createTime
                        });
            }
            TypeProvider _provider;
            synchronized (this) {
                isCanceled = true;
                _provider = current;
            }
            if (_provider != null) {
                _provider.cancel();
            }
        }

        @SuppressWarnings("unchecked")
        private List<? extends TypeDescriptor> getTypeNames(String text, int[] retry) {
            // TODO: Search twice, first for current project, then for all projects
            final Set<TypeDescriptor> items = new HashSet<TypeDescriptor>();
            final String[] message = new String[1];
            final Collection<TypeProvider.Context> contexts = new HashSet<TypeProvider.Context>(2);
            for (SearchType nameKind : nameKinds) {
                contexts.add(TypeProviderAccessor.DEFAULT.createContext(null, text, nameKind));
            }
            assert !contexts.isEmpty();
            assert rp.isRequestProcessorThread();
            if (typeProviders == null) {
                typeProviders = implicitTypeProviders != null ? implicitTypeProviders : Lookup.getDefault().lookupAll(TypeProvider.class);
            }
            for (TypeProvider provider : typeProviders) {
                if (isCanceled) {
                    return null;
                }
                current = provider;
                final TypeProvider.Result result = TypeProviderAccessor.DEFAULT.createResult(items, message, contexts.iterator().next());
                long start = System.currentTimeMillis();
                try {
                    LOGGER.log(Level.FINE, "Calling TypeProvider: {0}", provider);  //NOI18N
                    for (TypeProvider.Context context : contexts) {
                        provider.computeTypeNames(context, result);
                    }
                } finally {
                    current = null;
                }
                long delta = System.currentTimeMillis() - start;
                LOGGER.log(Level.FINE, "Provider ''{0}'' took {1} ms.",     //NOI18N
                        new Object[]{
                            provider.getDisplayName(),
                            delta
                        });
                retry[0] = mergeRetryTimeOut(
                    retry[0],
                    TypeProviderAccessor.DEFAULT.getRetry(result));
            }
            if ( !isCanceled ) {
                final ArrayList<TypeDescriptor> result = new ArrayList<TypeDescriptor>(items);
                Collections.sort(result, new TypeComparator(caseSensitive));
                panel.setWarning(message[0]);                
                return result;
            }
            else {
                return null;
            }            
        }

        private int mergeRetryTimeOut(
            int t1,
            int t2) {
            if (t1 == 0) {
                return t2;
            }
            if (t2 == 0) {
                return t1;
            }
            return Math.min(t1,t2);
        }
    }

    private static class MyPanel extends JPanel {
	
	private TypeDescriptor td;
	
	void setDescriptor(TypeDescriptor td) {
	    this.td = td;
	    // since the same component is reused for dirrerent list itens, 
	    // null the tool tip
	    putClientProperty(TOOL_TIP_TEXT_KEY, null);
	}

	@Override
	public String getToolTipText() {
	    // the tool tip is gotten from the descriptor 
	    // and cached in the standard TOOL_TIP_TEXT_KEY property
	    String text = (String) getClientProperty(TOOL_TIP_TEXT_KEY);
	    if( text == null ) {
                if( td != null ) {
                    text = td.getFileDisplayPath();
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }

    final void waitSearchFinished() {
        assert SwingUtilities.isEventDispatchThread();
        task.waitFinished();
    }

    private static final class Renderer extends EntitiesListCellRenderer implements ActionListener {
         
        private MyPanel rendererComponent;
        private JLabel jlName = HtmlRenderer.createLabel();
        private JLabel jlPkg = new JLabel();
        private JLabel jlPrj = new JLabel();
        private int DARKER_COLOR_COMPONENT = 5;
        private int LIGHTER_COLOR_COMPONENT = 80;        
        private Color fgColor;
        private Color fgColorLighter;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;
        
        private JList jList;
        private boolean caseSensitive;
        private final HighlightingNameFormatter typeNameFormatter;

        @SuppressWarnings("LeakingThisInConstructor")
        public Renderer(
                @NonNull final JList list,
                @NonNull final ButtonModel caseSensitive) {
            
            jList = list;
            this.caseSensitive = caseSensitive.isSelected();
            resetName();
            Container container = list.getParent();
            if ( container instanceof JViewport ) {
                ((JViewport)container).addChangeListener(this);
                stateChanged(new ChangeEvent(container));
            }
            
            rendererComponent = new MyPanel();
            rendererComponent.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (0,0,0,7);
            rendererComponent.add( jlName, c);
            
            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets (0,0,0,7);
            rendererComponent.add( jlPkg, c);
            
            c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.anchor = GridBagConstraints.EAST;
            rendererComponent.add( jlPrj, c);
            
            
            jlPkg.setOpaque(false);
            jlPrj.setOpaque(false);                        

            jlPkg.setFont(list.getFont());
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
            this.typeNameFormatter = HighlightingNameFormatter.createBoldFormatter();
            caseSensitive.addActionListener(this);
            jlName.setOpaque( true );
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
                jlName.setBackground( bgSelectionColor );
                jlPkg.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);                
                rendererComponent.setBackground(bgSelectionColor);
            }
            else {
                jlName.setForeground(fgColor);
                jlName.setBackground( bgColor );
                jlPkg.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }
            
            if ( value instanceof TypeDescriptor ) {
                long time = System.currentTimeMillis();
                TypeDescriptor td = (TypeDescriptor)value;                
                jlName.setIcon(td.getIcon());
                //highlight matching search text patterns in type
                final String formattedTypeName = typeNameFormatter.formatName(
                        td.getTypeName(),
                        TypeProviderAccessor.DEFAULT.getHighlightText(td),
                        caseSensitive,
                        isSelected? fgSelectionColor : fgColor);
                jlName.setText(formattedTypeName);
                jlPkg.setText(td.getContextName());
                setProjectName(jlPrj, td.getProjectName());
                jlPrj.setIcon(td.getProjectIcon());
		rendererComponent.setDescriptor(td);                
                LOGGER.log(Level.FINE, "  Time in paint {0} ms.", System.currentTimeMillis() - time);   //NOI18N
            }
            else {
                jlName.setText( value.toString() );
            }
            
            return rendererComponent;
        }
        
        @Override
        public void stateChanged(ChangeEvent event) {
            
            JViewport jv = (JViewport)event.getSource();
            
            jlName.setText( "Sample" ); // NOI18N
            //jlName.setIcon(UiUtils.getElementIcon(ElementKind.CLASS, null));
            jlName.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/type/sample.png", false)); //NOI18N
            
            jList.setFixedCellHeight(jlName.getPreferredSize().height);
            jList.setFixedCellWidth(jv.getExtentSize().width);
        }
        
        @Override
        public void actionPerformed(@NonNull final ActionEvent e) {
            caseSensitive = ((ButtonModel)e.getSource()).isSelected();
        }

        private void resetName() {
            ((HtmlRenderer.Renderer)jlName).reset();
            jlName.setFont(jList.getFont());
            jlName.setOpaque(true);
            ((HtmlRenderer.Renderer)jlName).setHtml(true);
            ((HtmlRenderer.Renderer)jlName).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
        }

     } // Renderer
    
    private class DialogButtonListener implements ActionListener {
        
        private GoToPanel panel;
        
        public DialogButtonListener( GoToPanel panel ) {
            this.panel = panel;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {            
            if ( e.getSource() == okButton) {
                panel.setSelectedTypes();
            }
        }
        
    }


    private Profile initializeProfiling() {
        boolean assertsOn = false;
        assert assertsOn = true;
        if (!assertsOn) {
            return null;
        }

        Sampler profiler = Sampler.createSampler("jumpto"); //NOI18N
        if (profiler == null) {
            return null;
        }
        return new Profile(profiler).start();
    }

    private class Profile implements Runnable {
        private final long time;
        private volatile  Sampler profiler;
        private volatile boolean profiling;

        public Profile(Sampler profiler) {
            time = System.currentTimeMillis();
            this.profiler = profiler;
        }
        
        Profile start() {
            PROFILE_RP.post(this, 3000); // 3s
            return this;
        }

        @Override
        public synchronized void run() {
            if (profiler != null) {
                profiling = true;
                profiler.start();
            }
        }

        private synchronized void stop() throws Exception {
            long delta = System.currentTimeMillis() - time;

            Sampler ss = profiler;
            profiler = null;
            if (!profiling) {
                return;
            }
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                ss.stopAndWriteTo(dos);
                dos.close();
                if (dos.size() > 0) {
                    Object[] params = new Object[]{out.toByteArray(), delta, "GoToType" };      //NOI18N
                    Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params); //NOI18N
                } else {
                    LOGGER.log(Level.WARNING, "no snapshot taken"); // NOI18N
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

}

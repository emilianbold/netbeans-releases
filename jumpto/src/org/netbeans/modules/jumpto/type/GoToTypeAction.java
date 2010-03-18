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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.jumpto.type;

import org.netbeans.api.project.Project;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.jumpto.type.TypeBrowser;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jumpto.file.LazyListModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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
    
    private SearchType nameKind;
    private static ListModel EMPTY_LIST_MODEL = new DefaultListModel();
    private static final RequestProcessor rp = new RequestProcessor ("GoToTypeAction-RequestProcessor",1);
    private Worker running;
    private RequestProcessor.Task task;
    GoToPanel panel;
    private Dialog dialog;
    private JButton okButton;
    private Collection<? extends TypeProvider> typeProviders;
    private final TypeBrowser.Filter typeFilter;
    private final String title;

    /** Creates a new instance of OpenTypeAction */
    public GoToTypeAction() {
        this(
            NbBundle.getMessage( GoToTypeAction.class, "DLG_GoToType" ),
            null
        );
    }
    
    public GoToTypeAction(String title, TypeBrowser.Filter typeFilter, TypeProvider... typeProviders) {
        super( NbBundle.getMessage( GoToTypeAction.class,"TXT_GoToType") );
        putValue("PopupMenuText", NbBundle.getBundle(GoToTypeAction.class).getString("editor-popup-TXT_GoToType")); // NOI18N
        this.title = title;
        this.typeFilter = typeFilter;
        this.typeProviders = typeProviders.length == 0 ? null : Arrays.asList(typeProviders);
    }
    
    public void actionPerformed( ActionEvent e ) {
        TypeDescriptor typeDescriptor = getSelectedType();
        if (typeDescriptor != null) {
            typeDescriptor.open();
        }
    }
            
    public TypeDescriptor getSelectedType() {
        return getSelectedType(true);
    }
    
    public TypeDescriptor getSelectedType(final boolean visible) {
        TypeDescriptor result = null;
        try {
            panel = new GoToPanel(this);
            dialog = createDialog(panel);
            
            Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
            String initSearchText = null;
            if (arr.length > 0) {
                EditorCookie ec = arr[0].getCookie (EditorCookie.class);
                if (ec != null) {
                    JEditorPane[] openedPanes = ec.getOpenedPanes ();
                    if (openedPanes != null) {
                        initSearchText = org.netbeans.editor.Utilities.getSelectionOrIdentifier(openedPanes [0]);
                        if (initSearchText != null && org.openide.util.Utilities.isJavaIdentifier(initSearchText)) {
                            panel.setInitialText(initSearchText);
                        }
                    }
                }
            }            
            
            dialog.setVisible(visible);
            result = panel.getSelectedType();

        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return result;
    }
    
    @Override
    public boolean isEnabled () {
        return OpenProjects.getDefault().getOpenProjects().length>0;
    }
    
    public boolean accept(Object obj) {
        return typeFilter == null ? true : typeFilter.accept((TypeDescriptor) obj);
    }
    
    public void scheduleUpdate(Runnable run) {
        SwingUtilities.invokeLater(run);
    }
    
    // Implementation of content provider --------------------------------------
    
    
    public ListCellRenderer getListCellRenderer( JList list ) {
        return new Renderer( list );        
    }
    
    
    public void setListModel( GoToPanel panel, String text ) {
        if (okButton != null) {
            okButton.setEnabled (false);
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
        
        int wildcard = containsWildCard(text);
                
        if (exact) {
            //nameKind = panel.isCaseSensitive() ? SearchType.EXACT_NAME : SearchType.CASE_INSENSITIVE_EXACT_NAME;
            nameKind = SearchType.EXACT_NAME;
        }
        else if ((isAllUpper(text) && text.length() > 1) || isCamelCase(text)) {
            nameKind = SearchType.CAMEL_CASE;
        }
        else if (wildcard != -1) {
            nameKind = panel.isCaseSensitive() ? SearchType.REGEXP : SearchType.CASE_INSENSITIVE_REGEXP;                
        }
        else {            
            nameKind = panel.isCaseSensitive() ? SearchType.PREFIX : SearchType.CASE_INSENSITIVE_PREFIX;
        }
        
        // Compute in other thread
        
        synchronized( this ) {
            running = new Worker( text );
            task = rp.post( running, 220);
            if ( panel.time != -1 ) {
                LOGGER.fine( "Worker posted after " + ( System.currentTimeMillis() - panel.time ) + " ms."  );                
            }
        }
    }
    
    public void closeDialog() {
        // Closing event can be sent several times.
        if (dialog == null ) { // #172568
            return; // OK - the dialog has already been closed.
        }
        dialog.setVisible( false );
        cleanup();
    }
    
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
        //System.out.println("CLEANUP");                
        //Thread.dumpStack();

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
            //GoToTypeAction.this.cache = null;
            if (typeProviders != null) {
                for (TypeProvider provider : typeProviders) {
                    provider.cleanup();
                }
            }
        }
    }
    
    // Private classes ---------------------------------------------------------
       
    
    
    private class Worker implements Runnable {
        
        private volatile boolean isCanceled = false;
        private volatile TypeProvider current;
        private final String text;
        
        private final long createTime;
        
        public Worker( String text ) {
            this.text = text;
            this.createTime = System.currentTimeMillis();
            LOGGER.fine( "Worker for " + text + " - created after " + ( System.currentTimeMillis() - panel.time ) + " ms."  );                
        }

        public void run() {
            for (;;) {
                final int[] retry = new int[1];

                Profile profile = initializeProfiling();
                try {
                    LOGGER.fine( "Worker for " + text + " - started " + ( System.currentTimeMillis() - createTime ) + " ms."  );

                    final List<? extends TypeDescriptor> types = getTypeNames(text, retry);
                    if ( isCanceled ) {
                        LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );
                        return;
                    }
                    ListModel model = Models.fromList(types);
                    if (typeFilter != null) {
                        model = LazyListModel.create(model, GoToTypeAction.this, 0.1, "Not computed yet");
                    }
                    final ListModel fmodel = model;
                    if ( isCanceled ) {
                        LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );
                        return;
                    }

                    if ( !isCanceled && fmodel != null ) {
                        LOGGER.fine( "Worker for text " + text + " finished after " + ( System.currentTimeMillis() - createTime ) + " ms."  );
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                panel.setModel(fmodel);
                                if (okButton != null && !types.isEmpty()) {
                                    okButton.setEnabled (true);
                                }
                            }
                        });
                    }
                } finally {
                    if (profile != null) {
                        try {
                            profile.stop();
                        } catch (Exception ex) {
                            LOGGER.log(Level.INFO, "Cannot stop profiling", ex);
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
                LOGGER.fine( "Worker for text " + text + " canceled after " + ( System.currentTimeMillis() - createTime ) + " ms."  );                
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
            List<TypeDescriptor> items;
            // Multiple providers: merge results
            items = new ArrayList<TypeDescriptor>(128);
            String[] message = new String[1];
            TypeProvider.Context context = TypeProviderAccessor.DEFAULT.createContext(null, text, nameKind);
            TypeProvider.Result result = TypeProviderAccessor.DEFAULT.createResult(items, message);
            if (typeProviders == null) {
                typeProviders = Lookup.getDefault().lookupAll(TypeProvider.class);
            }
            for (TypeProvider provider : typeProviders) {
                if (isCanceled) {
                    return null;
                }
                current = provider;
                long start = System.currentTimeMillis();
                try {
                    LOGGER.fine("Calling TypeProvider: " + provider);
                    provider.computeTypeNames(context, result);
                } finally {
                    current = null;
                }
                long delta = System.currentTimeMillis() - start;
                LOGGER.fine("Provider '" + provider.getDisplayName() + "' took " + delta + " ms.");
            }
            retry[0] = TypeProviderAccessor.DEFAULT.getRetry(result);
            if ( !isCanceled ) {   
                //time = System.currentTimeMillis();
                Collections.sort(items, new TypeComparator());
                panel.setWarning(message[0]);
                //sort += System.currentTimeMillis() - time;
                //LOGGER.fine("PERF - " + " GSS:  " + gss + " GSB " + gsb + " CP: " + cp + " SFB: " + sfb + " GTN: " + gtn + "  ADD: " + add + "  SORT: " + sort );
                return items;
            }
            else {
                return null;
            }
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
                    FileObject fo = td.getFileObject();
                    if (fo != null) {
                        text = FileUtil.getFileDisplayName(fo);
                    }
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }

    final void waitSearchFinished() {
        task.waitFinished();
    }

    private static class Renderer extends DefaultListCellRenderer implements ChangeListener {
         
        private MyPanel rendererComponent;
        private JLabel jlName = new JLabel();
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
        
        public Renderer( JList list ) {
            
            jList = list;
            
            Container container = list.getParent();
            if ( container instanceof JViewport ) {
                ((JViewport)container).addChangeListener(this);
                stateChanged(new ChangeEvent(container));
            }
            
            rendererComponent = new MyPanel();
            rendererComponent.setLayout(new BorderLayout());
            rendererComponent.add( jlName, BorderLayout.WEST );
            rendererComponent.add( jlPkg, BorderLayout.CENTER);
            rendererComponent.add( jlPrj, BorderLayout.EAST );
            
            
            jlName.setOpaque(false);
            jlPkg.setOpaque(false);
            jlPrj.setOpaque(false);
            
            jlName.setFont(list.getFont());
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
                            
            bgColor = list.getBackground();
            bgColorDarker = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();        
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
                jlPkg.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);
                rendererComponent.setBackground(bgSelectionColor);
            }
            else {
                jlName.setForeground(fgColor);
                jlPkg.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);                
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }
            
            if ( value instanceof TypeDescriptor ) {
                long time = System.currentTimeMillis();
                TypeDescriptor td = (TypeDescriptor)value;                
                jlName.setIcon(td.getIcon());
                jlName.setText(td.getTypeName());
                jlPkg.setText(td.getContextName());
                jlPrj.setText(td.getProjectName());
                jlPrj.setIcon(td.getProjectIcon());
		rendererComponent.setDescriptor(td);
//                FileObject fo = td.getFileObject();
//                if (fo != null) {
//                    rendererComponent.setToolTipText( FileUtil.getFileDisplayName(fo));
//                }
                LOGGER.fine("  Time in paint " + (System.currentTimeMillis() - time) + " ms.");
            }
            else {
                jlName.setText( value.toString() );
            }
            
            return rendererComponent;
        }
        
        public void stateChanged(ChangeEvent event) {
            
            JViewport jv = (JViewport)event.getSource();
            
            jlName.setText( "Sample" ); // NOI18N
            //jlName.setIcon(UiUtils.getElementIcon(ElementKind.CLASS, null));
            jlName.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/type/sample.png", false));
            
            jList.setFixedCellHeight(jlName.getPreferredSize().height);
            jList.setFixedCellWidth(jv.getExtentSize().width);
        }

     }
    
    private class DialogButtonListener implements ActionListener {
        
        private GoToPanel panel;
        
        public DialogButtonListener( GoToPanel panel ) {
            this.panel = panel;
        }
        
        public void actionPerformed(ActionEvent e) {            
            if ( e.getSource() == okButton) {
                panel.setSelectedType();
            }
        }
        
    }

    private class TypeComparator implements Comparator<TypeDescriptor> {
        public int compare(TypeDescriptor t1, TypeDescriptor t2) {
            Project mainProject = OpenProjects.getDefault().getMainProject();
            String mainProjectname = null;
            if (mainProject != null) {
                mainProjectname = ProjectUtils.getInformation(mainProject).getDisplayName();
            }
            String t1Name = t1.getTypeName();
            String t2Name = t2.getTypeName();

            // if names are equal, show these from main project first
            if (t1Name.equals(t2Name)) {
                String t1projectName = t1.getProjectName();
                String t2projectName = t2.getProjectName();
                if (t1projectName != null && t2projectName != null) {
                    // prioritize types from main project
                    if (mainProjectname != null && t1projectName.equals(mainProjectname)) {
                        return -1;
                    }

                    //prioritize types from any project
                    if (!t1projectName.equals("") && t2projectName.equals("")) { // NOI18N
                        return -1;
                    } else {
                        return +1;
                    }
                }

                
            }

           int cmpr = compareStrings( t1Name, t2Name);
           if ( cmpr != 0 ) {
               return cmpr;
           }
           cmpr = compareStrings( t1.getOuterName(), t2.getOuterName() );
           if ( cmpr != 0 ) {
               return cmpr;
           }
           return compareStrings( t1.getContextName(), t2.getContextName() );
        }
        
    }
        
    private int compareStrings(String s1, String s2) {
        if( s1 == null ) {
            s1 = ""; // NOI18N
        }
        if ( s2 == null ) {
            s2 = ""; // NOI18N
        }
        return s1.compareTo( s2 );
    }
    
    private Profile initializeProfiling() {
        FileObject fo = FileUtil.getConfigFile("Actions/Profile/org-netbeans-modules-profiler-actions-SelfSamplerAction.instance");
        if (fo == null) {
            return null;
        }
        Action a = (Action)fo.getAttribute("delegate"); // NOI18N
        if (a == null) {
            return null;
        }
        Object profiler = a.getValue("logger-jumpto"); //NOI18N
        if (profiler == null) {
            return null;
        }
        return new Profile(profiler);
    }

    private class Profile implements Runnable {
        Object profiler;
        boolean profiling;
        private final long time;

        public Profile(Object profiler) {
            time = System.currentTimeMillis();
            this.profiler = profiler;
            RequestProcessor.getDefault().post(this, 3000); // 3s
        }

        public synchronized void run() {
            profiling = true;
            if (profiler instanceof Runnable) {
                Runnable r = (Runnable)profiler;
                r.run();
            }
        }

        private synchronized void stop() throws Exception {
            long delta = System.currentTimeMillis() - time;

            ActionListener ss = (ActionListener)profiler;
            profiler = null;
            if (!profiling) {
                return;
            }
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                ss.actionPerformed(new ActionEvent(dos, 0, "write")); // NOI18N
                dos.close();
                if (dos.size() > 0) {
                    Object[] params = new Object[]{out.toByteArray(), delta, "GoToType" };
                    Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params);
                } else {
                    LOGGER.log(Level.WARNING, "no snapshot taken"); // NOI18N
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

}

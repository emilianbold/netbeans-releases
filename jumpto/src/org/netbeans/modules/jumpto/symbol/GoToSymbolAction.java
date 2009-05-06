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

package org.netbeans.modules.jumpto.symbol;

import java.awt.Insets;
import java.util.regex.Pattern;
import org.netbeans.modules.jumpto.type.Models;
import org.netbeans.modules.jumpto.type.UiOptions;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
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
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jumpto.file.LazyListModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/** 
 * @author Petr Hrebejk
 */
public class GoToSymbolAction extends AbstractAction implements GoToPanel.ContentProvider, LazyListModel.Filter {
    
    static final Logger LOGGER = Logger.getLogger(GoToSymbolAction.class.getName()); // Used from the panel as well
    
    private SearchType nameKind;
    private static ListModel EMPTY_LIST_MODEL = new DefaultListModel();
    private static final RequestProcessor rp = new RequestProcessor ("GoToSymbolAction-RequestProcessor",1);    //NOI18N
    private Worker running;
    private RequestProcessor.Task task;
    private GoToPanel panel;
    private Dialog dialog;
    private JButton okButton;
    private Collection<? extends SymbolProvider> typeProviders;    
    private final String title;

    /** Creates a new instance of OpenTypeAction */
    public GoToSymbolAction() {
        this(NbBundle.getMessage( GoToSymbolAction.class, "DLG_GoToSymbol"));
    }
    
    public GoToSymbolAction(String title) {
        super( NbBundle.getMessage( GoToSymbolAction.class,"TXT_GoToSymbol")  );
        this.title = title;
    }
    
    private Collection<? extends SymbolProvider> getTypeProviders() {
        if (typeProviders==null) {
            typeProviders = Arrays.asList(Lookup.getDefault().lookupAll(SymbolProvider.class).toArray(new SymbolProvider[0]));
        }
        return typeProviders;
    }
    
    public void actionPerformed( ActionEvent e ) {
        SymbolDescriptor typeDescriptor = getSelectedSymbol();
        if (typeDescriptor != null) {
            typeDescriptor.open();
        }
    }
            
    public SymbolDescriptor getSelectedSymbol() {
        SymbolDescriptor result = null;
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
            
            dialog.setVisible(true);
            result = panel.getSelectedSymbol();

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
        return true;
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
        final boolean isCaseSensitive = panel.isCaseSensitive();
        boolean exact = text.endsWith(" "); // NOI18N        
        text = text.trim();        
        if ( text.length() == 0) {
            panel.setModel(EMPTY_LIST_MODEL);
            return;
        }        
        int wildcard = containsWildCard(text);
        
        if (exact) {
            //nameKind = isCaseSensitive ? SearchType.EXACT_NAME : SearchType.CASE_INSENSITIVE_EXACT_NAME;
            nameKind = SearchType.EXACT_NAME;
        }
        else if ((isAllUpper(text) && text.length() > 1) || isCamelCase(text)) {
            nameKind = SearchType.CAMEL_CASE;
        }
        else if (wildcard != -1) {
            nameKind = isCaseSensitive ? SearchType.REGEXP : SearchType.CASE_INSENSITIVE_REGEXP;
        }
        else {            
            nameKind = isCaseSensitive ? SearchType.PREFIX : SearchType.CASE_INSENSITIVE_PREFIX;
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
    
    private static boolean isAllUpper( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( !Character.isUpperCase( text.charAt( i ) ) ) {
                return false;
            }
        }
        
        return true;
    }
    
    private static int containsWildCard( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( text.charAt( i ) == '?' || text.charAt( i ) == '*' ) { // NOI18N
                return i;                
            }
        }        
        return -1;
    }
    
    private static Pattern camelCasePattern = Pattern.compile("(?:\\p{javaUpperCase}(?:\\p{javaLowerCase}|\\p{Digit}|\\.|\\$)*){2,}"); // NOI18N
    
    private static boolean isCamelCase(String text) {
         return camelCasePattern.matcher(text).matches();
    }
    
    public void closeDialog() {
        dialog.setVisible( false );
        cleanup();
    }
    
    public boolean hasValidContent () {
        return this.okButton != null && this.okButton.isEnabled();
    }
    
    // Private methods ---------------------------------------------------------            
    
    
    /** Creates the dialog to show
     */
   private Dialog createDialog( final GoToPanel panel) {
       
        okButton = new JButton (NbBundle.getMessage(GoToSymbolAction.class, "CTL_OK"));
        okButton.setEnabled (false);
        panel.getAccessibleContext().setAccessibleName( NbBundle.getMessage( GoToSymbolAction.class, "AN_GoToSymbol")  ); //NOI18N
        panel.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( GoToSymbolAction.class, "AD_GoToSymbol")  ); //NOI18N
                        
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
            
        
        Dialog d = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        
        // Set size when needed
        final int width = UiOptions.GoToSymbolDialog.getWidth();
        final int height = UiOptions.GoToSymbolDialog.getHeight();
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
            public void windowClosed(WindowEvent e) {
                cleanup();
            }
        });
        
        return d;

    } 
   
    private Dimension initialDimension;
    
    private void cleanup() {
        //System.out.println("CLEANUP");                
        //Thread.dumpStack();

        if ( GoToSymbolAction.this.dialog != null ) { // Closing event for some reson sent twice
            // Save dialog size only when changed
            final int currentWidth = dialog.getWidth();
            final int currentHeight = dialog.getHeight();
            if (initialDimension != null && (initialDimension.width != currentWidth || initialDimension.height != currentHeight)) {
                UiOptions.GoToSymbolDialog.setHeight(currentHeight);
                UiOptions.GoToSymbolDialog.setWidth(currentWidth);
            }
            initialDimension = null;
            // Clean caches
            GoToSymbolAction.this.dialog.dispose();
            GoToSymbolAction.this.dialog = null;
            //GoToTypeAction.this.cache = null;
            for (SymbolProvider provider : getTypeProviders()) {
                provider.cleanup();
            }
        }
    }
    
    // Private classes ---------------------------------------------------------
       
    
    
    private class Worker implements Runnable {
        
        private volatile boolean isCanceled = false;
        private volatile SymbolProvider current;
        private final String text;
        
        private final long createTime;
        
        public Worker( String text ) {
            this.text = text;
            this.createTime = System.currentTimeMillis();
            LOGGER.fine( "Worker for " + text + " - created after " + ( System.currentTimeMillis() - panel.time ) + " ms."  );                
       }
        
        public void run() {
            
            LOGGER.fine( "Worker for " + text + " - started " + ( System.currentTimeMillis() - createTime ) + " ms."  );                
            
            final List<? extends SymbolDescriptor> types = getSymbolNames( text );
            if ( isCanceled ) {
                LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );                                
                return;
            }
            final ListModel fmodel = Models.fromList(types);
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
            
            
        }
        
        public void cancel() {
            if ( panel.time != -1 ) {
                LOGGER.fine( "Worker for text " + text + " canceled after " + ( System.currentTimeMillis() - createTime ) + " ms."  );                
            }
            SymbolProvider _provider;
            synchronized (this) {
                isCanceled = true;
                _provider = current;
            }
            if (_provider != null) {
                _provider.cancel();
            }
        }

        @SuppressWarnings("unchecked")
        private List<? extends SymbolDescriptor> getSymbolNames(String text) {
            // TODO: Search twice, first for current project, then for all projects
            List<SymbolDescriptor> items;
            // Multiple providers: merge results
            items = new ArrayList<SymbolDescriptor>(128);
            String[] message = new String[1];
            SymbolProvider.Context context = SymbolProviderAccessor.DEFAULT.createContext(null, text, nameKind);
            SymbolProvider.Result result = SymbolProviderAccessor.DEFAULT.createResult(items, message);
            for (SymbolProvider provider : getTypeProviders()) {
                current = provider;
                if (isCanceled) {
                    return null;
                }
                LOGGER.fine("Calling SymbolProvider: " + provider);
                provider.computeSymbolNames(context, result);
                current = null;
            }
            if ( !isCanceled ) {   
                //time = System.currentTimeMillis();
                Collections.sort(items, new SymbolComparator());
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
	
	private SymbolDescriptor td;
	
	void setDescriptor(SymbolDescriptor td) {
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
    
    private static class Renderer extends DefaultListCellRenderer implements ChangeListener {
         
        private MyPanel rendererComponent;
        private JLabel jlName = new JLabel();
        private JLabel jlOwner = new JLabel();
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
            rendererComponent.add( jlOwner, c);
            
            c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;            
            c.anchor = GridBagConstraints.EAST;
            rendererComponent.add( jlPrj, c);
            
            
            jlName.setOpaque(false);
            jlPrj.setOpaque(false);
            
            jlName.setFont(list.getFont());
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
        
        public Component getListCellRendererComponent( JList list,
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
                jlOwner.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);
                rendererComponent.setBackground(bgSelectionColor);
            }
            else {
                jlName.setForeground(fgColor);
                jlOwner.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }
            
            if ( value instanceof SymbolDescriptor ) {
                long time = System.currentTimeMillis();
                SymbolDescriptor td = (SymbolDescriptor)value;                
                jlName.setIcon(td.getIcon());                
                jlName.setText(td.getSymbolName());
                jlOwner.setText(NbBundle.getMessage(GoToSymbolAction.class, "MSG_DeclaredIn",td.getOwnerName()));
                jlPrj.setText(td.getProjectName());
                jlPrj.setIcon(td.getProjectIcon());
		rendererComponent.setDescriptor(td);
                FileObject fo = td.getFileObject();
                if (fo != null) {
                    rendererComponent.setToolTipText( FileUtil.getFileDisplayName(fo));
                }
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
                panel.setSelectedSymbol();
            }
        }
        
    }

    private class SymbolComparator implements Comparator<SymbolDescriptor> {
        public int compare(SymbolDescriptor t1, SymbolDescriptor t2) {
           int cmpr = compareStrings( t1.getSymbolName(), t2.getSymbolName());
           if ( cmpr != 0 ) {
               return cmpr;
           }
           //todo: more logic
           return cmpr;
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
    

}

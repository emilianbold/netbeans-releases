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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jumpto.type;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayList;
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
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/** XXX Icons
 * XXX Don't look for all projects (do it lazy in filter or renderer)
 * @author Petr Hrebejk
 */
public class GoToTypeAction extends AbstractAction implements GoToPanel.ContentProvider {
    
    static final Logger LOGGER = Logger.getLogger(GoToTypeAction.class.getName()); // Used from the panel as well
    
    private SearchType nameKind;
    private static ListModel EMPTY_LIST_MODEL = new DefaultListModel();
    private static final RequestProcessor rp = new RequestProcessor ("GoToTypeAction-RequestProcessor",1);
    private Worker running;
    private RequestProcessor.Task task;
    private GoToPanel panel;
    private Dialog dialog;
    private JButton okButton;
    private Collection<? extends TypeProvider> typeProviders;

    
    /** Creates a new instance of OpenTypeAction */
    public GoToTypeAction() {
        super( NbBundle.getMessage( GoToTypeAction.class,"TXT_GoToType") );
        putValue("PopupMenuText", NbBundle.getBundle(GoToTypeAction.class).getString("editor-popup-TXT_GoToType")); // NOI18N
    }
    
    public void actionPerformed( ActionEvent e ) {
        try {
            typeProviders = Lookup.getDefault().lookupAll(TypeProvider.class);
            
            panel = new GoToPanel( this );
            dialog = createDialog(panel);
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            } );
            
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
            
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    @Override
    public boolean isEnabled () {
        return OpenProjects.getDefault().getOpenProjects().length>0;
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
        
        text = text.trim();
        
        if ( text.length() == 0) {
            panel.setModel(EMPTY_LIST_MODEL);
            return;
        }
        
        if (isAllUpper(text)) {
            nameKind = SearchType.CAMEL_CASE;
        } 
        else if (containsWildCard(text) != -1) {
            if (Character.isJavaIdentifierStart(text.charAt(0))) {
                nameKind = panel.isCaseSensitive() ? SearchType.REGEXP : SearchType.CASE_INSENSITIVE_REGEXP;
            }
            else {
                panel.setModel(EMPTY_LIST_MODEL);
                return;
            }
                
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
        dialog.setVisible( false );
        cleanup();
    }
    
    public boolean hasValidContent () {
        return this.okButton != null && this.okButton.isEnabled();
    }
    
    // Private methods ---------------------------------------------------------
        
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
            if ( text.charAt( i ) == '?' || text.charAt( i ) == '*' ) {
                return i;                
            }
        }        
        return -1;
    }
    
    
    /** Creates the dialog to show
     */
   private Dialog createDialog( final GoToPanel panel) {
       
        okButton = new JButton (NbBundle.getMessage(GoToTypeAction.class, "CTL_OK"));
        okButton.setEnabled (false);
        panel.getAccessibleContext().setAccessibleName( NbBundle.getMessage( GoToTypeAction.class, "AN_GoToType") ); //NOI18N
        panel.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( GoToTypeAction.class, "AD_GoToType") ); //NOI18N
                        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            panel,                             // innerPane
            NbBundle.getMessage( GoToTypeAction.class, "DLG_GoToType" ), // NOI18N // displayName
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
        
        // Set size
        d.setPreferredSize( new Dimension(  UiOptions.GoToTypeDialog.getWidth(),
                                   UiOptions.GoToTypeDialog.getHeight() ) );
        
        // Center the dialog after the size changed.
        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * 9) / 10;
        int maxH = (r.height * 9) / 10;
        Dimension dim = d.getPreferredSize();
        dim.width = Math.min(dim.width, maxW);
        dim.height = Math.min(dim.height, maxH);
        d.setBounds(Utilities.findCenterBounds(dim));
        
        d.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                cleanup();
            }
        });
        
        return d;

    } 
    
    private void cleanup() {
        //System.out.println("CLEANUP");                
        //Thread.dumpStack();

        if ( GoToTypeAction.this.dialog != null ) { // Closing event for some reson sent twice
        
            // Save dialog size     
            UiOptions.GoToTypeDialog.setHeight(dialog.getHeight());
            UiOptions.GoToTypeDialog.setWidth(dialog.getWidth());        
            
            // Clean caches
            GoToTypeAction.this.dialog.dispose();
            GoToTypeAction.this.dialog = null;
            //GoToTypeAction.this.cache = null;
            for (TypeProvider provider : typeProviders) {
                provider.cleanup();
            }
        }
    }
    
    // Private classes ---------------------------------------------------------
       
    
    
    private class Worker implements Runnable {
        
        private volatile boolean isCanceled = false;
        private final String text;
        
        private final long createTime;
        
        public Worker( String text ) {
            this.text = text;
            this.createTime = System.currentTimeMillis();
            LOGGER.fine( "Worker for " + text + " - created after " + ( System.currentTimeMillis() - panel.time ) + " ms."  );                
       }
        
        public void run() {
            
            LOGGER.fine( "Worker for " + text + " - started " + ( System.currentTimeMillis() - createTime ) + " ms."  );                
            
            List<? extends TypeDescriptor> types = getTypeNames( text );
            if ( isCanceled ) {
                LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );                                
                return;
            }
            ListModel model = Models.fromList(types);
            if ( isCanceled ) {            
                LOGGER.fine( "Worker for " + text + " exited after cancel " + ( System.currentTimeMillis() - createTime ) + " ms."  );                                
                return;
            }
            
            if ( !isCanceled && model != null ) {
                LOGGER.fine( "Worker for text " + text + " finished after " + ( System.currentTimeMillis() - createTime ) + " ms."  );                
                
                panel.setModel(model);                
                if (okButton != null && !types.isEmpty()) {
                    okButton.setEnabled (true);
                }
            }
            
            
        }
        
        public void cancel() {
            if ( panel.time != -1 ) {
                LOGGER.fine( "Worker for text " + text + " canceled after " + ( System.currentTimeMillis() - createTime ) + " ms."  );                
            }

            isCanceled = true;
        }

        @SuppressWarnings("unchecked")
        private List<? extends TypeDescriptor> getTypeNames(String text) {
            // TODO: Search twice, first for current project, then for all projects
            List<TypeDescriptor> items;
            if (typeProviders.size() == 1) {
                items = (List<TypeDescriptor>) typeProviders.iterator().next().getTypeNames(null, text, nameKind);
            } else {
                // Multiple providers: merge results
                items = new ArrayList<TypeDescriptor>(128);
                for (TypeProvider provider : typeProviders) {
                    if (isCanceled) {
                        return null;
                    }
                    List<? extends TypeDescriptor> list = provider.getTypeNames(null, text, nameKind);
                    if (list != null) {
                        items.addAll(list);
                    }
                }

            }
            
            if ( !isCanceled ) {   
                //time = System.currentTimeMillis();
                Collections.sort(items, new TypeComparator());

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
            jlName.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/jumpto/type/sample.png")));
            
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
                panel.openSelectedItem();
            }
        }
        
    }

    private class TypeComparator implements Comparator<TypeDescriptor> {
        public int compare(TypeDescriptor t1, TypeDescriptor t2) {
           int cmpr = compareStrings( t1.getTypeName(), t2.getTypeName() );
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
}

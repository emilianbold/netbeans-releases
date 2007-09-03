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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.element.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider;
import org.netbeans.modules.cnd.modelutil.CsmImageName;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action for "Go to Function or Variable..." menu item
 * @author Vladimir Kvashin
 */
public class GoToElementAction extends AbstractAction implements GoToElementPanel.ContentProvider {

    /*package-local*/ static final boolean TRACE = Boolean.getBoolean("cnd.goto.fv.trace");
    
    private SearchType nameKind;
    private static ListModel EMPTY_LIST_MODEL = new DefaultListModel();
    private static final RequestProcessor rp = new RequestProcessor("GoToFuncVarAction-RequestProcessor", 1);
    private Worker running;
    private RequestProcessor.Task task;
    private GoToElementPanel panel;
    private Dialog dialog;
    private JButton okButton;
    private Collection<? extends ElementProvider> elementProviders;
    
    public GoToElementAction() {
        super( NbBundle.getMessage( GoToElementAction.class,"TXT_GoToFunctionOrVariable")  );
        //putValue("PopupMenuText", NbBundle.getBundle(GoToFuncVarAction.class).getString("editor-popup-TXT_GoToElement")); // NOI18N
    }

    public void actionPerformed(ActionEvent e) {
	try {
	    elementProviders = Lookup.getDefault().lookupAll(ElementProvider.class);
            panel = new GoToElementPanel( this );
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
	elementProviders = Lookup.getDefault().lookupAll(ElementProvider.class);
        if( OpenProjects.getDefault().getOpenProjects().length > 0 ) {
            for( ElementProvider provider : elementProviders ) {
		if( provider.isSuitable() ) {
		    return true;
		}
	    }
	}
	return false;
    }
    
    /** Creates the dialog to show
     */
   private Dialog createDialog( final GoToElementPanel panel) {
       
        okButton = new JButton (NbBundle.getMessage(GoToElementAction.class, "CTL_OK"));
        okButton.setEnabled (false);
        panel.getAccessibleContext().setAccessibleName( NbBundle.getMessage( GoToElementAction.class, "AN_GoToElement")  ); //NOI18N
        panel.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( GoToElementAction.class, "AD_GoToElement")  ); //NOI18N
                        
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            panel,                             // innerPane
            NbBundle.getMessage( GoToElementAction.class, "DLG_GoToElement"  ), // NOI18N // displayName
            true,
            new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
            okButton,
            DialogDescriptor.DEFAULT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new DialogButtonListener( panel )  );                                 // Action listener
        
         dialogDescriptor.setClosingOptions(new Object[] {okButton, DialogDescriptor.CANCEL_OPTION});
            
        Dialog d = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        
        // Set size
        d.setPreferredSize( new Dimension(  GoToElementOptions.GoToElementDialog.getWidth(),
                                   GoToElementOptions.GoToElementDialog.getHeight()        )        );
        
        // Center the dialog after the size changed.
        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * 9) / 10;
        int maxH = (r.height * 9) / 10;
        Dimension dim = d.getPreferredSize();
        dim.width = Math.min(dim.width, maxW);
        dim.height = Math.min(dim.height, maxH);
        d.setBounds(Utilities.findCenterBounds(dim));
        
        d.addWindowListener(new WindowAdapter() {
	    @Override
            public void windowClosed(WindowEvent e) {
                cleanup();
            }
        });
        
        return d;

    } 
   
    private class DialogButtonListener implements ActionListener {
        
        private GoToElementPanel panel;
        
        public DialogButtonListener( GoToElementPanel panel  ) {
            this.panel = panel;
        }
        
        public void actionPerformed(ActionEvent e) {            
            if ( e.getSource() == okButton) {
                panel.openSelectedItem();
            }
        }
        
    }   
   
    private void cleanup() {
        if ( dialog != null ) { // Closing event for some reson sent twice
        
            // Save dialog size     
            GoToElementOptions.GoToElementDialog.setHeight(dialog.getHeight());
            GoToElementOptions.GoToElementDialog.setWidth(dialog.getWidth());        
            
            dialog.dispose();
            dialog = null;
            for (ElementProvider provider : elementProviders) {
                provider.cleanup();
            }
        }
	
    }

    
    // Implementation of content provider --------------------------------------
    
    
    public ListCellRenderer getListCellRenderer( JList list ) {
        return new Renderer( list );        
    }
    
    
    public void setListModel( GoToElementPanel panel, String text ) {
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
		if( TRACE ) System.err.printf("Worker posted after %d ms.\n", System.currentTimeMillis() - panel.time);
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

    
    private class Worker implements Runnable {
	
	private volatile boolean isCanceled = false;
	private final String text;
	private final long createTime;

	public Worker(String text) {
            this.text = text;
            this.createTime = System.currentTimeMillis();
	    if( TRACE ) System.err.printf("Worker for %s created after %d ms\n", text, System.currentTimeMillis() - panel.time);
	}
	
	public void run() {
	    if( TRACE ) System.err.printf("Worker for %s started after %d ms\n", text, System.currentTimeMillis() - createTime);
            
            List<? extends ElementDescriptor> types = getTypeNames();
            if ( isCanceled ) {
		if( TRACE ) System.err.printf("Worker for %s cancelled after %d ms\n", text, System.currentTimeMillis() - createTime);
                return;
            }
            ListModel model = Models.fromList(types);
            if ( isCanceled ) {            
		if( TRACE ) System.err.printf("Worker for %s cancelled after %d ms\n", text, System.currentTimeMillis() - createTime);
                return;
            }
            
            if ( !isCanceled && model != null ) {
		if( TRACE ) System.err.printf("Worker for %s exited after %d ms\n", text, System.currentTimeMillis() - createTime);
                
                panel.setModel(model);                
                if (okButton != null && !types.isEmpty()) {
                    okButton.setEnabled (true);
                }
            }
            
            
	}
	
	public void cancel() {
	    if( TRACE ) System.err.printf("Worker.cancel() is called\n");
            isCanceled = true;
	}
	
	private List<? extends ElementDescriptor> getTypeNames() {
	    
	    Project[] projects = OpenProjects.getDefault().getOpenProjects();
	    
	    List<ElementDescriptor> items = null;

            for (ElementProvider provider : elementProviders) {
                if (isCanceled) {
                    return null;
                }
		for (int i = 0; i < projects.length; i++) {
		    Project project = projects[i];
                    Collection<? extends ElementDescriptor> list = provider.getElements(project, text, nameKind);
                    if (list != null) {
			if( items == null ) {
			    items = new ArrayList<ElementDescriptor>(list.size());
			}
                        items.addAll(list);
                    }
		}

            }
            
            if ( ! isCanceled && items != null) {   
                long time = System.currentTimeMillis();
                Collections.sort(items, new TypeComparator());
		if( TRACE ) System.err.printf("Sorting took %d ms\n", System.currentTimeMillis() - time);
                return items;
            }
            else {
                return null;
            }
        }
	
    }
    
    private class TypeComparator implements Comparator<ElementDescriptor> {
	
        public int compare(ElementDescriptor t1, ElementDescriptor t2) {
           int result = compareStrings( t1.getDisplayName(), t2.getDisplayName() );
	   if( result == 0 ) {
	       result = compareStrings( t1.getContextName(), t2.getContextName() );
               if( result == 0 ) {
                   result = compareStrings( t1.getProjectName(), t2.getProjectName() );
                   if( result == 0 ) {
                       result = compareStrings( t1.getAbsoluteFileName(), t2.getAbsoluteFileName() );
                   }
               }
	   }
	   return result;
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
    
    private static class Renderer extends DefaultListCellRenderer implements ChangeListener {
         
        private JPanel rendererComponent;
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
            
            rendererComponent = new JPanel();
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
            
            if ( value instanceof ElementDescriptor ) {
                ElementDescriptor td = (ElementDescriptor)value;                
                jlName.setIcon(td.getIcon());
                jlName.setText(td.getDisplayName());
                jlPkg.setText(td.getContextName());
                jlPrj.setText(td.getProjectName());
                jlPrj.setIcon(td.getProjectIcon());
		rendererComponent.setToolTipText(td.getAbsoluteFileName());
            }
            else {
                jlName.setText( value.toString() );
            }
            
            return rendererComponent;
        }
        
        public void stateChanged(ChangeEvent event) {
            
            JViewport jv = (JViewport)event.getSource();
            
            jlName.setText( "Sample" ); // NOI18N
            jlName.setIcon(new ImageIcon(Utilities.loadImage(CsmImageName.CLASS)));
            
            jList.setFixedCellHeight(jlName.getPreferredSize().height);
            jList.setFixedCellWidth(jv.getExtentSize().width);
        }

     }

}

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

package org.netbeans.modules.javadoc.search;

import java.io.Externalizable;
import java.io.ObjectStreamException;
import java.net.URL;
import java.util.ArrayList;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.javahelp.Help;
import org.netbeans.modules.javadoc.settings.DocumentationSettings;
import org.openide.awt.HtmlBrowser;
import org.openide.windows.TopComponent;
import org.openide.util.RequestProcessor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Main window for documentation index search
 *
 * @author Petr Hrebejk, Petr Suchomel
 */
public final class IndexSearch
            extends TopComponent
    implements Externalizable {

    private static final String INDEX_SEARCH_HELP_CTX_KEY = "javadoc.search.window"; //NOI18N
            
    private static final java.awt.Dimension PREFFERED_SIZE = new java.awt.Dimension( 580, 430 );

    static final long serialVersionUID =1200348578933093459L;

    /** The only instance allowed in system */
    private static Reference refIndexSearch;
    
    /** cache of previously searched strings */
    private static Object[] MRU = new Object[0];

    /** Search engine */
    private JavadocSearchEngine searchEngine = null;

    /** The state of the window is stored in hidden options of DocumentationSettings */
    private DocumentationSettings ds = DocumentationSettings.getDefault();

    private String quickFind;

    /* Current sort mode */
    private String currentSort = "A"; // NOI18N

    /* Hand made components */
    private javax.swing.JScrollPane resultsScrollPane;
    private javax.swing.JList resultsList;
    //private HtmlBrowser.BrowserComponent quickBrowser;
    private HtmlBrowser quickBrowser;
    private JSplitPane splitPanel;

    /** List models for different sorts */
    private ArrayList results = new ArrayList();

    private DefaultListModel referenceModel = null;
    private DefaultListModel typeModel = null;
    private DefaultListModel alphaModel = null;

    /* Holds split position if the quick view is disabled */
    private int oldSplit = DocumentationSettings.getDefault().getIdxSearchSplit();

    private final DefaultListModel waitModel = new DefaultListModel();
    private final DefaultListModel notModel = new DefaultListModel();
    private boolean setDividerLocation;

    /** Initializes the Form */
    public IndexSearch() {
        ResourceBundle b = NbBundle.getBundle(IndexSearch.class);
        DocIndexItem dii = new DocIndexItem( b.getString("CTL_SEARCH_Wait" ), "", null, "" );    //NOI18N
        dii.setIconIndex( DocSearchIcons.ICON_WAIT );
        waitModel.addElement( dii );

        DocIndexItem diin = new DocIndexItem( b.getString("CTL_SEARCH_NotFound" ), "", null, "" );   //NOI18N
        diin.setIconIndex( DocSearchIcons.ICON_NOT_FOUND );
        notModel.addElement( diin );
        
        initComponents ();
        
        // Force winsys to not show tab when this comp is alone                                                                                                                 
        putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N

        javax.swing.ComboBoxEditor editor = searchComboBox.getEditor();
        editor.addActionListener (new java.awt.event.ActionListener () {
                                      public void actionPerformed (java.awt.event.ActionEvent evt) {
                                          if ( searchEngine == null )
                                              searchButtonActionPerformed( evt );
                                      }
                                  }
                                 );

        // Split panel
        splitPanel = new JSplitPane (JSplitPane.VERTICAL_SPLIT);
        splitPanel.setPreferredSize(PREFFERED_SIZE);
        
        splitPanel.setDividerLocation(oldSplit / 100.0);
        //previous line does not work
        //setDividerLocation must be set in open
        setDividerLocation = true;
        

        java.awt.GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets (5, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (splitPanel, gridBagConstraints1);

        // Results - SrollPane & JList
        resultsScrollPane = new javax.swing.JScrollPane ();

        resultsList = new javax.swing.JList ();
        resultsList.setSelectionMode (javax.swing.ListSelectionModel.SINGLE_SELECTION );
        resultsList.addKeyListener (new java.awt.event.KeyAdapter () {
                                        public void keyPressed (java.awt.event.KeyEvent evt) {
                                            resultsListKeyPressed (evt);
                                        }
                                    }
                                   );
        resultsList.addMouseListener (new java.awt.event.MouseAdapter () {
                                          public void mouseClicked (java.awt.event.MouseEvent evt) {
                                              resultsListMouseClicked (evt);
                                          }
                                      }
                                     );

        resultsScrollPane.setViewportView (resultsList);

        splitPanel.setTopComponent(resultsScrollPane);

        // Quick browser component
        quickBrowser = new HtmlBrowser( true, false );//.BrowserComponent( true, false );
        quickBrowser.setEnableLocation( false );
        quickBrowser.setEnableHome( false );
        //browser buttons without border are too top
        quickBrowser.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 0, 0, 0)));
        splitPanel.setBottomComponent(quickBrowser);

        DefaultListModel listModel = new DefaultListModel(); // PENDING: Change to SortedArrayList
        resultsList.setModel( listModel );

        IndexListCellRenderer cr = new IndexListCellRenderer();
        resultsList.setCellRenderer( cr );

        resultsList.getSelectionModel().addListSelectionListener(
            new javax.swing.event.ListSelectionListener() {
                public void valueChanged( javax.swing.event.ListSelectionEvent evt ) {
                    showHelp( true );
                }
            });
        resultsScrollPane.validate();


        
        sourceButton.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/javadoc/resources/showSource.gif"))); // NOI18N
        byReferenceButton.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/javadoc/resources/refSort.gif"))); // NOI18N
        byTypeButton.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/javadoc/resources/typeSort.gif"))); // NOI18N
        byNameButton.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/javadoc/resources/alphaSort.gif"))); // NOI18N
        quickViewButton.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/javadoc/resources/list_html.gif"))); // NOI18N
        quickViewButton.setSelectedIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/javadoc/resources/list_only.gif"))); // NOI18N

        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add( byReferenceButton );
        bg.add( byTypeButton );
        bg.add( byNameButton );

        // Adding ToolTips

        searchButton.setToolTipText(b.getString( "CTL_SEARCH_search_ToolTip" ));    //NOI18N
        byReferenceButton.setToolTipText(b.getString( "CTL_SEARCH_byReference_ToolTip" ));   //NOI18N
        byTypeButton.setToolTipText(b.getString( "CTL_SEARCH_byType_ToolTip" ));   //NOI18N
        byNameButton.setToolTipText(b.getString( "CTL_SEARCH_byName_ToolTip" ));   //NOI18N
        quickViewButton.setToolTipText(b.getString( "CTL_SEARCH_quickView_ToolTip" ));   //NOI18N
        sourceButton.setToolTipText(b.getString( "CTL_SEARCH_showSource_ToolTip" ));   //NOI18N
        searchComboBox.setToolTipText(b.getString( "ACS_SEARCH_SearchComboBoxA11yDesc" ));   //NOI18N
        resultsList.setToolTipText(b.getString( "ACS_SEARCH_ResultsListA11yDesc" ));   //NOI18N
        quickBrowser.setToolTipText(b.getString( "ACS_SEARCH_QuickBrowserA11yDesc" ));   //NOI18N
        
        // Adding mnemonics
        if (!Utilities.isMac()) {
            byReferenceButton.setMnemonic(b.getString("CTL_SEARCH_byReference_Mnemonic").charAt(0));  // NOI18N
            byTypeButton.setMnemonic(b.getString("CTL_SEARCH_byType_Mnemonic").charAt(0));  // NOI18N
            byNameButton.setMnemonic(b.getString("CTL_SEARCH_byName_Mnemonic").charAt(0));  // NOI18N
            quickViewButton.setMnemonic(b.getString("CTL_SEARCH_quickView_Mnemonic").charAt(0));  // NOI18N
            sourceButton.setMnemonic(b.getString("CTL_SEARCH_showSource_Mnemonic").charAt(0));  // NOI18N
        }
        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(IndexSearch.class,"CTL_SEARCH_ButtonFind"));
        Mnemonics.setLocalizedText(helpButton, NbBundle.getMessage(IndexSearch.class,"CTL_SEARCH_ButtonHelp"));
        
        initAccessibility();
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    protected String preferredID() {
        return "JavaDocIndexSearch"; // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(INDEX_SEARCH_HELP_CTX_KEY);
    }
    
    private void initAccessibility() {
        ResourceBundle b = NbBundle.getBundle(IndexSearch.class);
        getAccessibleContext().setAccessibleName(b.getString("ACS_SEARCH_PanelA11yName"));  // NOI18N
        getAccessibleContext().setAccessibleDescription(b.getString("ACS_SEARCH_PanelA11yDesc"));  // NOI18N
        searchComboBox.getAccessibleContext().setAccessibleName(b.getString("ACS_SEARCH_SearchComboBoxA11yName"));  // NOI18N
        searchComboBox.getAccessibleContext().setAccessibleDescription(b.getString("ACS_SEARCH_SearchComboBoxA11yDesc")); // NOI18N
        resultsList.getAccessibleContext().setAccessibleName(b.getString("ACS_SEARCH_ResultsListA11yName"));  // NOI18N
        resultsList.getAccessibleContext().setAccessibleDescription(b.getString("ACS_SEARCH_ResultsListA11yDesc")); // NOI18N
        quickBrowser.getAccessibleContext().setAccessibleName(b.getString("ACS_SEARCH_QuickBrowserA11yName"));  // NOI18N
        quickBrowser.getAccessibleContext().setAccessibleDescription(b.getString("ACS_SEARCH_QuickBrowserA11yDesc"));  // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        searchComboBox = new javax.swing.JComboBox(MRU);
        searchButton = new javax.swing.JButton();
        sourceButton = new javax.swing.JButton();
        byNameButton = new javax.swing.JToggleButton();
        byReferenceButton = new javax.swing.JToggleButton();
        byTypeButton = new javax.swing.JToggleButton();
        quickViewButton = new javax.swing.JToggleButton();
        helpButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        searchComboBox.setEditable(true);
        searchComboBox.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        jPanel1.add(searchComboBox, gridBagConstraints);

        searchButton.setText("Search");
        searchButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(searchButton, gridBagConstraints);

        sourceButton.setPreferredSize(new java.awt.Dimension(25, 25));
        sourceButton.setMinimumSize(new java.awt.Dimension(25, 25));
        sourceButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(sourceButton, gridBagConstraints);

        byNameButton.setSelected(true);
        byNameButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byNameButton.setActionCommand("A");
        byNameButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byNameButton.setRequestFocusEnabled(false);
        byNameButton.addActionListener(formListener);

        jPanel1.add(byNameButton, new java.awt.GridBagConstraints());

        byReferenceButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byReferenceButton.setActionCommand("R");
        byReferenceButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byReferenceButton.setRequestFocusEnabled(false);
        byReferenceButton.addActionListener(formListener);

        jPanel1.add(byReferenceButton, new java.awt.GridBagConstraints());

        byTypeButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byTypeButton.setActionCommand("T");
        byTypeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byTypeButton.setRequestFocusEnabled(false);
        byTypeButton.addActionListener(formListener);

        jPanel1.add(byTypeButton, new java.awt.GridBagConstraints());

        quickViewButton.setSelected(true);
        quickViewButton.setRequestFocusEnabled(false);
        quickViewButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(quickViewButton, gridBagConstraints);

        helpButton.setToolTipText(org.openide.util.NbBundle.getBundle(IndexSearch.class).getString("CTL_SEARCH_ButtonHelp_tooltip"));
        helpButton.setText("Help");
        helpButton.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(helpButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == searchComboBox) {
                IndexSearch.this.searchComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == searchButton) {
                IndexSearch.this.searchButtonActionPerformed(evt);
            }
            else if (evt.getSource() == sourceButton) {
                IndexSearch.this.showSource(evt);
            }
            else if (evt.getSource() == byNameButton) {
                IndexSearch.this.sortButtonActionPerformed(evt);
            }
            else if (evt.getSource() == byReferenceButton) {
                IndexSearch.this.sortButtonActionPerformed(evt);
            }
            else if (evt.getSource() == byTypeButton) {
                IndexSearch.this.sortButtonActionPerformed(evt);
            }
            else if (evt.getSource() == quickViewButton) {
                IndexSearch.this.quickViewButtonActionPerformed(evt);
            }
            else if (evt.getSource() == helpButton) {
                IndexSearch.this.helpButtonActionPerformed(evt);
            }
        }
    }
    // </editor-fold>//GEN-END:initComponents

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        Help help=(Help)Lookup.getDefault().lookup(Help.class);
        
        help.showHelp(getHelpCtx());
    }//GEN-LAST:event_helpButtonActionPerformed

    private void showSource (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSource
        showSource();
    }//GEN-LAST:event_showSource

    private void sortButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortButtonActionPerformed

        currentSort = evt.getActionCommand();
        ds.setIdxSearchSort( currentSort );
        sortResults();

    }//GEN-LAST:event_sortButtonActionPerformed

    private void quickViewButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickViewButtonActionPerformed
        if ( quickViewButton.isSelected() ) {
            splitPanel.setDividerLocation( oldSplit == 100 ? 0.5 : oldSplit / 100.0 );
            ds.setIdxSearchSplit( oldSplit == 100 ? 50 : oldSplit );
            ds.setIdxSearchNoHtml( false );
            showHelp( true );
        }
        else {
            oldSplit = (int) (splitPanel.getDividerLocation() / splitPanel.getSize().getHeight() * 100);
            splitPanel.setDividerLocation( 1.0 );
            ds.setIdxSearchSplit( 100 );
            ds.setIdxSearchNoHtml( true );
        }
    }//GEN-LAST:event_quickViewButtonActionPerformed

    private void resultsListKeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resultsListKeyPressed
        // Add your handling code here:
        if ( evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER ||
                evt.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE ) {
            /*
            if ( evt.isShiftDown() ) {
              showSource();      
              evt.consume();
        }
            else
            */
            showHelp();
        }
    }//GEN-LAST:event_resultsListKeyPressed

    private void resultsListMouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsListMouseClicked
        // Add your handling code here:
        if ( evt.getClickCount() == 2 ) {
            if ( evt.isShiftDown() ) {
                showSource();
                evt.consume();
            }
            else
                showHelp();
        }
    }//GEN-LAST:event_resultsListMouseClicked

    private void showHelp(  ) {
        showHelp( false );
    }

    RequestProcessor.Task task=null;    
    /** Invokes the browser with help */
    private void showHelp( boolean quick ) {

        if (quick && splitPanel.getDividerLocation() == 100 )
            return;

        if (  resultsList.getMinSelectionIndex() < 0 )
            return;

        
        DocIndexItem  dii = (DocIndexItem)resultsList.getModel().getElementAt( resultsList.getMinSelectionIndex() );

        try {
            URL url = dii.getURL();

            if ( url == null )
                return;

            // Workaround for bug in FileSystems
            String strUrl = url.toString();

            if ( strUrl.startsWith( "nbfs:" ) && strUrl.charAt( 5 ) != '/' ){ // NOI18N
                url = new URL( "nbfs:/" + strUrl.substring( 5 ) ); // NOI18N
            }
            
            if ( quick ){
                final URL furl = url;
                if( task != null )
                    task.cancel();
                task = RequestProcessor.getDefault().post( new Runnable(){
                    public void run(){
                        quickBrowser.setURL( furl );
                    }
                }, 100 );      
            }
            else
                HtmlBrowser.URLDisplayer.getDefault().showURL( url );
        }
        catch ( java.net.MalformedURLException ex ) {
            // Do nothing if the URL isn't O.K.
        }
    }

    /** Tryies to find source code for the selected item in repository. If the
     * is foun opens the source 
     */
    private void showSource( ) {

        if ( resultsList.getMinSelectionIndex() < 0 ) {
            return;
        }

        DocIndexItem  dii = (DocIndexItem)resultsList.getModel().getElementAt( resultsList.getMinSelectionIndex() );

        try {
            Object[] e = SrcFinder.findSource( dii.getPackage(), dii.getURL() );

            if ( e != null ) {
                FileObject toOpen = (FileObject) e[0];
                ElementHandle eh = (ElementHandle) e[1];
                UiUtils.open(toOpen, eh);
            }
            else {
                NotifyDescriptor.Message nd = new NotifyDescriptor.Message( NbBundle.getMessage(IndexSearch.class, "MSG_SEARCH_SrcNotFound" ) );   //NOI18N
                DialogDisplayer.getDefault().notify( nd );
            }

        }
        catch ( java.net.MalformedURLException e ) {
            //System.out.println( e  );
        }
    }

    private void searchComboBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchComboBoxActionPerformed
        /*if ( searchEngine == null )
          searchButtonActionPerformed( evt );*/
    }//GEN-LAST:event_searchComboBoxActionPerformed

    private void searchButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        if ( searchEngine == null ) {
            if ( searchComboBox.getEditor().getItem().toString() != null &&
                    searchComboBox.getEditor().getItem().toString().length() > 0 ) {
                searchEngine = JavadocSearchEngine.getDefault();
                go();
            }
        }
        else {
            searchEngine.stop();
            searchEngine = null;
        }
    }//GEN-LAST:event_searchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton byNameButton;
    private javax.swing.JToggleButton byReferenceButton;
    private javax.swing.JToggleButton byTypeButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton quickViewButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JComboBox searchComboBox;
    private javax.swing.JButton sourceButton;
    // End of variables declaration//GEN-END:variables


    private void searchStoped() {
        searchEngine = null;
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
                                                    public void run() {
                                                        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(IndexSearch.class,"CTL_SEARCH_ButtonFind"));
                                                        if ( resultsList.getModel().getSize() > 0 ) {
                                                            resultsList.setSelectedIndex( 0 );
                                                            resultsList.grabFocus();
                                                        }
                                                        referenceModel = typeModel = alphaModel = null;
                                                        sortResults();
                                                    }
                                                } );
    }

    void setTextToFind( String toFind ) {
        quickFind = toFind;
        /*
        if ( toFind != null ) {
          quickFind = toFind; 
    }
        */
    }


    public void open() {
        super.open();

        if ( quickFind != null ) {
            searchComboBox.getEditor().setItem( quickFind );
            if ( searchEngine == null ) {
                searchButtonActionPerformed( null );
            }
        }

        quickFind = null;
        searchComboBox.getEditor().selectAll();
        
        if (setDividerLocation) {
            splitPanel.setDividerLocation(oldSplit / 100.0);
            setDividerLocation = false;
        }
    }

    java.awt.Dimension getPrefferedSize() {
        return PREFFERED_SIZE;
    }

    public static IndexSearch getDefault() {
        IndexSearch indexSearch;
        if (refIndexSearch == null || null == (indexSearch = (IndexSearch) refIndexSearch.get())) {
            indexSearch = new IndexSearch ();
            refIndexSearch = new SoftReference(indexSearch);

            indexSearch.setName( NbBundle.getMessage(IndexSearch.class, "CTL_SEARCH_WindowTitle") );   //NOI18N
            indexSearch.setIcon(Utilities.loadImage("org/netbeans/modules/javadoc/resources/searchDoc.gif")); // NOI18N
        }
        return indexSearch;
    }

    public void resolveButtonState() {

        final String sort = ds.getIdxSearchSort();
        final boolean noHtml = ds.isIdxSearchNoHtml();
        final int split = ds.getIdxSearchSplit();

        currentSort = sort;

        javax.swing.SwingUtilities.invokeLater( new Runnable() {
                                                    public void run() {
                                                        byNameButton.setSelected( sort.equals( "A" ) ); // NOI18N
                                                        byReferenceButton.setSelected( sort.equals( "R" ) ); // NOI18N
                                                        byTypeButton.setSelected( sort.equals( "T" ) ); // NOI18N

                                                        quickViewButton.setSelected( !noHtml );

                                                        splitPanel.setDividerLocation(split / 100.0);
                                                    }
                                                } );
    }
    
    /**
     * Replaces previously stored instances with the default one. Just due to
     * backward compatibility.
     * @return the default instance
     * @throws ObjectStreamException
     */ 
    private Object readResolve() throws ObjectStreamException {
        return getDefault();
    }

    void go() {
        String toFind = searchComboBox.getEditor().getItem().toString().trim();

        // Alocate array for results
        results = new ArrayList();

        //Clear all models
        referenceModel = null;
        typeModel = null;
        alphaModel = null;

        // Try to find this string in Combo

        for ( int i = 0; i < searchComboBox.getItemCount(); i++ ) {
            if ( searchComboBox.getItemAt( i ).toString().equals( toFind ) || i >= 10 ) {
                searchComboBox.removeItemAt( i );
            }
        }

        searchComboBox.insertItemAt( toFind, 0 );
        mirrorMRUStrings();
        searchComboBox.getEditor().setItem( toFind );

        resultsList.setModel( waitModel );

        try {
            searchEngine.search(new String[]{toFind}, new JavadocSearchEngine.SearchEngineCallback(){
                public void finished(){
                    searchStoped();
                }
                public void addItem(DocIndexItem item){
                    results.add(item);
                }
            });
        }
        catch(NoJavadocException noJdc){
            DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message( noJdc.getMessage() ) );   //NOI18N
            searchStoped();
            return;
        }
        
        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(IndexSearch.class,"CTL_SEARCH_ButtonStop"));
    }
    
    private void mirrorMRUStrings() {
        ComboBoxModel model = searchComboBox.getModel();
        int size = model.getSize();
        MRU = new Object[size];
        for (int i = 0; i < size; i++) {
            MRU[i] = model.getElementAt(i);
        }
    }

    DefaultListModel generateModel( java.util.Comparator comp ) {
        DefaultListModel model = new DefaultListModel();

        java.util.Collections.sort( results, comp );
        java.util.Iterator it = results.iterator();

        String pckg = null;

        while ( it.hasNext() ) {
            DocIndexItem dii = (DocIndexItem)it.next();
            if ( comp == DocIndexItem.REFERENCE_COMPARATOR &&
                    !dii.getPackage().equals( pckg ) &&
                    dii.getIconIndex() != DocSearchIcons.ICON_PACKAGE ) {
                DocIndexItem ndii = new DocIndexItem(  "PACKAGE ", dii.getPackage(), null, "" ); // NOI18N
                ndii.setIconIndex( DocSearchIcons.ICON_PACKAGE );
                model.addElement( ndii );
                pckg = dii.getPackage();
            }

            model.addElement( dii );
        }
        return model;
    }

    void sortResults() {

        if ( results.size() < 1 ) {
            resultsList.setModel( notModel );
        }
        else if ( currentSort.equals( "R" ) ) { // NOI18N
            if ( referenceModel == null ) {
                resultsList.setModel( waitModel );
                resultsList.invalidate();
                resultsList.revalidate();
                referenceModel = generateModel( DocIndexItem.REFERENCE_COMPARATOR );
            }
            resultsList.setModel( referenceModel );
        }
        else if ( currentSort.equals( "T" ) ) { // NOI18N
            if ( typeModel == null ) {
                resultsList.setModel( waitModel );
                resultsList.invalidate();
                resultsList.revalidate();
                typeModel = generateModel( DocIndexItem.TYPE_COMPARATOR );
            }
            resultsList.setModel( typeModel );
        }
        else if ( currentSort.equals( "A" ) ) { // NOI18N
            if ( alphaModel == null ) {
                resultsList.setModel( waitModel );
                resultsList.invalidate();
                resultsList.revalidate();
                alphaModel = generateModel( DocIndexItem.ALPHA_COMPARATOR );
            }
            resultsList.setModel( alphaModel );
        }

        resultsList.invalidate();
        resultsList.revalidate();
        resultsList.repaint();
    }
}

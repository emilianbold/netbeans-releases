/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.search;

import java.io.*;
import java.net.URL;

import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.ImageIcon;


import org.openide.awt.HtmlBrowser;
import org.openide.awt.SplittedPanel;
import org.openide.windows.TopComponent;
import org.openide.windows.Mode;
import org.openide.windows.Workspace;
import org.openide.util.RequestProcessor;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.src.Element;
import org.openide.cookies.OpenCookie;
import org.openide.util.HelpCtx;

import org.netbeans.modules.javadoc.settings.DocumentationSettings;

/** Main window for documentation index search
 *
 * @author Petr Hrebejk
 */
public class IndexSearch
            extends TopComponent
    implements Externalizable {

    //static final long serialVersionUID =3206093459760846163L;
    private static ResourceBundle bundle = null;
        
    private static final java.awt.Dimension PREFFERED_SIZE = new java.awt.Dimension( 580, 430 );

    static final long serialVersionUID =1200348578933093459L;

    /** The only instance allowed in system */
    private static IndexSearch indexSearch;

    /** Search engine */
    private SearchEngine searchEngine = null;

    /** The state of the window is stored in hidden options of DocumentationSettings */
    DocumentationSettings ds = new DocumentationSettings();

    private String quickFind;

    /* Button icons */
    //private static final ImageIcon windowIcon = new ImageIcon (IndexSearch.class.getResource ("/org/netbeans/modules/javadoc/resources/searchDoc.gif")); // NOI18N
    private static final ImageIcon refSortIcon = new ImageIcon (IndexSearch.class.getResource ("/org/netbeans/modules/javadoc/resources/refSort.gif")); // NOI18N
    private static final ImageIcon typeSortIcon = new ImageIcon (IndexSearch.class.getResource ("/org/netbeans/modules/javadoc/resources/typeSort.gif")); // NOI18N
    private static final ImageIcon alphaSortIcon = new ImageIcon (IndexSearch.class.getResource ("/org/netbeans/modules/javadoc/resources/alphaSort.gif")); // NOI18N
    private static final ImageIcon listOnlyIcon = new ImageIcon (IndexSearch.class.getResource ("/org/netbeans/modules/javadoc/resources/list_only.gif")); // NOI18N
    private static final ImageIcon listHtmlIcon = new ImageIcon (IndexSearch.class.getResource ("/org/netbeans/modules/javadoc/resources/list_html.gif")); // NOI18N
    private static final ImageIcon showSourceIcon = new ImageIcon (IndexSearch.class.getResource ("/org/netbeans/modules/javadoc/resources/showSource.gif")); // NOI18N

    private final static String ICON_RESOURCE = "/org/netbeans/modules/javadoc/resources/searchDoc.gif"; // NOI18N
    private final static java.awt.Image windowIcon = java.awt.Toolkit.getDefaultToolkit ().getImage (
                IndexSearch.class.getResource (ICON_RESOURCE));

    /* Current sort mode */
    private String currentSort = "A"; // NOI18N

    /* Hand made components */
    private javax.swing.JScrollPane resultsScrollPane;
    private javax.swing.JList resultsList;
    private HtmlBrowser.BrowserComponent quickBrowser;
    private org.openide.awt.SplittedPanel splitPanel;

    /** Button titles */

    private static final String STR_FIND = ResourceUtils.getBundledString ("CTL_SEARCH_ButtonFind");
    private static final String STR_STOP = ResourceUtils.getBundledString ("CTL_SEARCH_ButtonStop");

    /** List models for different sorts */
    private ArrayList results = new ArrayList();

    private DefaultListModel referenceModel = null;
    private DefaultListModel typeModel = null;
    private DefaultListModel alphaModel = null;

    /* Holds split position if the quick view is disabled */
    private int oldSplit = 50;

    private static final DefaultListModel waitModel = new DefaultListModel();
    private static final DefaultListModel notModel = new DefaultListModel();

    static {
        DocIndexItem dii = new DocIndexItem( ResourceUtils.getBundledString("CTL_SEARCH_Wait" ), "", null, "" );
        dii.setIconIndex( DocSearchIcons.ICON_WAIT );
        waitModel.addElement( dii );

        DocIndexItem diin = new DocIndexItem( ResourceUtils.getBundledString("CTL_SEARCH_NotFound" ), "", null, "" );
        diin.setIconIndex( DocSearchIcons.ICON_NOT_FOUND );
        notModel.addElement( diin );
    }

    /** Initializes the Form */
    public IndexSearch() {
        initComponents ();

        javax.swing.ComboBoxEditor editor = searchComboBox.getEditor();
        editor.addActionListener (new java.awt.event.ActionListener () {
                                      public void actionPerformed (java.awt.event.ActionEvent evt) {
                                          if ( searchEngine == null )
                                              searchButtonActionPerformed( evt );
                                      }
                                  }
                                 );

        // Split panel

        splitPanel = new org.openide.awt.SplittedPanel ();
        //splitPanel.setLayout (new java.awt.FlowLayout ());
        splitPanel.setSplitType( org.openide.awt.SplittedPanel.HORIZONTAL );
        splitPanel.setSplitAbsolute( false );
        splitPanel.setSplitPosition( oldSplit = 50 );
        splitPanel.setSplitDragable( true );
        splitPanel.setSplitTypeChangeEnabled( true );
        splitPanel.addSplitChangeListener( new SplittedPanel.SplitChangeListener() {
                                               public void splitChanged (SplittedPanel.SplitChangeEvent evt) {
                                                   int value = evt.getNewValue();
                                                   ds.setIdxSearchSplit( value );
                                                   if ( value == 100 ) {
                                                       quickViewButton.setSelected( false );
                                                       ds.setIdxSearchNoHtml( true );
                                                   }
                                                   else {
                                                       quickViewButton.setSelected( true );
                                                       ds.setIdxSearchNoHtml( false );
                                                   }
                                               }
                                           } );

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

        splitPanel.add( resultsScrollPane, org.openide.awt.SplittedPanel.ADD_FIRST );

        // Quick browser component
        quickBrowser = new HtmlBrowser.BrowserComponent( true, false );
        quickBrowser.setEnableLocation( false );
        quickBrowser.setEnableHome( false );
        splitPanel.add( quickBrowser, org.openide.awt.SplittedPanel.ADD_SECOND );

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

        searchButton.setText( STR_FIND );

        sourceButton.setIcon( showSourceIcon );
        byReferenceButton.setIcon( refSortIcon );
        byTypeButton.setIcon( typeSortIcon );
        byNameButton.setIcon( alphaSortIcon );
        quickViewButton.setIcon( listHtmlIcon );
        quickViewButton.setSelectedIcon( listOnlyIcon );

        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add( byReferenceButton );
        bg.add( byTypeButton );
        bg.add( byNameButton );

        // Adding ToolTips

        searchButton.setToolTipText(ResourceUtils.getBundledString( "CTL_SEARCH_search_ToolTip" ));
        byReferenceButton.setToolTipText(ResourceUtils.getBundledString( "CTL_SEARCH_byReference_ToolTip" ));
        byTypeButton.setToolTipText(ResourceUtils.getBundledString( "CTL_SEARCH_byType_ToolTip" ));
        byNameButton.setToolTipText(ResourceUtils.getBundledString( "CTL_SEARCH_byName_ToolTip" ));
        quickViewButton.setToolTipText(ResourceUtils.getBundledString( "CTL_SEARCH_quickView_ToolTip" ));
        sourceButton.setToolTipText(ResourceUtils.getBundledString( "CTL_SEARCH_showSource_ToolTip" ));
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (IndexSearch.class);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        searchComboBox = new javax.swing.JComboBox();
        searchButton = new javax.swing.JButton();
        sourceButton = new javax.swing.JButton();
        byNameButton = new javax.swing.JToggleButton();
        byReferenceButton = new javax.swing.JToggleButton();
        byTypeButton = new javax.swing.JToggleButton();
        quickViewButton = new javax.swing.JToggleButton();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        
        jPanel1.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        searchComboBox.setEditable(true);
        searchComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchComboBoxActionPerformed(evt);
            }
        }
        );
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 8);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints2.weightx = 1.0;
        jPanel1.add(searchComboBox, gridBagConstraints2);
        
        
        searchButton.setText(org.openide.util.NbBundle.getBundle(IndexSearch.class).getString("IndexSearch.searchButton.text"));
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        }
        );
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 3);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(searchButton, gridBagConstraints2);
        
        
        sourceButton.setPreferredSize(new java.awt.Dimension(25, 25));
        sourceButton.setMinimumSize(new java.awt.Dimension(25, 25));
        sourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSource(evt);
            }
        }
        );
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 3);
        jPanel1.add(sourceButton, gridBagConstraints2);
        
        
        byNameButton.setSelected(true);
        byNameButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byNameButton.setActionCommand("A");
        byNameButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byNameButton.setRequestFocusEnabled(false);
        byNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortButtonActionPerformed(evt);
            }
        }
        );
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        jPanel1.add(byNameButton, gridBagConstraints2);
        
        
        byReferenceButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byReferenceButton.setActionCommand("R");
        byReferenceButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byReferenceButton.setRequestFocusEnabled(false);
        byReferenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortButtonActionPerformed(evt);
            }
        }
        );
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        jPanel1.add(byReferenceButton, gridBagConstraints2);
        
        
        byTypeButton.setPreferredSize(new java.awt.Dimension(25, 25));
        byTypeButton.setActionCommand("T");
        byTypeButton.setMinimumSize(new java.awt.Dimension(25, 25));
        byTypeButton.setRequestFocusEnabled(false);
        byTypeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortButtonActionPerformed(evt);
            }
        }
        );
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        jPanel1.add(byTypeButton, gridBagConstraints2);
        
        
        quickViewButton.setSelected(true);
        quickViewButton.setRequestFocusEnabled(false);
        quickViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickViewButtonActionPerformed(evt);
            }
        }
        );
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(quickViewButton, gridBagConstraints2);
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0;
        add(jPanel1, gridBagConstraints1);
        
    }//GEN-END:initComponents

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
            splitPanel.setSplitPosition( oldSplit == 100 ? 50 : oldSplit );
            ds.setIdxSearchSplit( oldSplit == 100 ? 50 : oldSplit );
            ds.setIdxSearchNoHtml( false );
            showHelp( true );
        }
        else {
            oldSplit = splitPanel.getSplitPosition();
            splitPanel.setSplitPosition( 100 );
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

    /** Invokes the browser with help */
    private void showHelp( boolean quick ) {

        if (quick && splitPanel.getSplitPosition() == 100 )
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
            if ( quick )
                quickBrowser.setURL( url );
            else
                TopManager.getDefault().showUrl( url );
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
            Element e = SrcFinder.findSource( dii.getPackage(), dii.getURL() );

            if ( e != null ) {
                OpenCookie oc = (OpenCookie)e.getCookie( OpenCookie.class );
                if ( oc != null ) {
                    System.out.println( oc );
                    oc.open();
                }
                else {
                    NotifyDescriptor.Message nd = new NotifyDescriptor.Message( ResourceUtils.getBundledString( "MSG_SEARCH_SrcNotFound" ) );
                    TopManager.getDefault().notify( nd );
                }
            }
            else {
                NotifyDescriptor.Message nd = new NotifyDescriptor.Message( ResourceUtils.getBundledString( "MSG_SEARCH_SrcNotFound" ) );
                TopManager.getDefault().notify( nd );
            }

        }
        catch ( java.net.MalformedURLException e ) {
            System.out.println( e  );
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
                searchEngine = new SearchEngine();
                searchEngine.go();
            }
        }
        else {
            searchEngine.stop();
            searchEngine = null;
        }
    }//GEN-LAST:event_searchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox searchComboBox;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton sourceButton;
    private javax.swing.JToggleButton byNameButton;
    private javax.swing.JToggleButton byReferenceButton;
    private javax.swing.JToggleButton byTypeButton;
    private javax.swing.JToggleButton quickViewButton;
    // End of variables declaration//GEN-END:variables


    private void searchStoped() {
        searchEngine = null;
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
                                                    public void run() {
                                                        searchButton.setText( STR_FIND );
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
    }

    java.awt.Dimension getPrefferedSize() {
        return PREFFERED_SIZE;
    }

    public static IndexSearch getDefault() {
        if ( indexSearch == null ) {
            indexSearch = new IndexSearch ();
            Workspace workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();

            /*
            Mode myMode = workspace.createMode(
              "JavaDocSearch", //NOI8N // NOI18N
              org.openide.util.ResourceUtils.getBundledString("IndexSearch.workspace.name"), 
              IndexSearch.class.getResource (ICON_RESOURCE));
            myMode.setBounds(new Rectangle( 200, 200, 600, 400 ) );
            myMode.dockInto( indexSearch );
            */

            indexSearch.setName( ResourceUtils.getBundledString ("CTL_SEARCH_WindowTitle") );
        }
        indexSearch.setIcon( windowIcon );
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

                                                        splitPanel.setSplitPosition( split );
                                                    }
                                                } );
    }

    public void readExternal(final ObjectInput in )
    throws java.io.IOException, java.lang.ClassNotFoundException {

        super.readExternal( in );
        indexSearch = this;
        resolveButtonState();
        indexSearch = getDefault(); //test for null
    }

    public void writeExternal(final ObjectOutput out)
    throws java.io.IOException {

        super.writeExternal( out );
    }


    private class SearchEngine {

        private ArrayList tasks;

        private DocFileSystem[] docSystems;
        private IndexSearchThread.DocIndexItemConsumer diiConsumer;

        SearchEngine() {
            docSystems = DocFileSystem.getFolders();
            tasks = new ArrayList( docSystems.length );

            diiConsumer = new IndexSearchThread.DocIndexItemConsumer() {
                              public void addDocIndexItem( final DocIndexItem dii ) {
                                  results.add( dii );
                                  /*
                                  javax.swing.SwingUtilities.invokeLater( new Runnable() {
                                    public void run() {
                                      ((DefaultListModel)resultsList.getModel()).addElement( dii );
                                    }
                              } ) ;*/
                              }

                              public void indexSearchThreadFinished( IndexSearchThread t ) {
                                  tasks.remove( t );
                                  if ( tasks.isEmpty() )
                                      searchStoped();
                              }
                          };
        }

        /** Starts searching */

        void go() {

            if ( docSystems.length <= 0 ) {
                TopManager.getDefault().notify( new NotifyDescriptor.Message( ResourceUtils.getBundledString( "MSG_NoDoc" ) ) );
                searchStoped();
                return;
            }

            String toFind = new String( searchComboBox.getEditor().getItem().toString() );

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
            searchComboBox.getEditor().setItem( toFind );

            resultsList.setModel( waitModel );
            //((DefaultListModel)resultsList.getModel()).clear();

            for( int i = 0; i < docSystems.length; i++ ) {
                IndexSearchThread searchThread = new SearchThreadJdk12( toFind,  docSystems[i].getIndexFile() , diiConsumer );
                tasks.add( searchThread );
                searchThread.go();
            }
            searchButton.setText( STR_STOP );
        }

        /** Stops the search */

        void stop() {
            for( int i = 0; i < tasks.size(); i++ ) {
                SearchThreadJdk12 searchThread = (SearchThreadJdk12)tasks.get( i );
                searchThread.finish();
            }
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
                // try {
                DocIndexItem ndii = new DocIndexItem(  "PACKAGE ", dii.getPackage(), null, "" ); // NOI18N
                ndii.setIconIndex( DocSearchIcons.ICON_PACKAGE );
                model.addElement( ndii );
                pckg = dii.getPackage();
                // }
                // catch ( java.net.MalformedURLException e ) {
                //System.out.println( e );
                // Do nothing if bad URL
                // }
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

/*
 * Log
 *  27   Gandalf   1.26        1/18/00  Petr Hrebejk    Window made bigger
 *  26   Gandalf   1.25        1/15/00  Petr Hrebejk    UI Change
 *  25   Gandalf   1.24        1/13/00  Petr Hrebejk    i18n mk3  
 *  24   Gandalf   1.23        1/12/00  Petr Hrebejk    i18n mk2
 *  23   Gandalf   1.22        1/12/00  Petr Hrebejk    i18n
 *  22   Gandalf   1.21        1/11/00  Jesse Glick     Context help.
 *  21   Gandalf   1.20        11/23/99 Petr Hrebejk    Show source feature 
 *       added
 *  20   Gandalf   1.19        10/27/99 Petr Hrebejk    Bug fixes & back button 
 *       in Javadoc Quickview
 *  19   Gandalf   1.18        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        10/1/99  Petr Hrebejk    Serialization of sort 
 *       mode fixed
 *  17   Gandalf   1.16        8/17/99  Petr Hrebejk    IndexSearch window 
 *       serialization
 *  16   Gandalf   1.15        8/13/99  Petr Hrebejk    Exception icopn added & 
 *       Jdoc repository moved to this package
 *  15   Gandalf   1.14        7/29/99  David Simonek   changes concerning 
 *       window system
 *  14   Gandalf   1.13        7/26/99  Petr Hrebejk    AutoComment tool 
 *       implemented
 *  13   Gandalf   1.12        7/12/99  Petr Hrebejk    New window system
 *  12   Gandalf   1.11        6/23/99  Petr Hrebejk    HTML doc view & sort 
 *       modes added
 *  11   Gandalf   1.10        6/11/99  Petr Hrebejk    
 *  10   Gandalf   1.9         6/11/99  Petr Hrebejk    Better support for 
 *       search from editor; Enter for start searching
 *  9    Gandalf   1.8         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         6/4/99   Petr Hrebejk    
 *  7    Gandalf   1.6         5/27/99  Petr Hrebejk    Crtl+F1 documentation 
 *       search form editor added
 *  6    Gandalf   1.5         5/16/99  Petr Hrebejk    
 *  5    Gandalf   1.4         5/16/99  Petr Hrebejk    
 *  4    Gandalf   1.3         5/14/99  Petr Hrebejk    
 *  3    Gandalf   1.2         5/14/99  Petr Hrebejk    
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $
 */

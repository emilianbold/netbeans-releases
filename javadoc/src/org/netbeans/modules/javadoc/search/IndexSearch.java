/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.javadoc.search;

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
import org.openide.windows.TopComponent;
import org.openide.windows.Mode;
import org.openide.windows.Workspace;
import org.openide.util.RequestProcessor;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/** Main window for documentation index search
 *
 * @author Petr Hrebejk
 */
public class IndexSearch 
        extends TopComponent 
        implements Externalizable {
          
  /** The only instance allowed in system */        
  private static IndexSearch indexSearch;

  /** Search engine */
  private SearchEngine searchEngine = null;

  private String quickFind;
  
  /* Button icons */
  //private static final ImageIcon windowIcon = new ImageIcon (IndexSearch.class.getResource ("/com/netbeans/developer/modules/javadoc/resources/searchDoc.gif"));
  private static final ImageIcon refSortIcon = new ImageIcon (IndexSearch.class.getResource ("/com/netbeans/developer/modules/javadoc/resources/refSort.gif"));
  private static final ImageIcon typeSortIcon = new ImageIcon (IndexSearch.class.getResource ("/com/netbeans/developer/modules/javadoc/resources/typeSort.gif"));
  private static final ImageIcon alphaSortIcon = new ImageIcon (IndexSearch.class.getResource ("/com/netbeans/developer/modules/javadoc/resources/alphaSort.gif"));
  private static final ImageIcon listOnlyIcon = new ImageIcon (IndexSearch.class.getResource ("/com/netbeans/developer/modules/javadoc/resources/list_only.gif"));
  private static final ImageIcon listHtmlIcon = new ImageIcon (IndexSearch.class.getResource ("/com/netbeans/developer/modules/javadoc/resources/list_html.gif"));

  private final static String ICON_RESOURCE = "/com/netbeans/developer/modules/javadoc/resources/searchDoc.gif";
  private final static java.awt.Image windowIcon = java.awt.Toolkit.getDefaultToolkit ().getImage (
      IndexSearch.class.getResource (ICON_RESOURCE));

  /* Current sort mode */
  private String currentSort = "A";
  
  /* Hand made components */
  private javax.swing.JScrollPane resultsScrollPane;
  private javax.swing.JList resultsList;
  private HtmlBrowser.BrowserComponent quickBrowser; 
  private org.openide.awt.SplittedPanel splitPanel;
  
  private static ResourceBundle bundle = NbBundle.getBundle(IndexSearch.class);

  /** Button titles */

  private static final String STR_FIND = bundle.getString ("CTL_SEARCH_ButtonFind");
  private static final String STR_STOP = bundle.getString ("CTL_SEARCH_ButtonStop");

  /** List models for different sorts */
  private ArrayList results = new ArrayList();
  
  private DefaultListModel referenceModel = null;
  private DefaultListModel typeModel = null;
  private DefaultListModel alphaModel = null;
  
  /* Holds split position if the quick view is disabled */
  private int oldSplit = 50;
  
  private static final DefaultListModel waitModel = new DefaultListModel();
  
  static {
    DocIndexItem dii = new DocIndexItem( "Please wait ...", "", null, "" );
    dii.setIconIndex( DocSearchIcons.ICON_WAIT );
    waitModel.addElement( dii ); 
  }

  /** Initializes the Form */
  public IndexSearch() {
    initComponents ();

    // Split panel
    
    splitPanel = new org.openide.awt.SplittedPanel ();
    //splitPanel.setLayout (new java.awt.FlowLayout ());
    splitPanel.setSplitType( org.openide.awt.SplittedPanel.HORIZONTAL );
    splitPanel.setSplitAbsolute( false );
    splitPanel.setSplitPosition( oldSplit = 50 );
    splitPanel.setSplitDragable( true );
    splitPanel.setSplitTypeChangeEnabled( true );
    
    java.awt.GridBagConstraints gridBagConstraints1 = new java.awt.GridBagConstraints ();
    gridBagConstraints1.gridwidth = 0;
    gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints1.insets = new java.awt.Insets (5, 3, 3, 3);
    gridBagConstraints1.weightx = 1.0;
    gridBagConstraints1.weighty = 1.0;
    add (splitPanel, gridBagConstraints1);
    
    // Results - SrollPane & JList
    resultsScrollPane = new javax.swing.JScrollPane ();

    resultsList = new javax.swing.JList ();
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
    quickBrowser = new HtmlBrowser.BrowserComponent( false, false );
    //quickBrowser.setEnableLocation( false );
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

    byReferenceButton.setIcon( refSortIcon );
    byTypeButton.setIcon( typeSortIcon );
    byNameButton.setIcon( alphaSortIcon );
    quickViewButton.setIcon( listHtmlIcon );
    quickViewButton.setSelectedIcon( listOnlyIcon );
    
    javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
    bg.add( byReferenceButton );
    bg.add( byTypeButton );
    bg.add( byNameButton );
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
    private void initComponents () {//GEN-BEGIN:initComponents
      setLayout (new java.awt.GridBagLayout ());
      java.awt.GridBagConstraints gridBagConstraints1;

      jPanel1 = new javax.swing.JPanel ();
      jPanel1.setLayout (new java.awt.GridBagLayout ());
      java.awt.GridBagConstraints gridBagConstraints2;

      searchComboBox = new javax.swing.JComboBox ();
      searchComboBox.setEditable (true);
      searchComboBox.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          searchComboBoxActionPerformed (evt);
        }
      }
      );

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints2.insets = new java.awt.Insets (0, 3, 0, 8);
      gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTH;
      gridBagConstraints2.weightx = 1.0;
      jPanel1.add (searchComboBox, gridBagConstraints2);

      searchButton = new javax.swing.JButton ();
      searchButton.setText ("Find");
      searchButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          searchButtonActionPerformed (evt);
        }
      }
      );

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      gridBagConstraints2.insets = new java.awt.Insets (0, 0, 0, 3);
      gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
      jPanel1.add (searchButton, gridBagConstraints2);

      byNameButton = new javax.swing.JToggleButton ();
      byNameButton.setSelected (true);
      byNameButton.setPreferredSize (new java.awt.Dimension(25, 25));
      byNameButton.setRequestFocusEnabled (false);
      byNameButton.setActionCommand ("A");
      byNameButton.setMinimumSize (new java.awt.Dimension(25, 25));
      byNameButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          sortButtonActionPerformed (evt);
        }
      }
      );

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      jPanel1.add (byNameButton, gridBagConstraints2);

      byReferenceButton = new javax.swing.JToggleButton ();
      byReferenceButton.setPreferredSize (new java.awt.Dimension(25, 25));
      byReferenceButton.setRequestFocusEnabled (false);
      byReferenceButton.setActionCommand ("R");
      byReferenceButton.setMinimumSize (new java.awt.Dimension(25, 25));
      byReferenceButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          sortButtonActionPerformed (evt);
        }
      }
      );

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      jPanel1.add (byReferenceButton, gridBagConstraints2);

      byTypeButton = new javax.swing.JToggleButton ();
      byTypeButton.setPreferredSize (new java.awt.Dimension(25, 25));
      byTypeButton.setRequestFocusEnabled (false);
      byTypeButton.setActionCommand ("T");
      byTypeButton.setMinimumSize (new java.awt.Dimension(25, 25));
      byTypeButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          sortButtonActionPerformed (evt);
        }
      }
      );

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      jPanel1.add (byTypeButton, gridBagConstraints2);

      quickViewButton = new javax.swing.JToggleButton ();
      quickViewButton.setSelected (true);
      quickViewButton.setRequestFocusEnabled (false);
      quickViewButton.addActionListener (new java.awt.event.ActionListener () {
        public void actionPerformed (java.awt.event.ActionEvent evt) {
          quickViewButtonActionPerformed (evt);
        }
      }
      );

      gridBagConstraints2 = new java.awt.GridBagConstraints ();
      gridBagConstraints2.gridwidth = 0;
      gridBagConstraints2.insets = new java.awt.Insets (0, 5, 0, 0);
      jPanel1.add (quickViewButton, gridBagConstraints2);


      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.gridwidth = 0;
      gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints1.weightx = 1.0;
      add (jPanel1, gridBagConstraints1);

    }//GEN-END:initComponents

  private void sortButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortButtonActionPerformed

    currentSort = evt.getActionCommand(); 
    System.out.println( currentSort );
    sortResults();
     
  }//GEN-LAST:event_sortButtonActionPerformed

  private void quickViewButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickViewButtonActionPerformed
    if ( quickViewButton.isSelected() ) {
      splitPanel.setSplitPosition( oldSplit == 100 ? 50 : oldSplit );
      showHelp( true );
      }
    else {
      oldSplit = splitPanel.getSplitPosition();
      splitPanel.setSplitPosition( 100 );
    }
  }//GEN-LAST:event_quickViewButtonActionPerformed

  private void resultsListKeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resultsListKeyPressed
    // Add your handling code here:
    if ( evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER ||
         evt.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE )
      showHelp();      
  }//GEN-LAST:event_resultsListKeyPressed

  private void resultsListMouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsListMouseClicked
    // Add your handling code here:
    if ( evt.getClickCount() == 2 ) {
      showHelp( ); 
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

      if ( strUrl.startsWith( "nbfs:" ) && strUrl.charAt( 5 ) != '/' ){
        url = new URL( "nbfs:/" + strUrl.substring( 5 ) );
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
  
  
  private void searchComboBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchComboBoxActionPerformed
    if ( searchEngine == null )
      searchButtonActionPerformed( evt );
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

  public static IndexSearch getDefault() {
    if ( indexSearch == null ) {
      indexSearch = new IndexSearch ();
      Workspace workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();  
      Mode myMode = workspace.createMode("JavaDocSearch", "JavaDoc Index-Search", IndexSearch.class.getResource (ICON_RESOURCE));
      myMode.setBounds(new Rectangle( 200, 200, 450, 200 ) );
      myMode.dockInto( indexSearch );

      indexSearch.setName( bundle.getString ("CTL_SEARCH_WindowTitle") );
      indexSearch.setIcon( windowIcon );
    }
    
    return indexSearch;
  }
  
  public void readExternal(final ObjectInput in ) 
      throws java.io.IOException, java.lang.ClassNotFoundException {
        
    super.readExternal( in );
    indexSearch = this;
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
        TopManager.getDefault().notify( new NotifyDescriptor.Message( bundle.getString( "MSG_NoDoc" ) ) );
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
          DocIndexItem ndii = new DocIndexItem(  "PACKAGE ", dii.getPackage(), null, "" );
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
   
    if ( currentSort.equals( "R" ) ) {
      if ( referenceModel == null ) {
        resultsList.setModel( waitModel );
        resultsList.invalidate();
        resultsList.revalidate();
        referenceModel = generateModel( DocIndexItem.REFERENCE_COMPARATOR ); 
      }
      resultsList.setModel( referenceModel );
    }
    else if ( currentSort.equals( "T" ) ) {
      if ( typeModel == null ) {
        resultsList.setModel( waitModel );
        resultsList.invalidate();
        resultsList.revalidate();
        typeModel = generateModel( DocIndexItem.TYPE_COMPARATOR ); 
      }
      resultsList.setModel( typeModel );
    }
    else if ( currentSort.equals( "A" ) ) {
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
  }  
  
  
  
}

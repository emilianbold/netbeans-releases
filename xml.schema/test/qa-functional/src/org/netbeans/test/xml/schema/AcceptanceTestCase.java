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

// package name
package org.netbeans.test.xml.schema;

// Imports required by this java code
import java.awt.Point;
import java.util.zip.CRC32;
import org.netbeans.jemmy.JemmyException;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import junit.framework.TestSuite;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.xml.schema.lib.SchemaMultiView;
import org.netbeans.test.xml.schema.lib.util.Helpers;

import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import org.netbeans.junit.NbTestCase;
import java.util.Properties;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author ca@netbeans.org, michaelnazarov@netbeans.org
 */

public class AcceptanceTestCase extends JellyTestCase {

    // Name of project used for testing
    static final String PROJECT_NAME = "XSDTestProject";

    // Name and extension of schema used for testing.
    static final String TEST_SCHEMA_NAME = "testSchema";
    static final String SCHEMA_EXTENSION = ".xsd";

    // Marker to unzip project one time only.
    // Should be removed after complete migration to simpletest and
    // adding required functionality into framework.
    private static boolean bUnzipped = false;

    // Just a constructor
    public AcceptanceTestCase(String arg0) {
        super(arg0);
    }

    // Creating suite. Order of tests is important.
    public static Test suite( )
    {
      return NbModuleSuite.create(
          NbModuleSuite.createConfiguration( AcceptanceTestCase.class ).addTest(
              "createNewSchema",
              "createSchemaComponents",
              "customizeSchema",
              "checkSourceCRC",
              "refactorComplexType",
              "applyDesignPattern",
              "CreateXMLConstrainedDocument"
           )
           .enableModules( ".*" )
           .clusters( ".*" )
           //.gui( true )
        );
    }

    // One time setup. Used for unzipping sample project into working
    // directory. Should be removed after adding required functionality
    // into testing framework.
    public void setUp( )
    {
      if( !bUnzipped )
      {
      try
      {
        String sBase = getDataDir( ).getPath( ) + File.separator;//System.getProperty( "nbjunit.workdir" ) + File.separator + ".." + File.separator + "data" + File.separator;
        System.out.println( "Unzipping projects.zip into \"" + sBase + "\"..." );
        // Extract zip data
        ZipFile zf = new ZipFile( sBase + "projects.zip" );
        Enumeration<? extends ZipEntry> ent = zf.entries( );
        while( ent.hasMoreElements( ) )
        {
          ZipEntry e = ent.nextElement( );
          String name = e.getName( );
          if( e.isDirectory( ) )
          {
            ( new File( sBase + name ) ).mkdirs( );
          }
          else
          {
            InputStream is = zf.getInputStream( e );
            //File f = new File( name );
            //System.out.println( "-->" + f.getPath( ) );
            OutputStream os = new FileOutputStream( sBase + name );
            int r;
            byte[] b = new byte[ 1024 ];
            while( -1 != ( r = is.read( b ) ) )
              os.write( b, 0, r );
            is.close( );
            os.flush( );
            os.close( );
          }
        }
        zf.close( );

        // Open project
        openDataProjects( PROJECT_NAME );

        bUnzipped = true;
      }
      catch( IOException ex )
      {
        System.out.println( "ERROR: Unzipping projects.zip failed: " + ex.getMessage( ) );
      }
      }
    }

    // Creating schema for testing.
    // 1. Call new file wizard
    // 2. Select XML / XML schema category
    // 3. Press Next
    // 4. Set name for schema
    // 5. Finish wizard
    public void createNewSchema() {
        startTest();

        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke( );
        
        // Workaround for MacOS platform
        // TODO : check platform
        // TODO : remove after normal issue fix
        NewFileWizardOperator.invoke().cancel( );

        // Calling new file wizard
        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        // Slecting XML category
        opNewFileWizard.selectCategory("XML");
        // Selecting XML Schema type
        opNewFileWizard.selectFileType("XML Schema");
        // Press Next button
        opNewFileWizard.next();

        // Customizing schema properties...
        NewJavaFileNameLocationStepOperator opNewFileNameLocationStep = new NewJavaFileNameLocationStepOperator();
        // ...by setting custom name.
        opNewFileNameLocationStep.setObjectName(TEST_SCHEMA_NAME);
        // Finishing creation.
        opNewFileWizard.finish();

        // Checking is schema opened for editing or no
        TopComponentOperator opTopComponent = new TopComponentOperator(
            TEST_SCHEMA_NAME + SCHEMA_EXTENSION
          );
        
        endTest();
    }
    
    // Creating set of components inside schema
    // 1. Adding Complex type named CT
    // 2. Adding Simple type names ST
    // 3. Adding Element named E
    // 4. Adding Attribute named A
    public void createSchemaComponents() {
        startTest();

        // Components types and names
        String[][] aComponentsMenu = {
            {"Complex Type...", "CT"},
            {"Simple Type...",  "ST"},
            {"Element...",      "E"},
            {"Attribute...",    "A"}
        };

        // Getting schema view file tab
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        // Getting first column of schema view
        JListOperator opList = opMultiView.getColumnListOperator(0);

        // For each element in array...
        for (int i = 0; i <aComponentsMenu.length; i++) {
            // Detecting coordinates for calling popup menu for
            // adding required type of component.
            Point p = opList.getClickPoint(0);
            // Calling context popup menu
            opList.clickForPopup(p.x, p.y);
            // Pushing appropriate menu item (Ex.: "Add Element" for "Element")
            new JPopupMenuOperator().pushMenuNoBlock("Add|" + aComponentsMenu[i][0]);
            // Waiting till UI make all required background actions
            Helpers.waitNoEvent();
            // Looking for dialog used for addin
            JDialogOperator opCustomizer = new JDialogOperator();
            // Setting custom component name from array
            new JTextFieldOperator(opCustomizer, 0).setText(aComponentsMenu[i][1]);
            // Press OK button for completing addition
            new JButtonOperator(opCustomizer, "OK").pushNoBlock();
            // Waiting till UI make all required background actions
            Helpers.waitNoEvent();
        }
        
        endTest();
    }

    // Making some changes inside schema
    // 1. Customizing Element name E by setting type to CT
    // 2. Customizing Attribute by setting type to ST
    // 3. Customizing Complex type named CT by adding reference
    //    to Attribute named A.
    public void customizeSchema() {
        startTest();

        // Getting schema view file tab
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        
        //  Customize Element
        // Getting first column of schema view
        JListOperator opList0 = opMultiView.getColumnListOperator(0);
        // Select Elements item in list
        opList0.selectItem("Elements");
        // Get second column of schema view (with list Elements now)
        JListOperator opList1 = opMultiView.getColumnListOperator(1);
        // Calling popup context menu and pushing "Customize" item
        callPopupOnListItem(opList1, "E", "Customize");
        // Getting dialog used for customizing
        JDialogOperator opCustomizer = new JDialogOperator();
        // Selecting "Use existing Type" radio button
        new JRadioButtonOperator(opCustomizer, "Use Existing Type").pushNoBlock();
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Get customizing tree (enabled by radio button above)
        JTreeOperator opTree = new JTreeOperator(opCustomizer);
        // Finding required path in tree
        TreePath treePath = opTree.findPath("Complex Types|CT");
        // Selecting found path
        opTree.selectPath(treePath);
        // Press OK button for completing customizing
        new JButtonOperator(opCustomizer, "OK").pushNoBlock();
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        
        // Customize Attribute
        // Select Attributes list item in forst column
        opList0.selectItem("Attributes");
        // Getting secons column of schema view (with Attributes now)
        opList1 = opMultiView.getColumnListOperator(1);
        // Call popup context menu Customizing for existing attribute
        callPopupOnListItem(opList1, "A", "Customize");
        // Getting dialog used for customizing
        opCustomizer = new JDialogOperator();
        // Selecting "Use existing Type" radio button
        new JRadioButtonOperator(opCustomizer, "Use Existing Type").pushNoBlock();
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Get customizing tree (enabled by radio button above)
        opTree = new JTreeOperator(opCustomizer);
        // Finding required path in tree
        treePath = opTree.findPath("Simple Types|ST");
        // Selecting found path
        opTree.selectPath(treePath);
        // Press OK button for completing customizing
        new JButtonOperator(opCustomizer, "OK").pushNoBlock();
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        
        // Customize Complex Type
        // Select Complex Types list item in forst column
        opList0.selectItem("Complex Types");
        // Getting secons column of schema view (with Complex Types now)
        opList1 = opMultiView.getColumnListOperator(1);
        // Call popup context menu for adding reference to existing comple type
        callPopupOnListItem(opList1, "CT", "Add|Attribute Reference");
        // Getting dialog used for customizing
        opCustomizer = new JDialogOperator();
        // Get customizing tree
        opTree = new JTreeOperator(opCustomizer);
        // Finding required path in tree
        treePath = opTree.findPath("Attributes|A");
        // Selecting found path
        opTree.selectPath(treePath);
        // Press OK button for completing customizing
        new JButtonOperator(opCustomizer, "OK").pushNoBlock();
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        
        endTest();
    }

    // Check source code generated for schema after all manipulation above
    // 1. Getting complete source code of schema
    // 2. Removing all whitespaces
    // 3. Calculating CRC32 for resulting string
    // 4. Comparing with ideal precalculated value
    public void checkSourceCRC() {
        startTest();
        // This is precalculated value found by manual checking of
        // ideal code generated by fully correct build
        final long goldenCRC32 = 227031766L;

        // Getting schema view file tab
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        // Switching to source while we need to get source text
        opMultiView.switchToSource();
        // Get text editor of XML schema
        EditorOperator opEditor = new EditorOperator(TEST_SCHEMA_NAME);
        // Get full source code of schmea
        String strText = opEditor.getText();
        // Switching back to schema view to avoid confusing next test.
        opMultiView.switchToSchema();
        // Remove all spaces, tabs and newlines to avoid platform and
        // configuration related problems due to different indentations,
        // newline characters, empty lines etc.
        strText = strText.replaceAll("[ \t\f\r\n]", "");
        // Show result in log for manual checking in case of troubles
        Helpers.writeJemmyLog("{" + strText + "}");
        System.out.println( "{" + strText + "}" );
        // Creating new instance for CRC32 calculations
        CRC32 crc32 = new CRC32();
        // Get bytes of source code text
        crc32.update(strText.getBytes());
        // Calculate CRC32
        long checkSum = crc32.getValue();
        // Show result in log for manula checking in case of troubles
        Helpers.writeJemmyLog("CRC32=" + checkSum);
        // Compare calculated and ideal precalculated values
        if ( checkSum != goldenCRC32) {
            // Report error if so
            fail("Schema source check sum doesn't match golden value. Required: " + goldenCRC32 + ", calculated: " + checkSum );
        }
        
        endTest();
    }

    // Checking simple schema refactoring
    // 1. Renaming Complex type naemd CT to CT1
    // 2. Checking is Element named E which use renamed Complex type
    //    accepted changes and shows corerct CT1 value now
    public void refactorComplexType() {
        startTest();
        
        // Getting schema view file tab
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        // Get first column of schema view
        JListOperator opList0 = opMultiView.getColumnListOperator(0);
        // Select Complex Types item
        opList0.selectItem("Complex Types");
        // Get second column of schema view (now with Complex Types)
        JListOperator opList1 = opMultiView.getColumnListOperator(1);
        // Call Rename for existing complex type
        callPopupOnListItem(opList1, "CT", "Refactor|Rename...");
        // Get dialog used for refactoring
        JDialogOperator opDialog = new JDialogOperator();
        // Set new name for existing complex type
        new JTextFieldOperator(opDialog).setText("CT1");
        // Press Refactor button for refactoring
        new JButtonOperator(opDialog, "Refactor").pushNoBlock();
        // Wait till refactoring dome and dialog closed
        opDialog.waitClosed();
        // Get first column iof schema view
        opList0 = opMultiView.getColumnListOperator(0);
        // Select Elements item
        opList0.selectItem("Elements");
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Get second column of schema view (now with Elements)
        opList1 = opMultiView.getColumnListOperator(1);
        // Select existing element E
        opList1.selectItem("E");
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Get third column of schema view
        JListOperator opList2 = opMultiView.getColumnListOperator(2);
        // Due to known issue (fixed now) schema view doesn't show
        // correct information and this column might be absent. For
        // avoiding NPE and showing usefull error message this statement
        // was added.
        if( null == opList2 )
          failInvalidSchema( );
        // Select item CT1 in third column
        opList2.selectItem("CT1");
        // Switch to sour5ce view
        opMultiView.switchToSource();
        // Check is schema valid
        boolean bValid = isSchemaValid(TEST_SCHEMA_NAME);
        // switch back to schema view to avoid confusing next tests
        opMultiView.switchToSchema();
        // Report error if schema is not valid
        if (!bValid) {
            failInvalidSchema();
        }
        
        endTest();
    }

    protected void WaitDialogClosed( String sName )
    {
      JDialogOperator jdApplyProgress = new JDialogOperator( sName );
      int iCount = 0;
      while( 10 > iCount++ )
      {
        try
        {
          jdApplyProgress.waitClosed( );
          return;
        }
        catch( JemmyException ex )
        {
          System.out.println( "Apply timeout?" );
        }
      }
      fail( "Apply design pattern asked for too long time." );
    }

    // Apply design pattern for schema    
    // 1. Call popup menu for node represents our schema in project tree
    // 2. Change design pattern options and apply them to schema
    // 3. Check schema is still valid
    public void applyDesignPattern() {
        startTest();
        // Get projects tab
        ProjectsTabOperator pto = new ProjectsTabOperator();
        // Get project tree
        JTreeOperator opTree = pto.tree();
        
        // Get root of used project
        ProjectRootNode prn = pto.getProjectRootNode( PROJECT_NAME );
        // Find node represents out schema under projects root node
        Node node = new Node(
            prn,
            "Source Packages|<default package>|" + TEST_SCHEMA_NAME + SCHEMA_EXTENSION
          );
        // Call popup "Apply Design Patter" for schema node
        node.callPopup().pushMenuNoBlock("Apply Design Pattern...");
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Get wizard used for refactoring
        WizardOperator opWizard = new WizardOperator("Apply Design Pattern");
        // Find and select radio button named "Create a Single Global Element"
        new JRadioButtonOperator(opWizard, "Create a Single Global Element").pushNoBlock();
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Find and select radio button named "Do not Create Type(s)"
        new JRadioButtonOperator(opWizard, "Do not Create Type(s)").pushNoBlock();
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Press Finish button in wizard
        opWizard.finish();
        // Wait progress
        WaitDialogClosed( "Applying Design Pattern" );
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Getting schema view file tab
        SchemaMultiView opMultiView = new SchemaMultiView(TEST_SCHEMA_NAME);
        // Switch to source
        opMultiView.switchToSource();
        // Check is schema is valid
        boolean bValid = isSchemaValid(TEST_SCHEMA_NAME);
        // Switch back to schema view to avoid confusing next test(s)
        opMultiView.switchToSchema();
        // If schema is invalid then report so
        if (!bValid) {
            failInvalidSchema();
        }
        
        endTest();
    }

    // Checking is schema valid
    private boolean isSchemaValid(String strSchemaName) {
        // Assume it's true by default
        boolean bValid = true;
        // Get text editor for schema view source representation
        EditorOperator opEditor = new EditorOperator(strSchemaName);
        Helpers.pause( 1000 ); // try { Thread.sleep( 1000 ); } catch( InterruptedException ex ) { }
        // Call popup menu on schema source code
        opEditor.clickForPopup();
        // Push "Validate XML" menu item
        new JPopupMenuOperator().pushMenu("Validate XML");
        // Waiting till UI make all required background actions
        Helpers.waitNoEvent();
        // Get output operator
        OutputOperator opOutput = new OutputOperator();
        Helpers.pause( 1000 ); // try { Thread.sleep( 1000 ); } catch( InterruptedException ex ) { }
        // Get text from output
        String strOutput = opOutput.getText();
        // Check text against pattern represents correct output
        if (!strOutput.matches("\\D*0 Error\\(s\\),  0 Warning\\(s\\)\\.\\D*")) {
            // Log if any problems found
            Helpers.writeJemmyLog("Validate XML output:\n" + strOutput);
            // Set result as invalid
            bValid = false;
        }
        // Return result to caller
        return bValid;
    }

    // Report invalid schema
    private void failInvalidSchema() {
        // Report error with appropriate message
        fail("Schema validation failed.");
    }

    // Call popup menu on item in list of column schema view
    private void callPopupOnListItem(JListOperator opList, String strItem, String strMenuPath) {
        // Select required item
        opList.selectItem(strItem);
        // Get index of required item
        int index = opList.getSelectedIndex();
        // Detect point for calling popup menu on required item
        Point p = opList.getClickPoint(index);
        // Click for context popup menu on founf point
        opList.clickForPopup(p.x, p.y);
        // Push required menu item in popup menu
        new JPopupMenuOperator().pushMenuNoBlock(strMenuPath);
    }

    // Going down
    public void tearDown() {
        // Some technical staff
        new SaveAllAction().performAPI();
    }

    // Override test starting function, actually can be removed due
    // to fixed issue cause this function to appear
    protected void startTest(){
        // Normal starting
        super.startTest();
        // Close annoing window if any
        Helpers.closeUMLWarningIfOpened();
    }

    public class CImportClickData
    {
      public boolean inshort;
      public int row;
      public int col;
      public int count;
      public int result;
      public String error;
      public String checker;
      public int timeout;
      
      public CImportClickData(
          boolean _inshort,
          int _row,
          int _col,
          int _count,
          int _result,
          String _error,
          String _checker,
          int _timeout
        )
      {
        inshort = _inshort;
        row = _row;
        col = _col;
        count = _count;
        result = _result;
        error = _error;
        checker = _checker;
        timeout = _timeout;
      }
      
      public CImportClickData(
          boolean _inshort,
          int _row,
          int _col,
          int _count,
          int _result,
          String _error,
          String _checker
        )
      {
        this(
            _inshort,
            _row,
            _col,
            _count,
            _result,
            _error,
            _checker,
            750
        );
      }
    }

  // Should early find issues like #148823
  public void CreateXMLConstrainedDocument( )
  {
    ProjectsTabOperator pto = new ProjectsTabOperator( );
    ProjectRootNode prn = pto.getProjectRootNode(
        PROJECT_NAME + "|Source Packages|qa.xmltools.samples"
      );
    prn.select( );
      
    // new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("File|New File...");

    // JDialogOperator jdNew = new JDialogOperator( "New File" );
    // Workaround for MacOS platform
    // TODO : check platform
    // TODO : remove after normal issue fix
    NewFileWizardOperator.invoke( ).cancel( );
    NewFileWizardOperator.invoke( );

    NewFileWizardOperator fwNew = new NewFileWizardOperator( "New File" );
    fwNew.selectCategory( "XML" );
    fwNew.selectFileType( "XML Document" );
    fwNew.next( );

    fwNew.next( );

    JDialogOperator jnew = new JDialogOperator( "New File" );
    JRadioButtonOperator jbut = new JRadioButtonOperator(
        jnew,
        "XML Schema-Constrained Document"
      );
    jbut.setSelected( true );
    jbut.clickMouse( );
    fwNew.next( );

    // === PAGE ===
    jnew = new JDialogOperator( "New File" );
    JButtonOperator jBrowse = new JButtonOperator( jnew, "Browse" );
    jBrowse.pushNoBlock( );

    JDialogOperator jBrowser = new JDialogOperator( "Schema Browser" );
    JTableOperator jto = new JTableOperator( jBrowser, 0 );

    CImportClickData[] aimpData =
    {
      new CImportClickData( true, 0, 0, 2, 3, "Unknown import table state after first click, number of rows: ", null ),
      new CImportClickData( true, 1, 0, 2, 4, "Unknown import table state after second click, number of rows: ", null ),
      new CImportClickData( true, 2, 0, 2, 6, "Unknown import table state after third click, number of rows: ", null, 5000 ),
      new CImportClickData( true, 3, 0, 2, 7, "Unknown import table state after forth click, number of rows: ", null ),
      new CImportClickData( true, 4, 1, 1, 7, "Unknown to click on checkbox. #", null )
    };

    for( CImportClickData cli : aimpData )
    {
      try { Thread.sleep( 1000 ); } catch( InterruptedException ex ) { }

      jto.clickOnCell( cli.row, cli.col, cli.count );
      jto.pushKey( KeyEvent.VK_RIGHT );

      try { Thread.sleep( cli.timeout ); } catch( InterruptedException ex ) { }
      int iRows = jto.getRowCount( );
      if( cli.result != iRows )
      {
        fail(
            "There is problem with creating schema constrained XML document. "
            + cli.error
          );
      }
    }

    JButtonOperator jOk = new JButtonOperator( jBrowser, "OK" );
    jOk.push( );
    jBrowser.waitClosed( );

    JTableOperator jtable = new JTableOperator( jnew, 0 );

    jnew = new JDialogOperator( "New File" );
    jtable.clickOnCell( 0, 0, 1 );

    try
    {
      fwNew.next( );
      fwNew.finish( );
    }
    catch( JemmyException ex )
    {
      fail(
          "There is problem with creating schema constrained XML document: "
          + ex.getMessage( )
        );
    }

    prn = pto.getProjectRootNode(
        PROJECT_NAME + "|Source Packages|qa.xmltools.samples|newXMLDocument.xml"
      );
    prn.select( );
  }

}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.gui.httpserver;

import org.netbeans.junit.NbTestSuite; 
import org.netbeans.junit.NbTestCase; 
import org.netbeans.modules.httpserver.*;

import java.awt.Robot;
import java.awt.event.*;
import javax.swing.*;

import org.openide.awt.*;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.properties.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;

public class Module extends NbTestCase { 

	private String workDir=null;
	private String value=null;
	private String old_value=null;
	private ExplorerOperator explorer=null;
	private static boolean mwm=true;
	private NbDialogOperator dop=null;
	private OptionsOperator optionsOper=null;
	private PropertySheetTabOperator psto=null;
	private String delim="|";	// NOI18N
	private String failMessage="test failed";	// NOI18N

    public Module(String testName) { 
        super(testName); 
    } 

    /** Use for execution inside IDE */ 
    public static void main(java.lang.String[] args) { 
        junit.textui.TestRunner.run(new NbTestSuite(Module.class)); 
    } 

    public void setUp() { 

    }

    private void waitFor(int ms) {
//  On W2k k=1, on Solaris/Linux it's better to set k=2-3
	int k=1;
        new EventTool().waitNoEvent(ms*k);
    }

    private boolean checkDialog(String name) {

	try {
		dop=new NbDialogOperator(name);
	} catch(Exception ex) {
		failMessage="No '"+name+"' dialog appears";
		return false;
	}
	dop.close();
	return true;
    }

    private void switchToHTTPServerNode() {

	explorer = ExplorerOperator.invoke();
	explorer.selectPageRuntime();
	try {
		Node node=new Node(new RuntimeTabOperator().tree(),"HTTP Server");
		new ActionNoBlock(null,"Properties").performPopup(node);
	}catch(Exception e) {
		fail("HTTP server not found");
	}
    }

    private void startHTTPServer(boolean run) {

	explorer = ExplorerOperator.invoke();
	explorer.selectPageRuntime();

        HttpServerSettings server=new HttpServerSettings();

	try {
		Node node=new Node(new RuntimeTabOperator().tree(),"HTTP Server");
		if (run){
				if (server.isRunning()) new ActionNoBlock(null,"Stop HTTP Server").performPopup(node);
				new ActionNoBlock(null,"Start HTTP Server").performPopup(node);
			}
		else new ActionNoBlock(null,"Stop HTTP Server").performPopup(node);
	}catch(Exception e) {
		fail("HTTP can't start");
	}

	waitFor(5000);
    }

    private boolean checkResult(String url, String output) {

	HtmlBrowser browser = new HtmlBrowser ();
	browser.setURL(url);
	browser.requestFocus();

	JFrame jw = new JFrame();
	jw.getContentPane().add(browser);
	jw.show ();

	NbFrameOperator nfo=new NbFrameOperator(jw);
	waitFor(5000);

	String result=new JTextComponentOperator(nfo, 0).getText();

	if (-1==result.indexOf(output)) {
		nfo.close();
		return false;
	} else {
		nfo.close();
		return true;
	}
    }


// Internal HTTP Server Test Specification:  Test suite: 1. Browsing of User Repository


// 1.1
    public void test_1_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer(true);

 	if (!checkResult("http://localhost:8082/repository/","Filesystems")) fail("Error viewing 'Filesystems' page"); // NOI18N
    }


// Internal HTTP Server Test Specification:  Test suite: 2. Accessing Items on IDE Classpath


// 2.1
    public void test_2_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("classpath");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/classpath/","Filesystems")) fail("Error viewing 'Class path' page");	// NOI18N
    }

// 2.2
    public void test_2_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("classpath");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/classpath/org/netbeans/core/resources/templatesFileSystems.html","Select the type of filesystem")) fail("Error viewing 'templatesFileSystems.html' page");	// NOI18N
    }


// Internal HTTP Server Test Specification:  Test suite: 3. Accessing Javadoc


// 3.1
    public void test_3_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("javadoc");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/javadoc/","List of Javadoc mounts")) fail("Error viewing 'Javadoc' page");	// NOI18N
    }

  
// Internal HTTP Server Test Specification:  Test suite: 4. Module Properties


// 4.1 Hosts with Granted Access

// 4.1.1
    public void test_4_1_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N
	tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");

	switchToHTTPServerNode();
        pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/repository/","Filesystems")) fail("Error viewing 'Filesystems' page");	// NOI18N
    }


// 4.2 Port

// 4.2.1
    public void test_4_2_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("16384");	// NOI18N

	switchToHTTPServerNode();
        pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:16384/repository/","Filesystems")) fail("Error viewing 'Filesystems' page");	// NOI18N
    }

// 4.2.2
    public void test_4_2_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("16384");	// NOI18N

	switchToHTTPServerNode();
        pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (checkResult("http://localhost:8082/repository/","Filesystems")) fail("'Filesystems' page can be viewed");	// NOI18N
    }

// 4.2.4
    public void test_4_2_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");

	value=tf.getValue();
	tf.setValue("-9999");	// NOI18N

	if (!checkDialog("Information")) {
		tf.setValue(value);
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.2.5
    public void test_4_2_5() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");

	value=tf.getValue();
	tf.setValue("0");	// NOI18N

	if (!checkDialog("Information")) {
		tf.setValue(value);
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.2.6
    public void test_4_2_6() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");

	value=tf.getValue();
	tf.setValue("65536");	// NOI18N

	if (!checkDialog("Information")) {
		tf.setValue(value);
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.2.7
    public void test_4_2_7() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("65535");	// NOI18N

	switchToHTTPServerNode();
        pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:65535/repository/","Filesystems")) fail("Error viewing 'Filesystems' page");	// NOI18N
    }


// 4.3 Running

// 4.3.1
    public void test_4_3_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("16384");	// NOI18N

	switchToHTTPServerNode();
        pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer(true);
	startHTTPServer(false);

	if (checkResult("http://localhost:16384/repository/","Filesystems")) fail("'Filesystems' page can be viewed");	// NOI18N
    }

// 4.3.2
    public void test_4_3_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("16384");	// NOI18N
	
	switchToHTTPServerNode();
        pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:16384/repository/","Filesystems")) fail("Error viewing 'Filesystems' page");	// NOI18N
    }


// 4.5 Base Filesystems URL 

// 4.5.1
    public void test_4_5_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("");	// NOI18N

	String value=tf.getValue();
	tf.setValue("/repository/");	// NOI18N
	if (!value.equals("/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Filesystems URL' field value");
	}

	pw.close();
    }

// 4.5.2
    public void test_4_5_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (checkResult("http://localhost:8082/repository/","Filesystems")) fail("'Filesystems' page can be viewed");	// NOI18N
    }

// 4.5.3
    public void test_4_5_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/","Filesystems")) fail("Error viewing 'Filesystems' page");	// NOI18N
    }

// 4.5.4
    public void test_4_5_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("newrepository");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/newrepository/","Filesystems")) fail("Error viewing 'Filesystems' page");	// NOI18N
    }

// 4.6 Base Class Path URL 

// 4.6.1
    public void test_4_6_1() {
	
	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf1=new TextFieldProperty(psto,"Base Filesystems URL");
	TextFieldProperty tf2=new TextFieldProperty(psto,"Base Class Path URL");

	tf1.setValue("foo1");	// NOI18N
	tf2.setValue("foo1");	// NOI18N

	if (!checkDialog("Information")) {
		tf1.setValue("repository");	// NOI18N
		tf2.setValue("classpath");	// NOI18N
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.6.2
    public void test_4_6_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("");	// NOI18N

	String value=tf.getValue();
	tf.setValue("/classpath/");	// NOI18N
	if (!value.equals("/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Class Path URL' field value");
	}

	pw.close();
    }

// 4.6.3
    public void test_4_6_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (checkResult("http://localhost:8082/classpath/org/netbeans/core/resources/templatesFileSystems.html","Select the type of filesystem")) fail("'templatesFileSystems.html' page can be viewed");	// NOI18N
    }

// 4.6.4
    public void test_4_6_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/org/netbeans/core/resources/templatesFileSystems.html","Select the type of filesystem")) fail("Error viewing 'templatesFileSystems.html' page");	// NOI18N
    }

// 4.6.5
    public void test_4_6_5() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("newclasspath");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/newclasspath/org/netbeans/core/resources/templatesFileSystems.html","Select the type of filesystem")) fail("Error viewing 'templatesFileSystems.html' page");	// NOI18N
    }


// 4.7 Base Javadoc URL 

// 4.7.1
    public void test_4_7_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf1=new TextFieldProperty(psto,"Base Filesystems URL");
	TextFieldProperty tf2=new TextFieldProperty(psto,"Base Javadoc URL");

	tf1.setValue("foo2");	// NOI18N
	tf2.setValue("foo2");	// NOI18N

	if (!checkDialog("Information")) {
		tf1.setValue("repository");	// NOI18N
		tf2.setValue("javadoc");	// NOI18N
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.7.2
    public void test_4_7_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("");	// NOI18N

	String value=tf.getValue();
	tf.setValue("/javadoc/");	// NOI18N
	if (!value.equals("/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Javadoc URL' field value");
	}

	pw.close();
    }

// 4.7.3
    public void test_4_7_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (checkResult("http://localhost:8082/javadoc/","List of Javadoc mounts")) fail("'Javadoc' page can be viewed");	// NOI18N
    }

// 4.7.4
    public void test_4_7_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/","List of Javadoc mounts")) fail("Error viewing 'Javadoc' page");	// NOI18N
    }

// 4.7.5
    public void test_4_7_5() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("8082");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("newjavadoc");	// NOI18N
	pw.close();

	startHTTPServer(true);

	if (!checkResult("http://localhost:8082/newjavadoc/","List of Javadoc mounts")) fail("Error viewing 'Javadoc' page");	// NOI18N
    }


// 4.8 General Behavior 

// 4.8.1
    public void test_4_8_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.openEditor();

	if (!checkDialog("Hosts with Granted Access")) {
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.8.2
    public void test_4_8_2() {
	
	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		pw.close();
		fail("No 'Hosts with Granted Access' dialog appears");
	}

	JRadioButtonOperator rb=new JRadioButtonOperator(dop,"Any Host");
	rb.doClick();
	JButtonOperator cancel=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION"));
	cancel.doClick();

	value=tf.getValue();
	if (!value.equals("Selected Hosts: ")) {
		pw.close();
		fail("Cancel doesn't work.");
	}

	pw.close();
    }

// 4.8.3
    public void test_4_8_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		pw.close();
		fail("No 'Hosts with Granted Access' dialog appears");
	}

	JRadioButtonOperator rb=new JRadioButtonOperator(dop,"Any Host");
	rb.doClick();
	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();

	value=tf.getValue();
	if (!value.equals("Any Host")){
		pw.close();
		fail("'Any Host' isn't set.");
	}

	pw.close();
    }

// 4.8.4
    public void test_4_8_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
        tf.setValue("foo");	// NOI18N

	if (!checkDialog("Information")) {
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.8.5
    public void test_4_8_5() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Any Host");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		pw.close();
		fail("No 'Hosts with Granted Access' dialog appears");
	}

	JRadioButtonOperator rb=new JRadioButtonOperator(dop,"Selected Hosts");
	rb.doClick();
	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();

	value=tf.getValue();
	if (!value.equals("Selected Hosts: ")) {
		pw.close();
		fail("'Selected Hosts: ' isn't set.");
	}

	pw.close();
    }

// 4.8.6
    public void test_4_8_6() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	old_value=tf.getValue();

	JTextFieldOperator to=tf.textField();
        to.typeText("Selected Hosts: localhost");

	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_6");	// NOI18N
	}

	value=tf.getValue();
	if (!value.equals("Selected Hosts: localhost")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value");
	}

	tf.setValue(old_value);
	pw.close();
    }

// 4.8.7
    public void test_4_8_7() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	old_value=tf.getValue();

	JTextFieldOperator to=tf.textField();
        to.typeText("Selected Hosts: localhost, boo");

	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_7");	// NOI18N
	}

	value=tf.getValue();
	if (!value.equals("Selected Hosts: localhost, boo")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value");
	}

	pw.close();
    }

// 4.8.8
    public void test_4_8_8() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
        tf.setValue("Selected Hosts: localhost, boo");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Hosts with Granted Access' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	if (!value.equals("localhost, boo")) {
		dop.close();
        	pw.close();
		fail("Invalid 'Grant Access to:' textarea value");
	}

	dop.close();
	pw.close();
    }

// 4.8.9
    public void test_4_8_9() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.openEditor();

	if (!checkDialog("Base Class Path URL")) {
		tf.setValue(value);
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.8.10
    public void test_4_8_10() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Class Path URL");
	} catch(Exception ex) {
		pw.close();
		fail("No 'Base Class Path URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	jt.setText("/qqqqqqq/");	// NOI18N
	JButtonOperator cancel=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION"));
	cancel.doClick();

	if (!value.equals(tf.getValue())) {
		pw.close();
		fail("Cancel in 'Base Class Path URL' dialog doesn't work");
	}

	pw.close();
    }

// 4.8.11
    public void test_4_8_11() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	old_value=tf.getValue();

	JTextFieldOperator to=tf.textField();
        to.typeText("/testvalue_cp/");	// NOI18N

	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_12");	// NOI18N
	}

	value=tf.getValue();
	if (!value.equals("/testvalue_cp/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Class Path URL' field value");
	}

	pw.close();
    }

// 4.8.12
    public void test_4_8_12() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
        tf.setValue("/testvalue_cp/");	// NOI18N
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Class Path URL");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Base Class Path URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	if (!value.equals("/testvalue_cp/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Class Path URL' field value");
	}

	dop.close();	
	pw.close();
    }

// 4.8.13
    public void test_4_8_13() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Class Path URL");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Base Class Path URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	jt.setText("classpath");	// NOI18N

	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();
	value=tf.getValue();

	if (!value.equals("/classpath/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Class Path URL' field value");
	}

	pw.close();
    }

// 4.8.14
    public void test_4_8_14() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.openEditor();

	if (!checkDialog("Base Filesystems URL")) {
		tf.setValue(value);
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.8.15
    public void test_4_8_15() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Filesystems URL");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Base Filesystems URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	jt.setText("/qqqqqqq/");	// NOI18N
	JButtonOperator cancel=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION"));
	cancel.doClick();

	if (!value.equals(tf.getValue())) {
		dop.close();
		pw.close();
		fail("Cancel in 'Base Filesystems URL' dialog doesn't work");
	}

	pw.close();
    }

// 4.8.16
    public void test_4_8_16() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	old_value=tf.getValue();

	JTextFieldOperator to=tf.textField();
        to.typeText("/testvalue_fs/");	// NOI18N

	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_17");	// NOI18N
	}

	value=tf.getValue();
	if (!value.equals("/testvalue_fs/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Filesystems URL' field value");
	}

	pw.close();
    }

// 4.8.17
    public void test_4_8_17() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
        tf.setValue("/testvalue_fs/");	// NOI18N
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Filesystems URL");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Base Filesystems URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	if (!value.equals("/testvalue_fs/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Filesystems URL' field value");
	}

	dop.close();
	pw.close();
    }

// 4.8.18
    public void test_4_8_18() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Filesystems URL");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Base Filesystems URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	jt.setText("repository");	// NOI18N

	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();
	value=tf.getValue();

	if (!value.equals("/repository/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Filesystems URL' field value");
	}

	pw.close();
    }

// 4.8.19
    public void test_4_8_19() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.openEditor();

	if (!checkDialog("Base Javadoc URL")) {
		tf.setValue(value);
		pw.close();
		fail(failMessage);
	}

	pw.close();
    }

// 4.8.20
    public void test_4_8_20() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Javadoc URL");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Base Javadoc URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	jt.setText("/qqqqqqq/");	// NOI18N
	JButtonOperator cancel=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION"));
	cancel.doClick();

	if (!value.equals(tf.getValue())) {
		dop.close();
		pw.close();
		fail("Cancel in 'Base Javadoc URL' dialog doesn't work");
	}

	pw.close();
    }

// 4.8.21
    public void test_4_8_21() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	old_value=tf.getValue();

	JTextFieldOperator to=tf.textField();
        to.typeText("/testvalue_jd/");	// NOI18N

	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_22");	// NOI18N
	}

	value=tf.getValue();
	if (!value.equals("/testvalue_jd/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Javadoc URL' field value");
	}

	pw.close();
    }

// 4.8.22
    public void test_4_8_22() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
        tf.setValue("/testvalue_jd/");	// NOI18N
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Javadoc URL");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Base Javadoc URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	if (!value.equals("/testvalue_jd/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Javadoc URL' field value");
	}

	dop.close();
	pw.close();
    }

// 4.8.23
    public void test_4_8_23() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Javadoc URL");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Base Javadoc URL' dialog appears");
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	jt.setText("javadoc");	// NOI18N

	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();
	value=tf.getValue();

	if (!value.equals("/javadoc/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Javadoc URL' field value");
	}

	pw.close();
    }

// 4.8.24
    public void test_4_8_24() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Any Host");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		pw.close();
		fail("No 'Hosts with Granted Access' dialog appears");
	}

	JRadioButtonOperator rb=new JRadioButtonOperator(dop,"Selected Hosts");
	rb.doClick();

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	jt.setText("boo");	// NOI18N

	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();

	value=tf.getValue();
	if (!value.equals("Selected Hosts: boo")) {
		pw.close();
		fail("'Selected Hosts: ' isn't set.");
	}

	pw.close();
    }


// 4.9 Accessibility

// 4.9.1
    public void test_4_9_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Hosts with Granted Access' dialog appears");
	}

	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_ALT);
		rb.keyPress(java.awt.event.KeyEvent.VK_N);
		rb.keyRelease(java.awt.event.KeyEvent.VK_N);
		rb.keyRelease(java.awt.event.KeyEvent.VK_ALT);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_9_1");	// NOI18N
	}

	dop.ok();
	value=tf.getValue();

	if (!value.equals("Any Host")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value");
	}

	pw.close();
    }

// 4.9.2
    public void test_4_9_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Any Host");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Hosts with Granted Access' dialog appears");
	}

	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_ALT);
		rb.keyPress(java.awt.event.KeyEvent.VK_S);
		rb.keyRelease(java.awt.event.KeyEvent.VK_S);
		rb.keyRelease(java.awt.event.KeyEvent.VK_ALT);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_9_2");	// NOI18N
	}

	dop.ok();
	value=tf.getValue();

	if (!value.equals("Selected Hosts: ")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value");
	}

	pw.close();
    }

// 4.9.3
    public void test_4_9_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		dop.close();
		pw.close();
		fail("No 'Hosts with Granted Access' dialog appears");
	}

	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_ALT);
		rb.keyPress(java.awt.event.KeyEvent.VK_G);
		rb.keyRelease(java.awt.event.KeyEvent.VK_G);
		rb.keyRelease(java.awt.event.KeyEvent.VK_ALT);

		rb.keyPress(java.awt.event.KeyEvent.VK_T);
		rb.keyRelease(java.awt.event.KeyEvent.VK_T);
		rb.keyPress(java.awt.event.KeyEvent.VK_E);
		rb.keyRelease(java.awt.event.KeyEvent.VK_E);
		rb.keyPress(java.awt.event.KeyEvent.VK_S);
		rb.keyRelease(java.awt.event.KeyEvent.VK_S);
		rb.keyPress(java.awt.event.KeyEvent.VK_T);
		rb.keyRelease(java.awt.event.KeyEvent.VK_T);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_9_3");	// NOI18N
	}

	dop.ok();
	value=tf.getValue();

	if (!value.equals("Selected Hosts: test")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value");
	}

	pw.close();
    }
  
} 
package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import java.awt.*;

import java.io.*;

import javax.swing.*;

import org.netbeans.jemmy.demo.*;

public class jemmy_032 extends JemmyTest {
    public int runIt(Object obj) {

	try {

	    (new ClassReference("org.netbeans.jemmy.testing.Application_032")).startApplication();

	    WindowOperator winOper = new WindowOperator();
	    JFrameOperator frameOper = new JFrameOperator("Application_032");

	    ComponentOperator cmpo = new ComponentOperator(frameOper, 3);
	    ContainerOperator cnto = new ContainerOperator(frameOper, 3);
	    JComponentOperator jcmpo = new JComponentOperator(frameOper, 3);

	    getOutput().printLine("Component  : " +   cmpo.getSource().toString());
	    getOutput().printLine("Container  : " +   cnto.getSource().toString());
	    getOutput().printLine("JComponent : " +  jcmpo.getSource().toString());
	    if(cmpo.getSource() !=  cnto.getSource() ||
	       cmpo.getSource() != jcmpo.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    getOutput().printLine("Window: " + winOper.getSource().toString());
	    getOutput().printLine("Frame : " + frameOper.getSource().toString());
	    if(winOper.getSource() != frameOper.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    //buttons
	    new JButtonOperator(frameOper, "TtON").push();
	    JFrameOperator bttFrameOper = new JFrameOperator("Buttons");
	    Class[] classes = {
		Class.forName("javax.swing.JButton"), 
		Class.forName("javax.swing.JCheckBox"), 
		Class.forName("javax.swing.JRadioButton"), 
		Class.forName("javax.swing.JToggleButton"),
		Class.forName("javax.swing.JMenuItem")};

	    AbstractButtonOperator abo, tbo, bo;

	    abo = 
		new AbstractButtonOperator(bttFrameOper, classes[0].getName());
	    bo = new JButtonOperator(bttFrameOper, "");
	    getOutput().printLine("Abstract button: " + abo.getSource().toString());
	    getOutput().printLine("Concrete button: " +  bo.getSource().toString());
	    if(abo.getSource() != bo.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    abo = 
		new AbstractButtonOperator(bttFrameOper, classes[1].getName());
	    tbo = new JToggleButtonOperator(bttFrameOper, "");
	    bo = new JCheckBoxOperator(bttFrameOper, "");
	    getOutput().printLine("Abstract button: " + abo.getSource().toString());
	    getOutput().printLine("Toggle   button: " + tbo.getSource().toString());
	    getOutput().printLine("Concrete button: " +  bo.getSource().toString());
	    if(abo.getSource() != bo.getSource() || tbo.getSource() != bo.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    abo = 
		new AbstractButtonOperator(bttFrameOper, classes[2].getName());
	    tbo = new JToggleButtonOperator(bttFrameOper, "", 1);
	    bo = new JRadioButtonOperator(bttFrameOper, "");
	    getOutput().printLine("Abstract button: " + abo.getSource().toString());
	    getOutput().printLine("Toggle   button: " + tbo.getSource().toString());
	    getOutput().printLine("Concrete button: " +  bo.getSource().toString());
	    if(abo.getSource() != bo.getSource() || tbo.getSource() != bo.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    abo = 
		new AbstractButtonOperator(bttFrameOper, classes[3].getName());
	    tbo = new JToggleButtonOperator(bttFrameOper, "", 2);
	    bo = new JToggleButtonOperator(bttFrameOper, "toggle");
	    getOutput().printLine("Abstract button: " + abo.getSource().toString());
	    getOutput().printLine("Toggle   button: " + tbo.getSource().toString());
	    getOutput().printLine("Concrete button: " +  bo.getSource().toString());
	    if(abo.getSource() != bo.getSource() || tbo.getSource() != bo.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    abo = 
		new AbstractButtonOperator(bttFrameOper, classes[4].getName());
	    bo = new JMenuItemOperator(bttFrameOper, "");
	    getOutput().printLine("Abstract button: " + abo.getSource().toString());
	    getOutput().printLine("Concrete button: " +  bo.getSource().toString());
	    if(abo.getSource() != bo.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    bttFrameOper.getSource().setVisible(false);

	    //Menus
	    new JButtonOperator(frameOper, "US").push();
	    JFrameOperator mnFrameOper = new JFrameOperator("uS");
	    JMenuBarOperator mbo = new JMenuBarOperator(mnFrameOper);
	    JMenuOperator mo = new JMenuOperator(mnFrameOper, "");

	    JMenuOperator mo1 = new JMenuOperator(mnFrameOper);
	    if(mo1.getSource() != mo.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(mo1.getSource().toString());
		getOutput().printErrLine(mo.getSource().toString());
		finalize();
		return(1);
	    }

	    if(!testJMenuItem(mo)) {
		finalize();
		return(1);
	    }

	    if(!testJMenu(mo)) {
		finalize();
		return(1);
	    }

	    Thread.sleep(100);
	    mo.pushMenu("", "|", false, false);
	    Thread.sleep(100);
	    JPopupMenuOperator pmo = new JPopupMenuOperator();
	    JMenuItemOperator mio = new JMenuItemOperator(pmo, "");

	    JMenuItemOperator mio1 = new JMenuItemOperator(pmo);
	    if(mio1.getSource() != mio.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(mio1.getSource().toString());
		getOutput().printErrLine(mio.getSource().toString());
		finalize();
		return(1);
	    }

	    getOutput().printLine("Menu bar : " + mbo.getSource().toString());
	    getOutput().printLine("Menu     : " +  mo.getSource().toString());
	    getOutput().printLine("Popup    : " + pmo.getSource().toString());
	    getOutput().printLine("Menu item: " + mio.getSource().toString());

	    mnFrameOper.getSource().setVisible(false);

	    //lists
	    new JButtonOperator(frameOper, "li").push();
	    JFrameOperator lstFrameOper = new JFrameOperator("sts");
	    JComboBoxOperator cbo = new JComboBoxOperator(lstFrameOper, "0");
	    JListOperator lo = new JListOperator(lstFrameOper, "1");
	    JTableOperator tblo = new JTableOperator(lstFrameOper, null);
	    JTreeOperator tro = new JTreeOperator(lstFrameOper, "00");
	    getOutput().printLine("Combo : " +  cbo.getSource().toString());
	    getOutput().printLine("List  : " +   lo.getSource().toString());
	    getOutput().printLine("Table : " + tblo.getSource().toString());
	    getOutput().printLine("Tree  : " +  tro.getSource().toString());

	    lstFrameOper.getSource().setVisible(false);

	    //texts
	    new JButtonOperator(frameOper, "xts").push();
	    JFrameOperator txtFrameOper = new JFrameOperator("Buttons");
	    Class[] classes_txt = {
		Class.forName("javax.swing.JTextField"), 
		Class.forName("javax.swing.JTextArea"), 
		Class.forName("javax.swing.JEditorPane"), 
		Class.forName("javax.swing.JLabel")};

	    JTextComponentOperator atco, ctco;

	    atco = 
		new JTextComponentOperator(txtFrameOper, "JTextField");
	    ctco = new JTextFieldOperator(txtFrameOper, "");
	    getOutput().printLine("Text component: " + atco.getSource().toString());
	    getOutput().printLine("Text field    : " + ctco.getSource().toString());
	    if(atco.getSource() != ctco.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    atco = 
		new JTextComponentOperator(txtFrameOper, "JTextArea");
	    ctco = new JTextAreaOperator(txtFrameOper, "");
	    getOutput().printLine("Text component: " + atco.getSource().toString());
	    getOutput().printLine("Text area     : " + ctco.getSource().toString());
	    if(atco.getSource() != ctco.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    atco = 
		new JTextComponentOperator(txtFrameOper, "JEditorPane");
	    ctco = new JEditorPaneOperator(txtFrameOper, "");
	    getOutput().printLine("Text component: " + atco.getSource().toString());
	    getOutput().printLine("Editor pane   : " + ctco.getSource().toString());
	    if(atco.getSource() != ctco.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    JLabelOperator lbo = new JLabelOperator(txtFrameOper, "");
	    getOutput().printLine("Label : " + lbo.getSource().toString());
	    if(lbo.getSource() == null) {
		getOutput().printErrLine("Should not be null!");
		finalize();
		return(1);
	    }

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

public boolean testJMenu(JMenuOperator jMenuOperator) {
    if(((JMenu)jMenuOperator.getSource()).getDelay() == jMenuOperator.getDelay()) {
        printLine("getDelay does work");
    } else {
        printLine("getDelay does not work");
        printLine(((JMenu)jMenuOperator.getSource()).getDelay());
        printLine(jMenuOperator.getDelay());
        return(false);
    }
    if(((JMenu)jMenuOperator.getSource()).getItemCount() == jMenuOperator.getItemCount()) {
        printLine("getItemCount does work");
    } else {
        printLine("getItemCount does not work");
        printLine(((JMenu)jMenuOperator.getSource()).getItemCount());
        printLine(jMenuOperator.getItemCount());
        return(false);
    }
    if(((JMenu)jMenuOperator.getSource()).getMenuComponentCount() == jMenuOperator.getMenuComponentCount()) {
        printLine("getMenuComponentCount does work");
    } else {
        printLine("getMenuComponentCount does not work");
        printLine(((JMenu)jMenuOperator.getSource()).getMenuComponentCount());
        printLine(jMenuOperator.getMenuComponentCount());
        return(false);
    }
    if(((JMenu)jMenuOperator.getSource()).getPopupMenu() == null &&
       jMenuOperator.getPopupMenu() == null ||
       ((JMenu)jMenuOperator.getSource()).getPopupMenu().equals(jMenuOperator.getPopupMenu())) {
        printLine("getPopupMenu does work");
    } else {
        printLine("getPopupMenu does not work");
        printLine(((JMenu)jMenuOperator.getSource()).getPopupMenu());
        printLine(jMenuOperator.getPopupMenu());
        return(false);
    }
    if(((JMenu)jMenuOperator.getSource()).isPopupMenuVisible() == jMenuOperator.isPopupMenuVisible()) {
        printLine("isPopupMenuVisible does work");
    } else {
        printLine("isPopupMenuVisible does not work");
        printLine(((JMenu)jMenuOperator.getSource()).isPopupMenuVisible());
        printLine(jMenuOperator.isPopupMenuVisible());
        return(false);
    }
    if(((JMenu)jMenuOperator.getSource()).isTopLevelMenu() == jMenuOperator.isTopLevelMenu()) {
        printLine("isTopLevelMenu does work");
    } else {
        printLine("isTopLevelMenu does not work");
        printLine(((JMenu)jMenuOperator.getSource()).isTopLevelMenu());
        printLine(jMenuOperator.isTopLevelMenu());
        return(false);
    }
    return(true);
}

public boolean testJMenuItem(JMenuItemOperator jMenuItemOperator) {
    if(((JMenuItem)jMenuItemOperator.getSource()).getAccelerator() == null &&
       jMenuItemOperator.getAccelerator() == null ||
       ((JMenuItem)jMenuItemOperator.getSource()).getAccelerator().equals(jMenuItemOperator.getAccelerator())) {
        printLine("getAccelerator does work");
    } else {
        printLine("getAccelerator does not work");
        printLine(((JMenuItem)jMenuItemOperator.getSource()).getAccelerator());
        printLine(jMenuItemOperator.getAccelerator());
        return(false);
    }
    if(((JMenuItem)jMenuItemOperator.getSource()).getComponent() == null &&
       jMenuItemOperator.getComponent() == null ||
       ((JMenuItem)jMenuItemOperator.getSource()).getComponent().equals(jMenuItemOperator.getComponent())) {
        printLine("getComponent does work");
    } else {
        printLine("getComponent does not work");
        printLine(((JMenuItem)jMenuItemOperator.getSource()).getComponent());
        printLine(jMenuItemOperator.getComponent());
        return(false);
    }
    if(((JMenuItem)jMenuItemOperator.getSource()).isArmed() == jMenuItemOperator.isArmed()) {
        printLine("isArmed does work");
    } else {
        printLine("isArmed does not work");
        printLine(((JMenuItem)jMenuItemOperator.getSource()).isArmed());
        printLine(jMenuItemOperator.isArmed());
        return(false);
    }
    return(true);
}

}

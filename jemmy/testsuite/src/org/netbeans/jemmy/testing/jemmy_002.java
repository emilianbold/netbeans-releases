package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

public class jemmy_002 extends JemmyTest {
    public int runIt(Object obj) {

	try {

	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_002")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",
					      5000);
	    JFrame win =JFrameOperator.waitJFrame("Application_002", true, true);
	    JFrameOperator wino = new JFrameOperator(win);
	    
	    EventDispatcher.waitQueueEmpty();

	    Demonstrator.setTitle("jemmy_002 test");
	    Demonstrator.nextStep("Type text into text field");

	    JTextFieldOperator tfo = new JTextFieldOperator(JTextFieldOperator.
							    waitJTextField(win,
									   "Text",
									   false, true));

	    JTextFieldOperator tf1 = new JTextFieldOperator(wino);
	    if(tf1.getSource() != tfo.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(tfo.getSource().toString());
		getOutput().printErrLine(tf1.getSource().toString());
		finalize();
		return(1);
	    }

	    tfo.clearText();
	    tfo.typeText("Text has been typed");

	    JTextFieldOperator.waitJTextField(win, "has been typed", false, true);

	    Demonstrator.nextStep("Push menu/menuItem");

	    JMenuBarOperator mbo = new JMenuBarOperator(JMenuBarOperator.findJMenuBar(win));

            if(!mbo.showMenuItem("menu|submenu", "|").getText().equals("submenu")) {
		finalize();
		return(1);
            }

            if(!mbo.showMenuItem("menu", "|").getText().equals("menu")) {
		finalize();
		return(1);
            }

            if(!mbo.showMenuItem("menu|submenu|subsubmenu|menuItem", "|").getText().equals("menuItem")) {
		finalize();
		return(1);
            }

            mbo.pushMenu("menu", "|");

            if(!mbo.showMenuItem("menu|submenu|subsubmenu", "|").getText().equals("subsubmenu")) {
		finalize();
		return(1);
            }


	    mbo.pushMenu("menu|submenu|subsubmenu|menuItem", "|", true, true);
	    JLabelOperator lbo = new JLabelOperator(JLabelOperator.waitJLabel(win, "Menu \"menu/menuItem\" has been pushed", false, true));


	    
	    Demonstrator.nextStep("Push menu0");

	    mbo.pushMenu("menu0", "|", true, true);

	    Demonstrator.nextStep("Push menu1Item");

	    mbo.pushMenu("menu1Item", "|", true, true);
	    JLabelOperator.waitJLabel(win, "Menu \"menu1Item\" has been pushed", false, true);

	    Demonstrator.nextStep("Push button");

	    JButtonOperator bo = new JButtonOperator(JButtonOperator.
						     waitJButton(win,
								 "button",
								 true, true));

	    bo.push();
	    JLabelOperator.waitJLabel(win, "Button has been pushed", false, true);

	    Demonstrator.showFinalComment("Test passed");

	    if(!testAbstractButton(bo)) {
		finalize();
		return(1);
	    }

	    if(!testJButton(bo)) {
		finalize();
		return(1);
	    }

	    if(!testJMenuBar(mbo)) {
		finalize();
		return(1);
	    }

	    if(!testJLabel(lbo)) {
		finalize();
		return(1);
	    }

	} catch(TimeoutExpiredException e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}

	finalize();
	return(0);
    }

public boolean testAbstractButton(AbstractButtonOperator abstractButtonOperator) {
    if(((AbstractButton)abstractButtonOperator.getSource()).getActionCommand() == null &&
       abstractButtonOperator.getActionCommand() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getActionCommand().equals(abstractButtonOperator.getActionCommand())) {
        printLine("getActionCommand does work");
    } else {
        printLine("getActionCommand does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getActionCommand());
        printLine(abstractButtonOperator.getActionCommand());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getDisabledIcon() == null &&
       abstractButtonOperator.getDisabledIcon() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getDisabledIcon().equals(abstractButtonOperator.getDisabledIcon())) {
        printLine("getDisabledIcon does work");
    } else {
        printLine("getDisabledIcon does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getDisabledIcon());
        printLine(abstractButtonOperator.getDisabledIcon());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getDisabledSelectedIcon() == null &&
       abstractButtonOperator.getDisabledSelectedIcon() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getDisabledSelectedIcon().equals(abstractButtonOperator.getDisabledSelectedIcon())) {
        printLine("getDisabledSelectedIcon does work");
    } else {
        printLine("getDisabledSelectedIcon does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getDisabledSelectedIcon());
        printLine(abstractButtonOperator.getDisabledSelectedIcon());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getHorizontalAlignment() == abstractButtonOperator.getHorizontalAlignment()) {
        printLine("getHorizontalAlignment does work");
    } else {
        printLine("getHorizontalAlignment does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getHorizontalAlignment());
        printLine(abstractButtonOperator.getHorizontalAlignment());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getHorizontalTextPosition() == abstractButtonOperator.getHorizontalTextPosition()) {
        printLine("getHorizontalTextPosition does work");
    } else {
        printLine("getHorizontalTextPosition does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getHorizontalTextPosition());
        printLine(abstractButtonOperator.getHorizontalTextPosition());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getIcon() == null &&
       abstractButtonOperator.getIcon() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getIcon().equals(abstractButtonOperator.getIcon())) {
        printLine("getIcon does work");
    } else {
        printLine("getIcon does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getIcon());
        printLine(abstractButtonOperator.getIcon());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getMargin() == null &&
       abstractButtonOperator.getMargin() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getMargin().equals(abstractButtonOperator.getMargin())) {
        printLine("getMargin does work");
    } else {
        printLine("getMargin does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getMargin());
        printLine(abstractButtonOperator.getMargin());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getMnemonic() == abstractButtonOperator.getMnemonic()) {
        printLine("getMnemonic does work");
    } else {
        printLine("getMnemonic does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getMnemonic());
        printLine(abstractButtonOperator.getMnemonic());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getModel() == null &&
       abstractButtonOperator.getModel() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getModel().equals(abstractButtonOperator.getModel())) {
        printLine("getModel does work");
    } else {
        printLine("getModel does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getModel());
        printLine(abstractButtonOperator.getModel());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getPressedIcon() == null &&
       abstractButtonOperator.getPressedIcon() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getPressedIcon().equals(abstractButtonOperator.getPressedIcon())) {
        printLine("getPressedIcon does work");
    } else {
        printLine("getPressedIcon does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getPressedIcon());
        printLine(abstractButtonOperator.getPressedIcon());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getRolloverIcon() == null &&
       abstractButtonOperator.getRolloverIcon() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getRolloverIcon().equals(abstractButtonOperator.getRolloverIcon())) {
        printLine("getRolloverIcon does work");
    } else {
        printLine("getRolloverIcon does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getRolloverIcon());
        printLine(abstractButtonOperator.getRolloverIcon());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getRolloverSelectedIcon() == null &&
       abstractButtonOperator.getRolloverSelectedIcon() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getRolloverSelectedIcon().equals(abstractButtonOperator.getRolloverSelectedIcon())) {
        printLine("getRolloverSelectedIcon does work");
    } else {
        printLine("getRolloverSelectedIcon does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getRolloverSelectedIcon());
        printLine(abstractButtonOperator.getRolloverSelectedIcon());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getSelectedIcon() == null &&
       abstractButtonOperator.getSelectedIcon() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getSelectedIcon().equals(abstractButtonOperator.getSelectedIcon())) {
        printLine("getSelectedIcon does work");
    } else {
        printLine("getSelectedIcon does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getSelectedIcon());
        printLine(abstractButtonOperator.getSelectedIcon());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getText() == null &&
       abstractButtonOperator.getText() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getText().equals(abstractButtonOperator.getText())) {
        printLine("getText does work");
    } else {
        printLine("getText does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getText());
        printLine(abstractButtonOperator.getText());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getUI() == null &&
       abstractButtonOperator.getUI() == null ||
       ((AbstractButton)abstractButtonOperator.getSource()).getUI().equals(abstractButtonOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getUI());
        printLine(abstractButtonOperator.getUI());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getVerticalAlignment() == abstractButtonOperator.getVerticalAlignment()) {
        printLine("getVerticalAlignment does work");
    } else {
        printLine("getVerticalAlignment does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getVerticalAlignment());
        printLine(abstractButtonOperator.getVerticalAlignment());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).getVerticalTextPosition() == abstractButtonOperator.getVerticalTextPosition()) {
        printLine("getVerticalTextPosition does work");
    } else {
        printLine("getVerticalTextPosition does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).getVerticalTextPosition());
        printLine(abstractButtonOperator.getVerticalTextPosition());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).isBorderPainted() == abstractButtonOperator.isBorderPainted()) {
        printLine("isBorderPainted does work");
    } else {
        printLine("isBorderPainted does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).isBorderPainted());
        printLine(abstractButtonOperator.isBorderPainted());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).isContentAreaFilled() == abstractButtonOperator.isContentAreaFilled()) {
        printLine("isContentAreaFilled does work");
    } else {
        printLine("isContentAreaFilled does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).isContentAreaFilled());
        printLine(abstractButtonOperator.isContentAreaFilled());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).isFocusPainted() == abstractButtonOperator.isFocusPainted()) {
        printLine("isFocusPainted does work");
    } else {
        printLine("isFocusPainted does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).isFocusPainted());
        printLine(abstractButtonOperator.isFocusPainted());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).isRolloverEnabled() == abstractButtonOperator.isRolloverEnabled()) {
        printLine("isRolloverEnabled does work");
    } else {
        printLine("isRolloverEnabled does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).isRolloverEnabled());
        printLine(abstractButtonOperator.isRolloverEnabled());
        return(false);
    }
    if(((AbstractButton)abstractButtonOperator.getSource()).isSelected() == abstractButtonOperator.isSelected()) {
        printLine("isSelected does work");
    } else {
        printLine("isSelected does not work");
        printLine(((AbstractButton)abstractButtonOperator.getSource()).isSelected());
        printLine(abstractButtonOperator.isSelected());
        return(false);
    }
    return(true);
}

public boolean testJButton(JButtonOperator jButtonOperator) {
    if(((JButton)jButtonOperator.getSource()).isDefaultButton() == jButtonOperator.isDefaultButton()) {
        printLine("isDefaultButton does work");
    } else {
        printLine("isDefaultButton does not work");
        printLine(((JButton)jButtonOperator.getSource()).isDefaultButton());
        printLine(jButtonOperator.isDefaultButton());
        return(false);
    }
    if(((JButton)jButtonOperator.getSource()).isDefaultCapable() == jButtonOperator.isDefaultCapable()) {
        printLine("isDefaultCapable does work");
    } else {
        printLine("isDefaultCapable does not work");
        printLine(((JButton)jButtonOperator.getSource()).isDefaultCapable());
        printLine(jButtonOperator.isDefaultCapable());
        return(false);
    }
    return(true);
}

public boolean testJMenuBar(JMenuBarOperator jMenuBarOperator) {
    if(((JMenuBar)jMenuBarOperator.getSource()).getMargin() == null &&
       jMenuBarOperator.getMargin() == null ||
       ((JMenuBar)jMenuBarOperator.getSource()).getMargin().equals(jMenuBarOperator.getMargin())) {
        printLine("getMargin does work");
    } else {
        printLine("getMargin does not work");
        printLine(((JMenuBar)jMenuBarOperator.getSource()).getMargin());
        printLine(jMenuBarOperator.getMargin());
        return(false);
    }
    if(((JMenuBar)jMenuBarOperator.getSource()).getMenuCount() == jMenuBarOperator.getMenuCount()) {
        printLine("getMenuCount does work");
    } else {
        printLine("getMenuCount does not work");
        printLine(((JMenuBar)jMenuBarOperator.getSource()).getMenuCount());
        printLine(jMenuBarOperator.getMenuCount());
        return(false);
    }
    if(((JMenuBar)jMenuBarOperator.getSource()).getSelectionModel() == null &&
       jMenuBarOperator.getSelectionModel() == null ||
       ((JMenuBar)jMenuBarOperator.getSource()).getSelectionModel().equals(jMenuBarOperator.getSelectionModel())) {
        printLine("getSelectionModel does work");
    } else {
        printLine("getSelectionModel does not work");
        printLine(((JMenuBar)jMenuBarOperator.getSource()).getSelectionModel());
        printLine(jMenuBarOperator.getSelectionModel());
        return(false);
    }
    if(((JMenuBar)jMenuBarOperator.getSource()).getUI() == null &&
       jMenuBarOperator.getUI() == null ||
       ((JMenuBar)jMenuBarOperator.getSource()).getUI().equals(jMenuBarOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JMenuBar)jMenuBarOperator.getSource()).getUI());
        printLine(jMenuBarOperator.getUI());
        return(false);
    }
    if(((JMenuBar)jMenuBarOperator.getSource()).isBorderPainted() == jMenuBarOperator.isBorderPainted()) {
        printLine("isBorderPainted does work");
    } else {
        printLine("isBorderPainted does not work");
        printLine(((JMenuBar)jMenuBarOperator.getSource()).isBorderPainted());
        printLine(jMenuBarOperator.isBorderPainted());
        return(false);
    }
    if(((JMenuBar)jMenuBarOperator.getSource()).isSelected() == jMenuBarOperator.isSelected()) {
        printLine("isSelected does work");
    } else {
        printLine("isSelected does not work");
        printLine(((JMenuBar)jMenuBarOperator.getSource()).isSelected());
        printLine(jMenuBarOperator.isSelected());
        return(false);
    }
    return(true);
}

public boolean testJLabel(JLabelOperator jLabelOperator) {
    if(((JLabel)jLabelOperator.getSource()).getDisabledIcon() == null &&
       jLabelOperator.getDisabledIcon() == null ||
       ((JLabel)jLabelOperator.getSource()).getDisabledIcon().equals(jLabelOperator.getDisabledIcon())) {
        printLine("getDisabledIcon does work");
    } else {
        printLine("getDisabledIcon does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getDisabledIcon());
        printLine(jLabelOperator.getDisabledIcon());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getDisplayedMnemonic() == jLabelOperator.getDisplayedMnemonic()) {
        printLine("getDisplayedMnemonic does work");
    } else {
        printLine("getDisplayedMnemonic does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getDisplayedMnemonic());
        printLine(jLabelOperator.getDisplayedMnemonic());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getHorizontalAlignment() == jLabelOperator.getHorizontalAlignment()) {
        printLine("getHorizontalAlignment does work");
    } else {
        printLine("getHorizontalAlignment does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getHorizontalAlignment());
        printLine(jLabelOperator.getHorizontalAlignment());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getHorizontalTextPosition() == jLabelOperator.getHorizontalTextPosition()) {
        printLine("getHorizontalTextPosition does work");
    } else {
        printLine("getHorizontalTextPosition does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getHorizontalTextPosition());
        printLine(jLabelOperator.getHorizontalTextPosition());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getIcon() == null &&
       jLabelOperator.getIcon() == null ||
       ((JLabel)jLabelOperator.getSource()).getIcon().equals(jLabelOperator.getIcon())) {
        printLine("getIcon does work");
    } else {
        printLine("getIcon does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getIcon());
        printLine(jLabelOperator.getIcon());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getIconTextGap() == jLabelOperator.getIconTextGap()) {
        printLine("getIconTextGap does work");
    } else {
        printLine("getIconTextGap does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getIconTextGap());
        printLine(jLabelOperator.getIconTextGap());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getLabelFor() == null &&
       jLabelOperator.getLabelFor() == null ||
       ((JLabel)jLabelOperator.getSource()).getLabelFor().equals(jLabelOperator.getLabelFor())) {
        printLine("getLabelFor does work");
    } else {
        printLine("getLabelFor does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getLabelFor());
        printLine(jLabelOperator.getLabelFor());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getText() == null &&
       jLabelOperator.getText() == null ||
       ((JLabel)jLabelOperator.getSource()).getText().equals(jLabelOperator.getText())) {
        printLine("getText does work");
    } else {
        printLine("getText does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getText());
        printLine(jLabelOperator.getText());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getUI() == null &&
       jLabelOperator.getUI() == null ||
       ((JLabel)jLabelOperator.getSource()).getUI().equals(jLabelOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getUI());
        printLine(jLabelOperator.getUI());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getVerticalAlignment() == jLabelOperator.getVerticalAlignment()) {
        printLine("getVerticalAlignment does work");
    } else {
        printLine("getVerticalAlignment does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getVerticalAlignment());
        printLine(jLabelOperator.getVerticalAlignment());
        return(false);
    }
    if(((JLabel)jLabelOperator.getSource()).getVerticalTextPosition() == jLabelOperator.getVerticalTextPosition()) {
        printLine("getVerticalTextPosition does work");
    } else {
        printLine("getVerticalTextPosition does not work");
        printLine(((JLabel)jLabelOperator.getSource()).getVerticalTextPosition());
        printLine(jLabelOperator.getVerticalTextPosition());
        return(false);
    }
    return(true);
}

}

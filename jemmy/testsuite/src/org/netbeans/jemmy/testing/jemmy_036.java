package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.*;

import java.awt.*;

import javax.swing.*;

public class jemmy_036 extends JemmyTest {

    public int runIt(Object obj) {

	try {

	    (new ClassReference("org.netbeans.jemmy.testing.Application_036")).startApplication();

	    JFrame win =JFrameOperator.waitJFrame("Application_036", true, true);

	    FrameOperator fo = new FrameOperator(win);

	    ChoiceOperator co = new ChoiceOperator(fo);
	    co.selectItem("One");
	    if(!co.getSelectedItem().equals("One")) {
		getOutput().printErrLine("\"One\" has not been selected");
		finalize();
		return(1);
	    }
	    co.selectItem("Two");
	    if(!co.getSelectedItem().equals("Two")) {
		getOutput().printErrLine("\"Two\" has not been selected");
		finalize();
		return(1);
	    }
	    co.selectItem("Three");
	    if(!co.getSelectedItem().equals("Three")) {
		getOutput().printErrLine("\"Three\" has not been selected");
		finalize();
		return(1);
	    }

	    CheckboxOperator cbo = new CheckboxOperator(fo);
	    cbo.changeSelection(true);
	    if(!cbo.getState()) {
		getOutput().printErrLine("Checkbox has not been selected");
		finalize();
		return(1);
	    }

	    ButtonOperator bo = new ButtonOperator(fo);
	    bo.push();
	    new LabelOperator(fo, "button pushed");

	    cbo.changeSelection(false);
	    if(cbo.getState()) {
		getOutput().printErrLine("Checkbox has not been unselected");
		finalize();
		return(1);
	    }

	    TextFieldOperator tco = new TextFieldOperator(fo);
	    tco.clearText();
	    tco.typeText("Old text");
	    tco.enterText("New text");
	    new TextFieldOperator(fo, "New text");

	    ListOperator lo = new ListOperator(fo);
	    lo.selectItem(0);
	    if(lo.getSelectedIndex() != 0) {
		getOutput().printErrLine("0'th item has not been selected");
		finalize();
		return(1);
	    }
	    lo.selectItem(1);
	    if(lo.getSelectedIndex() != 1) {
		getOutput().printErrLine("1'th item has not been selected");
		finalize();
		return(1);
	    }
	    lo.selectItem(2);
	    if(lo.getSelectedIndex() != 2) {
		getOutput().printErrLine("2'th item has not been selected");
		finalize();
		return(1);
	    }
	    lo.selectItem(3);
	    if(lo.getSelectedIndex() != 3) {
		getOutput().printErrLine("3'th item has not been selected");
		finalize();
		return(1);
	    }

	    TextAreaOperator tao = new TextAreaOperator(fo);
	    tao.selectText(0, 10);
	    tao.enterText("Very\nNew\nFew\nLines");
	    new TextAreaOperator(fo, "Very\nNew\nFew\nLines");

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

}

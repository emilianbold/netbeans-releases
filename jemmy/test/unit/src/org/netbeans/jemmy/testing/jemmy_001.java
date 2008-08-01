package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;

import java.io.PrintWriter;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;

import javax.swing.text.JTextComponent;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.util.DefaultVisualizer;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.jemmy.util.NameComponentChooser;

public class jemmy_001 extends JemmyTest {
    public int runIt(Object obj) {

	try {
	    Exception e = (Exception)(new ActionProducer(new org.netbeans.jemmy.Action() {
		    public Object launch(Object obj) {
			try {
			    (new ClassReference("org.netbeans.jemmy.testing.Application_001")).startApplication();
			} catch(Exception ex) {
			    return(ex);
			}
			return(null);
		    }
		    public String getDescription() {
			return("");
		    }
		}, false).produceAction(null));

	    if(e != null) {
		throw(e);
	    }

                System.setProperty("jemmy.dump.a11y", "on");
	    JDialog win = JDialogOperator.waitJDialog("Application_001", true, true);

            Operator.setDefaultComponentVisualizer(((DefaultVisualizer)Operator.getDefaultComponentVisualizer()).cloneThis());

	    JDialogOperator fo = new JDialogOperator(win);
	    JDialogOperator fo2 = new JDialogOperator();
	    DialogOperator fo3 = new DialogOperator();
	    if(fo2.getSource() != fo.getSource() ||
	       fo3.getSource() != fo.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(fo.getSource().toString());
		getOutput().printErrLine(fo2.getSource().toString());
		getOutput().printErrLine(fo3.getSource().toString());
		finalize();
		return(1);
	    }
	    
	    Window window = new ComponentOperator(win).getWindow();
	    getOutput().printErrLine("getWindow returned " + window.toString());
	    getOutput().printErrLine("Expected           " + win.toString());
	    if(window != win) {
		finalize();
		return(1);
	    }
	    
	    JScrollPaneOperator scroller = new JScrollPaneOperator(JScrollPaneOperator.
								   findJScrollPane(win, 
										   ComponentSearcher.getTrueChooser("Scroll pane")));

	    JComboBoxOperator operator_1 = new JComboBoxOperator(JComboBoxOperator.
								 findJComboBox(win,
									       "editable_one",
									       true, true,
									       0));

	    JComboBoxOperator operator_10 = new JComboBoxOperator(fo);
	    JComboBoxOperator operator_11 = new JComboBoxOperator(fo, "editable_one");
	    JComboBoxOperator operator_12 = new JComboBoxOperator(fo, new NameComponentChooser("editable"));
	    if(operator_10.getSource() != operator_1.getSource() ||
	       operator_11.getSource() != operator_1.getSource() ||
	       operator_12.getSource() != operator_1.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(operator_1.getSource().toString());
		getOutput().printErrLine(operator_10.getSource().toString());
		getOutput().printErrLine(operator_11.getSource().toString());
		getOutput().printErrLine(operator_12.getSource().toString());
		finalize();
		return(1);
	    }
	    
	    JComboBoxOperator operator_2 = new JComboBoxOperator(JComboBoxOperator.
								 findJComboBox(win,
									       "non_editable_one",
									       true, true,
									       0));

	    getOutput().printLine("getItemCount returned " + Integer.toString(operator_1.getItemCount()));
	    if(operator_1.getItemCount() != 4) {
		finalize();
		return(1);
	    }

	    JListOperator lo0 = new JListOperator(fo);
	    lo0.clickOnItem("two");
	    JListOperator lo1 = new JListOperator(fo, "two", 1, 0);
	    JListOperator lo2 = new JListOperator(fo, "two");
	    JListOperator lo3 = new JListOperator(fo, new NameComponentChooser("list"));
	    if(lo1.getSource() != lo0.getSource() ||
	       lo2.getSource() != lo0.getSource() ||
	       lo3.getSource() != lo0.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(lo0.getSource().toString());
		getOutput().printErrLine(lo1.getSource().toString());
		getOutput().printErrLine(lo2.getSource().toString());
		getOutput().printErrLine(lo3.getSource().toString());
		finalize();
		return(1);
	    }

	    Demonstrator.setTitle("jemmy_001 test");

	    Demonstrator.nextStep("Select second element in the first combobox");

	    scroller.scrollToTop();

	    operator_1.selectItem("editable_two", true, true);
	    operator_1.waitItemSelected("editable_two");

	    JComboBoxOperator.waitJComboBox(win, "editable_two", true, true, -1);

	    if(operator_1.getSelectedIndex() != 1) {
		getOutput().printLine("getSelectedIndex returns " + Integer.toString(operator_1.getSelectedIndex()));
		finalize();
		return(1);
	    } else {
		getOutput().printLine("getSelectedIndex returns " + Integer.toString(operator_1.getSelectedIndex()));
	    }

	    if(!operator_1.getSelectedItem().equals("editable_two")) {
		getOutput().printLine("getSelectedItem returns " + operator_1.getSelectedItem().toString());
		finalize();
		return(1);
	    } else {
		getOutput().printLine("getSelectedItem returns " + operator_1.getSelectedItem().toString());
	    }

	    if(!operator_1.getItemAt(1).equals("editable_two")) {
		getOutput().printLine("getItemAt(1) returns " + operator_1.getItemAt(1).toString());
		finalize();
		return(1);
	    } else {
		getOutput().printLine("getItemAt(1) returns " + operator_1.getItemAt(1).toString());
	    }

	    Demonstrator.nextStep("Select second element in the second combobox");

	    scroller.scrollToBottom();

	    operator_2.selectItem("non_editable_two", true, true);
	    JComboBoxOperator.waitJComboBox(win, "non_editable_two", true, true, -1);

	    if(operator_2.getSelectedIndex() != 1) {
		getOutput().printLine("getSelectedIndex returns " + Integer.toString(operator_2.getSelectedIndex()));
		finalize();
		return(1);
	    } else {
		getOutput().printLine("getSelectedIndex returns " + Integer.toString(operator_2.getSelectedIndex()));
	    }

	    if(!operator_2.getSelectedItem().equals("non_editable_two")) {
		getOutput().printLine("getSelectedItem returns " + operator_2.getSelectedItem().toString());
		finalize();
		return(1);
	    } else {
		getOutput().printLine("getSelectedItem returns " + operator_2.getSelectedItem().toString());
	    }

	    Demonstrator.nextStep("Clear text in the first combobox");

	    scroller.scrollToTop();

	    operator_1.clearText();

	    Demonstrator.nextStep("Type new text in the first combobox");

	    JTextFieldOperator.waitJTextField(win, "", true, true);

	    operator_1.typeText("editable_old");
	    JTextFieldOperator.waitJTextField(win, "editable_old", true, true);

	    Demonstrator.nextStep("Change text in the first combobox");

	    JTextFieldOperator tfo = new JTextFieldOperator(operator_1.findJTextField());
	    tfo.selectText("old");
	    tfo.typeText("new");

	    JTextFieldOperator.waitJTextField(win, "editable_new", true, true);

	    operator_1.enterText("editable_five");

	    Demonstrator.nextStep("Select new item in the first combobox");

	    operator_1.selectItem("five", false, true);

	    JComboBoxOperator.waitJComboBox(win, "editable_five", true, true, -1);

	    Demonstrator.nextStep("Select third item in the second combobox");

	    scroller.scrollToBottom();

	    operator_2.selectItem(2);
	    JComboBoxOperator.waitJComboBox(win, "non_editable_three", true, true, -1);

	    JComboBoxOperator operator_00 = new JComboBoxOperator(fo, new NameComponentChooser("non_editable"));
	    JComboBoxOperator operator_01 = new JComboBoxOperator(fo, new NameComponentChooser("on_e", new Operator.DefaultStringComparator(false, false)));
	    if(operator_00.getSource() != operator_01.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(operator_00.getSource().toString());
		getOutput().printErrLine(operator_01.getSource().toString());
		finalize();
		return(1);
	    }

            fo.getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 1000);
            try {
                new JComboBoxOperator(fo, new NameComponentChooser("non_edit"));
		getOutput().printError("Found by subname!");
		finalize();
		return(1);
            } catch(Exception ee) {
            }


	    if(!testComponent(operator_1)) {
		finalize();
		return(1);
	    }

	    if(!testContainer(operator_1)) {
		finalize();
		return(1);
	    }

	    if(!testJComponent(operator_1)) {
		finalize();
		return(1);
	    }

	    if(!testJTextComponent(tfo)) {
		finalize();
		return(1);
	    }

	    if(!testJTextField(tfo)) {
		finalize();
		return(1);
	    }

	    if(!testWindow(fo)) {
		finalize();
		return(1);
	    }

	    if(!testJComboBox(operator_1)) {
		finalize();
		return(1);
	    }

	    Demonstrator.showFinalComment("Test passed");

	    Dumper.dumpComponent(operator_1.getSource(), getOutput().getOutput());

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

public boolean testComponent(ComponentOperator componentOperator) {
    if(componentOperator.getSource().getAlignmentX() == componentOperator.getAlignmentX()) {
        printLine("getAlignmentX does work");
    } else {
        printLine("getAlignmentX does not work");
        printLine(componentOperator.getSource().getAlignmentX());
        printLine(componentOperator.getAlignmentX());
        return(false);
    }
    if(componentOperator.getSource().getAlignmentY() == componentOperator.getAlignmentY()) {
        printLine("getAlignmentY does work");
    } else {
        printLine("getAlignmentY does not work");
        printLine(componentOperator.getSource().getAlignmentY());
        printLine(componentOperator.getAlignmentY());
        return(false);
    }
    if(componentOperator.getSource().getBackground() == null &&
       componentOperator.getBackground() == null ||
       componentOperator.getSource().getBackground().equals(componentOperator.getBackground())) {
        printLine("getBackground does work");
    } else {
        printLine("getBackground does not work");
        printLine(componentOperator.getSource().getBackground());
        printLine(componentOperator.getBackground());
        return(false);
    }
    if(componentOperator.getSource().getBounds() == null &&
       componentOperator.getBounds() == null ||
       componentOperator.getSource().getBounds().equals(componentOperator.getBounds())) {
        printLine("getBounds does work");
    } else {
        printLine("getBounds does not work");
        printLine(componentOperator.getSource().getBounds());
        printLine(componentOperator.getBounds());
        return(false);
    }
    if(componentOperator.getSource().getColorModel() == null &&
       componentOperator.getColorModel() == null ||
       componentOperator.getSource().getColorModel().equals(componentOperator.getColorModel())) {
        printLine("getColorModel does work");
    } else {
        printLine("getColorModel does not work");
        printLine(componentOperator.getSource().getColorModel());
        printLine(componentOperator.getColorModel());
        return(false);
    }
    if(componentOperator.getSource().getComponentOrientation() == null &&
       componentOperator.getComponentOrientation() == null ||
       componentOperator.getSource().getComponentOrientation().equals(componentOperator.getComponentOrientation())) {
        printLine("getComponentOrientation does work");
    } else {
        printLine("getComponentOrientation does not work");
        printLine(componentOperator.getSource().getComponentOrientation());
        printLine(componentOperator.getComponentOrientation());
        return(false);
    }
    if(componentOperator.getSource().getCursor() == null &&
       componentOperator.getCursor() == null ||
       componentOperator.getSource().getCursor().equals(componentOperator.getCursor())) {
        printLine("getCursor does work");
    } else {
        printLine("getCursor does not work");
        printLine(componentOperator.getSource().getCursor());
        printLine(componentOperator.getCursor());
        return(false);
    }
    if(componentOperator.getSource().getDropTarget() == null &&
       componentOperator.getDropTarget() == null ||
       componentOperator.getSource().getDropTarget().equals(componentOperator.getDropTarget())) {
        printLine("getDropTarget does work");
    } else {
        printLine("getDropTarget does not work");
        printLine(componentOperator.getSource().getDropTarget());
        printLine(componentOperator.getDropTarget());
        return(false);
    }
    if(componentOperator.getSource().getFont() == null &&
       componentOperator.getFont() == null ||
       componentOperator.getSource().getFont().equals(componentOperator.getFont())) {
        printLine("getFont does work");
    } else {
        printLine("getFont does not work");
        printLine(componentOperator.getSource().getFont());
        printLine(componentOperator.getFont());
        return(false);
    }
    if(componentOperator.getSource().getForeground() == null &&
       componentOperator.getForeground() == null ||
       componentOperator.getSource().getForeground().equals(componentOperator.getForeground())) {
        printLine("getForeground does work");
    } else {
        printLine("getForeground does not work");
        printLine(componentOperator.getSource().getForeground());
        printLine(componentOperator.getForeground());
        return(false);
    }
    if(componentOperator.getSource().getHeight() == componentOperator.getHeight()) {
        printLine("getHeight does work");
    } else {
        printLine("getHeight does not work");
        printLine(componentOperator.getSource().getHeight());
        printLine(componentOperator.getHeight());
        return(false);
    }
    if(componentOperator.getSource().getInputContext() == null &&
       componentOperator.getInputContext() == null ||
       componentOperator.getSource().getInputContext().equals(componentOperator.getInputContext())) {
        printLine("getInputContext does work");
    } else {
        printLine("getInputContext does not work");
        printLine(componentOperator.getSource().getInputContext());
        printLine(componentOperator.getInputContext());
        return(false);
    }
    if(componentOperator.getSource().getInputMethodRequests() == null &&
       componentOperator.getInputMethodRequests() == null ||
       componentOperator.getSource().getInputMethodRequests().equals(componentOperator.getInputMethodRequests())) {
        printLine("getInputMethodRequests does work");
    } else {
        printLine("getInputMethodRequests does not work");
        printLine(componentOperator.getSource().getInputMethodRequests());
        printLine(componentOperator.getInputMethodRequests());
        return(false);
    }
    if(componentOperator.getSource().getLocale() == null &&
       componentOperator.getLocale() == null ||
       componentOperator.getSource().getLocale().equals(componentOperator.getLocale())) {
        printLine("getLocale does work");
    } else {
        printLine("getLocale does not work");
        printLine(componentOperator.getSource().getLocale());
        printLine(componentOperator.getLocale());
        return(false);
    }
    if(componentOperator.getSource().getLocation() == null &&
       componentOperator.getLocation() == null ||
       componentOperator.getSource().getLocation().equals(componentOperator.getLocation())) {
        printLine("getLocation does work");
    } else {
        printLine("getLocation does not work");
        printLine(componentOperator.getSource().getLocation());
        printLine(componentOperator.getLocation());
        return(false);
    }
    if(componentOperator.getSource().getLocationOnScreen() == null &&
       componentOperator.getLocationOnScreen() == null ||
       componentOperator.getSource().getLocationOnScreen().equals(componentOperator.getLocationOnScreen())) {
        printLine("getLocationOnScreen does work");
    } else {
        printLine("getLocationOnScreen does not work");
        printLine(componentOperator.getSource().getLocationOnScreen());
        printLine(componentOperator.getLocationOnScreen());
        return(false);
    }
    if(componentOperator.getSource().getMaximumSize() == null &&
       componentOperator.getMaximumSize() == null ||
       componentOperator.getSource().getMaximumSize().equals(componentOperator.getMaximumSize())) {
        printLine("getMaximumSize does work");
    } else {
        printLine("getMaximumSize does not work");
        printLine(componentOperator.getSource().getMaximumSize());
        printLine(componentOperator.getMaximumSize());
        return(false);
    }
    if(componentOperator.getSource().getMinimumSize() == null &&
       componentOperator.getMinimumSize() == null ||
       componentOperator.getSource().getMinimumSize().equals(componentOperator.getMinimumSize())) {
        printLine("getMinimumSize does work");
    } else {
        printLine("getMinimumSize does not work");
        printLine(componentOperator.getSource().getMinimumSize());
        printLine(componentOperator.getMinimumSize());
        return(false);
    }
    if(componentOperator.getSource().getName() == null &&
       componentOperator.getName() == null ||
       componentOperator.getSource().getName().equals(componentOperator.getName())) {
        printLine("getName does work");
    } else {
        printLine("getName does not work");
        printLine(componentOperator.getSource().getName());
        printLine(componentOperator.getName());
        return(false);
    }
    if(componentOperator.getSource().getParent() == null &&
       componentOperator.getParent() == null ||
       componentOperator.getSource().getParent().equals(componentOperator.getParent())) {
        printLine("getParent does work");
    } else {
        printLine("getParent does not work");
        printLine(componentOperator.getSource().getParent());
        printLine(componentOperator.getParent());
        return(false);
    }
    if(componentOperator.getSource().getPreferredSize() == null &&
       componentOperator.getPreferredSize() == null ||
       componentOperator.getSource().getPreferredSize().equals(componentOperator.getPreferredSize())) {
        printLine("getPreferredSize does work");
    } else {
        printLine("getPreferredSize does not work");
        printLine(componentOperator.getSource().getPreferredSize());
        printLine(componentOperator.getPreferredSize());
        return(false);
    }
    if(componentOperator.getSource().getSize() == null &&
       componentOperator.getSize() == null ||
       componentOperator.getSource().getSize().equals(componentOperator.getSize())) {
        printLine("getSize does work");
    } else {
        printLine("getSize does not work");
        printLine(componentOperator.getSource().getSize());
        printLine(componentOperator.getSize());
        return(false);
    }
    if(componentOperator.getSource().getToolkit() == null &&
       componentOperator.getToolkit() == null ||
       componentOperator.getSource().getToolkit().equals(componentOperator.getToolkit())) {
        printLine("getToolkit does work");
    } else {
        printLine("getToolkit does not work");
        printLine(componentOperator.getSource().getToolkit());
        printLine(componentOperator.getToolkit());
        return(false);
    }
    if(componentOperator.getSource().getTreeLock() == null &&
       componentOperator.getTreeLock() == null ||
       componentOperator.getSource().getTreeLock().equals(componentOperator.getTreeLock())) {
        printLine("getTreeLock does work");
    } else {
        printLine("getTreeLock does not work");
        printLine(componentOperator.getSource().getTreeLock());
        printLine(componentOperator.getTreeLock());
        return(false);
    }
    if(componentOperator.getSource().getWidth() == componentOperator.getWidth()) {
        printLine("getWidth does work");
    } else {
        printLine("getWidth does not work");
        printLine(componentOperator.getSource().getWidth());
        printLine(componentOperator.getWidth());
        return(false);
    }
    if(componentOperator.getSource().getX() == componentOperator.getX()) {
        printLine("getX does work");
    } else {
        printLine("getX does not work");
        printLine(componentOperator.getSource().getX());
        printLine(componentOperator.getX());
        return(false);
    }
    if(componentOperator.getSource().getY() == componentOperator.getY()) {
        printLine("getY does work");
    } else {
        printLine("getY does not work");
        printLine(componentOperator.getSource().getY());
        printLine(componentOperator.getY());
        return(false);
    }
    if(componentOperator.getSource().hasFocus() == componentOperator.hasFocus()) {
        printLine("hasFocus does work");
    } else {
        printLine("hasFocus does not work");
        printLine(componentOperator.getSource().hasFocus());
        printLine(componentOperator.hasFocus());
        return(false);
    }
    if(componentOperator.getSource().isDisplayable() == componentOperator.isDisplayable()) {
        printLine("isDisplayable does work");
    } else {
        printLine("isDisplayable does not work");
        printLine(componentOperator.getSource().isDisplayable());
        printLine(componentOperator.isDisplayable());
        return(false);
    }
    if(componentOperator.getSource().isDoubleBuffered() == componentOperator.isDoubleBuffered()) {
        printLine("isDoubleBuffered does work");
    } else {
        printLine("isDoubleBuffered does not work");
        printLine(componentOperator.getSource().isDoubleBuffered());
        printLine(componentOperator.isDoubleBuffered());
        return(false);
    }
    if(componentOperator.getSource().isEnabled() == componentOperator.isEnabled()) {
        printLine("isEnabled does work");
    } else {
        printLine("isEnabled does not work");
        printLine(componentOperator.getSource().isEnabled());
        printLine(componentOperator.isEnabled());
        return(false);
    }
    if(componentOperator.getSource().isFocusTraversable() == componentOperator.isFocusTraversable()) {
        printLine("isFocusTraversable does work");
    } else {
        printLine("isFocusTraversable does not work");
        printLine(componentOperator.getSource().isFocusTraversable());
        printLine(componentOperator.isFocusTraversable());
        return(false);
    }
    if(componentOperator.getSource().isLightweight() == componentOperator.isLightweight()) {
        printLine("isLightweight does work");
    } else {
        printLine("isLightweight does not work");
        printLine(componentOperator.getSource().isLightweight());
        printLine(componentOperator.isLightweight());
        return(false);
    }
    if(componentOperator.getSource().isOpaque() == componentOperator.isOpaque()) {
        printLine("isOpaque does work");
    } else {
        printLine("isOpaque does not work");
        printLine(componentOperator.getSource().isOpaque());
        printLine(componentOperator.isOpaque());
        return(false);
    }
    if(componentOperator.getSource().isShowing() == componentOperator.isShowing()) {
        printLine("isShowing does work");
    } else {
        printLine("isShowing does not work");
        printLine(componentOperator.getSource().isShowing());
        printLine(componentOperator.isShowing());
        return(false);
    }
    if(componentOperator.getSource().isValid() == componentOperator.isValid()) {
        printLine("isValid does work");
    } else {
        printLine("isValid does not work");
        printLine(componentOperator.getSource().isValid());
        printLine(componentOperator.isValid());
        return(false);
    }
    if(componentOperator.getSource().isVisible() == componentOperator.isVisible()) {
        printLine("isVisible does work");
    } else {
        printLine("isVisible does not work");
        printLine(componentOperator.getSource().isVisible());
        printLine(componentOperator.isVisible());
        return(false);
    }
    return(true);
}

public boolean testContainer(ContainerOperator containerOperator) {
    if(((Container)containerOperator.getSource()).getComponentCount() == containerOperator.getComponentCount()) {
        printLine("getComponentCount does work");
    } else {
        printLine("getComponentCount does not work");
        printLine(((Container)containerOperator.getSource()).getComponentCount());
        printLine(containerOperator.getComponentCount());
        return(false);
    }
    if(((Container)containerOperator.getSource()).getInsets() == null &&
       containerOperator.getInsets() == null ||
       ((Container)containerOperator.getSource()).getInsets().equals(containerOperator.getInsets())) {
        printLine("getInsets does work");
    } else {
        printLine("getInsets does not work");
        printLine(((Container)containerOperator.getSource()).getInsets());
        printLine(containerOperator.getInsets());
        return(false);
    }
    if(((Container)containerOperator.getSource()).getLayout() == null &&
       containerOperator.getLayout() == null ||
       ((Container)containerOperator.getSource()).getLayout().equals(containerOperator.getLayout())) {
        printLine("getLayout does work");
    } else {
        printLine("getLayout does not work");
        printLine(((Container)containerOperator.getSource()).getLayout());
        printLine(containerOperator.getLayout());
        return(false);
    }
    return(true);
}

public boolean testJComponent(JComponentOperator jComponentOperator) {
    if(((JComponent)jComponentOperator.getSource()).getAccessibleContext() == null &&
       jComponentOperator.getAccessibleContext() == null ||
       ((JComponent)jComponentOperator.getSource()).getAccessibleContext().equals(jComponentOperator.getAccessibleContext())) {
        printLine("getAccessibleContext does work");
    } else {
        printLine("getAccessibleContext does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getAccessibleContext());
        printLine(jComponentOperator.getAccessibleContext());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getAutoscrolls() == jComponentOperator.getAutoscrolls()) {
        printLine("getAutoscrolls does work");
    } else {
        printLine("getAutoscrolls does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getAutoscrolls());
        printLine(jComponentOperator.getAutoscrolls());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getBorder() == null &&
       jComponentOperator.getBorder() == null ||
       ((JComponent)jComponentOperator.getSource()).getBorder().equals(jComponentOperator.getBorder())) {
        printLine("getBorder does work");
    } else {
        printLine("getBorder does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getBorder());
        printLine(jComponentOperator.getBorder());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getDebugGraphicsOptions() == jComponentOperator.getDebugGraphicsOptions()) {
        printLine("getDebugGraphicsOptions does work");
    } else {
        printLine("getDebugGraphicsOptions does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getDebugGraphicsOptions());
        printLine(jComponentOperator.getDebugGraphicsOptions());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getNextFocusableComponent() == null &&
       jComponentOperator.getNextFocusableComponent() == null ||
       ((JComponent)jComponentOperator.getSource()).getNextFocusableComponent().equals(jComponentOperator.getNextFocusableComponent())) {
        printLine("getNextFocusableComponent does work");
    } else {
        printLine("getNextFocusableComponent does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getNextFocusableComponent());
        printLine(jComponentOperator.getNextFocusableComponent());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getRootPane() == null &&
       jComponentOperator.getRootPane() == null ||
       ((JComponent)jComponentOperator.getSource()).getRootPane().equals(jComponentOperator.getRootPane())) {
        printLine("getRootPane does work");
    } else {
        printLine("getRootPane does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getRootPane());
        printLine(jComponentOperator.getRootPane());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getToolTipText() == null &&
       jComponentOperator.getToolTipText() == null ||
       ((JComponent)jComponentOperator.getSource()).getToolTipText().equals(jComponentOperator.getToolTipText())) {
        printLine("getToolTipText does work");
    } else {
        printLine("getToolTipText does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getToolTipText());
        printLine(jComponentOperator.getToolTipText());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getTopLevelAncestor() == null &&
       jComponentOperator.getTopLevelAncestor() == null ||
       ((JComponent)jComponentOperator.getSource()).getTopLevelAncestor().equals(jComponentOperator.getTopLevelAncestor())) {
        printLine("getTopLevelAncestor does work");
    } else {
        printLine("getTopLevelAncestor does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getTopLevelAncestor());
        printLine(jComponentOperator.getTopLevelAncestor());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getUIClassID() == null &&
       jComponentOperator.getUIClassID() == null ||
       ((JComponent)jComponentOperator.getSource()).getUIClassID().equals(jComponentOperator.getUIClassID())) {
        printLine("getUIClassID does work");
    } else {
        printLine("getUIClassID does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getUIClassID());
        printLine(jComponentOperator.getUIClassID());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).getVisibleRect() == null &&
       jComponentOperator.getVisibleRect() == null ||
       ((JComponent)jComponentOperator.getSource()).getVisibleRect().equals(jComponentOperator.getVisibleRect())) {
        printLine("getVisibleRect does work");
    } else {
        printLine("getVisibleRect does not work");
        printLine(((JComponent)jComponentOperator.getSource()).getVisibleRect());
        printLine(jComponentOperator.getVisibleRect());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).isFocusCycleRoot() == jComponentOperator.isFocusCycleRoot()) {
        printLine("isFocusCycleRoot does work");
    } else {
        printLine("isFocusCycleRoot does not work");
        printLine(((JComponent)jComponentOperator.getSource()).isFocusCycleRoot());
        printLine(jComponentOperator.isFocusCycleRoot());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).isManagingFocus() == jComponentOperator.isManagingFocus()) {
        printLine("isManagingFocus does work");
    } else {
        printLine("isManagingFocus does not work");
        printLine(((JComponent)jComponentOperator.getSource()).isManagingFocus());
        printLine(jComponentOperator.isManagingFocus());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).isOptimizedDrawingEnabled() == jComponentOperator.isOptimizedDrawingEnabled()) {
        printLine("isOptimizedDrawingEnabled does work");
    } else {
        printLine("isOptimizedDrawingEnabled does not work");
        printLine(((JComponent)jComponentOperator.getSource()).isOptimizedDrawingEnabled());
        printLine(jComponentOperator.isOptimizedDrawingEnabled());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).isPaintingTile() == jComponentOperator.isPaintingTile()) {
        printLine("isPaintingTile does work");
    } else {
        printLine("isPaintingTile does not work");
        printLine(((JComponent)jComponentOperator.getSource()).isPaintingTile());
        printLine(jComponentOperator.isPaintingTile());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).isRequestFocusEnabled() == jComponentOperator.isRequestFocusEnabled()) {
        printLine("isRequestFocusEnabled does work");
    } else {
        printLine("isRequestFocusEnabled does not work");
        printLine(((JComponent)jComponentOperator.getSource()).isRequestFocusEnabled());
        printLine(jComponentOperator.isRequestFocusEnabled());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).isValidateRoot() == jComponentOperator.isValidateRoot()) {
        printLine("isValidateRoot does work");
    } else {
        printLine("isValidateRoot does not work");
        printLine(((JComponent)jComponentOperator.getSource()).isValidateRoot());
        printLine(jComponentOperator.isValidateRoot());
        return(false);
    }
    if(((JComponent)jComponentOperator.getSource()).requestDefaultFocus() == jComponentOperator.requestDefaultFocus()) {
        printLine("requestDefaultFocus does work");
    } else {
        printLine("requestDefaultFocus does not work");
        printLine(((JComponent)jComponentOperator.getSource()).requestDefaultFocus());
        printLine(jComponentOperator.requestDefaultFocus());
        return(false);
    }
    return(true);
}

public boolean testJTextComponent(JTextComponentOperator jTextComponentOperator) {
    if(((JTextComponent)jTextComponentOperator.getSource()).getCaret() == null &&
       jTextComponentOperator.getCaret() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getCaret().equals(jTextComponentOperator.getCaret())) {
        printLine("getCaret does work");
    } else {
        printLine("getCaret does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getCaret());
        printLine(jTextComponentOperator.getCaret());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getCaretColor() == null &&
       jTextComponentOperator.getCaretColor() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getCaretColor().equals(jTextComponentOperator.getCaretColor())) {
        printLine("getCaretColor does work");
    } else {
        printLine("getCaretColor does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getCaretColor());
        printLine(jTextComponentOperator.getCaretColor());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getCaretPosition() == jTextComponentOperator.getCaretPosition()) {
        printLine("getCaretPosition does work");
    } else {
        printLine("getCaretPosition does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getCaretPosition());
        printLine(jTextComponentOperator.getCaretPosition());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getDisabledTextColor() == null &&
       jTextComponentOperator.getDisabledTextColor() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getDisabledTextColor().equals(jTextComponentOperator.getDisabledTextColor())) {
        printLine("getDisabledTextColor does work");
    } else {
        printLine("getDisabledTextColor does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getDisabledTextColor());
        printLine(jTextComponentOperator.getDisabledTextColor());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getDocument() == null &&
       jTextComponentOperator.getDocument() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getDocument().equals(jTextComponentOperator.getDocument())) {
        printLine("getDocument does work");
    } else {
        printLine("getDocument does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getDocument());
        printLine(jTextComponentOperator.getDocument());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getFocusAccelerator() == jTextComponentOperator.getFocusAccelerator()) {
        printLine("getFocusAccelerator does work");
    } else {
        printLine("getFocusAccelerator does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getFocusAccelerator());
        printLine(jTextComponentOperator.getFocusAccelerator());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getHighlighter() == null &&
       jTextComponentOperator.getHighlighter() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getHighlighter().equals(jTextComponentOperator.getHighlighter())) {
        printLine("getHighlighter does work");
    } else {
        printLine("getHighlighter does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getHighlighter());
        printLine(jTextComponentOperator.getHighlighter());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getKeymap() == null &&
       jTextComponentOperator.getKeymap() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getKeymap().equals(jTextComponentOperator.getKeymap())) {
        printLine("getKeymap does work");
    } else {
        printLine("getKeymap does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getKeymap());
        printLine(jTextComponentOperator.getKeymap());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getMargin() == null &&
       jTextComponentOperator.getMargin() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getMargin().equals(jTextComponentOperator.getMargin())) {
        printLine("getMargin does work");
    } else {
        printLine("getMargin does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getMargin());
        printLine(jTextComponentOperator.getMargin());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getPreferredScrollableViewportSize() == null &&
       jTextComponentOperator.getPreferredScrollableViewportSize() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getPreferredScrollableViewportSize().equals(jTextComponentOperator.getPreferredScrollableViewportSize())) {
        printLine("getPreferredScrollableViewportSize does work");
    } else {
        printLine("getPreferredScrollableViewportSize does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getPreferredScrollableViewportSize());
        printLine(jTextComponentOperator.getPreferredScrollableViewportSize());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getScrollableTracksViewportHeight() == jTextComponentOperator.getScrollableTracksViewportHeight()) {
        printLine("getScrollableTracksViewportHeight does work");
    } else {
        printLine("getScrollableTracksViewportHeight does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getScrollableTracksViewportHeight());
        printLine(jTextComponentOperator.getScrollableTracksViewportHeight());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getScrollableTracksViewportWidth() == jTextComponentOperator.getScrollableTracksViewportWidth()) {
        printLine("getScrollableTracksViewportWidth does work");
    } else {
        printLine("getScrollableTracksViewportWidth does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getScrollableTracksViewportWidth());
        printLine(jTextComponentOperator.getScrollableTracksViewportWidth());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getSelectedText() == null &&
       jTextComponentOperator.getSelectedText() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getSelectedText().equals(jTextComponentOperator.getSelectedText())) {
        printLine("getSelectedText does work");
    } else {
        printLine("getSelectedText does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getSelectedText());
        printLine(jTextComponentOperator.getSelectedText());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getSelectedTextColor() == null &&
       jTextComponentOperator.getSelectedTextColor() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getSelectedTextColor().equals(jTextComponentOperator.getSelectedTextColor())) {
        printLine("getSelectedTextColor does work");
    } else {
        printLine("getSelectedTextColor does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getSelectedTextColor());
        printLine(jTextComponentOperator.getSelectedTextColor());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getSelectionColor() == null &&
       jTextComponentOperator.getSelectionColor() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getSelectionColor().equals(jTextComponentOperator.getSelectionColor())) {
        printLine("getSelectionColor does work");
    } else {
        printLine("getSelectionColor does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getSelectionColor());
        printLine(jTextComponentOperator.getSelectionColor());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getSelectionEnd() == jTextComponentOperator.getSelectionEnd()) {
        printLine("getSelectionEnd does work");
    } else {
        printLine("getSelectionEnd does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getSelectionEnd());
        printLine(jTextComponentOperator.getSelectionEnd());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getSelectionStart() == jTextComponentOperator.getSelectionStart()) {
        printLine("getSelectionStart does work");
    } else {
        printLine("getSelectionStart does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getSelectionStart());
        printLine(jTextComponentOperator.getSelectionStart());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getText() == null &&
       jTextComponentOperator.getText() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getText().equals(jTextComponentOperator.getText())) {
        printLine("getText does work");
    } else {
        printLine("getText does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getText());
        printLine(jTextComponentOperator.getText());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).getUI() == null &&
       jTextComponentOperator.getUI() == null ||
       ((JTextComponent)jTextComponentOperator.getSource()).getUI().equals(jTextComponentOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).getUI());
        printLine(jTextComponentOperator.getUI());
        return(false);
    }
    if(((JTextComponent)jTextComponentOperator.getSource()).isEditable() == jTextComponentOperator.isEditable()) {
        printLine("isEditable does work");
    } else {
        printLine("isEditable does not work");
        printLine(((JTextComponent)jTextComponentOperator.getSource()).isEditable());
        printLine(jTextComponentOperator.isEditable());
        return(false);
    }
    return(true);
}

public boolean testJTextField(JTextFieldOperator jTextFieldOperator) {
    if(((JTextField)jTextFieldOperator.getSource()).getColumns() == jTextFieldOperator.getColumns()) {
        printLine("getColumns does work");
    } else {
        printLine("getColumns does not work");
        printLine(((JTextField)jTextFieldOperator.getSource()).getColumns());
        printLine(jTextFieldOperator.getColumns());
        return(false);
    }
    if(((JTextField)jTextFieldOperator.getSource()).getHorizontalAlignment() == jTextFieldOperator.getHorizontalAlignment()) {
        printLine("getHorizontalAlignment does work");
    } else {
        printLine("getHorizontalAlignment does not work");
        printLine(((JTextField)jTextFieldOperator.getSource()).getHorizontalAlignment());
        printLine(jTextFieldOperator.getHorizontalAlignment());
        return(false);
    }
    if(((JTextField)jTextFieldOperator.getSource()).getHorizontalVisibility() == null &&
       jTextFieldOperator.getHorizontalVisibility() == null ||
       ((JTextField)jTextFieldOperator.getSource()).getHorizontalVisibility().equals(jTextFieldOperator.getHorizontalVisibility())) {
        printLine("getHorizontalVisibility does work");
    } else {
        printLine("getHorizontalVisibility does not work");
        printLine(((JTextField)jTextFieldOperator.getSource()).getHorizontalVisibility());
        printLine(jTextFieldOperator.getHorizontalVisibility());
        return(false);
    }
    if(((JTextField)jTextFieldOperator.getSource()).getScrollOffset() == jTextFieldOperator.getScrollOffset()) {
        printLine("getScrollOffset does work");
    } else {
        printLine("getScrollOffset does not work");
        printLine(((JTextField)jTextFieldOperator.getSource()).getScrollOffset());
        printLine(jTextFieldOperator.getScrollOffset());
        return(false);
    }
    return(true);
}

public boolean testWindow(WindowOperator windowOperator) {
    if(((Window)windowOperator.getSource()).getFocusOwner() == null &&
       windowOperator.getFocusOwner() == null ||
       ((Window)windowOperator.getSource()).getFocusOwner().equals(windowOperator.getFocusOwner())) {
        printLine("getFocusOwner does work");
    } else {
        printLine("getFocusOwner does not work");
        printLine(((Window)windowOperator.getSource()).getFocusOwner());
        printLine(windowOperator.getFocusOwner());
        return(false);
    }
    if(((Window)windowOperator.getSource()).getOwner() == null &&
       windowOperator.getOwner() == null ||
       ((Window)windowOperator.getSource()).getOwner().equals(windowOperator.getOwner())) {
        printLine("getOwner does work");
    } else {
        printLine("getOwner does not work");
        printLine(((Window)windowOperator.getSource()).getOwner());
        printLine(windowOperator.getOwner());
        return(false);
    }
    if(((Window)windowOperator.getSource()).getWarningString() == null &&
       windowOperator.getWarningString() == null ||
       ((Window)windowOperator.getSource()).getWarningString().equals(windowOperator.getWarningString())) {
        printLine("getWarningString does work");
    } else {
        printLine("getWarningString does not work");
        printLine(((Window)windowOperator.getSource()).getWarningString());
        printLine(windowOperator.getWarningString());
        return(false);
    }
    return(true);
}
    /*
public boolean testFrame(FrameOperator frameOperator) {
    if(((Frame)frameOperator.getSource()).getIconImage() == null &&
       frameOperator.getIconImage() == null ||
       ((Frame)frameOperator.getSource()).getIconImage().equals(frameOperator.getIconImage())) {
        printLine("getIconImage does work");
    } else {
        printLine("getIconImage does not work");
        printLine(((Frame)frameOperator.getSource()).getIconImage());
        printLine(frameOperator.getIconImage());
        return(false);
    }
    if(((Frame)frameOperator.getSource()).getMenuBar() == null &&
       frameOperator.getMenuBar() == null ||
       ((Frame)frameOperator.getSource()).getMenuBar().equals(frameOperator.getMenuBar())) {
        printLine("getMenuBar does work");
    } else {
        printLine("getMenuBar does not work");
        printLine(((Frame)frameOperator.getSource()).getMenuBar());
        printLine(frameOperator.getMenuBar());
        return(false);
    }
    if(((Frame)frameOperator.getSource()).getState() == frameOperator.getState()) {
        printLine("getState does work");
    } else {
        printLine("getState does not work");
        printLine(((Frame)frameOperator.getSource()).getState());
        printLine(frameOperator.getState());
        return(false);
    }
    if(((Frame)frameOperator.getSource()).getTitle() == null &&
       frameOperator.getTitle() == null ||
       ((Frame)frameOperator.getSource()).getTitle().equals(frameOperator.getTitle())) {
        printLine("getTitle does work");
    } else {
        printLine("getTitle does not work");
        printLine(((Frame)frameOperator.getSource()).getTitle());
        printLine(frameOperator.getTitle());
        return(false);
    }
    if(((Frame)frameOperator.getSource()).isResizable() == frameOperator.isResizable()) {
        printLine("isResizable does work");
    } else {
        printLine("isResizable does not work");
        printLine(((Frame)frameOperator.getSource()).isResizable());
        printLine(frameOperator.isResizable());
        return(false);
    }
    return(true);
}

public boolean testJFrame(JFrameOperator jFrameOperator) {
    if(((JFrame)jFrameOperator.getSource()).getAccessibleContext() == null &&
       jFrameOperator.getAccessibleContext() == null ||
       ((JFrame)jFrameOperator.getSource()).getAccessibleContext().equals(jFrameOperator.getAccessibleContext())) {
        printLine("getAccessibleContext does work");
    } else {
        printLine("getAccessibleContext does not work");
        printLine(((JFrame)jFrameOperator.getSource()).getAccessibleContext());
        printLine(jFrameOperator.getAccessibleContext());
        return(false);
    }
    if(((JFrame)jFrameOperator.getSource()).getContentPane() == null &&
       jFrameOperator.getContentPane() == null ||
       ((JFrame)jFrameOperator.getSource()).getContentPane().equals(jFrameOperator.getContentPane())) {
        printLine("getContentPane does work");
    } else {
        printLine("getContentPane does not work");
        printLine(((JFrame)jFrameOperator.getSource()).getContentPane());
        printLine(jFrameOperator.getContentPane());
        return(false);
    }
    if(((JFrame)jFrameOperator.getSource()).getDefaultCloseOperation() == jFrameOperator.getDefaultCloseOperation()) {
        printLine("getDefaultCloseOperation does work");
    } else {
        printLine("getDefaultCloseOperation does not work");
        printLine(((JFrame)jFrameOperator.getSource()).getDefaultCloseOperation());
        printLine(jFrameOperator.getDefaultCloseOperation());
        return(false);
    }
    if(((JFrame)jFrameOperator.getSource()).getGlassPane() == null &&
       jFrameOperator.getGlassPane() == null ||
       ((JFrame)jFrameOperator.getSource()).getGlassPane().equals(jFrameOperator.getGlassPane())) {
        printLine("getGlassPane does work");
    } else {
        printLine("getGlassPane does not work");
        printLine(((JFrame)jFrameOperator.getSource()).getGlassPane());
        printLine(jFrameOperator.getGlassPane());
        return(false);
    }
    if(((JFrame)jFrameOperator.getSource()).getJMenuBar() == null &&
       jFrameOperator.getJMenuBar() == null ||
       ((JFrame)jFrameOperator.getSource()).getJMenuBar().equals(jFrameOperator.getJMenuBar())) {
        printLine("getJMenuBar does work");
    } else {
        printLine("getJMenuBar does not work");
        printLine(((JFrame)jFrameOperator.getSource()).getJMenuBar());
        printLine(jFrameOperator.getJMenuBar());
        return(false);
    }
    if(((JFrame)jFrameOperator.getSource()).getLayeredPane() == null &&
       jFrameOperator.getLayeredPane() == null ||
       ((JFrame)jFrameOperator.getSource()).getLayeredPane().equals(jFrameOperator.getLayeredPane())) {
        printLine("getLayeredPane does work");
    } else {
        printLine("getLayeredPane does not work");
        printLine(((JFrame)jFrameOperator.getSource()).getLayeredPane());
        printLine(jFrameOperator.getLayeredPane());
        return(false);
    }
    if(((JFrame)jFrameOperator.getSource()).getRootPane() == null &&
       jFrameOperator.getRootPane() == null ||
       ((JFrame)jFrameOperator.getSource()).getRootPane().equals(jFrameOperator.getRootPane())) {
        printLine("getRootPane does work");
    } else {
        printLine("getRootPane does not work");
        printLine(((JFrame)jFrameOperator.getSource()).getRootPane());
        printLine(jFrameOperator.getRootPane());
        return(false);
    }
    return(true);
}
    */
public boolean testJComboBox(JComboBoxOperator jComboBoxOperator) {
    if(((JComboBox)jComboBoxOperator.getSource()).getActionCommand() == null &&
       jComboBoxOperator.getActionCommand() == null ||
       ((JComboBox)jComboBoxOperator.getSource()).getActionCommand().equals(jComboBoxOperator.getActionCommand())) {
        printLine("getActionCommand does work");
    } else {
        printLine("getActionCommand does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getActionCommand());
        printLine(jComboBoxOperator.getActionCommand());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getEditor() == null &&
       jComboBoxOperator.getEditor() == null ||
       ((JComboBox)jComboBoxOperator.getSource()).getEditor().equals(jComboBoxOperator.getEditor())) {
        printLine("getEditor does work");
    } else {
        printLine("getEditor does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getEditor());
        printLine(jComboBoxOperator.getEditor());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getItemCount() == jComboBoxOperator.getItemCount()) {
        printLine("getItemCount does work");
    } else {
        printLine("getItemCount does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getItemCount());
        printLine(jComboBoxOperator.getItemCount());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getKeySelectionManager() == null &&
       jComboBoxOperator.getKeySelectionManager() == null ||
       ((JComboBox)jComboBoxOperator.getSource()).getKeySelectionManager().equals(jComboBoxOperator.getKeySelectionManager())) {
        printLine("getKeySelectionManager does work");
    } else {
        printLine("getKeySelectionManager does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getKeySelectionManager());
        printLine(jComboBoxOperator.getKeySelectionManager());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getMaximumRowCount() == jComboBoxOperator.getMaximumRowCount()) {
        printLine("getMaximumRowCount does work");
    } else {
        printLine("getMaximumRowCount does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getMaximumRowCount());
        printLine(jComboBoxOperator.getMaximumRowCount());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getModel() == null &&
       jComboBoxOperator.getModel() == null ||
       ((JComboBox)jComboBoxOperator.getSource()).getModel().equals(jComboBoxOperator.getModel())) {
        printLine("getModel does work");
    } else {
        printLine("getModel does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getModel());
        printLine(jComboBoxOperator.getModel());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getRenderer() == null &&
       jComboBoxOperator.getRenderer() == null ||
       ((JComboBox)jComboBoxOperator.getSource()).getRenderer().equals(jComboBoxOperator.getRenderer())) {
        printLine("getRenderer does work");
    } else {
        printLine("getRenderer does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getRenderer());
        printLine(jComboBoxOperator.getRenderer());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getSelectedIndex() == jComboBoxOperator.getSelectedIndex()) {
        printLine("getSelectedIndex does work");
    } else {
        printLine("getSelectedIndex does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getSelectedIndex());
        printLine(jComboBoxOperator.getSelectedIndex());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getSelectedItem() == null &&
       jComboBoxOperator.getSelectedItem() == null ||
       ((JComboBox)jComboBoxOperator.getSource()).getSelectedItem().equals(jComboBoxOperator.getSelectedItem())) {
        printLine("getSelectedItem does work");
    } else {
        printLine("getSelectedItem does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getSelectedItem());
        printLine(jComboBoxOperator.getSelectedItem());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).getUI() == null &&
       jComboBoxOperator.getUI() == null ||
       ((JComboBox)jComboBoxOperator.getSource()).getUI().equals(jComboBoxOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).getUI());
        printLine(jComboBoxOperator.getUI());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).isEditable() == jComboBoxOperator.isEditable()) {
        printLine("isEditable does work");
    } else {
        printLine("isEditable does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).isEditable());
        printLine(jComboBoxOperator.isEditable());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).isLightWeightPopupEnabled() == jComboBoxOperator.isLightWeightPopupEnabled()) {
        printLine("isLightWeightPopupEnabled does work");
    } else {
        printLine("isLightWeightPopupEnabled does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).isLightWeightPopupEnabled());
        printLine(jComboBoxOperator.isLightWeightPopupEnabled());
        return(false);
    }
    if(((JComboBox)jComboBoxOperator.getSource()).isPopupVisible() == jComboBoxOperator.isPopupVisible()) {
        printLine("isPopupVisible does work");
    } else {
        printLine("isPopupVisible does not work");
        printLine(((JComboBox)jComboBoxOperator.getSource()).isPopupVisible());
        printLine(jComboBoxOperator.isPopupVisible());
        return(false);
    }
    return(true);
}

}

/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.operators;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;

import java.awt.Component;
import java.awt.Container;

import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JButton;
import javax.swing.ListModel;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import javax.swing.plaf.FileChooserUI;

/**
 *
 * Class provides methods to cover main JFileChooser component functionality.
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JFileChooserOperator extends JComponentOperator 
    implements Timeoutable, Outputable {

    private final static long WAIT_LIST_PAINTED_TIMEOUT = 10000;

    private Timeouts timeouts;
    private TestOut output;
    private ComponentSearcher innerSearcher;

    /**
     * Constructor.
     */
    public JFileChooserOperator(JFileChooser comp) {
	super(comp);
	innerSearcher = new ComponentSearcher(comp);
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
    }

    /**
     * Constructor.
     * Waits component first.
     * Constructor can be used in complicated cases when
     * output or timeouts should differ from default.
     * @param timeouts
     * @param output
     * @throws TimeoutExpiredException
     */
    public JFileChooserOperator(Operator env) {
	this((JFileChooser)
	     waitComponent(JDialogOperator.
			   waitJDialog(new JFileChooserJDialogFinder(env.getOutput()),
				       0,
				       env.getTimeouts(),
				       env.getOutput()),
			   new JFileChooserFinder(),
			   0,
			   env.getTimeouts(),
			   env.getOutput()));
	copyEnvironment(env);
    }

    /**
     * Constructor.
     * Waits component first.
     * @throws TimeoutExpiredException
     */
    public JFileChooserOperator() {
	this(getEnvironmentOperator());
    }

    /**
     * Searches currently opened JDilog with  JFileChooser inside.
     */
    public static JDialog findJFileChooserDialog() {
	return(JDialogOperator.
	       findJDialog(new JFileChooserJDialogFinder(JemmyProperties.
							  getCurrentOutput())));
    }

    /**
     * Waits currently opened JDilog with  JFileChooser inside.
     * @throws TimeoutExpiredException
     */
    public static JDialog waitJFileChooserDialog() {
	return(JDialogOperator.
	       waitJDialog(new JFileChooserJDialogFinder(JemmyProperties.
							  getCurrentOutput())));
    }

    /**
     * Searches JFileChooser in container.
     */
    public static JFileChooser findJFileChooser(Container cont) {
	return((JFileChooser)findComponent(cont, new JFileChooserFinder()));
    }

    /**
     * Searches JFileChooser in container.
     * @throws TimeoutExpiredException
     */
    public static JFileChooser waitJFileChooser(Container cont) {
	return((JFileChooser)waitComponent(cont, new JFileChooserFinder()));
    }

    /**
     * Searches currently opened JFileChooser.
     */
    public static JFileChooser findJFileChooser() {
	return(findJFileChooser(findJFileChooserDialog()));
    }

    /**
     * Waits currently opened JFileChooser.
     * @throws TimeoutExpiredException
     */
    public static JFileChooser waitJFileChooser() {
	return(waitJFileChooser(waitJFileChooserDialog()));
    }

    static {
	Timeouts.initDefault("JFileChooserOperator.WaitListPaintedTimeout", WAIT_LIST_PAINTED_TIMEOUT);
    }

    /**
     * Defines current timeouts.
     * @param timeouts A collection of timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	super.setTimeouts(timeouts);
	this.timeouts = timeouts;
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output.createErrorOutput());
	if(innerSearcher != null) {
	    innerSearcher.setOutput(output.createErrorOutput());
	}
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Returns combo box containing path (upper).
     */
    public JComboBox getPathCombo() {
	return(getCombo(0));
    }

    /**
     * Returns combo box containing file types (lower).
     */
    public JComboBox getFileTypesCombo() {
	return(getCombo(1));
    }

    /**
     * Returns approve button.
     */
    public JButton getApproveButton() {
	return((JButton)innerSearcher.
	       findComponent(new ButtonFinder(getApproveButtonText())));
    }

    /**
     * Returns cancel button.
     */
    public JButton getCancelButton() {
	return((JButton)innerSearcher.
	       findComponent(new ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JButton &&
				  !(comp.getParent() instanceof JComboBox) &&
				  ((JButton)comp).getText().length() != 0);
		       }
		       public String getDescription() {
			   return("JButton");
		       }
		   }, 1));
    }

    /**
     * Returns "Home" button.
     */
    public JButton getHomeButton() {
	return(getNoTextButton(1));
    }

    /**
     * Returns "Up One Level" button.
     */
    public JButton getUpLevelButton() {
	return(getNoTextButton(0));
    }

    /**
     */
    public JToggleButton getListToggleButton() {
	return(getToggleButton(0));
    }

    /**
     */
    public JToggleButton getDetailsToggleButton() {
	return(getToggleButton(1));
    }

    /**
     * Returns field which can be used to type path.
     */
    public JTextField getPathField() {
	return((JTextField)innerSearcher.
	       findComponent(new ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JTextField);
		       }
		       public String getDescription() {
			   return("JTextField");
		       }
		   }));
    }

    /**
     * Returns file list.
     */
    public JList getFileList() {
	return((JList)innerSearcher.
	       findComponent(new ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JList);
		       }
		       public String getDescription() {
			   return("JList");
		       }
		   }));
    }

    /**
     * Pushes approve button.
     * @throws TimeoutExpiredException
     */
    public void approve() {
	output.printTrace("Push approve button in JFileChooser\n    : " +
			  getSource().toString());
	JButtonOperator approveOper = new JButtonOperator(getApproveButton());
	approveOper.copyEnvironment(this);
	approveOper.setOutput(output.createErrorOutput());
	approveOper.push();
    }

    /**
     * Pushes cancel button.
     * @throws TimeoutExpiredException
     */
    public void cancel() {
	output.printTrace("Push cancel button in JFileChooser\n    : " +
			  getSource().toString());
	JButtonOperator cancelOper = new JButtonOperator(getCancelButton());
	cancelOper.copyEnvironment(this);
	cancelOper.setOutput(output.createErrorOutput());
	cancelOper.push();
    }

    /**
     * Types file name into text field and pushes approve button.
     * @throws TimeoutExpiredException
     */
    public void chooseFile(String fileName) {
	output.printTrace("Choose file by JFileChooser\n    : " + fileName +
			  "\n    : " + getSource().toString());
	JTextFieldOperator fieldOper = new JTextFieldOperator(getPathField());
	fieldOper.copyEnvironment(this);
	fieldOper.setOutput(output.createErrorOutput());
	fieldOper.clearText();
	fieldOper.typeText(fileName);
	approve();
    }

    /**
     * Pushes "Up One Level" button.
     * @throws TimeoutExpiredException
     */
    public File goUpLevel() {
	output.printTrace("Go up level in JFileChooser\n    : " +
			  getSource().toString());
	JButtonOperator upOper = new JButtonOperator(getUpLevelButton());
	upOper.copyEnvironment(this);
	upOper.setOutput(output.createErrorOutput());
	upOper.push();
	waitPainted(-1);
	return(getCurrentDirectory());
    }

    /**
     * Pushes "Home" button.
     * @throws TimeoutExpiredException
     */
    public File goHome() {
	output.printTrace("Go home in JFileChooser\n    : " +
			  getSource().toString());
	JButtonOperator homeOper = new JButtonOperator(getHomeButton());
	homeOper.copyEnvironment(this);
	homeOper.setOutput(output.createErrorOutput());
	homeOper.push();
	waitPainted(-1);
	return(getCurrentDirectory());
    }

    /**
     * Clicks on file in the list.
     * @param index Ordinal file index.
     * @param clickCount
     * @see #clickOnFile(String, boolean, boolean, int)
     * @see #clickOnFile(String, boolean, boolean)
     */
    public void clickOnFile(int index, int clickCount) {
	output.printTrace("Click " + Integer.toString(clickCount) + 
			  "times on " + Integer.toString(index) + 
			  "`th file in JFileChooser\n    : " +
			  getSource().toString());
	JListOperator listOper = new JListOperator(getFileList());
	waitPainted(index);
	listOper.copyEnvironment(this);
	listOper.setOutput(output.createErrorOutput());
	listOper.clickOnItem(index, clickCount);
    }

    /**
     * Clicks on file in the list.
     * @param file File name (foo.c). Do not use full path (/tmp/foo.c) here.
     * @see #clickOnFile(int, int)
     * @see #clickOnFile(String, boolean, boolean)
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public void clickOnFile(String file, boolean ce, boolean cc, int clickCount) {
	clickOnFile(file, new DefaultStringComparator(ce, cc), clickCount);
    }

    public void clickOnFile(String file, int clickCount) {
	clickOnFile(file, getComparator(), clickCount);
    }

    /**
     * Clicks 1 time on file in the list.
     * @param file File name (foo.c). Do not use full path (/tmp/foo.c) here.
     * @see #clickOnFile(int, int)
     * @see #clickOnFile(String, boolean, boolean, int)
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public void clickOnFile(String file, boolean ce, boolean cc) {
	clickOnFile(file, ce, cc, 1);
    }

    public void clickOnFile(String file) {
	clickOnFile(file, 1);
    }

    /**
     * Enters into subdir curently displayed in the list.
     * @param dir Directory name (tmp1). Do not use full path (/tmp/tmp1) here.
     * @see #clickOnFile(int, int)
     * @see #clickOnFile(String, boolean, boolean)
     * @see #clickOnFile(String, boolean, boolean, int)
     */
    public File enterSubDir(String dir, boolean ce, boolean cc) {
	clickOnFile(dir, ce, cc, 2);
	return(getCurrentDirectory());
    }

    public File enterSubDir(String dir) {
	clickOnFile(dir, 2);
	return(getCurrentDirectory());
    }

    /**
     * Selects a file curently in the list.
     * @param file File name (foo.c). Do not use full path (/tmp/foo.c) here.
     * @see #clickOnFile(int, int)
     * @see #clickOnFile(String, boolean, boolean)
     * @see #clickOnFile(String, boolean, boolean, int)
     */
    public void selectFile(String file, boolean ce, boolean cc) {
	clickOnFile(file, ce, cc);
    }

    public void selectFile(String file) {
	clickOnFile(file);
    }

    /**
     * Selects directory from the combo box above.
     * @param dir Directory name (tmp1). Do not use full path (/tmp/tmp1) here.
     * @throws TimeoutExpiredException
     */
    public void selectPathDirectory(String dir, boolean ce, boolean cc) {
	selectPathDirectory(dir, new DefaultStringComparator(ce, cc));
    }

    /**
     * Selects directory from the combo box above.
     * @param dir Directory name (tmp1). Do not use full path (/tmp/tmp1) here.
     * @throws TimeoutExpiredException
     */
    public void selectPathDirectory(String dir) {
	selectPathDirectory(dir, getComparator());
    }

    /**
     * Selects file type from the combo box below.
     * @throws TimeoutExpiredException
     */
    public void selectFileType(String filter, boolean ce, boolean cc) {
	selectFileType(filter, new DefaultStringComparator(ce, cc));
    }

    /**
     * Selects file type from the combo box below.
     * @throws TimeoutExpiredException
     */
    public void selectFileType(String filter) {
	selectFileType(filter, getComparator());
    }

    /**
     * Checks if file is currently displayed in the list.
     * @param file File name (foo.c). Do not use full path (/tmp/foo.c) here.
     */
    public boolean checkFileDisplayed(String file, boolean ce, boolean cc) {
	return(checkFileDisplayed(file, new DefaultStringComparator(ce, cc)));
    }

    /**
     * Checks if file is currently displayed in the list.
     * @param file File name (foo.c). Do not use full path (/tmp/foo.c) here.
     */
    public boolean checkFileDisplayed(String file) {
	return(checkFileDisplayed(file, getComparator()));
    }

    /**
     * Return count of files currently displayed.
     */
    public int getFileCount() {
	waitPainted(-1);
	return(getFileList().getModel().getSize());
    }

    /**
     * Return files currently displayed.
     */
    public File[] getFiles() {
	waitPainted(-1);
	ListModel listModel = getFileList().getModel();
	File[] result = new File[listModel.getSize()];
	for(int i = 0; i < listModel.getSize(); i++) {
	    result[i] = (File)listModel.getElementAt(i);
	}
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JFileChooser.accept(File)</code> through queue*/
    public boolean accept(final File file) {
	return(runMapping(new MapBooleanAction("accept") {
		public boolean map() {
		    return(((JFileChooser)getSource()).accept(file));
		}}));}

    /**Maps <code>JFileChooser.addActionListener(ActionListener)</code> through queue*/
    public void addActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("addActionListener") {
		public void map() {
		    ((JFileChooser)getSource()).addActionListener(actionListener);
		}});}

    /**Maps <code>JFileChooser.addChoosableFileFilter(FileFilter)</code> through queue*/
    public void addChoosableFileFilter(final FileFilter fileFilter) {
	runMapping(new MapVoidAction("addChoosableFileFilter") {
		public void map() {
		    ((JFileChooser)getSource()).addChoosableFileFilter(fileFilter);
		}});}

    /**Maps <code>JFileChooser.approveSelection()</code> through queue*/
    public void approveSelection() {
	runMapping(new MapVoidAction("approveSelection") {
		public void map() {
		    ((JFileChooser)getSource()).approveSelection();
		}});}

    /**Maps <code>JFileChooser.cancelSelection()</code> through queue*/
    public void cancelSelection() {
	runMapping(new MapVoidAction("cancelSelection") {
		public void map() {
		    ((JFileChooser)getSource()).cancelSelection();
		}});}

    /**Maps <code>JFileChooser.changeToParentDirectory()</code> through queue*/
    public void changeToParentDirectory() {
	runMapping(new MapVoidAction("changeToParentDirectory") {
		public void map() {
		    ((JFileChooser)getSource()).changeToParentDirectory();
		}});}

    /**Maps <code>JFileChooser.ensureFileIsVisible(File)</code> through queue*/
    public void ensureFileIsVisible(final File file) {
	runMapping(new MapVoidAction("ensureFileIsVisible") {
		public void map() {
		    ((JFileChooser)getSource()).ensureFileIsVisible(file);
		}});}

    /**Maps <code>JFileChooser.getAcceptAllFileFilter()</code> through queue*/
    public FileFilter getAcceptAllFileFilter() {
	return((FileFilter)runMapping(new MapAction("getAcceptAllFileFilter") {
		public Object map() {
		    return(((JFileChooser)getSource()).getAcceptAllFileFilter());
		}}));}

    /**Maps <code>JFileChooser.getAccessory()</code> through queue*/
    public JComponent getAccessory() {
	return((JComponent)runMapping(new MapAction("getAccessory") {
		public Object map() {
		    return(((JFileChooser)getSource()).getAccessory());
		}}));}

    /**Maps <code>JFileChooser.getApproveButtonMnemonic()</code> through queue*/
    public int getApproveButtonMnemonic() {
	return(runMapping(new MapIntegerAction("getApproveButtonMnemonic") {
		public int map() {
		    return(((JFileChooser)getSource()).getApproveButtonMnemonic());
		}}));}

    /**Maps <code>JFileChooser.getApproveButtonText()</code> through queue*/
    public String getApproveButtonText() {
	return((String)runMapping(new MapAction("getApproveButtonText") {
		public Object map() {
		    return(((JFileChooser)getSource()).getApproveButtonText());
		}}));}

    /**Maps <code>JFileChooser.getApproveButtonToolTipText()</code> through queue*/
    public String getApproveButtonToolTipText() {
	return((String)runMapping(new MapAction("getApproveButtonToolTipText") {
		public Object map() {
		    return(((JFileChooser)getSource()).getApproveButtonToolTipText());
		}}));}

    /**Maps <code>JFileChooser.getChoosableFileFilters()</code> through queue*/
    public FileFilter[] getChoosableFileFilters() {
	return((FileFilter[])runMapping(new MapAction("getChoosableFileFilters") {
		public Object map() {
		    return(((JFileChooser)getSource()).getChoosableFileFilters());
		}}));}

    /**Maps <code>JFileChooser.getCurrentDirectory()</code> through queue*/
    public File getCurrentDirectory() {
	return((File)runMapping(new MapAction("getCurrentDirectory") {
		public Object map() {
		    return(((JFileChooser)getSource()).getCurrentDirectory());
		}}));}

    /**Maps <code>JFileChooser.getDescription(File)</code> through queue*/
    public String getDescription(final File file) {
	return((String)runMapping(new MapAction("getDescription") {
		public Object map() {
		    return(((JFileChooser)getSource()).getDescription(file));
		}}));}

    /**Maps <code>JFileChooser.getDialogTitle()</code> through queue*/
    public String getDialogTitle() {
	return((String)runMapping(new MapAction("getDialogTitle") {
		public Object map() {
		    return(((JFileChooser)getSource()).getDialogTitle());
		}}));}

    /**Maps <code>JFileChooser.getDialogType()</code> through queue*/
    public int getDialogType() {
	return(runMapping(new MapIntegerAction("getDialogType") {
		public int map() {
		    return(((JFileChooser)getSource()).getDialogType());
		}}));}

    /**Maps <code>JFileChooser.getFileFilter()</code> through queue*/
    public FileFilter getFileFilter() {
	return((FileFilter)runMapping(new MapAction("getFileFilter") {
		public Object map() {
		    return(((JFileChooser)getSource()).getFileFilter());
		}}));}

    /**Maps <code>JFileChooser.getFileSelectionMode()</code> through queue*/
    public int getFileSelectionMode() {
	return(runMapping(new MapIntegerAction("getFileSelectionMode") {
		public int map() {
		    return(((JFileChooser)getSource()).getFileSelectionMode());
		}}));}

    /**Maps <code>JFileChooser.getFileSystemView()</code> through queue*/
    public FileSystemView getFileSystemView() {
	return((FileSystemView)runMapping(new MapAction("getFileSystemView") {
		public Object map() {
		    return(((JFileChooser)getSource()).getFileSystemView());
		}}));}

    /**Maps <code>JFileChooser.getFileView()</code> through queue*/
    public FileView getFileView() {
	return((FileView)runMapping(new MapAction("getFileView") {
		public Object map() {
		    return(((JFileChooser)getSource()).getFileView());
		}}));}

    /**Maps <code>JFileChooser.getIcon(File)</code> through queue*/
    public Icon getIcon(final File file) {
	return((Icon)runMapping(new MapAction("getIcon") {
		public Object map() {
		    return(((JFileChooser)getSource()).getIcon(file));
		}}));}

    /**Maps <code>JFileChooser.getName(File)</code> through queue*/
    public String getName(final File file) {
	return((String)runMapping(new MapAction("getName") {
		public Object map() {
		    return(((JFileChooser)getSource()).getName(file));
		}}));}

    /**Maps <code>JFileChooser.getSelectedFile()</code> through queue*/
    public File getSelectedFile() {
	return((File)runMapping(new MapAction("getSelectedFile") {
		public Object map() {
		    return(((JFileChooser)getSource()).getSelectedFile());
		}}));}

    /**Maps <code>JFileChooser.getSelectedFiles()</code> through queue*/
    public File[] getSelectedFiles() {
	return((File[])runMapping(new MapAction("getSelectedFiles") {
		public Object map() {
		    return(((JFileChooser)getSource()).getSelectedFiles());
		}}));}

    /**Maps <code>JFileChooser.getTypeDescription(File)</code> through queue*/
    public String getTypeDescription(final File file) {
	return((String)runMapping(new MapAction("getTypeDescription") {
		public Object map() {
		    return(((JFileChooser)getSource()).getTypeDescription(file));
		}}));}

    /**Maps <code>JFileChooser.getUI()</code> through queue*/
    public FileChooserUI getUI() {
	return((FileChooserUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JFileChooser)getSource()).getUI());
		}}));}

    /**Maps <code>JFileChooser.isDirectorySelectionEnabled()</code> through queue*/
    public boolean isDirectorySelectionEnabled() {
	return(runMapping(new MapBooleanAction("isDirectorySelectionEnabled") {
		public boolean map() {
		    return(((JFileChooser)getSource()).isDirectorySelectionEnabled());
		}}));}

    /**Maps <code>JFileChooser.isFileHidingEnabled()</code> through queue*/
    public boolean isFileHidingEnabled() {
	return(runMapping(new MapBooleanAction("isFileHidingEnabled") {
		public boolean map() {
		    return(((JFileChooser)getSource()).isFileHidingEnabled());
		}}));}

    /**Maps <code>JFileChooser.isFileSelectionEnabled()</code> through queue*/
    public boolean isFileSelectionEnabled() {
	return(runMapping(new MapBooleanAction("isFileSelectionEnabled") {
		public boolean map() {
		    return(((JFileChooser)getSource()).isFileSelectionEnabled());
		}}));}

    /**Maps <code>JFileChooser.isMultiSelectionEnabled()</code> through queue*/
    public boolean isMultiSelectionEnabled() {
	return(runMapping(new MapBooleanAction("isMultiSelectionEnabled") {
		public boolean map() {
		    return(((JFileChooser)getSource()).isMultiSelectionEnabled());
		}}));}

    /**Maps <code>JFileChooser.isTraversable(File)</code> through queue*/
    public boolean isTraversable(final File file) {
	return(runMapping(new MapBooleanAction("isTraversable") {
		public boolean map() {
		    return(((JFileChooser)getSource()).isTraversable(file));
		}}));}

    /**Maps <code>JFileChooser.removeActionListener(ActionListener)</code> through queue*/
    public void removeActionListener(final ActionListener actionListener) {
	runMapping(new MapVoidAction("removeActionListener") {
		public void map() {
		    ((JFileChooser)getSource()).removeActionListener(actionListener);
		}});}

    /**Maps <code>JFileChooser.removeChoosableFileFilter(FileFilter)</code> through queue*/
    public boolean removeChoosableFileFilter(final FileFilter fileFilter) {
	return(runMapping(new MapBooleanAction("removeChoosableFileFilter") {
		public boolean map() {
		    return(((JFileChooser)getSource()).removeChoosableFileFilter(fileFilter));
		}}));}

    /**Maps <code>JFileChooser.rescanCurrentDirectory()</code> through queue*/
    public void rescanCurrentDirectory() {
	runMapping(new MapVoidAction("rescanCurrentDirectory") {
		public void map() {
		    ((JFileChooser)getSource()).rescanCurrentDirectory();
		}});}

    /**Maps <code>JFileChooser.resetChoosableFileFilters()</code> through queue*/
    public void resetChoosableFileFilters() {
	runMapping(new MapVoidAction("resetChoosableFileFilters") {
		public void map() {
		    ((JFileChooser)getSource()).resetChoosableFileFilters();
		}});}

    /**Maps <code>JFileChooser.setAccessory(JComponent)</code> through queue*/
    public void setAccessory(final JComponent jComponent) {
	runMapping(new MapVoidAction("setAccessory") {
		public void map() {
		    ((JFileChooser)getSource()).setAccessory(jComponent);
		}});}

    /**Maps <code>JFileChooser.setApproveButtonMnemonic(char)</code> through queue*/
    public void setApproveButtonMnemonic(final char c) {
	runMapping(new MapVoidAction("setApproveButtonMnemonic") {
		public void map() {
		    ((JFileChooser)getSource()).setApproveButtonMnemonic(c);
		}});}

    /**Maps <code>JFileChooser.setApproveButtonMnemonic(int)</code> through queue*/
    public void setApproveButtonMnemonic(final int i) {
	runMapping(new MapVoidAction("setApproveButtonMnemonic") {
		public void map() {
		    ((JFileChooser)getSource()).setApproveButtonMnemonic(i);
		}});}

    /**Maps <code>JFileChooser.setApproveButtonText(String)</code> through queue*/
    public void setApproveButtonText(final String string) {
	runMapping(new MapVoidAction("setApproveButtonText") {
		public void map() {
		    ((JFileChooser)getSource()).setApproveButtonText(string);
		}});}

    /**Maps <code>JFileChooser.setApproveButtonToolTipText(String)</code> through queue*/
    public void setApproveButtonToolTipText(final String string) {
	runMapping(new MapVoidAction("setApproveButtonToolTipText") {
		public void map() {
		    ((JFileChooser)getSource()).setApproveButtonToolTipText(string);
		}});}

    /**Maps <code>JFileChooser.setCurrentDirectory(File)</code> through queue*/
    public void setCurrentDirectory(final File file) {
	runMapping(new MapVoidAction("setCurrentDirectory") {
		public void map() {
		    ((JFileChooser)getSource()).setCurrentDirectory(file);
		}});}

    /**Maps <code>JFileChooser.setDialogTitle(String)</code> through queue*/
    public void setDialogTitle(final String string) {
	runMapping(new MapVoidAction("setDialogTitle") {
		public void map() {
		    ((JFileChooser)getSource()).setDialogTitle(string);
		}});}

    /**Maps <code>JFileChooser.setDialogType(int)</code> through queue*/
    public void setDialogType(final int i) {
	runMapping(new MapVoidAction("setDialogType") {
		public void map() {
		    ((JFileChooser)getSource()).setDialogType(i);
		}});}

    /**Maps <code>JFileChooser.setFileFilter(FileFilter)</code> through queue*/
    public void setFileFilter(final FileFilter fileFilter) {
	runMapping(new MapVoidAction("setFileFilter") {
		public void map() {
		    ((JFileChooser)getSource()).setFileFilter(fileFilter);
		}});}

    /**Maps <code>JFileChooser.setFileHidingEnabled(boolean)</code> through queue*/
    public void setFileHidingEnabled(final boolean b) {
	runMapping(new MapVoidAction("setFileHidingEnabled") {
		public void map() {
		    ((JFileChooser)getSource()).setFileHidingEnabled(b);
		}});}

    /**Maps <code>JFileChooser.setFileSelectionMode(int)</code> through queue*/
    public void setFileSelectionMode(final int i) {
	runMapping(new MapVoidAction("setFileSelectionMode") {
		public void map() {
		    ((JFileChooser)getSource()).setFileSelectionMode(i);
		}});}

    /**Maps <code>JFileChooser.setFileSystemView(FileSystemView)</code> through queue*/
    public void setFileSystemView(final FileSystemView fileSystemView) {
	runMapping(new MapVoidAction("setFileSystemView") {
		public void map() {
		    ((JFileChooser)getSource()).setFileSystemView(fileSystemView);
		}});}

    /**Maps <code>JFileChooser.setFileView(FileView)</code> through queue*/
    public void setFileView(final FileView fileView) {
	runMapping(new MapVoidAction("setFileView") {
		public void map() {
		    ((JFileChooser)getSource()).setFileView(fileView);
		}});}

    /**Maps <code>JFileChooser.setMultiSelectionEnabled(boolean)</code> through queue*/
    public void setMultiSelectionEnabled(final boolean b) {
	runMapping(new MapVoidAction("setMultiSelectionEnabled") {
		public void map() {
		    ((JFileChooser)getSource()).setMultiSelectionEnabled(b);
		}});}

    /**Maps <code>JFileChooser.setSelectedFile(File)</code> through queue*/
    public void setSelectedFile(final File file) {
	runMapping(new MapVoidAction("setSelectedFile") {
		public void map() {
		    ((JFileChooser)getSource()).setSelectedFile(file);
		}});}

    /**Maps <code>JFileChooser.setSelectedFiles(File[])</code> through queue*/
    public void setSelectedFiles(final File[] file) {
	runMapping(new MapVoidAction("setSelectedFiles") {
		public void map() {
		    ((JFileChooser)getSource()).setSelectedFiles(file);
		}});}

    /**Maps <code>JFileChooser.showDialog(Component, String)</code> through queue*/
    public int showDialog(final Component component, final String string) {
	return(runMapping(new MapIntegerAction("showDialog") {
		public int map() {
		    return(((JFileChooser)getSource()).showDialog(component, string));
		}}));}

    /**Maps <code>JFileChooser.showOpenDialog(Component)</code> through queue*/
    public int showOpenDialog(final Component component) {
	return(runMapping(new MapIntegerAction("showOpenDialog") {
		public int map() {
		    return(((JFileChooser)getSource()).showOpenDialog(component));
		}}));}

    /**Maps <code>JFileChooser.showSaveDialog(Component)</code> through queue*/
    public int showSaveDialog(final Component component) {
	return(runMapping(new MapIntegerAction("showSaveDialog") {
		public int map() {
		    return(((JFileChooser)getSource()).showSaveDialog(component));
		}}));}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private void waitPainted(int index) {
	Waiter drawingWaiter = new Waiter(new Waitable() {
		public Object actionProduced(Object param) {
		    JList list = getFileList();
		    int last_one = list.getModel().getSize() - 1;
		    if(last_one == -1) {
			return("");
		    }
		    int current = (param != null) ? ((Integer)param).intValue() : 0;
		    if(list.getCellBounds(current, current) != null) {
			return(list.getCellBounds(last_one, last_one));
		    } else {
			return(null);
		    }
		}
		public String getDescription() {
		    return("List drawed");
		}
	    });
	drawingWaiter.setTimeouts(getTimeouts().cloneThis());
	drawingWaiter.
	    getTimeouts().
	    setTimeout("Waiter.WaitingTime",
		       getTimeouts().
		       getTimeout("JFileChooserOperator.WaitListPaintedTimeout"));
	drawingWaiter.setOutput(getOutput().createErrorOutput());
	try {
	    drawingWaiter.waitAction((index != -1) ? new Integer(index) : null);
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	}
    }


    private JComboBox getCombo(int index) {
	return((JComboBox)innerSearcher.
	       findComponent(new ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JComboBox);
		       }
		       public String getDescription() {
			   return("JComboBox");
		       }
		   }, index));
    }

    private JButton getNoTextButton(int index) {
	return((JButton)innerSearcher.
	       findComponent(new ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JButton &&
				  !(comp.getParent() instanceof JComboBox) &&
				  ((JButton)comp).getText().length() == 0);
		       }
		       public String getDescription() {
			   return("JButton");
		       }
		   }, index));
    }

    private JToggleButton getToggleButton(int index) {
	return((JToggleButton)innerSearcher.
	       findComponent(new ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JToggleButton);
		       }
		       public String getDescription() {
			   return("JToggleButton");
		       }
		   }, index));
    }

    private int findFileIndex(String file, StringComparator comparator) {
	File[] files = getFiles();
	for(int i = 0; i < files.length; i++) {
	    if(comparator.equals(files[i].getName(), 
				 file)) {
		return(i);
	    }
	}
	return(-1);
    }

    private int findDirIndex(String dir, StringComparator comparator) {
	ComboBoxModel cbModel = getPathCombo().getModel();
	for(int i = cbModel.getSize() - 1; i >= 0; i--) {
	    if(comparator.equals(((File)cbModel.getElementAt(i)).getName(), 
				 dir)) {
		return(i);
	    }
	}
	return(-1);
    }

    private int findFileTypeIndex(String fileType, StringComparator comparator) {
	ComboBoxModel cbModel = getFileTypesCombo().getModel();
	for(int i = 0; i < cbModel.getSize(); i++) {
	    if(comparator.equals(((FileFilter)cbModel.getElementAt(i)).getDescription(), 
				 fileType)) {
		return(i);
	    }
	}
	return(-1);
    }

    private void selectPathDirectory(String dir, StringComparator comparator) {
	output.printTrace("Select \"" + dir + "\" directory in JFileChooser\n    : " +
			  getSource().toString());
	JComboBoxOperator comboOper = new JComboBoxOperator(getPathCombo());
	comboOper.copyEnvironment(this);
	comboOper.setOutput(output.createErrorOutput());
	comboOper.selectItem(findDirIndex(dir, comparator));
	waitPainted(-1);
    }

    private void clickOnFile(String file, StringComparator comparator, int clickCount) {
	output.printTrace("Click " + Integer.toString(clickCount) + 
			  "times on \"" + file + 
			  "\" file in JFileChooser\n    : " +
			  getSource().toString());
	clickOnFile(findFileIndex(file, comparator), clickCount);
    }

    private void selectFileType(String filter, StringComparator comparator) {
	output.printTrace("Select \"" + filter + "\" file type in JFileChooser\n    : " +
			  getSource().toString());
	JComboBoxOperator comboOper = new JComboBoxOperator(getFileTypesCombo());
	comboOper.copyEnvironment(this);
	comboOper.setOutput(output.createErrorOutput());
	comboOper.selectItem(findFileTypeIndex(filter, comparator));
	waitPainted(-1);
    }

    private boolean checkFileDisplayed(String file, StringComparator comparator) {
	waitPainted(-1);
	return(findFileIndex(file, comparator) != -1);
    }

    private static class JFileChooserJDialogFinder implements ComponentChooser {
	TestOut output;
	ComponentChooser subChooser;
	public JFileChooserJDialogFinder(TestOut output) {
	    this.output = output;
	    subChooser = new JFileChooserFinder();
	}
	public boolean checkComponent(Component comp) {
	    ComponentSearcher searcher = 
		new ComponentSearcher((Container)comp);
	    searcher.setOutput(output);
	    return(searcher.findComponent(subChooser) != null);
	}
	public String getDescription() {
	    return("JFileChooser's window");
	}
    }

    private static class JFileChooserFinder implements ComponentChooser {
	public JFileChooserFinder() {
	}
	public boolean checkComponent(Component comp) {
	    return(comp instanceof JFileChooser);
	}
	public String getDescription() {
	    return("JFileChooser");
	}
    }

    private class ButtonFinder implements ComponentChooser {
	String text;
	public ButtonFinder(String text) {
	    this.text = text;
	}
	public boolean checkComponent(Component comp) {
	    return(comp instanceof JButton &&
		   ((JButton)comp).
		   getText().
		   equals(text));
	}
	public String getDescription() {
	    return("\"" + text + "\" button");
	}
    }

}

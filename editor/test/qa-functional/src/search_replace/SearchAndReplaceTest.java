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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package search_replace;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jellytools.modules.editor.Find;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 * Basic Editor Find and Replace Tests
 *
 * @author Martin Roskanin
 */


public class SearchAndReplaceTest extends lib.EditorTestCase{
    
    // private PrintStream wrapper for System.out
    private PrintStream systemOutPSWrapper = new PrintStream(System.out);
    private int index = 0;
    private EditorOperator editor;
    private JEditorPaneOperator txtOper;
    private int WAIT_MAX_MILIS_FOR_FIND_OPERATION = 5000;
    
    public static final int NO_OPERATION              = 0x00000000;
    public static final int MATCH_CASE                = 0x00000001;
    public static final int WHOLE_WORDS               = 0x00000002;
    public static final int REGULAR_EXPRESSIONS       = 0x00000004;
    public static final int HIGHLIGHT_RESULTS         = 0x00000008;
    public static final int WRAP_AROUND               = 0x00000010;
    public static final int SEARCH_SELECTION          = 0x00000020;
    public static final int SEARCH_BACKWARDS          = 0x00000040;
    public static final int INCREMENTAL_SEARCH        = 0x00000080;
    public static final int ALL_UNCHECKED             = 0x10000000;
    public static final int NO_RESET                  = 0x20000000;
    public static final int NO_RESET_SEARCH_SELECTION = 0x40000000;
    
    /** Creates a new instance of Main */
    public SearchAndReplaceTest(String testMethodName) {
        super(testMethodName);
    }
    
    private String getIndexAsString(){
        String ret = String.valueOf(index);
        if (ret.length() == 1) ret = "0" + ret;
        return ret;
    }
    
    private String getRefFileName(){
        return this.getName()+getIndexAsString()+".ref"; //NOI18N
    }
    
    private String getGoldenFileName(){
        return this.getName()+getIndexAsString()+".pass"; //NOI18N
    }
    
    private String getDiffFileName(){
        return this.getName()+getIndexAsString()+".diff"; //NOI18N
    }
    
    // hashtable holding all already used logs and correspondig printstreams
    private Hashtable logStreamTable = null;
    
    private PrintStream getFileLog(String logName) throws IOException {
        OutputStream outputStream;
        FileOutputStream fileOutputStream;
        
        if ((logStreamTable == null)|(hasTestMethodChanged())) {
            // we haven't used logging capability - create hashtables
            logStreamTable = new Hashtable();
            //System.out.println("Created new hashtable");
        } else {
            if (logStreamTable.containsKey(logName)) {
                //System.out.println("Getting stream from cache:"+logName);
                return (PrintStream)logStreamTable.get(logName);
            }
        }
        // we didn't used this log, so let's create it
        FileOutputStream fileLog = new FileOutputStream(new File(getWorkDir(),logName));
        PrintStream printStreamLog = new PrintStream(fileLog,true);
        logStreamTable.put(logName,printStreamLog);
        //System.out.println("Created new stream:"+logName);
        return printStreamLog;
    }
    
    private String lastTestMethod=null;
    
    private boolean hasTestMethodChanged() {
        if (!this.getName().equals(lastTestMethod)) {
            lastTestMethod=this.getName();
            return true;
        } else {
            return false;
        }
    }
    
    public PrintStream getRef() {
        String refFilename = getRefFileName();
        try {
            return getFileLog(refFilename);
        } catch (IOException ioe) {
            // canot get ref file - return system.out
            //System.err.println("Test method "+this.getName()+" - cannot open ref file:"+refFilename
            //                                +" - defaulting to System.out and failing test");
            fail("Could not open reference file: "+refFilename);
            return  systemOutPSWrapper;
        }
    }
    
    protected void compareToGoldenFile(Document testDoc){
        try {
            ref(testDoc.getText(0, testDoc.getLength()));
            compareReferenceFiles(getRefFileName(), getGoldenFileName(), getDiffFileName());
            index++;
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
        }
    }
    
    private void resetFindProperties(Find find, boolean resetSearchSelection){
        find.cbMatchCase().setSelected(false);
        find.cbMatchWholeWordsOnly().setSelected(false);
        find.cbRegularExpressions().setSelected(false);
        find.cbHighlightSearch().setSelected(false);
        find.cbWrapSearch().setSelected(false);
        if (resetSearchSelection) {
            find.cbBlockSearch().setSelected(false);
        }
        find.cbBackwardSearch().setSelected(false);
        find.cbIncrementalSearch().setSelected(false);
        find.cboFindWhat().clearText();
    }
    
    protected Find openFindDialog(String text, int modifiers){
        new FindAction().perform();
        Find find = new Find();
        if (modifiers != 0 && (modifiers & NO_RESET) == 0) {
            resetFindProperties(find, (modifiers & NO_RESET_SEARCH_SELECTION) == 0);
        }
        if ((modifiers & MATCH_CASE) != 0){
            find.cbMatchCase().setSelected(true);
        }
        if ((modifiers & WHOLE_WORDS) != 0){
            find.cbMatchWholeWordsOnly().setSelected(true);
        }
        if ((modifiers & REGULAR_EXPRESSIONS) != 0){
            find.cbRegularExpressions().setSelected(true);
        }
        if ((modifiers & HIGHLIGHT_RESULTS) != 0){
            find.cbHighlightSearch().setSelected(true);
        }
        if ((modifiers & WRAP_AROUND) != 0){
            find.cbWrapSearch().setSelected(true);
        }
        if ((modifiers & SEARCH_SELECTION) != 0){
            find.cbBlockSearch().setSelected(true);
        }
        if ((modifiers & SEARCH_BACKWARDS) != 0){
            find.cbBackwardSearch().setSelected(true);
        }
        if ((modifiers & INCREMENTAL_SEARCH) != 0){
            find.cbIncrementalSearch().setSelected(true);
        }
        find.cboFindWhat().clearText();
        if (text != null){
            find.cboFindWhat().typeText(text);
        }
        return find;
    }
    
    private ValueResolver getSelectionResolver(final JEditorPaneOperator txtOper, final int startEtalon, final int endEtalon){
        
        ValueResolver clipboardValueResolver = new ValueResolver(){
            public Object getValue(){
                int selectionStart = txtOper.getSelectionStart();
                int selectionEnd = txtOper.getSelectionEnd();
                if (selectionStart == selectionEnd){
                    selectionStart = -1;
                    selectionEnd = -1;
                }
                if (selectionStart != startEtalon || selectionEnd != endEtalon){
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            }
        };
        
        return clipboardValueResolver;
    }
    
    private ValueResolver getIncFindResolver(final JEditorPaneOperator txtOper, final int startEtalon, final int endEtalon){
        
        ValueResolver clipboardValueResolver = new ValueResolver(){
            public Object getValue(){
                org.netbeans.editor.BaseTextUI ui = (org.netbeans.editor.BaseTextUI)txtOper.getUI();
                org.netbeans.editor.EditorUI editorUI = ui.getEditorUI();
                org.netbeans.editor.DrawLayerFactory.IncSearchLayer incLayer
                        = (org.netbeans.editor.DrawLayerFactory.IncSearchLayer)editorUI.findLayer(
                        org.netbeans.editor.DrawLayerFactory.INC_SEARCH_LAYER_NAME);
                int selectionStart = -1;
                int selectionEnd = -1;
                if (incLayer == null) {
                    return Boolean.FALSE;
                } else {
                    if (incLayer.isEnabled()) {
                        selectionStart = incLayer.getOffset();
                        selectionEnd = selectionStart + incLayer.getLength();
                    }
                }
                if (selectionStart == selectionEnd){
                    selectionStart = -1;
                    selectionEnd = -1;
                }
                if (selectionStart != startEtalon || selectionEnd != endEtalon){
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            }
        };
        
        return clipboardValueResolver;
    }
    
    
    protected boolean find(String text, int modifiers, int startEtalon, int endEtalon, int setCaretPos){
        if (setCaretPos > -1){
            txtOper.setCaretPosition(setCaretPos);
        }
        Find find = openFindDialog(text, modifiers);
        find.find();
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_FIND_OPERATION, getSelectionResolver(txtOper, startEtalon, endEtalon), Boolean.TRUE);
        int selectionStart = txtOper.getSelectionStart();
        int selectionEnd = txtOper.getSelectionEnd();
        if (selectionStart == selectionEnd){
            selectionEnd = -1;
            selectionStart = -1;
        }
        if (selectionStart != startEtalon || selectionEnd != endEtalon){
            log("--------------------------------------------------");
            log("Find operation failed. Selected text: "+selectionStart+" - "+selectionEnd+" >>> Expected values: "+startEtalon+" - "+endEtalon);
            log("find dialog find what combo value:"+find.cboFindWhat().getEditor().getItem());
            log("checkboxes:"+
                    "\n   cbBackwardSearch:"+find.cbBackwardSearch().isSelected()+
                    "\n   cbBlockSearch:"+find.cbBlockSearch().isSelected()+
                    "\n   cbHighlightSearch:"+find.cbHighlightSearch().isSelected()+
                    "\n   cbIncrementalSearch:"+find.cbIncrementalSearch().isSelected()+
                    "\n   cbMatchCase:"+find.cbMatchCase().isSelected()+
                    "\n   cbMatchWholeWordsOnly:"+find.cbMatchWholeWordsOnly().isSelected()+
                    "\n   cbRegularExpressions:"+find.cbRegularExpressions().isSelected()+
                    "\n   cbWrapSearch:"+find.cbWrapSearch().isSelected()
                    );
            log("--------------------------------------------------");
            fail("Find operation failed. Selected text: "+selectionStart+" - "+selectionEnd+" >>> Expected values: "+startEtalon+" - "+endEtalon); //NOI18N
            find.close();
            return false;
        }
        find.close();
        return true;
    }
    
    private void checkIncrementalSearch(Find find, String s, int startEtalon, int endEtalon){
        // Checking Disabled - the new highlighting SPI will be used so the draw layer's data should not be checked directly.
        if (true)
            return;
        
        find.cboFindWhat().typeText(s);
        
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_FIND_OPERATION, getIncFindResolver(txtOper, startEtalon, endEtalon), Boolean.TRUE);
        
        int selectionStart = -1;
        int selectionEnd = -1;
        
        org.netbeans.editor.BaseTextUI ui = (org.netbeans.editor.BaseTextUI)txtOper.getUI();
        org.netbeans.editor.EditorUI editorUI = ui.getEditorUI();
        org.netbeans.editor.DrawLayerFactory.IncSearchLayer incLayer
                = (org.netbeans.editor.DrawLayerFactory.IncSearchLayer)editorUI.findLayer(
                org.netbeans.editor.DrawLayerFactory.INC_SEARCH_LAYER_NAME);
        if (incLayer == null) {
            System.out.println("fail: layer not initialized");
        } else {
            if (incLayer.isEnabled()) {
                selectionStart = incLayer.getOffset();
                selectionEnd = selectionStart + incLayer.getLength();
            }
        }
        
        if (selectionStart == selectionEnd){
            selectionEnd = -1;
            selectionStart = -1;
        }
        if (selectionStart != startEtalon || selectionEnd != endEtalon){
            fail("Incremental find operation failed. Selected text: "+selectionStart+" - "+selectionEnd+" >>> Expected values: "+startEtalon+" - "+endEtalon); //NOI18N
        }
    }
    
    private void preselect(JEditorPaneOperator txtOper, int start, int end){
        txtOper.setSelectionStart(start);
        txtOper.setSelectionEnd(end);
    }
    
    private void checkSelection(JEditorPaneOperator txtOper, int startEtalon, int endEtalon, String errorMessage){
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_FIND_OPERATION, getSelectionResolver(txtOper, startEtalon, endEtalon), Boolean.TRUE);
        int selectionStart = txtOper.getSelectionStart();
        int selectionEnd = txtOper.getSelectionEnd();
        if (selectionStart == selectionEnd){
            selectionStart = -1;
            selectionEnd = -1;
        }
        if (selectionStart != startEtalon || selectionEnd != endEtalon){
            fail(errorMessage+" Selected text: "+selectionStart+" - "+selectionEnd+" >>> Expected values: "+startEtalon+" - "+endEtalon); //NOI18N
        }
    }
    
    public void testSearch(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            editor = getDefaultSampleEditorOperator();
            txtOper = editor.txtEditorPane();
            
            find("searchText", ALL_UNCHECKED, 161, 171, 0);
            find("SeArCHText", MATCH_CASE, 175, 185, 0);
            find("SeArCHText", WHOLE_WORDS, 275, 285, 206);
            find("SeArCHText", MATCH_CASE | WHOLE_WORDS, 289, 299, 206);
            find("SeArCHText", SEARCH_BACKWARDS, 341, 351, 352);
            find("SeArCHText", SEARCH_BACKWARDS | MATCH_CASE, 289, 299, 352);
            find("search", SEARCH_BACKWARDS | WHOLE_WORDS, 318, 324, 352);
            find("search", SEARCH_BACKWARDS | MATCH_CASE | WHOLE_WORDS, 311, 317, 352);
            find("insert", SEARCH_BACKWARDS | WRAP_AROUND, 86, 92, 86);
            find("insert", SEARCH_BACKWARDS, -1, -1, 86);
            
            //incremental search
            txtOper.setCaretPosition(328);
            Find find = openFindDialog(null, INCREMENTAL_SEARCH);
            checkIncrementalSearch(find, "t", 328, 329);
            checkIncrementalSearch(find, "e", 330, 332);
            checkIncrementalSearch(find, "x", 330, 333);
            checkIncrementalSearch(find, "t", 330, 334);
            checkIncrementalSearch(find, "x", 429, 434);
            checkIncrementalSearch(find, "y", -1, -1); // inc should fail
            find.close();
            
            //incremental search backwards + Match case
            txtOper.setCaretPosition(328);
            find = openFindDialog(null, INCREMENTAL_SEARCH | MATCH_CASE | SEARCH_BACKWARDS);
            checkIncrementalSearch(find, "s", 311, 312);
            checkIncrementalSearch(find, "e", 311, 313);
            checkIncrementalSearch(find, "a", 311, 314);
            checkIncrementalSearch(find, "r", 311, 315);
            checkIncrementalSearch(find, "c", 311, 316);
            checkIncrementalSearch(find, "h", 311, 317);
            checkIncrementalSearch(find, "T", 275, 282);
            checkIncrementalSearch(find, "e", 275, 283);
            checkIncrementalSearch(find, "x", 275, 284);
            checkIncrementalSearch(find, "T", -1, -1);
            find.close();
            
            //#53536 - CTRL-F & friends cancel selection
            txtOper.setSelectionStart(1);
            txtOper.setSelectionEnd(100);
            // NO_OPERATION - no check box reset, no checkbox set by default.
            final Find blockFind = openFindDialog(null, NO_OPERATION);
            
            // check if the "search selection" checkbox was checked
            waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_FIND_OPERATION, new ValueResolver(){
                public Object getValue(){
                    return Boolean.valueOf(blockFind.cbBlockSearch().isSelected());
                }
            }, Boolean.TRUE);
            if (!blockFind.cbBlockSearch().isSelected()){
                fail("Search Selection checkbox was not checked automaticaly after invoking " +
                        "Find dialog over selected text"); //NOI18N
            }
            
            blockFind.close();
            // Selection made before Find dialog invocation gets lost.
            // Either a searched text was found and the found text gets selected
            // or the searched text is not found and then no selection is done.
            // The selection is only retained in case there was nothing typed into the "find what" field
            
            //checkSelection(txtOper, 1, 100, "Issue #53536 testing failed!");
            
            //test find in selection
            find = openFindDialog(null, ALL_UNCHECKED); // reset find dialog checkboxes
            find.close();
            preselect(txtOper, 46, 132);
            // make sure that text outside the selection is not found
            find("searchText", NO_OPERATION, -1, -1, -1);
            
            // selection begins at ublic. 'p' is not selected. Next public
            // should be found
            preselect(txtOper, 47, 123);
            find("public", NO_RESET, 105, 111, -1);
            
            preselect(txtOper, 161, 544);
            find("SeArCHText", NO_RESET_SEARCH_SELECTION | MATCH_CASE, 175, 185, -1);
            preselect(txtOper, 206, 544);
            find("SeArCHText", NO_RESET_SEARCH_SELECTION | WHOLE_WORDS, 275, 285, -1);
            preselect(txtOper, 206, 544);
            find("SeArCHText", NO_RESET_SEARCH_SELECTION | MATCH_CASE | WHOLE_WORDS, 289, 299, -1);
            preselect(txtOper, 161, 544);
            find("searchText", NO_RESET_SEARCH_SELECTION | SEARCH_BACKWARDS, 469, 479, -1);
            preselect(txtOper, 161, 544);
            find("searchText", NO_RESET_SEARCH_SELECTION | SEARCH_BACKWARDS | MATCH_CASE, 455, 465, -1);
            preselect(txtOper, 161, 544);
            find("search", NO_RESET_SEARCH_SELECTION | SEARCH_BACKWARDS | WHOLE_WORDS, 318, 324, -1);
            preselect(txtOper, 161, 544);
            find("search", NO_RESET_SEARCH_SELECTION | SEARCH_BACKWARDS | MATCH_CASE | WHOLE_WORDS, 311, 317, -1);
            
            // wrap around block forwardSearch testing
            find = openFindDialog(null, ALL_UNCHECKED); // reset find dialog checkboxes
            find.close();
            preselect(txtOper, 409, 465);
            find = openFindDialog("searchText", NO_OPERATION); // search selection should be checked automatically
            find.find();
            checkSelection(txtOper, 410, 420, "Wrap around block testing failed!");
            find.find();
            checkSelection(txtOper, 423, 433, "Wrap around block testing failed!");
            find.find();
            checkSelection(txtOper, 455, 465, "Wrap around block testing failed!");
            find.find();
            checkSelection(txtOper, -1, -1, "Wrap around block testing failed!"); // should find, because wrap around is not checked yet
            find.cbWrapSearch().setSelected(true);
            find.find();
            checkSelection(txtOper, 410, 420, "Wrap around block testing failed!");
            find.close();
            
            // wrap around block bacwardSearch testing
            find = openFindDialog(null, ALL_UNCHECKED); // reset find dialog checkboxes
            find.close();
            preselect(txtOper, 409, 465);
            find = openFindDialog("searchText", NO_RESET_SEARCH_SELECTION | SEARCH_BACKWARDS);
            find.find();
            checkSelection(txtOper, 455, 465, "Wrap around block testing failed!");
            find.find();
            checkSelection(txtOper, 423, 433, "Wrap around block testing failed!");
            find.find();
            checkSelection(txtOper, 410, 420, "Wrap around block testing failed!");
            find.find();
            checkSelection(txtOper, -1, -1, "Wrap around block testing failed!"); // should find, because wrap around is not checked yet
            find.cbWrapSearch().setSelected(true);
            find.find();
            checkSelection(txtOper, 455, 465, "Wrap around block testing failed!");
            find.close();
            
            //incremental search in selected block
            find = openFindDialog(null, ALL_UNCHECKED); // reset find dialog checkboxes
            find.close();
            preselect(txtOper, 325, 360);
            find = openFindDialog(null, NO_RESET_SEARCH_SELECTION | INCREMENTAL_SEARCH);
            checkIncrementalSearch(find, "t", 325, 326);
            checkIncrementalSearch(find, "e", 325, 327);
            checkIncrementalSearch(find, "x", 325, 328);
            checkIncrementalSearch(find, "t", 325, 329);
            checkIncrementalSearch(find, "x", -1, -1); // inc should fail
            find.close();
            
            //incremental search backwards + Match case in selected block
            find = openFindDialog(null, ALL_UNCHECKED); // reset find dialog checkboxes
            find.close();
            preselect(txtOper, 251 , 350);
            find = openFindDialog(null, NO_RESET_SEARCH_SELECTION | INCREMENTAL_SEARCH | MATCH_CASE | SEARCH_BACKWARDS);
            checkIncrementalSearch(find, "T", 347, 348);
            checkIncrementalSearch(find, "e", 347, 349);
            checkIncrementalSearch(find, "x", 347, 350);
            checkIncrementalSearch(find, "t", 295, 299);
            checkIncrementalSearch(find, "X", -1, -1); // fails - behind selected area
            find.close();                       
        } finally {
            closeFileWithDiscard();
        }
    }
    
    public void testSearch2(){
        openDefaultProject();
        openDefaultSampleFile();
        Find find;
        try {
            editor = getDefaultSampleEditorOperator();
            txtOper = editor.txtEditorPane();
            
            //#52115
            // firstly try CTRL+V
            editor.setCaretPosition(16, 9);  //word "search"
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK);
            cutCopyViaStrokes(txtOper, KeyEvent.VK_C, KeyEvent.CTRL_MASK);
            editor.setCaretPosition(1, 1);
            find = openFindDialog(null, ALL_UNCHECKED); // reset find dialog checkboxes
            find.cboFindWhat().requestFocus(); // [temp] failing tests on SunOS & Linux
            waitForAWTThread();
            pasteViaStrokes(find, KeyEvent.VK_V, KeyEvent.CTRL_MASK, null);
            waitForAWTThread();
            find.btFind().requestFocus();
            waitForAWTThread();
            log("Searching for: "+find.cboFindWhat().getTextField().getText());
            find.find();
            find.close();
            checkSelection(txtOper, 8, 14, "Issue #52115 testing failed on CTRL+V!");
            // then Shift+Insert
            editor.setCaretPosition(327);  //word "text"
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK);
            cutCopyViaStrokes(txtOper, KeyEvent.VK_C, KeyEvent.CTRL_MASK);
            editor.setCaretPosition(1, 1);
            find = openFindDialog(null, ALL_UNCHECKED); // reset find dialog checkboxes
            find.cboFindWhat().requestFocus(); // [temp] failing tests on SunOS & Linux
            waitForAWTThread();
            pasteViaStrokes(find, KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK, null);
            waitForAWTThread();
            find.btFind().requestFocus();
            waitForAWTThread();
            log("Searching for: "+find.cboFindWhat().getTextField().getText());
            find.find();
            find.close();
            checkSelection(txtOper, 167, 171, "Issue #52115 testing failed on Shift+Insert!");
        } finally {
            closeFileWithDiscard();
        }
        
    }
    public void waitForAWTThread() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    log("Stop waiting");
                }
            });
        } catch (Exception e) {
            //ignored
        }
    }
    
    public void testRegExSearch(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            editor = getDefaultSampleEditorOperator();
            txtOper = editor.txtEditorPane();
            
            //test whether the "Whole Words" and "Incremental Search" are disabled during regEx
            final Find findRegEx = openFindDialog(null, REGULAR_EXPRESSIONS);
            waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_FIND_OPERATION, new ValueResolver(){
                public Object getValue(){
                    return Boolean.valueOf(
                            findRegEx.cbMatchWholeWordsOnly().isEnabled() &&
                            findRegEx.cbIncrementalSearch().isEnabled());
                }
            }, Boolean.FALSE);
            if (findRegEx.cbMatchWholeWordsOnly().isEnabled() || findRegEx.cbIncrementalSearch().isEnabled()){
                fail("Items disabling failed. \"Whole Words\" and \"Incremental Search\" should be disabled during regEx!  " + //NOI18N
                        "(\"Whole Words\" = "+findRegEx.cbMatchWholeWordsOnly().isEnabled()+ //NOI18N
                        ", \"Incremental Search\" = "+findRegEx.cbIncrementalSearch().isEnabled()+")"); //NOI18N
            }
            findRegEx.close();
            
            
            find("teest", REGULAR_EXPRESSIONS, 309, 314, 0);
            find("t.*st", REGULAR_EXPRESSIONS, 325, 337, 314);// find next teee...st
            find("t.*st", REGULAR_EXPRESSIONS, 348, 356, 326);// find next Teee...st, caret is just behind t
            find("T.*st", REGULAR_EXPRESSIONS | MATCH_CASE, 348, 356, 309);// find case sensitively Teee...st, skipping teee...st
            find("t.*st", REGULAR_EXPRESSIONS | SEARCH_BACKWARDS, 348, 356, 356);
            find("t.*st", REGULAR_EXPRESSIONS | SEARCH_BACKWARDS | MATCH_CASE, 325, 337, 356);
            
            // find one line strings + Wrap Search Testing
            String lineStringsExp = "\"[^\"\\r\\n]*\"";
            editor.setCaretPosition(225);
            Find find = openFindDialog(lineStringsExp, REGULAR_EXPRESSIONS);
            find.find();
            checkSelection(txtOper, 267, 286, "Line string search failed.");
            find.find();
            checkSelection(txtOper, 417, 430, "Line string search failed.");
            find.find();
            checkSelection(txtOper, -1, -1, "Line string search failed.");
            find.cbWrapSearch().setSelected(true);
            find.find();
            checkSelection(txtOper, 224, 237, "Line string wrap search failed.");
            find.close();
            
            // find one line strings + Wrap Search Testing + BACKWARD
            editor.setCaretPosition(429);
            find = openFindDialog(lineStringsExp, REGULAR_EXPRESSIONS | SEARCH_BACKWARDS);
            find.find();
            checkSelection(txtOper, 267, 286, "Line string BWD search failed.");
            find.find();
            checkSelection(txtOper, 224, 237, "Line string BWV search failed.");
            find.find();
            checkSelection(txtOper, -1, -1, "Line string BWV search failed.");
            find.cbWrapSearch().setSelected(true);
            find.find();
            checkSelection(txtOper, 417, 430, "Line string BWV wrap search failed.");
            find.close();
            
            //multiline strings
            find("\"[^\"]*\"", REGULAR_EXPRESSIONS, 456, 510, 432);
            
            // wrap around block forwardRegExSearch testing
            find = openFindDialog(null, REGULAR_EXPRESSIONS); // reset find dialog checkboxes
            find.close();
            preselect(txtOper, 326, 389);
            find = openFindDialog("T.*st", NO_OPERATION); // search selection should be checked automatically
            find.find();
            checkSelection(txtOper, 348, 356, "Wrap around block regEx testing failed!");
            find.find();
            checkSelection(txtOper, 367, 373, "Wrap around block regEx testing failed!");
            find.find();
            checkSelection(txtOper, -1, -1, "Wrap around block regEx testing failed!"); // should find, because wrap around is not checked yet
            find.cbWrapSearch().setSelected(true);
            find.find();
            checkSelection(txtOper, 348, 356, "Wrap around block regEx testing failed!");
            find.close();
            
            // wrap around block backward RegExSearch testing + match case
            find = openFindDialog(null, REGULAR_EXPRESSIONS | SEARCH_BACKWARDS | MATCH_CASE); // reset find dialog checkboxes
            find.close();
            preselect(txtOper, 252, 369);
            find = openFindDialog("t.*st", NO_OPERATION); // search selection should be checked automatically
            find.find();
            checkSelection(txtOper, 325, 337, "Wrap around block BWD regEx testing failed!");
            find.find();
            checkSelection(txtOper, 309, 314, "Wrap around block BWD regEx testing failed!");
            find.find();
            checkSelection(txtOper, -1, -1, "Wrap around block BWV regEx testing failed!"); // should find, because wrap around is not checked yet
            find.cbWrapSearch().setSelected(true);
            find.find();
            checkSelection(txtOper, 325, 337, "Wrap around block BWV regEx testing failed!");
            find.close();
            
            // find end line whitespaces
            String lineEndWhitespaces = "[ \\t]+$";
            editor.setCaretPosition(1);
            find = openFindDialog(lineEndWhitespaces, REGULAR_EXPRESSIONS);
            find.find();
            checkSelection(txtOper, 104, 108, "Find end line whitespaces testing failed!");
            find.find();
            checkSelection(txtOper, 443, 444, "Find end line whitespaces testing failed!");
            find.find();
            checkSelection(txtOper, 510, 511, "Find end line whitespaces testing failed!");
            find.find();
            checkSelection(txtOper, 599, 607, "Find end line whitespaces testing failed!");
            find.close();
            
            
        } finally {
            closeFileWithDiscard();
        }
    }
    
}
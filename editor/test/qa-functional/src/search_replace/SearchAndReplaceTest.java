/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package search_replace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
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
    
    
    public static final int MATCH_CASE          = 0x00000001;
    public static final int WHOLE_WORDS         = 0x00000002;
    public static final int REGULAR_EXPRESSIONS = 0x00000004;
    public static final int HIGHLIGHT_RESULTS   = 0x00000008;
    public static final int WRAP_AROUND         = 0x00000010;
    public static final int SEARCH_SELECTION    = 0x00000020;
    public static final int SEARCH_BACKWARDS    = 0x00000040;
    public static final int INCREMENTAL_SEARCH  = 0x00000080;
    
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
    
    private void resetFindProperties(Find find){
        find.cbMatchCase().setSelected(false);
        find.cbMatchWholeWordsOnly().setSelected(false);
        find.cbRegularExpressions().setSelected(false);
        find.cbHighlightSearch().setSelected(false);
        find.cbWrapSearch().setSelected(false);
        find.cbBlockSearch().setSelected(false);
        find.cbBackwardSearch().setSelected(false);
        find.cbIncrementalSearch().setSelected(false);
        find.cboFindWhat().typeText("");
    }
    
    protected Find openFindDialog(String text, int modifiers){
        new FindAction().perform();
        Find find = new Find();
        resetFindProperties(find);
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
        if (text != null){
            find.cboFindWhat().typeText(text);
        }
        return find;
    }

    private ValueResolver getFindResolver(final JEditorPaneOperator txtOper, final int startEtalon, final int endEtalon){
        
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
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_FIND_OPERATION, getFindResolver(txtOper, startEtalon, endEtalon), Boolean.TRUE);
        int selectionStart = txtOper.getSelectionStart();
        int selectionEnd = txtOper.getSelectionEnd();
        if (selectionStart == selectionEnd){
            selectionEnd = -1;
            selectionStart = -1;
        }
        if (selectionStart != startEtalon || selectionEnd != endEtalon){
            fail("Find operation failed. Selected text: "+selectionStart+" - "+selectionEnd+" >>> Expected values: "+startEtalon+" - "+endEtalon); //NOI18N
            find.close();            
            return false;
        }
        find.close();
        return true;
    }

    private void checkIncrementalSearch(Find find, String s, int startEtalon, int endEtalon){
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
    
    public void testSearch(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            editor = getDefaultSampleEditorOperator();
            txtOper = editor.txtEditorPane();

            find("searchText", 0, 161, 171, 0);
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
            
        } finally {
            closeFileWithDiscard();
        }
    }
    
}
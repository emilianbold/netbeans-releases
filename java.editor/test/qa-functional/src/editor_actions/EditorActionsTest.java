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

package editor_actions;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 * Basic Editor Actions Test class.
 * It contains basic editor actions functionality methods.
 *
 *
 * @author Martin Roskanin
 */
  public class EditorActionsTest extends EditorTestCase {

    // private PrintStream wrapper for System.out
    private PrintStream systemOutPSWrapper = new PrintStream(System.out);
    private int index = 0;
    public static final int WAIT_MAX_MILIS_FOR_UNDO_REDO = 2000;      
      
    /** Creates a new instance of Main */
    public EditorActionsTest(String testMethodName) {
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
        //waitForMilis(150);
        try {
        ref(testDoc.getText(0, testDoc.getLength()));
        compareReferenceFiles(getRefFileName(), getGoldenFileName(), getDiffFileName());
        index++;
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
        }
    }
    
    
    protected void waitForMilis(int maxMiliSeconds){
        int time = (int) maxMiliSeconds / 100;
        while (time > 0) {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
            
        }
    }

    protected ValueResolver getFileLengthChangeResolver(final JEditorPaneOperator txtOper, final int oldLength){
        log("");
        log("oldLength:"+oldLength);
        ValueResolver fileLengthValueResolver = new ValueResolver(){
            public Object getValue(){
                int newLength = txtOper.getDocument().getLength();
                log("newLength:"+newLength);
                return (newLength == oldLength) ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        
        return fileLengthValueResolver;
    }
    
    protected void resetCounter() {
        index = 0;
    }
    
}

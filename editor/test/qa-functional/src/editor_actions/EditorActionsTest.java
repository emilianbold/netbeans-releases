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
    public static final int WAIT_MAX_MILIS_FOR_CLIPBOARD = 4000;
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
    
    protected ValueResolver getClipboardResolver(final JEditorPaneOperator txtOper, final Transferable oldClipValue){
        
        ValueResolver clipboardValueResolver = new ValueResolver(){
            public Object getValue(){
                Transferable newClipValue = txtOper.getToolkit().getSystemClipboard().getContents(txtOper);
                log("newClipValue:"+newClipValue);
                return (newClipValue == oldClipValue) ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        
        return clipboardValueResolver;
    }

    protected void cutCopyViaStrokes(JEditorPaneOperator txtOper, int key, int mod){
        Transferable oldClipValue = txtOper.getToolkit().getSystemClipboard().getContents(txtOper);
        log("");
        log("oldClipValue:"+oldClipValue);
        txtOper.pushKey(key, mod);
        // give max WAIT_MAX_MILIS_FOR_CLIPBOARD milis for clipboard to change
        boolean success = waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_CLIPBOARD, getClipboardResolver(txtOper, oldClipValue), Boolean.FALSE);
        if (success == false){
            // give it one more chance. maybe selection was not ready at the time of
            // copying
            log("!!!! ONCE AGAIN");
            txtOper.pushKey(key, mod);
            // give max WAIT_MAX_MILIS_FOR_CLIPBOARD milis for clipboard to change
            waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_CLIPBOARD, getClipboardResolver(txtOper, oldClipValue), Boolean.FALSE);
        }
    }
    
}

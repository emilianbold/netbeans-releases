/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.cmdline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.regexp.*;

import org.openide.TopManager;
//import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.netbeans.api.diff.DiffProvider;
import org.netbeans.api.diff.Difference;

/**
 * The parser of an external diff utility compatible with Unix diff output.
 *
 * @author  Martin Entlicher
 */
public class CmdlineDiffProvider extends DiffProvider {

    private static final String REVISION_STR = "retrieving revision".intern();
    private static final String DIFF_REGEXP = "(^[0-9]+(,[0-9]+|)[d][0-9]+$)|"+
                                              "(^[0-9]+(,[0-9]+|)[c][0-9]+(,[0-9]+|)$)|"+
                                              "(^[0-9]+[a][0-9]+(,[0-9]+|)$)".intern();
    private static final int BUFF_LENGTH = 1024;

    private transient RE pattern;
    private String diffCmd;
    
    static final long serialVersionUID =4101521743158176210L;
    /** Creates new CmdlineDiffProvider
     * @param diffCmd The diff command. Must contain "{0}" and "{1}", which
     * will be replaced with the files being compared.
     */
    public CmdlineDiffProvider(String diffCmd) {
        this.diffCmd = diffCmd;
        try {
            pattern = new RE(DIFF_REGEXP);
        } catch (RESyntaxException resex) {}
    }

    /**
     * Set a new diff command.
     * @param diffCmd The diff command. Must contain "{0}" and "{1}", which
     * will be replaced with the files being compared.
     */
    public void setDiffCommand(String diffCmd) {
        this.diffCmd = diffCmd;
    }
    
    /**
     * Get the diff command being used.
     */
    public String getDiffCommand() {
        return diffCmd;
    }
    
    private boolean checkEmpty(String str, String element) {
        if (str == null || str.length() == 0) {
            /*
            if (this.stderrListener != null) {
                String[] elements = { "Bad format of diff result: "+element }; // NOI18N
                stderrListener.match(elements);
            }
            */
            //Edeb("Bad format of diff result: "+element); // NOI18N
            return true;
        }
        return false;
    }

    /**
     * Get the display name of this diff provider.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(CmdlineDiffProvider.class, "displayName");
    }
    
    /**
     * Get a short description of this diff provider.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(CmdlineDiffProvider.class, "shortDescription");
    }

    /**
     * Create the differences of the content two streams.
     * @param r1 the first source
     * @param r2 the second source to be compared with the first one.
     * @return the list of differences found, instances of {@link Difference};
     *        or <code>null</code> when some error occured.
     */
    public List createDiff(Reader r1, Reader r2) {
        File f1 = null;
        File f2 = null;
        try {
            f1 = File.createTempFile("TempDiff".intern(), null);
            f2 = File.createTempFile("TempDiff".intern(), null);
            FileWriter fw1 = new FileWriter(f1);
            FileWriter fw2 = new FileWriter(f2);
            char[] buffer = new char[BUFF_LENGTH];
            int length;
            while((length = r1.read(buffer)) > 0) fw1.write(buffer, 0, length);
            while((length = r2.read(buffer)) > 0) fw2.write(buffer, 0, length);
            r1.close();
            r2.close();
            fw1.close();
            fw2.close();
            return createDiff(f1, f2);
        } catch (IOException ioex) {
            return null;
        } finally {
            if (f1 != null) f1.delete();
            if (f2 != null) f2.delete();
        }
    }
    
    /**
     * Create the differences of the content of two FileObjects.
     * @param fo1 the first FileObject
     * @param fo2 the second FileObject to be compared with the first one.
     * @return the list of differences found, instances of {@link Difference};
     *        or <code>null</code> when some error occured.
     */
    public List createDiff(FileObject fo1, FileObject fo2) throws IOException {
        File f1 = FileUtil.toFile(fo1);
        File f2 = FileUtil.toFile(fo2);
        if (f1 != null && f2 != null) {
            return createDiff(f1, f2);
        } else {
            return null;
        }
    }
    
    private List createDiff(File f1, File f2) throws IOException {
        if (pattern == null) {
            try {
                pattern = new RE(DIFF_REGEXP);
            } catch (RESyntaxException resex) {
                throw (IOException) TopManager.getDefault().getErrorManager().annotate(
                    new IOException(), resex.getLocalizedMessage());
            }
        }
        String cmd = java.text.MessageFormat.format(diffCmd,
            new Object[] { f1.getAbsolutePath(), f2.getAbsolutePath() });
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            Reader stdout = new InputStreamReader(p.getInputStream());
            char[] buffer = new char[BUFF_LENGTH];
            StringBuffer outBuffer = new StringBuffer();
            int length;
            List differences = new ArrayList();
            while ((length = stdout.read(buffer)) > 0) {
                for (int i = 0; i < length; i++) {
                    if (buffer[i] == '\n') {
                        stdoutNextLine(outBuffer.toString(), differences);
                        outBuffer.delete(0, outBuffer.length());
                    } else {
                        if (buffer[i] != 13) {
                            outBuffer.append(buffer[i]);
                        }
                    }
                }
            }
            if (outBuffer.length() > 0) stdoutNextLine(outBuffer.toString(), differences);
            return differences;
        } catch (IOException ioex) {
            throw (IOException) TopManager.getDefault().getErrorManager().annotate(ioex,
                    NbBundle.getMessage(CmdlineDiffProvider.class, "runtimeError", cmd));
        }
    }
    
    private static String[] matchToStringArray(RE pattern, String line) {
        ArrayList v = new ArrayList(5);
        if (!pattern.match(line)) {
            return new String[0];
        }
        for(int i = 1; i < pattern.getParenCount(); i++){
            int subStart = pattern.getParenStart(i);
            int subEnd = pattern.getParenEnd(i);
            if (subStart >= 0 && subEnd > subStart)
                v.add(line.substring(subStart, subEnd));
        }
        int count = v.size();
        if (count <= 0) count = 1;
        String[] sa = new String[count];
        v.toArray(sa);
        return sa;
    }

    private void stdoutNextLine(String line, List differences) {
        String[] sa = matchToStringArray(pattern, line);
        if (sa != null && sa.length > 0) outputData(sa, differences);
    }
    
    /**
     * This method is called, with elements of the output data.
     * @param elements the elements of output data.
     */
    private void outputData(String[] elements, List differences) {
        //diffBuffer.append(elements[0]+"\n"); // NOI18N
        //D.deb("diff match: "+elements[0]); // NOI18N
        //System.out.println("diff outputData: "+elements[0]); // NOI18N

        int index = 0, commaIndex = 0;
        int n1 = 0, n2 = 0, n3 = 0, n4 = 0;
        String nStr;
        if ((index = elements[0].indexOf('a')) >= 0) {
            //DiffAction action = new DiffAction();
            try {
                n1 = Integer.parseInt(elements[0].substring(0, index));
                index++;
                commaIndex = elements[0].indexOf(',', index);
                if (commaIndex < 0) {
                    nStr = elements[0].substring(index, elements[0].length());
                    if (checkEmpty(nStr, elements[0])) return;
                    n3 = Integer.parseInt(nStr);
                    n4 = n3;
                } else {
                    nStr = elements[0].substring(index, commaIndex);
                    if (checkEmpty(nStr, elements[0])) return;
                    n3 = Integer.parseInt(nStr);
                    nStr = elements[0].substring(commaIndex+1, elements[0].length());
                    if (nStr == null || nStr.length() == 0) n4 = n3;
                    else n4 = Integer.parseInt(nStr);
                }
            } catch (NumberFormatException e) {
                /*
                if (this.stderrListener != null) {
                    String[] debugOut = { "NumberFormatException "+e.getMessage() }; // NOI18N
                    stderrListener.match(debugOut);
                }
                */
                //Edeb("NumberFormatException "+e.getMessage()); // NOI18N
                return;
            }
            //action.setAddAction(n1, n3, n4);
            //diffActions.add(action);
            differences.add(new Difference(Difference.ADD, n1, 0, n3, n4));
        } else if ((index = elements[0].indexOf('d')) >= 0) {
            //DiffAction action = new DiffAction();
            commaIndex = elements[0].lastIndexOf(',', index);
            try {
                if (commaIndex < 0) {
                    n1 = Integer.parseInt(elements[0].substring(0, index));
                    n2 = n1;
                } else {
                    nStr = elements[0].substring(0, commaIndex);
                    if (checkEmpty(nStr, elements[0])) return;
                    n1 = Integer.parseInt(nStr);
                    nStr = elements[0].substring(commaIndex+1, index);
                    if (checkEmpty(nStr, elements[0])) return;
                    n2 = Integer.parseInt(nStr);
                }
                nStr = elements[0].substring(index+1, elements[0].length());
                if (checkEmpty(nStr, elements[0])) return;
                n3 = Integer.parseInt(nStr);
            } catch (NumberFormatException e) {
                /*
                if (this.stderrListener != null) {
                    String[] debugOut = { "NumberFormatException "+e.getMessage() }; // NOI18N
                    stderrListener.match(debugOut);
                }
                */
                //Edeb("NumberFormatException "+e.getMessage()); // NOI18N
                return;
            }
            //action.setDeleteAction(n1, n2, n3);
            //diffActions.add(action);
            differences.add(new Difference(Difference.DELETE, n1, n2, n3, 0));
        } else if ((index = elements[0].indexOf('c')) >= 0) {
            //DiffAction action = new DiffAction();
            commaIndex = elements[0].lastIndexOf(',', index);
            try {
                if (commaIndex < 0) {
                    n1 = Integer.parseInt(elements[0].substring(0, index));
                    n2 = n1;
                } else {
                    nStr = elements[0].substring(0, commaIndex);
                    if (checkEmpty(nStr, elements[0])) return;
                    n1 = Integer.parseInt(nStr);
                    nStr = elements[0].substring(commaIndex+1, index);
                    if (checkEmpty(nStr, elements[0])) return;
                    n2 = Integer.parseInt(nStr);
                }
                index++;
                commaIndex = elements[0].indexOf(',', index);
                if (commaIndex < 0) {
                    nStr = elements[0].substring(index, elements[0].length());
                    if (checkEmpty(nStr, elements[0])) return;
                    n3 = Integer.parseInt(nStr);
                    n4 = n3;
                } else {
                    nStr = elements[0].substring(index, commaIndex);
                    if (checkEmpty(nStr, elements[0])) return;
                    n3 = Integer.parseInt(nStr);
                    nStr = elements[0].substring(commaIndex+1, elements[0].length());
                    if (nStr == null || nStr.length() == 0) n4 = n3;
                    else n4 = Integer.parseInt(nStr);
                }
            } catch (NumberFormatException e) {
                /*
                if (this.stderrListener != null) {
                    String[] debugOut = { "NumberFormatException "+e.getMessage() }; // NOI18N
                    stderrListener.match(debugOut);
                }
                */
                //Edeb("NumberFormatException "+e.getMessage()); // NOI18N
                return;
            }
            //action.setChangeAction(n1, n2, n3, n4);
            //diffActions.add(action);
            differences.add(new Difference(Difference.CHANGE, n1, n2, n3, n4));
        } else if (elements[0].indexOf(REVISION_STR) == 0) {
            String rev = elements[0].substring(REVISION_STR.length()).trim();
            //if (diffOutRev1 == null) diffOutRev1 = rev;
            //else diffOutRev2 = rev;
        }
    }
    
}

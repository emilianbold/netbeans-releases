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

import org.openide.TopManager;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.vcscore.diff.AbstractDiff;
import org.netbeans.modules.vcscore.cmdline.exec.*;
import org.netbeans.modules.vcscore.commands.VcsCommandExecutor;
import org.netbeans.modules.vcscore.commands.CommandDataOutputListener;

import org.netbeans.modules.diff. DiffProvider;

/**
 *
 * @author  Martin Entlicher
 */
public class CmdlineDiffProvider extends DiffProvider implements CommandDataOutputListener {

    private static final String REVISION_STR = "retrieving revision";

    private String diffCmd;
    private AbstractDiff diff = null;
    
    /** Creates new CmdlineDiffProvider */
    public CmdlineDiffProvider(String diffCmd) {
        this.diffCmd = diffCmd;
    }
    
    /**
     * Perform the diff of these two FileObjects and fill the differences to the diff object.
     * @param diff the diff object to fill the differences in
     * @param fo1 the first FileObject
     * @param fo2 the second FileObject to be compared with the first one.
     * @return true when no differences were found, false when either some differences were found
     *        or some error has occured.
     */
    public boolean performDiff(FileObject fo1, FileObject fo2, AbstractDiff diff) {
        File file1 = NbClassPath.toFile(fo1);
        File file2 = NbClassPath.toFile(fo2);
        this.diff = diff;
        boolean d;
        d = performDiff(diffCmd + " \"" + file1.getAbsolutePath() + "\""+
                                  " \"" + file2.getAbsolutePath() + "\"");
        return d;
    }
    
    private boolean performDiff(String args) {
        ExternalCommand ec = new ExternalCommand(args);
        try {
            ec.addStdoutRegexListener(this, "(^[0-9]+(,[0-9]+|)[d][0-9]+$)|(^[0-9]+(,[0-9]+|)[c][0-9]+(,[0-9]+|)$)|(^[0-9]+[a][0-9]+(,[0-9]+|)$)");
        } catch (BadRegexException exc) {
            TopManager.getDefault().notifyException(exc);
        }
        return (ec.exec() == VcsCommandExecutor.SUCCEEDED);
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
     * This method is called, with elements of the output data.
     * @param elements the elements of output data.
     */
    public void outputData(String[] elements) {
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
            diff.addAddAction(n1, n3, n4);
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
            diff.addDeleteAction(n1, n2, n3);
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
            diff.addChangeAction(n1, n2, n3, n4);
        } else if (elements[0].indexOf(REVISION_STR) == 0) {
            String rev = elements[0].substring(REVISION_STR.length()).trim();
            //if (diffOutRev1 == null) diffOutRev1 = rev;
            //else diffOutRev2 = rev;
        }
    }

}

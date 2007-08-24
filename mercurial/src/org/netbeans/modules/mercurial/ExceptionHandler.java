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
package org.netbeans.modules.mercurial;

import javax.swing.JButton;
import java.util.logging.Level;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class ExceptionHandler {    
     
    public final static int EX_UNKNOWN = 0;
    public final static int EX_ACTION_CANCELED_BY_USER = 2;
    public final static int EX_NO_REPOSITORY = 4;
    public final static int EX_ALREADY_TRACKED = 8;
    public final static int EX_NOT_TRACKED = 16;
    public final static int EX_CANNOT_READ_COMMIT_MSG = 32;
    public final static int EX_UNABLE_EXECUTE_CMD = 64;
                 
    static final String ACTION_CANCELED_BY_USER = org.openide.util.NbBundle.getMessage(ExceptionHandler.class, "MSG_ActionCanceledByUser"); // NOI18N
    private final HgException exception;
    private final int exceptionMask;



    public ExceptionHandler(HgException exception) {
        this.exception = exception;        
        exceptionMask = getMask(exception.getMessage());
    }

    protected int getExceptionMask() {
        return exceptionMask;
    }
    
    protected HgException getException() {
        return exception;
    }

    private static int getMask(String msg) {
        if(msg == null || msg.trim().equals("")) { // NOI18N
            return EX_UNKNOWN;
        }
        msg = msg.toLowerCase();        
        if(isCancelledAction(msg)) {
            return EX_ACTION_CANCELED_BY_USER;
        } else if(isNoRepository(msg)) {
            return EX_NO_REPOSITORY;               
        } else if(isNotTracked(msg)) {
            return EX_NOT_TRACKED;               
        } else if(isCannotReadCommitMsg(msg)) {
            return EX_CANNOT_READ_COMMIT_MSG;               
        } else if(isUnableExecuteCmd(msg)) {
            return EX_UNABLE_EXECUTE_CMD;               
        } else if(isAlreadyTracked(msg)) {
            return EX_ALREADY_TRACKED;
        }                
        return EX_UNKNOWN;
    }
    
    private static boolean isCancelledAction(String msg) {
        return msg.equals(ACTION_CANCELED_BY_USER);
    }
    
    public static boolean isNoRepository(String msg) {
        msg = msg.toLowerCase();
        return msg.indexOf("abort: There is no Mercurial repository here") > -1;                                   // NOI18N
    }
        
    public static boolean isUpdateSpansBranches(String msg) {
        msg = msg.toLowerCase();
        return msg.indexOf("abort: update spans branches") > -1;                                   // NOI18N
    }

    public static boolean isAlreadyTracked(String msg) {
        return msg.indexOf(" already tracked!") > -1;                          // NOI18N
    }

    public static boolean isNotTracked(String msg) {
        return msg.indexOf(" not tracked!") > -1;                          // NOI18N
    }
    
    public static boolean isCannotReadCommitMsg(String msg) {
        return msg.indexOf("abort: can't read commit message") > -1;                          // NOI18N
    }
    
    public static boolean isUnableExecuteCmd(String msg) {
        return msg.indexOf("unable to execute hg command") > -1;                          // NOI18N
    }
    
    public static boolean isUnableClone(String msg) {
        return msg.indexOf("abort: destination ") > -1;                          // NOI18N
    }
    
    public static boolean isNotFound(String msg) {
        return msg.indexOf("not found!") > -1;    // NOI18N
    }

    public static boolean isNoChangeNeeded(String msg) {
        return msg.indexOf("no change needed") > -1;    // NOI18N
    }

    // TODO: decide how to report exceptions to the user - information dialog?? 
    /** Analyzes exception and notifies user. */
    public void notifyException() {
        if(isCancelledAction(exception.getMessage())) {
            cancelledAction();
            return;
        }
        Mercurial.LOG.log(Level.INFO, null, exception);
    }
    
    public void notifyException(boolean notCanceled) {
        if(!notCanceled) {
            cancelledAction();
            return;
        }
        Mercurial.LOG.log(Level.INFO, null, exception);
    }

    public static String parseExceptionMessage(HgException exception) {
        String msg = exception.getMessage();
        int idx = msg.lastIndexOf("hg: "); // NOI18N
        if(idx > -1) {
            msg = msg.substring(idx);
        }
        return msg;
    }

    private void cancelledAction() {
        JButton ok = new JButton(NbBundle.getMessage(ExceptionHandler.class, "CTL_Action_OK")); // NOI18N
        NotifyDescriptor descriptor = new NotifyDescriptor(
                ACTION_CANCELED_BY_USER,
                NbBundle.getMessage(ExceptionHandler.class, "CTL_ActionCanceled_Title"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object [] { ok },
                ok);
        DialogDisplayer.getDefault().notify(descriptor);        
        return;
    }


    
}

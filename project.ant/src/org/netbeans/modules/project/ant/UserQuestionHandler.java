/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ant;

import java.io.IOException;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/**
 * Deals with {@link UserQuestionException}s.
 * @see "#46089"
 * @author Jesse Glick
 */
public final class UserQuestionHandler {
    
    private UserQuestionHandler() {}
    
    /**
     * Handle a user question exception later (in the event thread).
     * Displays a dialog and invokes the appropriate method on the callback.
     * The callback will be notified in the event thread.
     * Use when catching {@link UserQuestionException} during {@link FileObject#lock}.
     */
    public static void handle(final UserQuestionException e, final Callback callback) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(
                    e.getLocalizedMessage(),
                    NbBundle.getMessage(UserQuestionHandler.class, "TITLE_CannotWriteFile"),
                    NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION)) {
                    try {
                        e.confirmed();
                        callback.accepted();
                    } catch (IOException x) {
                        callback.error(x);
                    }
                } else {
                    callback.denied();
                }
            }
        });
    }
    
    /**
     * Intended behavior.
     */
    public interface Callback {
        
        /**
         * Called later if the user accepted the question.
         */
        void accepted();
        
        /**
         * Called later if the user denied the question.
         */
        void denied();
        
        /**
         * Called later if the user accepted the question but there was in fact a problem.
         */
        void error(IOException e);
        
    }
    
}

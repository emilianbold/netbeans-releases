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

package org.netbeans.api.editor.completion;

import org.netbeans.modules.editor.completion.CompletionImpl;

/**
 * Code completion allows the clients to request explicit showing
 * or hiding of the code completion.
 * <br>
 * It's a singleton instance.
 *
 * @author Miloslav Metelka
 * @version 1.01
 */

public final class Completion {

    private static final Completion singleton = new Completion();

    /**
     * Get the singleton instance of this class.
     */
    public static Completion get() {
        return singleton;
    }
    
    private Completion() {
    }

    /**
     * Request showing of the code completion popup
     * for the currently focused text component.
     * <br>
     * The completion will be shown if there are any results to be shown
     * for the particular context.
     *
     * <p>
     * This method can be called from any thread but when
     * called outside of AWT the request will be rescheduled into AWT.
     */
    public void showCompletion() {
        CompletionImpl.get().showCompletion();
    }
    
    /**
     * Hide a completion popup window if it's opened.
     *
     * <p>
     * This method can be called from any thread.
     * The cancelling of the possibly running tasks is done synchronously
     * and the GUI will be updated in the AWT thread.
     */
    public void hideCompletion() {
        CompletionImpl.get().hideCompletion();
    }

    /**
     * Request showing of the documentation popup
     * for the currently focused text component.
     * <br>
     * The documentation popup will be shown if there are any results to be shown
     * for the particular context.
     *
     * <p>
     * This method can be called from any thread but when
     * called outside of AWT the request will be rescheduled into AWT.
     */
    public void showDocumentation() {
        CompletionImpl.get().showDocumentation();
    }
    
    /**
     * Hides a documentation popup window if it's opened.
     *
     * <p>
     * This method can be called from any thread.
     * The cancelling of the possibly running tasks is done synchronously
     * and the GUI will be updated in the AWT thread.
     */
    public void hideDocumentation() {
        CompletionImpl.get().hideDocumentation();
    }

    /**
     * Request showing of the tooltip popup
     * for the currently focused text component.
     * <br>
     * The tooltip popup will be shown if there are any results to be shown
     * for the particular context.
     *
     * <p>
     * This method can be called from any thread but when
     * called outside of AWT the request will be rescheduled into AWT.
     */
    public void showToolTip() {
        CompletionImpl.get().showToolTip();
    }
    
    /**
     * Hides a tooltip popup window if it's opened.
     *
     * <p>
     * This method can be called from any thread.
     * The cancelling of the possibly running tasks is done synchronously
     * and the GUI will be updated in the AWT thread.
     */
    public void hideToolTip() {
        CompletionImpl.get().hideToolTip();
    }

    /**
     * Hide either of the possibly opened code completion,
     * documentation or tooltip windows.
     */
    public void hideAll() {
        CompletionImpl.get().hideAll();
    }

}

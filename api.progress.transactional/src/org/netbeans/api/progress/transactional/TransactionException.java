/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.api.progress.transactional;

/**
 * Exception which can be thrown from either run() or rollback() in a 
 * transaction.  If a checked exception is thrown in a transaction (for
 * example, while doing I/O), simply call Transaction.rethrow() to throw
 * it as a TransactionException.  The user will be notified per the
 * passed notification style, and no exception dialog will be shown
 * to the user.
 * <p/>
 * All string messages passed to constructors of the method should be localized.
 *
 * @author Tim Boudreau
 */
public final class TransactionException extends /*Runtime*/Exception {
    private final NotificationStyle style;
    private boolean isCancel;
    public TransactionException(String msg, Throwable cause, NotificationStyle style) {
        super (msg, cause);
        this.style = style;
    }

    public TransactionException(String msg) {
        super (msg);
        this.style = NotificationStyle.STATUS;
    }

    public TransactionException(Throwable cause) {
        super (cause);
        this.style = NotificationStyle.STATUS;
    }
    
    public TransactionException (String msg, NotificationStyle style) {
        super (msg);
        this.style = style;
    }
    
    public TransactionException(Throwable cause, NotificationStyle style) {
        super (cause);
        this.style = NotificationStyle.STATUS;
    }

    public NotificationStyle getStyle() {
        return style;
    }
    
    void setCancel(boolean val) {
        isCancel = val;
    }

    /**
     * Determine if this exception is the result of user- or programmatically-invoked cancellation
     * @return true if this exception was caused by cancellation of the transaction
     */
    public boolean isCancellation() {
        return isCancel;
    }

    /**
     * Hint to the UI for how the user should be notified about a failure
     */
    public enum NotificationStyle {
        /** Write to the log file but do not notify the user */
        NONE,
        /** Set the status bar text or equivalent - non-intrusive notification */
        STATUS,
        /** Show a popup dialog with the error message.  Use this options
         * sparingly, as popup dialogs interrupt user workflow.
         */
        POPUP
    }
}

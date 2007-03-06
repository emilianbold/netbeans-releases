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
package org.netbeans.jellytools.nodes;

import java.awt.Toolkit;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JDialogOperator;


/** Utilities to test nodes
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class Utils {

    /** Test cut */
    public static void testClipboard(final Object clipboard1) {
        final Object clipboard2 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    Object clipboard2 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                    return clipboard1 != clipboard2 ? Boolean.TRUE : null;
                }
                public String getDescription() {
                    return("Wait clipboard contains data"); // NOI18N
                }
        });
        try {
            waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Waiting interrupted.", e);
        }
    }
    
    /** Close "Confirm Object Deletion" dialog. **/
    public static void closeConfirmDialog() {
        // "Confirm Object Deletion"
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle",
                                               "MSG_ConfirmDeleteObjectTitle"); // NOI18N
        new JDialogOperator(confirmTitle).close();
    }
    /** Close "Rename" dialog. **/
    public static void closeRenameDialog() {
        String renameTitle = Bundle.getString("org.openide.actions.Bundle", "CTL_RenameTitle"); 
        new JDialogOperator(renameTitle).close();
    }
 
    /** Close properties */
    public static void closeProperties(String objectName) {
        new PropertySheetOperator(objectName).close();
    }
}

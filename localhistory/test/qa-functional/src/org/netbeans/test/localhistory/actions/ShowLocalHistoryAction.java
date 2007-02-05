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
/*
 * ShowLocalHistoryAction.java
 *
 * Created on February 2, 2007, 1:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.localhistory.actions;

import org.netbeans.jellytools.actions.Action;

/**
 *
 * @author peter
 */
public class ShowLocalHistoryAction extends Action {
    
    public static final String LH_ITEM = "Local History";
    public static final String SHOW_LOCAL_HISTORY_ITEM = "Show Local History";
    
    /** Creates a new instance of ShowLocalHistoryAction */
    public ShowLocalHistoryAction() {
        super(LH_ITEM + "|" + LH_ITEM + "|" + SHOW_LOCAL_HISTORY_ITEM, LH_ITEM + "|" + SHOW_LOCAL_HISTORY_ITEM);
    }
    
}

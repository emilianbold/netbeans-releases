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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CancelGraph.java
 *
 * Created on April 26, 2006, 3:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring.ui;

import org.openide.util.Cancellable;

/**
 *
 * @author Jeri Lockhart
 */


public class CancelGraph implements Cancellable,CancelSignal {

    private boolean isCancelRequested;

    public boolean cancel() {
        isCancelRequested = true;
        return true;
    }

    /**
     * Implement CancelSignal
     */
    public boolean isCancelRequested() {
        return isCancelRequested;
    }
}

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package gui;

/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org
 */
public class EPUtilities extends gui.Utilities{

    public static void waitForPendingBackgroundTasks() {
        waitForPendingBackgroundTasks(5);
    }
    
    public static void waitForPendingBackgroundTasks(int n) {
        // wait maximum n minutes
        for (int i=0; i<n*60; i++) {
            if (org.netbeans.progress.module.Controller.getDefault().getModel().getSize()==0)
                return;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
    
}

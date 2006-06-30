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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

/**
 * Task that cleans a <code>ResultModel</code>.
 *
 * @see  ResultModel
 * @author  Marian Petras
 */
class CleanTask implements Runnable {

    /** result model to clean */
    ResultModel resultModel;

    /**
     * Creates a new instance of CleanTask.
     *
     * @param  resultModel  result model to be cleaned
     */
    CleanTask(ResultModel resultModel) {
        this.resultModel = resultModel;
    }

    /**
     */
    public void run() {
        resultModel.close();
    }

}

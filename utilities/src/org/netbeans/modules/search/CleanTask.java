/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
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

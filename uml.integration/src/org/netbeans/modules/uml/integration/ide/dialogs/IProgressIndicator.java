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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * File         : IProgressIndicator.java
 * Version      : 1.0
 * Description  : Interface for a progress indicator.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide.dialogs;

/**
 *  Interface for a progress indicator (from Trey Spiva's ProgressIndicator
 * code).
 *
 * @author Darshan
 */
public interface IProgressIndicator {
    /**
     *  Shows the progress indicator. This method should exit silently if the
     * progress indicator is already displayed.
     */
    public void show();

    /**
     * Sets the maximum range of the progress bar.  The maximum value is used to
     * detimine the position of the progress bar. Increasing this range when
     * visible typically can cause the progress indicator to move backwards
     * (usually undesirable).
     *
     * @param value The maximum value.
     */
    public void setMaxRange(int value);

    /**
     * Returns the maximum range of the progress bar.
     * @return The maximum value.
     */
    public int getMaxRange();

    /**
     * Sets the minimum range of the progress bar.  The minimum value is used to
     * detimine the position of the progress. 
     * @param value The minimum value.
     */
    public void setMinRange(int value);

    /**
     * Sets the current progress information.
     * @param msg A message to be displayed to the user.
     * @param rangeCompleted The value to indicate the progress.
     */
    public void setProgress(String msg, int rangeCompleted);

    /**
     *  Increments the progress.
     * @param msg A message to be displayed to the user.
     */
    public void incrementProgress(String msg);

    /**
     *  Closes the progress indicator. This should fail silently if the progress
     * indicator has not been displayed yet, or has already been closed.
     */
    public void done();
}

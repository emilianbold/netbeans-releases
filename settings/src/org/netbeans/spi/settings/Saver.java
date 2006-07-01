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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.settings;

/** The Saver should be used as a callback to the framework implementation
 * to handle setting object changes.
 *
 * @author  Jan Pokorsky
 */
public interface Saver {
    /** Notify the framework to be aware of the setting object is changed.
     */
    public void markDirty();

    /** Notify the framework the setting object is changed and can be written down
     * @exception IOException if the save cannot be performed
     */
    public void requestSave() throws java.io.IOException;
}

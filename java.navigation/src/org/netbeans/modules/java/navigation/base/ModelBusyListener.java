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

package org.netbeans.modules.java.navigation.base;

/** Interface for navigator models to notify clients about their not-ready
 * state, during long computations.
 *
 * Note, this is temporary and will be deleted and replaced by simpler JComponent
 * navigator based API.
 *
 * @author Dafe Simonek
 */
public interface ModelBusyListener {

    /** Computation started.
     * Threading: Can be called on any thread
     */
    public void busyStart ();

    /** Computation finished.
     * Threading: Can be called on any thread
     */
    public void busyEnd ();

    /** Called when new content was loaded and is ready. It means that 
     * list data change events was already fired and so the Swing component
     * which contains the model already knows about new data.
     * Currently used only to keep selection in swing components after 
     * load of new data.
     *
     * Threading: Always called from EQT 
     */ 
    public void newContentReady ();
    
}

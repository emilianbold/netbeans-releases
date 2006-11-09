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
package org.netbeans.spi.editor.hints;

import java.util.Collection;
import java.util.ArrayList;
import javax.swing.text.Document;
import org.netbeans.modules.editor.hints.HintsControllerImpl;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Lahoda
 */
public final class HintsController {

    /** Creates a new instance of HintsController */
    private HintsController() {
    }

    /**Assign given list of errors to a file. This removes any errors that were assigned to this
     * file before under the same "layer". The file to which the errors should be assigned
     * is gathered from the given document.
     * 
     * @param doc document to which the errors should be assigned
     * @param layer unique layer ID
     * @param errors to use
     */
    public static void setErrors(final Document doc, final String layer, Collection<? extends ErrorDescription> errors) {
        final Collection<? extends ErrorDescription> errorsCopy = new ArrayList<ErrorDescription>(errors);

        WORKER.post(new Runnable() {
            public void run() {
                HintsControllerImpl.setErrors(doc, layer, errorsCopy);
            }
        });
    }
    
    /**Assign given list of errors to a given file. This removes any errors that were assigned to this
     * file before under the same "layer".
     *
     * @param file to which the errors should be assigned
     * @param layer unique layer ID
     * @param errors to use
     */
    public static void setErrors(final FileObject file, final String layer, Collection<? extends ErrorDescription> errors) {
        final Collection<? extends ErrorDescription> errorsCopy = new ArrayList<ErrorDescription>(errors);

        WORKER.post(new Runnable() {
            public void run() {
                HintsControllerImpl.setErrors(file, layer, errorsCopy);
            }
        });
    }
    
    private static RequestProcessor WORKER = new RequestProcessor("HintsController worker");
    
}

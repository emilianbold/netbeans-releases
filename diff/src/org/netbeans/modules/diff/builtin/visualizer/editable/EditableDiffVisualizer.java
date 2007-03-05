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
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.spi.diff.DiffVisualizer;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.io.Reader;
import java.io.IOException;

/**
 * Registration of the editable visualizer. 
 * 
 * @author Maros Sandor
 */
public class EditableDiffVisualizer extends DiffVisualizer {

    /**
     * Get the display name of this diff visualizer, CALLED VIA REFLECTION.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(EditableDiffVisualizer.class, "CTL_EditableDiffVisualizer_Name"); // NOI18N
    }
    
    /**
     * Get a short description of this diff visualizer, CALLED VIA REFLECTION.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(EditableDiffVisualizer.class, "CTL_EditableDiffVisualizer_Desc"); // NOI18N
    }
    
    public Component createView(Difference[] diffs, String name1, String title1, Reader r1, String name2, String title2, Reader r2, String MIMEType) throws IOException {
        DiffView view = createDiff(diffs, StreamSource.createSource(name1, title1, MIMEType, r1), StreamSource.createSource(name2, title2, MIMEType, r2));
        return view.getComponent();
    }

    public DiffView createDiff(Difference[] diffs, StreamSource s1, StreamSource s2) throws IOException {
        return new EditableDiffView(s1, s2);
    }
} 

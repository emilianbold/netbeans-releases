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

package threaddemo.data;

import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.datatransfer.NewType;
import threaddemo.model.Phadhail;

/**
 * A new type for (container) phadhails.
 * @author Jesse Glick
 */
public final class PhadhailNewType extends NewType {

    private final Phadhail ph;
    private final boolean dir;

    public PhadhailNewType(Phadhail ph, boolean dir) {
        if (!ph.hasChildren()) throw new IllegalArgumentException();
        this.ph = ph;
        this.dir = dir;
    }
    
    public void create() throws IOException {
        String title = "Create New " + getName();
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine("New name:", title);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            String name = d.getInputText();
            if (dir) {
                ph.createContainerPhadhail(name);
            } else {
                ph.createLeafPhadhail(name);
            }
        }
    }
    
    public String getName() {
        if (dir) {
            return "Directory";
        } else {
            return "File";
        }
    }
    
}


/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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


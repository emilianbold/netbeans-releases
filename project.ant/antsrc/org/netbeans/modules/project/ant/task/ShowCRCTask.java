/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ant.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.spi.project.support.ant.CRC32Calculator;

/**
 * Ant task to show CRCs of files such as <samp>build.xml</samp> as
 * computed by {@link GeneratedFilesHelper}.
 * @author Jesse Glick
 */
public final class ShowCRCTask extends Task {
    
    private File f;
    public void setFile(File f) {
        this.f = f;
    }
    
    /** Standard constructor. */
    public ShowCRCTask() {}
    
    public void execute() throws BuildException {
        if (f == null) {
            throw new BuildException("No 'file' attr");
        }
        try {
            InputStream is = new FileInputStream(f);
            try {
                String crc = CRC32Calculator.computeCrc32(is);
                log("CRC32 for " + f + ": " + crc);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
    
}

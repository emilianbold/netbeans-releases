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

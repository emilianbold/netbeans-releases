/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.lib.cvsclient.command;

import java.io.*;

/**
 * Describes checkout/tag/update information for a file.
 * The fields in instances of this object are populated by response handlers.
 *
 * @author  Thomas Singer
 */
public class DefaultFileInfoContainer extends FileInfoContainer {

    public static final String PERTINENT_STATE = "Y"; //NOI18N
    public static final String MERGED_FILE = "G"; //NOI18N
    private File file;

    private String type;

    public DefaultFileInfoContainer() {
    }

    /**
     * Returns the associated file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns true if the associated file is a directory.
     */
    public boolean isDirectory() {
        File file = getFile();
        if (file == null) {
            return false;
        }
        return file.isDirectory();
    }

    /**
     * Sets the associated file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns the type.
     * Mostly the type value equals to the states returned by update and tag command.
     * see description in cvs manual.
     * Some states are added:
     *   G - file was merged (when using the cvs update -j <rev> <file> command.
     *   D - file was deleted - no longer pertinent.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Return a string representation of this object. Useful for debugging.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(type);
        buffer.append("  "); //NOI18N
        if (isDirectory()) {
            buffer.append("Directory "); //NOI18N
        }
        buffer.append(file != null ? file.getAbsolutePath()
                      : "null"); //NOI18N
        return buffer.toString();
    }
}

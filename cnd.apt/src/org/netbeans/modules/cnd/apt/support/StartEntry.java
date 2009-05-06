/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.apt.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

/**
 *
 * @author Alexander Simon
 */
public final class StartEntry implements Persistent, SelfPersistent{
    private final CharSequence startFile;
    //private boolean isCPP; // TODO: flag to be used for understanding C/C++ lang
    private final Key startFileProject;
    public StartEntry(String startFile, Key startFileProject) {
        this.startFile = FilePathCache.getManager().getString(startFile);
        this.startFileProject = startFileProject;
    }
    
    public CharSequence getStartFile(){
        return startFile;
    }

    public Key getStartFileProject(){
        return startFileProject;
    }
    
    public void write(DataOutput output) throws IOException {
        assert output != null;
        output.writeUTF(startFile.toString());
        KeyFactory.getDefaultFactory().writeKey(startFileProject, output);
    }
    
    public StartEntry(final DataInput input) throws IOException {
        assert input != null;
        startFile = FilePathCache.getManager().getString(input.readUTF());
        startFileProject = KeyFactory.getDefaultFactory().readKey(input);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StartEntry other = (StartEntry) obj;
        if (this.startFile != other.startFile && (this.startFile == null || !this.startFile.equals(other.startFile))) {
            return false;
        }
        if (this.startFileProject != other.startFileProject && (this.startFileProject == null || !this.startFileProject.equals(other.startFileProject))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.startFile != null ? this.startFile.hashCode() : 0);
        hash = 89 * hash + (this.startFileProject != null ? this.startFileProject.hashCode() : 0);
        return hash;
    }

    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Start Entry: from file=" + startFile + "\nof project="+startFileProject); //NOI18N
        return out.toString();
    }
}

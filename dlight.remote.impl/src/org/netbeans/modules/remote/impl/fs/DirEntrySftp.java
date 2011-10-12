/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin 
 */
public class DirEntrySftp implements DirEntry {

    private final StatInfo statInfo;
    private String cache;

    public DirEntrySftp(StatInfo statInfo, String cache) {
        this.statInfo = statInfo;
        this.cache = cache;
    }
    
    @Override
    public boolean canExecute(ExecutionEnvironment execEnv) {
        return statInfo.canExecute(execEnv);
    }

    @Override
    public boolean canRead(ExecutionEnvironment execEnv) {
        return statInfo.canRead(execEnv);
    }

    @Override
    public boolean canWrite(ExecutionEnvironment execEnv) {
        return statInfo.canWrite(execEnv);
    }

    @Override
    public String getAccessAsString() {
        return statInfo.getAccessAsString();
    }

    @Override
    public String getCache() {
        return cache;
    }

    @Override
    public FileType getFileType() {
        if (statInfo.isDirectory()) {
            return FileType.Directory;
        } else if (statInfo.isLink()) {
            return FileType.SymbolicLink;
        } else if (statInfo.isPlainFile()) {
            return FileType.Regular;
        } else {
            return statInfo.getFileType();
        }
    }

    @Override
    public String getLinkTarget() {
        return statInfo.getLinkTarget();
    }

    @Override
    public String getName() {
        return statInfo.getName();
    }

    @Override
    public long getSize() {
        return statInfo.getSize();
    }

    @Override
    public boolean isDirectory() {
        return statInfo.isDirectory();
    }

    @Override
    public boolean isLink() {
        return statInfo.isLink();
    }

    @Override
    public boolean isPlainFile() {
        return statInfo.isPlainFile();
    }

    @Override
    public boolean isSameType(DirEntry other) {
        return isLink() == other.isLink() && isDirectory() == other.isDirectory() && isPlainFile() == other.isPlainFile();
    }

    @Override
    public boolean isSameUser(DirEntry other) {
        if (other instanceof DirEntrySftp) {
            return statInfo.getUserId() == ((DirEntrySftp) other).statInfo.getUserId();
        }
        return false;
    }

    @Override
    public boolean isSameLastModified(DirEntry other) {
        if (other instanceof DirEntrySftp) {
            return statInfo.getLastModified().equals(((DirEntrySftp)other).statInfo.getLastModified());
        }
        return false;
    }

    @Override
    public Date getLastModified() {
        return statInfo.getLastModified();
    }

    @Override
    public boolean isSameGroup(DirEntry other) {
        if (other instanceof DirEntrySftp) {
            return statInfo.getGropupId() == ((DirEntrySftp) other).statInfo.getGropupId();
        }
        return false;
    }

    @Override
    public void setCache(String cache) {
        this.cache = cache;
    }

    @Override
    public String toExternalForm() {
        return escape(cache) + ' ' + statInfo.toExternalForm();
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    public static DirEntrySftp fromExternalForm(String externalForm) throws FormatException {
        try {
            int pos = externalForm.indexOf(' ');
            if (pos < 1) {
                throw new FormatException("Wrong directory entry format: " + externalForm, false); //NOI18N
            }
            String cache = unescape(externalForm.substring(0, pos));
            StatInfo statInfo = StatInfo.fromExternalForm(externalForm.substring(pos + 1));
            return new DirEntrySftp(statInfo, cache);
        } catch (Exception ex) {
            throw new FormatException("Wrong directory entry format: " + externalForm, ex); // NOI18N
        }        
    }
    
    private static String escape(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            text = text.replace(" ", "\\ "); // NOI18N
            return text;
        }
    }

    private static String unescape(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            text = text.replace("\\ ", " "); // NOI18N
            return text;
        }
    }

    @Override
    public String toString() {
        return statInfo.toString() + " (" + cache + ')' + (isValid() ? "[valid]" : "[invalid]"); // NOI18N
    }
}

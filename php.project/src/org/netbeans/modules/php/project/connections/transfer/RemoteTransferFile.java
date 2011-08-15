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
package org.netbeans.modules.php.project.connections.transfer;

import org.netbeans.modules.php.project.connections.spi.RemoteFile;

/**
 * {@link TransferFile Transfer file} implementation for {@link RemoteFile remote file}.
 */
final class RemoteTransferFile extends TransferFile {

    // @GuardedBy(file)
    private final RemoteFile file;


    public RemoteTransferFile(RemoteFile file, TransferFile parent, String baseDirectory) {
        super(parent, baseDirectory);
        this.file = file;

        if (file == null) {
            throw new NullPointerException("Remote file cannot be null");
        }
        if (!baseDirectory.startsWith(REMOTE_PATH_SEPARATOR)) {
            throw new IllegalArgumentException("Base directory '" + baseDirectory + "' must start with '" + REMOTE_PATH_SEPARATOR + "'");
        }
        String parentDirectory = file.getParentDirectory();
        if (!parentDirectory.startsWith(REMOTE_PATH_SEPARATOR)) {
            throw new IllegalArgumentException("Parent directory '" + parentDirectory + "' must start with '" + REMOTE_PATH_SEPARATOR + "'");
        }
        if (!parentDirectory.startsWith(baseDirectory)) {
            throw new IllegalArgumentException("Parent directory '" + parentDirectory + "' must be underneath base directory '" + baseDirectory + "'");
        }
    }

    @Override
    public String getName() {
        synchronized (file) {
            return file.getName();
        }
    }

    @Override
    public String getRemotePath() {
        String absolutePath;
        synchronized (file) {
            absolutePath = file.getParentDirectory() + REMOTE_PATH_SEPARATOR + getName();
        }
        if (absolutePath.equals(baseDirectory)) {
            return REMOTE_PROJECT_ROOT;
        }
        // +1 => remove '/' from the beginning of the relative path
        return absolutePath.substring(baseDirectory.length() + REMOTE_PATH_SEPARATOR.length());
    }

    @Override
    protected long getSizeImpl() {
        if (isFile()) {
            synchronized (file) {
                return file.getSize();
            }
        }
        return 0L;
    }

    @Override
    public boolean isDirectory() {
        synchronized (file) {
            return file.isDirectory();
        }
    }

    @Override
    public boolean isFile() {
        synchronized (file) {
            return file.isFile();
        }
    }

    @Override
    public boolean isLink() {
        synchronized (file) {
            return file.isLink();
        }
    }

    @Override
    protected long getTimestampImpl() {
        synchronized (file) {
            return file.getTimestamp();
        }
    }

}

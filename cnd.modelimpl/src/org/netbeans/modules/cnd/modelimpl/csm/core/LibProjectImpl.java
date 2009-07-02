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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.Key;

/**
 * @author Vladimir Kvasihn
 */
public final class LibProjectImpl extends ProjectBase {

    private final CharSequence includePath;

    private LibProjectImpl(ModelImpl model, String includePathName) {
        super(model, includePathName, includePathName);
        this.includePath = FilePathCache.getManager().getString(includePathName);
        this.projectRoots.fixFolder(includePathName);
        assert this.includePath != null;
    }

    public static LibProjectImpl createInstance(ModelImpl model, String includePathName) {
        ProjectBase instance = null;
        assert includePathName != null;
        if (TraceFlags.PERSISTENT_REPOSITORY) {
            try {
                instance = readInstance(model, includePathName, includePathName);
            } catch (Exception e) {
                // just report to console;
                // the code below will create project "from scratch"
                cleanRepository(includePathName, true);
                DiagnosticExceptoins.register(e);
            }
        }
        if (instance == null) {
            instance = new LibProjectImpl(model, includePathName);
        }
        if (instance instanceof LibProjectImpl) {
            assert ((LibProjectImpl) instance).includePath != null;
        }
        return (LibProjectImpl) instance;

    }

    protected CharSequence getPath() {
        return includePath;
    }

    @Override
    protected void ensureFilesCreated() {
    }

    protected boolean isStableStatus() {
        return true;
    }

    @Override
    protected Collection<Key> getLibrariesKeys() {
        return Collections.<Key>emptySet();
    }

    /** override parent to avoid inifinite recursion */
    @Override
    public List<CsmProject> getLibraries() {
        return Collections.<CsmProject>emptyList();
    }

    public void onFileRemoved(FileImpl file) {
    }

    public void onFileRemoved(List<NativeFileItem> file) {
    }

    public void onFileImplRemoved(List<FileImpl> files) {
    }

    public void onFileAdded(NativeFileItem file) {
    }

    public void onFileAdded(List<NativeFileItem> file) {
    }

    public void onFilePropertyChanged(NativeFileItem nativeFile) {
    }

    public void onFilePropertyChanged(List<NativeFileItem> nativeFiles) {
    }

    @Override
    protected final ParserQueue.Position getIncludedFileParserQueuePosition() {
        return ParserQueue.Position.TAIL;
    }

    public boolean isArtificial() {
        return true;
    }

    @Override
    public NativeFileItem getNativeFileItem(CsmUID<CsmFile> file) {
        return null;
    }

    @Override
    protected void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem) {
    }

    @Override
    protected void removeNativeFileItem(CsmUID<CsmFile> file) {
    }

    @Override
    protected void clearNativeFileContainer() {
    }

    @Override
    public boolean isStable(CsmFile skipFile) {
        if (!isDisposing()) {
            return !ParserQueue.instance().hasFiles(this, (FileImpl) skipFile);
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(DataOutput aStream) throws IOException {
        super.write(aStream);
        assert this.includePath != null;
        PersistentUtils.writeUTF(includePath, aStream);
    }

    public LibProjectImpl(DataInput aStream) throws IOException {
        super(aStream);
        this.includePath = PersistentUtils.readUTF(aStream, FilePathCache.getManager());
        assert this.includePath != null;
        setPlatformProject(this.includePath);
    }
}

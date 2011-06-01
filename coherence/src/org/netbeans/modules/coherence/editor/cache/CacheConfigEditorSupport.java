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
package org.netbeans.modules.coherence.editor.cache;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class CacheConfigEditorSupport extends DataEditorSupport implements OpenCookie, EditorCookie, EditCookie {

    public final static int DEFAULT_PANE = 3;
    private static final Logger logger = Logger.getLogger(CacheConfigEditorSupport.class.getCanonicalName());
    final MultiViewDescription[] descriptions = {new CacheConfigGeneralView(this), new CacheConfigSchemeView(this), new CacheConfigMappingView(this), new CacheConfigTextView(this)};

    private CacheConfigEditorSupport(CacheConfigDataObject obj) {
        super(obj, new CacheConfigEnv(obj));
    }

    public static CacheConfigEditorSupport create(CacheConfigDataObject obj) {
        return new CacheConfigEditorSupport(obj);
    }

    @Override
    public CloneableEditorSupport.Pane createPane() {
        Pane pane = (Pane) MultiViewFactory.createCloneableMultiView(descriptions, descriptions[DEFAULT_PANE]);
        pane.getComponent().setDisplayName(getDataObject().getPrimaryFile().getNameExt());
        return pane;
    }

    @Override
    protected boolean asynchronousOpen() {
        return true;
    }

    @Override
    protected boolean notifyModified() {
        boolean retvalue;
        CacheConfigDataObject obj = (CacheConfigDataObject) getDataObject();
        retvalue = super.notifyModified();
        if (retvalue) {
            obj.ic.add(env);
        }
        return retvalue;
    }

    @Override
    protected void notifyUnmodified() {
        CacheConfigDataObject obj = (CacheConfigDataObject) getDataObject();
        super.notifyUnmodified();
        obj.ic.remove(env);
    }

    public static final class CacheConfigEnv extends DataEditorSupport.Env implements SaveCookie {

        public CacheConfigEnv(CacheConfigDataObject obj) {
            super(obj);
        }

        @Override
        protected FileObject getFile() {
            return super.getDataObject().getPrimaryFile();
        }

        @Override
        protected FileLock takeLock() throws IOException {
            return ((CacheConfigDataObject) super.getDataObject()).getPrimaryEntry().takeLock();
        }

        public void save() throws IOException {
            CacheConfigEditorSupport ed = (CacheConfigEditorSupport) this.findCloneableOpenSupport();
            logger.log(Level.INFO,"save() ");
            ed.saveDocument();
        }
    }
}

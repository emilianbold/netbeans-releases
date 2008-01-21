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
package org.netbeans.modules.cnd.highlight.semantic;

import java.lang.ref.WeakReference;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory.PhaseRunner;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author Sergey Grinev
 */
public abstract class HighlighterBase implements PhaseRunner, LookupListener {

    private final String mime;
    private final OffsetsBag bag;
    private final WeakReference<BaseDocument> weakDoc;

    public HighlighterBase(Document doc) {
        bag = new OffsetsBag(doc);
        mime = (String) doc.getProperty("mimeType"); //NOI18N
        Lookup lookup = MimeLookup.getLookup(MimePath.get(mime));
        Lookup.Result<FontColorSettings> result =
                lookup.lookup(new Lookup.Template<FontColorSettings>(FontColorSettings.class));
        result.addLookupListener(WeakListeners.create(LookupListener.class, this, null));
        result.allInstances();

        if (doc instanceof BaseDocument) {
            weakDoc = new WeakReference<BaseDocument>((BaseDocument) doc);
        } else {
            weakDoc = null;
        }

        updateFontColors();
    }
    
    protected BaseDocument getDocument() {
        return weakDoc != null ? weakDoc.get() : null;
    }

    public OffsetsBag getHighlightsBag() {
        return bag;
    }

    // LookupListener
    public void resultChanged(LookupEvent ev) {
        updateFontColors();
        run(PhaseRunner.Phase.INIT);
    }
    
    public void updateFontColors() {
        Lookup lookup = MimeLookup.getLookup(MimePath.get(mime));
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        initFontColors(fcs);
    }

    protected abstract void initFontColors(FontColorSettings fcs);

    protected boolean isCancelled() {
        return Thread.interrupted();
    }
}

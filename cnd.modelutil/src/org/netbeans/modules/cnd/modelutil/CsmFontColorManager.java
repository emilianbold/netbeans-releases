/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelutil;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Sergey Grinev
 */
public class CsmFontColorManager {

    private final Map<String, FontColorProviderImpl> providers = new HashMap<String, FontColorProviderImpl>();

    public void addListener(String mimeType, FontColorChangeListener listener) {
        getCreateProvider(mimeType).addListener(listener);
    }
    
    private static final String DEFAULT_MIME_TYPE = "text/x-c++"; //NOI18N
    
    /* package */ Color getColor(FontColorProvider.Entity color) {
        // completion is not aware of document type
        AttributeSet as = getCreateProvider(DEFAULT_MIME_TYPE).getColor(color);
        return isUnitTestsMode ? Color.red : (Color)as.getAttribute(StyleConstants.ColorConstants.Foreground);
    }
    
    private FontColorProviderImpl getCreateProvider(String mimeType) {
        synchronized (providers) {
            FontColorProviderImpl fcp = providers.get(mimeType);
            if (fcp == null) {
                fcp = new FontColorProviderImpl(mimeType);
                providers.put(mimeType, fcp);
            }
            return fcp;
        }
    }

    public static CsmFontColorManager instance() {
        return Instantiator.instance;
    }

    private static class Instantiator {

        public final static CsmFontColorManager instance = new CsmFontColorManager();
    }

    private final boolean isUnitTestsMode;
    
    private CsmFontColorManager() {
        isUnitTestsMode = CsmUtilities.isUnitTestsMode();
    }

    public interface FontColorChangeListener extends EventListener {

        void stateChanged(FontColorProvider fcp);
    }

    private static class FontColorProviderImpl implements FontColorProvider, LookupListener {

        private final String mimeType;
        private final List<WeakReference<FontColorChangeListener>> listeners = new ArrayList<WeakReference<FontColorChangeListener>>();
        FontColorSettings fcs;

        public FontColorProviderImpl(String mimeType) {
            this.mimeType = mimeType;
            Lookup lookup = MimeLookup.getLookup(MimePath.get(mimeType));
            Lookup.Result<FontColorSettings> result =
                    lookup.lookup(new Lookup.Template<FontColorSettings>(FontColorSettings.class));
            fcs = result.allInstances().iterator().next();
            result.addLookupListener(this);
        }

        public void addListener(FontColorChangeListener listener) {
            listeners.add(new WeakReference<FontColorChangeListener>(listener));
            listener.stateChanged(this);
        }

        public AttributeSet getColor(Entity color) {
            return fcs.getTokenFontColors(color.getResourceName());
        }

        public void resultChanged(LookupEvent ev) {
            Lookup lookup = MimeLookup.getLookup(MimePath.get(mimeType));
            fcs = lookup.lookup(FontColorSettings.class);
            for (ListIterator<WeakReference<FontColorChangeListener>> it = listeners.listIterator(); it.hasNext();) {
                WeakReference<FontColorChangeListener> wrcl = it.next();
                FontColorChangeListener cl = wrcl.get();
                if (cl != null) {
                    cl.stateChanged(this);
                } else {
                    it.remove();
                }
            }
        }
    }
}

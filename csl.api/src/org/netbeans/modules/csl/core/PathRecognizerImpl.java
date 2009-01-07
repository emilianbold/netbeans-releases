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

package org.netbeans.modules.csl.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vita
 */
@ServiceProvider(service=PathRecognizer.class)
public final class PathRecognizerImpl extends PathRecognizer {

    // ------------------------------------------------------------------------
    // PathRecognizer implementation
    // ------------------------------------------------------------------------

    @Override
    public Set<String> getSourcePathIds() {
        if (sourcePathIds == null) {
            collectInfo();
        }
        Set<String> spids = sourcePathIds;
        assert spids != null;
        return spids;
    }

    @Override
    public Set<String> getBinaryPathIds() {
        if (binaryPathIds == null) {
            collectInfo();
        }
        Set<String> bpids = binaryPathIds;
        assert bpids != null;
        return bpids;
    }

    @Override
    public Set<String> getMimeType() {
        if (mimeTypes == null) {
            collectInfo();
        }
        Set<String> mts = mimeTypes;
        assert mts != null;
        return mts;
    }

    // ------------------------------------------------------------------------
    // Public implementation
    // ------------------------------------------------------------------------

    public static synchronized PathRecognizerImpl getInstance() {
        return Lookup.getDefault().lookup(PathRecognizerImpl.class);
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private volatile Set<String> sourcePathIds = null;
    private volatile Set<String> binaryPathIds = null;
    private volatile Set<String> mimeTypes = null;

    /**
     * Use {@link #getInstance()} to get the cached instance of this class. This
     * constructor is public only for @ServiceProvider registration.
     */
    public PathRecognizerImpl() {
        // no-op
    }

    private void collectInfo() {
        Set<String> collectedSpids = new HashSet<String>();
        Set<String> collectedBpids = new HashSet<String>();
        Set<String> collectedMimetypes = new HashSet<String>();

        for(Language l : LanguageRegistry.getInstance()) {
            Set<String> spids = l.getSourcePathIds();
            if (spids != null && !spids.isEmpty()) {
                collectedSpids.addAll(spids);
            }

            Set<String> bpids = l.getBinaryPathIds();
            if (bpids != null && !bpids.isEmpty()) {
                collectedBpids.addAll(bpids);
            }

            collectedMimetypes.add(l.getMimeType());
        }

        synchronized (this) {
            if (sourcePathIds == null) {
                assert binaryPathIds == null;
                assert mimeTypes == null;

                sourcePathIds = Collections.unmodifiableSet(collectedSpids);
                binaryPathIds = Collections.unmodifiableSet(collectedBpids);
                mimeTypes = Collections.unmodifiableSet(collectedMimetypes);
            }
        }
    }
}

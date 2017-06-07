/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle;

import java.beans.PropertyChangeListener;
import java.net.URL;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;

/**
 *
 * @author Martin
 */
@SourcePathProvider.Registration(path = "netbeans-JPDASession")
public class FirstSourceURLProvider extends SourcePathProvider {
    
    private static final String[] NO_SOURCE_ROOTS = new String[]{};
    
    private static final String TRUFFLE_ACCESSOR_CLASS_NAME =
            "org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor"; // NOI18N
    private static final String TRUFFLE_ACCESSOR_PATH =
            "org/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleAccessor"; // NOI18N
    
    private final JPDADebugger debugger;
    
    public FirstSourceURLProvider(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public String getURL(String relativePath, boolean global) {
        if (TRUFFLE_ACCESSOR_PATH.equals(relativePath)) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
            if (currentPCInfo != null) {
                return currentPCInfo.getSourcePosition().getSource().getUrl().toExternalForm();
            }
        }
        return null;
    }
    
    public String getURL(JPDAClassType clazz, String stratum) {
        if (TRUFFLE_ACCESSOR_CLASS_NAME.equals(clazz.getName())) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
            if (currentPCInfo != null) {
                Source source = currentPCInfo.getSourcePosition().getSource();
                if (source != null) {
                    URL url = source.getUrl();
                    if (url != null) {
                        return url.toExternalForm();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getRelativePath(String url, char directorySeparator, boolean includeExtension) {
        return null;
    }

    @Override
    public String[] getSourceRoots() {
        return NO_SOURCE_ROOTS;
    }

    @Override
    public void setSourceRoots(String[] sourceRoots) {
    }

    @Override
    public String[] getOriginalSourceRoots() {
        return NO_SOURCE_ROOTS;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
}

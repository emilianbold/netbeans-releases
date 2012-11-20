/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.indexing.impl;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.spi.APTIndexingFilterProvider;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Egor Ushakov <gorrus@netbeans.org>
 */
@ServiceProvider(service = APTIndexingFilterProvider.class, path=APTIndexingFilterProvider.PATH, position=1000)
public class CndIndexingFilterProviderImpl implements APTIndexingFilterProvider {
    
    @Override
    public TokenStream getIndexed(FileSystem fs, CharSequence absPath, TokenStream orig) {
        return new IndexingFilter(fs, absPath.toString(), orig);
    }
    
    private static void store(final FileSystem fs, final String name, final Set<String> ids) {
        CndTextIndexImpl index = CndTextIndexManager.get(fs);
        index.put(name, ids);
    }
    
    private static class IndexingFilter implements TokenStream {
        private final TokenStream orig;
        private final FileSystem fs;
        private final String absPath;
        private final Set<String> ids = new HashSet<String>();
        
        public IndexingFilter(FileSystem fs, String absPath, TokenStream orig) {
            this.fs = fs;
            this.absPath = absPath;
            this.orig = orig;
        }
        
        @Override
        public APTToken nextToken() throws TokenStreamException {
            APTToken next = (APTToken) orig.nextToken();
            indexToken(next);
            return next;
        }
        
        private void indexToken(APTToken token) {
            // index only identifiers
            if (APTUtils.isID(token) || token.getType() == APTTokenTypes.ID_DEFINED) {
                ids.add(token.getText());
//                Logger.getLogger(IndexingFilterProviderImpl.class.getName()).info("Indexed " + token.getText());
            } else if (APTUtils.isEOF(token)) {
                // store on last token
                store(fs, absPath, ids);
            }
        }
    }
}

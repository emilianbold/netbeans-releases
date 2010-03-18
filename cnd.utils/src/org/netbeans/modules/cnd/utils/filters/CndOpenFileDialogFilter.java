/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.utils.filters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.openfile.OpenFileDialogFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexander Simon
 */
public class CndOpenFileDialogFilter {
    
    private CndOpenFileDialogFilter(){
    }

    private static String convertDescription(String description){
        int from = description.indexOf('(');
        int to = description.indexOf(')');
        if (from >= 0 && to > from) {
            description = description.substring(0, from)+description.substring(to+1);
        }
        return description;
    }

    private static String[] convertSuffixes(String suffixes){
        StringTokenizer st = new StringTokenizer(suffixes, " "); // NOI18N
        List<String> res = new ArrayList<String>();
        while(st.hasMoreTokens()) {
            res.add(st.nextToken().trim());
        }
        return res.toArray(new String[res.size()]);
    }

    private static class Adapter extends OpenFileDialogFilter {
        private SourceFileFilter delegate;
        public Adapter(SourceFileFilter delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getDescriptionString() {
            return CndOpenFileDialogFilter.convertDescription(delegate.getDescription());
        }

        @Override
        public boolean accept(File file) {
            return delegate.accept(file);
        }

        @Override
        public String[] getSuffixes() {
            return CndOpenFileDialogFilter.convertSuffixes(delegate.getSuffixesAsString());
        }
    }

    @ServiceProvider(service = org.netbeans.modules.openfile.OpenFileDialogFilter.class)
    public static final class CFilter extends Adapter {
        public CFilter() {
            super(CSourceFileFilter.getInstance());
        }
    }

    @ServiceProvider(service = org.netbeans.modules.openfile.OpenFileDialogFilter.class)
    public static final class CppFilter extends Adapter {
        public CppFilter() {
            super(CCSourceFileFilter.getInstance());
        }
    }

    @ServiceProvider(service = org.netbeans.modules.openfile.OpenFileDialogFilter.class)
    public static final class HeaderFilter extends Adapter {
        public HeaderFilter() {
            super(HeaderSourceFileFilter.getInstance());
        }
    }

    @ServiceProvider(service = org.netbeans.modules.openfile.OpenFileDialogFilter.class)
    public static final class FortranFilter extends Adapter {
        public FortranFilter() {
            super(FortranSourceFileFilter.getInstance());
        }
    }

    @ServiceProvider(service = org.netbeans.modules.openfile.OpenFileDialogFilter.class)
    public static final class ResourceFilter extends Adapter {
        public ResourceFilter() {
            super(ResourceFileFilter.getInstance());
        }
    }

    @ServiceProvider(service = org.netbeans.modules.openfile.OpenFileDialogFilter.class)
    public static final class QtFilter extends Adapter {
        public QtFilter() {
            super(QtFileFilter.getInstance());
        }
    }
}

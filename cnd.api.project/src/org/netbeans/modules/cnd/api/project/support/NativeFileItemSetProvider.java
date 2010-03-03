/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.project.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.source.spi.CndCookieProvider;
import org.openide.loaders.DataObject;
import org.openide.util.WeakSet;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Alexey Vladykin
 */
@ServiceProvider(service = CndCookieProvider.class)
public final class NativeFileItemSetProvider extends CndCookieProvider {

    @Override
    public void addLookup(DataObject dao, InstanceContent ic) {
        ic.add(new NativeFileItemSetImpl());
    }

    private static final class NativeFileItemSetImpl implements NativeFileItemSet {

        private Set<NativeFileItem> items = new WeakSet<NativeFileItem>(1);

        @Override
        public synchronized Collection<NativeFileItem> getItems() {
            ArrayList<NativeFileItem> res = new ArrayList<NativeFileItem>(items.size());
            for(NativeFileItem item : items) {
                if (item != null) {
                    res.add(item);
                }
            }
            return res;
        }

        @Override
        public synchronized void add(NativeFileItem item) {
            if (item == null) {
                return;
            }
            items.add(item);
        }

        @Override
        public synchronized void remove(NativeFileItem item) {
            if (item == null) {
                return;
            }
            items.remove(item);
        }

        @Override
        public boolean isEmpty() {
            return items.isEmpty();
        }
    }
}

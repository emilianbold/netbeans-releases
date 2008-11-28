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
package org.netbeans.modules.php.editor.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public class ModelUtils {

    private ModelUtils() {
    }

    @CheckForNull
    public static <T extends ModelElement> T getFirst(List<? extends T> all) {
        return all.size() > 0 ? all.get(0) : null;
    }

    @CheckForNull
    public static <T extends Occurence<? extends ModelElement>> T getFirst(List<? extends T> all) {
        return all.size() > 0 ? all.get(0) : null;
    }

    @CheckForNull
    public static <T extends ModelElement> T getLast(List<? extends T> all) {
        return all.size() > 0 ? all.get(all.size()-1) : null;
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> forFileOnly(List<? extends T> all, FileObject fo) {
        List<T> retval = new ArrayList<T>();
        for (T element : all) {
            if (element.getFileObject() == fo) {
                retval.add(element);
            }
        }
        return retval;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T extends ModelElement> List<? extends T> merge(List<? extends T>... all) {
        List<T> retval = new ArrayList<T>();
        for (List<? extends T> list : all) {
            retval.addAll(list);
        }
        return retval;
    }

    @NonNull
    public static ModelScope getModelScope(ModelElement element) {
        ModelScope tls = (element instanceof ModelScope) ? (ModelScope)element : null;
        while (tls == null && element != null) {
            element = element.getInScope();
            tls = (ModelScope) ((element instanceof ModelScope) ? element : null);
        }
        assert tls != null;
        return tls;
    }
}

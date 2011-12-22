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
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.*;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.ModelElement;
import org.netbeans.modules.javascript2.editor.model.Scope;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class ScopeImpl extends ModelElementImpl implements Scope {

    private OffsetRange offsetRange;
    private final List<ModelElementImpl> elements = Collections.synchronizedList(new LinkedList<ModelElementImpl>());
    
    public ScopeImpl(ModelElement in, JsElement.Kind kind, FileObject fileObject, String name, OffsetRange offsetRange, Set<Modifier> modifiers) {
        super(in, kind, fileObject, name, offsetRange, modifiers);
        this.offsetRange = offsetRange;
    }
    
    @Override
    public boolean signatureEquals(ElementHandle handle) {
        return true;
    }
    
    @Override
    public OffsetRange getBlockRange() {
        return offsetRange;
    }

    @Override
    public List<? extends ModelElement> getElements() {
        return new ArrayList<ModelElementImpl>(elements);
    }
    
    void addElement(ModelElementImpl element) {
        elements.add(element);
    }

    static interface ElementFilter<T extends ModelElement> {
        boolean isAccepted(ModelElement element);
    }
    
    @SuppressWarnings("unchecked")
    static <T extends ModelElement> Collection<? extends T> filter(final Collection<? extends ModelElement> original,
            final ElementFilter<T> filter) {
        Set<T> retval = new HashSet<T>();
        for (ModelElement baseElement : original) {
            boolean accepted = filter.isAccepted(baseElement);
            if (accepted) {
                retval.add((T) baseElement);
            }
        }
        return retval;
    }
    
}

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
package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
abstract class ScopeImpl extends ModelElementImpl implements Scope {

    private OffsetRange blockRange = null;
    private List<ModelElementImpl> elements = new ArrayList<ModelElementImpl>();
    private ModelScopeImpl mScope;

    //new contructors
    ScopeImpl(ScopeImpl inScope, ASTNodeInfo info, PhpModifiers modifiers, Block block) {
        super(inScope, info, modifiers);
        setBlockRange(block);
    }

    ScopeImpl(ScopeImpl inScope, IndexedElement element, PhpKind kind) {
        super(inScope, element, kind);
    }
    //old contructors
    ScopeImpl(ScopeImpl inScope, String name, Union2<String/*url*/, FileObject> file,
            OffsetRange offsetRange, PhpKind kind) {
        super(inScope, name, file, offsetRange, kind);
        assert isScopeKind(kind);
    }

    ScopeImpl(ScopeImpl inScope, String name, Union2<String/*url*/, FileObject> file,
            OffsetRange offsetRange, PhpKind kind,
            PhpModifiers modifier) {
        super(inScope, name, file, offsetRange, kind, modifier);
        assert isScopeKind(kind);
    }

    private static boolean isScopeKind(PhpKind kind) {
        switch (kind) {
            case PROGRAM:
            case INDEX:
            case CLASS:
            case FUNCTION:
            case IFACE:
            case METHOD:
            case VARIABLE:
                return true;
        }
        return false;
    }

    public List<? extends ModelElementImpl> getElements() {
        return elements;
    }

    void addElement(ModelElementImpl element) {
        elements.add(element);
    }

    @SuppressWarnings("unchecked")
    static <T extends ModelElementImpl> List<? extends T> filter(final List<? extends ModelElementImpl> original,
            final ElementFilter filter) {
        List<T> retval = new ArrayList<T>();
        for (ModelElementImpl baseElement : original) {
            boolean accepted = filter.isAccepted(baseElement);
            if (accepted) {
                retval.add((T) baseElement);
            }
        }
        return retval;
    }

    static interface ElementFilter {

        boolean isAccepted(ModelElementImpl element);
    }

    @Override
    final StringBuilder golden(int indent) {
        String prefix = "";//NOI18N
        for (int i = 0; i < indent; i++) {
            prefix += "  ";
        }//NOI18N
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(toString()).append("\n");//NOI18N
        List<? extends ModelElementImpl> allElems = getElements();
        for (ModelElementImpl modelElement : allElems) {
            sb.append(modelElement.golden(indent + 1));
        }
        return sb;
    }

    void setBlockRange(Block block) {
        if (block != null) {
            this.blockRange = new OffsetRange(block.getStartOffset(), block.getEndOffset());
        }
    }

    public OffsetRange getBlockRange() {
        //assert blockRange != null;
        return blockRange;
    }

    protected final IndexScopeImpl getTopIndexScopeImpl() {
        if (mScope == null) {
            mScope = (ModelScopeImpl) ModelUtils.getModelScope(this);
        }
        return (IndexScopeImpl) ((mScope instanceof IndexScopeImpl) ? mScope : null);
    }
}

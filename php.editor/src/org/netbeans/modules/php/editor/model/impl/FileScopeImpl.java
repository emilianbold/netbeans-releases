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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.model.*;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
final class FileScopeImpl extends ScopeImpl implements FileScope  {

    private CachingSupport cachedModelSupport;
    private ParserResult info;
    private Map<PhpElement, List<Occurence>> occurences =
            new HashMap<PhpElement, List<Occurence>>();
    private List<CodeMarkerImpl> codeMarkers = new ArrayList<CodeMarkerImpl>();

    FileScopeImpl(ParserResult info) {
        this(info, "program");//NOI18N
    }

    private FileScopeImpl(ParserResult info, String name) {
        super(null, name, Union2.<String, FileObject>createSecond(info != null ? info.getSnapshot().getSource().getFileObject() : null), new OffsetRange(0, 0), PhpElementKind.PROGRAM);//NOI18N
        this.info = info;
        this.cachedModelSupport = new CachingSupport(this);
    }

    void addCodeMarker(CodeMarkerImpl codeMarkerImpl) {
        codeMarkers.add(codeMarkerImpl);
    }

    void addOccurence(Occurence occurence) {
        PhpElement declaration = occurence.getDeclaration();
        addOccurence(declaration, occurence);
    }

    void addOccurence(final PhpElement declaration, Occurence occurence) {
        List<Occurence> ocList = occurences.get(declaration);
        if (ocList == null) {
            ocList = new ArrayList<Occurence>();
            List<Occurence> old = occurences.put(declaration, ocList);
            assert old == null;
        }
        assert occurence != null;
        ocList.add(occurence);
    }

    List<? extends CodeMarker> getMarkers() {
        return codeMarkers;
    }

    /**
     * @return the occurences
     */
    List<Occurence> getOccurences() {
        List<Occurence> ocList = new ArrayList<Occurence>();
        Collection<List<Occurence>> values = occurences.values();
        for (List<Occurence> list : values) {
            ocList.addAll(list);
        }
        return ocList;
    }

    List<Occurence> getAllOccurences(PhpElement declaration) {
        final List<Occurence> retval = occurences.get(declaration);
        return retval != null ? retval : Collections.<Occurence>emptyList();
    }

    List<Occurence> getAllOccurences(Occurence occurence) {
        return getAllOccurences(occurence.getDeclaration());
    }

    /**
     * @return the indexScope
     */
    public IndexScope getIndexScope() {
        return ModelVisitor.getIndexScope(info);
    }

    public Collection<? extends NamespaceScope> getDeclaredNamespaces() {
        return filter(getElements(), new ElementFilter<NamespaceScope>() {
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.NAMESPACE_DECLARATION);
            }
        });
    }

    @NonNull
    public CachingSupport getCachingSupport() {
        return cachedModelSupport;
    }

    public NamespaceScope getDefaultDeclaredNamespace() {
        return ModelUtils.getFirst(ModelUtils.filter(getDeclaredNamespaces(), new ModelUtils.ElementFilter<NamespaceScope>() {
            public boolean isAccepted(NamespaceScope ns) {
                return ns.isDefaultNamespace();
            }
        }));
    }

}

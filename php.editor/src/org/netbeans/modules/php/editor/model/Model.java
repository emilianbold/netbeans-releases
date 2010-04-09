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

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;
import org.netbeans.modules.php.editor.parser.api.Utils;

/**
 * @author Radek Matous
 */
public final class Model {
    private ModelVisitor modelVisitor;
    private final ParserResult info;
    private OccurencesSupport occurencesSupport;

    Model(ParserResult info) {
        this.info = info;
    }

//    Model(FileObject fo) {
//        ParserManager.
//    }

    public Model getExtendedModel() {
        getModelVisitor().extendModel();
        return this;
    }

    public FileScope getFileScope() {
        final ModelVisitor visitor = getModelVisitor();
        return visitor.getFileScope();
    }

    public IndexScope getIndexScope() {
        return ModelVisitor.getIndexScope(info);
    }

    public OccurencesSupport getOccurencesSupport(final OffsetRange range) {
        final ModelVisitor visitor = getModelVisitor();
        synchronized(this) {
            if (occurencesSupport == null || !range.containsInclusive(occurencesSupport.offset)) {
                occurencesSupport = new OccurencesSupport(visitor, range.getStart()+1);
            }
        }
        return occurencesSupport;
    }

    public OccurencesSupport getOccurencesSupport(final int offset) {
        final ModelVisitor visitor = getModelVisitor();
        synchronized(this) {
            if (occurencesSupport == null || occurencesSupport.offset != offset) {
                occurencesSupport = new OccurencesSupport(visitor, offset);
            }
        }
        return occurencesSupport;
    }

    public ParameterInfoSupport getParameterInfoSupport(final int offset) {
        final ModelVisitor visitor = getModelVisitor();
        return new ParameterInfoSupport(visitor, offset);
    }

    public VariableScope getVariableScope(final int offset) {
        final ModelVisitor visitor = getModelVisitor();
        return visitor.getVariableScope(offset);
    }

    public ModelElement findDeclaration(final PhpElement element) {
        final ModelVisitor visitor = getModelVisitor();
        return visitor.findDeclaration(element);
    }

    /**
     * @return the modelVisitor
     */
    synchronized ModelVisitor getModelVisitor() {
        if (modelVisitor == null) {
            modelVisitor = new ModelVisitor(info);
            modelVisitor.scan(Utils.getRoot(info));
        }

        return modelVisitor;
    }
}

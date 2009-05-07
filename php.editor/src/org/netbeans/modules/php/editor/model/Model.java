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

import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class Model {
    private ModelVisitor modelVisitor;
    private ParserResult info;
    private int offset;

    Model(ParserResult info) {
        this.info = info;
        this.offset = -1;
    }

    public FileScope getFileScope() {
        return getModelVisitor(-1).getModelScope();
    }

    public IndexScope getIndexScope() {
        return ModelVisitor.getIndexScope(info);
    }

    public OccurencesSupport getOccurencesSupport(final int offset) {
        return new OccurencesSupport(getModelVisitor(offset), offset);
    }

    public ParameterInfoSupport getParameterInfoSupport(final int offset) {
        return new ParameterInfoSupport(getModelVisitor(-1), info.getSnapshot().getSource().getDocument(false), offset);
    }

    public VariableScope getVariableScope(final int offset) {
        return getModelVisitor(-1).getVariableScope(offset);
    }

    /**
     * @return the modelVisitor
     */
    private ModelVisitor getModelVisitor(int offset) {
        if (modelVisitor == null || (offset >= 0 && this.offset != offset)) {
            if (offset < 0) {
                modelVisitor = new ModelVisitor(info);
            } else {
                modelVisitor = new ModelVisitor(info, offset);
            }
            modelVisitor.scan(Utils.getRoot(info));
        }

        return modelVisitor;
    }
    ModelVisitor getModelVisitor(ModelElement element) {
        Parameters.notNull("element", element);
        if (modelVisitor == null) {
            modelVisitor = new ModelVisitor(info, element);
            modelVisitor.scan(Utils.getRoot(info));
        }
        return modelVisitor;
    }
}

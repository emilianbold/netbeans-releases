/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCreator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A redefine creator.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class RedefineCreator extends ExternalReferenceCreator<Schema> {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The decorator for this redefine. */
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of RedefineCreator
     *
     * @param  schema  component to contain the redefine(s).
     */
    public RedefineCreator(Schema schema) {
        super(schema, null);
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        Schema schema = getModelComponent();
        SchemaModel model = schema.getModel();
        List<Node> nodes = getSelectedNodes();
        for (Node node : nodes) {
            Redefine imp = model.getFactory().createRedefine();
            // Save the location.
            imp.setSchemaLocation(getLocation(node));
            schema.addExternalReference(imp);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RedefineCreator.class);
    }

    protected String getTargetNamespace(Model model) {
        return ((SchemaModel) model).getSchema().getTargetNamespace();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getPrefixes(Model model) {
        SchemaModel sm = (SchemaModel) model;
        AbstractDocumentComponent schema =
                (AbstractDocumentComponent) sm.getSchema();
        return schema.getPrefixes();
    }

    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new SchemaReferenceDecorator(this);
        }
        return decorator;
    }

    public boolean mustNamespaceDiffer() {
        return false;
    }

    protected String referenceTypeName() {
        return NbBundle.getMessage(RedefineCreator.class,
                "LBL_RedefineCreator_Type");
    }
}

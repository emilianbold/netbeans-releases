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

package org.netbeans.modules.php.editor.parser;

import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.parser.astnodes.Program;


/**
 *
 * @author Petr Pisl
 */
public class PHPParseResult extends ParserResult {
    
    private final Program root;
    private List<Error> errors;
    private volatile Model model;

    public PHPParseResult(Snapshot snapshot, Program rootNode) {
        super(snapshot);
        this.root = rootNode;
        this.errors = Collections.<Error>emptyList();
    }

    public Program getProgram() {
        return root;
    }
    
    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    public Model getModel() {
        return getModel(true);
    }

    public Model getModel(boolean extended) {
        synchronized(this) {
            if (model == null) {
                model = ModelFactory.getModel(this);
            }
        }
        if (extended) {
            model.getExtendedModel();
        }
        return model;
    }

    @Override
    protected void invalidate() {
        // comments copied from Groovy:
        // FIXME parsing API
        // remove from parser cache (?)
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

}

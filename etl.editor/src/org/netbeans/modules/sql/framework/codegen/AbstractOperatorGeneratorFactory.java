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

package org.netbeans.modules.sql.framework.codegen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.sql.framework.codegen.base.GeneratorHelper;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import com.sun.etl.exception.BaseException;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public abstract class AbstractOperatorGeneratorFactory extends AbstractGenerator {

    private Map<String, Generator> operatorToGeneratorMap = null;
    private Generator delegate;

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        if (operatorToGeneratorMap == null) {
            createOperatorSpecificGenerator();
            //create delegate
            delegate = createDefaultOperatorGenerator();
        }

        SQLOperator operator = (SQLOperator) obj;
        // try to get operator specific generator
        Generator generator = operatorToGeneratorMap.get(operator.getOperatorType());
        // if we do not have operator specific generator then use the delegate as
        // a default
        if (generator == null) {
            generator = delegate;
        }

        return generator.generate(obj, context);
    }

    private void createOperatorSpecificGenerator() throws BaseException {
        operatorToGeneratorMap = new HashMap<String, Generator>();

        Map opToEvalClassMap = initializeOperatorSpecificGenerator();
        Iterator it = opToEvalClassMap.keySet().iterator();
        while (it.hasNext()) {
            String opName = (String) it.next();
            String className = (String) opToEvalClassMap.get(opName);

            Generator generator = GeneratorHelper.create(className, this.getGeneratorFactory());
            operatorToGeneratorMap.put(opName, generator);
        }
    }

    public abstract Map<String, String> initializeOperatorSpecificGenerator();

    Generator createDefaultOperatorGenerator() {
        Generator generator = new OperatorGenerator();
        generator.setGeneratorFactory(this.getGeneratorFactory());
        return generator;
    }
}
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.validation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.html.api.validation.ValidationContext;
import org.netbeans.html.api.validation.ValidationException;
import org.netbeans.html.api.validation.ValidationResult;
import org.netbeans.html.api.validation.Validator;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=Validator.class, position=10)
public class ValidatorImpl implements Validator {

    @Override
    public ValidationResult validate(ValidationContext context) throws ValidationException {
        assert canValidate(context.getVersion());
        
        try {
            ValidationTransaction validatorTransaction = 
                    ValidationTransaction.create(context.getVersion()); //NOI18N

            String source = context.getSource();
            validatorTransaction.validateCode(source);

            Collection<ProblemDescription> problems = new LinkedList<ProblemDescription>(validatorTransaction.getFoundProblems(ProblemDescription.WARNING));
            
            if(context.getSyntaxAnalyzerResult().getDetectedHtmlVersion() == null) {
                //unknown doctype, the HtmlSourceVersionQuery is used
                //some of the "missing doctype" errors should be suppressed
                filterNoDoctypeProblems(problems);
            }

            return new ValidationResult(this, context, problems, validatorTransaction.isSuccess());

        } catch (SAXException ex) {
            throw new ValidationException(ex);
        }

    }

    @Override
    public String getValidatorName() {
        return "validator.nu"; //NOI18N
    }

    @Override
    //XXX the validator can also validate html4, but for now such validation is done by the old SGML parser
    public boolean canValidate(HtmlVersion version) {
        switch(version) {
            case HTML41_FRAMESET:
            case HTML41_STRICT:
            case HTML41_TRANSATIONAL:
            case XHTML10_FRAMESET:
            case XHTML10_TRANSATIONAL:
            case XHTML10_STICT:
            case HTML5:
            case XHTML5:
                return true;
            default:
                return false;
        }
    }

    private void filterNoDoctypeProblems(Collection<ProblemDescription> problems) {
        for(Iterator<ProblemDescription> itr = problems.iterator(); itr.hasNext();) {
            ProblemDescription problem = itr.next();
            if(problem.getText().startsWith("Error: Start tag seen without seeing a doctype first.")) { //NOI18N
                itr.remove();
            }
        }
    }

}

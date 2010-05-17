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
package org.netbeans.modules.edm.codegen;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public abstract class BaseDB extends AbstractDB {


    protected SQLOperatorFactory factory;
    
    private static final SQLIdentifiers.Quoter quoter = SQLIdentifiers.createQuoter("\"");

    public int getCastingRule(int sourceType, int targetType) {
        return 0;
    }

    public String getDefaultDateFormat() {
        return null;
    }

    public String getEscapedName(String name) {
        if(isQuoteAlways()){
            return getQuoter().quoteAlways(name);
        }
        return getQuoter().quoteIfNeeded(name);
    }

    public String getUnescapedName(String name) {
        return getQuoter().unquote(name);
    }
    
    protected SQLIdentifiers.Quoter getQuoter(){
        return quoter;
    }

    public String getEscapedCatalogName(String name) {
        return this.getEscapedName(name);
    }

    public String getEscapedSchemaName(String name) {
        return this.getEscapedName(name);
    }

    public int getMaxTableNameLength() {
        return 0;
    }

    public boolean isAnsiJoinSyntaxSupported() {
        return true;
    }

    public AbstractGeneratorFactory createGeneratorFactory() {
        return new BaseGeneratorFactory(this);
    }

    public Statements createStatements() {
        return new BaseStatements(this);
    }

    public TypeGenerator createTypeGenerator() {
        return new BaseTypeGenerator();
    }


}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.edm.codegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.netbeans.modules.edm.editor.utils.AttributeFactory;
import org.netbeans.modules.edm.editor.utils.AttributeParser;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public abstract class AbstractDB implements DB {

    protected Statements statements;
    protected AbstractGeneratorFactory generatorFactory;
    protected TypeGenerator typeGenerator;
    protected Map templateMaps = new HashMap();
    private boolean quoteAlways = true;

    public AbstractDB() {
        this.generatorFactory = this.createGeneratorFactory();
        this.typeGenerator = this.createTypeGenerator();
        this.statements = this.createStatements();
        this.loadTemplates();
    }

    public abstract int getCastingRule(int sourceType, int targetType);

    public abstract String getDefaultDateFormat();

    public abstract boolean isAnsiJoinSyntaxSupported();

    public abstract int getMaxTableNameLength();

    public abstract String getEscapedName(String name);

    public abstract String getUnescapedName(String name);

    public abstract String getEscapedCatalogName(String name);

    public abstract String getEscapedSchemaName(String name);

    public abstract Statements createStatements();

    public abstract AbstractGeneratorFactory createGeneratorFactory();

    public abstract TypeGenerator createTypeGenerator();

    public abstract SQLOperatorFactory getOperatorFactory();

    public Statements getStatements() {
        return this.statements;
    }

    public AbstractGeneratorFactory getGeneratorFactory() {
        return this.generatorFactory;
    }

    public TypeGenerator getTypeGenerator() {
        return this.typeGenerator;
    }

    protected abstract Map loadTemplates();

    protected Map loadTemplates(String filename) {
        InputStream in = null;
        AttributeFactory fac = new AttributeFactory();

        try {
            in = this.getClass().getResourceAsStream(filename);
            AttributeParser aParser = new AttributeParser(in, fac);
            aParser.parse();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }

        return fac.getAttributeMap();
    }

    public String getTemplateFileName(String templateKey) {
        Attribute attr = (Attribute) templateMaps.get(templateKey);

        if (attr != null) {
            String val = (String) attr.getAttributeValue();
            return val;
        }

        return null;
    }

    public Reader getTemplateContent(String templateKey) {
        String templateFileName = getTemplateFileName(templateKey);
        if (templateFileName == null) {
            return null;
        }

        InputStream inStream = this.getClass().getResourceAsStream(templateFileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));

        return br;
    }
    
    public void setQuoteAlways(boolean quoteAlways){
        this.quoteAlways = quoteAlways;
    }
    
    public boolean isQuoteAlways(){
        return quoteAlways;
    }
}
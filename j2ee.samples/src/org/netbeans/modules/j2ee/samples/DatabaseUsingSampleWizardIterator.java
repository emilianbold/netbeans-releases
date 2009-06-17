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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.j2ee.samples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class DatabaseUsingSampleWizardIterator extends JavaEESamplesWizardIterator {
    private static final String DB_RES_FILE = "CustomerCMP-ejb/setup/derby_netPool.sun-resource"; //NOI18N

    @Override
    protected WizardDescriptor.Panel[] createPanels() {
        boolean specifyPrjName = "web".equals(Templates.getTemplate(wiz).getAttribute("prjType"));
        return new WizardDescriptor.Panel[] {
            new JavaEESamplesWizardPanel(true, specifyPrjName)
        };
    }
    
    @Override
    public Set<FileObject> instantiate() throws IOException{
        String dbName = (String) wiz.getProperty(WizardProperties.DB_NAME);
        try {
            DerbyDatabases.createDatabase(dbName, "app", "app"); //NOI18N
            
        } catch (IllegalStateException ex) {
            throw new RuntimeException(ex);
        } catch (DatabaseException ex) {
            throw new RuntimeException(ex);
        }
        Set r = super.instantiate();
        
        File projectDir = (File) wiz.getProperty(WizardProperties.PROJ_DIR);
        File dbResource = new File(projectDir, DB_RES_FILE);
        updateDBResource(dbResource, dbName);
        
        return r;
    }
    
    public static DatabaseUsingSampleWizardIterator createIterator() {
        return new DatabaseUsingSampleWizardIterator();
    }
    
    private void updateDBResource(File dbResource, String dbName) throws IOException {
        String xPathPath = "/resources/jdbc-connection-pool/property[@name='DatabaseName']/@value"; //NOI18N
        setValueInXMLFile(dbResource, xPathPath, dbName);
    }

    private static void setValueInXMLFile(File srcFile, String xPathPath, String value) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(srcFile);
            
            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate(xPathPath, document, XPathConstants.NODE);
            node.setTextContent(value);
            
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            
            Transformer aTransformer = tranFactory.newTransformer();
            Source src = new DOMSource(document);
            FileOutputStream fos = new FileOutputStream(srcFile);
            Result dest = new StreamResult(fos);
            aTransformer.transform(src, dest);
            fos.close();
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class DatabaseUsingSampleWizardIterator extends JavaEESamplesWizardIterator {
    private static final String DB_RES_FILE = "CustomerCMP-ejb/setup/derby_netPool.sun-resource"; //NOI18N

    @Override protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new JavaEESamplesWizardPanel(true)
        };
    }
    
    @Override public Set/*<FileObject>*/ instantiate() throws IOException{
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

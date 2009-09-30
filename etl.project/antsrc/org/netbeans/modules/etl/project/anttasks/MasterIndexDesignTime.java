/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.etl.project.anttasks;

import org.netbeans.modules.masterindex.plugin.TargetDBSchemaGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.tools.ant.Task;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.project.Localizer;


/**
 *
 * @author Manish
 */
public class MasterIndexDesignTime extends Task {

    private static String USER_DIR = System.getProperty("user.dir");
    private static String fs = System.getProperty("file.separator");
    private static String CONFIG_DIR = USER_DIR + fs + "src" + fs + "com" + fs + "stc" + fs + "eindex" + fs + "config";
    private static String OBJECTDEF = "object.xml";
    private static String QUERY_FILE_PATH = CONFIG_DIR + fs + "query.txt";
    private static String QNAME_PREFXI = "Enterprise.SystemObject";
    Element docroot = null;
    private String parent = null;
    private String[] children = null;
    // Structures
    ArrayList selectFields = new ArrayList();
    String parentPrimaryKey = null;
    ArrayList childrenForeignKey = new ArrayList();
    ArrayList orderByFields = new ArrayList();
    private static transient final Logger mLogger = Logger.getLogger(MasterIndexDesignTime.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /** Creates a new instance of Main */
    public MasterIndexDesignTime() {
    }

    private void parseObjectDef() {

        try {
            DocumentBuilder root = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            docroot = root.parse(CONFIG_DIR + fs + OBJECTDEF).getDocumentElement();
        } catch (SAXException ex) {
            mLogger.infoNoloc(mLoc.t("SAX Exception : " + CONFIG_DIR + fs + OBJECTDEF));
        } catch (IOException ex) {
            mLogger.infoNoloc(mLoc.t("Cannot I/O document : " + CONFIG_DIR + fs + OBJECTDEF));
        } catch (ParserConfigurationException ex) {
            mLogger.infoNoloc(mLoc.t("Cannot Parse document : " + CONFIG_DIR + fs + OBJECTDEF));
        }

        // Parse Relations
        parseRelations();

        // Find nodes
        NodeList nl1 = docroot.getElementsByTagName("nodes");
        for (int i = 0; i < nl1.getLength(); i++) {
            Element nodes_element = (Element) nl1.item(i);

            NodeList taglist = nodes_element.getElementsByTagName("tag");
            String tag_value = taglist.item(0).getTextContent();

            //Get the qualified names for the fields
            String qualname = getQualifiedName(tag_value);
            //Find Fileds
            NodeList fieldlist = nodes_element.getElementsByTagName("fields");
            for (int j = 0; j < fieldlist.getLength(); j++) {
                String filed_name = ((Element) fieldlist.item(j)).getElementsByTagName("field-name").item(0).getTextContent();
                this.selectFields.add(qualname + "." + filed_name);
            }
        }
    //System.out.println("Fields are :: " + this.selectFields.toString());
    }

    private String getQualifiedName(String tag_value) {
        String ret = null;
        if (tag_value.equals(parent)) {
            // Its a parent
            ret = QNAME_PREFXI + "." + this.parent;
        } else {
            // Its a child
            ret = QNAME_PREFXI + "." + this.parent + "." + tag_value;
        }

        return ret;
    }

    private void parseRelations() {
        String qualifiedname = null;

        // Read the relationssips tag and create the qualified name for this node
        NodeList relations = docroot.getElementsByTagName("relationships");
        // Find Parent
        this.parent = ((Element) relations.item(0)).getElementsByTagName("name").item(0).getTextContent();

        // Find Children
        NodeList childrenList = ((Element) relations.item(0)).getElementsByTagName("children");
        children = new String[childrenList.getLength()]; //Set the children Array
        for (int i = 0; i < childrenList.getLength(); i++) {
            Node child = childrenList.item(i);
            children[i] = child.getTextContent();
        }
    }

    private void createOrderBy() {
        this.orderByFields.add(QNAME_PREFXI + "." + "Person.PersonId");
    }

    private void createConditions() {
        this.parentPrimaryKey = QNAME_PREFXI + "." + "Person.PersonId";
        this.childrenForeignKey.add(QNAME_PREFXI + "." + "Person.Address.PersonId");
        this.childrenForeignKey.add(QNAME_PREFXI + "." + "Person.Phone.PersonId");
    }

    private void queryWriter() {
        try {
            StringBuffer sbuf = new StringBuffer();
            File queryFile = new File(QUERY_FILE_PATH);
            BufferedWriter out = new BufferedWriter(new FileWriter(queryFile));
            // Write Selects
            for (int i = 0; i < this.selectFields.size(); i++) {
                sbuf.append("SELECTFIELD" + (i + 1) + "=" + this.selectFields.get(i) + "\n");
            }
            // Write conditions
            for (int i = 0; i < this.childrenForeignKey.size(); i++) {
                sbuf.append("CONDITION" + (i + 1) + "=" + this.parentPrimaryKey + "=" + this.childrenForeignKey.get(i) + "\n");
            }
            //Write Order By
            sbuf.append("ORDERBYFIELD=" + this.parentPrimaryKey);

            //Write to File
            out.write(sbuf.toString());
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private static String objDefn = null;

    public static String getobjDefnLocn() {
        return objDefn;
    }

    public void execute() {
        mLogger.infoNoloc(mLoc.t("eTLeView Design Time - Query Builder [START] ...\n"));
        ChooseLocationDialog dialog = new ChooseLocationDialog(new JFrame(), true);
        dialog.setVisible(true);
        if ((dialog.getDBLocation()) != null && (dialog.getDBName()) != null && (dialog.getObjectDefinition()) != null) {
            TargetDBSchemaGenerator dbgen = TargetDBSchemaGenerator.getTargetDBSchemaGenerator();
            objDefn = dialog.getObjectDefinition();
            boolean fileIsValid = dbgen.setEViewConfigFilePath(dialog.getObjectDefinition(), "object.xml");//("C:/temp/eviewconfig", "objectdef.xml");
            if (fileIsValid) {
                File file = new File(dialog.getDBLocation());
                if (!file.exists()) {
                    file.mkdirs();
                }
                dbgen.createTargetDB(dialog.getDBLocation(), dialog.getDBName());//C:\temp\AAADB 
            }
            System.out.println("\neTLeView Design Time - Query Builder [END].");
            mLogger.infoNoloc(mLoc.t("\neTLeView Design Time - Query Builder [END]."));
        }else{
            mLogger.infoNoloc(mLoc.t("The dialog is cancelled or any one of the fields are empty"));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MasterIndexDesignTime main = new MasterIndexDesignTime();
        main.execute();
    }
}

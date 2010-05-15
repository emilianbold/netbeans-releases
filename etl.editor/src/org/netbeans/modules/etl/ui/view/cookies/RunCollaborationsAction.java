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
package org.netbeans.modules.etl.ui.view.cookies;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.dm.virtual.db.ui.wizard.CommonUtils;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.model.ETLDefinition;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.w3c.dom.Element;

/**
 *
 * @author sailajak
 */
public class RunCollaborationsAction extends CallableSystemAction {

    private static transient final Logger mLogger = Logger.getLogger(RunCollaborationsAction.class.getName());
    private static String fs = File.separator;
    String collabFolderName = "collaborations";
    String projecthome = null;
    String collabExtention = ".etl";

    @Override
    public void performAction() {
        mLogger.infoNoloc(Localizer.get().t("Running all the collaborations ... "));
        projecthome = CommonUtils.PRJ_PATH;
        String pathToeTLDefinitionFiles = projecthome + fs + collabFolderName;
        File collabDir = new File(pathToeTLDefinitionFiles);

        List etlFiles = getFilesRecursively(collabDir, collabExtention);

        ExecuteAllCollabCookie collabCookie = new ExecuteAllCollabCookie();
        collabCookie.startExec((ArrayList) etlFiles);

    }

    @Override
    public String getName() {
        return "Run All Collaborations";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public List getFilesRecursively(File dir, String collabExtention) {
        List ret = new ArrayList();
        File[] fileNdirs = dir.listFiles();
        if (fileNdirs == null) {
            return ret;
        }
        int j = fileNdirs.length;
        for (int i = 0; i < j; i++) {
            if (fileNdirs[i].getName().endsWith(collabExtention)) {
                ret.add(fileNdirs[i]);
            }
        }
        return ret;
    }

    public ETLDefinition getSQLDef(File etlfile) {
        ETLDefinitionImpl def = null;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            Element root = f.newDocumentBuilder().parse(etlfile).getDocumentElement();

            def = new ETLDefinitionImpl();
            def.parseXML(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }
}

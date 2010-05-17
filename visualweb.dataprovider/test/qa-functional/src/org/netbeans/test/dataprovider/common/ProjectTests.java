/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/
package org.netbeans.test.dataprovider.common;

import java.awt.event.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.plugins.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.*;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
        
public class ProjectTests implements Constants {
    private static java.util.List<String> prjNameList = new ArrayList<String>();
    private static int indexCurrentProject = -1;
    private static boolean is_J2EE14_PluginInstalled;

    public java.util.List<String> getPrjNameList() {
        return Collections.unmodifiableList(prjNameList);
    }
    public static String getCurrentPrjName() {
        return (indexCurrentProject > -1 ? prjNameList.get(indexCurrentProject) : null);
    }
    private void addNewProject(String prjName) {
        if (Utils.isStringEmpty(prjName)) return;
        if (prjNameList.contains(prjName)) {
            throw new RuntimeException("Project with the name: [" + prjName + 
                "] was already opened");
        }
        prjNameList.add(prjName);
        indexCurrentProject = prjNameList.size() - 1;
    }
    private void deleteProject(String prjName) {
        if ((Utils.isStringEmpty(prjName)) || (!prjNameList.contains(prjName)) ) return;
        prjNameList.remove(prjName);
        indexCurrentProject = prjNameList.size() - 1;
    }
    
    public String createNewProject() {
        if ((! is_J2EE14_PluginInstalled) && (Utils.isUsedJ2EELevel_14())) {
            PluginsOperator.getInstance().installAvailablePlugins(J2EE_LEVEL_14_COMPATIBILITY_KIT);
            Util.wait(500);
            new QueueTool().waitEmpty();
            is_J2EE14_PluginInstalled = true;
        }
        String errMsg = null;
        
        String j2EE_Level = TestPropertiesHandler.getServerProperty("J2EE_Level");
        String prjName = TestUtils.createNewProject(null, null, true, 
            PROJECT_CATEGORY_WEB, 
            PROJECT_TYPE_WEB_APP, 
            TestPropertiesHandler.getServerProperty("Source_Structure"), j2EE_Level);
        addNewProject(prjName);
        Utils.logMsg("+++ Project [" + prjName + "] has been created");
        
        return errMsg;
    }
    
    public String undeployCurrentProject() {
        return undeployProject(getCurrentPrjName());
    }
    private String undeployProject(String prjName) {
        String errMsg = null;
        try {
            WebUtils.undeployProject(prjName);
            new QueueTool().waitEmpty();
        } catch (Throwable e) {
            e.printStackTrace(Utils.logStream);
            errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        return errMsg;
    }
    
    public String closeCurrentProject() {
        return closeProject(getCurrentPrjName());
    }
    private String closeProject(String prjName) {
        String errMsg = null;
        try {
            Utils.putFocusOnWindowProjects();

            Utils.callPopupMenuOnProjectsTreeNode(prjName, POPUP_MENU_ITEM_CLOSE);
            deleteProject(prjName);

            Util.wait(2000);
            new QueueTool().waitEmpty();
            Utils.logMsg("+++ Project [" + prjName + "] has been closed");
        } catch (Throwable e) {
            e.printStackTrace(Utils.logStream);
            errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        return errMsg;
    }
}

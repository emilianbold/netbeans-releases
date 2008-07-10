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

package org.netbeans.modules.visualweb.gravy.model.project;

import org.netbeans.modules.visualweb.gravy.model.navigation.*;
import org.netbeans.modules.visualweb.gravy.model.components.*;
import org.netbeans.modules.visualweb.gravy.model.project.components.*;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.PageTopComponentOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;

import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.JemmyException;

import javax.swing.tree.TreePath;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Web pages included in Project.
 */

public class WebPage extends JSPFile implements LinkableSource, LinkableTarget {

    private final static String propID = "id";

    PageTopComponentOperator page = null;
    
    private JavaFile jf;
    
    private String sep = File.separator;
    
    /**
     * Creates a new instance of web page.
     * @param path Path to web page in project.
     * @param name Name of web page.
     */
    WebPage(TreePath path, String name, ProjectEntry parent) {
        super(path, name, parent);
        SourceFolder sf = ((WebPageFolder) parent).getSourceFolder();
        JavaFile jf = sf.addJavaFile(name);
        jf.webpage = this;
        this.jf = jf;
        webpage = this;
    }
    
    /**
     * Get Java file for this web page.
     * @return Java file for this web page.
     */
    public JavaFile getJavaFile() {
        return jf;
    }
    
    /**
     * Return added component.
     * @param webcomponent Component in palette to add.
     * @return Component which was added.
     */
    public Component add(WebComponent webcomponent){
        return add(webcomponent, new Point(0, 0));
    }

    /**
     * Return added component.
     * @param webcomponent Component in palette to add.
     * @param point Point to place component.
     * @return Component which was added.
     */
    public Component add(WebComponent webcomponent, Point point){
        return add(webcomponent, point, null);
    }
    
    /**
     * Return added component.
     * @param webcomponent Component in palette to add.
     * @param point Point to place component.
     * @param name Name of component.
     * @return Component which was added.
     */
    public Component add(WebComponent webcomponent, Point point, String name){
        open();
        DesignerPaneOperator designer = page.getDesigner();
        TestUtils.wait(1000);
        PaletteContainerOperator palette = null;
        try {
            palette = new PaletteContainerOperator(webcomponent.getComponentSet().getID());
        }
        catch(Exception e) {
            throw new JemmyException("Palette category with name " + webcomponent.getComponentSet().getID()+ " can't be found!", e);
        }
        TestUtils.wait(2000);
        try {
            palette.addComponent(webcomponent.getID(), designer, point);
        }
        catch(Exception e) {
            throw new JemmyException("Component " + webcomponent.getID()+ " can't be added!", e);
        }
        TestUtils.wait(2000);
        try {
            SheetTableOperator sheet = new SheetTableOperator();
            if (name == null) name = sheet.getValue(propID);
            else sheet.setTextValue(propID, name);
        }
        catch(Exception e) {
            throw new JemmyException("Name for component " + webcomponent.getID()+ " can't be set!", e);
        }
        Component newComponent;
        if (webcomponent.isVisual()) {
            if (webcomponent.isEventHadling()) newComponent = new VisualEventHandlingComponent(webcomponent, name);
            else newComponent = new VisualComponent(webcomponent, name);
            ((VisualComponent) newComponent).setCoordinates(point);
        }
        else newComponent = new NonVisualComponent(webcomponent, name);
        newComponent.Page = this;
        childList.add(newComponent);
        return newComponent;
    }

    /**
     * Return all component on web page.
     * @return Array of Component on web page.
     */
    public Component[] getComponents(){
        return ((Component[]) childList.toArray(new Component[childList.size()]));
    }

    /**
     * Return component on web page with specified index.
     * @param index Index of necessary component.
     * @return Component on web page with specified index.
     */
    public Component getComponent(int index){
        return ((Component) childList.get(index));
    }

    /**
     * Return component on web page with specified type and index.
     * @param index Index of necessary component.
     * @param type Type of necessary component.
     * @return Component on web page with specified type and index.
     */
    public Component getComponent(Component type, int index){
        List tmpComponents = new ArrayList();
        for (int i = 0; i < childList.size(); i++) {
            if (((Component) childList.get(i)).getType().equals(type.getType()))
                tmpComponents.add(childList.get(i));
        }
        return ((Component) tmpComponents.get(index));
    }

    /**
     * Return component on web page with specified ID.
     * @param ID ID of necessary component.
     * @return Component on web page with specified ID.
     */
    public Component getComponent(String ID){
        return null;
    }

    /**
     * Get name of linkable source web page.
     * @return Name of linkable source web page.
     */
    public String getLinkableSourceName() {
        String folder_path = getTreePath().toString();
        if (folder_path.indexOf("|", folder_path.indexOf("|") + 1) == -1) return getName() + ".jsp";
        folder_path = folder_path.substring(folder_path.indexOf("|", folder_path.indexOf("|") + 1) + 1, folder_path.length() - 1).replace('|', '/');
        return folder_path + "/" + getName() + ".jsp";
    }
    
    /**
     * Get name of linkable target web page.
     * @return Name of linkable target web page.
     */
    public String getLinkableTargetName() {
        String folder_path = getTreePath().toString();
        if (folder_path.indexOf("|", folder_path.indexOf("|") + 1) == -1) return getName() + ".jsp";
        folder_path = folder_path.substring(folder_path.indexOf("|", folder_path.indexOf("|") + 1) + 1, folder_path.length() - 1).replace('|', '/');
        return folder_path + "/" + getName() + ".jsp";
    }
    
    /**
     * Get project, which this web page belongs to.
     * @return Project Project, which this web page belongs to.
     */
    private Project getProject() {
        ProjectEntry tmp = this;
        while (!((tmp = tmp.getParent()) instanceof RootEntry));
        return ((RootEntry) tmp).getProject();
    }
    
    /**
     * Open web page file.
     */
    public void open() {
        long tmpTimeout = 0;
        String tmp_path = getTreePath().toString();
        int start_compare = tmp_path.indexOf(RootEntry.webPagesName) + RootEntry.webPagesName.length() + 1;
        String compare_tooltip;
        if (start_compare != tmp_path.length()) 
            compare_tooltip = ((tmp_path.substring(start_compare, tmp_path.length() - 1)) + "|" + this.name + ".jsp").replace('|', File.separatorChar);
        else 
            compare_tooltip = this.name + ".jsp";
        String project_path = getProject().getLocation() + getProject().getName() + sep + "web" + sep;
        compare_tooltip = project_path + compare_tooltip;
        try {
            tmpTimeout = RaveWindowOperator.getDefaultRave().getTimeouts().getTimeout("ComponentOperator.WaitComponentTimeout");
            RaveWindowOperator.getDefaultRave().getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 5000);
            if (page == null) {
                int i = 0;
                while (!((String) (page = new PageTopComponentOperator(getName(), i++)).getDump().get("Tooltip text")).equals(compare_tooltip));
            }
            else {
                try {
                    page.makeComponentVisible();
                }
                catch(Exception e) {throw new TimeoutExpiredException("");}
            }
        }
        catch (TimeoutExpiredException e) {
            int i = 0;
            super.open();
            while (!((String) (page = new PageTopComponentOperator(getName(), i++)).getDump().get("Tooltip text")).equals(compare_tooltip));
        }
        finally {
            RaveWindowOperator.getDefaultRave().getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", tmpTimeout);
        }
        page.getDesigner();
    }
    
    /**
     * Close web page file.
     */
    public void close() {
        open();
        TestUtils.wait(500);
        page.close();
    }
}

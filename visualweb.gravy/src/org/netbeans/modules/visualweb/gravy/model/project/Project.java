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

import org.netbeans.modules.visualweb.gravy.model.navigation.LinkManager;
import org.netbeans.modules.visualweb.gravy.Bundle;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JLabelOperator;

/**
 * Common class for all project types, describing all general actions and methods.
 */

public abstract class Project {

    private final static String bundle = "org.netbeans.modules.visualweb.gravy.model.project.Bundle";
    private final static String popupClose = Bundle.getStringTrimmed(
                                           Bundle.getStringTrimmed(bundle, "NBProjectsBundle"),
                                           Bundle.getStringTrimmed(bundle, "CloseProjectPopupItem"));
    private final static String popupRun = Bundle.getStringTrimmed(
                                           Bundle.getStringTrimmed(bundle, "WebProjectsBundle"),
                                           Bundle.getStringTrimmed(bundle, "RunProjectPopupItem"));

    private final static String STR_NO_VIEW = "<No View Available>";
    private final static String STR_MAIN = " [Main]";

    /**
     * Descriptor of project.
     */
    private ProjectDescriptor prjDescriptor;

    /**
     * Root entry of the project.
     */
    protected RootEntry root;

    /**
     * Link manager of project.
     */
    private LinkManager linkMng;

    /**
     * Creates project with specified descriptor.
     * @param prjDescriptor Descriptor of project.
     */
    public Project(ProjectDescriptor prjDescriptor) {
        setDescriptor(prjDescriptor);
        root = new RootEntry(this);
    }

    /**
     * Get root of project tree.
     */
    public RootEntry getRoot() {
        return root;
    }

    /**
     * Get link manager of project tree.
     */
    public LinkManager getLinkManager() {
        if (linkMng == null) return linkMng = new LinkManager(this);
        else return linkMng;
    }

    /**
     * Rename project.
     * @param newProjectName New project name.
     */
    public void rename(String newProjectName){
        String name = prjDescriptor.getProperty(prjDescriptor.NAME_KEY);
        if (!name.equals(newProjectName)) setName(newProjectName);
    }

    /**
     * Close project.
     */
    public void close() {
        try {
            new ProjectNavigatorOperator().pressPopupItemOnNode(getName(), popupClose);
        }
        catch(Exception e) {
            throw new JemmyException("Project can't be closed!", e);
        }
    }

    /**
     * Run project.
     */
    public void run() {
        try {
            new ProjectNavigatorOperator().pressPopupItemOnNode(getName(), popupRun);
            TestUtils.wait(2000);
            String[] runFinished = {getName() + STR_MAIN, STR_NO_VIEW, ""};
            waitLabelString(1, runFinished);
        }
        catch(Exception e) {
            throw new JemmyException("Project can't be run!", e);
        }
        TestUtils.wait(3000);
    }

    /**
     * Check label with specified index for correspondence to specified string.
     * Method wait when specified string appears in label.
     */
    private void waitLabelString(int index, String compareString) {
        String[] compareStrings = {compareString};
        waitLabelString(index, compareStrings);
    }

    /**
     * Check label with specified index for correspondence to one of a specified string.
     * Method wait when one of a specified string appears in label.
     */
    private void waitLabelString(int index, String[] compareStrings) {
        JLabelOperator jlo = new JLabelOperator(Util.getMainWindow(), index);
        boolean isStringAppeared = false;
        while (true) {
            jlo = new JLabelOperator(Util.getMainWindow(), index);
            TestUtils.wait(1000);            
            String labelText = jlo.getText();
            for (int i = 0; i < compareStrings.length; i++) {
                if (labelText == null || !labelText.equals(compareStrings[i]))
                    continue;
                else {
                    isStringAppeared = true;
                    break;
                }
            }
            if (isStringAppeared) break;
        }
    }

    /**
     * Check label with specified index for correspondence to specified string.
     * Method wait when specified string appears in label and then wait until it disappears.
     */
    private void waitLabelStringDisappear(int index, String compareString) {
        JLabelOperator jlo = new JLabelOperator(Util.getMainWindow(), index);
        while (jlo.getText() == null || !jlo.getText().equals(compareString)) {
            jlo = new JLabelOperator(Util.getMainWindow(), index);
            TestUtils.wait(1000);
        }
        while (jlo.getText() != null && jlo.getText().equals(compareString)) {
            jlo = new JLabelOperator(Util.getMainWindow(), index);
            TestUtils.wait(1000);
        }
    }

    /**
     * Save All project entries.
     */
    public void saveAll() {
    }

    /**
     * Get descriptor of project.
     * @return descriptor of project.
     */
    public ProjectDescriptor getDescriptor() {
        return prjDescriptor;
    }

    /**
     * Set descriptor of project.
     * @param prjDescriptor New descriptor of project.
     */
    private void setDescriptor(ProjectDescriptor prjDescriptor) {
        this.prjDescriptor = prjDescriptor;
    }

    /**
     * Get project name.
     * @return project name.
     */
    public String getName() {
        return prjDescriptor.getProperty(prjDescriptor.NAME_KEY);
    }

    /**
     * Set project name.
     * @param name New project name.
     */
    private void setName(String name) {
        String location = prjDescriptor.getProperty(prjDescriptor.LOCATION_KEY);
        prjDescriptor = new ProjectDescriptor(name, location);
    }

    /**
     * Get project location.
     * @return Absolute project location.
     */
    public String getLocation() {
        return prjDescriptor.getProperty(prjDescriptor.LOCATION_KEY);
    }

    /**
     * Set project location.
     * @param location Absolute project location.
     */
    private void setLocation(String location) {
        String name = prjDescriptor.getProperty(prjDescriptor.NAME_KEY);
        prjDescriptor = new ProjectDescriptor(name, location);
    }

    /**
     * Makes copy of the current project.
     * @param newName Project name.
     * @return Copy of the current project.
     */
    public abstract Project saveAs(String newName);

    /**
     * Makes copy of the current project.
     * @param newName Project name.
     * @param newLocation absolute project location.
     * @return Copy of the current project.
     */
    public abstract Project saveAs(String newName, String newLocation);
}

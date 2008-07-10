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

package org.netbeans.modules.visualweb.gravy.model.navigation;

import org.netbeans.modules.visualweb.gravy.model.project.*;
import org.netbeans.modules.visualweb.gravy.Bundle;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.navigation.NavigatorOperator;

import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.JemmyException;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Class for link managers.
 */

public class LinkManager {
    
    private final static String bundle = "org.netbeans.modules.visualweb.gravy.model.navigation.Bundle";
    private final static String navigationBundle = Bundle.getStringTrimmed(bundle, "NavigationBundle");
    private final static String nodeNavigation = Bundle.getStringTrimmed(navigationBundle, 
                                                 Bundle.getStringTrimmed(bundle, "PageNavigation"));
    private final static String popupOpen = Bundle.getStringTrimmed(bundle, "OpenPopupItem");
    
    private Project project;
    private Hashtable links = new Hashtable();
    
    /**
     * Create new LinkManager for the project.
     */
    public LinkManager(Project project) {
        this.project = project;
    }
    
    /**
     * Create link with specified parameters.
     * @param source LinkableSource which link link to.
     * @param target LinkableTaget which link link from.
     * @param name Link's name.
     * @return Created link.
     */
    public Link createLink(LinkableSource source, LinkableTarget target, String name) {
        ProjectNavigatorOperator prjNav = ProjectNavigatorOperator.showProjectNavigator();
        TestUtils.wait(1000);
        try {
            prjNav.pressPopupItemOnNode(project.getName() + "|" + nodeNavigation, popupOpen, new Operator.DefaultStringComparator(true, true));
        }
        catch(Exception e) {
            throw new JemmyException(popupOpen + " item in popup menu of " + nodeNavigation + " node can't be found!", e);
        }
        TestUtils.wait(1000);
        NavigatorOperator no = new NavigatorOperator();
        TestUtils.wait(1000);
        try {
            no.linkUsingXmlSource(source.getLinkableSourceName(), target.getLinkableTargetName(), name);
        }
        catch(Exception e) {
            throw new JemmyException("Link can't be created!", e);
        }
        TestUtils.wait(1000);
        Link newLink = new Link(source, target, name);
        links.put(name,  newLink);
        return newLink;
    }
    
    /**
     * Change link's source.
     * @param link Link for modification.
     * @param source New LinkableSource which link should link to.
     */
    public void changeSource(Link link, LinkableSource source) {
        link.source = source;
    }
    
    /**
     * Change link's target.
     * @param link Link for modification.
     * @param target New LinkableTarget which link should link from.
     */
    public void changeTarget(Link link, LinkableTarget target) {
        link.target = target;
    }
    
    /**
     * Change link's name.
     * @param link Link for modification.
     * @param name New link's name.
     */
    public void changeName(Link link, String name) {
        link.name = name;
    }
    
    /**
     * Remove link.
     * @param link Link which should be removed.
     */
    public void deleteLink(Link link) {
        links.remove(link.getName());
    }
    
    /**
     * Return all links.
     */
    public Link[] getLinks() {
        return ((Link[]) links.values().toArray(new Link[links.size()]));
    }
    
    /**
     * Return all links with specified source.
     * @param source LinkableSource which links link to.
     * @return Array of links.
     */
    public Link[] getFromLinks(LinkableSource source) {
        ArrayList srcLinks = new ArrayList();
        Link[] tmpLinks = ((Link[]) links.values().toArray(new Link[links.size()]));
        for (int i = 0; i < tmpLinks.length; i++) {
            if (tmpLinks[i].getSource().equals(source))
                srcLinks.add(tmpLinks[i]);
        }
        return ((Link[]) srcLinks.toArray(new Link[srcLinks.size()]));
    }
    
    /**
     * Return all links with specified target.
     * @param target LinkableTarget which links link from.
     * @return Array of links.
     */
    public Link[] getToLinks(LinkableTarget target) {
        ArrayList trgLinks = new ArrayList();
        Link[] tmpLinks = ((Link[]) links.values().toArray(new Link[links.size()]));
        for (int i = 0; i < tmpLinks.length; i++) {
            if (tmpLinks[i].getTarget().equals(target))
                trgLinks.add(tmpLinks[i]);
        }
        return ((Link[]) trgLinks.toArray(new Link[trgLinks.size()]));
    }
}

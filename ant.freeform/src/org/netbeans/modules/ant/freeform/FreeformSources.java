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

package org.netbeans.modules.ant.freeform;

import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Handles source dir list for a freeform project.
 * XXX will not correctly unregister released external source roots
 * @author Jesse Glick
 */
final class FreeformSources implements Sources, AntProjectListener {
    
    private final FreeformProject project;
    
    public FreeformSources(FreeformProject project) {
        this.project = project;
        project.helper().addAntProjectListener(this);
        initSources(); // have to register external build roots eagerly
    }
    
    private Sources delegate;
    private final ChangeSupport cs = new ChangeSupport(this);
    
    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            public SourceGroup[] run() {
                if (delegate == null) {
                    delegate = initSources();
                }
                return delegate.getSourceGroups(type);
            }
        });
    }
    
    private Sources initSources() {
        SourcesHelper h = new SourcesHelper(project, project.helper(), project.evaluator());
        Element genldata = project.getPrimaryConfigurationData();
        Element foldersE = XMLUtil.findElement(genldata, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        if (foldersE != null) {
            for (Element folderE : XMLUtil.findSubElements(foldersE)) {
                Element locationE = XMLUtil.findElement(folderE, "location", FreeformProjectType.NS_GENERAL); // NOI18N
                String location = XMLUtil.findText(locationE);
                if (folderE.getLocalName().equals("build-folder")) { // NOI18N
                    h.addNonSourceRoot(location);
                } else if (folderE.getLocalName().equals("build-file")) { // NOI18N
                    h.addOwnedFile(location);
                } else {
                    assert folderE.getLocalName().equals("source-folder") : folderE;
                    Element nameE = XMLUtil.findElement(folderE, "label", FreeformProjectType.NS_GENERAL); // NOI18N
                    String name = XMLUtil.findText(nameE);
                    Element typeE = XMLUtil.findElement(folderE, "type", FreeformProjectType.NS_GENERAL); // NOI18N
                    String includes = null;
                    Element includesE = XMLUtil.findElement(folderE, "includes", FreeformProjectType.NS_GENERAL); // NOI18N
                    if (includesE != null) {
                        includes = XMLUtil.findText(includesE);
                    }
                    String excludes = null;
                    Element excludesE = XMLUtil.findElement(folderE, "excludes", FreeformProjectType.NS_GENERAL); // NOI18N
                    if (excludesE != null) {
                        excludes = XMLUtil.findText(excludesE);
                    }
                    if (typeE != null) {
                        String type = XMLUtil.findText(typeE);
                        h.addTypedSourceRoot(location, includes, excludes, type, name, null, null);
                    } else {
                        h.addPrincipalSourceRoot(location, includes, excludes, name, null, null);
                    }
                }
            }
        }
        h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        return h.createSources();
    }
    
    public void addChangeListener(ChangeListener changeListener) {
        cs.addChangeListener(changeListener);
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        cs.removeChangeListener(changeListener);
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        delegate = null;
        cs.fireChange();
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
}

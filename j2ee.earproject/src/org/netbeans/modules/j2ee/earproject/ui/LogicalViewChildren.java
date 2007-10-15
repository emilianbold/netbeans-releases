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

package org.netbeans.modules.j2ee.earproject.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * List of children of a containing node.
 * Each child node is represented by one key from some data model.
 * Remember to document what your permitted keys are!
 * Edit this template to work with the classes and logic of your data model.
 * @author vkraemer
 */
public class LogicalViewChildren extends Children.Keys<String>  implements AntProjectListener {
    
    private final AntProjectHelper model;
    private final java.util.Map<String, VisualClassPathItem> vcpItems;
    
    public LogicalViewChildren(AntProjectHelper model) {
        if (null == model) {
            throw new IllegalArgumentException("model cannot be null"); // NOI18N
        }
        this.model = model;
        vcpItems = new HashMap<String, VisualClassPathItem>();
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        // there has been race condition here - incorrect order of listener & update
        // listen to changes in the model:
        model.addAntProjectListener(this);
        // set the children to use:
        updateKeys();
    }
    
    private void updateKeys() {
        Project p = FileOwnerQuery.getOwner(model.getProjectDirectory());
        //#62823 debug
        if(p == null) {
            Logger.getLogger("global").log(Level.WARNING, null,
                    new IllegalStateException("FileOwnerQuery.getOwner(" + model.getProjectDirectory() + ") returned null. " + // NOI18N
                    "Please report this with the situation description to issue #62823 " + // NOI18N
                    "(http://www.netbeans.org/issues/show_bug.cgi?id=62823)."));
            return ;
        }
        
        EarProject earProject = p.getLookup().lookup(EarProject.class);
        List<VisualClassPathItem> vcpis = earProject.getProjectProperties().getJarContentAdditional();
        synchronized (vcpItems) {
            vcpItems.clear();
            for (VisualClassPathItem vcpi : vcpis) {
                Object obj = vcpi.getObject();
                if (!(obj instanceof AntArtifact)) {
                    continue;
                }
                AntArtifact aa = (AntArtifact) obj;
                Project vcpiProject = aa.getProject();
                J2eeModuleProvider jmp = vcpiProject.getLookup().lookup(J2eeModuleProvider.class);
                if (null != jmp) {
                    vcpItems.put(vcpi.getRaw(), vcpi);
                }
            }
            setKeys(vcpItems.keySet());
        }
    }
    
    @Override
    protected void removeNotify() {
        model.removeAntProjectListener(this);
        setKeys(Collections.<String>emptySet());
        super.removeNotify();
    }
    
    protected Node[] createNodes(String key) {
        synchronized (vcpItems) {
            VisualClassPathItem vcpItem = vcpItems.get(key);
            return new Node[] { new ModuleNode(vcpItem, model.getProjectDirectory() ) };
        }
    }
    
    public void modelChanged(Object ev) {
        // your data model changed, so update the children to match:
        updateKeys();
    }
    
    public void configurationXmlChanged(AntProjectEvent ape) {
        // unsafe to call Children.setKeys() while holding a mutext
        // here the caller holds ProjectManager.mutex() read access
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }
 
    public void propertiesChanged(final AntProjectEvent ape) {
        // unsafe to call Children.setKeys() while holding a mutext
        // here the caller holds ProjectManager.mutex() read access
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }
}

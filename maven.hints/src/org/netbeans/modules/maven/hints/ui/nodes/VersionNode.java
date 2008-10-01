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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.hints.ui.nodes;

import javax.swing.Action;

import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * 
 * @author Anuradha
 */
public class VersionNode extends AbstractNode {

    private NBVersionInfo nbvi;
    private boolean hasJavadoc;
    private boolean hasSources;



    /** Creates a new instance of VersionNode */
    public VersionNode(NBVersionInfo versionInfo, boolean javadoc, boolean source) {
        super(Children.LEAF);
 
        hasJavadoc = javadoc;
        hasSources = source;
        this.nbvi = versionInfo;
        
            setName(versionInfo.getVersion());
            setDisplayName(versionInfo.getVersion() + " [ " + versionInfo.getType() 
                    + (versionInfo.getClassifier() != null ? ("," + versionInfo.getClassifier()) : "") + " ] "
                    + " - "+versionInfo.getRepoId()
                    
                    );
        
        setIconBaseWithExtension("org/netbeans/modules/maven/hints/DependencyJar.gif"); //NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
      
        return new Action[0];
    }

    @Override
    public java.awt.Image getIcon(int param) {
        java.awt.Image retValue = super.getIcon(param);
        if (hasJavadoc) {
            retValue = ImageUtilities.mergeImages(retValue,
                    ImageUtilities.loadImage("org/netbeans/modules/maven/hints/DependencyJavadocIncluded.png"),//NOI18N
                    12, 12);
        }
        if (hasSources) {
            retValue = ImageUtilities.mergeImages(retValue,
                    ImageUtilities.loadImage("org/netbeans/modules/maven/hints/DependencySrcIncluded.png"),//NOI18N
                    12, 8);
        }
        return retValue;

    }

    public NBVersionInfo getNBVersionInfo() {
        return nbvi;
    }

    @Override
    public String getShortDescription() {
        
        return nbvi.toString();
    }
}

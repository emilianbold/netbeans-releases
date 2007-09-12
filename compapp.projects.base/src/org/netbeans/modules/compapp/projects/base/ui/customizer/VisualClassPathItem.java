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


package org.netbeans.modules.compapp.projects.base.ui.customizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.Utilities;

import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;

/** Represents classpath items of various types. Can be used in the model
 * of classpath editing controls.
 *
 * @author  phrebejk
 */
public class VisualClassPathItem {

    // Types of the classpath elements
    public static final int TYPE_JAR = 0;
    public static final int TYPE_LIBRARY = 1; // XXX Not used yet
    public static final int TYPE_ARTIFACT = 2;
    public static final int TYPE_CLASSPATH = 3; // XXX Not used yet

    private static String RESOURCE_ICON_JAR = "org/netbeans/modules/compapp/projects/base/ui/resources/jar.gif"; //NOI18N
    private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/compapp/projects/base/ui/resources/libraries.gif"; //NOI18N
    private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/compapp/projects/base/ui/resources/projectDependencies.gif"; //NOI18N
    private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/compapp/projects/base/ui/resources/j2seProject.gif"; //NOI18N

    private static Icon ICON_JAR = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_JAR ) );
    private static Icon ICON_LIBRARY = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_LIBRARY ) );
    private static Icon ICON_ARTIFACT  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_ARTIFACT ) );
    private static Icon ICON_CLASSPATH  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_CLASSPATH ) );


    private int type;
    private Object cpElement;
    private String raw;
    private String eval;
    private boolean inDeployment;

    private String shortName;
    private String projectName;
    private String asaType;

    public VisualClassPathItem( Object cpElement, int type, String raw, String eval, boolean inDeployment) {
        assert cpElement != null;
        String src;
        this.cpElement = cpElement;
        this.type = type;
        this.raw = raw;
        this.eval = eval;
        this.inDeployment = inDeployment;

        if (cpElement instanceof AntArtifact) {
            AntArtifact aa = (AntArtifact) cpElement;

            Project p = aa.getProject();
            ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
            if (info != null) {
               projectName = info.getName();
               AntProjectHelper ah = p.getLookup().lookup(AntProjectHelper.class);
               if (ah != null) {
                   src = ah.getStandardPropertyEvaluator ().getProperty (IcanproProjectProperties.SRC_DIR);
               }
               else {
                   src= (aa.getArtifactLocations()[0]).toString(); //TBD temp test
               }
               shortName = projectName+"  ("+info.getProject().getProjectDirectory().getPath()+")";
            }

            // extract the JBI component type info
            String aType = aa.getType();
            int idx = aType.indexOf(':');
            if (idx > 0) {
                asaType = aType.substring(idx+1);
            }
        }

    }

    public Object getObject() {
        return cpElement;
    }

    public int getType() {
        return type;
    }

    public String getRaw() {
        return raw;
    }

    public Boolean isInDeployment() {
        return Boolean.valueOf(inDeployment);
    }

    public void setInDeployment(Boolean inDeployment) {
        this.inDeployment = inDeployment.booleanValue();
    }

    public String getEvaluated() {
        return eval == null ? getRaw() : eval;
    }

    public boolean canDelete() {
        return getType() != TYPE_CLASSPATH;
    }

    public Icon getIcon() {

        switch( getType() ) {
            case TYPE_JAR:
                return ICON_JAR;
            case TYPE_LIBRARY:
                return ICON_LIBRARY;
            case TYPE_ARTIFACT:
                return ICON_ARTIFACT;
            case TYPE_CLASSPATH:
                return ICON_CLASSPATH;
            default:
                return null;
        }

    }

    private URI getArtifactLocation(AntArtifact artifact) {
        URI[] us = artifact.getArtifactLocations();
        if ((us != null) && (us.length > 0)) {
            return us[0];
        }
        return null;
    }

    @Override
    public int hashCode() {

        int hash = getType();

        switch ( getType() ) {
            case TYPE_ARTIFACT:
                AntArtifact aa = (AntArtifact)getObject();

                hash += aa.getType().hashCode();
                hash += aa.getScriptLocation().hashCode();
                hash += getArtifactLocation(aa).hashCode();
                break;
            default:
                hash += getObject().hashCode();
                break;
        }

        return hash;
    }

    @Override
    public boolean equals( Object object ) {

        if ( !( object instanceof VisualClassPathItem ) ) {
            return false;
        }
        VisualClassPathItem vcpi = (VisualClassPathItem)object;

        if ( getType() != vcpi.getType() ) {
            return false;
        }

        switch ( getType() ) {
            case TYPE_ARTIFACT:
                AntArtifact aa1 = (AntArtifact)getObject();
                AntArtifact aa2 = (AntArtifact)vcpi.getObject();

                if ( aa1.getType() != aa2.getType() ) {
                    return false;
                }

                if ( !aa1.getScriptLocation().equals( aa2.getScriptLocation() ) ) {
                    return false;
                }

                if ( !getArtifactLocation(aa1).equals( getArtifactLocation(aa2) ) ) {
                    return false;
                }

                return true;
            default:
                return getObject().equals( vcpi.getObject() );
        }

    }

    @Override
    public String toString() {
        switch ( getType() ) {
            case TYPE_JAR:
                return getEvaluated();
            case TYPE_LIBRARY:
                return ((Library)this.getObject()).getDisplayName();
            case TYPE_ARTIFACT:
                return shortName; // getEvaluated();
            case TYPE_CLASSPATH:
                return getEvaluated();
            default:
                assert true : "Unknown item type"; // NOI18N
                return getEvaluated();
        }
    }
}
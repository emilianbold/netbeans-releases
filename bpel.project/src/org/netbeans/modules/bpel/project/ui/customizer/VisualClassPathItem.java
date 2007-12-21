/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.project.ui.customizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.*;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.Utilities;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;

import java.net.URI;

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

    private static String RESOURCE_ICON_JAR = "org/netbeans/modules/bpel/project/ui/resources/jar.gif"; //NOI18N
    private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/bpel/project/ui/resources/libraries.gif"; //NOI18N
    private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/bpel/project/ui/resources/projectDependencies.gif"; //NOI18N
    private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/bpel/project/ui/resources/j2seProject.gif"; //NOI18N

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
        String src;
        this.cpElement = cpElement;
        this.type = type;
        this.raw = raw;
        this.eval = eval;
        this.inDeployment = inDeployment;

        if (cpElement instanceof AntArtifact) {
            AntArtifact aa = (AntArtifact) cpElement;

            Project p = aa.getProject();
            ProjectInformation info = (ProjectInformation) p.getLookup().lookup(ProjectInformation.class);
            if (info != null) {
               projectName = info.getName();
               AntProjectHelper ah = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
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
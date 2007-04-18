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

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import java.util.Iterator;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;

import org.openide.util.Utilities;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * Represents classpath items of various types. Can be used in the model of classpath editing
 * controls.
 *
 * @author phrebejk
 */
public class VisualClassPathItem {
    // Types of the classpath elements
    
    /**
     * DOCUMENT ME!
     */
    public static final int TYPE_JAR = 0;
    
    /**
     * DOCUMENT ME!
     */
    public static final int TYPE_LIBRARY = 1; // XXX Not used yet
    
    /**
     * DOCUMENT ME!
     */
    public static final int TYPE_ARTIFACT = 2;
    
    /**
     * DOCUMENT ME!
     */
    public static final int TYPE_CLASSPATH = 3; // XXX Not used yet
    
    private static String RESOURCE_ICON_JAR = "org/netbeans/modules/compapp/projects/jbi/ui/resources/jar.gif"; // NOI18N
    private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/compapp/projects/jbi/ui/resources/libraries.gif"; // NOI18N
    private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/compapp/projects/jbi/ui/resources/projectDependencies.gif"; // NOI18N
    private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/compapp/projects/jbi/ui/resources/j2seProject.gif"; // NOI18N
    private static Icon ICON_JAR = new ImageIcon(Utilities.loadImage(RESOURCE_ICON_JAR));
    private static Icon ICON_LIBRARY = new ImageIcon(Utilities.loadImage(RESOURCE_ICON_LIBRARY));
    private static Icon ICON_ARTIFACT = new ImageIcon(Utilities.loadImage(RESOURCE_ICON_ARTIFACT));
    private static Icon ICON_CLASSPATH = new ImageIcon(
            Utilities.loadImage(RESOURCE_ICON_CLASSPATH)
            );
    
    private int type;
    private Object cpElement;
    private String raw;
    private String eval;
    private String shortName;
    private String projectName;
    private boolean inDeployment;
    
    // ASA inforamtion
    private String asaType;
    private String asaAlias;
    private String asaUUID;
    private String asaDescription;
    private String asaTarget;
    private Icon projIcon;
    
    /**
     * Creates a new VisualClassPathItem object.
     *
     * @param cpElement DOCUMENT ME!
     * @param type DOCUMENT ME!
     * @param raw DOCUMENT ME!
     * @param eval DOCUMENT ME!
     * @param inDeployment DOCUMENT ME!
     */
    public VisualClassPathItem(
            Object cpElement, int type, String raw, String eval, boolean inDeployment
            ) {
        this.cpElement = cpElement;
        this.type = type;
        this.raw = raw;
        this.eval = eval;
        this.inDeployment = inDeployment;
        this.shortName = (eval != null) ? eval : raw;
        this.projectName = ""; // NOI18N
        this.asaType = ""; // NOI18N
        
        if (cpElement instanceof AntArtifact) {
            AntArtifact aa = (AntArtifact) cpElement;
            
            ProjectInformation info = (ProjectInformation) aa.getProject().getLookup().lookup(
                    ProjectInformation.class
                    );
            
            if (info != null) {
                projectName = info.getName();   // e.x., SynchronousSample
                
//                if (eval != null) {
//                    int i = eval.lastIndexOf('/');
//
//                    if (i > 0) {
//                        shortName = projectName + "@" + eval.substring(i + 1);
//                    }
//                }
                shortName = projectName + ".jar"; // NOI18N
                this.projIcon = info.getIcon();
            }
            
            // extract the JBI component type info
            String aType = aa.getType();
            int idx = aType.indexOf(':');
            if (idx > 0) {
                asaType = aType.substring(idx + 1); // e.x., com.sun.bpelse
            } else {
                if (isJavaEEProjectAntArtifact(aa)){
                    asaType = JbiProjectConstants.JAVA_EE_SE_COMPONENT_NAME;
                }
            }
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param artifact DOCUMENT ME!
     * @param pathInWar DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static VisualClassPathItem create(AntArtifact artifact, String pathInWar) {
        return new VisualClassPathItem(
                artifact, VisualClassPathItem.TYPE_ARTIFACT,
                
                //null,
                artifact.getArtifactLocations()[0].toString(), pathInWar, false
                );
    }
    
    public static boolean isJavaEEProjectAntArtifact(AntArtifact aa){
        Project project = aa.getProject();
         if ( project != null ) {
            AntArtifactProvider prov = (AntArtifactProvider)project.getLookup().lookup(AntArtifactProvider.class);
            if (prov != null) {
                AntArtifact[] artifacts = prov.getBuildArtifacts();
                Iterator<String> artifactTypeItr = null;
                String artifactType = null;
                if (artifacts != null) {
                    for (int i = 0; i < artifacts.length; i++) {
                        artifactTypeItr = JbiProjectConstants.JAVA_EE_AA_TYPES.iterator();
                        while (artifactTypeItr.hasNext()){
                            artifactType = artifactTypeItr.next();
                            if (artifacts[i].getType().startsWith(artifactType)) {
                                return true;
                            }
                        }
                    }
                }
            }
         }
        return false;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object getObject() {
        return cpElement;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getType() {
        return type;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getRaw() {
        return raw;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getProjectName() {
        return projectName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAsaType() {
        return asaType;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getShortName() {
        return shortName;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Boolean isInDeployment() {
        return Boolean.valueOf(inDeployment);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param inDeployment DOCUMENT ME!
     */
    public void setInDeployment(Boolean inDeployment) {
        this.inDeployment = inDeployment.booleanValue();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getEvaluated() {
        return (eval == null) ? getRaw() : eval;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean canDelete() {
        return getType() != TYPE_CLASSPATH;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Icon getIcon() {
        switch (getType()) {
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
    
    
    public Icon getProjectIcon(){
        return this.projIcon;
    }
    
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int hashCode() {
        int hash = getType();
        
        switch (getType()) {
            case TYPE_ARTIFACT:
                if (getObject() != null) {
                    AntArtifact aa = (AntArtifact) getObject();
                    
                    hash += aa.getType().hashCode();
                    hash += aa.getScriptLocation().hashCode();
                    hash += aa.getArtifactLocations()[0].hashCode();
                } else {
                    hash += getRaw().hashCode();
                }
                break;
            default:
                if (getObject() != null) {
                    hash += getObject().hashCode();
                } else {
                    hash += getRaw().hashCode();
                }
                break;
        }
        
        return hash;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param object DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(Object object) {
        if (!(object instanceof VisualClassPathItem)) {
            return false;
        }
        
        VisualClassPathItem vcpi = (VisualClassPathItem) object;
        
        if (getType() != vcpi.getType()) {
            return false;
        }
        
        switch (getType()) {
            case TYPE_ARTIFACT:
                
                AntArtifact aa1 = (AntArtifact) getObject();
                AntArtifact aa2 = (AntArtifact) vcpi.getObject();

                if ((aa1 == null) && (aa2 == null)){
                    return true;
                }
                
                if ((aa1 == null) && (aa2 != null)){
                    return false;
                }

                if ((aa1 != null) && (aa2 == null)){
                    return false;
                }
                
                if (aa1.getType() != aa2.getType()) {
                    return false;
                }
                
                if (!aa1.getScriptLocation().equals(aa2.getScriptLocation())) {
                    return false;
                }
                
                if (!aa1.getArtifactLocations()[0].equals(aa2.getArtifactLocations()[0])) {
                    return false;
                }
                
                return true;
                
            default:
                return getObject().equals(vcpi.getObject());
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        switch (getType()) {
            case TYPE_JAR:
                return getEvaluated();
                
            case TYPE_LIBRARY:
                return ((Library) this.getObject()).getDisplayName();
                
            case TYPE_ARTIFACT:
                return shortName; // getEvaluated();
                
            case TYPE_CLASSPATH:
                return getEvaluated();
                
            default:
                assert true : "Unknown item type"; // NOI18N
                
                return getEvaluated();
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAsaAlias() {
        return asaAlias;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param asaAlias DOCUMENT ME!
     */
    public void setAsaAlias(String asaAlias) {
        this.asaAlias = asaAlias;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAsaUUID() {
        return asaUUID;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param asaUUID DOCUMENT ME!
     */
    public void setAsaUUID(String asaUUID) {
        this.asaUUID = asaUUID;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAsaDescription() {
        return asaDescription;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param asaDescription DOCUMENT ME!
     */
    public void setAsaDescription(String asaDescription) {
        this.asaDescription = asaDescription;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAsaTarget() {
        return asaTarget;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param asaTarget DOCUMENT ME!
     */
    public void setAsaTarget(String asaTarget) {
        this.asaTarget = asaTarget;
    }
}

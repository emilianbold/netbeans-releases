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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.jaxb.ui;

import java.awt.Image;
import java.beans.IntrospectionException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.actions.JAXBRefreshAction;
import org.netbeans.modules.xml.jaxb.actions.JAXBWizardOpenXSDIntoEditorAction;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizardSchemaNode.JAXBWizardSchemaNodeChildren;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author gpatil
 */
public class JAXBBindingSupportFileNode extends AbstractNode {
    public static final String ORIG_LOCATION = "origLocation" ; // No I18N
    public static final String ORIG_LOCATION_TYPE = "orginLocationType"; //NOI18N
    public static final String LOC_SCHEMA_ROOT = "localSchemaRoot"; //NOI18N
    
    private static Action[] actions = null;    
    private FileObject xsdFileObject;
    private String origLocation ;
    private Boolean origLocationType ;
    private FileObject localSchemaRoot;
        
    public JAXBBindingSupportFileNode(Project prj, FileObject xsdFile, FileObject locSchemaRoot, Boolean origLocType, String origLocation) throws IntrospectionException {
        super( Children.LEAF, createLookup( xsdFile, prj ) );
        this.xsdFileObject = xsdFile;
        this.origLocation = origLocation;
        this.origLocationType = origLocType;
        this.localSchemaRoot = locSchemaRoot;
        
        this.setValue(ORIG_LOCATION, this.origLocation);
        this.setValue(ORIG_LOCATION_TYPE, this.origLocationType);
        this.setValue(LOC_SCHEMA_ROOT, this.localSchemaRoot);
        
        xsdFileObject.addFileChangeListener( new FileChangeListener() {
            public void fileAttributeChanged(
                    FileAttributeEvent fileAttributeEvent) {
            }
            
            public void fileChanged(
                    FileEvent fileAttributeEvent) {
            }
            
            public void fileDataCreated(
                    FileEvent fileAttributeEvent) {
            }
            
            public void fileDeleted(
                    FileEvent fileAttributeEvent) {
                JAXBWizardSchemaNodeChildren xsdChildren =
                        (JAXBWizardSchemaNodeChildren)JAXBBindingSupportFileNode.
                        this.getParentNode().getChildren();
                xsdChildren.refreshChildren();
                JAXBBindingSupportFileNode.this.fireNodeDestroyed();
            }
            
            public void fileFolderCreated(FileEvent fe) {
            }
            
            public void fileRenamed(FileRenameEvent frenameEvent) {
            }
        } );
        initActions();
        this.setShortDescription(xsdFile.getPath());
    }
    
    private static Lookup createLookup(FileObject xsdFileObject, 
                                        Project prj) {
        return Lookups.fixed( new Object[] {
            xsdFileObject,
            prj
        } );
    }

    public String getName() {
        return xsdFileObject.getName();
    }
    
    public String getDisplayName() {
        return xsdFileObject.getNameExt();
    }
    
    private void initActions() {
        if ( actions == null ) {
            actions = new Action[] {
                SystemAction.get(JAXBWizardOpenXSDIntoEditorAction.class),
                SystemAction.get(JAXBRefreshAction.class)
            };
        }
    }
    
    public Action[] getActions(boolean b) {
        return actions;
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(JAXBWizardOpenXSDIntoEditorAction.class);
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage( 
                "org/netbeans/modules/xml/jaxb/resources/xmlObject.gif" ); // No I18N
    }
    
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage( 
                "org/netbeans/modules/xml/jaxb/resources/xmlObject.gif" ); // No I18N  
    }
 }
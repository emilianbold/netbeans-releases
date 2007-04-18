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

package org.netbeans.modules.compapp.javaee.sunresources.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.modules.compapp.javaee.sunresources.ResourceAggregator;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;
import org.netbeans.modules.compapp.javaee.sunresources.actions.WidgetMoveProvider;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.Resources;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.ApplicationArchive;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.Archive;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.ArchiveConstants;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.EJBArchive;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.WebArchive;
import org.netbeans.modules.compapp.javaee.sunresources.tool.graph.CMapScene;
import org.netbeans.modules.compapp.javaee.sunresources.tool.graph.VisualUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author echou
 */
public class JavaEETool {
    
    private ArchiveConstants.ArchiveType type;
    private Project p;
    private Archive archive;
    private CMapScene scene;
    private boolean valid = true;
    
    public JavaEETool(File directory) throws Exception {
        
    }
    
    public JavaEETool(ArchiveConstants.ArchiveType type, Project p) throws Exception {
        this.type = type;
        this.p = p;
        
        if (type == ArchiveConstants.ArchiveType.EAR) {
            archive = new ApplicationArchive(p);
        } else if (type == ArchiveConstants.ArchiveType.EJB) {
            archive = new EJBArchive(p);
        } else if (type == ArchiveConstants.ArchiveType.WAR) {
            archive = new WebArchive(p);
        } else {
            throw new Exception(
                    NbBundle.getMessage(JavaEETool.class, "EXC_unknown_archive", type.toString())
                    );
        }
        archive.open();
        scene = VisualUtil.constructCMapScene(archive);
        
        // hover action
        scene.registerNodeWidgetAction(scene.createWidgetHoverAction());
        
        // move action
        WidgetAction moveAction =
            ActionFactory.createMoveAction(null, new WidgetMoveProvider(archive.getJAXBHandler()));
        scene.registerNodeWidgetAction(moveAction);
    }
    
    public void close() throws Exception {
        archive.close();
        archive = null;
        valid = false;
    }
    
    public ResourceAggregator getResourceAggregator() {
        isValid();
        return archive.getResourceAggregator();
    }
    
    public FileObject getResourceDir() {
        isValid();
        return archive.getResourceDir();
    }
    
    public JComponent getGraphView() {
        isValid();
        JComponent sceneView = scene.getView();
        if (sceneView == null) {
            sceneView = scene.createView ();
        }
        return sceneView;
    }
    
    private void isValid() {
        if (!valid) {
            throw new IllegalStateException(
                    NbBundle.getMessage(JavaEETool.class, "EXC_already_closed")
                    );
        }
    }
    
    public static void main(String[] args) {
        //File ear = new File("C:/nbprojects/EnterpriseApplication1/dist/EnterpriseApplication1.ear");
        //File ear = new File("C:/nbprojects/EnterpriseApplication14/dist/EnterpriseApplication14.ear");
        File ear = new File("C:/temp/EnterpriseApplication4.ear");
        File tmpDir = new File("C:/temp");
        //File xml = new File("C:/testcode/wasilla/graph3.xml");
        
        try {
            /*
            File appPath = FileUtil.explode(ear, tmpDir);
            ApplicationArchive archive = new ApplicationArchive(appPath);
            archive.open();
            
            System.out.println("archive = " + archive);
            System.out.println("cmap=" + archive.getCMap());
            
            CMapScene scene = VisualUtil.constructCMapScene(archive);
            
            // hover action
            scene.registerNodeWidgetAction(scene.createWidgetHoverAction());
            
            // move action
            WidgetAction moveAction =
                ActionFactory.createMoveAction(null, new WidgetMoveProvider(archive.getJAXBHandler()));
            scene.registerNodeWidgetAction(moveAction);
            
            
            
            SceneSupport.show (scene, VisualUtil.MAX_WIDTH, VisualUtil.MAX_HEIGHT, 
                    archive, ear);
            */
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

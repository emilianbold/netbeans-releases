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
package org.netbeans.modules.uml.drawingarea.persistence;

import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.netbeans.modules.uml.drawingarea.widgets.SubWidget;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jyothi
 */
public class PersistenceManager {

    NodeWriter nodeWriter;
    FileObject fileObject;
    
    private OutputStreamWriter osw = null;

    public PersistenceManager() {
        //do we need to do something here?
    }
    
    public PersistenceManager(FileObject fo) {
        this.fileObject = fo;
        nodeWriter = new NodeWriter(getWriter());
    }

    public synchronized void saveDiagram(DesignerScene scene) {
        long start = System.currentTimeMillis();
        if (scene != null) {
            //write the scene info first
            try
            {
                if (scene instanceof DiagramNodeWriter) {
                    ((DiagramNodeWriter) scene).save(nodeWriter);
                }

                //now  write the elements..
                for (IPresentationElement elt : scene.getNodes()) {
                    NodeWriter nWriter = new NodeWriter(getWriter());
                    Widget nWidget = scene.findWidget(elt);
                    if (nWidget instanceof DiagramNodeWriter) {
                        if (hasContainerWidgetAsParent(nWidget) || nWidget instanceof SubWidget) {
                        //do nothing..
                        // do not write now.. this node will be written as a part of its container
//                            System.out.println(" Parent is a container widget ");
                        } else {
                            ((DiagramNodeWriter) nWidget).save(nWriter);
                        }
                    }
                }
                for (IPresentationElement presElt : scene.getEdges()) {
                    EdgeWriter edgeWriter = new EdgeWriter(getWriter());
                    Widget eWidget = scene.findWidget(presElt);
                    if (eWidget instanceof DiagramEdgeWriter) {
                        ((DiagramEdgeWriter) eWidget).save(edgeWriter);
                    }

                }
                //end the scene tags
                nodeWriter.endScene(getWriter());
            }
            finally
            {
                closeWriter(getWriter());
            }
            //clear the anchor map
            PersistenceUtil.clearAnchorMap();
        }
//        float elapsedTimeSec = (System.currentTimeMillis() - start) / 1000F;
//        System.err.println(" !!!!!!!!!!!!!!!!!!!! Total time to SAVE the diagram : " +elapsedTimeSec);
    }
    
    private boolean hasContainerWidgetAsParent(Widget widget) {
        boolean retVal = false;
        Widget parent;
        Widget child = widget;
        if ((child != null) && !(child instanceof Scene))
        {
            while (true) 
            {
                parent = child.getParentWidget();                
                if (parent instanceof Scene)                 
                {
                    return false;
                }
                else if (parent instanceof ContainerWidget)
                {
                    return true;
                }
                else
                {
                    child = parent;
                }
            }           
        }        
        return retVal;
    }

    public BufferedWriter getWriter() {
        BufferedWriter bw = null;
        try
        {
            if (nodeWriter == null)
            {
                if (fileObject != null)
                {
                    if (fileObject.isLocked())
                    {
                        fileObject.lock().releaseLock();
                    }                    
                    osw = new OutputStreamWriter(fileObject.getOutputStream(), "UTF-8");
                    bw = new BufferedWriter(osw);
                }
            }
            else
            {
                bw = nodeWriter.getBufferedWriter();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return bw;
    }

    private void closeWriter(BufferedWriter bw) {
        try {
            if (bw != null) {
                bw.flush();
                bw.close();
            }
            if (osw != null) {
                osw.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public DesignerScene loadDiagram(String fileName, 
                                     UMLDiagramTopComponent topComp, 
                                     boolean groupEdges) {
        DesignerScene scene = null;
        try
        {
            DiagramLoader diagLoader = new DiagramLoader(fileName, topComp, groupEdges);
//            long start = System.currentTimeMillis();
            //set to diagramLoading mode
            PersistenceUtil.setDiagramLoading(true);
            scene = diagLoader.openDiagram();
            //done with diagramLoading.. unset the mode
            PersistenceUtil.setDiagramLoading(false);
//            float elapsedTimeSec = (System.currentTimeMillis() - start) / 1000F;
//            System.err.println(" !!!!!!!!!!!!!!!!!!!! Total time to load the diagram : " + elapsedTimeSec);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            PersistenceUtil.setDiagramLoading(false);
        }
        return scene;
    }
}

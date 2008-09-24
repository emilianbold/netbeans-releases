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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.SeparatorWidget.Orientation;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWithCompartments;
import org.openide.util.Exceptions;

/**
 *
 * @author sp153251
 */
public class LoadSubPartitionsProvider implements ActionProvider{
    private UMLNodeWidget partition;
    private Orientation orientation;
    private ArrayList<String> offsets;
    private IActivityPartition ap;

    public LoadSubPartitionsProvider(UMLNodeWidget partition,IActivityPartition ap,SeparatorWidget.Orientation orientation,ArrayList<String> offsets)
    {
        this.partition=partition;
        this.orientation=orientation;
        this.offsets=offsets;
        this.ap=ap;
    }
    
    public void perfomeAction() {
        ETList<IActivityPartition> subpartitions=ap.getSubPartitions();
        if(subpartitions!=null && subpartitions.size()>1)
        {
            DesignerScene scene=(DesignerScene) partition.getScene();
            offsets.add(0, "0");
            int totalInitialWidth=0;
            ArrayList<NodeInfo> nis=new ArrayList<NodeInfo>();
            for(int i=0;i<subpartitions.size();i++)
            {
                Widget subW=null;
                IActivityPartition subEl=subpartitions.get(i);
                for(IPresentationElement subPE:subEl.getPresentationElements())
                {
                    Widget tmp=scene.findWidget(subPE);
                    if(tmp!=null)//may be on another diagram
                        for(Widget par=tmp.getParentWidget();par!=null;par=par.getParentWidget())
                        {
                            if(par==partition)
                            {
                                subW=tmp;
                                break;
                            }
                        }
                    if(subW!=null)break;
                }
                if(subW!=null)
                {
                    Dimension size=subW.getBounds().getSize();
                    if(orientation==orientation.VERTICAL)
                    {
                        totalInitialWidth+=size.height;
                        int bottom=Integer.parseInt(offsets.get(i));
                        int up=totalInitialWidth;
                        if((i+1)<offsets.size())
                        {
                            up=Integer.parseInt(offsets.get(i+1));
                        }
                        size.height=up-bottom;
                    }
                    else///
                    {
                        totalInitialWidth+=size.width;
                        int bottom=Integer.parseInt(offsets.get(i));
                        int up=totalInitialWidth;
                        if((i+1)<offsets.size())
                        {
                            up=Integer.parseInt(offsets.get(i+1));
                        }
                        size.width=up-bottom;
                    }
                    NodeInfo ni=new NodeInfo();
                    ni.setModelElement(subEl);
                    ni.setSize(size);
                    nis.add(ni);
                }
            }
            for(int i=0;i<nis.size();i++)
            {
                partition.load(nis.get(i));
            }
        }
        new Thread()
        {
            @Override
            public void run()
            {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        public void run() {
                            ContainerWithCompartments cont = (ContainerWithCompartments) partition;
                            cont.addChildrenInBounds();
                            //
                            final DesignerScene scene=(DesignerScene) partition.getScene();
                            scene.getDiagram().setDirty(true);
                                new Thread()
                                        //we can get perfomance regression with saving after each conteiner resolving, but it's better then have incinsistent diagram state.
                                        //on other side it looks hen most common scenario include one or at max several of partitions, so in most cases regression shouldn't be signinficant
                                        //TBD. it's good to find a better way and save only once if everal partitions exist.
                                {
                                    @Override
                                    public void run()
                                    {
                                        try {
                                            scene.getDiagram().save();
                                        } catch (IOException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                }.start();
                         }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }.start();
    }

}

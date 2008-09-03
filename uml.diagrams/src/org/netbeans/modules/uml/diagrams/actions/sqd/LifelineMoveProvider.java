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

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.awt.Point;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.SQDDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.ui.trackbar.JTrackBar;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 * lifeline provider which handle movement by external provider and additionally handle cars on track bar
 * @author sp153251
 */
public class LifelineMoveProvider implements MoveProvider {

                private JTrackBar tb;
                private MoveProvider provider;
                
                public LifelineMoveProvider(MoveProvider baseProvider)
                {
                    provider=baseProvider;
                }
                
                public void movementStarted(Widget widget) {
                    provider.movementStarted(widget);
                }

                public void movementFinished(Widget widget) {
                    provider.movementFinished(widget);
                   DesignerScene scene=((DesignerScene) widget.getScene());
                    if(tb==null)
                    {
                        tb=((SQDDiagramTopComponent) scene.getEngine().getTopComponent()).getTrackBar();
                    }
                    IPresentationElement el=(IPresentationElement) scene.findObject(widget);
                    tb.moveObject(el);
                }

                public Point getOriginalLocation(Widget widget) {
                    return provider.getOriginalLocation(widget);
                }
                
                public void setNewLocation(Widget widget, Point location) {
                    provider.setNewLocation(widget, location);
                 }
            };
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetKeyEvent;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Sheryl Su
 */
public class WidgetAcceptAction extends WidgetAction.Adapter
{

    private AcceptProvider provider;

    public WidgetAcceptAction(AcceptProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public State mousePressed(Widget widget, WidgetMouseEvent event)
    {
        State retVal = WidgetAction.State.REJECTED;

        DesignerScene scene = null;
        if (widget.getScene() instanceof DesignerScene)
        {
            scene = (DesignerScene) widget.getScene();
        }
        
        String paletteTool = DesignerTools.PALETTE;

        if (event.getButton() == MouseEvent.BUTTON1 && scene.getActiveTool().equals(paletteTool))
        {
            Transferable transferrable = CopyPasteSupport.getTransferable();

            ConnectorState acceptable = provider.isAcceptable(widget, event.getPoint(), transferrable);

            if (acceptable == ConnectorState.ACCEPT)
            {
                provider.accept(widget, event.getPoint(), transferrable);
                retVal = State.CONSUMED;
            } else if (acceptable == ConnectorState.REJECT_AND_STOP)
            {
                retVal = State.CONSUMED;
            }
        }

        int onmask = InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;

        // If the shift key is pressed we are doing a multi-drop
        if (!((event.getModifiersEx() & onmask) == onmask))
        {
            clear(scene);
        }

        return retVal;
    }

    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event)
    {
        State retVal = WidgetAction.State.REJECTED;

        if (event.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            DesignerScene scene = null;
            if (widget.getScene() instanceof DesignerScene)
            {
                scene = (DesignerScene) widget.getScene();
                clear(scene);
            }
        }

        return retVal;
    }

    private void clear(DesignerScene scene)
    {
        clearPalette(scene);
        clearClipBoard();
        scene.removeBackgroundWidget();
    }

    private void clearPalette(DesignerScene scene)
    {
        if (scene != null)
        {
            TopComponent topC = scene.getTopComponent();
            PaletteController controller = topC.getLookup().lookup(PaletteController.class);
            if (controller != null)
            {
                controller.setSelectedItem(Lookup.EMPTY, Lookup.EMPTY);
            }
        }
    }

    private void clearClipBoard()
    {
        Clipboard clipboard = CopyPasteSupport.getClipboard();
        clipboard.setContents(new ADTransferable(""), new StringSelection(""));
    }


    public State drop(Widget widget, WidgetDropTargetDropEvent event)
    {
        State retVal = State.REJECTED;
        Transferable transferrable = event.getTransferable();//CopyPasteSupport.getTransferable();

        ConnectorState acceptable = provider.isAcceptable(widget, event.getPoint(), transferrable);

        if (acceptable == ConnectorState.ACCEPT)
        {
            provider.accept(widget, event.getPoint(), transferrable);
            retVal = State.CONSUMED;
        } else if (acceptable == ConnectorState.REJECT_AND_STOP)
        {
            retVal = State.CONSUMED;
        }

        clear((DesignerScene)widget.getScene());
        return retVal;
    }
}

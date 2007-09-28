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


/*
 * DrawingAreaRefreshHelper.java
 *
 * Created on May 19, 2004, 7:17 AM
 */

package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import com.tomsawyer.editor.TSEGraph;
import java.util.Collections;

import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 *
 * @author  Trey Spiva
 */
public class DrawingAreaRefreshHelper
{
    private static Map m_BusyMap = Collections.synchronizedMap( new HashMap());
    
    /** Creates a new instance of DrawingAreaRefreshHelper */
    private DrawingAreaRefreshHelper()
    {
    }
    
    public static void refreshDrawingArea(final IDrawingAreaControl ctrl, final boolean redrawAll)
    {
        if (ctrl ==  null)
            return;
        
        // update the queue with the new request to refresh the drawing area
        if (m_BusyMap.containsKey(ctrl))
            m_BusyMap.remove(ctrl);
        m_BusyMap.put(ctrl, new Boolean(redrawAll));
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    if (m_BusyMap.containsKey(ctrl))
                    {
                        boolean redraw = ((Boolean)m_BusyMap.get(ctrl)).booleanValue();
                        ADGraphWindow wnd = ctrl.getGraphWindow();
                        if (wnd != null)
                        {
                            if (redraw)
                            {
                                TSEGraph graph = wnd.getGraph();
                                if (graph != null && graph.getUI() != null)
                                {
                                    wnd.addInvalidRegion(RectConversions.inflate(graph.getUI().getBounds(), 10.0,10.0));
                                    wnd.updateInvalidRegions(true);
                                }
                                else
                                {
                                    wnd.invalidate();
                                }
                            }
                            else
                            {
                                wnd.updateInvalidRegions(true);
                            }
                        }
                        ctrl.updateSecondaryWindows();
                    }
                }
                catch(Exception e)
                {
                    // Here to make sure that the control gets removed from the map too avoid memory leaks and
                    // potential dead lock if an exception gets thrown.
                }
                
                // Make sure we're not in the map after this call.
                m_BusyMap.remove(ctrl);
            }
        });
        
    }
    
    public static void refreshDrawingArea(final IDrawingAreaControl ctrl)
    {
        refreshDrawingArea(ctrl, false);
    }
}

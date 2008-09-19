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

package org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.drawingarea.actions.SQDMessageConnectProvider;

/**
 *
 * @author sp153251
 */
public interface SQDDiagramEngineExtension {
    /**
     * need ability to move messages if necessary
     * @param msg
     * @param dy
     */
    public static final String SHOW_MESSAGE_NUMBERS="Show Message Numbers";//just keys, may even have less natural value (i.e. like 'sfsd224')
    public static final String SHOW_RETURN_MESSAGES="Show Return Messages";
    public static final String SHOW_INTERACTION_BOUNDARY="Show Interaction Boundary";
    //
    public static final int DEFAULT_LIFELINE_Y=60;
    public static final int DEFAULT_ACTORLIFELINE_Y=20;
        
    public void bumpMessage(ConnectionWidget msg,int dy);

    /**
     * need access for message connect provider from drawing area module
     * use second parameter for result of sycnh message if you have one and want it to be used in provider
     * @param call
     * @param result
     * @return
     */
    abstract public SQDMessageConnectProvider getConnectProvider(IMessage call,IMessage result);
    
    /**
     * layout action operates with specific widgets on sqd, so located in diagrams
     * but invication should be from drawing area sometimes
     */
    
    public void layout(boolean save);
    
    /**
     * set mode not to use trackbar in next action (do not add car)
     * need to be called and reset after each action to avoid possible bad states.
     */
    public void doNotUseTrackbar();
}

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
package org.netbeans.modules.uml.drawingarea.view;

import java.awt.Color;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.modules.uml.drawingarea.border.ResizeBorder;

/**
 *
 * @author jyothi
 */
public interface UMLWidget {

    public static final Color BORDER_HILIGHTED_COLOR = new Color(0xFFA400);
    public  static ResizeBorder NON_RESIZABLE_BORDER =
            new ResizeBorder(5, Color.BLACK,
                             new ResizeProvider.ControlPoint[]{});
    // Every WidgetType should have an ID
    // Also refer to UMLWidgetIDString.java
    public String getWidgetID();

    public enum UMLWidgetIDString {

        COMBINEDFRAGMENTWIDGET,
        LIFELINEWIDGET,
        ASSOCIATIONCONNECTORWIDGET,
        COMMENTLINKCONNECTORWIDGET,
        DERIVATIONCONNECTORWIDGET,
        GENERALIZATIONCONNECTORWIDGET,
        IMPLEMENTATIONCONNECTORWIDGET,
        ASYNCHRONOUSMESSAGECONNECTIONWIDGET,
        CREATEMESSAGECONNECTIONWIDGET,
        RESULTMESSAGECONNECTIONWIDGET,
        SYNCHRONOUSMESSAGECONNECTIONWIDGET,
        COMMENTWIDGET,
        DATATYPEWIDGET,
        DERIVATIONCLASSIFIERWIDGET,
        UMLCLASSWIDGET,
        UMLINTERFACEWIDGET,
        ELEMENTLISTWIDGET,
        ATTRIBUTEWIDGET,
        OPERATIONWIDGET,
        PACKAGEWIDGET,
        LABELWIDGET,
        INTERACTIONOPERANDCONSTRAINTWIDGET,
        EXPRESSIONWIDGET,
        NUMBEREDNAMELABELWIDGET,
        OPERATIONLABELWIDGET,
        ABSTRACTION_CONNECTION,
        DEPENDENCY_CONNECTION,
        REALIZATION_CONNECTION,
        PERMISSION_CONNECTION,
        USAGE_CONNECTION,
        INVOCATIONWIDGET,
        ACTIVITYGROUPWIDGET,
        ACTEXPRESSIONWIDGET,
        INITIALNODEWIDGET,
        FINALNODEWIDGET,
        FLOWFINALNODEWIDGET,
        FORKWIDGET,
        DATASTOREWIDGET,
        PARAMUSAGEWIDGET,
        PARTITIONWIDGET,
        SUBPARTITIONWIDGET,
        DECISIONNODEWIDGET,
        SIGNALNODEWIDGET,
        ACTIVITYEDGEWIDGET,
        NESTEDLINKCONNECTIONWIDGET,
        ASSOCIATIONCLASSCONNECTORWIDGET,
        CONNECT_TO_ASSOCIATION_CLASS_CONNECTORWIDGET,
        INTERFACEWIDGET,
        STATEWIDGET,
        STATETRANSITIONWIDGET,
        INITIALSTATEWIDGET,
        CHOICEPSEUDOSTATEWIDGET,
        DEEPHISTORYSTATEWIDGET,
        ENTRYPOINTSTATEWIDGET,
        FINALSTATEWIDGET,
        JUNCTIONSTATEWIDGET,
        SHALLOWHISTORYSTATEWIDGET,
        ABORTEDFINALSTATEWIDGET,
        FORKSTATEWIDGET,
        USECASEWIDGET,
        EXTENSIONPOINTWIDGET,
        ACTORWIDGET,
        INCLUDECONNECTOR,
        EXTENDCONNECTOR,
        DESIGNPATTERNWIDGET,
        PARTFACADEEDGECONNECTIONWIDGET,
        INTERACTIONOPERAND,
        ENUMERATION_WIDGET,
        ENUMERATION_LITERAL_WIDGET,
        ARTIFACTWIDGET,
        ALIASED_TYPEWIDGET,

    }
    
    public static enum Orientation { VERTICAL, HORIZONTAL};
    
    public void remove();
    
    public void refresh(boolean resizetocontent);
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import org.openide.nodes.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * @author Tomas Pavek
 */

public interface LayoutSupportDelegate {

    // initialization
    public void initialize(LayoutSupportContext layoutContext,
                           LayoutManager lmInstance,
                           boolean fromCode);

    // type of support
    Class getSupportedClass();
    boolean isDedicated();

    // node presentation
    boolean shouldHaveNode();
    String getDisplayName();
    Image getIcon(int type);

    // properties and customizer
    Node.PropertySet[] getPropertySets();
    Class getCustomizerClass();

    // code meta data
    CodeGroup getLayoutCode();
    CodeGroup getComponentCode(int index);
    CodeExpression getComponentCodeExpression(int index);

    // components adding/removing
    int getComponentCount();
    void addComponents(CodeExpression[] compExpressions,
                       LayoutConstraints[] constraints);
    void removeComponent(int index);
    void removeAll();

    // is something changed (in comparison with default layout)?
    boolean isLayoutChanged(Container defaultContainer,
                            Container defaultContainerDelegate);

    // managing layout constraints for components
    LayoutConstraints getConstraints(int index);
    void convertConstraints(LayoutConstraints[] previousConstraints,
                            LayoutConstraints[] currentConstraints,
                            Component[] components);

    // managing live components
    void setLayoutToContainer(Container container,
                                     Container containerDelegate);
    void addComponentsToContainer(Container container,
                                  Container containerDelegate,
                                  Component[] components,
                                  int index);
    boolean removeComponentFromContainer(Container container,
                                         Container containerDelegate,
                                         Component component,
                                         int index);
    boolean clearContainer(Container container,
                                  Container containerDelegate);

    // drag and drop support
    LayoutConstraints getNewConstraints(Container container,
                                        Container containerDelegate,
                                        Component component,
                                        int index, // ??
                                        Point posInCont,
                                        Point posInComp);
    int getNewIndex(Container container,
                    Container containerDelegate,
                    Component component,
                    int index, // ??
                    Point posInCont,
                    Point posInComp);

    boolean paintDragFeedback(Container container, 
                              Container containerDelegate,
                              Component component,
                              LayoutConstraints newConstraints,
                              int newIndex,
                              Graphics g);

    // resizing support
    int getResizableDirections(Component component, int index);
    LayoutConstraints getResizedConstraints(Component component,
                                            int index,
                                            Insets sizeChanges);

    // copying
    LayoutSupportDelegate cloneLayoutSupport(LayoutSupportContext targetContext,
                                             CodeExpression[] targetComponents);
}

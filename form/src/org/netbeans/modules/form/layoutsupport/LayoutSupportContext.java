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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import org.netbeans.modules.form.codestructure.*;

/**
 * Interface providing necessary context information for LayoutSupportDelegate
 * implementation. Its purpose is to "connect" the layout delegate with the
 * rest of the layout support infrastructure (which implements this interface).
 * LayoutSupportDelegate receives an instance of LayoutSupportContext as
 * a parameter of initialize method.
 * Besides providing information, this interface also contains two methods
 * which should be called from the layout delegate to notify the infrastructure
 * about changes: containerLayoutChanged and componentLayoutChanged.
 * Note: these calls need not be handled explicitly if the layout support
 * implementation uses FormProperty subclass for properties (instead of
 * Node.Property only).
 *
 * @author Tomas Pavek
 */

public interface LayoutSupportContext {

    /** Gets the CodeStructure object to be used for reading/creating code of
     * the container layout configuration.
     * @return main CodeStructure object holding code data
     */
    public CodeStructure getCodeStructure();

    /** Gets the code expression of the primary container (reference container
     * instance in form metadata structures).
     * @return CodeExpression of the primary container
     */
    public CodeExpression getContainerCodeExpression();

    /** Gets the code expression of the primary container delegate.
     * #return CodeEpression of primary container delegate.
     */
    public CodeExpression getContainerDelegateCodeExpression();

    /** Gets the primary container. This is the reference instance used in form
     * metadata structures.
     * @return instance of the primary container
     */
    public Container getPrimaryContainer();

    /** Gets the container delegate of the primary container.
     * @return instance of the primary container delegate
     */
    public Container getPrimaryContainerDelegate();

    /** Gets the primary component (reference instance) on given index in
     * the primary container.
     * @return component on given index in primary container.
     */
    public Component getPrimaryComponent(int index);

    /** This method should be called by the layout delegate if some change
     * requires to update the layout in the primary container completely
     * (remove components, set new layout, add components again). To be used
     * probably only in case the supported layout manager is not a bean
     * (e.g. BoxLayout).
     */
    public void updatePrimaryContainer();

    /** This method should be called by the layout delegate to notify about
     * changing a property of container layout. The infrastructure then calls
     * back the delegate's acceptContainerLayoutChange method which may
     * throw PropertyVetoException to revert the property change.
     */
    public void containerLayoutChanged(PropertyChangeEvent evt)
        throws PropertyVetoException;

    /** This method should be called by the layout delegate to notify about
     * changing a property of component layout constraint. The infrastructure
     * then  calls back the delegate's acceptComponentLayoutChange method which
     * may throw PropertyVetoException to revert the property change.
     */
    public void componentLayoutChanged(int index, PropertyChangeEvent evt)
        throws PropertyVetoException;
}

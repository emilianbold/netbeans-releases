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
package org.openide.explorer.propertysheet.editors;

import java.awt.Component;


/** Enhances standard property editor to support in-place custom editors and tagged values.
 * <strong>Use of this class is strongly discouraged</strong> - the original
 * NetBeans property sheet did not specify a strong contract for when the
 * property should be updated from an editor such as the custom inplace editor
 * this interface allows you to provide.  The new (NetBeans 4.0 and later)
 * property sheet does specify such a contract, and the InplaceEditor
 * interface exists to allow it to be fulfilled.
 * @author Jan Jancura, Ian Formanek
 * @deprecated Instead of this class, implement ExPropertyEditor and InplaceEditor.Factory.  Also
 * create an implementation of InplaceEditor for the custom inline editor.
 * In the <code>attachEnv()</code> method of your ExPropertyEditor, call
 * <code>PropertyEnv.registerInplaceEditorFactory(this)</code>.  <p><strong>
 * Before you do any of this</strong> read the prose documentation on the
 * Explorer API and be sure you cannot do what you need with an existing
 * property editor - it is very rare to actually need to provide a custom
 * editor component.
 * @see org.openide.explorer.propertysheet.InplaceEditor
 * @see org.openide.explorer.propertysheet.InplaceEditor.Factory
 * @see org.openide.explorer.propertysheet.PropertyEnv
 */
public interface EnhancedPropertyEditor extends java.beans.PropertyEditor {
    /** Get an in-place editor.
    * @return a custom property editor to be shown inside the property
    *         sheet
    */
    public Component getInPlaceCustomEditor();

    /** Test for support of in-place custom editors.
    * @return <code>true</code> if supported
    */
    public boolean hasInPlaceCustomEditor();

    /** Test for support of editing of tagged values.
    * Must also accept custom strings, otherwise you may may specify a standard property editor accepting only tagged values.
    * @return <code>true</code> if supported
    */
    public boolean supportsEditingTaggedValues();
}

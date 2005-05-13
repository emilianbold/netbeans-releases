/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.spi.multiview;

import java.awt.Image;
import org.openide.util.HelpCtx;

/** Description of multi view element. Implementations should be lightweight
 * and fast. Creating heavyweight {@link org.netbeans.core.spi.multiview.MultiViewElement} instances asociated with
 * Swing visual representation should be done lazily in {@link #createElement} methods.
 * The implementing class should be serializable. For performance reasons, 
 * don't include the element into serialization of the description. That one will be handled 
 * separately when necessary.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
public interface MultiViewDescription {
    
    /** Gets persistence type of multi view element, the TopComponent will decide
     * on it's onw persistenceType based on the sum of all it's elements.
     * {@link org.openide.windows.TopComponent#PERSISTENCE_ALWAYS} has higher priority than {@link org.openide.windows.TopComponent#PERSISTENCE_ONLY_OPENED}
     * and {@link org.openide.windows.TopComponent#PERSISTENCE_NEVER} has lowest priority.
     * The {@link org.openide.windows.TopComponent} will be stored only if at least one element requesting persistence
     * was made visible.
     */
    public int getPersistenceType();

    /** 
     * Gets localized display name of multi view element. Will be placed on the Element's toggle button.
     *@return localized display name
     */
    public String getDisplayName();
    
    /** 
     * Icon for the MultiViewDescription's multiview component. Will be shown as TopComponent's icon
     * when this element is selected.
     * @return the icon of multi view element 
     */
    public Image getIcon ();

    /** Get the help context of multi view element.
    */
    public HelpCtx getHelpCtx ();
    
    /**
     * A Description's contribution 
     * to unique {@link org.openide.windows.TopComponent}'s Id returned by <code>getID</code>. Returned value is used as starting
     * value for creating unique {@link org.openide.windows.TopComponent} ID for whole enclosing multi view
     * component.
     * Value should be preferably unique, but need not be.
     */
    public String preferredID();
    
    /** Creates and returns asociated multi view element. it is called just once during the lifecycle of the 
     * multiview component.
     * @return the multiview element associated with this description
     */
    public MultiViewElement createElement ();
    
}
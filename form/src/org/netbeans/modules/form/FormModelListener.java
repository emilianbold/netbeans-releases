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


package org.netbeans.modules.form;

/**
 *
 * @author Tran Duc Trung
 */

public interface FormModelListener extends java.util.EventListener
{
    public void formChanged(FormModelEvent e);

    public void formLoaded(FormModelEvent e);
    public void formToBeSaved(FormModelEvent e);
//    public void formToBeClosed(FormModelEvent e);

    public void containerLayoutChanged(FormModelEvent e);
    public void componentLayoutChanged(FormModelEvent e);

    public void componentAdded(FormModelEvent e);
    public void componentRemoved(FormModelEvent e);
    public void componentsReordered(FormModelEvent e);

    public void componentPropertyChanged(FormModelEvent e);
    public void syntheticPropertyChanged(FormModelEvent e);

    public void eventHandlerAdded(FormModelEvent e);
    public void eventHandlerRemoved(FormModelEvent e);
    public void eventHandlerRenamed(FormModelEvent e);
}

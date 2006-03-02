/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xam;

import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Access the underlying structure of the model.
 *
 * @author nn136682
 */

public interface ModelAccess<M extends Model> {
    
    interface NodeUpdater {
        void updateReference(Node node);
    }
    
    public void addUndoableEditListener(UndoableEditListener listener);
    public void removeUndoableEditListener(UndoableEditListener listener);
    
    public void prepareForUndoRedo();
    public void finishUndoRedo() throws IOException;
    
    public Model.State sync() throws IOException;
    
    public void flush();
    
    public boolean areSameNodes(Node n1, Node n2);
    
    /**
     * @return child element index in the children list of given parent.
     */
    public int getElementIndexOf(Node parent, Element child);
    
    public void setAttribute(Element element, String name, String value, NodeUpdater updater);
    
    public void removeAttribute(Element element, String name, NodeUpdater updater);
    
    public void appendChild(Node node, Node newChild, NodeUpdater updater);
    
    public void insertBefore(Node node, Node newChild, Node refChild, NodeUpdater updater);
    
    public void removeChild(Node node, Node child, NodeUpdater updater);
    
    public void replaceChild(Node node, Node child, Node newChild, NodeUpdater updater);
    
    public void setText(Element element, String val, NodeUpdater updater);
    
    public void setPrefix(org.w3c.dom.Element node, String prefix);

    public int findPosition(org.w3c.dom.Node node);

	public Element duplicate(Element element);
}

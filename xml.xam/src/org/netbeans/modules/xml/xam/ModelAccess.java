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

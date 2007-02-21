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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.psm;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;

/**
 *
 * @author Alexander Zgursky
 */
public class PsmEntityImpl implements PsmEntity {
    private final String myXpath;
    private final QName myQName;
    private final String myName;
    private final boolean myIsActivity;
    private final boolean myIsLoop;
    
    private PsmEntityImpl myParent;
    private PsmEntityImpl myLoopChild;
    private List<PsmEntityImpl> myChildren;
    
    /** Creates a new instance of PsmEntityImpl */
    protected PsmEntityImpl(String xpath, QName qName,
            String name, boolean isActivity, boolean isLoop)
    {
        myXpath = xpath;
        myQName = qName;
        myName = name;
        myIsActivity = isActivity;
        myIsLoop = isLoop;
    }
    
    public String getXpath() {
        return myXpath;
    }
    
    public QName getQName() {
        return myQName;
    }
    
    public String getTag() {
        return myQName.getLocalPart();
    }
    
    public String getName() {
        return myName;
    }
    
    public boolean isActivity() {
        return myIsActivity;
    }
    
    public boolean isLoop() {
        return myIsLoop;
    }
    
    public PsmEntity getParent() {
        return myParent;
    }

    public PsmEntityImpl[] getChildren() {
        if (myChildren != null) {
            return myChildren.toArray(new PsmEntityImpl[myChildren.size()]);
        } else {
            return new PsmEntityImpl[0];
        }
    }
    
    public int getChildrenCount() {
        if (myChildren != null) {
            return myChildren.size();
        } else {
            return 0;
        }
    }

    public boolean hasChildren() {
        return myChildren != null;
    }
    
    public PsmEntityImpl getLoopChild() {
        return myLoopChild;
    }
    
    
    protected void addChild(PsmEntityImpl child) {
        if (myChildren == null) {
            myChildren = new ArrayList<PsmEntityImpl>();
        }
        myChildren.add(child);
        if (myIsLoop) {
            assert myLoopChild == null : "Loops can not have more than one child";
            myLoopChild = child;
        }
        child.setParent(this);
    }

    private void setParent(PsmEntityImpl parent) {
        myParent = parent;
    }
}

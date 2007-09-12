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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.test.editor.app.core;

import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Vector;
import org.netbeans.test.editor.app.Main;
import org.netbeans.test.editor.app.core.actions.ActionRegistry;
import org.netbeans.test.editor.app.core.properties.BadPropertyNameException;
import org.netbeans.test.editor.app.core.properties.BooleanProperty;
import org.netbeans.test.editor.app.core.properties.Properties;
import org.netbeans.test.editor.app.gui.actions.TestDeleteAction;
import org.netbeans.test.editor.app.gui.actions.TreeNewType;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
/**
 *
 * @author  ehucka
 * @version
 */
public class TestSubTest extends Test {
    
    public static final String OWNLOGGER="OwnLogger";
    
    /** Creates new TestSubTest */
    public TestSubTest(int num,Logger logr) {
        this("subTest"+Integer.toString(num));
        logger=logr;
    }
    
    public TestSubTest(String name) {
        super(name);
    }
    
    public TestSubTest(Element node) {
        super(node);
        if (node.getAttribute(OWNLOGGER).equals("true")) {
            logger = new Logger(Main.frame.getEditor());
        } else {
            logger=null;
        }
    }
    
    public Element toXML(Element node) {
        super.toXML(node);
        node.setAttribute(OWNLOGGER, (logger != owner.getLogger()) ? "true" : "false");
        return node;
    }
    
    public void fromXML(Element node) throws BadPropertyNameException {
        super.fromXML(node);
        if (node.getAttribute(OWNLOGGER).equals("true")) {
            logger = new Logger(Main.frame.getEditor());
        } else {
            logger=null;
        }
    }
    
    public Properties getProperties() {
        Properties ret=super.getProperties();
        ret.put(OWNLOGGER, new BooleanProperty((logger != getOwner().getLogger())));
        return ret;
    }
    
    public Object getProperty(String name) throws BadPropertyNameException {
        if (name.compareTo(OWNLOGGER) == 0) {
            return new BooleanProperty((logger != getOwner().getLogger()));
        } else {
            return super.getProperty(name);
        }
    }
    
    public void setProperty(String name, Object value)  throws BadPropertyNameException {
        if (name.compareTo(OWNLOGGER) == 0) {
            if (((BooleanProperty)(value)).getValue()) {
                logger = new Logger(Main.frame.getEditor());
            } else {
                logger=null;
            }
        } else {
            super.setProperty(name, value);
        }
    }
    
    public String getAuthor() {
        return ((Test)owner).getAuthor();
    }
    
    public String getVersion() {
        return ((Test)owner).getVersion();
    }
    
    public void setOwnLogger(boolean b) {
        if (b && logger == owner.getLogger()) {
            logger=new Logger(Main.frame.getEditor());
            firePropertyChange(OWNLOGGER,null,b ? Boolean.TRUE : Boolean.FALSE);
        } else {
            if (!b && logger != owner.getLogger()) {
                logger=owner.getLogger();
                firePropertyChange(OWNLOGGER,null,b ? Boolean.TRUE : Boolean.FALSE);
            }
        }
    }
    
    public boolean getOwnLogger() {
        return (logger != owner.getLogger());
    }
    
    public void perform() {
        Main.log("\nSub Test: "+getName()+" starts execution.");
        isPerforming=true;
        for(int i=0;i < getChildCount();i++) {
            if (!isPerforming) break;
            if (get(i) instanceof TestCallAction) {
                ((TestCallAction)get(i)).performAndWait();
            }
        }
        isPerforming=false;
    }
    
    public boolean isPerfoming() {
        return isPerforming;
    }
    
    public void stop() {
        if (getLogger().isPerforming())
            getLogger().stopPerforming();
        isPerforming=false;
    }
    
    protected void registerNewTypes() {
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestStep.class);
        ActionRegistry.getDefault().addRegisteredNewType(getClass(), TestCallAction.class);
    }
}

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

package org.netbeans.modules.visualweb.gravy;

import java.awt.Component;
import java.io.IOException;
import org.netbeans.jemmy.ComponentChooser;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.Operator;

import org.openide.windows.*;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;

import javax.swing.*;

/**
 * Class for TopComponents.
 */
public class TopComponentOperator extends JComponentOperator {
    
    /**
     * Create TopComponentOperator with specified name in given container.
     * @param parent Container where TopComponent will be looking for.
     * @param ID Name of the TopComponent.
     */
    public TopComponentOperator(ContainerOperator parent, String ID) {
        super(parent, new TopComponentChooser(ID));
    }

    /**
     * Create TopComponentOperator with chooser in given container.
     * @param parent Container where TopComponent will be looking for.
     * @param chooser Chooser for finding.
     */
    public TopComponentOperator(ContainerOperator parent, ComponentChooser chooser) {
        super(parent, chooser);
    }
    
    /**
     * Create TopComponentOperator from given JComponent.
     * @param component JComponent from which TopComponentOperator will be created.
     */
    public TopComponentOperator(JComponent component) {
        super(component);
    }

    public static class TopComponentChooser implements ComponentChooser {
        String ID;
        public TopComponentChooser(String ID) {
            this.ID = ID;
        }
        public boolean checkComponent(Component comp) {
            return(comp instanceof TopComponent && comp != null &&
                   ((TopComponent)comp).getName() != null && 
                   ((TopComponent)comp).getName().indexOf(ID) != -1);
        }
        public String getDescription() {
            return("A TopComponent with \"" + ID + "\" ID");
        }
    }
    

    /**
     * Find TopComponent with specified name in given container with given subchooser.
     * @param cont Container where TopComponent will be looking for.
     * @param name Name of the TopComponent.
     * @param subchooser Subchooser for finding.
     * @return JComponent Found component.
     */
    protected static JComponent findTopComponent(ContainerOperator cont, String name, int index, ComponentChooser subchooser) {
        Object tc[]=TopComponent.getRegistry().getOpened().toArray();
        StringComparator comparator=cont==null?Operator.getDefaultStringComparator():cont.getComparator();
        TopComponent c;
        for (int i=0; i<tc.length; i++) {
            c=(TopComponent)tc[i];
            if (c.isShowing() && comparator.equals(c.getName(), name) && isUnder(cont, c) && (subchooser==null || subchooser.checkComponent(c))) {
                index--;
                if (index<0)
                    return c;
            }
        }
        for (int i=0; i<tc.length; i++) {
            c=(TopComponent)tc[i];
            if ((!c.isShowing()) && isParentShowing(c) && comparator.equals(c.getName(), name) && isUnder(cont, c) && (subchooser==null || subchooser.checkComponent(c))) {
                index--;
                if (index<0)
                    return c;
            }
        }
        return null;
    }

    /**
     * Check if component's parent is showing.
     * @param c Component for verification.
     * @return True if component's parent is showing.
     */
    private static boolean isParentShowing(Component c) {
        while (c!=null) {
            if (c.isShowing()) return true;
            c=c.getParent();
        }
        return false;
    }

    /**
     * Check if component is under given container.
     * @param cont Container.
     * @param c Component for verification.
     * @return True if component is under given container.
     */
    private static boolean isUnder(ContainerOperator cont, Component c) {
        if (cont==null) return true;
        Component comp=cont.getSource();
        while (comp!=c && c!=null) c=c.getParent();
        return (comp==c);
    }

    /** Closes this TopComponent instance by IDE API call and wait until
     * it is not closed. If this TopComponent is modified (e.g. editor top
     * component), question dialog is shown and you have to close it. To close
     * this TopComponent and discard possible changes use {@link #closeDiscard}
     * method.
     */
    public void close() {
        // run in a new thread because question may block further execution
        new Thread(new Runnable() {
            public void run() {
                // run in dispatch thread
                runMapping(new MapVoidAction("close") {
                    public void map() {
                        ((TopComponent)getSource()).close();
                    }
                });
            }
        }, "thread to close TopComponent").start();
        // wait to be away
        waitComponentShowing(false);
    }

    /**
     * Waits the topcomponent to be closed.
     */
    public void waitClosed() {
	getOutput().printLine("Wait topcomponent to be closed \n    : "+
			      getSource().toString());
	getOutput().printGolden("Wait topcomponent to be closed");
	waitState(new ComponentChooser() {
		public boolean checkComponent(Component comp) {
		    return(!comp.isVisible());
		}
		public String getDescription() {
		    return("Closed topcomponent");
		}
	    });
    }

    /** Saves content of this TopComponent. If it is not applicable or content
     * of TopComponent is not modified, it does nothing.
     */
    public void save() {
        // should be just one node
        org.openide.nodes.Node[] nodes = ((TopComponent)getSource()).getActivatedNodes();
        // TopComponent like Execution doesn't have any nodes associated
        if(nodes != null) {
            for(int i=0;i<nodes.length;i++) {
                SaveCookie sc = (SaveCookie)nodes[i].getCookie(SaveCookie.class);
                if(sc != null) {
                    try {
                        sc.save();
                    } catch (IOException e) {
                        throw new JemmyException("Exception while saving this TopComponent.", e);
                    }
                }
            }
        }
    }

    /** Closes this TopComponent instance by IDE API call and wait until
     * it is not closed. If this TopComponent is modified (e.g. editor top
     * component), it discards possible changes.
     * @see #close
     */
    public void closeDiscard() {
        setUnmodified();
        close();
    }

    /** Finds DataObject for the content of this TopComponent and set it
     * unmodified. Used in closeDiscard method.
     */
    private void setUnmodified() {
        // should be just one node
        org.openide.nodes.Node[] nodes = ((TopComponent)getSource()).getActivatedNodes();
        // TopComponent like Execution doesn't have any nodes associated
        if(nodes != null) {
            for(int i=0;i<nodes.length;i++) {
                DataObject dob = (DataObject)nodes[i].getCookie(DataObject.class);
                dob.setModified(false);
            }
        }
    }

    /** Waits for index-th TopComponent with given name in IDE registry.
     * It throws JemmyException when TopComponent is not find until timeout
     * expires.
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @return TopComponent instance or throws JemmyException if not found
     * @see #findTopComponent
     */
    protected static JComponent waitTopComponent(final String name, final int index) {
        return waitTopComponent(null, name, index, null);
    }

    /** Waits for index-th TopComponent with given name in IDE registry.
     * It throws JemmyException when TopComponent is not find until timeout
     * expires.
     * @param cont container where to search
     * @param name name of TopComponent
     * @param index index of TopComponent
     * @param subchooser ComponentChooser to determine exact TopComponent
     * @return TopComponent instance or throws JemmyException if not found
     * @see #findTopComponent
     */
    protected static JComponent waitTopComponent(final ContainerOperator cont, final String name, final int index, final ComponentChooser subchooser) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    return findTopComponent(cont, name, index, subchooser);
                }
                public String getDescription() {
                    return("Wait TopComponent with name="+name+
                           " index="+String.valueOf(index)+
                           (subchooser == null ? "" : " subchooser="+subchooser.getDescription())+
                           " loaded");
                }
            });
            Timeouts times = JemmyProperties.getCurrentTimeouts().cloneThis();
            times.setTimeout("Waiter.WaitingTime", times.getTimeout("ComponentOperator.WaitComponentTimeout"));
            waiter.setTimeouts(times);
            waiter.setOutput(JemmyProperties.getCurrentOutput());
            return((JComponent)waiter.waitAction(null));
        } catch(InterruptedException e) {
            return(null);
        }
    }


}

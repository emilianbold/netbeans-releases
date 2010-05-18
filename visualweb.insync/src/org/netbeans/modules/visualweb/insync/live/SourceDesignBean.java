/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.visualweb.insync.live;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.tree.TreeNode;

import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.EventDescriptor;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.event.DesignBeanListener;

/**
 * Abstract base partial implementation of a DesignBean. Handles management of the BeanInfo and
 * DesignInfo, as well as the live instance.
 *
 * @author Joe Nuxoll
 * @author Carl Quinn
 */
public abstract class SourceDesignBean implements DesignBean, TreeNode, PropertyChangeListener {

    public static final SourceDesignBean[] EMPTY_ARRAY = {};

    protected final LiveUnit unit;
    protected final BeanInfo beanInfo;
    protected final DesignInfo liveBeanInfo;

    protected SourceDesignBean parent;
    protected ArrayList children;   // null when not a container
    protected Object instance;
    protected boolean ready;  // true when created and ready to expose to clients

    /**
     * @param unit
     * @param beanInfo
     * @param liveBeanInfo
     * @param parent
     * @param instance
     */
    protected SourceDesignBean(LiveUnit unit, BeanInfo beanInfo, DesignInfo liveBeanInfo,
                             SourceDesignBean parent, Object instance) {
        this.unit = unit;
        this.parent = parent;
        this.beanInfo = beanInfo;
        this.liveBeanInfo = liveBeanInfo;
        this.instance = instance;
    }

    /**
     * Internal mutator for synthetic liveBean updates
     * @param instance
     */
    public void setInstance(Object instance) {
        this.instance = instance;
    }

    /**
     * Invoke the registered cleanup method for the bean's instance
     */
    public void invokeCleanupMethod() {
    }

    public static class ClipImage {
        Class type;
        String facetName;
        String instanceName;
        SourceDesignBean bean;  // for cross-reference fixup
        SourceDesignProperty.ClipImage[] props;
        SourceDesignEvent.ClipImage[] events;
        ClipImage[] children;

        public String toString() {
            StringBuffer sb = new StringBuffer();
            toString(sb);
            return sb.toString();
        }

        public void toString(StringBuffer sb) {
            sb.append("[DesignBean.ClipImage");
            sb.append(" type=" + type.getName());
            sb.append(" facetName=" + facetName);
            sb.append(" instanceName=" + instanceName);
            if (props != null) {
                sb.append(" props=");
                for (int i = 0; i < props.length; i++)
                    props[i].toString(sb);
            }
            if (events != null) {
                sb.append(" events=");
                for (int i = 0; i < events.length; i++)
                    events[i].toString(sb);
            }
            if (children != null) {
                sb.append(" children=");
                for (int i = 0; i < children.length; i++)
                    children[i].toString(sb);
            }
            sb.append("]");
        }
    }

    public ClipImage getClipImage() {
        ClipImage ci = new ClipImage();
        ci.type = beanInfo.getBeanDescriptor().getBeanClass();
        ci.instanceName = getInstanceName();
        ci.bean = this;

        if (properties == null)
            loadProperties();

        if (properties != null) {
            ArrayList pcis = new ArrayList(properties.size());
            for (int i = 0; i < properties.size(); i++) {
                SourceDesignProperty slp = (SourceDesignProperty)properties.get(i);
                SourceDesignProperty.ClipImage pci = slp.getClipImage();
                if (pci != null)
                    pcis.add(pci);
            }
            ci.props = new SourceDesignProperty.ClipImage[pcis.size()];
            pcis.toArray(ci.props);
        }
        
        if (events == null) {
            loadEvents();
        }

        if (events != null) {
            ArrayList ecis = new ArrayList(events.size());
            for (int i = 0; i < events.size(); i++) {
                SourceDesignEvent sle = (SourceDesignEvent)events.get(i);
                SourceDesignEvent.ClipImage eci = sle.getClipImage();
                if (eci != null)
                    ecis.add(eci);
            }
            ci.events = new SourceDesignEvent.ClipImage[ecis.size()];
            ecis.toArray(ci.events);
        }
        
        if (children != null) {
            ci.children = new ClipImage[children.size()];
            for (int i = 0; i < children.size(); i++)
                ci.children[i] = ((SourceDesignBean)children.get(i)).getClipImage();
        }
        return ci;
    }

    //------------------------------------------------------------------------------- PropertyChange

    /*
    protected void hookPropertyChangeListener() {
        try {
            Method apcl = instance.getClass().getMethod("addPropertyChangeListener",
                                                        new Class[] {PropertyChangeListener.class});
            if (apcl != null)
                apcl.invoke(instance, new Object[] {this});
        }
        catch (Throwable t) {
        }
    }

    protected void unhookPropertyChangeListener() {
        try {
            Method rpcl = instance.getClass().getMethod("removePropertyChangeListener",
                                                        new Class[] {PropertyChangeListener.class});
            if (rpcl != null)
                rpcl.invoke(instance, new Object[] {this});
        }
        catch (Throwable t) {
        }
    }*/

    //------------------------------------------------------------------------------------- DesignBean

    public BeanInfo getBeanInfo() {
        return beanInfo;
    }

    public DesignInfo getDesignInfo() {
        return liveBeanInfo;
    }

    public Object getInstance() {
        return instance;
    }

    public String getInstanceName() {
        return null;
    }

    public boolean canSetInstanceName() {
        return false;
    }

    public boolean setInstanceName(String name) {
        return setInstanceName(name, false);
    }

    public boolean setInstanceName(String name, boolean autoNumber) {
        return false;
    }

    public DesignContext getDesignContext() {
        return unit;
    }

    public DesignBean getBeanParent() {
        return parent;
    }

    public boolean isContainer() {
        return children != null;
    }

    public int getChildBeanCount() {
        return children != null ? children.size() : -1;
    }

    public DesignBean getChildBean(int index) {
        return children != null ? (DesignBean)children.get(index) : null;
    }

    public DesignBean[] getChildBeans() {
        return children != null ? (DesignBean[])children.toArray(SourceDesignBean.EMPTY_ARRAY) : null;
    }

    //----------------------------------------------------------------------------------- Properties

    protected ArrayList properties;
    protected HashMap propertyHash;

    protected abstract SourceDesignProperty newDesignProperty(PropertyDescriptor descriptor);

    public final void loadProperties() {
        properties = new ArrayList();
        propertyHash = new HashMap();
        PropertyDescriptor[] propDescrs = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propDescrs.length; i++) {
            SourceDesignProperty prop = newDesignProperty(propDescrs[i]);
            if (prop != null) {
                prop.initLive();
                properties.add(prop);
                propertyHash.put(propDescrs[i], prop);
            }
        }
        // EAT: Unfortunately there are some side effects on loadEvents that affect properties
        if (events == null)
            loadEvents();
    }

    public DesignProperty[] getProperties() {
        if (properties == null)
            loadProperties();
        return (DesignProperty[])properties.toArray(new DesignProperty[properties.size()]);
    }

    public DesignProperty getProperty(PropertyDescriptor property) {
        if (properties == null)
            loadProperties();
        return (DesignProperty)propertyHash.get(property);
    }

    public DesignProperty getProperty(String propertyName) {
        if (properties == null)
            loadProperties();
        for (int i = 0; i < properties.size(); i++) {
            DesignProperty p = (DesignProperty)properties.get(i);
            if (p.getPropertyDescriptor().getName().equals(propertyName))
                return p;
        }
        return null;
    }
    
    public DesignEvent getEvent(String eventName) {
        if (events == null)
            loadEvents();
        for (int i = 0; i < events.size(); i++) {
            DesignEvent e = (DesignEvent)events.get(i);
            if (e.getEventDescriptor().getName().equals(eventName))
                return e;
        }
        return null;
    }

    //--------------------------------------------------------------------------------------- Events

    protected ArrayList events;

    protected abstract SourceDesignEvent newDesignEvent(EventDescriptor descriptor);

    protected void loadEvents() {
        events = new ArrayList();
        EventSetDescriptor[] esds = beanInfo.getEventSetDescriptors();
        for (int i = 0; i < esds.length; i++) {
            MethodDescriptor[] mds = esds[i].getListenerMethodDescriptors();
            for (int j = 0; j < mds.length; j++) {
                EventDescriptor ed = new EventDescriptor(esds[i], mds[j]);
                SourceDesignEvent e = newDesignEvent(ed);
                if (e != null)
                    events.add(e);
            }
        }
    }

    public DesignEvent[] getEvents() {
        if (events == null)
            loadEvents();
        return (DesignEvent[])events.toArray(SourceDesignEvent.EMPTY_ARRAY);
    }

    public DesignEvent[] getEvents(EventSetDescriptor eventSet) {
        if (events == null)
            loadEvents();

        int lec = 0;
        for (int i = 0; i < events.size(); i++) {
            DesignEvent e = (DesignEvent)events.get(i);
            if (e.getEventDescriptor().getEventSetDescriptor() == eventSet)
                lec++;
        }

        DesignEvent[] les = new DesignEvent[lec];
        lec = 0;
        for (int i = 0; i < events.size(); i++) {
            DesignEvent e = (DesignEvent)events.get(i);
            if (e.getEventDescriptor().getEventSetDescriptor() == eventSet)
                les[lec++] = e;
        }

        return les;
    }

    public DesignEvent getEvent(EventSetDescriptor eventSet, MethodDescriptor event) {
        if (events == null)
            loadEvents();

        for (int i = 0; i < events.size(); i++) {
            DesignEvent e = (DesignEvent)events.get(i);
            EventDescriptor ed = e.getEventDescriptor();
            if (ed.getEventSetDescriptor() == eventSet && ed.getListenerMethodDescriptor() == event)
                return e;
        }
        return null;
    }

    public DesignEvent getEvent(EventDescriptor event) {
        return getEvent(event.getEventSetDescriptor(), event.getListenerMethodDescriptor());
    }

    //-------------------------------------------------------------------------------- Context Items

    protected ArrayList contextItems;

    public void addContextItem(DisplayAction item) {
        if (contextItems == null)
            contextItems = new ArrayList();
        contextItems.add(item);
    }

    public void removeContextItem(DisplayAction item) {
        if (contextItems == null)
            return;
        contextItems.remove(item);
        if (contextItems.size() == 0)
            contextItems = null;
    }

    public DisplayAction[] getContextItems() {
        if (contextItems != null)
            return (DisplayAction[])contextItems.toArray(new DisplayAction[contextItems.size()]);
        return DisplayAction.EMPTY_ARRAY;
    }

    //------------------------------------------------------------------------------------ Listeners

    protected ArrayList lblList = new ArrayList();

    public void addDesignBeanListener(DesignBeanListener beanListener) {
        if (beanListener != null) {
            //System.err.println("SLB.addDesignBeanListener l:" + beanListener);
            lblList.add(beanListener);
        }
    }

    public void removeDesignBeanListener(DesignBeanListener beanListener) {
        lblList.remove(beanListener);
    }

    public DesignBeanListener[] getDesignBeanListeners() {
        return (DesignBeanListener[])lblList.toArray(new DesignBeanListener[lblList.size()]);
    }

    protected void fireDesignBeanInstanceNameChanged(String oldInstanceName) {
        for (Iterator li = lblList.iterator(); li.hasNext(); ) {
            try {
                ((DesignBeanListener)li.next()).instanceNameChanged(this, oldInstanceName);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        unit.fireBeanInstanceNameChanged(this, oldInstanceName);
    }

    protected void fireDesignBeanChanged() {
        for (Iterator li = lblList.iterator(); li.hasNext(); ) {
            try {
                ((DesignBeanListener)li.next()).beanChanged(this);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        unit.fireBeanChanged(this);
    }

    protected void fireDesignPropertyChanged(DesignProperty prop, Object oldValue) {
        for (Iterator li = lblList.iterator(); li.hasNext(); ) {
            DesignBeanListener l = (DesignBeanListener)li.next();
            //System.err.println("SLB.fireDesignPropertyChanged prop:" + prop + " l:" + l);
            try {
                l.propertyChanged(prop, oldValue);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        unit.firePropertyChanged(prop, oldValue);
    }

    protected void fireDesignEventChanged(DesignEvent event) {
        for (Iterator li = lblList.iterator(); li.hasNext(); ) {
            try {
                ((DesignBeanListener)li.next()).eventChanged(event);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        unit.fireEventChanged(event);
    }

    /**
     * Propagate a regular JavaBean propertyChange out to our listeners by firing a live
     * propertyChanged event
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (properties == null)
            loadProperties();
        DesignProperty prop = (DesignProperty)propertyHash.get(e.getPropertyName());
        if (prop != null)
            fireDesignPropertyChanged(prop, e.getOldValue());
    }

    //----------------------------------------------------------------------- Internal Containership

    /*
    public boolean addLiveChildExtended(SourceDesignBean parent, DesignBean lbean, Position pos) {
        int index = pos != null ? pos.getIndex() : -1;
        if (index >= 0 && index < children.size()) {
            if (children.get(index) == lbean)
                return true;
        }
        else {
            index = children.size();
        }

        SourceDesignBean oldparent = (SourceDesignBean)lbean.getBeanParent();
        if (oldparent != null)
            oldparent.removeLiveChild(lbean);

        children.add(index, lbean);
        ((SourceDesignBean)lbean).parent = parent;

        return true;
    }*/

    /**
     * For use by LiveUnit during normal creation
     * @param lbean
     */
    public void addLiveChild(SourceDesignBean lbean, Position pos) {
        int index = pos != null ? pos.getIndex() : -1;
        if (index >= 0 && index < children.size())
            children.add(index, lbean);
        else
            children.add(lbean);
        lbean.parent = this;
    }

    public void removeLiveChild(SourceDesignBean lbean) {
        children.remove(lbean);
    }

    public void clearLiveChildren() {
        children.clear();
    }    

    public SourceDesignBean findLiveChild(String name) {
        for (Iterator i = children.iterator(); i.hasNext(); ) {
            SourceDesignBean slb = (SourceDesignBean)i.next();
            if (slb.getInstanceName().equals(name))
                return slb;
        }
        return null;
    }

    //------------------------------------------------------------------------------------- TreeNode

    private DesignBeanNode node;
    DesignBeanNode getNode() {
        if (node == null) {
            // XXX TODO Ugly implementation, clean it (look at DataObject.getNodeRepresentation).
            node = DesignBeanNode.getInstance(this);
        }
        return node;
    }

    public TreeNode getChildAt(int childIndex) {
        if (children != null) {
            // TODO EAT: This is a hack, but returning null causes other problems,
            // and there seems to be a bug copying lots of components from one page to another, see bug #6347643
            if (childIndex >= children.size()) {
                childIndex = children.size() - 1;
            }
            Object childO = children.get(childIndex);
            if (childO instanceof TreeNode)
                return (TreeNode)childO;
            System.err.println("SLBA.getChildAt: Not a TreeNode: " + childO + " class:" + childO.getClass());
        }
        return null;
    }

    public int getChildCount() {
        return children != null ? children.size() : -1;
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        if (children != null) {
            int n = children.size();
            for (int i = 0; i < n; i++) {
                if (children.get(i) == node)
                    return i;
            }
        }
        return -1;
    }

    public boolean getAllowsChildren() {
        return children != null;
    }

    public boolean isLeaf() {
        return children == null;
    }

    // Note: using old Enumeration only for old NetBeans code
    public Enumeration children() {
        return children != null ? Collections.enumeration(children) : null;
    }

    protected void setChildCapable() {

        children = new ArrayList();
    }

    //--------------------------------------------------------------------------------------- Object

    /**
     * Utility function for debugging--return the name part of this object's class name
     */
    protected String clzName() {
        String cls = getClass().getName();
        int dot = cls.lastIndexOf('.');
        return cls.substring(dot + 1);
    }

    /**
     *
     */
    public String toString() {
//        StringBuffer sb = new StringBuffer(30);
//        sb.append("[");
//        sb.append(clzName());
//        toString(sb);
//        sb.append("]");
//        return sb.toString();
        return super.toString() + "[instanceName=" + getInstanceName() + ", instance=" + getInstance() + "]"; // NOI18N
    }

    public void toString(StringBuffer sb) {
        Object instance = getInstance();
        sb.append(" instance:");
        if (instance == null) {
            sb.append("null");
        } else {
            sb.append(instance);
        }
        /*sb.append(" ");
        DesignBean[] kids = getChildBeans();
        if (kids != null)
            for (int i = 0; i < kids.length; i++)
                sb.append(kids[i].toString());*/
    }
}

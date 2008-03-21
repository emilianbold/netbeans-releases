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

package org.netbeans.beaninfo.editors;

import java.beans.BeanDescriptor;
import java.beans.Introspector;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.*;
import org.openide.explorer.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/** Service type panel for viewing, selecting and configuring
* of executors and other services.
*
* @author Jaroslav Tulach
*/
@SuppressWarnings("deprecation")
public class ServiceTypePanel extends org.netbeans.beaninfo.ExplorerPanel {

    private int width_components=0,width_leftcomponent=0;

    /** the super class of objects that we display.
    */
    private Class<? extends ServiceType> clazz;

    /** list of all services */
    private List<ServiceType> services;

    /** @see ServiceTypeEditor#none */
    private ServiceType none;
    
    /** 
     * False - we are selecting from the registered service types, true - creating
     * new instances of the services
     */
    private boolean createNew = false;

    static final long serialVersionUID =861345226525021334L;
    /** Creates new Panel PropertyEditor
    * @param clazz the super class of objects that we display
    * @param name string to name the panel with
    * @param none no-op type, or null
    */
    public ServiceTypePanel(Class<? extends ServiceType> clazz, String name, ServiceType none, boolean createNew) {
        this.clazz = clazz;
        this.none = none;
        this.createNew = createNew;
        update ();

        initComponents ();

        // #20886 Workaround for jdk JSplitPane bug.
        handleDividerLocation();
        
        label.setText(name);
        listView1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ServiceTypePanel.class).getString("ACSD_ServiceTypeList"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(ServiceTypePanel.class).getString("ACSD_ServiceTypePanel"));

        getExplorerManager ().addPropertyChangeListener (new java.beans.PropertyChangeListener () {
                    public void propertyChange (java.beans.PropertyChangeEvent ev) {
                        if ( ExplorerManager.PROP_SELECTED_NODES.equals(ev.getPropertyName()) ) {
                            firePropertyChange( DialogDescriptor.PROP_HELP_CTX, null, null );
                        }
                        firePropertyChange ();
                    }
                });
        if (name.length() > 0) {
            label.setDisplayedMnemonic(name.charAt(0));
        }
    }

    private void handleDividerLocation() {
        // There is a problem with divider of JSplitPane
        // in some cases -> see bugtraq #4786896.
        int listWidth = listView1.getPreferredSize().width;
        int propWidth = propertySheetView1.getPreferredSize().width;
        int splitWidth = jSplitPane1.getPreferredSize().width;
        int location = (int)((float)listWidth/(listWidth + propWidth) * splitWidth);
        if(location > 0) {
            jSplitPane1.setDividerLocation(location);
        }
    }
    
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Sets the selected value of the component.
    */
    public void setServiceType (ServiceType s) {
        if (s == null) {
            return;
        }
        
        int i = -1;//services.indexOf (s);
        for (int n = 0; n < services.size(); n++) {
            if ((services.get(n)).getName().equals(s.getName())) {
                i = n;
            }
        }

        if (i < 0) {
            // if s is not found try to add s to the nodes by temporarily 
            // assigning the value to the none var (none value is added to the list of services)
            ServiceType oldNone = none;
            none = s;
            update(); // recreates the nodes
            none = oldNone;
            i = services.indexOf (s);
        }

        if (i < 0) {
            Logger.getAnonymousLogger().warning("ServiceTypePanel: Unable to add service " + s.getName()); // NOI18N
            i = 0;
        }
        
        Node[] nodes = getExplorerManager ().getRootContext ().getChildren ().getNodes ();
        if (i >= nodes.length) return;

        try {
            getExplorerManager ().setSelectedNodes (new Node[] {
                                                        nodes[i]
                                                    });
        } catch (java.beans.PropertyVetoException ex) {
            Logger.getLogger(ServiceTypePanel.class.getName()).log(Level.WARNING, null, ex);
        }

        firePropertyChange ();
    }

    /** Sets the selected value of the component.
    * @return selected type or null
    */
    public ServiceType getServiceType () {
        Node[] arr = getExplorerManager ().getSelectedNodes ();
        if (arr.length > 0) {
            return ((MN) arr[0]).getServiceType ();
        }
        return null;
    }

    /** Fires property change.
    */
    void firePropertyChange () {
        firePropertyChange ("serviceType", null, null); // NOI18N
    }

    /** Updates the current state of the explorer manager.
    */
    private void update () {
        Children ch = new Children.Array ();
        AbstractNode n = new AbstractNode (ch);

        ch.add ((Node[])nodes ().toArray (new Node[nodes().size()]));

        getExplorerManager ().setRootContext (n);
        setActivatedNodes(new Node[0]);
    }

    /** Computes the list of nodes that should represent all services classes
    * of the given type.
    *
    * @return list of Nodes
    */
    private List<Node> nodes () {
        services = new ArrayList<ServiceType> (20);
        List<Node> l = new LinkedList<Node> ();
        ServiceType.Registry registry = Lookup.getDefault().lookup(ServiceType.Registry.class);
        Enumeration<? extends ServiceType> en = registry.services (clazz);
        while (en.hasMoreElements ()) {
            try {
                ServiceType service = en.nextElement ();
                BeanDescriptor bd = Introspector.getBeanInfo(service.getClass()).getBeanDescriptor();
                if (bd != null && bd.isHidden()) {
                    continue;
                }
                
                if (createNew) {
                    // in this case create a new instance for all types
                    ServiceType newObject = service.getClass().newInstance();
                    l.add(new MN(newObject));
                    services.add(newObject);
                } else {
                    l.add (new MN (service));
                    services.add (service);
                }
            } catch (java.beans.IntrospectionException ex) {
                Logger.getLogger(ServiceTypePanel.class.getName()).log(Level.WARNING, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(ServiceTypePanel.class.getName()).log(Level.WARNING, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ServiceTypePanel.class.getName()).log(Level.WARNING, null, ex);
            }
        }
        try {
            if (none != null) {
                l.add (new MN (none));
                services.add (none);
            }
        } catch (java.beans.IntrospectionException ex) {
            Logger.getLogger(ServiceTypePanel.class.getName()).log(Level.WARNING, null, ex);
        }
        return l;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jSplitPane1 = new javax.swing.JSplitPane();
        listView1 = new org.openide.explorer.view.ListView();
        propertySheetView1 = new org.openide.explorer.propertysheet.PropertySheetView();
        label = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(0, 2));

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        jSplitPane1.setDividerSize(5);
        jSplitPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jSplitPane1ComponentResized(evt);
            }
        });

        listView1.setDefaultProcessor(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) {} });
        listView1.setPopupAllowed(false);
        listView1.setSelectionMode(1);
        listView1.setTraversalAllowed(false);
        listView1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                listView1ComponentResized(evt);
            }
        });

        jSplitPane1.setLeftComponent(listView1);

        jSplitPane1.setRightComponent(propertySheetView1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        label.setLabelFor(listView1);
        add(label, java.awt.BorderLayout.NORTH);

    }//GEN-END:initComponents

    private void listView1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_listView1ComponentResized
        width_leftcomponent=listView1.getWidth();
    }//GEN-LAST:event_listView1ComponentResized

    private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jSplitPane1ComponentResized
        int width,locator;
        if(width_components>0&&width_leftcomponent>0) {
            width=listView1.getWidth()+propertySheetView1.getWidth();
            locator=width*width_leftcomponent/width_components;
            jSplitPane1.setDividerLocation(locator);
            width_leftcomponent=locator;
            width_components=width;
        } else {
            width_leftcomponent=listView1.getWidth();
            width_components=width_leftcomponent+propertySheetView1.getWidth();
        }
    }//GEN-LAST:event_jSplitPane1ComponentResized


    private void removeButtonPressed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonPressed
    }//GEN-LAST:event_removeButtonPressed

    private void addButtonPressed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonPressed
    }//GEN-LAST:event_addButtonPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel label;
    private org.openide.explorer.view.ListView listView1;
    private org.openide.explorer.propertysheet.PropertySheetView propertySheetView1;
    // End of variables declaration//GEN-END:variables

    /** Node for displaying services */
    private final class MN extends BeanNode {
        public MN (ServiceType t) throws java.beans.IntrospectionException {
            super (t);
        }

        public ServiceType getServiceType () {
            return (ServiceType)getBean ();
        }

        // Prevent folks from changing the name here!
        public Node.PropertySet[] getPropertySets () {
            final Node.PropertySet[] sets = super.getPropertySets ();
            if (createNew) {
                return sets; // when creating new copies user can change the name
            }
            Node.PropertySet[] nue = new Node.PropertySet[sets.length];
            for (int i = 0; i < sets.length; i++) {
                final int ii = i;
                nue[i] = new Node.PropertySet () {
                             {
                                 this.setName (sets[ii].getName ());
                                 this.setDisplayName (sets[ii].getDisplayName ());
                                 this.setShortDescription (sets[ii].getShortDescription ());
                             }
                             public Node.Property[] getProperties () {
                                 Node.Property[] props = sets[ii].getProperties ();
                                 List<Node.Property> nueprops = new ArrayList<Node.Property> ();
                                 for (int j = 0; j < props.length; j++)
                                     if (! "name".equals (props[j].getName ())) // NOI18N
                                         nueprops.add (props[j]);
                                 return nueprops.toArray (new Node.Property[nueprops.size ()]);
                             }
                         };
            }
            return nue;
        }
    }
}

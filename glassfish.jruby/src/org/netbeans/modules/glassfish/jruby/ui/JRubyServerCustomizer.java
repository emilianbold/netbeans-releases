/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.jruby.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.glassfish.jruby.JRubyServerModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule;

/**
 *
 * @author Peter Williams
 */
public class JRubyServerCustomizer extends javax.swing.JPanel {

    public static final String DEFAULT_RUBY_PLATFORM_ID = "ruby.platform.id"; // NOI18N
    
    private GlassfishModule commonSupport;
    private boolean useRootContextChanged = false;
    
    public JRubyServerCustomizer(GlassfishModule commonSupport) {
        this.commonSupport = commonSupport;
        
        initComponents();
    }

    private void initFields() {
        setFilterModel(comboJRubyPlatform.getModel());
        Map<String, String> ip = commonSupport.getInstanceProperties();
        RubyPlatform defaultPlatform = getDefaultPlatform(ip.get(DEFAULT_RUBY_PLATFORM_ID));
        comboJRubyPlatform.setSelectedItem(defaultPlatform);
        
        boolean useRootContextEnabled = Boolean.parseBoolean(ip.get(JRubyServerModule.USE_ROOT_CONTEXT_ATTR));
        useRootContextCheckBox.setSelected(useRootContextEnabled);
    }
    
    private RubyPlatform getDefaultPlatform(String savedPlatformId) {
        RubyPlatform result = RubyPlatformManager.getPlatformByID(savedPlatformId);
        if(result == null || !result.isJRuby()) {
            result = RubyPlatformManager.getDefaultPlatform();
            if(null == result || !result.isJRuby()) {
                result = null;
                Iterator<RubyPlatform> iter = RubyPlatformManager.platformIterator();
                while(iter.hasNext()) {
                    RubyPlatform p = iter.next();
                    if(p.isJRuby()) {
                        result = p;
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private void persistFields() {
        Object o = comboJRubyPlatform.getSelectedItem();
        if(o instanceof RubyPlatform) {
            RubyPlatform p = (RubyPlatform) o;
            commonSupport.setEnvironmentProperty(DEFAULT_RUBY_PLATFORM_ID, p.getID(), true);
            commonSupport.setEnvironmentProperty(GlassfishModule.JRUBY_HOME, p.getHome().getAbsolutePath(), true);
        }
        
        if(useRootContextChanged) {
            String useRootContextEnabled = Boolean.toString(useRootContextCheckBox.isSelected());
            commonSupport.setEnvironmentProperty(JRubyServerModule.USE_ROOT_CONTEXT_ATTR, useRootContextEnabled, true);
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        initFields();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        persistFields();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelJRubyPlatform = new javax.swing.JLabel();
        comboJRubyPlatform = org.netbeans.modules.ruby.platform.PlatformComponentFactory.getRubyPlatformsComboxBox();
        useRootContextCheckBox = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(JRubyServerCustomizer.class, "JRubyServerCustomizer.name")); // NOI18N

        labelJRubyPlatform.setText(org.openide.util.NbBundle.getMessage(JRubyServerCustomizer.class, "JRubyServerCustomizer.labelJRubyPlatform.text")); // NOI18N

        comboJRubyPlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboJRubyPlatformActionPerformed(evt);
            }
        });
        comboJRubyPlatform.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                comboJRubyPlatformPropertyChange(evt);
            }
        });

        useRootContextCheckBox.setText(org.openide.util.NbBundle.getMessage(JRubyServerCustomizer.class, "LBL_UseRootContext")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, useRootContextCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(labelJRubyPlatform)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comboJRubyPlatform, 0, 244, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelJRubyPlatform)
                    .add(comboJRubyPlatform, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(useRootContextCheckBox)
                .addContainerGap(166, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private AtomicBoolean modelChanging = new AtomicBoolean(false);
    
private void comboJRubyPlatformPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_comboJRubyPlatformPropertyChange
    if("model".equals(evt.getPropertyName())) {
        if(!modelChanging.getAndSet(true)) {
            try {
                ComboBoxModel model = (ComboBoxModel) evt.getNewValue();
                setFilterModel(model);
            } finally {
                modelChanging.set(false);
            }
        }
    }
}//GEN-LAST:event_comboJRubyPlatformPropertyChange

private void comboJRubyPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboJRubyPlatformActionPerformed
    useRootContextChanged = true;
}//GEN-LAST:event_comboJRubyPlatformActionPerformed

    private void setFilterModel(ComboBoxModel model) {
        if(model != null &&
                (model.getSize() == 0 || model.getElementAt(0) instanceof RubyPlatform)) {
            model = new FilterModel(model);
            comboJRubyPlatform.setModel(model);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comboJRubyPlatform;
    private javax.swing.JLabel labelJRubyPlatform;
    private javax.swing.JCheckBox useRootContextCheckBox;
    // End of variables declaration//GEN-END:variables

    private static class FilterModel implements ComboBoxModel {

        private ComboBoxModel delegate;
        private Map<RubyPlatform, Integer> data;
        
        FilterModel(ComboBoxModel delegate) {
            this.delegate = delegate;
            updateModel();
        }
        
        private synchronized void updateModel() {
            data = new HashMap<RubyPlatform, Integer>();
            int size = delegate.getSize();
            for(int i = 0; i < size; i++) {
                Object o = delegate.getElementAt(i);
                if(o instanceof RubyPlatform) {
                    RubyPlatform p = (RubyPlatform) o;
                    if(p.isJRuby()) {
                        data.put(p, Integer.valueOf(i));
                    }
                }
            }
        }
        
        public synchronized void setSelectedItem(Object anItem) {
            Integer mappedIndex = data.get(anItem);
            if(mappedIndex != null) {
                delegate.setSelectedItem(anItem);
            }
        }

        public synchronized Object getSelectedItem() {
            Object o = delegate.getSelectedItem();
            if(o instanceof RubyPlatform) {
                RubyPlatform p = (RubyPlatform) o;
                if(p.isJRuby()) {
                    return p;
                }
            }
            return null;
        }

        public synchronized int getSize() {
            return data.size();
        }

        public synchronized Object getElementAt(int index) {
            Iterator<RubyPlatform> iter = data.keySet().iterator();
            while(index-- > 0 && iter.hasNext()) {
                iter.next();
            }
            return iter.hasNext() ? iter.next() : null;
        }

        public void addListDataListener(ListDataListener l) {
            delegate.addListDataListener(l);
        }

        public void removeListDataListener(ListDataListener l) {
            delegate.removeListDataListener(l);
        }

    }
    
}

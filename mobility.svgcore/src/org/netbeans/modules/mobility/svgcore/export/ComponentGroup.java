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
package org.netbeans.modules.mobility.svgcore.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;

/**
 * Simple data binding between Swing components
 * 
 * @author Pavel Benes
 */
    
public class ComponentGroup implements ActionListener, ChangeListener {
        
    public static abstract class ComponentWrapper {
        protected final JComponent      m_delegate;

        public abstract float getValue();
        public abstract void  setValue(float value);

        protected ComponentWrapper(JComponent delegate) {
            m_delegate = delegate;
        }
        
        protected void addListener(ComponentGroup group) {
            if ( m_delegate instanceof JSpinner) {
                ((JSpinner) m_delegate).addChangeListener(group);
            } else if ( m_delegate instanceof JSlider) {
                ((JSlider) m_delegate).addChangeListener(group);
            } else if ( m_delegate instanceof JComboBox) {
                ((JComboBox) m_delegate).addActionListener(group);
            }
        }
        
        public static ComponentWrapper wrap( JComponent comp) {
            if ( comp instanceof JSpinner) {
                return wrap( (JSpinner) comp);
            } else if (comp instanceof JSlider) {
                return wrap( (JSlider) comp);
            } else {
                throw new IllegalArgumentException("Could not wrap " + comp);
            }
        }

        public static ComponentWrapper wrap( JSpinner spinner) {
            return new ComponentWrapper(spinner) {
                public float getValue() {
                    return ((Number) ((JSpinner) m_delegate).getValue()).floatValue();
                }
                
                public void setValue(float value) {
                    JSpinner spinner = (JSpinner) m_delegate;
                    Object prevValue = spinner.getValue();
                    if (prevValue instanceof Integer) {
                        spinner.setValue(new Integer( Math.round(value)));                   
                    } else {
                        spinner.setValue(new Double(value));                   
                    }
                }
            };
        }

        public static ComponentWrapper wrap( JSlider slider) {
            return new SliderWrapper(slider);
        }
    }
    
    public static class SliderWrapper extends ComponentWrapper {
        protected SliderWrapper(JSlider slider) {
            super(slider);
        }
        public float getValue() {
            return ((JSlider)m_delegate).getValue() / 100.0f;
        }

        public void setValue(float value) {
            ((JSlider)m_delegate).setValue( Math.round(value * 100));
        }
    }
    
    private final ComponentWrapper [] m_wrappers;
    private       boolean             m_isUpdateInProgress = false;

    public ComponentGroup( Object ... objects) {
        m_wrappers = new ComponentWrapper[objects.length];
        
        for (int i = 0; i < objects.length; i++) {
            Object o = objects[i];
            if ( o instanceof JComponent) {
                m_wrappers[i] = ComponentWrapper.wrap( (JComponent) o);
            } else if ( o instanceof ComponentWrapper) {
                m_wrappers[i] = (ComponentWrapper) o;
            } else {
                throw new IllegalArgumentException("Illegal object");
            }
            m_wrappers[i].addListener(this);
        }
    }
    
    public ComponentWrapper findWrapper( JComponent comp) {
        for ( ComponentWrapper wrapper : m_wrappers) {
            if ( wrapper.m_delegate == comp) {
                return wrapper;
            }
        }
        return null;
    }
    
    public float getValue() {
        return m_wrappers[0].getValue();
    }
    
    public boolean valueChanged( JComponent comp) {
        for (int i = 0; i < m_wrappers.length; i++) {
            if ( m_wrappers[i].m_delegate == comp) {                    
                return valueChanged(i);
            }
        }
        SceneManager.log(Level.SEVERE, "Component " + comp + " not found!"); //NOI18N
        return false;
    }

    public boolean valueChanged( int index) {
        if ( !m_isUpdateInProgress) {
            try {
                m_isUpdateInProgress = true;
                float value = m_wrappers[index].getValue();
                for ( int i = 0; i < m_wrappers.length; i++) {
                    if ( i != index) {
                        m_wrappers[i].setValue(value);
                    }
                }
                return true;
            } finally {
                m_isUpdateInProgress = false;
            }
        } else {
            return false;
        }
    }

    public void actionPerformed(ActionEvent e) {
        JComponent comp = (JComponent) e.getSource();

        if ( valueChanged( comp)) {
            refresh(comp);
        }
    }

    public void stateChanged(ChangeEvent e) {
        JComponent comp = (JComponent) e.getSource();

        if ( valueChanged( comp)) {
            refresh(comp);
        }
    }
    
    protected void refresh(JComponent comp) {    
    }
}    

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.export;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
        System.err.println("Component not found!");
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

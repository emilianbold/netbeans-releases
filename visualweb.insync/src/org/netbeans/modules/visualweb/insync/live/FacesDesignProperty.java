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

import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import java.beans.PropertyDescriptor;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.faces.FacesBindingPropertyEditor;
import com.sun.rave.designtime.faces.FacesDesignContext;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;

/**
 * A BeansDesignProperty subclass that knows how to handle the additional processing for JSF Value
 * Binding when used. Property field is always in markup, thus always a MarkupProperty.
 *
 * @author Carl Quinn
 */
public class FacesDesignProperty extends BeansDesignProperty
                               implements com.sun.rave.designtime.faces.FacesDesignProperty {

    /**
     * @return whether or not the property is allowed to be an EL value binding expression
     */
    public static final boolean isBindingProperty(PropertyDescriptor pd) {
        String name = pd.getName();
        return name != null &&
            !(name.equals("id") || name.equals("parent") || name.equals("var") || name.equals("rowIndex"));
    }

    /**
     * Construct a new FacesDesignProperty given a descriptor and a bean.
     * @param descriptor
     * @param lbean
     */
    FacesDesignProperty(PropertyDescriptor descriptor, BeansDesignBean lbean) {
        super(descriptor, lbean);
    }

    /*
     * Initialize out live property value if we have a persisted property.
     */
    protected void initLive() {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());
	        if (property != null) {
	            // intercept if the source is a string that we know is a binding EL
	            String source = getValueSource();
	            if (isBindingValue(source))
	                invokeSetter(fromSource(source));
	            else
	                super.initLive();
	        }
	    } finally {
			Thread.currentThread().setContextClassLoader(oldContextClassLoader);
		}
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.live.SourceDesignProperty#getClipImage()
     */
    public ClipImage getClipImage() {
        String name = descriptor.getName();
        if (!isModified() || name.equals("id"))  // don't bother saving id props
            return null;
        Object value = getValueBinding();
        if (value == null)
            value = getValue();
        return new ClipImage(name, value);
    }

    //----------------------------------------------------------------------------------- Conversion

    /*
     * @see org.netbeans.modules.visualweb.insync.live.BeansDesignProperty#toSource(java.lang.Object)
     */
    protected String toSource(Object value) {
        if (value instanceof ValueBinding)
            return ((ValueBinding)value).getExpressionString();
        return super.toSource(value);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.live.BeansDesignProperty#fromSource(java.lang.String)
     */
    protected Object fromSourceIncludeUnknown(String sourceValue) {
        if (isBindingValue(sourceValue)) {
            Application app = ((FacesPageUnit)liveBean.unit.sourceUnit).getFacesApplication();
            return app.createValueBinding(sourceValue);
        }
        return super.fromSourceIncludeUnknown(sourceValue);
    }

    //-------------------------------------------------------------------------------------- Getters

    /*
     * Get this live property value by using the faces UIComponent getValueBinding method instead of
     * the usuall bean property getter
     * @see org.netbeans.modules.visualweb.insync.live.SourceDesignProperty#invokeGetter()
     */
    protected Object invokeGetter() {
        ValueBinding vb = getValueBinding();
        if (vb != null)
            return vb;
        return super.invokeGetter();
    }

    /*
     * Automatically dereference VBs here to make them transparent
     * @see com.sun.rave.designtime.DesignProperty#getValue()
     */
    public Object getValue() {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());
            Object v = invokeGetter();
            if (v instanceof ValueBinding) {
                try {
                    if (liveBean.unit.getModel().isBusted()) {
                        return null;
                    }
                    return ((ValueBinding)v).getValue(liveBean.unit.getFacesContext());
                }
                catch (Exception e) {
                    return null;
                }
            }
            return v;
    	} finally {
    		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }

    //-------------------------------------------------------------------------------------- Setters

    protected boolean invokeSetter(Object value) {
        boolean wasBound = isBound();
        UIComponent uic = (UIComponent)liveBean.getInstance();
        if (value instanceof ValueBinding) {
            ValueBinding vb = (ValueBinding)value;
            if (!wasBound) {
                unloadPropertyEditor();
            }
            try {
                uic.setValueBinding(descriptor.getName(), vb);
                if (!descriptor.getPropertyType().isPrimitive())
                    super.invokeSetter(null);  // clear out any object values so that the binding is used
                setModified(true);
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            if (wasBound) {
                unloadPropertyEditor();
                uic.setValueBinding(descriptor.getName(), null);  // clear out bindings so that they are not used
            }
            return super.invokeSetter(value);
        }
    }

    /*
     * @see com.sun.rave.designtime.DesignProperty#setValue(java.lang.Object)
     */
    public boolean setValue(Object value) {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());
            // intercept strings that look like ValueBindings and convert them first
            if (value instanceof String && isBindingValue((String)value)) {
                value = fromSource((String)value);
            }
            else {
                // for value referencable objects, generate the reference
                DesignBean lb = liveBean.unit.getBeanForInstance(value);
                if (lb != null) {
                    DesignContext lc = lb.getDesignContext();
                    String contextName = liveBean.unit.getRootContainer().getInstanceName();
                    if (lc instanceof FacesDesignContext) {
                    	FacesDesignContext flc = (FacesDesignContext)lc;
                    	contextName = flc.getReferenceName();
                    }
                    String valueSource = "#{" + contextName +
                        "." + lb.getInstanceName() + "}";
                    //return setValueBinding(null, valueSource);
                    return super.setValue(fromSource(valueSource));
                }
            }
            // default: allow super to set something sensible, calling us back to invoke setter
            return super.setValue(value);
    	} finally {
    		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    	}
    }

    //-------------------------------------------------- com.sun.rave.designtime.faces.FacesDesignProperty

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignProperty#isBound()
     */
    public boolean isBound() {
        return getValueBinding() != null;
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignProperty#getValueBinding()
     */
    public ValueBinding getValueBinding() {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
            DesignBean designBean = getDesignBean();
            if (designBean == null) {
                return null;
            }
            DesignContext designContext = designBean.getDesignContext();
            if (designBean == null) {
                return null;
            }
            BeansUnit beansUnit = ((LiveUnit)designContext).getBeansUnit();
            // XXX Possible NPE, when this gets invalid, and still in use.
            if (beansUnit == null) {
                return null;
            }
            
    		Thread.currentThread().setContextClassLoader(beansUnit.getClassLoader());
	        UIComponent uic = (UIComponent)liveBean.getInstance();
	        ValueBinding vb = uic.getValueBinding(descriptor.getName());
	        return vb;
	    } finally {
			Thread.currentThread().setContextClassLoader(oldContextClassLoader);
		}
    }

    /*
     * @see com.sun.rave.designtime.faces.FacesDesignProperty#setValueBinding(javax.faces.el.ValueBinding)
     */
    public void setValueBinding(ValueBinding binding) {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());
    		super.setValue(binding);
    	} finally {
 			Thread.currentThread().setContextClassLoader(oldContextClassLoader);
 		}
    }

    //--------------------------------------------------------------------------------------- SourceLiveProperty

    protected Class getIsBoundPropertyEditorClass() {
// INTEGRATION
// Need to figure out how we provide the PropertySheet functionality we want, such
// as the bound property editor case
//        return PropertySheet.TriggerBoundCusomEditorActionProperty.boundPropertyEditorClass;
        return null;
    }
    
    /**
     * Load the property editor for this property and cache it for internal use.
     */
    protected void loadEditor() {
//        Boolean ignoreIsBound = Boolean.valueOf((String) descriptor.getValue("ignoreIsBound"));
//        Class clazz = descriptor.getPropertyEditorClass();
//        if (clazz != null && FacesBindingPropertyEditor.class.isAssignableFrom(clazz))
//            ignoreIsBound = Boolean.TRUE;
//        if (!ignoreIsBound.booleanValue() && isBound() ) {
//            // initialize a different kind of property editor for bound case
//            clazz = getIsBoundPropertyEditorClass();
//            if (clazz != null) {
//                loadEditor(clazz);
//                return;
//            }
//        }
        super.loadEditor();
    }

    //--------------------------------------------------------------------------------------- Object

    /**
     *
     */
    public String toString() {
        return "[FLP name:" + descriptor.getName() +
            " type:" + descriptor.getPropertyType() +
            " value:" + getValue() + " valueSource:" + getValueSource() + "]";
    }
}

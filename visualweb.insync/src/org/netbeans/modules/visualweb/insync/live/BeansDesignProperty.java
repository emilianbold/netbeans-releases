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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.openide.util.NbBundle;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.faces.FacesDesignContext;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.Property;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope;

/**
 * DesignProperty implementation based on delegation to beans.Property and subclasses, using Java
 * and/or markup source.
 *
 * @author Carl Quinn
 */
public class BeansDesignProperty extends SourceDesignProperty {

    public static final Object FROMSOURCE_UNKNOWNVALUE = new Object();

    Property property;  // the persisted property state if set, i.e. not default, null if not set

    //--------------------------------------------------------------------------------- Construction

    /**
     * @param descriptor
     * @param lbean
     */
    BeansDesignProperty(PropertyDescriptor descriptor, BeansDesignBean lbean) {
        super(descriptor, lbean);
        property = lbean.bean.getProperty(descriptor.getName());
    }

    /**
     *
     */
    protected void initLive() {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());
            if (property != null) {
                // try to get value object from down below. Will return primitives or bean
                Object value = property.getValue(descriptor.getPropertyType());

                // if not known by Property, then use live knowledge to attempt conversion
                if (value == null) {
                    String valueSource = property.getValueSource();
                    assert Trace.trace("insync.live", "JLP.restoring " + descriptor.getName() +
                                       " from \"" + valueSource + "\"");
                    value = fromSource(valueSource);
                }
                // if it's a bean, look up its live counterpart & use the instance
                else if (value instanceof Bean) {
                    DesignBean lb = liveBean.unit.getDesignBean((Bean)value);
                    value = lb.getInstance();
                }
                // else others are values ready to use

                assert Trace.trace("insync.live", "JLP.restoring " + descriptor.getName() + " as " + value +
                                   (value != null ? (" (" + value.getClass().getName() + ")") : "" ));
                invokeSetter(value);
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
        return new ClipImage(name, getValue());
    }

    //----------------------------------------------------------------------------------- Conversion

    /**
     * An extended Class.isAssignableFrom() that treats primitive lvalues as their object
     * counterpart.
     *
     * @param to
     * @param from
     * @return
     */
    public static boolean isAssignableFrom(Class to, Class from) {
        if (to.isPrimitive()) {
            if (to == Boolean.TYPE)
                to = Boolean.class;
            else if (to == Character.TYPE)
                to = Character.class;
            else if (to == Byte.TYPE)
                to = Byte.class;
            else if (to == Short.TYPE)
                to = Short.class;
            else if (to == Integer.TYPE)
                to = Integer.class;
            else if (to == Long.TYPE)
                to = Long.class;
            else if (to == Float.TYPE)
                to = Float.class;
            else if (to == Double.TYPE)
                to = Double.class;
        }
        return to.isAssignableFrom(from);
    }

    /**
     * Try to convert an object to source: markup or java.
     *
     * @param value
     * @return
     */
    protected String toSource(Object value) {        
        boolean isMarkup = isMarkupSource();
        //System.err.println("JLP.toSource: " + liveBean.getInstanceName() + "." + descriptor.getName() + " to:" + value);
        //System.err.println("JLP.toSource: isMarkup:" + isMarkup);
        if (value == null)
            return isMarkup ? null : "null";

        if (isMarkup || isAssignableFrom(descriptor.getPropertyType(), value.getClass())) {
            // Look locally first as an optimization
            DesignBean lb = liveBean.unit.getBeanForInstance(value);
            
            //VB expressions cannot be used to refer the properties in the same bean
            //when the initializers are added into the constructor
            //This is required for migrated reef projects
            if((liveBean.unit.getBeansUnit().getPropertiesInitMethod().isConstructor()) &&
               lb != null) {
                return lb.getInstanceName();
            }
            
            if(lb == null) {
                // Now look through all contexts and see if we can find it
                // !EAT TODONOW Optimize this, so that lookup for simple values like strings dont take too long ?
                LiveUnit[] units = (LiveUnit[]) ((FacesModelSet) liveBean.unit.getModel().getOwner()).findDesignContexts(
                        new String[]{Scope.REQUEST.toString(), Scope.SESSION.toString(), Scope.APPLICATION.toString()});
                for (int i=0; i < units.length; i++) {
                    lb = units[i].getBeanForInstance(value);
                    if(lb != null) break;
                }
            }

            if (lb != null) {
                String binding = "#{"+ ((FacesDesignContext)lb.getDesignContext()).getReferenceName() + "." + lb.getInstanceName() + "}";
                if (isMarkup)
                    return binding;
                String typeName = getPropertyDescriptor().getPropertyType().getCanonicalName();
                return "(" + typeName + ")getValue(\"" + binding + "\")";
            }
            //System.err.println("JLP.toSource: valueSource:" + (isMarkup ? value.toString() : toJavaInitializationString(value)));
            return isMarkup ? value.toString() : toJavaInitializationString(value);
        }
        return "null/*!!BAD PROPERTY TYPE SET!!*/";
    }

    /**
     * Try to convert source, markup or java, to an object.
     *
     * @param sourceValue
     * @return
     */
    protected Object fromSource(String sourceValue) {
        Object result = fromSourceIncludeUnknown(sourceValue);
        if (result == FROMSOURCE_UNKNOWNVALUE)
            return null;
        return result;
    }

    /**
     * Try to convert source, markup or java, to an object.
     * If I am unable to compute the value, then return FROMSOUR_UNKNOWNVALUE.
     * @param sourceValue
     * @return
     */
    protected Object fromSourceIncludeUnknown(String sourceValue) {
        boolean isMarkup = isMarkupSource();
        if (isMarkup)
            return sourceValue;

        Object value = FROMSOURCE_UNKNOWNVALUE;
        try {
            // Look for (java.sql.ResultSet)getValue("#{SessionBean1.personRowSet1}")
            // !EAT TODO need to create a subclass of BeansDesignProperty that does the VB stuff, since this
            // is Faces dependent
            String lookFor = "getValue(\"#{";
            int index = sourceValue.indexOf(lookFor);
            if (index != -1) {
                int lastIndex = sourceValue.indexOf("}", index);
                if (lastIndex != -1) {
                    String vbString = sourceValue.substring(index + lookFor.length() -2, lastIndex+1);
                    ValueBinding vb = FacesContext.getCurrentInstance().getApplication().createValueBinding(vbString);
                    value = vb.getValue(FacesContext.getCurrentInstance());
                    if(value == null){
                        //Workaround for #120251
                        //It seems that the value will be null when the evaluation of an EL expression is 
                        //trigerred by evaluation of another EL expression where-in the first part of the 
                        //expression is same for both. It is strange that no exception is thrown in this 
                        //scenario. Trying again work fine
                        value = vb.getValue(FacesContext.getCurrentInstance());
                    }
                    return value;
                }
            }
            // see if it is class.FIELD constant reference
            int rpart = sourceValue.lastIndexOf('.');
            if (rpart != -1) {
                String cname = sourceValue.substring(0, rpart).trim();
                String fname = sourceValue.substring(rpart+1);
                // Check to see if sourceValue is something like getSessionBean1().getGreeterClient1()
                // We should try to handle this kind of case, but it will have to wait
                // !EAT TODO XXX
                if (cname.endsWith(")")) {
                } else {
                    //System.err.println("JLP.fromSource cname:" + cname + " fname:" + fname);
                    Class c = liveBean.unit.sourceUnit.getBeanClass(cname);
                    // Check for case where we have something like objectListDataProvider1.setObjectType(webapplication1.Name.class);
                    if (fname.equals("class")) // NOI18N
                        value = c;
                    else {
                        Field f = c.getField(fname);
                        value = f.get(null);
                    }
                }
            }
            // see if it is a sibling bean reference
            else {
                DesignBean lb = liveBean.unit.getBeanByName(sourceValue);
                if (lb != null) {
                    value = lb.getInstance();
                }
            }
        }
        catch (Exception e) {
            System.err.println("Caught " + e + " in JLP.fromSource: " + descriptor.getName());
            e.printStackTrace();
        }
        //System.err.println("JLP.fromSource value:" + value);
        return value;
    }

    //-------------------------------------------------------------------------------------- Getters

    /*
     * @see com.sun.rave.designtime.DesignProperty#getValueSource()
     */
    public String getValueSource() {
    	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(((LiveUnit)getDesignBean().getDesignContext()).getBeansUnit().getClassLoader());
    		return property != null ? property.getValueSource() : null;
	    } finally {
			Thread.currentThread().setContextClassLoader(oldContextClassLoader);
		}
    }

    //-------------------------------------------------------------------------------------- Setters

    /**
     * Pass a value (in object and/or source form) to our bean property, creating it as needed.
     *
     * @param value
     * @param valueSource
     */
    protected void setBeanProperty(Object valueOrUnknown, String valueSource) {

        Object value;
        if (valueOrUnknown == FROMSOURCE_UNKNOWNVALUE)
            value = null;
        else
            value = valueOrUnknown;
        // intercept values that are the same as our initial value, and remove the source instead
        if (valueOrUnknown != FROMSOURCE_UNKNOWNVALUE && equalsInitial(value)) {
            unsetBeanProperty();
            return;
        }
        // could intercept unnecessary sets and just return
        //if (objectsEqual(value, getValue())
        //    return;

        // real values need to be saved in source
        UndoEvent event = null;
        try {
            String propname = getPropertyDescriptor().getName();
            String description = NbBundle.getMessage(BeansDesignProperty.class, "SetProperty", propname); // NOI18N
            event = liveBean.unit.model.writeLock(description);

            // valueSource being null indicates that we should remove the property
            if (valueSource == null) {
                if (property == null) {
                    // just leave unset if already null
                }
                else {
                    BeansDesignBean jlbean = (BeansDesignBean)liveBean;
                    jlbean.bean.unsetProperty(property);
                    property = null;
                }
            }
            // valueSource being non-null indicates that we should add or update the property
            else {
                // replace live instances with their beans Bean to let it generate the identifier
                if (value != null) {
                    BeansDesignBean vlb = (BeansDesignBean)liveBean.unit.getBeanForInstance(value);
                    if (vlb != null)
                        value = vlb.bean;
                }
                if (property == null) {
                    BeansDesignBean jlbean = (BeansDesignBean)liveBean;
                    String name = getPropertyDescriptor().getName();
                    property = jlbean.bean.setProperty(name, valueOrUnknown, valueSource);
                }
                else {
                    property.setValue(valueOrUnknown, valueSource);
                }
            }
        }
        finally {
            liveBean.unit.model.writeUnlock(event);
        }
    }

    /**
     * Unset our bean property, as needed.
     */
    protected void unsetBeanProperty() {
        if (property != null) {
            UndoEvent event = null;
            try {
                String propname = getPropertyDescriptor().getName();
                String description = NbBundle.getMessage(BeansDesignProperty.class, "UnsetProperty", propname); // NOI18N
                event = liveBean.unit.model.writeLock(description);

                BeansDesignBean jlbean = (BeansDesignBean)liveBean;
                jlbean.bean.unsetProperty(property);
                property = null;
            }
            finally {
                liveBean.unit.model.writeUnlock(event);
            }
        }
        modified = false;  // no longer modified, but no need to reacquire
    }

    /*
     * @see com.sun.rave.designtime.DesignProperty#setValue(java.lang.Object)
     */
    public boolean setValue(Object value) {
        assert Trace.trace("insync.live", "JLP.setValue " + descriptor.getName() + " from:" + getValue() +
                           " to:" + value);
        Object oldValue = invokeGetter();
        boolean result = invokeSetter(value);
        if (result) {
            setBeanProperty(value, toSource(value));
            liveBean.fireDesignPropertyChanged(this, oldValue);
        }
        return result;
    }

	// A fake object sent in place of old value in the property change event
	// when the property state changes non-bound to bound or bound to non-bound
	// to force the listeners to process the event fully
    private static final Object NOT_EQUAL = new Object() {
        public boolean equals(Object obj) {
            return false;
        };
    };
    
    /*
     * @see com.sun.rave.designtime.DesignProperty#setValueSource(java.lang.String)
     */
    public boolean setValueSource(String valueSource) {
        boolean wasBound = isBindingValue(getValueSource());
        Object oldValue = invokeGetter();
        if (valueSource == null) {
            return unset();
        }
        Object value = fromSourceIncludeUnknown(valueSource);
        //System.err.println("JLP.setValueSource " + liveBean.getInstanceName() + "." +descriptor.getName() +
        //                   " from:" + getValue() +
        //                   " to src:\"" + valueSource + "\" val:" + value +
        //                   (value != null ? (" class:" + value.getClass().getName()) : ""));
        setBeanProperty(value, valueSource);
        if (value == FROMSOURCE_UNKNOWNVALUE)
            value = null;
        boolean result = invokeSetter(value);
        if (result) {
            boolean isBound = isBindingValue(getValueSource());
            liveBean.fireDesignPropertyChanged(this, ((isBound == wasBound) ? oldValue : NOT_EQUAL));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------- Misc

    /**
     * @return true if this property is managed in markup, false if in java.
     */
    public boolean isMarkupSource() {
        return property != null
            ? property.isMarkupProperty() : ((BeansDesignBean)liveBean).isMarkupProperty(descriptor);
    }

    /*
     * @see com.sun.rave.designtime.DesignProperty#isModified()
     */
    public boolean isModified() {
        boolean mod = super.isModified();
        if (mod != (property != null)) {
            assert Trace.trace("insync.live", "JLP.isModified State out of sync! mod:" + mod +
                               " prop:" + property);
        }
        return mod;  //!CQ XXX or just return property != null ???
    }

    /*
     * @see com.sun.rave.designtime.DesignProperty#unset()
     */
    public boolean unset() {
        Object oldValue = invokeGetter();
        boolean ok = super.unset();
        if (ok) {  //!CQ XXX maybe revert before/even if super fails?
            unsetBeanProperty();
            liveBean.fireDesignPropertyChanged(this, oldValue);
        }
        return ok;
    }

    /**
     * @return whether or not the value string is EL value or method binding expression
     */
    public static final boolean isBindingValue(String value) {
        return value != null && value.startsWith("#{") && value.endsWith("}");
    }

    //--------------------------------------------------------------------------------------- Object

    /**
     *
     */
    public String toString() {
        return "[BLP name:" + descriptor.getName() + " value:" + getValue() + " valueSource:" +
            getValueSource() + "]";
    }
}

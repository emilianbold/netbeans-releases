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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import com.sun.rave.designtime.faces.FacesBindingPropertyEditor;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;
import java.beans.PropertyEditorSupport;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.convert.*;
import javax.faces.el.ValueBinding;


/**
 * A property editor for converter properties.
 *
 * @author gjmurphy
 */
public class ConverterPropertyEditor extends PropertyEditorBase implements FacesBindingPropertyEditor,
        com.sun.rave.propertyeditors.ConverterPropertyEditor {

    private static final ResourceBundle bundle =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle"); //NOI18N

    // Some default JSF converters, used in the event that the project context cannot be scanned
    // for converters
    private static final Class[] defaultFacesConverterClasses = new Class[] {
        BooleanConverter.class,
        ByteConverter.class,
        CharacterConverter.class,
        DateTimeConverter.class,
        DoubleConverter.class,
        FloatConverter.class,
        IntegerConverter.class,
        LongConverter.class,
        NumberConverter.class,
        ShortConverter.class
    };

    public String[] getTags() {
        DesignBean[] converterBeans = getConverterBeans();
        String[] converterLabels = getConverterLabels();
        String[] tags = new String[converterBeans.length + converterLabels.length + 1];
        int index = 0;
        tags[index++] = "";
        for (int i = 0; i < converterBeans.length; i++) {
            tags[index++] = converterBeans[i].getInstanceName();
        }
        for (int i = 0; i < converterLabels.length; i++) {
            tags[index++] = converterLabels[i];
        }
        return tags;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().length() == 0) {
            this.setValue(null);
            return;
        }
        DesignBean[] converterBeans = getConverterBeans();
        Converter converter = null;
        // Determine if user selected a converter that is already created
        for (int i = 0; i < converterBeans.length && converter == null; i++) {
            if (converterBeans[i].getInstanceName().equals(text)) {
                converter = (Converter)converterBeans[i].getInstance();
            }
        }
        if (converter == null) {
            String[] converterLabels = getConverterLabels();
            // Created a new converter of the type the user selected
            Class[] converterClasses = getConverterClasses();
            for (int i = 0; i < converterLabels.length && converter == null; i++) {
                if (converterLabels[i].equals(text)) {
                    DesignProperty designProperty = this.getDesignProperty();
                    DesignBean createResult =
                            designProperty.getDesignBean().getDesignContext().createBean(converterClasses[i].getName(), null, null);
                    if (createResult != null) {
                        converter = (Converter)createResult.getInstance();
                    }
                }
            }
        }
        this.setValue(converter);
    }

    public String getAsText() {
        Object value = getValue();
        if (value instanceof ValueBinding) {
            return ((ValueBinding)value).getExpressionString();
        }
        if (value instanceof String) {
            return (String)value;
        }
        DesignBean designBean = getDesignBean();
        if( designBean != null ) {
            if( designBean.getInstance() instanceof NumberConverter ) {
                // Here are the defautls the NumberConverter has:
                // - min integer digits to 1
                // - max integer digits to 40
                // - min fractional digites to 0
                // - max fractional digits to 3
                designBean.getProperty( "minIntegerDigits").setValue( new Integer(1) );
                designBean.getProperty( "maxIntegerDigits").setValue( new Integer(40) );
                designBean.getProperty( "minFractionDigits").setValue( new Integer(0) );
                designBean.getProperty( "maxFractionDigits").setValue( new Integer(3) );
            }
        }
        return (designBean == null) ? "" : designBean.getInstanceName(); //NOI18N
    }
    
    public boolean supportsCustomEditor() {
        return false;
    }
    
    private static Comparator<Class> converterComparator = new Comparator<Class>() {
        public int compare(Class class1, Class class2) {
            String name1 = class1.getName();
            String name2 = class2.getName();
            return name1.substring(name1.lastIndexOf('.') + 1).compareTo(name2.substring(name2.lastIndexOf('.') + 1));
        }
    };
    
    
    // A global map of classes to converters for those classes, used to avoid expensive
    // repetitive recalculation of converter classes. If a new component library is
    // imported into the IDE, any converters will be discovered and added to the map.
    private static Map<Class, Class> converterClassMap = new HashMap<Class, Class>();
    
    private Class[] converterClasses;
    
    /**
     * Generates an array of classes for all converter components registered with the
     * design-time JSF application.
     */
    protected Class[] getConverterClasses() {
        if (converterClasses != null)
            return converterClasses;
        DesignProperty designProperty = this.getDesignProperty();
        if (designProperty == null)
            return defaultFacesConverterClasses;
        FacesContext facesContext =
                ((FacesDesignContext) designProperty.getDesignBean().getDesignContext()).getFacesContext();
        Application application = facesContext.getApplication();
        
        TreeSet<Class> set = new TreeSet<Class>(converterComparator);
        
        Iterator<Class> iter;
                
        FacesDesignProject facesDesignProject = (FacesDesignProject)designProperty.getDesignBean().getDesignContext().getProject();
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(facesDesignProject.getContextClassLoader());
            // Add the conveters registered by types
            iter = application.getConverterTypes();
            while (iter.hasNext()) {
                Class propertyClass = iter.next();
                if (!converterClassMap.containsKey(propertyClass)) {
                    Converter converter = application.createConverter(propertyClass);
                    converterClassMap.put(propertyClass, converter.getClass());
                }
                set.add(converterClassMap.get(propertyClass));
            }
            
            // Add the converters registered by Ids
            Iterator idConverterIter = application.getConverterIds();
            while (idConverterIter.hasNext()) {
                Converter converter = application.createConverter( (String)idConverterIter.next() );
                set.add( converter.getClass() );
            }    
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
                
        converterClasses = new Class[set.size()];
        iter = set.iterator();
        for (int i = 0; i < converterClasses.length; i++)
            converterClasses[i] = iter.next();
    
        return converterClasses;
    }
    
    private String[] converterLabels;
    
    /**
     * Generates an array of display labels for all converter classes.
     */
    protected String[] getConverterLabels() {
        if (converterLabels != null)
            return converterLabels;
        Class[] converterClasses = getConverterClasses();
        converterLabels = new String[converterClasses.length];
        MessageFormat labelFormat =
                new MessageFormat(bundle.getString("ConverterPropertyEditor.newConverterLabel")); //NOI18N
        Object[] args = new Object[1];
        for (int i = 0; i < converterClasses.length; i++) {
            String name = converterClasses[i].getName();
            args[0] = name.substring(name.lastIndexOf('.') + 1);
            converterLabels[i] = labelFormat.format(args);
        }
        return converterLabels;
    }
    
    private DesignBean getDesignBean() {
        Object value = getValue();
        DesignBean[] lbeans = getConverterBeans();
        for (int i = 0; i < lbeans.length; i++) {
            if (lbeans[i].getInstance() == value) {
                return lbeans[i];
            }
        }
        return null;
    }
    
    private DesignBean[] getConverterBeans() {
        DesignProperty designProperty = this.getDesignProperty();
        if (designProperty == null)
            return new DesignBean[0];
        return designProperty.getDesignBean().getDesignContext().getBeansOfType(Converter.class);
    }
    
}

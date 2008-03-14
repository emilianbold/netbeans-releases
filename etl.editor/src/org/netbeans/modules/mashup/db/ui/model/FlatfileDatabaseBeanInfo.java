/*
 *
 *          Copyright (c) 2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 */
package org.netbeans.modules.mashup.db.ui.model;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.ArrayList;
import java.util.List;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;



/**
 * Exposes getters for flatfile database properties. TODO Extend this class to expose
 * setters for read-write property sheets (MutabledFlatfileDatabaseBeanInfo?)
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class FlatfileDatabaseBeanInfo extends SimpleBeanInfo {

    private static BeanDescriptor beanDescriptor = null;
    private static EventSetDescriptor[] eventSet = null;
    private static MethodDescriptor[] methods = null;
    private static PropertyDescriptor[] properties = null;
    private static transient final Logger mLogger = Logger.getLogger(FlatfileDatabaseBeanInfo.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static int defaultPropertyIndex = -1; // GEN-BEGIN:Idx
    private static int defaultEventIndex = -1; // GEN-END:Idx

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable properties of this bean. May return
     *         null if the information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(FlatfileDatabase.class);
        }

        return beanDescriptor;
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable properties
     *         supported by this bean. May return null if the information should be
     *         obtained by automatic analysis.
     *         <p>
     *         If a property is indexed, then its entry in the result array will belong to
     *         the IndexedPropertyDescriptor subclass of PropertyDescriptor. A client of
     *         getPropertyDescriptors can use "instanceof" to check if a given
     *         PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (properties == null) {
            List myProps = new ArrayList();

            try {
                PropertyDescriptor pd = new PropertyDescriptor("name", FlatfileDatabase.class, "getName", null); // NOI18N
                String nbBundle1 = mLoc.t("BUND197: Flat file definition name");
                String label = nbBundle1.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("description", FlatfileDatabase.class, "getDescription", null); // NOI18N
                String nbBundle2 = mLoc.t("BUND198: Description");
                String label = nbBundle2.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            properties = (PropertyDescriptor[]) myProps.toArray(new PropertyDescriptor[myProps.size()]);
        }

        return properties;
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     * 
     * @return An array of EventSetDescriptors describing the kinds of events fired by
     *         this bean. May return null if the information should be obtained by
     *         automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        if (eventSet == null) {
            eventSet = new EventSetDescriptor[0];
        }
        return eventSet;
    }

    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     * 
     * @return An array of MethodDescriptors describing the methods implemented by this
     *         bean. May return null if the information should be obtained by automatic
     *         analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        if (methods == null) {
            methods = new MethodDescriptor[0];
        }

        return methods;
    }

    /**
     * A bean may have a "default" property that is the property that will mostly commonly
     * be initially chosen for update by human's who are customizing the bean.
     * 
     * @return Index of default property in the PropertyDescriptor array returned by
     *         getPropertyDescriptors.
     *         <P>
     *         Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will mostly commonly be
     * used by human's when using the bean.
     * 
     * @return Index of default event in the EventSetDescriptor array returned by
     *         getEventSetDescriptors.
     *         <P>
     *         Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }
}

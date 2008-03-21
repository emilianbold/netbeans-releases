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
 * Abstract BeanInfo implementation to expose read-only access to selected table
 * properties.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public abstract class FlatfileTableBeanInfo extends SimpleBeanInfo {

    private static PropertyDescriptor[] properties = null;
    private static EventSetDescriptor[] eventSets = null;
    private static MethodDescriptor[] methods = null;
    private static transient final Logger mLogger = Logger.getLogger(FlatfileTableBeanInfo.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable properties of this bean. May return
     *         null if the information should be obtained by automatic analysis.
     */
    public abstract BeanDescriptor getBeanDescriptor();

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
                PropertyDescriptor pd = new PropertyDescriptor("tableName", FlatfileTable.class, "getTableName", null); // NOI18N
                String nbBundle1 = mLoc.t("BUND199: Table name");
                String label = nbBundle1.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("fileType", FlatfileTable.class, "getFileType", null); // NOI18N
                String nbBundle2 = mLoc.t("BUND200: File type");
                String label = nbBundle2.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("description", FlatfileTable.class, "getDescription", null); // NOI18N
                String nbBundle3 = mLoc.t("BUND198: Description");
                String label = nbBundle3.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("fileName", FlatfileTable.class, "getFileName", null); // NOI18N
                String nbBundle4 = mLoc.t("BUND202: File name");
                String label = nbBundle4.substring(15);// NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException e) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("encodingScheme", FlatfileTable.class, "getEncodingScheme", null); // NOI18N
                String nbBundle5 = mLoc.t("BUND203: Encoding scheme");
                String label = nbBundle5.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("rowsToSkip", FlatfileTable.class, "getRowsToSkip", null); // NOI18N
                String nbBundle6 = mLoc.t("BUND204: Initial rows to skip");
                String label = nbBundle6.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("maxFaults", FlatfileTable.class, "getMaxFaults", null); // NOI18N
                String nbBundle7 = mLoc.t("BUND205: Maximum # faults to tolerate");
                String label = nbBundle7.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("isFirstLineHeader", FlatfileTable.class, "isFirstLineHeader", null); // NOI18N
                String nbBundle8 = mLoc.t("BUND206: First line is header");
                String label = nbBundle8.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("recordDelimiter", FlatfileTable.class, "getRecordDelimiter", null); // NOI18N
                String nbBundle9 = mLoc.t("BUND207: Record delimiter");
                String label = nbBundle9.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
            }

            try {
                PropertyDescriptor pd = new PropertyDescriptor("trimWhiteSpace",
                        FlatfileTable.class, "enableWhiteSpaceTrimming", null); // NOI18N
                String nbBundle10 = mLoc.t("BUND208: Trim white space for columns");
                String label = nbBundle10.substring(15); // NOI18N
                pd.setDisplayName(label);
                myProps.add(pd);
            } catch (IntrospectionException ignore) {
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
        if (eventSets == null) {
            eventSets = new EventSetDescriptor[0];
        }

        return eventSets;
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
}

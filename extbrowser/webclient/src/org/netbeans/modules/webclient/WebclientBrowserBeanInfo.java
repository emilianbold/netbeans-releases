package org.netbeans.modules.webclient;

import java.beans.*;

public class WebclientBrowserBeanInfo extends SimpleBeanInfo {


    // Bean descriptor
    private static BeanDescriptor beanDescriptor = null /*lazy*/; 

    private static BeanDescriptor getBdescriptor(){
        if(beanDescriptor == null){
            beanDescriptor = new BeanDescriptor  ( WebclientBrowser.class , null );

    // Here you can add code for customizing the BeanDescriptor.

        }         return beanDescriptor;         }

    // Property identifiers
    private static final int PROPERTY_appData = 0;

    // Property array 
    private static PropertyDescriptor[] properties = null /*lazy*/; 

    private static PropertyDescriptor[] getPdescriptor(){
        if(properties == null){
            properties = new PropertyDescriptor[1];
    
            try {
                properties[PROPERTY_appData] = new PropertyDescriptor ( "appData", WebclientBrowser.class, "getAppData", "setAppData" ); // NOI18N
            }
            catch( IntrospectionException e) {}

    // Here you can add code for customizing the properties array.

        }         return properties;         }

    // Event set information will be obtained from introspection.
    private static EventSetDescriptor[] eventSets = null;
    private static EventSetDescriptor[] getEdescriptor(){
        return eventSets;
    }

    // Here you can add code for customizing the event sets array.


    // Method information will be obtained from introspection.
    private static MethodDescriptor[] methods = null;
    private static MethodDescriptor[] getMdescriptor(){
        return methods;
    }

    // Here you can add code for customizing the methods array.
    

    private static final int defaultPropertyIndex = -1;
    private static final int defaultEventIndex = -1;


    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     * 
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
	//return beanDescriptor;
	return getBdescriptor();
    }

    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        //return properties;
	return getPdescriptor();
    }

    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     * 
     * @return  An array of EventSetDescriptors describing the kinds of 
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        //return eventSets;
	return getEdescriptor();
    }

}


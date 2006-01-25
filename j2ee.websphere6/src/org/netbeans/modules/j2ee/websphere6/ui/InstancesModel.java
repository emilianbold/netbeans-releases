/*
 * InstancesModel.java
 *
 * Created on 24 январь 2006 г., 13:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.websphere6.ui;
import javax.swing.*;
import java.util.*;

  /**
     * A combobox model that represents the list of local instances. It 
     * contains a vector of objects of Instance class that contain all data
     * for the instance
     * 
     * @author Kirill Sorokin, redesigned by Dmitry Lipin
     */
public class InstancesModel extends AbstractListModel 
            implements ComboBoxModel {
        /**
         * A vector with the instances
         */
        private Vector instances;
        
        /**
         * The index of the selected instance
         */
        private int selectedIndex = 0;
        
        /**
         * Creates a new instance of InstancesModel
         * 
         * @param instances a vector with the locally found instances
         */
        public InstancesModel(Vector instances) {
            // save the instances
            this.instances = instances;
            
            // set the selected index to zero
            this.selectedIndex = 0;
        }
        
        /**
         * Sets the selected index to the index of the supplied item
         * 
         * @param item the instance which should be selected
         */
        public void setSelectedItem(Object item) {
            // set the index to the given item's index or to -1
            // if the item does not exists
            selectedIndex = instances.indexOf(item);
        }
        
        /**
         * Get the instance with the specified instance
         * 
         * @param index the index of the desired instance
         * 
         * @return the instance at the given index
         */
        public Object getElementAt(int index) {
            return instances.elementAt(index);
        }
        
        /**
         * Returns the total number of instances
         * 
         * @return the number of instances
         */
        public int getSize() {
            return instances.size();
        }
        
        /**
         * Returns the instance at the selected index
         * 
         * @return the instance at the selected index
         */
        public Object getSelectedItem() {
            // if there are no instances return null
            if (instances.size() == 0) {
                return null;
            }
            
            // return the element at the index
            return instances.elementAt(selectedIndex);
        }
}

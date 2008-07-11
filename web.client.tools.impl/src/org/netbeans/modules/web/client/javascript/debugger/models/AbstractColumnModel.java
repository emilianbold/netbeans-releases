package org.netbeans.modules.web.client.javascript.debugger.models;

import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.viewmodel.ColumnModel;




/**
* Defines model for one table view column. Can be used together with
* {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
*/
public abstract class AbstractColumnModel extends ColumnModel {

   Properties properties = Properties.getDefault ().
       getProperties ("debugger").getProperties ("breakpoints");


   /**
    * Set true if column is visible.
    *
    * @param visible set true if column is visible
    */
   public void setVisible (boolean visible) {
       properties.setBoolean (getID () + ".visible", visible);
   }

   /**
    * Set true if column should be sorted by default.
    *
    * @param sorted set true if column should be sorted by default
    */
   public void setSorted (boolean sorted) {
       properties.setBoolean (getID () + ".sorted", sorted);
   }

   /**
    * Set true if column should be sorted by default in descending order.
    *
    * @param sortedDescending set true if column should be sorted by default
    *        in descending order
    */
   public void setSortedDescending (boolean sortedDescending) {
       properties.setBoolean (getID () + ".sortedDescending", sortedDescending);
   }

   /**
    * Should return current order number of this column.
    *
    * @return current order number of this column
    */
   public int getCurrentOrderNumber () {
       return properties.getInt (getID () + ".currentOrderNumber", -1);
   }

   /**
    * Is called when current order number of this column is changed.
    *
    * @param newOrderNumber new order number
    */
   public void setCurrentOrderNumber (int newOrderNumber) {
       properties.setInt (getID () + ".currentOrderNumber", newOrderNumber);
   }

   /**
    * Return column width of this column.
    *
    * @return column width of this column
    */
   public int getColumnWidth () {
       return properties.getInt (getID () + ".columnWidth", 150);
   }

   /**
    * Is called when column width of this column is changed.
    *
    * @param newColumnWidth a new column width
    */
   public void setColumnWidth (int newColumnWidth) {
       properties.setInt (getID () + ".columnWidth", newColumnWidth);
   }

   /**
    * True if column should be visible by default.
    *
    * @return true if column should be visible by default
    */
   public boolean isVisible () {
       return properties.getBoolean (getID () + ".visible", true);
   }

   /**
    * True if column should be sorted by default.
    *
    * @return true if column should be sorted by default
    */
   public boolean isSorted () {
       return properties.getBoolean (getID () + ".sorted", false);
   }

   /**
    * True if column should be sorted by default in descending order.
    *
    * @return true if column should be sorted by default in descending order
    */
   public boolean isSortedDescending () {
       return properties.getBoolean (getID () + ".sortedDescending", false);
   }
}
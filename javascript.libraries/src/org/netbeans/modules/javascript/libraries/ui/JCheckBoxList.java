/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.javascript.libraries.ui;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import org.netbeans.api.project.libraries.Library;


/**
 * CheckBoxList implementation
 * @author Winston Prakash
 */
public class JCheckBoxList extends JList {

    
    private Vector selectedValues = new Vector();
    
    public JCheckBoxList(ListModel dataModel){
        super(dataModel);
        init();
    }
    
    public JCheckBoxList(final Object[] listData){
        super(listData);
        init();
    }
    
    public JCheckBoxList() {
        super();
        init();
        
    }
    
    @Override
    public Object[] getSelectedValues(){
            return selectedValues.toArray(new Object[]{});
    }
    
    /**
     * Initialize the List to act as a check box list
     */
    public void init(){
        setCellRenderer( new CustomListRenderer()  );
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        addMouseListener(new CustomListToggleCheckbox());
        addKeyListener( new CustomListToggleCheckbox() ) ;
    }
    
    /**
     * Custom List Cell renderer that renders a check box 
     */
    private class CustomListRenderer extends JCheckBox implements ListCellRenderer{
        public CustomListRenderer() {
            setHorizontalAlignment(JCheckBox.LEFT);
            setVerticalAlignment(JCheckBox.CENTER);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Library) {
                Library lib = (Library) value;
                this.setText(lib.getDisplayName());
            } else {
                setText(value.toString());
            }
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            setSelected(selectedValues.contains(value));
            
            return this;
        }
    }
    
    /** Create a mouse & key listener to change the
     *  checkbox selection since checkbox will never get
     *  the mouse events for itself to render.  Nor space bar.
     */
    private class CustomListToggleCheckbox extends MouseAdapter implements KeyListener {
        
        public void keyReleased( java.awt.event.KeyEvent e ) {
        }
        public void keyPressed( java.awt.event.KeyEvent e ) {
        }
        public void keyTyped( java.awt.event.KeyEvent ke ) {
            // Luckily, in the common L&Fs (Java L&F, windows L&F, and Motif L&F),
            // the space bar is used to toggle the selection.
            if ( ' ' == ke.getKeyChar() ) { // NOI18N
                toggleCheckBox(ke, false) ;
            }
        }
        
        public void mouseClicked(MouseEvent e) {
            //if (e.getX() < 20){
                toggleCheckBox(e, true) ;
            //}
        }
        /***
         * Toggle the checkbox and add or remove the value to the selected values list
         */
        private void toggleCheckBox(java.awt.AWTEvent e, boolean saveCurrent) {
            JList list = (JList) e.getSource();
            int index = list.getSelectedIndex();
            if (index < 0) return;
            Object selection  = list.getModel().getElementAt(index);
            
            if (selectedValues.contains(selection)){
                selectedValues.remove(selection);
            }else{
               selectedValues.add(selection); 
            }
            list.repaint();  
        }
    } 
    
    
    
}



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.ui.util;

/**
 *
 * @author jalmero
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;


public class PrimitiveTypeSpinnerListModel extends SpinnerListModel {

    private static JSpinner spinner1 = null;
    private static JFormattedTextField tfld = null;
    
    public PrimitiveTypeSpinnerListModel(List<?> values) {
        super(values);    
    }

      public PrimitiveTypeSpinnerListModel(Object[] values) {
        super(values);       
      }
      
      public void setSpinner(JSpinner spinner) {
          spinner1 = spinner;
        spinner1 = spinner;
        if (spinner1 != null) {
            tfld = ((JSpinner.DefaultEditor)spinner1.getEditor()).getTextField();        
        }              
      }
      public Object getNextValue() {
        Object returnValue = super.getNextValue();
        if (returnValue == null) {
          returnValue = getList().get(0);
        }
        return returnValue;
      }

      public Object getPreviousValue() {
        Object returnValue = super.getPreviousValue();
        if (returnValue == null) {
          List list = getList();
          returnValue = list.get(list.size() - 1);
        }
        return returnValue;
      }

      public Object getValue() {
          if (tfld != null) {
              return tfld.getValue(); 
          } else {
              return super.getValue();
          }

      }

      public void setValue(Object obj) {      
            try {
                 super.setValue(obj);
                 if (tfld != null) {
                    tfld.setValue(obj);
                 }
            } 
           catch (IllegalArgumentException ex) {
                 if (tfld != null) {
                    tfld.setValue(obj);
                 }               
                throw ex;            
            }
      }
 
      public void setup() {
          
      }
  public static void main(String args[]) {
    JFrame frame = new JFrame("JSpinner Sample");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    String[] values = new String[]{"a","b","c"};
    String[] newValues = new String[9999];

    for (int i = 0; i < 9999; i++) {
        newValues[i] = String.valueOf(i);
    }
    spinner1 = new JSpinner();
    PrimitiveTypeSpinnerListModel model = new PrimitiveTypeSpinnerListModel( 
                       newValues);
    
    spinner1.setModel(model);
    tfld = ((JSpinner.DefaultEditor)spinner1.getEditor()).getTextField();
    JPanel panel1 = new JPanel(new BorderLayout());
    panel1.add(spinner1, BorderLayout.CENTER);
    JButton btn = new JButton();
    btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("\n****\nVALUE = " + spinner1.getValue());
                 System.out.println("tVALUE = " + tfld.getValue());
                 System.out.println("ttext = " + tfld.getText());
                 
            }
    });
    JButton btn2 = new JButton();
    btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spinner1.setValue("1000");
                 
            }
    });    
    panel1.add(btn, BorderLayout.SOUTH);
    panel1.add(btn2, BorderLayout.EAST);
    frame.add(panel1, BorderLayout.SOUTH);

 

    frame.setSize(200, 90);
    frame.setVisible(true);
   
  }
}

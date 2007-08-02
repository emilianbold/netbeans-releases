package org.netbeans.modules.mobility.deployment.ricoh;

import java.awt.Component;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.openide.util.NbBundle;


//handles mapping of deployment methods
class DeploymentComboBoxModel extends AbstractListModel implements MutableComboBoxModel, ListDataListener {
    
    static String SD_CARD_DEPLOY = "sdcard"; //NOI18N
    static String HTTP_DEPLOY    = "httppost"; //NOI18N
    
    static String deployPropStr = SD_CARD_DEPLOY;
    
    
    static class DeployMethodRenderer extends BasicComboBoxRenderer
    {
        public Component getListCellRendererComponent(
                                                 JList list, 
                                                 Object value,
                                                 int index, 
                                                 boolean isSelected, 
                                                 boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
            setText(findAntItem(value.toString()).getDispName());
            return this;
        }
    }
    
    static class DeployType
    {
        private class MyString
        {
            final String str;
            MyString(String s) 
            {
                str=s;
            }
            
            public boolean equals(Object o)
            {                
                return str.equals(o.toString());
                
            }
            
            public String toString()
            {
                return str;
            }
        }
        
        private String dispName;
        private MyString antName;
        
        public DeployType(String d, String a)
        {
            dispName=d;
            antName=new MyString(a);
        }
        
        public String toString()    { return antName.str; }
        public String getDispName() { return dispName; }
    }
    
    static private DeployType[] methods = 
    {
        new DeployType(NbBundle.getMessage(RicohCustomizerPanel.class, "LBL_Deploy_" + SD_CARD_DEPLOY), SD_CARD_DEPLOY),
        new DeployType(NbBundle.getMessage(RicohCustomizerPanel.class, "LBL_Deploy_" + HTTP_DEPLOY), HTTP_DEPLOY),
    };
    
    
    private DeployType selectedItem;

    static DeployType findAntItem(String selectedItem)
    {
        DeployType type=methods[0];
        for (int i = 0; i < methods.length; i++) 
        {
            if ((selectedItem != null) && (methods[i].antName.equals(selectedItem)))
            {
                type = methods[i];
                return type;
            }
        }
        return type;
    }
    
    DeploymentComboBoxModel(String selectedItem)
    {
        this.selectedItem = findAntItem(selectedItem);
    }
    
    public int getSize() {
        return methods.length;
    }

    public Object getElementAt(int index) {
        return methods[index].antName;
    }

    public void setSelectedItem(Object antItem) {
        selectedItem = findAntItem(antItem.toString());
    }

    public DeployType getSelectedItem(){
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].equals(selectedItem)){
                return methods[i];
            }
        }
        return methods[0];
    }
    
    public void intervalAdded(ListDataEvent e) {
    }

    public void intervalRemoved(ListDataEvent e) {
    }

    public void contentsChanged(ListDataEvent e) {
    }

    public void addElement(Object obj)
    {
    }

    public void removeElement(Object obj)
    {
    }

    public void insertElementAt(Object obj, int index)
    {
    }

    public void removeElementAt(int index)
    {
    }

}
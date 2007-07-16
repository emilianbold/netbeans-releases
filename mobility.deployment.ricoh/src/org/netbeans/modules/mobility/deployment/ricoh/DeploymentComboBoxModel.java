package org.netbeans.modules.mobility.deployment.ricoh;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.openide.util.NbBundle;


//handles mapping of deployment methods
class DeploymentComboBoxModel extends AbstractListModel implements MutableComboBoxModel, ListDataListener {
    
    static String SSH_DEPLOY     = "scp"; //NOI18N
    static String SMB_DEPLOY     = "samba"; //NOI18N
    static String SD_CARD_DEPLOY = "sdcard"; //NOI18N
    static String HTTP_DEPLOY    = "httppost"; //NOI18N
    
    static String deployPropStr = SD_CARD_DEPLOY;
    
    private String[] methodsByDisplayName = 
    {
        NbBundle.getMessage(RicohCustomizerPanel.class, "LBL_Deploy_" + SD_CARD_DEPLOY), NbBundle.getMessage(RicohCustomizerPanel.class, "LBL_Deploy_" + SMB_DEPLOY), NbBundle.getMessage(RicohCustomizerPanel.class, "LBL_Deploy_" + SSH_DEPLOY), NbBundle.getMessage(RicohCustomizerPanel.class, "LBL_Deploy_" + HTTP_DEPLOY) //NOI18N            
    };
    
    private String[] methods = {
            SD_CARD_DEPLOY,
            SMB_DEPLOY,
            SSH_DEPLOY,
            HTTP_DEPLOY
        };
    
    private String selectedItem;

    DeploymentComboBoxModel(String selectedItem)
    {
        for (int i = 0; i < methods.length; i++) 
        {
            if ((selectedItem != null) && (methods[i].equals(selectedItem)))
            {
                this.selectedItem = methodsByDisplayName[i];
                return;
            }
            else
            {
                this.selectedItem = methodsByDisplayName[0];            
            }
        }
    }
    
    public int getSize() {
        return methodsByDisplayName.length;
    }

    public Object getElementAt(int index) {
        return methodsByDisplayName[index];
    }

    public void setSelectedItem(Object anItem) {
        selectedItem = (String) anItem;
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public String getSelectedDeployment(){
        for (int i = 0; i < methodsByDisplayName.length; i++) {
            if (methodsByDisplayName[i].equals(selectedItem)){
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
package org.netbeans.modules.uml.codegen.ui.customizer;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.netbeans.modules.uml.codegen.ui.customizer.TemplateEntry;
import org.netbeans.modules.uml.codegen.ui.customizer.TabbedPanelModel;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Family;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamiliesHandler;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;

/**
 *
 * @author treyspiva
 */

public class TemplateModel extends TabbedPanelModel
{
    private TemplateFamiliesHandler handler = TemplateFamiliesHandler.getInstance(true);
    private UMLProjectProperties umlProjectProperties = null;
    private List<String> checkedTree = null;
    
    public TemplateModel()
    {
        
    }
    
    public TemplateModel(UMLProjectProperties umlPrjProps)
    {
        setUMLProjectProperties(umlPrjProps);
    }
    
    public List<String> getCategories()
    {
        List<String> categories = new ArrayList<String>();
        Family[] families = handler.getTemplateFamilies().getFamily();
        
        for (int i=0; i < families.length; i++)
        {
            categories.add(families[i].getName());
        }

        return categories;
    }

    public String getToolTip(String category)
    {
        return "";
    }

    public JComponent getPanel(String category)
    {
        DomainObject[] domains = null;
        String familyName = null;
        Family[] families = handler.getTemplateFamilies().getFamily();
        
        for (int i=0; i < families.length; i++)
        {
            if (families[i].getName().equals(category))
            {
                domains = families[i].getDomainObject();
                familyName = families[i].getName();
                break;
            }
        }

        Box templateList = Box.createVerticalBox();
        for (DomainObject domain : domains)
        {
            TemplateEntry entry = new TemplateEntry(familyName, domain, 
                fetchCheckedValue(familyName, domain.getName()));
            
            addTemplateEntryCheckEventListener(entry);
            
            templateList.add(entry);
        }

        return templateList;
    }

    
    private boolean fetchCheckedValue(String familyName, String domainName)
    {
        return checkedTree.indexOf(familyName + ':' + domainName) != -1;
    }
    
    private void updateCheckedValue(
        String familyName, String domainName, boolean isChecked)
    {
        if (checkedTree.indexOf(familyName + ':' + domainName) == -1 &&
            isChecked)
        {
            checkedTree.add(familyName + ':' + domainName);
        }
        
        else if (checkedTree.indexOf(familyName + ':' + domainName) != -1 &&
            !isChecked)
        {
            checkedTree.remove(familyName + ':' + domainName);
        }
    }
    
    public void setUMLProjectProperties(UMLProjectProperties val)
    {
        umlProjectProperties = val;
        checkedTree = umlProjectProperties.getCodeGenTemplatesArray();
    }

    public UMLProjectProperties getUMLProjectProperties()
    {
        umlProjectProperties.setCodeGenTemplates(checkedTree);
        return umlProjectProperties;
    }

    private void addTemplateEntryCheckEventListener(TemplateEntry entry)
    {
        entry.getTemplateNameField().addItemListener(new java.awt.event.ItemListener() 
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt) 
            {
                JCheckBox checkBox = (JCheckBox)evt.getSource();
                
                updateCheckedValue(
                    checkBox.getClientProperty("familyName").toString(), 
                    checkBox.getText(), 
                    checkBox.isSelected());
            }
        });
    }    
}

/*
 * JellyComponentGenerator.java
 *
 * Created on November 4, 2002, 3:01 PM
 */

package org.netbeans.modules.testtools.generator;

import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
// TODO - import org.netbeans.core.windows.services.NbPresenter;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.jemmysupport.generator.ComponentGenerator;
import org.netbeans.modules.jemmysupport.generator.ComponentGeneratorPanel;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/** Jelly Tools Component Generator class is extension of Jemmy Component Generator.
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class JellyComponentGenerator extends ComponentGenerator {
    
    static Properties jellyOperators;
    static {
        jellyOperators = new java.util.Properties();
        try {
            jellyOperators.load( JellyComponentGenerator.class.getClassLoader().getResourceAsStream("org/netbeans/modules/testtools/generator/JellyComponentGenerator.properties")); // NOI18N
        } catch (Exception e) {
            e.printStackTrace();
            throw new java.lang.reflect.UndeclaredThrowableException(e, NbBundle.getMessage(ComponentGeneratorPanel.class, "MSG_PropertiesNotLoaded")); // NOI18N
        }
    }        
    
    /** Creates a new instance of JellyComponentGenerator */
    public JellyComponentGenerator(Properties props) {
        super(props);
        addOperatorRecords(jellyOperators);
    }
    
    protected ComponentOperator createOperator(Component comp) {
        if (comp instanceof PropertySheet) return new PropertySheetOperator((JComponent)comp);
        if (comp instanceof TopComponent) return new TopComponentOperator((JComponent)comp);
        // TODO if (comp instanceof NbPresenter) {
        if (comp instanceof JDialog) {
           NbDialogOperator dlg = new NbDialogOperator((JDialog)comp);
           JLabel l;
           if (((l=JLabelOperator.findJLabel((Container)comp, "Steps", true, true))!=null)&&(l.getLabelFor() instanceof JList)) {
               if (dlg.getTitle().startsWith("New Wizard")) {
                   return new NewWizardOperator((JDialog)comp);
               }
               return new WizardOperator((JDialog)comp);
           }
           return dlg;
        }
        if (comp.getClass().getName().equals("org.openide.explorer.view.TreeTable")) return new TreeTableOperator((JTable)comp);
        return super.createOperator(comp);
    }

    static final HashSet NbDialogCaptions=new HashSet();
    static {
        NbDialogCaptions.add("Yes");
        NbDialogCaptions.add("No");
        NbDialogCaptions.add("OK");
        NbDialogCaptions.add("Cancel");
        NbDialogCaptions.add("Close");
        NbDialogCaptions.add("Help");
    };
    static final HashSet WizardCaptions=new HashSet();
    static {
        NbDialogCaptions.add("Next >");
        NbDialogCaptions.add("Back >");
        NbDialogCaptions.add("Finish");
    };
    
    protected ComponentRecord addComponent(ComponentOperator componentOperator, ContainerOperator containerOperator, ComponentRecord parentComponent ) {
        if (containerOperator instanceof NbDialogOperator 
            && componentOperator instanceof JButtonOperator
            &&  NbDialogCaptions.contains(((JButtonOperator)componentOperator).getText()))
                return null;
        if (containerOperator instanceof WizardOperator) {
            if (componentOperator instanceof JButtonOperator
            &&  WizardCaptions.contains(((JButtonOperator)componentOperator).getText()))
                return null;
            if (componentOperator instanceof JListOperator
            &&  ((WizardOperator)containerOperator).lstSteps().getSource()==componentOperator.getSource())
                return null;
        }
        return super.addComponent(componentOperator, containerOperator, parentComponent);
    }
    
    protected boolean isTopComponent(Component comp) {
        return (comp instanceof TopComponent)||super.isTopComponent(comp);
    }
}

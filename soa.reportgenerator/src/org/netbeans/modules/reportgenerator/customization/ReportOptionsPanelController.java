/*
 * ReportOptionsPanelController.java
 * 
 * Created on Oct 15, 2007, 6:56:24 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.reportgenerator.customization;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class ReportOptionsPanelController extends OptionsPanelController {

    private PropertyChangeSupport pSupport = new PropertyChangeSupport(this);
    
    @Override
    public void update() {
        
    }

    @Override
    public void applyChanges() {
        
    }

    @Override
    public void cancel() {
        
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isChanged() {
        return true;
    }

    @Override
    public JComponent getComponent(Lookup arg0) {
        return new ReportCustomizationPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener arg0) {
        pSupport.addPropertyChangeListener(arg0);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener arg0) {
        pSupport.removePropertyChangeListener(arg0);
    }

}

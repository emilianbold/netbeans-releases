/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.options;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/**
 */
public final class RegisteredCategory extends OptionsCategory {
    private static Icon icon;
    private static PropertyChangeListener propertyChangeListener;
    private Collection<String> calls = new HashSet<String>();

    public void setInvalid() {
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, null, null));
    }
    
    public void helpChanged() {
        propertyChangeListener.propertyChange(new PropertyChangeEvent(this, OptionsPanelController.PROP_HELP_CTX, null, null));
    }
    

    public Icon getIcon() {
        if (icon == null) {
            Image image = Utilities.loadImage("org/netbeans/modules/options/resources/generalOptions.png");
            icon = new ImageIcon(image);
        }
        return icon;
    }

    public String getCategoryName() {
        return "CTL_General_Options";
    }

    public String getTitle() {
        return "CTL_General_Options_Title";
    }

    public String getDescription() {
        return "CTL_General_Options_Description";
    }
    
    public void assertThreadingForAllCallsWereTested() {
        TestCase.assertTrue(calls.contains("update()"));
        TestCase.assertTrue(calls.contains("cancel()"));        
        TestCase.assertTrue(calls.contains("isValid()"));
        TestCase.assertTrue(calls.contains("getLookup()"));
        TestCase.assertTrue(calls.contains("getComponent()"));        
        TestCase.assertTrue(calls.contains("applyChanges()"));
        
    }

    public OptionsPanelController create() {
        return new OptionsPanelController() {

            public void update() {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                calls.add("update()");
            }

            public void applyChanges() {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                TestCase.assertTrue(calls.contains("update()"));
                calls.add("applyChanges()");
            }

            public void cancel() {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                TestCase.assertTrue(calls.contains("update()"));
                calls.add("cancel()");
            }

            public boolean isValid() {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                calls.add("isValid()");
                return true;
            }

            public boolean isChanged() {
                return false;
            }

            public HelpCtx getHelpCtx() {
                return null;
            }
            
            public Lookup getLookup() {
                TestCase.assertFalse(SwingUtilities.isEventDispatchThread());
                calls.add("getLookup()");                
                return super.getLookup();
            }
            

            public JComponent getComponent(Lookup masterLookup) {
                TestCase.assertTrue(SwingUtilities.isEventDispatchThread());
                calls.add("getComponent()");
                return new JLabel();
            }

            public void addPropertyChangeListener(PropertyChangeListener l) {
                propertyChangeListener = l;
            }

            public void removePropertyChangeListener(PropertyChangeListener l) {
                propertyChangeListener = null;
            }
        };        
    }
}

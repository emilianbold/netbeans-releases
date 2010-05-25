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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.encoder.coco.ui.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.encoder.coco.ui.CocoEncodingConst;
import org.netbeans.modules.encoder.ui.basic.Utils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class GenerateXSDWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private static final String[] CHARSET_NAMES = Utils.getCharsetNames(false);
    private static final String[] CHARSET_NAMES_EXTRA = Utils.getCharsetNames(true);
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GenerateXSDVisualPanel component;
    
    private String copybookName;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new GenerateXSDVisualPanel();
            component.setCopybookCodePageList(CHARSET_NAMES);
            component.setCopybookCodePage("US-ASCII");  //NOI18N
            component.setDisplayCodePageList(CHARSET_NAMES);
            component.setDisplayCodePage("US-ASCII"); // Originally was "IBM037"
            component.setDisplay1CodePageList(CHARSET_NAMES);
            component.setPredecodeCodingList(CHARSET_NAMES_EXTRA);
            component.setPostencodeCodingList(CHARSET_NAMES_EXTRA);
            component.setCheckReservedWords(true);
            component.setIgnore72ColBeyond(true);
            component.setOverwriteExisting(true);
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(WizardDescriptor settings) {
        copybookName = (String) settings.getProperty(
                        PropertyKey.CURRENT_FILE_NAME);
        if (CocoEncodingConst.isCOBOL(copybookName)) {
            copybookName = copybookName.substring(0, copybookName.lastIndexOf("."));
        }
        component.setTargetNamespace(
                "http://encoder.netbeans.org/coco/" + copybookName);
    }

    public void storeSettings(WizardDescriptor wd) {
        wd.putProperty(PropertyKey.COPYBOOK_CODEPAGE,
                component.getCopybookCodePage());
        wd.putProperty(PropertyKey.DISPLAY_CODEPAGE,
                component.getDisplayCodePage());
        wd.putProperty(PropertyKey.DISPLAY1_CODEPAGE,
                component.getDisplay1CodePage());
        wd.putProperty(PropertyKey.PREDECODE_CODING,
                component.getPredecodeCoding());
        wd.putProperty(PropertyKey.POSTENCODE_CODING,
                component.getPostencodeCoding());
        wd.putProperty(PropertyKey.CHECK_RESERVED_WORDS,
                component.getCheckReservedWords());
        wd.putProperty(PropertyKey.IGNORE_72_COL_BEYOND,
                component.getIgnore72ColBeyond());
        wd.putProperty(PropertyKey.OVERWRITE_EXIST,
                component.getOverwriteExisting());
        wd.putProperty(PropertyKey.TARGET_NAMESPACE,
                component.getTargetNamespace());
    }
}

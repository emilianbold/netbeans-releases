/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.platform.wizard;

import java.awt.Component;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import junit.framework.*;
import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.event.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.*;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;
import org.netbeans.spi.java.platform.PlatformInstall;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;

/**
 *
 * @author Tomas Zezula
 */
public class PlatformInstallIteratorTest extends NbTestCase {
    
    public PlatformInstallIteratorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(PlatformInstallIteratorTest.class);
        
        return suite;
    }
    
    public void testSinglePlatformInstall () throws IOException {
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest(new GeneralPlatformInstall[] {
            new FileBasedPlatformInstall ("FileBased1", new WizardDescriptor.Panel[] {
                new Panel ("FileBased1_panel1")
            })
        });
        PlatformInstallIterator iterator = PlatformInstallIterator.create();
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 1, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof LocationChooser.Panel);
        ((JFileChooser)panel.getComponent()).setSelectedFile(this.getWorkDir());    //Select some folder
        assertTrue ("LocationChooser is not valid after folder was selected",panel.isValid());
        assertTrue ("Should have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
        iterator.nextPanel();
        assertEquals("Invalid state", 2, iterator.getPanelIndex());
        panel = iterator.current();
        assertEquals("Invalid panel","FileBased1_panel1",panel.getComponent().getName());
        assertFalse ("Should not have next panel",iterator.hasNext());
        assertTrue ("Should have previous panel", iterator.hasPrevious());
    }
    
    public void testSingleCustomInstall () throws IOException {
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest(new GeneralPlatformInstall[] {
            new OtherPlatformInstall ("Custom1", new WizardDescriptor.Panel[] {
                new Panel ("Custom1_panel1")
            })
        });
        PlatformInstallIterator iterator = PlatformInstallIterator.create();
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 3, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertEquals("Invalid panel","Custom1_panel1",panel.getComponent().getName());
        assertFalse ("Should not have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
    }
    
    public void testMultipleGenralPlatformInstalls () throws IOException {
        GeneralPlatformInstall[] installers = new GeneralPlatformInstall[] {
            new FileBasedPlatformInstall ("FileBased1", new WizardDescriptor.Panel[] {
                new Panel ("FileBased1_panel1")
            }),
            new OtherPlatformInstall ("Custom1", new WizardDescriptor.Panel[] {
                new Panel ("Custom1_panel1")
            })
        };
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest (installers);
        PlatformInstallIterator iterator = PlatformInstallIterator.create();        
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 0, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof SelectorPanel.Panel);
        assertTrue ("Installer was not found",((SelectorPanel)panel.getComponent()).selectInstaller(installers[0]));
        assertTrue ("SelectorPanel should be valid",panel.isValid());
        assertTrue ("Should have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
        iterator.nextPanel ();
        assertEquals("Invalid state", 1, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof LocationChooser.Panel);
        assertTrue ("Should have previous panel", iterator.hasPrevious());
        iterator.previousPanel();
        assertEquals("Invalid state", 0, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof SelectorPanel.Panel);
        assertTrue ("Installer was not found",((SelectorPanel)panel.getComponent()).selectInstaller(installers[1]));
        assertTrue ("SelectorPanel should be valid",panel.isValid());
        assertTrue ("Should have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
        iterator.nextPanel ();
        assertEquals("Invalid state", 3, iterator.getPanelIndex());
        panel = iterator.current();
        assertEquals("Invalid panel","Custom1_panel1",panel.getComponent().getName());
        assertFalse ("Should not have next panel",iterator.hasNext());
        assertTrue ("Should have previous panel", iterator.hasPrevious());
    }
    
    public void testMultipleFileBasedPlatformInstalls () throws IOException {
        GeneralPlatformInstall[] installers = new GeneralPlatformInstall[] {
            new FileBasedPlatformInstall ("FileBased1", new WizardDescriptor.Panel[] {
                new Panel ("FileBased1_panel1")
            }),
            new FileBasedPlatformInstall ("FileBased2", new WizardDescriptor.Panel[] {
                new Panel ("FileBased2_panel2")
            }),
        };
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest (installers);
        PlatformInstallIterator iterator = PlatformInstallIterator.create();        
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 0, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof SelectorPanel.Panel);
        assertTrue ("Installer was not found",((SelectorPanel)panel.getComponent()).selectInstaller(installers[0]));
        iterator.nextPanel ();
        assertEquals("Invalid state", 1, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof LocationChooser.Panel);
        PlatformInstall platformInstall = ((LocationChooser.Panel)panel).getPlatformInstall();
        assertEquals ("Invalid PlatformInstall",installers[0],platformInstall);
        iterator.previousPanel();
        assertEquals("Invalid state", 0, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof SelectorPanel.Panel);
        assertTrue ("Installer was not found",((SelectorPanel)panel.getComponent()).selectInstaller(installers[1]));
        iterator.nextPanel ();
        assertEquals("Invalid state", 1, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof LocationChooser.Panel);
        platformInstall = ((LocationChooser.Panel)panel).getPlatformInstall();
        assertEquals ("Invalid PlatformInstall",installers[1],platformInstall);
    }
    
    public void testIteratorWithMorePanels () throws IOException {
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest(new GeneralPlatformInstall[] {
            new OtherPlatformInstall ("Custom1", new WizardDescriptor.Panel[] {
                new Panel ("Custom1_panel1"),
                new Panel ("Custom1_panel2"),
            })
        });
        PlatformInstallIterator iterator = PlatformInstallIterator.create();
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 3, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertEquals("Invalid panel","Custom1_panel1",panel.getComponent().getName());
        assertTrue ("Should have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
        iterator.nextPanel();
        panel = iterator.current();
        assertEquals("Invalid panel","Custom1_panel2",panel.getComponent().getName());
        assertFalse ("Should not have next panel",iterator.hasNext());
        assertTrue ("Should have previous panel", iterator.hasPrevious());
    }
    
    private static class FileBasedPlatformInstall extends PlatformInstall {
        
        private String name;
        private WizardDescriptor.InstantiatingIterator iterator;
        
        public FileBasedPlatformInstall (String name, WizardDescriptor.Panel[] panels) {
            this.name = name;
            this.iterator = new Iterator (panels);
        }

        public WizardDescriptor.InstantiatingIterator createIterator(FileObject baseFolder) {
            return this.iterator;
        }

        public boolean accept(FileObject baseFolder) {
            return true;
        }

        public String getDisplayName() {
            return this.name;
        }                        
    }
    
    private static class OtherPlatformInstall extends CustomPlatformInstall {
        
        private String name;
        private WizardDescriptor.InstantiatingIterator iterator;
        
        public OtherPlatformInstall (String name, WizardDescriptor.Panel[] panels) {
            this.name = name;
            this.iterator = new Iterator (panels);
        }

        public String getDisplayName() {
            return this.name;
        }

        public WizardDescriptor.InstantiatingIterator createIterator () {
            return this.iterator;
        }
                        
    }
    
    private static class Iterator implements WizardDescriptor.InstantiatingIterator {
        
        private WizardDescriptor.Panel[] panels;
        private int index;
        
        public Iterator (WizardDescriptor.Panel[] panels) {
            this.panels = panels;
        }
        
        public void removeChangeListener(ChangeListener l) {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void uninitialize(WizardDescriptor wizard) {
        }

        public void initialize(WizardDescriptor wizard) {
            this.index = 0;
        }

        public void previousPanel() {
            this.index--;
        }

        public void nextPanel() {
            this.index++;
        }

        public String name() {
            return "Test";      //NOI18N
        }

        public Set instantiate() throws IOException {
            return Collections.EMPTY_SET;
        }

        public boolean hasPrevious() {
            return this.index > 0;
        }

        public boolean hasNext() {
            return this.index < (this.panels.length - 1);
        }

        public WizardDescriptor.Panel current() {
            return this.panels[this.index];
        }
        
    }
    
    private static class Panel implements WizardDescriptor.Panel {        
        
        private JPanel p;
        private String name;
        
        public Panel (String name) {
            this.name = name;
        }
        
        public void removeChangeListener(ChangeListener l) {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void storeSettings(Object settings) {
        }

        public void readSettings(Object settings) {
        }

        public boolean isValid() {
            return true;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public Component getComponent() {
            if (this.p == null) {
                p = new JPanel ();
                p.setName(this.name);
            }
            return p;
        }
        
    }
    
}

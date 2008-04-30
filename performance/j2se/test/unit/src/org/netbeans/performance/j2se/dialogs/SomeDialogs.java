package org.netbeans.performance.j2se.dialogs;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.Bundle;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

public class SomeDialogs extends PerformanceTestCase {

    protected static String menu;

    public SomeDialogs(String name) {
        super(name);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public void testServerDialog() {
        menu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools") +
                "|" +
                Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "CTL_ServerManager");
        doMeasurement();
    }

    public void testTemplateDialog() {
        menu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools") +
                "|" +
                Bundle.getStringTrimmed("org.netbeans.modules.favorites.templates.Bundle", "LBL_TemplatesAction_Name");
        doMeasurement();
    }

    public void testOptionsDialog() {
        menu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools") +
                "|" +
                Bundle.getStringTrimmed("org.netbeans.modules.options.Bundle", "CTL_Options_Window_Action");
        
         doMeasurement();
    }

    public void testOpenProjectDialog() {
                menu= Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/File") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle","LBL_OpenProjectAction_Name");
        //TITLE = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle","LBL_PrjChooser_Title");

         doMeasurement();
    }

    public ComponentOperator open() {
        MainWindowOperator.getDefault().menuBar().pushMenu(menu);
        return null;
    }

    /*    @Override
    public void close() {
    }*/
    
    @Override
    public void setUp() {
    }

    public void prepare() {
        org.netbeans.modules.performance.utilities.CommonUtilities.closeMemoryToolbar();
    }
    /*    @Override
    public void initialize() {
    
    }*/
    /*    @Override
    public void tearDown() {
    }*/
}
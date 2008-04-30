package org.netbeans.performance.j2se;

import org.netbeans.performance.j2se.menus.MainMenu;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;


public class MeasureMenusTest extends NbTestCase {
    public MeasureMenusTest(String name) {
	super(name);
    }
    
    public static Test suite() {
        
        NbTestSuite s = new NbTestSuite();

	s.addTest(NbModuleSuite.create(MainMenu.class,".*",".*"));
        
        return s;
    }
    
  
}
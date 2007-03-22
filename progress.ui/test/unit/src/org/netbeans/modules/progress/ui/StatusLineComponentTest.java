/*
 * StatusLineComponentTest.java
 * JUnit based test
 *
 * Created on March 20, 2007, 2:37 PM
 */

package org.netbeans.modules.progress.ui;

import junit.framework.TestCase;

/**
 *
 * @author mkleint
 */
public class StatusLineComponentTest extends TestCase {
    
    public StatusLineComponentTest(String testName) {
        super(testName);
    }
    
    public void testGetBarString() {
        double percentage = 0.0;
        long estimatedCompletion = -1;
        String result = StatusLineComponent.getBarString(percentage,
                                                         estimatedCompletion);
        assertEquals("0.0%", result);
        
        percentage = 0.5;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("0.50%", result);
        
        percentage = 1.0;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("1%", result);
        
        percentage = 50.0;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("50%", result);
        
        percentage = 99.33;
        result = StatusLineComponent.getBarString(percentage, estimatedCompletion);
        assertEquals("99.33%", result);
    }

}

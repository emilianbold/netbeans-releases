/*
 * KitsTrackerTest.java
 *
 * Created on March 7, 2007, 2:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.editor.impl;

import java.lang.reflect.Method;
import javax.swing.text.EditorKit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.lib.KitsTracker;

/**
 *
 * @author vita
 */
public class KitsTrackerTest extends NbTestCase {
    
    /** Creates a new instance of KitsTrackerTest */
    public KitsTrackerTest(String name) {
        super(name);
    }
    
    // o.n.editor.BaseKit uses similar code
    public void testKitsTrackerCallable() throws Exception {
        Class clazz = getClass().getClassLoader().loadClass("org.netbeans.modules.editor.lib.KitsTracker"); //NOI18N
        Method getInstanceMethod = clazz.getDeclaredMethod("getInstance"); //NOI18N
        Method findMimeTypeMethod = clazz.getDeclaredMethod("findMimeType", Class.class); //NOI18N
        Object kitsTracker = getInstanceMethod.invoke(null);
        String mimeType = (String) findMimeTypeMethod.invoke(kitsTracker, EditorKit.class);
        assertNull("EditorKit.class should not have a mime type", mimeType);
    }

    public void testKitsTrackerImplInstalled() throws Exception {
        KitsTracker tracker = KitsTracker.getInstance();
        assertEquals("Wrong KitsTracker implementation installed", KitsTrackerImpl.class, tracker.getClass());
    }
}

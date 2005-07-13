/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.run;

import java.io.File;
import java.net.URL;
import java.util.Set;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.openide.windows.OutputListener;

/**
 * Trick to let {@link AntSession}, {@link AntEvent}, and {@link TaskStructure}
 * be final classes when naturally they should be interfaces because their
 * implementation is elsewhere.
 * @see "#45491"
 * @author Jesse Glick
 */
public final class LoggerTrampoline {
    
    private LoggerTrampoline() {}
    
    public interface Creator {
        AntSession makeAntSession(AntSessionImpl impl);
        AntEvent makeAntEvent(AntEventImpl impl);
        TaskStructure makeTaskStructure(TaskStructureImpl impl);
    }
    
    public static Creator ANT_SESSION_CREATOR, ANT_EVENT_CREATOR, TASK_STRUCTURE_CREATOR;
    static {
        Class c1 = AntSession.class;
        try {
            Class.forName(c1.getName(), true, c1.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Class c2 = AntEvent.class;
        try {
            Class.forName(c2.getName(), true, c2.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Class c3 = TaskStructure.class;
        try {
            Class.forName(c3.getName(), true, c3.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        assert ANT_SESSION_CREATOR != null && ANT_EVENT_CREATOR != null && TASK_STRUCTURE_CREATOR != null;
    }
    
    public interface AntSessionImpl {
        File getOriginatingScript();
        String[] getOriginatingTargets();
        Object getCustomData(AntLogger logger);
        void putCustomData(AntLogger logger, Object data);
        void println(String message, boolean err, OutputListener listener);
        void deliverMessageLogged(AntEvent originalEvent, String message, int level);
        void consumeException(Throwable t) throws IllegalStateException;
        boolean isExceptionConsumed(Throwable t);
        int getVerbosity();
        String getDisplayName();
        OutputListener createStandardHyperlink(URL file, String message, int line1, int column1, int line2, int column2);
    }
    
    public interface AntEventImpl {
        AntSession getSession();
        void consume() throws IllegalStateException;
        boolean isConsumed();
        File getScriptLocation();
        int getLine();
        String getTargetName();
        String getTaskName();
        TaskStructure getTaskStructure();
        String getMessage();
        int getLogLevel();
        Throwable getException();
        String getProperty(String name);
        Set/*<String>*/ getPropertyNames();
        String evaluate(String text);
    }
    
    public interface TaskStructureImpl {
        String getName();
        String getAttribute(String name);
        Set/*<String>*/ getAttributeNames();
        String getText();
        TaskStructure[] getChildren();
    }
    
}

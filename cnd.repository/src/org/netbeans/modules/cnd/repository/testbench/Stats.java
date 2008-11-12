/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.repository.testbench;

import org.netbeans.modules.cnd.repository.spi.Key;

/**
 *
 * @author Sergey Grinev
 */
public class Stats {
    
    private Stats() {}
    
    public static final int debugPut = 0;
    public static final int debugGot = 0;
    public static final int debugReadFromFile = 0;
    public static final int debugNotFound = 0;
    public static final int debugGotFromHardCache = 0;
    public static final int nullDataTriggered = 0;
    
    public static final boolean monitorRemovedKeys = getBoolean("cnd.repository.monitor.removed.keys", false); //NOI18N
    
    public static final boolean isDebug = getBoolean("cnd.repository.use.dev", false); //NOI18N
    public static final boolean verbosePut = getBoolean("cnd.repository.verbose.put", false); //NOI18N
    public static final boolean validatePut = getBoolean("cnd.repository.validate.put", false); //NOI18N
    public static final boolean validateKeys = getBoolean("cnd.repository.validate.keys", false); //NOI18N
    public static final boolean rememberKeys = getBoolean("cnd.repository.remember.keys", false); //NOI18N
    public static final boolean useNullWorkaround = getBoolean("cnd.repository.workaround.nulldata", false); //NOI18N
    
    public static final boolean useHardRefRepository = getBoolean("cnd.repository.hardrefs", false); //NOI18N
    public static final boolean queueTiming = getBoolean("cnd.repository.queue.timing", false); //NOI18N
    public static final boolean queueTrace = getBoolean("cnd.repository.queue.trace", false); //NOI18N
    public static final boolean queueUseTicking = getBoolean("cnd.repository.queue.ticking", true); //NOI18N
    
    public static final int fileStatisticsLevel = getInteger("cnd.repository.file.stat", 0); //NOI18N
    public static final int fileStatisticsRanges = getInteger("cnd.repository.file.stat.ranges", 10); //NOI18N

    public static final boolean writeStatistics = getBoolean("cnd.repository.write.stat", false); //NOI18N
    
    public static final boolean multyFileStatistics = getBoolean("cnd.repository.mf.stat", false); //NOI18N

    public static final boolean memoryCacheHitStatistics = getBoolean("cnd.repository.mem.cache.stat", false); //NOI18N
    
    public static final boolean dumoFileOnExit = getBoolean("cnd.repository.dump.on.exit", false); //NOI18N
    public static final int maintenanceInterval = getInteger("cnd.repository.queue.maintenance", 500); //NOI18N
    public static final boolean allowMaintenance = getBoolean("cnd.repository.defragm", true); //NOI18N
    
    public static final int fileRWAccess = getInteger("cnd.repository.rw", 0); //NOI18N
    public static final int bufSize = getInteger("cnd.repository.bufsize", -1); //NOI18N
    public static final boolean useCompactIndex = getBoolean("cnd.repository.compact.index", true); //NOI18N
    
    public static final String traceKeyName = System.getProperty("cnd.repository.trace.key"); //NOI18N
    public static final boolean traceKey = (traceKeyName != null); //NOI18N
    
    public static final boolean traceDefragmentation = getBoolean("cnd.repository.trace.defragm", false); //NOI18N

    public static final boolean hardFickle = getBoolean("cnd.repository.hard.fickle", false); //NOI18N
    public static final int defragmentationThreashold = getInteger("cnd.repository.defragm.threshold", 50); //NOI18N
            
    public final static String ENCODING = System.getProperty("file.encoding"); // NOI18N
    
    public final static boolean TRACE_REPOSITORY_TRANSLATOR = getBoolean("cnd.repository.trace.translator", false); //NOI18N
    public final static boolean TRACE_UNIT_DELETION = getBoolean("cnd.repository.trace.unit.deletion", false); //I18N
    
    public static final boolean isTraceKey(Key key) {
	if( traceKey ) {
	    if( key != null ) {
		for (int i = 0; i < key.getDepth(); i++) {
		    if( traceKeyName.equals(key.getAt(i)) ) {
			return true;
		    }
		}
	    }
	}
	return false;
    }
    
    public static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if( text != null ) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }
    
    public static int getInteger(String name, int result) {
        String text = System.getProperty(name);
        if( text != null ) {
            result = Integer.parseInt(text);
        }
        return result;
    }
    
    public static void report(String st) {
        log(
                "Put: " + debugPut + //NOI18N
                "; Got: " + debugGot + //NOI18N
                "; Read: " + debugReadFromFile + //NOI18N
                "; N/A: " + debugNotFound //NOI18N
                + "; Hard: " + debugGotFromHardCache //NOI18N
                + st);
    }
    
    public static void report() {
        report("");
    }
    
    public static void report(int hard, int soft) {
        report("; in Hard cache: " + hard + "; in Soft cache <"+soft); // NOI18N
    }
    
    public static void log(String st) {
        if (useNullWorkaround) {
            st += "; NULL: " + nullDataTriggered; //NOI18N
        }
        if (isDebug) { System.err.println("DEBUG [Repository] " + st); } //NOI18N
    }
    
}

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

package org.netbeans.modules.cnd.repository.sfs.statistics;

import java.io.PrintStream;
import java.util.*;

/**
 * Base class for collecting simple statistics
 * @author Vladimir Kvashin
 */
public abstract class BaseStatistics<K> {

    protected int min = 0;
    protected int max = 0;
    protected int cnt = 0;
    
    protected int sum;
    
    protected String text;
    
    protected Map<K, Integer> values;
    
    protected int level;
    
    protected static final int LEVEL_NONE = 0;
    protected static final int LEVEL_MINUMUN = 1;
    protected static final int LEVEL_MEDIUM = 2;
    protected static final int LEVEL_MAXIMUM = 3;
    
    public BaseStatistics(String text, int level) {
	this.text = text;
	this.level = level;
	if( level > LEVEL_MINUMUN ) {
	    values = new TreeMap<K, Integer>();
	}
    }    
    
    public void consume(K key, int value) {
	if( value > max ) {
	    max = value;
	}
	if( value < min ) {
	    min = value;
	}
	cnt++;
	sum += value;
	if( values != null ) {
	    Integer count = values.get(value);
	    values.put(key, Integer.valueOf((count == null) ? 1 : count.intValue() + 1));
	}
    }    

    public void print(PrintStream ps) {
	int avg = (cnt == 0) ? 0 : sum / cnt;
	ps.printf("%s %8d min    %8d max    %8d avg\n", text, min, max, avg);	// NOI18N
	if( values != null ) {
	    printDistribution(ps);
	}
    }
    
    protected void printDistributionDetailed(PrintStream ps) {
	for( Map.Entry<K, Integer> entry : values.entrySet() ) {
	    ps.printf("\t%8d %8d\n", entry.getKey(), entry.getValue());	// NOI18N
	}
    }
    
    protected void printDistribution(PrintStream ps) {
	ps.printf("\tDistribution:\n");	// NOI18N
	printDistributionDetailed(ps);
    }
    
}

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

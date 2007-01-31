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

package dwarfvsmodel;

import com.sun.crypto.provider.DESCipher;
import java.io.PrintStream;
import java.util.*;

/**
 * Parses command line; 
 * stores options
 * @author Vladimir Kvashin
 */
public class Config {
    
    /** Exception that is thrown in the case of illegal argument(s) */
    public static class WrongArgumentException extends Exception {
	public WrongArgumentException(String message) {
	    super(message);
	}
    }
   
    /**
     * Base class that represents a single option.
     * Contains all functionality except for value type related.
     */
    public static abstract class Option {
	
	/** A key for identifying this option - setting via config file, system property, etc */
	private String key;
	
	/** A brief, single-line, option description */
	private String description;
	
	/** Possible command line switches */
	private String[] switches;
	
	/** 
	 * Command line switch that was used 
	 * (this field is used mainly for error reporting) 
	 */
	private String usedSwitch;
	
	/**
	 * @param key A key for identifying this option - setting via config file, system property, etc
	 * @param description A brief, single-line, description of this option
	 * @param switches Possible command line switches
	 */
	public Option(String key, String description, String ... switches) {
	    this.key = key;
	    this.description = description;	    
	    this.switches = switches;
	    usedSwitch = switches[0];
	}
	
	/** Gets a key for identifying this option - setting via config file, system property, et */
	public String getKey() {
	    return key;
	}
	
	/** Gets a brief, single-line option description */ 
	public String getDescription() {
	    return description;
	}
	
	/** Determines whether the given switch corresponds to this option */
	protected boolean accept(String sw) {
	    for (int i = 0; i < switches.length; i++) {
		if( switches[i].equals(sw) ) {
		    usedSwitch = sw;
		    return true;
		}
	    }
	    return false;
	}
	
	/** Determines whether this option needs value or not */ 
	protected abstract boolean needsValue();
	
	/** 
	 * Is called for each argument that follows this option's switch 
	 * (i.e. the switch that is accepted by accept method)
	 * 
	 */
	protected abstract void parseValue(String value) throws WrongArgumentException;
	
	
	/**
	 * Gets a command line switch that was used 
	 * (this field is used mainly for error reporting) 
	 */
	protected String getUsedSwitch() {
	    return usedSwitch;
	}
	
	/** Returns the value of this option converted to string */
	protected abstract String getStringValue();
    }
    
    /**
     * Represents a single option of boolean type
     */
    public static class BooleanOption extends Option {
	
	boolean value;
	
	public BooleanOption(String key, String description, boolean defaultValue, String ... switches) {
	    super(key, description, switches);
	    this.value = defaultValue;
	}

	public boolean needsValue() {
	    return false;
	}
	
	public void  parseValue(String value) throws WrongArgumentException {
	    throw new WrongArgumentException(getUsedSwitch() + " option should bow be followed by a value"); //NOI18N
	}
	
	public boolean getValue() {
	    return value;
	}

	protected boolean accept(String sw) {
	    boolean reverse = sw.endsWith("-");
	    if( reverse ) {
		sw = sw.substring(0, sw.length()-1);
	    }
	    boolean accept = super.accept(sw);
	    if( accept ) {
		value = ! reverse;
	    }
	    return accept;
	}
	
	protected String getStringValue() {
	    return value ? "yes" : "no";
	}
    }
    
    public static class StringOption extends Option {
	
	String value;
	
	public StringOption(String key, String description, String defaultValue, String ... switches) {
	    super(key, description, switches);
	    this.value = defaultValue;
	}

	public boolean needsValue() {
	    return true;
	}
	
	public void  parseValue(String value) throws WrongArgumentException {
	    this.value = value;
	}
	
	public String getValue() {
	    return value;
	}	
	
	protected String getStringValue() {
	    return value;
	}
	
    }
    
    private Map<String, Option> options = new HashMap<String, Option>();
    
    public void addBooleanOption(String key, String description, boolean defaultValue, String ... switches) {
	addOption(new BooleanOption(key, description, defaultValue, switches));
    }
    
    public void addStringOption(String key, String description, String defaultValue, String ... switches) {
	addOption(new StringOption (key, description, defaultValue, switches));
    }
    
    public Collection<Option> getOtions() {
	return options.values();
    }
    
    private void addOption(Option option) {
	for( Option curr : options.values() ) {
	    for (int i = 0; i < curr.switches.length; i++) {
		for (int j = 0; j < option.switches.length; j++) {
		    if( option.switches[j].equals(curr.switches[i]) ) {
			throw new IllegalArgumentException("Duplicate option switches"); //NOI18N
		    }
		}
	    }
	}
	options.put(option.getKey(), option);
    }
    
    public void parse(String[] args) throws WrongArgumentException {
	for (int i = 0; i < args.length; i++) {
	    for( Option option : options.values() ) {
		if( option.accept(args[i]) ) {
		    if( option.needsValue() && i < args.length) {
			option.parseValue(args[++i]);
		    }
		}
	    }
	}
    }
    
    public void dump(PrintStream ps) {
	for( Option option : options.values() ) {
	    ps.println(option.getDescription() + " \t" + option.getStringValue());
	}
    }
}

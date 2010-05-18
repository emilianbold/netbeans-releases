/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
		    used();
		    return true;
		}
	    }
	    return false;
	}
	
	/** Is called when this option has been set */
	protected void used() {
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
	
	private boolean value;
	
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
	    boolean reverse = sw.endsWith("-"); // NOI18N
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
	    return value ? "yes" : "no"; // NOI18N
	}
    }
    
    public static class StringOption extends Option {
	
	private String value;
	
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
    
    public static class StringListOption extends Option {
	
	private List<String> values = new ArrayList<String>();
	
	public StringListOption(String key, String description, String ... switches) {
	    super(key, description, switches);
	}

	public boolean needsValue() {
	    return true;
	}
	
	public void  parseValue(String value) throws WrongArgumentException {
	    this.values.add(value);
	}
	
	public List<String> getValue() {
	    return values;
	}	
	
	protected String getStringValue() {
	    return values.toString();
	}
	
    }    
    
    /** Maps option keys to options */
    private Map<String, Option> map = new HashMap<String, Option>();
    
    /** Holds a list of options in order they were added */
    private List<Option> list = new ArrayList<Option>();
    
    private List<String> parameters = new ArrayList<String>();
    
    public BooleanOption addBooleanOption(String key, String description, boolean defaultValue, String ... switches) {
	BooleanOption option = new BooleanOption(key, description, defaultValue, switches);
	addOption(option);
	return option;
    }
    
    public StringOption addStringOption(String key, String description, String defaultValue, String ... switches) {
	StringOption option = new StringOption (key, description, defaultValue, switches);
	addOption(option);
	return option;
    }
    
    public StringListOption addStringListOption(String key, String description, String ... switches) {
	StringListOption option = new StringListOption (key, description, switches);
	addOption(option);
	return option;
    }
    
    /** Gets a list of options in order they were added */
    public List<Option> getOptions() {
	return list;
    }
    
    public List<String> getParameters() {
	return parameters;
    }
    
    private void addOption(Option option) {
	for( Option curr : map.values() ) {
	    for (int i = 0; i < curr.switches.length; i++) {
		for (int j = 0; j < option.switches.length; j++) {
		    if( option.switches[j].equals(curr.switches[i]) ) {
			throw new IllegalArgumentException("Duplicate option switches: " + // NOI18N
				option.switches[j] + " and " + curr.switches[i]); //NOI18N
		    }
		}
	    }
	}
	map.put(option.getKey(), option);
	list.add(option);
    }

    private Option findOption(String sw) throws WrongArgumentException {
	for( Option option : map.values() ) {
	    if( option.accept(sw) ) {
		return option;
	    }
	}
	throw new WrongArgumentException("Unsupported option -" + sw); //NOI18N
    }
    

    
    private Iterator<String> convert(final String[] args) {
	return new Iterator<String>() {
	    private int cursor = 0;
	    public boolean hasNext() {
		return cursor < args.length;
	    }
	    public void remove() {
		throw new UnsupportedOperationException();
	    }

	    public String next() {
		return args[cursor++];
	    }
	    
	};
    }    
    
    private void parse(Option option, Iterator<String> it) throws WrongArgumentException {
	if( option.needsValue() ) {
	    if( it.hasNext() ) {
		String value = it.next();
		if( value.startsWith("-") ) { // NOI18N
		    throwRequiresValue(option);
		}
		else {
		    option.parseValue(value);
		}
	    }
	    else {
		throwRequiresValue(option);
	    }
	}
    }
    
    private void throwRequiresValue(Option option) throws WrongArgumentException {
	throw new WrongArgumentException("Option -" + option.getUsedSwitch() + " requires a value"); //NOI18N
    }
    
    public void parse(String[] args) throws WrongArgumentException {

	Iterator<String> it = convert(args);
	while( it.hasNext() ) {
	    String arg = it.next();
	    if( arg.startsWith("--") ) { // NOI18N
		parse(findOption(arg.substring(2)), it);
	    }
	    else if( arg.startsWith("-") ) { // NOI18N
		Option option = findOption(arg.substring(1, 2));
		if( arg.length() == 2 ) {
		    // just one flag
		    parse(option, it);
		}
		else {
		    // several flags or value?
		    if( option.needsValue() ) {
			option.parseValue(arg.substring(2));
		    }
		    else {
			for( int pos = 2; pos < arg.length(); pos++ ) {
			    Option nextOption = findOption(arg.substring(pos, pos+1));
			    if( nextOption.needsValue() ) {
				throwRequiresValue(option);
			    }
			}
		    }
		}
	    }
	    else {
		parameters.add(arg);
	    }
	}
    }
    
    public void readProperties(Properties props) throws WrongArgumentException {
	for( Map.Entry entry : props.entrySet() ) {
	    Option option = map.get(entry.getKey());
	    if( option != null ) {
		option.used();
		if( option.needsValue() ) {
		    option.parseValue((String) entry.getValue());
		}
	    }
	}
    }
        
    public Option getOption(String key) {
	Option option = map.get(key);
	if( option == null ) {
	    throw new IllegalArgumentException("No such option: " + key); //NOI18N
	}
	return option;
    }
    
    public String getStringValue(String key) {
	return getOption(key).getStringValue();
    }
        
    public void dump(PrintStream ps) {
	for( Option option : getOptions() ) {
	    ps.println(option.getDescription() + ": \t" + option.getStringValue()); // NOI18N
	}
	ps.println("Parameters:"); // NOI18N
	for( String parameter : parameters ) {
	    ps.println(parameter);
	}
    }
}

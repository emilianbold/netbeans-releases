/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.SwingUtilities;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.netbeans.api.javahelp.Help;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

/**
 *
 * @author  vkraemer
 */
public class Utils implements org.netbeans.modules.j2ee.sun.share.Constants {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }
    
    private static final String KEYSEPSTR = "|"; // NOI18N
    
    private static ResourceBundle ubundle =
        ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.share.configbean.Bundle"); // NOI18N
            
    public static String getFQNKey(String uri, String fname) {
        String key = ""; 
        if (null != uri)
            key += uri;
        if (key.indexOf(KEYSEPSTR)>-1)
            throw new IllegalArgumentException("uri");
        key += KEYSEPSTR + fname;
        assert !key.equals(KEYSEPSTR);
        return key;
    }
    
    public static String getUriFromKey(String key) {
		String uri = "";
		int sepLocation = key.indexOf(KEYSEPSTR);
		if (sepLocation > 1)
			uri = key.substring(0,sepLocation);
		return uri;
    }
    
    public static String getFilenameFromKey(String key) {
		int sepLocation = key.indexOf(KEYSEPSTR);
		//if (sepLocation > 1)
			//uri = key.substring(0,sepLocation);
		String fname = key.substring(sepLocation+1);
		return fname;
    }
    
    public static boolean notEmpty(String testedString) {
        return (testedString != null) && (testedString.length() > 0);
    }	

	public static boolean hasTrailingSlash(String path) {
		return (path.charAt(path.length()-1) == '/');
	}
	
	public static boolean containsWhitespace(String data) {
		boolean result = false;
		
		if(notEmpty(data)) {
			for(int i = 0, datalength = data.length(); i < datalength; i++) {
				if(Character.isSpaceChar(data.charAt(i))) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
    public static boolean isJavaIdentifier(String id) {
		boolean result = true;
		
        if(!notEmpty(id) || !Character.isJavaIdentifierStart(id.charAt(0))) {
            result = false;
        } else {
			for(int i = 1, idlength = id.length(); i < idlength; i++) {
				if(!Character.isJavaIdentifierPart(id.charAt(i))) {
					result = false;
					break;
				}
			}
		}
		
		return result;
    }

    public static boolean isJavaPackage(String pkg) {
		boolean result = false;
		
		if(notEmpty(pkg)) {
			int state = 0;
			for(int i = 0, pkglength = pkg.length(); i < pkglength && state < 2; i++) {
				switch(state) {
				case 0:
					if(Character.isJavaIdentifierStart(pkg.charAt(i))) {
						state = 1;
					} else {
						state = 2;
					}
					break;
				case 1:
					if(pkg.charAt(i) == '.') {
						state = 0;
					} else if(!Character.isJavaIdentifierPart(pkg.charAt(i))) {
						state = 2;
					}
					break;
				}
			}
			
			if(state == 1) {
				result = true;
			}
		}
		
		return result;
    }

	public static CommonDDBean [] listToArray(List list, Class targetClass) {
		CommonDDBean [] result = null;
		if(list != null) {
			int size = list.size();
			if(size != 0) {
				result = (CommonDDBean []) Array.newInstance(targetClass, size);
				for(int i = 0; i < size; i++) {
					CommonDDBean property = (CommonDDBean) list.get(i);
					result[i] = (CommonDDBean) property.clone();
				}
			}
		}
		return result;
	}

	public static List arrayToList(CommonDDBean[] beans) {
		List result = null;

		if(beans != null && beans.length > 0) {
			result = new ArrayList(beans.length+3);
			for(int i = 0; i < beans.length; i++) {
				result.add(beans[i]);
			}
		}

		return result;
	}

	private static final String [] booleanStrings = {
		"0", "1",			// NOI18N
		"false", "true",	// NOI18N
		"no", "yes",		// NOI18N
		"off", "on"			// NOI18N
	};

	public static boolean booleanValueOf(String val) {
		boolean result = false;
		int valueIndex = -1;

		if(val != null && val.length() > 0) {
			for(int i = 0; i < booleanStrings.length; i++) {
				if(val.compareToIgnoreCase(booleanStrings[i]) == 0) {
					valueIndex = i;
					break;
				}
			}
		}

		if(valueIndex >= 0) {
			if(valueIndex%2 == 1) {
				result = true;
			}
		}

		return result;
	}

	public static URL getResourceURL(String resource, Class relatedClass) {
		URL result = null;
		ClassLoader classLoader = relatedClass.getClassLoader();

		if(classLoader instanceof java.net.URLClassLoader) {
			URLClassLoader urlClassLoader = (java.net.URLClassLoader) classLoader;
			result = urlClassLoader.findResource(resource);
		}
                else {
			result = classLoader.getResource(resource);
                }

		return result;
	}

	public static void invokeHelp(String helpId) {
		invokeHelp(new HelpCtx(helpId));
	}
	
	public static void invokeHelp(final HelpCtx helpCtx) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				((Help) Lookup.getDefault().lookup(Help.class))
					.showHelp(helpCtx);
			}
		});
	}

	/** Select an appropriate default value for a cmp-resource
	 * @reurns the value to place in the jndi-name element
	 */
	static String getDefaultCmpResourceJndiName(EjbJarRoot jarDCB) {
		return "jdo/pmf";
	}
	       
    public static java.io.File createDestFile(java.io.File baseDir, String uri, String fname) {
        String fixedUri = uri.replace('.', '_');
        String metaDir = determineMetaDir(fname);
        if (null != metaDir)
            metaDir += File.separator + fname;
        else
            metaDir = fname;
        java.io.File retVal = new java.io.File(baseDir.getAbsolutePath() + File.separator +
            fixedUri + File.separator + metaDir);
        if (! retVal.getParentFile().exists())
            retVal.getParentFile().mkdirs();
        return retVal;        
    }
    
    private static String determineMetaDir(String name) {
        String retVal = null;
        if (name.endsWith("xml"))
            retVal = "META-INF";
        if (name.endsWith("sun-web.xml"))
            retVal = "WEB-INF";
        return retVal;
    }
    
    static ConfigurationException makeCE(String messageKey, Object[] params, Throwable cause) {
        String format = null;
        boolean poorFormat = false;
        try {
            format = ubundle.getString(messageKey);
        }
        catch (RuntimeException re) {
            poorFormat = true;
            jsr88Logger.throwing(Utils.class.getName(), "makeCE", re);
            format = ubundle.getString("DEF_ConfigurationExceptionFormat");
            int len = 1;
            if (null != params)
                len += params.length;
            Object tparams[] = new Object[len];
            tparams[0] = messageKey;
            for (int i = 1; i < len; i++) {
                tparams[i] = params[i-1];
            }
            params = tparams;
        }
            
        String message = MessageFormat.format(format, params);
        ConfigurationException retVal = new ConfigurationException(message);
        if (null != cause) {
            retVal.initCause(cause);
        }
        if (poorFormat)
            jsr88Logger.severe(message);
        return retVal;
    }
    
}

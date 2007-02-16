/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.awt.event.ItemEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Iterator;
import javax.swing.SwingUtilities;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.netbeans.api.javahelp.Help;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.openide.ErrorManager;


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
    
    public static boolean strEmpty(String testedString) {
        return testedString == null || testedString.length() == 0;
    }
    
    public static boolean strEquals(String one, String two) {
        boolean result = false;
        
        if(one == null) {
            result = (two == null);
        } else {
            if(two == null) {
                result = false;
            } else {
                result = one.equals(two);
            }
        }
        return result;
    }
    
    public static boolean strEquivalent(String one, String two) {
        boolean result = false;
        
        if(strEmpty(one) && strEmpty(two)) {
            result = true;
        } else if(one != null && two != null) {
            result = one.equals(two);
        }
        
        return result;
    }
    
    public static int strCompareTo(String one, String two) {
        int result;
        
        if(one == null) {
            if(two == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if(two == null) {
                result = 1;
            } else {
                result = one.compareTo(two);
            }
        }
        
        return result;
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
	
    public static boolean isJavaIdentifier(final String id) {
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

    public static boolean isJavaPackage(final String pkg) {
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
    
    public static boolean isJavaClass(final String cls) {
        return isJavaPackage(cls);
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

	public static CommonDDBean [] listToArray(List list, Class targetClass, String newVersion) {
		CommonDDBean [] result = null;
		if(list != null) {
			int size = list.size();
			if(size != 0) {
				result = (CommonDDBean []) Array.newInstance(targetClass, size);
				for(int i = 0; i < size; i++) {
					CommonDDBean property = (CommonDDBean) list.get(i);
					result[i] = (CommonDDBean) property.cloneVersion(newVersion);
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
            val = val.trim();
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
    
    public static String encodeUrlField(String url) {
        String encodedUrl = url;
        
        // Change spaces to underscores - this step might be redundant now, considering
        // the UTF8 encoding being done now.
        if(encodedUrl != null) {
            encodedUrl = encodedUrl.replace (' ', '_'); //NOI18N
        }
        
        // For each url element, do UTF encoding of that element.
        if(encodedUrl != null) { // see bug 56280
            try {
                StringBuffer result = new StringBuffer(encodedUrl.length() + 10);
                String s[] = encodedUrl.split("/"); // NOI18N
                for(int i = 0; i < s.length; i++) {
                    result.append(java.net.URLEncoder.encode(s[i], "UTF-8")); // NOI18N
                    if(i != s.length - 1) {
                        result.append("/"); // NOI18N
                    }
                }
                encodedUrl = result.toString();
            } catch (Exception ex){
                // log this
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        return encodedUrl;
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
				((Help) Lookup.getDefault().lookup(Help.class)).showHelp(helpCtx);
			}
		});
	}

	public static boolean interpretCheckboxState(ItemEvent e) {
		boolean state = false;
		
		if(e.getStateChange() == ItemEvent.SELECTED) {
			state = true;
		} else if(e.getStateChange() == ItemEvent.DESELECTED) {
			state = false;
		}
		
		return state;
    }
    
	/** Select an appropriate default value for a cmp-resource
	 * @reurns the value to place in the jndi-name element
	 */
	static String getDefaultCmpResourceJndiName(EjbJarRoot jarDCB) {
		return "jdo/pmf";
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
    
    /** This method walks the DCB tree from the root of the DConfigBean tree looking
     *  for the service ref of the specified name.  Used by MessageSecurityProviderImpl.
     * 
     * Optimize later... there is probably a nice OOP way to do this.  I didn't
     * want to build the capability into the DCB tree directly because it's a bit
     * specialized, and performing this search requires access to getChildren()
     * which is package protected and probably should not be public (although getParent()
     * is public, as required by JSR-88 -- go figure).
     */
    public static ServiceRef findServiceRef(SunONEDeploymentConfiguration config, String serviceRefName) {
        ServiceRef result = null;
        BaseRoot rootDCB = config.getMasterDCBRoot();
        
        if(rootDCB instanceof EjbJarRoot) {
            Iterator childIter = rootDCB.getChildren().iterator();
            while(childIter.hasNext() && result == null) {
               Object child = childIter.next();
               if(child instanceof BaseEjb) {
                   Iterator subChildIter = ((BaseEjb) child).getChildren().iterator();
                   while(subChildIter.hasNext() && result == null) {
                       Object subChild = subChildIter.next();
                       if(subChild instanceof ServiceRef) {
                           ServiceRef serviceRef = (ServiceRef) subChild;
                           if(serviceRefName.equals(serviceRef.getServiceRefName())) {
                               result = serviceRef;
                           }
                       }
                   }
               }
           }
        } else {
            Iterator childIter = rootDCB.getChildren().iterator();
            while(childIter.hasNext()) {
                Object child = childIter.next();
                if(child instanceof ServiceRef) {
                    ServiceRef serviceRef = (ServiceRef) child;
                    if(serviceRefName.equals(serviceRef.getServiceRefName())) {
                        result = serviceRef;
                        break;
                    }
                }
            }
        }
        
        return result;
    }
}

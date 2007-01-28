/*
 * ClassDescriptor.java
 *
 * Created on May 14, 2004, 12:51 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

/**
 * This class is to describe the information for a java class
 *
 * @author  cao
 */
public class ClassDescriptor 
{
    // Full path file name, e.g. d:/rave/ejb/src/com/sun/rave/load/EjbLoader.java
    private String fullPathFileName;
    
    // File name with package in the path, e.g. org/netbeans/modules/visualweb/ejb/load/EjbLoader.java
    private String packageFileName;
    
    // Name of the class without package, e.g. EjbLoader
    private String className;
    
    // Name with pacakge, e.g. org.netbeans.modules.visualweb.ejb.load
    private String packageName;
    
    // To indicate it is an inner class
    private boolean innerClass;
 
    
    /**
     * Constructor
     */
    public ClassDescriptor( String className, String packageName, String fullPathFileName, String packageFileName, boolean innerClass ) 
    {
        this.className = className;
        this.packageName = packageName;
        this.fullPathFileName = fullPathFileName;
        this.packageFileName = packageFileName;
        this.innerClass = innerClass;
    }
    
    public ClassDescriptor( String className, String packageName, String fullPathFileName, String packageFileName ) 
    {
        this( className, packageName, fullPathFileName, packageFileName, false );
    }
    
    public String getClassName() { return this.className; }
    public String getPackageName() { return this.packageName; }
    public String getFullPathFileName() { return this.fullPathFileName; }
    public String getPackageFileName() { return this.packageFileName; }
    public boolean isInnerClass() { return this.innerClass; }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "fullPathFileName: " + fullPathFileName + "\n" );
        buf.append( "packageFileName: " + packageFileName + "\n" );
        buf.append( "className: " + className + "\n" );
        buf.append( "packageName: " + packageName + "\n" );
        return buf.toString();
    }
    
}

<?xml version="1.0" encoding="UTF-8"?>

<project name="Mac Installer Properties" basedir="." >
    
    
    <property name="install.dir" value="/Applications/NetBeans"/>
    
    <!-- Base IDE properties   -->   
    <property name="baseide.install.name" value="NetBeans 6.1 Dev ${buildnumber}"/>
    <property name="baseide.version" value="6.1"/>
    <property name="appname" value="NetBeans 6.1 Dev ${buildnumber}"/>
    <property name="appversion" value="6.1 Development Version"/>
    <property name="app.name" value="${install.dir}/${appname}.app"/>
    <property name="nbClusterDir" value="nb6.1"/>      
    
    <!-- Tomcat properties   -->    
    <property name="tomcat.install.dir" value="${install.dir}/apache-tomcat-6.0.16"/>
    <property name="tomcat.version" value="6.0.16"/>
    <property name="tomcat_location" value="tomcat/apache-tomcat-6.0.16.zip"/>
    <property name="tomcat.appname" value="Apache Tomcat ${tomcat.version}"/>
            
    <!-- GlassFish properties   -->   
    <property name="glassfish.install.dir" value="${install.dir}/glassfish-v2ur1"/>
    <property name="glassfish.version" value="v2ur1"/>
    <property name="glassfish_location" value="java/re/glassfish/9.1_01/promoted/fcs/b09d/images/mac/glassfish-image-v2ur1-b09d.jar"/>
    <property name="glassfish.appname" value="GlassFish V2 UR1"/>    
    
    <!-- Open ESB Properties-->    
    <property name="openesb.install.dir" value="${glassfish.install.dir}/addons"/>
    <property name="openesb.version" value="v2p4"/>
    <property name="openesb_location" value="kits/ojc/main/latest/installers/jbi_components_installer.jar"/>
    <property name="openesb_core_source" value="kits/openesb/main/latest/CORE/jbi-core-installer.jar"/>
    <property name="openesb.appname" value="Open ESB V2 Preview 4"/>
                 
    <property name="javase.appname" value="Java SE"/>
    <property name="javaee.appname" value="Web and Java EE"/>
    <property name="ruby.appname" value="Ruby"/>
    <property name="soa.appname" value="SOA"/>
    <property name="cpp.appname" value="C/C++"/>

    <property name="dmg.prefix.name" value="_${prefix}-${buildnumber}"/>
              
</project>
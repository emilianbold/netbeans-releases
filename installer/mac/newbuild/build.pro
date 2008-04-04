<?xml version="1.0" encoding="UTF-8"?>

<project name="Mac Installer Properties" basedir="." >
        
    <property name="install.dir" value="/Applications/NetBeans"/>
    
    <!-- Base IDE properties   -->       
    <property name="baseide.version" value="6.1"/>
    <property name="appname" value="NetBeans 6.1 Dev ${buildnumber}"/> 
    <property name="app.name" value="${install.dir}/${appname}.app"/>
    <property name="nbClusterDir" value="nb6.1"/>      

    <property name="appversion" value="6.1 Development Version"/>
    
    <!-- Tomcat properties   -->    
    <property name="tomcat.install.dir" value="${install.dir}/apache-tomcat-6.0.16"/>
    <property name="tomcat.version" value="6.0.16"/>
    <property name="tomcat_location" value="${binary_cache_host}/tomcat/apache-tomcat-6.0.16.zip"/> 
            
    <!-- GlassFish properties   -->   
    <property name="glassfish.install.dir" value="${install.dir}/glassfish-v2ur2"/>
    <property name="glassfish.version" value="v2ur2"/>
    <property name="glassfish_location" value="${gf_builds_host}/java/re/glassfish/9.1_02/promoted/fcs/latest/images/mac/glassfish-image-SNAPSHOT.jar"/>  
    <property name="glassfish_location_ml" value="${gf_builds_host}/java/re/glassfish/9.1_01/promoted/fcs/b09d/l10n/mac/glassfish-image-v2ur1-b09d-darwin-ml.jar"/>  
    
    <!-- Open ESB Properties-->    
    <property name="openesb.install.dir" value="${glassfish.install.dir}/addons"/>
    <property name="openesb.version" value="v2"/>
    <property name="openesb_location" value="${openesb_builds_host}/kits/ojc/openesb_as9_ur2/latest/installers/jbi_components_installer.jar"/>
    <!--property name="openesb_core_source" value="${openesb_builds_host}/kits/openesb/main/latest/CORE/jbi-core-installer.jar"/-->                  

    <property name="dmg.prefix.name" value="${prefix}-${buildnumber}"/>                         
</project>
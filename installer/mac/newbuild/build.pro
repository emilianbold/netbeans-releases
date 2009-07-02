<?xml version="1.0" encoding="UTF-8"?>

<project name="Mac Installer Properties" basedir="." >
   
    <property name="translatedfiles.src" value="${basedir}/../../../src"/>
        
    <property name="install.dir" value="/Applications/NetBeans"/>
    
    <!-- Base IDE properties   -->       
    <property name="baseide.version" value="Dev"/>
    <property name="appname" value="NetBeans Dev ${buildnumber}"/> 
    <property name="mpkg.name_nb" value="NetBeans Dev ${buildnumber}"/> 
    <property name="app.name" value="${install.dir}/${appname}.app"/>
    <property name="nbClusterDir" value="nb6.7"/>      
    <property name="nb.check.build.number" value="0"/>

    <property name="appversion" value="Development Version"/>
    <property name="nb.display.version.long"  value="Development Version ${buildnumber}"/>
    <property name="nb.display.version.short" value="Dev"/>

    <property name="servicetag.source" value="NetBeans ${nb.display.version.short} Installer"/>
    
    <!-- Tomcat properties   -->    
    <property name="tomcat.version" value="6.0.20"/>
    <property name="tomcat.install.dir" value="${install.dir}/apache-tomcat-${tomcat.version}"/>
    <property name="tomcat_location" value="${binary_cache_host}/tomcat/apache-tomcat-${tomcat.version}.zip"/> 
            
    <!-- GlassFish properties   -->   
    <property name="glassfish.install.dir" value="${install.dir}/glassfish-v2.1"/>
    <property name="glassfish.version" value="v2.1"/>
    <property name="glassfish.display.version" value="V2.1"/>
    <!--<property name="glassfish_location" value="${gf_builds_host}/java/re/glassfish_branch/9.1.1/promoted/fcs/b60e/images/mac/glassfish-image-SNAPSHOT.jar"/>-->
    <property name="glassfish_location"    value="file:${user.home}/releng/hudson/glassfish-image-v2.1-b60e.jar"/>
    <!--<property name="glassfish_location_ml" value="${gf_builds_host}/java/re/glassfish_branch/9.1.1/promoted/fcs/b60e/l10n/mac/glassfish-image-SNAPSHOT-ml.jar"/>-->
    <property name="glassfish_location_ml" value="file:${user.home}/releng/hudson/glassfish-image-v2.1-b60e-ml.jar"/>


    <!-- SJSAS properties   -->
    <property name="sjsas.install.dir" value="${install.dir}/SUNWappserver"/>
    <property name="sjsas.version" value="v2.1"/>
    <property name="sjsas.display.version" value="v2.1"/>
    <property name="sjsas.milestone.number" value="60e"/>
    <property name="sjsas_location" value="${gf_builds_host}/java/re/glassfish_branch/9.1.1/promoted/fcs/b${sjsas.milestone.number}/bundles/appserver_install_image-mac-b${sjsas.milestone.number}.zip"/>
    <!--property name="sjsas_location"    value="file:${user.home}/releng/hudson/appserver_install_image-mac-b${sjsas.milestone.number}.zip"/-->
    <property name="sjsas_location_ml" value="${gf_builds_host}/java/re/glassfish_branch/9.1.1/promoted/fcs/b${sjsas.milestone.number}/l10n-nb/appserver_install_image-mac-b${sjsas.milestone.number}-ml.zip"/>
    <!--property name="sjsas_location_ml" value="file:${user.home}/releng/hudson/appserver_install_image-mac-b${sjsas.milestone.number}-ml.zip"/-->



    <!-- Sun GlassFish V3 properties   -->
    <property name="glassfish.v3.sun.build.type"      value="preview"/>
    <property name="glassfish.v3.sun.location.prefix" value="${gf_builds_host}/java/re/glassfish/10.0/promoted"/>
    <!--
    <property name="glassfish.v3.sun.build.number"    value="28e"/>
    -->
    <loadresource property="glassfish.v3.sun.build.number">
          <url url="${glassfish.v3.sun.location.prefix}/trunk-latest/archive/bundles"/>
          <filterchain>
	    <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)sges-v3-${glassfish.v3.sun.build.type}-b([0-9a-z]+)\.zip(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    <property name="glassfish.v3.sun.display.version" value="v3 b${glassfish.v3.sun.build.number}"/>
    <property name="glassfish.v3.sun.version"      value="b${glassfish.v3.sun.build.number}"/>
    <property name="glassfish.v3.sun.install.dir"  value="${install.dir}/sges-v3-b${glassfish.v3.sun.build.number}"/>
    <property name="glassfish_v3_sun_location"        value="${glassfish.v3.sun.location.prefix}/${glassfish.v3.sun.build.type}/${glassfish.v3.sun.version}/archive/bundles/sges-v3-${glassfish.v3.sun.build.type}-${glassfish.v3.sun.version}.zip"/>
    <property name="glassfish_v3_sun_location_ml" value="${glassfish_v3_sun_location}"/>
    <!--<property name="glassfish_v3_sun_location_ml"     value="${glassfish.v3.sun.location.prefix}/${glassfish.v3.sun.build.type}/${glassfish.v3.sun.version}/archive/bundles-ml/sges-v3-${glassfish.v3.sun.build.type}-ml.zip"/-->
    <!--
    <property name="glassfish_v3_sun_location"     value="${binary_cache_host}/glassfish/sges-v3-prelude.zip"/>
    <property name="glassfish_v3_sun_location_ml"  value="${binary_cache_host}/glassfish/sges-v3-prelude-ml.zip"/>
    -->
    <property name="glassfish.v3.sun.subdir"       value="glassfishv3"/>


    <!-- GlassFish V3 properties   -->   
    <property name="glassfish.v3.build.type"      value="preview"/>
    <property name="glassfish.v3.location.prefix" value="${gf_builds_host}/java/re/glassfish/10.0/promoted"/>
    <!--
    <property name="glassfish.v3.build.number"    value="28c"/>
    -->
    
    <loadresource property="glassfish.v3.build.number">
          <url url="${glassfish.v3.location.prefix}/trunk-latest/archive/bundles"/>
          <filterchain>
	    <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)glassfish-v3-${glassfish.v3.build.type}-b([0-9a-z]+)\.zip(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <property name="glassfish.v3.display.version" value="v3 b${glassfish.v3.build.number}"/>
    <property name="glassfish.v3.version"      value="b${glassfish.v3.build.number}"/>
    <property name="glassfish.v3.install.dir"  value="${install.dir}/glassfish-v3-b${glassfish.v3.build.number}"/>    
    <property name="glassfish_v3_location"     value="${glassfish.v3.location.prefix}/${glassfish.v3.build.type}/${glassfish.v3.version}/archive/bundles/glassfish-v3-${glassfish.v3.build.type}-${glassfish.v3.version}.zip"/>
    <property name="glassfish_v3_location_ml"  value="${glassfish_v3_location}"/>
    <!--
    <property name="glassfish_v3_location"     value="${binary_cache_host}/glassfish/glassfish-v3-prelude.zip"/>
    <property name="glassfish_v3_location_ml"  value="${binary_cache_host}/glassfish/glassfish-v3-prelude-ml.zip"/>
    -->
    <property name="glassfish.v3.subdir"       value="glassfishv3"/>
    
    <!-- Open ESB Properties-->    
    <property name="openesb.install.dir" value="${sjsas.install.dir}/addons"/>
    <property name="openesb.version" value="v2"/>
    <property name="openesb_location" value="${openesb_builds_host}/kits/ojc/openesb_as9_ur2/latest/installers/jbi_components_installer.jar"/>
    <!--property name="openesb_core_source" value="${openesb_builds_host}/kits/openesb/main/latest/CORE/jbi-core-installer.jar"/-->                  

    <!-- JavaFX Properties -->
    <property name="javafx_location"     value="${javafx_builds_host}/lastSuccessfulBuild/artifact/zips/javafx-cluster-mac.zip"/>
    <property name="javafx_sdk_location" value="${javafx_builds_host}/lastSuccessfulBuild/artifact/zips/javafx-sdk-mac.zip"/>


    <property name="dmg.prefix.name" value="${prefix}-${buildnumber}"/>                         

    <property name="mpkg.name_nb_mysql" value="NetBeans IDE with MySQL"/> 
    <property name="mysql.version"      value="5.0.67"/>
    <property name="mysql_10.5.pkg.name" value="mysql-5.0.67-osx10.5-x86"/>
    <property name="mysql_10.4.pkg.name" value="mysql-5.0.67-osx10.4-i686"/>
    <property name="mysql_10.5.dmg.name" value="netbeans-6.5-mysql-macosx10.5-x86"/>
    <property name="mysql_10.4.dmg.name" value="netbeans-6.5-mysql-macosx10.4-x86"/>
    <property name="mysql_startup.pkg.name" value="MySQLStartupItem"/>
    <property name="mysql_prefPane.name" value="MySQL.prefPane"/>
    <property name="mysql_connector.name" value="mysql-connector-java-5.1.6-bin"/>
    <property name="mysql_10.5.location" value="${binary_cache_host}/mysql/${mysql_10.5.pkg.name}.dmg"/>
    <property name="mysql_10.4.location" value="${binary_cache_host}/mysql/${mysql_10.4.pkg.name}.dmg"/>
    <property name="mysql.install.dir" value="/usr/local/mysql"/>
    <property name="mysql_connector.location" value="${binary_cache_host}/mysql/${mysql_connector.name}.jar"/>   
    <property name="mysql_license.name" value="NB_GF_MySQL.txt"/>   
    <property name="mysql_readme.name" value="NB_GF_MySQL_Bundle_Thirdparty_license_readme.txt"/>

</project>

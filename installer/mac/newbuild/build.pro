<?xml version="1.0" encoding="UTF-8"?>

<project name="Mac Installer Properties" basedir="." >

    <property name="packagemaker.path" value="/Developer/Applications/Utilities/PackageMaker.app/Contents/MacOS/PackageMaker"/>
   
    <property name="translatedfiles.src" value="${basedir}/../../../src"/>
        
    <property name="install.dir" value="/Applications/NetBeans"/>
    
    <!-- Base IDE properties   -->       
    <property name="baseide.version" value="Dev"/>
    <property name="appname" value="NetBeans Dev ${buildnumber}"/> 
    <property name="mpkg.name_nb" value="NetBeans Dev ${buildnumber}"/> 
    <property name="app.name" value="${install.dir}/${appname}.app"/>
    <property name="nbClusterDir" value="nb"/>      
    <property name="nb.check.build.number" value="0"/>
    <property name="nb.id" value="${buildnumber}"/>

    <property name="appversion" value="Development Version"/>
    <property name="nb.display.version.long"  value="Development Version ${buildnumber}"/>
    <property name="nb.display.version.short" value="Dev"/>

    <!-- Tomcat properties   -->    
    <property name="tomcat.version" value="7.0.27"/>
    <property name="tomcat.id" value="7.0.27"/>
    <property name="tomcat.install.dir" value="${install.dir}/apache-tomcat-${tomcat.version}"/>
    <property name="tomcat_location" value="${binary_cache_host}/tomcat/apache-tomcat-${tomcat.version}.zip"/> 
            
    <!-- GlassFish V3 properties   -->   
    <property name="glassfish.v3.build.type"      value=""/>
    <!-- /java/re/glassfish/3.1.2.2/promoted/b05/bundles/ -->
    <property name="glassfish.v3.location.prefix" value="${gf_builds_host}/java/re/glassfish/3.1.2.2/promoted"/>
    
    <!--<property name="glassfish.v3.build.number"    value="74b"/>-->
    
    
    <!-- XXX - a hack for 3.1.2 patch2
    <loadresource property="glassfish.v3.build.number">
          <url url="${glassfish.v3.location.prefix}/latest/archive/bundles"/>
          <filterchain>
	    <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)glassfish-3.1.2-b([0-9a-z]+)\.zip(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>-->
    <property name="glassfish.v3.build.number"    value="23"/>
    
    <property name="glassfish.v3.display.version" value="3.1.2.2"/>
    <property name="glassfish.v3.version"      value="b${glassfish.v3.build.number}"/>
    <property name="glassfish.v3.id"           value="b${glassfish.v3.build.number}"/>
    <property name="glassfish.v3.install.dir"  value="${install.dir}/glassfish-3.1.2.2"/>
    <!--
        <property name="glassfish_v3_location"     value="${glassfish.v3.location.prefix}/${glassfish.v3.build.type}/${glassfish.v3.version}/archive/bundles/glassfish-3.1.2-${glassfish.v3.version}.zip"/>
        <property name="glassfish_v3_location_ml"  value="${glassfish.v3.location.prefix}/${glassfish.v3.build.type}/${glassfish.v3.version}/archive/bundles/glassfish-3.1.2-${glassfish.v3.version}-ml.zip"/>
    -->
    <property name="glassfish_v3_location"     value="${glassfish.v3.location.prefix}/b05/bundles/glassfish-3.1.2-2-b05.zip"/>
    <property name="glassfish_v3_location_ml"  value="${glassfish.v3.location.prefix}/b05/bundles/glassfish-3.1.2-2-b05-ml.zip"/>
    <property name="glassfish.v3.subdir"       value="glassfish3"/>
    
    <!-- Java ME SDK 3.0 Properties-->
    <property name="javame_sdk30_bits_location" value="${binary_cache_host}/wtk/javame_sdk_30/mac/java-me-sdk-mac.zip"/>
    <property name="javame_sdk30_bits_update_location" value="${binary_cache_host}/wtk/javame_sdk_30/mac/java-me-sdk-mac-update.zip"/>
    <property name="javame_sdk30_xml_location"  value="${binary_cache_host}/wtk/javame_sdk_30/mac/Java_TM__Platform_Micro_Edition_SDK_3_0.xml"/>

    <!-- Java FX Runtime -->
    <property name="javafx_runtime_location" value="${jdk_builds_host}/java/re/javafx/2.2.0/promoted/fcs/b21/bundles/macosx-x86_64/javafx-runtime-for-cobundle.zip"/>

    <property name="dmg.prefix.name" value="${prefix}-${buildnumber}"/>                         

    <!-- JDK Properties-->    
    <property name="mpkg.prefix_nb_jdk" value=" with JDK"/> 
    <property name="mpkg.version_jdk" value=" 7 Update 09"/> 
    <property name="jdk.bundle.files.prefix" value="jdk-7u9"/>
    <property name="jdk.bundle.files.suffix" value="nb-dev"/>
    <property name="output.jdk7.dir" value="jdk/"/>
    <property name="default.jdk7.home" value="/Library/Java/JavaVirtualMachines/jdk1.7.0_09.jdk/Contents/Home"/>
    <property name="jdk_bits_location" value="${jdk_builds_host}/java/re/jdk/7u9/promoted/fcs/b05/bundles/macosx-x64/jdk-7u9-macosx-x64.dmg"/>
    <property name="jdk.package.name" value="JDK\ 7\ Update\ 09"/>

</project>

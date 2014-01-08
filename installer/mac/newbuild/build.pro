<?xml version="1.0" encoding="UTF-8"?>

<project name="Mac Installer Properties" basedir="." >

    <property name="packagemaker.path" value="/Developer/Applications/Utilities/PackageMaker.app/Contents/MacOS/PackageMaker"/>
   
    <property name="translatedfiles.src" value="${basedir}/../../../src"/>
        
    <property name="install.dir" value="/Applications/NetBeans"/>
    
    <!-- Base IDE properties   -->       
    <property name="baseide.version" value="8.0 Beta"/>
    <property name="appname" value="NetBeans 8.0 Beta"/> 
    <property name="mpkg.name_nb" value="NetBeans 8.0 Beta"/> 
    <property name="app.name" value="${install.dir}/${appname}.app"/>
    <property name="nbClusterDir" value="nb"/>      
    <property name="nb.check.build.number" value="0"/>

    <!-- Unique ID in db/receipts for Development builds -->
    <property name="nb.id" value="${baseide.version}-${buildnumber}"/>
    <!-- Unique ID in db/receipts for release build -->
    <!-- <property name="nb.id" value="${baseide.version}"/>-->

    <property name="appversion" value="8.0 Beta"/>
    <property name="nb.display.version.long"  value="8.0 Beta"/>
    <property name="nb.display.version.short" value="8.0 Beta"/>

    <!-- Tomcat properties   -->    
    <property name="tomcat.version" value="7.0.41"/>
    <property name="tomcat.id" value="7.0.41"/>
    <property name="tomcat.install.dir" value="${install.dir}/apache-tomcat-${tomcat.version}"/>
    <property name="tomcat_location" value="${binary_cache_host}/tomcat/apache-tomcat-${tomcat.version}.zip"/> 
            
    <!-- GlassFish 4 properties   -->   
    <property name="glassfish.build.type"      value=""/>
    <property name="glassfish.location.prefix" value="${gf_builds_host}/java/re/glassfish/4.0/promoted"/>
    
    <loadresource property="glassfish.build.number">
          <url url="${glassfish.location.prefix}/latest/archive/bundles"/>
          <filterchain>
	    <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)glassfish-4.0-b([0-9a-z]+)\.zip(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <property name="glassfish.display.version" value="4.0"/>
    <property name="glassfish.version"      value="b${glassfish.build.number}"/>
    <property name="glassfish.id"           value="${glassfish.display.version}"/>
    <property name="glassfish.install.dir"  value="${install.dir}/glassfish-4.0"/>
    <property name="glassfish_location"     value="${glassfish.location.prefix}/${glassfish.build.type}/${glassfish.version}/archive/bundles/glassfish-4.0-${glassfish.version}.zip"/>
    <property name="glassfish_location_ml"  value="${glassfish.location.prefix}/${glassfish.build.type}/${glassfish.version}/archive/bundles/glassfish-4.0-${glassfish.version}-ml.zip"/>
    <property name="glassfish.subdir"       value="glassfish4"/>
    
    <property name="dmg.prefix.name" value="${prefix}"/>                         

    <!-- JDK Properties-->    
    <property name="jdk.builds.path" value="${jdk_builds_host}/${jdk7_builds_path}/latest/bundles/macosx-x64"/>
    <loadresource property="jdk.version.number">
          <url url="${jdk.builds.path}"/>
          <filterchain>
	    <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)u([0-9]+)-([a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <loadresource property="jdk.update.number">
          <url url="${jdk.builds.path}"/>
          <filterchain>
	    <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)u([0-9]+)-([a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\3" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <loadresource property="jdk.build.type">
          <url url="${jdk.builds.path}"/>
          <filterchain>
	    <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)u([0-9]+)-([a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\4" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    <condition property="jdk.ea.text" value="ea-" else="">
        <equals arg1="${jdk.build.type}" arg2="ea"/>
    </condition>
    
    
    <loadresource property="jdk.build.number">
          <url url="${jdk.builds.path}"/>
          <filterchain>
	    <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)u([0-9]+)-([a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\5" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <property name="mpkg.prefix_nb_jdk" value=" with JDK"/> 
    <property name="mpkg.version_jdk" value=" ${jdk.version.number} Update ${jdk.update.number}"/> 
    <property name="jdk.bundle.files.prefix" value="jdk-${jdk.version.number}u${jdk.update.number}"/>
    <property name="jdk.bundle.files.suffix" value="nb-dev"/>
    <property name="output.jdk7.dir" value="jdk/"/>
    <property name="default.jdk7.home" value="/Library/Java/JavaVirtualMachines/jdk1.${jdk.version.number}.0_${jdk.update.number}.jdk/Contents/Home"/>
    <property name="jdk_bits_location" value="${jdk_builds_host}/${jdk7_builds_path}/all/b${jdk.build.number}/bundles/macosx-x64/jdk-${jdk.version.number}u${jdk.update.number}-${jdk.ea.text}macosx-x64.dmg"/>
    <property name="jdk.package.name" value="JDK\ ${jdk.version.number}\ Update\ ${jdk.update.number}"/>

</project>

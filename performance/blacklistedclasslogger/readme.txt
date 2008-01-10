You can run it with the attached logging configuration file using:
netbeans 
  -cp:p /path/to/BlacklistedClassLogger/dist/BlacklistedClassLogger.jar 
  -J-Djava.util.logging.config.file=log.properties 
  -J-Dorg.netbeans.performance.test.utilities.BlacklistedClassLogger.blacklist.filename=blacklist.txt
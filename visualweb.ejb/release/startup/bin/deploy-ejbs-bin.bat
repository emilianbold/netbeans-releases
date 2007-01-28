@echo off
rem
rem  This deploys the sample ejbs
rem  It's called from the installer in the post-install step.
rem

set cluster=@CLUSTER_DIR@

for /f "usebackq tokens=* delims=\" %%a in ('%0') do (
 set basedir=%%~fsa
)

for /f "usebackq" %%b in ('%basedir%\..') do (
 set upone=%%~dpb
)
set scriptLocation=%upone%\%cluster%\startup\bin

call %scriptLocation%\deploy-ejbs.bat %* 

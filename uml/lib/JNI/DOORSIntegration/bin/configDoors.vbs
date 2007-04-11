Rem *** Please call using "cscript configDoors.vbs"

Rem Get the registry Object
Const HKEY_LOCAL_MACHINE = &H80000002

strComputer = "."

Set oReg=GetObject("winmgmts:{impersonationLevel=impersonate}!\\" & _
    strComputer & "\root\default:StdRegProv")

Set fso = CreateObject("Scripting.FileSystemObject")

Call ConfigDoors

Rem Reset oReg and fso
Set oReg = Nothing
Set fso = Nothing

Function ConfigDoors
    
    Rem*** construct the path of this running script  
 
    scriptFullPath = WScript.ScriptFullName  ' get full path name of the this script
    scriptName = WScript.ScriptName          ' get the name only of this script
    namePos = Instr(scriptFullPath,  scriptName)  'find the index of the script name

    If (namePos = 0) Then   ' if script name is not found, return.
       Exit Function
    End If

    pathLen = Len(scriptFullPath)   ' get the length of the script full path name
    nameLen = Len(scriptName)       ' get the length of the script name

    scriptPath = Left(scriptFullPath, (pathLen - (nameLen+1)))  ' extract the path from the full path name
    WScript.Echo "Path to this script " & scriptPath 

    Rem *** Get Doors Installation Directory from window registry

    doorsKeyPath = "Software\Telelogic\DOORS\7.0"
    doorsInstallDirKey = "InstallationDirectory"
    lRC = oReg.GetStringValue(HKEY_LOCAL_MACHINE, _
        doorsKeyPath, doorsInstallDirKey, doorsInstallDir)
    If (lRC = 0) And (Err.Number = 0) Then
        If (fso.FolderExists(doorsInstallDir)) Then
            WScript.Echo "Doors Install directory " & doorsInstallDir
        Else
            WScript.Echo "Doors installation not found."
            Exit Function
        End If
    Else
        WScript.Echo "Doors installation not found."
        Exit Function
    End If

    Rem *** Copy dxl files

    tgtDir = doorsInstallDir & "\lib\"
    if (fso.FolderExists(tgtDir)) Then
    Else
        fso.createFolder(tgtDir)
    End If
    srcDir = scriptPath & "\lib\dxl"
    WScript.Echo vbNewLine & "Copying files ..." & vbNewLine & " from "  & srcDir & _
        vbNewLine & " to " & tgtDir

    lRC = fso.CopyFolder(srcDir, tgtDir)
    If (lRC = 0) And (Err.Number = 0) Then
          WScript.Echo "Files copied sucessfully."
    Else
          WScript.Echo "Error Occured - Files not copied."
    End If

    Rem *** Create registry entry

    doorsAddinsKeyPath = "Software\Telelogic\DOORS\7.0\Config"
    doorsAddinsValueName = "addins"
    doorsAddinsValue = doorsInstallDir & "\lib\dxl\addins\user"
    WScript.Echo vbNewLine & "Creating a registry key and value for DOORS ..." & _
       vbNewLine & " " & doorsAddinsKeyPath & "\" & doorsAddinsValueName & " = " & doorsAddinsValue

    lRC = oReg.SetStringValue (HKEY_LOCAL_MACHINE, _
        doorsAddinsKeyPath, doorsAddinsValueName, doorsAddinsValue)
    If (lRC = 0) And (Err.Number = 0) Then
          WScript.Echo "Registry added successfully."
    Else
          WScript.Echo "Error Occured - Key not created."
    End If
 

    Rem *** set Doors dll to the PATH environment ***

    WScript.Echo vbNewLine & "Adding Doors dll to the system PATH environment ..." 
    doorsDLLPath = scriptPath & "\modules\bin"

    On Error Resume Next
    strComputer = "."
    Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
    Set colItems = objWMIService.ExecQuery( _
          "Select * from Win32_Environment")
    
    Found = False

    For Each objItem in colItems
      If UCase(objItem.Name) = "PATH" Then
          Found = True
          existingPath = objItem.VariableValue
          Rem WScript.Echo " Old Path = " & existingPath

          Rem *** check if the doors dll has already been in the path
	  If InStr(1,existingPath,doorsDLLPath,1) = 0 Then
             objItem.VariableValue = existingPath & ";" & doorsDLLPath
             objItem.Put_
          End If

	    WScript.Echo " New Path = " &  objItem.VariableValue
          Exit For
       End If
    Next

    If Found = False Then
       WScript.Echo " PATH environment" _
       & " not found."
    End If
End Function

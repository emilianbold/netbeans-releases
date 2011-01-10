property folderPath : "{0}"
property filePath : "{1}"

set the_folder to (POSIX file folderPath) as alias
set the_file to (POSIX file filePath) as alias
tell application "Finder"
        activate
        open the_folder
        select the_file
end tell

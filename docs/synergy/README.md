
# How to install Synergy
#### 1. Install Apache HTTP server, PHP and MYSQL
Install Apache with PHP and MySQL:
https://www.vultr.com/docs/how-to-install-apache-mysql-and-php-on-ubuntu-16-04

Next enable .htaccess using steps from
https://linode.com/docs/web-servers/apache/how-to-set-up-htaccess-on-apache/

#### 2. Get Synergy sources
assuming apache's document root is in `/var/www/html`, in terminal:

```
cd /var/www/html
svn checkout https://svn.netbeans.org/svn/opensynergy~source-code-repository
mv opensynergy~source-code-repository synergy
cd synergy
```
Now you are in the synergy main directory.

#### 3. Create database 
this will also create default project, default version and a new user with username `import` and password `import` - this user is administrator, in terminal:

```
mysql -u root -p < server/db/structure.sql
```

#### 4. If needed change DB connection details
update file `server/setup/conf.php` from line 18 which contains database connection details to match your database credentials

```
define('DHOST', 'mysql:host=localhost;dbname=synergy;charset=UTF8');
define('DUSER', 'user');
define('DPASS', 'password');
define('DB', 'synergy');
define('DBHOST', 'localhost');
```

#### 5. Now Synergy should be almost up and running
you can try it in your browser `http://localhost/synergy/client/app/`

#### 6. Setup directories for attachments and images
By default, images will be stored at `/var/www/media/` and attachments at `/var/www/att/`. Either make sure these 2 paths exist and 
that apache server can write there, or you'll need to change the setting to point to paths which meet the criteria.

The change can be done via Synergy UI in `Administration -> Server` setting where you can find 2 setting fields: `ATTACHMENT_PATH` 
and `IMAGE_PATH`. Note also the `IMAGE_BASE` which must correlate with `IMAGE_BASE`, for instance if `IMAGE_PATH` is `/var/www/html/images`, 
then `IMAGE_BASE` would be `http://localhost/images/` . Please note that these 2 directories must have proper permissions - for local setup 777 is fine

#### 7. Setting up error log file
In terminal, navigate to the Synergy directory and open file `.htaccess`, line 33 starts with `php_value error_log` and 
it ends with path to error log file. Synergy will print error logs there in case something is wrong. 

Either create the default path and file (`/var/www/synergy/server/errors/errors.log`) or change the path in `.htaccess` file to your path. 
Again make sure it is possible to write to file - setting 777 permissions should be on local deployment.

#### 8. Allow Synergy to store static data from test runs
In terminal in synergy directory, execute following:
```
chmod 777 -R server/data
```

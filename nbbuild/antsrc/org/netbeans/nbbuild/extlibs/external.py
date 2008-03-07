import os, re, urllib2, sha, inspect, sys
from mercurial import util, httprepo, ui

if sys.version_info < (2, 4, 2):
    ui.ui().warn('Warning: external hook requires Python 2.4.4+ (2.5.1 preferred)\n')

# Workaround for a Python bug (in linecache.py?):
# http://bugs.python.org/issue1728
# http://bugs.python.org/issue1665
# https://bugs.launchpad.net/ubuntu/+source/python-defaults/+bug/70902
# http://mail.python.org/pipermail/python-bugs-list/2007-March/037472.html
_inspect_findsource_orig = inspect.findsource
def _inspect_findsource_robust(object):
    try:
        return _inspect_findsource_orig(object)
    except IndexError:
        raise IOError('workaround for linecache bug')
inspect.findsource=_inspect_findsource_robust

# Compatibility for Hg not including f8ad3b76e923:
def _findparamvalue(function, param):
    for framerec in inspect.getouterframes(inspect.currentframe()):
        if framerec[3] == function:
            funcvars = inspect.getargvalues(framerec[0])[3]
            if not param in funcvars:
                raise util.Abort('No parameter named %s in function %s' % (param, function))
            return funcvars[param]
    raise util.Abort('No function named %s in stack' % function)
def _filename(f):
    if f:
        return f
    return _findparamvalue('_filter', 'filename')
def _ui(u):
    if u:
        return u
    return _findparamvalue('_filter', 'self').ui
def _repo(r):
    if r:
        return r
    return _findparamvalue('_filter', 'self')

def _trim(filename):
    return os.path.basename(filename)
def _cachedir():
    if 'HGEXTERNALCACHE' in os.environ:
        d = os.environ['HGEXTERNALCACHE']
    else:
        d = os.path.expanduser('~/.hgexternalcache')
    if not os.path.exists(d):
        os.makedirs(d)
    return d
def _sha1hash(data):
    return sha.new(data).hexdigest().upper()

def _download_to_cache(url, ui, sha1, filename, cachefile):
    ui.status('Downloading %s\n' % url)
    handle = urllib2.urlopen(url)
    data = handle.read()
    handle.close()
    if sha1 != _sha1hash(data):
        raise util.Abort('hash mismatch in %s' % filename)
    # XXX acquire write lock, or write to temp file and do atomic rename
    handle = open(cachefile, 'wb')
    handle.write(data)
    handle.close()
    return data

def download(s, cmd, ui=None, filename=None, **kwargs):
    # To support dev versions of Hg with f8ad3b76e923 but not f3a8b5360100:
    cmd = re.sub(r'^download: *', '', cmd)
    filename = _filename(filename)
    ui = _ui(ui)
    n = _trim(filename)
    m = re.match(r'^<<<EXTERNAL (([0-9A-F]{40})-([a-zA-Z0-9._+-]+))>>>\n$', s)
    if not m:
        raise util.Abort('malformed contents in %s' % filename)
    if n != m.group(3):
        raise util.Abort('incorrect basename in %s: %s' % (filename, m.group(3)))
    cachefile = os.path.join(_cachedir(), m.group(1))
    if os.path.exists(cachefile):
        handle = open(cachefile, 'rb')
        data = handle.read()
        handle.close()
        if m.group(2) != _sha1hash(data):
            try:
                os.remove(cachefile)
            except:
                pass
            raise util.Abort('hash mismatch in %s' % filename)
    else:
        data = _download_to_cache(url = cmd + m.group(1),
                                  ui = ui,
                                  sha1 = m.group(2),
                                  filename = filename,
                                  cachefile = cachefile)
    return data

def upload(s, cmd, ui=None, repo=None, filename=None, **kwargs):
    cmd = re.sub(r'^upload: *', '', cmd)
    filename = _filename(filename)
    ui = _ui(ui)
    repo = _repo(repo)
    n = _trim(filename)
    if not re.match(r'[a-zA-Z0-9._+-]+', n):
        raise util.Abort('unsupported file basename: %s', filename)
    sha1 = _sha1hash(s)
    full = '%s-%s' % (sha1, n)
    cachefile = os.path.join(_cachedir(), full)
    if not os.path.exists(cachefile):
        # XXX forces download and upload URLs to be related;
        # would be better to get download URL from download filter,
        # but that info is not trivially accessible here:
        m = re.match(r'(https?://)(([^:@]+)(:([^@]+))?@)?(.+)upload', cmd)
        if not m:
            raise util.Abort('malformed upload URL: %s' % cmd)
        downloadurl = m.group(1) + m.group(6) + full
        url = m.group(1) + m.group(6) + 'upload'
        try:
            _download_to_cache(url = downloadurl,
                               ui = ui,
                               sha1 = sha1,
                               filename = filename,
                               cachefile = cachefile)
            ui.status('No need to upload %s: %s already exists\n' % (filename, downloadurl))
        except IOError:
            # probably a 404, this is normal
            pm = httprepo.passwordmgr(ui)
            pm.add_password(None, url, m.group(3), m.group(5))
            ui.status('Uploading %s to %s (%s Kb)\n' % (filename, url, len(s) / 1024))
            auth = urllib2.HTTPBasicAuthHandler(pm)
            try:
                data = {'file': repo.wfile(filename)}
                # XXX support proxies; look at httprepo.httprepository.__init__
                # or http://www.hackorama.com/python/upload.shtml
                urllib2.build_opener(MultipartPostHandler, auth).open(url, data).close()
            except IOError, err:
                raise util.Abort('Problem uploading %s to %s (try it manually using a web browser): %s\n' % (filename, url, err))
            # Now ensure that the upload actually worked, by downloading:
            _download_to_cache(url = downloadurl,
                               ui = ui,
                               sha1 = sha1,
                               filename = filename,
                               cachefile = cachefile)
    return '<<<EXTERNAL %s>>>\n' % full

def reposetup(ui, repo):
    if not repo.local():
        return
    for name, fn in {'download:': download, 'upload:': upload}.iteritems():
        try:
            # Hg 0.9.6+ (with f8ad3b76e923):
            repo.adddatafilter(name, fn)
        except AttributeError:
            # Hg 0.9.5:
            util.filtertable[name] = fn

# --- FROM http://odin.himinbi.org/MultipartPostHandler.py ---
# (with some bug fixes!)
import urllib
import urllib2
import mimetools, mimetypes
import os, stat

class Callable:
    def __init__(self, anycallable):
        self.__call__ = anycallable

# Controls how sequences are uncoded. If true, elements may be given multiple values by
#  assigning a sequence.
doseq = 1

class MultipartPostHandler(urllib2.BaseHandler):
    handler_order = urllib2.HTTPHandler.handler_order - 10 # needs to run first

    def http_request(self, request):
        data = request.get_data()
        if data is not None and type(data) != str:
            v_files = []
            v_vars = []
            for(key, value) in data.items():
                try:
                     value.name
                     v_files.append((key, value))
                except AttributeError:
                     v_vars.append((key, value))

            if len(v_files) == 0:
                data = urllib.urlencode(v_vars, doseq)
            else:
                boundary, data = self.multipart_encode(v_vars, v_files)
                contenttype = 'multipart/form-data; boundary=%s' % boundary
                if(request.has_header('Content-Type')
                   and request.get_header('Content-Type').find('multipart/form-data') != 0):
                    print "Replacing %s with %s" % (request.get_header('content-type'), 'multipart/form-data')
                request.add_unredirected_header('Content-Type', contenttype)

            request.add_data(data)
        return request

    def multipart_encode(vars, files, boundary = None, buffer = None):
        if boundary is None:
            boundary = mimetools.choose_boundary()
        if buffer is None:
            buffer = ''
        for(key, value) in vars:
            buffer += '--%s\r\n' % boundary
            buffer += 'Content-Disposition: form-data; name="%s"' % key
            buffer += '\r\n\r\n' + value + '\r\n'
        for(key, fd) in files:
            filename = os.path.basename(fd.name)
            contenttype = mimetypes.guess_type(filename)[0] or 'application/octet-stream'
            buffer += '--%s\r\n' % boundary
            buffer += 'Content-Disposition: form-data; name="%s"; filename="%s"\r\n' % (key, filename)
            buffer += 'Content-Type: %s\r\n' % contenttype
            fd.seek(0)
            buffer += '\r\n' + fd.read() + '\r\n'
        buffer += '--%s--\r\n\r\n' % boundary
        return boundary, buffer
    multipart_encode = Callable(multipart_encode)

    https_request = http_request
# ------------------------------------------------------------

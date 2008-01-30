# XXX check compatibility; seems to be OK on Python 2.4.4, but not 2.4.2

import os, re, urllib2, sha, inspect

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

from mercurial import util, httprepo
# Compatibility for Hg not including give-filters-more-context.diff:
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
    # XXX permit this to be overridden by an environment variable
    d = os.path.expanduser('~/.hgexternalcache')
    if not os.path.exists(d):
        os.makedirs(d)
    return d
def _sha1hash(data):
    return sha.new(data).hexdigest().upper()
def download(s, cmd, filename=None, ui=None, **kwargs):
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
            # XXX delete cachefile
            raise util.Abort('hash mismatch in %s' % filename)
    else:
        url = cmd + m.group(1)
        ui.status('Downloading %s\n' % url)
        handle = urllib2.urlopen(url)
        data = handle.read()
        handle.close()
        if m.group(2) != _sha1hash(data):
            raise util.Abort('hash mismatch in %s' % filename)
        # XXX acquire write lock, or write to temp file and do atomic rename
        handle = open(cachefile, 'wb')
        handle.write(data)
        handle.close()
    return data
def upload(s, cmd, filename=None, ui=None, repo=None, **kwargs):
    filename = _filename(filename)
    ui = _ui(ui)
    repo = _repo(repo)
    n = _trim(filename)
    if not re.match(r'[a-zA-Z0-9._+-]+', n):
        raise util.Abort('unsupported file basename: %s', filename)
    full = '%s-%s' % (_sha1hash(s), n)
    cachefile = os.path.join(_cachedir(), full)
    if not os.path.exists(cachefile):
        handle = open(cachefile, 'wb')
        handle.write(s)
        handle.close()
        # XXX check if file exists on server; if so, don't try to upload it again
        url = cmd
        pm = httprepo.passwordmgr(ui)
        m = re.match(r'(https?://)(([^:@]+)(:([^@]+))?@)?(.+)', url)
        if m:
            url = m.group(1) + m.group(6)
            pm.add_password(None, url, m.group(3), m.group(5))
        ui.status('Uploading %s to %s (%s Kb)\n' % (filename, url, len(s) / 1024))
        auth = urllib2.HTTPBasicAuthHandler(pm)
        try:
            data = {'file': open(repo.wjoin(filename))}
            # XXX support proxies; look at httprepo.httprepository.__init__
            # or http://www.hackorama.com/python/upload.shtml
            urllib2.build_opener(MultipartPostHandler, auth).open(url, data).close()
        except IOError, err:
            ui.warn('Problem uploading %s to %s (try it manually using a web browser): %s\n' % (filename, url, err))
    return '<<<EXTERNAL %s>>>\n' % full
util.filtertable.update({
    'download:': download,
    'upload:': upload,
    })
# XXX for Hg 0.9.6+, try instead adding to reposetup: repo.adddatafilter(name, fn)
# --- FROM http://odin.himinbi.org/MultipartPostHandler.py ---
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
            try:
                 for(key, value) in data.items():
                     if type(value) == file:
                         v_files.append((key, value))
                     else:
                         v_vars.append((key, value))
            except TypeError:
                systype, value, traceback = sys.exc_info()
                raise TypeError, "not a valid non-string sequence or mapping object", traceback

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
            file_size = os.fstat(fd.fileno())[stat.ST_SIZE]
            filename = os.path.basename(fd.name)
            contenttype = mimetypes.guess_type(filename)[0] or 'application/octet-stream'
            buffer += '--%s\r\n' % boundary
            buffer += 'Content-Disposition: form-data; name="%s"; filename="%s"\r\n' % (key, filename)
            buffer += 'Content-Type: %s\r\n' % contenttype
            # buffer += 'Content-Length: %s\r\n' % file_size
            fd.seek(0)
            buffer += '\r\n' + fd.read() + '\r\n'
        buffer += '--%s--\r\n\r\n' % boundary
        return boundary, buffer
    multipart_encode = Callable(multipart_encode)

    https_request = http_request
# ------------------------------------------------------------

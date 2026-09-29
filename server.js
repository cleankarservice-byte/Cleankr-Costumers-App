const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.css': 'text/css',
  '.js': 'application/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.apk': 'application/vnd.android.package-archive',
  '.aab': 'application/octet-stream'
};

const server = http.createServer((req, res) => {
  let cleanUrl = req.url.split('?')[0];
  if (cleanUrl === '/' || cleanUrl === '') {
    cleanUrl = '/index.html';
  }

  const filePath = path.join(PUBLIC_DIR, cleanUrl);

  // Security check: ensure filePath is inside PUBLIC_DIR
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    res.end('Forbidden');
    return;
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('Not Found');
      return;
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    const filename = path.basename(filePath);

    const headers = {
      'Content-Type': contentType,
      'Content-Length': stats.size,
      'Last-Modified': stats.mtime.toUTCString(),
      'Accept-Ranges': 'bytes'
    };

    if (ext === '.apk' || ext === '.aab') {
      headers['Content-Disposition'] = `attachment; filename="${filename}"`;
    }

    if (req.method === 'HEAD') {
      res.writeHead(200, headers);
      res.end();
      return;
    }

    res.writeHead(200, headers);
    const readStream = fs.createReadStream(filePath);

    readStream.on('error', () => {
      if (!res.headersSent) {
        res.writeHead(500);
      }
      res.end();
    });

    req.on('close', () => {
      readStream.destroy();
    });

    readStream.pipe(res);
  });
});

server.on('error', (err) => {
  console.error('Server error:', err);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Cleankr download server running on port ${PORT}`);
});

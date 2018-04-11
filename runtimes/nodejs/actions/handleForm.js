const Busboy = require('busboy');

function main(args) {
  const busboy = new Busboy({
    headers: {
      'content-type': args.__ow_headers['content-type']
    },
  });

  const result = {};
  return new Promise((resolve, reject) => {
    busboy.on('file', (fieldname, file, filename, encoding, mimetype) => {
      file.on('data', (data) => {
        result.file = data;
      });
      file.on('end', () => {
        result.filename = filename;
        result.contentType = mimetype;
      });
    });

    busboy.on('field', (fieldname, value) => {
      result[fieldname] = value;
    });

    busboy.on('error', error => reject(`Parse error: ${error}`));
    busboy.on('finish', () => {
      resolve({
        Bucket: result.bucket,
        Key: result.key,
        Body: result.file,
      });
    })

    busboy.write(args.__ow_body, 'base64');
    busboy.end();
  });
}

exports.main = main;

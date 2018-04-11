function main(args) {
  return {
    statusCode: 200,
    headers: {
      'Content-Type': 'text/html'
    },
    body: `
    <html>
      <body>
        <p>Thank you for your input. Go look at your Cloud Object Storage bucket to see your uploaded file!</p>
      </body>
    </html>
    `
  };
}

exports.main = main;

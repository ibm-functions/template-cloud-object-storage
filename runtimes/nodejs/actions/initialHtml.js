function main(args) {
  return html(
    `<html>
      <body>
        <!-- Simple form which will send a POST request -->
        <form action="./upload-a-file-sequence" method="post" enctype="multipart/form-data">
          <br/><br/>
          <input id="POST-bucket" type="text" name="bucket" placeholder="COS Bucket Name" required>
          <br/><br/>
          <input id="POST-key" type="text" name="key"  placeholder="File name" required>
          <br/><br/>
          <input id="POST-body" type="file" name="body" required>
          <br/><br/>
          <input type="submit">
        </form>
      </body>
    </html>`
  )
}

function html(html) {
  return {
    statusCode: 200,
    headers: {
      'Content-Type': 'text/html',
      'Cache-Control': 'max-age=300',
    },
    body: html,
  };
}
exports.main = main;

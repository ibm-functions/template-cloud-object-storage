
const openwhisk = require('openwhisk');

async function main(args) {
  // invoke the COS get_signed_url action to get URLs for uploading and reading files.
  const actionName = '/_/cloud-object-storage/get-signed-url';
  const fileName = 'userProfileImg';
  const blocking = true;
  const options = { ignore_certs: 'true' };
  const ow = openwhisk(options);
  const params = { key: fileName, operation: 'putObject' };
  const putUrl = ow.actions.invoke({ actionName, blocking, params });
  params.operation = 'getObject';
  const getUrl = ow.actions.invoke({ actionName, blocking, params });
  try {
      results = await Promise.all([putUrl, getUrl])
  } catch (err) {
      console.log(err);
      throw err;
  }
  return getHtml(results[0].response.result.data, results[1].response.result.data)
}

function getHtml(theSignedUrlPut, theSignedUrlGet) {
  return html(
    `<html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
      </head>
      <body onload="setCurrentProfileImage()">
        <h4> Before uploading a file:</h4>
        <ul>
            <li>Create Cloud Object Storage HMAC Credentials ({hmac:true} on credential creation)</li>
            <li>Create a bucket on Cloud Object Storage</li>
            <li>Add the bucket name as a parameter to the get-signed-url action in the cloud-object-storage package</li>
            <li>Add CORS policy to bucket</li>
        </ul>
        <h4> Current Profile Image:</h4>
        <img src="http://via.placeholder.com/200x200" class="my-image" style="max-width: 200px; height: auto;"></img>
        <h4> Upload a file:</h4>
        <form id="myform" enctype="multipart/form-data">
          <input id="theFile" type="file" name="body" required>
        </form>
        <button onclick="getUrlAndUploadImage()"> Upload </button>
        <script>
          function setCurrentProfileImage() {
            fetch('${theSignedUrlGet}', {
                method: 'GET',
                headers: {
                  'Access-Control-Allow-Origin': '*',
                  'Access-Control-Allow-Methods': 'GET',
                },
                cache: false,
                processData: false,
                contentType: false
            })
            .then(response => response.blob())
            .then((response) => {
              if(response.type != 'application/xml') {
                  var objectURL = URL.createObjectURL(response);
                  var myImage = document.querySelector('.my-image');
                  myImage.src = objectURL;
              }
            })
          }
          function getUrlAndUploadImage() {
            const fileInput = document.getElementById('theFile');
            const file = fileInput.files[0];
            const myform = document.getElementById('myform');
            const fd = new FormData(myform);
            fetch('${theSignedUrlPut}', {
                method: 'PUT',
                body: file,
                headers: {
                  'Access-Control-Allow-Origin': '*',
                  'Access-Control-Allow-Methods': 'PUT',
                },
                cache: false,
                processData: false,
                contentType: false
            })
            .then((response) => {
              setCurrentProfileImage();
            })
            .catch(error => console.error('Error posting to presigned URL:', error))
          }
        </script>
      </body>
    </html>`
  );
}

function html(inputHtml) {
  return {
    statusCode: 200,
    headers: {
      'Content-Type': 'text/html',
      'Cache-Control': 'max-age=300',
    },
    body: inputHtml,
  };
}

exports.main = main;

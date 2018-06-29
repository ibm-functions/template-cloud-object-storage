/*
 * This action returns an html form for uploading a profile image to cloud object storage.
 * Cloud Functions actions accept a single parameter, which must be a JSON object.
 *
 * In this case, the args variable will look like:
 *   {
 *     "bucket": "your COS bucket name",
 *   }
 */
const openwhisk = require('openwhisk');

async function main(args) {
  const namespace = process.env.__OW_NAMESPACE;
  const getSignedUrlAction = `/${namespace}/cloud-object-storage/client-get-signed-url`;
  const putCORSAction = `/${namespace}/cloud-object-storage/bucket-cors-put`;
  const fileName = 'userProfileImg';
  const blocking = true;
  const params = { bucket: args.bucket };
  const ignore_certs = args.ignore_certs ? args.ignore_certs : false
  // Initialize the Openwhisk NPM package
  const ow = openwhisk({ ignore_certs });

  // set up cors configuration on the bucket
  params.corsConfig = {
    CORSRules: [{
      AllowedHeaders: ['*'],
      AllowedMethods: ['PUT', 'GET', 'DELETE'],
      AllowedOrigins: ['*'],
    }],
  };
  try {
    await ow.actions.invoke({ actionName: putCORSAction, blocking, params });
  } catch (err) {
    console.log(err);
    throw err;
  }

  // get signed urls for 'GET' and 'PUT' operations on bucket
  params.key = fileName;
  params.operation = 'putObject';
  delete params.corsConfig;
  const putUrl = ow.actions.invoke({ actionName: getSignedUrlAction, blocking, params });
  params.operation = 'getObject';
  const getUrl = ow.actions.invoke({ actionName: getSignedUrlAction, blocking, params });
  let results;
  try {
    results = await Promise.all([putUrl, getUrl]);
  } catch (err) {
    console.log(err);
    throw err;
  }
  // return the html with signed urls populated
  return getHtml(results[0].response.result.body, results[1].response.result.body)
}

function getHtml(theSignedUrlPut, theSignedUrlGet) {
  return html(
    `<html>
      <body onload="setCurrentProfileImage()">
        <h4> Before uploading a file:</h4>
        <ul>
            <li>Create Cloud Object Storage HMAC Credentials ({HMAC:true} on credential creation)</li>
            <li>Create a bucket in the Cloud Object Storage Service</li>
            <li>Add the bucket name as a parameter to this action</li>
        </ul>
        <h4> Current Profile Image:</h4>
        <img src="https://via.placeholder.com/200x200" class="my-image" style="max-width: 200px; height: auto;"></img>
        <h4> Upload a file:</h4>
        <form id="myform" enctype="multipart/form-data">
          <input id="theFile" type="file" name="body" required>
        </form>
        <button onclick="getUrlAndUploadImage()"> Upload </button>
        <script>
          function setCurrentProfileImage() {
            fetch('${theSignedUrlGet}')
            .then(response => response.blob())
            .then((response) => {
              if(response.type != 'application/xml') {
                  var myImage = document.querySelector('.my-image');
                  myImage.src = URL.createObjectURL(response);
              }
            })
          }
          function getUrlAndUploadImage() {
            const fileInput = document.getElementById('theFile');
            const file = fileInput.files[0];
            fetch('${theSignedUrlPut}', {
                method: 'PUT',
                body: file,
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
    headers: { 'Content-Type': 'text/html' },
    body: inputHtml,
  };
}

exports.main = main;

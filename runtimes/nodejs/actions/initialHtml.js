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
      AllowedOrigins: ['https://openwhisk.ng.bluemix.net'],
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
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
      </head>
      <body onload="setCurrentProfileImage()">
        <h4> Before uploading a file:</h4>
        <ul>
            <li>Create Cloud Object Storage HMAC Credentials ({hmac:true} on credential creation)</li>
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

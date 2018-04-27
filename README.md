# template-cloud-object-storage
# WIP DO NOT USE IN PRODUCTION
[![Build Status](https://travis-ci.org/ibm-functions/template-cloud-object-storage.svg?branch=master)](https://travis-ci.org/ibm-functions/template-cloud-object-storage)

### Overview
You can use the Cloud Object Storage template to deploy IBM Cloud Functions assets for you. The assets created by this template are described in the `manifest.yaml` file, which can be found at `template-cloud-object-storage/runtimes/nodejs/manifest.yaml`.

The Cloud Object Storage template creates a form where users can upload a photo into a Cloud Object Storage bucket. The template deploys a Web Action, *initialHtml*, that contains an html form, which passes its data to the *handleForm* Action. When the form data is parsed by the *handleForm* Action, the image uploaded in the form is stored in the Cloud Object Storage instance. Since this is a public Web action, be aware that *any* user can post photos into your bucket. For added security, you can [secure](https://console.bluemix.net/docs/openwhisk/openwhisk_webactions.html#securing-web-actions) the Web action.

### Prerequisites
The template expects to find a `cloud-object-storage` package that contains a *client-get-signed-url* and a *bucket-cors-put* function. You can download and deploy the `cloud-object-storage` package from [here](https://github.com/ibm-functions/package-cloud-object-storage).

Use the `wskdeploy` utility to deploy the template and package. In the future, `wskdeploy` will be integrated into a new `wsk` plug-in command `bx wsk deploy`.

For now, you can download it from [here](https://github.com/apache/incubator-openwhisk-wskdeploy/releases), and add `wskdeploy` to your PATH.

### Deploying the Template
After the `cloud-object-storage` package is deployed, use the `wskdeploy` tool to deploy the assets described in the `manifest.yaml` file. The `PACKAGE_NAME` value can be changed to your own package name.
```
pushd runtimes/nodejs/
PACKAGE_NAME=upload-a-photo wskdeploy
popd
```
Once the template is deployed, you can find the web address of the simple app by first going to the [Actions](https://console.bluemix.net/openwhisk/actions) page, selecting your action, and then clicking **Endpoints**.
Under the **Endpoints** tab you will find a web action section with a link. Copy this link without the .json suffix, and paste it into your browser's address bar.

Once the template is deployed, you can find the web address of the simple app by first going to the Actions page, selecting your action, and then clicking Endpoints.


You should now see a simple app for updating a user's profile picture. Go check out the application code to see how everything is working together, and expand this into your own app!

### Available Languages
The `cloud-object-storage` template is available in Node.js.

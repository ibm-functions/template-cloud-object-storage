# template-cloud-object-storage
# WIP DO NOT USE IN PRODUCTION
[![Build Status](https://travis-ci.org/ibm-functions/template-cloud-object-storage.svg?branch=master)](https://travis-ci.org/ibm-functions/template-cloud-object-storage)

### Overview
You can use the `cloud-object-storage` template to deploy IBM Cloud Functions assets for you.  The assets created by this template are described in the `manifest.yaml` file, which can be found at `template-cloud-object-storage/runtimes/nodejs/manifest.yaml`.

The template creates a form where users can upload a photo into a cloud object storage bucket.  The template deploys a Web Action, *initialHtml*, that contains an html form, which passes its data to the *handleForm* Action.  When the form data is parsed by the *handleForm* Action, the image uploaded in the form is stored in the Cloud Object Storage instance.  Because this is a public web action, keep in mind that any users can post photos into your bucket.  This web action could be [secured](https://console.bluemix.net/docs/openwhisk/openwhisk_webactions.html#securing-web-actions).

### Prerequisites
The template expects to find a `cloud-object-storage` package that contains an *object-write* function.  You can download and deploy the `cloud-object-storage` package from [here.](https://github.com/ibm-functions/package-cloud-object-storage)

You will use the `wskdeploy` utility to deploy the template and package.  In the future, `wskdeploy` will be integrated into a new `wsk` plugin command `bx wsk deploy`.

For now, you can download it from [here](https://github.com/apache/incubator-openwhisk-wskdeploy/releases), and add `wskdeploy` to your PATH.

### Deploying the Template
After you've deployed the cloud-object-storage package, use the `wskdeploy` tool to deploy the assets described in the `manifest.yaml` file.  `PACKAGE_NAME` can be changed to your own package name.
```
pushd runtimes/nodejs/
PACKAGE_NAME=upload-a-photo wskdeploy
popd
```

### Available Languages
The `cloud-object-storage` template is available in Node.js.

# template-cloud-object-storage
# WIP DO NOT USE IN PRODUCTION
[![Build Status](https://travis-ci.org/ibm-functions/template-cloud-object-storage.svg?branch=master)](https://travis-ci.org/ibm-functions/template-cloud-object-storage)

### Overview
You can use the `cloud-object-storage` template to deploy IBM Cloud Functions assets for you.  The assets created by this template are described in the `manifest.yaml` file, which can be found at `template-cloud-object-storage/runtimes/nodejs/manifest.yaml`.

The template expects to find a `cloud-object-storage` package that contains an *object-write* function.  You can download and deploy the `cloud-object-storage` package from [here.](https://github.com/ibm-functions/package-cloud-object-storage)

The template deploys a Web Action, *initialHtml*, that contains an html form, which passes its data to the *handleForm* Action.  When the form data is parsed by the *handleForm* Action, the image uploaded in the form is stored in the Cloud Object Storage instance.

You can use the `wskdeploy` tool to deploy this asset yourself using the `manifest.yaml` file.


### Available Languages
The `cloud-object-storage` template is available in Node.js.

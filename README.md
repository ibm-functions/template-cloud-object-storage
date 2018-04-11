# template-cloud-object-storage
# WIP DO NOT USE IN PRODUCTION
[![Build Status](https://travis-ci.org/ibm-functions/template-cloud-object-storage.svg?branch=master)](https://travis-ci.org/ibm-functions/template-cloud-object-storage)

### Overview
You can use this template to deploy some IBM Cloud Functions assets for you.  The assets created by this template are described in the manifest.yaml file, which can be found at `template-cloud-object-storage/runtimes/nodejs/manifest.yaml`

This template expects there to be a cloud-object-storage package containing an object-write function.  You can find and deploy that package from [here.](https://github.com/ibm-functions/package-cloud-object-storage)

This template will deploy a web action containing some html.  That html is a form, which will pass its data to the handleForm.js action.  When the form data is parsed by handle form, the image uploaded in the form is stored in the Cloud Object Storage instance.

You can use the wskdeploy tool to deploy this asset yourself using the manifest and available code.


### Available Languages
This template is available in node.js.

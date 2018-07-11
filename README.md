# template-cloud-object-storage
# WIP DO NOT USE IN PRODUCTION
[![Build Status](https://travis-ci.org/ibm-functions/template-cloud-object-storage.svg?branch=master)](https://travis-ci.org/ibm-functions/template-cloud-object-storage)

## Overview
You can use the Cloud Object Storage template to upload an image to a Cloud Object Storage bucket and then retrieve back a thumbnail of that image.

The IBM Cloud Functions assets created by this template are described in the `manifest.yaml` file, which can be found at `template-cloud-object-storage/runtimes/nodejs/manifest.yaml`.

The Cloud Object Storage template deploys a Cloud Functions Web Action, *app*, which creates a form where users can upload a photo into a Cloud Object Storage bucket. The web form passes its data to the *getUrlAndUploadImage* function. When the form data is parsed by the *getUrlAndUploadImage* Action, the image uploaded in the form is stored in the Cloud Object Storage instance. Since this is a public Web action, be aware that *any* user can post photos into your bucket. For added security, you can [secure](https://console.bluemix.net/docs/openwhisk/openwhisk_webactions.html#securing-web-actions) the Web action.
The web form will then retrieve back the image using the *setCurrentProfileImage* function and display the image within the web form.

## Deploying the template
  ### Deploying from the IBM Cloud CLI using the Cloud Functions plugin:
  #### Prerequisites
  The template expects to find a `cloud-object-storage` package that contains a *client-get-signed-url* and a *bucket-cors-put* function. You can download and deploy the `cloud-object-storage` package from [here](https://github.com/ibm-functions/package-cloud-object-storage).

  Use the `wskdeploy` utility to deploy the template and package. In the future, `wskdeploy` will be integrated into a new `wsk` plug-in command `bx wsk deploy`.

  For now, you can download it from [here](https://github.com/apache/incubator-openwhisk-wskdeploy/releases), and add `wskdeploy` to your PATH.

  #### Deploying the Template
  After the `cloud-object-storage` package is deployed, use the `wskdeploy` tool to deploy the assets described in the `manifest.yaml` file. The `PACKAGE_NAME` value can be changed to your own package name. The BUCKET is the bucket in your COS instance you want the template to use.
  ```
  pushd runtimes/nodejs/
  PACKAGE_NAME=upload-a-photo BUCKET=myBucket wskdeploy
  popd
  ```
  Once the template is deployed, you can find the web address of the simple app by first going to the [Actions](https://console.bluemix.net/openwhisk/actions) page, selecting your action, and then clicking **Endpoints**.
  Under the **Endpoints** tab you will find a web action section with a link. Copy this link without the .json suffix, and paste it into your browser's address bar.

  You should now see a simple app for updating a user's profile picture. Go check out the application code to see how everything is working together, and expand this into your own app!

  ### Deploying from the IBM Cloud Functions UI:
  1. To create this Template, go to the [**Upload Image** template](https://console.bluemix.net/openwhisk/create/template/upload-image) on the IBM Cloud Functions UI

  2. Next, provide a name for your package or use the provided default name `upload-image` and click **Next**.

   #### Depending on the Cloud Object Storage Package

   The Upload Image template relies on the Cloud Object Storage package. In order for the template to function properly      you must configure it to use the service credentials of an existing Cloud Object Storage instance. IMPORTANT NOTICE: Your      Cloud Object Storage instance must contain credentials with the HMAC keys present(see below) and at least one Bucket          present.
   There are three approaches you can take:
   
   * **Create an new instance**: Selecting this option will take you to the IBM Cloud page for creating Cloud Object Storage instances. It is important that after you create your instance you create a set of Service Credentials that contain the needed HMAC keys (See below)
   * **Input your own credentials**: Selecting this will prompt you to manually enter your own credentials for a COS instance
   * **Existing Instances**: If you already have any COS instances created they should be automatically populated in the dropdown. Clicking an existing instance will attempt to fetch the credentials as well as any Buckets existing on that instance
   ****Important Note****
   In order for the COS Template to be deployed properly your COS instance should have HMAC keys present as well as an already existing bucket.  For information on creating HMAC keys refer to this documentation: [Create COS Service Credentials](https://console.bluemix.net/docs/services/cloud-object-storage/iam/service-credentials.html#service-credentials)

  3. Once you input the information for your COS instance the **Deploy** button should be enabled and you can deploy the template. 

  4. After the template deploys you should be on the **Code** page for the *app* Action. You can then click on the **Endpoints** tab in the lefthand navigation. Under the **Endpoints** tab you will find a web action section with a link. Copy this link without the .json suffix, and paste it into your browser's address bar. This will display the template's web form for you to interact with. 

## Available Languages
The `cloud-object-storage` template is available in Node.js.

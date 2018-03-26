var CloudObjectStorage = require('ibm-cos-sdk');
function main(args) {
  let { cos, params } = getParamsCOS(args, CloudObjectStorage);
  return cos.deleteObject(params).promise();
}














function getPgetParamsCOS(args, COS) {
  let Bucket = args.bucket || args.Bucket;
  let Key = args.key || args.Key;
  let endpoint = args.endpoint || 's3-api.us-geo.objectstorage.softlayer.net';
  let ibmAuthEndpoint = args.ibmAuthEndpoint || 'https://iam.ng.bluemix.net/oidc/token';
  let apiKeyId = args.apikey || args.apiKeyId || args.__bx_creds["cloud-object-storage"].apikey;
  let serviceInstanceId = args.resource_instance_id || args.serviceInstanceId || args.__bx_creds["cloud-object-storage"].resource_instance_id;

  var params = args;
  params.Bucket = Bucket;
  params.Key = Key;
  delete params.__bx_creds;

  cos = cos || new COS.S3({ endpoint, ibmAuthEndpoint, apiKeyId, serviceInstanceId });
  return { cos, params };
}

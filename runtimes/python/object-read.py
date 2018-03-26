import sys
import json
import ibm_boto3
from ibm_botocore.client import Config

def main(args):
  resultsGetParams = getParamsCOS(args);
  cos = resultsGetParams['cos']
  params = resultsGetParams['params']
  object = cos.get_object(
    Bucket=params['Bucket'],
    Key=params['Key'],
  )
  return {'data': str(object['Body'].read()).encode(encoding='UTF-8')}


def getParamsCOS(args):
  endpoint = args.get("endpoint","https://s3-api.us-geo.objectstorage.softlayer.net")
  api_key_id = args.get("apikey", args.get("apiKeyId", "")) # or args['__bx_creds']['cloud-object-storage']
  service_instance_id = args.get("resource_instance_id", args.get("serviceInstanceId", "")) #or args.__bx_creds["cloud-object-storage"][resource_instance_id];
  ibm_auth_endpoint = args.get("ibmAuthEndpoint", 'https://iam.ng.bluemix.net/oidc/token')
  cos = ibm_boto3.client('s3',
    ibm_api_key_id=api_key_id,
    ibm_service_instance_id=service_instance_id,
    ibm_auth_endpoint=ibm_auth_endpoint,
    config=Config(signature_version='oauth'),
    endpoint_url=endpoint)
  params = {};
  params['Bucket'] = args['Bucket'];
  params['Key'] = args['Key'];

  # delete params.__bx_creds;
  return {'cos':cos, 'params':params};
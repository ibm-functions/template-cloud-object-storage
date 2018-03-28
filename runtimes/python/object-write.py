# This action will write to cloud object storage.  If the Cloud Object Storage
# service is not bound to this action or to the package containing this action,
# then you must provide the service information as argument input to this function.
# In this case, the params variable will look like:
#   {
#     "Bucket": "your COS bucket name",
#     "Key": "Name of the object to delete",
#     "Body": "Body of the object to write"
#   }

import sys
import json
import ibm_boto3
from ibm_botocore.client import Config

def main(args):
  resultsGetParams = getParamsCOS(args)
  cos = resultsGetParams['cos']
  params = resultsGetParams['params']
  object = cos.put_object(
    Body=params['Body'],
    Bucket=params['Bucket'],
    Key=params['Key'],
  )
  return {'data': str(object).encode(encoding='UTF-8')}


def getParamsCOS(args):
  endpoint = args.get('endpoint','https://s3-api.us-geo.objectstorage.softlayer.net')
  api_key_id = args.get('apikey', args.get('apiKeyId', args.get('__bx_creds', {}).get('cloud-object-storage', {}).get('apikey', '')))
  service_instance_id = args.get('resource_instance_id', args.get('serviceInstanceId', args.get('__bx_creds', {}).get('cloud-object-storage', {}).get('resource_instance_id', '')))
  ibm_auth_endpoint = args.get('ibmAuthEndpoint', 'https://iam.ng.bluemix.net/oidc/token')
  cos = ibm_boto3.client('s3',
    ibm_api_key_id=api_key_id,
    ibm_service_instance_id=service_instance_id,
    ibm_auth_endpoint=ibm_auth_endpoint,
    config=Config(signature_version='oauth'),
    endpoint_url=endpoint)
  params = {}
  params['Bucket'] = args['Bucket']
  params['Key'] = args['Key']
  params['Body'] = args['Body']
  return {'cos':cos, 'params':params}

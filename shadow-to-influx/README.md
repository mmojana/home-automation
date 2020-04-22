# AWS IoT Shadow to Influx

The goal of this small project is to provide an example of an AWS Lambda function that reacts to the updates to an [AWS device shadow](https://docs.aws.amazon.com/iot/latest/developerguide/iot-device-shadows.html) and sends the corresponding metrics to an [Influx](https://www.influxdata.com/) instance using its [REST API](https://v2.docs.influxdata.com/v2.0/write-data/#influxdb-api).

## Deploy and configuration

The steps to correctly deploy and configure this function are:

1. Create an Influx database instance on one of your servers or subscribe to a cloud solution.
2. Using the console of [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/), define a new secret that contains the API key to be used to call the Influx REST API. The value must not contain the "Token " prefix and the key must grant write access to the bucket you want to send the data to.
3. From the root folder (where the `pom.xml` file is located), compile the project with:
```
mvn clean package
```
4. Create a new lambda function in your AWS management console. The artifact to upload will be the "jar-with-dependencies" file in the target subfolder. Set the runtime to "Java 8" and the handler to `ch.mojanam.shadowtoinflux.ConverterFunction::handleRequest`.
5. Define the environment variables for the function:

|Key | Description | Example value |
|----|-------------|---------------|
|INFLUX_BASE_URL | The influx REST API endpoint prefix; `/api/v2/write` will be appended. | https://eu-central-1-1.aws.cloud2.influxdata.com/ |
| INFLUX_BUCKET | The name of the bucket where to write the data | home-automation |
| INFLUX_ORGANIZATION | The "organization" which contains the bucket | my-org |
| INFLUX_TOKEN_SECRET_NAME | The name of the AWS secret  | influxdb-cloud-api-key |
| INFLUX_TOKEN_SECRET_REGION | The AWS region where the secret above is defined | eu-central-1 |

6. In [AWS Identity and Access Management](https://aws.amazon.com/iam/) console, attach a new policy to the role of the lambda function just created (you can view it in the "Permission" tab of the lambda function):
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": "secretsmanager:GetSecretValue",
            "Resource": "<ARN_here>"
        }
    ]
}
```
Please replace `<ARN_here>`with the ARN of the secret defined at step 2. They normally start with `arn:aws:secretsmanager:`.

7. Add a trigger of type "AWS IoT" to the function.
Define a custom rule like the following:
```
SELECT topic(3)               as thingName,
       current.state.reported as reportedState
FROM '$aws/things/+/shadow/update/documents'
```
You can customize the query to return only some fields or to filters out some events. The function expects a `thingName` that will be used as "measurement" in Influx terms and a `reportedState` field with a map that will then generate the field set of the metric (see <https://v2.docs.influxdata.com/v2.0/reference/syntax/line-protocol/>).

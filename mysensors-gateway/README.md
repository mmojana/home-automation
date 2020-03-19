# MySensors gateway
`Dockerfile` for the [MySensors gateway](https://www.mysensors.org/build/raspberry) to be executed on a Raspberry 3 with an RFM69HCW radio. The settings can be easily changed, so the container can run on different devices/radios.



To build the image:
```
docker image build -t mysensors-gateway:1.0 .
```

To start a container without Docker Compose:
```
echo "00000000000000000000000000000000" > /tmp/secrets/aes_key
sudo docker run --privileged -p 5003:5003 -v /dev/mem:/dev/mem -v /tmp/secrets:/run/secrets -e AES_KEY_FILE=/run/secrets/aes_key mysensors-gateway:1.0
```
where `00000000000000000000000000000000` is the AES key to be used to encode/decode the network traffic. Of course any file can be used to store the AES key, just make the environment variable `AES_KEY_FILE` point to it.

To start a container with Docker Compose you would need to include in your `docker-compose.yml` something like:
```
environment:
  - AES_KEY_FILE=/run/secrets/aes_key
secrets:
  - aes_key
```

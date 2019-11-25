# MySensors gateway
`Dockerfile` for the [MySensors gateway](https://www.mysensors.org/build/raspberry) to be executed on a Raspberry 3 with an RFM69HCW radio. The settings can be easily changed, so the container can run on different devices/radios.

Requires a `mysensors.conf` file to be present on this folder. 

To build the image:
```
docker image build -t mysensors-gateway:1.0 .
```

To start a container:
```
sudo docker run --privileged -p 5003:5003 -v /dev/mem:/dev/mem mysensors-gateway:1.0
```


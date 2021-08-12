# daquila
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

If you want to connect a modbus device with a C2mon Server, you can use this project.

This DAQ is capable of connecting one or more modbus devices to a C2mon Server. Recommended is the use of the Docker image.
## How to get started:
1. Pull the Docker image
```
docker pull ghcr.io/genexarbeiter/daquila/daquila:latest
```
2. Create a file named config file named daq-config.yaml in the root folder of the docker container. (Of course you can also mount the file)
3. Modify the config for your purpose, here's an example of what it could (should) look like
```yaml
#Some basic configuration
general:
  c2monHost: 0.0.0.0 # host of the c2mon (required)
  c2monPort: 00000 # port of the c2mon (required)
  processName: P_EXAMPLE # process name (required)
  forceConfiguration: false # if true all c2mon configuration for the process will be overwritten on daq startup
  performanceMode: false # if true offset, multiplier and threshold checking is disabled for performance reasons

#Here you can configure the daq for your modbus equipment
equipments:
  - name: E_EXAMPLE # name of the modbus device (required)
    aliveTagInterval: 100000 # time in milliseconds an aliveTag is sent to C2mon (default: 100.000ms) (optional)
    refreshInterval: 10000 # time in milliseconds for refreshing all data tags (default: 10.000ms) (optional)
    type: modbus # type of the connection (default: modbus)
    connectionSettings: # connection of the modbus device 
      address: 1.1.1.1 # host of the modbus device (required)
      port: 502 # port of the modbus device (required)
      unitID: 1 # normally 1 (default: 1)
    # define all signals (data and commands) provided by the modbus interface
    signals:
      - name: MEASUREMENT_1 # name of the signal (required)
        type: s64 # type of the signal (s16, s32, s64, float32, float64, bool | <==> | signed int, float, boolean) (required)
        description: # optional
        modbus: # required
          type: read # type can be one of the following words: [read,write] (required)
          startAddress: 10004 # modbus address of the signal (required)
          register: holding # register type can be holding or coil (required)
          
      - name: COMMAND_1 # name of the signal (required)
        min: 0 # minimum value (required)
        max: 4294967296 # maximum value (required)
        type: s64 # type of the signal (s16, s32, s64, float32, float64, bool | <==> | signed int, float, boolean) (required)
        description: # description for the signal (optional)
        modbus: # required
          type: write # type can be one of the following words: [read,write] (required)
          startAddress: 10000 # modbus address of the signal (required)
          register: holding # register type can be holding or coil (required)
          
  - name: E_EXAMPLE_2 # name of the modbus device (required)
    aliveTagInterval: 100000 # time in milliseconds an aliveTag is sent to C2mon (default: 100.000ms) (optional)
    refreshInterval: 10000 # time in milliseconds for refreshing all data tags (default: 10.000ms) (optional)
    type: modbus # type of the connection (default: modbus)
    connectionSettings: # connection of the modbus device 
      address: 1.1.1.1 # host of the modbus device (required)
      port: 502 # port of the modbus device (required)
      unitID: 1 # normally 1 (default: 1)
    # define all signals (data and commands) provided by the modbus interface
    signals:
      - name: MEASUREMENT_1 # name of the signal (required)
        type: s64 # type of the signal (s16, s32, s64, float32, float64, bool | <==> | signed int, float, boolean) (required)
        description: # optional
        modbus: # required
          type: read # type can be one of the following words: [read,write] (required)
          startAddress: 10004 # modbus address of the signal (required)
          register: holding # register type can be holding or coil (required)
```
4. Start the container
```
docker start ghcr.io/genexarbeiter/daquila/daquila:latest
```

## License
```
MIT License

Copyright (c) 2021 Max Julian Meyer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

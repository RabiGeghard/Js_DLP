# JS_DLP

## ⚠️ Disclaimer

This project is for educational and research purposes only. Users should comply with their organization's security policies and applicable laws. The authors are not responsible for any misuse or damage caused by this software.

## Overview

JS_DLP is a research project exploring data transfer mechanisms in restricted environments. It demonstrates how JavaScript-based data transfer works in environments with strict Data Loss Prevention (DLP) controls.

## Prerequisites

### Hardware Requirements

1. Raspberry Pi 4
2. USB-C Power Data Adapter/Splitter
   - Recommended: Belkin USB-C Data Charge Adapter
   - [Product Link](https://www.belkin.com/p/usb-c-data-charge-adapter/WCZ002btWH.html)
3. USB-C to USB-A Male-to-Male Cable

![connect_schema](images/connect_psd.png "connect_schema")

### Software Requirements

- Raspberry Pi OS (latest version)
- Python 3.x
- Git
- Web browser

## Video 
[![Alt text](https://img.youtube.com/vi/DQVI1KEKvwQ/0.jpg)](https://www.youtube.com/watch?v=DQVI1KEKvwQ)

## Installation

### 1. Set up Raspberry Pi 4

Follow the official Raspberry Pi setup guide:
[Getting Started with Raspberry Pi](https://www.raspberrypi.com/documentation/computers/getting-started.html)

### 2. Connect Power Data Adapter

- Connect power input to power source
- Connect data cable to target PC

### 3. Install zero-hid

```bash
# Update system packages
```
```bash
sudo apt-get update
```
```bash
sudo apt-get install -y git python3-pip python3-venv
```
```bash

# Clone zero-hid repository
```
```bash
git clone https://github.com/thewh1teagle/zero-hid
```
```bash
cd zero-hid/usb_gadget
```
```bash
sudo ./installer
```
```bash
# Reboot system

# Set up Python virtual environment
```
```bash
python3 -m venv ~/venv
```
```bash
source ~/venv/bin/activate
```
```bash

# Install required packages
```
```bash
pip3 install zero-hid
```
```bash
pip3 install flask
```

### 4. Install JS_DLP

```bash
cd ~
```
```bash
git clone https://github.com/RabiGeghard/Js_DLP
```
```bash
cd Js_DLP/flask_app 
```
```bash
source ~/venv/bin/activate
```
```bash
python3 app.py
```

## Usage

### Sending Data

1. Open notepad on target pc
2. locate [local send data via browser](http://127.0.0.1:5000/) 
2. Load and save `receive.html`
3. Open `receive.html` in browser
4. Load `send.html`

### Receiving Data
- open `send.html` on target pc
- drop file into browser
- Use `js_dlp.apk` on Android device
- Or build from source code
- Received data is stored in `/downloads/js_dlp/`

### Troubleshooting Frame Scanning

If some frames cannot be scanned properly:

1. Uncheck "Auto" checkbox
2. Use left/right arrows to navigate missed frames
3. Monitor scanning statistics:
   - **Minimal**: First missed frame
   - **Current**: Last scanned frame
   - **Scored**: Number of scanned frames
   - **Total**: Total number of frames
   
## Technical Details

The system operates by:
1. Creating a USB HID interface using zero-hid
2. Establishing a web-based data transfer channel
3. Implementing frame-based data transmission
4. Providing Android-based receiver functionality

## Result

| Action | Size | Time | Speed |
|---------|--------|------|-------|
| In | 18.4k | 739s | 25 byte/sec |
| Out | 131.6k | 53s | 2,542 byte/sec |


## Contributing

Contributions are welcome! Please submit issues and pull requests on GitHub.

## License

This project is licensed under [appropriate license] - see LICENSE file for details.

## Acknowledgments

- zero-hid project contributors
- Raspberry Pi Foundation
- Flask development team


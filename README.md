# simple-transport-protocol
A simple transport protocol on top of a lower level communication stack.

## Running the application
Run Test called TransportProtocolTest from package transLayer and it will run a test on the abstract class implementation of the protocol


## Requirements
- the program requires JUnit, Log4j, Mockito, Maven and java.util classes.

## Ouputs
- no outputs are generated, n future we'll add support to send transmission logs to file. We currently log at info/error levels to console.


## Introduction
Write transport protocol on top of the provided link-layer that is capable of
reliable transmission of the data having arbitrary size.

The communication stack provides a link layer that is capable of transmitting
data in chunks of 20 bytes.
The link layer doesn't give any transmission order guaranties.
Data loss or corruption error in the link layer is unlikely but can occur.
The link-layer provides API consisting of the asynchronous callback on data
arrival and synchronous call to send data.

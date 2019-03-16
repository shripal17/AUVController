#!/usr/bin/python

import logging
import logging.handlers
import argparse
import sys
import os
import time
from bluetooth import *

class LoggerHelper(object):
    def __init__(self, logger, level):
        self.logger = logger
        self.level = level

    def write(self, message):
        if message.rstrip() != "":
            self.logger.log(self.level, message.rstrip())


def setup_logging():
    # Default logging settings
    LOG_FILE = "/home/pi/AUV/btservice.log"
    LOG_LEVEL = logging.INFO

    # Define and parse command line arguments
    argp = argparse.ArgumentParser(description="Raspberry PI Bluetooth Server")
    argp.add_argument("-l", "--log", help="log (default '" + LOG_FILE + "')")

    # Grab the log file from arguments
    args = argp.parse_args()
    if args.log:
        LOG_FILE = args.log

    # Setup the logger
    logger = logging.getLogger(__name__)
    # Set the log level
    logger.setLevel(LOG_LEVEL)
    # Make a rolling event log that resets at midnight and backs-up every 3 days
    handler = logging.handlers.TimedRotatingFileHandler(LOG_FILE,
        when="midnight",
        backupCount=3)

    # Log messages should include time stamp and log level
    formatter = logging.Formatter('%(asctime)s %(levelname)-8s %(message)s')
    # Attach the formatter to the handler
    handler.setFormatter(formatter)
    # Attach the handler to the logger
    logger.addHandler(handler)

    # Replace stdout with logging to file at INFO level
    sys.stdout = LoggerHelper(logger, logging.INFO)
    # Replace stderr with logging to file at ERROR level
    sys.stderr = LoggerHelper(logger, logging.ERROR)


# Main loop
def main():
    # Setup logging
    # setup_logging()
    
    # setup empty variables
    
    # accelerometer x, y, z
    acc = [0, 0, 0]
    
    # gyroscope x, y, z
    gyro = [0, 0, 0]
    
    # orientation: azimuth, pitch, roll
    orientation = [0, 0, 0]
    
    # light intensity
    light = 0
    
    # compass degrees
    direction = 0
    
    # We need to wait until Bluetooth init is done
    time.sleep(1)

    # Make device visible
    os.system("hciconfig hci0 piscan")

    # Create a new server socket using RFCOMM protocol
    server_sock = BluetoothSocket(RFCOMM)
    # Bind to any port
    server_sock.bind(("", 1))
    # Start listening
    server_sock.listen(1)
    
    operations = ["Acc,", "Gyr,", "LDi,", "Ori,"]

    # Get the port the server socket is listening
    port = server_sock.getsockname()[1]

    # The service UUID to advertise
    uuid = "00001101-0000-1000-8000-00805F9B34FB"

    # Start advertising the service
    advertise_service(server_sock, "AUV",
                       service_id=uuid,
                       service_classes=[uuid, SERIAL_PORT_CLASS],
                       profiles=[SERIAL_PORT_PROFILE])

    # Main Bluetooth server loop
    while True:
        print("Waiting for connection on RFCOMM channel %d" % port)

        try:
            client_sock = None

            # This will block until we get a new connection
            client_sock, client_info = server_sock.accept()
            print("Accepted connection from ", client_info)

            data = ""
            while True:
                # Read the data sent by the client
                data += client_sock.recv(1024)
                if ((len(data) > 0)):
                    # We are receiving multiple lines at once, hence, split each line as a 1 part
                    parts = data.split("\r\n")
                    for part in parts:
                        print(part)
                        if (operations[0] in part):
                            subparts = part.split(",")
                            acc[0] = float(subparts[1])
                            acc[1] = float(subparts[2])
                            acc[2] = float(subparts[3])
                            print(acc)
                            
                        elif (operations[1] in part):
                            subparts = part.split(",")
                            gyro[0] = float(subparts[1])
                            gyro[1] = float(subparts[2])
                            gyro[2] = float(subparts[3])
                            print(gyro)
                            
                        elif (operations[2] in part):
                            subparts = part.split(",")
                            light = float(subparts[1])
                            direction = float(subparts[2])
                            print(light, direction)
                            
                        elif (operations[3] in part):
                            subparts = part.split(",")
                            orientation[0] = float(subparts[1])
                            orientation[1] = float(subparts[2])
                            orientation[2] = float(subparts[3])
                            print(orientation)
                    
                    # clear the input string
                    data = ""

                # print "Received [%s]" % data

                # Handle the request
            
            
                # client_sock.send(response)
                # print "Sent back [%s]" % response

        except IOError:
            pass

        except KeyboardInterrupt:

            if client_sock is not None:
                client_sock.close()

            server_sock.close()

            print("Server going down")
            break

main()
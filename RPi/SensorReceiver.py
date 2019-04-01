#!/usr/bin/python

from bluetooth import *
import sys
import threading

class SensorReceiver(threading.Thread):
    
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
    
    client_sock = None
    server_sock = None
    port = 0
        
    def __init__(self):
        threading.Thread.__init__(self)
        # Make device visible
        os.system("sudo hciconfig hci0 piscan")
        
        # Create a new server socket using RFCOMM protocol
        self.server_sock = BluetoothSocket(RFCOMM)
        
        # Bind to any port
        self.server_sock.bind(("", PORT_ANY))
        
        # Start listening
        self.server_sock.listen(1)
        
        # Get the port the server socket is listening
        self.port = self.server_sock.getsockname()[1]

        # The service UUID to advertise
        uuid = "00001101-0000-1000-8000-00805F9B34FB"

        # Start advertising the service
        advertise_service(self.server_sock, "AUV",
                           service_id=uuid,
                           service_classes=[uuid, SERIAL_PORT_CLASS],
                           profiles=[SERIAL_PORT_PROFILE])
        
    def run(self):
        operations = ["Acc,", "Gyr,", "LDi,", "Ori,"]
        # Main Bluetooth server loop
        while True:
            print("Waiting for connection on RFCOMM channel %d" % self.port)

            try:
                # This will block until we get a new connection
                self.client_sock, client_info = self.server_sock.accept()
                print("Accepted connection from ", client_info)

                data = ""
                while True:
                    # Read the data sent by the client
                    data += self.client_sock.recv(1024)
                    if ((len(data) > 0)):
                        # We are receiving multiple lines at once, hence, split each line as a 1 part
                        parts = data.split("\r\n")
                        for part in parts:
                            print(part)
                            if (operations[0] in part):
                                subparts = part.split(",")
                                self.acc[0] = float(subparts[1])
                                self.acc[1] = float(subparts[2])
                                self.acc[2] = float(subparts[3])
                                print(self.acc)
                                
                            elif (operations[1] in part):
                                subparts = part.split(",")
                                self.gyro[0] = float(subparts[1])
                                self.gyro[1] = float(subparts[2])
                                self.gyro[2] = float(subparts[3])
                                print(self.gyro)
                                
                            elif (operations[2] in part):
                                subparts = part.split(",")
                                self.light = float(subparts[1])
                                self.direction = float(subparts[2])
                                print(self.light, self.direction)
                                
                            elif (operations[3] in part):
                                subparts = part.split(",")
                                self.orientation[0] = float(subparts[1])
                                self.orientation[1] = float(subparts[2])
                                self.orientation[2] = float(subparts[3])
                                print(self.orientation)
                        
                        # clear the input string
                        data = ""

            except IOError:
                pass

            except KeyboardInterrupt:
                self.stop()
                break
            
    def stop(self):
        if self.client_sock is not None:
            self.client_sock.close()

        self.server_sock.close()

        print("SensorReceiver down")
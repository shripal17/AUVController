#!/usr/bin/env python

import threading
import serial as ser


class FRDMInterface(threading.Thread):
    serial = None

    def __init__(self):
        threading.Thread.__init__(self)
        self.serial = ser.Serial("/dev/ttyS0", 115200)

    def send(self, subPump, horiServos, vertiServos):
        if self.serial is not None:
            self.serial.write("{},{},{}".format(subPump, horiServos, vertiServos))

    def stop(self):
        if self.serial is not None:
            self.serial.close()

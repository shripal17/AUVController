#!/usr/bin/env python

from time import sleep
from time import time

from FRDMInterface import FRDMInterface
from HatSM import HatSM
from SX127x.LoRa import *
from SensorReceiver import SensorReceiver


class MyLora(LoRa):
    def __init__(self):
        super(MyLora, self).__init__(True)

        self.sensorReceiver = SensorReceiver()
        self.lastRecTime = 0
        self.frdmInterface = FRDMInterface()
        self.shouldContinue = True
        self.subPumps = 0
        self.horiFlaps = 0
        self.horiDir = 0
        self.propMotor = 0
        self.mainPump = 0

        self.orientation = [0, 0, 0]

        self.hat = HatSM()

        self.set_mode(MODE.SLEEP)
        self.set_dio_mapping([0] * 6)
        print(self)

    def on_rx_done(self):
        print("\nRxDone")
        self.clear_irq_flags(RxDone=1)
        try:
            self.lastRecTime = time()
            payload = self.read_payload(nocheck=True)
            recvdString = ""
            for i in payload:
                recvdString += chr(int(i))
            parts = recvdString.split(",")
            self.horiFlaps = float(parts[0])
            self.horiDir = float(parts[1])
            self.propMotor = float(parts[2])
            self.mainPump = float(parts[3])
            self.subPumps = int(parts[4])

            print("HF %d" % self.horiFlaps)
            print("HD %d" % self.horiDir)
            print("PM %d" % self.propMotor)
            print("MP %d" % self.mainPump)
            print("SP %d" % self.subPumps)

            self.hat.updateSpeeds(self.propMotor, self.mainPump)
            self.frdmInterface.send(self.subPumps, self.horiFlaps, self.horiDir)
        finally:
            self.reset_ptr_rx()
            self.send_sensor_data()

    def on_tx_done(self):
        print("\nTxDone %d" % time())
        self.clear_irq_flags(TxDone=1)
        self.set_dio_mapping([0] * 6)
        self.set_mode(MODE.RXCONT)

    def on_rx_timeout(self):
        print("\non_RxTimeout")
        print(self.get_irq_flags())

    def start(self):
        self.sensorReceiver.start()
        self.lastRecTime = 0
        self.set_dio_mapping([0] * 6)
        self.set_mode(MODE.RXCONT)
        print("RXCONT")
        while self.shouldContinue:
            if time() - self.lastRecTime > 2:
                self.hat.updateSpeeds(0, 0)
                sleep(2)

    def stop(self):
        self.shouldContinue = False
        self.hat.teardown()
        self.frdmInterface.stop()
        self.sensorReceiver.stop()

    def send(self, data):
        try:
            print('sending {}'.format(data))
        finally:
            self.write_payload(data)
            self.set_dio_mapping([1, 0, 0, 0, 0, 0])
            self.set_mode(MODE.TX)

    def send_data(self, data):
        self.write_payload(data)
        self.set_dio_mapping([1, 0, 0, 0, 0, 0])
        self.set_mode(MODE.TX)

    def send_sensor_data(self):
        self.orientation = self.sensorReceiver.get_orientation()
        ori = "{},{},{}".format(self.orientation[0], self.orientation[1], self.orientation[2])
        to_send = []
        for c in ori:
            to_send.append(ord(c))
        self.send(to_send)


BOARD.setup()
lora = MyLora()
lora.set_freq(866.0)
"""lora.set_max_payload_length(15000)
lora.set_bw(BW.BW15_6)
lora.set_coding_rate(CODING_RATE.CR4_5)"""
# lora.set_spreading_factor(6)
lora.set_pa_config(pa_select=1)
lora.set_agc_auto_on(True)
try:
    lora.start()
except KeyboardInterrupt:
    print("Exiting")
finally:
    lora.stop()
    BOARD.teardown()
    exit(0)

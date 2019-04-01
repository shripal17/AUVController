#!/usr/bin/env python

from time import time
from time import sleep

class MyLora(LoRa):
    def __init__(self):
        super(MyLora, self).__init__(True)
        self.set_mode(MODE.SLEEP)
        self.set_dio_mapping([0] * 6)
        print(self)

    def on_rx_done(self):
        print("\nRxDone")
        self.clear_irq_flags(RxDone=1)
        try:
            self.lastRecTime = time()
            payload = self.read_payload(nocheck=True)
            data = bytes(payload).decode()
            print(str(data))
            
        finally:
            

    def on_tx_done(self):
        print("\nTxDone %d" % time())
        self.clear_irq_flags(TxDone=1)
        self.set_dio_mapping([0] * 6) 
        self.set_mode(MODE.RXCONT)

    def on_rx_timeout(self):
        print("\non_RxTimeout")
        print(self.get_irq_flags())

    def start(self):
        self.sensorReceiver = SensorReceiver()
        self.sensorReceiver.run()
        self.lastRecTime = 0
        while self.cont:
            if (time() - self.lastRecTime > 2):
                
                sleep(2)
            

    def stop(self):
        self.sensorReceiver.stop()

    def send(self, data):
        try:
            print('sending %d' % data)
        finally:
            self.write_payload([data])
            self.set_dio_mapping([1,0,0,0,0,0])
            self.set_mode(MODE.TX)

    def send_data(self, data):
        self.write_payload(data)
        self.set_dio_mapping([1,0,0,0,0,0])
        self.set_mode(MODE.TX)

BOARD.setup()
lora = MyLora()
lora.set_freq(866.0)
"""lora.set_max_payload_length(15000)
lora.set_bw(BW.BW15_6)
lora.set_coding_rate(CODING_RATE.CR4_5)"""
#lora.set_spreading_factor(6)
lora.set_pa_config(pa_select=1)
lora.set_agc_auto_on(True)
try:
    lora.start()
except KeyboardInterrupt:
    print("Exiting")
finally:
    lora.stop()
    BOARD.teardown()

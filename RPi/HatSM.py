from time import sleep

import RPi.GPIO as GPIO


class HatSM:
    AN2 = 13  # set pwm2 pin on MD10-Hat
    AN1 = 12  # set pwm1 pin on MD10-hat
    DIG2 = 24  # set dir2 pin on MD10-Hat
    DIG1 = 26  # set dir1 pin on MD10-Hat
    p1 = None
    p2 = None
    verbose = True

    def __init__(self, verbose=True):
        self.verbose = verbose
        GPIO.setmode(GPIO.BCM)  # GPIO numbering
        GPIO.setwarnings(False)  # enable warning from GPIO
        GPIO.setup(self.AN2, GPIO.OUT)  # set pin as output
        GPIO.setup(self.AN1, GPIO.OUT)  # set pin as output
        GPIO.setup(self.DIG2, GPIO.OUT)  # set pin as output
        GPIO.setup(self.DIG1, GPIO.OUT)  # set pin as output
        sleep(1)  # delay for 1 seconds
        self.p1 = GPIO.PWM(self.AN1, 100)  # set pwm for M1
        self.p2 = GPIO.PWM(self.AN2, 100)  # set pwm for M2

    def forward(self, speed=100):
        if self.verbose:
            print('Forward: %d' % speed)
        GPIO.output(self.DIG1, GPIO.LOW)  # set DIG1 as LOW, to control direction
        GPIO.output(self.DIG2, GPIO.LOW)  # set DIG2 as LOW, to control direction
        self.p1.start(speed)  # set speed for M1 at 100%
        self.p2.start(speed)  # set speed for M2 at 100%

    def backward(self, speed=100):
        if self.verbose:
            print('Backward: %d' % speed)
        GPIO.output(self.DIG1, GPIO.HIGH)  # set DIG1 as LOW, to control direction
        GPIO.output(self.DIG2, GPIO.HIGH)  # set DIG2 as LOW, to control direction
        self.p1.start(speed)  # set speed for M1 at 100%
        self.p2.start(speed)  # set speed for M2 at 100%

    def left(self, speed=100):
        if self.verbose:
            print('Left: %d' % speed)
        GPIO.output(self.DIG1, GPIO.HIGH)  # set DIG1 as LOW, to control direction
        GPIO.output(self.DIG2, GPIO.LOW)  # set DIG2 as LOW, to control direction
        self.p1.start(speed)  # set speed for M1 at 100%
        self.p2.start(speed)  # set speed for M2 at 100%

    def right(self, speed=100):
        if self.verbose:
            print('Right: %d' % speed)
        GPIO.output(self.DIG1, GPIO.LOW)  # set DIG1 as LOW, to control direction
        GPIO.output(self.DIG2, GPIO.HIGH)  # set DIG2 as LOW, to control direction
        self.p1.start(speed)  # set speed for M1 at 100%
        self.p2.start(speed)  # set speed for M2 at 100%

    def stop(self):
        if self.verbose:
            print('Stop')
        GPIO.output(self.DIG1, GPIO.LOW)  # set DIG1 as LOW, to control direction
        GPIO.output(self.DIG2, GPIO.LOW)  # set DIG2 as LOW, to control direction
        self.p1.start(0)  # set speed for M1 at 100%
        self.p2.start(0)  # set speed for M2 at 100%

    def updateSpeeds(self, propellor, pump):
        if propellor > 0:
            GPIO.output(self.DIG1, GPIO.HIGH)
        else:
            GPIO.output(self.DIG1, GPIO.LOW)
        self.p1.start(abs(propellor))

        if pump > 0:
            GPIO.output(self.DIG2, GPIO.HIGH)
        else:
            GPIO.output(self.DIG2, GPIO.LOW)
        self.p2.start(abs(pump))

    def teardown(self):
        self.stop()
        GPIO.cleanup()

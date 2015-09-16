import sys
import random
import os

def randomNumbers(numNumbers, minValue=0,maxValue=10,zeroPad=0):
    return "".join(map(str,[random.randrange(minValue, maxValue) for i in range(numNumbers)])).zfill(zeroPad)

numRecords = sys.argv[1]
nameFile = open("names.txt", "r")
names = [line.strip("\n") for line in nameFile]
with open("personnel.csv", "w") as f:
    f.write(numRecords + "\n")
    for i in range(int(numRecords)):
        name = random.choice(names)
        #names.remove(name)
        SSN = "-".join([randomNumbers(3), randomNumbers(2), randomNumbers(4)])
        birthday = "".join([randomNumbers(1,1,12,2), randomNumbers(1,1,31,2), randomNumbers(1,1925,2010)])
        f.write(",".join([name, birthday, SSN]) + "\n")



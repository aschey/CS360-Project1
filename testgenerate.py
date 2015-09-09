import sys
import random
import os

def randomNumbers(numNumbers):
    return "".join(map(str,[random.randrange(0,10) for i in range(numNumbers)]))

numRecords = sys.argv[1]
nameFile = open("names.txt", "r")
names = [line.strip("\n") for line in nameFile]
with open("personnel.csv", "w") as f:
    f.write(numRecords + "\n")
    for i in range(int(numRecords)):
        name = random.choice(names)
        #names.remove(name)
        SSN = "-".join([randomNumbers(3), randomNumbers(2), randomNumbers(4)])
        birthday = randomNumbers(8)
        f.write(",".join([name, SSN, birthday]) + "\n")



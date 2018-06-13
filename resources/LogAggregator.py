#!/usr/bin/python3
import os
import glob

class LogEntry:
	def __init__(self, date, time, base, counter, askExchange, bidExchange, ask, bid, percent):
		self.date = date.rstrip()
		self.time = time.rstrip()
		self.base = base.rstrip()
		self.counter = counter.rstrip()
		self.askExchange = askExchange.rstrip()
		self.ask = ask.rstrip()
		self.bidExchange = bidExchange.rstrip()
		self.bid = bid.rstrip()
		self.percent = percent.rstrip()

		if self.percent[-1] == '%':
			self.percent = self.percent[:-1]
		self.percent = float(self.percent)
		self.ask = float(self.ask)
		self.bid = float(self.bid)
		if self.askExchange[0] == '#':
			self.askExchange = self.askExchange[1:]
		if self.bidExchange[0] == '#':
			self.bidExchange = self.bidExchange[1:] 

	def toString(self):
		print('date:'+self.date+', time:'+self.time+', base:'+self.base+', counter:'+self.counter+', askExchange:'+self.askExchange+', bidExchange:'+self.bidExchange+', ask:'+self.ask+', bid:'+self.bid+', percent:'+self.percent)

def addEntry(entries, line, filename):
	global currentPair
	if not line:
		return

	# if the line is a pair, set currentPair
	if line[0] != ' ':
		currentPair = line
		return

	# if the line is an entry, append entries[]
	split = line.split()
	if len(split) != 8:
		return
	
	percent = split[0]		#   [0] 3.4000%  
	askExchange = split[1]	#   [1] #bitfinex 
							#   [2] Ask 
	ask = split[3]			#   [3] 7374.3 
							#   [4] vs 
	bidExchange = split[5]	#   [5] #bitbay  
							#   [6] Bid 
	bid = split[7]			#   [7] 7634

	date = filename[26:31]
	time = filename[33:37]
	currencies = currentPair.split('/')
	base = currencies[0]
	counter = currencies[1]

	entry = LogEntry(date, time, base, counter, askExchange, bidExchange, ask, bid, percent)
	entries.append(entry)

def exchangeTotals(entries):
	countDict = {}
	for entry in entries:
		if entry.percent < 25:
			if entry.askExchange in countDict.keys():
				countDict[entry.askExchange] += 1
			else:
				countDict[entry.askExchange] = 1
			if entry.bidExchange in countDict.keys():
				countDict[entry.bidExchange] += 1
			else:
				countDict[entry.bidExchange] = 1

	countList = sorted(countDict.items(), key=lambda x: x[1])

	for item in countList:
		print(str(item[1]) + '    ' + item[0])

def exchangeAsksBids(entries):
	askDict = {}
	bidDict = {}
	for entry in entries:
		if entry.percent < 25:
			if entry.askExchange in askDict.keys():
				askDict[entry.askExchange] += 1
			else:
				askDict[entry.askExchange] = 1
			if entry.bidExchange in bidDict.keys():
				bidDict[entry.bidExchange] += 1
			else:
				bidDict[entry.bidExchange] = 1

	askList = sorted(askDict.items(), key=lambda x: x[1])
	bidList = sorted(bidDict.items(), key=lambda x: x[1])

	print("ASKS")
	for item in askList:
		print('  ' + str(item[1]) + '    ' + item[0])
	print("BIDS")
	for item in bidList:
		print('  ' + str(item[1]) + '    ' + item[0])

def exchangePairs(entries):
	countDict = {}
	for entry in entries:
		exKey = ''
		if entry.percent < 25:
			if entry.askExchange < entry.bidExchange:
				exKey = entry.askExchange + "-" + entry.bidExchange
			else:
				exKey = entry.bidExchange + "-" + entry.askExchange

			if exKey in countDict.keys():
				countDict[exKey] += 1
			else:
				countDict[exKey] = 1

	countList = sorted(countDict.items(), key=lambda x: x[1])
	print("EXCHANGE PAIRS")
	for item in countList:
		print('  ' + str(item[1]) + '    ' + item[0])

def fiatEntries(entries):
	fiats = ['USD', 'EUR', 'NZD', 'JPY', 'GBD', 'PLN', 'CAD']
	fiatEntries = []
	for entry in entries:
		if entry.base in fiats or entry.counter in fiats:
			fiatEntries.append(entry)
	return fiatEntries

def cryptoEntries(entries):
	fiats = ['USD', 'EUR', 'NZD', 'JPY', 'GBD', 'PLN', 'CAD']
	cryptoEntries = []
	for entry in entries:
		if entry.base not in fiats and entry.counter not in fiats:
			cryptoEntries.append(entry)
	return cryptoEntries

def exchangeAskBidCountPerCurrency(entries):
	# Make a dict with a list of entries for every currency
	entriesByCurrency = {}
	for entry in entries:
		if entry.base not in entriesByCurrency.keys():
			entriesByCurrency[entry.base] = []
		entriesByCurrency[entry.askExchange].append(entry)

		if entry.counter not in entriesByCurrency.keys():
			entriesByCurrency[entry.counter] = []
		entriesByCurrency[entry.bidExchange].append(entry)

	for key, value in entriesByCurrency.items():
		print('******* ' + key)


def main():
	currentPair = ''
	cwd = os.getcwd()
	print(cwd)
	entries = []
	for filename in glob.glob('../../../logs/Comparison*'):
		logFile= open(filename)
		for line in logFile:
			addEntry(entries, line, filename)

	# number of exchange mentions, number of exchange mentions per currency, number of exchanges as asks, bids
	exchangeTotals(entries)
	exchangeAsksBids(entries)
	exchangePairs(entries)

	fEntries = []
	cEntries = []
	fEntries = fiatEntries(entries)
	cEntries = cryptoEntries(entries)

	print("\n\nFIAT ENTRIES " + str(len(fEntries)))
	exchangeTotals(fEntries)
	exchangePairs(fEntries)

	print("\n\nCRYPTO ENTRIES " + str(len(cEntries)))
	exchangeTotals(cEntries)
	exchangePairs(cEntries)

if __name__ == "__main__":
	print('executing LogAnalyzer')
	main()

# scrooge_coin
In ScroogeCoin, the central authority Scrooge receives transactions from users.
This program implements the logic used by Scrooge to process transactions and produce the ledger. 

This program is a part of Bitcoin and Cryptocurrency Technologies course provided by Princeton University. 

Scrooge Coin, in contrast to Bitcoin, is a centralized system, where Scrooge is responsible for validating transactions and processing them. He organizes transactions into time periods or blocks. In each block, Scrooge will receive a list of transactions, validate the transactions he receives, and publish a list of validated transactions.

Transaction class represents a ScroogeCoin transaction and has inner classes Transaction.Output and Transaction.Input. A transaction output consists of a value and a public key to which it is being paid. For the public keys, the built-in Java PublicKey class is used.

A transaction input consists of the hash of the transaction that contains the corresponding output, the
index of this output in that transaction (indices are simply integers starting from 0), and a digital
signature. For the input to be valid, the signature it contains must be a valid signature over the
current transaction with the public key in the spent output.


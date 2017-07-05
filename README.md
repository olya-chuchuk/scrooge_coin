# Scrooge Coin

In ScroogeCoin, the central authority Scrooge receives transactions from users.
This program implements the logic used by Scrooge to process transactions and produce the ledger. 

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Version numbers below indicate the versions used.

 * Java 1.8.0_131 (http://java.oracle.com)


### Installing

A step by step series of examples that tell you have to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system



# scrooge_coin
In ScroogeCoin, the central authority Scrooge receives transactions from users.
This program implements the logic used by Scrooge to process transactions and produce the ledger. 

This program is a part of Bitcoin and Cryptocurrency Technologies course provided by Princeton University. 

Scrooge Coin, in contrast to Bitcoin, is a centralized system, where Scrooge is responsible for validating transactions and processing them. He organizes transactions into time periods or blocks. In each block, Scrooge receives a list of transactions, validate the transactions he receives, and publish a list of validated transactions.

Transaction class represents a ScroogeCoin transaction and has inner classes Transaction.Output and Transaction.Input. A transaction output consists of a value and a public key to which it is being paid. For the public keys, the built-in Java PublicKey class is used.

A transaction input consists of the hash of the transaction that contains the corresponding output, the
index of this output in that transaction (indices are simply integers starting from 0), and a digital
signature. For the input to be valid, the signature it contains must be a valid signature over the
current transaction with the public key in the spent output.

A transaction consists of a list of inputs, a list of outputs and a unique ID (see the getRawTx()
method). The class also contains methods to add and remove an input, add an output, compute
digests to sign/hash, add a signature to an input, and compute and store the hash of the transaction
once all inputs/outputs/signatures have been added.

UTXO class represents an unspent transaction output. A UTXO
contains the hash of the transaction from which it originates as well as its index within that
transaction. a UTXOPool class represents the current set of outstanding
UTXOs and contains a map from each UTXO to its corresponding transaction output. 

I was responsible for creating a file called TxHandler.java that implements the following API:
```
public​ class​ TxHandler​ {
 /** Creates a public ledger whose current UTXOPool (collection of unspent
 * transaction outputs) is utxoPool. This should make a defensive copy of
 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
 */
 public​ TxHandler​(UTXOPool utxoPool);
 /** Returns true if
 * (1) all outputs claimed by tx are in the current UTXO pool,
 * (2) the signatures on each input of tx are valid,
 * (3) no UTXO is claimed multiple times by tx,
 * (4) all of tx’s output values are non-negative, and
 * (5) the sum of tx’s input values is greater than or equal to the sum of
 its output values; and false otherwise.
 */
 public​ boolean​ isValidTx​(Transaction tx);
 /** Handles each epoch by receiving an unordered array of proposed
 * transactions, checking each transaction for correctness,
 * returning a mutually valid array of accepted transactions,
 * and updating the current UTXO pool as appropriate.
 */
 public​ Transaction[] handleTxs​(Transaction[] possibleTxs);
}
```
Method handleTxs() returns a mutually valid transaction set of maximal size
(one that can’t be enlarged simply by adding more transactions). It does not compute a set of
maximum size (one for which there is no larger mutually valid transaction set).
Based on the transactions it has chosen to accept, handleTxs also updates its internal
UTXOPool to reflect the current set of unspent transaction outputs, so that future calls to
handleTxs() and isValidTx() are able to correctly process/validate transactions that claim
outputs from transactions that were accepted in a previous call to handleTxs().

Also, I created a second file called MaxFeeTxHandler.java whose handleTxs() method
finds a set of transactions with maximum total transaction fees -- i.e. maximized the sum over all
transactions in the set of (sum of input values - sum of output values)).



import java.util.*;

/**
 * Handler class, Scrooge itself, responsible for providing operations, 
 * validation of transactions. The center of the system that we have to trust!
 * @author AcerS
 *
 */
public class TxHandler {
    
    /**
     * The current UTXO Pool, which contains all UTXO at the current moment in the system
     */
    protected UTXOPool curUTXOPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        curUTXOPool = new UTXOPool(utxoPool);
    }
    
    protected static boolean isValidTxInPool(Transaction tx, UTXOPool pool) {
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        Set<UTXO> claimedUTXO = new HashSet<>();
        double inSum = 0;
        double outSum = 0;
        for(int i = 0; i < inputs.size(); ++i) {
            Transaction.Input in = inputs.get(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            // (1)
            if(!pool.contains(utxo)) {
                return false;
            }
            // (2)
            if(!Crypto.verifySignature(pool.getTxOutput(utxo).address, 
                    tx.getRawDataToSign(i), in.signature)) {
                return false;
            }
            // (3)
            if(claimedUTXO.contains(utxo)) {
                return false;
            }
            claimedUTXO.add(utxo);
            // (5)
            inSum += pool.getTxOutput(utxo).value;
        }
        for(Transaction.Output out: outputs) {
            // (4)
            if(out.value < 0) {
                return false;
            }
            // (5)
            outSum += out.value;
        }
        // (5)
        if(outSum > inSum) {
            return false; 
        }
        return true;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        return isValidTxInPool(tx, this.curUTXOPool);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Set<Transaction> txs = new HashSet<>(possibleTxs.length);
        for(Transaction t : possibleTxs) {
            txs.add(t);
        }
        ArrayList<Transaction> validTxs = new ArrayList<>();
        int processed;
        do {
            processed = 0;
            for(Transaction t : txs) {
                if(isValidTx(t)) {
                    process(t);
                    processed++;
                    validTxs.add(t);
                }
            }
        } while (txs.size() > 0 && processed > 0);
        return validTxs.toArray(new Transaction[validTxs.size()]);
    }
    
    /**
     * processes a valid Transaction {@param t} and
     * updates the current UTXO pool as appropriate.
     * @param t - Transaction to process, valid for the current moment
     */
    protected void process(Transaction t) {
        processInPool(t, curUTXOPool);
    }
    
    protected static void processInPool(Transaction t, UTXOPool pool) {
        if(t.getHash() == null) {
            t.finalize();
        }
        for(Transaction.Input in: t.getInputs()) {
            pool.removeUTXO(new UTXO(in.prevTxHash, in.outputIndex));
        }
        for(int i = 0; i < t.getOutputs().size(); ++i) {
            pool.addUTXO(new UTXO(t.getHash(), i), t.getOutput(i));
        }
    }
}

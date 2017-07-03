import java.util.*;

public class MaxFeeTxHandler extends TxHandler{
    
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        super(utxoPool);
    }

    /**
     * handle subset of possible transactions with the maximum sum of fees,
     * makes appropriate changes to the current UTXO pool
     * @param possibleTxs
     * @return processed subset
     */
    @Override
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        UTXOPool dummyPool = new UTXOPool(curUTXOPool);
        Transaction[] validTxs = getValidTransactions(possibleTxs, dummyPool);
        //all transactions are finalized at this moment
        State.txs = validTxs;
        State.fee = countFees(validTxs, dummyPool);
        State.set = new HashSet<>();
        State.toAdd = new HashSet<>();
        State.set.add(new State(validTxs.length, curUTXOPool));
        for(int i = 0; i < validTxs.length; ++i) {
            for(State st: State.set) {
                st.addNextTx();
            }
            State.set.addAll(State.toAdd);
            State.toAdd.clear();
            double minAns = 0;
            for(State st: State.set) {
                minAns = Double.max(minAns, st.curSum);
            }
            final double ans = minAns;
            State.set.removeIf((a) -> a.maxPosSum - ans < -1e-8);
        }
        State[] states = new State[State.set.size()];
        State.set.toArray(states);
        double maxSum = -1;
        int maxState = -1;
        for(int i = 0; i < states.length; ++i) {
            if(states[i].curSum > maxSum) {
                maxSum = states[i].curSum;
                maxState = i;
            }
        }              
        ArrayList<Transaction> ans = new ArrayList<>();
        for(int i = 0; i < validTxs.length; ++i) {
            if(states[maxState].state[i]) {
                ans.add(validTxs[i]);
            }
        }
        for(Transaction t: ans) {
            process(t);
        }
        return ans.toArray(new Transaction[ans.size()]);
    }
    
    /**
     * finds maximum subset of mutually valid transactions not considering double-spending
     * expands pool {@param pool{
     * @param possibleTxs
     * @param pool
     * @return founded subset in order needed for valid procession
     */
    private static Transaction[] getValidTransactions(Transaction[] possibleTxs, UTXOPool pool) {
        ArrayList<Transaction> txs = new ArrayList<>(possibleTxs.length);
        for(Transaction t: possibleTxs) {
            txs.add(t);
        }
        ArrayList<Transaction> valid = new ArrayList<>();
        ArrayList<Transaction> toRemove = new ArrayList<>();
        int processed;
        do {
            processed = 0;
            for(Transaction t: txs) {
                if(isValidTxInPool(t, pool)) {
                    processed++;
                    addOutputs(pool, t);
                    valid.add(t);
                    toRemove.add(t);
                }
            }
            txs.removeAll(toRemove);
            toRemove.clear();
        } while(txs.size() > 0 && processed > 0);
        return valid.toArray(new Transaction[valid.size()]);
    }
    
    /**
     * adds all output of transaction t without deleting its inputs from a pool
     * @param pool
     * @param t
     */
    private static void addOutputs(final UTXOPool pool, final Transaction t) {
        if(t.getHash() == null) {
            t.finalize();
        }
        for(int i = 0; i < t.getOutputs().size(); ++i) {
            pool.addUTXO(new UTXO(t.getHash(), i), t.getOutput(i));
        }
    }
    
    /**
     * Class representing a state in B&B algorithm
     * @author AcerS
     *
     */
    private static class State {
        static double[] fee;
        static Set<State> set;
        static Set<State> toAdd;
        static Transaction[] txs;
        // for i <= lastSet : 0 if excluded, 1 if included
        // for i > lastSet: -1 if invalid
        boolean state[];
        UTXOPool curPool;
        int lastSet;
        // current sum of fees of all included transactions
        double curSum;
        // maximum possible sum obtained from this state
        // that is curSum + all pending fees
        double maxPosSum;
        
        State(int length, UTXOPool pool) {
            state = new boolean[length];
            curPool = new UTXOPool(pool);
            lastSet = -1;
            curSum = 0;
            for(double d: fee) {
                maxPosSum += d;
            }
        }
        
        State(final State other) {
            state = Arrays.copyOf(other.state, other.state.length);
            curPool = new UTXOPool(other.curPool);
            lastSet = other.lastSet;
            curSum = other.curSum;
            maxPosSum = other.maxPosSum;
        }
        
        void addNextTx() {
            ++lastSet;
            if(isValidTxInPool(txs[lastSet], curPool)) {
                State copyState = new State(this);
                copyState.state[lastSet] = true;
                copyState.curSum += fee[lastSet];
                if(copyState.curSum - copyState.maxPosSum > 1e-8) {
                    throw new RuntimeException("CurSum > MaxPosSum\n" + this + copyState);
                }
                processInPool(txs[lastSet], copyState.curPool);
                toAdd.add(copyState);
            }
            state[lastSet] = false;
            maxPosSum -= fee[lastSet];
            if(curSum - maxPosSum > 1e-8) {
                throw new RuntimeException("CurSum > MaxPosSum\n" + this);
            }
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("State:");
            sb.append("\nlastSet : " + lastSet);
            sb.append("\n" + Arrays.toString(state));
            sb.append("\ncurSum : " + curSum);
            sb.append("\nmaxPosSum : " + maxPosSum);
            return sb.toString();
        }
    }
    
    private static double[] countFees(final Transaction[] txs, final UTXOPool pool) {
        double[] fee = new double[txs.length];
        for(int i = 0; i < txs.length; ++i) {
            Transaction t = txs[i];
            for(Transaction.Input in: t.getInputs()) {
                UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                fee[i] += pool.getTxOutput(utxo).value;
            }
            for(Transaction.Output out: t.getOutputs()) {
                fee[i] -= out.value;
            }
        }
        return fee;
    }
    
}

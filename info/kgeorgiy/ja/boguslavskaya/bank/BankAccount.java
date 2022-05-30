package info.kgeorgiy.ja.boguslavskaya.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

public class BankAccount implements Account, Serializable {
    private final String id;
    private int amount;

    public BankAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    public BankAccount(final String id, final int amount){
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }

    @Override
    public synchronized void changeAmount(int change) throws RemoteException {
        System.out.println("Changing amount for account " + id);
        this.amount += change;
    }
}

package info.kgeorgiy.ja.boguslavskaya.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPerson implements Person, Serializable {
    private final String name;
    private final String lastName;
    private final String passportId;
    private final Map<String, Account> accounts;

    public LocalPerson(String name, String lastName, String passportId, Map<String, Account> accounts) throws RemoteException {
        this(name, lastName, passportId);
        for (String id : accounts.keySet()){
            Account account = accounts.get(id);
            Account newAccount = new BankAccount(account.getId(), account.getAmount());
            this.accounts.put(id, newAccount);
        }
    }
    public LocalPerson(String name, String lastName, String passportId){
        this.name = name;
        this.lastName = lastName;
        this.passportId = passportId;
        this.accounts = new HashMap<>();
    }

    // :NOTE: AbstractPerson
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassportID() {
        return passportId;
    }

    public Account getPersonAccount(String subId) throws RemoteException {
        return accounts.get(subId);
    }

    public Map<String, Account> getMapOfAccounts() throws RemoteException {
        return accounts;
    }

    public void addNewAccount(String subId, Account account) throws RemoteException {
        accounts.put(subId, account);
    }
}

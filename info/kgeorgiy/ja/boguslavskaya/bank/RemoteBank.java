package info.kgeorgiy.ja.boguslavskaya.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<String, Account>> accountsByPersons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        System.out.println("Creating account " + id);
        String passportId = id.split(":")[0];
        final Account account = new BankAccount(id);
        Person person = persons.get(passportId);
        if (person == null){
            System.out.println("Can't find the owner of account " + id);
            return null;
        }
        if (accounts.putIfAbsent(id, account) == null) {
            accountsByPersons.get(passportId).put(id, account);
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }


    @Override
    public Person getPerson(String passportId, PersonType type) throws RemoteException{
        Person person = persons.get(passportId);
        if (type == PersonType.LOCAL){
            person =  new LocalPerson(person.getName(), person.getLastName(), person.getPassportID(), accountsByPersons.get(person.getPassportID()));
        }
        return person;
    }

    @Override
    public Person createPerson(String name, String lastName, String passportId, PersonType type) throws RemoteException{
        System.out.println("Creating " + (type == PersonType.LOCAL ? "local" : "remote") +" person " + lastName + " " + name);
        if (type == PersonType.REMOTE){
            final Person person = new RemotePerson(name, lastName, passportId);
            if (persons.putIfAbsent(passportId, person) == null) {
                accountsByPersons.put(passportId, new ConcurrentHashMap<>());
                UnicastRemoteObject.exportObject(person, port);
                return person;
            } else {
                return getPerson(passportId, PersonType.REMOTE);
            }
        } else {
            return new LocalPerson(name, lastName, passportId, new ConcurrentHashMap<>());
        }
    }

    @Override
    public Account createAccountOfPerson(String subId, Person person) throws RemoteException {
        System.out.println("Creating new person's account");
        Account account = new BankAccount(person.getPassportID() + ":" + subId);
        if (person instanceof LocalPerson){
            ((LocalPerson) person).addNewAccount(subId, account);
        } else {
            if (!persons.containsKey(person.getPassportID())){
                persons.put(person.getPassportID(), person);
                accountsByPersons.put(person.getPassportID(), new ConcurrentHashMap<>());
            }
            accountsByPersons.get(person.getPassportID()).put(person.getPassportID() + ":" + subId, account);
            if (accounts.putIfAbsent(person.getPassportID() + ":" + subId, account) == null) {
                UnicastRemoteObject.exportObject(account, port);
            }
        }
        return account;
    }

    @Override
    public Account getAccountByPerson(Person person, String subId) throws RemoteException {
        if (person instanceof LocalPerson){
            return ((LocalPerson) person).getPersonAccount(person.getPassportID() + ":" + subId);
        }
        return accountsByPersons.get(person.getPassportID()).get(person.getPassportID() + ":" + subId);
    }
}

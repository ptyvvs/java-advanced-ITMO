package info.kgeorgiy.ja.boguslavskaya.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Returns person by passport id.
     * @param passportId passport id
     * @param type {@link PersonType} type of returned person
     * @return person with specified passport id or {@code null} if such person does not exist
     */
    Person getPerson(String passportId, PersonType type) throws RemoteException;


    /**
     * Creates a given type person.
     * @param name name
     * @param lastName last name
     * @param passportId passport id
     * @param type {@link PersonType} to be created
     * @return {@link Person} which was created by given parameters.
     */
    Person createPerson(String name, String lastName, String passportId, PersonType type) throws RemoteException;

    /**
     * Creates account and connects it to the specified person.
     * @param subId id without person's passport id
     * @param person person
     * @return {@link Account} created by given person.
     */
    Account createAccountOfPerson(String subId, Person person) throws RemoteException;

    /**
     * Returns account of specified person with given id or {@code null} if account doesn't exists.
     * @param person {@link Person}
     * @param subId person's account id
     * @return {@link Account}
     */
    Account getAccountByPerson(Person person, String subId) throws RemoteException;
}
